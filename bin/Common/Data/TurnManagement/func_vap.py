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
import tensorrt
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

# For face image clipper
import dlib
import cv2

from torch.autograd import profiler

import signal

from func_util import Behaviors, wait, int2float, print_size_of_model

import torch.nn as nn
from torch.ao.quantization import get_default_qconfig, prepare, convert

# For mainloop timeout
# s_mainloop = time.time()
# s_mainloop = time.perf_counter()
# timeout_loop_started = False

os.environ["OPENCV_FFMPEG_READ_ATTEMPTS"] = "8192"
torch.backends.cudnn.benchmark = True
everything_deterministic()

class VAP_interface:
    
    def __init__(self, vap_user, vap_agent, turn, mic_update, port, is_active, is_ready):
        
        self.vap_user = vap_user
        self.vap_agent = vap_agent
        self.turn = turn
        self.mic_update = mic_update
        self.port = port
        self.is_active = is_active
        self.is_ready = is_ready
    
    def get_VAP(self):
        
        return self.vap_user.value, self.vap_agent.value

    def get_turn(self, a, b):

        return self.turn.value
    
    def updateMicPort(self, port):
        
        self.mic_update.value = True
        self.port.value = port
        
        
def vap_runner(mic_host, mic_port, sample_rate, input_length, detector_path, sp_path, tgt_size, image_fps, 
               agent_speaking_state,
               vap_user, vap_agent, turn, mic_update, port, is_active, is_ready):
    
    vap = VAP(mic_host, mic_port, sample_rate, input_length, detector_path, sp_path, tgt_size, image_fps, 
              agent_speaking_state, is_active, is_ready)
    vap.start()
    
    while True:
        
        vap_user.value, vap_agent.value = vap.get_VAP()
        turn.value = vap.get_turn(vap_user.value, vap_agent.value)
        
        if mic_update.value:
            vap.updateMicPort(port.value)
            mic_update.value = False
        
        # is_active.value = vap.is_active
        # is_ready.value = vap.is_ready
        
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
                 agent_speaking_state, is_active, is_ready):

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
        self.is_active = is_active

        # self.dtype = torch.float32
        self.dtype = torch.float16
        # self.dtype = torch.bfloat16
        # self.dtype = torch.qint8
        
        with torch.amp.autocast('cuda'):

            self.model = VAPModel.load_from_checkpoint(configs["cfg_dict"]["checkpoint"], strict=False, conf=configs["model"])
            # self.model.eval()
            # self.model.cuda()
            self.model.cpu()
            self.model.to(self.dtype)
            
            # print('Compiling model...',end='',flush=True)
            # self.model = torch.compile(self.model)
            # self.model = torch.compile(self.model, backend="tensorrt")
            # self.model = torch.compile(self.model, mode="reduce-overhead")
            # self.model = torch.compile(self.model, backend="cudagraphs")
            # print('done')
            
            print("model on cuda:", next(self.model.parameters()).is_cuda)

        self.model.event_conf = configs["event"]
        self.model.event_extractor = TurnTakingEvents(self.model.event_conf)
        
        self.userAudio = UserAudio(mic_host, mic_port)
        self.userAudio.start()
        
        self.agentAudio = AgentAudio(agent_speaking_state, agent_audio_path, sample_rate, input_length)
        
        self.face_clipper = Face_clipper(detector_path, sp_path, tgt_size)
        
        self.userFaceImage = UserFaceImage(self.face_clipper)
        self.agentFaceImage = AgentFaceImage(self.face_clipper, agent_image_path)
        
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
        
        self.is_ready = is_ready

        audio_tensor = torch.concat(
            (torch.from_numpy(self.audio_user).unsqueeze(0), 
             torch.from_numpy(self.audio_agent).unsqueeze(0)), dim=0).unsqueeze(0)
        audio_tensor = audio_tensor.cuda().to(dtype=self.dtype)
        face_image_user_tensor = torch.from_numpy(self.face_image_user).unsqueeze(0).cuda()
        face_image_user_tensor = face_image_user_tensor.to(dtype=self.dtype)
        face_image_agent_tensor = torch.from_numpy(self.face_image_agent).unsqueeze(0).cuda()
        face_image_agent_tensor = face_image_agent_tensor.to(dtype=self.dtype)
        self.input_data = {
            "waveform":torch.rand_like(audio_tensor).cuda(),
            "gaze1":self.dummy_tensor,
            "head1":self.dummy_tensor,
            "face1":self.dummy_tensor,
            "body1":self.dummy_tensor,
            "face_im1":torch.rand_like(face_image_user_tensor).cuda(),
            "gaze2":self.dummy_tensor,
            "head2":self.dummy_tensor,
            "face2":self.dummy_tensor,
            "body2":self.dummy_tensor,
            "face_im2":torch.rand_like(face_image_agent_tensor).cuda(),
        }
        
        # print('initializing model...',end='',flush=True)

        # # prepare graph
        # torch.cuda.synchronize()
        # stream = torch.cuda.Stream()
        # stream.wait_stream(torch.cuda.current_stream())

        # with torch.cuda.stream(stream):

        #     self.graph = torch.cuda.CUDAGraph()
        #     with torch.cuda.graph(self.graph):
                
        #         self.output_data = self.model(src = self.input_data)
            
        #     torch.cuda.synchronize()
            
        # stream.synchronize()
        
        # for _ in range(10):
        #     self.graph.replay()
        # torch.cuda.synchronize()
        
        # print('done')

        # with torch.amp.autocast('cuda'):
            
        #     print_size_of_model(self.model, "fp32")

        #     # qconfig = get_default_qconfig("fbgemm")
        #     # self.model.qconfig = qconfig
        #     # self.model = torch.quantization.fuse_modules(self.model, [])
        #     # self.model = torch.quantization.quantize_dynamic(self.model, {nn.Linear}, dtype=torch.qint8)
        #     # self.model = torch.quantization.quantize_dynamic(self.model, dtype=torch.qint8)
        #     # self.model = prepare(self.model)
        #     # self.model(self.input_data)

        #     print_size_of_model(self.model, "int8")
            
        #     # self.model = convert(self.model)
        
        self.model._trainer = object()
        self.model = self.model.cuda().eval()
        
        #
        ### Need further work to be ready for jit
        #
        # self.model = torch.jit.script(self.model)
        # self.model = torch.jit.trace(self.model, example_kwarg_inputs={"src":self.input_data}, strict=False)

        print("model on cuda:", next(self.model.parameters()).is_cuda)
        
        # warmup
        print('warm up...', end='', flush=True)
        with torch.no_grad():
            for i in range(10):
                self.model(self.input_data)
        print('done')
    
    def main_loop(self):
        
        # i = 0

        while self.is_active.value:
            
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
                time_prep = e_time - s_time
                # print('[VAP data prep] {:.2f}'.format(time_prep))
                
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

                with torch.no_grad():
                    result = self.model(src = src)
                
                ### cuda graph: start ##################################################################
                
                ##
                ## Not working yet...
                ##
                
                # self.input_data["waveform"].copy_(audio_tensor)
                # self.input_data["face_im1"].copy_(face_image_user_tensor)
                # self.input_data["face_im2"].copy_(face_image_agent_tensor)
                # self.graph.replay()
                # result = self.output_data

                ### cuda graph: end ##################################################################

                e_time = time.perf_counter()
                time_calc_1 = e_time - s_time
                s_time = time.perf_counter()
                
                # audio VAP
                # result = self.model(waveform = audio_tensor)
                
                # print('1')
                
                # event = self.model.event_extractor(result["vad"])

                # print('2')
                
                probs = self.model.objective.get_probs(result["logits"])

                e_time = time.perf_counter()
                time_calc_2 = e_time - s_time
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
                time_post = e_time - s_time
                # print('[VAP post-process] {:.2f}'.format(e_time - s_time))
                
                time_all = e_time - s_time_all
                # print('[VAP all] {:.2f}'.format(e_time - s_time_all))
                print('ALL: {:4.2f}, PREP: {:4.2f} ({:4.2f}%), CALC1: {:4.2f} ({:4.2f}%), CALC2: {:4.2f} ({:4.2f}%), POST: {:4.2f} ({:4.2f}%)'.format(
                    time_all, 
                    time_prep, time_prep/time_all, 
                    time_calc_1, time_calc_1/time_all,
                    time_calc_2, time_calc_2/time_all,
                    time_post, time_post/time_all
                    )
                )
                

                self.cnt += 1
                
                self.is_ready.value = True
            
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
                self.is_active.value = False
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
        
        self.is_active.value = True
        self.main_thread = Thread(target = self.main_loop)
        self.main_thread.daemon = True
        self.main_thread.start()
        print('[TurnManagement VAP] loop started')
        
    def stop(self):

        self.lock.acquire()
        self.is_active.value = False
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

    def __init__(self, face_clipper, agent_image_path):
        
        self.agent_image_path = agent_image_path
        self.face_clipper = face_clipper
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
        face_frame = self.face_clipper.crip(frame)
        
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
    
    def __init__(self, face_clipper):
        
        self.camera = Camera()
        self.face_clipper = face_clipper
        self.cnt = 0
        
    def get(self, frame_sequence, visualize = True):
        
        frame = self.camera.get()
        face_frame = self.face_clipper.crip(frame)
        
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

class Face_clipper:
    
    def __init__(self, detector_path, sp_path, tgt_size):

        self.detector = dlib.cnn_face_detector = dlib.cnn_face_detection_model_v1(detector_path)
        # self.detector = dlib.get_frontal_face_detector()
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
                    
                    try:
                        # For dlib.cnn_face_detector = dlib.cnn_face_detection_model_v1(detector_path)
                        detection = detection.rect
                    except:
                        # For dlib.get_frontal_face_detector()
                        pass
                    
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
