import os
from dotenv import load_dotenv
from openai import OpenAI
import json

# --- 기본 설정 ---
load_dotenv()
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")
client = OpenAI(api_key=OPENAI_API_KEY)

# --- 분석할 한국어 텍스트 파일 읽기 ---
TEXT_FILE_PATH = '/Users/sejin/Desktop/어린왕자 2장.txt'

with open(TEXT_FILE_PATH, "r", encoding="utf-8") as f:
    korean_text_input = f.read().strip()

print(f"\n--- 텍스트 파일 읽기 완료: {TEXT_FILE_PATH} ---")
print(f"텍스트 길이: {len(korean_text_input)}자")

# --- 프롬프트 ---
prompt = f"""
Read the following [KOREAN_TEXT] and return a JSON with:
- core_atmosphere: The dominant mood (e.g., "Tense Chase").
- emotional_palette: A list of key emotions (e.g., "Hope", "Fear").
- musical_directions: A list of 3 varied ideas, each with:
    - genre: The suggested genre.
    - instruments: A list of key instruments.
    - keywords: A list of descriptive keywords.
Use English for all music terms.

[KOREAN_TEXT]
{korean_text_input}
"""

print("\n--- 예시 2: Role-Playing 테스트 ---")

try:
    response = client.chat.completions.create(
        model="gpt-4o-mini",
        messages=[
            {
                "role": "system",
                "content": "You are an expert Music Supervisor for major films, specializing in analyzing Korean content."
            },
            {
                "role": "user",
                "content": prompt
            }
        ],
        response_format={"type": "json_object"},
        temperature=0.7
    )

    output_json = json.loads(response.choices[0].message.content)
    print(json.dumps(output_json, indent=2, ensure_ascii=False))

except Exception as e:
    print(f"API 호출 중 오류 발생: {e}")

print("--- 테스트가 종료되었습니다. ---")
