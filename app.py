from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Optional
import os
import time
import asyncio
import requests

from utils import extract_chapters_from_epub
from analyze import analyze_text
from music_prompt_generator import generate_music_prompt
from musicgen_service import generate_music_local, load_model

app = FastAPI(title="AI Music E-Book Server (Async Pipeline)")

# --- 데이터 모델 ---

class BookAnalysisRequest(BaseModel):
    isbn: str
    fileUrl: str

class ChapterAnalysisResult(BaseModel):
    chapter_number: int
    main_mood: str
    emotions: List[str]
    genres: List[str]
    instruments: List[str]
    tempo: List[str]
    keywords: List[str]

class ChapterMusicResult(BaseModel):
    chapter_number: int
    music_path: str
    main_mood: str
    selected_genres: List[str]
    selected_instruments: List[str]
    selected_tempo: List[str]
    selected_keywords: List[str]

# --- 자바 서버 URL 설정 ---
JAVA_ANALYSIS_CALLBACK_URL = "http://localhost:8080/api/python/analysis/callback"
JAVA_MUSIC_CALLBACK_URL = "http://localhost:8080/api/python/music/callback"

# --- 음악 생성 세마포어 (1개씩만 실행되게 제한) ---
music_generation_lock = asyncio.Semaphore(1)

# --- 음악 생성 모델 로드 ---
@app.on_event("startup")
async def startup_event():
    """
    서버 시작 시 한 번만 MusicGen 모델을 메모리에 로드
    """
    load_model()

# --- 도서 내용 분석 API (비동기 파이프라인 방식) ---
@app.post("/books/analyze")
async def analyze_book(request: BookAnalysisRequest):
    try:
        epub_path = request.fileUrl
        isbn = request.isbn

        if not os.path.exists(epub_path):
            raise HTTPException(status_code=404, detail=f"EPUB file not found: {epub_path}")

        print(f"📚 Starting async pipeline analysis for: ISBN={isbn}")

        chapters = extract_chapters_from_epub(epub_path)
        if not chapters:
            raise HTTPException(status_code=400, detail="No chapters found in EPUB file")

        print(f"📖 Extracted {len(chapters)} chapters")

        # 챕터별 순차 분석, 음악 생성은 병렬로 실행
        for ch in chapters:
            chapter_num = ch["chapter_number"]
            print(f"\n{'='*60}")
            print(f"🧠 [CH{chapter_num}] Starting analysis...")

            try:
                with open(ch["text_path"], "r", encoding="utf-8") as f:
                    text = f.read()

                # 텍스트 분석
                analysis = analyze_text(text)
                analysis_result = ChapterAnalysisResult(
                    chapter_number=chapter_num,
                    main_mood=analysis.get("main_mood", ""),
                    emotions=analysis.get("emotions", []),
                    genres=analysis.get("genres", []),
                    instruments=analysis.get("instruments", []),
                    tempo=analysis.get("tempo", []),
                    keywords=analysis.get("keywords", [])
                )

                print(f"✅ [CH{chapter_num}] Analysis completed - Mood: {analysis_result.main_mood}")

                # 자바 서버로 분석 결과 전송
                send_analysis_to_java(isbn, analysis_result)

                # 분석 끝나자마자 음악 생성 비동기 실행
                asyncio.create_task(
                    generate_music_for_chapter_async(
                        isbn=isbn,
                        chapter_num=chapter_num,
                        analysis_result=analysis_result
                    )
                )
                print(f"🎵 [CH{chapter_num}] Music generation task created\n")
                
                # 이벤트 루프에 제어권 양보
                await asyncio.sleep(0)

            except Exception as e:
                print(f"❌ [CH{chapter_num}] Error: {str(e)}")
                continue

        print(f"\n{'='*60}")
        print(f"🎉 All {len(chapters)} chapters analyzed.")

        return {
            "status": "success",
            "message": "Book analysis completed, music generation running in background",
            "isbn": isbn,
            "total_chapters": len(chapters)
        }

    except HTTPException:
        raise
    except Exception as e:
        print(f"❌ Pipeline error: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Pipeline error: {str(e)}")

