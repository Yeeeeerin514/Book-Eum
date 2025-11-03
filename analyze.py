import json
from openai import OpenAI
from config import OPENAI_API_KEY

client = OpenAI(api_key=OPENAI_API_KEY)

def analyze_text(text: str) -> dict:
    """
    EPUB 텍스트를 받아서 LLM으로 음악 분석 결과(JSON) 생성
    """
    prompt = f"""
    Analyze the [KOREAN_TEXT] and output a single JSON object.
    All JSON values must be in English.

    JSON Structure:
    {{
      "main_mood": "1 dominant mood",
      "emotions": ["2-3 emotions"],
      "genres": ["3-4 suitable music genres"],
      "instruments": ["3-5 suitable instruments"],
      "tempo": ["1-2 tempo descriptions"],
      "keywords": ["3-5 descriptive music keywords"]
    }}

    [KOREAN_TEXT]
    {text[:5000]}
    """

    response = client.chat.completions.create(
        model="gpt-4o-mini",
        messages=[
            {"role": "system", "content": "You are an expert text analyzer specializing in matching music to text."},
            {"role": "user", "content": prompt}
        ],
        response_format={"type": "json_object"},
        temperature=0.7
    )

    return json.loads(response.choices[0].message.content)
