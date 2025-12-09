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
_device = None  # MPS device

def load_model():
    """
    MusicGen 모델을 로드 (최초 1회만 실행)
    - 모델이 이미 메모리에 있으면 캐시된 객체 반환
    - Mac MPS 사용 가능 시 MPS로 이동, FP16 적용
    """
    global _model, _processor, _device

    if _model is None:
        print("🎵 Loading MusicGen model...")

        # 1️⃣ 디바이스 설정
        if torch.backends.mps.is_available():
            _device = torch.device("mps")
            print("✅ Using MPS (Mac GPU)")
        else:
            _device = torch.device("cpu")
            print("✅ Using CPU")

        # 2️⃣ 모델 & 프로세서 로드
        _processor = AutoProcessor.from_pretrained("facebook/musicgen-small")
        _model = MusicgenForConditionalGeneration.from_pretrained("facebook/musicgen-small")
        
        # 3️⃣ FP16 변환 및 디바이스 이동
        if _device.type == "mps":
            _model = _model.half()
        _model = _model.to(_device)
        _model.eval()
        print(f"✅ Model loaded on {_device}")

    return _model, _processor, _device


def generate_music_local(prompt: str, filename: str, duration_sec: float = 5.0) -> str:
    """
    MusicGen 모델로 음악 생성 (MPS + FP16 최적화)
    """
    try:
        # 1️⃣ 모델 로드
        model, processor, device = load_model()
        
        print(f"🎵 Generating music with prompt: {prompt}")
        start_time = time.time()
        
        # 2️⃣ 프롬프트 전처리
        inputs = processor(
            text=[prompt],
            padding=True,
            return_tensors="pt"
        )
        # 입력도 디바이스로 이동
        inputs = {k: v.to(device) for k, v in inputs.items()}
        
        # FP16 autocast 적용 (MPS만)
        autocast_ctx = torch.autocast if device.type == "mps" else torch.no_grad

        sampling_rate = model.config.audio_encoder.sampling_rate
        frame_rate = model.config.audio_encoder.frame_rate
        max_new_tokens = int(duration_sec * frame_rate)

        # 3️⃣ 음악 생성
        with torch.no_grad():
            if device.type == "mps":
                with torch.autocast(device_type="mps", dtype=torch.float16):
                    audio_values = model.generate(
                        **inputs,
                        max_new_tokens=max_new_tokens,
                        do_sample=True,
                        guidance_scale=3.0
                    )
            else:
                audio_values = model.generate(
                    **inputs,
                    max_new_tokens=max_new_tokens,
                    do_sample=True,
                    guidance_scale=3.0
                )

        end_time = time.time()
        elapsed = end_time - start_time

        # 4️⃣ 후처리 및 파일 저장
        audio_numpy = audio_values[0, 0].cpu().numpy()
        audio_numpy = audio_numpy / np.max(np.abs(audio_numpy))
        audio_int16 = (audio_numpy * 32767).astype(np.int16)

        filepath = os.path.join(MUSIC_DIR, filename)
        write(filepath, rate=sampling_rate, data=audio_int16)

        print(f"✅ Music saved to: {filepath}")
        print(f"⏰ Generation time: {elapsed:.2f}초")
        return filepath

    except Exception as e:
        print(f"❌ Error generating music: {str(e)}")
        raise e


def generate_music_batch(prompts: list, filenames: list, duration_sec: float = 30.0) -> list:
    """
    여러 프롬프트를 한 번에 처리 (배치 생성)
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
