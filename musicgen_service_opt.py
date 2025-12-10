import torch
import numpy as np
from transformers import AutoProcessor, MusicgenForConditionalGeneration, BitsAndBytesConfig
from config import MUSIC_DIR
import time
import os
from scipy.io.wavfile import write
from typing import Tuple


# --- 성능 테스트 모드 선택 (1~4) ---
# 1 : GPU Standard (FP32, .to("cuda") 방식)
# 2 : GPU FP16 (반정밀도, 속도 개선)
# 3 : GPU INT4 (양자화, 메모리 최소화)
# 4 : GPU FP16 + Flash Attention 2 (L4 최적화, 가장 빠름)
PERFORMANCE_MODE = 1

# 전역 변수 (모델 캐싱용)
_model = None
_processor = None

def load_model():
    """
    설정된 PERFORMANCE_MODE에 따라 모델을 다르게 로드합니다.
    """
    global _model, _processor

    # 이미 로드되어 있으면 반환 (싱글톤)
    if _model is not None:
        return _model, _processor

    print(f"\n🎵 [Case {PERFORMANCE_MODE}] Loading MusicGen Model...")

    # 0. 메모리 정리 (모드 변경 시 충돌 방지)
    if torch.cuda.is_available():
        torch.cuda.empty_cache()

    model_id = "facebook/musicgen-small"
    _processor = AutoProcessor.from_pretrained(model_id)

    # --- Case 1: GPU Standard (FP32) - 기존 방식 ---
    if PERFORMANCE_MODE == 1:
        print("⚙️  설정: GPU Standard (FP32) - Naive Loading")
        # 1. CPU에 먼저 로드
        _model = MusicgenForConditionalGeneration.from_pretrained(
            model_id, 
            torch_dtype=torch.float32
        )
        # 2. 수동으로 GPU 이동
        if torch.cuda.is_available():
            _model = _model.to("cuda")
            print("✅ Model moved to CUDA via .to('cuda')")

    # --- Case 2: GPU FP16 (반정밀도) ---
    elif PERFORMANCE_MODE == 2:
        print("⚙️  설정: GPU Half Precision (FP16)")
        _model = MusicgenForConditionalGeneration.from_pretrained(
            model_id,
            device_map="cuda",
            torch_dtype=torch.float16 # 용량 절반, 속도 향상
        )

    # --- Case 3: GPU INT4 (양자화) ---
    elif PERFORMANCE_MODE == 3:
        print("⚙️  설정: GPU 4-bit Quantization (INT4)")
        # 양자화 설정 객체 생성
        bnb_config = BitsAndBytesConfig(
            load_in_4bit=True,
            bnb_4bit_compute_dtype=torch.float16,
            bnb_4bit_quant_type="nf4"
        )
        _model = MusicgenForConditionalGeneration.from_pretrained(
            model_id,
            quantization_config=bnb_config, # 설정 주입
            device_map="cuda"
        )

    # --- Case 4: GPU FP16 + Flash Attention 2 (L4 최적화) ---
    elif PERFORMANCE_MODE == 4:
        print("⚙️  설정: GPU FP16 + Flash Attention 2")
        _model = MusicgenForConditionalGeneration.from_pretrained(
            model_id,
            device_map="cuda",
            torch_dtype=torch.float16,
            attn_implementation="flash_attention_2" # 가속 기술 활성화
        )

    else:
        raise ValueError("PERFORMANCE_MODE는 1~5 사이여야 합니다.")

    return _model, _processor

def generate_music_local(prompt: str, filename: str, duration_sec: float = 5.0) -> Tuple[str, float]:
    """
    음악을 생성하고 파일 경로와 소요 시간을 반환합니다.
    """
    try:
        model, processor = load_model()

        print(f"🎵 Generating music... [Mode {PERFORMANCE_MODE}] Prompt: {prompt}")
        start_time = time.time()

        # 1. 입력 데이터 처리
        inputs = processor(
            text=[prompt],
            padding=True,
            return_tensors="pt"
        )

        # 2. 입력 데이터를 모델이 있는 장치(CPU/GPU)로 이동
        inputs = inputs.to(model.device)

        # 3. 생성 파라미터 계산
        sampling_rate = model.config.audio_encoder.sampling_rate
        frame_rate = model.config.audio_encoder.frame_rate
        max_new_tokens = int(duration_sec * frame_rate)

        # 4. 음악 생성 (추론)
        with torch.no_grad():
            audio_values = model.generate(
                **inputs,
                max_new_tokens=max_new_tokens,
                do_sample=True,
                guidance_scale=3.0
            )

        end_time = time.time()
        elapsed = end_time - start_time

        # 5. 오디오 후처리 (GPU -> CPU -> Numpy)
        audio_numpy = audio_values[0, 0].cpu().numpy()

        # 볼륨 정규화 (Normalization) - 소리 깨짐 방지
        max_val = np.max(np.abs(audio_numpy))
        if max_val > 0:
            audio_numpy = audio_numpy / max_val
        
        # 16비트 PCM 변환
        audio_int16 = (audio_numpy * 32767).astype(np.int16)

        # 6. 파일 저장
        filepath = os.path.join(MUSIC_DIR, filename)
        write(filepath, rate=sampling_rate, data=audio_int16)

        print(f"✅ Saved: {filepath}")
        print(f"⏱️ [Case {PERFORMANCE_MODE}] Generation Time: {elapsed:.2f}s")

        return filepath, elapsed

    except Exception as e:
        print(f"❌ Error: {str(e)}")
        raise e