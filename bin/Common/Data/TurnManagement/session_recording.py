# -*- coding: utf-8 -*-
"""
Shared session recording helpers for TurnManagement Python scripts.

Recording classes are copied from RealTimeGreta.py (proven working).
  - WavSessionRecorder: wall-clock placement, finalize with session_end padding
  - SystemAudioCapture: agent loopback via soundcard
  - MicRecordCapture: direct mic via sounddevice (bypasses Java socket)
  - ClassicTurnTimelineLogger: CSV for classic turnManager actions
  - ActionTimelineLogger: CSV for RealTime VAP actions
"""
from __future__ import annotations

import csv
import os
import time
import traceback
import wave
from threading import Lock, Thread
from typing import Dict, List, Optional, Tuple

import numpy as np


# ---------------------------------------------------------------------------
# WAV session recorder  (identical to RealTimeGreta.py)
# ---------------------------------------------------------------------------

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


# ---------------------------------------------------------------------------
# Direct microphone capture for recording  (like RealTimeGreta MicCapture,
# but without StreamingAudioBuffer — only writes to WavSessionRecorder)
# ---------------------------------------------------------------------------

class MicRecordCapture(Thread):
    """
    Captures audio from the default microphone using `sounddevice`.
    This bypasses the Java mic server entirely for recording purposes.
    The Java socket VAD still runs in parallel for turn management.

    Install: pip install sounddevice
    """

    def __init__(
        self,
        sample_rate: int = 16000,
        device_index: Optional[int] = None,
        wav_recorder: Optional[WavSessionRecorder] = None,
    ):
        super().__init__(daemon=True)
        self.sample_rate = sample_rate
        self.device_index = device_index
        self.wav_recorder = wav_recorder
        self.is_active = False
        self._lock = Lock()

    def start(self):
        self.is_active = True
        super().start()

    def run(self):
        try:
            import sounddevice as sd
        except ImportError:
            print(
                "[MicRecordCapture] ERROR: 'sounddevice' not installed! "
                "User WAV will be empty. Install with: pip install sounddevice"
            )
            return

        chunk_duration = 0.05
        chunk_frames = int(self.sample_rate * chunk_duration)
        chunk_count = 0
        log_interval = 200

        try:
            default_input = sd.query_devices(kind='input')
            print(
                f"[MicRecordCapture] Default input device: {default_input['name']} "
                f"(index={sd.default.device[0]}, sr={default_input['default_samplerate']})"
            )
            if self.device_index is not None:
                dev_info = sd.query_devices(self.device_index)
                print(f"[MicRecordCapture] Using specified device #{self.device_index}: {dev_info['name']}")

            device = self.device_index

            with sd.InputStream(
                samplerate=self.sample_rate,
                channels=1,
                dtype='float32',
                device=device,
                blocksize=chunk_frames,
            ) as stream:
                print(
                    f"[MicRecordCapture] Recording started "
                    f"(sr={self.sample_rate}, chunk={chunk_frames} samples/{chunk_duration*1000:.0f}ms)"
                )
                while self.is_active:
                    try:
                        data, overflowed = stream.read(chunk_frames)
                        if overflowed:
                            print("[MicRecordCapture] WARNING: input overflow (dropped samples)")

                        audio_float32 = data.flatten().astype(np.float32)
                        if self.wav_recorder is not None:
                            self.wav_recorder.append(audio_float32)
                        chunk_count += 1

                        if chunk_count == 1:
                            rms = np.sqrt(np.mean(audio_float32 ** 2))
                            print(f"[MicRecordCapture] First chunk ({len(audio_float32)} samples, rms={rms:.4f})")

                        if chunk_count % log_interval == 0:
                            rms = np.sqrt(np.mean(audio_float32 ** 2))
                            dur = chunk_count * chunk_duration
                            print(
                                f"[MicRecordCapture] chunks={chunk_count}, ~{dur:.1f}s, "
                                f"rms={rms:.4f} {'(speech)' if rms > 0.005 else '(quiet)'}"
                            )

                    except Exception as e:
                        print(f"[MicRecordCapture] Read error: {e}")
                        time.sleep(0.01)

        except Exception as e:
            print(f"[MicRecordCapture] Init error: {e}")
            traceback.print_exc()

        print(f"[MicRecordCapture] Stopped (total chunks: {chunk_count})")

    def stop(self):
        with self._lock:
            self.is_active = False


