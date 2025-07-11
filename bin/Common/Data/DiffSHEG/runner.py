import numpy as np
import os
import socket
import json
from os.path import join as pjoin
import queue
import time
from threading import Thread, Lock
import librosa
import traceback

#import utils.paramUtil as paramUtil
from options.train_options import TrainCompOptions
# from utils.plot_script import *

from models import MotionTransformer, UniDiffuser
from trainers import DDPMRunner_beat
from datasets import ShowDataset
from Deps import *

#from mmcv.runner import get_dist_info, init_dist
#from mmcv.parallel import MMDistributedDataParallel, MMDataParallel
import warnings

import torch
import torch.backends.cudnn as cudnn
import torch.distributed as dist
import torch.multiprocessing as mp
import torch.nn as nn
import torch.nn.parallel
import torch.optim
import torch.utils.data
import torch.utils.data.distributed

import sys
sys.path.append(os.path.join(sys.path[2], "A_TalkSHOW_ori"))

agent_speaking_state = False

def build_models(opt, dim_pose, audio_dim=128, audio_latent_dim=256, style_dim=4):
    if opt.unidiffuser:
        encoder = UniDiffuser(
            opt=opt,
            input_feats=dim_pose,
            audio_dim=audio_dim,
            aud_latent_dim=audio_latent_dim,
            style_dim=style_dim,
            num_frames=opt.n_poses,
            num_layers=opt.num_layers,
            latent_dim=opt.latent_dim,
            no_clip=opt.no_clip,
            no_eff=opt.no_eff,
            pe_type=opt.PE)
    else:
        encoder = MotionTransformer(
            opt=opt,
            input_feats=dim_pose,
            audio_dim=audio_dim,
            style_dim=style_dim,
            num_frames=opt.n_poses,
            num_layers=opt.num_layers,
            latent_dim=opt.latent_dim,
            no_clip=opt.no_clip,
            no_eff=opt.no_eff,
            pe_type=opt.PE)
    return encoder

def build_fgd_val_model(opt):
    eval_model_module = __import__(f"models.motion_autoencoder", fromlist=["something"])
    eval_model = getattr(eval_model_module, 'HalfEmbeddingNet')(opt)

    #print(f"init 'HalfEmbeddingNet' success")
    return eval_model

def main():
    
    print("[DiffSHEG Greta] Python start")

    parser = TrainCompOptions()
    opt = parser.parse()

    opt.data_root = 'data/BEAT'
    opt.fps = 15
    opt.dim_pose = 141
    if opt.remove_hand:
        opt.dim_pose = 33
    opt.expression_dim = 51
    opt.net_dim_pose = opt.dim_pose  # si gesture only
    opt.audio_dim = 128
    if opt.use_aud_feat:
        opt.audio_dim = 1024
    opt.pose_fps = 15       # 15 fps is required; interpolation is done elsewhere
    opt.n_poses = 34
    opt.model_dir = './checkpoints/beat/beat_GesExpr_unify_addHubert_encodeHubert_mlpIncludeX_condRes_LN/model'
    opt.ckpt='fgd_best.tar'        
    opt.audio_dim = 128
    if opt.use_aud_feat:
        opt.audio_dim = 1024
    opt.style_dim = 30 # totally 30 subjects
    opt.speaker_dim = 30
    opt.word_index_num = 5793
    opt.word_dims = 300
    opt.word_f = 128
    opt.emotion_f = 8
    opt.emotion_dims = 8
    opt.freeze_wordembed = False
    opt.hidden_size = 256
    opt.n_layer = 4
    opt.stride = 10
    opt.pose_fps = 15
    opt.vae_length = 300
    opt.new_cache = False
    opt.audio_norm = False
    opt.facial_norm = True
    opt.pose_norm = True
    opt.train_data_path = f'data/BEAT/beat_cache/{opt.beat_cache_name}/train/'
    opt.val_data_path = f'data/BEAT/beat_cache/{opt.beat_cache_name}/val/'
    opt.test_data_path = f'data/BEAT/beat_cache/{opt.beat_cache_name}/test/'
    opt.mean_pose_path = f'data/BEAT/beat_cache/{opt.beat_cache_name}/train/'
    opt.std_pose_path = f'data/BEAT/beat_cache/{opt.beat_cache_name}/train/'
    opt.multi_length_training = [1.0]
    opt.audio_rep = 'wave16k'
    opt.facial_rep = 'facial52'
    opt.speaker_id = 'id'
    opt.pose_rep = 'bvh_rot'
    opt.word_rep = 'text'
    opt.sem_rep = 'sem'
    opt.emo_rep = 'emo'
    opt.dataset_name = 'beat'
    opt.mode = 'test_custom_audio'
    opt.device = torch.device("cuda")

    test_dataset = __import__(f"datasets.{opt.dataset_name}", fromlist=["something"]).BeatDataset(opt, "test")

    model = build_models(opt, opt.net_dim_pose, opt.audio_dim, opt.audio_latent_dim, opt.style_dim)
    model.to(opt.device)

    runner = DDPMRunner_beat(opt,model)
    
    text_buffer_size = 1024

    feedback_server_host = socket.gethostname()
    feedback_server_port = 6500
    
    greta_host = socket.gethostname()
    greta_port = 6501

    system_ready = False

    global_lock = Lock()

    feedback_socket = socket.socket()
    try:
        feedback_socket.connect((feedback_server_host, feedback_server_port))
    except ConnectionRefusedError:
        print(f"[DiffSHEG Greta] Feedback server not available at {feedback_server_host}:{feedback_server_port}. Exiting.")
        return
    
    feedback_thread = Thread(target=feedback_loop, args= (feedback_socket, global_lock, text_buffer_size))
    feedback_thread.start()

    greta_socket = socket.socket()
    try:
        greta_socket.connect((greta_host,greta_port))
    except ConnectionRefusedError:
        print(f"[DiffSHEG Greta] greta server not available at {greta_host}:{greta_port}. Exiting.")
        return

    

    agent_audio_path = "../../../output.wav"
    audio_sr = 16000
    audio_buffer = np.zeros(int(audio_sr * 10.0))
    agent = Agent(agent_audio_path, audio_sr, 10.0)
    gesture_generator = None
    while True:
        try:
            with global_lock:
                is_speaking = agent_speaking_state

                if is_speaking:
                    if gesture_generator == None:
                        print("[DiffSHEG Greta] Agent started speaking. Initializing gesture generator.")
                        initial_audio_chunk = np.zeros(1200, dtype=np.float32)
                        gesture_generator = runner.generate_realtime_frame(initial_audio_chunk, [], test_dataset)
                    current_audio_chunk, _ = agent.get(audio_buffer)

                    if gesture_generator:
                        try:
                            generated_motion = next(gesture_generator)

                            motion_str = ' '.join(map(str,generated_motion))

                            greta_socket.send('{}\r\n'.format(motion_str).encode())

                            greta_socket.recv(text_buffer_size)
                        except StopIteration:
                            gesture_generator = None
                            print("[DiffSHEG Greta] Gesture generator finished.")
                else:
                    if gesture_generator is not None:
                        print("[DiffSHEG Greta] Agent stopped speaking")
                        gesture_generator = None
                    time.sleep(0.01)
                time.sleep(1)
            
        except KeyboardInterrupt:
            print("[DiffSHEG Greta] Keyboard interrupt")
            break
        except Exception as e:
            print(f"[DiffSHEG Greta] Error in main loop: {e}")
            traceback.print_exc()
            break

    feedback_socket.close()
    greta_socket.close()

    print("[DiffSHEG Greta] Python end")
                        

