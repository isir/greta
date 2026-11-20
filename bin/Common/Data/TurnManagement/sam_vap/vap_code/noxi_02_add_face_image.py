# -*- coding: utf-8 -*-
"""

"""

from pathlib import Path
from tqdm import tqdm
import pprint as pp
import numpy as np
import time
import csv
import sys
import os

# from sklearn.model_selection import train_test_split
# from pydub import AudioSegment
# import torchaudio

import shutil

def main():
    
    ###
    ### requred to send face image sequence (.npy) to noxi directory
    ###
    
    src_tgt_pairs = [['noxi/train_paris.csv', 'noxi/train_paris_ext.csv'],
                     ['noxi/valid_paris.csv', 'noxi/valid_paris_ext.csv'],
                     ['noxi/test_paris.csv', 'noxi/test_paris_ext.csv']
                     ]

    # WRITE = False
    WRITE = True
    
    for src_path, tgt_path in src_tgt_pairs:
    
        
        src_data = load_csv(src_path)
        
        src_columns = src_data[0]
        src_list = src_data[1:]
        
        # for i in range(len(src_columns)):
        #     print('{:2d}, {}'.format(i, src_columns[i]))
        # pp.pprint(src_list[0])
        
        tgt_list = []
        tgt_columns = src_columns.copy()
        tgt_columns.insert(8, 'face_im_path1')
        tgt_columns.insert(13, 'face_im_path2')
        
        # for i in range(len(tgt_columns)):
        #     print('{:2d}, {}'.format(i, tgt_columns[i]))
            
        for row in src_list:
            
            tmp_src_path = row[4]
            tmp_tgt_path = tmp_src_path.replace('csv', 'npy')
            row.insert(8, tmp_tgt_path)
            # print(tmp_src_path)
            # print(tmp_tgt_path)
            
            tmp_src_path = row[9]
            tmp_tgt_path = tmp_src_path.replace('csv', 'npy')
            row.insert(13, tmp_tgt_path)
            # print(tmp_src_path)
            # print(tmp_tgt_path)
            
            tgt_list.append(row)
            
            # input()
            
        tgt_list.insert(0, tgt_columns)
        
        # pp.pprint(tgt_list[0])
        # print(np.shape(tgt_list))
        
        if WRITE:
            write_csv(tgt_path, tgt_list)


def load_csv(src_path, delimiter = ','):
    
    with open(src_path, 'r') as f:
        reader = csv.reader(f, delimiter = delimiter)
        src_data = [x for x in reader]
    
    return src_data

def write_csv(tgt_path, tgt_data):
    
    with open(tgt_path, 'w', newline='') as f:
        writer = csv.writer(f)
        writer.writerows(tgt_data)
    
    print('complete to write:', tgt_path)

if __name__ == '__main__':
    main()
    
"""
[
 [
  [3.77, 4.51], [5.25, 7.84], [8.46, 14.1], [16.73, 17.39], [17.72, 20.33]
 ], 
 [
  [13.16, 13.4], [13.98, 14.13], [14.56, 17.74], [20.73, 22.0]                                                                              
 ]
]
"""
