#!/usr/bin/env python3
"""
Real-Time VAP Inference and Visualization System

Live-compatible architecture with just-in-time prediction:
- 5s startup delay (build initial context)
- 1s stride (new inference every second)
- 10Hz visualization (matches model training)
- Minimal buffering (predictions ready ~100ms before needed)

Usage:
    python realtime_inference.py \
        --audio1 speaker1.wav \
        --audio2 speaker2.wav \
        --checkpoint model.ckpt \
        --stride_duration 1.0 \
        --playback_speed 1.0
"""

import argparse
import torch
import torchaudio
import numpy as np
import matplotlib.pyplot as plt
import matplotlib.animation as animation
from pathlib import Path
from typing import Dict, Tuple, Optional, List
import warnings
import time
import threading
import sounddevice as sd
import cv2
from queue import Queue
from dataclasses import dataclass, field
from collections import deque
import sys

warnings.filterwarnings("ignore")

# Import VAP components
from model import VapGPT, VapConfig
from events import TurnTakingEvents
import torch.nn.functional as F

# Import config classes for checkpoint compatibility
from finetune import OptConfig, DataConfig
from events import EventConfig

# Add config classes to module namespace for pickle compatibility
current_module = sys.modules[__name__]
current_module.VapConfig = VapConfig
current_module.OptConfig = OptConfig
current_module.DataConfig = DataConfig
current_module.EventConfig = EventConfig


# ============================================================================
# Data Structures
# ============================================================================

@dataclass
class PredictionFrame:
    """Single frame of predictions"""
    time_start: float  # Start time in seconds
    time_end: float    # End time in seconds
    
    # VAP predictions
    vap_p_now: Optional[np.ndarray] = None      # [2] - P_now for both speakers
    vap_p_future: Optional[np.ndarray] = None   # [2] - P_future for both speakers
    
    # BC predictions (cascade)
    bc_detection_probs: Optional[np.ndarray] = None  # [2] - Any BC probability
    bc_type_probs: Optional[np.ndarray] = None       # [2, 3] - [reactive, emotional, no_bc]
    bc_predicted_class: Optional[np.ndarray] = None  # [2] - Final predicted class
    
    # HN predictions (cascade)
    hn_detection_probs: Optional[np.ndarray] = None  # [2] - Any HN probability
    hn_type_probs: Optional[np.ndarray] = None       # [2, 4] - [short, long, long_p, no_nod]
    hn_predicted_class: Optional[np.ndarray] = None  # [2] - Final predicted class


class PredictionBuffer:
    """
    Thread-safe buffer for predictions
    Stores predictions indexed by time with minimal memory footprint
    """
    
    def __init__(self, max_buffer_duration: float = 3.0, frame_hz: float = 10.0):
        """
        Args:
            max_buffer_duration: Maximum buffer size in seconds
            frame_hz: Frame rate (Hz)
        """
        self.max_frames = int(max_buffer_duration * frame_hz)
        self.frame_hz = frame_hz
        self.buffer = deque(maxlen=self.max_frames)
        self.lock = threading.Lock()
        
    def push(self, predictions: List[PredictionFrame]):
        """Add new predictions to buffer (thread-safe)"""
        with self.lock:
            self.buffer.extend(predictions)
            
    def get_range(self, start_time: float, end_time: float) -> List[PredictionFrame]:
        """
        Get predictions for a time range
        
        Args:
            start_time: Start time in seconds
            end_time: End time in seconds
            
        Returns:
            List of PredictionFrame objects in the requested range
        """
        with self.lock:
            result = []
            for pred in self.buffer:
                # Check if prediction overlaps with requested range
                if pred.time_start < end_time and pred.time_end > start_time:
                    result.append(pred)
            return result
    
    def get_latest_time(self) -> float:
        """Get the end time of the latest prediction"""
        with self.lock:
            if len(self.buffer) == 0:
                return 0.0
            return self.buffer[-1].time_end
    
    def clear(self):
        """Clear all predictions"""
        with self.lock:
            self.buffer.clear()


# ============================================================================
# Model Wrapper (from offline_inference.py)
# ============================================================================

