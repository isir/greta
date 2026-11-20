"""
test_pipeline.py

Recibe frames desde Greta via puerto 5963 (activar checkbox "SamVap" en Greta).
Greta envia CSV de 28 valores por linea:
  [0]       timestamp
  [1-17]    AU01_r .. AU45_r  (17 AUs, ya normalizadas 0-1 por Greta)
  [18-21]   gazeAngleX, gazeAngleY, gaze0.x, gaze0.y
  [22-27]   headPoseR.x/y/z, headPoseT.x/y/z

Modos:
  check_aus  : muestra las 17 AUs en tiempo real
  test_model : pipeline completo E3 sin dialog acts, envia resultado a Greta (5561)

Uso:
  python test_pipeline.py --mode check_aus
  python test_pipeline.py --mode test_model
"""

import argparse
import socket as pysocket
import time
from collections import deque

import numpy as np
import torch

T_CHUNK  = 18
N_AUS    = 17
WINDOW   = 8
AU_START = 1   # las AUs empiezan en columna 1

COLS_AU = [
    'AU01_r','AU02_r','AU04_r','AU05_r','AU06_r','AU07_r',
    'AU09_r','AU10_r','AU12_r','AU14_r','AU15_r','AU17_r',
    'AU20_r','AU23_r','AU25_r','AU26_r','AU45_r',
]

from scipy.ndimage import gaussian_filter1d
from scipy.interpolate import interp1d

# Config post-processing (igual que export_to_greta2.py)
FADE_FRAMES = 6
SIGMA       = 3.0
FPS_OUT     = 25
T_TARGET    = 15   # int(round(0.6 * 25)) = 15 frames por chunk a 25fps

# Amplificacion por AU (igual que export_to_greta.sh)
# aus_speaking_1 → alpha=0.3, eyebrow → alpha=2.5
AMPLIFY = {
    #'AU01_r': 1.0, 'AU02_r': 1.0,          # cejas
    'AU25_r': 0.3, 'AU26_r': 0.3,          # boca (speaking)
}
COLS_AU_LIST = [
    'AU01_r','AU02_r','AU04_r','AU05_r','AU06_r','AU07_r',
    'AU09_r','AU10_r','AU12_r','AU14_r','AU15_r','AU17_r',
    'AU20_r','AU23_r','AU25_r','AU26_r','AU45_r',
]

def postprocess_chunk(y_chunk, prev_chunk=None,
                      fade_frames=FADE_FRAMES, sigma=SIGMA):
    """
    y_chunk:    np.array [T=18, 17]  chunk actual desnormalizado
    prev_chunk: np.array [T=18, 17] o None — chunk anterior para crossfade

    Devuelve np.array [T_TARGET=15, 17] listo para enviar a Greta.
    """
    T, A = y_chunk.shape

    # 1. Crossfade con chunk anterior
    out = y_chunk.copy()
    if prev_chunk is not None:
        for f in range(fade_frames):
            alpha = f / fade_frames
            i_prev = T - fade_frames + f   # ultimos frames del chunk anterior
            i_curr = f                      # primeros frames del chunk actual
            if i_prev < T and i_curr < T:
                blended = (1 - alpha) * prev_chunk[i_prev] + alpha * y_chunk[i_curr]
                out[i_curr] = blended

    # 2. Gaussian smoothing
    out = gaussian_filter1d(out, sigma=sigma, axis=0)

    # 3. Amplify por AU
    mean = out.mean(axis=0, keepdims=True)
    for au_name, alpha in AMPLIFY.items():
        if au_name in COLS_AU_LIST:
            idx = COLS_AU_LIST.index(au_name)
            out[:, idx] = mean[:, idx] + alpha * (out[:, idx] - mean[:, idx])
            out[:, idx] = np.clip(out[:, idx], 0, None)

    # 4. Resample de T=18 a T_TARGET=15 (25fps)
    x_orig   = np.linspace(0, 1, T)
    x_target = np.linspace(0, 1, T_TARGET)
    resampled = interp1d(x_orig, out, axis=0, kind='linear')(x_target)

    return resampled  # [15, 17]

# -----------------------------------------------------------------------
# Servidor TCP: Python escucha, Greta se conecta
# -----------------------------------------------------------------------

def start_server(port, host='localhost'):
    """
    Python actua como servidor TCP en el puerto dado.
    Greta (SamVap / connectToRealTimeGreta) se conecta como cliente.
    """
    srv = pysocket.socket(pysocket.AF_INET, pysocket.SOCK_STREAM)
    srv.setsockopt(pysocket.SOL_SOCKET, pysocket.SO_REUSEADDR, 1)
    srv.bind((host, port))
    srv.listen(1)
    print(f"[Server] Escuchando en {host}:{port} - activa SamVap en Greta...")
    conn, addr = srv.accept()
    print(f"[Server] Greta conectada desde {addr}")
    return srv, conn, conn.makefile('r')


