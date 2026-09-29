#!/usr/bin/env python3
"""
Real-time animated visualization for offline VAP inference results
Simulates real-time playback of the conversation with live updating plots
"""

import numpy as np
import matplotlib.pyplot as plt
import matplotlib.animation as animation
from matplotlib.patches import Rectangle
from typing import Dict, Optional, Tuple
import argparse
from pathlib import Path
import time


class AnimatedVAPVisualizer:
    """
    Real-time animated visualization system for VAP predictions
    Simulates live conversation playback with updating plots
    """
    
    def __init__(self, figsize: Tuple[int, int] = (16, 10), playback_speed: float = 1.0):
        """
        Initialize animated visualizer
        
        Args:
            figsize: Figure size for plots
            playback_speed: Playback speed multiplier (1.0 = real-time)
        """
        self.figsize = figsize
        self.playback_speed = playback_speed
        
        # Animation parameters
        self.window_duration = 10.0  # Show last 10 seconds
        self.update_interval = 50    # Update every 50ms (20 FPS)
        
        # Initialize plot elements
        self.fig = None
        self.axes = None
        self.lines = {}
        self.current_time = 0.0
        
    def setup_plots(self, results: Dict):
        """
        Setup the animated plot structure
        
        Args:
            results: Results dictionary from inference
        """
        # Create figure and subplots
        self.fig, self.axes = plt.subplots(5, 1, figsize=self.figsize, sharex=True)
        
        # Store results
        self.results = results
        self.total_duration = results['duration']
        self.time_axis = results['time_axis']
        self.frame_hz = results['frame_hz']
        
        # Setup each subplot
        self._setup_audio_plots()
        self._setup_vap_plots()
        self._setup_current_time_indicator()
        
        # Configure layout
        plt.tight_layout()
        plt.subplots_adjust(hspace=0.3)
        
    def _setup_audio_plots(self):
        """Setup audio waveform plots"""
        # Speaker 1 waveform
        self.axes[0].set_ylabel('Speaker 1\nAmplitude', fontsize=10)
        self.axes[0].set_title('Real-time VAP Analysis - Live Conversation Playback', 
                              fontsize=14, fontweight='bold')
        self.axes[0].grid(True, alpha=0.3)
        self.axes[0].set_ylim(-1, 1)
        
        # Speaker 2 waveform  
        self.axes[1].set_ylabel('Speaker 2\nAmplitude', fontsize=10)
        self.axes[1].grid(True, alpha=0.3)
        self.axes[1].set_ylim(-1, 1)
        
        # Initialize empty lines
        self.lines['audio1'], = self.axes[0].plot([], [], color='blue', linewidth=1.0, alpha=0.8)
        self.lines['audio2'], = self.axes[1].plot([], [], color='red', linewidth=1.0, alpha=0.8)
        
    def _setup_vap_plots(self):
        """Setup VAP probability plots"""
        # VAP P_now
        self.axes[2].set_ylabel('P_now\nProbability', fontsize=10)
        self.axes[2].set_ylim(0, 1)
        self.axes[2].grid(True, alpha=0.3)
        
        # VAP P_future
        self.axes[3].set_ylabel('P_future\nProbability', fontsize=10) 
        self.axes[3].set_ylim(0, 1)
        self.axes[3].grid(True, alpha=0.3)
        
        # Turn-taking events
        self.axes[4].set_ylabel('Turn Events', fontsize=10)
        self.axes[4].set_xlabel('Time (seconds)', fontsize=12)
        self.axes[4].set_ylim(-0.5, 1.5)
        self.axes[4].grid(True, alpha=0.3)
        
        # VAP probability lines
        vap_labels = ['Hold', 'Shift', 'Backchannel', 'Silence']
        colors = ['green', 'orange', 'purple', 'gray']
        
        p_now = self.results['vap_probs']['p_now']
        p_future = self.results['vap_probs']['p_future']
        
        # P_now lines
        for i, (label, color) in enumerate(zip(vap_labels, colors)):
            if i < p_now.shape[1]:
                line, = self.axes[2].plot([], [], label=label, color=color, linewidth=2.0)
                self.lines[f'p_now_{i}'] = line
        
        # P_future lines
        for i, (label, color) in enumerate(zip(vap_labels, colors)):
            if i < p_future.shape[1]:
                line, = self.axes[3].plot([], [], label=label, color=color, linewidth=2.0)
                self.lines[f'p_future_{i}'] = line
        
        # Turn event markers
        self.lines['turn_events'], = self.axes[4].plot([], [], 'ro', markersize=8, alpha=0.7, label='Turn Shifts')
        
        # Add legends
        self.axes[2].legend(loc='upper right', fontsize=8)
        self.axes[3].legend(loc='upper right', fontsize=8)
        self.axes[4].legend(loc='upper right', fontsize=8)
        
    def _setup_current_time_indicator(self):
        """Setup current time indicator line"""
        self.current_time_lines = []
        for ax in self.axes:
            line = ax.axvline(x=0, color='red', linestyle='-', linewidth=2, alpha=0.8, label='Current Time')
            self.current_time_lines.append(line)
    
    def _get_window_data(self, current_time: float) -> Tuple[float, float, slice, slice]:
        """
        Get data window for current time
        
        Args:
            current_time: Current playback time
            
        Returns:
            Tuple of (start_time, end_time, audio_slice, frame_slice)
        """
        # Define time window
        start_time = max(0, current_time - self.window_duration)
        end_time = min(self.total_duration, current_time)
        
        # Get audio slice
        sample_rate = self.results['sample_rate']
        audio_start_idx = int(start_time * sample_rate)
        audio_end_idx = int(end_time * sample_rate)
        audio_slice = slice(audio_start_idx, audio_end_idx)
        
        # Get frame slice
        frame_start_idx = int(start_time * self.frame_hz)
        frame_end_idx = int(end_time * self.frame_hz)
        frame_slice = slice(frame_start_idx, frame_end_idx)
        
        return start_time, end_time, audio_slice, frame_slice
    
    def _detect_turn_events(self, p_now_window: np.ndarray, time_window: np.ndarray) -> np.ndarray:
        """
        Detect turn-taking events in current window
        
        Args:
            p_now_window: P_now probabilities for current window
            time_window: Time axis for current window
            
        Returns:
            Array of turn event times
        """
        if p_now_window.shape[1] > 1:  # Ensure shift class exists
            shift_probs = p_now_window[:, 1]  # Shift class
            
            # Find peaks in shift probability
            threshold = 0.6
            peaks = []
            
            for i in range(1, len(shift_probs) - 1):
                if (shift_probs[i] > threshold and 
                    shift_probs[i] > shift_probs[i-1] and 
                    shift_probs[i] > shift_probs[i+1]):
                    peaks.append(time_window[i])
            
            return np.array(peaks)
        
        return np.array([])
    
    def animate_frame(self, frame_num: int):
        """
        Animation function called for each frame
        
        Args:
            frame_num: Current frame number
        """
        # Calculate current time
        self.current_time = (frame_num * self.update_interval / 1000.0) * self.playback_speed
        
        if self.current_time > self.total_duration:
            return list(self.lines.values()) + self.current_time_lines
        
        # Get data window
        start_time, end_time, audio_slice, frame_slice = self._get_window_data(self.current_time)
        
        # Create time axes for window
        audio_window_time = np.linspace(start_time, end_time, audio_slice.stop - audio_slice.start)
        frame_window_time = self.time_axis[frame_slice]
        
        # Update audio plots
        if len(audio_window_time) > 0:
            audio1_window = self.results['audio1'][audio_slice]
            audio2_window = self.results['audio2'][audio_slice]
            
            self.lines['audio1'].set_data(audio_window_time, audio1_window)
            self.lines['audio2'].set_data(audio_window_time, audio2_window)
        
        # Update VAP plots
        if len(frame_window_time) > 0:
            p_now_window = self.results['vap_probs']['p_now'][frame_slice]
            p_future_window = self.results['vap_probs']['p_future'][frame_slice]
            
            # Update P_now lines
            for i in range(p_now_window.shape[1]):
                if f'p_now_{i}' in self.lines:
                    self.lines[f'p_now_{i}'].set_data(frame_window_time, p_now_window[:, i])
            
            # Update P_future lines
            for i in range(p_future_window.shape[1]):
                if f'p_future_{i}' in self.lines:
                    self.lines[f'p_future_{i}'].set_data(frame_window_time, p_future_window[:, i])
            
            # Update turn events
            turn_events = self._detect_turn_events(p_now_window, frame_window_time)
            if len(turn_events) > 0:
                event_y = np.ones_like(turn_events)
                self.lines['turn_events'].set_data(turn_events, event_y)
            else:
                self.lines['turn_events'].set_data([], [])
        
        # Update time window for all axes
        for ax in self.axes:
            ax.set_xlim(start_time, end_time)
        
        # Update current time indicator
        for line in self.current_time_lines:
            line.set_xdata([self.current_time, self.current_time])
        
        # Update title with current time
        self.axes[0].set_title(f'Real-time VAP Analysis - Time: {self.current_time:.1f}s / {self.total_duration:.1f}s', 
                              fontsize=14, fontweight='bold')
        
        return list(self.lines.values()) + self.current_time_lines
    
    def create_animation(self, results: Dict, save_path: Optional[str] = None, 
                        show_live: bool = True) -> animation.FuncAnimation:
        """
        Create and run the animated visualization
        
        Args:
            results: Results dictionary from inference
            save_path: Optional path to save animation as MP4/GIF
            show_live: Whether to show live animation
            
        Returns:
            Animation object
        """
        print("🎬 Creating animated visualization...")
        
        # Setup plots
        self.setup_plots(results)
        
        # Calculate total frames
        total_frames = int((self.total_duration / self.playback_speed) * (1000 / self.update_interval))
        
        print(f"📊 Animation info:")
        print(f"   Duration: {self.total_duration:.1f}s")
        print(f"   Playback speed: {self.playback_speed}x")
        print(f"   Update interval: {self.update_interval}ms")
        print(f"   Total frames: {total_frames}")
        
        # Create animation
        anim = animation.FuncAnimation(
            self.fig, 
            self.animate_frame,
            frames=total_frames,
            interval=self.update_interval,
            blit=False,  # Set to False for better compatibility
            repeat=True
        )
        
        # Save animation if requested
        if save_path:
            print(f"💾 Saving animation to: {save_path}")
            
            if save_path.endswith('.mp4'):
                # Save as MP4 video
                Writer = animation.writers['ffmpeg']
                writer = Writer(fps=20, metadata=dict(artist='VAP-Analyzer'), bitrate=1800)
                anim.save(save_path, writer=writer)
            elif save_path.endswith('.gif'):
                # Save as GIF
                anim.save(save_path, writer='pillow', fps=10)
            else:
                print("⚠️  Unsupported format. Use .mp4 or .gif")
        
        # Show live animation
        if show_live:
            print("🎥 Starting live animation playback...")
            print("   Close the window to stop playback")
            plt.show()
        
        return anim