class VAPModelWrapper:
    """
    Wrapper for VAP model loading and inference
    Reuses logic from offline_inference.py
    """
    
    def __init__(self, checkpoint_path: str, device: str = "cuda"):
        """
        Initialize VAP model
        
        Args:
            checkpoint_path: Path to model checkpoint
            device: Device to run on ('cuda' or 'cpu')
        """
        self.device = device
        self.checkpoint_path = checkpoint_path
        
        # Load model
        self.model, self.conf = self._load_model()
        
        # Initialize event processor
        self.event_processor = TurnTakingEvents()
        
        print(f"✅ Loaded VAP model from {checkpoint_path}")
        print(f"📊 Model configuration:")
        print(f"   - Sample rate: {self.conf.sample_rate}")
        print(f"   - Frame rate: {self.conf.frame_hz}")
        print(f"   - Context limit: {getattr(self.conf, 'context_limit', -1)} frames")
        print(f"   - BC detection head: {self.conf.bc_detection_head}")
        print(f"   - HN detection head: {self.conf.hn_detection_head}")
        
    def _load_model(self) -> Tuple[VapGPT, VapConfig]:
        """Load VAP model from checkpoint"""
        
        # Load checkpoint using safe loader
        from safe_checkpoint_loader import load_vap_checkpoint_safe
        checkpoint = load_vap_checkpoint_safe(self.checkpoint_path, map_location="cpu")
        
        if checkpoint is None:
            raise RuntimeError(f"Failed to load checkpoint from {self.checkpoint_path}")
        
        # Extract configuration
        if 'hyper_parameters' in checkpoint:
            conf_dict = checkpoint['hyper_parameters']['conf']
            if isinstance(conf_dict, dict):
                conf = VapConfig(**conf_dict)
            else:
                conf = conf_dict
        elif 'conf' in checkpoint:
            conf_dict = checkpoint['conf']
            if isinstance(conf_dict, dict):
                conf = VapConfig(**conf_dict)
            else:
                conf = conf_dict
        else:
            print("⚠️  Warning: No config found in checkpoint, using default VapConfig")
            conf = VapConfig()
        
        # Initialize model with CPU-safe loading
        from cpu_safe_encoder_patch import CPUSafeContext
        
        with CPUSafeContext():
            model = VapGPT(conf)
        
        # Load state dict
        if 'state_dict' in checkpoint:
            state_dict = checkpoint['state_dict']
            state_dict = {k.replace('model.', ''): v for k, v in state_dict.items()}
        else:
            state_dict = checkpoint
            
        # Ensure all tensors are on CPU before loading
        cpu_state_dict = {}
        for k, v in state_dict.items():
            if isinstance(v, torch.Tensor):
                cpu_state_dict[k] = v.cpu()
            else:
                cpu_state_dict[k] = v
        
        model.load_state_dict(cpu_state_dict, strict=False)
        model.eval()
        model.to(self.device)
        model.device = torch.device(self.device)
        
        # Enable selective prediction for inference optimization
        conf.inference_mode = True
        
        return model, conf
    
    def load_multimodal_data(self, face_path: str, gaze_path: str, body_path: str, 
                            audio_duration_frames: int) -> Dict[str, torch.Tensor]:
        """
        Load multimodal CSV data and align with audio duration
        
        Args:
            face_path: Path to face CSV file
            gaze_path: Path to gaze CSV file  
            body_path: Path to body CSV file
            audio_duration_frames: Expected number of frames to match audio
            
        Returns:
            Dictionary containing aligned multimodal tensors
        """
        import pandas as pd
        
        multimodal_data = {}
        
        # Load face data
        if face_path and Path(face_path).exists():
            face_df = pd.read_csv(face_path)
            # Extract face features (skip timestamp columns)
            face_cols = [col for col in face_df.columns if col not in ['frame', 'timestamp', 'success']]
            face_features = torch.tensor(face_df[face_cols].values, dtype=torch.float32)
            multimodal_data['face'] = self._align_to_audio_duration(face_features, audio_duration_frames)
        
        # Load gaze data  
        if gaze_path and Path(gaze_path).exists():
            gaze_df = pd.read_csv(gaze_path)
            gaze_cols = [col for col in gaze_df.columns if col not in ['frame', 'timestamp', 'success']]
            gaze_features = torch.tensor(gaze_df[gaze_cols].values, dtype=torch.float32)
            multimodal_data['gaze'] = self._align_to_audio_duration(gaze_features, audio_duration_frames)
        
        # Load body/head pose data
        if body_path and Path(body_path).exists():
            body_df = pd.read_csv(body_path)
            # For head pose, we expect pitch, yaw, roll (3 dimensions)
            body_cols = [col for col in body_df.columns if col not in ['frame', 'timestamp', 'success']][:3]
            body_features = torch.tensor(body_df[body_cols].values, dtype=torch.float32)
            multimodal_data['body'] = self._align_to_audio_duration(body_features, audio_duration_frames)
        
        return multimodal_data
    
    def _align_to_audio_duration(self, features: torch.Tensor, target_frames: int) -> torch.Tensor:
        """
        Align multimodal features to match audio duration
        
        Args:
            features: Input feature tensor [T, D]
            target_frames: Target number of frames
            
        Returns:
            Aligned tensor [target_frames, D]
        """
        current_frames = features.shape[0]
        
        if current_frames == target_frames:
            return features
        elif current_frames > target_frames:
            # Truncate
            return features[:target_frames]
        else:
            # Pad with zeros
            padding = torch.zeros(target_frames - current_frames, features.shape[1])
            return torch.cat([features, padding], dim=0)
    
    def prepare_batch(self, audio1: torch.Tensor, audio2: torch.Tensor,
                     multimodal_paths: Dict[str, Dict[str, str]] = None) -> Dict:
        """
        Prepare batch for model inference
        
        Args:
            audio1: Speaker 1 audio [T]
            audio2: Speaker 2 audio [T]
            multimodal_paths: Dictionary with paths to multimodal CSV files
            
        Returns:
            Batch dictionary
        """
        # Ensure same length
        min_length = min(len(audio1), len(audio2))
        audio1 = audio1[:min_length]
        audio2 = audio2[:min_length]
        
        # Stack into stereo format [2, T] and add batch dimension [1, 2, T]
        waveform = torch.stack([audio1, audio2], dim=0).unsqueeze(0).to(self.device)
        
        # Calculate expected frames for alignment
        audio_duration_sec = waveform.shape[-1] / self.conf.sample_rate
        expected_frames = int(audio_duration_sec * self.conf.frame_hz)
        
        # Create batch
        batch = {'waveform': waveform}
        
        # Add multimodal data if model supports it
        if self.conf.multimodal:
            # Define expected multimodal keys and their dimensions
            multimodal_dims = {
                'gaze': 4,    # gaze features
                'head': 6,    # head pose features  
                'face': 17,   # facial action units
                'body': 3     # body pose (now head pose: pitch, yaw, roll)
            }
            
            # Load multimodal data for both speakers
            for speaker_id in [1, 2]:
                speaker_paths = multimodal_paths.get(f'speaker{speaker_id}', {}) if multimodal_paths else {}
                
                if speaker_paths:
                    # Load actual multimodal data
                    multimodal_data = self.load_multimodal_data(
                        speaker_paths.get('face'),
                        speaker_paths.get('gaze'), 
                        speaker_paths.get('body'),
                        expected_frames
                    )
                    
                    # Add to batch with proper naming
                    for modality, data in multimodal_data.items():
                        batch[f'{modality}{speaker_id}'] = data.unsqueeze(0).to(self.device)
                    
                    # Add dummy data for missing modalities
                    for modality, dim in multimodal_dims.items():
                        key = f'{modality}{speaker_id}'
                        if key not in batch:
                            batch[key] = torch.zeros(1, expected_frames, dim, device=self.device)
                else:
                    # Add dummy data for all modalities when no paths provided
                    for modality, dim in multimodal_dims.items():
                        batch[f'{modality}{speaker_id}'] = torch.zeros(1, expected_frames, dim, device=self.device)
                
                # Add face image data if using face encoder
                if getattr(self.conf, 'use_face_encoder', False):
                    # Add dummy face image data [1, T, C, H, W] - typical face image dimensions
                    batch[f'face_im{speaker_id}'] = torch.zeros(1, expected_frames, 3, 224, 224, device=self.device)
        
        # Add dummy semantic features if using semantics
        if getattr(self.conf, 'use_whisper_semantics', False) or getattr(self.conf, 'use_hubert_semantics', False):
            # Create dummy semantic features [1, T, D]
            semantic_dim = 1024  # Whisper feature dimension
            batch['whisper_semantics'] = torch.zeros(
                1, expected_frames, semantic_dim, 
                device=self.device
            )
        
        return batch
    
    @torch.no_grad()
    def forward(self, batch: Dict) -> Dict:
        """
        Run model forward pass
        
        Args:
            batch: Input batch dictionary
            
        Returns:
            Model outputs dictionary
        """
        return self.model(batch)


# ============================================================================
# Inference Engine (Background Thread)
# ============================================================================

