
import numpy as np
import torch
import time
import pandas as pd
import random
import sys
import os
import argparse
from Model import Model_mlp_diff, Model_Cond_Diffusion, ObservationEmbedder, SpeakingTurnDescriptorEmbedder, ChunkDescriptorEmbedder
from Deps import Miror, Client, Sender, ExternalTensorClient, RealTimeProcessor
from Processings import interpolate_activations_btwchunks, process_and_save_to_csv, adaptive_interpolation_btwchunks, crossfade_activations_btwchunks, exclusive_group_activation, discrete_symmetric_groups
from Randomizers import generate_random_tensors_numpy, generate_random_series_sequence

def resource_path(relative_path):
    base_path = getattr(sys, '_MEIPASS', os.path.dirname(os.path.abspath(__file__)))
    return os.path.join(base_path, relative_path)

n_hidden = 512
n_T = 1000
num_event_types =13
event_embedding_dim =64
embed_output_dim =128
guide_w =0
num_facial_types = 7
facial_embed_dim = 32
cnn_output_dim = 512
lstm_hidden_dim = 256
x_dim = 137
y_dim = 137
delay =  0



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
    file = "input_output_sequences_random.txt"


    while True:
        try:
            with client.lock:
                input_vector = client.latest_processed_batch

            if not input_vector:
                time.sleep(0.01)
                continue

            start_time = time.time()


            updated_buffer = preprocess_real_time(input_vector, processor)
            input_tensor = torch.tensor(updated_buffer, dtype=torch.float32).unsqueeze(0).to(device)

            # # Get the latest z_tensor from the tensor client
            z_tensor = tensor_client.get_z_tensor()

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



            # with torch.no_grad():
            #     model.guide_w = guide_weight
            #     start_time2 = time.time()
            #     print(chunk_descriptor_tensor)
            #     y_pred = model.sample(input_tensor, z_tensor, chunk_descriptor_tensor).detach().cpu().numpy()
            #     end_time2 = time.time()
            #     inference_time = end_time2 - start_time2
            #     print(f"Inference time for the current batch: {inference_time: .4f} seconds")
            #
            #
            # best_prediction = np.round(y_pred)
            # best_prediction[best_prediction == 4] = 3
            # best_prediction[best_prediction >= 5] = 0
            # best_prediction[best_prediction < 0] = 0
            #
            #
            # reprojected_output = processor.reproject_to_buffer(best_prediction[0], processor.buffer_size)


            #TO SWITCH TO RANDOM GENERATION !!
            reprojected_output = generate_random_series_sequence(processor.buffer_size, 4)

            # Store the reprojected output sequence in the Storer
            miror.store_sequence(reprojected_output)

            if miror.should_mirror():
                print("Mirroring Mode Activated: Using input tensor as output.")
                reprojected_output = miror.mirror_sequence(input_vector)


            process_and_save_to_csv(reprojected_output, intermed_csv_path, transformed_csv_path, ground_csv_path, final_csv_path, adjusted_csv_path, extended_csv_path, 15, 30)

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
                #df_prev, df_curr = interpolate_activations_btwchunks(
                    #df_prev, df_curr, columns_to_interpolat, N, K
                #)
                df_prev, df_curr = crossfade_activations_btwchunks(df_prev, df_curr, columns_to_interpolat, overlap=N)

                #df_prev, df_curr = adaptive_interpolation_btwchunks(
                    #df_prev, df_curr, columns_to_interpolat, min_overlap=5, max_overlap=15, diff_threshold=0.2, k=10
                #)

                # Update the main DataFrame with modified chunks
                df.update(df_prev)
                df.update(df_curr)

            # Define the groups with their representative AU and corresponding columns.
            groups = {
                'smile': {'rep': 'AU12_r', 'cols': ['AU12_r', 'AU25_r', 'AU06_r', 'AU02_r']},  # Adjust names as needed
                'frown': {'rep': 'AU10_r', 'cols': ['AU20_r', 'AU09_r', 'AU04_r']},
                'other': {'rep': 'AU15_r', 'cols': ['AU15_r', 'AU10_r','AU01_r']}
            }

            # Apply the adaptive accelerated fade with priority logic.
            df_exclusive = exclusive_group_activation(df, groups, threshold=0.05)
            df_t= discrete_symmetric_groups(df_exclusive, groups, threshold=0.05)
            df_t.to_csv('FinalCrossinterpolated.csv', index=False)
            last_16_rows = df_t.tail(processor.buffer_size).to_csv(index=False, header=False)
            sender.queue_data(last_16_rows.splitlines())

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
    parser.add_argument('--N', type=int, default=15, help='An integer value for N (default: 10)')
    parser.add_argument('--K', type=int, default=20, help='An integer value for K (default: 20)')
    parser.add_argument('--M', type=int, default=6, help='An integer value for M (default: 6)')
    args = parser.parse_args()

    K = args.K
    N = args.N
    M = args.M
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

    observation_embedder = ObservationEmbedder(num_facial_types, facial_embed_dim, cnn_output_dim, lstm_hidden_dim,
                                               sequence_length)
    mi_embedder = SpeakingTurnDescriptorEmbedder(num_event_types, event_embedding_dim, embed_output_dim)
    chunk_embedder = ChunkDescriptorEmbedder(continious_embedding_dim=16, valence_embedding_dim=8, output_dim=64)

    model_path = resource_path('saved_model_NewmodelChunkd1000.pth')
    nn_model = Model_mlp_diff(observation_embedder, mi_embedder, chunk_embedder, sequence_length,
                              net_type="transformer")
    model = Model_Cond_Diffusion(nn_model, observation_embedder, mi_embedder, chunk_embedder, betas=(1e-4, 0.02),
                                 n_T=n_T, device=device, x_dim=x_dim, y_dim=y_dim, drop_prob=0, guide_w=guide_w)

    model.load_state_dict(torch.load(model_path, map_location=device))
    model.to(device)
    processor = RealTimeProcessor(delay, buffer_size=buffer , target_size=137)

    mirror = Miror(consecutive_zero_threshold=M)

    client = Client(5560, "localhost", buffer)
    client.connect_to_server()
    client.start_receiving()

    #Initialize the external tensor client
    tensor_client = ExternalTensorClient(conversion_table, server1_address="localhost", server1_port= 50200, server2_address="localhost", server2_port=50201)
    tensor_client.connect()

    sender = Sender(5561, "localhost")

    real_time_inference_loop(model, device, client, sender,  processor, mirror, tensor_client, N , K, guide_weight=guide_w)



if __name__ == "__main__":
    main()