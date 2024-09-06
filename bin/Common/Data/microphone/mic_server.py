#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Fri Sep  6 11:10:43 2024

@author: takeshi-s
"""
import pprint as pp
import numpy as np
import traceback
import sys
import os

import socket

import pyaudio
from threading import Thread, Lock

def main():
    "Main function"

    #######################################
    
    # get the hostname
    host = socket.gethostname()
    port = 9000  # initiate port no above 1024

    server_socket = socket.socket()  # get instance
    # look closely. The bind() function takes tuple as argument
    server_socket.bind((host, port))  # bind host address and port together
    
    #######################################

    FORMAT = pyaudio.paInt16
    CHANNELS = 1
    # RATE = 44100
    RATE = 16000
    
    # INTERVAL = 0.1
    INTERVAL = 0.05
    # INTERVAL = 0.01
    
    BUFFER_SIZE = 4096
    
    CHUNK = int(RATE * INTERVAL)

    mic = Microphone(FORMAT, CHANNELS, RATE, CHUNK, BUFFER_SIZE)
    mic.start()

    #######################################

    # configure how many client the server can listen simultaneously
    server_socket.listen(10)
    
    thread_list = []
    
    while True:
        
        try:

            connection, address = server_socket.accept()  # accept new connection
            
            thread = Thread(target = on_new_client, args = (connection, address, mic))
            thread.name = str(address)
            thread.daemon = True
            thread.start()
            
            thread_list.append(thread)
            
            dead_flag_list = []
            for i, thread in enumerate(thread_list):
                if not thread.is_alive():
                    dead_flag_list.append(i)
            dead_flag_list.reverse()
            for index in dead_flag_list:
                thread_list.pop(index)
                
            print('Arriving connections:', end = '')
            for thread in thread_list:
                print(' ({}),'.format(thread.name), end = '')
            print()
        
        except KeyboardInterrupt:
            break
        
    # connection.close()  # close the connection
    server_socket.close()
    print()
    print('Server has been closed')  

class Microphone(Thread):
    
    def __init__(self, FORMAT, CHANNELS, RATE, CHUNK, BUFFER_SIZE):
        
        if (RATE == 16000) and (CHUNK < 512):
            print('CHUNK should be more than 512 for RATE 16000 (Current: {})'.format(CHUNK))
            sys.exit()
        elif (RATE == 8000) and (CHUNK < 256):
            print('CHUNK should be more than 256 for RATE 8000 (Current: {})'.format(CHUNK))
            sys.exit()

        # Thread.__init__(self)
        # self.daemon = True

        self.FORMAT = FORMAT
        self.CHANNELS = CHANNELS
        self.RATE = RATE
        self.CHUNK = CHUNK
        self.BUFFER_SIZE = BUFFER_SIZE
        
        self.data = None
        
        self.lock = Lock()
    
    def start(self):

        self.p = pyaudio.PyAudio()
        
        def callback(in_data, frame_count, time_info, status):
            return (in_data, pyaudio.paContinue)
         
        self.stream = self.p.open(format=self.FORMAT,
                        channels=self.CHANNELS,
                        rate=self.RATE,
                        input=True,
                        output=True,
                        # stream_callback=callback
                        )
         
        self.stream.start_stream()

        print("Mic stream started")
    
    def get_array(self):
        
        if self.stream.is_active():
                        
            buffer = self.get_buffer()

            audio_int16 = np.frombuffer(buffer, dtype=np.int16)
            audio_float32 = self.int2float(audio_int16)
            
            data = self.trim(audio_float32)
                    
        return data

    def get_buffer(self):
        
        if self.stream.is_active():
                        
            data = self.stream.read(self.CHUNK, False)
        
        return data
    
    def trim(self, data):
        
        if self.RATE == 16000:
            data = data[:512]
        if self.RATE == 8000:
            data = data[:256]
        
        return data
            
    def stop(self):
        
        self.stream.stop_stream()
        self.stream.close()
        self.p.terminate()
        
        print()
        print("Mic stream stopped")

    def int2float(self, sound):
        abs_max = np.abs(sound).max()
        sound = sound.astype('float32')
        if abs_max > 0:
            sound *= 1/32768
        sound = sound.squeeze()  # depends on the use case
        return sound

def on_new_client(clientsocket,address, mic):
    
    print("Connection from: " + str(address))
    
    while True:
        
        try:
            
            data = clientsocket.recv(mic.BUFFER_SIZE).decode()
            if data == '':
                break
            # print("from connected user: " + data)
            # data = input('SERVER >> ').encode()
            data = mic.get_buffer()
            clientsocket.send(data)
        
        except Exception as e:
            print(e)
            break
        
    clientsocket.close()
    print('Connection from: ' + str(address) + " closed")
    
if __name__ == '__main__':
    main()