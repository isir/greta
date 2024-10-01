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

from threading import Thread, Lock

agent_speaking_state = False

def main():
    
    "Main function"

    #######################################

    mic_host = socket.gethostname()  # as both code is running on same pc
    mic_port = 9000  # socket server port number
    
    feedback_server_host = socket.gethostname()
    feedback_server_port = 5960
    
    RATE = 16000

    BUFFER_SIZE = 4096
    
    vad_interval = 0.01
    
    #######################################

    vad_threshold = 0.8
    
    turn_shift_threshold = 1.0
    vad_history_size = int(turn_shift_threshold/vad_interval)
    
    vad_history = []
    vad_result_binary = 0
    
    system_ready = False
    agent_speaking_state_history = []
    utterance_start_detected = False
    
    feedback_is_alive = False
    
    #######################################
    
    # feedback_socket.bind((feedback_server_host, feedback_server_port))
    # feedback_socket.settimeout(0.2) # set timeout for listening
    # feedback_socket.listen(10) # configure how many client the server can listen simultaneously
    
    test_data = test_data_generator()

    vad_client = VAD(mic_host, mic_port, RATE, BUFFER_SIZE)
    vad_client.start()
    
    global_lock = Lock()

    feedback_socket = socket.socket()
    feedback_socket.connect((feedback_server_host, feedback_server_port))
    feedback_thread = Thread(target = feedback_loop, args = (feedback_socket, vad_client, global_lock))
    feedback_thread.daemon = True
    feedback_thread.start()
    
    # print()
    cnt = 0
    while True:
                
        try:
            
            vad_result = vad_client.get_vad_result()
            # print('\r' + 'VAD result: ' + '#' * int(vad_result*20) + ' ' * (20 - int(vad_result*20)), end='')            
                        
            if vad_result > vad_threshold:
                vad_result_binary = 1
            else:
                vad_result_binary = 0
            agent_speaking_state_history.append(agent_speaking_state)
                
            vad_history.append(vad_result_binary)
            if len(vad_history) > vad_history_size:
                vad_history = vad_history[-vad_history_size:]
                agent_speaking_state_history = agent_speaking_state_history[-vad_history_size:]
            else:
                # print('Warming up')
                continue
            
            if system_ready == False:
                system_ready = True
                print('Generator started')
            
            # print(vad_history[-20:])
                
            action, behavior_cnt, utterance_start_detected = generate_behavior(
                vad_result_binary, vad_history, agent_speaking_state, agent_speaking_state_history, vad_interval,
                utterance_start_detected, cnt)
            
            
            if (action != "nothing") and (action != "waiting"):
                # if vad_result_binary == 0:
                #     print("cnt {:>10d}, vad: {:>2d}, agent_speaking_state: {:>02d}, action: {:<20s}, IPU cnt: {:>02d}".format(cnt, vad_result_binary, agent_speaking_state, action, behavior_cnt))
                # if vad_result_binary == 1:
                #     print("cnt {:>10d}, vad: {:>2d}, agent_speaking_state: {:>02d}, action: {:<20s}, start speak cnt: {:>02d}".format(cnt, vad_result_binary, agent_speaking_state, action, behavior_cnt))
                print(action)

            # print("cnt {:>10d}, vad: {:>2d}, agent_speaking_state: {:>02d}, action: {:>10s}, cnt: {:>02d}".format(cnt, vad_result_binary, agent_speaking_state, action, behavior_cnt))


            time.sleep(vad_interval)
            
            cnt += 1
            
            if action == 'turnShift':
                break
        
        except KeyboardInterrupt:

            break
        
    vad_client.stop()
    feedback_socket.close()
    
def feedback_loop(clientsocket, vad_client, lock):

    global agent_speaking_state
    
    print('Feedback loop started')
    
    while True:
        
        try:
            
            data = clientsocket.recv(vad_client.buffer_size).decode()

            lock.acquire()
            agent_speaking_state = data
            lock.release()

            data = 'ok'
            clientsocket.send(data)
        
        except Exception as e:
            # print(e)
            break
    
    clientsocket.close()

    print('Feedback loop ended')

