from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Dict, Optional
import os

from utils import extract_chapters_from_epub, save_text_locally
from analyze import analyze_text
from music_prompt_generator import generate_music_prompt
from musicgen_service import generate_music_local

app = FastAPI(title="AI Music E-Book Server")

# --- 도서 내용 분석 요청 ---

class BookAnalysisRequest(BaseModel):
    isbn: str
    epubFileUrl: str

class ChapterAnalysisResult(BaseModel):
    """챕터별 분석 결과 (DB 저장용)"""
    chapter_number: int
    chapter_title: str
    text_length: int
    main_mood: str
    emotions: List[str]
    genres: List[str]
    instruments: List[str]
    tempo: List[str]
    keywords: List[str]

class BookAnalysisResponse(BaseModel):
    """자바 서버로 반환할 전체 분석 결과"""
    isbn: str
    isAnalyzed: bool
    chapters: List[ChapterAnalysisResult]

class MusicGenerationRequest(BaseModel):
    """음악 생성 요청 (이미 분석된 책 기반)"""
    isbn: str
    title: str
    chapters: List[ChapterAnalysisResult]  # DB에서 가져온 분석 결과
    duration_sec: Optional[float] = 5.0

class ChapterMusicResult(BaseModel):
    """챕터별 음악 생성 결과"""
    chapter_number: int
    music_prompt: str
    music_path: str

class MusicGenerationResponse(BaseModel):
    """음악 생성 결과"""
    isbn: str
    title: str
    total_musics: int
    musics: List[ChapterMusicResult]

@app.post("/books/{isbn}/analyze", response_model=BookAnalysisResponse)
async def analyze_book(isbn: str, request: BookAnalysisRequest):
    """
    [자바 → 파이썬] 도서 내용 분석 요청
    
    - EPUB 파일로부터 챕터별 텍스트 추출
    - 각 챕터를 OpenAI로 분석 (main_mood, emotions, genres, instruments, tempo, keywords)
    - 분석 결과를 자바 서버로 반환 → DB 저장
    
    Args:
        isbn: 도서 ISBN (URL 파라미터)
        request: 도서 정보 (title, author, isbn, plot, fileUri)
    
    Returns:
        BookAnalysisResponse: 챕터별 분석 결과
    """
    try:
        epub_path = request.epubFileUrl
        # 1. EPUB 파일 존재 확인
        if not os.path.exists(epub_path):
            raise HTTPException(
                status_code=404, 
                detail=f"EPUB file not found: {epub_path}"
            )
        
        print(f"📚 Starting analysis for book: ISBN={isbn}")
        
        # 2. EPUB에서 챕터 추출
        chapters = extract_chapters_from_epub(epub_path)
        if not chapters:
            raise HTTPException(
                status_code=400,
                detail="No chapters found in EPUB file"
            )
        print(f"📖 Extracted {len(chapters)} chapters")
        
        # 3. 첫 번째 챕터만 분석
        first_chapter = chapters[0]
        print(f"🧠 Analyzing Chapter {first_chapter['chapter_number']}: {first_chapter['title']}")

        # 텍스트 읽기
        with open(first_chapter["text_path"], "r", encoding="utf-8") as f:
            text = f.read()

        # OpenAI 분석
        analysis = analyze_text(text)

        # 결과 생성
        result = ChapterAnalysisResult(
            chapter_number=first_chapter["chapter_number"],
            chapter_title=first_chapter["title"],
            text_length=first_chapter["text_length"],
            main_mood=analysis.get("main_mood", ""),
            emotions=analysis.get("emotions", []),
            genres=analysis.get("genres", []),
            instruments=analysis.get("instruments", []),
            tempo=analysis.get("tempo", []),
            keywords=analysis.get("keywords", [])
        )

        # 응답 생성
        response = BookAnalysisResponse(
            isbn=isbn,
            isAnalyzed=True,
            chapters=[result],
        )

        print(f"✅ Single chapter analyzed successfully: mood={result.main_mood}")
        return response
        
    except HTTPException:
        raise
    except Exception as e:
        print(f"❌ Error analyzing book: {str(e)}")
        raise HTTPException(
            status_code=500, 
            detail=f"Error analyzing book: {str(e)}"
        )

