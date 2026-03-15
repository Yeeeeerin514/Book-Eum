import os
import re
from ebooklib import epub
from bs4 import BeautifulSoup
from config import TEXT_DIR, MUSIC_DIR
from typing import List, Dict
import ebooklib

def extract_chapters_from_epub(epub_path: str) -> List[Dict]:
    """
    EPUB 파일로부터 챕터별로 텍스트를 추출하고 파일로 저장
    
    Returns:
        List of dicts containing chapter info: [{title, text_path}, ...]
    """
    book = epub.read_epub(epub_path)
    book_name = os.path.basename(epub_path).replace(".epub", "")
    
    # 책 이름으로 서브 디렉터리 생성
    book_text_dir = os.path.join(TEXT_DIR, book_name)
    os.makedirs(book_text_dir, exist_ok=True)
    
    chapter_info = []
    
    # 챕터 패턴이 있는 항목 찾기
    chapter_items = []
    for item in book.get_items_of_type(ebooklib.ITEM_DOCUMENT):
        name = item.get_name()
        soup = BeautifulSoup(item.get_body_content(), "html.parser")
        
        # 제목 태그에서 챕터 패턴 찾기
        headings = " ".join(h.get_text(strip=True) for h in soup.find_all(["h1", "h2", "h3", "h4"]))
        
        # CHAPTER 또는 제X장 등의 패턴이 제목 태그에 포함된 경우
        if re.search(r"(chapter|CHAPTER|Chapter|제\s*\d+장|\d+\s*장|PART\s*\d+|Part\s*\d+)", headings):
            chapter_items.append((item, headings))
    
    # 챕터가 없으면 모든 문서를 챕터로 간주
    if not chapter_items:
        for item in book.get_items_of_type(ebooklib.ITEM_DOCUMENT):
            soup = BeautifulSoup(item.get_body_content(), "html.parser")
            headings = " ".join(h.get_text(strip=True) for h in soup.find_all(["h1", "h2", "h3", "h4"]))
            chapter_items.append((item, headings if headings else "Untitled"))
    
    # 챕터별 텍스트 추출 및 저장
    for idx, (item, title) in enumerate(chapter_items, start=1):
        soup = BeautifulSoup(item.get_body_content(), "html.parser")
        
        # 문단(<p>) 기준 텍스트 추출
        paragraphs = [p.get_text(strip=True) for p in soup.find_all("p")]
        text = "\n\n".join(paragraphs)
        
        # 텍스트가 너무 짧으면 건너뛰기
        if len(text.strip()) < 100:
            continue
        
        # 파일로 저장
        filename = f"chapter_{idx:02d}.txt"
        filepath = os.path.join(book_text_dir, filename)
        
        with open(filepath, "w", encoding="utf-8") as f:
            f.write(text)
        
        chapter_info.append({
            "chapter_number": idx,
            "title": title.strip()[:100],  # 제목 길이 제한
            "text_path": filepath,
            "text_length": len(text)
        })
        
        print(f"✅ Saved Chapter {idx}: {filepath}")
    
    return chapter_info

def extract_text_from_epub(epub_path: str) -> str:
    """
    EPUB 전체 텍스트를 하나의 문자열로 추출 (구버전 호환용)
    """
    book = epub.read_epub(epub_path)
    text = ""
    for item in book.get_items():
        if item.get_type() == ebooklib.ITEM_DOCUMENT:
            soup = BeautifulSoup(item.get_body_content(), "html.parser")
            text += soup.get_text(separator="\n")
    return text.strip()

def save_text_locally(text: str, filename: str) -> str:
    """
    텍스트를 파일로 저장
    """
    filepath = os.path.join(TEXT_DIR, filename)
    with open(filepath, "w", encoding="utf-8") as f:
        f.write(text)
    return filepath

def save_music_locally(audio_numpy, sampling_rate: int, filename: str) -> str:
    """
    음악 파일을 WAV로 저장
    """
    from scipy.io.wavfile import write
    filepath = os.path.join(MUSIC_DIR, filename)
    write(filepath, rate=sampling_rate, data=audio_numpy)
    return filepath