def test_data_generator():

    test_data = []
    test_data.extend([0 for x in range(100)])
    # test_data.extend([1 for x in range(200)])
    # test_data.extend([0 for x in range(20)])
    # test_data.extend([1 for x in range(100)])
    
    index = 0
    while True:
        
        output = test_data[index]
        
        yield output
        
        index += 1
        if index == len(test_data):
            index = 0

def get_agent_speaking_state(test_data = None):
    
    speaking_state = 0

    if test_data != None:
        
        speaking_state = next(test_data)
        
    else:
        
        assert True, "get_agent_speaking_state is not implemented yet"
    
    return speaking_state

def generate_behavior(vad_result_binary, vad_history, agent_speaking_state, agent_speaking_state_history, vad_interval, 
                      utterance_start_detected, global_cnt):
    
    """
    Rule 1:
        Return do nothing signal if agent is speaking
    Rule 2:
        Return turn shift signal if all vad_history is 0
    Rule 3:
        Return backchannel signal if IPU (silence more than 0.2 seconds) detected
    """
    
    s_time = time.perf_counter()
    
    # silence threshold in seconds to determine Inter-Pausal Unit (IPU) boundary
    IPU_threshold = 0.2
    IPU_threshold_size = int(IPU_threshold/vad_interval)
    
    # # silence threshold in seconds to determine turn shift
    # agent_turn_threshold = 1.0
    # agent_turn_threshold_size = int(agent_turn_threshold/vad_interval)
    
    # non-silence threshold in seconds to determine user has started to speak
    utterance_start_threshold = 0.3
    utterance_start_threshold_size = int(utterance_start_threshold/vad_interval)
    
    behaviors = Behaviors()
    IPU_detected = False
    turn_shift_detected = False
    # utterance_start_detected = False
    
    # print(vad_history)
    
    if agent_speaking_state:
        
        return behaviors.nothing
    
    if not agent_speaking_state:
        
        ##################################################
        # Turn shift (user to agent) detection
        ##################################################

        if (utterance_start_detected) and not (1 in vad_history):
            turn_shift_detected = True
        
        ##################################################
        # IPU, utterance start detection
        ##################################################

        cnt = 0
        for i in range(1, len(vad_history)+1):
            i = -i
            # print(i)
            # input()
            
            # IPU detection
            if vad_result_binary == 0:

                if vad_history[i] == 0:
                    cnt += 1
                    # if cnt == IPU_threshold_size:
                    #     IPU_detected = True
                    #     # print('IPU detected')
                    #     break
                else:
                    IPU_detected = False
                    # print('IPU not detected')
                    break
        
            # User's start of speaking detection
            elif (vad_result_binary == 1) and (not utterance_start_detected):
                    
                if vad_history[i] == 1:
                    cnt += 1
                    # if cnt == user_speak_threshold_size:
                    #     utterance_start_detected = True
                    #     # print('start detected')
                    #     break
                else:
                    # utterance_start_detected = False
                    # print('start not detected')
                    break
            elif (vad_result_binary == 1):
                if vad_history[i] == 1:
                    cnt += 1
                    # if cnt == user_speak_threshold_size:
                    #     utterance_start_detected = True
                    #     # print('start detected')
                    #     break
                else:
                    # utterance_start_detected = False
                    # print('start not detected')
                    break
            
        if (vad_result_binary == 0) and (cnt == IPU_threshold_size):
            IPU_detected = True
        else:
            IPU_detected = False

        if (vad_result_binary == 1) and (cnt == utterance_start_threshold_size):
            utterance_start_detected = True
        else:
            utterance_start_detected = utterance_start_detected
            
            # print(i, cnt)
            
            # if vad_result_binary:
            #     print("{}: {} consective speaking".format(vad_result_binary, cnt))
            # else:
            #     print("{}: {} consective silence".format(vad_result_binary, cnt))
        
        # if vad_result_binary == 1:
        #     print(i, cnt)
        #     input()

    e_time = time.perf_counter()
        
    # print('global_cnt {:05d}, vad {}({:03d})), IPU {:02d}, utterance_start {:02d}, turn_shift {:02d}, time {:.3f}'.format(
    #     global_cnt, vad_result_binary, cnt, IPU_detected, utterance_start_detected, turn_shift_detected, e_time - s_time
    #     ),
    #     end = ''
    # )
    # print(', speaking - [' + '#' * int(np.sum(vad_history)/len(vad_history)*10) 
    #       + '_' * int((1 - np.sum(vad_history)/len(vad_history))*10) + ']', 
    #       end = ''
    # )
    # print(', silent - [' + '#' * int((1 - np.sum(vad_history)/len(vad_history))*10) 
    #       + '_' * int(np.sum(vad_history)/len(vad_history)*10) + ']'
    # )

    ##################################################
    # Final decision
    ##################################################
        
    if utterance_start_detected:
        
        if (vad_result_binary == 1) and (cnt == utterance_start_threshold_size):
            result = behaviors.reactive
        else:
            result = behaviors.nothing
    
        if turn_shift_detected:
            
            result = behaviors.shift

        elif IPU_detected:
            
            result = behaviors.responsive
    
    elif (not utterance_start_detected):

        result = behaviors.waiting
    
    else:
        
        result = behaviors.nothing
    
    return result, cnt, utterance_start_detected
            
            
        
    
