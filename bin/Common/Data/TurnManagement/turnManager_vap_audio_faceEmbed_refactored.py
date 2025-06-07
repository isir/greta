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

# from threading import Thread, Lock

import multiprocessing
from multiprocessing import Process, Manager, Value, Lock
import ctypes

# For VAP
import pickle
import librosa
from finetune_vapNotFreeze import VAPModel, DataConfig, OptConfig, get_run_name
from utils import everything_deterministic, write_json
from model import VapGPT, VapConfig
from events import TurnTakingEvents, EventConfig

# For face image clipper
import dlib
import cv2

from torch.autograd import profiler

import signal

from func_util import Behaviors, wait, int2float, feedback_loop, Timeout_looper, generate_nodding, generate_turnShift
from func_vad import VAD
from func_vap import VAP_interface, vap_runner

os.environ["OPENCV_FFMPEG_READ_ATTEMPTS"] = "8192"
torch.backends.cudnn.benchmark = True

def main():
    
    process = multiprocessing.current_process()
    print('[TurnManagement Greta] PID = {}'.format(process.pid))

    # main_inspect_socket = socket.socket()
    # main_inspect_socket.bind((socket.gethostname(), 6544))
    # main_inspect_socket.listen(1)
    # try:
    #     main_inspecter, _ = main_inspect_socket.accept()
    # except:
    #     pass
    
    print("[TurnMangement Greta] Python start")
    
    "Main function"

    #######################################

    mic_host = socket.gethostname()  # as both code is running on same pc
    # mic_port = 5959  # socket server port number
    mic_port = 9000  # socket server port number    

    feedback_server_host = socket.gethostname()
    feedback_server_port = 5960
    
    greta_host = socket.gethostname()
    greta_port = 5961
    
    RATE = 16000

    audio_buffer_size = 4096
    text_buffer_size = 100
    
    ######################################
    
    # VAP model parameter

    vap_interval = 0.1
    vap_input_length = 20.0

    ######################################

    # VAP turn taking parameter

    # 0: user, 1: agent
    curr_turn = 1
    
    # speech turn onset sensitivity
    # vap_history_length = 20
    user_onset = 10
    agent_onset = 10
    
    #######################################

    # VAD nodding parameter
    
    vad_threshold = 0.8

    # CAUTION: not used yet for this version
    # silence threshold in seconds to determine turn shift
    turn_shift_threshold = 1.0
    vap_history_size = int(turn_shift_threshold/vap_interval)

    # silence threshold in seconds to determine Inter-Pausal Unit (IPU) boundary
    # IPU_threshold = 0.2
    IPU_threshold = 0.5

    # non-silence threshold in seconds to determine user has started to speak
    # utterance_start_threshold = 03
    utterance_start_threshold = 0.5

    vad_history = []
    vad_result_binary = 0

    vap_history = []
    vap_result_binary = 0
    
    system_ready = False
    agent_speaking_state_history = []
    utterance_start_detected = False
    
    feedback_is_alive = False
    
    check_turn_shift = True
    
    #######################################
    
    agent_speaking_state = Value(ctypes.c_bool, False)
    
    # Face image clipper parameter
    
    detector_path = "dlib_models/mmod_human_face_detector.dat"
    sp_path = 'dlib_models/shape_predictor_5_face_landmarks.dat'        
    tgt_size = (112, 112)
    image_fps = 25
    
    # test_data = dummy_data_generator()
    
    global_lock = Lock()

    vad_client = VAD(mic_host, mic_port, RATE, audio_buffer_size)
    vad_client.start()

    # vap_client = VAP(mic_host, mic_port, RATE, vap_input_length, 
    #                  detector_path, sp_path, tgt_size, image_fps)
    # vap_client.start()
    
    vap_user = Value(ctypes.c_float, 0.0)
    vap_agent = Value(ctypes.c_float, 1.0)
    turn = Value(ctypes.c_int, 0)
    mic_update = Value(ctypes.c_bool, False)
    port = Value(ctypes.c_int, 9000)
    is_active = Value(ctypes.c_bool, False)
    is_ready = Value(ctypes.c_bool, False)
    vap_client = VAP_interface(vap_user, vap_agent, turn, mic_update, port, is_active, is_ready)
    
    vap_process = Process(target=vap_runner, 
                          args=(mic_host, mic_port, RATE, vap_input_length, 
                                detector_path, sp_path, tgt_size, image_fps,
                                agent_speaking_state,
                                vap_user, vap_agent, turn, mic_update, port, is_active, is_ready))
    vap_process.daemon = True
    vap_process.start()
    
    #
    ### INSPECTION: ok
    #
    # while True:
    #     vad_result = vad_client.get_vad_result()
    #     vap_user, vap_agent = vap_client.get_VAP()
    #     vap_result_binary = vap_client.get_turn(vap_user, vap_agent)
    
    feedback_socket = socket.socket()
    feedback_socket.connect((feedback_server_host, feedback_server_port))
    # feedback_socket.settimeout(0.05)
    feedback_process = Process(target = feedback_loop, args = (feedback_socket, global_lock, text_buffer_size, agent_speaking_state))
    feedback_process.daemon = True
    feedback_process.start()
    
    greta_socket = socket.socket()
    greta_socket.connect((greta_host, greta_port))
    greta_socket.settimeout(0.05)
        
    while True:

        try:
            message = greta_socket.recv(text_buffer_size)
            print('[TurnManagement Greta] connection established:', message.decode())
            break
        except socket.timeout:
            pass
        except Exception as e:
            print(e)
            sys.exit()
            
    behaviors = Behaviors()
    
    time.sleep(1)
    
    # mainloop_timeout_looper = Timeout_looper(global_lock)
    mainloop_timeout_looper = Timeout_looper()

    # global s_mainloop, timeout_loop_started

    # main_timeout_thread = Thread(target = main_timeout_loop)
    # main_timeout_thread.daemon = True
    # main_timeout_thread.start()
    
    # with global_lock:
    #     s_mainloop = time.time()
    
    cnt = 0
    while True:
        
        # print(1)
        
        try:
            
            # try:
            #     main_inspecter.send("1".encode())
            # except:
            #     pass

            s_time = time.time()
            
            if not vad_client.is_active:
                break

            if not vap_client.is_active:
                break
            
            vad_result = vad_client.get_vad_result()
            if vad_result > vad_threshold:
                vad_result_binary = 1
            else:
                vad_result_binary = 0
                        
            agent_speaking_state_history.append(agent_speaking_state.value)
            vad_history.append(vad_result_binary)
            vap_history.append(vap_result_binary)
            if len(vap_history) > vap_history_size:
                vap_history = vap_history[-vap_history_size:]
                vap_history = vap_history[-vap_history_size:]
                agent_speaking_state_history = agent_speaking_state_history[-vap_history_size:]
            else:
                continue
            
            if not vap_client.is_ready.value:
                continue
            
            if system_ready == False:
                system_ready = True
                greta_socket.send('[TurnManagement Greta] Generator started\r\n'.encode())
                # greta_socket.settimeout(1)

            # try:
            #     main_inspecter.send("2".encode())
            # except:
            #     pass
                
            tmp_out= generate_nodding(
                vad_result_binary, vad_history, agent_speaking_state, agent_speaking_state_history, vap_interval,
                utterance_start_detected, cnt, check_turn_shift, IPU_threshold, utterance_start_threshold)            
            vad_action                  = tmp_out[0]
            # behavior_cnt                = tmp_out[1]
            utterance_start_detected    = tmp_out[1]
            check_turn_shift            = tmp_out[2]

            tmp_out= generate_turnShift(vap_result_binary, vap_history, curr_turn, user_onset, agent_onset)
            vap_action                  = tmp_out[0]
            curr_turn                   = tmp_out[1]
            vap_history                 = tmp_out[2]
            
            if vap_action != behaviors.nothing:
                action = vap_action
            else:
                action = vad_action

            # try:
            #     main_inspecter.send("3".encode())
            # except:
            #     pass
            
            # action = vad_action
            
            # print('[python turnManager action]: {:20s}, {:20s}'.format(vad_action, vap_action))
            
            try:
                greta_socket.send('{}\r\n'.format(action).encode())
            except:
                pass

            # print(3)
            try:
                message = greta_socket.recv(text_buffer_size).decode()
            except:
                message = "[TurnManagement python] greta socket receive failed"
            
            # print(4)
            # print(message)

            if 'kill' in message:
                print("[TurnManagement Greta] kill signal received")
                break
            if "updateMicPort" in message:
                new_port = int(message.split(" ")[1])
                vap_client.updateMicPort(new_port)
            
            cnt += 1
            
            if agent_speaking_state.value:
                print('[TurnManagement AgentSpeakingState] ', agent_speaking_state.value)
            
            #
            # To make sure the VAD interval is constant
            #
            e_time = time.time()
            elapsed = e_time - s_time
            # print('Main loop', elapsed)
            sleep_duration = vap_interval - elapsed - 0.00 #subtract "if branching" processing delay
            if sleep_duration > 0:
                # print('Sleep:', vap_interval - elapsed)
                # time.sleep(sleep_duration)
                wait(sleep_duration)
                # e_time = time.perf_counter()

            e_time2 = time.time()
            # print(e_time)
            # print(e_time2)
            # print('Main loop', e_time2 - s_time)
                
            # print(5)

            # try:
            #     main_inspecter.send("4".encode())
            # except:
            #     pass
        
        
        except KeyboardInterrupt:

            break
        
        except Exception as e:

            traceback.print_exc()

            # try:
            #     main_inspecter.send(traceback.format_exc().encode())
            # except:
            #     pass

            break
        
        # with global_lock:
        #     s_mainloop = time.time()
        #     timeout_loop_started = True
        
        if not mainloop_timeout_looper.is_started():
            mainloop_timeout_looper.start_count()
        if  not mainloop_timeout_looper.is_alive():
            # os._exit(0)
            break
        mainloop_timeout_looper.reset()
    
    # try:
    #     vad_client.stop()
        
    #     main_inspecter.send("vad client killed".encode())
        
    #     vap_client.stop()

    #     main_inspecter.send("vap client killed".encode())

    #     feedback_socket.close()

    #     main_inspecter.send("feedback client killed".encode())

    #     greta_socket.close()

    #     main_inspecter.send("greta client killed".encode())

    # except:
    #     traceback.print_exc()

    # try:
    #     main_inspecter.send("clients killed".encode())
    # except:
    #     pass
    
    try:
        mainloop_timeout_looper.kill()
        vap_process.kill()
        os.kill(multiprocessing.current_process().pid, signal.SIGILL)
        multiprocessing.current_process().kill()
        # cv2.destroyAllWindows()
    except:
        traceback.print_exc()

    print("[TurnManagement Greta] Python end")
    os._exit(0)            

if __name__ == '__main__':

    main()
    
