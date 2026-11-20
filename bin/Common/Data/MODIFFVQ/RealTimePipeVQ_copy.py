"""
RealTimePipeE3.py
-----------------
Real-time inference pipeline for the E3 predictor (VQ-VAE + conditional
transformer). Receives client AUs from Greta (port 5963, activated via
"SamVap" checkbox), runs inference, and sends predicted therapist AUs
back to Greta (MODIFF_VQFrame on port 5561).

Dialog acts are currently a zero placeholder (see empty_dialog_act).
Mistral integration is pending — when ready, hook ExternalTensorClient
into the loop.

Data flow:
    OpenFace ---> Greta ---> [TCP 5963 CSV, 28 cols] ---> this script
    this script ---> [TCP 5561 CSV, 32 cols] ---> MODIFF_VQFrame ---> avatar
"""

import argparse
import os
import socket as pysocket
import sys
import time
from collections import deque

import numpy as np
import torch
from scipy.interpolate import interp1d
from scipy.ndimage import gaussian_filter1d

from model.MODIF_baseline import AllPipeline
from model.encoders import (AUsEncoder, DialogActEncoder, AudioEncoder,
                            SinusoidalEmbeddings)
from model.vqvae import VQVAE
import socket
import threading
from dialog_act_mapping import empty_dialog_act
from audio_feature_extractor_rt import AudioExtractorRT
from da_client_rt import DAClientRT


class Sender:
    """Sends CSV lines to MODIFF_VQFrame (port 5561) in a background thread."""
    def __init__(self, forward_port, forward_address):
        self.forward_socket  = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.forward_port    = forward_port
        self.forward_address = forward_address
        self.lock  = threading.Lock()
        self.queue = []
        self.connect_to_forwarding_server()
        self.sending_thread = threading.Thread(
            target=self.send_data_continuously, daemon=True)
        self.sending_thread.start()

    def connect_to_forwarding_server(self):
        while True:
            try:
                self.forward_socket.connect(
                    (self.forward_address, self.forward_port))
                print(f"[Sender] Connected to "
                      f"{self.forward_address}:{self.forward_port}")
                break
            except ConnectionRefusedError:
                print("[Sender] MODIFF_VQFrame not ready, retrying in 2s...")
                time.sleep(2)

    def queue_data(self, data):
        with self.lock:
            self.queue.extend(data)

    def send_data_continuously(self):
        while True:
            with self.lock:
                if self.queue:
                    row = self.queue.pop(0)
                    self._send(row)
            time.sleep(0.04)

    def _send(self, data):
        try:
            self.forward_socket.sendall((data + "\n").encode("utf-8"))
        except Exception as e:
            print(f"[Sender] Send error: {e}")


# =====================================================================
# Config
# =====================================================================
# Set to True to enable real-time audio features (requires sounddevice
# and WAV2VEC2_LARGE download on first run).
ADD_AUDIO_CLIENT  = True
ADD_DIALOG_ACTS   = True   # Set to False to use zero placeholder
DA_SERVER_PORT    = 50202  # Port where MICounselorRL sends classified DAs


# Chunking / windowing (must match training)
T_CHUNK   = 18   # frames per chunk (VQ-VAE T)
N_AUS     = 17   # number of AUs
WINDOW    = 8    # W: number of chunks in context

# Model architecture (must match training)
D_MODEL            = 128
N_HEADS            = 8
NUM_LAYERS_T       = 4
DROPOUT            = 0.3
T_DOWN             = 9      # T // 2 = 18 // 2
VQVAE_DZ           = 128
VQVAE_K            = 64
VQVAE_NUM_LAYERS   = 3
AUDIO_DIM          = 1024
AUDIO_LAYERS       = 4
USE_THERAPIST_PREV = False  # E3 does not use therapist_prev
USE_VQVAE_ENC      = False

# Model checkpoints
VQVAE_PATH      = 'model/vqvae_model.pt'
PREDICTOR_PATH  = 'model/predictor_model.pt'
NORM_STATS_PATH = 'model/norm_stats.npy'

# Ports
GRETA_IN_PORT  = 5963   # Greta -> Python (SamVap)
GRETA_OUT_PORT = 5561   # Python -> MODIFF_VQFrame

# Post-processing
FADE_FRAMES = 6
SIGMA       = 3.0
FPS_OUT     = 25
T_TARGET    = 15   # int(round(0.6 * 25)) = 15 frames/chunk at 25fps

# Per-AU amplification (alpha < 1 attenuates, alpha > 1 amplifies)
AMPLIFY = {
    # 'AU01_r': 2.5, 'AU02_r': 2.5,   # eyebrows
    'AU25_r': 0.3, 'AU26_r': 0.3,     # mouth (speaking)
}

