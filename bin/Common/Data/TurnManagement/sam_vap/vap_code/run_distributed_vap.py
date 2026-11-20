#!/usr/bin/env python3
"""
Distributed VAP System Launcher
Starts both VAP server and visualization client
Similar to VAP-Realtime distributed architecture
"""

import subprocess
import time
import argparse
import sys
import os
import signal
from pathlib import Path


class DistributedVAPLauncher:
    """
    Launcher for distributed VAP system
    Manages server and client processes
    """
    
    def __init__(self):
        self.server_process = None
        self.client_process = None
        
    def start_server(self, model_path: str, host: str = "localhost", port: int = 8765,
                    device: str = "cuda", chunk_duration: float = 0.5, 
                    model_config_overrides: dict = None):
        """Start VAP inference server"""
        print(f"🚀 Starting VAP server on {host}:{port}")
        
        server_cmd = [
            sys.executable, "vap_server.py",
            "--model_path", model_path,
            "--host", host,
            "--port", str(port),
            "--device", device,
            "--chunk_duration", str(chunk_duration)
        ]
        
        # Add model config overrides
        if model_config_overrides:
            for key, value in model_config_overrides.items():
                server_cmd.extend([f"--{key}", str(value)])
        
        try:
            self.server_process = subprocess.Popen(
                server_cmd,
                stdout=subprocess.PIPE,
                stderr=subprocess.STDOUT,
                universal_newlines=True,
                bufsize=1
            )
            
            print("✅ VAP server started")
            return True
            
        except Exception as e:
            print(f"❌ Failed to start server: {e}")
            return False
            
    def start_client(self, audio1_path: str, audio2_path: str, 
                    server_host: str = "localhost", server_port: int = 8765,
                    playback_speed: float = 1.0, window_duration: float = 10.0):
        """Start VAP visualization client"""
        print(f"🎨 Starting VAP client connecting to {server_host}:{server_port}")
        
        client_cmd = [
            sys.executable, "vap_client.py",
            "--audio1", audio1_path,
            "--audio2", audio2_path,
            "--server_host", server_host,
            "--server_port", str(server_port),
            "--playback_speed", str(playback_speed),
            "--window_duration", str(window_duration)
        ]
        
        try:
            self.client_process = subprocess.Popen(
                client_cmd,
                stdout=subprocess.PIPE,
                stderr=subprocess.STDOUT,
                universal_newlines=True,
                bufsize=1
            )
            
            print("✅ VAP client started")
            return True
            
        except Exception as e:
            print(f"❌ Failed to start client: {e}")
            return False
            
    def wait_for_server_ready(self, timeout: int = 30):
        """Wait for server to be ready"""
        print("⏳ Waiting for server to be ready...")
        
        import socket
        import time
        
        start_time = time.time()
        while time.time() - start_time < timeout:
            try:
                # Try to connect to server port
                sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                sock.settimeout(1)
                result = sock.connect_ex(('localhost', 8765))
                sock.close()
                
                if result == 0:
                    print("✅ Server is ready")
                    return True
                    
            except Exception:
                pass
                
            time.sleep(1)
            
        print("❌ Server not ready within timeout")
        return False
        
    def monitor_processes(self):
        """Monitor server and client processes"""
        print("👀 Monitoring processes...")
        print("   Press Ctrl+C to stop both server and client")
        
        try:
            while True:
                # Check server status
                if self.server_process and self.server_process.poll() is not None:
                    print("⚠️  Server process ended")
                    break
                    
                # Check client status
                if self.client_process and self.client_process.poll() is not None:
                    print("⚠️  Client process ended")
                    break
                    
                time.sleep(1)
                
        except KeyboardInterrupt:
            print("\n⏹️  Stopping distributed VAP system...")
            
        finally:
            self.cleanup()
            
    def cleanup(self):
        """Clean up processes"""
        print("🧹 Cleaning up processes...")
        
        # Stop client first
        if self.client_process:
            try:
                self.client_process.terminate()
                self.client_process.wait(timeout=5)
                print("✅ Client stopped")
            except Exception as e:
                print(f"⚠️  Error stopping client: {e}")
                try:
                    self.client_process.kill()
                except:
                    pass
                    
        # Stop server
        if self.server_process:
            try:
                self.server_process.terminate()
                self.server_process.wait(timeout=5)
                print("✅ Server stopped")
            except Exception as e:
                print(f"⚠️  Error stopping server: {e}")
                try:
                    self.server_process.kill()
                except:
                    pass
                    
    def run_distributed_system(self, model_path: str, audio1_path: str, audio2_path: str,
                              host: str = "localhost", port: int = 8765,
                              device: str = "cuda", chunk_duration: float = 0.5,
                              playback_speed: float = 1.0, window_duration: float = 10.0,
                              model_config_overrides: dict = None):
        """Run complete distributed VAP system"""
        
        print("🌐 DISTRIBUTED VAP SYSTEM")
        print("=" * 50)
        print(f"📋 Configuration:")
        print(f"   Model: {model_path}")
        print(f"   Audio 1: {audio1_path}")
        print(f"   Audio 2: {audio2_path}")
        print(f"   Server: {host}:{port}")
        print(f"   Device: {device}")
        print(f"   Chunk duration: {chunk_duration}s")
        print(f"   Playback speed: {playback_speed}x")
        print("")
        
        try:
            # Start server
            if not self.start_server(model_path, host, port, device, chunk_duration, model_config_overrides):
                return False
                
            # Wait for server to be ready
            if not self.wait_for_server_ready():
                return False
                
            # Start client
            if not self.start_client(audio1_path, audio2_path, host, port, 
                                   playback_speed, window_duration):
                return False
                
            # Monitor processes
            self.monitor_processes()
            
            return True
            
        except Exception as e:
            print(f"❌ System error: {e}")
            self.cleanup()
            return False


