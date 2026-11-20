import socket
import threading
import struct
import time
from queue import Queue, Empty

class Sender(threading.Thread):
    def __init__(self, port, address, data_queue):
        super().__init__()
        self.port = port
        self.address = address
        self.data_queue = data_queue
        self.sender_socket = None
        self.connected = False
        self.stop_event = threading.Event()

    def run(self):
        while not self.stop_event.is_set():
            if not self.connected:
                self.connect_to_server()
            else:
                try:
                    data = self.data_queue.get(timeout=1)
                    self.send_data(data)
                except Empty:
                    continue
                except Exception as e:
                    print(f"Error sending data: {e}")
                    self.connected = False
                    self.sender_socket.close()
                    self.sender_socket = None

    def connect_to_server(self):
        while not self.stop_event.is_set():
            try:
                self.sender_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                self.sender_socket.connect((self.address, self.port))
                print("Sender connected to destination server at", self.address, "on port", self.port)
                self.connected = True
                break
            except ConnectionRefusedError:
                print("Destination server not active yet. Sender retrying in 2 seconds...")
                time.sleep(2)
            except Exception as e:
                print(f"Sender failed to connect to destination server: {e}")
                time.sleep(2)

    def send_data(self, data):
        # Assuming data is a list of integers
        data_format = f'!{len(data)}i'  # Network byte order integers
        packed_data = struct.pack(data_format, *data)
        self.sender_socket.sendall(packed_data)
        print("Sender sent data:", data)

    def stop(self):
        self.stop_event.set()
        if self.sender_socket:
            self.sender_socket.close()

class Client(threading.Thread):
    def __init__(self, port, address, batch_size=17, data_queue=None):
        super().__init__()
        self.port = port
        self.address = address
        self.batch_size = batch_size
        self.data_queue = data_queue
        self.client_socket = None
        self.frames = []  # To accumulate frames
        self.lock = threading.Lock()
        self.connected = False
        self.stop_event = threading.Event()

    def run(self):
        while not self.stop_event.is_set():
            if not self.connected:
                self.connect_to_server()
            else:
                self.receive_aus_continuously()

    def connect_to_server(self):
        while not self.stop_event.is_set():
            try:
                self.client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                self.client_socket.connect((self.address, self.port))
                print("Client connected to server at", self.address, "on port", self.port)
                self.connected = True
                break
            except ConnectionRefusedError:
                print("Source server not active yet. Client retrying in 2 seconds...")
                time.sleep(2)
            except Exception as e:
                print(f"Client failed to connect to source server: {e}")
                time.sleep(2)

    def receive_aus_continuously(self):
        while not self.stop_event.is_set():
            try:
                self.receive_aus()
            except Exception as e:
                print(f"Error receiving data: {e}")
                self.connected = False
                self.client_socket.close()
                self.client_socket = None
                break

    def receive_aus(self):
        # Receive the data
        au_list = []
        for _ in range(9):
            data = self.client_socket.recv(8)
            if not data:
                raise ConnectionError("Connection lost")
            au = struct.unpack('!d', data)[0]
            au_list.append(au)
        # Append the received frame to the frames list
        self.frames.append(au_list)
        if len(self.frames) == self.batch_size:
            self.process_and_send_batched_frames()
            self.frames = []  # Clear the frames list after sending

    def process_and_send_batched_frames(self):
        processed_frames = [self.process_frame(frame) for frame in self.frames]
        # Flatten the processed frames
        flat_frames = [item for frame in processed_frames for item in frame]
        with self.lock:
            self.latest_processed_batch = flat_frames
        # Put data into the queue
        if self.data_queue:
            self.data_queue.put(flat_frames)

    def process_frame(self, frame):
        # Check for the highest value in the frame
        max_value = max(frame)
        if max_value <= 0.5:
            return [0]
        highest_au_index = frame.index(max_value)
        if highest_au_index in [2, 3, 4]:  # au12, au6, au25
            return [4]
        elif highest_au_index in [5, 6]:  # au9, au10
            return [5]
        elif highest_au_index in [7, 8]:  # au14, au15
            return [6]
        return [0]  # Default case, should not be reached

    def stop(self):
        self.stop_event.set()
        if self.client_socket:
            self.client_socket.close()

# Main code integrating the Sender with the Client
if __name__ == '__main__':

    data_queue = Queue()

    # Instantiate the Sender thread
    sender = Sender(port=50153, address='localhost', data_queue=data_queue)
    sender.start()

    # Instantiate the Client thread
    client = Client(port=50150, address='localhost', batch_size=64, data_queue=data_queue)
    client.start()

    try:
        # Keep the main thread alive
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        print("Stopping threads...")
        client.stop()
        sender.stop()
        client.join()
        sender.join()