def create_static_overview(results: Dict, save_path: Optional[str] = None):
    """
    Create static overview plot showing the entire conversation
    
    Args:
        results: Results dictionary from inference
        save_path: Optional path to save the plot
    """
    fig, axes = plt.subplots(5, 1, figsize=(16, 10), sharex=True)
    
    audio1 = results['audio1']
    audio2 = results['audio2']
    time_axis = results['time_axis']
    vap_probs = results['vap_probs']
    
    # Create audio time axis
    audio_time = np.linspace(0, results['duration'], len(audio1))
    
    # Plot audio waveforms
    axes[0].plot(audio_time, audio1, color='blue', alpha=0.7, linewidth=0.5)
    axes[0].set_ylabel('Speaker 1\nAmplitude', fontsize=10)
    axes[0].set_title('Complete Conversation Overview', fontsize=14, fontweight='bold')
    axes[0].grid(True, alpha=0.3)
    
    axes[1].plot(audio_time, audio2, color='red', alpha=0.7, linewidth=0.5)
    axes[1].set_ylabel('Speaker 2\nAmplitude', fontsize=10)
    axes[1].grid(True, alpha=0.3)
    
    # Plot VAP probabilities
    p_now = vap_probs['p_now']
    p_future = vap_probs['p_future']
    
    vap_labels = ['Hold', 'Shift', 'Backchannel', 'Silence']
    colors = ['green', 'orange', 'purple', 'gray']
    
    # P_now
    for i, (label, color) in enumerate(zip(vap_labels, colors)):
        if i < p_now.shape[1]:
            axes[2].plot(time_axis, p_now[:, i], label=label, color=color, linewidth=1.5)
    
    axes[2].set_ylabel('P_now\nProbability', fontsize=10)
    axes[2].set_ylim(0, 1)
    axes[2].legend(loc='upper right', fontsize=8)
    axes[2].grid(True, alpha=0.3)
    
    # P_future
    for i, (label, color) in enumerate(zip(vap_labels, colors)):
        if i < p_future.shape[1]:
            axes[3].plot(time_axis, p_future[:, i], label=label, color=color, linewidth=1.5)
    
    axes[3].set_ylabel('P_future\nProbability', fontsize=10)
    axes[3].set_ylim(0, 1)
    axes[3].legend(loc='upper right', fontsize=8)
    axes[3].grid(True, alpha=0.3)
    
    # Turn events
    if p_now.shape[1] > 1:
        shift_probs = p_now[:, 1]
        turn_events = time_axis[shift_probs > 0.6]
        
        axes[4].scatter(turn_events, np.ones_like(turn_events), 
                       color='red', s=50, alpha=0.7, label='Turn Shifts')
        
        # Add vertical lines for major turn events
        for event_time in turn_events:
            for ax in axes:
                ax.axvline(x=event_time, color='red', linestyle='--', alpha=0.3, linewidth=1)
    
    axes[4].set_ylabel('Turn Events', fontsize=10)
    axes[4].set_xlabel('Time (seconds)', fontsize=12)
    axes[4].set_ylim(-0.5, 1.5)
    axes[4].legend(loc='upper right', fontsize=8)
    axes[4].grid(True, alpha=0.3)
    
    plt.tight_layout()
    
    if save_path:
        plt.savefig(save_path, dpi=300, bbox_inches='tight')
        print(f"💾 Static overview saved to: {save_path}")
    
    plt.show()


