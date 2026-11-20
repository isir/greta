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

def main():
    
    
    keyword = 'Paris'
    # keyword = 'Augsburg'
    # keyword = 'Nottingham'
    
    train_base = 'train'
    valid_base = 'valid'
    test_base = 'test'

    # train_base = 'train_2p00'
    # valid_base = 'valid_2p00'
    # test_base = 'test_2p00'

    src_train_path = 'noxi/{}.csv'.format(train_base)
    src_valid_path = 'noxi/{}.csv'.format(valid_base)
    src_test_path = 'noxi/{}.csv'.format(test_base)

    tgt_train_path = 'noxi/{}_{}.csv'.format(train_base, keyword.lower())
    tgt_valid_path = 'noxi/{}_{}.csv'.format(valid_base, keyword.lower())
    tgt_test_path = 'noxi/{}_{}.csv'.format(test_base, keyword.lower())
    
    # print(tgt_train_path)
    # print(tgt_valid_path)
    # print(tgt_test_path)
    # input('ok?: ')
    
    train_size = 0.8
    valid_size = 0.1
    test_size = 0.1    
    
    data = load_csv(src_train_path)
    # pp.pprint(data[:2])
    
    data.extend(load_csv(src_valid_path)[1:])
    data.extend(load_csv(src_test_path)[1:])
    
    print(np.shape(data))
    
    data_columns = data[0]
    data_list = data[1:]
    
    data_list = filter_by_keyword(data_list, keyword)
    
    if len(data_list) == 0:
        print('No keyword matched')
        sys.exit()
    
    train_data, valid_test_data = train_test_split(data_list, train_size = train_size, test_size = valid_size + test_size)
    valid_data, test_data = train_test_split(valid_test_data, 
                                             train_size = valid_size / (valid_size + test_size), 
                                             test_size = test_size / (valid_size + test_size))
    
    train_data.insert(0, data_columns)
    valid_data.insert(0, data_columns)
    test_data.insert(0, data_columns)
    
    write_csv(tgt_train_path, train_data)
    write_csv(tgt_valid_path, valid_data)
    write_csv(tgt_test_path, test_data)
    

def filter_by_keyword(src, keyword):
    
    tgt = []
    
    for row in tqdm(src, desc=f'Filtering by {keyword}'):
        
        if keyword in row[0]:
            
            tgt.append(row)
    
    return tgt

def load_csv(src_path, delimiter = ','):
    
    with open(src_path, 'r') as f:
        reader = csv.reader(f, delimiter = delimiter)
        src_data = [x for x in reader]
    
    return src_data

def write_csv(tgt_path, tgt_data):
    
    with open(tgt_path, 'w', newline='') as f:
        writer = csv.writer(f)
        writer.writerows(tgt_data)

if __name__ == '__main__':
    main()