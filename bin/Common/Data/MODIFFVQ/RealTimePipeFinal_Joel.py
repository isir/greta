
import numpy as np
import torch
import time
import pandas as pd
import random
import sys
import os
import argparse
from collections import deque
from model.MODIF_baseline import AllPipeline
from model.encoders import *
from model.vqvae import VQVAE
from dialog_act_mapping import empty_dialog_act
from Deps import Miror, Client, Sender, ExternalTensorClient, RealTimeProcessor
from Processings import interpolate_activations_btwchunks, process_and_save_to_csv, adaptive_interpolation_btwchunks, exclusive_group_activation, discrete_symmetric_groups
from Randomizers import generate_random_tensors_numpy, generate_random_series_sequence

def resource_path(relative_path):
    base_path = getattr(sys, '_MEIPASS', os.path.dirname(os.path.abspath(__file__)))
    return os.path.join(base_path, relative_path)

delay = 0

#VQVAE config
VQVAE_PATH = 'MODIFFVQ/vqvae_model.pt'
VQVAE_DZ   = 128
VQVAE_K    = 64
VQVAE_NUM_LAYERS = 3


# Predictor config
MODIFFVQ_PATH = 'MODIFFVQ/predictor_model.pt'
D_MODEL        = 128
N_HEADS        = 8
NUM_LAYERS_T   = 2     # transformer layers in predictor
DROPOUT        = 0.3
WINDOW_LENGTH  = 8
T_DOWN         = 9     # T // 2 = 18 // 2
USE_THERAPIST_PREV = False  # E3 doesn't use therapist_prev
USE_VQVAE_ENC  = False
AUDIO_DIM      = 1024
T_CHUNK = 18          # frames por chunk (VQ-VAE T)
N_AUS = 17            # numero de AUs
WINDOW = 8            # W: numero de chunks de contexto


# Inference flags (must match what the model was trained with)
ADD_DIALOG_ACTS      = True
ADD_AUDIO_CLIENT     = True
ADD_AUDIO_THERAPIST  = False
COMPUTE_AUDIO_FEATURES = True

# Norm stats
NORM_STATS_PATH = 'norm_stats.npy'

NUM_LAYERS_T   = 4    
AUDIO_DIM      = 1024
AUDIO_LAYERS   = 4     

def load_norm_stats(path):
    """min_x/max_x/min_y/max_y from norm_stats.npy."""
    stats = np.load(path, allow_pickle=True).item()
    return stats['min_x'], stats['max_x'], stats['min_y'], stats['max_y']


def normalize_client(x, min_x, max_x):
    """x: np.array [T, 17] it normalizes the client AUs to [0, 1] range using min-max normalization."""
    return (x - min_x) / (max_x - min_x + 1e-8)

def denormalize_therapist(y, min_y, max_y):
    """y: np.array [T, 17] it denormalizes the therapist AUs from [0, 1] range back to original scale using min-max denormalization."""
    return y * (max_y - min_y + 1e-8) + min_y

def build_dialog_act_tensors(dialog_act_window, device):
    """
    Convierte un deque de W dicts de dialog acts al formato que espera
    DialogActEncoder.forward: cada campo categorico [1, W], silences [1, W, 4].
    
    Converts a deque of W dialog act dicts to the format expected by 
    DialogActEncoder.forward: each categorical field [1, W], silences [1, W, 4].
    """
    keys_cat = [
        'client_talk_type', 'reflection_subtype', 'question_subtype',
        'main_therapist_behaviour', 'therapist_input_subtype'
    ]
    z = {}
    for k in keys_cat:
        vals = [da[k] for da in dialog_act_window]  # W int
        z[k] = torch.tensor([vals], dtype=torch.float32, device=device)  # [1, W]
 
    silences = [da['silences'] for da in dialog_act_window]  # W list of 4 binary flags
    z['silences'] = torch.tensor([silences], dtype=torch.float32, device=device)  # [1, W, 4]
    return z

def format_chunk_to_csv(y_chunk, start_timestamp, fps=25.0):
    """
    y_chunk: np.array [T, 17] denormalized therapist AUs.
    Returns a list of CSV strings (one per frame) with 32 columns.
    Non-AU columns (gaze/pose) are set to 0. Timestamp increments per frame.
    """
    lines = []
    dt = 1.0 / fps
    n_nonau = 15  # timestamp + 6 gaze + 2 gaze_angle... en realidad 15 cols antes de AUs
    for i, frame in enumerate(y_chunk):
        ts = start_timestamp + i * dt
        # 15 columnas no-AU: timestamp seguido de 14 ceros
        row = [f"{ts:.4f}"] + ["0"] * (n_nonau - 1)
        # 17 AUs
        row += [f"{v:.4f}" for v in frame]
        lines.append(",".join(row))
    return lines