# --- 감정 기반 AI 음악 생성 요청
@app.post("/books/{isbn}/generate-music", response_model=MusicGenerationResponse)
async def generate_music_for_book(isbn: str, request: MusicGenerationRequest):
    """
    [자바 → 파이썬] 이미 분석된 책에 대해 새로운 음악 생성
    
    - DB에서 가져온 분석 결과로 새로운 프롬프트 구성 (랜덤 샘플링)
    - 각 챕터별 음악 생성
    - 생성된 음악 파일 경로를 자바 서버로 반환 → DB 업데이트
    
    Args:
        isbn: 도서 ISBN
        request: 음악 생성 요청 (DB에서 가져온 분석 결과 포함)
    
    Returns:
        MusicGenerationResponse: 챕터별 음악 생성 결과
    """
    try:
        print(f"🎵 Starting music generation for: {request.title} (ISBN: {isbn})")
        
        music_results = []
        
        for chapter in request.chapters:
            print(f"🎼 Generating music for Chapter {chapter.chapter_number}")
            
            # 1. 분석 결과를 dict 형식으로 변환
            analysis_dict = {
                "main_mood": chapter.main_mood,
                "emotions": chapter.emotions,
                "genres": chapter.genres,
                "instruments": chapter.instruments,
                "tempo": chapter.tempo,
                "keywords": chapter.keywords
            }
            
            # 2. 새로운 프롬프트 생성 (랜덤 샘플링으로 매번 다름)
            music_prompt = generate_music_prompt(analysis_dict)
            
            # 3. 음악 생성
            # 파일명: {isbn}_chapter_{chapter_number}_{timestamp}.wav
            import time
            timestamp = int(time.time())
            filename = f"{isbn}_chapter_{chapter.chapter_number:02d}_{timestamp}.wav"
            
            music_path = generate_music_local(
                music_prompt, 
                filename,
                duration_sec=request.duration_sec
            )
            
            music_results.append(ChapterMusicResult(
                chapter_number=chapter.chapter_number,
                music_prompt=music_prompt,
                music_path=music_path
            ))
            
            print(f"✅ Music generated for Chapter {chapter.chapter_number}: {filename}")
        
        response = MusicGenerationResponse(
            isbn=isbn,
            title=request.title,
            total_musics=len(music_results),
            musics=music_results
        )
        
        print(f"🎉 Music generation complete: {len(music_results)} tracks")
        
        return response
        
    except Exception as e:
        print(f"❌ Error generating music: {str(e)}")
        raise HTTPException(
            status_code=500,
            detail=f"Error generating music: {str(e)}"
        )

@app.post("/books/analyze-and-generate-music")
async def analyze_and_generate_music(request: BookAnalysisRequest):
    """
    [통합 엔드포인트] 분석 + 음악 생성 한 번에 처리
    (발표 시연용 - 빠른 테스트를 위해)
    """
    try:
        # 1. 도서 분석
        analysis_response = await analyze_book(request.isbn, request)
        
        # 2. 음악 생성 요청 구성
        music_request = MusicGenerationRequest(
            isbn=request.isbn,
            title=request.title,
            chapters=analysis_response.chapters
        )
        
        # 3. 음악 생성
        music_response = await generate_music_for_book(request.isbn, music_request)
        
        # 4. 통합 결과 반환
        return {
            "analysis": analysis_response,
            "music": music_response
        }
        
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Error in full pipeline: {str(e)}"
        )

@app.get("/health")
async def health_check():
    """서버 상태 확인"""
    return {
        "status": "healthy",
        "service": "AI Music E-Book Analysis Server"
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)