def main():
    """Main function for animated VAP visualization"""
    parser = argparse.ArgumentParser(description='Animated VAP Visualization')
    parser.add_argument('--results_path', type=str, required=True,
                       help='Path to inference results (numpy file)')
    parser.add_argument('--output_dir', type=str, default='output',
                       help='Output directory for animations')
    parser.add_argument('--playback_speed', type=float, default=1.0,
                       help='Playback speed multiplier (1.0 = real-time)')
    parser.add_argument('--save_mp4', action='store_true',
                       help='Save animation as MP4 video')
    parser.add_argument('--save_gif', action='store_true',
                       help='Save animation as GIF')
    parser.add_argument('--static_only', action='store_true',
                       help='Create only static overview plot')
    
    args = parser.parse_args()
    
    # Load results
    results = np.load(args.results_path, allow_pickle=True).item()
    
    # Create output directory
    output_dir = Path(args.output_dir)
    output_dir.mkdir(exist_ok=True)
    
    if args.static_only:
        # Create static overview
        static_path = output_dir / "conversation_overview.png"
        create_static_overview(results, str(static_path))
    else:
        # Create animated visualization
        visualizer = AnimatedVAPVisualizer(playback_speed=args.playback_speed)
        
        # Determine save paths
        save_path = None
        if args.save_mp4:
            save_path = output_dir / "vap_animation.mp4"
        elif args.save_gif:
            save_path = output_dir / "vap_animation.gif"
        
        # Create and run animation
        anim = visualizer.create_animation(results, str(save_path) if save_path else None)


if __name__ == "__main__":
    main()