def real_time_inference_loop_e3(
        model, device, client, sender, tensor_client,
        norm_stats_path,
        fps=25.0):
    """
    model: AllPipeline (E3) already loaded and in eval()
    client: Client (receives chunks of 17 AUs from the client)
    sender: Sender (sends CSV to Greta port 5561)
    tensor_client: source of dialog acts (adapted); can be None to test
    """
    model.eval()
 
    min_x, max_x, min_y, max_y = load_norm_stats(norm_stats_path)
 

    # buffers for W chunks
    au_client_window = deque(maxlen=WINDOW_LENGTH)
    dialog_act_window = deque(maxlen=WINDOW_LENGTH)
 
    running_timestamp = 0.0
    chunk_dt = T_CHUNK / fps  #length of a chunk in seconds
 
    print("[E3] Inference loop started. Waiting for client chunks...")
 
    while True:
        try:
            # wait for a new chunk from the client ([T, 17] continuous)
            with client.lock:
                chunk = client.latest_processed_batch
                client.latest_processed_batch = None  
 
            if chunk is None:
                time.sleep(0.005)
                continue
 
            chunk = np.asarray(chunk, dtype=np.float32).reshape(T_CHUNK, N_AUS)
 
            # normalize client AUs to [0, 1] range
            chunk_norm = normalize_client(chunk, min_x, max_x)
            au_client_window.append(chunk_norm)
 
            # more recent dialog acts
            if tensor_client is not None:
                da = tensor_client.get_current_dialog_act()  # dict o None
                if da is None:
                    da = empty_dialog_act()
            else:
                da = empty_dialog_act()
            dialog_act_window.append(da)
 
            # wait until we have enough context (W chunks) to run inference
            if len(au_client_window) < WINDOW:
                print(f"[E3] Filling window {len(au_client_window)}/{WINDOW}")
                continue
 
            # x_au_client: [1, W, T, 17]
            x_au_client = torch.tensor(
                np.stack(list(au_client_window), axis=0),
                dtype=torch.float32, device=device
            ).unsqueeze(0) #unused batch dimension
 
            z = build_dialog_act_tensors(dialog_act_window, device)
 
            # infer therapist AUs for the current chunk
            start = time.time()
            with torch.no_grad():
                y_pred = model.predict_aus(
                    x_au_client=x_au_client,
                    x_au_therapist_prev=None,
                    x_dialog_acts=z,
                    x_audio_client=None,     #TODO: add audio features if available
                    x_audio_therapist=None,
                    is_new_session=None,
                )
            infer_ms = (time.time() - start) * 1000.0
 
            # y_pred: [1, T, 17] -> [T, 17]
            y_chunk = y_pred.squeeze(0).cpu().numpy()
 
            # denormalize therapist AUs back to original scale
            y_chunk = denormalize_therapist(y_chunk, min_y, max_y)
 
            # format chunk to CSV lines and send to Greta
            lines = format_chunk_to_csv(y_chunk, running_timestamp, fps=fps)
            sender.queue_data(lines)
 
            running_timestamp += chunk_dt
 
            print(f"[E3] chunk sent | infer {infer_ms:.2f} ms | ts {running_timestamp:.2f}s")
 
        except Exception as e:
            import traceback
            print("[E3] Error in loop:", e)
            traceback.print_exc()
            break


def generate_chunk_descriptor_tensor(storer, previous_input_tensor, C_value):
    """
    Generates the chunk_descriptor_tensor based on the previous reprojected output
    and the previous input tensor stored in the Storer.

    :param storer: A Storer object that stores the previous reprojected output.
    :param previous_input_tensor: The previous input tensor.
    :return: chunk_descriptor_tensor (PyTorch tensor)
    """
    value1 = 0
    value2 = 0

    # Extract the previous reprojected output and previous input tensor
    previous_reprojected_output = storer.stored_sequences[-1] if storer.stored_sequences else None  # Last stored sequence

    # Check for previous reprojected output
    if previous_reprojected_output is not None:
        count_1s = previous_reprojected_output.count(1)
        count_2s_and_3s = previous_reprojected_output.count(2) + previous_reprojected_output.count(3)

        if all(value == 0 for value in previous_reprojected_output):
            value1 = 0
        elif count_2s_and_3s > count_1s:
            value1 = -1
        else:
            value1 = 1

    # Check for previous input tensor
    if previous_input_tensor != None :
        input_values = previous_input_tensor.cpu().numpy().flatten().tolist()  # Convert to list for easy counting
        count_1s = input_values.count(1)
        count_2s_and_3s = input_values.count(2) + input_values.count(3)

        if all(value == 0 for value in input_values):
            value2 = 0
        elif count_2s_and_3s > count_1s:
            value2 = -1
        else:
            value2 = 1
    else :
        value2 = 0


    # Construct the chunk descriptor tensor
    chunk_descriptor_tensor = torch.tensor([[C_value, value1, value2]], dtype=torch.float32)

    return chunk_descriptor_tensor



