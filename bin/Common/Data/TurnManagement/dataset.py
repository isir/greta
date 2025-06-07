import os
import torch
from torch import Tensor
from torch.utils.data import Dataset, DataLoader
import pandas as pd
from glob import glob
from os.path import join
import json
from typing import Dict, Union

from audio import load_waveform
from utils import vad_list_to_onehot

from pathlib import Path
from tqdm import tqdm
import pprint as pp
import numpy as np
import csv

import multiprocessing
from multiprocessing import Pool, Manager, Lock

from collections import Counter

import math

from pympler import asizeof

# import dlib
# from video import load_video

DATASETS = ["fisher", "switchboard", "candor", "callhome", "callfriend", "maptask"]




def load_df(path):
    def _vl(x):
        return json.loads(x)
        # if isinstance(x, str):
        #     if len(x) > 0:
        #         return json.loads(x)
        # return x

    def _session(x):
        return str(x)

    converters = {
        "vad_list": _vl,
        "session": _session,
    }
    return pd.read_csv(path, converters=converters)


def combine_dsets(root):
    splits = {}
    for split in ["train", "val", "test"]:
        tmp = []
        for file in glob(join(root, f"{split}*")):
            tmp.append(load_df(file))
        splits[split] = pd.concat(tmp)

    new_name = basename(root)
    splits["train"].to_csv(f"data/{new_name}_train.csv", index=False)
    splits["val"].to_csv(f"data/{new_name}_val.csv", index=False)
    splits["test"].to_csv(f"data/{new_name}_test.csv", index=False)
    print(f"Saved splits to data/{new_name}_[train/val/test].csv")
    

def get_ref_ratio(data, ref_start_sec, ref_end_sec, ref_sample_rate):
    
    
    ref_start_ratio = (ref_start_sec * ref_sample_rate)/data.shape[1]
    ref_end_ratio = (ref_end_sec * ref_sample_rate)/data.shape[1]
    
    # buf = 0.1
    # if ref_end_ratio > 1:
        
    #     # print('################################')
    #     # print('### Ref end ratio over 1.0')
    #     # print('################################')
        
    #     # input()
        
        # new_ref_end_ratio = ref_end_ratio - (1 - ref_end_ratio)
        # ref_end_sec = (ref_end_ratio - 1 - ref_end_ratio) * data.shape[1] / ref_sample_rate
    
    return ref_start_ratio, ref_end_ratio

def get_chunk(src, ref_start_ratio, ref_end_ratio, tgt_size):
    
    start_index = round(src.size(0) * ref_start_ratio)
    end_index = round(src.size(0) * ref_end_ratio)
    
    if (end_index - start_index) < tgt_size:
        # print(f'chunk error: smaller, tried from {start_index} to {end_index}, actual length {end_index - start_index}')
        tgt = src[-tgt_size:]

    elif (end_index - start_index) > tgt_size:
        
        # print(f'chunk error: bigger, tried from {start_index} to {end_index}, length of {end_index - start_index}')
        tgt = src[end_index-tgt_size:end_index]
        
        # print('############################')
        # print('something unexpected happen')
        # print('############################')
        # print(src_audio.shape)
        # print(ref_start_ratio)
        # print(ref_end_ratio)
        # print(start_index)
        # print(end_index)
        # print(tgt_size)
        # import sys
        # sys.exit()
        
    else:
        
        tgt = src[start_index:end_index]
    
    if tgt.size(0) != tgt_size:
        print(f'Final weapon: tgt = src_audio[:, -tgt_size:], {start_index} {end_index} {end_index - start_index}')
        tgt = src[-tgt_size:]
    
    return tgt

def get_audio_chunk(src, ref_start_ratio, ref_end_ratio, tgt_size):
    
    start_index = round(src.size(1) * ref_start_ratio)
    end_index = round(src.size(1) * ref_end_ratio)
    
    if (end_index - start_index) < tgt_size:
        # print(f'chunk error: smaller, tried from {start_index} to {end_index}, actual length {end_index - start_index}')
        tgt = src[:, -tgt_size:]

    elif (end_index - start_index) > tgt_size:
        
        # print(f'chunk error: bigger, tried from {start_index} to {end_index}, length of {end_index - start_index}')
        tgt = src[:, end_index-tgt_size:end_index]
        
        # print('############################')
        # print('something unexpected happen')
        # print('############################')
        # print(src_audio.shape)
        # print(ref_start_ratio)
        # print(ref_end_ratio)
        # print(start_index)
        # print(end_index)
        # print(tgt_size)
        # import sys
        # sys.exit()
        
    else:
        
        tgt = src[:, start_index:end_index]
    
    if tgt.size(1) != tgt_size:
        print(f'Final weapon: tgt = src_audio[:, -tgt_size:], {start_index} {end_index} {end_index - start_index}')
        tgt = src[:, -tgt_size:]
    
    return tgt

def get_tensor_dict_size(src_dict):

    total_size = 0
    for key in src_dict.keys():
        tensor = src_dict[key]
        tensor_size_bytes = tensor.element_size() * tensor.nelement()
        tensor_size_gb = tensor_size_bytes / (1024 * 1024 * 1024)
        total_size += tensor_size_gb
    return total_size

