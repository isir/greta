import socket
import pickle
import struct


class SimpleReceiver:
    def __init__(self, host='localhost', port=6500):
        self.host = host
        self.port = port
        self.sock = None

    def connect(self):
        self.sock = socket.socket()
        self.sock.connect((self.host, self.port))
        print("[Receiver] Connected to server.")

    def wait_for_audio_and_receive_array(self):
        header = self.sock.recv(5).decode()
        if header != "audio":
            print(f"[Receiver] Unexpected header: {header}")
            return None

        length_bytes = self.sock.recv(4)
        total_len = struct.unpack('>I', length_bytes)[0]
        payload = b""
        while len(payload) < total_len:
            payload += self.sock.recv(total_len - len(payload))

        audio_array = pickle.loads(payload)
        print(f"[Receiver] Received array of shape {audio_array.shape}")
        return audio_array

    def close(self):
        self.sock.close()


class SimpleSender:
    def __init__(self, host='localhost', port=6501):
        self.host = host
        self.port = port
        self.sock = None

    def connect(self):
        self.sock = socket.socket()
        self.sock.connect((self.host, self.port))
        print(f"[Sender] Connected to frame socket at {self.host}:{self.port}")

    def send_frame(self, frame_array):
        payload = pickle.dumps(frame_array)
        length = struct.pack('>I', len(payload))
        self.sock.sendall(b"frame" + length + payload)
        print(f"[Sender] Sent frame of shape {frame_array.shape}")

    def close(self):
        if self.sock:
            self.sock.close()
