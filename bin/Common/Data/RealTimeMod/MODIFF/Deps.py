import numpy as np
import struct
import socket
import torch
import time
import threading
from therapist_behavior_inference import get_therapist_intent
from client_behavior_inference import get_client_intent
from mistralai.client import MistralClient
import os
import json

api_key_file = os.path.join(os.path.dirname(__file__), 'api_key.txt')
with open(api_key_file, 'r') as f:
    MISTRAL_API_KEY = f.read()
client_online = MistralClient(api_key=MISTRAL_API_KEY)



class Miror:
    def __init__(self, consecutive_zero_threshold=10):
        self.stored_sequences = []
        self.consecutive_zero_threshold = consecutive_zero_threshold
        self.mirroring = False
        self.conversion_dict = {4: 1, 5: 2, 6: 3, 0: 0}  # Conversion mapping

    def store_sequence(self, sequence):
        self.stored_sequences.append(sequence)

        # Keep only the last 4 sequences
        if len(self.stored_sequences) > self.consecutive_zero_threshold:
            self.stored_sequences.pop(0)
        a=0
        for seq in self.stored_sequences:
            if not np.any(seq):
                a+=1
        if a == self.consecutive_zero_threshold:
            self.mirroring = True
            self.stored_sequences.clear()
        else:
            self.mirroring = False  # Deactivate mirroring mode

    def mirror_sequence(self, input_vector):
        input_values = input_vector # Convert tensor to numpy array
        mirrored_output = np.vectorize(self.conversion_dict.get)(input_values)  # Apply conversion
        return mirrored_output.tolist()

    def should_mirror(self):
        return self.mirroring


class ExternalTensorClient:
    def __init__(self,  conversion_table, server1_address, server1_port, server2_address, server2_port):
        self.server1_address = server1_address
        self.server1_port = server1_port
        self.server2_address = server2_address
        self.server2_port = server2_port
        self.conversion_table = conversion_table

        self.z_tensor = None  # Initialize z_tensor as None before any updates
        self.lock = threading.Lock()  # Use a lock to protect access to z_tensor
        self.server1_socket = None
        self.server2_socket = None

    def connect(self):
        """Attempt to connect to both servers, retrying if they are not available."""
        threading.Thread(target=self.connect_to_server1, daemon=True).start()
        threading.Thread(target=self.connect_to_server2, daemon=True).start()

    def connect_to_server1(self):
        """Keep trying to connect to server1."""
        while True:
            try:
                self.server1_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                # self.server1_socket.settimeout(1.0)
                self.server1_socket.connect((self.server1_address, self.server1_port))
                print(f"Connected to server1 at {self.server1_address}:{self.server1_port}")
                self.receive_from_server1()  # Start receiving data once connected
                break
            # except ConnectionRefusedError:
            except Exception:
                print(f"Server1 not available, retrying in 2 seconds...")
                time.sleep(2)  # Retry every 2 seconds

    def connect_to_server2(self):
        """Keep trying to connect to server2."""
        while True:
            try:
                self.server2_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                # self.server2_socket.settimeout(1.0)
                self.server2_socket.connect((self.server2_address, self.server2_port))
                print(f"Connected to server2 at {self.server2_address}:{self.server2_port}")
                self.receive_from_server2()  # Start receiving data once connected
                break
            # except ConnectionRefusedError:
            except Exception:
                print(f"Server2 not available, retrying in 2 seconds...")
                time.sleep(2)  # Retry every 2 seconds

    def receive_from_server1(self):
        """Receive strings from server1 and update z_tensor."""
        while True:
            try:
                serialized_data = self._receive_string(self.server1_socket)
                data = json.loads(serialized_data)
                if len(data)<2:
                    data.append(" ") # voir quel contexte par defaut on append quand le contexte est vide
                #print("Current sentence recived:", data[0])
                #print("Contexte recived :", data[1])
                da = get_therapist_intent(client_online, data[0], data[1])
                print("Dialogue act computed is :", da)
                z_tensor = self.generate_z_tensor_from_string(da)
                with self.lock:
                    self.z_tensor = z_tensor  # Update z_tensor safely
                print(f"Updated z_tensor from server1: {z_tensor}")
            except Exception as e:
                print(f"Error in server1: {e}")
                self.connect_to_server1()  # Reconnect if the connection drops
                break

    def receive_from_server2(self):
        """Receive strings from server2 and update z_tensor."""
        while True:
            try:
                serialized_data = self._receive_string(self.server2_socket)
                data = json.loads(serialized_data)

                if len(data)<2:
                    data.append(" ") # voir quel contexte par defaut on append quand le contexte est vide
                    
                # if isinstance(data, float):
                #     data = [" "] # voir quel contexte par defaut on append quand le contexte est vide                    
                # elif (len(data)<2):
                #     data.append(" ") # voir quel contexte par defaut on append quand le contexte est vide

                print("Current sentence recived:", data[0])
                print("Contexte recived :", data[1])
                da = get_client_intent(client_online, data[0], data[1])
                print("Dialogue act computed is :", da)
                z_tensor = self.generate_z_tensor_from_string(da)
                with self.lock:
                    self.z_tensor = z_tensor  # Update z_tensor safely
                print(f"Updated z_tensor from server2: {z_tensor}")
            except Exception as e:
                print(f"Error in server2: {e}")
                self.connect_to_server2()  # Reconnect if the connection drops
                break


    def _receive_string(self, sock):
        """Receive one length-prefixed string from a given socket."""
        length_prefix = self._recv_all(sock, 4)
        if not length_prefix:
            raise ConnectionError("Connection lost while reading message length")
        message_length = struct.unpack('!I', length_prefix)[0]
        data = self._recv_all(sock, message_length)
        if not data:
            raise ConnectionError("Connection lost while reading message data")
        string_value = data.decode('utf-8')
        return string_value

    def _recv_all(self, sock, n):
        """Helper method to receive n bytes or return None if EOF is hit."""
        data = bytearray()
        while len(data) < n:
            packet = sock.recv(n - len(data))
            if not packet:
                return None
            data.extend(packet)
        return bytes(data)

    def generate_z_tensor_from_string(self, string_value):
        # Retrieve the corresponding values from the conversion table
        value_one, value_two = self.conversion_table.get(string_value,
                                                    (0.0, 0.0))  # Default to (0.0, 0.0) if string not found

        # Third value is either 0 or the same as value_one (based on your explanation)
        value_three = value_one  # You can set this to value_one if needed, e.g., value_three = value_one
        # value_four = 0.0 si nécessaire

        # Create the z_tensor
        z_tensor = torch.tensor([[value_one, value_two, value_three]], dtype=torch.float32)

        return z_tensor

    def get_z_tensor(self):
        """Safely get the current z_tensor."""
        with self.lock:
            return self.z_tensor

    def close(self):
        """Close the connections to both servers."""
        if self.server1_socket:
            self.server1_socket.close()
        if self.server2_socket:
            self.server2_socket.close()
        print("Connections to both servers closed.")

