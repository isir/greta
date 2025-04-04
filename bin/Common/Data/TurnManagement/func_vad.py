#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Fri Sep  6 11:10:55 2024

@author: takeshi-s
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
from silero_vad import load_silero_vad

# from threading import Thread, Lock

import multiprocessing
from multiprocessing import Process, Manager, Value, Lock, Queue
import ctypes

# For VAP
import pickle
import librosa
from finetune_vapNotFreeze import VAPModel, DataConfig, OptConfig, get_run_name
from utils import everything_deterministic, write_json
from model import VapGPT, VapConfig
from events import TurnTakingEvents, EventConfig

# For face image cripper
import dlib
import cv2

from torch.autograd import profiler

import signal

from func_util import Behaviors, wait, int2float

# For mainloop timeout
# s_mainloop = time.time()
# s_mainloop = time.perf_counter()
# timeout_loop_started = False

os.environ["OPENCV_FFMPEG_READ_ATTEMPTS"] = "8192"
torch.backends.cudnn.benchmark = True

class VAD(object):
    
    def __init__(self, host, port, rate, buffer_size):

        self.host = host
        self.port = port
        self.rate = rate
        self.buffer_size = buffer_size
        
        
        self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        # self.device = torch.device("cpu")


        while True:
            try:

                self.client_socket = socket.socket()
                self.client_socket.settimeout(0.5)
                self.client_socket.connect((host, port))
                break

            except Exception as e:
                print("[TurnManagement VAD] Trying to connect to mic server ({})".format(e))
                time.sleep(1)
        
        # self.vad_result = 0
        
        self.lock = Lock()
        
        self.is_active = Value(ctypes.c_bool, False)
        
        self.result_queue = Queue()
    
    def start(self):
        
        self.is_active.value = True
        self.main_process = Process(target = self.main_loop, args=(self.device, self.rate, 
                                                                   self.lock, self.result_queue, self.is_active))
        self.main_process.daemon = True
        self.main_process.start()
        print('[TurnManagement VAD] loop started')
    
    # def main_loop(self, queue, is_active, client_socket, rate, device, model, ):
    def main_loop(self, device, rate, lock, result_queue, is_active):
        
        message = 'ok'

        model = load_silero_vad()
        model = model.to(device)
        
        while is_active:

            try:
                
                self.client_socket.send('{}\r\n'.format(message).encode())  # send message
                data = self.client_socket.recv(self.buffer_size)  # receive response
                
                np_int16 = np.frombuffer(data, dtype=np.int16)
                                
                np_float32 = int2float(np_int16)

                if self.rate == 16000:
                    np_float32 = np_float32[:512]
                if self.rate == 8000:
                    np_float32 = np_float32[:256]
                    
                torch_float32 = torch.from_numpy(np_float32)
                torch_float32 = torch_float32.to(device)

                vad_result = model(torch_float32, rate).item()
                
                # print('\r' + '#' * int(vad_result*20) + ' ' * (20 - int(vad_result*20)), end='')
                
                # print('in loop', vad_result)
                
                lock.acquire()
                result_queue.put(vad_result)
                lock.release()
            
            except socket.timeout:
                
                print('[TurnManagement VAD] waiting for microphone connection [timeout]')
                time.sleep(1)

            except ConnectionResetError:

                print('[TurnManagement VAD] waiting for microphone connection [ConnectionReset]')
                time.sleep(1)
                
            except WindowsError as e:
                if e.winerror == 10057:
                    print("[TurnManagement VAD] waiting for microphone connection [NotConnected]")
                    time.sleep(1)

            except Exception:
                
                # self.lock.acquire()
                # self.is_active = False
                # self.lock.release()
                
                traceback.print_exc()
                time.sleep(1)
                
                # just in case
                lock.acquire()
                is_active = False
                lock.release()
                break
            
            wait(0.01)
            
                # pass
        print('[TurnManagement VAD] loop stopped')

    def get_vad_result(self):
        
        return self.result_queue.get()
    
    def stop(self):
        
        self.lock.acquire()
        self.is_active.value = False
        self.lock.release()
        
        time.sleep(1)
        
        # self.client_socket.send('kill'.encode())
        self.client_socket.close()  # close the connection
        # print()
        # print('Client has been closed')
        
    def updateMicPort(self, new_port):
        
        # self.stop()
        
        while True:
        
            try:

                self.client_socket = socket.socket()
                self.client_socket.connect((self.host, new_port))
                break

            except Exception as e:
                print("[TurnManagement VAD] Trying to connect to mic server ({})".format(e))
                time.sleep(1)
        
        # self.start()
        
        print("[TurnManagement VAD] Mic server updated")
