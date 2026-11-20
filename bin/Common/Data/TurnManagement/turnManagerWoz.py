#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Wizard of Oz Turn Manager for Greta.

Simplified version of turnManager.py that only reports Voice Activity Detection
(VAD) state to Java. The backchannel decision is left to the human operator
through the WoZ interface buttons.

Sends to Java:
  - "userSpeaking"  : user voice activity detected (utterance started)
  - "userSilent"    : user is not speaking

Note: Agent speaking state is NOT tracked in Python (same behavior as the
original turnManager.py where the global variable bug prevents it from working).
The Java side handles the agent-speaking guard independently.

Based on turnManager.py by Takeshi Saga.
"""

import pprint as pp
import numpy as np
import traceback
import shutil
import math
import time
import csv
import sys
import os

import socket
import torch
from silero_vad import load_silero_vad, read_audio, get_speech_timestamps

from threading import Thread, Lock

import multiprocessing
process = multiprocessing.current_process()
print('[TurnManagementWoZ Greta] PID = {}'.format(process.pid))

agent_speaking_state = False


def main():
    
    print("[TurnManagementWoZ Greta] Python start")
    
    "Main function"

    #######################################

    mic_host = socket.gethostname()
    mic_port = 9000

    feedback_server_host = socket.gethostname()
    feedback_server_port = 5960
    
    greta_host = socket.gethostname()
    greta_port = 5961
    
    RATE = 16000

    audio_buffer_size = 4096
    text_buffer_size = 100
    
    vad_interval = 0.05
    
    #######################################

    vad_threshold = 0.5

    # silence threshold in seconds to determine that the user stopped speaking
    # Set higher (1.5s) to avoid losing userSpeaking during brief pauses between words
    silence_threshold = 1.5
    silence_threshold_size = int(silence_threshold / vad_interval)

    # non-silence threshold in seconds to determine user has started to speak
    # Set lower (0.2s) for faster response to user starting to speak
    utterance_start_threshold = 0.2
    utterance_start_threshold_size = int(utterance_start_threshold / vad_interval)

    # History size: max of the two thresholds
    vad_history_size = max(silence_threshold_size, utterance_start_threshold_size) + 5

    vad_history = []
    vad_result_binary = 0
    
    system_ready = False
    utterance_start_detected = False
    
    #######################################
    
    vad_client = VAD(mic_host, mic_port, RATE, audio_buffer_size)
    vad_client.start()
    
    global_lock = Lock()

    feedback_socket = socket.socket()
    feedback_socket.connect((feedback_server_host, feedback_server_port))
    feedback_socket.settimeout(0.05)
    feedback_thread = Thread(target=feedback_loop, args=(feedback_socket, vad_client, global_lock, text_buffer_size))
    feedback_thread.daemon = True
    feedback_thread.start()
    
    greta_socket = socket.socket()
    greta_socket.connect((greta_host, greta_port))
    
    # Wait for initial connection acknowledgement from Java
    while True:
        try:
            message = greta_socket.recv(text_buffer_size)
            print('[TurnManagementWoZ Greta] connection established:', message.decode())
            break
        except socket.timeout:
            pass
        except Exception as e:
            print(e)
            sys.exit()
    
    cnt = 0
    while True:
        
        try:
            
            s_time = time.perf_counter()
            
            if not vad_client.is_active:
                break
            
            vad_result = vad_client.get_vad_result()
                        
            if vad_result > vad_threshold:
                vad_result_binary = 1
            else:
                vad_result_binary = 0
                
            vad_history.append(vad_result_binary)
            if len(vad_history) > vad_history_size:
                vad_history = vad_history[-vad_history_size:]
            else:
                continue
            
            if system_ready == False:
                system_ready = True
                greta_socket.send('[TurnManagementWoZ Greta] Generator started\r\n'.encode())
                greta_socket.settimeout(1)
                
            ###############################################
            # Determine VAD state for the WoZ interface
            # Note: agent speaking state is NOT checked here.
            # The Java side handles the agent-speaking guard.
            ###############################################

            # Detect utterance start: consecutive 1s in VAD history
            if (vad_result_binary == 1) and (not utterance_start_detected):
                consecutive_speech = 0
                for i in range(1, len(vad_history) + 1):
                    if vad_history[-i] == 1:
                        consecutive_speech += 1
                    else:
                        break
                if consecutive_speech >= utterance_start_threshold_size:
                    utterance_start_detected = True
                    print("[TurnManagementWoZ] Utterance start detected (consecutive speech: {})".format(consecutive_speech))

            # Detect silence: consecutive 0s in VAD history
            if (vad_result_binary == 0) and utterance_start_detected:
                consecutive_silence = 0
                for i in range(1, len(vad_history) + 1):
                    if vad_history[-i] == 0:
                        consecutive_silence += 1
                    else:
                        break
                if consecutive_silence >= silence_threshold_size:
                    utterance_start_detected = False
                    print("[TurnManagementWoZ] User silence detected (consecutive silence: {})".format(consecutive_silence))

            # Send state to Java
            if utterance_start_detected:
                action = "userSpeaking"
            else:
                action = "userSilent"

            greta_socket.send('{}\r\n'.format(action).encode())

            message = greta_socket.recv(text_buffer_size).decode()

            if 'kill' in message:
                print("[TurnManagementWoZ Greta] kill signal received")
                break
            if "updateMicPort" in message:
                new_port = int(message.split(" ")[1])
                vad_client.updateMicPort(new_port)
            
            cnt += 1
            
            # Maintain constant VAD interval
            e_time = time.perf_counter()
            while (e_time - s_time) < vad_interval:
                time.sleep(vad_interval - (e_time - s_time))
                e_time = time.perf_counter()
        
        except KeyboardInterrupt:
            break
        
        except Exception as e:
            traceback.print_exc()
            break
        
    vad_client.stop()
    feedback_socket.close()
    greta_socket.close()
    
    print("[TurnManagementWoZ Greta] Python end")

    
def feedback_loop(feedback_socket, vad_client, lock, text_buffer_size):
    
    print('[TurnManagementWoZ feedback] loop started')
    
    while True:
        
        try:
            
            data = feedback_socket.recv(text_buffer_size).decode()

            # Note: we receive the feedback but don't use agent_speaking_state
            # for VAD decisions. The Java side handles the agent-speaking guard.

            data = 'ok'
            feedback_socket.send('{}\r\n'.format(data).encode())
        
        except socket.timeout:
            continue
        
        except ConnectionResetError:
            print('[TurnManagementWoZ feedback] ConnectionReset')
            break
            
        except WindowsError as e:
            if e.winerror == 10057:
                print("[TurnManagementWoZ feedback] NotConnected")
                break
                
        except Exception:
            traceback.print_exc()
            break
    
    feedback_socket.close()

    print('[TurnManagementWoZ feedback] loop ended')


class VAD(object):
    
    def __init__(self, host, port, rate, buffer_size):

        self.host = host
        self.port = port
        self.rate = rate
        self.buffer_size = buffer_size
        
        self.model = load_silero_vad()

        while True:
            try:
                self.client_socket = socket.socket()
                self.client_socket.settimeout(1.0)
                self.client_socket.connect((host, port))
                break
            except Exception as e:
                print("[TurnManagementWoZ VAD] Trying to connect to mic server ({})".format(e))
                time.sleep(1)
        
        self.vad_result = 0
        
        self.lock = Lock()
        
        self.is_active = False
    
    def start(self):
        
        self.is_active = True
        self.main_thread = Thread(target=self.main_loop)
        self.main_thread.daemon = True
        self.main_thread.start()
        print('[TurnManagementWoZ VAD] loop started')
    
    def main_loop(self):
        
        message = 'ok'
        
        while self.is_active:

            try:
                
                self.client_socket.send('{}\r\n'.format(message).encode())
                data = self.client_socket.recv(self.buffer_size)
                
                np_int16 = np.frombuffer(data, dtype=np.int16)
                                
                np_float32 = int2float(np_int16)

                if self.rate == 16000:
                    np_float32 = np_float32[:512]
                if self.rate == 8000:
                    np_float32 = np_float32[:256]

                vad_result = self.model(torch.from_numpy(np_float32), self.rate).item()
                
                self.lock.acquire()
                self.vad_result = vad_result
                self.lock.release()
            
            except socket.timeout:
                print('[TurnManagementWoZ VAD] waiting for microphone connection [timeout]')
                time.sleep(1)

            except ConnectionResetError:
                print('[TurnManagementWoZ VAD] waiting for microphone connection [ConnectionReset]')
                time.sleep(1)
                
            except WindowsError as e:
                if e.winerror == 10057:
                    print("[TurnManagementWoZ VAD] waiting for microphone connection [NotConnected]")
                    time.sleep(1)

            except Exception:
                traceback.print_exc()
                time.sleep(1)
                
                self.lock.acquire()
                self.is_active = False
                self.lock.release()
                break
            
        print('[TurnManagementWoZ VAD] loop stopped')

    def get_vad_result(self):
        return self.vad_result
    
    def stop(self):
        
        self.lock.acquire()
        self.is_active = False
        self.lock.release()
        
        time.sleep(1)
        
        self.client_socket.close()
        
    def updateMicPort(self, new_port):
        
        while True:
            try:
                self.client_socket = socket.socket()
                self.client_socket.connect((self.host, new_port))
                break
            except Exception as e:
                print("[TurnManagementWoZ VAD] Trying to connect to mic server ({})".format(e))
                time.sleep(1)
        
        print("[TurnManagementWoZ VAD] Mic server updated")


def int2float(sound):
    
    abs_max = np.abs(sound).max()
    sound = sound.astype('float32')
    if abs_max > 0:
        sound *= 1/32768
    sound = sound.squeeze()
    
    return sound
    
if __name__ == '__main__':
    main()
