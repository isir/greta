#!/usr/bin/env python3
"""
Threaded real-time visualization system for VAP predictions
Mimics VAP-Realtime threading architecture for better performance
"""

import threading
import queue
import time
import numpy as np
import matplotlib.pyplot as plt
import matplotlib.animation as animation
from typing import Dict, Optional, Tuple
from dataclasses import dataclass
from pathlib import Path


@dataclass
class VAPFrame:
    """Single frame of VAP data"""
    timestamp: float
    audio1_chunk: np.ndarray
    audio2_chunk: np.ndarray
    vap_probs: Dict[str, np.ndarray]
    turn_events: np.ndarray


class ThreadedVAPProcessor:
    """
    Threaded VAP data processor - simulates real-time inference
    """
    
    def __init__(self, results: Dict, playback_speed: float = 1.0, chunk_size: float = 0.1):
        """
        Initialize threaded processor
        
        Args:
            results: Pre-computed inference results
            playback_speed: Playback speed multiplier
            chunk_size: Size of each data chunk in seconds
        """
        self.results = results
        self.playback_speed = playback_speed
        self.chunk_size = chunk_size
        
        # Threading components
        self.frame_queue = queue.Queue(maxsize=100)  # Buffer for smooth playback
        self.running = threading.Event()
        self.paused = threading.Event()
        
        # Timing
        self.total_duration = results['duration']
        self.frame_hz = results['frame_hz']
        self.sample_rate = results['sample_rate']
        
        # Current position
        self.current_time = 0.0
        
    def start_processing(self):
        """Start the threaded data processing"""
        self.running.set()
        self.processing_thread = threading.Thread(target=self._process_data, daemon=True)
        self.processing_thread.start()
        print("🔄 Started threaded data processing")
        
    def stop_processing(self):
        """Stop the threaded data processing"""
        self.running.clear()
        if hasattr(self, 'processing_thread'):
            self.processing_thread.join(timeout=1.0)
        print("⏹️  Stopped threaded data processing")
        
    def pause(self):
        """Pause data processing"""
        self.paused.set()
        print("⏸️  Paused playback")
        
    def resume(self):
        """Resume data processing"""
        self.paused.clear()
        print("▶️  Resumed playback")
        
    def _process_data(self):
        """Main data processing loop (runs in separate thread)"""
        print("🎬 Starting data processing thread...")
        
        while self.running.is_set() and self.current_time < self.total_duration:
            # Check if paused
            if self.paused.is_set():
                time.sleep(0.1)
                continue
                
            # Calculate timing
            real_time_delay = self.chunk_size / self.playback_speed
            process_start = time.time()
            
            # Create frame data
            frame = self._create_frame(self.current_time)
            
            # Add to queue (blocks if queue is full - natural backpressure)
            try:
                self.frame_queue.put(frame, timeout=0.1)
            except queue.Full:
                print("⚠️  Frame queue full, dropping frame")
                
            # Advance time
            self.current_time += self.chunk_size
            
            # Maintain real-time timing
            process_time = time.time() - process_start
            sleep_time = max(0, real_time_delay - process_time)
            
            if sleep_time > 0:
                time.sleep(sleep_time)
                
        print("✅ Data processing thread completed")
        
    def _create_frame(self, timestamp: float) -> VAPFrame:
        """
        Create a single frame of data at given timestamp
        
        Args:
            timestamp: Current timestamp
            
        Returns:
            VAPFrame object with current data
        """
        # Calculate indices
        audio_idx = int(timestamp * self.sample_rate)
        frame_idx = int(timestamp * self.frame_hz)
        
        # Extract audio chunks
        chunk_samples = int(self.chunk_size * self.sample_rate)
        audio1_chunk = self.results['audio1'][audio_idx:audio_idx + chunk_samples]
        audio2_chunk = self.results['audio2'][audio_idx:audio_idx + chunk_samples]
        
        # Pad if necessary
        if len(audio1_chunk) < chunk_samples:
            audio1_chunk = np.pad(audio1_chunk, (0, chunk_samples - len(audio1_chunk)))
            audio2_chunk = np.pad(audio2_chunk, (0, chunk_samples - len(audio2_chunk)))
            
        # Extract VAP probabilities
        chunk_frames = int(self.chunk_size * self.frame_hz)
        p_now_chunk = self.results['vap_probs']['p_now'][frame_idx:frame_idx + chunk_frames]
        p_future_chunk = self.results['vap_probs']['p_future'][frame_idx:frame_idx + chunk_frames]
        
        # Pad if necessary
        if len(p_now_chunk) < chunk_frames:
            pad_size = chunk_frames - len(p_now_chunk)
            p_now_chunk = np.pad(p_now_chunk, ((0, pad_size), (0, 0)))
            p_future_chunk = np.pad(p_future_chunk, ((0, pad_size), (0, 0)))
            
        vap_probs = {
            'p_now': p_now_chunk,
            'p_future': p_future_chunk
        }
        
        # Detect turn events in this chunk
        turn_events = self._detect_turn_events(p_now_chunk, timestamp)
        
        return VAPFrame(
            timestamp=timestamp,
            audio1_chunk=audio1_chunk,
            audio2_chunk=audio2_chunk,
            vap_probs=vap_probs,
            turn_events=turn_events
        )
        
    def _detect_turn_events(self, p_now_chunk: np.ndarray, base_timestamp: float) -> np.ndarray:
        """Detect turn events in current chunk"""
        if p_now_chunk.shape[1] > 1:  # Ensure shift class exists
            shift_probs = p_now_chunk[:, 1]
            threshold = 0.6
            
            # Find local maxima above threshold
            events = []
            for i in range(1, len(shift_probs) - 1):
                if (shift_probs[i] > threshold and 
                    shift_probs[i] > shift_probs[i-1] and 
                    shift_probs[i] > shift_probs[i+1]):
                    event_time = base_timestamp + (i / self.frame_hz)
                    events.append(event_time)
                    
            return np.array(events)
        
        return np.array([])
        
    def get_next_frame(self) -> Optional[VAPFrame]:
        """Get next frame from processing queue"""
        try:
            return self.frame_queue.get(timeout=0.1)
        except queue.Empty:
            return None