class InferenceEngine(threading.Thread):
    """
    Background thread for continuous inference
    Runs inference every stride_duration seconds
    """
    
    def __init__(self, 
                 model_wrapper: VAPModelWrapper,
                 audio1: torch.Tensor,
                 audio2: torch.Tensor,
                 prediction_buffer: PredictionBuffer,
                 multimodal_paths: Optional[Dict[str, Dict[str, str]]] = None,
                 context_duration: float = 5.0,
                 stride_duration: float = 1.0,
                 bc_detection_threshold: float = 0.5,
                 hn_detection_threshold: float = 0.5,
                 bc_shift: Optional[float] = None,
                 hn_shift: Optional[float] = None):
        """
        Args:
            model_wrapper: VAP model wrapper
            audio1: Full audio for speaker 1
            audio2: Full audio for speaker 2
            prediction_buffer: Shared prediction buffer
            multimodal_paths: Dictionary with multimodal file paths for each speaker
            context_duration: Context window size (seconds)
            stride_duration: Time between inferences (seconds)
            bc_detection_threshold: Threshold for BC detection
            hn_detection_threshold: Threshold for HN detection
            bc_shift: BC prediction shift in seconds (None = use model config)
            hn_shift: HN prediction shift in seconds (None = use model config)
        """
        super().__init__(daemon=True)
        
        self.model_wrapper = model_wrapper
        self.audio1 = audio1
        self.audio2 = audio2
        self.prediction_buffer = prediction_buffer
        self.multimodal_paths = multimodal_paths
        self.context_duration = context_duration
        self.stride_duration = stride_duration
        self.bc_threshold = bc_detection_threshold
        self.hn_threshold = hn_detection_threshold
        
        self.sample_rate = model_wrapper.conf.sample_rate
        self.frame_hz = model_wrapper.conf.frame_hz
        
        # Get prediction shifts (from args or model config)
        if bc_shift is not None:
            self.bc_shift = bc_shift
        else:
            self.bc_shift = getattr(model_wrapper.conf, 'backchannel_prediction_shift_ms', 500) / 1000.0
        
        if hn_shift is not None:
            self.hn_shift = hn_shift
        else:
            self.hn_shift = getattr(model_wrapper.conf, 'head_nod_prediction_shift_ms', 500) / 1000.0
        
        # Calculate selective frames for inference optimization
        stride_frames = int(stride_duration * self.frame_hz)
        self.model_wrapper.conf.inference_predict_last_n_frames = stride_frames
        
        # Control flags
        self.running = False
        self.current_time = 0.0  # Current inference position
        self.audio_duration = len(audio1) / self.sample_rate
        
        # Performance tracking
        self.inference_times = []
        self.late_inferences = 0  # Count of inferences that exceeded stride duration
        
    def run(self):
        """Main inference loop (runs in background thread)"""
        self.running = True
        
        print(f"\n🎯 Inference Engine Started")
        print(f"   Context: {self.context_duration}s")
        print(f"   Stride: {self.stride_duration}s")
        print(f"   Selective frames: {self.model_wrapper.conf.inference_predict_last_n_frames}")
        print(f"   BC shift: {self.bc_shift}s ({self.bc_shift*1000:.0f}ms)")
        print(f"   HN shift: {self.hn_shift}s ({self.hn_shift*1000:.0f}ms)")
        
        # Start from context_duration (we have enough context now)
        self.current_time = self.context_duration
        inference_count = 0
        
        while self.running and self.current_time < self.audio_duration:
            inference_count += 1
            start_time = time.time()
            
            # Extract context window [current_time - context_duration : current_time]
            context_start = max(0.0, self.current_time - self.context_duration)
            context_end = self.current_time
            
            # Run inference
            try:
                predictions = self._run_inference(context_start, context_end)
                self.prediction_buffer.push(predictions)
                
                inference_duration = time.time() - start_time
                self.inference_times.append(inference_duration)
                
                # Check if inference exceeded stride duration (real-time capability)
                if inference_duration > self.stride_duration:
                    self.late_inferences += 1
                    lag_ms = (inference_duration - self.stride_duration) * 1000
                    print(f"⚠️  WARNING: Inference #{inference_count} took {inference_duration*1000:.0f}ms, "
                          f"exceeds stride {self.stride_duration*1000:.0f}ms!")
                    print(f"   System is falling behind real-time by {lag_ms:.0f}ms per stride.")
                    print(f"   In live scenario: Predictions would be delayed.")
                
                # Log performance and memory usage
                if inference_count % 5 == 0:  # Log every 5 inferences
                    avg_time = np.mean(self.inference_times[-5:])
                    realtime_ratio = self.stride_duration / avg_time if avg_time > 0 else float('inf')
                    status_emoji = "✅" if realtime_ratio >= 1.0 else "❌"
                    
                    mem_info = ""
                    if torch.cuda.is_available():
                        mem_allocated = torch.cuda.memory_allocated() / 1024**2  # MB
                        mem_reserved = torch.cuda.memory_reserved() / 1024**2  # MB
                        mem_info = f" | GPU: {mem_allocated:.0f}MB / {mem_reserved:.0f}MB"
                    
                    print(f"{status_emoji} Inference #{inference_count}: {avg_time*1000:.1f}ms "
                          f"(RT ratio: {realtime_ratio:.2f}x) | Position: {self.current_time:.1f}s / {self.audio_duration:.1f}s{mem_info}")
                
                # Periodic garbage collection to prevent memory leaks
                if inference_count % 50 == 0:
                    import gc
                    gc.collect()
                    if torch.cuda.is_available():
                        torch.cuda.empty_cache()
                
            except Exception as e:
                print(f"❌ Inference error at t={self.current_time:.1f}s: {e}")
                import traceback
                traceback.print_exc()  # Print full traceback for debugging
            
            # Move to next stride position
            self.current_time += self.stride_duration
            
            # Sleep if needed to match stride timing (for real-time simulation)
            # In true live scenario, this would naturally wait for new audio data
            elapsed = time.time() - start_time
            sleep_time = max(0, self.stride_duration - elapsed)
            if sleep_time > 0:
                time.sleep(sleep_time)
        
        print(f"\n✅ Inference Engine Completed")
        print(f"   Total inferences: {inference_count}")
        if len(self.inference_times) > 0:
            avg_inference = np.mean(self.inference_times)
            min_inference = np.min(self.inference_times)
            max_inference = np.max(self.inference_times)
            realtime_ratio = self.stride_duration / avg_inference if avg_inference > 0 else float('inf')
            
            print(f"   Avg inference time: {avg_inference*1000:.1f}ms (min: {min_inference*1000:.1f}ms, max: {max_inference*1000:.1f}ms)")
            print(f"   Real-time ratio: {realtime_ratio:.2f}x (1.0x = exactly real-time)")
            
            if realtime_ratio >= 1.0:
                print(f"   ✅ System is REAL-TIME capable (inference < {self.stride_duration*1000:.0f}ms stride)")
            else:
                print(f"   ❌ System is NOT real-time (inference > {self.stride_duration*1000:.0f}ms stride)")
                print(f"   Late inferences: {self.late_inferences}/{inference_count} ({100*self.late_inferences/inference_count:.1f}%)")
                speedup_needed = 1.0 / realtime_ratio
                print(f"   Would need {speedup_needed:.2f}x faster hardware/model for live use")
        
    def stop(self):
        """Stop the inference engine"""
        self.running = False
    
    def _run_inference(self, context_start: float, context_end: float) -> List[PredictionFrame]:
        """
        Run inference on a context window
        
        Args:
            context_start: Start time of context (seconds)
            context_end: End time of context (seconds)
            
        Returns:
            List of PredictionFrame objects for the stride region
        """
        # Extract audio chunks
        start_sample = int(context_start * self.sample_rate)
        end_sample = int(context_end * self.sample_rate)
        
        audio1_chunk = self.audio1[start_sample:end_sample]
        audio2_chunk = self.audio2[start_sample:end_sample]
        
        # Prepare batch (with multimodal data if available)
        batch = self.model_wrapper.prepare_batch(audio1_chunk, audio2_chunk, 
                                                  multimodal_paths=self.multimodal_paths)
        
        # Forward pass
        with torch.no_grad():
            output = self.model_wrapper.forward(batch)
            
            # Immediately move all outputs to CPU and convert to numpy to free GPU memory
            # This prevents GPU memory accumulation over long sessions
            stride_frames = int(self.stride_duration * self.frame_hz)
        
        # VAP predictions - extract from logits using model's objective
        logits = output['logits']  # [B, T, n_classes]
        vap_probs = self.model_wrapper.model.objective.get_probs(logits)
        
        # Extract p_now and p_future for the stride region
        p_now = vap_probs['p_now'][0, -stride_frames:, :].cpu().numpy()  # [stride_frames, 4] (VAP classes)
        p_future = vap_probs['p_future'][0, -stride_frames:, :].cpu().numpy()  # [stride_frames, 4]
        
        # For visualization, we'll show shift probability for both speakers
        # VAP outputs 4 classes: [hold, shift, backchannel, silence]
        # We'll use shift probability (index 1) as the main turn-taking signal
        p_now_shift = p_now[:, 1:2]  # [stride_frames, 1] - shift probability
        p_future_shift = p_future[:, 1:2]  # [stride_frames, 1]
        
        # Broadcast to both speakers for visualization
        p_now = np.tile(p_now_shift, (1, 2))  # [stride_frames, 2]
        p_future = np.tile(p_future_shift, (1, 2))  # [stride_frames, 2]
        
        # BC predictions (cascade) - Model outputs [B, T, n_classes], predicting for both speakers
        # Detection: [B, T, 2] where classes are [no_bc, any_bc]
        bc_detection_logits = output['bc_detection_logits'][0, -stride_frames:, :]  # [stride_frames, 2]
        bc_detection_probs = torch.softmax(bc_detection_logits, dim=-1).cpu().numpy()  # [stride_frames, 2]
        
        # Type: [B, T, 2] where classes are [reactive, emotional]  
        bc_type_logits = output['bc_type_logits'][0, -stride_frames:, :]  # [stride_frames, 2]
        bc_type_probs = torch.softmax(bc_type_logits, dim=-1).cpu().numpy()  # [stride_frames, 2]
        
        # Apply cascade logic: P(any_bc) from detection head
        prob_any_bc = bc_detection_probs[:, 1]  # [stride_frames] - probability of any_bc
        has_bc = prob_any_bc >= self.bc_threshold  # Boolean mask
        
        # Classify type only where BC detected
        bc_predicted_class = np.full(stride_frames, -1, dtype=np.int32)
        if has_bc.any():
            type_preds = np.argmax(bc_type_probs, axis=-1)  # [stride_frames]
            bc_predicted_class[has_bc] = type_preds[has_bc]
        
        # For visualization: need [stride_frames, 2] shape (2 speakers)
        # Duplicate for both speakers (model predicts same for both in current architecture)
        bc_detection_probs_viz = np.tile(bc_detection_probs[:, 1:2], (1, 2))  # [stride_frames, 2]
        
        # Apply cascade: P(type | any_bc) * P(any_bc) = P(type AND any_bc)
        # This gives the joint probability of each type
        prob_any_bc_expand = bc_detection_probs[:, 1:2]  # [stride_frames, 1]
        bc_type_probs_joint = bc_type_probs * prob_any_bc_expand  # [stride_frames, 2]
        
        # Now create visualization array with all 3 classes
        bc_type_probs_with_no = np.zeros((stride_frames, 2, 3))  # [stride_frames, 2_speakers, 3_classes]
        # Duplicate for both speakers (model predicts same for both)
        bc_type_probs_with_no[:, 0, 0] = bc_type_probs_joint[:, 0]  # P(reactive AND any_bc)
        bc_type_probs_with_no[:, 0, 1] = bc_type_probs_joint[:, 1]  # P(emotional AND any_bc)
        bc_type_probs_with_no[:, 0, 2] = bc_detection_probs[:, 0]   # P(no_bc)
        bc_type_probs_with_no[:, 1, :] = bc_type_probs_with_no[:, 0, :]  # Copy to speaker 2
        
        # HN predictions (cascade) - Similar structure
        # Detection: [B, T, 2] where classes are [no_nod, any_nod]
        hn_detection_logits = output['hn_detection_logits'][0, -stride_frames:, :]  # [stride_frames, 2]
        hn_detection_probs = torch.softmax(hn_detection_logits, dim=-1).cpu().numpy()  # [stride_frames, 2]
        
        # Type: [B, T, 3] where classes are [short, long, long_p]
        hn_type_logits = output['hn_type_logits'][0, -stride_frames:, :]  # [stride_frames, 3]
        hn_type_probs = torch.softmax(hn_type_logits, dim=-1).cpu().numpy()  # [stride_frames, 3]
        
        # Apply cascade logic
        prob_any_hn = hn_detection_probs[:, 1]  # [stride_frames]
        has_hn = prob_any_hn >= self.hn_threshold
        
        hn_predicted_class = np.full(stride_frames, -1, dtype=np.int32)
        if has_hn.any():
            type_preds = np.argmax(hn_type_probs, axis=-1)  # [stride_frames]
            hn_predicted_class[has_hn] = type_preds[has_hn]
        
        # For visualization: duplicate for both speakers
        hn_detection_probs_viz = np.tile(hn_detection_probs[:, 1:2], (1, 2))  # [stride_frames, 2]
        
        # Apply cascade: P(type | any_nod) * P(any_nod) = P(type AND any_nod)
        # This gives the joint probability of each type
        prob_any_hn_expand = hn_detection_probs[:, 1:2]  # [stride_frames, 1]
        hn_type_probs_joint = hn_type_probs * prob_any_hn_expand  # [stride_frames, 3]
        
        # Now create visualization array with all 4 classes
        hn_type_probs_with_no = np.zeros((stride_frames, 2, 4))  # [stride_frames, 2_speakers, 4_classes]
        # Duplicate for both speakers (model predicts same for both)
        hn_type_probs_with_no[:, 0, 0] = hn_type_probs_joint[:, 0]  # P(short AND any_nod)
        hn_type_probs_with_no[:, 0, 1] = hn_type_probs_joint[:, 1]  # P(long AND any_nod)
        hn_type_probs_with_no[:, 0, 2] = hn_type_probs_joint[:, 2]  # P(long_p AND any_nod)
        hn_type_probs_with_no[:, 0, 3] = hn_detection_probs[:, 0]   # P(no_nod)
        hn_type_probs_with_no[:, 1, :] = hn_type_probs_with_no[:, 0, :]  # Copy to speaker 2
        
        # Create PredictionFrame objects with CORRECT time alignment
        # 
        # Understanding:
        # - Selective prediction: last N frames of context
        # - Context: [context_start, context_end]
        # - Last N frames represent: [context_end - stride_duration, context_end]
        # - With shift S, frame at time T predicts event at time T+S
        # 
        # Example: context_end=5s, stride=1s, bc_shift=0.5s
        # - Last 1s of context: [4s, 5s]
        # - Frame at 4.0s predicts BC at 4.0+0.5=4.5s
        # - Frame at 5.0s predicts BC at 5.0+0.5=5.5s
        # - Predictions span: [4.5s, 5.5s]
        #
        # BUT: For real-time streaming with stride=1s, we want predictions
        # to cover [context_end, context_end+stride] to avoid gaps
        # This means we need shift = stride for continuous coverage
        # 
        # With your configuration (stride=1s, shift=2s):
        # - Frame at 4.0s predicts at 4.0+2.0=6.0s
        # - Frame at 5.0s predicts at 5.0+2.0=7.0s
        # - Predictions span: [6.0s, 7.0s] ✅
        
        predictions = []
        frame_duration = 1.0 / self.frame_hz
        
        for i in range(stride_frames):
            # The i-th frame in the selective prediction represents a context frame
            # at time: context_end - stride_duration + (i * frame_duration)
            context_frame_time = context_end - self.stride_duration + (i * frame_duration)
            
            # BC/HN predictions: Apply shift to get prediction time
            bc_prediction_time = context_frame_time + self.bc_shift
            hn_prediction_time = context_frame_time + self.hn_shift
            
            # For simplicity, use BC shift for the frame timestamp
            # (Both BC and HN use same shift typically: 500ms)
            frame_time_start = bc_prediction_time
            frame_time_end = frame_time_start + frame_duration
            
            pred = PredictionFrame(
                time_start=frame_time_start,
                time_end=frame_time_end,
                vap_p_now=p_now[i],
                vap_p_future=p_future[i],
                bc_detection_probs=bc_detection_probs_viz[i],
                bc_type_probs=bc_type_probs_with_no[i],
                bc_predicted_class=np.array([bc_predicted_class[i], bc_predicted_class[i]]),  # Duplicate for both speakers
                hn_detection_probs=hn_detection_probs_viz[i],
                hn_type_probs=hn_type_probs_with_no[i],
                hn_predicted_class=np.array([hn_predicted_class[i], hn_predicted_class[i]])  # Duplicate for both speakers
            )
            predictions.append(pred)
        
        # Explicit cleanup to prevent GPU memory accumulation
        del output, logits, vap_probs
        del bc_detection_logits, bc_detection_probs, bc_type_logits, bc_type_probs
        del hn_detection_logits, hn_detection_probs, hn_type_logits, hn_type_probs
        del batch  # Also free batch tensors
        
        # Clear CUDA cache periodically to prevent fragmentation
        if torch.cuda.is_available():
            torch.cuda.empty_cache()
        
        return predictions


