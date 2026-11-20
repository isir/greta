#!/usr/bin/env python3
"""
Head Nod Detection and Classification for VAP Integration

Based on "Real-time Generation of Various Types of Nodding for Avatar Attentive Listening System"
Implements OpenFace-based head nod detection using pitch (Pose_Rx) analysis.

Detects and classifies head nods into:
- short: Small amplitude nods
- long: Large amplitude nods without overshoot  
- long_p: Large amplitude nods with "swinging up" overshoot
"""

import numpy as np
import pandas as pd
import scipy.signal
from scipy.ndimage import uniform_filter1d
from typing import List, Tuple, Dict, Optional
import matplotlib.pyplot as plt
from pathlib import Path
import os

class HeadNodDetector:
    """
    Detects and classifies head nods from OpenFace head pose data.
    
    Parameters from paper:
    - Frame rate: 100 Hz (we'll work with 30 fps from OpenFace and optionally upsample)
    - Smoothing: 7-frame moving average
    - Nod duration: 0.3-1.5 seconds
    - Minimum amplitude: >0.05-0.1 radians
    """
    
    def __init__(
        self,
        target_fps: int = 50,  # Match VAP pipeline frame rate
        smoothing_window: int = 7,
        min_nod_duration: float = 0.3,  # seconds
        max_nod_duration: float = 1.5,  # seconds
        min_amplitude_threshold: float = 0.09,  # radians (~5.2 degrees) - minimum for short
        large_amplitude_threshold: float = 0.262,  # radians (~15.0 degrees) - threshold for long/long_p
        swinging_threshold: float = 0.08,  # radians (~4.6 degrees) for overshoot detection
    ):
        self.target_fps = target_fps
        self.smoothing_window = smoothing_window
        self.min_nod_duration = min_nod_duration
        self.max_nod_duration = max_nod_duration
        self.min_amplitude_threshold = min_amplitude_threshold
        self.large_amplitude_threshold = large_amplitude_threshold
        self.swinging_threshold = swinging_threshold
        
        # Derived parameters
        self.min_nod_frames = int(min_nod_duration * target_fps)
        self.max_nod_frames = int(max_nod_duration * target_fps)
        
    def load_head_pose_data(self, csv_path: str) -> Tuple[np.ndarray, float]:
        """
        Load head pose data from OpenFace CSV file.
        
        Returns:
            pitch_signal: Pose_Rx (pitch) values in radians
            original_fps: Estimated frame rate of input data
        """
        if not os.path.exists(csv_path):
            raise FileNotFoundError(f"Head pose file not found: {csv_path}")
        
        df = pd.read_csv(csv_path)
        
        # Check for expected columns
        if 'pose_Rx' not in df.columns:
            raise ValueError(f"Expected 'pose_Rx' column not found in {csv_path}")
        
        pitch_signal = df['pose_Rx'].values
        
        # Estimate original frame rate (typically 30 fps for OpenFace)
        # For now, assume 30 fps - could be improved by checking timestamps if available
        original_fps = 30.0
        
        print(f"Loaded {len(pitch_signal)} frames from {os.path.basename(csv_path)}")
        print(f"Pitch range: {pitch_signal.min():.3f} to {pitch_signal.max():.3f} radians")
        
        return pitch_signal, original_fps
    
    def preprocess_signal(self, pitch_signal: np.ndarray, original_fps: float) -> np.ndarray:
        """
        Preprocess the pitch signal: resample and smooth.
        
        Steps:
        1. Resample to target FPS if needed
        2. Apply smoothing (moving average)
        """
        # Step 1: Resample if needed
        if abs(original_fps - self.target_fps) > 0.1:
            print(f"Resampling from {original_fps} fps to {self.target_fps} fps")
            # Calculate new length
            new_length = int(len(pitch_signal) * self.target_fps / original_fps)
            # Resample using scipy
            resampled_signal = scipy.signal.resample(pitch_signal, new_length)
        else:
            resampled_signal = pitch_signal.copy()
        
        # Step 2: Apply smoothing (7-frame moving average)
        print(f"Applying {self.smoothing_window}-frame moving average smoothing")
        smoothed_signal = uniform_filter1d(resampled_signal, size=self.smoothing_window, mode='nearest')
        
        return smoothed_signal
    
    def detect_nod_segments(self, smoothed_pitch: np.ndarray) -> List[Dict]:
        """
        Detect nodding segments using gradient analysis.
        
        A nod = one downward + upward cycle within specified duration.
        
        Returns:
            List of detected nod segments with start/end frames and basic properties
        """
        # Compute gradient (first derivative)
        gradient = np.gradient(smoothed_pitch)
        
        # Find zero crossings in gradient (local minima/maxima in pitch)
        zero_crossings = []
        for i in range(1, len(gradient)):
            if gradient[i-1] * gradient[i] < 0:  # Sign change
                zero_crossings.append(i)
        
        nod_segments = []
        
        # Look for nod patterns: down -> up within time window
        for i in range(len(zero_crossings) - 1):
            start_frame = zero_crossings[i]
            
            # Look for next few zero crossings to form a complete nod cycle
            for j in range(i + 1, min(i + 4, len(zero_crossings))):  # Check up to 3 next crossings
                end_frame = zero_crossings[j]
                duration_frames = end_frame - start_frame
                
                # Check if duration is within nod range
                if self.min_nod_frames <= duration_frames <= self.max_nod_frames:
                    
                    # Extract segment
                    segment_pitch = smoothed_pitch[start_frame:end_frame+1]
                    
                    # Check if it looks like a nod (has both down and up movement)
                    min_pitch = np.min(segment_pitch)
                    max_pitch = np.max(segment_pitch)
                    amplitude = max_pitch - min_pitch
                    
                    # Must exceed minimum amplitude
                    if amplitude >= self.min_amplitude_threshold:
                        
                        # Find the actual min and max positions
                        min_idx = np.argmin(segment_pitch) + start_frame
                        max_idx = np.argmax(segment_pitch) + start_frame
                        
                        # Basic nod pattern check: should have a clear minimum
                        baseline = (segment_pitch[0] + segment_pitch[-1]) / 2
                        nod_depth = baseline - min_pitch
                        
                        if nod_depth > 0.01:  # At least some downward movement
                            nod_segments.append({
                                'start_frame': start_frame,
                                'end_frame': end_frame,
                                'duration_frames': duration_frames,
                                'duration_seconds': duration_frames / self.target_fps,
                                'amplitude': amplitude,
                                'min_pitch': min_pitch,
                                'max_pitch': max_pitch,
                                'min_frame': min_idx,
                                'max_frame': max_idx,
                                'baseline': baseline,
                                'nod_depth': nod_depth
                            })
                            break  # Found a valid nod, move to next start point
        
        print(f"Detected {len(nod_segments)} potential nod segments")
        return nod_segments
    
    def classify_nods(self, nod_segments: List[Dict], smoothed_pitch: np.ndarray) -> List[Dict]:
        """
        Classify detected nods into short/long/long_p categories.
        
        Classification rules from paper:
        - short: Small amplitude
        - long: Large amplitude, no overshoot
        - long_p: Large amplitude, with "swinging up" overshoot
        """
        classified_nods = []
        
        for nod in nod_segments:
            start_frame = nod['start_frame']
            end_frame = nod['end_frame']
            amplitude = nod['amplitude']
            baseline = nod['baseline']
            min_pitch = nod['min_pitch']
            
            # Check for overshoot (swinging up)
            # Look for rebound higher than starting position after the nod
            has_overshoot = False
            if end_frame + 10 < len(smoothed_pitch):  # Have enough data after nod
                post_nod_window = smoothed_pitch[end_frame:end_frame+10]
                max_post_nod = np.max(post_nod_window)
                
                # Overshoot if post-nod maximum exceeds baseline by threshold
                if max_post_nod > baseline + self.swinging_threshold:
                    has_overshoot = True
            
            # Classify based on amplitude, overshoot, AND duration
            # New classification rules:
            # - Below 0.09 rad: too small, classify as no_nod (will be filtered out)
            # - 0.09-0.262 rad AND 0.30-0.60s: short
            # - >0.262 rad AND up to 1.50s without overshoot: long  
            # - >0.262 rad AND up to 1.50s with overshoot ≥0.08 rad: long_p
            
            duration_seconds = nod['duration_seconds']
            
            if amplitude < self.min_amplitude_threshold:
                nod_type = 'no_nod'  # Too small to be a meaningful nod
            elif amplitude < self.large_amplitude_threshold:
                # Short nods: must be within 0.30-0.60s duration range
                if 0.30 <= duration_seconds <= 0.60:
                    nod_type = 'short'   # 0.09-0.262 rad, 0.30-0.60s
                else:
                    nod_type = 'no_nod'  # Wrong duration for short nod
            else:  # amplitude >= 0.262 rad
                # Long/Long_p nods: must be within up to 1.50s duration
                if duration_seconds <= 1.50:
                    if has_overshoot:
                        nod_type = 'long_p'  # Large amplitude with overshoot ≥0.08 rad
                    else:
                        nod_type = 'long'    # Large amplitude without overshoot
                else:
                    nod_type = 'no_nod'  # Too long duration
            
            # Add classification to nod data
            classified_nod = nod.copy()
            classified_nod.update({
                'nod_type': nod_type,
                'has_overshoot': has_overshoot,
                'amplitude_category': 'large' if amplitude >= self.large_amplitude_threshold else 'small'
            })
            
            classified_nods.append(classified_nod)
        
        # Print classification summary
        type_counts = {}
        for nod in classified_nods:
            nod_type = nod['nod_type']
            type_counts[nod_type] = type_counts.get(nod_type, 0) + 1
        
        print(f"Nod classification: {type_counts}")
        return classified_nods
    
    def create_frame_labels(self, classified_nods: List[Dict], total_frames: int) -> np.ndarray:
        """
        Create frame-level labels for the entire signal.
        
        Labels:
        0 = no_nod
        1 = short
        2 = long  
        3 = long_p
        """
        labels = np.zeros(total_frames, dtype=int)
        
        label_mapping = {
            'short': 1,
            'long': 2,
            'long_p': 3
        }
        
        for nod in classified_nods:
            start_frame = nod['start_frame']
            end_frame = nod['end_frame']
            nod_type = nod['nod_type']
            
            # Only label frames for valid nod types (skip 'no_nod')
            if nod_type in label_mapping:
                labels[start_frame:end_frame+1] = label_mapping[nod_type]
        
        return labels
    
    def process_head_pose_file(self, csv_path: str, debug: bool = False) -> Tuple[np.ndarray, List[Dict]]:
        """
        Complete processing pipeline for a single head pose file.
        
        Returns:
            frame_labels: Array of frame-level nod labels
            classified_nods: List of detected and classified nod segments
        """
        print(f"\n🎯 Processing head pose file: {os.path.basename(csv_path)}")
        
        # Step 1: Load data
        pitch_signal, original_fps = self.load_head_pose_data(csv_path)
        
        # Step 2: Preprocess
        smoothed_pitch = self.preprocess_signal(pitch_signal, original_fps)
        
        # Step 3: Detect nod segments
        nod_segments = self.detect_nod_segments(smoothed_pitch)
        
        # Step 4: Classify nods
        classified_nods = self.classify_nods(nod_segments, smoothed_pitch)
        
        # Step 5: Create frame labels
        frame_labels = self.create_frame_labels(classified_nods, len(smoothed_pitch))
        
        # Debug visualization if requested
        if debug and len(classified_nods) > 0:
            self._plot_debug_visualization(smoothed_pitch, classified_nods, csv_path)
        
        print(f"✅ Generated {len(frame_labels)} frame labels with {len(classified_nods)} nods")
        
        return frame_labels, classified_nods
    
    def _plot_debug_visualization(self, smoothed_pitch: np.ndarray, classified_nods: List[Dict], csv_path: str):
        """Create debug visualization of detected nods."""
        plt.figure(figsize=(15, 6))
        
        # Plot smoothed pitch signal
        time_axis = np.arange(len(smoothed_pitch)) / self.target_fps
        plt.plot(time_axis, smoothed_pitch, 'b-', alpha=0.7, label='Smoothed Pitch')
        
        # Color map for nod types
        colors = {'short': 'green', 'long': 'orange', 'long_p': 'red'}
        
        # Highlight detected nods
        for nod in classified_nods:
            start_time = nod['start_frame'] / self.target_fps
            end_time = nod['end_frame'] / self.target_fps
            nod_type = nod['nod_type']
            
            plt.axvspan(start_time, end_time, alpha=0.3, color=colors[nod_type], 
                       label=f"{nod_type}" if nod_type not in [n['nod_type'] for n in classified_nods[:classified_nods.index(nod)]] else "")
        
        plt.xlabel('Time (seconds)')
        plt.ylabel('Pitch (radians)')
        plt.title(f'Head Nod Detection: {os.path.basename(csv_path)}')
        plt.legend()
        plt.grid(True, alpha=0.3)
        plt.tight_layout()
        
        # Save debug plot
        debug_path = f"debug_nod_detection_{os.path.basename(csv_path)}.png"
        plt.savefig(debug_path, dpi=150, bbox_inches='tight')
        plt.close()
        print(f"Debug plot saved: {debug_path}")

def test_head_nod_detection():
    """Test the head nod detection on a sample file."""
    print("🧪 TESTING HEAD NOD DETECTION")
    print("=" * 50)
    
    # Initialize detector
    detector = HeadNodDetector()
    
    # Test on a sample file
    test_file = "noxi/Paris_01-head_expert.csv"
    
    if os.path.exists(test_file):
        try:
            frame_labels, classified_nods = detector.process_head_pose_file(test_file, debug=True)
            
            print(f"\n📊 RESULTS SUMMARY:")
            print(f"Total frames: {len(frame_labels)}")
            print(f"Detected nods: {len(classified_nods)}")
            
            # Count label distribution
            unique_labels, counts = np.unique(frame_labels, return_counts=True)
            label_names = ['no_nod', 'short', 'long', 'long_p']
            
            for label, count in zip(unique_labels, counts):
                percentage = count / len(frame_labels) * 100
                print(f"{label_names[label]:8}: {count:6} frames ({percentage:5.2f}%)")
            
            return True
            
        except Exception as e:
            print(f"❌ Error processing file: {e}")
            import traceback
            traceback.print_exc()
            return False
    else:
        print(f"❌ Test file not found: {test_file}")
        return False

if __name__ == "__main__":
    test_head_nod_detection()
