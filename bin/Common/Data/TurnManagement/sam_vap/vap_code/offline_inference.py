#!/usr/bin/env python3
"""
Offline VAP Inference and Visualization System

Step 1: Audio-only inference with VAP predictions
Based on VAP-Realtime repository for inspiration

Usage:
    python offline_inference.py --audio1 speaker1.wav --audio2 speaker2.wav --model_path checkpoint.ckpt
"""

import argparse
import torch
import torchaudio
import numpy as np
import matplotlib.pyplot as plt
import matplotlib.patches as patches
from pathlib import Path
from typing import Dict, Tuple, Optional, List
import warnings
import time
warnings.filterwarnings("ignore")

# Import VAP components
from model import VapGPT, VapConfig
from events import TurnTakingEvents
import torch.nn.functional as F

# Import config classes for checkpoint compatibility
from finetune import OptConfig, DataConfig
from events import EventConfig

# Add config classes to module namespace for pickle compatibility
import sys
current_module = sys.modules[__name__]
current_module.VapConfig = VapConfig
current_module.OptConfig = OptConfig
current_module.DataConfig = DataConfig
current_module.EventConfig = EventConfig


class OfflineVAPInference:
    """
    Offline VAP inference system for audio-only predictions
    """
    
    def __init__(self, model_path: str, device: str = "cuda", 
                 model_config_overrides: Dict = None):
        """
        Initialize the offline inference system
        
        Args:
            model_path: Path to trained VAP model checkpoint
            device: Device to run inference on
            model_config_overrides: Dictionary of model config overrides
        """
        self.device = "cpu"
        self.model_path = model_path
        self.model_config_overrides = model_config_overrides or {}
        # Detection thresholds (can be overridden from CLI in main())
        self.bc_detection_threshold: float = 0.5
        self.hn_detection_threshold: float = 0.5
        
        # Load model
        self.model, self.conf = self.load_model()
        
        # Initialize event processor
        self.event_processor = TurnTakingEvents()
        
        print(f"✅ Loaded VAP model from {model_path}")
        print(f"📊 Model configuration:")
        print(f"   - Sample rate: {self.conf.sample_rate}")
        print(f"   - Frame rate: {self.conf.frame_hz}")
        print(f"   - Multimodal: {self.conf.multimodal}")
        print(f"   - Whisper semantics: {getattr(self.conf, 'use_whisper_semantics', False)}")
        print(f"   - HuBERT semantics: {getattr(self.conf, 'use_hubert_semantics', False)}")
        context_limit = getattr(self.conf, 'context_limit', -1)
        context_limit_display = f"{context_limit} frames" if context_limit > 0 else "unlimited"
        print(f"   - Context limit: {context_limit_display}")
        print(f"   - BC prediction: {self.conf.predict_backchannels}")
        print(f"   - Head nod prediction: {self.conf.predict_head_nods}")
        print(f"   - BC detection head: {self.conf.bc_detection_head}")
        print(f"   - BC type head: {self.conf.bc_type_head}")
        print(f"   - HN detection head: {self.conf.hn_detection_head}")
        print(f"   - HN type head: {self.conf.hn_type_head}")
        
        # Display shift parameters
        bc_shift_ms = getattr(self.conf, 'backchannel_prediction_shift_ms', 500)
        hn_shift_ms = getattr(self.conf, 'head_nod_prediction_shift_ms', 500)
        print(f"   - BC prediction shift: {bc_shift_ms}ms ({bc_shift_ms/1000:.1f}s)")
        print(f"   - HN prediction shift: {hn_shift_ms}ms ({hn_shift_ms/1000:.1f}s)")
        
        print(f"   - Face encoder: {getattr(self.conf, 'use_face_encoder', False)}")
        print(f"   - Gaze encoder: {getattr(self.conf, 'use_gaze_encoder', False)}")
        print(f"   - Body encoder: {getattr(self.conf, 'use_body_encoder', False)}")
        
    def load_model(self) -> Tuple[VapGPT, VapConfig]:
        """Load VAP model from checkpoint"""
        
        # Load checkpoint using safe loader
        from safe_checkpoint_loader import load_vap_checkpoint_safe
        checkpoint = load_vap_checkpoint_safe(self.model_path, map_location="cpu")
        
        if checkpoint is None:
            raise RuntimeError(f"Failed to load checkpoint from {self.model_path}")
        
        # Extract configuration
        if 'hyper_parameters' in checkpoint:
            # Lightning checkpoint
            conf_dict = checkpoint['hyper_parameters']['conf']
            if isinstance(conf_dict, dict):
                conf = VapConfig(**conf_dict)
            else:
                conf = conf_dict
        elif 'conf' in checkpoint:
            # Direct model checkpoint with config
            conf_dict = checkpoint['conf']
            if isinstance(conf_dict, dict):
                conf = VapConfig(**conf_dict)
            else:
                conf = conf_dict
        else:
            # Direct model checkpoint without config - use default config
            print("⚠️  Warning: No config found in checkpoint, using default VapConfig")
            conf = VapConfig()
            
        # Apply config overrides
        if self.model_config_overrides:
            print(f"🔧 Applying model config overrides:")
            for key, value in self.model_config_overrides.items():
                if hasattr(conf, key):
                    old_value = getattr(conf, key)
                    setattr(conf, key, value)
                    print(f"   - {key}: {old_value} → {value}")
                else:
                    print(f"   - Warning: Unknown config parameter '{key}' ignored")
        # Note: If no overrides provided, keep the trained model's original configuration
        
        # Initialize model with CPU-safe loading
        from cpu_safe_encoder_patch import CPUSafeContext
        
        with CPUSafeContext():
            model = VapGPT(conf)
        
        # Load state dict
        if 'state_dict' in checkpoint:
            # Lightning checkpoint
            state_dict = checkpoint['state_dict']
            # Remove 'model.' prefix if present
            state_dict = {k.replace('model.', ''): v for k, v in state_dict.items()}
        else:
            state_dict = checkpoint
            
        # Ensure all tensors in state_dict are on CPU before loading
        cpu_state_dict = {}
        for k, v in state_dict.items():
            if isinstance(v, torch.Tensor):
                cpu_state_dict[k] = v.cpu()
            else:
                cpu_state_dict[k] = v
        
        model.load_state_dict(cpu_state_dict, strict=False)
        model.eval()
        model.to(self.device)
        
        # Add device attribute to model for compatibility
        model.device = torch.device(self.device)
        
        # Enable selective prediction for inference optimization
        # This will make the model only predict the last N frames during inference
        # reducing computation while maintaining temporal consistency
        conf.inference_mode = True
        
        return model, conf
    
    def load_audio(self, audio_path: str) -> Tuple[torch.Tensor, int]:
        """
        Load and preprocess audio file
        
        Args:
            audio_path: Path to audio file
            
        Returns:
            Tuple of (audio_tensor, original_sample_rate)
        """
        audio, sr = torchaudio.load(audio_path)
        
        # Convert to mono if stereo
        if audio.shape[0] > 1:
            audio = audio.mean(dim=0, keepdim=True)
            
        # Resample to model sample rate if needed
        if sr != self.conf.sample_rate:
            resampler = torchaudio.transforms.Resample(sr, self.conf.sample_rate)
            audio = resampler(audio)
            
        return audio.squeeze(0), sr
    
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
                     multimodal_paths: Dict[str, str] = None) -> Dict:
        """
        Prepare batch for VAP model inference
        
        Args:
            audio1: Speaker 1 audio tensor
            audio2: Speaker 2 audio tensor
            multimodal_paths: Dictionary with paths to multimodal CSV files
            
        Returns:
            Batch dictionary for model input
        """
        # Ensure same length
        min_length = min(len(audio1), len(audio2))
        audio1 = audio1[:min_length]
        audio2 = audio2[:min_length]
        
        # Stack into stereo format [2, T]
        waveform = torch.stack([audio1, audio2], dim=0)
        
        # Add batch dimension [1, 2, T]
        waveform = waveform.unsqueeze(0).to(self.device)
        
        # Calculate expected frames for alignment
        audio_duration_sec = waveform.shape[-1] / self.conf.sample_rate
        expected_frames = int(audio_duration_sec * self.conf.frame_hz)
        
        # Create batch
        batch = {
            'waveform': waveform,
        }
        
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
    def inference(self, audio1_path: str, audio2_path: str, 
                 multimodal_paths: Dict[str, Dict[str, str]] = None,
                 chunk_duration: float = 5.0,
                 stride_duration: float = 1.0,
                 selective_frames: int = -1) -> Dict:
        """
        Run VAP inference on two audio files with optional multimodal data
        
        Implements streaming-style inference with sliding context window:
        - Context: chunk_duration (e.g., 5s of past audio)
        - Stride: stride_duration (e.g., 1.0s between inferences)
        - Selective prediction: Only predict last N frames for efficiency
        
        Args:
            audio1_path: Path to speaker 1 audio
            audio2_path: Path to speaker 2 audio
            multimodal_paths: Dictionary with multimodal CSV paths
            chunk_duration: Context duration for each inference (seconds)
            stride_duration: Time between consecutive inferences (seconds)
            selective_frames: Number of last frames to predict (-1 = auto from stride)
            
        Returns:
            Dictionary containing predictions and metadata
        """
        print(f"🎵 Loading audio files...")
        print(f"   Speaker 1: {audio1_path}")
        print(f"   Speaker 2: {audio2_path}")
        
        # Load audio files
        audio1, sr1 = self.load_audio(audio1_path)
        audio2, sr2 = self.load_audio(audio2_path)
        
        # Calculate selective frames if auto
        if selective_frames == -1:
            # Auto-calculate: predict frames corresponding to stride duration
            selective_frames = int(stride_duration * self.conf.frame_hz * 1.5)  # 1.5x stride for overlap
            print(f"🔧 Auto-calculated selective_frames: {selective_frames} frames ({selective_frames / self.conf.frame_hz:.1f}s)")
        elif selective_frames == 0:
            selective_frames = -1  # -1 means predict all frames in model
            print(f"🔧 Selective prediction: DISABLED (predicting all frames)")
        else:
            print(f"🔧 Selective prediction: {selective_frames} frames ({selective_frames / self.conf.frame_hz:.1f}s)")
        
        # Configure model for selective prediction
        self.conf.inference_predict_last_n_frames = selective_frames if selective_frames > 0 else -1
        
        print(f"📊 Audio info:")
        print(f"   Duration: {len(audio1) / self.conf.sample_rate:.2f}s")
        print(f"   Sample rate: {self.conf.sample_rate}")
        print(f"   Frame rate: {self.conf.frame_hz}Hz ({1000/self.conf.frame_hz:.0f}ms/frame)")
        print(f"   Context duration: {chunk_duration}s")
        print(f"   Stride duration: {stride_duration}s")
        
        # Print multimodal info if provided
        if multimodal_paths:
            print(f"📊 Multimodal data:")
            for speaker, paths in multimodal_paths.items():
                print(f"   {speaker}:")
                for modality, path in paths.items():
                    exists = "✅" if Path(path).exists() else "❌"
                    print(f"     {modality}: {exists} {path}")
        
        # Process audio with streaming strategy
        print(f"🔮 Running streaming VAP inference...")
        results = self._process_audio_chunks(audio1, audio2, multimodal_paths, 
                                             chunk_duration, stride_duration)
        
        return results

    def load_ground_truth_labels(self, bc_expert_csv: str = None,
                                hn_expert_csv: str = None,
                                audio_duration_frames: int = None) -> Dict[str, torch.Tensor]:
        """
        Load ground truth BC and HN labels from CSV files for expert speakers only
        
        Args:
            bc_expert_csv: Path to expert backchannel ground truth CSV file
            hn_expert_csv: Path to expert head nod ground truth CSV file
            audio_duration_frames: Expected number of frames based on audio duration
            
        Returns:
            Dictionary with expert ground truth tensors
        """
        import pandas as pd
        import os
        
        gt_labels = {}
        
        def process_bc_labels(csv_path: str, speaker_type: str) -> Dict[str, torch.Tensor]:
            """Process BC labels for a single speaker"""
            if not csv_path or not os.path.exists(csv_path):
                return {
                    'bc_detection': torch.full((audio_duration_frames,), 0, dtype=torch.long),
                    'bc_type': torch.full((audio_duration_frames,), -1, dtype=torch.long)
                }
            
            try:
                print(f"📊 Loading {speaker_type} BC ground truth from: {csv_path}")
                bc_df = pd.read_csv(csv_path)
                
                if 'backchannel_class' in bc_df.columns:
                    bc_labels = torch.tensor(bc_df['backchannel_class'].values, dtype=torch.long)
                else:
                    print(f"⚠️  Warning: 'backchannel_class' column not found in {csv_path}")
                    bc_labels = torch.full((audio_duration_frames,), -1, dtype=torch.long)
                
                # Process BC labels: 0=no_bc, 1=reactive, 2=emotional
                # Convert to detection + type format for visualization
                no_bc_mask = bc_labels == 0
                reactive_mask = bc_labels == 1  
                emotional_mask = bc_labels == 2
                
                # Create detection labels: 0=no_bc, 1=any_bc
                detection_labels = torch.where(no_bc_mask, torch.tensor(0), torch.tensor(1))
                
                # Create type labels: 0=reactive, 1=emotional, -1=no_bc
                type_labels = torch.full_like(bc_labels, -1)
                type_labels[reactive_mask] = 0
                type_labels[emotional_mask] = 1
                
                # Ensure correct length
                if len(bc_labels) != audio_duration_frames:
                    if len(bc_labels) > audio_duration_frames:
                        detection_labels = detection_labels[:audio_duration_frames]
                        type_labels = type_labels[:audio_duration_frames]
                    else:
                        # Pad with no_bc
                        detection_padding = torch.zeros(audio_duration_frames - len(detection_labels), dtype=torch.long)
                        type_padding = torch.full((audio_duration_frames - len(type_labels),), -1, dtype=torch.long)
                        detection_labels = torch.cat([detection_labels, detection_padding])
                        type_labels = torch.cat([type_labels, type_padding])
                
                print(f"   ✅ Loaded {len(bc_labels)} {speaker_type} BC frames")
                return {
                    'bc_detection': detection_labels,
                    'bc_type': type_labels
                }
                
            except Exception as e:
                print(f"❌ Error loading {speaker_type} BC ground truth: {e}")
                return {
                    'bc_detection': torch.full((audio_duration_frames,), 0, dtype=torch.long),
                    'bc_type': torch.full((audio_duration_frames,), -1, dtype=torch.long)
                }
        
        def process_hn_labels(csv_path: str, speaker_type: str) -> torch.Tensor:
            """Process HN labels for a single speaker"""
            if not csv_path or not os.path.exists(csv_path):
                return torch.full((audio_duration_frames,), -1, dtype=torch.long)
            
            try:
                print(f"📊 Loading {speaker_type} HN ground truth from: {csv_path}")
                hn_df = pd.read_csv(csv_path)
                
                if 'head_nod_class' in hn_df.columns:
                    hn_labels = torch.tensor(hn_df['head_nod_class'].values, dtype=torch.long)
                else:
                    print(f"⚠️  Warning: 'head_nod_class' column not found in {csv_path}")
                    hn_labels = torch.full((audio_duration_frames,), -1, dtype=torch.long)
                
                # Process HN labels: 0=no_nod, 1=short, 2=long, 3=long_p
                # Convert to 3-class format: 0=short, 1=long, 2=long_p, -1=no_nod
                no_nod_mask = hn_labels == 0
                short_mask = hn_labels == 1
                long_mask = hn_labels == 2
                long_p_mask = hn_labels == 3
                
                processed_labels = torch.full_like(hn_labels, -1)
                processed_labels[short_mask] = 0
                processed_labels[long_mask] = 1
                processed_labels[long_p_mask] = 2
                
                # Ensure correct length
                if len(hn_labels) != audio_duration_frames:
                    if len(hn_labels) > audio_duration_frames:
                        processed_labels = processed_labels[:audio_duration_frames]
                    else:
                        # Pad with no_nod
                        padding = torch.full((audio_duration_frames - len(processed_labels),), -1, dtype=torch.long)
                        processed_labels = torch.cat([processed_labels, padding])
                
                print(f"   ✅ Loaded {len(hn_labels)} {speaker_type} HN frames")
                return processed_labels
                
            except Exception as e:
                print(f"❌ Error loading {speaker_type} HN ground truth: {e}")
                return torch.full((audio_duration_frames,), -1, dtype=torch.long)
        
        # Load BC ground truth for both speakers
        gt_labels['bc_expert'] = process_bc_labels(bc_expert_csv, 'expert')
        
        # Load HN ground truth for both speakers
        gt_labels['hn_expert'] = process_hn_labels(hn_expert_csv, 'expert')
        
        return gt_labels
    
    def _process_audio_chunks(self, audio1: torch.Tensor, audio2: torch.Tensor,
                             multimodal_paths: Dict[str, Dict[str, str]] = None,
                             chunk_duration: float = 5.0,
                             stride_duration: float = 1.0) -> Dict:
        """
        Process audio with streaming-style inference using sliding context window
        
        Strategy:
        - Each inference uses chunk_duration of context (e.g., 5s)
        - Move forward by stride_duration between inferences (e.g., 1.0s)
        - Overlap predictions are aggregated for smoothness
        - Selective prediction reduces computation
        
        Args:
            audio1: Speaker 1 audio tensor
            audio2: Speaker 2 audio tensor  
            multimodal_paths: Multimodal data paths
            chunk_duration: Context duration for each inference (seconds)
            stride_duration: Time between consecutive inferences (seconds)
            
        Returns:
            Combined results from all chunks with aggregated overlaps
        """
        # Calculate parameters
        sample_rate = self.conf.sample_rate
        frame_hz = self.conf.frame_hz
        chunk_samples = int(chunk_duration * sample_rate)
        stride_samples = int(stride_duration * sample_rate)
        total_duration = len(audio1) / sample_rate
        
        print(f"📊 Streaming inference configuration:")
        print(f"   Total duration: {total_duration:.1f}s")
        print(f"   Context size: {chunk_duration}s ({int(chunk_duration * frame_hz)} frames)")
        print(f"   Stride: {stride_duration}s ({int(stride_duration * frame_hz)} frames)")
        
        # Initialize result containers
        all_logits = []
        all_vap_probs_p_now = []
        all_vap_probs_p_future = []
        all_bc_predictions = [] if self.conf.predict_backchannels else None
        all_hn_predictions = [] if self.conf.predict_head_nods else None
        
        # Calculate number of chunks needed
        # Each chunk starts at stride * idx, so we need enough chunks to cover the audio
        num_chunks = int(np.ceil((len(audio1) - chunk_samples + stride_samples) / stride_samples))
        
        # Timing statistics
        chunk_times = []
        forward_pass_times = []
        per_frame_times = []
        
        for chunk_idx in range(num_chunks):
            chunk_start_time = time.time()
            
            # Calculate chunk boundaries with overlap
            start_sample = chunk_idx * stride_samples
            end_sample = min(start_sample + chunk_samples, len(audio1))
            
            # Extract chunk
            audio1_chunk = audio1[start_sample:end_sample]
            audio2_chunk = audio2[start_sample:end_sample]
            
            chunk_duration_actual = (end_sample - start_sample) / sample_rate
            num_frames_in_chunk = int(chunk_duration_actual * self.conf.frame_hz)
            
            print(f"   Processing chunk {chunk_idx + 1}/{num_chunks} ({chunk_duration_actual:.1f}s, {num_frames_in_chunk} frames)...")
            
            # Prepare batch for this chunk
            batch_prep_start = time.time()
            batch = self.prepare_batch(audio1_chunk, audio2_chunk, multimodal_paths)
            batch_prep_time = time.time() - batch_prep_start
            
            # Run inference on chunk
            try:
                # Measure forward pass time
                forward_start = time.time()
                with torch.no_grad():
                    output = self.model(batch)
                forward_time = time.time() - forward_start
                forward_pass_times.append(forward_time)
                
                # Calculate per-frame inference time
                per_frame_time = forward_time / num_frames_in_chunk
                per_frame_times.append(per_frame_time)
                
                print(f"      ⏱️  Batch prep: {batch_prep_time*1000:.2f}ms")
                print(f"      ⏱️  Forward pass: {forward_time*1000:.2f}ms")
                print(f"      ⏱️  Per frame: {per_frame_time*1000:.2f}ms ({1/per_frame_time:.1f} fps)")
                print(f"      ⏱️  Real-time factor: {chunk_duration_actual/forward_time:.2f}x")
                
                # Check if we can process in real-time
                frame_duration = 1.0 / self.conf.frame_hz  # 40ms @ 25Hz
                if per_frame_time <= frame_duration:
                    print(f"      ✅ Real-time capable: {per_frame_time*1000:.1f}ms < {frame_duration*1000:.1f}ms per frame")
                else:
                    print(f"      ⚠️  Not real-time: {per_frame_time*1000:.1f}ms > {frame_duration*1000:.1f}ms per frame")
                    print(f"         → Can process every {int(per_frame_time/frame_duration)+1} frames")
                
                # Extract predictions
                logits = output['logits']  # [1, T, n_classes]
                vap_probs = self.model.objective.get_probs(logits)
                
                # Store results
                all_logits.append(logits.cpu().numpy().squeeze(0))  # [T, n_classes]
                all_vap_probs_p_now.append(vap_probs['p_now'].cpu().numpy().squeeze(0))  # [T, 4]
                all_vap_probs_p_future.append(vap_probs['p_future'].cpu().numpy().squeeze(0))  # [T, 4]
                
                # Extract BC predictions if available - TWO-HEAD CASCADE
                if self.conf.predict_backchannels:
                    print(f"🔍 Debug: BC prediction enabled. Looking for BC outputs...")
                    print(f"   Available output keys: {list(output.keys())}")
                    
                    # Check for old single-head BC outputs
                    if 'backchannel_logits' in output:
                        print(f"   Found OLD single-head BC output: 'backchannel_logits'")
                    if 'backchannel_probs' in output:
                        print(f"   Found OLD single-head BC output: 'backchannel_probs'")
                    
                    # Check for new two-head BC outputs
                    if 'bc_detection_logits' in output:
                        print(f"   Found NEW two-head BC output: 'bc_detection_logits'")
                    if 'bc_type_logits' in output:
                        print(f"   Found NEW two-head BC output: 'bc_type_logits'")
                
                # Try NEW two-head BC architecture first
                if self.conf.predict_backchannels and 'bc_detection_logits' in output:
                    # Stage 1: BC Detection (no_bc vs any_bc)
                    bc_detection_logits = output['bc_detection_logits']  # [1, T, 2]
                    bc_detection_probs = F.softmax(bc_detection_logits, dim=-1)
                    
                    # Stage 2: BC Type Classification (reactive vs emotional)
                    bc_type_logits = output['bc_type_logits']  # [1, T, 2]
                    bc_type_probs = F.softmax(bc_type_logits, dim=-1)
                    
                    # Cascade prediction with threshold on P(any_bc)
                    prob_any_bc = bc_detection_probs[..., 1]  # [1, T]
                    threshold_bc = getattr(self, 'bc_detection_threshold', 0.5)
                    has_bc = prob_any_bc >= threshold_bc  # [1, T]
                    
                    # Initialize predictions as -1 (no_bc)
                    predicted_classes = torch.full((bc_type_probs.shape[0], bc_type_probs.shape[1]), -1, dtype=torch.long)
                    
                    # Stage 2: Where BC detected, classify type (0=reactive, 1=emotional)
                    if has_bc.any():
                        type_preds = torch.argmax(bc_type_probs, dim=-1)  # [1, T]
                        predicted_classes[has_bc] = type_preds[has_bc]
                    
                    # Store predictions with both stages' information
                    bc_pred = {
                        'detection_logits': bc_detection_logits.cpu().numpy().squeeze(0),  # [T, 2]
                        'detection_probs': bc_detection_probs.cpu().numpy().squeeze(0),  # [T, 2]
                        'type_logits': bc_type_logits.cpu().numpy().squeeze(0),  # [T, 2]
                        'type_probs': bc_type_probs.cpu().numpy().squeeze(0),  # [T, 2]
                        'predicted_classes': predicted_classes.cpu().numpy().squeeze(0),  # [T]
                        'detection_threshold': float(threshold_bc),
                        # Legacy compatibility
                        'logits': bc_type_logits.cpu().numpy().squeeze(0),
                        'probs': bc_type_probs.cpu().numpy().squeeze(0),
                    }
                    all_bc_predictions.append(bc_pred)
                
                # Fallback: Try OLD single-head BC architecture
                elif self.conf.predict_backchannels and 'backchannel_logits' in output:
                    print(f"   Using OLD single-head BC architecture")
                    bc_logits = output['backchannel_logits']  # [1, T, 3] - old format
                    bc_probs = F.softmax(bc_logits, dim=-1)
                    
                    # Convert old 3-class to new 2-class format
                    # Old: [no_bc, reactive, emotional] -> New: detection + type
                    no_bc_probs = bc_probs[:, :, 0]  # [1, T]
                    any_bc_probs = bc_probs[:, :, 1:3].sum(dim=-1)  # [1, T] - reactive + emotional
                    
                    # Create detection probabilities
                    detection_probs = torch.stack([no_bc_probs, any_bc_probs], dim=-1)  # [1, T, 2]
                    
                    # Create type probabilities (normalize reactive + emotional)
                    reactive_probs = bc_probs[:, :, 1]  # [1, T]
                    emotional_probs = bc_probs[:, :, 2]  # [1, T]
                    type_probs = torch.stack([reactive_probs, emotional_probs], dim=-1)  # [1, T, 2]
                    
                    # Cascade prediction using old format
                    detection_preds = torch.argmax(detection_probs, dim=-1)  # [1, T]
                    has_bc = detection_preds == 1
                    
                    predicted_classes = torch.full((bc_probs.shape[0], bc_probs.shape[1]), -1, dtype=torch.long)
                    if has_bc.any():
                        type_preds = torch.argmax(type_probs, dim=-1)
                        predicted_classes[has_bc] = type_preds[has_bc]
                    
                    # Store in new format
                    bc_pred = {
                        'detection_logits': detection_probs.cpu().numpy().squeeze(0),  # [T, 2]
                        'detection_probs': detection_probs.cpu().numpy().squeeze(0),  # [T, 2]
                        'type_logits': type_probs.cpu().numpy().squeeze(0),  # [T, 2]
                        'type_probs': type_probs.cpu().numpy().squeeze(0),  # [T, 2]
                        'predicted_classes': predicted_classes.cpu().numpy().squeeze(0),  # [T]
                        # Legacy compatibility
                        'logits': type_probs.cpu().numpy().squeeze(0),
                        'probs': type_probs.cpu().numpy().squeeze(0),
                    }
                    all_bc_predictions.append(bc_pred)
                
                # Extract head nod predictions if available - TWO-HEAD ARCHITECTURE
                if self.conf.predict_head_nods and 'hn_detection_logits' in output:
                    # Stage 1: HN Detection (no_nod vs any_nod)
                    hn_detection_logits = output['hn_detection_logits']
                    hn_detection_probs = F.softmax(hn_detection_logits, dim=-1)
                    
                    # Stage 2: HN Type Classification (short, long, long_p)
                    hn_type_logits = output['hn_type_logits']
                    hn_type_probs = F.softmax(hn_type_logits, dim=-1)
                    
                    # Cascade prediction with threshold on P(any_nod)
                    prob_any_hn = hn_detection_probs[..., 1]  # [B, T]
                    threshold_hn = getattr(self, 'hn_detection_threshold', 0.5)
                    has_hn = prob_any_hn >= threshold_hn  # boolean mask
                    
                    # Get type predictions
                    type_preds = torch.argmax(hn_type_probs, dim=-1)  # [B, T]
                    
                    # Combine: use type prediction only for detected HN frames
                    predicted_classes = torch.full_like(type_preds, -1)  # Start with all -1 (no_nod)
                    predicted_classes[has_hn] = type_preds[has_hn]
                    
                    # Store in new format
                    hn_pred = {
                        'detection_logits': hn_detection_logits.cpu().numpy().squeeze(0),  # [T, 2]
                        'detection_probs': hn_detection_probs.cpu().numpy().squeeze(0),  # [T, 2]
                        'type_logits': hn_type_probs.cpu().numpy().squeeze(0),  # [T, 3]
                        'type_probs': hn_type_probs.cpu().numpy().squeeze(0),  # [T, 3]
                        'predicted_classes': predicted_classes.cpu().numpy().squeeze(0),  # [T]
                        'detection_threshold': float(threshold_hn),
                        # Legacy compatibility
                        'logits': hn_type_probs.cpu().numpy().squeeze(0),
                        'probs': hn_type_probs.cpu().numpy().squeeze(0),
                    }
                    all_hn_predictions.append(hn_pred)
                
                # Record total chunk processing time
                chunk_time = time.time() - chunk_start_time
                chunk_times.append(chunk_time)
                print(f"      ⏱️  Total chunk time: {chunk_time*1000:.2f}ms\n")
                
            except Exception as e:
                print(f"❌ Error processing chunk {chunk_idx + 1}: {e}")
                # Continue with next chunk
                continue
        
        # Combine results from all chunks
        print("🔗 Combining results from overlapping chunks...")
        
        # Calculate frames
        stride_frames = int(stride_duration * frame_hz)
        chunk_frames = int(chunk_duration * frame_hz)
        overlap_frames = chunk_frames - stride_frames  # Frames that overlap between chunks
        
        print(f"   📐 Aggregation strategy:")
        print(f"   - Chunk size: {chunk_frames} frames ({chunk_duration:.1f}s)")
        print(f"   - Stride: {stride_frames} frames ({stride_duration:.1f}s)")
        print(f"   - Overlap: {overlap_frames} frames ({overlap_frames / frame_hz:.1f}s)")
        print(f"   - VAP: Simple stitching (keeps stride regions)")
        print(f"   - BC/HN: Probability averaging over overlaps (VAP-style smooth aggregation)")
        
        # Helper: Simple stitching for VAP (no aggregation - VAP handles this internally)
        def stitch_vap_predictions(predictions_list):
            """Stitch VAP predictions by keeping center regions (VAP-specific)"""
            if not predictions_list:
                return None
            
            if len(predictions_list) == 1:
                return predictions_list[0]
            
            stitched = []
            for i, pred in enumerate(predictions_list):
                if i == 0:
                    # First chunk: keep everything except last overlap/2
                    trim_end = overlap_frames // 2 if overlap_frames > 0 else 0
                    stitched.append(pred[:-trim_end] if trim_end > 0 else pred)
                elif i == len(predictions_list) - 1:
                    # Last chunk: keep everything except first overlap/2
                    trim_start = overlap_frames // 2 if overlap_frames > 0 else 0
                    stitched.append(pred[trim_start:])
                else:
                    # Middle chunks: trim both sides by overlap/2
                    trim = overlap_frames // 2 if overlap_frames > 0 else 0
                    if trim > 0:
                        stitched.append(pred[trim:-trim])
                    else:
                        stitched.append(pred)
            
            return np.concatenate(stitched, axis=0)
        
        # Helper: Aggregation for BC/HN (average overlapping predictions)
        def aggregate_bc_hn_predictions(predictions_list):
            """
            Aggregate BC/HN predictions by AVERAGING overlapping regions.
            
            This mimics VAP's aggregation philosophy:
            - Each frame predicts t+shift (shift configured in model, e.g., 500ms, 2000ms)
            - Multiple chunks may predict the same target time
            - Average their probabilities for smoother, more robust predictions
            """
            if not predictions_list:
                return None
            
            if len(predictions_list) == 1:
                return predictions_list[0]
            
            # Calculate total output length
            total_frames = stride_frames * (len(predictions_list) - 1) + len(predictions_list[-1])
            
            # For probabilistic predictions (detection_probs, type_probs)
            if predictions_list[0].ndim == 2:  # [T, C]
                num_classes = predictions_list[0].shape[1]
                aggregated = np.zeros((total_frames, num_classes), dtype=np.float32)
                counts = np.zeros((total_frames, 1), dtype=np.float32)
                
                for chunk_idx, chunk_pred in enumerate(predictions_list):
                    start_frame = chunk_idx * stride_frames
                    end_frame = start_frame + len(chunk_pred)
                    
                    # Accumulate predictions
                    aggregated[start_frame:end_frame] += chunk_pred
                    counts[start_frame:end_frame] += 1
                
                # Average where there are overlaps
                aggregated = aggregated / np.maximum(counts, 1)
                return aggregated
            
            # For class predictions (predicted_classes) - take most recent
            elif predictions_list[0].ndim == 1:  # [T]
                aggregated = np.full(total_frames, -1, dtype=predictions_list[0].dtype)
                
                for chunk_idx, chunk_pred in enumerate(predictions_list):
                    start_frame = chunk_idx * stride_frames
                    end_frame = start_frame + len(chunk_pred)
                    
                    # For overlaps, later chunks override (they have more context)
                    aggregated[start_frame:end_frame] = chunk_pred
                
                return aggregated
            
            else:
                raise ValueError(f"Unexpected prediction shape: {predictions_list[0].shape}")
        
        # Stitch VAP predictions (unchanged - VAP handles aggregation internally)
        print("   📊 Stitching VAP predictions (standard approach)...")
        combined_logits = stitch_vap_predictions(all_logits)
        combined_vap_p_now = stitch_vap_predictions(all_vap_probs_p_now)
        combined_vap_p_future = stitch_vap_predictions(all_vap_probs_p_future)
        
        # Aggregate BC predictions with overlapping averaging
        combined_bc_predictions = None
        if all_bc_predictions:
            print("   🔄 Aggregating BC predictions (averaging overlaps for smooth curves)...")
            combined_bc_predictions = {
                'detection_logits': aggregate_bc_hn_predictions([pred['detection_logits'] for pred in all_bc_predictions]),
                'detection_probs': aggregate_bc_hn_predictions([pred['detection_probs'] for pred in all_bc_predictions]),
                'type_logits': aggregate_bc_hn_predictions([pred['type_logits'] for pred in all_bc_predictions]),
                'type_probs': aggregate_bc_hn_predictions([pred['type_probs'] for pred in all_bc_predictions]),
                'predicted_classes': aggregate_bc_hn_predictions([pred['predicted_classes'] for pred in all_bc_predictions]),
                # Legacy compatibility
                'logits': aggregate_bc_hn_predictions([pred['logits'] for pred in all_bc_predictions]),
                'probs': aggregate_bc_hn_predictions([pred['probs'] for pred in all_bc_predictions]),
            }
            print(f"      ✅ BC predictions aggregated: {combined_bc_predictions['detection_probs'].shape}")
        
        # Aggregate head nod predictions with overlapping averaging
        combined_hn_predictions = None
        if all_hn_predictions:
            print("   🔄 Aggregating HN predictions (averaging overlaps for smooth curves)...")
            combined_hn_predictions = {
                'detection_logits': aggregate_bc_hn_predictions([pred['detection_logits'] for pred in all_hn_predictions]),
                'detection_probs': aggregate_bc_hn_predictions([pred['detection_probs'] for pred in all_hn_predictions]),
                'type_logits': aggregate_bc_hn_predictions([pred['type_logits'] for pred in all_hn_predictions]),
                'type_probs': aggregate_bc_hn_predictions([pred['type_probs'] for pred in all_hn_predictions]),
                'predicted_classes': aggregate_bc_hn_predictions([pred['predicted_classes'] for pred in all_hn_predictions]),
                # Legacy compatibility
                'logits': aggregate_bc_hn_predictions([pred['logits'] for pred in all_hn_predictions]),
                'probs': aggregate_bc_hn_predictions([pred['probs'] for pred in all_hn_predictions]),
            }
            print(f"      ✅ HN predictions aggregated: {combined_hn_predictions['detection_probs'].shape}")
        
        # Create time axis
        total_frames = combined_logits.shape[0]
        time_axis = np.linspace(0, total_duration, total_frames)
        
        # Ensure both audio arrays have the same length (use the shorter one)
        min_length = min(len(audio1), len(audio2))
        audio1_aligned = audio1[:min_length].cpu().numpy()
        audio2_aligned = audio2[:min_length].cpu().numpy()
        
        # Prepare final results
        results = {
            'audio1': audio1_aligned,
            'audio2': audio2_aligned,
            'time_axis': time_axis,
            'duration': total_duration,
            'sample_rate': self.conf.sample_rate,
            'frame_hz': self.conf.frame_hz,
            'vap_probs': {
                'p_now': combined_vap_p_now,  # [total_T, 4]
                'p_future': combined_vap_p_future,  # [total_T, 4]
            },
            'logits': combined_logits,  # [total_T, n_classes]
            'bc_predictions': combined_bc_predictions,
            'hn_predictions': combined_hn_predictions,
            'bc_detection_threshold': getattr(self, 'bc_detection_threshold', 0.5),
            'hn_detection_threshold': getattr(self, 'hn_detection_threshold', 0.5),
            'multimodal_used': multimodal_paths is not None and self.conf.multimodal,
        }
        
        # Print timing summary
        print(f"✅ Chunked inference complete!")
        print(f"   Total chunks: {num_chunks}")
        print(f"   Total frames: {total_frames}")
        print(f"   Total duration: {total_duration:.2f}s")
        
        if forward_pass_times:
            print(f"\n📊 Performance Statistics:")
            print(f"   Forward Pass Timing:")
            print(f"   - Mean: {np.mean(forward_pass_times)*1000:.2f}ms")
            print(f"   - Median: {np.median(forward_pass_times)*1000:.2f}ms")
            print(f"   - Min: {np.min(forward_pass_times)*1000:.2f}ms")
            print(f"   - Max: {np.max(forward_pass_times)*1000:.2f}ms")
            print(f"   - Std: {np.std(forward_pass_times)*1000:.2f}ms")
            
            print(f"\n   Per-Frame Timing:")
            mean_per_frame = np.mean(per_frame_times)
            frame_duration = 1.0 / self.conf.frame_hz
            print(f"   - Mean: {mean_per_frame*1000:.2f}ms/frame")
            print(f"   - Target: {frame_duration*1000:.2f}ms/frame ({self.conf.frame_hz} Hz)")
            print(f"   - FPS: {1/mean_per_frame:.1f} frames/sec")
            
            print(f"\n   Real-Time Analysis:")
            rtf = np.mean([chunk_duration / forward_pass_times[i] for i, chunk_duration in enumerate([chunk_duration]*len(forward_pass_times))])
            print(f"   - Real-time factor: {rtf:.2f}x")
            print(f"   - Processing speed: {rtf:.2f}x faster than real-time")
            
            if mean_per_frame <= frame_duration:
                print(f"   ✅ Model is REAL-TIME capable!")
                print(f"   - Can process frame-by-frame at {self.conf.frame_hz}Hz")
                print(f"   - Latency: {mean_per_frame*1000:.1f}ms per frame")
            else:
                frames_per_inference = int(np.ceil(mean_per_frame / frame_duration))
                print(f"   ⚠️  Model is NOT frame-by-frame real-time")
                print(f"   - Suggested mini-batch: {frames_per_inference} frames ({frames_per_inference * frame_duration * 1000:.0f}ms)")
                print(f"   - Effective rate: {self.conf.frame_hz / frames_per_inference:.1f}Hz updates")
            
            # BC/HN prediction timing
            print(f"\n   BC/HN Prediction Implications:")
            bc_shift_ms = getattr(self.conf, 'backchannel_prediction_shift_ms', 500)
            hn_shift_ms = getattr(self.conf, 'head_nod_prediction_shift_ms', 500)
            print(f"   - BC shift: {bc_shift_ms}ms ({bc_shift_ms/1000:.1f}s) into future")
            print(f"   - HN shift: {hn_shift_ms}ms ({hn_shift_ms/1000:.1f}s) into future")
            print(f"   - Frames in BC shift: {int(bc_shift_ms * self.conf.frame_hz / 1000)} frames")
            print(f"   - Frames in HN shift: {int(hn_shift_ms * self.conf.frame_hz / 1000)} frames")
            if mean_per_frame <= frame_duration:
                print(f"   - Can provide continuous BC/HN predictions every {frame_duration*1000:.0f}ms")
            else:
                print(f"   - Can provide BC/HN predictions every {frames_per_inference * frame_duration * 1000:.0f}ms")
                print(f"   - BC predictions per shift window: {int(bc_shift_ms / (frames_per_inference * frame_duration * 1000))}")
                print(f"   - HN predictions per shift window: {int(hn_shift_ms / (frames_per_inference * frame_duration * 1000))}")
        
        return results


