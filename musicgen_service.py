import torch
import numpy as np
from transformers import AutoProcessor, MusicgenForConditionalGeneration
from config import MUSIC_DIR
import time
import os
from scipy.io.wavfile import write

# 전역 변수로 모델 캐싱 (한 번만 로드)
_model = None
_processor = None

def load_model():
    """
    MusicGen 모델을 로드 (최초 1회만 실행)
    - 모델이 이미 메모리에 있으면 캐시된 객체 반환
    - GPU 사용 가능 시 모델을 GPU로 이동
    """
    global _model, _processor
    
    if _model is None:
        print("🎵 Loading MusicGen model...")

        # 모델과 프로세서 로드
        _processor = AutoProcessor.from_pretrained("facebook/musicgen-small")
        _model = MusicgenForConditionalGeneration.from_pretrained("facebook/musicgen-small")
        
        # GPU 사용 가능하면 GPU로 이동
        if torch.cuda.is_available():
            _model = _model.to("cuda")
            print("✅ Model loaded on GPU")
        else:
            print("✅ Model loaded on CPU")
    
    return _model, _processor

def generate_music(prompt: str, filename: str, duration_sec: float = 5.0) -> str:
    """
    MusicGen 모델로 음악 생성
    
    Args:
        prompt: 음악 생성 프롬프트
        filename: 저장할 파일명 (예: "chapter_01.wav")
        duration_sec: 음악 길이 (초)
    
    Returns:
        저장된 음악 파일의 전체 경로
    """
    try:
        # 1. 모델 로드
        model, processor = load_model()
        
        print(f"🎵 Generating music with prompt: {prompt}")

        start_time = time.time() # 시작 시간 기록
        
        # 2. 프롬프트 전처리
        inputs = processor(
            text=[prompt],
            padding=True,
            return_tensors="pt"
        )
        
        # GPU 사용 시 입력도 GPU로 이동
        if torch.cuda.is_available():
            inputs = {k: v.to("cuda") for k, v in inputs.items()}
        
        # 3. 생성 파라미터 설정
        sampling_rate = model.config.audio_encoder.sampling_rate  # 기본: 32000Hz
        frame_rate = model.config.audio_encoder.frame_rate        # 기본: 약 50Hz
        max_new_tokens = int(duration_sec * frame_rate)           # 생성할 토큰 수 계산
        
        # 4. 음악 생성
        with torch.no_grad():
            audio_values = model.generate(
                **inputs,
                max_new_tokens=max_new_tokens,
                do_sample=True,
                guidance_scale=3.0 
            )

        end_time = time.time()  # 종료 시간
        elapsed = end_time - start_time
        
        # 5. 오디오 후처리 및 파일 저장
        audio_numpy = audio_values[0, 0].cpu().numpy()
        audio_numpy = audio_numpy / np.max(np.abs(audio_numpy)) # 정규화
        audio_int16 = (audio_numpy * 32767).astype(np.int16) # 16-bit PCM으로 변환
        
        filepath = os.path.join(MUSIC_DIR, filename)
        write(filepath, rate=sampling_rate, data=audio_int16)        
        
        # 로그 출력
        print(f"✅ Music saved to: {filepath}")
        print(f"⏰ Generation time: {elapsed:.2f}초")

        return filepath
        
    except Exception as e:
        print(f"❌ Error generating music: {str(e)}")
        raise e

def generate_music_batch(prompts: list, filenames: list, duration_sec: float = 5.0) -> list:
    """
    여러 프롬프트를 한 번에 처리 (배치 생성)
    
    Args:
        prompts: 프롬프트 리스트
        filenames: 파일명 리스트
        duration_sec: 음악 길이
    
    Returns:
        생성된 음악 파일 경로 리스트
    """
    results = []
    for prompt, filename in zip(prompts, filenames):
        try:
            path = generate_music_local(prompt, filename, duration_sec)
            results.append(path)
        except Exception as e:
            print(f"Failed to generate {filename}: {e}")
            results.append(None)
    
    return results