# Input parsing: the CSV Greta sends on port 5963 has 28 columns.
# Column 0 = timestamp, columns 1..17 = the 17 AUs, rest = gaze/pose (unused).
AU_START = 1

COLS_AU = [
    'AU01_r', 'AU02_r', 'AU04_r', 'AU05_r', 'AU06_r', 'AU07_r',
    'AU09_r', 'AU10_r', 'AU12_r', 'AU14_r', 'AU15_r', 'AU17_r',
    'AU20_r', 'AU23_r', 'AU25_r', 'AU26_r', 'AU45_r',
]


# =====================================================================
# Model loading
# =====================================================================

def load_model(vqvae_path, predictor_path, device):
    """Load VQ-VAE and E3 predictor with the same config used in training."""
    vqvae = VQVAE(
        in_features=17, d_model=128, dz=VQVAE_DZ,
        K=VQVAE_K, T=T_CHUNK, n_heads=4, num_layers=VQVAE_NUM_LAYERS,
        dropout=0.1, beta=0.25,
    ).to(device)
    ck = torch.load(vqvae_path, map_location=device)
    vqvae.load_state_dict(ck['model_state'])
    vqvae.eval()
    print(f"[Model] VQ-VAE loaded from {vqvae_path}")

    model = AllPipeline(
        client_aus_encoder=AUsEncoder(in_features=17, d_model=D_MODEL),
        therapist_aus_encoder=AUsEncoder(in_features=17, d_model=D_MODEL),
        pos_encoder=SinusoidalEmbeddings(d_model=D_MODEL),
        dialog_act_encoder=DialogActEncoder(d_model=D_MODEL),
        audio_encoder=AudioEncoder(in_features=AUDIO_DIM, d_model=D_MODEL,
                                    num_layers=AUDIO_LAYERS),
        W=WINDOW,
        d_model=D_MODEL,
        n_heads=N_HEADS,
        num_layers_transformer=NUM_LAYERS_T,
        dropout=DROPOUT,
        vqvae=vqvae,
        K=VQVAE_K,
        T_down=T_DOWN,
        use_therapist_prev=USE_THERAPIST_PREV,
        use_vqvae_enc=USE_VQVAE_ENC,
    ).to(device)
    ck2 = torch.load(predictor_path, map_location=device, weights_only=False)
    model.load_state_dict(ck2['model_state'])
    model.eval()
    print(f"[Model] E3 predictor loaded from {predictor_path} "
          f"(epoch={ck2['epoch']+1}, val_pearson={ck2.get('val_pearson', '?')})")
    return model


# =====================================================================
# Normalization helpers
# =====================================================================

def load_norm_stats(path):
    """Load min_x/max_x/min_y/max_y from norm_stats.npy."""
    stats = np.load(path, allow_pickle=True).item()
    return stats['min_x'], stats['max_x'], stats['min_y'], stats['max_y']


def normalize_client(x, min_x, max_x):
    """Min-max normalization of client AUs to [0, 1] (matches training)."""
    return (x - min_x) / (max_x - min_x + 1e-8)


def denormalize_therapist(y, min_y, max_y):
    """Min-max denormalization of therapist AUs back to original scale."""
    return y * (max_y - min_y + 1e-8) + min_y


# =====================================================================
# Dialog acts
# =====================================================================

def build_dialog_act_tensors(dialog_act_window, device):
    """Convert a list of W dialog act dicts to tensors for DialogActEncoder."""
    keys_cat = [
        'client_talk_type', 'reflection_subtype', 'question_subtype',
        'main_therapist_behaviour', 'therapist_input_subtype'
    ]
    z = {}
    for k in keys_cat:
        vals = [da[k] for da in dialog_act_window]
        z[k] = torch.tensor([vals], dtype=torch.float32, device=device)
    silences = [da['silences'] for da in dialog_act_window]
    z['silences'] = torch.tensor([silences], dtype=torch.float32, device=device)
    return z


def default_dialog_acts(device):
    """
    More realistic placeholder than all-zeros:
    - client: neutral talk (2)
    - therapist: reflection/simple (main=1, reflection_subtype=1)
    """
    z = {
        'client_talk_type':         torch.full((1, WINDOW), 2, dtype=torch.float32, device=device),  # neutral
        'reflection_subtype':       torch.full((1, WINDOW), 1, dtype=torch.float32, device=device),  # simple
        'question_subtype':         torch.zeros(1, WINDOW, dtype=torch.float32, device=device),
        'main_therapist_behaviour': torch.full((1, WINDOW), 1, dtype=torch.float32, device=device),  # reflection
        'therapist_input_subtype':  torch.zeros(1, WINDOW, dtype=torch.float32, device=device),
    }
    z['silences'] = torch.zeros(1, WINDOW, 4, dtype=torch.float32, device=device)
    return z