class ThreadedVAPVisualizer:
    """
    Threaded visualization system with separate rendering thread
    """
    
    def __init__(self, figsize: Tuple[int, int] = (16, 10), window_duration: float = 10.0):
        """
        Initialize threaded visualizer
        
        Args:
            figsize: Figure size
            window_duration: Time window to display
        """
        self.figsize = figsize
        self.window_duration = window_duration
        
        # Threading components
        self.render_queue = queue.Queue(maxsize=50)
        self.rendering = threading.Event()
        
        # Visualization state
        self.current_data = []
        self.current_time = 0.0
        
        # Plot elements
        self.fig = None
        self.axes = None
        self.lines = {}
        
    def setup_plots(self):
        """Setup the plot structure"""
        self.fig, self.axes = plt.subplots(5, 1, figsize=self.figsize, sharex=True)
        
        # Setup each subplot
        self._setup_audio_plots()
        self._setup_vap_plots()
        self._setup_controls()
        
        plt.tight_layout()
        
    def _setup_audio_plots(self):
        """Setup audio waveform plots"""
        # Speaker 1
        self.axes[0].set_ylabel('Speaker 1\nAmplitude', fontsize=10)
        self.axes[0].set_title('Threaded Real-time VAP Analysis', fontsize=14, fontweight='bold')
        self.axes[0].grid(True, alpha=0.3)
        self.axes[0].set_ylim(-1, 1)
        
        # Speaker 2
        self.axes[1].set_ylabel('Speaker 2\nAmplitude', fontsize=10)
        self.axes[1].grid(True, alpha=0.3)
        self.axes[1].set_ylim(-1, 1)
        
        # Initialize lines
        self.lines['audio1'], = self.axes[0].plot([], [], 'b-', linewidth=1.0, alpha=0.8)
        self.lines['audio2'], = self.axes[1].plot([], [], 'r-', linewidth=1.0, alpha=0.8)
        
    def _setup_vap_plots(self):
        """Setup VAP probability plots"""
        # P_now
        self.axes[2].set_ylabel('P_now\nProbability', fontsize=10)
        self.axes[2].set_ylim(0, 1)
        self.axes[2].grid(True, alpha=0.3)
        
        # P_future
        self.axes[3].set_ylabel('P_future\nProbability', fontsize=10)
        self.axes[3].set_ylim(0, 1)
        self.axes[3].grid(True, alpha=0.3)
        
        # Turn events
        self.axes[4].set_ylabel('Turn Events', fontsize=10)
        self.axes[4].set_xlabel('Time (seconds)', fontsize=12)
        self.axes[4].set_ylim(-0.5, 1.5)
        self.axes[4].grid(True, alpha=0.3)
        
        # VAP lines
        vap_labels = ['Hold', 'Shift', 'Backchannel', 'Silence']
        colors = ['green', 'orange', 'purple', 'gray']
        
        for i, (label, color) in enumerate(zip(vap_labels, colors)):
            # P_now lines
            line, = self.axes[2].plot([], [], label=label, color=color, linewidth=2.0)
            self.lines[f'p_now_{i}'] = line
            
            # P_future lines
            line, = self.axes[3].plot([], [], label=label, color=color, linewidth=2.0)
            self.lines[f'p_future_{i}'] = line
            
        # Turn events
        self.lines['turn_events'], = self.axes[4].plot([], [], 'ro', markersize=8, alpha=0.7)
        
        # Legends
        self.axes[2].legend(loc='upper right', fontsize=8)
        self.axes[3].legend(loc='upper right', fontsize=8)
        
    def _setup_controls(self):
        """Setup current time indicator"""
        self.current_time_lines = []
        for ax in self.axes:
            line = ax.axvline(x=0, color='red', linestyle='-', linewidth=2, alpha=0.8)
            self.current_time_lines.append(line)
            
    def start_rendering(self, processor: ThreadedVAPProcessor):
        """Start the rendering thread"""
        self.processor = processor
        self.rendering.set()
        self.render_thread = threading.Thread(target=self._render_loop, daemon=True)
        self.render_thread.start()
        print("🎨 Started threaded rendering")
        
    def stop_rendering(self):
        """Stop the rendering thread"""
        self.rendering.clear()
        if hasattr(self, 'render_thread'):
            self.render_thread.join(timeout=1.0)
        print("🛑 Stopped threaded rendering")
        
    def _render_loop(self):
        """Main rendering loop (runs in separate thread)"""
        print("🖼️  Starting rendering thread...")
        
        while self.rendering.is_set():
            # Get next frame from processor
            frame = self.processor.get_next_frame()
            
            if frame is None:
                time.sleep(0.01)  # Small delay if no data
                continue
                
            # Add frame to current data
            self.current_data.append(frame)
            self.current_time = frame.timestamp
            
            # Keep only data within window
            cutoff_time = self.current_time - self.window_duration
            self.current_data = [f for f in self.current_data if f.timestamp >= cutoff_time]
            
            # Update visualization (thread-safe)
            self._update_plots()
            
            # Small delay for smooth animation
            time.sleep(0.02)  # ~50 FPS
            
        print("✅ Rendering thread completed")
        
    def _update_plots(self):
        """Update all plots with current data"""
        if not self.current_data:
            return
            
        # Collect all data in window
        timestamps = [f.timestamp for f in self.current_data]
        
        # Update audio plots
        self._update_audio_plots(timestamps)
        
        # Update VAP plots  
        self._update_vap_plots(timestamps)
        
        # Update turn events
        self._update_turn_events()
        
        # Update time window
        start_time = max(0, self.current_time - self.window_duration)
        end_time = self.current_time
        
        for ax in self.axes:
            ax.set_xlim(start_time, end_time)
            
        # Update current time indicator
        for line in self.current_time_lines:
            line.set_xdata([self.current_time, self.current_time])
            
        # Update title
        self.axes[0].set_title(f'Threaded Real-time VAP Analysis - Time: {self.current_time:.1f}s', 
                              fontsize=14, fontweight='bold')
        
        # Trigger redraw
        self.fig.canvas.draw_idle()
        
    def _update_audio_plots(self, timestamps):
        """Update audio waveform plots"""
        # Concatenate audio data
        audio1_data = np.concatenate([f.audio1_chunk for f in self.current_data])
        audio2_data = np.concatenate([f.audio2_chunk for f in self.current_data])
        
        # Create time axis
        start_time = timestamps[0] if timestamps else 0
        audio_time = np.linspace(start_time, self.current_time, len(audio1_data))
        
        # Update lines
        self.lines['audio1'].set_data(audio_time, audio1_data)
        self.lines['audio2'].set_data(audio_time, audio2_data)
        
    def _update_vap_plots(self, timestamps):
        """Update VAP probability plots"""
        # Concatenate VAP data
        p_now_data = np.concatenate([f.vap_probs['p_now'] for f in self.current_data])
        p_future_data = np.concatenate([f.vap_probs['p_future'] for f in self.current_data])
        
        # Create time axis
        start_time = timestamps[0] if timestamps else 0
        vap_time = np.linspace(start_time, self.current_time, len(p_now_data))
        
        # Update P_now lines
        for i in range(min(4, p_now_data.shape[1])):
            if f'p_now_{i}' in self.lines:
                self.lines[f'p_now_{i}'].set_data(vap_time, p_now_data[:, i])
                
        # Update P_future lines
        for i in range(min(4, p_future_data.shape[1])):
            if f'p_future_{i}' in self.lines:
                self.lines[f'p_future_{i}'].set_data(vap_time, p_future_data[:, i])
                
    def _update_turn_events(self):
        """Update turn event markers"""
        # Collect all turn events in current window
        all_events = []
        for frame in self.current_data:
            all_events.extend(frame.turn_events)
            
        if all_events:
            event_times = np.array(all_events)
            event_y = np.ones_like(event_times)
            self.lines['turn_events'].set_data(event_times, event_y)
        else:
            self.lines['turn_events'].set_data([], [])