# --- 자바 서버로 분석 결과 전송 ---
def send_analysis_to_java(isbn: str, analysis_result: ChapterAnalysisResult):
    try:
        payload = {
            "isbn": isbn,
            "chapter_number": analysis_result.chapter_number,
            "main_mood": analysis_result.main_mood,
            "emotions": analysis_result.emotions,
            "genres": analysis_result.genres,
            "instruments": analysis_result.instruments,
            "tempo": analysis_result.tempo,
            "keywords": analysis_result.keywords
        }

        response = requests.post(JAVA_ANALYSIS_CALLBACK_URL, json=payload, timeout=10)

        if response.status_code == 200:
            print(f"📤 [CH{analysis_result.chapter_number}] Analysis result sent to Java successfully")
        else:
            print(f"⚠️ [CH{analysis_result.chapter_number}] Java callback failed: {response.status_code}")
            print(f"   Response: {response.text}")

    except Exception as e:
        print(f"❌ [CH{analysis_result.chapter_number}] Failed to send to Java: {str(e)}")

# --- 비동기 음악 생성 ---
async def generate_music_for_chapter_async(isbn: str, chapter_num: int, analysis_result: ChapterAnalysisResult):
    """
    챕터 분석 완료 후 비동기적으로 음악 생성 및 자바 콜백
    단, 음악 생성은 동시에 하나씩만 실행되도록 제한
    """
    # 락 획득 전: 태스크가 대기 중임을 표시
    print(f"⏱️ [CH{chapter_num}] Music task queued, waiting for generation slot...")

    async with music_generation_lock:
        try:
            print(f"\n{'='*60}")
            print(f"🎼 [CH{chapter_num}] Async music generation started...")

            # 프롬프트 생성
            prompt_result = generate_music_prompt({
                "main_mood": analysis_result.main_mood,
                "emotions": analysis_result.emotions,
                "genres": analysis_result.genres,
                "instruments": analysis_result.instruments,
                "tempo": analysis_result.tempo,
                "keywords": analysis_result.keywords,
            })

            prompt = prompt_result["prompt"]
            print(f"📝 [CH{chapter_num}] Prompt: {prompt}")

            # 오래 걸리는 음악 생성은 스레드 풀로 실행 (논블로킹)
            loop = asyncio.get_event_loop()
            timestamp = int(time.time())
            filename = f"{isbn}_ch{chapter_num:02d}_{timestamp}.wav"

            music_path = await loop.run_in_executor(
                None, lambda: generate_music_local(prompt, filename, duration_sec=5.0)
            )

            print(f"✅ [CH{chapter_num}] Music generated: {music_path}")

            # 결과 구성 및 자바로 전송
            music_result = ChapterMusicResult(
                chapter_number=chapter_num,
                music_path=music_path,
                main_mood=prompt_result["main_mood"],
                selected_genres=prompt_result["selected_genres"],
                selected_instruments=prompt_result["selected_instruments"],
                selected_tempo=prompt_result["selected_tempo"],
                selected_keywords=prompt_result["selected_keywords"]
            )

            send_music_to_java(isbn, music_result)

        except Exception as e:
            print(f"❌ [CH{chapter_num}] Music generation error: {str(e)}")

# --- 자바 서버로 음악 결과 전송 ---
def send_music_to_java(isbn: str, music_result: ChapterMusicResult):
    try:
        payload = {
            "isbn": isbn,
            "chapter_number": music_result.chapter_number,
            "music_path": music_result.music_path,
            "main_mood": music_result.main_mood,
            "selected_genres": music_result.selected_genres,
            "selected_instruments": music_result.selected_instruments,
            "selected_tempo": music_result.selected_tempo,
            "selected_keywords": music_result.selected_keywords
        }

        response = requests.post(JAVA_MUSIC_CALLBACK_URL, json=payload, timeout=10)

        if response.status_code == 200:
            print(f"📤 [CH{music_result.chapter_number}] Music result sent to Java successfully")
        else:
            print(f"⚠️ [CH{music_result.chapter_number}] Java music callback failed: {response.status_code}")

    except Exception as e:
        print(f"❌ [CH{music_result.chapter_number}] Failed to send music to Java: {str(e)}")

@app.get("/health")
async def health_check():
    return {"status": "healthy", "mode": "async-pipeline"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
