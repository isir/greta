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
    
    HOST = 'localhost'
    PORT = 3150
    BUFSIZE = 4096
    
    try:
        
        client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        client.connect((HOST, PORT))
        data = client.recv(BUFSIZE)
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
    
    except:
        import traceback
        traceback.print_exc()

if __name__ == '__main__':
    main()