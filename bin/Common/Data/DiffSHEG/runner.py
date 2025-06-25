import numpy
import os
import socket
import json
from os.path import join as pjoin
import queue
import time

#import utils.paramUtil as paramUtil
from options.train_options import TrainCompOptions
# from utils.plot_script import *

from models import MotionTransformer, UniDiffuser
from trainers import ddpm_beat_trainer
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

def start_realtime_loop(runner, test_dataset, host="localhost", port=6500,
                        SR=16000, hop_size=1200, buffer_sec=10):
    print("starting loop")


    receiver = SimpleReceiver(host=host, port=port)
    sender = SimpleSender(host=host, port=port+1)

    receiver.connect()
    sender.connect()

    buffer_audio = []

    try:
        while True:
            header = receiver.sock.recv(4).decode()
            if header == "kill":
                print("[Server] Kill command received. Exiting.")
                break
            elif header == "audi":
                length_bytes = receiver.sock.recv(4)
                total_len = struct.unpack('>I', length_bytes)[0]
                payload = b""
                while len(payload) < total_len:
                    payload += receiver.sock.recv(total_len - len(payload))
                audio_array = pickle.loads(payload)
                print(f"[Receiver] Received audio array of shape {audio_array.shape}")

                gen = runner.generate_realtime_frame(audio_array, buffer_audio, test_dataset, hop_size=hop_size, sr=SR)
                for frame in gen:
                    sender.send_frame(frame)
    # Keyboardinterrupt for now
    except KeyboardInterrupt:
        print("Loop Stopped by keyboard (to change)")


            
            

    




def main():
    parser = TrainCompOptions()
    opt = parser.parse()

    opt.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    opt.distributed = False
    opt.gpu_id = 0 if torch.cuda.is_available() else None
    test_dataset = __import__(f"datasets.{opt.dataset_name}", fromlist=["something"]).BeatDataset(opt, "test")

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
    opt.n_poses = 150 if not hasattr(opt, 'n_poses') else opt.n_poses
    opt.model_dir = './checkpoints/beat/beat_GesExpr_unify_addHubert_encodeHubert_mlpIncludeX_condRes_LN/model'
    opt.ckpt='fgd_best.tar'

    model = build_models(opt, opt.net_dim_pose, opt.audio_dim, opt.audio_latent_dim, opt.style_dim)
    model.to(opt.device)

    runner = ddpm_beat_trainer(opt,model)

    start_realtime_loop(runner, test_dataset)

if __name__=='__main()__':
    main()