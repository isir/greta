# -*- coding: utf-8 -*-
"""
Created on Wed Jun 26 17:00:59 2024

@author: takes
"""

import pprint as pp
import time
import sys
import os

import socket
import pickle
import base64

def main():
    
    HOST = socket.gethostname()
    PORT = 9000
    BUFSIZE = 4096
    
    client = socket.socket()
    client.connect((HOST, PORT))
    # data = client.recv(BUFSIZE)
    # print(data.decode('UTF-8'))
    
    # while True:
    #     data = input()
    #     client.sendall(data.encode('UTF-8'))
    #     if data == 'end':
    #         break
    
    s_time = time.time()
    
    data = 'kill'
    client.sendall(data.encode('UTF-8'))
    
    e_time = time.time()
    # print('Process time: {}'.format(e_time - s_time))

    client.close()

if __name__ == '__main__':
    main()