from fastapi import FastAPI, HTTPException, BackgroundTasks
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

app = FastAPI(title="AI Music E-Book Server")

# --- 데이터 모델 ---

# 1. 책 분석 요청 모델
class BookAnalysisRequest(BaseModel):
    isbn: str
    fileUrl: str

# 2. 책 분석 결과 모델 (Java 전송용)
class ChapterAnalysisResult(BaseModel):
    isbn: str
    chapter_number: int
    main_mood: str
    emotions: List[str]
    genres: List[str]
    instruments: List[str]
    tempo: List[str]
    keywords: List[str]

# 3. 음악 생성 요청 모델
class MusicGenerationRequest(BaseModel):
    isbn: str
    chapter_number: int
    main_mood: str
    emotions: List[str]
    genres: List[str]
    instruments: List[str]
    tempo: List[str]
    keywords: List[str]

# 4. 음악 생성 결과 모델 (Java 콜백용)
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

# --- 음악 생성 모델 로드 (서버 시작 시) ---
@app.on_event("startup")
async def startup_event():
    load_model()

# --- 1. 도서 내용 분서 API ---
@app.post("/books/chapters/analsis")
async def analyze_book(request: BookAnalysisRequest):
    """
    EPUB을 받아 챕터별로 분석하고, 결과를 Java로 콜백 전송.
    """
    try:
        epub_path = request.fileUrl
        isbn = request.isbn

        if not os.path.exists(epub_path):
            raise HTTPException(status_code=404, detail=f"EPUB file not found: {epub_path}")

        print(f"📚 Starting analysis for: ISBN={isbn}")

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
                    isbn=request.isbn,
                    chapter_number=chapter_num,
                    main_mood=analysis.get("main_mood", ""),
                    emotions=analysis.get("emotions", []),
                    genres=analysis.get("genres", []),
                    instruments=analysis.get("instruments", []),
                    tempo=analysis.get("tempo", []),
                    keywords=analysis.get("keywords", [])
                )

                print(f"✅ [CH{chapter_num}] Analysis completed. Mood: {analysis_result.main_mood}")

                # 자바 서버로 분석 결과 전송
                send_analysis_to_java(isbn, analysis_result)

                # 다음 챕터 분석 전 잠시 대기 (시스템 부하 조절)
                await asyncio.sleep(0.1)

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
            "message": "Book analysis pipeline finished. Check Java server for results.",
            "isbn": isbn,
            "total_chapters": len(chapters)
        }

    except HTTPException:
        raise
    except Exception as e:
        print(f"❌ Analysis processing error: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Analysis processing error: {str(e)}")
    
# --- 2. 음악 생성 API ---
@app.post("/books/chapters/ai-music")
async def request_music_generation(request: MusicGenerationRequest, background_tasks: BackgroundTasks):
    """
    Java 서버가 책 분석 결과를 토대로 음악 생성을 요청하는 엔드포인트.
    요청 즉시 응답(202)을 보내고, 실제 생성은 백그라운드에서 진행됨.
    """
    print(f"\n🎵 [CH{request.chapter_number}] Received music generation request from Java.")
    
    # 백그라운드 태스크로 음악 생성 로직 등록 (Java에게는 바로 응답)
    background_tasks.add_task(process_music_generation_task, request)
    
    return {
        "status": "accepted",
        "message": "Music generation started in background",
        "isbn": request.isbn,
        "chapter_number": request.chapter_number
    }

# --- 3. 내부 처리 및 콜백 함수들 ---
async def process_music_generation_task(req: MusicGenerationRequest):
    """
    실제 음악을 생성하고 Java로 완료 콜백을 보내는 백그라운드 작업
    """
    # 락 획득 대기 (GPU 메모리 보호)
    async with music_generation_lock:
        try:
            print(f"🎼 [CH{req.chapter_number}] Processing music generation task...")
            
            # 1. 프롬프트 생성
            # MusicPromptGenerator가 dict를 받도록 되어 있으므로 변환
            analysis_dict = {
                "main_mood": req.main_mood,
                "emotions": req.emotions,
                "genres": req.genres,
                "instruments": req.instruments,
                "tempo": req.tempo,
                "keywords": req.keywords
            }
            
            prompt_result = generate_music_prompt(analysis_dict)
            final_prompt = prompt_result["prompt"]
            print(f"📝 [CH{req.chapter_number}] Generated Prompt: {final_prompt}")

            # 2. 음악 생성 (blocking IO는 executor로 실행)
            loop = asyncio.get_event_loop()
            timestamp = int(time.time())
            filename = f"{req.isbn}_ch{req.chapter_number:02d}_{timestamp}.wav"

            music_path = await loop.run_in_executor(
                None, 
                lambda: generate_music_local(final_prompt, filename, duration_sec=5.0) # 길이는 필요에 따라 조정
            )
            
            print(f"✅ [CH{req.chapter_number}] Music generated at: {music_path}")

            # 3. 결과 객체 생성
            music_result = ChapterMusicResult(
                chapter_number=req.chapter_number,
                music_path=music_path,
                main_mood=prompt_result["main_mood"],
                selected_genres=prompt_result["selected_genres"],
                selected_instruments=prompt_result["selected_instruments"],
                selected_tempo=prompt_result["selected_tempo"],
                selected_keywords=prompt_result["selected_keywords"]
            )

            # 4. 자바 서버로 완료 알림 (콜백)
            send_music_to_java(req.isbn, music_result)

        except Exception as e:
            print(f"❌ [CH{req.chapter_number}] Music generation failed: {str(e)}")
            # 필요 시 자바 서버로 에러 상태 전송하는 로직 추가 가능


def send_analysis_to_java(isbn: str, analysis_result: ChapterAnalysisResult):
    """책 분석 결과 전송"""
    try:
        payload = analysis_result.model_dump()
        payload["isbn"] = isbn

        response = requests.post(JAVA_ANALYSIS_CALLBACK_URL, json=payload, timeout=5)

        if response.status_code == 200:
            print(f"📤 [CH{analysis_result.chapter_number}] Analysis sent to Java.")
        else:
            print(f"⚠️ [CH{analysis_result.chapter_number}] Java analysis callback failed: {response.status_code}")
    except Exception as e:
        print(f"❌ [CH{analysis_result.chapter_number}] Failed to send to Java: {str(e)}")

async def generate_music_for_chapter_async(isbn: str, chapter_num: int, analysis_result: ChapterAnalysisResult):
    """음악 생성 결과 전송"""
    try:
        payload = music_result.dict()
        payload["isbn"] = isbn

        response = requests.post(JAVA_MUSIC_CALLBACK_URL, json=payload, timeout=10)

        if response.status_code == 200:
            print(f"📤 [CH{music_result.chapter_number}] Music result sent to Java.")
        else:
            print(f"⚠️ [CH{music_result.chapter_number}] Java music callback failed: {response.status_code}")
    except Exception as e:
        print(f"❌ [CH{music_result.chapter_number}] Failed to send music result to Java: {str(e)}")

# --- 자바 서버로 음악 결과 전송 ---
def send_music_to_java(isbn: str, music_result: ChapterMusicResult):
    try:
        payload = music_result.model_dump()
        payload["isbn"] = isbn

        response = requests.post(JAVA_MUSIC_CALLBACK_URL, json=payload, timeout=10)

        if response.status_code == 200:
            print(f"📤 [CH{music_result.chapter_number}] Music result sent to Java.")
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
