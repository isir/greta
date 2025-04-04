# -*- coding: utf-8 -*-
"""
Created on Thu Mar 27 10:02:44 2025

@author: takes
"""


import socket
import time

def main():
    
    hostname = socket.gethostname()
    client = socket.socket()
    
    print("Trying to connect...", end="", flush=True)
    while True:
        try:
            client.connect((hostname, 6544))
            break
        except:
            pass
    print("done")
    
    client.settimeout(0.01)
    
    while True:
        try:
            val = client.recv(1024).decode()
            print(val, flush=True)
            time.sleep(0.01)
        except:
            pass
    
if __name__ == '__main__':
    main()