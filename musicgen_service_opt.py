import torch
import numpy as np
from transformers import AutoProcessor, MusicgenForConditionalGeneration, BitsAndBytesConfig
from config import MUSIC_DIR
import time
import os
from scipy.io.wavfile import write
from typing import Tuple

# ------------------------------------------
#  성능 모드 선택
#  1 : FP32 기본 (가장 안정적)
#  2 : FP16 (속도 ↑, VRAM ↓)
#  3 : INT4 양자화 (VRAM 최소)
# ------------------------------------------
PERFORMANCE_MODE = 1

# 모델 캐시
_model = None
_processor = None

# ============================================================
#  Model Loader
# ============================================================
def load_model(force_reload: bool = False):
    """
    설정된 PERFORMANCE_MODE에 따라 모델을 로드하여 반환.
    force_reload=True 시 강제로 다시 로드.
    """
    global _model, _processor

    if _model is not None and not force_reload:
        return _model, _processor

    print(f"\n🎵 [Case {PERFORMANCE_MODE}] Loading MusicGen Model...")

    # CUDA 캐시 정리
    if torch.cuda.is_available():
        torch.cuda.empty_cache()

    model_id = "facebook/musicgen-small"
    _processor = AutoProcessor.from_pretrained(model_id)
    if torch.cuda.is_available():
        device = "cuda"
    else: device = "cpu"

    # -----------------------------------
    # Case 1: FP32 Standard
    # -----------------------------------
    if PERFORMANCE_MODE == 1:
        print("⚙️  설정: FP32 (Standard)")
        _model = MusicgenForConditionalGeneration.from_pretrained(model_id)
        _model = _model.to(torch.float32).to(device)

    # -----------------------------------
    # Case 2: FP16
    # -----------------------------------
    elif PERFORMANCE_MODE == 2:
        print("⚙️  설정: FP16 (Half Precision)")
        _model = MusicgenForConditionalGeneration.from_pretrained(
            model_id,
            torch_dtype=torch.float16,
            attn_implementation="sdpa"  # L4 GPU 가속 (검증 필요)
        )
        _model = _model.to(device)

    else:
        raise ValueError("PERFORMANCE_MODE는 1 or 2여야 합니다.")

    return _model, _processor

# ============================================================
#  Music Generation
# ============================================================
def generate_music(
    prompt: str,
    filename: str,
    duration_sec: float = 5.0
) -> Tuple[str, float]:
    """
    음악 생성 후 파일 저장, 경로와 시간 반환
    """
    try:
        model, processor = load_model()

        print(f"🎵 Generating music... [Mode {PERFORMANCE_MODE}] Prompt: {prompt}")
        start_time = time.time()

        # 1. Processor via CPU → GPU 이동
        inputs = processor(text=[prompt], padding=True, return_tensors="pt")

        # dict 형태로 안전하게 이동
        device = model.device
        inputs = {k: v.to(device) for k, v in inputs.items()}

        # 2. 토큰 수 계산
        sampling_rate = model.config.audio_encoder.sampling_rate
        frame_rate = model.config.audio_encoder.frame_rate

        max_new_tokens = max(1, int(duration_sec * frame_rate))

        # 3. Generate
        with torch.no_grad():
            audio_values = model.generate(
                **inputs,
                max_new_tokens=max_new_tokens,
                do_sample=True,
                guidance_scale=3.0
            )

        elapsed = time.time() - start_time

        # 4. 후처리: GPU --> CPU
        audio_numpy = audio_values[0, 0].cpu().numpy()

        # Normalize
        peak = np.max(np.abs(audio_numpy))
        if peak > 0:
            audio_numpy = audio_numpy / (peak + 1e-9)

        # 5. 16bit PCM 변환
        audio_int16 = np.clip(audio_numpy, -1.0, 1.0)
        audio_int16 = (audio_int16 * 32767).astype(np.int16)

        # 6. 파일 저장
        filepath = os.path.join(MUSIC_DIR, filename)
        write(filepath, rate=sampling_rate, data=audio_int16)

        print(f"✅ Saved: {filepath}")
        print(f"⏱️  Time: {elapsed:.2f}s")

        return filepath, elapsed

    except Exception as e:
        print(f"❌ Error: {e}")
        raise e