def main():
    """Main function for distributed VAP launcher"""
    parser = argparse.ArgumentParser(description='Distributed VAP System Launcher')
    
    # Required arguments
    parser.add_argument('--model_path', type=str, required=True,
                       help='Path to VAP model checkpoint')
    parser.add_argument('--audio1', type=str, required=True,
                       help='Path to speaker 1 audio file')
    parser.add_argument('--audio2', type=str, required=True,
                       help='Path to speaker 2 audio file')
    
    # Server configuration
    parser.add_argument('--host', type=str, default='localhost',
                       help='Server host (default: localhost)')
    parser.add_argument('--port', type=int, default=8765,
                       help='Server port (default: 8765)')
    parser.add_argument('--device', type=str, default='cuda',
                       help='Device for inference (cuda/cpu)')
    parser.add_argument('--chunk_duration', type=float, default=0.5,
                       help='Audio chunk duration in seconds')
    
    # Client configuration
    parser.add_argument('--playback_speed', type=float, default=1.0,
                       help='Playback speed multiplier')
    parser.add_argument('--window_duration', type=float, default=10.0,
                       help='Visualization window duration')
    
    # Mode selection
    parser.add_argument('--server_only', action='store_true',
                       help='Start only the server')
    parser.add_argument('--client_only', action='store_true',
                       help='Start only the client (server must be running)')
    
    # Model configuration arguments
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
    parser.add_argument('--multimodal', action='store_true',
                       help='Enable multimodal features')
    parser.add_argument('--predict_backchannels', action='store_true',
                       help='Enable backchannel prediction')
    parser.add_argument('--predict_head_nods', action='store_true',
                       help='Enable head nod prediction')
    parser.add_argument('--use_face_encoder', action='store_true',
                       help='Enable face encoder')
    parser.add_argument('--use_gaze_encoder', action='store_true',
                       help='Enable gaze encoder')
    parser.add_argument('--use_body_encoder', action='store_true',
                       help='Enable body encoder')
    parser.add_argument('--pretrained_face_encoder', type=str,
                       help='Path to pretrained face encoder model (FormerDFER)')
    
    args = parser.parse_args()
    
    # Validate files exist
    if not Path(args.model_path).exists():
        print(f"❌ Model file not found: {args.model_path}")
        return 1
        
    if not args.client_only:
        if not Path(args.audio1).exists():
            print(f"❌ Audio file not found: {args.audio1}")
            return 1
        if not Path(args.audio2).exists():
            print(f"❌ Audio file not found: {args.audio2}")
            return 1
    
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
    other_args = ['multimodal', 'predict_backchannels', 'predict_head_nods',
                  'use_face_encoder', 'use_gaze_encoder', 'use_body_encoder',
                  'pretrained_face_encoder']
    for arg in other_args:
        if hasattr(args, arg):
            value = getattr(args, arg)
            # For boolean args (store_true), only override if explicitly set to True
            # For string/numeric args, only override if not None
            if (isinstance(value, bool) and value) or (not isinstance(value, bool) and value is not None):
                model_config_overrides[arg] = value
    
    # Create launcher
    launcher = DistributedVAPLauncher()
    
    try:
        if args.server_only:
            # Start only server
            print("🖥️  SERVER-ONLY MODE")
            if launcher.start_server(args.model_path, args.host, args.port, 
                                   args.device, args.chunk_duration, model_config_overrides):
                print("✅ Server running. Press Ctrl+C to stop.")
                launcher.monitor_processes()
            
        elif args.client_only:
            # Start only client
            print("🖥️  CLIENT-ONLY MODE")
            if launcher.start_client(args.audio1, args.audio2, args.host, args.port,
                                   args.playback_speed, args.window_duration):
                print("✅ Client running. Close visualization to stop.")
                launcher.monitor_processes()
            
        else:
            # Start both server and client
            success = launcher.run_distributed_system(
                args.model_path, args.audio1, args.audio2,
                args.host, args.port, args.device, args.chunk_duration,
                args.playback_speed, args.window_duration, model_config_overrides
            )
            
            return 0 if success else 1
            
    except KeyboardInterrupt:
        print("\n⏹️  Stopped by user")
        return 0


if __name__ == "__main__":
    exit(main())