# =====================================================================
# Post-processing (crossfade + smoothing + amplification + resample)
# =====================================================================

def postprocess_chunk(y_chunk, prev_chunk=None,
                      fade_frames=FADE_FRAMES, sigma=SIGMA):
    """
    y_chunk:    np.array [T=18, 17] denormalized current chunk.
    prev_chunk: np.array [T=18, 17] or None (previous chunk for crossfade).

    Returns np.array [T_TARGET=15, 17] ready to send to Greta.
    """
    T, A = y_chunk.shape
    out = y_chunk.copy()

    # 1. Crossfade with previous chunk (blend last fade_frames of prev
    #    into first fade_frames of current).
    if prev_chunk is not None:
        for f in range(fade_frames):
            alpha = f / fade_frames
            i_prev = T - fade_frames + f
            i_curr = f
            if i_prev < T and i_curr < T:
                out[i_curr] = (1 - alpha) * prev_chunk[i_prev] + alpha * y_chunk[i_curr]

    # 2. Gaussian smoothing along time axis.
    out = gaussian_filter1d(out, sigma=sigma, axis=0)

    # 3. Per-AU amplification (deviation scaling around the mean).
    mean = out.mean(axis=0, keepdims=True)
    for au_name, alpha in AMPLIFY.items():
        if au_name in COLS_AU:
            idx = COLS_AU.index(au_name)
            out[:, idx] = mean[:, idx] + alpha * (out[:, idx] - mean[:, idx])
            out[:, idx] = np.clip(out[:, idx], 0, None)

    # 4. Resample from T=18 to T_TARGET=15 (25fps target).
    x_orig   = np.linspace(0, 1, T)
    x_target = np.linspace(0, 1, T_TARGET)
    resampled = interp1d(x_orig, out, axis=0, kind='linear')(x_target)
    return resampled


# =====================================================================
# Networking
# =====================================================================

def start_input_server(port, host='localhost'):
    """
    Python listens on `port`; Greta connects here as a client (SamVap
    checkbox triggers OpenFaceFrame.connectToRealTimeGreta).
    """
    srv = pysocket.socket(pysocket.AF_INET, pysocket.SOCK_STREAM)
    srv.setsockopt(pysocket.SOL_SOCKET, pysocket.SO_REUSEADDR, 1)
    srv.bind((host, port))
    srv.listen(1)
    print(f"[Input] Listening on {host}:{port} — activate SamVap in Greta...")
    conn, addr = srv.accept()
    print(f"[Input] Greta connected from {addr}")
    return srv, conn, conn.makefile('r')


def recv_frame(f):
    """Read one CSV line from Greta and return the 17 AUs, or None."""
    try:
        line = f.readline()
    except Exception:
        return None
    if not line:
        return None
    parts = line.strip().split(',')
    if len(parts) < AU_START + N_AUS:
        return None
    try:
        return np.array(
            [float(parts[AU_START + i]) for i in range(N_AUS)],
            dtype=np.float32,
        )
    except ValueError:
        return None


# =====================================================================
# CSV formatting for MODIFF_VQFrame.loadMODIFF()
# =====================================================================

def format_to_greta_csv(y_chunk, start_frame):
    """
    Format a chunk into CSV lines matching the 32-column header expected by
    loadMODIFF(): timestamp + 14 zeros (gaze/pose unused) + 17 AUs.

    timestamp is a frame counter that increments by 25 per frame so the
    frameNumber inside loadMODIFF (which does `time * 0.04`) grows by 1
    each frame.
    """
    lines = []
    for i, frame in enumerate(y_chunk):
        frame_num = start_frame + i * 25
        row = [f"{frame_num}"] + ["0"] * 14 + [f"{v:.4f}" for v in frame]
        lines.append(",".join(row))
    return lines


# =====================================================================
# Real-time inference loop
# =====================================================================