class Sender:
    def __init__(self, forward_port, forward_address):
        self.forward_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.forward_port = forward_port
        self.forward_address = forward_address
        self.lock = threading.Lock()
        self.queue = []
        self.connect_to_forwarding_server()
        self.sending_thread = threading.Thread(target=self.send_data_continuously)
        self.sending_thread.daemon = True
        self.sending_thread.start()  # Start the thread

    def connect_to_forwarding_server(self):
        while True:
            try:
                self.forward_socket.connect((self.forward_address, self.forward_port))
                print("Connected to forwarding server at", self.forward_address, "on port", self.forward_port)
                break
            except ConnectionRefusedError:
                print("Forwarding server not active yet. Retrying in 2 seconds...")
                time.sleep(2)

    def queue_data(self, data):
        with self.lock:
            self.queue.extend(data)

    def send_data_continuously(self):
        while True:
            with self.lock:
                if self.queue:
                    row = self.queue.pop(0)
                    self.send_to_forwarding_server(row)
                    #print(f"Sent data: {row}")
            time.sleep(0.04)  # Sleep briefly to avoid busy-waiting #ROBINET A AJUSTER

    def send_to_forwarding_server(self, data):
        try:
            self.forward_socket.sendall((data + "\n").encode('utf-8'))
        except Exception as e:
            print("Failed to send data to forwarding server:", e)

