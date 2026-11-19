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

    
    # Face image cripper parameter
    
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
    vap_client = VAP_interface(vap_user, vap_agent, turn, mic_update, port, is_active)
    
    vap_process = Process(target=vap_runner, 
                          args=(mic_host, mic_port, RATE, vap_input_length, 
                                detector_path, sp_path, tgt_size, image_fps,
                                agent_speaking_state,
                                vap_user, vap_agent, turn, mic_update, port, is_active))
    vap_process.start()
    
    # ### Initial run (model initialization)
    # vap_user, vap_agent = vap_client.get_VAP()
    # vap_result_binary = vap_client.get_turn(vap_user, vap_agent)
    
    #
    ### INSPECTION: ok
    #
    # while True:
    #     vad_result = vad_client.get_vad_result()
    #     vap_user, vap_agent = vap_client.get_VAP()
    #     vap_result_binary = vap_client.get_turn(vap_user, vap_agent)
    
    face_cripper = Face_cripper(detector_path, sp_path, tgt_size)

    feedback_socket = socket.socket()
    feedback_socket.connect((feedback_server_host, feedback_server_port))
    # feedback_socket.settimeout(0.05)
    feedback_thread = Thread(target = feedback_loop, args = (feedback_socket, global_lock, text_buffer_size, agent_speaking_state))
    feedback_thread.daemon = True
    feedback_thread.start()
    
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
            
            vap_user, vap_agent = vap_client.get_VAP()
            vap_result_binary = vap_client.get_turn(vap_user, vap_agent)
            
            agent_speaking_state_history.append(agent_speaking_state.value)
            vad_history.append(vad_result_binary)
            vap_history.append(vap_result_binary)
            if len(vap_history) > vap_history_size:
                vap_history = vap_history[-vap_history_size:]
                vap_history = vap_history[-vap_history_size:]
                agent_speaking_state_history = agent_speaking_state_history[-vap_history_size:]
            else:
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

    
def feedback_loop(feedback_socket, lock, text_buffer_size, agent_speaking_state):
    
    # global agent_speaking_state
    
    print('[TurnManagement feedback] loop started')
    
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
        
                
class VAD(object):
    
    def __init__(self, host, port, rate, buffer_size):

        self.host = host
        self.port = port
        self.rate = rate
        self.buffer_size = buffer_size
        
        self.model = load_silero_vad()
        
        self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        # self.device = torch.device("cpu")

        self.model = self.model.to(self.device)

        while True:
            try:

                self.client_socket = socket.socket()
                self.client_socket.settimeout(1.0)
                self.client_socket.connect((host, port))
                break

            except Exception as e:
                print("[TurnManagement VAD] Trying to connect to mic server ({})".format(e))
                time.sleep(1)
        
        self.vad_result = 0
        
        self.lock = Lock()
        
        self.is_active = False
    
    def start(self):
        
        self.is_active = True
        self.main_thread = Thread(target = self.main_loop)
        self.main_thread.daemon = True
        self.main_thread.start()
        print('[TurnManagement VAD] loop started')
    
    # def main_loop(self, queue, is_active, client_socket, rate, device, model, ):
    def main_loop(self):
        
        message = 'ok'
        
        while self.is_active:

            try:
                
                self.client_socket.send('{}\r\n'.format(message).encode())  # send message
                data = self.client_socket.recv(self.buffer_size)  # receive response
                
                np_int16 = np.frombuffer(data, dtype=np.int16)
                                
                np_float32 = int2float(np_int16)

                if self.rate == 16000:
                    np_float32 = np_float32[:512]
                if self.rate == 8000:
                    np_float32 = np_float32[:256]
                    
                torch_float32 = torch.from_numpy(np_float32)
                torch_float32 = torch_float32.to(self.device)

                vad_result = self.model(torch_float32, self.rate).item()
                
                # print('\r' + '#' * int(vad_result*20) + ' ' * (20 - int(vad_result*20)), end='')
                
                # print('in loop', vad_result)
                
                self.lock.acquire()
                self.vad_result = vad_result
                self.lock.release()
            
            except socket.timeout:
                
                print('[TurnManagement VAD] waiting for microphone connection [timeout]')
                time.sleep(1)

            except ConnectionResetError:

                print('[TurnManagement VAD] waiting for microphone connection [ConnectionReset]')
                time.sleep(1)
                
            except WindowsError as e:
                if e.winerror == 10057:
                    print("[TurnManagement VAD] waiting for microphone connection [NotConnected]")
                    time.sleep(1)

            except Exception:
                
                # self.lock.acquire()
                # self.is_active = False
                # self.lock.release()
                
                traceback.print_exc()
                time.sleep(1)
                
                # just in case
                self.lock.acquire()
                self.is_active = False
                self.lock.release()
                break
            
            wait(0.01)
            
                # pass
        print('[TurnManagement VAD] loop stopped')

    def get_vad_result(self):
        
        return self.vad_result
    
    def stop(self):
        
        self.lock.acquire()
        self.is_active = False
        self.lock.release()
        
        time.sleep(1)
        
        # self.client_socket.send('kill'.encode())
        self.client_socket.close()  # close the connection
        # print()
        # print('Client has been closed')
        
    def updateMicPort(self, new_port):
        
        # self.stop()
        
        while True:
        
            try:

                self.client_socket = socket.socket()
                self.client_socket.connect((self.host, new_port))
                break

            except Exception as e:
                print("[TurnManagement VAD] Trying to connect to mic server ({})".format(e))
                time.sleep(1)
        
        # self.start()
        
        print("[TurnManagement VAD] Mic server updated")