class VAPVisualizer:
    """
    Visualization system for VAP predictions
    """
    
    def __init__(self, figsize: Tuple[int, int] = (15, 10)):
        """
        Initialize visualizer
        
        Args:
            figsize: Figure size for plots
        """
        self.figsize = figsize
        
    def plot_audio_and_vap(self, results: Dict, save_path: Optional[str] = None, 
                          bc_visualization: str = 'category', hn_visualization: str = 'category'):
        """
        Create comprehensive visualization of audio and VAP predictions
        
        Args:
            results: Dictionary containing inference results
            save_path: Optional path to save the plot
            bc_visualization: 'probability' or 'category' for BC visualization mode
            hn_visualization: 'probability' or 'category' for head nod visualization mode
        
        Args:
            results: Results dictionary from inference
            save_path: Optional path to save the plot
        """
        # Determine number of subplots based on available predictions and ground truth
        n_plots = 4  # Base: audio1, audio2, p_now, p_future
        
        # BC plots: detection + type predictions, plus separate ground truth plots
        if results.get('bc_predictions') is not None:
            n_plots += 2  # BC detection + BC type predictions
            # if 'ground_truth' in results:
            #     if 'bc_expert' in results['ground_truth']:
            #         n_plots += 1  # BC expert ground truth
        
        # HN plots: detection + type predictions (two-head), plus separate ground truth plots
        if results.get('hn_predictions') is not None:
            # Check if two-head architecture (detection + type) or legacy single-head
            if 'detection_probs' in results['hn_predictions']:
                n_plots += 2  # HN detection + HN type predictions (two-head architecture)
            else:
                n_plots += 1  # HN predictions (legacy single-head)
            
            # if 'ground_truth' in results:
            #     if 'hn_expert' in results['ground_truth']:
            #         n_plots += 1  # HN expert ground truth
            
        # Adjust figure size based on number of plots
        figsize = (self.figsize[0], self.figsize[1] * n_plots / 4)
        fig, axes = plt.subplots(n_plots, 1, figsize=figsize, sharex=True)
        
        # Debug: Print visualization modes
        print(f"🎨 Visualization Modes:")
        print(f"   BC Mode: {bc_visualization}")
        print(f"   HN Mode: {hn_visualization}")
        
        # # Check if ground truth is available
        # if 'ground_truth' in results:
        #     print(f"📊 Ground truth will be plotted alongside predictions")
        
        audio1 = results['audio1']
        audio2 = results['audio2']
        time_axis = results['time_axis']
        vap_probs = results['vap_probs']
        duration = results['duration']
        
        # Create audio time axes for each speaker
        audio_time1 = np.linspace(0, len(audio1) / results['sample_rate'], len(audio1))
        audio_time2 = np.linspace(0, len(audio2) / results['sample_rate'], len(audio2))
        
        # Plot 1: Speaker 1 waveform
        axes[0].plot(audio_time1, audio1, color='blue', alpha=0.7, linewidth=0.5)
        axes[0].set_ylabel('Speaker 1\nAmplitude', fontsize=10)
        axes[0].set_title('Audio Waveforms and VAP Predictions', fontsize=14, fontweight='bold')
        axes[0].grid(True, alpha=0.3)
        
        # Plot 2: Speaker 2 waveform
        axes[1].plot(audio_time2, audio2, color='red', alpha=0.7, linewidth=0.5)
        axes[1].set_ylabel('Speaker 2\nAmplitude', fontsize=10)
        axes[1].grid(True, alpha=0.3)
        
        # Plot 3: VAP P_now (current turn-taking probabilities)
        p_now = vap_probs['p_now']  # [T, 4]
        
        # VAP classes: [hold, shift, backchannel, silence] (typical VAP setup)
        vap_labels = ['Hold', 'Shift', 'Backchannel', 'Silence']
        colors = ['green', 'orange', 'purple', 'gray']
        
        for i, (label, color) in enumerate(zip(vap_labels, colors)):
            if i < p_now.shape[1]:
                axes[2].plot(time_axis, p_now[:, i], label=label, color=color, linewidth=1.5)
        
        axes[2].set_ylabel('P_now\nProbability', fontsize=10)
        axes[2].set_ylim(0, 1)
        axes[2].legend(loc='upper right', fontsize=8)
        axes[2].grid(True, alpha=0.3)
        
        # Plot 4: VAP P_future (future turn-taking probabilities)
        p_future = vap_probs['p_future']  # [T, 4]
        
        for i, (label, color) in enumerate(zip(vap_labels, colors)):
            if i < p_future.shape[1]:
                axes[3].plot(time_axis, p_future[:, i], label=label, color=color, linewidth=1.5)
        
        axes[3].set_ylabel('P_future\nProbability', fontsize=10)
        axes[3].set_ylim(0, 1)
        axes[3].legend(loc='upper right', fontsize=8)
        axes[3].grid(True, alpha=0.3)
        
        # Current plot index for additional predictions
        plot_idx = 4
        
        # Plot 5 & 6: Two-Head Backchannel Predictions (if available)
        if results.get('bc_predictions') is not None:
            bc_preds = results['bc_predictions']
            predicted_classes = bc_preds['predicted_classes']  # [T] - frame-wise predictions
            
            # Two-head architecture: separate detection and type probabilities
            detection_probs = bc_preds.get('detection_probs')  # [T, 2] - [no_bc, any_bc]
            type_probs = bc_preds.get('type_probs')  # [T, 2] - [reactive, emotional]
            
            # Debug: Print prediction statistics
            unique_classes, counts = np.unique(predicted_classes, return_counts=True)
            print(f"🔍 BC Prediction Statistics:")
            for cls, count in zip(unique_classes, counts):
                if cls == -1:
                    print(f"   No BC (-1): {count} frames ({count/len(predicted_classes)*100:.1f}%)")
                elif cls == 0:
                    print(f"   Reactive BC (0): {count} frames ({count/len(predicted_classes)*100:.1f}%)")
                elif cls == 1:
                    print(f"   Emotional BC (1): {count} frames ({count/len(predicted_classes)*100:.1f}%)")
            
            # PLOT 5: BC Detection (Probability of any_bc) - Predictions only
            if detection_probs is not None:
                # Plot both probabilities
                axes[plot_idx].plot(time_axis, detection_probs[:, 0], label='P(no_bc)', color='gray', linewidth=1.5, alpha=0.7)
                axes[plot_idx].plot(time_axis, detection_probs[:, 1], label='P(any_bc)', color='purple', linewidth=1.5)
                # Threshold line if available
                thr = results.get('bc_detection_threshold', None)
                if thr is not None:
                    axes[plot_idx].axhline(y=thr, color='red', linestyle='--', linewidth=1.0, alpha=0.7, label=f'Th={thr:.2f}')
                
                axes[plot_idx].set_ylabel('BC Detection\nPrediction', fontsize=10)
                axes[plot_idx].set_ylim(0, 1)
                axes[plot_idx].legend(loc='upper right', fontsize=8)
                axes[plot_idx].grid(True, alpha=0.3)
                axes[plot_idx].set_xlim(0, duration)
                plot_idx += 1
            
            # PLOT 6: BC Type Classification - Predictions only
            if type_probs is not None:
                bc_labels = ['Reactive BC', 'Emotional BC']
                bc_colors = ['gold', 'dodgerblue']
                
                # CATEGORY MODE: Show predicted classes as vertical lines (only where BC detected)
                for frame_idx, pred_class in enumerate(predicted_classes):
                    if pred_class >= 0:  # Valid prediction (not -1 for no_bc)
                        time_point = time_axis[frame_idx]
                        color = bc_colors[pred_class]
                        axes[plot_idx].axvline(x=time_point, color=color, alpha=0.8, linewidth=1.5)
                
                # Create legend
                legend_elements = []
                for i, (label, color) in enumerate(zip(bc_labels, bc_colors)):
                    legend_elements.append(plt.Line2D([0], [0], color=color, linewidth=3, label=label))
                
                axes[plot_idx].set_ylabel('BC Type\nPrediction', fontsize=10)
                axes[plot_idx].set_ylim(0, 1)
                axes[plot_idx].legend(handles=legend_elements, loc='upper right', fontsize=8)
                axes[plot_idx].grid(True, alpha=0.3)
                axes[plot_idx].set_xlim(0, duration)
                plot_idx += 1
            
            # # PLOT 7: BC Expert Ground Truth (if available)
            # if 'ground_truth' in results and 'bc_expert' in results['ground_truth']:
            #     bc_labels = ['Reactive BC', 'Emotional BC']
            #     bc_colors = ['gold', 'dodgerblue']
            #    gt_expert_type = results['ground_truth']['bc_expert']['bc_type']
            #     
            #     for frame_idx, gt_class in enumerate(gt_expert_type):
            #         if gt_class >= 0:  # Valid ground truth (not -1 for no_bc)
            #             time_point = time_axis[frame_idx]
            #             color = bc_colors[gt_class]
            #             axes[plot_idx].axvline(x=time_point, color=color, alpha=0.8, linewidth=2)
            #     
            #     # Create legend
            #     legend_elements = []
            #     for i, (label, color) in enumerate(zip(bc_labels, bc_colors)):
            #         legend_elements.append(plt.Line2D([0], [0], color=color, linewidth=3, label=label))
            #     
            #     axes[plot_idx].set_ylabel('BC Expert\nGround Truth', fontsize=10)
            #     axes[plot_idx].set_ylim(0, 1)
            #     axes[plot_idx].legend(handles=legend_elements, loc='upper right', fontsize=8)
            #     axes[plot_idx].grid(True, alpha=0.3)
            #     axes[plot_idx].set_xlim(0, duration)
            #     plot_idx += 1
            
           
        
        # Plot: Head Nod Detection Prediction (Two-Head Architecture)
        if results.get('hn_predictions') is not None:
            hn_preds = results['hn_predictions']
            predicted_classes = hn_preds['predicted_classes']  # [T] - frame-wise predictions
            
            # Debug: Print prediction statistics
            unique_classes, counts = np.unique(predicted_classes, return_counts=True)
            print(f"🔍 Head Nod Prediction Statistics:")
            for cls, count in zip(unique_classes, counts):
                if cls == -1:
                    print(f"   No Nod (-1): {count} frames ({count/len(predicted_classes)*100:.1f}%)")
                elif cls == 0:
                    print(f"   Short Nod (0): {count} frames ({count/len(predicted_classes)*100:.1f}%)")
                elif cls == 1:
                    print(f"   Long Nod (1): {count} frames ({count/len(predicted_classes)*100:.1f}%)")
                elif cls == 2:
                    print(f"   Long Nod+ (2): {count} frames ({count/len(predicted_classes)*100:.1f}%)")
            
            # Check if we have two-head architecture outputs
            if 'detection_probs' in hn_preds and 'type_probs' in hn_preds:
                # TWO-HEAD ARCHITECTURE
                hn_detection_probs = hn_preds['detection_probs']  # [T, 2]
                hn_type_probs = hn_preds['type_probs']  # [T, 3]
                
                # Plot 1: HN Detection Probability (P(any_nod))
                detection_prob_any_nod = hn_detection_probs[:, 1]  # Probability of any_nod
                axes[plot_idx].plot(time_axis, detection_prob_any_nod, color='purple', 
                                   linewidth=1.5, alpha=0.8, label='P(any_nod)')
                # Threshold line if available
                thr_hn = results.get('hn_detection_threshold', None)
                if thr_hn is not None:
                    axes[plot_idx].axhline(y=thr_hn, color='red', linestyle='--', linewidth=1.0, alpha=0.7, label=f'Th={thr_hn:.2f}')
                axes[plot_idx].set_ylabel('HN Detection\nP(any_nod)', fontsize=10)
                axes[plot_idx].set_ylim(0, 1)
                axes[plot_idx].legend(loc='upper right', fontsize=8)
                axes[plot_idx].grid(True, alpha=0.3)
                axes[plot_idx].set_xlim(0, duration)
                plot_idx += 1
                
                # Plot 2: HN Type Classification - show category predictions as vertical lines
                hn_labels = ['Short Nod', 'Long Nod', 'Long Nod+']
                hn_colors = ['gold', 'dodgerblue', 'limegreen']
                
                if hn_visualization == 'probability':
                    # PROBABILITY MODE: Show type probability curves
                    for class_idx in range(hn_type_probs.shape[1]):  # 0=short, 1=long, 2=long_p
                        prob_curve = hn_type_probs[:, class_idx]
                        axes[plot_idx].plot(time_axis, prob_curve, color=hn_colors[class_idx], 
                                           linewidth=1.5, alpha=0.8, label=hn_labels[class_idx])
                    
                    axes[plot_idx].set_ylabel('HN Type\nProbability', fontsize=10)
                    axes[plot_idx].set_ylim(0, 1)
                    axes[plot_idx].legend(loc='upper right', fontsize=8)
                    axes[plot_idx].grid(True, alpha=0.3)
                    axes[plot_idx].set_xlim(0, duration)
                else:
                    # CATEGORY MODE: Show predicted classes as vertical lines
                    for frame_idx, pred_class in enumerate(predicted_classes):
                        if pred_class >= 0:  # Valid prediction (not -1 for no_nod)
                            time_point = time_axis[frame_idx]
                            color = hn_colors[pred_class]
                            axes[plot_idx].axvline(x=time_point, color=color, alpha=0.8, linewidth=1.5)
                    
                    # Create legend
                    legend_elements = []
                    for i, (label, color) in enumerate(zip(hn_labels, hn_colors)):
                        legend_elements.append(plt.Line2D([0], [0], color=color, linewidth=3, label=label))
                    
                    axes[plot_idx].set_ylabel('HN Type\nPrediction', fontsize=10)
                    axes[plot_idx].set_ylim(0, 1)
                    axes[plot_idx].legend(handles=legend_elements, loc='upper right', fontsize=8)
                    axes[plot_idx].grid(True, alpha=0.3)
                    axes[plot_idx].set_xlim(0, duration)
                
                plot_idx += 1
            else:
                # LEGACY SINGLE-HEAD ARCHITECTURE (backward compatibility)
                hn_probs = hn_preds['probs']  # [T, hn_classes]
                hn_labels = ['Short Nod', 'Long Nod', 'Long Nod+']
                hn_colors = ['gold', 'dodgerblue', 'limegreen']
                
                if hn_visualization == 'probability':
                    # PROBABILITY MODE: Show probability curves for each class
                    for class_idx in range(hn_probs.shape[1]):  # 0=short, 1=long, 2=long_p
                        prob_curve = hn_probs[:, class_idx]
                        axes[plot_idx].plot(time_axis, prob_curve, color=hn_colors[class_idx], 
                                           linewidth=1.5, alpha=0.8, label=hn_labels[class_idx])
                    
                    axes[plot_idx].set_ylabel('Head Nod\nProbability', fontsize=10)
                    axes[plot_idx].set_ylim(0, 1)
                    axes[plot_idx].legend(loc='upper right', fontsize=8)
                    axes[plot_idx].grid(True, alpha=0.3)
                    axes[plot_idx].set_xlim(0, duration)
                else:
                    # CATEGORY MODE: Show predicted classes as vertical lines
                    for frame_idx, pred_class in enumerate(predicted_classes):
                        if pred_class >= 0:  # Valid prediction (not -1 for no_nod)
                            time_point = time_axis[frame_idx]
                            color = hn_colors[pred_class]
                            axes[plot_idx].axvline(x=time_point, color=color, alpha=0.8, linewidth=1.5)
                    
                    # Create legend
                    legend_elements = []
                    for i, (label, color) in enumerate(zip(hn_labels, hn_colors)):
                        legend_elements.append(plt.Line2D([0], [0], color=color, linewidth=3, label=label))
                    
                    axes[plot_idx].set_ylabel('Head Nod\nPrediction', fontsize=10)
                    axes[plot_idx].set_ylim(0, 1)
                    axes[plot_idx].legend(handles=legend_elements, loc='upper right', fontsize=8)
                    axes[plot_idx].grid(True, alpha=0.3)
                    axes[plot_idx].set_xlim(0, duration)
                
                plot_idx += 1
        
        # # Plot: Head Nod Expert Ground Truth (if available)
        # if results.get('hn_predictions') is not None and 'ground_truth' in results and 'hn_expert' in results['ground_truth']:
        #     hn_labels = ['Short Nod', 'Long Nod', 'Long Nod+']
        #     hn_colors = ['gold', 'dodgerblue', 'limegreen']
        #     gt_expert_hn = results['ground_truth']['hn_expert']
        #     
        #     for frame_idx, gt_class in enumerate(gt_expert_hn):
        #         if gt_class >= 0:  # Valid ground truth (not -1 for no_nod)
        #             time_point = time_axis[frame_idx]
        #             color = hn_colors[gt_class]
        #             axes[plot_idx].axvline(x=time_point, color=color, alpha=0.8, linewidth=2)
        #     
        #     # Create legend
        #     legend_elements = []
        #     for i, (label, color) in enumerate(zip(hn_labels, hn_colors)):
        #         legend_elements.append(plt.Line2D([0], [0], color=color, linewidth=3, label=label))
        #     
        #     axes[plot_idx].set_ylabel('HN Expert\nGround Truth', fontsize=10)
        #     axes[plot_idx].set_ylim(0, 1)
        #     axes[plot_idx].legend(handles=legend_elements, loc='upper right', fontsize=8)
        #     axes[plot_idx].grid(True, alpha=0.3)
        #     axes[plot_idx].set_xlim(0, duration)
        #     plot_idx += 1
        
       
        
        # Set x-label on the last plot
        axes[-1].set_xlabel('Time (seconds)', fontsize=12)
        
        # Adjust layout
        plt.tight_layout()
        
        if save_path:
            plt.savefig(save_path, dpi=300, bbox_inches='tight')
            print(f"💾 Visualization saved to: {save_path}")
        
        plt.show()
    
    def _add_turn_events(self, axes: List, results: Dict):
        """
        Add turn-taking event markers to the plots
        
        Args:
            axes: List of matplotlib axes
            results: Results dictionary from inference
        """
        # Simple turn-taking detection based on P_now shift probability
        p_now = results['vap_probs']['p_now']
        time_axis = results['time_axis']
        
        # Detect shift events (simplified)
        shift_threshold = 0.5
        if p_now.shape[1] > 1:  # Ensure shift class exists
            shift_probs = p_now[:, 1]  # Shift class
            shift_events = time_axis[shift_probs > shift_threshold]
            
            # Add vertical lines for turn-taking events
            for ax in axes:
                for event_time in shift_events:
                    ax.axvline(x=event_time, color='red', linestyle='--', 
                              alpha=0.6, linewidth=1, label='Turn Shift' if ax == axes[0] else "")


