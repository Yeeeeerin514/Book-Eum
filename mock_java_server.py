from fastapi import FastAPI, Request
import uvicorn
import json
import time
import asyncio

app = FastAPI(title="Mock Java Server")

@app.post("/api/python/analysis/callback")
async def receive_analysis_callback(request: Request):
    """
    파이썬 서버에서 챕터 분석 결과를 받는 엔드포인트
    """
    data = await request.json()
    print("\n📥 [MOCK JAVA] 분석 결과 수신 ----------------------")
    print(json.dumps(data, ensure_ascii=False, indent=2))
    print("----------------------------------------------------\n")
    # 처리 시간 시뮬레이션 (비동기)
    await asyncio.sleep(0.5)
    return {"status": "received-analysis", "chapter_number": data.get("chapter_number")}

@app.post("/api/python/music/callback")
async def receive_music_callback(request: Request):
    """
    파이썬 서버에서 음악 생성 결과를 받는 엔드포인트
    """
    data = await request.json()
    print("\n🎵 [MOCK JAVA] 음악 결과 수신 ------------------------")
    print(json.dumps(data, ensure_ascii=False, indent=2))
    print("----------------------------------------------------\n")
    # 처리 시간 시뮬레이션 (비동기)
    await asyncio.sleep(0.5)
    return {"status": "received-music", "chapter_number": data.get("chapter_number")}

@app.get("/health")
async def health_check():
    return {"status": "mock-java-ok"}

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8080)