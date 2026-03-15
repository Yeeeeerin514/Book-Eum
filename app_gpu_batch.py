import asyncio

from fastapi import FastAPI, HTTPException
from fastapi.responses import FileResponse
from pydantic import BaseModel
from typing import List
import json
import urllib.parse

from music_prompt_generator import generate_music_prompt
from musicgen_service_batch import generate_music_batch, load_model

app = FastAPI(title="GCP Music Server")

# --- 설정 ---
BATCH_SIZE = 4          # 최대 묶음 크기
BATCH_TIMEOUT = 5.0     # 요청이 다 안 차도 이 시간 지나면 바로 처리 (초)

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

# --- 내부 큐 관리를 위한 클래스 ---
class BatchItem:
    def __init__(self, prompt: str, filename: str, metadata: dict):
        self.prompt = prompt
        self.filename = filename
        self.metadata = metadata
        self.future = asyncio.Future() # 결과를 기다릴 비동기 객체

queue: asyncio.Queue = asyncio.Queue()

@app.on_event("startup")
async def startup_event():
    load_model()
    # 백그라운드 배치 프로세서 시작
    asyncio.create_task(process_batch_loop())

# --- 백그라운드 루프: 큐에서 꺼내서 배치 처리 ---
async def process_batch_loop():
    print("🚀 Batch Processor Started.")
    while True:
        batch_items: List[BatchItem] = []
        
        # 1. 첫 번째 아이템 기다리기 (Block)
        item = await queue.get() # !! 버퍼에서 하나를 꺼내는 것
        batch_items.append(item)
        
        # 2. 나머지 아이템 채우기 (Timeout 동안 들어오는 것들 모으기)
        try:
            while len(batch_items) < BATCH_SIZE:
                item = await asyncio.wait_for(queue.get(), timeout=BATCH_TIMEOUT)
                batch_items.append(item)
        except asyncio.TimeoutError:
            pass # 시간이 지나면 모인 것끼리 처리

        # 3. 배치 처리 실행
        if batch_items:
            prompts = [item.prompt for item in batch_items]
            filenames = [item.filename for item in batch_items]
            
            try:
                # GPU 연산
                paths, elapsed = await asyncio.to_thread(
                    generate_music_batch, prompts, filenames, duration_sec=30.0
                )
                
                # 4. 각 요청자에게 결과 돌려주기 (Future resolve)
                for i, item in enumerate(batch_items):
                    result = {
                        "path": paths[i],
                        "elapsed": elapsed, # 배치가 걸린 총 시간
                        "batch_size": len(batch_items)
                    }
                    if not item.future.done():
                        item.future.set_result(result)
                        
            except Exception as e:
                print(f"❌ Batch Processing Failed: {e}")
                for item in batch_items:
                    if not item.future.done():
                        item.future.set_exception(e)

# --- 음악 생성 API ---
@app.post("/books/chapters/ai-music")
async def handle_generate_music(req: MusicGenerationRequest):
    """
    Input: 분석 데이터 (JSON)
    Output: 
      - Body: .wav 파일
      - Header('X-Music-Metadata'): 프롬프트 및 선택된 옵션 (JSON String)
    """
    try:
        print(f"\n🎵 [CH{req.chapter_number}] GCP received music generation request from Java.")

        # 1. 프롬프트 생성
        analysis_dict = req.model_dump()
        prompt_result = generate_music_prompt(analysis_dict)
        final_prompt = prompt_result["prompt"]
        filename = f"{req.isbn}_{req.chapter_number}.wav"
        
        print(f"📝 [CH{req.chapter_number}] Generated Prompt: {final_prompt}")

        # 2. 메타데이터 준비 (결과 대기 전에 미리 준비)
        metadata_dict = {
            "isbn": req.isbn,
            "chapter_number": req.chapter_number,
            "main_mood": req.main_mood,
            "final_prompt": final_prompt,
            "selected_genres": prompt_result["selected_genres"],
            "selected_instruments": prompt_result["selected_instruments"],
            "selected_tempo": prompt_result["selected_tempo"],
            "selected_keywords": prompt_result["selected_keywords"],
        }

        # 3. 큐에 넣기
        item = BatchItem(final_prompt, filename, metadata_dict)
        await queue.put(item)

        # 4. 결과 수신
        result = await item.future # 백그라운드 배치 처리가 끝날 때까지 대기함.

        file_path = result["path"]
        elapsed_time = round(result["elapsed"], 2)
        batch_size = result["batch_size"]

        # 5. 메타데이터에 생성 정보 추가 (요청하신 부분)
        metadata_dict["generation_time_sec"] = elapsed_time  # 생성 소요 시간 (float)
        metadata_dict["batch_size"] = batch_size             # 배치 크기 (int)

        # 6. 헤더 인코딩 및 응답 전송
        metadata_json = json.dumps(metadata_dict)
        metadata_encoded = urllib.parse.quote(metadata_json)

        response = FileResponse(
            path=file_path, 
            filename=filename, 
            media_type="audio/wav"
        )
        response.headers["X-Music-Metadata"] = metadata_encoded
        
        print(f"📤 [CH{req.chapter_number}] Response sent. (Waited batch)")
        return response

    except Exception as e:
        print(f"❌ Error: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)