def recv_frame(f):
    """Lee una linea y devuelve las 17 AUs (ya normalizadas 0-1) o None."""
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
        return np.array([float(parts[AU_START + i]) for i in range(N_AUS)],
                        dtype=np.float32)
    except ValueError:
        return None


# -----------------------------------------------------------------------
# Modo 1: solo ver AUs
# -----------------------------------------------------------------------

def mode_check_aus(port):
    print("\n=== MODO: check_aus ===")
    srv, conn, f = start_server(port)

    frame_count = 0
    hdr = "Frame  " + "  ".join(f"{c[2:6]:>6}" for c in COLS_AU)
    print(hdr)
    print("-" * len(hdr))

    try:
        while True:
            aus = recv_frame(f)
            if aus is None:
                time.sleep(0.002)
                continue
            frame_count += 1
            if frame_count % 5 == 0:
                vals = "  ".join(f"{v:6.3f}" for v in np.clip(aus * 5.0, 0.0, None))
                print(f"{frame_count:>5}  {vals}")
    except KeyboardInterrupt:
        print(f"\nDetenido tras {frame_count} frames.")
    finally:
        conn.close(); srv.close()


# -----------------------------------------------------------------------
# Modo 2: test completo del modelo
# -----------------------------------------------------------------------

def load_model(vqvae_path, predictor_path, device):
    import sys, os
    sys.path.insert(0, os.path.join(os.path.dirname(__file__), 'model'))
    from MODIF_baseline import AllPipeline
    from encoders import (AUsEncoder, DialogActEncoder,
                          AudioEncoder, SinusoidalEmbeddings)
    from vqvae import VQVAE

    vqvae = VQVAE(in_features=17, d_model=128, dz=128,
                  K=64, T=18, n_heads=4, num_layers=3,
                  dropout=0.1, beta=0.25).to(device)
    ck = torch.load(vqvae_path, map_location=device)
    vqvae.load_state_dict(ck['model_state'])
    vqvae.eval()
    print(f"[Model] VQ-VAE cargado.")

    model = AllPipeline(
        client_aus_encoder=AUsEncoder(in_features=17, d_model=128),
        therapist_aus_encoder=AUsEncoder(in_features=17, d_model=128),
        pos_encoder=SinusoidalEmbeddings(d_model=128),
        dialog_act_encoder=DialogActEncoder(d_model=128),
        audio_encoder=AudioEncoder(in_features=1024, d_model=128, num_layers=4),
        W=WINDOW, d_model=128, n_heads=8, num_layers_transformer=4,
        dropout=0.3, 
        vqvae=vqvae, K=64, T_down=9,
        use_therapist_prev=False, use_vqvae_enc=False,
    ).to(device)
    ck2 = torch.load(predictor_path, map_location=device, weights_only=False)
    model.load_state_dict(ck2['model_state'])
    model.eval()
    print(f"[Model] Predictor E3 cargado. "
          f"epoch={ck2['epoch']+1}, "
          f"val_pearson={ck2.get('val_pearson','?')}")
    return model


def placeholder_dialog_acts(device):
    z = {k: torch.zeros(1, WINDOW, dtype=torch.float32, device=device)
         for k in ['client_talk_type','reflection_subtype','question_subtype',
                   'main_therapist_behaviour','therapist_input_subtype']}
    z['silences'] = torch.zeros(1, WINDOW, 4, dtype=torch.float32, device=device)
    return z


def format_to_greta_csv(y_chunk, start_frame):
    """y_chunk: [T_TARGET=15, 17]"""
    lines = []
    for i, frame in enumerate(y_chunk):
        frame_num = start_frame + i * 25
        row = [f"{frame_num}"] + ["0"] * 14 + [f"{v:.4f}" for v in frame]
        lines.append(",".join(row))
    return lines