def real_time_inference_loop(model, device, sender, in_file,
                             norm_stats_path, audio_extractor=None,
                             da_client=None):
    """
    model:   AllPipeline (E3), already loaded and in eval() mode
    sender:  Deps.Sender (background thread that pushes CSV to Greta 5561)
    in_file: file-like object reading CSV lines from Greta 5963
    """
    min_x, max_x, min_y, max_y = load_norm_stats(norm_stats_path)
    print("[Loop] Norm stats loaded.")

    frame_buffer   = []
    au_window      = deque(maxlen=WINDOW)
    running_frame  = 0
    chunk_count    = 0
    prev_chunk_raw = None

    print("[Loop] Started. Waiting for client frames...\n")

    while True:
        try:
            aus = recv_frame(in_file)
            if aus is None:
                time.sleep(0.002)
                continue

            # Greta sends AUs already divided by 5 (range ~0-1). The model
            # was trained on 0-5 scale, so scale back and clip negatives
            # (filter artifacts can produce them).
            frame_buffer.append(np.clip(aus * 5.0, 0.0, None))
            if len(frame_buffer) < T_CHUNK:
                continue

            chunk = np.stack(frame_buffer, axis=0)   # [18, 17]
            frame_buffer = []

            chunk_norm = normalize_client(chunk, min_x, max_x)
            au_window.append(chunk_norm)
            chunk_count += 1

            if len(au_window) < WINDOW:
                continue

            x_au = torch.tensor(
                np.stack(list(au_window), axis=0),
                dtype=torch.float32, device=device,
            ).unsqueeze(0)  # [1, W, T, 17]

            # Dialog acts: use real DAs if available, else placeholder
            if da_client is not None:
                da_client.push_to_window()
                da_window = da_client.get_da_window()
                if da_window is not None:
                    z = build_dialog_act_tensors(da_window, device)
                else:
                    z = default_dialog_acts(device)
                    print("[Loop] Dialog acts not yet full, using placeholder.")
            else:
                z = default_dialog_acts(device)
                print("[Loop] Dialog acts disabled, using placeholder.")

            # Get audio features if extractor is available and window is full
            audio_tensor = None
            if audio_extractor is not None:
                audio_tensor = audio_extractor.get_audio_window()
                if audio_tensor is not None:
                    audio_tensor = audio_tensor.to(device)

            t0 = time.time()
            with torch.no_grad():
                y_pred = model.predict_aus(
                    x_au_client=x_au,
                    x_au_therapist_prev=None,
                    x_dialog_acts=z,
                    x_audio_client=audio_tensor,
                    x_audio_therapist=None,
                    is_new_session=None,
                )
            infer_ms = (time.time() - t0) * 1000.0

            y_chunk = y_pred.squeeze(0).cpu().numpy()   # [18, 17]
            y_chunk = denormalize_therapist(y_chunk, min_y, max_y)

            # Post-processing uses the raw [18, 17] previous chunk; save
            # BEFORE resampling so the next crossfade sees the full length.
            y_chunk_post = postprocess_chunk(y_chunk, prev_chunk=prev_chunk_raw)
            prev_chunk_raw = y_chunk.copy()

            lines = format_to_greta_csv(y_chunk_post, running_frame)
            sender.queue_data(lines)
            running_frame += T_TARGET

            print(f"[chunk {chunk_count:>3}] infer={infer_ms:.1f}ms "
                  f"| max={y_chunk_post.max():.3f}")

        except Exception as e:
            import traceback
            print("[Loop] Error:", e)
            traceback.print_exc()
            break


# =====================================================================
# Main
# =====================================================================

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--greta_in_port',  type=int, default=GRETA_IN_PORT)
    parser.add_argument('--greta_out_port', type=int, default=GRETA_OUT_PORT)
    parser.add_argument('--vqvae_path',     type=str, default=VQVAE_PATH)
    parser.add_argument('--predictor_path', type=str, default=PREDICTOR_PATH)
    parser.add_argument('--norm_stats',     type=str, default=NORM_STATS_PATH)
    args = parser.parse_args()

    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    print(f"[Device] {device}")

    model = load_model(args.vqvae_path, args.predictor_path, device)

    # Output first: MODIFF_VQFrame must already be listening on 5561
    # (activate "Enable" in MODIFF_VQ before running this script).
    # Sender blocks until the connection is established.
    sender = Sender(args.greta_out_port, "localhost")

    # Audio extractor (optional — set ADD_AUDIO_CLIENT=False to disable)
    audio_extractor = None
    if ADD_AUDIO_CLIENT:
        audio_extractor = AudioExtractorRT(device=device, window=WINDOW)
        audio_extractor.start()

    # Input: Python listens; Greta connects when SamVap is activated.
    srv_in, conn_in, f_in = start_input_server(args.greta_in_port)

    # Dialog act client (optional — set ADD_DIALOG_ACTS=False to disable)
    da_client = None
    if ADD_DIALOG_ACTS:
        try:
            da_client = DAClientRT(window=WINDOW, port=DA_SERVER_PORT)
            da_client.start()
        except Exception as e:
            print(f"[DA] Could not connect to DA_Server: {e}")
            print("[DA] Running without dialog acts (placeholder).")
            da_client = None

    try:
        real_time_inference_loop(model, device, sender, f_in,
                                 args.norm_stats, audio_extractor, da_client)
    except KeyboardInterrupt:
        print("\n[Main] Interrupted by user.")
    finally:
        f_in.close()
        conn_in.close()
        srv_in.close()


if __name__ == "__main__":
    main()