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

from sklearn.model_selection import train_test_split
from pydub import AudioSegment
import torchaudio
import shutil

def main():
    
    src_dir = Path(r'noxi_orig')
    tgt_dir = Path(r'noxi')
    
    chunk_duration = 20.0 #default
    # chunk_duration = 0.5
    # chunk_duration = 2.0
    
    horizon = 2.0
    stride = 1.0
    
    train_size = 0.7
    valid_size = 0.1
    test_size = 0.2
    
    files = [x for x in src_dir.glob('**/*') if x.is_file()]
    # files = [x for x in src_dir.glob('**/*') if x.is_dir()]
    
    files = [x for x in files if '.gitkeep' not in str(x)]
    
    # pp.pprint(files)

    data_list = [
        ['id', 'start', 'end', 
         'audio_path', 
         # 'face_path1', 'gaze_path1', 'head_path1', 'pose_path1', 
         # 'face_path2', 'gaze_path2', 'head_path2', 'pose_path2', 
         'face_path1', 'gaze_path1', 'head_path1', 'body_path1', 
         'face_path2', 'gaze_path2', 'head_path2', 'body_path2', 
         'vad_list', 
         'session', 'dataset']
    ]
    
    data_dict = {}
    
    for file in tqdm(files, desc = 'Collecting data src'):
        # print(file.absolute())
        separated = str(file.absolute()).split(os.sep)
        file_name = separated[-1]
        file_id = separated[-2]
        # print(file_id, file_name)
        
        # input()
        if not (file_id in data_dict.keys()):
            
            data_dict[file_id] = {}
            data_dict[file_id]['session'] = 0
            data_dict[file_id]['dataset'] = 'noxi'            

        # if 'mix' in file_name:
        #     tgt_path = tgt_dir / '{}-{}'.format(file_id, file_name)
        #     data_dict[file_id]['audio_path'] = str(tgt_path.absolute().relative_to(Path(os.getcwd())))
            
        #     if not tgt_path.exists():
        #         shutil.copy(file.absolute(), tgt_path.absolute())

        if 'audio' in file_name:
            # print(file_name)

            # if 'expert' in file_name:
            #     input()
            
            if 'novice' in file_name:
                
                tgt_path = tgt_dir / '{}-{}'.format(file_id, file_name)
                # print(tgt_path)
                data_dict[file_id]['audio_path1'] = str(tgt_path.absolute().relative_to(Path(os.getcwd())))

                if not tgt_path.exists():
                    shutil.copy(file.absolute(), tgt_path.absolute())
                
            if 'expert' in file_name:
                
                tgt_path = tgt_dir / '{}-{}'.format(file_id, file_name)
                # print(tgt_path)
                data_dict[file_id]['audio_path2'] = str(tgt_path.absolute().relative_to(Path(os.getcwd())))
                # print(2)

                if not tgt_path.exists():
                    shutil.copy(file.absolute(), tgt_path.absolute())
            
            if 'mix' in file_name:
                
                # print(3)
                continue
            
            tmp_file_name = file_name
            tmp_file_name = tmp_file_name.replace('novice', 'mix')
            tmp_file_name = tmp_file_name.replace('expert', 'mix')
            tgt_path = tgt_dir / '{}-{}'.format(file_id, tmp_file_name)
            
            # print(data_dict[file_id].keys())
            # input()
            # input()
            
            if (('audio_path1' in data_dict[file_id].keys()) and ('audio_path2' in data_dict[file_id].keys())):

                data_dict[file_id]['audio_path'] = str(tgt_path.absolute().relative_to(Path(os.getcwd())))

                if not tgt_path.exists():
            
                    # shutil.copy(file.absolute(), tgt_path.absolute())
                    wav1 = AudioSegment.from_wav(data_dict[file_id]['audio_path1'])
                    wav2 = AudioSegment.from_wav(data_dict[file_id]['audio_path2'])
                    
                    # print(len(wav1))
                    # print(len(wav2))
                    
                    wav1 = wav1[:min(len(wav1), len(wav2))]
                    wav2 = wav2[:min(len(wav1), len(wav2))]
                    
                    wav_mix = AudioSegment.from_mono_audiosegments(wav1, wav2)
                    wav_mix.export(tgt_path, format='wav')
                


        elif 'non_varbal' in file_name:
            
            face_data, gaze_data, head_data, body_data = load_nonverbal_data(file.absolute())
            
            if 'novice' in file_name:
                
                tgt_path = tgt_dir / '{}-face_{}'.format(file_id, file_name.split('_')[-1])
                data_dict[file_id]['face_path1'] = str(tgt_path.absolute().relative_to(Path(os.getcwd())))
                if not tgt_path.exists():
                    write_csv(tgt_path, face_data)

                tgt_path = tgt_dir / '{}-gaze_{}'.format(file_id, file_name.split('_')[-1])
                data_dict[file_id]['gaze_path1'] = str(tgt_path.absolute().relative_to(Path(os.getcwd())))
                if not tgt_path.exists():
                    write_csv(tgt_path, gaze_data)

                tgt_path = tgt_dir / '{}-head_{}'.format(file_id, file_name.split('_')[-1])
                data_dict[file_id]['head_path1'] = str(tgt_path.absolute().relative_to(Path(os.getcwd())))
                if not tgt_path.exists():
                    write_csv(tgt_path, head_data)

                tgt_path = tgt_dir / '{}-body_{}'.format(file_id, file_name.split('_')[-1])
                data_dict[file_id]['body_path1'] = str(tgt_path.absolute().relative_to(Path(os.getcwd())))
                if not tgt_path.exists():
                    write_csv(tgt_path, body_data)
            
            else:
                
                tgt_path = tgt_dir / '{}-face_{}'.format(file_id, file_name.split('_')[-1])
                data_dict[file_id]['face_path2'] = str(tgt_path.absolute().relative_to(Path(os.getcwd())))
                if not tgt_path.exists():
                    write_csv(tgt_path, face_data)

                tgt_path = tgt_dir / '{}-gaze_{}'.format(file_id, file_name.split('_')[-1])
                data_dict[file_id]['gaze_path2'] = str(tgt_path.absolute().relative_to(Path(os.getcwd())))
                if not tgt_path.exists():
                    write_csv(tgt_path, gaze_data)

                tgt_path = tgt_dir / '{}-head_{}'.format(file_id, file_name.split('_')[-1])
                data_dict[file_id]['head_path2'] = str(tgt_path.absolute().relative_to(Path(os.getcwd())))
                if not tgt_path.exists():
                    write_csv(tgt_path, head_data)

                tgt_path = tgt_dir / '{}-body_{}'.format(file_id, file_name.split('_')[-1])
                data_dict[file_id]['body_path2'] = str(tgt_path.absolute().relative_to(Path(os.getcwd())))
                if not tgt_path.exists():
                    write_csv(tgt_path, body_data)

        
        elif 'vad' in file_name:
            
            start_int, end_int, vad_list = get_vad_data(file)
            
            data_dict[file_id]['start'] = start_int
            data_dict[file_id]['end'] = end_int
            
            if 'vad_list' in data_dict[file_id].keys():
                data_dict[file_id]['vad_list'][0].extend(vad_list[0])
                data_dict[file_id]['vad_list'][1].extend(vad_list[1])
                
                # print(file_id, file_name)
                # print(len(data_dict[file_id]['vad_list'][0]))
                # print(len(data_dict[file_id]['vad_list'][1]))
                # input()
                
            else:
                data_dict[file_id]['vad_list'] = vad_list
    
    for file_id in tqdm(data_dict.keys(), desc='Creating data split'):
        
        x, sr = torchaudio.load(data_dict[file_id]['audio_path'])
        # print(np.shape(x))
        # print(sr)
        audio_duration = x.shape[1]/sr
        
        tmp_time = 0
        
        while True:
            
            
            tmp_data_dict = {}
            
            if (tmp_time + chunk_duration + horizon) < audio_duration:
                
                # print('{:.2f} to {:.2f} (audio_duration: {:.2f})'.format(tmp_time, tmp_time + chunk_duration, audio_duration))

                tmp_data_dict               = data_dict[file_id].copy()
                tmp_data_dict['start']      = tmp_time
                tmp_data_dict['end']        = tmp_time + chunk_duration
                
                tmp_data_dict['vad_list']   = get_partial_vad(data_dict[file_id]['vad_list'], tmp_time, tmp_time + chunk_duration + horizon)
                    
                data_list.append([file_id, #0
                                  tmp_data_dict['start'],
                                  tmp_data_dict['end'],
                                  tmp_data_dict['audio_path'],
                                  tmp_data_dict['face_path1'],
                                  tmp_data_dict['gaze_path1'],#5
                                  tmp_data_dict['head_path1'],
                                  tmp_data_dict['body_path1'],
                                  tmp_data_dict['face_path2'],
                                  tmp_data_dict['gaze_path2'],
                                  tmp_data_dict['head_path2'],#10
                                  tmp_data_dict['body_path2'],
                                  tmp_data_dict['vad_list'],#12
                                  tmp_data_dict['session'],
                                  tmp_data_dict['dataset']
                                  ])
                
                tmp_time += stride

            else:
                
                # input()
                break

    data_columns = data_list[0]
    data_list = data_list[1:]

    train_data, valid_test_data = train_test_split(data_list, train_size = train_size, test_size = valid_size + test_size)
    valid_data, test_data = train_test_split(valid_test_data, train_size = (valid_size / (valid_size + test_size)), test_size = (test_size / (valid_size + test_size)))
    
    train_data.insert(0, data_columns)
    valid_data.insert(0, data_columns)
    test_data.insert(0, data_columns)
    
    if chunk_duration != 20.0:

        train_name = 'train_{:.2f}'.format(chunk_duration).replace('.', 'p') + '.csv'
        train_path = tgt_dir / train_name
        
        valid_name = 'valid_{:.2f}'.format(chunk_duration).replace('.', 'p') + '.csv'
        valid_path = tgt_dir / valid_name
        
        test_name = 'test_{:.2f}'.format(chunk_duration).replace('.', 'p') + '.csv'
        test_path = tgt_dir / test_name

        # print(train_path)
        # print(valid_path)
        # print(test_path)
        # input('ok?: ')

        write_csv(train_path, train_data)
        write_csv(valid_path, valid_data)
        write_csv(test_path, test_data)
    
    else:
        
        train_path = tgt_dir / 'train.csv'
        valid_path = tgt_dir / 'valid.csv'
        test_path = tgt_dir / 'test.csv'

        # print(train_path)
        # print(valid_path)
        # print(test_path)
        # input('ok?: ')
    
        write_csv(train_path, train_data)    
        write_csv(valid_path, valid_data)    
        write_csv(test_path, test_data)
    
    
    print('###################################')
    print('Inspect')
    print('###################################')
    
    pp.pprint(data_list[:3])
    
    for x in data_list[:50]:
        pp.pprint(x[12])
    
    print('total_size', len(data_list)-1)
    print('train_size: {:} ({:.2f})'.format(len(train_data)-1, (len(train_data)-1)/(len(data_list)-1)))
    print('valid_size: {:} ({:.2f})'.format(len(valid_data)-1, (len(valid_data)-1)/(len(data_list)-1)))
    print('test_size: {:} ({:.2f})'.format(len(test_data)-1, (len(test_data)-1)/(len(data_list)-1)))
    

