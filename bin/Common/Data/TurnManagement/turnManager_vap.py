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
from silero_vad import load_silero_vad, read_audio, get_speech_timestamps

from threading import Thread, Lock

import multiprocessing
process = multiprocessing.current_process()
print('[TurnManagement Greta] PID = {}'.format(process.pid))

import pickle
import librosa
from VAP_main import VAPModel, DataConfig, OptConfig, get_run_name
from utils import everything_deterministic, write_json
from model import VapGPT, VapConfig
from events import TurnTakingEvents, EventConfig


agent_speaking_state = False

def main():
    
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
    user_onset = 1
    agent_onset = 10
    
    #######################################

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
    
    # test_data = dummy_data_generator()
    
    global_lock = Lock()
    
    feedback_socket = socket.socket()
    feedback_socket.connect((feedback_server_host, feedback_server_port))
    # feedback_socket.settimeout(0.05)
    feedback_thread = Thread(target = feedback_loop, args = (feedback_socket, global_lock, text_buffer_size))
    feedback_thread.daemon = True
    feedback_thread.start()
    
    greta_socket = socket.socket()
    greta_socket.connect((greta_host, greta_port))
    
    behaviors = Behaviors()
    
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

    vad_client = VAD(mic_host, mic_port, RATE, audio_buffer_size)
    vad_client.start()

    vap_client = VAP(mic_host, mic_port, RATE, vap_input_length)
    vap_client.start()
    
    time.sleep(1)
    
    cnt = 0
    while True:
        
        # print(1)
        
        try:

            s_time = time.perf_counter()
            
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
            
            agent_speaking_state_history.append(agent_speaking_state)
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
            
            greta_socket.send('{}\r\n'.format(action).encode())

                
            # print(3)

            message = greta_socket.recv(text_buffer_size).decode()
            
            # print(4)
            # print(message)

            if 'kill' in message:
                print("[TurnManagement Greta] kill signal received")
                break
            if "updateMicPort" in message:
                new_port = int(message.split(" ")[1])
                vap_client.updateMicPort(new_port)
            
            cnt += 1
            
            if agent_speaking_state:
                print('[TurnManagement AgentSpeakingState] ', agent_speaking_state)
            
            #
            # To make sure the VAD interval is constant
            #
            e_time = time.perf_counter()
            while (e_time - s_time) < vap_interval:
                # print('Sleep:', vad_interval - (e_time - s_time))                
                time.sleep(vap_interval - (e_time - s_time))
                e_time = time.perf_counter()
                
            # print(5)
        
        
        except KeyboardInterrupt:

            break
        
        except Exception as e:

            traceback.print_exc()
            break
    
    try:
        vad_client.stop()
        vap_client.stop()
        feedback_socket.close()
        greta_socket.close()
    except:
        traceback.print_exc()
    
    print("[TurnManagement Greta] Python end")

    
def feedback_loop(feedback_socket, lock, text_buffer_size):
    
    global agent_speaking_state
    
    print('[TurnManagement feedback] loop started')
    
    while True:
        
        try:
            
            data = feedback_socket.recv(text_buffer_size).decode()
            
            lock.acquire()
            if data == 'end':
                agent_speaking_state = 0
            elif data == 'start':
                agent_speaking_state = 1
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
        
    if agent_speaking_state:
        
        check_turn_shift = True
        
        return behaviors.nothing, utterance_start_detected, check_turn_shift
    
    if not agent_speaking_state:
        
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

