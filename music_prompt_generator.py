import random
from typing import Dict, List

def generate_music_prompt(analysis_json: dict,
                          max_instruments=3,
                          max_keywords=2,
                          max_genres=2,
                          max_tempo=1) -> Dict:
    """
    OpenAI 분석 결과를 MusicGen용 프롬프트로 변환
    """
    
    # 핵심 분위기 (항상 포함)
    main_mood = analysis_json.get("main_mood", "")
    
    # 장르 랜덤 샘플링
    all_genres = analysis_json.get("genres", [])
    selected_genres = random.sample(all_genres, min(max_genres, len(all_genres))) if all_genres else []
    
    # 악기 랜덤 샘플링
    all_instruments = analysis_json.get("instruments", [])
    selected_instruments = random.sample(all_instruments, min(max_instruments, len(all_instruments))) if all_instruments else []
    
    # 템포 랜덤 샘플링
    all_tempo = analysis_json.get("tempo", [])
    selected_tempo = random.sample(all_tempo, min(max_tempo, len(all_tempo))) if all_tempo else []
    
    # 키워드 랜덤 샘플링
    all_keywords = analysis_json.get("keywords", [])
    selected_keywords = random.sample(all_keywords, min(max_keywords, len(all_keywords))) if all_keywords else []
    
    # MusicGen용 프롬프트 문자열 생성
    prompt_parts = []
    
    if main_mood:
        prompt_parts.append(main_mood)
    
    if selected_genres:
        prompt_parts.append(", ".join(selected_genres))
    
    if selected_instruments:
        prompt_parts.append("with " + ", ".join(selected_instruments))
    
    if selected_tempo:
        prompt_parts.append("at " + ", ".join(selected_tempo) + " tempo")
    
    if selected_keywords:
        prompt_parts.append("featuring " + ", ".join(selected_keywords))
    
    prompt_string = ", ".join(prompt_parts)
    
    # 결과 반환 (구조화된 데이터 + 프롬프트 문자열)
    return {
        "prompt": prompt_string,                    # MusicGen에 전달할 문자열
        "main_mood": main_mood,                     # 핵심 분위기
        "selected_genres": selected_genres,         # 선택된 장르들
        "selected_instruments": selected_instruments, # 선택된 악기들
        "selected_tempo": selected_tempo,           # 선택된 템포
        "selected_keywords": selected_keywords      # 선택된 키워드들
    }