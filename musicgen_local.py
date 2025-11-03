import torch
from transformers import AutoProcessor, MusicgenForConditionalGeneration
from utils import save_music_locally
import os
import time

# 모델 초기화 (한 번만 로드)
device = "cuda:0" if torch.cuda.is_available() else "cpu"
processor = AutoProcessor.from_pretrained("facebook/musicgen-small")
model = MusicgenForConditionalGeneration.from_pretrained("facebook/musicgen-small").to(device)

def generate_music_local(prompt: str, filename: str, duration_sec=30):
    """
    로컬 MusicGen 모델로 WAV 생성 후 저장
    """
    print(f">> MusicGen 생성 중: {prompt}")

    inputs = processor(
        text=[prompt],
        padding=True,
        return_tensors="pt"
    ).to(device)

    audio_values = model.generate(**inputs, do_sample=True, guidance_scale=3, max_new_tokens=1500)

    sampling_rate = model.config.audio_encoder.sampling_rate
    audio_numpy = audio_values.cpu().numpy().squeeze()

    saved_path = save_music_locally(audio_numpy, sampling_rate, filename)
    print(f"   v 저장 완료: {saved_path}")
    return saved_path
