#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
RealTimeGreta.py - VAP Model Integration for Greta Platform

Core architecture connecting the VAP (Voice Activity Projection) model to the
Greta platform for real-time turn-taking, backchannel, and head nod prediction.

Architecture:
    [Mic Audio] ──┐
                  ├──> [VAPModelWrapper] ──> [ActionDecider] ──> [GretaDispatcher] ──> Java
    [OpenFace]  ──┘         (model)          (thresholds)        (TCP socket)

Model outputs -> Greta actions:
    HN short     -> shortNod         (woz_nod_short.xml)
    HN long      -> longNod          (woz_nod_long.xml)
    HN long_p    -> longNodTwice     (woz_nod_long_twice.xml)
    BC reactive  -> continuerBC      (random continuer FML)
    BC emotional -> assessmentBC     (random assessment FML)
    VAP shift    -> turnShift        (turn shift signal)

Based on:
    - realtime_inference.py (model wrapper + inference engine)
    - turnManagerWoz.py (mic socket + Greta socket communication)

Usage:
    python RealTimeGreta.py
    python RealTimeGreta.py --checkpoint sam_vap/checkpoints/your_model.ckpt

    Optional full-session WAV logs (user microphone + agent speaker loopback):
    python RealTimeGreta.py --record --record_dir "C:\\experiments\\run1"
