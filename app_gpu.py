from fastapi import FastAPI, HTTPException
from fastapi.responses import FileResponse
from pydantic import BaseModel
from typing import List
import json
import urllib.parse

from music_prompt_generator import generate_music_prompt
from musicgen_service import generate_music, load_model

app = FastAPI(title="GCP Music Server")

# --- 데이터 모델 ---
# 음악 생성 요청 모델 
class MusicGenerationRequest(BaseModel):
    isbn: str
    chapter_number: int
    main_mood: str
    emotions: List[str]
    genres: List[str]
    instruments: List[str]
    tempo: List[str]
    keywords: List[str]

@app.on_event("startup")
async def startup_event():
    load_model()

# --- 파일 삭제 헬퍼 함수 ---
def remove_file(path: str):
    try:
        os.remove(path)
        print(f"🗑️ Cleaned up temp file: {path}")
    except Exception as e:
        print(f"⚠️ Failed to delete temp file: {str(e)}")

# --- 음악 생성 API ---
@app.post("/books/chapters/ai-music")
def handle_generate_music(req: MusicGenerationRequest):
    """
    Input: 분석 데이터 (JSON)
    Output: 
      - Body: .wav 파일
      - Header('X-Music-Metadata'): 프롬프트 및 선택된 옵션 (JSON String)
    """
    try:
        print(f"\n🎵 [CH{req.chapter_number}] GCP received music generation request from Java.")

        # 1. 음악 프롬프트 생성
        analysis_dict = req.model_dump()
        prompt_result = generate_music_prompt(analysis_dict)
        
        final_prompt = prompt_result["prompt"]
        print(f"📝 [CH{req.chapter_number}] Generated Prompt: {final_prompt}")

        # 2. 음악 생성
        filename = f"{req.isbn}_{req.chapter_number}.wav"
        file_path, elapsed_time = generate_music(final_prompt, filename, duration_sec=30.0)
        
        # 소수점 2자리로 다듬기
        elapsed_time = round(elapsed_time, 2)
        print(f"⏱️ Generation Time (from Service): {elapsed_time} sec")

        # 3. 음악 메타데이터 생성
        # (HTTP 헤더에는 한글이나 특수문자가 들어가면 안 되므로 URL Encoding 필수)
        metadata = {
            "isbn": req.isbn,
            "chapter_number": req.chapter_number,
            "main_mood": req.main_mood,
            "final_prompt": final_prompt,
            "selected_genres": prompt_result["selected_genres"],
            "selected_instruments": prompt_result["selected_instruments"],
            "selected_tempo": prompt_result["selected_tempo"],
            "selected_keywords": prompt_result["selected_keywords"],
            "generation_time_sec": elapsed_time
        }
        # JSON 직렬화 후 URL 인코딩
        metadata_json = json.dumps(metadata)
        metadata_encoded = urllib.parse.quote(metadata_json)

        # 4. 응답 전송
        response = FileResponse(
            path=file_path, 
            filename=filename, 
            media_type="audio/wav"
        )
        # 헤더 추가
        response.headers["X-Music-Metadata"] = metadata_encoded
        
        return response

    except Exception as e:
        print(f"❌ Error: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)