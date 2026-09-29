# -*- coding: utf-8 -*-
"""

pyfeat:
    https://py-feat.org/basic_tutorials/01_basics.html#working-with-multiple-images
    https://github.com/cosanlab/py-feat/blob/main/feat/detector.py
    
dlib python example:
    https://github.com/davisking/dlib/tree/master/python_examples
    
dlib with cuda on conda:
    https://anaconda.org/zeroae/dlib-cuda

"""

# from mediapipe import solutions
# from mediapipe.framework.formats import landmark_pb2
import numpy as np

# import cv2

# import mediapipe as mp
# from mediapipe.tasks import python
# from mediapipe.tasks.python import vision
# VisionRunningMode = mp.tasks.vision.RunningMode

# from feat import Detector
# from feat.utils import set_torch_device
# from feat.utils.io import video_to_tensor

import cv2
import sys
import math
import time
import copy
# import torch
import pprint as pp
from tqdm import tqdm
from pathlib import Path
# import moviepy.editor.VideoFileClip
from moviepy.editor import VideoFileClip

# batch_size = None        
# # batch_size = 128

# if batch_size != None:
#     from torch.utils.data import DataLoader
#     from feat.data import TensorDataset
#     from feat.utils.image_operations import compute_original_image_size

# import pandas as pd
from multiprocessing import Pool, Manager, Lock

# import torchvision.io as io
# from torchvision import transforms

import dlib
# print(dlib.DLIB_USE_CUDA)

import os
os.environ["OPENCV_FFMPEG_READ_ATTEMPTS"] = "8192"

# print(1)

# import math

MARGIN = 10  # pixels
FONT_SIZE = 1
FONT_THICKNESS = 1
HANDEDNESS_TEXT_COLOR = (88, 205, 54) # vibrant green

def main(manager, lock):

    # STEP 3: Load the input image.
    # image = mp.Image.create_from_file("image_hand.jpg")
    # image = mp.Image.create_from_file("image_body.jpg")
    
    # cv_image = cv2.imread('image_body.jpg')
    # cv_image = cv2.cvtColor(cv_image, cv2.COLOR_BGR2RGB)
    # image = mp.Image(image_format=mp.ImageFormat.SRGB, data=cv_image)
    
    # Check available cameras
    # index = 0
    # arr = []
    # while True:
    #     cap = cv2.VideoCapture(index)
    #     if not cap.read()[0]:
    #         break
    #     else:
    #         arr.append(index)
    #     cap.release()
    #     index += 1    
    # print("available camera:", arr)
    
    # set_torch_device(device='cuda')
    
    detector_path = "mmod_human_face_detector.dat"
    sp_path = 'shape_predictor_5_face_landmarks.dat'
        
    tgt_size = (112, 112)    
    # tgt_fps = 30
    tgt_fps = None
    tmp_dir = None

    # src_dir = Path(r'../src')
    # tgt_dir = Path(r'../face')
    # src_dir = Path(r'../src_paris')
    
    # src_dir = Path(r'../src_paris_6')
    # tgt_dir = Path(r'../face_paris')
    
    src_dir = Path(r'src_paris')
    tgt_dir = Path(r'../noxi')
    
    # use_multiprocess = False
    use_multiprocess = True
    num_process = 10
    
    if tgt_fps != None:
        
        tmp_dir = Path("{}_{}".format(str(src_dir), tgt_fps))
        if tmp_dir.exists():
            print(f'{str(tmp_dir)} exists')
            sys.exit()
        else:
            tmp_dir.mkdir()
    
    # num_workers = 0
    
    # pin_memory = True
    
    # num_process = 10
    
    # resize = transforms.Resize(tgt_size)
    
    # detector = dlib.get_frontal_face_detector()

    # Load the image using Dlib
    # img = dlib.load_rgb_image(face_file_path)
    
    # cap = cv2.VideoCapture()
    # ret, frame = cap.read()

    # # Ask the detector to find the bounding boxes of each face. The 1 in the
    # # second argument indicates that we should upsample the image 1 time. This
    # # will make everything bigger and allow us to detect more faces.
    # dets = detector(frame, 1)

    # num_faces = len(dets)
    # if num_faces == 0:
    #     print('no face found')

    # # Find the 5 face landmarks we need to do the alignment.
    # faces = dlib.full_object_detections()
    # for detection in dets:
    #     faces.append(sp(frame, detection))

    # # Get the aligned face images
    # # Optionally: 
    # # images = dlib.get_face_chips(img, faces, size=160, padding=0.25)
    # images = dlib.get_face_chips(frame, faces, size=320)
    
    # cap = cv2.VideoCapture(0)
    # # cap = cv2.VideoCapture(1)
    
    # cap.set(3, 640)
    # cap.set(4, 480)
    
    # cap.set(3, 320)
    # cap.set(4, 240)
    
    # cap.set(3, 160)
    # cap.set(4, 120)
    
    # input()

    # print(1)

    src_paths = list(src_dir.rglob('*.mp4'))
    
    # pp.pprint(src_paths)
    
    # pbar_list = [tqdm(total=len(src_paths), desc='Processed video files'), None]
    
    available_positions = manager.list([True for _ in range(num_process)])
    shared_list = manager.list([detector_path, sp_path, src_paths, tgt_dir, tgt_fps, tgt_size, tmp_dir, 
                                lock, available_positions])    
    extractor = Extractor(shared_list)
    
    index_list = list(range(len(src_paths)))
        
    if use_multiprocess:
        
        tqdm_iterator = TqdmIterator(index_list, position=0, 
                                     desc='{:02d}: Total progress'.format(0), shared_list=shared_list)
        
        print(f'use multiprocessing with {num_process} processes')
        with Pool(processes=num_process) as p:
            _ = p.imap(extractor.extract_face_from_video, tqdm_iterator)
            _ = list(_)
            # print(list(_))

    else:
        for i in tqdm(index_list, desc='Total progress'):
            
            print('Cooling down ...', end='')
            #cooldown to make sure opencv VideoCapture has been closed
            time.sleep(3)
            print('ok')
            
            extractor.extract_face_from_video(i)
        

