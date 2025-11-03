import random

def generate_music_prompt(analysis_json: dict,
                          max_instruments=3,
                          max_keywords=2,
                          max_genres=2,
                          max_tempo=1) -> str:
    prompt_parts = []

    # 핵심 분위기 고정
    prompt_parts.append(analysis_json.get("main_mood", ""))

    # 장르 랜덤 샘플링
    genres = analysis_json.get("genres", [])
    if genres:
        prompt_parts.append(", ".join(random.sample(genres, min(max_genres, len(genres)))))

    # 악기 랜덤 샘플링
    instruments = analysis_json.get("instruments", [])
    if instruments:
        prompt_parts.append("with " + ", ".join(random.sample(instruments, min(max_instruments, len(instruments)))))

    # 템포 랜덤 샘플링
    tempo = analysis_json.get("tempo", [])
    if tempo:
        prompt_parts.append("at " + ", ".join(random.sample(tempo, min(max_tempo, len(tempo)))) + " tempo")

    # 키워드 랜덤 샘플링
    keywords = analysis_json.get("keywords", [])
    if keywords:
        prompt_parts.append("featuring " + ", ".join(random.sample(keywords, min(max_keywords, len(keywords)))))

    return ", ".join(prompt_parts)