class Client:
    def __init__(self, port, address, batch_size):
        self.client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.port = port
        self.address = address
        self.frames = []  # To accumulate frames
        self.batch_size = batch_size  # Define the batch size
        self.latest_processed_batch = None
        self.lock = threading.Lock()

    def connect_to_server(self):
        # Connect to the server
        self.client_socket.connect((self.address, self.port))
        print("Connected to server at", self.address, "on port", self.port)

    def connect_to_server(self):
        while True:
            try:
                self.client_socket.connect((self.address, self.port))
                print("Connected to server at", self.address, "on port", self.port)
                break
            except ConnectionRefusedError:
                print("Server not active yet. Retrying in 2 seconds...")
                time.sleep(2)
            except Exception:
                import traceback
                print("Server refuses binding. Retrying in 2 seconds...")
                traceback.print_exc()
                time.sleep(2)

    def start_receiving(self):
        thread = threading.Thread(target=self.receive_aus_continuously)
        thread.daemon = True  # Daemonize thread
        thread.start()

    def receive_aus_continuously(self):
        while True:
            self.receive_aus()

    def receive_aus(self):
        # Receive the data
        au1 = self.client_socket.recv(8)
        au2 = self.client_socket.recv(8)
        au12 = self.client_socket.recv(8)
        au6 = self.client_socket.recv(8)
        au25 = self.client_socket.recv(8)
        au9 = self.client_socket.recv(8)
        au10 = self.client_socket.recv(8)
        au14 = self.client_socket.recv(8)
        au15 = self.client_socket.recv(8)

        au1 = struct.unpack('!d', au1)[0]
        au2 = struct.unpack('!d', au2)[0]
        au12 = struct.unpack('!d', au12)[0]
        au6 = struct.unpack('!d', au6)[0]
        au25 = struct.unpack('!d', au25)[0]
        au9 = struct.unpack('!d', au9)[0]
        au10 = struct.unpack('!d', au10)[0]
        au14 = struct.unpack('!d', au14)[0]
        au15 = struct.unpack('!d', au15)[0]

        # Append the received frame to the frames list
        frame = [au1, au2, au12, au6, au25, au9, au10, au14, au15]
        self.frames.append(frame)

        if len(self.frames) == self.batch_size:
            self.process_and_send_batched_frames()
            self.frames = []  # Clear the frames list after sending

    def process_and_send_batched_frames(self):
        processed_frames = [self.process_frame(frame) for frame in self.frames]
        # Flatten the processed frames
        flat_frames = [item for frame in processed_frames for item in frame]
        with self.lock:
            self.latest_processed_batch = flat_frames
        return flat_frames

    def process_frame(self, frame):
        # Check for the highest value in the frame
        max_value = max(frame)
        if max_value <= 0.6:
            return [0]
        highest_au_index = frame.index(max_value)
        if highest_au_index in [2, 3, 4]:  # au12, au6, au25
            return [4]
        elif highest_au_index in [5, 6]:  # au9, au10
            return [5]
        elif highest_au_index in [7, 8]:  # au14, au15
            return [6]
        return [0]  # Default case, should not be reached

class RealTimeProcessor:
    def __init__(self, delay , buffer_size, target_size=137):
        self.buffer_size = buffer_size
        #self.buffer = buffer_sequence
        self.target_size = target_size+ delay
        self.buffer = [0] * buffer_size

    def update_buffer(self, new_data):
        if len(new_data) != self.buffer_size:
            raise ValueError("New data must be exactly 16 frames long.")
        #self.buffer = self.buffer[len(new_data):] + new_data
        self.buffer = new_data

    def project_to_target(self):
        buffer_length = len(self.buffer)
        projected = [0] * self.target_size

        # Calculate the projection ratio
        ratio = self.target_size / buffer_length

        # Iterate through the buffer and fill the projected array
        pos = 0
        for i in range(buffer_length):
            value = self.buffer[i]
            count = round((i + 1) * ratio) - round(i * ratio)
            for _ in range(count):
                if pos < self.target_size:
                    projected[pos] = value
                    pos += 1
                else:
                    break
        # print("projected client sequence ")
        # print(projected)
        return projected

    def reproject_to_buffer(self, projected, buffer_size):
        target_length = len(projected)
        reprojected = [0] * buffer_size
        # Calculate the reprojection ratio
        ratio = round(target_length / buffer_size)
        # Initialize the position in the projected array
        pos = 0.0

        for i in range(buffer_size):
            value_count = 0
            value_sum = 0
            next_pos = pos + ratio

            # Sum the values in the range corresponding to the current buffer position
            while pos < next_pos and int(pos) < target_length:
                value_sum += projected[int(pos)]
                value_count += 1
                pos += 1

            # Average the values and assign to the reprojected buffer
            if value_count > 0:
                reprojected[i] = round(value_sum / value_count)
            else:
                reprojected[i] = 0

        return reprojected

    def get_buffer(self):
        return self.buffer

    def get_projected_buffer(self):
        return self.project_to_target()