def preprocess_real_time(input_frames, processor):
    processor.update_buffer(input_frames)
    return processor.get_projected_buffer()




def real_time_inference_loop(model, device, client, sender, processor, miror, tensor_client, N =10, K = 30, guide_weight=0.0):
    model.eval()

    print(torch.cuda.memory_allocated() / 1024 ** 3, "GB allocated")
    print(torch.cuda.memory_reserved() / 1024 ** 3, "GB reserved")

    # A commmenter for server reciving MI dialogs
    batch_size = 1
    previous_z_tensor, _ = generate_random_tensors_numpy(batch_size)
    previous_input_tensor = None
    C_value = 0.0
    val = processor.buffer_size * 0.04


    intermed_csv_path = 'intermed.csv'
    transformed_csv_path = 'csv_file.csv'
    final_csv_path = 'Finalcsv.csv'
    adjusted_csv_path = 'Ajusted_Final_csv_file.csv'
    ground_csv_path = 'GroundTruth.csv'
    extended_csv_path = 'FinalInterpolated.csv'

    # Initialize lists to hold all input vectors and reprojected outputs
    input_vector_list = []
    reprojected_output_list = []
    # file = "C:\\Users\\isir\\Desktop\\RealTimeExperiment_Nezih\\input_output_sequences.txt"
    file = "input_output_sequences.txt"
    
    cnt = 0

    while True:
        try:
            
            print(1, cnt)
            cnt += 1
            
            with client.lock:
                input_vector = client.latest_processed_batch

            if not input_vector:
                time.sleep(0.01)
                continue

            start_time = time.time()

            print(2, cnt)
            cnt += 1


            updated_buffer = preprocess_real_time(input_vector, processor)
            input_tensor = torch.tensor(updated_buffer, dtype=torch.float32).unsqueeze(0).to(device)

            print(3, cnt)
            cnt += 1


            # # Get the latest z_tensor from the tensor client
            z_tensor = tensor_client.get_z_tensor()
            
            print('z_tensor: ', z_tensor)

            # If no new z_tensor is received, continue using the previous value
            if z_tensor is None:
                print("No new z_tensor received, using previous z_tensor.")
                z_tensor = previous_z_tensor
            else:
                previous_z_tensor = z_tensor  # Update the previous_z_tensor with the latest received value

            z_tensor = z_tensor.to(device)

            #For the chunk descriptor it should be computed given the loop iteration number, and the previous projected outputs using the miroring storer
            #_, chunk_descriptor_tensor = generate_random_tensors_numpy(batch_size)

            chunk_descriptor_tensor = generate_chunk_descriptor_tensor(miror, previous_input_tensor, C_value)
            C_value += 0.0001



            chunk_descriptor_tensor = chunk_descriptor_tensor.to(device)



            with torch.no_grad():
                model.guide_w = guide_weight
                start_time2 = time.time()
                print(chunk_descriptor_tensor)
                y_pred = model.sample(input_tensor, z_tensor, chunk_descriptor_tensor).detach().cpu().numpy()
                end_time2 = time.time()
                inference_time = end_time2 - start_time2
                print(f"Inference time for the current batch: {inference_time: .4f} seconds")

            




            # Read the last 16 rows from the adjusted CSV file and queue them for the sender
            df = pd.read_csv(extended_csv_path)
            columns_to_interpolat = ['AU06_r', 'AU25_r', 'AU12_r', 'AU10_r', 'AU09_r', 'AU14_r', 'AU15_r', 'AU01_r',
                                      'AU04_r', 'AU20_r', 'AU02_r']
            # Ensure enough data is available
            if df.shape[0] > (processor.buffer_size * 2) - 1:
                a = processor.buffer_size
                b = processor.buffer_size * 2
                # Get the previous and current chunks
                df_prev = df.iloc[-b:-a].reset_index(drop=True)
                df_curr = df.iloc[-a:].reset_index(drop=True)

                # Apply the interpolation between chunks
                df_prev, df_curr = adaptive_interpolation_btwchunks(
        df_prev, df_curr, columns_to_interpolat, min_overlap=5, max_overlap=15, diff_threshold=0.2, k=10
    )
                # df_prev, df_curr = interpolate_activations_btwchunks(
                # df_prev, df_curr, columns_to_interpolat, N, K
                # )

                # Update the main DataFrame with modified chunks
                df.update(df_prev)
                df.update(df_curr)
                # Define the groups with their representative AU and corresponding columns.
                groups = {
                    'smile': {'rep': 'AU12_r', 'cols': ['AU12_r', 'AU25_r', 'AU06_r', 'AU02_r']},
                    # Adjust names as needed
                    'frown': {'rep': 'AU10_r', 'cols': ['AU20_r', 'AU09_r', 'AU04_r']},
                    'other': {'rep': 'AU15_r', 'cols': ['AU15_r', 'AU10_r', 'AU01_r']}
                }

                # Apply the adaptive accelerated fade with priority logic.
                df_exclusive = exclusive_group_activation(df, groups, threshold=0.05)
                df_t = discrete_symmetric_groups(df_exclusive, groups, threshold=0.05)
                df_t.to_csv('FinalCrossinterpolated.csv', index=False)
                last_16_rows = df_t.tail(processor.buffer_size).to_csv(index=False, header=False)
                sender.queue_data(last_16_rows.splitlines())

            last_16_rows = df.tail(processor.buffer_size).to_csv(index=False, header=False)
            sender.queue_data(last_16_rows.splitlines())
            
            # print("[MODIFF-TEST]", last_16_rows)

            previous_input_tensor = input_tensor

            print("Client sequence of length :", len(input_vector), ":")
            print(input_vector)
            print("Reprojected Output of length:", len(reprojected_output), ":")
            print(reprojected_output) 

            input_vector_list.append(input_vector)
            reprojected_output_list.append(reprojected_output)

            # Write the updated lists to the log file
            with open(file, "w") as log_file:
                log_file.write(f"Input Vector: {input_vector_list}\n")
                log_file.write(f"Reprojected Output: {reprojected_output_list}\n")


            elapsed_time = time.time() - start_time
            time.sleep(max(0, val - elapsed_time))
        except Exception as e:
            print("Error:", e)

            break