def mode_test_model(greta_in_port, greta_out_port,
                    vqvae_path, predictor_path, norm_stats_path):
    print("\n=== MODO: test_model ===")
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    print(f"[Device] {device}")

    model = load_model(vqvae_path, predictor_path, device)

    # Las AUs que llegan de Greta ya estan normalizadas 0-1 (divididas por 5).
    # El modelo fue entrenado con norm_stats min-max sobre esos valores 0-1.
    # Cargamos norm_stats solo para desnormalizar la salida.
    stats = np.load(norm_stats_path, allow_pickle=True).item()
    min_x, max_x = stats['min_x'], stats['max_x']
    min_y, max_y = stats['min_y'], stats['max_y']
    print(f"[Norm] norm_stats cargado.")

    # Salida: Python conecta a MODIFF_VQFrame (que ya debe estar escuchando en 5561)
    # IMPORTANTE: activar Enable en MODIFF_VQ ANTES de correr este script
    print(f"[Greta OUT] Conectando a MODIFF_VQFrame puerto {greta_out_port}...")
    sock_out = pysocket.socket(pysocket.AF_INET, pysocket.SOCK_STREAM)
    sock_out.connect(("localhost", greta_out_port))
    f_out = sock_out.makefile('w')
    print(f"[Greta OUT] Conectado OK.")

    # Entrada: Python escucha, Greta se conecta
    srv_in, conn_in, f_in = start_server(greta_in_port)

    frame_buffer = []
    au_window    = deque(maxlen=WINDOW)
    running_ts   = 0
    chunk_count  = 0
    prev_chunk_raw = None
    print("\n[test_model] Corriendo. Ctrl+C para detener.\n")
    try:
        while True:
            aus = recv_frame(f_in)
            if aus is None:
                time.sleep(0.002)
                continue

            frame_buffer.append(np.clip(aus * 5.0, 0.0, None))
            if len(frame_buffer) < T_CHUNK:
                continue

            chunk = np.stack(frame_buffer, axis=0)  # [18, 17], ya en 0-1
            frame_buffer = []

            # Normalizar con min_x/max_x del entrenamiento
            chunk_norm = (chunk - min_x) / (max_x - min_x + 1e-8)
            au_window.append(chunk_norm)
            chunk_count += 1

            nonzero_in = (chunk.mean(axis=0) > 0.01).sum()
            print(f"[Chunk {chunk_count:>3}] "
                  f"AUs cliente activas={nonzero_in}/17 | "
                  f"ventana={len(au_window)}/{WINDOW}")

            if len(au_window) < WINDOW:
                continue

            x_au = torch.tensor(
                np.stack(list(au_window), axis=0),
                dtype=torch.float32, device=device
            ).unsqueeze(0)  # [1, W, T, 17]

            z = placeholder_dialog_acts(device)

            t0 = time.time()
            with torch.no_grad():
                y_pred = model.predict_aus(
                    x_au_client=x_au,
                    x_au_therapist_prev=None,
                    x_dialog_acts=z,
                    x_audio_client=None,
                    x_audio_therapist=None,
                    is_new_session=None,
                )
            infer_ms = (time.time() - t0) * 1000.0

            y_chunk = y_pred.squeeze(0).cpu().numpy()          # [T, 17]
            y_chunk = y_chunk * (max_y - min_y + 1e-8) + min_y
            y_chunk_post = postprocess_chunk(y_chunk, prev_chunk=prev_chunk_raw)
            prev_chunk_raw = y_chunk.copy()  # guardar ANTES del resample para el proximo chunk

            

            y_mean = y_chunk_post.mean(axis=0)
            print(f"         -> {infer_ms:.1f}ms | "
                  f"AUs terapeuta activas={(y_mean > 0.5).sum()}/17 | "
                  f"max={y_mean.max():.3f}")

            lines = format_to_greta_csv(y_chunk_post, running_ts)
            for line in lines:
                f_out.write(line + '\n')
            f_out.flush()
            running_ts += T_TARGET
            print(f"         -> Chunk {chunk_count} enviado a Greta.\n")

    except KeyboardInterrupt:
        print(f"\nDetenido. Chunks: {chunk_count}")
    finally:
        f_in.close();  conn_in.close();  srv_in.close()
        f_out.close(); sock_out.close()


# -----------------------------------------------------------------------
# Main
# -----------------------------------------------------------------------

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--mode', choices=['check_aus','test_model'],
                        default='check_aus')
    parser.add_argument('--greta_in_port',  type=int, default=5963,
                        help='Puerto donde Python escucha a Greta (SamVap)')
    parser.add_argument('--greta_out_port', type=int, default=5561,
                        help='Puerto de MODIFF_VQFrame en Greta')
    parser.add_argument('--vqvae_path',     type=str,
                        default='model/vqvae_model.pt')
    parser.add_argument('--predictor_path', type=str,
                        default='model/predictor_model.pt')
    parser.add_argument('--norm_stats',     type=str,
                        default='model/norm_stats.npy')
    args = parser.parse_args()

    if args.mode == 'check_aus':
        mode_check_aus(args.greta_in_port)
    else:
        mode_test_model(
            greta_in_port=args.greta_in_port,
            greta_out_port=args.greta_out_port,
            vqvae_path=args.vqvae_path,
            predictor_path=args.predictor_path,
            norm_stats_path=args.norm_stats,
        )