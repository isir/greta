# -*- coding: utf-8 -*-
"""
Created on Wed Mar 19 13:12:52 2025

@author: takes
"""

import pprint as pp
import time
import csv
import sys
import os

import socket
import numpy as np

def main():
    
    address = socket.gethostname()
    port = 9000
    
    print('trying to connect to {}, {}'.format(address, port))
    
    client = socket.socket()
    client.connect((address, port))
    
    print('connected to {}, {}'.format(address, port))
    
    cnt = 0
    
    while True:
        s_time = time.time()
        client.send('ok'.encode())
        # _ = client.recv(4096).decode()
        data = client.recv(4096)
        data = np.frombuffer(data, dtype=np.int16)
        # print(type(_))
        # print(np.shape(_))
        # print(_)
        cnt += 1
        e_time = time.time()

        print("{:05d}, {:10.2f}, {:10.2f}, {}".format(cnt, data[-1], e_time - s_time, len(data)))

if __name__ == '__main__':
    main()