# ============================================================================
# Audio Playback Engine (Separate Thread)
# ============================================================================

class AudioPlaybackEngine(threading.Thread):
    """
    Audio playback synchronized with visualization
    Plays audio in real-time while visualization updates
    """
    
    def __init__(self,
                 audio1: np.ndarray,
                 audio2: np.ndarray,
                 sample_rate: int,
                 startup_delay: float = 5.0):
        """
        Initialize audio playback engine
        
        Args:
            audio1: Audio for speaker 1 (mono)
            audio2: Audio for speaker 2 (mono)
            sample_rate: Sample rate in Hz
            startup_delay: Time to wait before starting playback (matches context building)
        """
        super().__init__(daemon=True)
        self.audio1 = audio1
        self.audio2 = audio2
        self.sample_rate = sample_rate
        self.startup_delay = startup_delay
        self.running = False
        self.stream = None
        
        # Create stereo audio: Left=Speaker1, Right=Speaker2
        # Both arrays should have the same length
        self.stereo_audio = np.stack([audio1, audio2], axis=1)  # [N, 2]
        
        print(f"\n🎵 Audio Playback Engine Initialized")
        print(f"   Duration: {len(audio1)/sample_rate:.1f}s")
        print(f"   Sample rate: {sample_rate} Hz")
        print(f"   Channels: Stereo (Left=Speaker1, Right=Speaker2)")
    
    def run(self):
        """Start audio playback (runs in background thread)"""
        self.running = True
        
        # Wait for startup delay (matching context building)
        print(f"\n🎧 Waiting {self.startup_delay}s before starting audio playback...")
        time.sleep(self.startup_delay)
        
        print(f"🎵 Starting audio playback (synchronized with visualization)...")
        
        try:
            # Play stereo audio
            sd.play(self.stereo_audio, samplerate=self.sample_rate, blocking=True)
            
            print(f"\n🎵 Audio playback completed")
            
        except Exception as e:
            print(f"❌ Audio playback error: {e}")
    
    def stop(self):
        """Stop audio playback"""
        self.running = False
        if self.stream is not None:
            sd.stop()