class VAP_interface:
    
    def __init__(self, vap_user, vap_agent, turn, mic_update, port, is_active):
        
        self.vap_user = vap_user
        self.vap_agent = vap_agent
        self.turn = turn
        self.mic_update = mic_update
        self.port = port
        self.is_active = is_active
    
    def get_VAP(self):
        
        return self.vap_user.value, self.vap_agent.value

    def get_turn(self, a, b):

        return self.turn.value
    
    def updateMicPort(self, port):
        
        self.mic_update.value = True
        self.port.value = port
        
        
def vap_runner(mic_host, mic_port, sample_rate, input_length, detector_path, sp_path, tgt_size, image_fps, 
               agent_speaking_state,
               vap_user, vap_agent, turn, mic_update, port, is_active):
    
    vap = VAP(mic_host, mic_port, sample_rate, input_length, detector_path, sp_path, tgt_size, image_fps, agent_speaking_state)
    vap.start()
    
    while True:
        
        vap_user.value, vap_agent.value = vap.get_VAP()
        turn.value = vap.get_turn(vap_user.value, vap_agent.value)
        
        if mic_update.value:
            vap.updateMicPort(port.value)
            mic_update.value = False
        
        is_active.value = vap.is_active
        
        wait(0.01)

    try:
        os.kill(multiprocessing.current_process().pid, signal.SIGILL)
        multiprocessing.current_process().kill()
        # cv2.destroyAllWindows()
    except:
        traceback.print_exc()

    print("[TurnManagement Greta] Python end")
    os._exit(0)

