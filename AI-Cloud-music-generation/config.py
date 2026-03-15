import os

# 현재 프로젝트의 기본 경로
BASE_DIR = os.path.dirname(os.path.abspath(__file__))

# 결과물 저장할 최상위 폴더
OUTPUT_DIR = os.path.join(BASE_DIR, "output")
# 음악 저장 경로
MUSIC_DIR = os.path.join(OUTPUT_DIR, "music")

# 폴더 생성
os.makedirs(MUSIC_DIR, exist_ok=True)