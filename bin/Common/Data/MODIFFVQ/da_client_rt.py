"""
da_client_rt.py
---------------
Real-time dialog act client for the E3 predictor.

Connects to the DA_Server in MICounselorIncrementalDA.py (port 50202)
and receives classified dialog acts as [client_da, therapist_da] JSON.

Maps them to the MISC/MITI schema using dialog_act_mapping.py and
maintains a sliding window of W dialog act dicts aligned with the
AU inference loop.
"""

import json
import socket
import struct
import threading
from collections import deque

from dialog_act_mapping import (
    empty_dialog_act,
    therapist_intent_to_fields,
    client_intent_to_field,
)

DA_HOST = 'localhost'
DA_PORT = 50202


class DAClientRT:
    """
    Connects to DA_Server (port 50202) and maintains a sliding window
    of W dialog act dicts for the inference loop.
    """

    def __init__(self, window=8, host=DA_HOST, port=DA_PORT):
        self.window = window
        self.host   = host
        self.port   = port

        # Latest classified dialog act (updated on every message)
        self._current_da   = empty_dialog_act()
        self._da_lock      = threading.Lock()

        # Sliding window of W dicts (one per chunk)
        self._window_buf   = deque(maxlen=window)
        self._window_lock  = threading.Lock()

        self._running = False

    def start(self):
        """Connect to DA_Server and start receiving thread."""
        self._sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self._sock.connect((self.host, self.port))
        print(f"[DA] Connected to DA_Server at {self.host}:{self.port}")

        self._running = True
        self._thread  = threading.Thread(
            target=self._recv_loop, daemon=True
        )
        self._thread.start()

    def stop(self):
        self._running = False
        try:
            self._sock.close()
        except Exception:
            pass

    def _recv_all(self, n):
        """Read exactly n bytes from socket."""
        buf = b""
        while len(buf) < n:
            chunk = self._sock.recv(n - len(buf))
            if not chunk:
                raise ConnectionError("DA_Server closed connection")
            buf += chunk
        return buf

    def _recv_loop(self):
        """Receive [client_da, therapist_da] JSON messages from DA_Server."""
        while self._running:
            try:
                # Read 4-byte length prefix
                length_bytes = self._recv_all(4)
                length = struct.unpack('!I', length_bytes)[0]

                # Read the JSON payload
                data_bytes = self._recv_all(length)
                payload    = json.loads(data_bytes.decode('utf-8'))

                # payload is [client_da_string, therapist_da_string]
                if isinstance(payload, list) and len(payload) >= 2:
                    client_da_str   = payload[0]
                    therapist_da_str = payload[1]
                    silence_vec = payload[2] if len(payload) >= 3 else [0, 0, 0, 0]
                    self._update_da(payload[0], payload[1], silence_vec)

            except ConnectionError as e:
                if self._running:
                    print(f"[DA] Connection error: {e}")
                break
            except Exception as e:
                if self._running:
                    print(f"[DA] Recv error: {e}")

    def _update_da(self, client_da_str, therapist_da_str,silence_vec=None):
        """Map raw intent strings to MISC/MITI schema and update current DA."""
        da = empty_dialog_act()

        # Map client intent
        client_fields = client_intent_to_field(client_da_str)
        da.update(client_fields)

        # Map therapist intent
        therapist_fields = therapist_intent_to_fields(therapist_da_str)
        da.update(therapist_fields)

        if silence_vec is not None:
            da['silences'] = silence_vec

        with self._da_lock:
            self._current_da = da

        print(f"[DA] Updated: client='{client_da_str}' -> {client_fields} | "
              f"therapist='{therapist_da_str}' -> "
              f"main={da['main_therapist_behaviour']} | "
              f"reflection={da['reflection_subtype']} | "
              f"question={da['question_subtype']} | "
              f"therapist_input={da['therapist_input_subtype']} | "
              f"silences={da['silences']}")



    def get_current_da(self):
        """Return the most recent dialog act dict."""
        with self._da_lock:
            return dict(self._current_da)

    def push_to_window(self):
        """
        Push the current dialog act to the sliding window.
        Call once per chunk (aligned with AU chunk accumulation).
        """
        with self._da_lock:
            da = dict(self._current_da)
        with self._window_lock:
            self._window_buf.append(da)

    def get_da_window(self):
        """
        Returns the current W-chunk sliding window as a list of dicts,
        or None if not yet full.
        """
        with self._window_lock:
            if len(self._window_buf) < self.window:
                return None
            return list(self._window_buf)

    def is_ready(self):
        with self._window_lock:
            return len(self._window_buf) >= self.window