class VAP:
    
    def __init__(self, 
                 mic_host, mic_port, sample_rate, input_length,
                 detector_path, sp_path, tgt_size, image_fps,
                 agent_speaking_state):

        process = multiprocessing.current_process()
        print('[TurnManagement VAP] PID = {}'.format(process.pid))

        os.environ["OPENCV_FFMPEG_READ_ATTEMPTS"] = "8192"
        torch.backends.cudnn.benchmark = True
        
        # configs = get_args()
        
        # config_path = "configs_vap_audio.pkl"
        config_path = "configs_vap_audio_faceEmbed.pkl"
        
        # agent_audio_path = "output.wav"
        agent_audio_path = "../../../output.wav" #/bin/output.wav from /bin/Common/Data/TurnManagement
        # agent_audio_path = "../../../../output.wav" #/bin/output.wav from /bin/Common/Data/TurnManagement/test_rvap
        
        agent_image_path = "../../../Capture_0.png"
            
        # with open(config_path, "wb") as f:
        #     pickle.dump(configs, f)
            
        with open(config_path, "rb") as f:
            configs = pickle.load(f)
    
        # args = configs["args"]    
        # checkpoint = args.checkpoint
    
        pp.pprint(configs)
        
        self.lock = Lock()
        self.is_active = False

        # self.dtype = torch.float32
        self.dtype = torch.float16
        # self.dtype = torch.bfloat16
        # self.dtype = torch.qint8

        with torch.no_grad():            
            with torch.amp.autocast('cuda'):

                self.model = VAPModel.load_from_checkpoint(configs["cfg_dict"]["checkpoint"], strict=False, conf=configs["model"])
                self.model.eval()
                self.model.cuda()
                self.model.to(self.dtype)
                # self.model = torch.compile(self.model)
                print("model on cuda:", next(self.model.parameters()).is_cuda)
    
        self.model.event_conf = configs["event"]
        self.model.event_extractor = TurnTakingEvents(self.model.event_conf)
        
        self.userAudio = UserAudio(mic_host, mic_port)
        self.userAudio.start()
        
        self.agentAudio = AgentAudio(agent_speaking_state, agent_audio_path, sample_rate, input_length)
        
        self.face_cripper = Face_cripper(detector_path, sp_path, tgt_size)
        
        self.userFaceImage = UserFaceImage(self.face_cripper)
        self.agentFaceImage = AgentFaceImage(self.face_cripper, agent_image_path)
        
        self.audio_user = np.zeros((int(sample_rate * input_length)), dtype=float)
        self.audio_agent = np.zeros((int(sample_rate * input_length)), dtype=float)
        
        self.face_image_user = np.zeros((int(image_fps * input_length), 3, tgt_size[0], tgt_size[1]))
        self.face_image_agent = np.zeros((int(image_fps * input_length), 3, tgt_size[0], tgt_size[1]))
        
        self.prev_agent_update = time.time()
        
        self.vap_user = 0
        self.vap_agent = 0
        self.amp_max_user = 1e-10
        self.amp_max_agent = 1e-10
                
        self.turn_history_length = 3
        self.turn_history = [1 for _ in range(self.turn_history_length)]
        
        self.dummy_tensor = torch.zeros([0]).cuda().to(dtype=self.dtype)
        
        self.cnt = 0
    
    def main_loop(self):
        
        # i = 0

        while self.is_active:
            
            try:

                s_time = time.perf_counter()
                s_time_all = s_time
                
                # (B, C, Samples)
                # data = torch.randn(1, 2, int(16000 * 20.0)).cuda()
                
                # print(1)
                
                # self.lock.acquire()

                self.audio_user = self.userAudio.get(self.audio_user)
                self.audio_agent = self.agentAudio.get(self.audio_agent)
                
                # self.face_image_user = self.userFaceImage.get(self.face_image_user, visualize = False)
                # self.face_image_agent = self.agentFaceImage.get(self.face_image_agent, visualize = False)
                self.face_image_user = self.userFaceImage.get(self.face_image_user, visualize = True)
                self.face_image_agent = self.agentFaceImage.get(self.face_image_agent, visualize = True)

                # self.lock.release()
                
                # print(3)

                audio_user_amax = np.amax(self.audio_user)
                audio_agent_amax = np.amax(self.audio_agent)
                # print(audio_user_amax, audio_agent_amax)
                
                # Audio peak normalization
                # self.audio_user = (self.audio_user - self.audio_user.mean()) / (self.audio_user.std() + 1e-10)
                # self.audio_agent = (self.audio_agent - self.audio_agent.mean()) / (self.audio_agent.std() + 1e-10)
                if audio_user_amax != 0:
                    normalized_audio_user = self.audio_user / (np.amax(self.audio_user))
                else:
                    normalized_aduio_user = self.audio_user
                    
                if audio_agent_amax != 0:
                    normalized_audio_agent = self.audio_agent / np.amax(self.audio_agent)
                else:
                    normalized_audio_agent = self.audio_agent
                
                # normalized_audio_user = self.audio_user
                # normalized_audio_agent = self.audio_agent
                
                # print("amax {:5.2f} {:5.2f}".format(np.amax(self.audio_user), np.amax(self.audio_agent)))
                # print("std  {:5.2f} {:5.2f}".format(np.abs(self.audio_user).std(), np.abs(self.audio_agent).std()))
            
                # Concat audio from mic and agent (replace with real code)
                audio_tensor = torch.concat(
                    (torch.from_numpy(normalized_audio_user).unsqueeze(0), 
                     torch.from_numpy(normalized_audio_agent).unsqueeze(0)), dim=0).unsqueeze(0)
                # audio_tensor = audio_tensor.float().cuda()
                audio_tensor = audio_tensor.cuda().to(dtype=self.dtype)
                
                face_image_user_tensor = torch.from_numpy(self.face_image_user).unsqueeze(0).cuda()
                face_image_user_tensor = face_image_user_tensor.to(dtype=self.dtype)

                face_image_agent_tensor = torch.from_numpy(self.face_image_agent).unsqueeze(0).cuda()
                face_image_agent_tensor = face_image_agent_tensor.to(dtype=self.dtype)
                
                e_time = time.perf_counter()
                # print('[VAP data prep] {:.2f}'.format(e_time - s_time))
                
                s_time = time.perf_counter()
                
                # print(audio_tensor.type())
                # print(face_image_user_tensor.type())
                # print(face_image_agent_tensor.type())
                # print(self.dummy_tensor.type())
                
                # print('before')

                # audio/faceEmbed VAP
                src = {
                    "waveform":audio_tensor,
                    "gaze1":self.dummy_tensor,
                    "head1":self.dummy_tensor,
                    "face1":self.dummy_tensor,
                    "body1":self.dummy_tensor,
                    "face_im1":face_image_user_tensor,
                    "gaze2":self.dummy_tensor,
                    "head2":self.dummy_tensor,
                    "face2":self.dummy_tensor,
                    "body2":self.dummy_tensor,
                    "face_im2":face_image_agent_tensor,
                }
                
                # with profiler.profile(record_shapes=True, use_cuda=True) as prof:
                #     with profiler.record_function("model_inference"):

                #         with torch.no_grad():            
                #             # with torch.cuda.amp.autocast():
                
                #                 result = self.model(src = src)
 
                # print(prof.key_averages(group_by_input_shape=True).table(sort_by="cuda_time_total"))
                # print(prof.key_averages(group_by_input_shape=True).table())
                # input('sleep: enter to proceed: ')


                result = self.model(src = src)
                # result["logits"] = result["logits"].to(torch.float32)
                
                # audio VAP
                # result = self.model(waveform = audio_tensor)
                
                # print('1')
                
                # event = self.model.event_extractor(result["vad"])

                # print('2')
                
                probs = self.model.objective.get_probs(result["logits"])

                e_time = time.perf_counter()
                # print('[VAP calc] {:.2f}'.format(e_time - s_time))

                s_time = time.perf_counter()

                # print('after')
                
                # pp.pprint(event)
                
                # pp.pprint(probs)
                
                # print(result["vad"].shape)
                # print(result["logits"].shape)
                # print(probs["p_now"].shape)
                # print(probs["p_future"].shape)
                # print(probs["p_tot"].shape)
                # input()
                                
                # monitor input audio
                
                
                # vap = probs["p_tot"][0, -1]
                vap = probs["p_future"][0, -1]
                
                # vap = (vap - vap.mean()) / vap.std()
                # vap = (vap - 0.4) * 7
                
                # vap_user = vap[0]
                # vap_agent = vap[1]
                
                # self.lock.acquire()
                
                #VAD
                # self.vap_user = result["vad"].cpu().detach().numpy()[0, -1, 1]
                # self.vap_agent = result["vad"].cpu().detach().numpy()[0, -1, 0]
                
                #VAP
                self.vap_user = vap[1]
                self.vap_agent = vap[0]
                                            
                # self.lock.release()

                # self.print_vap_audio(self.cnt, self.vap_user, self.vap_agent, self.audio_user, self.audio_agent)
                
                # print("{:5.2f}, {:5.2f}".format(tmp_amp_user, tmp_amp_agent))
                
                # print(np.shape(data[0, 0]))
                # print(np.shape(data[0, 1]))
                # input()
                
                # print(result["logits"].shape)
                # print(result["vad"].shape)
                
                # print('[TurnManagement VAP] main loop {:05d}'.format(self.cnt))

                e_time = time.perf_counter()
                # print('[VAP post-process] {:.2f}'.format(e_time - s_time))
                
                print('[VAP all] {:.2f}'.format(e_time - s_time_all))

                self.cnt += 1
            
            except socket.timeout:
                
                print('[TurnManagement VAP] waiting for microphone connection [timeout]')
                time.sleep(1)

            except ConnectionResetError:

                print('[TurnManagement VAP] waiting for microphone connection [ConnectionReset]')
                time.sleep(1)
                
            except WindowsError as e:
                if e.winerror == 10057:
                    print("[TurnManagement VAP] waiting for microphone connection [NotConnected]")
                    time.sleep(1)

            except Exception:
                
                # self.lock.acquire()
                # self.is_active = False
                # self.lock.release()
                
                traceback.print_exc()
                time.sleep(1)
                
                # just in case
                self.lock.acquire()
                self.is_active = False
                self.lock.release()
                break
            
            wait(0.01)

        print('[TurnManagement VAP] loop stopped')
    
    def get_VAP(self):
        
        return self.vap_user, self.vap_agent
    
    def get_audio(self):
        
        return self.audio_user, self.audio_agent
    
    def get_turn(self, vap_user, vap_agent):
        
        # turn: 0 - user, 1 - agent
        
        if vap_user > 0.53:
            
            tmp_turn = 0
        
        else:
            
            tmp_turn = 1
        
        self.turn_history.append(tmp_turn)
        
        # print(len(self.turn_history))
        # print(self.turn_history_length)
        
        self.turn_history = self.turn_history[-self.turn_history_length:]
        
        # if np.all(self.turn_history) == 0:
        #     turn_decision = 0
        # elif np.all(self.turn_history) == 1:
        #     turn_decision = 1
        # else:
        #     assert False, "something wrong happened"
        
        turn_decision = round(np.average(self.turn_history))
        
        return turn_decision
    
    def print_vap_audio(self, i, vap_user, vap_agent, audio_user, audio_agent, turn = None):
        
        # self.lock.acquire()

        tmp_amp_user = abs(audio_user[-1])
        tmp_amp_agent = abs(audio_agent[-1])
        if tmp_amp_user > self.amp_max_user:
            self.amp_max_user = tmp_amp_user
        if tmp_amp_agent > self.amp_max_agent:
            self.amp_max_agent = tmp_amp_agent

        # self.lock.release()

        print("\r {:04d} Usr - {:5.2f} ".format(i, vap_user) + "#" * int(10 * tmp_amp_user / self.amp_max_user) + " " * (10 - int(10 * tmp_amp_user / self.amp_max_user)), end='')
        print(" | Sys - {:5.2f} ".format(vap_agent) + "#" * int(10 * tmp_amp_agent / self.amp_max_agent) + " " * (10 - int(10 * tmp_amp_agent / self.amp_max_agent)), end='')
        
        if turn != None:
            
            if round(turn) == 0:
                print(' - turn: user')
            elif round(turn) == 1:
                print(' - turn: agent')
            
            # print("{:5.2f}".format(turn))
        
        else:
            
            print()
        

    def start(self):
        
        self.is_active = True
        self.main_thread = Thread(target = self.main_loop)
        self.main_thread.daemon = True
        self.main_thread.start()
        print('[TurnManagement VAP] loop started')
        
    def stop(self):

        self.lock.acquire()
        self.is_active = False
        self.lock.release()
        
        time.sleep(1)
        
        # self.client_socket.send('kill'.encode())
        # self.mic.stop()  # close the connection
        # print()
        # print('Client has been closed')

    def updateMicPort(self, new_port):
        
        self.userAudio.updatePort(new_port)
                
