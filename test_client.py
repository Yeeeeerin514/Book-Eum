import requests
import json

# Python 서버 URL
BASE_URL = "http://localhost:8000"

def test_full_pipeline(epub_path: str):
    """
    전체 파이프라인 테스트
    """
    print("=" * 50)
    print("🚀 Starting Full Pipeline Test")
    print("=" * 50)
    
    # 전체 파이프라인 실행
    response = requests.post(
        f"{BASE_URL}/process_full_pipeline",
        json={"epub_path": epub_path}
    )
    
    if response.status_code == 200:
        result = response.json()
        print(f"\n✅ Success! Processed {result['total_chapters']} chapters")
        print(f"Book: {result['book_name']}")
        
        for chapter in result['results']:
            print(f"\n--- Chapter {chapter['chapter_number']}: {chapter['chapter_title']} ---")
            print(f"Text: {chapter['text_path']}")
            print(f"Prompt: {chapter['prompt']}")
            print(f"Music: {chapter['music_path']}")
        
        return result
    else:
        print(f"❌ Error: {response.status_code}")
        print(response.text)
        return None

def test_step_by_step(epub_path: str):
    """
    단계별 테스트
    """
    print("=" * 50)
    print("🔍 Starting Step-by-Step Test")
    print("=" * 50)
    
    # 1. EPUB 처리
    print("\n📖 Step 1: Processing EPUB...")
    response = requests.post(
        f"{BASE_URL}/process_epub",
        json={"epub_path": epub_path}
    )
    
    if response.status_code != 200:
        print(f"❌ Failed at step 1: {response.text}")
        return
    
    epub_result = response.json()
    print(f"✅ Extracted {epub_result['total_chapters']} chapters")
    
    # 첫 번째 챕터만 테스트
    if epub_result['total_chapters'] == 0:
        print("❌ No chapters found")
        return
    
    first_chapter = epub_result['chapters'][0]
    print(f"\n📝 Testing with: {first_chapter['title']}")
    
    # 2. 챕터 분석
    print("\n🧠 Step 2: Analyzing chapter...")
    response = requests.post(
        f"{BASE_URL}/analyze_chapter",
        params={"chapter_path": first_chapter['text_path']}
    )
    
    if response.status_code != 200:
        print(f"❌ Failed at step 2: {response.text}")
        return
    
    analysis_result = response.json()
    print(f"✅ Analysis complete:")
    print(json.dumps(analysis_result['analysis'], indent=2))
    
    # 3. 프롬프트 생성
    print("\n✍️ Step 3: Generating music prompt...")
    response = requests.post(
        f"{BASE_URL}/generate_prompt",
        json=analysis_result['analysis']
    )
    
    if response.status_code != 200:
        print(f"❌ Failed at step 3: {response.text}")
        return
    
    prompt_result = response.json()
    print(f"✅ Prompt: {prompt_result['music_prompt']}")
    
    # 4. 음악 생성
    print("\n🎵 Step 4: Generating music...")
    response = requests.post(
        f"{BASE_URL}/generate_music",
        json={
            "music_prompt": prompt_result['music_prompt'],
            "chapter_number": 1,
            "book_name": epub_result['book_name']
        }
    )
    
    if response.status_code != 200:
        print(f"❌ Failed at step 4: {response.text}")
        return
    
    music_result = response.json()
    print(f"✅ Music saved to: {music_result['music_path']}")
    
    print("\n" + "=" * 50)
    print("🎉 All steps completed successfully!")
    print("=" * 50)

def test_health():
    """
    서버 상태 확인
    """
    response = requests.get(f"{BASE_URL}/health")
    if response.status_code == 200:
        print("✅ Server is healthy")
        return True
    else:
        print("❌ Server is not responding")
        return False

if __name__ == "__main__":
    # 서버 상태 확인
    if not test_health():
        print("Please start the server first: python app.py")
        exit(1)
    
    # EPUB 파일 경로 (Java 서버가 저장한 경로)
    EPUB_PATH = "/Users/sejin/epubs/sherlock/sherlock_fixed.epub" # 변경 필요
    
    print("\nChoose test mode:")
    print("1. Full Pipeline (recommended for demo)")
    print("2. Step by Step (for debugging)")
    
    choice = input("Enter choice (1 or 2): ")
    
    if choice == "1":
        test_full_pipeline(EPUB_PATH)
    elif choice == "2":
        test_step_by_step(EPUB_PATH)
    else:
        print("Invalid choice")