"""

import argparse
import torch
import numpy as np
import time
import sys
import os
import wave
import csv
import socket
import traceback
from datetime import datetime
from threading import Thread, Lock
from collections import deque
from dataclasses import dataclass, field
from typing import Dict, Tuple, Optional, List
import warnings

warnings.filterwarnings("ignore")

# ============================================================================
# Path setup: VAP Python sources + checkpoints live under TurnManagement/sam_vap/
# ============================================================================

_TURN_MGMT_DIR = os.path.dirname(os.path.abspath(__file__))
SAM_VAP_DIR = os.path.join(_TURN_MGMT_DIR, "sam_vap")
CHECKPOINTS_DIR = os.path.join(SAM_VAP_DIR, "checkpoints")
VAP_MODEL_DIR = os.environ.get(
    "GRETA_VAP_CODE_DIR",
    os.path.join(SAM_VAP_DIR, "vap_code"),
)

CKPT_MULTIMODAL = (
    "VapGPT_25Hz_ad20s_134-epoch9-val_0.04519_double_CAascade_double_shift1000"
    "_5S_25hz_Agressive_HNw.ckpt"
)
CKPT_AUDIO_ONLY = (
    "VapGPT_25Hz_ad20s_134-epoch9-val_0.07268_Audio_only_5s_25hz_double_double"
    "_Shift_1000.ckpt"
)
DEFAULT_CHECKPOINT = os.path.join(CHECKPOINTS_DIR, CKPT_MULTIMODAL)


def _resolve_path_under_turn_mgmt(path: str) -> str:
    """Resolve relative paths from the TurnManagement folder."""
    if os.path.isabs(path):
        return path
    return os.path.join(_TURN_MGMT_DIR, path)


if not os.path.isdir(VAP_MODEL_DIR):
    raise RuntimeError(
        f"VAP Python code not found at {VAP_MODEL_DIR}. "
        f"Copy Nezih_Vap_BC_HN sources into sam_vap/vap_code/ (see README.txt)."
    )
if VAP_MODEL_DIR not in sys.path:
    sys.path.insert(0, VAP_MODEL_DIR)

from model import VapGPT, VapConfig
from events import TurnTakingEvents
import torch.nn.functional as F

# Config classes for checkpoint compatibility
from finetune import OptConfig, DataConfig
from events import EventConfig

current_module = sys.modules[__name__]
current_module.VapConfig = VapConfig
current_module.OptConfig = OptConfig
current_module.DataConfig = DataConfig
current_module.EventConfig = EventConfig


# ============================================================================
# Configuration
# ============================================================================

@dataclass
class GretaConfig:
    """Configuration for the RealTimeGreta system"""

    # Network ports (matching TurnManagementWoZ.java)
    feedback_port: int = 5960
    greta_port: int = 5961
    openface_port: int = 5963

    # Audio
    sample_rate: int = 16000

    # Model inference
    context_duration: float = 5.0       # seconds of context for model
    stride_duration: float = 1.0        # seconds between inferences
    frame_hz: float = 25.0              # model frame rate

    # Detection thresholds
    bc_detection_threshold: float = 0.5
    hn_detection_threshold: float = 0.7
    vap_shift_threshold: float = 0.6

    # Cooldowns (seconds) - minimum time between consecutive actions
    # Prevents rapid-fire triggers from noisy predictions
    bc_cooldown: float = 3.0
    hn_cooldown: float = 3.0
    shift_cooldown: float = 5.0

    # Prediction shifts (seconds)
    bc_shift: float = 1.0
    hn_shift: float = 1.0


# ============================================================================
# Model Wrapper (from realtime_inference.py - kept identical)
# ============================================================================

class VAPModelWrapper:
    """
    Wrapper for VAP model loading and inference.
    Reused from realtime_inference.py with minimal changes.
    """

    def __init__(self, checkpoint_path: str, device: str = "cuda",
                 config_overrides: Optional[Dict] = None):
        """
        Args:
            checkpoint_path: Path to .ckpt file
            device: 'cuda' or 'cpu'
            config_overrides: Dict of VapConfig fields to override before model
                construction. Keys match VapConfig field names, e.g.
                {'predict_backchannels': True, 'pretrained_face_encoder': '...'}.
                Only non-None values are applied.
        """
        self.device = device
        self.checkpoint_path = checkpoint_path
        self.config_overrides = config_overrides or {}

        self.model, self.conf = self._load_model()
        self.event_processor = TurnTakingEvents()

        print(f"[RealTimeGreta] Loaded VAP model from {checkpoint_path}")
        print(f"[RealTimeGreta] Final model config:")
        print(f"  sample_rate={self.conf.sample_rate}, frame_hz={self.conf.frame_hz}")
        print(f"  multimodal={getattr(self.conf, 'multimodal', False)}")
        print(f"  use_face_encoder={getattr(self.conf, 'use_face_encoder', False)}")
        print(f"  mode={getattr(self.conf, 'mode', 'N/A')}")
        print(f"  predict_backchannels={getattr(self.conf, 'predict_backchannels', False)}")
        print(f"  predict_head_nods={getattr(self.conf, 'predict_head_nods', False)}")
        print(f"  bc_detection_head={getattr(self.conf, 'bc_detection_head', False)}")
        print(f"  hn_detection_head={getattr(self.conf, 'hn_detection_head', False)}")
        print(f"  use_hubert_semantics={getattr(self.conf, 'use_hubert_semantics', False)}")
        print(f"  use_whisper_semantics={getattr(self.conf, 'use_whisper_semantics', False)}")

    def _load_model(self) -> Tuple[VapGPT, VapConfig]:
        from safe_checkpoint_loader import load_vap_checkpoint_safe
        checkpoint = load_vap_checkpoint_safe(self.checkpoint_path, map_location="cpu")

        if checkpoint is None:
            raise RuntimeError(f"Failed to load checkpoint from {self.checkpoint_path}")

        if 'hyper_parameters' in checkpoint:
            conf_dict = checkpoint['hyper_parameters']['conf']
            conf = VapConfig(**conf_dict) if isinstance(conf_dict, dict) else conf_dict
        elif 'conf' in checkpoint:
            conf_dict = checkpoint['conf']
            conf = VapConfig(**conf_dict) if isinstance(conf_dict, dict) else conf_dict
        else:
            print("[RealTimeGreta] Warning: No config in checkpoint, using defaults")
            conf = VapConfig()

        # --- Print checkpoint config before overrides ---
        print(f"[RealTimeGreta] Config from checkpoint:")
        print(f"  predict_backchannels={getattr(conf, 'predict_backchannels', False)}")
        print(f"  predict_head_nods={getattr(conf, 'predict_head_nods', False)}")
        print(f"  use_face_encoder={getattr(conf, 'use_face_encoder', False)}")
        print(f"  multimodal={getattr(conf, 'multimodal', False)}")
        print(f"  use_hubert_semantics={getattr(conf, 'use_hubert_semantics', False)}")
        print(f"  use_whisper_semantics={getattr(conf, 'use_whisper_semantics', False)}")
        print(f"  mode={getattr(conf, 'mode', 'N/A')}")
        print(f"  load_pretrained={getattr(conf, 'load_pretrained', 1)}")
        print(f"  pretrained_face_encoder={getattr(conf, 'pretrained_face_encoder', None)}")
        print(f"  pretrained_cpc={getattr(conf, 'pretrained_cpc', None)}")
        print(f"  pretrained_vap={getattr(conf, 'pretrained_vap', None)}")

        # Disable pretrained loading by default: the checkpoint already contains
        # all trained weights (including face encoder). Loading pretrained
        # separately would fail if the original training path doesn't exist
        # on this machine. VapConfig uses int (0/1) not bool.
        conf.load_pretrained = 0

        # --- Apply CLI overrides ---
        overrides_applied = []
        for key, value in self.config_overrides.items():
            if value is None:
                continue
            if hasattr(conf, key):
                old_val = getattr(conf, key)
                setattr(conf, key, value)
                overrides_applied.append(f"  {key}: {old_val} -> {value}")
            else:
                print(f"[RealTimeGreta] Warning: unknown config key '{key}', skipping")

        if overrides_applied:
            print(f"[RealTimeGreta] Applied {len(overrides_applied)} CLI overrides:")
            for line in overrides_applied:
                print(line)

        # If face encoder path was overridden, re-enable pretrained loading for it
        if self.config_overrides.get('pretrained_face_encoder') and \
           getattr(conf, 'use_face_encoder', False):
            conf.load_pretrained = 1
            print(f"[RealTimeGreta] Re-enabled pretrained loading for face encoder")

        from cpu_safe_encoder_patch import CPUSafeContext
        
        with CPUSafeContext():
            model = VapGPT(conf)

        if 'state_dict' in checkpoint:
            state_dict = {k.replace('model.', ''): v for k, v in checkpoint['state_dict'].items()}
        else:
            state_dict = checkpoint

        cpu_state_dict = {k: v.cpu() if isinstance(v, torch.Tensor) else v
                          for k, v in state_dict.items()}

        missing, unexpected = model.load_state_dict(cpu_state_dict, strict=False)
        if missing:
            print(f"[RealTimeGreta] Warning: {len(missing)} missing keys in state_dict")
            for k in missing[:10]:
                print(f"  missing: {k}")
            if len(missing) > 10:
                print(f"  ... and {len(missing) - 10} more")
        if unexpected:
            print(f"[RealTimeGreta] Note: {len(unexpected)} unexpected keys (ignored)")

        model.eval()
        model.to(self.device)
        model.device = torch.device(self.device)
        conf.inference_mode = True

        return model, conf

    def _align_to_duration(self, features: torch.Tensor, target_frames: int) -> torch.Tensor:
        current = features.shape[0]
        if current == target_frames:
            return features
        elif current > target_frames:
            return features[:target_frames]
        else:
            padding = torch.zeros(target_frames - current, features.shape[1])
            return torch.cat([features, padding], dim=0)

    def prepare_batch(self, audio_user: torch.Tensor, audio_agent: torch.Tensor,
                      multimodal_features: Optional[Dict[str, torch.Tensor]] = None,
                      verbose: bool = False) -> Dict:
        """
        Prepare batch for model inference.

        Args:
            audio_user: User (speaker 1) audio [T]
            audio_agent: Agent (speaker 2) audio [T] - can be zeros
            multimodal_features: Optional dict with keys like 'face1', 'gaze1', 'head1',
                                 'body1' as [T_frames, D] tensors (already at frame_hz)
            verbose: If True, log detailed batch composition
        Returns:
            Batch dictionary ready for model forward pass
        """
        min_length = min(len(audio_user), len(audio_agent))
        audio_user = audio_user[:min_length]
        audio_agent = audio_agent[:min_length]

        waveform = torch.stack([audio_user, audio_agent], dim=0).unsqueeze(0).to(self.device)

        audio_duration_sec = waveform.shape[-1] / self.conf.sample_rate
        expected_frames = int(audio_duration_sec * self.conf.frame_hz)

        batch = {'waveform': waveform}

        live_modalities = []
        zero_modalities = []

        if self.conf.multimodal:
            multimodal_dims = {
                'gaze': 4,
                'head': 6,
                'face': 17,
                'body': 3
            }

            for speaker_id in [1, 2]:
                for modality, dim in multimodal_dims.items():
                    key = f'{modality}{speaker_id}'
                    if multimodal_features and key in multimodal_features:
                        feat = multimodal_features[key]
                        feat = self._align_to_duration(feat, expected_frames)
                        batch[key] = feat.unsqueeze(0).to(self.device)
                        live_modalities.append(key)
                    else:
                        batch[key] = torch.zeros(1, expected_frames, dim, device=self.device)
                        zero_modalities.append(key)

                if getattr(self.conf, 'use_face_encoder', False):
                    batch[f'face_im{speaker_id}'] = torch.zeros(
                        1, expected_frames, 3, 224, 224, device=self.device)
                    zero_modalities.append(f'face_im{speaker_id}')

        # Whisper/HuBERT semantics: the model does NOT read them from the batch.
        # It computes them inside forward() via encode_semantics(waveform), which
        # runs each channel of the waveform through whisper_encoder or hubert_encoder.
        # So no batch key needed; cross-modal fusion (CPC + semantics) uses the
        # same raw audio we pass as 'waveform'.

        if verbose:
            user_rms = torch.sqrt(torch.mean(audio_user ** 2)).item()
            agent_rms = torch.sqrt(torch.mean(audio_agent ** 2)).item()
            print(f"  [Batch] waveform: [{waveform.shape}], duration={audio_duration_sec:.2f}s, "
                  f"frames={expected_frames}")
            print(f"  [Batch] user_audio_rms={user_rms:.4f} "
                  f"{'(speech)' if user_rms > 0.0005 else '(quiet)'}, "
                  f"agent_audio_rms={agent_rms:.6f} "
                  f"{'(speaking)' if agent_rms > 0.001 else '(silent)'}")
            if live_modalities:
                print(f"  [Batch] LIVE modalities: {live_modalities}")
            if zero_modalities:
                print(f"  [Batch] ZEROS modalities: {zero_modalities}")
            print(f"  [Batch] Total keys: {list(batch.keys())}")

        return batch

    @torch.no_grad()
    def forward(self, batch: Dict) -> Dict:
        return self.model(batch)


# ============================================================================
# Streaming Audio Buffer
# ============================================================================

class StreamingAudioBuffer:
    """
    Thread-safe circular buffer for accumulating live audio samples.
    Provides a sliding window of the last N seconds of audio.
    """

    def __init__(self, max_duration_sec: float, sample_rate: int):
        self.sample_rate = sample_rate
        self.max_samples = int(max_duration_sec * sample_rate)
        self.buffer = deque(maxlen=self.max_samples)
        self.lock = Lock()
        self.total_samples_received = 0

    def push(self, samples: np.ndarray):
        """Add new audio samples (thread-safe)"""
        with self.lock:
            self.buffer.extend(samples.flatten())
            self.total_samples_received += len(samples.flatten())

    def get_last_n_seconds(self, duration_sec: float) -> np.ndarray:
        """Get the last N seconds of audio as a numpy array"""
        n_samples = int(duration_sec * self.sample_rate)
        with self.lock:
            buf_list = list(self.buffer)
            if len(buf_list) < n_samples:
                padding = [0.0] * (n_samples - len(buf_list))
                return np.array(padding + buf_list, dtype=np.float32)
            return np.array(buf_list[-n_samples:], dtype=np.float32)

    def get_duration_sec(self) -> float:
        """Get current buffer duration in seconds"""
        with self.lock:
            return len(self.buffer) / self.sample_rate

    def get_total_duration_sec(self) -> float:
        """Get total audio received so far in seconds"""
        return self.total_samples_received / self.sample_rate


# ============================================================================
# Session WAV recording (optional experiment logging)
# ============================================================================

class WavSessionRecorder:
    """
    Accumulates float32 mono samples (-1..1) and writes a 16-bit PCM WAV on finalize().
    Thread-safe for concurrent append from capture threads.
    """

    def __init__(self, out_path: str, sample_rate: int, session_t0: float):
        self.out_path = out_path
        self.sample_rate = sample_rate
        self.session_t0 = session_t0
        self._chunks: List[Tuple[int, np.ndarray]] = []
        self._max_end_idx = 0
        self._lock = Lock()

    def append(self, samples: np.ndarray):
        with self._lock:
            chunk = np.asarray(samples, dtype=np.float32).flatten().copy()
            if len(chunk) == 0:
                return
            # Stream.read()/record() returns the chunk that just happened.
            chunk_end_t = time.perf_counter() - self.session_t0
            chunk_start_t = max(0.0, chunk_end_t - (len(chunk) / float(self.sample_rate)))
            start_idx = int(round(chunk_start_t * self.sample_rate))
            end_idx = start_idx + len(chunk)
            self._max_end_idx = max(self._max_end_idx, end_idx)
            self._chunks.append((start_idx, chunk))

    def finalize(self, total_duration_sec: Optional[float] = None):
        with self._lock:
            if not self._chunks:
                print(f"[WavSessionRecorder] No samples for {self.out_path}, skipping write")
                return
            total_samples = self._max_end_idx
            if total_duration_sec is not None:
                total_samples = max(total_samples, int(round(total_duration_sec * self.sample_rate)))
            audio = np.zeros(total_samples, dtype=np.float32)
            for start_idx, chunk in self._chunks:
                end_idx = min(start_idx + len(chunk), total_samples)
                n = end_idx - start_idx
                if n > 0:
                    audio[start_idx:end_idx] = chunk[:n]
            self._chunks.clear()
        audio = np.clip(audio, -1.0, 1.0)
        pcm = (audio * 32767.0).astype(np.int16)
        _dir = os.path.dirname(os.path.abspath(self.out_path))
        if _dir:
            os.makedirs(_dir, exist_ok=True)
        with wave.open(self.out_path, "wb") as wf:
            wf.setnchannels(1)
            wf.setsampwidth(2)
            wf.setframerate(self.sample_rate)
            wf.writeframes(pcm.tobytes())
        dur = len(pcm) / float(self.sample_rate)
        print(f"[WavSessionRecorder] Wrote {dur:.1f}s -> {self.out_path}")


class ActionTimelineLogger:
    """
    Writes a frame-level CSV aligned with session clock and aligned WAV files.
    Each row is one frame (1/frame_hz seconds).
    """

    ACTION_COLUMNS = [
        "shortNod",
        "longNod",
        "longNodTwice",
        "continuerBC",
        "assessmentBC",
        "turnShift",
    ]

    def __init__(self, out_csv_path: str, frame_hz: float):
        self.out_csv_path = out_csv_path
        self.frame_hz = frame_hz
        self._events: List[Tuple[float, str]] = []
        self._lock = Lock()

    def add_event(self, timestamp_sec: float, action: str):
        if action == "nothing":
            return
        if action not in self.ACTION_COLUMNS:
            return
        with self._lock:
            self._events.append((max(0.0, float(timestamp_sec)), action))

    def finalize(self, total_duration_sec: float):
        n_frames = max(1, int(np.ceil(total_duration_sec * self.frame_hz)) + 1)
        flags = {a: np.zeros(n_frames, dtype=np.uint8) for a in self.ACTION_COLUMNS}
        actions_per_frame: List[List[str]] = [[] for _ in range(n_frames)]

        with self._lock:
            events = list(self._events)

        for t, action in events:
            idx = int(t * self.frame_hz)
            if idx < 0:
                idx = 0
            if idx >= n_frames:
                idx = n_frames - 1
            flags[action][idx] = 1
            actions_per_frame[idx].append(action)

        _dir = os.path.dirname(os.path.abspath(self.out_csv_path))
        if _dir:
            os.makedirs(_dir, exist_ok=True)
        with open(self.out_csv_path, "w", encoding="utf-8", newline="") as f:
            writer = csv.writer(f)
            writer.writerow(["timestamp_sec", *self.ACTION_COLUMNS, "actions"])
            for i in range(n_frames):
                t = i / self.frame_hz
                row = [f"{t:.3f}"]
                row.extend(int(flags[a][i]) for a in self.ACTION_COLUMNS)
                row.append("|".join(actions_per_frame[i]))
                writer.writerow(row)
        print(f"[ActionTimelineLogger] Wrote {n_frames} frames -> {self.out_csv_path}")


# ============================================================================
# Microphone Capture (direct, using sounddevice)
# ============================================================================

class MicCapture(Thread):
    """
    Captures audio from the default microphone using the `sounddevice` library.
    This bypasses the Java mic server entirely, capturing audio directly in Python.

    Install: pip install sounddevice
    """

    def __init__(self, audio_buffer: StreamingAudioBuffer, sample_rate: int = 16000,
                 device_index: Optional[int] = None,
                 wav_recorder: Optional[WavSessionRecorder] = None):
        super().__init__(daemon=True)
        self.audio_buffer = audio_buffer
        self.sample_rate = sample_rate
        self.device_index = device_index
        self.wav_recorder = wav_recorder
        self.is_active = False
        self.lock = Lock()

    def start(self):
        self.is_active = True
        super().start()

    def run(self):
        try:
            import sounddevice as sd
        except ImportError:
            print("[MicCapture] ERROR: 'sounddevice' not installed! "
                  "User audio will be zeros. Install with: pip install sounddevice")
            return

        chunk_duration = 0.05  # 50ms chunks
        chunk_frames = int(self.sample_rate * chunk_duration)
        chunk_count = 0
        log_interval = 200  # log every N chunks (~10s)

        try:
            # List available devices for debugging
            devices = sd.query_devices()
            default_input = sd.query_devices(kind='input')
            print(f"[MicCapture] Default input device: {default_input['name']} "
                  f"(index={sd.default.device[0]}, sr={default_input['default_samplerate']})")

            if self.device_index is not None:
                dev_info = sd.query_devices(self.device_index)
                print(f"[MicCapture] Using specified device #{self.device_index}: {dev_info['name']}")

            device = self.device_index  # None = default

            with sd.InputStream(samplerate=self.sample_rate, channels=1,
                                dtype='float32', device=device,
                                blocksize=chunk_frames) as stream:
                print(f"[MicCapture] Recording started (sr={self.sample_rate}, "
                      f"chunk={chunk_frames} samples/{chunk_duration*1000:.0f}ms)")

                while self.is_active:
                    try:
                        data, overflowed = stream.read(chunk_frames)
                        if overflowed:
                            print("[MicCapture] WARNING: input overflow (dropped samples)")

                        audio_float32 = data.flatten().astype(np.float32)
                        self.audio_buffer.push(audio_float32)
                        if self.wav_recorder is not None:
                            self.wav_recorder.append(audio_float32)
                        chunk_count += 1

                        if chunk_count == 1:
                            rms = np.sqrt(np.mean(audio_float32 ** 2))
                            print(f"[MicCapture] First chunk captured ({len(audio_float32)} samples, rms={rms:.4f})")

                        if chunk_count % log_interval == 0:
                            buf_dur = self.audio_buffer.get_duration_sec()
                            total_dur = self.audio_buffer.get_total_duration_sec()
                            rms = np.sqrt(np.mean(audio_float32 ** 2))
                            print(f"[MicCapture] chunks={chunk_count}, buf={buf_dur:.1f}s, "
                                  f"total={total_dur:.1f}s, last_rms={rms:.4f} "
                                  f"{'(speech)' if rms > 0.005 else '(quiet)'}")

                    except Exception as e:
                        print(f"[MicCapture] Read error: {e}")
                        time.sleep(0.01)

        except Exception as e:
            print(f"[MicCapture] Init error: {e}")
            traceback.print_exc()

        print(f"[MicCapture] Stopped (total chunks: {chunk_count})")

    def stop(self):
        with self.lock:
            self.is_active = False


# ============================================================================
# System Audio Loopback Capture (Agent's TTS voice)
# ============================================================================

class SystemAudioCapture(Thread):
    """
    Captures the system audio output (speaker loopback) to get the agent's
    TTS voice in real-time. Uses the `soundcard` library with WASAPI loopback
    on Windows.

    This avoids any Java-side modifications -- whatever CereProc (or any TTS)
    plays through the speakers is captured here as the agent audio channel.

    Install: pip install soundcard
    """

    def __init__(self, audio_buffer: StreamingAudioBuffer, sample_rate: int = 16000,
                 wav_recorder: Optional[WavSessionRecorder] = None):
        super().__init__(daemon=True)
        self.audio_buffer = audio_buffer
        self.sample_rate = sample_rate
        self.wav_recorder = wav_recorder
        self.is_active = False
        self.lock = Lock()

    def start(self):
        self.is_active = True
        super().start()

    def run(self):
        try:
            import soundcard as sc
        except ImportError:
            print("[RealTimeGreta SystemAudioCapture] 'soundcard' not installed. "
                  "Agent audio will be zeros. Install with: pip install soundcard")
            return

        try:
            default_speaker = sc.default_speaker()
            print(f"[RealTimeGreta SystemAudioCapture] Default speaker: {default_speaker.name}")

            loopback_mic = sc.get_microphone(
                id=str(default_speaker.name),
                include_loopback=True
            )
            print(f"[RealTimeGreta SystemAudioCapture] Loopback device: {loopback_mic.name}")

            chunk_frames = int(self.sample_rate * 0.05)  # 50ms chunks
            chunk_count = 0
            log_interval = 200  # log every N chunks (~10s at 50ms)

            with loopback_mic.recorder(samplerate=self.sample_rate, channels=1) as recorder:
                print("[SystemAudioCapture] Capturing agent audio (loopback)")

                while self.is_active:
                    try:
                        data = recorder.record(numframes=chunk_frames)
                        audio_float32 = data.flatten().astype(np.float32)
                        self.audio_buffer.push(audio_float32)
                        if self.wav_recorder is not None:
                            self.wav_recorder.append(audio_float32)
                        chunk_count += 1

                        if chunk_count == 1:
                            print(f"[SystemAudioCapture] First chunk captured ({len(audio_float32)} samples)")
                        if chunk_count % log_interval == 0:
                            buf_dur = self.audio_buffer.get_duration_sec()
                            rms = np.sqrt(np.mean(audio_float32 ** 2))
                            print(f"[SystemAudioCapture] chunks={chunk_count}, buf={buf_dur:.1f}s, "
                                  f"last_rms={rms:.6f} {'(agent speaking)' if rms > 0.001 else '(silence)'}")

                    except Exception as e:
                        print(f"[SystemAudioCapture] Record error: {e}")
                        time.sleep(0.1)

        except Exception as e:
            print(f"[RealTimeGreta SystemAudioCapture] Init error: {e}")
            print("[RealTimeGreta SystemAudioCapture] Agent audio will be zeros.")
            traceback.print_exc()

        print("[RealTimeGreta SystemAudioCapture] Stopped")

    def stop(self):
        with self.lock:
            self.is_active = False


# ============================================================================
# OpenFace Feature Buffer
# ============================================================================

class OpenFaceBuffer:
    """
    Thread-safe buffer for OpenFace features received from Java.

    Incoming CSV line format from OpenFaceFrame.java (28 values):
        timestamp,
        AU01..AU17  (17 AUs, normalized 0-1),
        gazeAngleX, gazeAngleY, gaze0.x, gaze0.y,   (4 gaze values)
        headPoseR.x, headPoseR.y, headPoseR.z,       (6 head pose values)
        headPoseT.x, headPoseT.y, headPoseT.z

    These map to model input channels:
        face1 : 17 AUs
        gaze1 : 4 gaze values
        head1 : 6 head pose values
        body1 : 3 zeros (not available from OpenFace)
    """

    # Column layout of the incoming CSV (after timestamp)
    FACE_START, FACE_END = 0, 17       # 17 AUs
    GAZE_START, GAZE_END = 17, 21      # 4 gaze values
    HEAD_START, HEAD_END = 21, 27      # 6 head pose values

    def __init__(self, frame_hz: float, max_duration_sec: float = 10.0):
        self.frame_hz = frame_hz
        self.max_frames = int(max_duration_sec * frame_hz)
        self.lock = Lock()

        self.dims = {'face': 17, 'gaze': 4, 'head': 6, 'body': 3}
        self.buffers = {k: deque(maxlen=self.max_frames) for k in self.dims}
        self.total_frames_received = 0

    def push_csv_line(self, csv_line: str):
        """
        Parse a CSV line from OpenFaceFrame.java and push into buffers.
        Expected: timestamp, 17 AUs, 4 gaze, 6 head (28 values total).
        """
        try:
            values = [float(v) for v in csv_line.strip().split(',')]
            if len(values) < 28:
                if self.total_frames_received == 0:
                    print(f"[OpenFaceBuffer] Malformed first line ({len(values)} values, expected 28)")
                return

            features = values[1:]  # skip timestamp

            face = np.array(features[self.FACE_START:self.FACE_END], dtype=np.float32)
            gaze = np.array(features[self.GAZE_START:self.GAZE_END], dtype=np.float32)
            head = np.array(features[self.HEAD_START:self.HEAD_END], dtype=np.float32)
            body = np.zeros(3, dtype=np.float32)

            with self.lock:
                self.buffers['face'].append(face)
                self.buffers['gaze'].append(gaze)
                self.buffers['head'].append(head)
                self.buffers['body'].append(body)
                self.total_frames_received += 1

                if self.total_frames_received == 1:
                    print(f"[OpenFaceBuffer] First frame parsed:")
                    print(f"  face AUs (17): {face[:5]}... (showing first 5)")
                    print(f"  gaze (4): {gaze}")
                    print(f"  head (6): {head}")
                if self.total_frames_received % 300 == 0:
                    print(f"[OpenFaceBuffer] {self.total_frames_received} frames buffered, "
                          f"face_mean={np.mean([np.mean(f) for f in self.buffers['face']]):.4f}")

        except (ValueError, IndexError) as e:
            print(f"[OpenFaceBuffer] Parse error: {e} (line: {csv_line[:60]}...)")

    def push_frame(self, features: Dict[str, np.ndarray]):
        """Push one frame of pre-parsed features"""
        with self.lock:
            for key, data in features.items():
                if key in self.buffers:
                    self.buffers[key].append(data)
            self.total_frames_received += 1

    def get_last_n_seconds(self, duration_sec: float) -> Dict[str, torch.Tensor]:
        """Get features as tensors for the last N seconds (speaker 1 = user)"""
        n_frames = int(duration_sec * self.frame_hz)
        result = {}

        with self.lock:
            for key, dim in self.dims.items():
                buf = list(self.buffers[key])
                if len(buf) >= n_frames:
                    arr = np.array(buf[-n_frames:], dtype=np.float32)
                else:
                    arr = np.zeros((n_frames, dim), dtype=np.float32)
                    if len(buf) > 0:
                        existing = np.array(buf, dtype=np.float32)
                        arr[-len(buf):] = existing

                result[f'{key}1'] = torch.tensor(arr, dtype=torch.float32)

        return result

    def has_data(self) -> bool:
        with self.lock:
            return self.total_frames_received > 0


# ============================================================================
# OpenFace Receiver (TCP server accepting features from Java)
# ============================================================================

class OpenFaceReceiver(Thread):
    """
    TCP server that accepts a connection from OpenFaceFrame.java and
    receives CSV feature lines in real-time.

    Protocol:
        - Python opens a ServerSocket on openface_port (default 5963)
        - Java calls OpenFaceFrame.connectToRealTimeGreta("localhost", 5963)
        - Java streams one CSV line per frame (at OpenFace frame rate ~30fps)
        - Python parses each line and feeds it into OpenFaceBuffer

    OpenFace typically runs at ~30fps while the model expects 50Hz.
    The OpenFaceBuffer accumulates frames at whatever rate they arrive,
    and the model uses nearest-neighbor sampling from the buffer.
    """

    def __init__(self, port: int, openface_buffer: OpenFaceBuffer):
        super().__init__(daemon=True)
        self.port = port
        self.openface_buffer = openface_buffer
        self.is_active = False
        self.server_socket = None
        self.client_socket = None

    def start(self):
        self.is_active = True
        super().start()

    def run(self):
        try:
            self.server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            self.server_socket.bind(('0.0.0.0', self.port))
            self.server_socket.listen(1)
            self.server_socket.settimeout(2.0)
            print(f"[OpenFaceReceiver] *** Listening on port {self.port} ***")
            print(f"[OpenFaceReceiver] Waiting for Java OpenFaceFrame to connect...")
            print(f"[OpenFaceReceiver] (Make sure to check the RealTimeGreta checkbox in the "
                  f"OpenFace GUI AFTER this message appears)")

            wait_ticks = 0
            while self.is_active:
                try:
                    self.client_socket, addr = self.server_socket.accept()
                    print(f"[OpenFaceReceiver] *** Java connected from {addr} ***")
                    self._receive_loop()
                    print(f"[OpenFaceReceiver] _receive_loop() exited, waiting for reconnection...")
                except socket.timeout:
                    wait_ticks += 1
                    if wait_ticks % 15 == 0:  # every ~30s
                        print(f"[OpenFaceReceiver] Still waiting for Java to connect on port {self.port}... "
                              f"({wait_ticks * 2}s elapsed)")
                    continue
                except Exception as e:
                    if self.is_active:
                        print(f"[OpenFaceReceiver] Accept error: {e}")
                        traceback.print_exc()
                        time.sleep(1)

        except Exception as e:
            print(f"[OpenFaceReceiver] Server bind/listen error: {e}")
            traceback.print_exc()
        finally:
            self._cleanup()

    def _receive_loop(self):
        """Read CSV lines from the connected Java client"""
        buffer = ""
        frame_count = 0
        recv_count = 0
        timeout_count = 0

        try:
            self.client_socket.settimeout(5.0)
            print(f"[OpenFaceReceiver] _receive_loop started, waiting for data...")

            while self.is_active:
                try:
                    data = self.client_socket.recv(4096)
                    recv_count += 1

                    if not data:
                        print(f"[OpenFaceReceiver] Java disconnected (recv returned empty after "
                              f"{frame_count} frames, {recv_count} recv calls)")
                        break

                    if recv_count == 1:
                        raw_preview = data[:200]
                        print(f"[OpenFaceReceiver] First raw data ({len(data)} bytes): "
                              f"{raw_preview}")

                    buffer += data.decode('utf-8', errors='replace')
                    timeout_count = 0  # reset timeout counter on successful recv

                    while '\n' in buffer:
                        line, buffer = buffer.split('\n', 1)
                        line = line.strip()
                        if line:
                            self.openface_buffer.push_csv_line(line)
                            frame_count += 1

                            if frame_count == 1:
                                print(f"[OpenFaceReceiver] First CSV line parsed: "
                                      f"{line[:100]}...")
                            if frame_count % 300 == 0:
                                print(f"[OpenFaceReceiver] Received {frame_count} frames "
                                      f"(buf: {self.openface_buffer.total_frames_received})")

                except socket.timeout:
                    timeout_count += 1
                    if timeout_count == 1:
                        print(f"[OpenFaceReceiver] Timeout waiting for data (5s) - "
                              f"is OpenFace still running? ({frame_count} frames so far)")
                    if timeout_count % 6 == 0:  # every ~30s of silence
                        print(f"[OpenFaceReceiver] Still no data ({timeout_count * 5}s of silence, "
                              f"{frame_count} frames total)")
                    continue

        except ConnectionResetError:
            print(f"[OpenFaceReceiver] Connection reset by Java (after {frame_count} frames)")
        except Exception as e:
            print(f"[OpenFaceReceiver] Receive error: {e}")
            traceback.print_exc()
        finally:
            print(f"[OpenFaceReceiver] _receive_loop ended: {frame_count} frames, "
                  f"{recv_count} recv calls")
            if self.client_socket:
                try:
                    self.client_socket.close()
                except:
                    pass
                self.client_socket = None

    def stop(self):
        self.is_active = False
        self._cleanup()

    def _cleanup(self):
        try:
            if self.client_socket:
                self.client_socket.close()
        except:
            pass
        try:
            if self.server_socket:
                self.server_socket.close()
        except:
            pass
        print("[RealTimeGreta OpenFaceReceiver] Stopped")


# ============================================================================
# Action Decider (thresholding + cooldowns)
# ============================================================================

class ActionDecider:
    """
    Takes model output probabilities and decides which action to trigger.
    Implements detection thresholds, cascade logic, and cooldown timers.
    """

    # Action name constants
    ACTION_NONE = "nothing"
    ACTION_SHORT_NOD = "shortNod"
    ACTION_LONG_NOD = "longNod"
    ACTION_LONG_NOD_TWICE = "longNodTwice"
    ACTION_CONTINUER_BC = "continuerBC"
    ACTION_ASSESSMENT_BC = "assessmentBC"
    ACTION_TURN_SHIFT = "turnShift"

    def __init__(self, config: GretaConfig):
        self.config = config
        self.last_bc_time = -999.0
        self.last_hn_time = -999.0
        self.last_shift_time = -999.0

    def decide(self, output: Dict, stride_frames: int, current_time: float,
               model_wrapper: VAPModelWrapper, verbose: bool = False) -> List[str]:
        """
        Analyze model output and decide actions for the stride window.
        When verbose=True, prints for every inference: predictions, thresholds,
        above/below, and whether each activation was triggered or blocked by cooldown.
        """
        actions = []
        conf = model_wrapper.conf

        # --- VAP turn-taking (always available) ---
        logits = output.get('logits')
        avg_shift_prob = 0.0
        shift_above = False
        shift_triggered = False
        shift_blocked_cd = False
        if logits is not None:
            vap_probs = model_wrapper.model.objective.get_probs(logits)
            p_now = vap_probs['p_now'][0, -stride_frames:, :].cpu().numpy()
            avg_shift_prob = np.mean(p_now[:, 1])
            shift_above = avg_shift_prob > self.config.vap_shift_threshold
            shift_on_cd = (current_time - self.last_shift_time) <= self.config.shift_cooldown

            if shift_above:
                if not shift_on_cd:
                    actions.append(self.ACTION_TURN_SHIFT)
                    self.last_shift_time = current_time
                    shift_triggered = True
                else:
                    shift_blocked_cd = True
        else:
            if verbose:
                print(f"  [VAP] No 'logits' in model output!")

        # --- Head Nod detection (cascade) ---
        avg_hn_prob = 0.0
        hn_type_probs_str = "-"
        has_hn = getattr(conf, 'predict_head_nods', False) and 'hn_detection_logits' in output
        hn_above = False
        hn_triggered = False
        hn_blocked_cd = False
        hn_action_str = "-"
        if has_hn:
            hn_det_logits = output['hn_detection_logits'][0, -stride_frames:, :]
            hn_det_probs = torch.softmax(hn_det_logits, dim=-1).cpu().numpy()
            avg_hn_prob = np.mean(hn_det_probs[:, 1])
            hn_above = avg_hn_prob > self.config.hn_detection_threshold
            hn_on_cd = (current_time - self.last_hn_time) <= self.config.hn_cooldown

            if hn_above:
                if not hn_on_cd:
                    if 'hn_type_logits' in output:
                        hn_type_logits = output['hn_type_logits'][0, -stride_frames:, :]
                        hn_type_probs = torch.softmax(hn_type_logits, dim=-1).cpu().numpy()
                        avg_type = np.mean(hn_type_probs, axis=0)
                        predicted_type = np.argmax(avg_type)
                        type_names = ['short', 'long', 'long_twice']
                        hn_action_str = type_names[predicted_type] if predicted_type < len(type_names) else f"idx{predicted_type}"
                        hn_type_probs_str = ",".join(f"{p:.3f}" for p in avg_type)

                        if predicted_type == 0:
                            actions.append(self.ACTION_SHORT_NOD)
                        elif predicted_type == 1:
                            actions.append(self.ACTION_LONG_NOD)
                        elif predicted_type == 2:
                            actions.append(self.ACTION_LONG_NOD_TWICE)
                    else:
                        actions.append(self.ACTION_SHORT_NOD)
                        hn_action_str = "short(default)"
                        hn_type_probs_str = "-"
                    self.last_hn_time = current_time
                    hn_triggered = True
                else:
                    hn_blocked_cd = True
                    if 'hn_type_logits' in output:
                        hn_type_logits = output['hn_type_logits'][0, -stride_frames:, :]
                        hn_type_probs = torch.softmax(hn_type_logits, dim=-1).cpu().numpy()
                        avg_type = np.mean(hn_type_probs, axis=0)
                        type_names = ['short', 'long', 'long_twice']
                        pred = np.argmax(avg_type)
                        hn_action_str = type_names[pred] if pred < len(type_names) else f"idx{pred}"
                        hn_type_probs_str = ",".join(f"{p:.3f}" for p in avg_type)

        # --- Backchannel detection (cascade) ---
        avg_bc_prob = 0.0
        bc_type_probs_str = "-"
        has_bc = getattr(conf, 'predict_backchannels', False) and 'bc_detection_logits' in output
        bc_above = False
        bc_triggered = False
        bc_blocked_cd = False
        bc_action_str = "-"
        if has_bc:
            bc_det_logits = output['bc_detection_logits'][0, -stride_frames:, :]
            bc_det_probs = torch.softmax(bc_det_logits, dim=-1).cpu().numpy()
            avg_bc_prob = np.mean(bc_det_probs[:, 1])
            bc_above = avg_bc_prob > self.config.bc_detection_threshold
            bc_on_cd = (current_time - self.last_bc_time) <= self.config.bc_cooldown

            if bc_above:
                if not bc_on_cd:
                    if 'bc_type_logits' in output:
                        bc_type_logits = output['bc_type_logits'][0, -stride_frames:, :]
                        bc_type_probs = torch.softmax(bc_type_logits, dim=-1).cpu().numpy()
                        avg_type = np.mean(bc_type_probs, axis=0)
                        predicted_type = np.argmax(avg_type)
                        type_names = ['continuer', 'assessment']
                        bc_action_str = type_names[predicted_type] if predicted_type < len(type_names) else f"idx{predicted_type}"
                        bc_type_probs_str = ",".join(f"{p:.3f}" for p in avg_type)

                        if predicted_type == 0:
                            actions.append(self.ACTION_CONTINUER_BC)
                        elif predicted_type == 1:
                            actions.append(self.ACTION_ASSESSMENT_BC)
                    else:
                        actions.append(self.ACTION_CONTINUER_BC)
                        bc_action_str = "continuer(default)"
                        bc_type_probs_str = "-"
                    self.last_bc_time = current_time
                    bc_triggered = True
                else:
                    bc_blocked_cd = True
                    if 'bc_type_logits' in output:
                        bc_type_logits = output['bc_type_logits'][0, -stride_frames:, :]
                        bc_type_probs = torch.softmax(bc_type_logits, dim=-1).cpu().numpy()
                        avg_type = np.mean(bc_type_probs, axis=0)
                        type_names = ['continuer', 'assessment']
                        pred = np.argmax(avg_type)
                        bc_action_str = type_names[pred] if pred < len(type_names) else f"idx{pred}"
                        bc_type_probs_str = ",".join(f"{p:.3f}" for p in avg_type)

        # --- Per-inference log (when verbose) ---
        if verbose:
            print(f"  [Predictions]")
            print(f"    VAP shift:  p={avg_shift_prob:.3f}  threshold={self.config.vap_shift_threshold:.3f}  "
                  f"{'ABOVE' if shift_above else 'below'}  -> "
                  f"{'TRIGGERED (turnShift)' if shift_triggered else ('BLOCKED (cooldown)' if shift_blocked_cd else 'no action')}")
            if has_hn:
                print(f"    HN:         p={avg_hn_prob:.3f}  threshold={self.config.hn_detection_threshold:.3f}  "
                      f"{'ABOVE' if hn_above else 'below'}  type_probs=[{hn_type_probs_str}]  -> "
                      f"{'TRIGGERED (' + hn_action_str + ')' if hn_triggered else ('BLOCKED (cooldown)' if hn_blocked_cd else 'no action')}")
            else:
                print(f"    HN:         (disabled)")
            if has_bc:
                print(f"    BC:         p={avg_bc_prob:.3f}  threshold={self.config.bc_detection_threshold:.3f}  "
                      f"{'ABOVE' if bc_above else 'below'}  type_probs=[{bc_type_probs_str}]  -> "
                      f"{'TRIGGERED (' + bc_action_str + ')' if bc_triggered else ('BLOCKED (cooldown)' if bc_blocked_cd else 'no action')}")
            else:
                print(f"    BC:         (disabled)")
            print(f"  [Decision] actions={actions if actions else 'none'}")

            if shift_triggered:
                print(f"  >>> ACTION: TURN SHIFT (p={avg_shift_prob:.3f})")
            if hn_triggered:
                print(f"  >>> ACTION: HEAD NOD [{hn_action_str}] (det={avg_hn_prob:.3f})")
            if bc_triggered:
                print(f"  >>> ACTION: BACKCHANNEL [{bc_action_str}] (det={avg_bc_prob:.3f})")

        return actions


# ============================================================================
# Greta Action Dispatcher (sends commands to Java via TCP socket)
# ============================================================================

class GretaDispatcher:
    """
    Sends action commands to the Java TurnManagement module via TCP socket.
    Uses the same socket protocol as turnManagerWoz.py (port 5961).

    The Java side will receive action strings and trigger the appropriate
    FML file loading (head nods, verbal backchannels, or interruptions).
    """

    def __init__(self, host: str, port: int):
        self.host = host
        self.port = port
        self.socket = None
        self.connected = False

    def connect(self, feedback_host: str, feedback_port: int):
        """
        Connect to the Greta Java server (same pattern as turnManagerWoz.py).
        Also starts the feedback socket for agent speaking state.
        """
        # Feedback socket (agent speaking state)
        self.feedback_socket = socket.socket()
        self.feedback_socket.connect((feedback_host, feedback_port))
        self.feedback_socket.settimeout(0.05)

        feedback_thread = Thread(target=self._feedback_loop, daemon=True)
        feedback_thread.start()

        # Main command socket
        self.socket = socket.socket()
        self.socket.connect((self.host, self.port))

        # Wait for acknowledgement
        text_buffer_size = 100
        while True:
            try:
                message = self.socket.recv(text_buffer_size)
                print(f"[RealTimeGreta Dispatcher] Connection established: {message.decode()}")
                break
            except socket.timeout:
                pass
            except Exception as e:
                print(f"[RealTimeGreta Dispatcher] Connection error: {e}")
                sys.exit()

        self.connected = True

    def send_action(self, action: str):
        """Send an action command to Java"""
        if not self.connected:
            print("[Dispatcher] Not connected, cannot send action")
            return

        try:
            self.socket.send('{}\r\n'.format(action).encode())
            response = self.socket.recv(100).decode().strip()

            if action != "nothing":
                print(f"  [Dispatcher] SENT '{action}' -> Java responded: '{response}'")

            if 'kill' in response:
                print("[Dispatcher] Kill signal received from Java")
                self.connected = False
            if 'updateMicPort' in response:
                new_port = int(response.split(" ")[1])
                print(f"[Dispatcher] Mic port update requested: {new_port}")

        except Exception as e:
            print(f"[Dispatcher] Send error for '{action}': {e}")

    def send_ready(self):
        """Send ready signal to Java (same as turnManagerWoz.py)"""
        try:
            self.socket.send('[RealTimeGreta] Generator started\r\n'.encode())
            self.socket.settimeout(1)
            print("[RealTimeGreta Dispatcher] Ready signal sent")
        except Exception as e:
            print(f"[RealTimeGreta Dispatcher] Ready signal error: {e}")

    def close(self):
        """Close all sockets"""
        try:
            if self.socket:
                self.socket.close()
            if self.feedback_socket:
                self.feedback_socket.close()
        except:
            pass

    def _feedback_loop(self):
        """Receive agent speaking state feedback from Java (kept for compatibility)"""
        print("[RealTimeGreta Feedback] Loop started")
        while True:
            try:
                data = self.feedback_socket.recv(100).decode()
                self.feedback_socket.send('ok\r\n'.encode())
            except socket.timeout:
                continue
            except ConnectionResetError:
                print("[RealTimeGreta Feedback] ConnectionReset")
                break
            except Exception:
                break
        print("[RealTimeGreta Feedback] Loop ended")


# ============================================================================
# Streaming Inference Engine
# ============================================================================

class StreamingInferenceEngine:
    """
    Core inference loop.
    Every stride_duration seconds, extracts context from the audio buffer,
    runs the model, and dispatches actions to Greta.
    """

    def __init__(self, model_wrapper: VAPModelWrapper, config: GretaConfig,
                 audio_buffer_user: StreamingAudioBuffer,
                 audio_buffer_agent: StreamingAudioBuffer,
                 openface_buffer: OpenFaceBuffer,
                 dispatcher: GretaDispatcher,
                 session_t0: Optional[float] = None,
                 action_logger: Optional[ActionTimelineLogger] = None):
        self.model = model_wrapper
        self.config = config
        self.audio_buffer_user = audio_buffer_user
        self.audio_buffer_agent = audio_buffer_agent
        self.openface_buffer = openface_buffer
        self.dispatcher = dispatcher
        self.session_t0 = session_t0
        self.action_logger = action_logger
        self.action_decider = ActionDecider(config)
        self.running = False

        stride_frames = int(config.stride_duration * model_wrapper.conf.frame_hz)
        model_wrapper.conf.inference_predict_last_n_frames = stride_frames

    def run(self):
        """Main inference loop (blocking, runs on main thread or a dedicated thread)"""
        self.running = True
        config = self.config
        model = self.model
        stride_frames = int(config.stride_duration * model.conf.frame_hz)

        print(f"\n{'='*70}")
        print(f"  [Engine] STARTING INFERENCE LOOP")
        print(f"{'='*70}")
        print(f"  Context window:  {config.context_duration}s")
        print(f"  Stride:          {config.stride_duration}s ({stride_frames} frames)")
        print(f"  Shift threshold: {config.vap_shift_threshold} (cooldown: {config.shift_cooldown}s)")
        print(f"  BC threshold:    {config.bc_detection_threshold} (cooldown: {config.bc_cooldown}s)")
        print(f"  HN threshold:    {config.hn_detection_threshold} (cooldown: {config.hn_cooldown}s)")
        print(f"  Model outputs: logits=always, "
              f"bc={'YES' if getattr(model.conf, 'predict_backchannels', False) else 'NO'}, "
              f"hn={'YES' if getattr(model.conf, 'predict_head_nods', False) else 'NO'}")
        print(f"  Waiting for {config.context_duration}s of user audio to build initial context...")

        # Wait until we have enough user audio
        wait_start = time.time()
        while self.audio_buffer_user.get_total_duration_sec() < config.context_duration:
            elapsed_wait = time.time() - wait_start
            if int(elapsed_wait) % 2 == 0 and elapsed_wait > 0:
                mic_dur = self.audio_buffer_user.get_total_duration_sec()
                of_frames = self.openface_buffer.total_frames_received
                agent_dur = self.audio_buffer_agent.get_total_duration_sec()
                print(f"  [Engine] Waiting... mic_audio={mic_dur:.1f}s/{config.context_duration}s, "
                      f"agent_audio={agent_dur:.1f}s, openface_frames={of_frames}")
            time.sleep(0.5)
            if not self.running:
                return

        print(f"  [Engine] Initial context ready! Starting inference...")

        # Signal ready to Java
        self.dispatcher.send_ready()

        inference_count = 0
        total_actions_sent = 0
        total_gated = 0

        # VAD threshold for listener gating (same as HybridRealTime)
        vad_threshold = 0.002

        while self.running:
            inference_count += 1
            loop_start = time.perf_counter()

            try:
                print(f"\n--- Inference #{inference_count} ---")

                # Get context window of user audio (mic)
                user_np = self.audio_buffer_user.get_last_n_seconds(config.context_duration)
                audio_user = torch.tensor(user_np, dtype=torch.float32)

                # Get context window of agent audio (system loopback)
                agent_np = self.audio_buffer_agent.get_last_n_seconds(config.context_duration)
                audio_agent = torch.tensor(agent_np, dtype=torch.float32)

                # VAD on the last stride_duration of audio for listener gating
                stride_samples = int(config.stride_duration * config.sample_rate)
                user_stride = user_np[-stride_samples:] if len(user_np) >= stride_samples else user_np
                agent_stride = agent_np[-stride_samples:] if len(agent_np) >= stride_samples else agent_np
                user_rms = float(np.sqrt(np.mean(user_stride ** 2)))
                agent_rms = float(np.sqrt(np.mean(agent_stride ** 2)))
                user_speaking = user_rms > vad_threshold
                agent_speaking = agent_rms > vad_threshold

                # Get OpenFace features for context window
                multimodal = self.openface_buffer.get_last_n_seconds(config.context_duration)

                of_total = self.openface_buffer.total_frames_received
                of_has = self.openface_buffer.has_data()
                mm_keys = list(multimodal.keys()) if multimodal else []
                mm_shapes = {k: v.shape for k, v in multimodal.items()} if multimodal else {}
                print(f"  [Input context] context_duration={config.context_duration}s")
                print(f"  [Input] user_audio: {audio_user.shape}, agent_audio: {audio_agent.shape}")
                print(f"  [VAD]   user_rms={user_rms:.4f} {'SPEAKING' if user_speaking else 'SILENT'} | "
                      f"agent_rms={agent_rms:.4f} {'SPEAKING' if agent_speaking else 'SILENT'}")
                print(f"  [Input] openface: has_data={of_has}, total_frames={of_total}, mm_keys={mm_keys}")
                if mm_shapes:
                    for k, s in mm_shapes.items():
                        print(f"  [Input]   {k}: {s}")

                # Prepare batch
                batch = model.prepare_batch(audio_user, audio_agent,
                                            multimodal_features=multimodal,
                                            verbose=True)

                # Forward pass
                t_fwd_start = time.perf_counter()
                with torch.no_grad():
                    output = model.forward(batch)
                t_fwd = time.perf_counter() - t_fwd_start

                output_keys = list(output.keys())
                shapes = {k: v.shape if hasattr(v, 'shape') else type(v).__name__
                          for k, v in output.items()}
                print(f"  [Forward] {t_fwd*1000:.1f}ms, output_keys={output_keys}")
                for k, s in shapes.items():
                    print(f"  [Forward]   {k}: {s}")

                current_time = self.audio_buffer_user.get_total_duration_sec()

                # Decide actions (always verbose: predictions, thresholds, cooldowns)
                actions = self.action_decider.decide(output, stride_frames,
                                                     current_time, model,
                                                     verbose=True)

                # Listener gate: BC/HN only when user is speaking AND agent is silent
                listener_ok = user_speaking and (not agent_speaking)
                # Halt gate: turnShift only when user is actually speaking
                halt_ok = user_speaking

                for action in actions:
                    if action in (ActionDecider.ACTION_SHORT_NOD,
                                  ActionDecider.ACTION_LONG_NOD,
                                  ActionDecider.ACTION_LONG_NOD_TWICE,
                                  ActionDecider.ACTION_CONTINUER_BC,
                                  ActionDecider.ACTION_ASSESSMENT_BC):
                        if listener_ok:
                            self.dispatcher.send_action(action)
                            if self.action_logger is not None and self.session_t0 is not None:
                                self.action_logger.add_event(time.perf_counter() - self.session_t0, action)
                            total_actions_sent += 1
                            print(f"  >>> GRETA: sent '{action}' (agent listener behavior)")
                        else:
                            total_gated += 1
                            print(f"  >>> GATED '{action}' -- user={'SPEAKING' if user_speaking else 'SILENT'} "
                                  f"agent={'SPEAKING' if agent_speaking else 'SILENT'} "
                                  f"(need: user=SPEAKING & agent=SILENT)")
                    elif action == ActionDecider.ACTION_TURN_SHIFT:
                        if halt_ok:
                            self.dispatcher.send_action(action)
                            if self.action_logger is not None and self.session_t0 is not None:
                                self.action_logger.add_event(time.perf_counter() - self.session_t0, action)
                            total_actions_sent += 1
                            print(f"  >>> GRETA: sent '{action}' (user is speaking, halt agent)")
                        else:
                            total_gated += 1
                            print(f"  >>> GATED '{action}' -- user={'SPEAKING' if user_speaking else 'SILENT'} "
                                  f"(need: user=SPEAKING to halt)")
                    else:
                        self.dispatcher.send_action(action)
                        if self.action_logger is not None and self.session_t0 is not None:
                            self.action_logger.add_event(time.perf_counter() - self.session_t0, action)
                        total_actions_sent += 1

                # If no action, still send heartbeat so Java knows we're alive
                if not actions:
                    self.dispatcher.send_action("nothing")

                # Cleanup GPU memory
                del output, batch
                if torch.cuda.is_available():
                    torch.cuda.empty_cache()

                # Performance and summary (every inference)
                inference_time = time.perf_counter() - loop_start
                rt_ratio = config.stride_duration / inference_time if inference_time > 0 else float('inf')
                status = "OK" if rt_ratio >= 1.0 else "SLOW"
                print(f"  [Perf] total={inference_time*1000:.0f}ms (fwd={t_fwd*1000:.0f}ms), "
                      f"RT ratio={rt_ratio:.2f}x [{status}]")
                print(f"  [Stats] t={current_time:.1f}s, total_actions_sent={total_actions_sent}, "
                      f"total_gated={total_gated}, "
                      f"actions_this_step={actions if actions else 'none'}")

            except Exception as e:
                print(f"[Engine] Inference #{inference_count} ERROR: {e}")
                traceback.print_exc()

            # Maintain stride timing
            elapsed = time.perf_counter() - loop_start
            sleep_time = max(0, config.stride_duration - elapsed)
            if sleep_time > 0:
                time.sleep(sleep_time)

        print(f"[Engine] Inference loop stopped after {inference_count} steps, "
              f"{total_actions_sent} actions sent, {total_gated} gated")

    def stop(self):
        self.running = False


# ============================================================================
# Main Entry Point
# ============================================================================

def main():
    parser = argparse.ArgumentParser(description='RealTimeGreta - VAP Model for Greta Platform')

    # --- Model arguments ---
    parser.add_argument(
        '--checkpoint', type=str, default=DEFAULT_CHECKPOINT,
        help='Path to VAP model checkpoint (.ckpt); relative paths are under TurnManagement/',
    )
    parser.add_argument('--device', type=str, default='auto',
                        choices=['auto', 'cuda', 'cpu'],
                        help="Device for inference ('auto' = CUDA if available)")

    # Model architecture flags (override checkpoint config if provided)
    parser.add_argument('--predict_backchannels', action='store_true', default=None,
                        help='Force enable BC prediction heads')
    parser.add_argument('--predict_hn', action='store_true', default=None,
                        help='Force enable HN prediction heads (predict_head_nods)')
    parser.add_argument('--multimodal', action='store_true', default=None,
                        help='Force enable multimodal input (face AUs, gaze, head, body)')
    parser.add_argument('--use_face_encoder', action='store_true', default=None,
                        help='Force enable face encoder (EncoderFormerDFER)')
    parser.add_argument('--use_hubert_semantics', action='store_true', default=None,
                        help='Force enable HuBERT semantic encoder')
    parser.add_argument('--use_whisper_semantics', action='store_true', default=None,
                        help='Force enable Whisper semantic encoder')

    # Model mode (architecture variant)
    parser.add_argument('--mode', type=int, default=None,
                        help='Model architecture mode (0=full, 1=audio+face, etc.)')

    # Pretrained model paths (override checkpoint-embedded paths)
    parser.add_argument('--face_encoder_path', type=str, default=None,
                        help='Path to pretrained face encoder weights (.pth)')
    parser.add_argument('--pretrained_vap', type=str, default=None,
                        help='Path to pretrained VAP state dict')
    parser.add_argument('--pretrained_cpc', type=str, default=None,
                        help='Path to pretrained CPC encoder weights')
    parser.add_argument('--pretrained_hubert_model', type=str, default=None,
                        help='HuBERT model name or path (e.g. facebook/hubert-base-ls960)')
    parser.add_argument('--pretrained_whisper_model', type=str, default=None,
                        help='Whisper model name or path (e.g. openai/whisper-tiny.en)')

    # --- Network ports ---
    parser.add_argument('--openface_port', type=int, default=5963,
                        help='OpenFace receiver port [default: 5963]')

    # --- Microphone ---
    parser.add_argument('--mic_device', type=int, default=None,
                        help='Microphone device index for sounddevice (None = default mic)')

    # --- Session recording (experiment logs) ---
    parser.add_argument('--record', action='store_true', default=True,
                        help='Save full-session WAV (user mic + agent loopback) AND action CSV')
    parser.add_argument('--record_csv_only', action='store_true', default=False,
                        help='Save only the action CSV (no audio WAVs)')
    parser.add_argument('--record_dir', type=str, default="C:/Users/Pelachaud/Documents/",
                        help='Output folder for recordings (required with --record or --record_csv_only)')

    # --- Inference parameters ---
    parser.add_argument('--context_duration', type=float, default=5.0,
                        help='Context window in seconds [default: 5.0]')
    parser.add_argument('--stride_duration', type=float, default=0.4,
                        help='Inference stride in seconds [default: 1.0]')
    parser.add_argument('--bc_threshold', type=float, default=0.185,
                        help='BC detection threshold [default: 0.5]')
    parser.add_argument('--hn_threshold', type=float, default=0.6,
                        help='HN detection threshold [default: 0.5]')
    parser.add_argument('--shift_threshold', type=float, default=0.5,
                        help='VAP shift detection threshold [default: 0.6]')
    parser.add_argument('--bc_cooldown', type=float, default=3.0,
                        help='BC cooldown in seconds [default: 3.0]')
    parser.add_argument('--hn_cooldown', type=float, default=2.0,
                        help='HN cooldown in seconds [default: 2.0]')
    parser.add_argument('--shift_cooldown', type=float, default=5.0,
                        help='Turn shift cooldown in seconds [default: 5.0]')

    args = parser.parse_args()
    args.checkpoint = _resolve_path_under_turn_mgmt(args.checkpoint)
    if args.face_encoder_path is not None:
        args.face_encoder_path = _resolve_path_under_turn_mgmt(args.face_encoder_path)
    if args.pretrained_vap is not None:
        args.pretrained_vap = _resolve_path_under_turn_mgmt(args.pretrained_vap)
    if args.pretrained_cpc is not None:
        args.pretrained_cpc = _resolve_path_under_turn_mgmt(args.pretrained_cpc)
    if not os.path.isfile(args.checkpoint):
        raise FileNotFoundError(
            f"VAP checkpoint not found: {args.checkpoint}\n"
            f"Copy .ckpt files into {CHECKPOINTS_DIR} (see sam_vap/checkpoints/README.txt)."
        )

    # Record mode flags
    args.record_csv = args.record or args.record_csv_only
    args.record_audio = args.record and not args.record_csv_only

    if args.record_csv and not args.record_dir:
        parser.error("--record/--record_csv_only requires --record_dir PATH")

    print("\n" + "=" * 70)
    print("  RealTimeGreta - VAP Model Integration for Greta Platform")
    print("=" * 70)

    # Resolve device
    if args.device == 'auto':
        device = 'cuda' if torch.cuda.is_available() else 'cpu'
    elif args.device == 'cuda' and not torch.cuda.is_available():
        print("[RealTimeGreta] CUDA not available, falling back to CPU")
        device = 'cpu'
    else:
        device = args.device
    print(f"[RealTimeGreta] Using device: {device}")

    # Build configuration
    config = GretaConfig(
        openface_port=args.openface_port,
        context_duration=args.context_duration,
        stride_duration=args.stride_duration,
        bc_detection_threshold=args.bc_threshold,
        hn_detection_threshold=args.hn_threshold,
        vap_shift_threshold=args.shift_threshold,
        bc_cooldown=args.bc_cooldown,
        hn_cooldown=args.hn_cooldown,
        shift_cooldown=args.shift_cooldown,
    )

    # 1) Build config overrides from CLI arguments
    # Only non-None values are applied; checkpoint config is used otherwise.
    config_overrides = {}

    # Architecture flags (store_true args: None means not provided, True means --flag was set)
    if args.predict_backchannels is not None:
        config_overrides['predict_backchannels'] = True
    if args.predict_hn is not None:
        config_overrides['predict_head_nods'] = True
    if args.multimodal is not None:
        config_overrides['multimodal'] = True
    if args.use_face_encoder is not None:
        config_overrides['use_face_encoder'] = True
    if args.use_hubert_semantics is not None:
        config_overrides['use_hubert_semantics'] = True
    if args.use_whisper_semantics is not None:
        config_overrides['use_whisper_semantics'] = True
    if args.mode is not None:
        config_overrides['mode'] = args.mode

    # Pretrained model paths
    if args.face_encoder_path is not None:
        config_overrides['pretrained_face_encoder'] = args.face_encoder_path
    if args.pretrained_vap is not None:
        config_overrides['pretrained_vap'] = args.pretrained_vap
    if args.pretrained_cpc is not None:
        config_overrides['pretrained_cpc'] = args.pretrained_cpc
    if args.pretrained_hubert_model is not None:
        config_overrides['pretrained_hubert_model'] = args.pretrained_hubert_model
    if args.pretrained_whisper_model is not None:
        config_overrides['pretrained_whisper_model'] = args.pretrained_whisper_model

    # Load model
    print("\n[RealTimeGreta] Loading VAP model...")
    model_wrapper = VAPModelWrapper(
        args.checkpoint,
        device=device,
        config_overrides=config_overrides
    )

    # Print active capabilities summary
    mc = model_wrapper.conf
    print(f"\n[RealTimeGreta] Active capabilities:")
    print(f"  VAP turn-taking:    always ON")
    print(f"  BC prediction:      {'ON' if getattr(mc, 'predict_backchannels', False) else 'OFF'}")
    print(f"  HN prediction:      {'ON' if getattr(mc, 'predict_head_nods', False) else 'OFF'}")
    print(f"  Face encoder:       {'ON' if getattr(mc, 'use_face_encoder', False) else 'OFF'}")
    print(f"  Multimodal:         {'ON' if getattr(mc, 'multimodal', False) else 'OFF'}")
    print(f"  HuBERT semantics:   {'ON' if getattr(mc, 'use_hubert_semantics', False) else 'OFF'}")
    print(f"  Whisper semantics:  {'ON' if getattr(mc, 'use_whisper_semantics', False) else 'OFF'}")

    # Update config with model's actual sample rate and frame rate
    config.sample_rate = model_wrapper.conf.sample_rate
    config.frame_hz = model_wrapper.conf.frame_hz

    # Optional WAV recorders (filled by MicCapture / SystemAudioCapture)
    recorder_user: Optional[WavSessionRecorder] = None
    recorder_agent: Optional[WavSessionRecorder] = None
    action_logger: Optional[ActionTimelineLogger] = None
    session_t0 = time.perf_counter()
    if args.record_csv:
        os.makedirs(args.record_dir, exist_ok=True)
        stamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        base = os.path.join(os.path.abspath(args.record_dir), f"greta_rt_{stamp}")
        action_csv = base + "_actions.csv"
        action_logger = ActionTimelineLogger(action_csv, frame_hz=config.frame_hz)
        if args.record_audio:
            user_wav = base + "_user.wav"
            agent_wav = base + "_agent.wav"
            recorder_user = WavSessionRecorder(user_wav, config.sample_rate, session_t0=session_t0)
            recorder_agent = WavSessionRecorder(agent_wav, config.sample_rate, session_t0=session_t0)
            print(f"\n[RealTimeGreta] Session recording ENABLED (audio + CSV)")
            print(f"  User (mic):       {user_wav}")
            print(f"  Agent (loopback): {agent_wav}")
            print(f"  Actions (CSV):    {action_csv}")
        else:
            print(f"\n[RealTimeGreta] Session recording ENABLED (CSV only, no audio)")
            print(f"  Actions (CSV):    {action_csv}")

    # 2) Create streaming audio buffers (user mic + agent loopback)
    audio_buffer_user = StreamingAudioBuffer(
        max_duration_sec=config.context_duration + 5.0,
        sample_rate=config.sample_rate
    )
    audio_buffer_agent = StreamingAudioBuffer(
        max_duration_sec=config.context_duration + 5.0,
        sample_rate=config.sample_rate
    )

    # 2b) Start system audio loopback capture for agent voice
    print("\n[RealTimeGreta] Starting system audio loopback capture (agent voice)...")
    system_audio_capture = SystemAudioCapture(
        audio_buffer=audio_buffer_agent,
        sample_rate=config.sample_rate,
        wav_recorder=recorder_agent
    )
    system_audio_capture.start()

    # 3) Create OpenFace feature buffer and start receiver
    openface_buffer = OpenFaceBuffer(
        frame_hz=config.frame_hz,
        max_duration_sec=config.context_duration + 5.0
    )

    print(f"\n[RealTimeGreta] Starting OpenFace receiver on port {config.openface_port}...")
    openface_receiver = OpenFaceReceiver(
        port=config.openface_port,
        openface_buffer=openface_buffer
    )
    openface_receiver.start()

    # 4) Start direct microphone capture (bypasses Java mic server)
    print("\n[RealTimeGreta] Starting microphone capture (direct via sounddevice)...")
    mic_capture = MicCapture(
        audio_buffer=audio_buffer_user,
        sample_rate=config.sample_rate,
        device_index=args.mic_device,
        wav_recorder=recorder_user
    )
    mic_capture.start()

    # 5) Connect to Greta Java backend
    print("\n[RealTimeGreta] Connecting to Greta Java backend...")
    greta_host = socket.gethostname()
    dispatcher = GretaDispatcher(host=greta_host, port=config.greta_port)
    dispatcher.connect(feedback_host=greta_host, feedback_port=config.feedback_port)

    # 6) Create and run inference engine
    engine = StreamingInferenceEngine(
        model_wrapper=model_wrapper,
        config=config,
        audio_buffer_user=audio_buffer_user,
        audio_buffer_agent=audio_buffer_agent,
        openface_buffer=openface_buffer,
        dispatcher=dispatcher,
        session_t0=session_t0,
        action_logger=action_logger
    )

    print("\n[RealTimeGreta] System ready. Starting inference loop...")
    print("  Press Ctrl+C to stop.\n")

    try:
        engine.run()  # Blocking
    except KeyboardInterrupt:
        print("\n[RealTimeGreta] Interrupted by user")
    finally:
        engine.stop()
        mic_capture.stop()
        system_audio_capture.stop()
        mic_capture.join(timeout=3.0)
        system_audio_capture.join(timeout=3.0)
        openface_receiver.stop()
        dispatcher.close()
        session_end = max(0.0, time.perf_counter() - session_t0)
        if recorder_user is not None:
            recorder_user.finalize(total_duration_sec=session_end)
        if recorder_agent is not None:
            recorder_agent.finalize(total_duration_sec=session_end)
        if action_logger is not None:
            action_logger.finalize(total_duration_sec=session_end)
        print("[RealTimeGreta] Shutdown complete")


if __name__ == '__main__':
    main()
