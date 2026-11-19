# -*- coding: utf-8 -*-
"""
Created on Mon Mar 24 10:50:30 2025

@author: takes
"""

from pathlib import Path
from tqdm import tqdm
import pprint as pp
import time
import csv
import sys
import os

from finetune_vapNotFreeze import VAPModel, DataConfig, OptConfig, get_run_name
from evaluation import get_args
from utils import everything_deterministic, write_json
from model import VapGPT, VapConfig
from events import TurnTakingEvents, EventConfig

import pickle

import torch

def main():
    
    
    configs = get_args()

    if "saga-mode1" in configs["cfg_dict"]["checkpoint"]:
        config_path = "configs_vap_audio_faceEmbed.pkl"
    else:
        config_path = "configs_vap_audio.pkl"
    
    print_section('Config path: ' + config_path)

    with open(config_path, "wb") as f:
        pickle.dump(configs, f)
        
    with open(config_path, "rb") as f:
        configs = pickle.load(f)
        

    print_section('Test load: start...')
    
    with torch.no_grad():

        model = VAPModel.load_from_checkpoint(configs["cfg_dict"]["checkpoint"], strict=False, conf=configs["model"])
        model.eval()
    
        model.event_conf = configs["event"]
        model.event_extractor = TurnTakingEvents(model.event_conf)
    
    print_section('done')

def print_section(text):

    print('##########################')
    print(text)
    print('##########################')
        
if __name__ == '__main__':
    main()