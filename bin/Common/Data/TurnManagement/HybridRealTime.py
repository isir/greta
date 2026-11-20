#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
HybridRealTime.py - File-Based Inference + Greta Agent + Audio Display

Hybrid pipeline combining:
  - File-based inputs (pre-recorded .wav + multimodal CSVs, like realtime_inference.py)
  - SAM-VAP model core with config overrides (from RealTimeGreta.py)
  - Dual output targets:
      1. Greta agent: BC / HN dispatched via TCP (same as RealTimeGreta.py)
      2. Audio display: stereo playback with waveform visualization
  - Turn-shift handling: mutes speaker 1 playback until next speech onset

Architecture:
    [Pre-loaded .wav + CSV files]
              |
              v
    [VAPModelWrapper]  ──>  [ActionDecider]
      (model core)          (thresholds + cooldowns)
                                  |
                    ┌─────────────┴──────────────┐
                    v                            v
            [GretaDispatcher]          [MutableAudioPlayback]
            (BC/HN via TCP)            (turn-shift -> mute spk1)
                                             |
                                    [WaveformVisualizer]
                                    (2 audio plots + cursor)

Usage:
    python HybridRealTime.py --audio1 spk1.wav --audio2 spk2.wav
    python HybridRealTime.py --audio1 spk1.wav --audio2 spk2.wav --no_greta
    (default checkpoint: sam_vap/checkpoints/ under TurnManagement/)
