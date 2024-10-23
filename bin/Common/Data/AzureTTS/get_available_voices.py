#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Mon Oct  7 21:28:10 2024

@author: takeshi-s

Ref:
    Furhat remote API: https://docs.furhat.io/remote-api/
    Viseme in MS Azure speech service: https://learn.microsoft.com/en-us/azure/ai-services/speech-service/how-to-speech-synthesis-viseme?tabs=3dblendshapes&pivots=programming-language-python
    Language, locale of Azure speech: https://learn.microsoft.com/en-us/azure/ai-services/speech-service/language-support?tabs=tts#viseme
    Save Azure TTS audio into wav file: https://stackoverflow.com/questions/77923835/how-to-save-a-stream-object-in-azure-text-to-speech-without-speaking-the-text-us
    Install Azure speech SDK: https://learn.microsoft.com/en-us/azure/ai-services/speech-service/quickstarts/setup-platform?tabs=macos%2Cubuntu%2Cdotnetcli%2Cdotnet%2Cjre%2Cmaven%2Cnodejs%2Cmac%2Cpypi&pivots=programming-language-python
    Azure TTS quickstart: https://learn.microsoft.com/en-us/azure/ai-services/speech-service/get-started-text-to-speech?tabs=macos%2Cterminal&pivots=programming-language-python
    Azure TTS how to insert timer marker: https://learn.microsoft.com/en-us/azure/ai-services/speech-service/how-to-speech-synthesis?tabs=browserjs%2Cterminal&pivots=programming-language-python#subscribe-to-synthesizer-events
    Greta TTS related files:
        https://github.com/isir/greta/blob/master/core/BehaviorRealizer/src/greta/core/behaviorrealizer/Realizer.java
        https://github.com/isir/greta/blob/master/core/Util/src/greta/core/util/time/Temporizer.java
        https://github.com/isir/greta/blob/f6c25ca1b51a76b744fade68c79580450cf5560e/core/Util/src/greta/core/util/speech/Speech.java#L155
        https://github.com/isir/greta/blob/master/core/Util/src/greta/core/util/speech/TTS.java
        https://github.com/isir/greta/blob/master/auxiliary/TTS/CereProc/src/greta/auxiliary/tts/cereproc/CereProcTTS.java

"""
# from matplotlib import pyplot as plt
from pathlib import Path
# from tqdm import tqdm
import pprint as pp
# import pandas as pd
# import numpy as np
import traceback
import shutil
import math
import time
import csv
import sys
import os

import azure.cognitiveservices.speech as speechsdk
import io
import tempfile

viseme_list = []

def main():

    speech_key_file = 'SPEECH_KEY.txt'
    speech_region_file = 'SPEECH_REGION.txt'
    with open(speech_key_file, 'r') as f:
        SPEECH_KEY = f.read()
    with open(speech_region_file, 'r') as f:
        SPEECH_REGION = f.read()

    TTS_obj = AzureTTS(SPEECH_KEY, SPEECH_REGION)
    
    result = TTS_obj.speech_synthesizer.get_voices_async("").get()
    for voice in result.voices:
        if ("Neural" in voice.short_name) and not (":" in voice.short_name):
            print("{},{}".format(voice.short_name, voice.gender))
    
    # print(len(result.voices))

class AzureTTS(object):
    
    def __init__(self, subscription_key, service_region):
        
        speech_config = speechsdk.SpeechConfig(subscription=subscription_key, region=service_region)
        temp_file_path = tempfile.NamedTemporaryFile(suffix=".wav", delete=False).name
        audio_config = speechsdk.audio.AudioOutputConfig(filename=temp_file_path)
        self.speech_synthesizer = speechsdk.SpeechSynthesizer(speech_config=speech_config, audio_config=audio_config)
    
    def text2speech(self, text_to_speak):
        
        global viseme_list
        
        # # If VisemeID is the only thing you want, you can also use `speak_text_async()`
        # result = speech_synthesizer.speak_ssml_async(ssml).get()    
        result = self.speech_synthesizer.speak_text_async(text_to_speak).get()
        
        viseme_result = viseme_list.copy()
        viseme_list = []
        
        return [result, viseme_result]
    
    def save_audio(self, result, file_path = 'output.wav'):
        
        with open(file_path, 'wb') as audio_file:
            audio_file.write(result.audio_data)
        
        # print(f"Audio saved to {file_path}")
        
    def save_viseme(self, viseme_result, file_path = 'output.txt'):
        
        output_text = []
        for viseme_id, viseme_duration in viseme_result:
            output_text.append("{:} {:.3f}\n".format(viseme_id, viseme_duration))
            # output_text.append([viseme_id, viseme_duration])
        
        with open(file_path, 'w') as f:
            f.writelines(output_text)

        # print(f"Viseme saved to {file_path}")

        
if __name__ == '__main__':
    main()