class AgentAudio:
    
    def __init__(self, agent_speaking_state, agent_audio_path = "output.wav", rate = 16000, input_length = 20.0):
        
        self.audio_path = agent_audio_path

        self.rate = rate
        self.input_length = input_length
        
        self.agent_speech = None
        self.prev_agent_update = time.time()
        
        self.agent_speaking_state = agent_speaking_state
    
    def get(self, prev_chunk):
        
        if self.agent_speaking_state.value:
            
            if self.agent_speech == None:
                print("[TurnManagement Agent] updated agent speech wav")
                self.agent_speech = AgentSpeech(self.audio_path, self.rate, self.input_length)

            curr_chunk, OVER = self.agent_speech.get(prev_chunk)

            if OVER:
                self.agent_speech = None
            
        else:
            
            over_frames = int(self.rate * (time.time() - self.prev_agent_update))
            curr_chunk = np.concatenate((prev_chunk, np.zeros(over_frames)), axis = 0)[-int(self.rate * self.input_length):]
            
            self.agent_speech = None

        self.prev_agent_update = time.time()
        
        return curr_chunk
            
class AgentSpeech:
    
    def __init__(self, audio_path = "output.wav", rate=16000, input_length=20.0):
        
        self.rate = rate
        self.input_length = input_length

        self.audio, sr = librosa.load(audio_path, sr=self.rate, mono=True)
        self.s_time = time.time()
        self.duration = librosa.get_duration(y=self.audio, sr=sr)
        
        self.curr_index = 0
                
        self.OVER = False
    
    def get(self, prev_chunk):
        
        curr_sec = time.time() - self.s_time

        print('duration: {:.2f}, curr_sec: {:.2f}'.format(self.duration, curr_sec))
                
        if self.duration < curr_sec:

            over_frames = int(self.rate * (curr_sec - self.duration))
            
            # To deal with the case (self.duration < self.input_length), add self.chunk at the front
            prev_chunk = np.concatenate((prev_chunk, self.audio, np.zeros(over_frames, dtype=float)))
            
            curr_chunk = prev_chunk[-int(self.rate * self.input_length):]
            
            self.OVER = True
                
        else:
            prev_chunk = np.concatenate((prev_chunk, self.audio[self.curr_index:int(len(self.audio) * curr_sec / self.duration)]))
            curr_chunk = prev_chunk[-int(self.rate * self.input_length):]
            self.curr_index = int(len(self.audio) * curr_sec / self.duration)
        
        return curr_chunk, self.OVER

