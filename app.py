from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List
import os
import asyncio
import requests

from utils import extract_chapters_from_epub
from analyze import analyze_text

app = FastAPI(title="AI Analysis & Prompt Server (Local)")

# --- Java 서버 설정 ---
JAVA_ANALYSIS_CALLBACK_URL = "http://localhost:8080/books/analyze/callback"


# --- 데이터 모델 ---

# 1. 책 분석 요청 모델
class BookAnalysisRequest(BaseModel):
    isbn: str
    file_url: str


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


# --- 도서 내용 분석 API ---
@app.post("/books/analyze")
async def analyze_book(request: BookAnalysisRequest):
    """
    EPUB을 받아 챕터별로 분석하고, 결과를 Java로 콜백 전송.
    """
    try:
        epub_path = request.file_url
        isbn = request.isbn

        if not os.path.exists(epub_path):
            raise HTTPException(status_code=404, detail=f"EPUB file not found: {epub_path}")

        print(f"📚 Starting analysis for: ISBN={isbn}")

        # 1. EPUB 파일 챕터별 파싱
        chapters = extract_chapters_from_epub(epub_path)
        if not chapters:
            raise HTTPException(status_code=400, detail="No chapters found in EPUB file")

        print(f"📖 Extracted {len(chapters)} chapters")

        # 2. 챕터별 순차 분석
        for ch in chapters:
            chapter_num = ch["chapter_number"]
            print(f"🧠 [CH{chapter_num}] Analyzing text...")

            try:
                with open(ch["text_path"], "r", encoding="utf-8") as f:
                    text = f.read()

                # OpenAI 텍스트 분석
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
                await asyncio.sleep(0.1)  # 다음 챕터 분석 전 잠시 대기 (시스템 부하 조절)

            except Exception as e:
                print(f"❌ [CH{chapter_num}] Error: {str(e)}")
                continue

        print(f"🎉 All {len(chapters)} chapters analyzed.")

        return {
            "status": "success",
            "message": "Analysis completed.",
            "isbn": isbn,
            "total_chapters": len(chapters)
        }

    except Exception as e:
        print(f"❌ Analysis processing error: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Analysis processing error: {str(e)}")


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


@app.get("/health")
async def health_check():
    return {"status": "healthy", "mode": "async-pipeline"}


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=8000)