def create_threaded_visualization(results: Dict, playback_speed: float = 1.0, 
                                save_path: Optional[str] = None) -> Tuple[ThreadedVAPProcessor, ThreadedVAPVisualizer]:
    """
    Create and run threaded VAP visualization
    
    Args:
        results: Inference results
        playback_speed: Playback speed multiplier
        save_path: Optional path to save animation
        
    Returns:
        Tuple of (processor, visualizer)
    """
    print("🧵 Creating threaded VAP visualization system...")
    
    # Create components
    processor = ThreadedVAPProcessor(results, playback_speed)
    visualizer = ThreadedVAPVisualizer()
    
    # Setup visualization
    visualizer.setup_plots()
    
    # Start threads
    processor.start_processing()
    visualizer.start_rendering(processor)
    
    print("🎬 Threaded visualization started!")
    print("   - Data processing thread: ✅")
    print("   - Rendering thread: ✅")
    print("   - Close window to stop")
    
    try:
        # Show visualization
        plt.show()
        
    except KeyboardInterrupt:
        print("\n⏹️  Stopping visualization...")
        
    finally:
        # Cleanup
        processor.stop_processing()
        visualizer.stop_rendering()
        
    return processor, visualizer


def main():
    """Main function for threaded visualization"""
    import argparse
    
    parser = argparse.ArgumentParser(description='Threaded VAP Visualization')
    parser.add_argument('--results_path', type=str, required=True,
                       help='Path to inference results numpy file')
    parser.add_argument('--playback_speed', type=float, default=1.0,
                       help='Playback speed multiplier')
    
    args = parser.parse_args()
    
    # Load results
    results = np.load(args.results_path, allow_pickle=True).item()
    
    # Create threaded visualization
    processor, visualizer = create_threaded_visualization(results, args.playback_speed)


if __name__ == "__main__":
    main()