class UserAudio:
    
    def __init__(self, host, port):
        
        self.rate = 16000
        self.input_length = 20.0
        self.buffer_size = 4096
        
        self.host = host
        self.port = port
        
    def start(self):
        
        while True:
            
            try:

                self.client_socket = socket.socket()
                self.client_socket.settimeout(1.0)
                self.client_socket.connect((self.host, self.port))
                print("[TurnManagement Mic] Connected to mic server ({}, {})".format(self.host, self.port))                
                break

            except Exception as e:
                print("[TurnManagement Mic] Trying to connect to mic server ({})".format(e))
                time.sleep(1)
    
    def stop(self):
        
        self.client_socket.close()
        print("[TurnManagement Mic] Closed mic server")
                
    def get(self, prev_chunk):
        
        # print(4)
        
        self.client_socket.send('{}\r\n'.format("ok").encode())  # send message

        # print(5)

        data = self.client_socket.recv(self.buffer_size)  # receive response

        np_int16 = np.frombuffer(data, dtype=np.int16)
                        
        np_float32 = int2float(np_int16)

        curr_chunk = np.concatenate((prev_chunk, np_float32))[-int(self.rate * self.input_length):]

        # print(6)
        
        return curr_chunk
    
    def updatePort(self, new_port):
        
        while True:
        
            try:

                self.client_socket = socket.socket()
                self.client_socket.connect((self.host, new_port))
                break

            except Exception as e:
                print("[TurnManagement Mic] Trying to connect to mic server ({})".format(e))
                time.sleep(1)
        
        # self.start()
        
        print("[TurnManagement Mic] Mic server updated")        