def get_partial_vad(src_list, start_time, end_time):
    
    pass

    tgt_list = [[], []]
    
    for ch in range(2):
        for row in src_list[ch]:
            
            if (((start_time < row[0]) and (row[0] < end_time)) 
                or ((start_time < row[1]) and (row[1] < end_time))):
            
                if (row[0] < start_time):
                    tmp_start_time = start_time
                elif (start_time <= row[0]):
                    tmp_start_time = row[0]
                
                if (row[1] < end_time):
                    tmp_end_time = row[1]
                elif (end_time <= row[1]):
                    tmp_end_time = end_time
                
                tgt_list[ch].append([tmp_start_time, tmp_end_time])
    
    # pp.pprint(tgt_list)
    # input()
    
    return tgt_list
            
            
            

def get_vad_data(src_path):
    
    vad_list = load_vad_csv(src_path, ' ')
    
    # return 0, 0, [0,0,0]
    return 0, 0, vad_list

def load_vad_csv(src_path, delimiter = ','):
    
    src_data = load_csv(src_path, delimiter)
    
    tgt_data = [[], []]
    for row in src_data:
        if row[-1] == '[speech]':
           if 'novice' in str(src_path.absolute()):
               tgt_data[0].append([float(row[1]), float(row[2])])
           else:
               tgt_data[1].append([float(row[1]), float(row[2])])
               
    # print(src_path)
    # print(np.shape(tgt_data[0]))
    # print(np.shape(tgt_data[1]))
    # input()
    
    return tgt_data