def main():
    """Main function for offline VAP inference and visualization"""
    parser = argparse.ArgumentParser(description='Offline VAP Inference and Visualization')
    parser.add_argument('--audio1', type=str, required=True, 
                       help='Path to speaker 1 audio file')
    parser.add_argument('--audio2', type=str, required=True,
                       help='Path to speaker 2 audio file')
    parser.add_argument('--model_path', type=str, required=True,
                       help='Path to trained VAP model checkpoint')
    parser.add_argument('--output_dir', type=str, default='output',
                       help='Output directory for visualizations')
    parser.add_argument('--device', type=str, default='cuda',
                       help='Device for inference (cuda/cpu)')
    parser.add_argument('--animated', action='store_true',
                       help='Create animated real-time visualization')
    parser.add_argument('--playback_speed', type=float, default=1.0,
                       help='Animation playback speed (1.0 = real-time)')
    parser.add_argument('--save_mp4', action='store_true',
                       help='Save animation as MP4 video')
    parser.add_argument('--save_gif', action='store_true',
                       help='Save animation as GIF')
    parser.add_argument('--static_only', action='store_true',
                       help='Create only static PNG visualization')
    parser.add_argument('--threaded', action='store_true',
                       help='Use threaded visualization (like VAP-Realtime)')
    parser.add_argument('--realtime', action='store_true',
                       help='True real-time processing with live inference')
    parser.add_argument('--chunk_duration', type=float, default=5.0,
                       help='Context duration for processing (seconds, default: 5s)')
    parser.add_argument('--stride_duration', type=float, default=1.0,
                       help='Stride duration between inferences (seconds, default: 1.0s for real-time streaming)')
    parser.add_argument('--selective_frames', type=int, default=-1,
                       help='Number of last frames to predict (default: -1 = auto-calculate from stride, 0 = all frames)')
    
    # Multimodal data arguments
    parser.add_argument('--face1', type=str, help='Path to speaker 1 face CSV file')
    parser.add_argument('--gaze1', type=str, help='Path to speaker 1 gaze CSV file')
    parser.add_argument('--body1', type=str, help='Path to speaker 1 body/head CSV file')
    parser.add_argument('--face2', type=str, help='Path to speaker 2 face CSV file')
    parser.add_argument('--gaze2', type=str, help='Path to speaker 2 gaze CSV file')
    parser.add_argument('--body2', type=str, help='Path to speaker 2 body/head CSV file')
    
    # Model configuration arguments
    parser.add_argument('--multimodal', action='store_true',
                       help='Enable multimodal features')
    parser.add_argument('--predict_backchannels', action='store_true',
                       help='Enable backchannel prediction')
    parser.add_argument('--predict_head_nods', action='store_true',
                       help='Enable head nod prediction')
    parser.add_argument('--bc_visualization', type=str, default='category', choices=['probability', 'category'],
                       help='BC visualization mode: probability (show prob curves) or category (show predicted classes)')
    parser.add_argument('--hn_visualization', type=str, default='category', choices=['probability', 'category'],
                       help='Head nod visualization mode: probability (show prob curves) or category (show predicted classes)')
    # Detection thresholds for cascade
    parser.add_argument('--bc_detection_threshold', type=float, default=0.5,
                       help='Threshold for P(any_bc) to trigger BC type classification (default: 0.5)')
    parser.add_argument('--hn_detection_threshold', type=float, default=0.5,
                       help='Threshold for P(any_nod) to trigger HN type classification (default: 0.5)')
    
    # Ground truth data arguments
    parser.add_argument('--bc_expert_csv', type=str, help='Path to expert backchannel ground truth CSV file')
    parser.add_argument('--hn_expert_csv', type=str, help='Path to expert head nod ground truth CSV file')
    
    parser.add_argument('--vap_use_whisper_semantics', action='store_true',
                       help='Enable Whisper semantic features')
    parser.add_argument('--vap_use_hubert_semantics', action='store_true',
                       help='Enable HuBERT semantic features')
    parser.add_argument('--vap_pretrained_whisper_model', type=str,
                       help='Whisper model name (e.g., openai/whisper-tiny.en)')
    parser.add_argument('--vap_pretrained_hubert_model', type=str,
                       help='HuBERT model name (e.g., facebook/hubert-base-ls960)')
    parser.add_argument('--vap_context_limit', type=float,
                       help='Context limit in seconds (e.g., 10, 5, 3)')
    parser.add_argument('--pretrained_face_encoder', type=str,
                       help='Path to pretrained face encoder model (FormerDFER)')
    
    args = parser.parse_args()
    
    # Create output directory
    output_dir = Path(args.output_dir)
    output_dir.mkdir(exist_ok=True)
    
    print("🚀 Starting VAP Inference System")
    print("=" * 50)
    
    # Check for real-time mode first
    if args.realtime:
        print("🔴 REAL-TIME MODE: Live inference with simultaneous visualization")
        try:
            from realtime_inference import create_realtime_system
            
            # Create and run real-time system
            audio_processor, vap_inference_rt, visualizer_rt = create_realtime_system(
                args.audio1, args.audio2, args.model_path,
                chunk_duration=args.chunk_duration,
                playback_speed=args.playback_speed,
                device=args.device
            )
            
            print("🎉 Real-time processing completed!")
            return
            
        except ImportError as e:
            print(f"❌ Real-time mode not available: {e}")
            print("📊 Falling back to offline mode...")
    
    try:
        # Build model config overrides - only include explicitly provided arguments
        model_config_overrides = {}
        
        # Handle vap_ prefixed arguments (matching training structure)
        vap_args = ['use_whisper_semantics', 'use_hubert_semantics', 'context_limit', 
                    'pretrained_whisper_model', 'pretrained_hubert_model']
        for arg in vap_args:
            vap_arg_name = f'vap_{arg}'
            if hasattr(args, vap_arg_name):
                value = getattr(args, vap_arg_name)
                # For boolean args (store_true), only override if explicitly set to True
                # For string/numeric args, only override if not None
                if (isinstance(value, bool) and value) or (not isinstance(value, bool) and value is not None):
                    model_config_overrides[arg] = value
        
        # Handle non-vap prefixed arguments  
        other_args = ['multimodal', 'predict_backchannels', 'predict_head_nods', 'pretrained_face_encoder']
        for arg in other_args:
            if hasattr(args, arg):
                value = getattr(args, arg)
                # For boolean args (store_true), only override if explicitly set to True
                # For string/numeric args, only override if not None
                if (isinstance(value, bool) and value) or (not isinstance(value, bool) and value is not None):
                    model_config_overrides[arg] = value
        
        # Initialize offline inference system
        vap_inference = OfflineVAPInference(args.model_path, args.device, model_config_overrides)
        # Apply detection thresholds from CLI
        vap_inference.bc_detection_threshold = float(getattr(args, 'bc_detection_threshold', 0.5))
        vap_inference.hn_detection_threshold = float(getattr(args, 'hn_detection_threshold', 0.5))
        
        # Build multimodal paths if provided
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
            # Remove None values
            for speaker in multimodal_paths:
                multimodal_paths[speaker] = {k: v for k, v in multimodal_paths[speaker].items() if v is not None}
        
        # Run inference with streaming strategy
        results = vap_inference.inference(args.audio1, args.audio2, multimodal_paths, 
                                         chunk_duration=args.chunk_duration,
                                         stride_duration=args.stride_duration,
                                         selective_frames=args.selective_frames)

        print("INFERENCE COMPLETED")
        
        # # Load ground truth AFTER inference for visualization comparison
        # if any([args.bc_expert_csv, args.hn_expert_csv]):
        #     print(f"\n📊 Loading ground truth for visualization...")
        #     # Use the number of frames from inference results
        #     total_frames = len(results['time_axis'])
        #     gt_labels = vap_inference.load_ground_truth_labels(
        #         args.bc_expert_csv,
        #         args.hn_expert_csv, 
        #         total_frames
        #     )
        #     results['ground_truth'] = gt_labels
        #     print(f"✅ Ground truth loaded and synchronized with predictions")
        
        # Generate output filenames
        audio1_name = Path(args.audio1).stem
        audio2_name = Path(args.audio2).stem
        base_name = f"vap_analysis_{audio1_name}_vs_{audio2_name}"
        
        # Save results for potential animation use
        results_path = output_dir / f"{base_name}_results.npy"
        np.save(results_path, results)
        print(f"💾 Results saved to: {results_path}")
        
        if args.static_only:
            # Create only static visualization
            visualizer = VAPVisualizer()
            static_path = output_dir / f"{base_name}_static.png"
            visualizer.plot_audio_and_vap(results, str(static_path), 
                                         bc_visualization=args.bc_visualization,
                                         hn_visualization=args.hn_visualization)
            
        elif args.animated or args.threaded:
            # Create animated visualization
            try:
                if args.threaded:
                    # Use threaded visualization (like VAP-Realtime)
                    from threaded_visualization import create_threaded_visualization
                    
                    print("🧵 Creating threaded visualization (VAP-Realtime style)...")
                    processor, visualizer_threaded = create_threaded_visualization(
                        results, 
                        playback_speed=args.playback_speed
                    )
                    
                else:
                    # Use standard animation
                    from animated_visualization import AnimatedVAPVisualizer
                    
                    print("🎬 Creating animated visualization...")
                    anim_visualizer = AnimatedVAPVisualizer(playback_speed=args.playback_speed)
                    
                    # Determine save path for animation
                    save_path = None
                    if args.save_mp4:
                        save_path = output_dir / f"{base_name}_animation.mp4"
                    elif args.save_gif:
                        save_path = output_dir / f"{base_name}_animation.gif"
                    
                    # Create and run animation
                    anim = anim_visualizer.create_animation(
                        results, 
                        str(save_path) if save_path else None,
                        show_live=True
                    )
                
            except ImportError as e:
                print(f"⚠️  Animation not available: {e}")
                print("📊 Creating static visualization instead...")
                visualizer = VAPVisualizer()
                static_path = output_dir / f"{base_name}_static.png"
                visualizer.plot_audio_and_vap(results, str(static_path),
                                             bc_visualization=args.bc_visualization,
                                             hn_visualization=args.hn_visualization)
        
        else:
            # Default: Create both static and offer animation
            visualizer = VAPVisualizer()
            static_path = output_dir / f"{base_name}_static.png"
            visualizer.plot_audio_and_vap(results, str(static_path),
                                         bc_visualization=args.bc_visualization,
                                         hn_visualization=args.hn_visualization)
            
            print("\n🎬 Animation Options:")
            print(f"   For animated playback, run:")
            print(f"   python animated_visualization.py --results_path {results_path}")
            print(f"   python animated_visualization.py --results_path {results_path} --save_mp4")
        
        print("🎉 Analysis complete!")
        
    except Exception as e:
        print(f"❌ Error during inference: {e}")
        import traceback
        traceback.print_exc()


if __name__ == "__main__":
    main()
