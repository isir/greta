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
    
    print("[Greta DiffSHEG] Python start")

    parser = TrainCompOptions()
    opt = parser.parse()

    opt.data_root = 'data/BEAT'
    opt.fps = 15
    opt.net_dim_pose = 192
    opt.dim_pose = 141
    opt.split_pos = 141
    if opt.remove_hand:
        opt.dim_pose = 33
    opt.expression_dim = 51
    opt.audio_dim = 128
    if opt.use_aud_feat:
        opt.audio_dim = 1024
    opt.pose_fps = 15       # 15 fps is required; interpolation is done elsewhere
    opt.n_poses = 30
    opt.overlap_len = 4
    opt.ddim = True
    opt.timestep_respacing = "ddim25"
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
    opt.jump_n_sample = 2
    opt.stride = 10
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
    opt.name = 'beat_GesExpr_unify_addHubert_encodeHubert_mlpIncludeX_condRes_LN'
    opt.dataset_name = 'beat'
    opt.mode = 'test_custom_audio'
    opt.device = torch.device("cuda")
    print(f"[Greta DiffSHEG] opt.device is {opt.device}")

    test_dataset = __import__(f"datasets.{opt.dataset_name}", fromlist=["something"]).BeatDataset(opt, "test")

    model = build_models(opt, opt.net_dim_pose, opt.audio_dim, opt.audio_latent_dim, opt.style_dim)
    model.to(opt.device)

    runner = DDPMRunner_beat(opt,model, test_dataset)
    
    text_buffer_size = 1024

    feedback_server_host = socket.gethostname()
    feedback_server_port = 6500
    
    greta_host = socket.gethostname()
    greta_port = 6501

    global_lock = Lock()

    feedback_socket = socket.socket()
    try:
        feedback_socket.connect((feedback_server_host, feedback_server_port))
    except ConnectionRefusedError:
        print(f"[DiffSHEG Greta] Feedback server not available at {feedback_server_host}:{feedback_server_port}. Exiting.")
        return
    
    feedback_thread = Thread(target=feedback_loop, args= (feedback_socket, global_lock, text_buffer_size))
    feedback_thread.daemon = True
    feedback_thread.start()

    greta_socket = socket.socket()
    try:
        greta_socket.connect((greta_host,greta_port))
    except ConnectionRefusedError:
        print(f"[DiffSHEG Greta] greta server not available at {greta_host}:{greta_port}. Exiting.")
        return

    

    agent_audio_path = "../../../output.wav"
    audio_sr = 16000
    #agent = Agent(agent_audio_path=agent_audio_path, rate=audio_sr, input_length=2.0)
    #prev_chunk = np.zeros(int(audio_sr * agent.input_length), dtype=np.float32)

    is_generating = False
    gesture_queue = queue.Queue()

    def producer_task(audio_data):
        print("[Greta DiffSHEG] Prod task started")
        try:
            #start_generator_init = time.time()
            generator = runner.generate_realtime_frame(audio_data)
            #print(f"[Greta DiffSHEG] Time to initialize generator: {time.time() - start_generator_init:.4f} seconds")
            for frame in generator:
                gesture_queue.put(frame)
            gesture_queue.put(None)

        except Exception as e:
            print(f"[Greta DiffSHEG] Error in producer thread: {e}")
            traceback.print_exc()
            gesture_queue.put(None)
    
    try:
        producer_thread = None
        while True:
            with global_lock:
                is_speaking = agent_speaking_state

            if is_speaking and not is_generating:
                is_generating = True
                #start_load_time = time.time() 
                audio_data, _ = librosa.load(agent_audio_path, sr=audio_sr)
                #print(f"[Greta DiffSHEG] Time to load audio: {time.time() - start_load_time:.4f} seconds")
                audio_data = audio_data.astype(np.float32)
                producer_thread = Thread(target=producer_task, args=(audio_data,))
                producer_thread.start()

            try:
                frame_chunk = gesture_queue.get_nowait()

                if frame_chunk is None:
                    print("[Greta DiffSHEG] Producer finished.")
                    if producer_thread is not None:
                        producer_thread.join()
                        producer_thread = None
                    is_generating = False
                else:
                    print("[Greta DiffSHEG] sending batch of frame")
                    for frame in frame_chunk:
                        motion_str = ' '.join(map(str, frame))
                        greta_socket.send('{}\r\n'.format(motion_str).encode())
                        time.sleep(1/25)
            
            except queue.Empty:
                pass


            time.sleep(1/60)
            
    except KeyboardInterrupt:
        print("[Greta DiffSHEG] Keyboard interrupt")
    except Exception as e:
        print(f"[DiffSHEG Greta] Error in main loop: {e}")
        traceback.print_exc()
    finally:
        feedback_socket.close()
        greta_socket.close()    
        print("[Greta DiffSHEG] Python end")
                    
def feedback_loop(feedback_socket, global_lock, text_buffer_size):
    global agent_speaking_state
    print('[DiffSHEG feedback] loop started')
    while True:
        print(f"[Greta DIFFSHEG] FEEDBACK LOOP waiting for message at {time.time()}")
        print(f"[Greta DIFFSHEG] agent_speaking_state {agent_speaking_state}")
        sys.stdout.flush()
        try:
            data = feedback_socket.recv(text_buffer_size).decode().strip()
            if not data:
                print('[DiffSHEG feedback] Empty data received, connection may be closing.')
                time.sleep(0.01)
                continue

            with global_lock:
                if data == 'end':
                    print("[Greta DIFFSHEG] end feedback received by python")
                    agent_speaking_state = False
                elif data == 'start':
                    print("[Greta DIFFSHEG] start feedback received by python")
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


class Agent:
    
    def __init__(self, agent_audio_path = "output.wav", rate = 16000, input_length = 20.0):
        
        self.audio_path = agent_audio_path

        self.rate = rate
        self.input_length = input_length
        
        self.agent_speech = None
        self.prev_agent_update = time.time()
    
    def get(self, prev_chunk):
        OVER = False
        
        if agent_speaking_state:
            
            if self.agent_speech == None:
                print("[Greta DIFFSHEG] updated agent speech wav")
                self.agent_speech = AgentSpeech(self.audio_path, self.rate, self.input_length)

            curr_chunk, OVER = self.agent_speech.get(prev_chunk)

            if OVER:
                self.agent_speech = None
            
        else:
            
            over_frames = int(self.rate * (time.time() - self.prev_agent_update))
            curr_chunk = np.concatenate((prev_chunk, np.zeros(over_frames)), axis = 0)[-int(self.rate * self.input_length):]
            
            self.agent_speech = None

        return curr_chunk, OVER
            
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
            print(f"--- Audio chunking start at {time.time()} ---")
            prev_chunk = np.concatenate((prev_chunk, self.audio[self.curr_index:int(len(self.audio) * curr_sec / self.duration)]))
            curr_chunk = prev_chunk[-int(self.rate * self.input_length):]
            self.curr_index = int(len(self.audio) * curr_sec / self.duration)
            print(f"--- Audio chunking end at {time.time()} ---")
        curr_chunk = np.ascontiguousarray(curr_chunk)
                
        return curr_chunk, self.OVER
    

if __name__=='__main__':
    main()