class AgentFaceImage:

    def __init__(self, face_cripper, agent_image_path):
        
        self.agent_image_path = agent_image_path
        self.face_cripper = face_cripper
        self.cnt = 0
        
        self.zero_frame = np.zeros((500, 500, 3), dtype=np.int8)
    
    def get(self, frame_sequence, visualize = True):
        
        # cnt = 0
        # while True:
        #     try:
        #         frame = cv2.imread(self.agent_image_path)
        #         if not (frame is None):
        #             break
        #         else:
        #             print("AgentFaceImage read error:", cnt)
        #             cnt += 1
        #     except Exception as e:
        #         print("AgentFaceImage unexpected error:", e)
                
        #     time.sleep(0.1)
        
        try:
            frame = cv2.imread(self.agent_image_path)
            if frame is None:
                frame = self.zero_frame.copy()
        except:
            frame = self.zero_frame.copy()
            
        # print(type(frame))
        # print(frame.shape)
        face_frame = self.face_cripper.crip(frame)
        
        if visualize:
            face_frame_viz = face_frame.copy()
            shape = np.shape(face_frame_viz)
            fontFace = cv2.FONT_HERSHEY_DUPLEX
            scale = cv2.getFontScaleFromHeight(fontFace, 10)
            cv2.putText(face_frame_viz,"{:05d}".format(self.cnt), (0,int(shape[1]/5)), cv2.FONT_HERSHEY_DUPLEX, scale, (255,255,255))
            cv2.imshow('AgentFace', face_frame_viz)
            
            ### BE CAREFUL: VERY SLOW (about 15 ms precision)
            if cv2.waitKey(1) & 0xFF == ord('q'):
                pass

        # channel last to channel first
        face_frame = np.moveaxis(face_frame, -1, 0)
        
        face_frame = np.expand_dims(face_frame, axis=0)
        frame_sequence = np.concatenate((frame_sequence, face_frame), axis=0)[1:].astype(np.uint8)
        
        self.cnt += 1
        
        # print(frame_sequence.shape)
        
        return frame_sequence
    