class VAP:
    
    def __init__(self, mic_host, mic_port, sample_rate, input_length):
    
        # configs = get_args()
    
        config_path = "configs_vap_audio.pkl"
        
        # agent_audio_path = "output.wav"
        agent_audio_path = "../../../output.wav" #/bin/output.wav from /bin/Common/Data/TurnManagement
        # agent_audio_path = "../../../../output.wav" #/bin/output.wav from /bin/Common/Data/TurnManagement/test_rvap
            
        # with open(config_path, "wb") as f:
        #     pickle.dump(configs, f)
            
        with open(config_path, "rb") as f:
            configs = pickle.load(f)
    
        # args = configs["args"]    
        # checkpoint = args.checkpoint
    
        pp.pprint(configs)
        
        self.lock = Lock()
        self.is_active = False
                
        with torch.no_grad():

            self.model = VAPModel.load_from_checkpoint(configs["cfg_dict"]["checkpoint"], strict=False, conf=configs["model"])
            self.model.eval()
        
            self.model.event_conf = configs["event"]
            self.model.event_extractor = TurnTakingEvents(self.model.event_conf)
        
        self.mic = Mic(mic_host, mic_port)
        self.mic.start()
        
        self.agent = Agent(agent_audio_path, sample_rate, input_length)
        
        
        self.audio_user = np.zeros((int(sample_rate * input_length)), dtype=float)
        self.audio_agent = np.zeros((int(sample_rate * input_length)), dtype=float)
        
        self.prev_agent_update = time.time()
        
        self.vap_user = 0
        self.vap_agent = 0
        self.amp_max_user = 1e-10
        self.amp_max_agent = 1e-10
        
        self.turn_history_length = 3
        self.turn_history = [1 for _ in range(self.turn_history_length)]
    
    def main_loop(self):

        while self.is_active:
            
            try:
        
                # (B, C, Samples)
                # data = torch.randn(1, 2, int(16000 * 20.0)).cuda()
                
                # print(1)
                
                self.lock.acquire()
                self.audio_user = self.mic.get(self.audio_user)
                self.audio_agent = self.agent.get(self.audio_agent)
                self.lock.release()
                
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
                data = torch.concat((torch.from_numpy(normalized_audio_user).unsqueeze(0), 
                                     torch.from_numpy(normalized_audio_agent).unsqueeze(0)), dim=0).unsqueeze(0)
            
                data = data.float().cuda()
                
                s_time = time.time()
                
                # audio VAP
                result = self.model(waveform = data)
                
                event = self.model.event_extractor(result["vad"])
                
                probs = self.model.objective.get_probs(result["logits"])
                
                # pp.pprint(event)
                
                # pp.pprint(probs)
                
                # print(result["vad"].shape)
                # print(result["logits"].shape)
                # print(probs["p_now"].shape)
                # print(probs["p_future"].shape)
                # print(probs["p_tot"].shape)
                # input()
                
                e_time = time.time()
                
                # monitor input audio
                
                
                # vap = probs["p_tot"][0, -1]
                vap = probs["p_future"][0, -1]
                
                # vap = (vap - vap.mean()) / vap.std()
                # vap = (vap - 0.4) * 7
                
                # vap_user = vap[0]
                # vap_agent = vap[1]
                
                self.lock.acquire()
                
                #VAD
                # self.vap_user = result["vad"].cpu().detach().numpy()[0, -1, 1]
                # self.vap_agent = result["vad"].cpu().detach().numpy()[0, -1, 0]
                
                #VAP
                self.vap_user = vap[1]
                self.vap_agent = vap[0]
                                            
                self.lock.release()

                # self.print_vap_audio(i, self.vap_user, self.vap_agent, self.audio_user, self.audio_agent)
                
                # print("{:5.2f}, {:5.2f}".format(tmp_amp_user, tmp_amp_agent))
                
                # print(np.shape(data[0, 0]))
                # print(np.shape(data[0, 1]))
                # input()
                
                # print(result["logits"].shape)
                # print(result["vad"].shape)
            
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

        print('[TurnManagement VAP] loop stopped')
    
    def get_VAP(self):
        
        return self.vap_user, self.vap_agent
    
    def get_audio(self):
        
        return self.audio_user, self.audio_agent
    
    def get_turn(self, vap_user, vap_agent):
        
        if vap_user > 0.52:
            
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
        
        turn_decision = np.average(self.turn_history)
        
        return turn_decision
    
    def print_vap_audio(self, i, vap_user, vap_agent, audio_user, audio_agent, turn = None):
        
        self.lock.acquire()

        tmp_amp_user = abs(audio_user[-1])
        tmp_amp_agent = abs(audio_agent[-1])
        if tmp_amp_user > self.amp_max_user:
            self.amp_max_user = tmp_amp_user
        if tmp_amp_agent > self.amp_max_agent:
            self.amp_max_agent = tmp_amp_agent

        self.lock.release()

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
        self.mic.stop()  # close the connection
        # print()
        # print('Client has been closed')

    def updateMicPort(self, new_port):
        
        self.mic.updatePort(new_port)
                
class Agent:
    
    def __init__(self, agent_audio_path = "output.wav", rate = 16000, input_length = 20.0):
        
        self.audio_path = agent_audio_path

        self.rate = rate
        self.input_length = input_length
        
        self.agent_speech = None
        self.prev_agent_update = time.time()
    
    def get(self, prev_chunk):
        
        if agent_speaking_state:
            
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

class Mic:
    
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
    
if __name__ == '__main__':
    main()
