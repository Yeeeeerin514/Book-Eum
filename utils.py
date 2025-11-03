import os
from ebooklib import epub
from bs4 import BeautifulSoup
from config import TEXT_DIR, MUSIC_DIR

def extract_text_from_epub(epub_path: str) -> str:
    book = epub.read_epub(epub_path)
    text = ""
    for item in book.get_items():
        if item.get_type() == epub.ITEM_DOCUMENT:
            soup = BeautifulSoup(item.get_body_content(), "html.parser")
            text += soup.get_text(separator="\n")
    return text.strip()

def save_text_locally(text: str, filename: str) -> str:
    filepath = os.path.join(TEXT_DIR, filename)
    with open(filepath, "w", encoding="utf-8") as f:
        f.write(text)
    return filepath

def save_music_locally(audio_numpy, sampling_rate, filename: str) -> str:
    from scipy.io.wavfile import write
    filepath = os.path.join(MUSIC_DIR, filename)
    write(filepath, rate=sampling_rate, data=audio_numpy)
    return filepath