class UserFaceImage:
    
    def __init__(self, face_cripper):
        
        self.camera = Camera()
        self.face_cripper = face_cripper
        self.cnt = 0
        
    def get(self, frame_sequence, visualize = True):
        
        frame = self.camera.get()
        face_frame = self.face_cripper.crip(frame)
        
        if visualize:
            face_frame_viz = face_frame.copy()
            shape = np.shape(face_frame_viz)
            fontFace = cv2.FONT_HERSHEY_DUPLEX
            scale = cv2.getFontScaleFromHeight(fontFace, 10)
            cv2.putText(face_frame_viz,"{:05d}".format(self.cnt), (0,int(shape[1]/5)), cv2.FONT_HERSHEY_DUPLEX, scale, (255,255,255))
            cv2.imshow('UserFace', face_frame_viz)

            ### BE CAREFUL: VERY SLOW (about 15 ms precision)
            if cv2.waitKey(1) & 0xFF == ord('q'):
                pass
        
        # channel last to channel first
        face_frame = np.moveaxis(face_frame, -1, 0)
        # print(face_frame.shape)
        
        face_frame = np.expand_dims(face_frame, axis=0)
        frame_sequence = np.concatenate((frame_sequence, face_frame), axis=0)[1:].astype(np.uint8)
        
        self.cnt += 1
        
        # print(frame_sequence.shape)

        return frame_sequence