class Behaviors():
    
    def __init__(self):
        
        self.nothing = 'nothing'

        self.reactive = 'reactiveBackchannel'
        self.responsive = 'responsiveBackchannel'
        self.shift = 'turnShift'

        # self.reactive = 'Nod'
        # self.responsive = 'Node_yes'
        # self.shift = 'turnShift'

        self.waiting = 'waiting'
        
                
class VAD(object):
    
    def __init__(self, host, port, rate, buffer_size):

        self.host = host
        self.port = port
        self.rate = rate
        self.buffer_size = buffer_size
        
        self.model = load_silero_vad()
        
        self.client_socket = socket.socket()
        self.client_socket.connect((host, port))
        
        self.vad_result = 0
        
        self.lock = Lock()
        
        self.is_active = False
    
    def start(self):
        
        self.is_active = True
        self.main_thread = Thread(target = self.main_loop)
        self.main_thread.daemon = True
        self.main_thread.start()
        # print('VAD loop started')
    
    def main_loop(self):
        
        message = 'ok'
        
        while self.is_active:

            try:
                
                self.client_socket.send(message.encode())  # send message
                data = self.client_socket.recv(self.buffer_size)  # receive response
                
                np_int16 = np.frombuffer(data, dtype=np.int16)
                np_float32 = int2float(np_int16)

                if self.rate == 16000:
                    np_float32 = np_float32[:512]
                if self.rate == 8000:
                    np_float32 = np_float32[:256]

                vad_result = self.model(torch.from_numpy(np_float32), self.rate).item()
                
                # print('\r' + '#' * int(vad_result*20) + ' ' * (20 - int(vad_result*20)), end='')
                
                # print('in loop', vad_result)
                
                # self.lock.acquire()
                self.vad_result = vad_result
                # self.lock.release()

            except Exception:
                
                self.lock.acquire()
                self.is_active = False
                self.lock.release()
                
                # just in case
                break
            
                # pass

    def get_vad_result(self):
        
        return self.vad_result
    
    def stop(self):
        
        self.is_active = False
        # self.client_socket.send('kill'.encode())
        self.client_socket.close()  # close the connection
        # print()
        # print('Client has been closed')
        

def int2float(sound):
    
    abs_max = np.abs(sound).max()
    sound = sound.astype('float32')
    if abs_max > 0:
        sound *= 1/32768
    sound = sound.squeeze()  # depends on the use case
    
    return sound
    
if __name__ == '__main__':
    main()
