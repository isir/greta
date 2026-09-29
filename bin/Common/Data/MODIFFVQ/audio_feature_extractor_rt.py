"""
audio_feature_extractor_rt.py
------------------------------
Real-time audio feature extraction for the E3 predictor.

Reuses AudioFeatureExtractor from the project (get_features_time logic)
but reads from the microphone server (port 9000) instead of WAV files.

The mic server (Microphone.py) accepts multiple TCP clients simultaneously,
so DeepGram.py and this extractor can both connect without conflict.

Offline pipeline reference (ActionUnitsDataset3.py):
    bundle    = torchaudio.pipelines.WAV2VEC2_LARGE  (16000 Hz)
    num_layers = 4  ->  features[-4:]  ->  [4, T_audio, 1024]
    chunk      = 0.6s = 9600 samples

AudioEncoder expects: [B, W, L, T_audio, F] = [1, 8, 4, T_audio, 1024]
"""

import socket
import threading
import queue
from collections import deque

import numpy as np
import torch
import torchaudio

# Reuse the project's AudioFeatureExtractor
from model.AudioFeatureExtractor import AudioFeatureExtractor


# =====================================================================
# Config 
# =====================================================================
SAMPLE_RATE    = 16000
CHUNK_DURATION = 0.6                              # seconds per AU chunk
CHUNK_SAMPLES  = int(SAMPLE_RATE * CHUNK_DURATION)  # 9600 samples
NUM_LAYERS     = 4                                # last 4 layers of wav2vec2
BUFFER_SIZE    = 4096                             # bytes per recv() call
MIC_HOST       = socket.gethostname()
MIC_PORT       = 9000


class AudioExtractorRT:
    """
    Connects to the microphone server on port 9000, accumulates raw int16
    audio, extracts wav2vec2 features per chunk using AudioFeatureExtractor,
    and maintains a sliding window of W chunks for the inference loop.
    """

    def __init__(self, device, window=8,
                 mic_host=MIC_HOST, mic_port=MIC_PORT):
        self.device   = device
        self.window   = window
        self.mic_host = mic_host
        self.mic_port = mic_port

        # Reuse project's AudioFeatureExtractor with WAV2VEC2_LARGE
        print("[Audio] Loading WAV2VEC2_LARGE via AudioFeatureExtractor...")
        bundle = torchaudio.pipelines.WAV2VEC2_LARGE
        self.extractor = AudioFeatureExtractor(
            bundle=bundle,
            device=device,
            num_layers=NUM_LAYERS,  # will take features[-4:]
        )
        self.extractor.eval()
        self.sample_rate = bundle.sample_rate  # 16000
        print(f"[Audio] Loaded. Sample rate: {self.sample_rate} Hz, "
              f"num_layers={NUM_LAYERS}")

        # Raw audio accumulation (bytes from mic server)
        self._raw_bytes  = b""
        self._raw_lock   = threading.Lock()

        # Queue of raw audio chunks (bytes) ready for feature extraction
        self._audio_queue  = queue.Queue()

        # Sliding window of extracted feature chunks [4, T_audio, 1024]
        self._window_buf   = deque(maxlen=window)
        self._window_lock  = threading.Lock()

        self._running = False

    def start(self):
        """Connect to mic server and start recv + extraction threads."""
        self._running = True

        # Connect using same protocol as Microphone_client in DeepGram.py
        self._mic_sock = socket.socket()
        self._mic_sock.connect((self.mic_host, self.mic_port))
        print(f"[Audio] Connected to mic server at "
              f"{self.mic_host}:{self.mic_port}")

        self._recv_thread = threading.Thread(
            target=self._recv_loop, daemon=True
        )
        self._extract_thread = threading.Thread(
            target=self._extraction_loop, daemon=True
        )
        self._recv_thread.start()
        self._extract_thread.start()
        print("[Audio] Started.")

    def stop(self):
        self._running = False
        try:
            self._mic_sock.close()
        except Exception:
            pass
        print("[Audio] Stopped.")

    # ------------------------------------------------------------------
    # Receive loop
    # ------------------------------------------------------------------

    def _recv_loop(self):
        """
        Request audio chunks from mic server and push full chunks to queue.
        Protocol: send 'ok', receive BUFFER_SIZE bytes of int16 audio.
        Accumulate until CHUNK_SAMPLES * 2 bytes (one 0.6s chunk) are ready.
        """
        bytes_per_chunk = CHUNK_SAMPLES * 2  # int16 = 2 bytes/sample

        while self._running:
            try:
                self._mic_sock.send(b"ok")
                data = self._mic_sock.recv(BUFFER_SIZE)
                if not data:
                    break

                with self._raw_lock:
                    self._raw_bytes += data
                    while len(self._raw_bytes) >= bytes_per_chunk:
                        chunk = self._raw_bytes[:bytes_per_chunk]
                        self._raw_bytes = self._raw_bytes[bytes_per_chunk:]
                        self._audio_queue.put(chunk)

            except Exception as e:
                if self._running:
                    print(f"[Audio] Recv error: {e}")
                break

    # ------------------------------------------------------------------
    # Feature extraction loop
    # ------------------------------------------------------------------

    def _bytes_to_waveform(self, chunk_bytes):
        """Convert raw int16 bytes to float32 waveform tensor [1, samples]."""
        audio_int16   = np.frombuffer(chunk_bytes, dtype=np.int16)
        audio_float32 = audio_int16.astype(np.float32) / 32768.0
        return torch.tensor(audio_float32).unsqueeze(0)  # [1, CHUNK_SAMPLES]

    def _extract_features(self, chunk_bytes):
        """
        Extract wav2vec2 features from one 0.6s audio chunk.

        Reuses AudioFeatureExtractor.get_features_time logic but on a
        waveform tensor directly (no file I/O needed at inference time).

        Returns: torch.Tensor [L=4, T_audio, 1024] on CPU
        """
        waveform = self._bytes_to_waveform(chunk_bytes).to(self.device)
        # [1, CHUNK_SAMPLES] -> extract_features -> list of [1, T_audio, 1024]
        with torch.no_grad():
            features, _ = self.extractor.model.extract_features(waveform)
        # Take last NUM_LAYERS layers, stack -> [L, T_audio, 1024]
        feats = torch.stack(features[-NUM_LAYERS:], dim=0).squeeze(1)
        return feats.cpu()  # [4, T_audio, 1024]

    def _extraction_loop(self):
        """Pop chunks from queue, extract features, update window."""
        while self._running:
            try:
                chunk_bytes = self._audio_queue.get(timeout=1.0)
            except queue.Empty:
                continue
            try:
                feats = self._extract_features(chunk_bytes)  # [4, T_audio, 1024]
                with self._window_lock:
                    self._window_buf.append(feats)
            except Exception as e:
                print(f"[Audio] Extraction error: {e}")

    # ------------------------------------------------------------------
    # Public interface for the inference loop
    # ------------------------------------------------------------------

    def get_audio_window(self):
        """
        Returns the current W-chunk sliding window as a model-ready tensor,
        or None if the window is not yet full.

        Shape: [1, W, L, T_audio, 1024]
        Matches AudioEncoder.forward expectation: [B, W, L, T_audio, F]
        """
        with self._window_lock:
            if len(self._window_buf) < self.window:
                return None
            stacked = torch.stack(list(self._window_buf), dim=0)  # [W, L, T_audio, 1024]
        return stacked.unsqueeze(0)  # [1, W, L, T_audio, 1024]

    def is_ready(self):
        """True when the sliding window is full."""
        with self._window_lock:
            return len(self._window_buf) >= self.window