class Face_cripper:
    
    def __init__(self, detector_path, sp_path, tgt_size):

        self.detector = dlib.cnn_face_detector = dlib.cnn_face_detection_model_v1(detector_path)
        self.sp = dlib.shape_predictor(sp_path)
        self.tgt_size = tgt_size
    
    def crip(self, frame):
        
        try:
            # dets = detector(frame, 1)
            dets = self.detector(frame, 0)
            
            # print(np.shape(dets))
            
            num_faces = len(dets)
            if num_faces != 0:
    
                faces = dlib.full_object_detections()
                for detection in dets:
                    
                    # rects = dlib.rectangles()
                    # rects.extend([d.rect for d in dets])                
                    # detection = rects
                    
                    detection = detection.rect
                    
                    # print(np.shape(detection))
                    # print(detection)
                    # print(detection.rect.left(), detection.rect.top(), detection.rect.right(), detection.rect.bottom(), detection.confidence)
                    
                    # print(type(frame))
                    # print(type(detection))
                    
                    shape = self.sp(frame, detection)
                    faces.append(shape)
                
                face_frame = dlib.get_face_chips(frame, faces, size=self.tgt_size[0])[0]
            else:
                face_frame = np.zeros((self.tgt_size[0], self.tgt_size[1], 3))
        except Exception as e:
            print("[TurnManagement Face_clipper]", e)
            face_frame = np.zeros((self.tgt_size[0], self.tgt_size[1], 3))
        
        return face_frame

class Camera:
    
    def __init__(self, camera_index = 0):

        try:
            self.cap = cv2.VideoCapture(camera_index)
            ret, frame = self.cap.read()
            if not ret:
                print('[TurnManagement Camera]: Failed to read camera image (index: {}). Retry with index {}'.format(camera_index, camera_index+1))
                self.cap = cv2.VideoCapture(camera_index+1)
                ret, frame = self.cap.read()
                if not ret:
                    assert False, '[TurnManagement Camera] Error: could not load neither camera {} or camera {}'.format(camera_index, camera_index+1)
                else:
                    self.camera_index = camera_index + 1
            else:
                self.camera_index = camera_index
        except:
            traceback.print_exc()
            
        self.zero_frame = np.zeros((500, 500, 3), dtype=np.int8)
    
    def get(self):
        
        ret, frame = self.cap.read()
        
        if not ret:
            frame = self.zero_frame.copy()
        
        return frame
    
    def stop(self):
        
        self.cap.release()

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


# def main_timeout_loop():
    
#     limit = 5
    
#     while True:
        
#         # e_mainloop = time.time()
#         e_mainloop = time.perf_counter()
#         print("[TurnManagement] mainloop timeout elapsed", e_mainloop - s_mainloop)
#         log("[TurnManagement] mainloop timeout elapsed " + str(e_mainloop - s_mainloop))
#         if timeout_loop_started and (e_mainloop - s_mainloop > limit):
#             print('[TurnManagement] mainloop timeout')
#             # sys.exit()
#             # os._exit(0)
#             time.sleep(2)
#             os._exit(0)
        
#         time.sleep(0.5)

# class Timeout_looper:
    
#     def __init__(self, lock):
        
#         self.looper = Thread(target = main_timeout_loop)
#         self.looper.daemon = True
#         self.looper.start()
        
#         self.lock = lock
    
#     def start_count(self):

#         global timeout_loop_started
        
#         with self.lock:
            
#             timeout_loop_started = True
    
#     def reset(self):
        
#         global s_mainloop
#         # s_mainloop = time.time()
#         with self.lock:
#             s_mainloop = time.perf_counter()
    
#     def is_started(self):
        
#         return timeout_loop_started


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
            

if __name__ == '__main__':

    main()
    