# ---------------------------------------------------------------------------
# System audio loopback capture  (identical to RealTimeGreta.py)
# ---------------------------------------------------------------------------

class SystemAudioCapture(Thread):
    """
    Captures the system audio output (speaker loopback) to get the agent's
    TTS voice. Uses `soundcard` with WASAPI loopback on Windows.

    Install: pip install soundcard
    """

    def __init__(
        self,
        sample_rate: int = 16000,
        wav_recorder: Optional[WavSessionRecorder] = None,
    ):
        super().__init__(daemon=True)
        self.sample_rate = sample_rate
        self.wav_recorder = wav_recorder
        self.is_active = False
        self._lock = Lock()

    def start(self):
        self.is_active = True
        super().start()

    def run(self):
        try:
            import soundcard as sc
        except ImportError:
            print(
                "[SystemAudioCapture] 'soundcard' not installed. "
                "Agent WAV will be empty. Install: pip install soundcard"
            )
            return

        try:
            default_speaker = sc.default_speaker()
            print(f"[SystemAudioCapture] Default speaker: {default_speaker.name}")

            loopback_mic = sc.get_microphone(
                id=str(default_speaker.name),
                include_loopback=True,
            )
            print(f"[SystemAudioCapture] Loopback device: {loopback_mic.name}")

            chunk_frames = int(self.sample_rate * 0.05)
            chunk_count = 0
            log_interval = 200

            with loopback_mic.recorder(samplerate=self.sample_rate, channels=1) as recorder:
                print("[SystemAudioCapture] Capturing agent audio (loopback)")

                while self.is_active:
                    try:
                        data = recorder.record(numframes=chunk_frames)
                        audio_float32 = data.flatten().astype(np.float32)
                        if self.wav_recorder is not None:
                            self.wav_recorder.append(audio_float32)
                        chunk_count += 1

                        if chunk_count == 1:
                            print(f"[SystemAudioCapture] First chunk ({len(audio_float32)} samples)")
                        if chunk_count % log_interval == 0:
                            rms = np.sqrt(np.mean(audio_float32 ** 2))
                            print(
                                f"[SystemAudioCapture] chunks={chunk_count}, "
                                f"rms={rms:.6f} {'(agent speaking)' if rms > 0.001 else '(silence)'}"
                            )

                    except Exception as e:
                        print(f"[SystemAudioCapture] Record error: {e}")
                        time.sleep(0.1)

        except Exception as e:
            print(f"[SystemAudioCapture] Init error: {e}")
            traceback.print_exc()

        print(f"[SystemAudioCapture] Stopped")

    def stop(self):
        with self._lock:
            self.is_active = False


# ---------------------------------------------------------------------------
# CSV loggers
# ---------------------------------------------------------------------------

class ActionTimelineLogger:
    """Frame-level CSV for VAP-style action names (shortNod, continuerBC, ...)."""

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
            idx = max(0, min(n_frames - 1, idx))
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


class ClassicTurnTimelineLogger(ActionTimelineLogger):
    """
    Frame-level CSV for classic turnManager.py.
    Same column layout as ActionTimelineLogger (shortNod, longNod, longNodTwice,
    continuerBC, assessmentBC, turnShift, actions) so CSVs from both pipelines
    are directly comparable. Filters out nothing/waiting.
    """

    VALID_ACTIONS = frozenset(ActionTimelineLogger.ACTION_COLUMNS)

    def add_event(self, timestamp_sec: float, action: str):
        if action in ("nothing", "waiting"):
            return
        if action not in self.VALID_ACTIONS:
            return
        with self._lock:
            self._events.append((max(0.0, float(timestamp_sec)), action))
