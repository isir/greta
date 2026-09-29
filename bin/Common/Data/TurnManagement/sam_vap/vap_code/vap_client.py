#!/usr/bin/env python3
"""
VAP Visualization Client
Connects to VAP server and displays real-time visualizations
Similar to VAP-Realtime client architecture
"""

import asyncio
import websockets
import json
import numpy as np
import matplotlib.pyplot as plt
from matplotlib.animation import FuncAnimation
import threading
import queue
import time
from typing import Dict, Optional, List
from dataclasses import dataclass
import argparse
import logging


@dataclass
class VAPPrediction:
    """VAP prediction data from server"""
    timestamp: float
    chunk_id: int
    inference_time: float
    p_now: np.ndarray
    p_future: np.ndarray
    chunk_frames: int


class VAPVisualizationClient:
    """
    VAP visualization client
    Connects to VAP server and displays real-time predictions
    """
    
    def __init__(self, server_host: str = "localhost", server_port: int = 8765,
                 window_duration: float = 10.0, figsize: tuple = (16, 10)):
        """
        Initialize VAP visualization client
        
        Args:
            server_host: VAP server host
            server_port: VAP server port
            window_duration: Visualization window duration
            figsize: Figure size
        """
        self.server_host = server_host
        self.server_port = server_port
        self.server_url = f"ws://{server_host}:{server_port}"
        self.window_duration = window_duration
        self.figsize = figsize
        
        # Connection state
        self.websocket = None
        self.connected = False
        self.server_config = None
        
        # Data storage
        self.prediction_queue = queue.Queue()
        self.predictions = []
        self.current_time = 0.0
        
        # Visualization components
        self.fig = None
        self.axes = None
        self.lines = {}
        self.animation = None
        
        # Threading
        self.running = threading.Event()
        
        # Setup logging
        logging.basicConfig(level=logging.INFO)
        self.logger = logging.getLogger(__name__)
        
    async def connect_to_server(self):
        """Connect to VAP server"""
        try:
            self.logger.info(f"🔗 Connecting to VAP server at {self.server_url}")
            
            self.websocket = await websockets.connect(self.server_url)
            self.connected = True
            
            self.logger.info("✅ Connected to VAP server")
            
            # Start message handling
            await self._handle_server_messages()
            
        except Exception as e:
            self.logger.error(f"❌ Connection failed: {e}")
            self.connected = False
            
    async def _handle_server_messages(self):
        """Handle incoming messages from server"""
        try:
            async for message in self.websocket:
                data = json.loads(message)
                await self._process_server_message(data)
                
        except websockets.exceptions.ConnectionClosed:
            self.logger.info("🔌 Server connection closed")
            self.connected = False
        except Exception as e:
            self.logger.error(f"❌ Message handling error: {e}")
            
    async def _process_server_message(self, data):
        """Process incoming server message"""
        msg_type = data.get('type')
        
        if msg_type == 'config':
            # Server configuration
            self.server_config = data
            self.logger.info(f"📋 Received server config: {data['model_config']}")
            
        elif msg_type == 'vap_prediction':
            # VAP prediction result
            prediction = VAPPrediction(
                timestamp=data['timestamp'],
                chunk_id=data['chunk_id'],
                inference_time=data['inference_time'],
                p_now=np.array(data['vap_probs']['p_now']),
                p_future=np.array(data['vap_probs']['p_future']),
                chunk_frames=data['chunk_frames']
            )
            
            # Add to queue for visualization
            try:
                self.prediction_queue.put(prediction, timeout=0.1)
            except queue.Full:
                self.logger.warning("⚠️  Prediction queue full, dropping data")
                
        elif msg_type == 'pong':
            # Server ping response
            latency = time.time() - data['timestamp']
            self.logger.debug(f"🏓 Server latency: {latency*1000:.1f}ms")
            
    async def send_audio_processing_request(self, audio1_path: str, audio2_path: str, 
                                          playback_speed: float = 1.0):
        """Request server to start processing audio files"""
        if not self.connected:
            self.logger.error("❌ Not connected to server")
            return
            
        request = {
            'type': 'start_processing',
            'audio1_path': audio1_path,
            'audio2_path': audio2_path,
            'playback_speed': playback_speed
        }
        
        await self.websocket.send(json.dumps(request))
        self.logger.info(f"🎵 Requested audio processing: {audio1_path}, {audio2_path}")
        
    async def send_ping(self):
        """Send ping to server"""
        if self.connected:
            ping_msg = {'type': 'ping', 'timestamp': time.time()}
            await self.websocket.send(json.dumps(ping_msg))
            
    def setup_visualization(self):
        """Setup real-time visualization"""
        self.logger.info("🎨 Setting up visualization...")
        
        # Create figure and subplots
        self.fig, self.axes = plt.subplots(4, 1, figsize=self.figsize, sharex=True)
        
        # Configure subplots
        self.axes[0].set_ylabel('VAP P_now\nProbabilities', fontsize=10)
        self.axes[0].set_title('Real-time VAP Predictions from Server', fontsize=14, fontweight='bold')
        self.axes[0].grid(True, alpha=0.3)
        self.axes[0].set_ylim(0, 1)
        
        self.axes[1].set_ylabel('VAP P_future\nProbabilities', fontsize=10)
        self.axes[1].grid(True, alpha=0.3)
        self.axes[1].set_ylim(0, 1)
        
        self.axes[2].set_ylabel('Inference Time\n(ms)', fontsize=10)
        self.axes[2].grid(True, alpha=0.3)
        self.axes[2].set_ylim(0, 200)
        
        self.axes[3].set_ylabel('Server Status', fontsize=10)
        self.axes[3].set_xlabel('Time (seconds)', fontsize=12)
        self.axes[3].grid(True, alpha=0.3)
        self.axes[3].set_ylim(-0.5, 1.5)
        
        # Initialize lines
        vap_labels = ['Hold', 'Shift', 'Backchannel', 'Silence']
        colors = ['green', 'orange', 'purple', 'gray']
        
        # P_now lines
        for i, (label, color) in enumerate(zip(vap_labels, colors)):
            line, = self.axes[0].plot([], [], label=label, color=color, linewidth=2.0)
            self.lines[f'p_now_{i}'] = line
            
        # P_future lines
        for i, (label, color) in enumerate(zip(vap_labels, colors)):
            line, = self.axes[1].plot([], [], label=label, color=color, linewidth=2.0)
            self.lines[f'p_future_{i}'] = line
            
        # Inference time line
        self.lines['inference_time'], = self.axes[2].plot([], [], 'b-', linewidth=2.0, label='Inference Time')
        
        # Connection status
        self.lines['connection'], = self.axes[3].plot([], [], 'go', markersize=8, label='Connected')
        
        # Current time indicator
        self.current_time_lines = []
        for ax in self.axes:
            line = ax.axvline(x=0, color='red', linestyle='-', linewidth=2, alpha=0.8)
            self.current_time_lines.append(line)
            
        # Legends
        self.axes[0].legend(loc='upper right', fontsize=8)
        self.axes[1].legend(loc='upper right', fontsize=8)
        self.axes[2].legend(loc='upper right', fontsize=8)
        self.axes[3].legend(loc='upper right', fontsize=8)
        
        plt.tight_layout()
        
    def start_visualization_updates(self):
        """Start visualization update thread"""
        self.running.set()
        self.update_thread = threading.Thread(target=self._visualization_update_loop, daemon=True)
        self.update_thread.start()
        self.logger.info("🔄 Started visualization updates")
        
    def stop_visualization_updates(self):
        """Stop visualization updates"""
        self.running.clear()
        if hasattr(self, 'update_thread'):
            self.update_thread.join(timeout=1.0)
        self.logger.info("⏹️  Stopped visualization updates")
        
    def _visualization_update_loop(self):
        """Visualization update loop (runs in thread)"""
        while self.running.is_set():
            # Get latest predictions
            try:
                prediction = self.prediction_queue.get(timeout=0.1)
                self.predictions.append(prediction)
                self.current_time = prediction.timestamp
                
                # Update visualization
                self._update_plots()
                
            except queue.Empty:
                pass
            except Exception as e:
                self.logger.error(f"❌ Visualization update error: {e}")
                
            time.sleep(0.05)  # 20 FPS updates
            
    def _update_plots(self):
        """Update all plots with latest data"""
        if not self.predictions:
            return
            
        # Filter data to current window
        cutoff_time = self.current_time - self.window_duration
        self.predictions = [p for p in self.predictions if p.timestamp >= cutoff_time]
        
        if not self.predictions:
            return
            
        # Collect data for plotting
        timestamps = []
        all_p_now = []
        all_p_future = []
        inference_times = []
        
        for pred in self.predictions:
            # Create time axis for this prediction
            chunk_duration = pred.chunk_frames / 50.0  # Assuming 50 FPS
            chunk_times = np.linspace(pred.timestamp, 
                                    pred.timestamp + chunk_duration,
                                    pred.chunk_frames)
            
            timestamps.extend(chunk_times)
            all_p_now.append(pred.p_now)
            all_p_future.append(pred.p_future)
            inference_times.append(pred.inference_time * 1000)  # Convert to ms
            
        if all_p_now:
            # Concatenate VAP data
            p_now_concat = np.concatenate(all_p_now, axis=0)
            p_future_concat = np.concatenate(all_p_future, axis=0)
            times_array = np.array(timestamps)
            
            # Update P_now lines
            for i in range(min(4, p_now_concat.shape[1])):
                if f'p_now_{i}' in self.lines:
                    self.lines[f'p_now_{i}'].set_data(times_array, p_now_concat[:, i])
                    
            # Update P_future lines
            for i in range(min(4, p_future_concat.shape[1])):
                if f'p_future_{i}' in self.lines:
                    self.lines[f'p_future_{i}'].set_data(times_array, p_future_concat[:, i])
                    
            # Update inference time
            pred_times = [p.timestamp for p in self.predictions]
            self.lines['inference_time'].set_data(pred_times, inference_times)
            
            # Update connection status
            connection_times = pred_times
            connection_status = [1.0 if self.connected else 0.0] * len(connection_times)
            self.lines['connection'].set_data(connection_times, connection_status)
            
        # Update time window
        start_time = max(0, self.current_time - self.window_duration)
        end_time = self.current_time + 1.0
        
        for ax in self.axes:
            ax.set_xlim(start_time, end_time)
            
        # Update current time indicator
        for line in self.current_time_lines:
            line.set_xdata([self.current_time, self.current_time])
            
        # Update title with stats
        if self.predictions:
            avg_inference = np.mean([p.inference_time * 1000 for p in self.predictions[-10:]])
            self.axes[0].set_title(
                f'Real-time VAP Predictions - Time: {self.current_time:.1f}s | '
                f'Avg Inference: {avg_inference:.1f}ms | '
                f'Connected: {"✅" if self.connected else "❌"}',
                fontsize=14, fontweight='bold'
            )
            
        # Trigger redraw
        self.fig.canvas.draw_idle()
        
    async def run_client(self, audio1_path: str, audio2_path: str, 
                        playback_speed: float = 1.0):
        """Run complete client session"""
        try:
            # Setup visualization
            self.setup_visualization()
            
            # Start visualization updates
            self.start_visualization_updates()
            
            # Connect to server
            await self.connect_to_server()
            
            if self.connected:
                # Request audio processing
                await self.send_audio_processing_request(audio1_path, audio2_path, playback_speed)
                
                # Show visualization
                plt.show()
                
        except Exception as e:
            self.logger.error(f"❌ Client error: {e}")
        finally:
            # Cleanup
            self.stop_visualization_updates()
            if self.websocket:
                await self.websocket.close()


async def main():
    """Main function for VAP client"""
    parser = argparse.ArgumentParser(description='VAP Visualization Client')
    parser.add_argument('--audio1', type=str, required=True,
                       help='Path to speaker 1 audio file')
    parser.add_argument('--audio2', type=str, required=True,
                       help='Path to speaker 2 audio file')
    parser.add_argument('--server_host', type=str, default='localhost',
                       help='VAP server host (default: localhost)')
    parser.add_argument('--server_port', type=int, default=8765,
                       help='VAP server port (default: 8765)')
    parser.add_argument('--playback_speed', type=float, default=1.0,
                       help='Playback speed multiplier (default: 1.0)')
    parser.add_argument('--window_duration', type=float, default=10.0,
                       help='Visualization window duration (default: 10.0s)')
    
    args = parser.parse_args()
    
    # Create and run client
    client = VAPVisualizationClient(
        args.server_host, 
        args.server_port,
        args.window_duration
    )
    
    await client.run_client(args.audio1, args.audio2, args.playback_speed)


if __name__ == "__main__":
    asyncio.run(main())