class VapDataset(Dataset):
    def __init__(
        self,
        path,
        horizon: float = 2,
        sample_rate: int = 16_000,
        # sample_rate: int = 8_000,
        frame_hz: int = 50,
        mono: bool = False,
        num_pool: int = os.cpu_count(),
        # num_pool: int = 4,
        use_cache: bool = False,
        multimodal: bool = False,
        use_face_encoder: bool = False,
        exclude_av_cache: bool = False,
        preload_av: bool = False,
        cache_dir: str = 'tmp_cache',
        manager: Manager = None,
        lock: Lock = None,
        # linux: bool = False,
    ):
        self.path = path
        self.df = load_df(path)

        self.sample_rate = sample_rate
        self.frame_hz = frame_hz
        self.horizon = horizon
        self.mono = mono

        self.multimodal = multimodal
        self.use_face_encoder = use_face_encoder

        self.audio_path_conv_cash = {}
        
        self.use_cache = use_cache
        self.exclude_av_cache = exclude_av_cache
        self.preload_av = preload_av
        self.cache_dir = Path(cache_dir)
        
        self.lock = lock
        self.pool_availability = manager.list([True for _ in range(num_pool)])
            
        self.use_multiprocess = True
        print('use_multiprocess:', self.use_multiprocess)
        
        if self.use_cache:

            # self.cache_dir = Path('tmp_cache_6')
            self.cache_lookup = {}
            
            path = Path(path)
            path_name = path.name.replace('_short', '')
            path = path.parent / path_name
            path = str(path)
            
            # if self.multimodal:                
            #     self.cache_csv = self.cache_dir / 'cache_multimodal_{}.csv'.format(Path(path).stem)
            # else:
            #     self.cache_csv = self.cache_dir / 'cache_{}.csv'.format(Path(path).stem)

            self.cache_csv = self.cache_dir / 'cache_multimodal_{}.csv'.format(Path(path).stem)
            
            # create data pairs grouped by id
            self.df = self.df.sort_values(by=['id'])        
            self.df_dict = {}
            for idx in tqdm(range(len(self.df)), desc='Creating data pairs'):
                d = self.df.iloc[idx]
                if not d['id'] in self.df_dict.keys():
                    self.df_dict[d['id']] = [d]
                else:
                    self.df_dict[d['id']].append(d)            

            if not self.cache_dir.exists():
            
                self.cache_dir.mkdir(parents=True)
            
            if not self.cache_csv.exists():
                
                # for idx in tqdm(range(len(self.df)), desc='Creating cache files ({}): '.format(self.cache_csv.name)):
                    
                print('###############################################')
                print('### Creating cache for {} - start'.format(Path(self.path).stem))
                print('###############################################')
                
                try:

                    key_val_list = []
                    keys = manager.list(self.df_dict.keys())
                    print(keys)
                                        
                    ###
                    ### Test run
                    ###
                    
                    # self.write_cache_multimodal(keys[0])
                    # print('Test of write_cache_multimodal(): OK')
                    
                    # input('Ready to start multiprocessing?')
                    
                    # # test run
                    # print()
                    # print('###############')
                    # print('### Test run')
                    # print('###############')
                    # print()
                    # _ = self.write_cache_multimodal(keys[0])
                    
                    keys_iterator = TqdmIterator(keys, self.lock, desc='Creating cache - total progress')
                    
                    if self.use_multiprocess:

                        with Pool(num_pool) as p:
    
                            for tmp_key_val_list in p.imap(self.write_cache_multimodal, keys_iterator):
                                key_val_list.extend(tmp_key_val_list)
                        
                    else:
                        for key in keys_iterator:
                            tmp_key_val_list = self.write_cache_multimodal(key)
                            key_val_list.extend(tmp_key_val_list)
    
                except KeyboardInterrupt:
    
                    p.terminate()
                    p.join()
                    import sys
                    sys.exit()
                    
                # pp.pprint(key_val_list)
                        
                
                for key, val in key_val_list:
                    self.cache_lookup[key] = val
                
                with open(self.cache_csv, 'w', newline='') as f:
                    writer = csv.writer(f)
                    lookup_list = []
                    for key in self.cache_lookup.keys():
                        lookup_list.append([key[0], key[1], key[2], self.cache_lookup[key]])
                    lookup_list = sorted(lookup_list)
                    writer.writerows(lookup_list)
                
                # print('test ->', end='')
                # for idx in range(100):
                #     d = self.df.iloc[idx]
                #     audio_path = str(d["audio_path"])
                #     w = torch.load(self.cache_lookup[(audio_path, 'start:{}'.format(d['start']), 'end:{}'.format(d['end']))])
                # print('ok')

                print('###############################################')
                print('### Creating cache for {} - done'.format(Path(self.path).stem))
                print('###############################################')

            
            self.cache_lookup = {}
            with open(self.cache_csv, 'r') as f:
                reader = csv.reader(f)
                for data in reader:
                    self.cache_lookup[(data[0], data[1], data[2])] = data[3]
            print('cache loaded ({})'.format(Path(self.path).stem))
            
            keys = [x for x in self.cache_lookup.keys() if 'Paris_01-audio_mix' in x[0]]
            
            # pp.pprint(keys)
    
    def preload(self, cache_lookup = None, audio_dict = None, image_dict = None):
        
        if cache_lookup == None:
            cache_lookup = self.cache_lookup

        paths = [key[0] for key in cache_lookup.keys()]
        
        #filter only unique items
        counter = Counter(paths)
        paths = [item for item, count in counter.items()]
        
        if audio_dict == None:

            audio_dict = {}
            tgt_paths = []

            for path in paths:
                if ('audio' in path) and ('wav' in path):
                    tgt_paths.append(path)
    
            for path in tqdm(tgt_paths, desc='Preload AV for {} (audio)'.format(Path(self.path).stem)):
                audio_dict[path], _ = load_waveform(path, sample_rate=self.sample_rate, mono=self.mono,)
        
        else:
            
            print('Preload AV for {} (audio) - reuse'.format(Path(self.path).stem))
        
        if image_dict == None:

            image_dict = {}
            
            if self.use_face_encoder:

                tgt_paths = []
    
                for path in paths:
                    if ('face' in path) and (('.npy' in path) or ('.pt' in path)):
                        tgt_paths.append(path)
        
                for path in tqdm(tgt_paths, desc='Preload AV for {} (image)'.format(Path(self.path).stem)):
                    image_dict[path] = self.load_array_to_tensor(path)
            
            else:
                
                pass
        
        else:
            
            print('Preload AV for {} (image) - reuse'.format(Path(self.path).stem))
            
        self.audio_dict = audio_dict
        self.image_dict = image_dict
        
        dict_size_gb = get_tensor_dict_size(audio_dict)
        print('Preloaded size of audio_dict: {:.2f} GB'.format(dict_size_gb))

        dict_size_gb = get_tensor_dict_size(image_dict)
        print('Preloaded size of image_dict: {:.2f} GB'.format(dict_size_gb))
        
        return audio_dict, image_dict
                    
    def __len__(self):
        
        return len(self.df)

    def __getitem__(self, idx: int) -> Dict[str, Union[Tensor, str, int, float, None]]:        
        
        d = self.df.iloc[idx]
        
        # print(os.uname()[1])
        # print(d["audio_path"])
        audio_path = str(d["audio_path"])

        # Duration can be 19.99999999999997 for some clips and result in wrong vad-shape
        # so we round it to nearest second
        duration = round(d["end"] - d["start"])
        # duration = d["end"] - d["start"]
        
        if self.use_cache:
        
            if self.multimodal:
                
                gaze1 = self.load_csv_from_cache(str(d["gaze_path1"]), d['start'], d['end'])
                head1 = self.load_csv_from_cache(str(d["head_path1"]), d['start'], d['end'])
                face1 = self.load_csv_from_cache(str(d["face_path1"]), d['start'], d['end'])
                body1 = self.load_csv_from_cache(str(d["body_path1"]), d['start'], d['end'])
                
                gaze2 = self.load_csv_from_cache(str(d["gaze_path2"]), d['start'], d['end'])
                head2 = self.load_csv_from_cache(str(d["head_path2"]), d['start'], d['end'])
                face2 = self.load_csv_from_cache(str(d["face_path2"]), d['start'], d['end'])
                body2 = self.load_csv_from_cache(str(d["body_path2"]), d['start'], d['end'])
    
                face_im_path1 = str(d['face_im_path1']).replace('\\', '/')
                face_im_path2 = str(d['face_im_path2']).replace('\\', '/')
                
                if self.exclude_av_cache:

                    if self.preload_av:
                        
                        audio_path = audio_path.replace('\\', '/')
                        ref_start_ratio, ref_end_ratio = get_ref_ratio(self.audio_dict[audio_path], d["start"], d["end"], self.sample_rate)
                        audio = get_audio_chunk(self.audio_dict[audio_path], ref_start_ratio, ref_end_ratio, self.sample_rate * duration)


                        if self.use_face_encoder:                    
    
                            face_im_1 = self.image_dict[face_im_path1]
                            face_im_2 = self.image_dict[face_im_path2]
                            
                            if audio.size(1) != int(self.sample_rate * duration):
                                print('$$$$$$$$$$$$$$$$$$$$$$$$$$$$')
                                print(audio.shape)
                                print(gaze1.shape)
                                print(head1.shape)
                                print(face1.shape)
                                print(body1.shape)
                                print(face_im_1.shape)
                                print(gaze2.shape)
                                print(head2.shape)
                                print(face2.shape)
                                print(body2.shape)
                                print(face_im_2.shape)
                                print('$$$$$$$$$$$$$$$$$$$$$$$$$$$$')
                                                        
                            face_im_1 = get_chunk(face_im_1, ref_start_ratio, ref_end_ratio, round(self.frame_hz * duration))
                            face_im_2 = get_chunk(face_im_2, ref_start_ratio, ref_end_ratio, round(self.frame_hz * duration))
                    
                        else:
                            
                            face_im_1 = torch.Tensor([0])
                            face_im_2 = torch.Tensor([0])

                    else:
                        
                        audio_ref, _ = load_waveform(audio_path, 
                                                     sample_rate=self.sample_rate, 
                                                     mono=self.mono)                        
                        ref_start_ratio, ref_end_ratio = get_ref_ratio(audio_ref, d["start"], d["end"], self.sample_rate)
                        audio, _ = load_waveform(audio_path, 
                                                 sample_rate=self.sample_rate, 
                                                 start_time=d["start"], end_time=d["end"], 
                                                 mono=self.mono)
                        
                        if self.use_face_encoder:
                            
                            face_im_1 = self.load_array_to_tensor(face_im_path1)
                            face_im_2 = self.load_array_to_tensor(face_im_path2)
                            
                            face_im_1 = get_chunk(face_im_1, ref_start_ratio, ref_end_ratio, round(self.frame_hz * duration))
                            face_im_2 = get_chunk(face_im_2, ref_start_ratio, ref_end_ratio, round(self.frame_hz * duration))

                        else:
                            
                            face_im_1 = torch.Tensor([0])
                            face_im_2 = torch.Tensor([0])

                else:
                    
                    audio_path = audio_path.replace('\\', '/')
                    # audio_path = audio_path.replace('/', '\\')
                    audio_cache_path = self.cache_lookup[(audio_path, 'start:{}'.format(d['start']), 'end:{}'.format(d['end']))]
                    audio_cache_path = audio_cache_path.replace('\\', '/')
                    audio = torch.load(audio_cache_path)

                    if self.use_face_encoder:

                        face_im_1 = self.load_tensor_from_cache(str(d['face_im_path1']), d['start'], d['end'])
                        face_im_2 = self.load_tensor_from_cache(str(d['face_im_path2']), d['start'], d['end'])

                
                # print('##########')
                # print(audio.shape)
                # print(gaze1.shape)
                # print(head1.shape)
                # print(face1.shape)
                # print(body1.shape)
                # print(audio.shape)
                # print(gaze2.shape)
                # print(head2.shape)
                # print(face2.shape)
                # print(body2.shape)
                # print('##########')
    
                # print(w.dtype)
                # print(gaze1.dtype)
                # print(head1.dtype)
                # print(face1.dtype)
                # print(body1.dtype)
                   
                vad = vad_list_to_onehot(
                    d["vad_list"], duration=duration + self.horizon, frame_hz=self.frame_hz
                )
                
                gaze1 = self.align_shape(audio, gaze1)
                head1 = self.align_shape(audio, head1)
                face1 = self.align_shape(audio, face1)
                body1 = self.align_shape(audio, body1)

                gaze2 = self.align_shape(audio, gaze2)
                head2 = self.align_shape(audio, head2)
                face2 = self.align_shape(audio, face2)
                body2 = self.align_shape(audio, body2)
                
                # print('#################')
                
                if self.use_face_encoder:

                    # print(face_im_1.shape)
                    
                    face_im_1 = self.align_shape(audio, face_im_1)
                    face_im_2 = self.align_shape(audio, face_im_2)
                
                # print(audio.shape)
                # print(gaze1.shape)
                # print(head1.shape)
                # print(face1.shape)
                # print(body1.shape)
                # print(face_im_1.shape)
                # print(vad.shape)                
                # import sys
                # sys.exit()
                
                # to make sure cuDNN properly works by managing memory layout
                audio = audio.contiguous()
                gaze1 = gaze1.contiguous()
                head1 = head1.contiguous()
                face1 = face1.contiguous()
                body1 = body1.contiguous()
                face_im_1 = face_im_1.contiguous()
                gaze2 = gaze2.contiguous()
                head2 = head2.contiguous()
                face2 = face2.contiguous()
                body2 = body2.contiguous()
                face_im_2 = face_im_2.contiguous()

                # vad = vad.contiguous()
                
                #print('Dataset.__getitem__', idx)

                return {
                    "session": d["session"],
                    "waveform": audio,
                    "gaze1": gaze1,
                    "head1": head1,
                    "face1": face1,
                    "body1": body1,
                    "gaze2": gaze2,
                    "head2": head2,
                    "face2": face2,
                    "body2": body2,
                    "face_im1": face_im_1,
                    "face_im2": face_im_2,
                    "vad": vad,
                    "dataset": d["dataset"],
                }
            
            else:
                
                audio_path = audio_path.replace('\\', '/')

                if self.exclude_av_cache:
                    
                    if self.preload_av:

                        audio_path = audio_path.replace('\\', '/')
                        ref_start_ratio, ref_end_ratio = get_ref_ratio(self.audio_dict[audio_path], d["start"], d["end"], self.sample_rate)
                        audio = get_audio_chunk(self.audio_dict[audio_path], ref_start_ratio, ref_end_ratio, self.sample_rate * duration)
                    
                    else:

                        audio_ref, _ = load_waveform(audio_path, sample_rate=self.sample_rate, mono=self.mono)                        
                        ref_start_ratio, ref_end_ratio = get_ref_ratio(audio_ref, d["start"], d["end"], self.sample_rate)
                        audio, _ = load_waveform(audio_path, 
                                                 sample_rate=self.sample_rate, 
                                                 start_time=d["start"], end_time=d["end"], 
                                                 mono=self.mono)
                
                else:
                    
                    audio = torch.load(self.cache_lookup[(audio_path, 'start:{}'.format(d['start']), 'end:{}'.format(d['end']))])


                vad = vad_list_to_onehot(
                    d["vad_list"], duration=duration + self.horizon, frame_hz=self.frame_hz
                )

                audio = audio.contiguous()
                # vad = vad.contiguous()

                return {
                    "session": d["session"],
                    "waveform": audio,
                    "vad": vad,
                    "dataset": d["dataset"],
                }
            
        else:
            
            audio_path = audio_path.replace('\\', '/')
            
            if self.multimodal:
                
                audio, _ = load_waveform(
                    audio_path,
                    sample_rate=self.sample_rate,
                    # start_time=d["start"],
                    # end_time=d["end"],
                    mono=self.mono,
                )
                
                gaze_path1 = d['gaze_path1'].replace('\\', '/')
                head_path1 = d['head_path1'].replace('\\', '/')
                face_path1 = d['face_path1'].replace('\\', '/')
                body_path1 = d['body_path1'].replace('\\', '/')

                gaze_path2 = d['gaze_path2'].replace('\\', '/')
                head_path2 = d['head_path2'].replace('\\', '/')
                face_path2 = d['face_path2'].replace('\\', '/')
                body_path2 = d['body_path2'].replace('\\', '/')
                
                face_im_path1 = d['face_im_path1'].replace('\\', '/')
                face_im_path2 = d['face_im_path2'].replace('\\', '/')                
                
                #self.load_csv_with_filter(csv_path)
                # gaze1 = torch.from_numpy(pd.read_csv(gaze_path1).filter(regex="^(?!.*confidence).*$", axis=1).values).type(torch.get_default_dtype())
                # head1 = torch.from_numpy(pd.read_csv(head_path1).filter(regex="^(?!.*confidence).*$", axis=1).values).type(torch.get_default_dtype())
                # face1 = torch.from_numpy(pd.read_csv(face_path1).filter(regex="^(?!.*confidence).*$", axis=1).values).type(torch.get_default_dtype())
                # body1 = torch.from_numpy(pd.read_csv(body_path1).filter(regex="^(?!.*confidence).*$", axis=1).values).type(torch.get_default_dtype())
                # gaze2 = torch.from_numpy(pd.read_csv(gaze_path2).filter(regex="^(?!.*confidence).*$", axis=1).values).type(torch.get_default_dtype())
                # head2 = torch.from_numpy(pd.read_csv(head_path2).filter(regex="^(?!.*confidence).*$", axis=1).values).type(torch.get_default_dtype())
                # face2 = torch.from_numpy(pd.read_csv(face_path2).filter(regex="^(?!.*confidence).*$", axis=1).values).type(torch.get_default_dtype())
                # body2 = torch.from_numpy(pd.read_csv(body_path2).filter(regex="^(?!.*confidence).*$", axis=1).values).type(torch.get_default_dtype())
                
                gaze1 = self.load_csv_with_filter(gaze_path1)
                head1 = self.load_csv_with_filter(head_path1)
                face1 = self.load_csv_with_filter(face_path1)
                body1 = self.load_csv_with_filter(body_path1)
                gaze2 = self.load_csv_with_filter(gaze_path2)
                head2 = self.load_csv_with_filter(head_path2)
                face2 = self.load_csv_with_filter(face_path2)
                body2 = self.load_csv_with_filter(body_path2)
                
                face_im1 = self.load_array_to_tensor(face_im_path1)
                face_im2 = self.load_array_to_tensor(face_im_path2)
                
                ref_start_ratio, ref_end_ratio = get_ref_ratio(audio, d["start"], d["end"], self.sample_rate)            
                                
                gaze1 = get_chunk(gaze1, ref_start_ratio, ref_end_ratio, round(self.frame_hz * duration))
                head1 = get_chunk(head1, ref_start_ratio, ref_end_ratio, round(self.frame_hz * duration))
                face1 = get_chunk(face1, ref_start_ratio, ref_end_ratio, round(self.frame_hz * duration))
                body1 = get_chunk(body1, ref_start_ratio, ref_end_ratio, round(self.frame_hz * duration))
                gaze2 = get_chunk(gaze2, ref_start_ratio, ref_end_ratio, round(self.frame_hz * duration))
                head2 = get_chunk(head2, ref_start_ratio, ref_end_ratio, round(self.frame_hz * duration))
                face2 = get_chunk(face2, ref_start_ratio, ref_end_ratio, round(self.frame_hz * duration))
                body2 = get_chunk(body2, ref_start_ratio, ref_end_ratio, round(self.frame_hz * duration))
                
                face_im1 = get_chunk(face_im1, ref_start_ratio, ref_end_ratio, round(self.frame_hz * duration))
                face_im2 = get_chunk(face_im2, ref_start_ratio, ref_end_ratio, round(self.frame_hz * duration))
                
                audio, _ = load_waveform(
                    audio_path,
                    sample_rate=self.sample_rate,
                    start_time=d["start"],
                    end_time=d["end"],
                    mono=self.mono,
                )
                
                
                # image1, _ = load_video(
                #     video_path1,
                #     start_time=d["start"],
                #     end_time=d["end"]
                # )
                # face1 = get_face(image1)
                # body1 = get_body(image1)

                # image2, _ = load_video(
                #     video_path2,
                #     start_time=d["start"],
                #     end_time=d["end"]
                # )
                # face2 = get_face(image2)
                # body2 = get_body(image2)

                
                # print('##########')
                # print(w.shape)
                # print(gaze1.shape)
                # print(head1.shape)
                # print(face1.shape)
                # print(body1.shape)
    
                # print(w.dtype)
                # print(gaze1.dtype)
                # print(head1.dtype)
                # print(face1.dtype)
                # print(body1.dtype)
                   
                vad = vad_list_to_onehot(
                    d["vad_list"], duration=duration + self.horizon, frame_hz=self.frame_hz
                )

                gaze1 = gaze1.contiguous()
                head1 = head1.contiguous()
                face1 = face1.contiguous()
                body1 = body1.contiguous()
                face_im1 = face_im1.contiguous()
                gaze2 = gaze2.contiguous()
                head2 = head2.contiguous()
                face2 = face2.contiguous()
                body2 = body2.contiguous()
                face_im2 = face_im2.contiguous()

                # vad = vad.contiguous()

                return {
                    "session": d["session"],
                    "waveform": audio,
                    "gaze1": gaze1,
                    "head1": head1,
                    "face1": face1,
                    "body1": body1,
                    "gaze2": gaze2,
                    "head2": head2,
                    "face2": face2,
                    "body2": body2,
                    "face_im1": face_im_1,
                    "face_im2": face_im_2,
                    "vad": vad,
                    "dataset": d["dataset"],
                }
            
            else:
                
                audio, _ = load_waveform(
                    audio_path,
                    sample_rate=self.sample_rate,
                    start_time=d["start"],
                    end_time=d["end"],
                    mono=self.mono,
                )
                
                vad = vad_list_to_onehot(
                    d["vad_list"], duration=duration + self.horizon, frame_hz=self.frame_hz
                )

                audio = audio.contiguous()
                # vad = vad.contiguous()

                return {
                    "session": d["session"],
                    "waveform": audio,
                    "vad": vad,
                    "dataset": d["dataset"],
                }

    def write_cache_csv(self, src_path, src_tensor, src_df, ref_tensor, start, end):    
        
        # duration = round(end - start)
        duration = end - start
        
        src_path = src_path.replace('\\', '/')
        
        ref_start_ratio, ref_end_ratio = get_ref_ratio(ref_tensor, start, end, self.sample_rate)
        src_tensor = get_chunk(src_tensor, ref_start_ratio, ref_end_ratio, round(self.frame_hz * duration))
        
        # src_df.values = src_tensor.numpy()
        tgt_df = pd.DataFrame(src_tensor, columns = src_df.columns)
        
        start_str = "{}".format(start)
        start_str = start_str.replace(".", "p")
        end_str = "{}".format(end)
        end_str = end_str.replace(".", "p")

        tgt_path = self.cache_dir / '{}_{}_{}.csv'.format(Path(src_path).stem, start_str, end_str)
        tgt_df.to_csv(tgt_path, index = False)
        
        # print(tgt_path)
        
        # cache_lookup[(src_path, 'start:{}'.format(start), 'end:{}'.format(end))] = tgt_path        
        # return cache_lookup
        
        
        key = (src_path, 'start:{}'.format(start), 'end:{}'.format(end))
        val = tgt_path  
        
        # if str(val).split(".")[-1]=="pt":
        #     print(key, val)
        #     input()

        return (key, val)

    def write_cache_audio(self, audio_path, start, end, exclude_av_cache):
        
        audio_path = audio_path.replace('\\', '/')
        
        start_str = "{}".format(start)
        start_str = start_str.replace(".", "p")
        end_str = "{}".format(end)
        end_str = end_str.replace(".", "p")

        pt_path = self.cache_dir / '{}_{}_{}.pt'.format(Path(audio_path).stem, start_str, end_str)
        
        if not exclude_av_cache:

            audio, _ = load_waveform(
                audio_path,
                sample_rate=self.sample_rate,
                start_time=start,
                end_time=end,
                mono=self.mono,
            )
            torch.save(audio, pt_path)
        
        
        # print(pt_path)

        # cache_lookup[(audio_path, 'start:{}'.format(start), 'end:{}'.format(end))] = pt_path
        # return cache_lookup
        
        key = (audio_path, 'start:{}'.format(start), 'end:{}'.format(end))
        val = pt_path
        return (key, val)

    def write_cache_tensor(self, src_path, src_tensor, ref_tensor, start, end, exclude_av_cache, use_numpy = False):
        
        duration = round(end - start)
        
        src_path = src_path.replace('\\', '/')
            
        ref_start_ratio, ref_end_ratio = get_ref_ratio(ref_tensor, start, end, self.sample_rate)
        src_tensor = get_chunk(src_tensor, ref_start_ratio, ref_end_ratio, round(self.frame_hz * duration))
        
        src_tensor = src_tensor.type(torch.uint8).contiguous()

        start_str = "{}".format(start)
        start_str = start_str.replace(".", "p")
        end_str = "{}".format(end)
        end_str = end_str.replace(".", "p")
        
        if use_numpy:
            #.npy seems smaller than .pt
            tgt_path = self.cache_dir / '{}_{}_{}.npy'.format(Path(src_path).stem, start_str, end_str)
            src_array = src_tensor.detach().numpy()
            if not exclude_av_cache:
                np.save(tgt_path, src_array)
        else:
            tgt_path = self.cache_dir / '{}_{}_{}.pt'.format(Path(src_path).stem, start_str, end_str)        
            if not exclude_av_cache:
                torch.save(src_tensor, tgt_path)
        
        # print(pt_path)

        # cache_lookup[(audio_path, 'start:{}'.format(start), 'end:{}'.format(end))] = pt_path
        # return cache_lookup
        
        key = (str(src_path), 'start:{}'.format(start), 'end:{}'.format(end))
        val = str(tgt_path)
        return (key, val)
    
    def write_cache_multimodal(self, key):
        
        key_val_list = []
        
        # print(key)
        # print(self.df_dict[key])
        
        d = self.df_dict[key][0]
        
        # print(Path(d['audio_path']))
        # print(Path(d['audio_path']).as_posix())
        # print(Path(d['audio_path']).exists())
        
        audio_path = d['audio_path'].replace('\\', '/')
        
        # audio_path = Path(audio_path)
        # print(audio_path)
        
        # input('DDD')
        
        audio_tensor, _ = load_waveform(
            audio_path,
            sample_rate=self.sample_rate,
            mono=self.mono,
        )
        
        gaze_tensor_1, gaze_d_1 = self.load_csv_to_tensor(str(d['gaze_path1']))
        head_tensor_1, head_d_1 = self.load_csv_to_tensor(str(d['head_path1']))
        face_tensor_1, face_d_1 = self.load_csv_to_tensor(str(d['face_path1']))
        body_tensor_1, body_d_1 = self.load_csv_to_tensor(str(d['body_path1']))
        
        gaze_tensor_2, gaze_d_2 = self.load_csv_to_tensor(str(d['gaze_path2']))
        head_tensor_2, head_d_2 = self.load_csv_to_tensor(str(d['head_path2']))
        face_tensor_2, face_d_2 = self.load_csv_to_tensor(str(d['face_path2']))
        body_tensor_2, body_d_2 = self.load_csv_to_tensor(str(d['body_path2']))
        
        face_im_tensor_1 = self.load_array_to_tensor(str(d['face_im_path1']))
        face_im_tensor_2 = self.load_array_to_tensor(str(d['face_im_path2']))
        
        with self.lock:
            tqdm_position = 0
            for i, candidate in enumerate(self.pool_availability):
                if candidate:
                    tqdm_position = i
                    self.pool_availability[tqdm_position] = False
                    break
            pbar = tqdm(total=len(self.df_dict[key]), position=tqdm_position+1, desc='{} - {}'.format(tqdm_position, key), leave=None)
        
        for d in self.df_dict[key]:
        
            # print(key, d['start'], d['end'])
            
            key, val = self.write_cache_audio(str(d["audio_path"]), d["start"], d["end"], self.exclude_av_cache)
            key_val_list.append([key, val])
    
            # key, val = self.write_cache_csv(str(d["gaze_path1"]), str(d["audio_path"]), d["start"], d["end"])
            key, val = self.write_cache_csv(str(d["gaze_path1"]), gaze_tensor_1, gaze_d_1, audio_tensor, d["start"], d["end"])
            key_val_list.append([key, val])
    
            key, val = self.write_cache_csv(str(d["head_path1"]), head_tensor_1, head_d_1, audio_tensor, d["start"], d["end"])
            key_val_list.append([key, val])
     
            key, val = self.write_cache_csv(str(d["face_path1"]), face_tensor_1, face_d_1, audio_tensor, d["start"], d["end"])
            key_val_list.append([key, val])
    
            key, val = self.write_cache_csv(str(d["body_path1"]), body_tensor_1, body_d_1, audio_tensor, d["start"], d["end"])
            key_val_list.append([key, val])

            key, val = self.write_cache_csv(str(d["gaze_path2"]), gaze_tensor_2, gaze_d_2, audio_tensor, d["start"], d["end"])
            key_val_list.append([key, val])

            key, val = self.write_cache_csv(str(d["head_path2"]), head_tensor_2, head_d_2, audio_tensor, d["start"], d["end"])
            key_val_list.append([key, val])

            key, val = self.write_cache_csv(str(d["face_path2"]), face_tensor_2, face_d_2, audio_tensor, d["start"], d["end"])
            key_val_list.append([key, val])

            key, val = self.write_cache_csv(str(d["body_path2"]), body_tensor_2, body_d_2, audio_tensor, d["start"], d["end"])
            key_val_list.append([key, val])
            
            key, val = self.write_cache_tensor(str(d["face_im_path1"]), face_im_tensor_1, audio_tensor, d["start"], d["end"], self.exclude_av_cache, use_numpy = True)
            key_val_list.append([key, val])

            key, val = self.write_cache_tensor(str(d["face_im_path2"]), face_im_tensor_2, audio_tensor, d["start"], d["end"], self.exclude_av_cache, use_numpy = True)
            key_val_list.append([key, val])
            
            with self.lock:
                pbar.update(1)
        
        with self.lock:
            self.pool_availability[tqdm_position] = True
        
        return key_val_list

    def load_csv_to_tensor(self, src_path):
        
        # src_path = src_path.replace('/', '\\')
        src_path = src_path.replace('\\', '/')
        # print(src_path)
        
        src_df = pd.read_csv(src_path).filter(regex="^(?!.*confidence).*$", axis=1)
        src_tensor = torch.from_numpy(src_df.values).type(torch.get_default_dtype())
        
        return src_tensor, src_df
    
    def load_array_to_tensor(self, src_path):
        
        # src_path = src_path.replace('/', '\\')
        src_path = src_path.replace('\\', '/')
        
        src_path = Path(src_path)
        
        if src_path.suffix == '.npy':
            # data = np.load(src_path)
            data = np.load(src_path, mmap_mode='r')
            src_tensor = torch.from_numpy(data)
        elif src_path.suffix == '.pt':
            src_tensor = torch.load(src_path)
        else:
            assert False, '{} should be .npy or .pt'.format(str(src_path))
        
        return src_tensor

    def load_tensor_from_cache(self, path, start, end):
        
        # path = path.replace('/', '\\')
        path = path.replace('\\', '/')

        tensor_path = self.cache_lookup[
            (path, 
             'start:{}'.format(start), 
             'end:{}'.format(end))
            ]
        tensor_path = tensor_path.replace('\\', '/')
        # print(csv_path)
        
        
        if '.pt' in tensor_path:
            # print(path)
            # print(self.cache_lookup[(path, 'start:{}'.format(start), 'end:{}'.format(end))])
            return torch.load(tensor_path).type(torch.get_default_dtype())

        elif '.npy' in tensor_path:
            return torch.from_numpy(np.load(tensor_path)).type(torch.get_default_dtype())
    
    def load_csv_from_cache(self, path, start, end):
        
        # path = path.replace('/', '\\')
        path = path.replace('\\', '/')

        csv_path = self.cache_lookup[
            (path, 
             'start:{}'.format(start), 
             'end:{}'.format(end))
            ]
        csv_path = csv_path.replace('\\', '/')
        # print(csv_path)
        
        # print(path)
        # print(self.cache_lookup[(path, 'start:{}'.format(start), 'end:{}'.format(end))])
        return self.load_csv_with_filter(csv_path)
    
    def load_csv_with_filter(self, csv_path):
        
        # csv_path = csv_path.replace('/', '\\')
        csv_path = csv_path.replace('\\', '/')
        
        data = pd.read_csv(
                csv_path
        ).filter(regex="^(?!.*confidence).*$", axis=1).values

        #print(type(data))
        #pp.pprint(data)

        data = torch.from_numpy(
            data
        ).type(torch.get_default_dtype())
        
        return data
    
    def align_shape(self, ref, src):
        
        # Align length of src sequence along with ref length (caused by sequence index rounding while retrieving chunks)
                
        tgt_length = int(ref.shape[1] / self.sample_rate * self.frame_hz)
        # tgt_length = math.ceil(ref.shape[1] / self.sample_rate * self.frame_hz)
        
        # print('### alignn shape: start ########################')
        # print(ref.shape)
        # print(src.shape)
        # print(tgt_length)
        
        if src.shape[0] < tgt_length:

            #b, len, dim
            src = torch.nn.functional.pad(src, (0, 0, 0, tgt_length - src.shape[0]), value=0)

        elif tgt_length < src.shape[0]:

            src = src[:tgt_length]
        
        # print(src.shape)
        # print('### alignn shape: end ########################')
            
        return src
    
class TqdmIterator:
    
    def __init__(self, src, lock, position = 0, desc = ''):
        
        self.src = src
        self.num = len(src)
        self.current = 0
        
        self.lock = lock

        with self.lock:
            self.pbar = tqdm(total=self.num, position = position, desc = desc, leave=None)

    def __iter__(self):
        
        return self
    
    def __next__(self):
        
        if self.current == self.num:
            raise StopIteration()
        
        output = self.src[self.current]
        
        self.current += 1
        
        with self.lock:
            self.pbar.update(1)
        
        return output
        
if __name__ == "__main__":
    from os.path import basename

    # root = "data/sliding_window_ad20_ov1_ho2"
    # splits = combine_dsets(root)

    dset = VapDataset(path="data/sliding_window_ad20_ov1_ho2_val.csv")

    d = dset[0]

    dloader = DataLoader(dset, batch_size=4, num_workers=4, shuffle=True)

    batch = next(iter(dloader))
