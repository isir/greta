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

import multiprocessing
process = multiprocessing.current_process()
print('[Microphone] PID = {}'.format(process.pid))


def main(port = 9000):
    
    start_mic_server(port)
    
def  start_mic_server(port = 9000):
    
    "Main function"

    #######################################
    
    # get the hostname
    host = socket.gethostname()
    
    # port = 9000
    port = int(port)

    server_socket = socket.socket()  # get instance
    # look closely. The bind() function takes tuple as argument
    server_socket.bind((host, port))  # bind host address and port together
    
    print("[Microphone] Microphone server has been launched at {}, port {}".format(host, port))
    
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
    
    KILL_SIGNAL = False

    mic = Microphone(FORMAT, CHANNELS, RATE, CHUNK, BUFFER_SIZE, KILL_SIGNAL)
    mic.start()

    #######################################

    # configure how many client the server can listen simultaneously
    server_socket.listen(10)
    server_socket.settimeout(0.5)
    
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
                
            print('[Microphone] Arriving connections:', end = '')
            for thread in thread_list:
                print(' ({}),'.format(thread.name), end = '')
            print()

            print('[Microphone] mic.KILL_SIGNAL = ', mic.KILL_SIGNAL)
            if mic.KILL_SIGNAL:
                break

        except socket.timeout:
            # print('[Microphone] server socket timeout')
            continue
        
        except KeyboardInterrupt:
            break
        
        
        
    # connection.close()  # close the connection
    mic.stop()
    server_socket.close()
    # print()
    print('[Microphone] Microphone server has been closed')  

class Microphone(Thread):
    
    def __init__(self, FORMAT, CHANNELS, RATE, CHUNK, BUFFER_SIZE, KILL_SIGNAL):
        
        if (RATE == 16000) and (CHUNK < 512):
            print('[Microphone] CHUNK should be more than 512 for RATE 16000 (Current: {})'.format(CHUNK))
            sys.exit()
        elif (RATE == 8000) and (CHUNK < 256):
            print('[Microphone] CHUNK should be more than 256 for RATE 8000 (Current: {})'.format(CHUNK))
            sys.exit()

        # Thread.__init__(self)
        # self.daemon = True

        self.FORMAT = FORMAT
        self.CHANNELS = CHANNELS
        self.RATE = RATE
        self.CHUNK = CHUNK
        self.BUFFER_SIZE = BUFFER_SIZE
        self.KILL_SIGNAL = KILL_SIGNAL
        
        self.data = None
        
        self.lock = Lock()
    
    def start(self):

        self.p = pyaudio.PyAudio()
        
        info = self.p.get_host_api_info_by_index(0)
        numdevices = info.get('deviceCount')
        
        for i in range(0, numdevices):
            if (self.p.get_device_info_by_host_api_device_index(0, i).get('maxInputChannels')) > 0:
                device_info = "Input Device id {} - {}".format(i, self.p.get_device_info_by_host_api_device_index(0, i).get('name'))
                
                #In case device name includes non-unicode character, just replace it with unicode character
                device_info = device_info.encode("ascii", 'ignore').decode()
                # device_info = unidecode(unicode(device_info, 'utf-8'))
                
                print(device_info)
        
        mic_index = 0
        mic_name = self.p.get_device_info_by_host_api_device_index(0, i).get('name').encode('ascii', 'ignore')
        print("### Selected mic is {} with index {}".format(mic_name, mic_index))
        
        print("##############################################################################")
        print("[INFO] If you want to specify microphone to use,")
        print("       please specify device index by changing \"input_device_index\"")
        print("       in bin\Common\Data\microphone\mic_server.py")
        print("##############################################################################")
        
        def callback(in_data, frame_count, time_info, status):
            return (in_data, pyaudio.paContinue)
         
        self.stream = self.p.open(format=self.FORMAT,
                        channels=self.CHANNELS,
                        rate=self.RATE,
                        input=True,
                        output=True,
                        # stream_callback=callback,
                        input_device_index = mic_index
                        )
         
        self.stream.start_stream()

        print("[Microphone] Mic stream started")
    
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
        
        # print()
        print("[Microphone] Mic stream stopped")

    def int2float(self, sound):
        abs_max = np.abs(sound).max()
        sound = sound.astype('float32')
        if abs_max > 0:
            sound *= 1/32768
        sound = sound.squeeze()  # depends on the use case
        return sound
    
    def setKILL_SIGNAL(self):
        
        self.lock.acquire()
        self.KILL_SIGNAL = True
        self.lock.release()

def on_new_client(clientsocket,address, mic):
    
    print("[Microphone] Connection from: " + str(address))
    
    while True:
        
        try:
            
            data = clientsocket.recv(mic.BUFFER_SIZE).decode()
            if data == 'kill':
                clientsocket.send("kill signal received\r\n".encode())
                mic.setKILL_SIGNAL()
                mic.stop()
                break
            # print("from connected user: " + data)
            # data = input('SERVER >> ').encode()
            data = mic.get_buffer()
            clientsocket.send(data)
        
        except Exception as e:
            print(e)
            break
        
    clientsocket.close()
    print('[Microphone] Connection from: ' + str(address) + " closed")
    
if __name__ == '__main__':
    
    args = sys.argv
    
    if len(args) >= 2:
        main(args[1])
    else:
        main()