class TqdmIterator:
    
    def __init__(self, src, position = 0, desc = '', shared_list = None):
        
        self.num = len(src)
        self.current = 0
        
        self.pbar = tqdm(total=self.num, position = position, desc = desc, leave=None)
        
        # self.lock = lock
        
        self.shared_list = shared_list
    
    def __iter__(self):
        
        return self
    
    def __next__(self):
        
        if self.current == self.num:
            raise StopIteration()
            
        ret = self.current
        self.current += 1
        
        with self.shared_list[7]:
            self.pbar.update(1)
        
        return ret
        
class Extractor:
    
    def __init__(self, shared_list):
        
        self.detector_path   = shared_list[0]
        self.sp_path         = shared_list[1]
        self.src_paths  = shared_list[2]
        self.tgt_dir    = shared_list[3]
        self.tgt_fps    = shared_list[4]
        self.tgt_size   = shared_list[5]
        
        self.tmp_dir    = shared_list[6]
        
        self.lock = shared_list[7]
        
        self.available_positions = shared_list[8]
        
        self.shared_list = shared_list
    
    
    def extract_face_from_video(self, i):
        
        self.detector = dlib.cnn_face_detector = dlib.cnn_face_detection_model_v1(self.detector_path)
        self.sp = dlib.shape_predictor(self.sp_path)
    
        src_path = self.src_paths[i]
        tgt_path = self.tgt_dir / (src_path.stem + '.npy')
        
        tqdm_position = i
        with self.lock:
            for position_candidate, available in enumerate(self.shared_list[8]):
                if available:
                    tqdm_position = position_candidate
                    self.shared_list[-1][tqdm_position] = False
                    # print('position {} for {}'.format(tqdm_position, i))
                    break
        # self.shared_list[7].release()

        # print(str(src_path.absolute()))
        # print(tgt_path)
    
        # sys.exit()
    
        cap = cv2.VideoCapture(str(src_path.absolute()))
                
        fps = int(cap.get(cv2.CAP_PROP_FPS))
        # print('fps', fps)
        
        if (self.tgt_fps != None) and (fps != self.tgt_fps):
            # cap.close()            
            clip = VideoFileClip(str(src_path.absolute()))
            src_path = self.tmp_dir / src_path.name
            clip.write_videofile(str(src_path.absolute()), fps=self.tgt_fps)
            cap = cv2.VideoCapture(str(src_path.absolute()))
            
        
        num_frames = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))
        # print('num_frames', num_frames)         
                
        # sys.eixt()
        
        no_face_cnt = 0
        processed_frames = []
        
        pbar = tqdm(total = num_frames, position=tqdm_position+1, 
                      desc='Row {:02d} - index {:02d}: Processing {}'.format(tqdm_position+1, i, src_path.name),
                      leave=None)
        
        for j in range(num_frames):
            
            with self.lock:
                pbar.update(1)
            
            ret, frame = cap.read()
            
            if not ret:
                continue
    
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
                
                processed_frames.append(face_frame)
                
            else:
                no_face_cnt += 1
                # print()
                # print('no face found (no_face_cnt:{})'.format(no_face_cnt))
                # print()
                face_frame = np.zeros((self.tgt_size[0], self.tgt_size[1], 3))
                processed_frames.append(face_frame)
    
            # if j == 9:
            #     # print('break for debug')
            #     time.sleep(3)
            #     break
            
            if j == (num_frames-1):
                time.sleep(3)
                
        
            # time.sleep(0.01)
        
        processed_frames = np.stack(processed_frames).astype(np.uint8)
        
        cap.release()
        
        
        
        np.save(str(tgt_path), processed_frames)
        
        data = np.load(str(tgt_path))
    
        # print()
        # print('Completed {}'.format(src_path.name), np.shape(data))
        # print()
        
        # self.shared_list[7].acquire()
        with self.lock:
            self.shared_list[-1][tqdm_position] = True
        # self.shared_list[7].release()
        
        return None

if __name__ == '__main__':
    manager = Manager()
    lock = manager.Lock()

    main(manager, lock)