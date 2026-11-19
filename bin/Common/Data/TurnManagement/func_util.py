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

from threading import Thread, Lock

import multiprocessing
from multiprocessing import Process, Manager, Value
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


# For mainloop timeout
# s_mainloop = time.time()
# s_mainloop = time.perf_counter()
# timeout_loop_started = False

os.environ["OPENCV_FFMPEG_READ_ATTEMPTS"] = "8192"
torch.backends.cudnn.benchmark = True
    
def feedback_loop(feedback_socket, lock, text_buffer_size, agent_speaking_state):
    
    # global agent_speaking_state
    
    print('[TurnManagement feedback] loop started')
    
    try:
        while True:
            
            try:
                
                data = feedback_socket.recv(text_buffer_size).decode()
                
                if data == 'end':
                    state = 0
                elif data == 'start':
                    state = 1
    
                lock.acquire()
                agent_speaking_state.value = state
                lock.release()
    
                data = 'ok'
                feedback_socket.send('{}\r\n'.format(data).encode())
            
            except socket.timeout:
                
                continue
            
            except ConnectionResetError:
    
                print('[TurnManagement feedback] ConnectionReset')
                break
                
            except WindowsError as e:
    
                if e.winerror == 10057:
                    print("[TurnManagement feedback] NotConnected")
                    break
                    
            except Exception:
    
                traceback.print_exc()
                break
            
            wait(0.01)
    finally:
    
        feedback_socket.close()    
        print('[TurnManagement feedback] loop ended')


def generate_turnShift(vap_result_binary, vap_history, curr_turn, user_onset, agent_onset):
    
    action = Behaviors().nothing
    
    turn = round(vap_result_binary)
    vap_history.append(turn)
    if curr_turn == 1:
        if np.all(vap_history[-user_onset:]) == 0:
            print('turn shift: agent > user')
            # action = Behaviors().shift
            action = Behaviors().shift_a2u
            curr_turn = 0
    if curr_turn == 0:
        if np.all(vap_history[-agent_onset:]) == 1:
            print('turn shift: user > agent')
            # action = Behaviors().shift
            action = Behaviors().shift_u2a
            curr_turn = 1

    return action, curr_turn, vap_history

def generate_nodding(vad_result_binary, vad_history, agent_speaking_state, agent_speaking_state_history, vad_interval, 
                      utterance_start_detected, global_cnt, check_turn_shift, IPU_threshold, utterance_start_threshold):
    
    """
    Rule 1:
        Return do nothing signal if agent is speaking
    Rule 2:
        Return turn shift signal if all vad_history is 0
    Rule 3:
        Return backchannel signal if IPU (silence more than 0.2 seconds) detected
    """
    
    s_time = time.perf_counter()
    
    IPU_threshold_size = int(IPU_threshold/vad_interval)
    
    utterance_start_threshold_size = int(utterance_start_threshold/vad_interval)
    
    behaviors = Behaviors()
    IPU_detected = False
    turn_shift_detected = False
        
    if agent_speaking_state.value:
        
        check_turn_shift = True
        
        return behaviors.nothing, utterance_start_detected, check_turn_shift
    
    else:
        
        ##################################################
        # Turn shift (user to agent) detection
        ##################################################

        if check_turn_shift and (utterance_start_detected) and not (1 in vad_history):
            turn_shift_detected = True
        
        ##################################################
        # IPU, utterance start detection
        ##################################################

        cnt = 0
        for i in range(1, len(vad_history)+1):
            i = -i
            
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
            
            check_turn_shift = False

        elif IPU_detected:
            
            result = behaviors.responsive
    
    elif (not utterance_start_detected):

        result = behaviors.waiting
    
    else:
        
        result = behaviors.nothing
    
    # return result, cnt, utterance_start_detected, check_turn_shift
    return result, utterance_start_detected, check_turn_shift

class Behaviors():
    
    def __init__(self):
        
        self.nothing = 'nothing'

        self.reactive = 'reactiveBackchannel'
        self.responsive = 'responsiveBackchannel'

        self.shift = 'turnShift'
        self.shift_u2a = 'turnShiftUserToAgent'
        self.shift_a2u = 'turnShiftAgentToUser'

        self.waiting = 'waiting'

def timer_loop(limit, port):
    
    hostname = socket.gethostname()
    client = socket.socket()
    client.connect((hostname, port))
    
    _ = client.recv(1024)
    client.settimeout(0.01)
    

    s_time = time.perf_counter()
    
    # print("Waiting connection to socket inspecter...", end = '', flush=True)
    inspect_socket = socket.socket()
    inspect_socket.bind((socket.gethostname(), 6543))
    inspect_socket.listen(1)
    inspect_socket.settimeout(0.5)
    try:
        inspecter, _ = inspect_socket.accept()
        inspecter.settimeout(0.01)
    except:
        pass
    # print('done')
        
    while True:
        
        e_time = time.perf_counter()
        elapsed = e_time - s_time
        # print(elapsed)
        
        try:
            inspecter.send("elapsed {:.2f}".format(elapsed).encode())
        except:
            wait(0.01)
        
        try:
            s_time = float(client.recv(1024).decode())
            inspecter.send("s_time {:.2f}".format(s_time).encode())
        except:
            wait(0.01)
        
        if elapsed > limit:
            print('[TurnManagement] mainloop timeout', elapsed)            
            break
        
        # time.sleep(0.01)

    time.sleep(2)
    os._exit(0)

class Timeout_looper:
    
    def __init__(self, limit = 5, port = 9999):
        
        self.looper = Process(target=timer_loop, args=(limit, port))
        self.looper.start()
        
        self.server = socket.socket()
        self.hostname = socket.gethostname()
        self.server.bind((self.hostname, port))
        self.server.listen(1)
        
        self.conn, _ = self.server.accept()
        
        self.started = False
        
    def reset(self):
        
        assert self.started, "Timer.start() should be called before Timer.reset()"
        
        s_time = time.perf_counter()
        s_time = str(s_time).encode()
        self.conn.send(s_time)
        
    def start_count(self):
        
        self.started = True
        # self.conn.send('start'.encode())
    
    def is_started(self):
        
        return self.started
    
    def is_alive(self):
        
        return self.looper.is_alive()
    
    def kill(self):
        
        # os.kill(int(self.looper.pid), signal.SIGILL)
        self.looper.kill()
    
def log(text):

    with open("log.txt", "a") as f:
        f.write(text + "\n")

def wait(duration):

    s_time = time.perf_counter()
    while (time.perf_counter() - s_time) < duration:
        pass

def dummy_data_generator():

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

def get_dummy_agent_speaking_state(test_data = None):
    
    speaking_state = 0

    if test_data != None:
        
        speaking_state = next(test_data)
        
    else:
        
        assert True, "[TurnManagement] get_agent_speaking_state is not implemented yet"
    
    return speaking_state

def int2float(sound):
    
    abs_max = np.abs(sound).max()
    sound = sound.astype('float32')
    if abs_max > 0:
        sound *= 1/32768
    sound = sound.squeeze()  # depends on the use case
    
    return sound

def print_size_of_model(model, label=""):
    torch.save(model.state_dict(), "temp.p")
    size=os.path.getsize("temp.p")
    print("model: ",label,' \t','Size (KB):', size/1e3)
    os.remove('temp.p')
    return size