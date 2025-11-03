from fastapi import FastAPI, UploadFile, File, HTTPException
from analyze import analyze_text
from prompt_generator import generate_music_prompt
from musicgen_local import generate_music_local
from utils import extract_text_from_epub, save_text_locally
import os

app = FastAPI(title="AI Music E-Book Server (Local)")

@app.post("/upload_epub")
async def upload_epub(file: UploadFile = File(...)):
    try:
        content = await file.read()
        temp_path = os.path.join("output", file.filename)
        with open(temp_path, "wb") as f:
            f.write(content)
        return {"local_path": temp_path}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/analyze")
def analyze_epub(local_path: str):
    try:
        text = extract_text_from_epub(local_path)
        save_text_locally(text, os.path.basename(local_path).replace(".epub", ".txt"))
        analysis_json = analyze_text(text)
        return analysis_json
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/generate_prompt")
def generate_prompt(analysis_json: dict):
    try:
        prompt = generate_music_prompt(analysis_json)
        return {"music_prompt": prompt}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/generate_music")
def generate_music(music_prompt: str, filename: str):
    try:
        saved_path = generate_music_local(music_prompt, filename)
        return {"music_path": saved_path}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
