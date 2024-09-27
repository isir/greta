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

import multiprocessing
process = multiprocessing.current_process()
print('[TurnManagement kill server] PID = {}'.format(process.pid))

def main(PORT = 5961):
    
    HOST = socket.gethostname()
    
    # PORT = 9000
    PORT = int(PORT)
    
    BUFSIZE = 4096
    
    client = socket.socket()
    
    try:
    
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
        
        print('TurnManagement server (port {}) has been killed'.format(PORT))
        
    except:
        
        print('Failed to close TurnManagement server (port {}), but this is normal if there is other TurnManagement server.'.format(PORT))

if __name__ == '__main__':
    args = sys.argv
    if len(args) >= 2:
        main(args[1])
    else:
        main()