class Agent:
    def __init__(self, agent_audio_path="output.wav", rate=16000, input_length=20.0):
        self.audio_path = agent_audio_path
        self.rate = rate
        self.input_length = input_length
        self.agent_speech = None
    
    def get(self, prev_chunk):
        if agent_speaking_state:

            if self.agent_speech is None:
                print("[DiffSHEG Agent] Agent started speaking, loading new audio file.")
                self.agent_speech = AgentSpeech(self.audio_path, self.rate, self.input_length)
            
            curr_chunk, OVER = self.agent_speech.get(prev_chunk)

            if OVER:
                self.agent_speech = None
            return curr_chunk, OVER
        else:
            silence = np.zeros(int(self.rate * 0.1), dtype=np.float32)
            curr_chunk = np.concatenate((prev_chunk, silence))[-int(self.rate * self.input_length):]
            self.agent_speech = None
            return curr_chunk, True

class AgentSpeech:
    def __init__(self, audio_path="output.wav", rate=16000, input_length=20.0):
        self.rate = rate
        self.input_length = input_length
        self.audio = None
        self.s_time = time.time()
        self.duration = 0
        self.curr_index = 0
        self.OVER = False

        for _ in range(5):
            try:
                self.audio, sr = librosa.load(audio_path, sr=self.rate, mono=True)
                self.duration = librosa.get_duration(y=self.audio, sr=sr)
                if self.duration > 0:
                    print(f"[DiffSHEG AgentSpeech] Loaded audio file '{audio_path}' with duration: {self.duration:.2f}s")
                    break
            except Exception as e:
                print(f"Waiting for audio file to be ready... ({e})")
                time.sleep(0.1)

        if self.audio is None:
             print(f"[DiffSHEG AgentSpeech] Failed to load audio file: {audio_path}")
             self.OVER = True
    
    def get(self, prev_chunk):
        if self.OVER or self.audio is None:
            return prev_chunk, True

        elapsed_time = time.time() - self.s_time
        
        if elapsed_time >= self.duration:
            self.OVER = True
            return prev_chunk, True
            
        target_index = int(elapsed_time * self.rate)
        
        new_audio_segment = self.audio[self.curr_index:target_index]
        self.curr_index = target_index
        
        updated_chunk = np.concatenate((prev_chunk, new_audio_segment))
        curr_chunk = updated_chunk[-int(self.rate * self.input_length):]
        
        return curr_chunk, self.OVER

def feedback_loop(feedback_socket, global_lock, text_buffer_size):
    global agent_speaking_state
    print('[DiffSHEG feedback] loop started')
    while True:
        try:
            data = feedback_socket.recv(text_buffer_size).decode().strip()
            if not data:
                print('[DiffSHEG feedback] Empty data received, connection may be closing.')
                time.sleep(0.5)
                continue

            with global_lock:
                if data == 'end':
                    agent_speaking_state = False
                elif data == 'start':
                    agent_speaking_state = True
            
            # Send acknowledgment back
            feedback_socket.sendall('ok\r\n'.encode())

        except socket.timeout:
            continue
        except ConnectionResetError:
            print('[DiffSHEG feedback] ConnectionReset')
            break
        except BrokenPipeError:
            print("[DiffSHEG feedback] Broken pipe error.")
            break
        except Exception as e:
            if isinstance(e, socket.error) and e.errno == 10057: # Not connected
                 print("[DiffSHEG feedback] Socket not connected.")
                 break
            print(f"[DiffSHEG feedback] Error: {e}")
            traceback.print_exc()
            break
    
    feedback_socket.close()
    print('[DiffSHEG feedback] loop ended')

if __name__=='__main__':
    main()