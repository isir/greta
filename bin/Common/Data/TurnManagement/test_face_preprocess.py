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

import sys
import math
import time
import copy
import pprint as pp
from tqdm import tqdm

import numpy as np
import dlib
import cv2

import os
os.environ["OPENCV_FFMPEG_READ_ATTEMPTS"] = "8192"

# print(1)

# import math

# MARGIN = 10  # pixels
# FONT_SIZE = 1
# FONT_THICKNESS = 1
# HANDEDNESS_TEXT_COLOR = (88, 205, 54) # vibrant green

def main():
    
    detector_path = "dlib_models/mmod_human_face_detector.dat"
    sp_path = 'dlib_models/shape_predictor_5_face_landmarks.dat'
        
    tgt_size = (112, 112)    
    
    fps_list = []
    
    face_cripper = Face_cripper(detector_path, sp_path, tgt_size)

    ###########################
    ### Camera usage
    ###########################

    # cap = cv2.VideoCapture(0)
    # while cap.isOpened():

    #     ret, frame = cap.read()
        
    #     if not ret:
    #         continue
        
    #     s_time = time.time()

    #     face_frame = face_cripper.crip(frame)

    #     cv2.imshow('Webcam Live', face_frame)

    #     # 'q'キーが押されたらループから抜ける
    #     if cv2.waitKey(1) & 0xFF == ord('q'):
    #         break        
        
    #     e_time = time.time()
        
    #     tmp_fps = 1/(e_time - s_time)
    #     fps_list.append(tmp_fps)
    #     print('fps: {:10.2f}, (ave: {:10.2f})'.format(tmp_fps, np.average(fps_list)))
        
    #     #average: 45fps
    
    # cap.release()

    ###########################
    ### Image usage
    ###########################
    
    image = cv2.imread("E:/2024-2026_ISIR/NetBeansProjects/greta-github-dev/greta/bin/Capture_0.png")
    
    face_image = face_cripper.crip(image)

    cv2.imshow("Image", face_image)

    # Wait for the user to press a key
    cv2.waitKey(0)
    
    # Close all windows
    cv2.destroyAllWindows()
    

class Face_cripper:
    
    def __init__(self, detector_path, sp_path, tgt_size):

        self.detector = dlib.cnn_face_detector = dlib.cnn_face_detection_model_v1(detector_path)
        self.sp = dlib.shape_predictor(sp_path)
        self.tgt_size = tgt_size
    
    def crip(self, frame):
        
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
            # print()
            # print('no face found (no_face_cnt:{})'.format(no_face_cnt))
            # print()
            face_frame = np.zeros((self.tgt_size[0], self.tgt_size[1], 3))
        
        return face_frame


if __name__ == '__main__':
    main()