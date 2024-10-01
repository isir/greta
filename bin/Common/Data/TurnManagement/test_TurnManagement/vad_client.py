#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Fri Sep  6 11:10:55 2024

@author: takeshi-s
"""
# from matplotlib import pyplot as plt
# from pathlib import Path
# from tqdm import tqdm
import pprint as pp
# import pandas as pd
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

def main():
    
    "Main function"

    #######################################

    host = socket.gethostname()  # as both code is running on same pc
    port = 5959  # socket server port number

    client_socket = socket.socket()  # instantiate
    client_socket.connect((host, port))  # connect to the server
    
    RATE = 16000
    model = load_silero_vad()

    BUFFER_SIZE = 4096

    message = 'ok'

    while True:

        try:
            
            client_socket.send(message.encode())  # send message
            data = client_socket.recv(BUFFER_SIZE)  # receive response
            
            np_int16 = np.frombuffer(data, dtype=np.int16)
            np_float32 = int2float(np_int16)

            if RATE == 16000:
                np_float32 = np_float32[:512]
            if RATE == 8000:
                np_float32 = np_float32[:256]

            vad_result = model(torch.from_numpy(np_float32), RATE).item()
            
            print('\r' + '#' * int(vad_result*20) + ' ' * (20 - int(vad_result*20)), end='')            

        except KeyboardInterrupt:
            break
    
    # client_socket.send('kill'.encode())
    client_socket.close()  # close the connection
    print()
    print('Client has been closed')

def int2float(sound):
    
    abs_max = np.abs(sound).max()
    sound = sound.astype('float32')
    if abs_max > 0:
        sound *= 1/32768
    sound = sound.squeeze()  # depends on the use case
    
    return sound
    
if __name__ == '__main__':
    main()