def load_csv(src_path, delimiter = ','):
    
    with open(src_path, 'r') as f:
        reader = csv.reader(f, delimiter = delimiter)
        src_data = [x for x in reader]
    
    return src_data

def write_csv(tgt_path, tgt_data):
    
    with open(tgt_path, 'w', newline='') as f:
        writer = csv.writer(f)
        writer.writerows(tgt_data)

def load_nonverbal_data(src_path):
    
    src_data = load_csv(src_path)
    
    # print(src_data[0])
    # for i in range(len(src_data[0])):
    #     print(i, src_data[0][i])
    # sys.exit()
    
    gaze_data = [['gaze_x', 'gaze_y']]
    face_data = [['AU01', 'AU02', 'AU04', 'AU05', 'AU06', 'AU07', 'AU09', 'AU10', 'AU12', 'AU14', 'AU15', 'AU17', 'AU20', 'AU23', 'AU25', 'AU26', 'AU45']]
    head_data = [['head_x', 'head_y', 'head_z']]
    body_data = [['pose_1_x', 'pose_1_y', 'pose_1_confidence', 'pose_2_x', 'pose_2_y', 'pose_2_confidence', 'pose_3_x', 'pose_3_y', 'pose_3_confidence', 'pose_4_x', 'pose_4_y', 'pose_4_confidence', 'pose_5_x', 'pose_5_y', 'pose_5_confidence', 'pose_6_x', 'pose_6_y', 'pose_6_confidence', 'pose_7_x', 'pose_7_y', 'pose_7_confidence']]

    for row in src_data[1:]:
        
        gaze_data.append(row[1:4])
        face_data.append(row[4:21])
        head_data.append(row[21:24])
        body_data.append(row[24:])
        # pass
    
    return face_data, gaze_data, head_data, body_data

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