"""

import argparse
import torch
import torchaudio
import numpy as np
import time
import sys
import os
import threading
import traceback
import warnings
from pathlib import Path
from typing import Dict, Optional, List

warnings.filterwarnings("ignore")

# ============================================================================
# Path setup
# ============================================================================

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))

if SCRIPT_DIR not in sys.path:
    sys.path.insert(0, SCRIPT_DIR)

from RealTimeGreta import (
    VAPModelWrapper,
    ActionDecider,
    GretaDispatcher,
    GretaConfig,
    _resolve_path_under_turn_mgmt,
    DEFAULT_CHECKPOINT,
)

import sounddevice as sd


# ============================================================================
# Audio Loading
# ============================================================================

def load_audio(audio_path: str, target_sr: int) -> torch.Tensor:
    """Load audio file, convert to mono, resample to target sample rate."""
    audio, sr = torchaudio.load(audio_path)
    if audio.shape[0] > 1:
        audio = audio.mean(dim=0, keepdim=True)
    if sr != target_sr:
        resampler = torchaudio.transforms.Resample(sr, target_sr)
        audio = resampler(audio)
    return audio.squeeze(0)


PLAYBACK_SR = 44100


def load_audio_playback(audio_path: str) -> tuple:
    """Load audio at a high sample rate suitable for playback (44.1 kHz)."""
    audio, sr = torchaudio.load(audio_path)
    if audio.shape[0] > 1:
        audio = audio.mean(dim=0, keepdim=True)
    if sr != PLAYBACK_SR:
        resampler = torchaudio.transforms.Resample(sr, PLAYBACK_SR)
        audio = resampler(audio)
    return audio.squeeze(0).numpy(), PLAYBACK_SR


# ============================================================================
# Multimodal CSV Loading
# ============================================================================

def load_single_csv(csv_path: str, key_name: str, speaker_id: int,
                    expected_dim: int, target_frames: int) -> Optional[torch.Tensor]:
    """
    Load one multimodal CSV file, extract feature columns, pad/truncate to
    target_frames rows and expected_dim columns.
    """
    if not csv_path or not Path(csv_path).exists():
        return None

    import pandas as pd
    df = pd.read_csv(csv_path)
    cols = [c for c in df.columns if c not in ['frame', 'timestamp', 'success']]
    cols = cols[:expected_dim]
    features = torch.tensor(df[cols].values, dtype=torch.float32)

    # Pad columns if CSV has fewer than expected
    if features.shape[1] < expected_dim:
        col_pad = torch.zeros(features.shape[0], expected_dim - features.shape[1])
        features = torch.cat([features, col_pad], dim=1)

    # Align rows to target_frames
    if features.shape[0] > target_frames:
        features = features[:target_frames]
    elif features.shape[0] < target_frames:
        row_pad = torch.zeros(target_frames - features.shape[0], features.shape[1])
        features = torch.cat([features, row_pad], dim=0)

    print(f"  [Multimodal] {key_name}{speaker_id}: {Path(csv_path).name} -> {list(features.shape)}")
    return features


def load_all_multimodal(args, target_frames: int) -> Dict[str, torch.Tensor]:
    """
    Load all multimodal CSVs specified in args.
    Returns dict with keys like 'face1', 'gaze1', 'head1', 'body1', etc.
    """
    MODALITIES = {
        'face': 17,
        'gaze': 4,
        'head': 6,
        'body': 3,
    }

    data = {}
    for spk_id in [1, 2]:
        for mod_name, dim in MODALITIES.items():
            arg_name = f'{mod_name}{spk_id}'
            csv_path = getattr(args, arg_name, None)
            if csv_path:
                feat = load_single_csv(csv_path, mod_name, spk_id, dim, target_frames)
                if feat is not None:
                    data[f'{mod_name}{spk_id}'] = feat

        # Face images: expect .npy with shape [T, 3, 224, 224]
        face_im_path = getattr(args, f'face_im{spk_id}', None)
        if face_im_path and Path(face_im_path).exists():
            face_im = torch.tensor(np.load(face_im_path), dtype=torch.float32)
            if face_im.shape[0] > target_frames:
                face_im = face_im[:target_frames]
            elif face_im.shape[0] < target_frames:
                pad_shape = list(face_im.shape)
                pad_shape[0] = target_frames - face_im.shape[0]
                face_im = torch.cat([face_im, torch.zeros(pad_shape)], dim=0)
            data[f'face_im{spk_id}'] = face_im
            print(f"  [Multimodal] face_im{spk_id}: {Path(face_im_path).name} -> {list(face_im.shape)}")

    return data


# ============================================================================
# Mutable Audio Playback (sd.play pattern from realtime_inference.py + mute)
# ============================================================================

class MutableAudioPlayback(threading.Thread):
    """
    Stereo audio playback using sd.play(blocking=True), identical to the
    AudioPlaybackEngine in realtime_inference.py.

    Mute support: the stereo array is modified in-place ahead of the playback
    cursor.  set_mute(True) zeros speaker 1 from the current position onward;
    set_mute(False) restores the original samples from the current position
    onward.  Because sd.play reads from the array as it plays, changes are
    picked up immediately.
    """

    def __init__(self, audio1: np.ndarray, audio2: np.ndarray,
                 sample_rate: int, startup_delay: float = 0):
        super().__init__(daemon=True)
        self.sample_rate = sample_rate
        self.startup_delay = startup_delay
        self.running = False

        n = min(len(audio1), len(audio2))
        self.total_samples = n
        self.duration = n / sample_rate

        # Keep a pristine copy of speaker 1 for un-muting
        self.original_audio1 = audio1[:n].astype(np.float32).copy()

        # Build the stereo array that sd.play() will read from
        self.stereo_audio = np.stack(
            [audio1[:n].astype(np.float32),
             audio2[:n].astype(np.float32)], axis=1)  # [N, 2]

        self.mute_speaker1 = False
        self.mute_regions: List[tuple] = []
        self._current_mute_start: Optional[float] = None
        self._play_start_wall: Optional[float] = None

        print(f"[AudioPlayback] Initialized: {self.duration:.1f}s, "
              f"sr={sample_rate}, stereo (L=Spk1, R=Spk2)")

    def get_current_time(self) -> float:
        """Estimate current playback position from wall-clock time."""
        if self._play_start_wall is None:
            return 0.0
        return min(time.time() - self._play_start_wall, self.duration)

    def _current_sample(self) -> int:
        return min(int(self.get_current_time() * self.sample_rate),
                   self.total_samples)

    def set_mute(self, muted: bool):
        """Mute or unmute speaker 1 by rewriting the stereo array in-place."""
        if muted and not self.mute_speaker1:
            pos = self._current_sample()
            self.stereo_audio[pos:, 0] = 0.0
            self._current_mute_start = self.get_current_time()
            self.mute_speaker1 = True
            print(f"  [AudioPlayback] MUTING speaker 1 at t={self._current_mute_start:.2f}s")

        elif not muted and self.mute_speaker1:
            pos = self._current_sample()
            self.stereo_audio[pos:, 0] = self.original_audio1[pos:]
            end_t = self.get_current_time()
            if self._current_mute_start is not None:
                self.mute_regions.append((self._current_mute_start, end_t))
                print(f"  [AudioPlayback] UNMUTING speaker 1 at t={end_t:.2f}s "
                      f"(muted for {end_t - self._current_mute_start:.2f}s)")
            self._current_mute_start = None
            self.mute_speaker1 = False

    def run(self):
        self.running = True
        print(f"[AudioPlayback] Waiting {self.startup_delay}s before playback...")
        time.sleep(self.startup_delay)

        print(f"[AudioPlayback] Starting stereo playback (sd.play)...")
        self._play_start_wall = time.time()

        try:
            sd.play(self.stereo_audio, samplerate=self.sample_rate, blocking=True)

            # Finalize any open mute region
            if self.mute_speaker1 and self._current_mute_start is not None:
                self.mute_regions.append((self._current_mute_start, self.duration))

            print(f"[AudioPlayback] Playback completed ({len(self.mute_regions)} mute regions)")
        except Exception as e:
            print(f"[AudioPlayback] Error: {e}")
            traceback.print_exc()

    def stop(self):
        self.running = False
        sd.stop()


# ============================================================================
# Hybrid Inference Engine (file-based, wall-clock synced, dual output)
# ============================================================================

class HybridInferenceEngine(threading.Thread):
    """
    Iterates over pre-loaded audio + multimodal data stride by stride,
    synchronized with wall-clock time so that model processing advances
    at the same pace as audio playback.

    The model predicts actions for SPEAKER 1 (the expert) only.
    Speaker 2's data is used purely as model input context.

    BC/HN gating rule (listener-only):
        BC and HN actions are ONLY dispatched when speaker 1 is NOT speaking
        AND speaker 2 IS speaking.  This enforces that backchannels and head
        nods are listener behaviors.

    Mute logic for speaker 1:
        When a turn-shift is detected AND speaker 1 is currently speaking
        (RMS > threshold), speaker 1 is muted.
        Speaker 1 is unmuted when their RMS drops back below the threshold
        (i.e. their current utterance ends).

    On each stride the engine:
      1. Slices a context window from the pre-loaded audio + multimodal data
      2. Computes RMS for both speakers (VAD-like speaking flags)
      3. Prepares a batch and runs the SAM-VAP model forward pass
      4. ActionDecider produces actions from model output probabilities
      5. BC / HN actions  -> gated by listener condition, then dispatched to Greta
      6. Turn-shift action -> mutes speaker 1 if speaking; unmutes when silent
      7. Logs timing: model stride position vs audio playback position
    """

    def __init__(self,
                 model_wrapper: VAPModelWrapper,
                 config: GretaConfig,
                 audio1: torch.Tensor,
                 audio2: torch.Tensor,
                 multimodal_data: Optional[Dict[str, torch.Tensor]],
                 dispatcher: Optional[GretaDispatcher],
                 playback: MutableAudioPlayback,
                 unmute_energy_threshold: float = 0.01):
        super().__init__(daemon=True)
        self.model = model_wrapper
        self.config = config
        self.audio1 = audio1
        self.audio2 = audio2
        self.multimodal_data = multimodal_data or {}
        self.dispatcher = dispatcher
        self.playback = playback
        self.unmute_threshold = unmute_energy_threshold
        self.running = False

        self.action_decider = ActionDecider(config)
        self.sample_rate = model_wrapper.conf.sample_rate
        self.frame_hz = model_wrapper.conf.frame_hz
        self.audio_duration = len(audio1) / self.sample_rate

        stride_frames = int(config.stride_duration * self.frame_hz)
        model_wrapper.conf.inference_predict_last_n_frames = stride_frames

    def _greta_connected(self) -> bool:
        return self.dispatcher is not None and self.dispatcher.connected

    def _rms(self, audio: torch.Tensor, start: int, end: int) -> float:
        """Compute RMS energy of an audio segment."""
        end = min(end, len(audio))
        if end <= start:
            return 0.0
        seg = audio[start:end].numpy()
        return float(np.sqrt(np.mean(seg ** 2)))

    def run(self):
        self.running = True
        config = self.config
        model = self.model
        stride_frames = int(config.stride_duration * self.frame_hz)

        print(f"\n{'='*70}")
        print(f"  [HybridEngine] STARTING INFERENCE LOOP")
        print(f"{'='*70}")
        print(f"  Audio duration:    {self.audio_duration:.1f}s")
        print(f"  Context window:    {config.context_duration}s")
        print(f"  Stride:            {config.stride_duration}s ({stride_frames} frames)")
        print(f"  Shift threshold:   {config.vap_shift_threshold} (cd={config.shift_cooldown}s)")
        print(f"  BC threshold:      {config.bc_detection_threshold} (cd={config.bc_cooldown}s)")
        print(f"  HN threshold:      {config.hn_detection_threshold} (cd={config.hn_cooldown}s)")
        print(f"  VAD threshold:     {self.unmute_threshold} (spk speaking if RMS > thr)")
        print(f"  BC/HN gate:        only when spk1=silent AND spk2=speaking")
        print(f"  Mute rule:         turnShift + spk1 speaking -> mute; spk1 silent -> unmute")
        print(f"  Greta dispatch:    {'CONNECTED' if self._greta_connected() else 'DISABLED'}")
        print(f"  Multimodal keys:   {list(self.multimodal_data.keys()) or 'none'}")
        print(f"  Action target:     SPEAKER 1 only (expert)")

        print(f"\n  Waiting {config.context_duration}s for initial context...")
        time.sleep(config.context_duration)
        print(f"  Initial context ready. Starting inference.\n")

        if self._greta_connected():
            self.dispatcher.send_ready()

        current_time = config.context_duration
        inference_count = 0
        total_greta_actions = 0
        total_gated_bc_hn = 0
        total_mutes = 0
        wall_start = time.time()

        while self.running and current_time < self.audio_duration:
            inference_count += 1
            loop_t0 = time.perf_counter()

            try:
                context_start = max(0.0, current_time - config.context_duration)
                context_end = current_time

                # -- Playback vs model timing (drift should be near 0) --
                playback_t = self.playback.get_current_time()
                model_t = current_time
                drift = model_t - playback_t

                # ---- Slice audio (model sample rate) ----
                s0 = int(context_start * self.sample_rate)
                s1 = int(context_end * self.sample_rate)
                audio1_chunk = self.audio1[s0:s1]
                audio2_chunk = self.audio2[s0:s1]

                # RMS over the current stride window (end of context) for VAD
                stride_s0 = int((context_end - config.stride_duration) * self.sample_rate)
                stride_s1 = s1
                spk1_rms = self._rms(self.audio1, stride_s0, stride_s1)
                spk2_rms = self._rms(self.audio2, stride_s0, stride_s1)
                spk1_speaking = spk1_rms > self.unmute_threshold
                spk2_speaking = spk2_rms > self.unmute_threshold

                # ---- Slice multimodal features ----
                f0 = int(context_start * self.frame_hz)
                f1 = int(context_end * self.frame_hz)
                mm_context: Optional[Dict[str, torch.Tensor]] = None
                if self.multimodal_data:
                    mm_context = {k: v[f0:f1] for k, v in self.multimodal_data.items()}

                # ---- Header log ----
                mute_tag = "MUTED" if self.playback.mute_speaker1 else "NORMAL"
                print(f"\n--- Inference #{inference_count} | "
                      f"model_t={model_t:.1f}s | playback_t={playback_t:.1f}s | "
                      f"drift={drift:+.2f}s | {mute_tag} ---")
                print(f"  [Context] {context_start:.1f}s -> {context_end:.1f}s "
                      f"({config.context_duration}s window)")
                print(f"  [VAD]     spk1_rms={spk1_rms:.4f} "
                      f"{'SPEAKING' if spk1_speaking else 'SILENT'} | "
                      f"spk2_rms={spk2_rms:.4f} "
                      f"{'SPEAKING' if spk2_speaking else 'SILENT'}")

                # ---- Prepare batch ----
                batch = model.prepare_batch(
                    audio1_chunk, audio2_chunk,
                    multimodal_features=mm_context,
                    verbose=(inference_count <= 3)
                )

                # ---- Forward pass ----
                t_fwd = time.perf_counter()
                with torch.no_grad():
                    output = model.forward(batch)
                fwd_ms = (time.perf_counter() - t_fwd) * 1000

                # ---- Decide actions (model output -> raw action list) ----
                actions = self.action_decider.decide(
                    output, stride_frames, current_time, model, verbose=True
                )

                # ---- Listener gate for BC/HN ----
                # BC and HN are listener behaviors: only allowed when spk1
                # is NOT speaking and spk2 IS speaking.
                listener_ok = (not spk1_speaking) and spk2_speaking

                for action in actions:
                    if action == ActionDecider.ACTION_TURN_SHIFT:
                        # Mute speaker 1 only if they are currently speaking
                        if spk1_speaking and not self.playback.mute_speaker1:
                            self.playback.set_mute(True)
                            total_mutes += 1
                            print(f"  >>> TURN SHIFT at t={current_time:.1f}s "
                                  f"| spk1 SPEAKING -> MUTING spk1")
                        else:
                            print(f"  >>> TURN SHIFT at t={current_time:.1f}s "
                                  f"| spk1 {'already muted' if self.playback.mute_speaker1 else 'silent'} "
                                  f"-> no mute change")
                    elif action in (ActionDecider.ACTION_SHORT_NOD,
                                    ActionDecider.ACTION_LONG_NOD,
                                    ActionDecider.ACTION_LONG_NOD_TWICE,
                                    ActionDecider.ACTION_CONTINUER_BC,
                                    ActionDecider.ACTION_ASSESSMENT_BC):
                        if listener_ok:
                            if self._greta_connected():
                                self.dispatcher.send_action(action)
                                total_greta_actions += 1
                            print(f"  >>> GRETA: sent '{action}' (spk1 listener behavior)")
                        else:
                            total_gated_bc_hn += 1
                            print(f"  >>> GATED '{action}' -- spk1={'SPEAKING' if spk1_speaking else 'SILENT'} "
                                  f"spk2={'SPEAKING' if spk2_speaking else 'SILENT'} "
                                  f"(need: spk1=SILENT & spk2=SPEAKING)")

                # Heartbeat
                if not actions and self._greta_connected():
                    self.dispatcher.send_action("nothing")

                # ---- Unmute check: spk1 goes silent -> unmute ----
                if self.playback.mute_speaker1 and not spk1_speaking:
                    self.playback.set_mute(False)
                    print(f"  [Unmute] Spk1 RMS={spk1_rms:.6f} < {self.unmute_threshold} "
                          f"-> spk1 utterance ended, UNMUTED")

                # ---- Cleanup ----
                del output, batch
                if torch.cuda.is_available():
                    torch.cuda.empty_cache()

                # ---- Timing summary ----
                total_ms = (time.perf_counter() - loop_t0) * 1000
                rt_ratio = (config.stride_duration * 1000) / total_ms if total_ms > 0 else 999
                status_tag = "OK" if rt_ratio >= 1.0 else "SLOW"

                print(f"  [Perf] fwd={fwd_ms:.0f}ms total={total_ms:.0f}ms "
                      f"RT={rt_ratio:.1f}x [{status_tag}]  "
                      f"actions={actions or 'none'}")

            except Exception as e:
                print(f"  [HybridEngine] ERROR #{inference_count}: {e}")
                traceback.print_exc()

            # Advance model time
            current_time += config.stride_duration

            # Wall-clock sync: sleep to stay aligned with real-time playback
            target_wall = wall_start + (current_time - config.context_duration)
            sleep_s = target_wall - time.time()
            if sleep_s > 0:
                time.sleep(sleep_s)

        print(f"\n{'='*70}")
        print(f"  [HybridEngine] COMPLETED")
        print(f"  Inferences:       {inference_count}")
        print(f"  Greta actions:    {total_greta_actions}")
        print(f"  Gated BC/HN:      {total_gated_bc_hn} (blocked by listener gate)")
        print(f"  Mute events:      {total_mutes}")
        print(f"  Mute regions:     {len(self.playback.mute_regions)}")
        print(f"{'='*70}")

    def stop(self):
        self.running = False


# ============================================================================
# Main Entry Point
# ============================================================================

def main():
    parser = argparse.ArgumentParser(
        description='HybridRealTime - File-based VAP inference with Greta + Audio display')

    # ---- Required ----
    parser.add_argument('--audio1', type=str, required=True,
                        help='Path to speaker 1 audio (.wav)')
    parser.add_argument('--audio2', type=str, required=True,
                        help='Path to speaker 2 audio (.wav)')
    parser.add_argument('--checkpoint', type=str, default=DEFAULT_CHECKPOINT,
                        help='Path to VAP model checkpoint (.ckpt)')

    # ---- Multimodal CSV files (all optional) ----
    parser.add_argument('--face1', type=str, default=None,
                        help='Speaker 1 face AUs CSV')
    parser.add_argument('--gaze1', type=str, default=None,
                        help='Speaker 1 gaze CSV')
    parser.add_argument('--head1', type=str, default=None,
                        help='Speaker 1 head pose CSV (6D)')
    parser.add_argument('--body1', type=str, default=None,
                        help='Speaker 1 body pose CSV (3D)')
    parser.add_argument('--face_im1', type=str, default=None,
                        help='Speaker 1 face images (.npy array [T, 3, 224, 224])')
    parser.add_argument('--face2', type=str, default=None,
                        help='Speaker 2 face AUs CSV')
    parser.add_argument('--gaze2', type=str, default=None,
                        help='Speaker 2 gaze CSV')
    parser.add_argument('--head2', type=str, default=None,
                        help='Speaker 2 head pose CSV (6D)')
    parser.add_argument('--body2', type=str, default=None,
                        help='Speaker 2 body pose CSV (3D)')
    parser.add_argument('--face_im2', type=str, default=None,
                        help='Speaker 2 face images (.npy array [T, 3, 224, 224])')

    # ---- Model architecture overrides ----
    parser.add_argument('--predict_backchannels', action='store_true', default=True,
                        help='Enable BC prediction heads')
    parser.add_argument('--predict_hn', action='store_true', default=True,
                        help='Enable HN prediction heads')
    parser.add_argument('--multimodal', action='store_true', default=True,
                        help='Enable multimodal input')
    parser.add_argument('--use_face_encoder', action='store_true', default=True,
                        help='Enable face encoder')
    parser.add_argument('--use_hubert_semantics', action='store_true', default=True,
                        help='Enable HuBERT semantic encoder')
    parser.add_argument('--use_whisper_semantics', action='store_true', default=True,
                        help='Enable Whisper semantic encoder')
    parser.add_argument('--mode', type=int, default=3,
                        help='Model architecture mode')

    # ---- Pretrained paths ----
    parser.add_argument('--face_encoder_path', type=str, default=None)
    parser.add_argument('--pretrained_vap', type=str, default=None)
    parser.add_argument('--pretrained_cpc', type=str, default=None)
    parser.add_argument('--pretrained_hubert_model', type=str, default=None)
    parser.add_argument('--pretrained_whisper_model', type=str, default= "OpenAI/whisper-tiny")

    # ---- Inference parameters ----
    parser.add_argument('--context_duration', type=float, default=5.0,
                        help='Context window in seconds [default: 5.0]')
    parser.add_argument('--stride_duration', type=float, default=0.5,
                        help='Stride between inferences in seconds [default: 1.0]')
    parser.add_argument('--bc_threshold', type=float, default=0.4,
                        help='BC detection threshold [default: 0.4]')
    parser.add_argument('--hn_threshold', type=float, default=0.75,
                        help='HN detection threshold [default: 0.75]')
    parser.add_argument('--shift_threshold', type=float, default=0.5,
                        help='Turn-shift detection threshold [default: 0.5]')
    parser.add_argument('--bc_cooldown', type=float, default=3.0,
                        help='BC cooldown in seconds [default: 3.0]')
    parser.add_argument('--hn_cooldown', type=float, default=2.0,
                        help='HN cooldown in seconds [default: 2.0]')
    parser.add_argument('--shift_cooldown', type=float, default=5.0,
                        help='Turn-shift cooldown in seconds [default: 5.0]')

    # ---- Mute control ----
    parser.add_argument('--unmute_energy', type=float, default=0.001,
                        help='RMS threshold for mute state machine: spk2 silent + spk1 onset [default: 0.01]')

    # ---- Greta connection (optional) ----
    parser.add_argument('--no_greta', action='store_true',
                        help='Disable Greta TCP connection (audio-only mode)')
    parser.add_argument('--greta_host', type=str, default=None,
                        help='Greta Java backend host (default: auto-detect)')
    parser.add_argument('--feedback_port', type=int, default=5960,
                        help='Feedback port [default: 5960]')
    parser.add_argument('--greta_port', type=int, default=5961,
                        help='Greta command port [default: 5961]')

    # ---- Device ----
    parser.add_argument('--device', type=str, default='auto',
                        choices=['auto', 'cuda', 'cpu'],
                        help="Inference device [default: auto]")

    args = parser.parse_args()
    args.checkpoint = _resolve_path_under_turn_mgmt(args.checkpoint)
    args.audio1 = _resolve_path_under_turn_mgmt(args.audio1)
    args.audio2 = _resolve_path_under_turn_mgmt(args.audio2)
    for _path_arg in (
        "face1", "gaze1", "head1", "body1", "face_im1",
        "face2", "gaze2", "head2", "body2", "face_im2",
        "face_encoder_path", "pretrained_vap", "pretrained_cpc",
    ):
        _val = getattr(args, _path_arg, None)
        if _val is not None:
            setattr(args, _path_arg, _resolve_path_under_turn_mgmt(_val))

    # ==================================================================
    print("\n" + "=" * 70)
    print("  HybridRealTime - File + Greta + Audio Display Pipeline")
    print("=" * 70)

    # ---- Resolve device ----
    if args.device == 'auto':
        device = 'cuda' if torch.cuda.is_available() else 'cpu'
    elif args.device == 'cuda' and not torch.cuda.is_available():
        print("[Hybrid] CUDA not available, falling back to CPU")
        device = 'cpu'
    else:
        device = args.device
    print(f"[Hybrid] Device: {device}")

    # ---- Build config overrides ----
    config_overrides: Dict = {}
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

    # ---- 1) Load model ----
    print("\n[Hybrid] Loading VAP model...")
    model_wrapper = VAPModelWrapper(
        args.checkpoint, device=device, config_overrides=config_overrides
    )
    sr = model_wrapper.conf.sample_rate
    frame_hz = model_wrapper.conf.frame_hz

    mc = model_wrapper.conf
    print(f"\n[Hybrid] Active capabilities:")
    print(f"  VAP turn-taking:   always ON")
    print(f"  BC prediction:     {'ON' if getattr(mc, 'predict_backchannels', False) else 'OFF'}")
    print(f"  HN prediction:     {'ON' if getattr(mc, 'predict_head_nods', False) else 'OFF'}")
    print(f"  Face encoder:      {'ON' if getattr(mc, 'use_face_encoder', False) else 'OFF'}")
    print(f"  Multimodal:        {'ON' if getattr(mc, 'multimodal', False) else 'OFF'}")
    print(f"  HuBERT semantics:  {'ON' if getattr(mc, 'use_hubert_semantics', False) else 'OFF'}")
    print(f"  Whisper semantics: {'ON' if getattr(mc, 'use_whisper_semantics', False) else 'OFF'}")

    # ---- 2) Load audio ----
    print(f"\n[Hybrid] Loading audio files...")
    audio1 = load_audio(args.audio1, sr)
    audio2 = load_audio(args.audio2, sr)

    if len(audio1) != len(audio2):
        min_len = min(len(audio1), len(audio2))
        print(f"  Length mismatch: {len(audio1)/sr:.2f}s vs {len(audio2)/sr:.2f}s "
              f"-> truncating to {min_len/sr:.2f}s")
        audio1 = audio1[:min_len]
        audio2 = audio2[:min_len]

    duration = len(audio1) / sr
    print(f"  Duration: {duration:.1f}s | Sample rate: {sr} Hz")

    # ---- 3) Load multimodal CSVs ----
    total_frames = int(duration * frame_hz)
    multimodal_data = load_all_multimodal(args, total_frames)
    if multimodal_data:
        print(f"  Loaded modalities: {list(multimodal_data.keys())}")
    else:
        print(f"  No multimodal data provided (zeros will be used)")

    # ---- 4) Build GretaConfig ----
    config = GretaConfig(
        context_duration=args.context_duration,
        stride_duration=args.stride_duration,
        bc_detection_threshold=args.bc_threshold,
        hn_detection_threshold=args.hn_threshold,
        vap_shift_threshold=args.shift_threshold,
        bc_cooldown=args.bc_cooldown,
        hn_cooldown=args.hn_cooldown,
        shift_cooldown=args.shift_cooldown,
    )
    config.sample_rate = sr
    config.frame_hz = frame_hz

    # ---- 5) Optionally connect to Greta ----
    dispatcher: Optional[GretaDispatcher] = None
    if not args.no_greta:
        import socket as sock_module
        greta_host = args.greta_host or sock_module.gethostname()
        print(f"\n[Hybrid] Connecting to Greta at {greta_host}:{args.greta_port}...")
        try:
            dispatcher = GretaDispatcher(host=greta_host, port=args.greta_port)
            dispatcher.connect(feedback_host=greta_host, feedback_port=args.feedback_port)
            print(f"  Connected to Greta!")
        except Exception as e:
            print(f"  WARNING: Could not connect to Greta: {e}")
            print(f"  Continuing in audio-only mode.")
            dispatcher = None
    else:
        print(f"\n[Hybrid] Greta dispatch disabled (--no_greta flag)")

    # ---- 6) Create audio playback at 44.1 kHz (original quality) ----
    print(f"\n[Hybrid] Loading audio at {PLAYBACK_SR} Hz for playback...")
    audio1_play, play_sr = load_audio_playback(args.audio1)
    audio2_play, _       = load_audio_playback(args.audio2)

    if len(audio1_play) != len(audio2_play):
        pmin = min(len(audio1_play), len(audio2_play))
        audio1_play = audio1_play[:pmin]
        audio2_play = audio2_play[:pmin]

    playback = MutableAudioPlayback(
        audio1=audio1_play,
        audio2=audio2_play,
        sample_rate=play_sr,
        startup_delay=0.0
    )

    # ---- 7) Create inference engine ----
    engine = HybridInferenceEngine(
        model_wrapper=model_wrapper,
        config=config,
        audio1=audio1,
        audio2=audio2,
        multimodal_data=multimodal_data if multimodal_data else None,
        dispatcher=dispatcher,
        playback=playback,
        unmute_energy_threshold=args.unmute_energy
    )

    # ---- 8) Launch ----
    print(f"\n[Hybrid] Launching pipeline (context_delay={args.context_duration}s)...")
    print(f"  Audio playback: stereo at {play_sr} Hz")
    print(f"  Model inference: {sr} Hz")
    print(f"  Press Ctrl+C to stop.\n")
    engine.start()
    playback.start()

    # Main thread waits for playback to finish (or Ctrl+C)
    try:
        playback.join()
    except KeyboardInterrupt:
        print("\n[Hybrid] Interrupted by user")

    # ---- Cleanup ----
    engine.stop()
    playback.stop()
    if dispatcher:
        dispatcher.close()
    engine.join(timeout=3.0)

    print(f"\n{'='*70}")
    print(f"  HybridRealTime completed!")
    if playback.mute_regions:
        print(f"  Mute regions ({len(playback.mute_regions)}):")
        for i, (s, e) in enumerate(playback.mute_regions):
            print(f"    [{i+1}] {s:.2f}s -> {e:.2f}s  ({e-s:.2f}s)")
    print(f"{'='*70}\n")


if __name__ == '__main__':
    main()
