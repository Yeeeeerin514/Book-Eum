import torch
import numpy as np
from transformers import AutoProcessor, MusicgenForConditionalGeneration, BitsAndBytesConfig
from config import MUSIC_DIR
import time
import os
from scipy.io.wavfile import write
from typing import Tuple, List

# ------------------------------------------
#  성능 모드 선택
#  1 : FP32 기본 (가장 안정적)
#  2 : FP16 (속도 UP, VRAM DOWN)
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
                dtype=torch.float16,
            )
        _model = _model.to(device)

    else:
        raise ValueError("PERFORMANCE_MODE는 1 or 2여야 합니다.")

    return _model, _processor


# ============================================================
#  Batch Music Generation
# ============================================================
def generate_music_batch(
    prompts: List[str],
    filenames: List[str],
    duration_sec: float = 30.0
) -> Tuple[List[str], float]:
    """
    여러 프롬프트를 한 번에 받아 배치 처리
    """
    try:
        model, processor = load_model()
        batch_size = len(prompts)

        print(f"\n🎵 Generating Batch Music... Size: {batch_size}")

        start_time = time.time()

        # 1. 토큰화
        # 서로 다른 길이의 텍스트를 가장 긴 텍스트 길이에 맞춰 패딩함.
        inputs = processor(
            text=prompts,
            padding=True,
            return_tensors="pt"
        )

        # 인풋 데이터 GPU로 이동
        device = model.device
        inputs = {k: v.to(device) for k, v in inputs.items()}

        # 2. 토큰 수 계산
        sampling_rate = model.config.audio_encoder.sampling_rate
        frame_rate = model.config.audio_encoder.frame_rate
        max_new_tokens = max(1, int(duration_sec * frame_rate))

        # 3. Generate (한 번의 GPU 연산으로 N개의 오디오 생성)
        with torch.no_grad():
            audio_values = model.generate(
                **inputs,
                max_new_tokens=max_new_tokens,
                do_sample=True,
                guidance_scale=3.0
            )
            # audio_values shape: [batch_size, 1, samples]

        elapsed = time.time() - start_time

        # 4. 결과 저장 루프
        saved_paths = []

        # CPU로 한 번에 내리기
        audio_numpy_batch = audio_values.cpu().numpy()

        for i, filename in enumerate(filenames):
            # 개별 오디오 추출 [1, samples] -> [samples]
            audio_numpy = audio_numpy_batch[i, 0]

            # Normalize
            peak = np.max(np.abs(audio_numpy))
            if peak > 0:
                audio_numpy = audio_numpy / (peak + 1e-9)

            # 16bit PCM 변환
            audio_int16 = np.clip(audio_numpy, -1.0, 1.0)
            audio_int16 = (audio_int16 * 32767).astype(np.int16)

            # 파일 저장
            filepath = os.path.join(MUSIC_DIR, filename)
            write(filepath, rate=sampling_rate, data=audio_int16)
            saved_paths.append(filepath)
        
        print(f"✅ Batch Complete. Saved {len(saved_paths)} files.")
        print(f"⏱️ Total Batch Time: {elapsed:.2f}s (Avg per track: {elapsed/batch_size:.2f}s)")

        return saved_paths, elapsed

    except Exception as e:
        print(f"❌ Batch Error: {e}")
        raise e
