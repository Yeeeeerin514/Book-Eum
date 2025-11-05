from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Dict
import os
import json

from utils import extract_chapters_from_epub, save_text_locally
from analyze import analyze_text
from music_prompt_generator import generate_music_prompt
from musicgen_service import generate_music_local

app = FastAPI(title="AI Music E-Book Server (Local)")

class EpubPathRequest(BaseModel):
    epub_path: str

class AnalysisRequest(BaseModel):
    chapter_paths: List[str]

class MusicGenerationRequest(BaseModel):
    music_prompt: str
    chapter_number: int
    book_name: str

@app.post("/process_epub")
async def process_epub(request: EpubPathRequest):
    """
    Java 서버로부터 epub 파일 경로를 받아서 챕터별로 텍스트 추출
    """
    try:
        if not os.path.exists(request.epub_path):
            raise HTTPException(status_code=404, detail=f"EPUB file not found: {request.epub_path}")
        
        # 챕터별 텍스트 추출 및 저장
        chapter_info = extract_chapters_from_epub(request.epub_path)
        
        return {
            "status": "success",
            "book_name": os.path.basename(request.epub_path).replace(".epub", ""),
            "total_chapters": len(chapter_info),
            "chapters": chapter_info
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error processing EPUB: {str(e)}")
    
@app.post("/analyze_chapter")
async def analyze_chapter(chapter_path: str):
    """
    특정 챕터 텍스트 파일을 분석하여 음악 메타데이터 생성
    """
    try:
        if not os.path.exists(chapter_path):
            raise HTTPException(status_code=404, detail=f"Chapter file not found: {chapter_path}")
        
        # 텍스트 파일 읽기
        with open(chapter_path, "r", encoding="utf-8") as f:
            text = f.read()
        
        # OpenAI로 분석
        analysis_result = analyze_text(text)
        
        return {
            "status": "success",
            "chapter_path": chapter_path,
            "analysis": analysis_result
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error analyzing chapter: {str(e)}")

@app.post("/generate_prompt")
async def create_music_prompt(analysis_json: dict):
    """
    분석 결과로부터 MusicGen용 프롬프트 생성
    """
    try:
        prompt = generate_music_prompt(analysis_json)
        return {
            "status": "success",
            "music_prompt": prompt
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error generating prompt: {str(e)}")
    
@app.post("/generate_music")
async def generate_music(request: MusicGenerationRequest):
    """
    프롬프트로부터 음악 생성
    """
    try:
        # 파일명 생성: book_name_chapter_01.wav
        filename = f"{request.book_name}_chapter_{request.chapter_number:02d}.wav"
        
        # 음악 생성
        music_path = generate_music_local(request.music_prompt, filename)
        
        return {
            "status": "success",
            "music_path": music_path,
            "chapter_number": request.chapter_number
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error generating music: {str(e)}")    
    
@app.post("/process_full_pipeline")
async def process_full_pipeline(request: EpubPathRequest):
    """
    전체 파이프라인: EPUB 처리 → 분석 → 프롬프트 생성 → 음악 생성
    """
    try:
        # 1. EPUB 챕터 추출
        chapter_info = extract_chapters_from_epub(request.epub_path)
        book_name = os.path.basename(request.epub_path).replace(".epub", "")
        
        results = []
        
        # 2. 각 챕터별로 처리
        for idx, chapter in enumerate(chapter_info, start=1):
            # 2-1. 텍스트 읽기
            with open(chapter["text_path"], "r", encoding="utf-8") as f:
                text = f.read()
            
            # 2-2. 분석
            analysis = analyze_text(text)
            
            # 2-3. 프롬프트 생성
            prompt = generate_music_prompt(analysis)
            
            # 2-4. 음악 생성
            filename = f"{book_name}_chapter_{idx:02d}.wav"
            music_path = generate_music_local(prompt, filename)
            
            results.append({
                "chapter_number": idx,
                "chapter_title": chapter.get("title", f"Chapter {idx}"),
                "text_path": chapter["text_path"],
                "analysis": analysis,
                "prompt": prompt,
                "music_path": music_path
            })
        
        return {
            "status": "success",
            "book_name": book_name,
            "total_chapters": len(results),
            "results": results
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error in full pipeline: {str(e)}")
    
@app.get("/health")
async def health_check():
    return {"status": "healthy"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
