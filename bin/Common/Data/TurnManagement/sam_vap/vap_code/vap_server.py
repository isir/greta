#!/usr/bin/env python3
"""
VAP Inference Server
Runs VAP model inference and serves predictions via WebSocket
Similar to VAP-Realtime server architecture
"""

import asyncio
import websockets
import json
import torch
import torchaudio
import numpy as np
import time
import threading
import queue
from pathlib import Path
from typing import Dict, Optional, List
import argparse
import logging

# Import VAP components
from model import VapGPT, VapConfig
import torch.nn.functional as F


class VAPInferenceServer:
    """
    VAP model inference server
    Processes audio chunks and serves predictions via WebSocket
    """
    
    def __init__(self, model_path: str, device: str = "cuda", 
                 chunk_duration: float = 0.5, context_duration: float = 5.0,
                 model_config_overrides: Optional[Dict] = None):
        """
        Initialize VAP inference server
        
        Args:
            model_path: Path to VAP model checkpoint
            device: Device for inference
            chunk_duration: Audio chunk duration in seconds
            context_duration: Context buffer duration in seconds
            model_config_overrides: Dictionary of model config overrides
        """
        self.model_path = model_path
        self.device = device
        self.chunk_duration = chunk_duration
        self.context_duration = context_duration
        self.model_config_overrides = model_config_overrides or {}
        
        # Model components
        self.model = None
        self.conf = None
        
        # Server state
        self.clients = set()
        self.audio_buffer = []
        self.context_buffer = []
        self.sample_rate = 16000
        self.is_processing = False
        
        # Load model
        self._load_model()
        
        # Setup logging
        logging.basicConfig(level=logging.INFO)
        self.logger = logging.getLogger(__name__)
        
    def _load_model(self):
        """Load VAP model from checkpoint"""
        self.logger.info(f"Loading VAP model from {self.model_path}")
        
        try:
            # Load checkpoint (allow config classes for PyTorch 2.6+ compatibility)
            from finetune import OptConfig, DataConfig
            from events import EventConfig
            
            # Add config classes to main module namespace for pickle compatibility
            import sys
            current_module = sys.modules[__name__]
            current_module.OptConfig = OptConfig
            current_module.DataConfig = DataConfig
            current_module.EventConfig = EventConfig
            current_module.VapConfig = VapConfig
            
            torch.serialization.add_safe_globals([VapConfig, OptConfig, DataConfig, EventConfig])
            checkpoint = torch.load(self.model_path, map_location=self.device, weights_only=False)
            
            # Extract configuration
            if 'hyper_parameters' in checkpoint:
                conf_dict = checkpoint['hyper_parameters']['conf']
                if isinstance(conf_dict, dict):
                    self.conf = VapConfig(**conf_dict)
                else:
                    self.conf = conf_dict
            else:
                self.conf = VapConfig()
            
            # Apply config overrides BEFORE model creation
            if self.model_config_overrides:
                self.logger.info(f"Applying model config overrides:")
                for key, value in self.model_config_overrides.items():
                    if hasattr(self.conf, key):
                        old_value = getattr(self.conf, key)
                        setattr(self.conf, key, value)
                        self.logger.info(f"   - {key}: {old_value} → {value}")
                    else:
                        self.logger.warning(f"   - Unknown config parameter '{key}' ignored")
            else:
                # Default to audio-only configuration if no overrides
                self.conf.multimodal = False
                self.conf.predict_backchannels = False
                self.conf.predict_head_nods = False
            
            # Initialize model
            self.model = VapGPT(self.conf)
            
            # Load state dict
            if 'state_dict' in checkpoint:
                state_dict = checkpoint['state_dict']
                state_dict = {k.replace('model.', ''): v for k, v in state_dict.items()}
            else:
                state_dict = checkpoint
                
            self.model.load_state_dict(state_dict, strict=False)
            self.model.eval()
            self.model.to(self.device)
            
            self.logger.info("✅ VAP model loaded successfully!")
            self.logger.info(f"   - Device: {self.device}")
            self.logger.info(f"   - Sample rate: {self.sample_rate} Hz")
            self.logger.info(f"   - Frame rate: {self.conf.frame_hz} Hz")
            self.logger.info(f"   - Context duration: {self.context_duration}s")
            self.logger.info(f"   - Chunk duration: {self.chunk_duration}s")
            self.logger.info("📋 Model Configuration:")
            self.logger.info(f"     • Audio only: {not (self.conf.use_face_encoder or self.conf.use_gaze_encoder or self.conf.use_body_encoder)}")
            self.logger.info(f"     • Semantics: {self.conf.use_semantic}")
            self.logger.info(f"     • Context limit: {getattr(self.conf, 'vap_context_limit', 'unlimited')}s")
            self.logger.info(f"     • Multimodal: {self.conf.multimodal}")
            self.logger.info(f"     • BC prediction: {self.conf.predict_backchannels}")
            self.logger.info(f"     • Head nod prediction: {self.conf.predict_head_nods}")
            self.logger.info(f"     • Face encoder: {self.conf.use_face_encoder}")
            self.logger.info(f"     • Gaze encoder: {self.conf.use_gaze_encoder}")
            self.logger.info(f"     • Body encoder: {self.conf.use_body_encoder}")
            
        except Exception as e:
            self.logger.error(f"❌ Failed to load model: {e}")
            raise
            
    async def register_client(self, websocket, path):
        """Register new client connection"""
        self.clients.add(websocket)
        client_id = id(websocket)
        self.logger.info(f"🔗 Client {client_id} connected")
        
        try:
            # Send initial configuration
            config_msg = {
                'type': 'config',
                'sample_rate': self.sample_rate,
                'chunk_duration': self.chunk_duration,
                'frame_hz': self.conf.frame_hz,
                'model_config': {
                    'sample_rate': self.conf.sample_rate,
                    'frame_hz': self.conf.frame_hz,
                    'multimodal': self.conf.multimodal
                }
            }
            await websocket.send(json.dumps(config_msg))
            
            # Handle client messages
            async for message in websocket:
                await self._handle_client_message(websocket, message)
                
        except websockets.exceptions.ConnectionClosed:
            pass
        except Exception as e:
            self.logger.error(f"❌ Client {client_id} error: {e}")
        finally:
            self.clients.remove(websocket)
            self.logger.info(f"🔌 Client {client_id} disconnected")
            
    async def _handle_client_message(self, websocket, message):
        """Handle incoming client message"""
        try:
            data = json.loads(message)
            msg_type = data.get('type')
            
            if msg_type == 'audio_chunk':
                # Receive audio chunk from client
                await self._process_audio_chunk(data)
                
            elif msg_type == 'start_processing':
                # Start processing audio file
                await self._start_audio_processing(data)
                
            elif msg_type == 'stop_processing':
                # Stop processing
                self.is_processing = False
                
            elif msg_type == 'ping':
                # Respond to ping
                pong_msg = {'type': 'pong', 'timestamp': time.time()}
                await websocket.send(json.dumps(pong_msg))
                
        except Exception as e:
            self.logger.error(f"❌ Error handling message: {e}")
            
    async def _process_audio_chunk(self, data):
        """Process incoming audio chunk and run inference"""
        try:
            # Extract audio data
            audio1_data = np.array(data['audio1'], dtype=np.float32)
            audio2_data = np.array(data['audio2'], dtype=np.float32)
            timestamp = data['timestamp']
            chunk_id = data.get('chunk_id', 0)
            
            # Add to context buffer
            self.context_buffer.append({
                'audio1': audio1_data,
                'audio2': audio2_data,
                'timestamp': timestamp
            })
            
            # Maintain context window
            context_samples = int(self.context_duration * self.sample_rate)
            total_samples = sum(len(chunk['audio1']) for chunk in self.context_buffer)
            
            while total_samples > context_samples and len(self.context_buffer) > 1:
                removed = self.context_buffer.pop(0)
                total_samples -= len(removed['audio1'])
                
            # Run inference
            inference_start = time.time()
            result = await self._run_inference()
            inference_time = time.time() - inference_start
            
            if result is not None:
                # Prepare response
                response = {
                    'type': 'vap_prediction',
                    'timestamp': timestamp,
                    'chunk_id': chunk_id,
                    'inference_time': inference_time,
                    'vap_probs': {
                        'p_now': result['p_now'].tolist(),
                        'p_future': result['p_future'].tolist()
                    },
                    'chunk_frames': result['chunk_frames']
                }
                
                # Broadcast to all clients
                await self._broadcast_message(response)
                
        except Exception as e:
            self.logger.error(f"❌ Error processing audio chunk: {e}")
            
    async def _run_inference(self):
        """Run VAP inference on current context"""
        if not self.context_buffer:
            return None
            
        try:
            # Concatenate context audio
            audio1_context = np.concatenate([chunk['audio1'] for chunk in self.context_buffer])
            audio2_context = np.concatenate([chunk['audio2'] for chunk in self.context_buffer])
            
            # Prepare batch
            waveform = torch.stack([
                torch.from_numpy(audio1_context),
                torch.from_numpy(audio2_context)
            ], dim=0).unsqueeze(0).float().to(self.device)
            
            batch = {'waveform': waveform}
            
            # Add dummy semantics if needed
            if self.conf.use_whisper_semantics:
                audio_duration_sec = waveform.shape[-1] / self.conf.sample_rate
                semantic_frames = int(audio_duration_sec * self.conf.frame_hz)
                semantic_dim = 1024
                batch['whisper_semantics'] = torch.zeros(
                    1, semantic_frames, semantic_dim, device=self.device
                )
                
            # Run inference
            with torch.no_grad():
                output = self.model(batch)
                
            # Extract predictions
            logits = output['logits']
            vap_probs = self.model.objective.get_probs(logits)
            
            # Get predictions for latest chunk
            latest_chunk = self.context_buffer[-1]
            chunk_duration = len(latest_chunk['audio1']) / self.sample_rate
            chunk_frames = int(chunk_duration * self.conf.frame_hz)
            
            result = {
                'p_now': vap_probs['p_now'][0, -chunk_frames:].cpu().numpy(),
                'p_future': vap_probs['p_future'][0, -chunk_frames:].cpu().numpy(),
                'chunk_frames': chunk_frames
            }
            
            return result
            
        except Exception as e:
            self.logger.error(f"❌ Inference error: {e}")
            return None
            
    async def _start_audio_processing(self, data):
        """Start processing audio files"""
        try:
            audio1_path = data['audio1_path']
            audio2_path = data['audio2_path']
            playback_speed = data.get('playback_speed', 1.0)
            
            self.logger.info(f"🎵 Starting audio processing: {audio1_path}, {audio2_path}")
            
            # Start audio processing in background thread
            thread = threading.Thread(
                target=self._audio_processing_thread,
                args=(audio1_path, audio2_path, playback_speed),
                daemon=True
            )
            thread.start()
            
        except Exception as e:
            self.logger.error(f"❌ Error starting audio processing: {e}")
            
    def _audio_processing_thread(self, audio1_path: str, audio2_path: str, playback_speed: float):
        """Audio processing thread (runs in background)"""
        try:
            # Load audio files
            audio1, sr1 = torchaudio.load(audio1_path)
            audio2, sr2 = torchaudio.load(audio2_path)
            
            # Convert to mono
            if audio1.shape[0] > 1:
                audio1 = audio1.mean(dim=0, keepdim=True)
            if audio2.shape[0] > 1:
                audio2 = audio2.mean(dim=0, keepdim=True)
                
            # Resample if needed
            if sr1 != self.sample_rate:
                resampler = torchaudio.transforms.Resample(sr1, self.sample_rate)
                audio1 = resampler(audio1)
            if sr2 != self.sample_rate:
                resampler = torchaudio.transforms.Resample(sr2, self.sample_rate)
                audio2 = resampler(audio2)
                
            # Ensure same length
            min_length = min(audio1.shape[1], audio2.shape[1])
            audio1 = audio1[0, :min_length].numpy()
            audio2 = audio2[0, :min_length].numpy()
            
            # Process in chunks
            chunk_samples = int(self.chunk_duration * self.sample_rate)
            total_duration = min_length / self.sample_rate
            current_position = 0.0
            chunk_id = 0
            
            self.is_processing = True
            self.logger.info(f"🔄 Processing {total_duration:.2f}s audio in {self.chunk_duration}s chunks")
            
            while self.is_processing and current_position < total_duration:
                # Extract chunk
                start_sample = int(current_position * self.sample_rate)
                end_sample = start_sample + chunk_samples
                
                audio1_chunk = audio1[start_sample:end_sample]
                audio2_chunk = audio2[start_sample:end_sample]
                
                # Pad if necessary
                if len(audio1_chunk) < chunk_samples:
                    audio1_chunk = np.pad(audio1_chunk, (0, chunk_samples - len(audio1_chunk)))
                    audio2_chunk = np.pad(audio2_chunk, (0, chunk_samples - len(audio2_chunk)))
                    
                # Create message
                chunk_msg = {
                    'type': 'audio_chunk',
                    'timestamp': current_position,
                    'chunk_id': chunk_id,
                    'audio1': audio1_chunk.tolist(),
                    'audio2': audio2_chunk.tolist()
                }
                
                # Send to processing
                asyncio.run_coroutine_threadsafe(
                    self._process_audio_chunk(chunk_msg),
                    asyncio.get_event_loop()
                )
                
                # Advance
                current_position += self.chunk_duration
                chunk_id += 1
                
                # Maintain timing
                time.sleep(self.chunk_duration / playback_speed)
                
            self.logger.info("✅ Audio processing completed")
            
        except Exception as e:
            self.logger.error(f"❌ Audio processing error: {e}")
            
    async def _broadcast_message(self, message):
        """Broadcast message to all connected clients"""
        if self.clients:
            # Send to all clients
            await asyncio.gather(
                *[client.send(json.dumps(message)) for client in self.clients],
                return_exceptions=True
            )
            
    async def start_server(self, host: str = "localhost", port: int = 8765):
        """Start the WebSocket server"""
        self.logger.info(f"🚀 Starting VAP server on {host}:{port}")
        
        async with websockets.serve(self.register_client, host, port):
            self.logger.info("✅ VAP server running...")
            await asyncio.Future()  # Run forever


def main():
    """Main function for VAP server"""
    parser = argparse.ArgumentParser(description='VAP Inference Server')
    parser.add_argument('--model_path', type=str, required=True,
                       help='Path to VAP model checkpoint')
    parser.add_argument('--host', type=str, default='localhost',
                       help='Server host (default: localhost)')
    parser.add_argument('--port', type=int, default=8765,
                       help='Server port (default: 8765)')
    parser.add_argument('--device', type=str, default='cuda',
                       help='Device for inference (cuda/cpu)')
    parser.add_argument('--chunk_duration', type=float, default=0.5,
                       help='Audio chunk duration in seconds')
    parser.add_argument('--context_duration', type=float, default=5.0,
                       help='Context buffer duration in seconds')
    
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
    
    # Create and start server
    server = VAPInferenceServer(
        args.model_path, 
        args.device,
        args.chunk_duration,
        args.context_duration,
        model_config_overrides
    )
    
    try:
        asyncio.run(server.start_server(args.host, args.port))
    except KeyboardInterrupt:
        print("\n⏹️  Server stopped by user")


if __name__ == "__main__":
    main()