# ============================================================================
# Video Playback Engine (Separate Thread)
# ============================================================================

class VideoPlaybackEngine(threading.Thread):
    """
    Video playback synchronized with audio and visualization
    Displays video in separate OpenCV window
    """
    
    def __init__(self,
                 video_path: str,
                 startup_delay: float = 5.0,
                 window_name: str = "Speaker 1 Video"):
        """
        Initialize video playback engine
        
        Args:
            video_path: Path to video file
            startup_delay: Time to wait before starting playback (matches context building)
            window_name: Name for OpenCV window
        """
        super().__init__(daemon=True)
        self.video_path = video_path
        self.startup_delay = startup_delay
        self.window_name = window_name
        self.running = False
        
        # Open video to get metadata
        self.cap = cv2.VideoCapture(video_path)
        if not self.cap.isOpened():
            raise ValueError(f"Could not open video: {video_path}")
        
        self.fps = self.cap.get(cv2.CAP_PROP_FPS)
        self.total_frames = int(self.cap.get(cv2.CAP_PROP_FRAME_COUNT))
        self.duration = self.total_frames / self.fps if self.fps > 0 else 0
        self.width = int(self.cap.get(cv2.CAP_PROP_FRAME_WIDTH))
        self.height = int(self.cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
        
        # Close and will reopen in playback thread
        self.cap.release()
        
        print(f"\n🎬 Video Playback Engine Initialized")
        print(f"   Video: {video_path}")
        print(f"   Duration: {self.duration:.1f}s")
        print(f"   FPS: {self.fps:.1f}")
        print(f"   Resolution: {self.width}x{self.height}")
    
    def run(self):
        """Start video playback (runs in background thread)"""
        self.running = True
        
        # Wait for startup delay (matching context building)
        print(f"\n🎥 Waiting {self.startup_delay}s before starting video playback...")
        time.sleep(self.startup_delay)
        
        print(f"🎬 Starting video playback (synchronized with audio)...")
        
        try:
            # Reopen video for playback
            cap = cv2.VideoCapture(self.video_path)
            if not cap.isOpened():
                print(f"❌ Could not reopen video: {self.video_path}")
                return
            
            # Create window
            cv2.namedWindow(self.window_name, cv2.WINDOW_NORMAL)
            cv2.resizeWindow(self.window_name, self.width, self.height)
            
            # Calculate frame delay for real-time playback
            frame_delay = int(1000 / self.fps) if self.fps > 0 else 33  # ms
            
            frame_count = 0
            start_time = time.time()
            
            while self.running:
                ret, frame = cap.read()
                
                if not ret:
                    # End of video
                    break
                
                # Display frame
                cv2.imshow(self.window_name, frame)
                
                # Check for window close or ESC key
                key = cv2.waitKey(frame_delay) & 0xFF
                if key == 27 or cv2.getWindowProperty(self.window_name, cv2.WND_PROP_VISIBLE) < 1:
                    # ESC pressed or window closed
                    break
                
                frame_count += 1
                
                # Synchronization: maintain target FPS
                expected_time = frame_count / self.fps
                actual_time = time.time() - start_time
                if actual_time < expected_time:
                    time.sleep(expected_time - actual_time)
            
            cap.release()
            cv2.destroyWindow(self.window_name)
            
            print(f"\n🎬 Video playback completed")
            
        except Exception as e:
            print(f"❌ Video playback error: {e}")
            import traceback
            traceback.print_exc()
    
    def stop(self):
        """Stop video playback"""
        self.running = False
        try:
            cv2.destroyWindow(self.window_name)
        except:
            pass


# ============================================================================
# Visualization Engine (Main Thread, 10Hz Updates)
# ============================================================================

class RealtimeVisualizer:
    """
    Real-time visualization at 10Hz
    Updates plots progressively as predictions become available
    """
    
    def __init__(self,
                 audio1: np.ndarray,
                 audio2: np.ndarray,
                 sample_rate: int,
                 frame_hz: float,
                 prediction_buffer: PredictionBuffer,
                 bc_detection_threshold: float = 0.5,
                 hn_detection_threshold: float = 0.5,
                 bc_mode: str = 'probability',
                 hn_mode: str = 'probability'):
        """
        Args:
            audio1: Speaker 1 audio waveform
            audio2: Speaker 2 audio waveform
            sample_rate: Audio sample rate
            frame_hz: Model frame rate
            prediction_buffer: Shared prediction buffer
            bc_detection_threshold: BC detection threshold
            hn_detection_threshold: HN detection threshold
            bc_mode: BC visualization mode ('probability' or 'category')
            hn_mode: HN visualization mode ('probability' or 'category')
        """
        self.audio1 = audio1
        self.audio2 = audio2
        self.sample_rate = sample_rate
        self.frame_hz = frame_hz
        self.prediction_buffer = prediction_buffer
        self.bc_threshold = bc_detection_threshold
        self.hn_threshold = hn_detection_threshold
        self.bc_mode = bc_mode
        self.hn_mode = hn_mode
        
        self.audio_duration = len(audio1) / sample_rate
        self.current_time = 0.0  # Current playback time
        self.start_time = None  # Will be set when visualization starts (real wall-clock time)
        
        # Storage for cumulative prediction history (for progressive plotting)
        self.prediction_history = {
            'times': [],
            'vap_p_now': [],
            'vap_p_future': [],
            'bc_det_probs': [],
            'bc_type_probs': [],
            'hn_det_probs': [],
            'hn_type_probs': []
        }
        
        # Initialize plots
        self._init_plots()
        
    def _init_plots(self):
        """Initialize matplotlib figure and axes"""
        # Calculate number of plots
        n_plots = 5  # Audio1, Audio2, VAP P_now, VAP P_future, BC detection
        if self.bc_mode == 'probability':
            n_plots += 1  # BC type probabilities
        
        n_plots += 1  # HN detection
        if self.hn_mode == 'probability':
            n_plots += 1  # HN type probabilities
        
        self.fig, self.axes = plt.subplots(n_plots, 1, figsize=(16, 2.5 * n_plots))
        self.fig.suptitle('Real-Time VAP Inference - Speaker 1 Focus', fontsize=16, fontweight='bold')
        
        # Store axis indices (audio1 and audio2 will be assigned in their section)
        ax_idx = 2  # Start after audio1 and audio2
        self.ax_vap_now = self.axes[ax_idx]; ax_idx += 1
        self.ax_vap_future = self.axes[ax_idx]; ax_idx += 1
        self.ax_bc_det = self.axes[ax_idx]; ax_idx += 1
        if self.bc_mode == 'probability':
            self.ax_bc_type = self.axes[ax_idx]; ax_idx += 1
        self.ax_hn_det = self.axes[ax_idx]; ax_idx += 1
        if self.hn_mode == 'probability':
            self.ax_hn_type = self.axes[ax_idx]; ax_idx += 1
        
        # Initialize audio plots (progressive visualization) - separate for each speaker
        # Audio plot 1: Speaker 1
        self.ax_audio1 = self.axes[0]
        self.ax_audio1.set_xlim(0, self.audio_duration)
        self.ax_audio1.set_ylim(-1, 1)  # Normalized audio range
        self.ax_audio1.set_ylabel('Amplitude')
        self.ax_audio1.set_title('Audio - Speaker 1 (Progressive)')
        self.ax_audio1.grid(True, alpha=0.3)
        
        self.audio_line1 = self.ax_audio1.plot([], [], color='blue', linewidth=0.8)[0]
        self.time_marker1 = self.ax_audio1.axvline(x=0, color='red', linestyle='--', linewidth=2)
        
        # Audio plot 2: Speaker 2
        self.ax_audio2 = self.axes[1]
        self.ax_audio2.set_xlim(0, self.audio_duration)
        self.ax_audio2.set_ylim(-1, 1)
        self.ax_audio2.set_ylabel('Amplitude')
        self.ax_audio2.set_title('Audio - Speaker 2 (Progressive)')
        self.ax_audio2.grid(True, alpha=0.3)
        
        self.audio_line2 = self.ax_audio2.plot([], [], color='red', linewidth=0.8)[0]
        self.time_marker2 = self.ax_audio2.axvline(x=0, color='red', linestyle='--', linewidth=2)
        
        # Initialize VAP P_now plot (Speaker 1 only)
        self.ax_vap_now.set_xlim(0, self.audio_duration)
        self.ax_vap_now.set_ylim(0, 1)
        self.ax_vap_now.set_ylabel('Probability')
        self.ax_vap_now.set_title('VAP P_now - Speaker 1 (Current Turn-Taking)')
        self.ax_vap_now.grid(True, alpha=0.3)
        
        self.vap_now_line = self.ax_vap_now.plot([], [], label='P_now', color='blue', linewidth=2)[0]
        self.ax_vap_now.legend(loc='upper right')
        
        # Initialize VAP P_future plot (Speaker 1 only)
        self.ax_vap_future.set_xlim(0, self.audio_duration)
        self.ax_vap_future.set_ylim(0, 1)
        self.ax_vap_future.set_ylabel('Probability')
        self.ax_vap_future.set_title('VAP P_future - Speaker 1 (Future Turn-Taking)')
        self.ax_vap_future.grid(True, alpha=0.3)
        
        self.vap_future_line = self.ax_vap_future.plot([], [], label='P_future', color='red', linewidth=2)[0]
        self.ax_vap_future.legend(loc='upper right')
        
        # Initialize BC detection plot (Speaker 1 only)
        self.ax_bc_det.set_xlim(0, self.audio_duration)
        self.ax_bc_det.set_ylim(0, 1)
        self.ax_bc_det.set_ylabel('Probability')
        self.ax_bc_det.set_title(f'BC Detection - Speaker 1 (threshold={self.bc_threshold})')
        self.ax_bc_det.axhline(y=self.bc_threshold, color='gray', linestyle='--', linewidth=1, alpha=0.5)
        self.ax_bc_det.grid(True, alpha=0.3)
        
        self.bc_det_line = self.ax_bc_det.plot([], [], label='Any BC', color='blue', linewidth=2)[0]
        self.ax_bc_det.legend(loc='upper right')
        
        # Initialize BC type plot (if probability mode) - Speaker 1 only
        if self.bc_mode == 'probability':
            self.ax_bc_type.set_xlim(0, self.audio_duration)
            self.ax_bc_type.set_ylim(0, 1)
            self.ax_bc_type.set_ylabel('Probability')
            self.ax_bc_type.set_title('BC Type Probabilities - Speaker 1')
            self.ax_bc_type.grid(True, alpha=0.3)
            
            self.bc_type_lines = {
                'reactive': self.ax_bc_type.plot([], [], label='Reactive', color='green', linewidth=2)[0],
                'emotional': self.ax_bc_type.plot([], [], label='Emotional', color='orange', linewidth=2)[0],
            }
            self.ax_bc_type.legend(loc='upper right')
        
        # Initialize HN detection plot (Speaker 1 only)
        self.ax_hn_det.set_xlim(0, self.audio_duration)
        self.ax_hn_det.set_ylim(0, 1)
        self.ax_hn_det.set_ylabel('Probability')
        self.ax_hn_det.set_title(f'HN Detection - Speaker 1 (threshold={self.hn_threshold})')
        self.ax_hn_det.axhline(y=self.hn_threshold, color='gray', linestyle='--', linewidth=1, alpha=0.5)
        self.ax_hn_det.grid(True, alpha=0.3)
        
        self.hn_det_line = self.ax_hn_det.plot([], [], label='Any HN', color='purple', linewidth=2)[0]
        self.ax_hn_det.legend(loc='upper right')
        
        # Initialize HN type plot (if probability mode) - Speaker 1 only
        if self.hn_mode == 'probability':
            self.ax_hn_type.set_xlim(0, self.audio_duration)
            self.ax_hn_type.set_ylim(0, 1)
            self.ax_hn_type.set_ylabel('Probability')
            self.ax_hn_type.set_title('HN Type Probabilities - Speaker 1')
            self.ax_hn_type.grid(True, alpha=0.3)
            
            self.hn_type_lines = {
                'short': self.ax_hn_type.plot([], [], label='Short', color='blue', linewidth=2)[0],
                'long': self.ax_hn_type.plot([], [], label='Long', color='green', linewidth=2)[0],
                'long_p': self.ax_hn_type.plot([], [], label='Long+', color='red', linewidth=2)[0],
            }
            self.ax_hn_type.legend(loc='upper right')
        
        plt.tight_layout()
    
    def update(self, frame):
        """
        Update function called by matplotlib animation (10Hz)
        
        Args:
            frame: Frame number (not used, we use wall-clock time)
        """
        # Initialize start time on first call
        if self.start_time is None:
            self.start_time = time.time()
            print(f"⏱️  Visualization clock started at t=0.0s")
        
        # Use REAL wall-clock time (not frame-based time)
        # This ensures synchronization with audio playback
        self.current_time = time.time() - self.start_time
        
        # Periodic sync status log (every 10 seconds)
        if int(self.current_time) % 10 == 0 and int(self.current_time) > 0:
            if not hasattr(self, '_last_log_time') or int(self.current_time) != self._last_log_time:
                self._last_log_time = int(self.current_time)
                print(f"⏱️  Visualization sync check: t={self.current_time:.1f}s (wall-clock time)")
        
        # Update progressive audio visualization (separate plots)
        current_sample = int(self.current_time * self.sample_rate)
        if current_sample > 0:
            audio_time_axis = np.arange(current_sample) / self.sample_rate
            self.audio_line1.set_data(audio_time_axis, self.audio1[:current_sample])
            self.audio_line2.set_data(audio_time_axis, self.audio2[:current_sample])
        
        # Update time markers on audio plots
        self.time_marker1.set_xdata([self.current_time, self.current_time])
        self.time_marker2.set_xdata([self.current_time, self.current_time])
        
        # Fetch NEW predictions from buffer (only recent ones)
        predictions = self.prediction_buffer.get_range(0, self.current_time)
        
        if len(predictions) > 0:
            # Accumulate new predictions into history (avoid duplicates)
            for pred in predictions:
                pred_time = pred.time_start
                # Only add if not already in history
                if len(self.prediction_history['times']) == 0 or pred_time > self.prediction_history['times'][-1]:
                    self.prediction_history['times'].append(pred_time)
                    self.prediction_history['vap_p_now'].append(pred.vap_p_now[0])  # Speaker 1 only
                    self.prediction_history['vap_p_future'].append(pred.vap_p_future[0])  # Speaker 1 only
                    self.prediction_history['bc_det_probs'].append(pred.bc_detection_probs[0])  # Speaker 1 only
                    self.prediction_history['bc_type_probs'].append(pred.bc_type_probs[0])  # Speaker 1 only
                    self.prediction_history['hn_det_probs'].append(pred.hn_detection_probs[0])  # Speaker 1 only
                    self.prediction_history['hn_type_probs'].append(pred.hn_type_probs[0])  # Speaker 1 only
        
        # Plot cumulative history (like audio - progressively growing)
        if len(self.prediction_history['times']) > 0:
            times = np.array(self.prediction_history['times'])
            
            # VAP data (Speaker 1 only)
            vap_p_now = np.array(self.prediction_history['vap_p_now'])
            vap_p_future = np.array(self.prediction_history['vap_p_future'])
            
            # BC data (Speaker 1 only)
            bc_det_probs = np.array(self.prediction_history['bc_det_probs'])
            bc_type_probs = np.array(self.prediction_history['bc_type_probs'])  # [N, 3]
            
            # HN data (Speaker 1 only)
            hn_det_probs = np.array(self.prediction_history['hn_det_probs'])
            hn_type_probs = np.array(self.prediction_history['hn_type_probs'])  # [N, 4]
            
            # Update VAP lines (Speaker 1 only)
            self.vap_now_line.set_data(times, vap_p_now)
            self.vap_future_line.set_data(times, vap_p_future)
            
            # Update BC detection line (Speaker 1 only)
            self.bc_det_line.set_data(times, bc_det_probs)
            
            # Update BC type lines (Speaker 1 only, if probability mode)
            if self.bc_mode == 'probability':
                self.bc_type_lines['reactive'].set_data(times, bc_type_probs[:, 0])
                self.bc_type_lines['emotional'].set_data(times, bc_type_probs[:, 1])
            
            # Update HN detection line (Speaker 1 only)
            self.hn_det_line.set_data(times, hn_det_probs)
            
            # Update HN type lines (Speaker 1 only, if probability mode)
            if self.hn_mode == 'probability':
                self.hn_type_lines['short'].set_data(times, hn_type_probs[:, 0])
                self.hn_type_lines['long'].set_data(times, hn_type_probs[:, 1])
                self.hn_type_lines['long_p'].set_data(times, hn_type_probs[:, 2])
        
        # Stop animation when we reach the end
        if self.current_time >= self.audio_duration:
            print("\n🎬 Playback completed!")
            return []
        
        return []
    
    def run(self, startup_duration: float = 5.0):
        """
        Run the visualization
        
        Args:
            startup_duration: Wait time for initial context building
        """
        # Show startup message
        print(f"\n🔧 Building initial context ({startup_duration}s)...")
        print("   Visualization will start after context is ready.")
        
        # Wait for startup
        time.sleep(startup_duration)
        
        print(f"\n🎬 Starting real-time visualization at 10Hz...")
        print("   ⏱️  Using wall-clock time for synchronization with audio playback")
        
        # Create animation (10Hz = 100ms interval)
        # Note: Uses wall-clock time in update(), not frame count
        interval = 1000 / self.frame_hz  # milliseconds
        anim = animation.FuncAnimation(
            self.fig, 
            self.update, 
            interval=interval,
            blit=False,
            cache_frame_data=False
        )
        
        plt.show()


# ============================================================================
# Main Coordinator
# ============================================================================

def load_audio(audio_path: str, target_sr: int) -> torch.Tensor:
    """Load and resample audio file"""
    audio, sr = torchaudio.load(audio_path)
    
    # Convert to mono if stereo
    if audio.shape[0] > 1:
        audio = audio.mean(dim=0, keepdim=True)
    
    # Resample if needed
    if sr != target_sr:
        resampler = torchaudio.transforms.Resample(sr, target_sr)
        audio = resampler(audio)
    
    return audio.squeeze(0)


def main():
    parser = argparse.ArgumentParser(description='Real-Time VAP Inference and Visualization')
    
    # Required arguments
    parser.add_argument('--audio1', type=str, required=True,
                       help='Path to speaker 1 audio file')
    parser.add_argument('--audio2', type=str, required=True,
                       help='Path to speaker 2 audio file')
    parser.add_argument('--checkpoint', type=str, required=True,
                       help='Path to VAP model checkpoint')
    
    # Video playback (optional)
    parser.add_argument('--video1', type=str, default=None,
                       help='Path to speaker 1 video file (optional, muted playback)')
    parser.add_argument('--video2', type=str, default=None,
                       help='Path to speaker 2 video file (optional, muted playback)')
    
    # Multimodal data (optional)
    parser.add_argument('--face1', type=str, default=None,
                       help='Path to speaker 1 face CSV file')
    parser.add_argument('--gaze1', type=str, default=None,
                       help='Path to speaker 1 gaze CSV file')
    parser.add_argument('--body1', type=str, default=None,
                       help='Path to speaker 1 body/head pose CSV file')
    parser.add_argument('--face2', type=str, default=None,
                       help='Path to speaker 2 face CSV file')
    parser.add_argument('--gaze2', type=str, default=None,
                       help='Path to speaker 2 gaze CSV file')
    parser.add_argument('--body2', type=str, default=None,
                       help='Path to speaker 2 body/head pose CSV file')
    
    # Inference parameters
    parser.add_argument('--context_duration', type=float, default=5.0,
                       help='Context window size (seconds) [default: 5.0]')
    parser.add_argument('--stride_duration', type=float, default=0.5,
                       help='Time between inferences (seconds) [default: 1.0]')
    parser.add_argument('--bc_detection_threshold', type=float, default=0.5,
                       help='BC detection threshold [default: 0.5]')
    parser.add_argument('--hn_detection_threshold', type=float, default=0.5,
                       help='HN detection threshold [default: 0.5]')
    parser.add_argument('--bc_shift', type=float, default=None,
                       help='BC prediction shift in seconds (None = use model config)')
    parser.add_argument('--hn_shift', type=float, default=None,
                       help='HN prediction shift in seconds (None = use model config)')
    
    # Visualization parameters
    parser.add_argument('--bc_visualization', type=str, default='probability',
                       choices=['probability', 'category'],
                       help='BC visualization mode')
    parser.add_argument('--hn_visualization', type=str, default='probability',
                       choices=['probability', 'category'],
                       help='HN visualization mode')
    
    # Device
    parser.add_argument('--device', type=str, default='auto',
                       choices=['auto', 'cuda', 'cpu'],
                       help="Device to run inference on ('auto' = choose CUDA if available)")
    
    args = parser.parse_args()
    
    # Load model
    print("\n" + "="*80)
    print("REAL-TIME VAP INFERENCE SYSTEM")
    print("="*80)
    
    # Resolve device selection
    if args.device == 'auto':
        resolved_device = 'cuda' if torch.cuda.is_available() else 'cpu'
        print(f"\n🧠 Auto device selection: using {resolved_device.upper()}")
    elif args.device == 'cuda' and not torch.cuda.is_available():
        print("\n⚠️  Requested CUDA but PyTorch was not compiled with CUDA support.")
        print("   Falling back to CPU. Re-run with '--device cpu' to silence this warning.")
        resolved_device = 'cpu'
    else:
        resolved_device = args.device
    
    model_wrapper = VAPModelWrapper(args.checkpoint, device=resolved_device)
    
    # Load audio files
    print(f"\n🎵 Loading audio files...")
    audio1 = load_audio(args.audio1, model_wrapper.conf.sample_rate)
    audio2 = load_audio(args.audio2, model_wrapper.conf.sample_rate)
    
    # Ensure both audio streams have identical length
    if len(audio1) != len(audio2):
        min_len = min(len(audio1), len(audio2))
        print(f"⚠️  Audio length mismatch detected: "
              f"{len(audio1)/model_wrapper.conf.sample_rate:.2f}s vs "
              f"{len(audio2)/model_wrapper.conf.sample_rate:.2f}s")
        print(f"   Truncating both to {min_len / model_wrapper.conf.sample_rate:.2f}s "
              f"for synchronized visualization.")
        audio1 = audio1[:min_len]
        audio2 = audio2[:min_len]
    
    duration = len(audio1) / model_wrapper.conf.sample_rate
    print(f"   Duration: {duration:.1f}s")
    print(f"   Sample rate: {model_wrapper.conf.sample_rate} Hz")
    
    # Prepare multimodal paths if provided
    multimodal_paths = None
    if any([args.face1, args.gaze1, args.body1, args.face2, args.gaze2, args.body2]):
        multimodal_paths = {
            'speaker1': {
                'face': args.face1,
                'gaze': args.gaze1,
                'body': args.body1
            },
            'speaker2': {
                'face': args.face2,
                'gaze': args.gaze2,
                'body': args.body2
            }
        }
        print(f"\n📊 Multimodal data:")
        for spk, paths in multimodal_paths.items():
            provided = [k for k, v in paths.items() if v is not None]
            if provided:
                print(f"   {spk}: {', '.join(provided)}")
    
    # Initialize prediction buffer with large capacity for full audio duration
    # Use audio duration + buffer for the entire conversation
    prediction_buffer = PredictionBuffer(
        max_buffer_duration=duration + 10.0,  # Entire audio + 10s buffer
        frame_hz=model_wrapper.conf.frame_hz
    )
    
    # Create inference engine
    inference_engine = InferenceEngine(
        model_wrapper=model_wrapper,
        audio1=audio1,
        audio2=audio2,
        prediction_buffer=prediction_buffer,
        multimodal_paths=multimodal_paths,
        context_duration=args.context_duration,
        stride_duration=args.stride_duration,
        bc_detection_threshold=args.bc_detection_threshold,
        hn_detection_threshold=args.hn_detection_threshold,
        bc_shift=args.bc_shift,
        hn_shift=args.hn_shift
    )
    
    # Create visualizer with normalized audio (visualization only, model uses original)
    audio1_numpy = audio1.numpy()
    audio2_numpy = audio2.numpy()
    
    # Normalize each audio stream independently for equal visual density
    # This ONLY affects visualization, NOT model inference
    # Using 99th percentile instead of max to avoid outliers
    audio1_percentile = np.percentile(np.abs(audio1_numpy), 99)
    audio2_percentile = np.percentile(np.abs(audio2_numpy), 99)
    
    # Use percentile for normalization (more robust than max)
    audio1_norm_factor = audio1_percentile if audio1_percentile > 0 else 1.0
    audio2_norm_factor = audio2_percentile if audio2_percentile > 0 else 1.0
    
    audio1_viz = audio1_numpy / audio1_norm_factor
    audio2_viz = audio2_numpy / audio2_norm_factor
    
    # Scale down to 0.6 of full range for better visualization (less overwhelming)
    viz_scale = 0.3
    audio1_viz = audio1_viz * viz_scale
    audio2_viz = audio2_viz * viz_scale
    
    print(f"\n🎨 Audio normalization for visualization:")
    print(f"   Speaker 1: 99th percentile={audio1_norm_factor:.4f}, scaled to ±{viz_scale:.1f} range")
    print(f"   Speaker 2: 99th percentile={audio2_norm_factor:.4f}, scaled to ±{viz_scale:.1f} range")
    print(f"   → Both normalized to similar visual range for better comparison")
    
    visualizer = RealtimeVisualizer(
        audio1=audio1_viz,
        audio2=audio2_viz,
        sample_rate=model_wrapper.conf.sample_rate,
        frame_hz=model_wrapper.conf.frame_hz,
        prediction_buffer=prediction_buffer,
        bc_detection_threshold=args.bc_detection_threshold,
        hn_detection_threshold=args.hn_detection_threshold,
        bc_mode=args.bc_visualization,
        hn_mode=args.hn_visualization
    )
    
    # Create audio playback engine (uses ORIGINAL audio, not normalized)
    audio_playback = AudioPlaybackEngine(
        audio1=audio1_numpy,
        audio2=audio2_numpy,
        sample_rate=model_wrapper.conf.sample_rate,
        startup_delay=args.context_duration
    )
    
    # Create video playback engines if video paths provided
    video_engines = []
    if args.video1:
        try:
            video1_engine = VideoPlaybackEngine(
                video_path=args.video1,
                startup_delay=args.context_duration,
                window_name="Speaker 1 Video"
            )
            video_engines.append(video1_engine)
        except Exception as e:
            print(f"⚠️  Could not initialize video1: {e}")
    
    if args.video2:
        try:
            video2_engine = VideoPlaybackEngine(
                video_path=args.video2,
                startup_delay=args.context_duration,
                window_name="Speaker 2 Video"
            )
            video_engines.append(video2_engine)
        except Exception as e:
            print(f"⚠️  Could not initialize video2: {e}")
    
    # Start all engines (background threads)
    inference_engine.start()
    audio_playback.start()
    for video_engine in video_engines:
        video_engine.start()
    
    # Run visualizer (main thread, blocks until done)
    visualizer.run(startup_duration=args.context_duration)
    
    # Stop all engines
    inference_engine.stop()
    audio_playback.stop()
    for video_engine in video_engines:
        video_engine.stop()
    
    inference_engine.join(timeout=2.0)
    audio_playback.join(timeout=2.0)
    for video_engine in video_engines:
        video_engine.join(timeout=2.0)
    
    print("\n" + "="*80)
    print("✅ Real-time inference completed successfully!")
    print("="*80)


if __name__ == '__main__':
    main()
