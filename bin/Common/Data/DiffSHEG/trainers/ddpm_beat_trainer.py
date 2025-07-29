import numpy as np
import scipy
import torch
import torch.nn.functional as F
import random
import time
from models.transformer import MotionTransformer
from torch.utils.data import DataLoader
import torch.optim as optim
from torch.nn.utils import clip_grad_norm_
from collections import OrderedDict
#from utils.utils import print_current_loss
from os.path import join as pjoin
import codecs as cs
import torch.distributed as dist
from tqdm import tqdm
import os
from enum import Enum
from models.respace import SpacedDiffusion, space_timesteps


from datasets import data_tools

#import wandb
import json
import librosa

from mmcv.runner import get_dist_info
from models.gaussian_diffusion import (
    GaussianDiffusion,
    get_named_beta_schedule,
    create_named_schedule_sampler,
    ModelMeanType,
    ModelVarType,
    LossType
)

import datasets.rotation_converter as rot_cvt

import soundfile as sf


class DDPMRunner_beat(object):

    def __init__(self, args, encoder, test_dataset, eval_model=None):
        from transformers import Wav2Vec2Processor, HubertModel
        self.wav2vec2_processor = Wav2Vec2Processor.from_pretrained("facebook/hubert-large-ls960-ft")
        self.hubert_model = HubertModel.from_pretrained("facebook/hubert-large-ls960-ft")

        self.opt = args
        self.device = args.device
        self.encoder = encoder
        self.epoch = 0
        self.eval_model = eval_model
        self.opt.is_train = False  # Ensure inference mode

        self.ori_list = data_tools.joints_list["greta_ori_fingers_bis"]
        self.target_list = data_tools.joints_list["spine_neck_141_renamed"]
        self.gt_bvh_path = "data/GRETA/Base_greta_fingers_bis.bvh"
        self.file_content_length = 411
        with open(self.gt_bvh_path, 'r') as bvh_base:
            self.offset_data = bvh_base.readlines()[self.file_content_length]
            self.offset_data = np.fromstring(self.offset_data, dtype=float, sep=' ')
        offset_base = torch.tensor([0., 0., -np.pi/2])
        self.base_R_offset = rot_cvt.euler_angles_to_matrix(offset_base, "XYZ").float()
        self.thumb1_R_offset = rot_cvt.euler_angles_to_matrix(torch.tensor([10., 42., -7.]) * np.pi / 180, "XYZ").float()
        self.thumb2_R_offset = rot_cvt.euler_angles_to_matrix(torch.tensor([10., 0., 0.]) * np.pi / 180, "XYZ").float()
        self.arm_joints = ("Elbow", "Wrist")

        if eval_model is not None and 'test' not in self.opt.mode:
            self.load_fid_net(args.e_path)
            self.eval_model.eval()

        self.diffusion_steps = args.diffusion_steps
        sampler = 'uniform'
        beta_scheduler = 'linear'
        betas = get_named_beta_schedule(beta_scheduler, self.diffusion_steps)
        model_mean_type = {
            "epsilon": ModelMeanType.EPSILON,
            "start_x": ModelMeanType.START_X,
            "previous_x": ModelMeanType.PREVIOUS_X
        }

        self.diffusion = GaussianDiffusion(
            opt=args,
            betas=betas,
            model_mean_type=model_mean_type[args.model_mean_type],
            model_var_type=ModelVarType.FIXED_SMALL,
            loss_type=LossType.MSE
        )

        if self.opt.ddim:
            self.diffusion_ddim_val = SpacedDiffusion(
                use_timesteps=space_timesteps(self.diffusion_steps, 'ddim25'),
                opt=args,
                betas=betas,
                model_mean_type=model_mean_type[args.model_mean_type],
                model_var_type=ModelVarType.FIXED_SMALL,
                loss_type=LossType.MSE,
                rescale_timesteps=False,
            )

        self.sampler = create_named_schedule_sampler(sampler, self.diffusion)
        self.sampler_name = sampler

        self.to(self.device)

        if self.opt.dataset_name == 'beat' and (self.opt.expression_only or self.opt.net_dim_pose == 192):
            self.face_mean = np.load("./data/json_mean.npy")
            self.face_std = np.load("./data/json_std.npy")
            self.facial_list = ['browDownLeft', 'browDownRight', 'browInnerUp', 'browOuterUpLeft', 
                                'browOuterUpRight', 'cheekPuff', 'cheekSquintLeft', 'cheekSquintRight', 
                                'eyeBlinkLeft', 'eyeBlinkRight', 'eyeLookDownLeft', 'eyeLookDownRight', 
                                'eyeLookInLeft', 'eyeLookInRight', 'eyeLookOutLeft', 'eyeLookOutRight', 
                                'eyeLookUpLeft', 'eyeLookUpRight', 'eyeSquintLeft', 'eyeSquintRight', 
                                'eyeWideLeft', 'eyeWideRight', 'jawForward', 'jawLeft', 'jawOpen', 
                                'jawRight', 'mouthClose', 'mouthDimpleLeft', 'mouthDimpleRight', 
                                'mouthFrownLeft', 'mouthFrownRight', 'mouthFunnel', 'mouthLeft', 
                                'mouthLowerDownLeft', 'mouthLowerDownRight', 'mouthPressLeft', 
                                'mouthPressRight', 'mouthPucker', 'mouthRight', 'mouthRollLower', 
                                'mouthRollUpper', 'mouthShrugLower', 'mouthShrugUpper', 'mouthSmileLeft', 
                                'mouthSmileRight', 'mouthStretchLeft', 'mouthStretchRight', 'mouthUpperUpLeft', 
                                'mouthUpperUpRight', 'noseSneerLeft', 'noseSneerRight']
        
        self.std_pose_axis_angle = test_dataset.std_pose_axis_angle
        self.mean_pose_axis_angle = test_dataset.mean_pose_axis_angle
        self._audio_buffer = np.array([], dtype=np.float32)
            
        model_dir = os.path.join(self.opt.model_dir, self.opt.ckpt)
        self.epoch, _, _, _, _ = self.load(model_dir)
        
        self.encoder.eval()

        self.eval_mode()  # Put encoder into eval mode

    def generate_batch(self, audio_emb, p_id, dim_pose, add_cond={}, inpaint_dict=None):
        audio_emb = audio_emb.to(self.device)
        B = len(audio_emb)
        T = audio_emb.shape[1]
        cur_len = torch.LongTensor([T for ii in range(B)]).to(self.device)

        if self.opt.ddim:
            output = self.diffusion_ddim_val.ddim_sample_loop(
                self.encoder,
                (B, T, dim_pose),
                clip_denoised=False,
                progress=False,
                model_kwargs={
                    "audio_emb": audio_emb, 
                    "length": cur_len, 
                    "person_id": p_id,
                    "add_cond": add_cond,
                    "y": inpaint_dict,
                    "pe_type": self.opt.PE
                })
        else:
            output = self.diffusion.p_sample_loop(
                self.encoder,
                (B, T, dim_pose),
                clip_denoised=False,
                progress=False,
                model_kwargs={
                    "audio_emb": audio_emb, 
                    "length": cur_len, 
                    "person_id": p_id,
                    "add_cond": add_cond,
                    "y": inpaint_dict,
                    "pe_type": self.opt.PE
                })

        return output
    
    def to(self, device):
        self.encoder = self.encoder.to(device)

    def train_mode(self):
        self.encoder.train()

    def eval_mode(self):
        self.encoder.eval()

    def one_hot(self, ids, dim):
        ones_eye = torch.eye(dim)
        return (ones_eye[ids.long()].squeeze())


    def load(self, model_dir):
        checkpoint = torch.load(model_dir, map_location=self.device)

        if self.opt.PE == "pe_sinu_repeat":
            mm = checkpoint['encoder']['PE.pe'][:, :self.n_poses, :]
            checkpoint['encoder']['PE.pe'] = torch.cat((mm, mm, mm, mm), -2)
        if self.opt.is_train:
            self.opt_encoder.load_state_dict(checkpoint['opt_encoder'])
        
        try:
            self.encoder.module.load_state_dict(checkpoint['encoder'], strict=False)
        except:
            self.encoder.load_state_dict(checkpoint['encoder'], strict=False)


        return checkpoint['ep'], checkpoint.get('total_it', 0), \
                checkpoint.get('best_fgd', 99999), checkpoint.get('best_mse', 99999), \
                checkpoint.get('best_pck', 0)
    
    def get_windows(self, x, size, step):
        if isinstance(x, dict):
            out = {}
            for key in x.keys():
                out[key] = self.get_windows(x[key], size, step)
            out_dict_list = []
            for i in range(len(out[list(out.keys())[0]])):
                out_dict_list.append({key: out[key][i] for key in out.keys()})
            return out_dict_list
        else:
            seq_len = x.shape[1]
            if seq_len <= size:
                return [x]
            else:
                win_num = (seq_len - (size-step)) / float(step)
                out = [x[:, mm*step : mm*step + size, ...] for mm in range(int(win_num))]
                if win_num - int(win_num) != 0:
                    out.append(x[:, int(win_num)*step:, ...])  
                return out
            
    def slerp_interpolate_quat(self, seq, target_fps=25, original_fps=15):
        # seq: (1, T, J, 4) — quaternions per joint
        assert seq.shape[0] == 1, "Only batch size of 1 supported."
        seq = seq.squeeze(0)  # (T, J, 4)
        T, J, _ = seq.shape
        target_T = int(T * target_fps / original_fps)
        x_old = np.linspace(0, 1, T)
        x_new = np.linspace(0, 1, target_T)
        
        result = np.zeros((target_T, J, 4))
        for j in range(J):
            rot = scipy.spatial.transform.Rotation.from_quat(seq[:, j])          # (T,) Rotation object for joint j
            slerp = scipy.spatial.transform.Slerp(x_old, rot)
            interp_rot = slerp(x_new)
            result[:, j] = interp_rot.as_quat()   # (target_T, 4)
        return result[np.newaxis, ...]  # (1, target_T, J, 4)
    

    def generate_realtime_frame(self, audio_data, hop_size=1200, sr=16000):
        #start_realtime_frame = time.time()

        audio_emb = torch.from_numpy(
            np.swapaxes(librosa.feature.melspectrogram(y=librosa.resample(audio_data, orig_sr=sr, target_sr=18000), 
                                                      sr=18000, hop_length=hop_size, n_mels=128), -1, -2).astype(np.float32)
        ).unsqueeze(0).to(self.device)

        motions = torch.zeros((1, audio_emb.shape[-2], self.opt.net_dim_pose)).to(self.opt.device)
        window_step = self.opt.n_poses - self.opt.overlap_len
        audio_emb_list = self.get_windows(audio_emb, self.opt.n_poses, window_step)
        motions_list = self.get_windows(motions, self.opt.n_poses, window_step)

        add_cond = {}
        if self.opt.expAddHubert or self.opt.addHubert:
            add_cond["pretrain_aud_feat"] = get_hubert_from_16k_speech_long(
                self.hubert_model, self.wav2vec2_processor,
                torch.from_numpy(audio_data).unsqueeze(0).to(self.device),
                device=self.device
            )
            add_cond["pretrain_aud_feat"] = F.interpolate(
                add_cond["pretrain_aud_feat"].swapaxes(-1, -2).unsqueeze(0),
                size=audio_emb.shape[-2], mode='linear', align_corners=True
            ).swapaxes(-1, -2)
        if isinstance(add_cond, dict):
            for key in add_cond.keys():
                add_cond[key] = add_cond[key].to(self.device)

        if add_cond not in [None, {}]:
            add_cond_list = self.get_windows(add_cond, self.opt.n_poses, window_step) if add_cond else [{} for _ in audio_emb_list]
        
        if self.opt.fix_very_first:
            zero_pose = (-self.mean_pose_axis_angle) / self.std_pose_axis_angle
            zero_pose[14] = 80 * np.pi/180
            zero_pose[80] = -70 * np.pi/180
            zero_pose = np.pad(zero_pose,(0,51))
            base_inpaint = torch.tensor(np.tile(zero_pose,(self.opt.overlap_len,1)), device=self.device).unsqueeze(0)
        
        p_id = torch.ones((1, 1)) * 1
        p_id = self.one_hot(p_id, self.opt.speaker_dim).detach().to(self.device)

        for ii, (audio_emb, motions) in enumerate(zip(audio_emb_list, motions_list)):
            if add_cond not in [None, {}]:
                add_cond = add_cond_list[ii]
            inpaint_dict = {}
            if self.opt.overlap_len > 0:
                inpaint_dict['gt'] = torch.zeros_like(motions)
                inpaint_dict['outpainting_mask'] = torch.zeros_like(motions, dtype=torch.bool, device=motions.device)
                if ii == 0 and self.opt.fix_very_first:
                    inpaint_dict['outpainting_mask'][..., :self.opt.overlap_len, :] = True
                    inpaint_dict['gt'][:, :self.opt.overlap_len, ...] = base_inpaint
                elif ii > 0:
                    inpaint_dict['outpainting_mask'][..., :self.opt.overlap_len, :] = True
                    inpaint_dict['gt'][:, :self.opt.overlap_len, ...] = outputs[:, -self.opt.overlap_len:, ...]
                
                #start_generate_batch = time.time()

                outputs = self.generate_batch(audio_emb, p_id, self.opt.net_dim_pose, add_cond, inpaint_dict)

                #print(f"[Greta DiffSHEG] generate_batch returned after {time.time() - start_generate_batch:.4f} seconds")

                outputs_out = outputs.cpu().clone()
                outputs_out = outputs_out[:,:,:self.opt.split_pos]
                if ii < len(motions_list)-1:
                    outputs_out = outputs_out[:, :window_step, :]
                denorm_out = outputs_out * self.std_pose_axis_angle + self.mean_pose_axis_angle
                B, T, C = denorm_out.shape
                T_interp = T * 25 // 15
                C_quat = C * 4 // 3
                quat_out = rot_cvt.axis_angle_to_quaternion(denorm_out.reshape(B, T, C // 3, 3)).reshape(B, T, C_quat // 4, 4)
                quat_out = torch.tensor(self.slerp_interpolate_quat(seq=quat_out, target_fps=25, original_fps=15))
                denorm_out = rot_cvt.quaternion_to_axis_angle(quat_out.reshape(B, T_interp, C_quat // 4, 4)).reshape(B, T_interp, C)
                euler_out = rot_cvt.axis_angle_to_euler_angles(denorm_out.reshape(B, T_interp, C // 3, 3), convention='XYZ').reshape(B, T_interp, C)
                euler_out = euler_out * (180 / np.pi)

                frames_for_this_chunk = []
                for i in range(T_interp):
                    data = euler_out[0, i, :].clone()
                    data_rotation = self.offset_data.copy()
                    for iii, (k, v) in enumerate(self.target_list.items()):
                        if k in self.ori_list:
                            is_right = k.startswith("R")
                            R_offset = self.base_R_offset if is_right else self.base_R_offset.T
                            R_joint = rot_cvt.euler_angles_to_matrix(data[iii * 3:iii * 3 + 3] * np.pi / 180, "XYZ").float()
                            if k.endswith("Shoulder"):
                                R_joint = R_joint @ R_offset
                            elif k.endswith('HandT1'):
                                R_joint = self.thumb1_R_offset @ R_joint
                            elif k.endswith('HandT2'):
                                R_joint = self.thumb2_R_offset @ R_joint
                            elif k.endswith(self.arm_joints):
                                R_joint = R_offset.T @ R_joint @ R_offset
                            data[iii * 3:iii * 3 + 3] = rot_cvt.matrix_to_euler_angles(R_joint, "XYZ") * 180 / np.pi
                            data_rotation[self.ori_list[k][1] - v:self.ori_list[k][1]] = data[iii * 3:iii * 3 + 3]
                    frames_for_this_chunk.append(data_rotation)
                #print(f"[Greta DiffSHEG] Exiting generate_realtime_frame (total time for this chunk): {time.time() - start_realtime_frame:.4f} seconds"
                yield frames_for_this_chunk

            
@torch.no_grad()
def get_hubert_from_16k_speech_long(hubert_model, wav2vec2_processor, speech, device="cuda:0"):
    hubert_model = hubert_model.to(device)
    # if speech.ndim ==2:
    #     speech = speech[:, 0] # [T, 2] ==> [T,]
    input_values_all = wav2vec2_processor(speech, return_tensors="pt", sampling_rate=16000).input_values.squeeze(0) # [1, T]
    input_values_all = input_values_all.to(device)
    # For long audio sequence, due to the memory limitation, we cannot process them in one run
    # HuBERT process the wav with a CNN of stride [5,2,2,2,2,2], making a stride of 320
    # Besides, the kernel is [10,3,3,3,3,2,2], making 400 a fundamental unit to get 1 time step.
    # So the CNN is euqal to a big Conv1D with kernel k=400 and stride s=320
    # We have the equation to calculate out time step: T = floor((t-k)/s)
    # To prevent overlap, we set each clip length of (K+S*(N-1)), where N is the expected length T of this clip
    # The start point of next clip should roll back with a length of (kernel-stride) so it is stride * N
    kernel = 400
    stride = 320
    clip_length = stride * 1000
    num_iter = input_values_all.shape[1] // clip_length
    expected_T = (input_values_all.shape[1] - (kernel-stride)) // stride
    res_lst = []
    for i in range(num_iter):
        if i == 0:
            start_idx = 0
            end_idx = clip_length - stride + kernel
        else:
            start_idx = clip_length * i
            end_idx = start_idx + (clip_length - stride + kernel)
        input_values = input_values_all[:, start_idx: end_idx]
        hidden_states = hubert_model.forward(input_values).last_hidden_state # [B=1, T=pts//320, hid=1024]
        res_lst.append(hidden_states[0])
    if num_iter > 0:
        input_values = input_values_all[:, clip_length * num_iter:]
    else:
        input_values = input_values_all
    # if input_values.shape[1] != 0:
    if input_values.shape[1] >= kernel: # if the last batch is shorter than kernel_size, skip it            
        hidden_states = hubert_model(input_values).last_hidden_state # [B=1, T=pts//320, hid=1024]
        res_lst.append(hidden_states[0])
    
    ret = torch.cat(res_lst, dim=0).cpu() # [T, 1024]
    # assert ret.shape[0] == expected_T
    assert abs(ret.shape[0] - expected_T) <= 1
    if ret.shape[0] < expected_T:
        ret = torch.nn.functional.pad(ret, (0,0,0,expected_T-ret.shape[0]))
    else:
        ret = ret[:expected_T]
    return ret