def main():
    sequence_length = 137
    parser = argparse.ArgumentParser(description='Process command-line arguments for RealTimePipeFinal.')
    parser.add_argument('--buffer', type=int, default=32, help='An integer value for buffer (default: 34)')
    args = parser.parse_args()

    buffer = args.buffer
    sequence_length += delay

    # IT HELPS TO SAVE ONLY ONE INTERACTION FILE // to take into account when dealing with experimental setup
    if os.path.exists('Ajusted_Final_csv_file.csv'):
        os.remove('Ajusted_Final_csv_file.csv')
        os.remove('csv_file.csv')
        os.remove('Finalcsv.csv')
        os.remove('FinalInterpolated.csv')
        os.remove('intermed.csv')

    # conversion_table = {
    #     "Ask for consent": (1.0, 8.0),
    #     "Medical Education and Guidance": (1.0, 8.0),
    #     "Planning with the Patient": (1.0, 5.0),
    #     "Give Solutions": (1.0, 5.0),
    #     "Ask about current emotions": (1.0, 8.0),
    #     "Reflections": (1.0, 7.0),
    #     "Ask for information": (1.0, 8.0),
    #     "Empathic reactions": (1.0, 7.0),
    #     "Acknowledge Progress and Encourage": (1.0, 6.0),
    #     "Backchannel": (3.0, 5.0),
    #     "Greeting or Closing" : (1.0, 9.0),
    #     "Experience Normalization and Reassurance": (1.0, 9.0),
    #     "Changing unhealthy behavior": (2.0, 12.0),
    #     "Sustaining unhealthy behavior": (2.0, 12.0),
    #     "Sharing negative feeling or emotion": (2.0,10.0),
    #     "Sharing positive feeling or emotion": (2.0, 10.0),
    #     "Realization or Understanding": (2.0, 10.0),
    #     "Sharing personal information": (2.0, 10.0),
    #     "Asking for medical information": (2.0, 11.0)
    # }
    #FR
    conversion_table = {
        'Demander le consentement': (1.0, 8.0),
        'Éducation et orientation médicale': (1.0, 8.0),
        'Planifier avec le patient': (1.0, 5.0),
        'Donner des solutions': (1.0, 5.0),
        'Posez des questions sur vos émotions actuelles': (1.0, 8.0),
        'Réflexions': (1.0, 7.0),
        'Demander des informations': (1.0, 8.0),
        'Réactions empathiques': (1.0, 7.0),
        'Reconnaître les progrès et encourager': (1.0, 6.0),
        'Canal arrière': (3.0, 5.0),
        'Salutation ou clôture': (1.0, 9.0),
        'Expérience de normalisation et de réconfort': (1.0, 9.0),
        'Changer les comportements malsains': (2.0, 10.0),
        'Maintenir un comportement malsain': (2.0, 11.0),
        'Partager un sentiment ou une émotion négative': (2.0, 12.0),
        'Partager un sentiment ou une émotion positive': (2.0, 12.0),
        'Réalisation ou compréhension': (2.0, 12.0),
        "Partage d'informations personnelles": (2.0, 12.0),
        "Demande d'informations médicales": (2.0, 12.0)
    }

    
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    print(f"Using device: {device}")
    print(torch.cuda.memory_allocated() / 1024 ** 3, "GB allocated")
    print(torch.cuda.memory_reserved() / 1024 ** 3, "GB reserved")

    observation_embedder = ObservationEmbedder(num_facial_types, facial_embed_dim, cnn_output_dim, lstm_hidden_dim,
                                               sequence_length)
    mi_embedder = SpeakingTurnDescriptorEmbedder(num_event_types, event_embedding_dim, embed_output_dim)
    chunk_embedder = ChunkDescriptorEmbedder(continious_embedding_dim=16, valence_embedding_dim=8, output_dim=64)

    vqvae_path = resource_path(VQVAE_PATH)
    model_path = resource_path(MODIFFVQ_PATH)

    #encoders
    aus_emb   = AUsEncoder(in_features=17, d_model=D_MODEL)
    diag_emb  = DialogActEncoder(d_model=D_MODEL)
    audio_emb = AudioEncoder(in_features=AUDIO_DIM, d_model=D_MODEL, num_layers=AUDIO_LAYERS)

    #models
    vqvae = VQVAE(
                in_features=17, d_model=128, dz=VQVAE_DZ,
                K=VQVAE_K, T=18, n_heads=4, num_layers=VQVAE_NUM_LAYERS,
                dropout=0.1, beta=0.25
            ).to(device)

    checkpoint = torch.load(VQVAE_PATH, map_location=device)
    vqvae.load_state_dict(checkpoint['model_state'])
    vqvae.eval()

    model = AllPipeline(
        client_aus_encoder=aus_emb,
        therapist_aus_encoder=aus_emb,
        pos_encoder=SinusoidalEmbeddings(d_model=D_MODEL),
        dialog_act_encoder=diag_emb,
        audio_encoder=audio_emb,
        W=WINDOW_LENGTH,
        d_model=D_MODEL,
        n_heads=N_HEADS,
        num_layers_transformer=NUM_LAYERS_T,
        dropout=DROPOUT,
        output_dim=17,
        vqvae=vqvae,
        K=VQVAE_K,
        T_down=T_DOWN,
        use_therapist_prev=USE_THERAPIST_PREV,
        use_vqvae_enc=USE_VQVAE_ENC,
    ).to(device)

    checkpoint = torch.load(model_path, map_location=device)
    model.load_state_dict(checkpoint['model_state'])
    model.eval()

    print(f'Predictor loaded: epoch {checkpoint["epoch"]+1}, '
      f'val Pearson {checkpoint.get("val_pearson", "?"):.4f}')

    processor = RealTimeProcessor(delay, buffer_size=buffer , target_size=137)

    mirror = Miror(consecutive_zero_threshold=M)

    # client = Client(50150, "localhost", buffer)
    client = Client(5560, "localhost", buffer)
    client.connect_to_server()
    client.start_receiving()

    #Initialize the external tensor client
    tensor_client = ExternalTensorClient(conversion_table, server1_address="localhost", server1_port= 50200, server2_address="localhost", server2_port=50201)
    # tensor_client = ExternalTensorClient(conversion_table, server1_address="localhost", server1_port= 5555, server2_address="localhost", server2_port=5556)
    tensor_client.connect()

    # sender = Sender(50151, "localhost")
    sender = Sender(5561, "localhost")

    real_time_inference_loop(model, device, client, sender,  processor, mirror, tensor_client, N , K, guide_weight=guide_w)




if __name__ == "__main__":
    main()