import os
from dotenv import load_dotenv

load_dotenv()

OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")

# 로컬 저장 경로
OUTPUT_DIR = os.path.join(os.getcwd(), "output")
TEXT_DIR = os.path.join(OUTPUT_DIR, "texts")

# 폴더 생성
os.makedirs(TEXT_DIR, exist_ok=True)
