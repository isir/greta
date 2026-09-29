# -*- coding: utf-8 -*-
"""
Created on Thu Oct 17 12:05:29 2024

@author: takes

Quickstart: https://learn.microsoft.com/en-us/azure/ai-services/speech-service/get-started-text-to-speech?tabs=windows%2Cterminal&pivots=programming-language-python
python example: https://github.com/Azure-Samples/cognitive-services-speech-sdk/blob/master/samples/python/console/speech_synthesis_sample.py
How to lower latency: https://learn.microsoft.com/en-us/azure/ai-services/speech-service/how-to-lower-speech-synthesis-latency?pivots=programming-language-python

VisemeID to IPA phoneme: https://learn.microsoft.com/en-us/azure/ai-services/speech-service/how-to-speech-synthesis-viseme?tabs=visemeid&pivots=programming-language-python
SSML phonetic alphabets: https://learn.microsoft.com/en-us/azure/ai-services/speech-service/speech-ssml-phonetic-sets

"""

from pathlib import Path
# from tqdm import tqdm
import pprint as pp
import time
import csv
import sys
import os

import re

import azure.cognitiveservices.speech as speechsdk

viseme_list = []

def viseme_cb(evt):
    
    # print("Viseme event received: audio offset: {}ms, viseme id: {}.".format(evt.audio_offset / 10000, evt.viseme_id))

    # `Animation` is an xml string for SVG or a json string for blend shapes
    animation = evt.animation
    
    viseme_list.append([evt.audio_offset / 10000, evt.viseme_id])
    print('viseme {:.2f} {}'.format(evt.audio_offset / 10000, evt.viseme_id))
    
def bookmark_cb(evt):
    
    # print("Bookmark reached: {}, audio offset: {}ms, bookmark text: {}.".format(evt, evt.audio_offset / 10000, evt.text))
    print("bookmark {} {:.2f}".format(evt.text, evt.audio_offset / 10000))

def main(ssml_file, audio_file, VOICE_NAME, encoding):
    
    ssml_file = Path(ssml_file).name
    audio_file = Path(audio_file).name
        
    speech_key_file = 'SPEECH_KEY.txt'
    speech_region_file = 'SPEECH_REGION.txt'
    with open(speech_key_file, 'r') as f:
        SPEECH_KEY = f.read()
    with open(speech_region_file, 'r') as f:
        SPEECH_REGION = f.read()
        
    
    # VOICE_NAME = 'en-US-AvaMultilingualNeural'
    # VOICE_NAME = 'ja-JP-NanamiNeural'
    # VOICE_NAME = 'en-GB-AdaMultilingualNeural'

    # This example requires environment variables named "SPEECH_KEY" and "SPEECH_REGION"
    speech_config = speechsdk.SpeechConfig(subscription=SPEECH_KEY, region=SPEECH_REGION)
    audio_config = speechsdk.audio.AudioOutputConfig(use_default_speaker=True, filename=audio_file)
    
    # The neural multilingual voice can speak different languages based on the input text.
    speech_config.speech_synthesis_voice_name=VOICE_NAME
    
    speech_synthesizer = speechsdk.SpeechSynthesizer(speech_config=speech_config, audio_config=audio_config)
    speech_synthesizer.viseme_received.connect(viseme_cb)
    speech_synthesizer.bookmark_reached.connect(bookmark_cb)
    
    # # Get text from the console and synthesize to the default speaker.
    # print("Enter some text that you want to speak >")
    # text = input()
    # ssml = None
    
    # Bookmark tag is needed in the SSML, e.g.
    text = None
    # ssml = "<speak version='1.0' xml:lang='en-US' xmlns='http://www.w3.org/2001/10/synthesis' " \
    #         "xmlns:mstts='http://www.w3.org/2001/mstts'>" \
    #         "<voice name='Microsoft Server Speech Text to Speech Voice (en-US, AriaNeural)'>" \
    #         "<bookmark mark='bookmark_one'/> one. " \
    #         "<bookmark mark='bookmark_two'/> two. three. four.</voice></speak> "

    # ssml = "<speak version='1.0' xml:lang='en-US' xmlns='http://www.w3.org/2001/10/synthesis' " \
    #         "xmlns:mstts='http://www.w3.org/2001/mstts'>" \
    #         "<voice name='en-US-AndrewMultilingualNeural'>" \
    #         "<bookmark mark='bookmark_one'/> one. " \
    #         "<bookmark mark='bookmark_two'/> two. three. four.</voice></speak> "

    # ssml_file = sys.argv[1]
    ssml_load_success = False
    try:
        with open(ssml_file, 'r', encoding = encoding) as f:
            ssml = f.read()
        ssml_load_success = True
    except:
        import traceback
        print(traceback.print_exc())

    if not ssml_load_success:
        assert True, "failed to load ssml file."
    
    # print(ssml[1])
    # print(type(ssml[1]))
    # print("<")
    # print(type("<"))
    # print(ssml[0] == "<")
    if ssml[1] != "<":
        ssml = ssml[3:]
    ssml = modifySSML4AzureTTS(ssml, VOICE_NAME)
    
    
    with open('python_log.txt', 'w', encoding="UTF-8") as f:
        f.write('##### SSML received #####\n')
        f.write(ssml + "\n")
        f.write('#########################\n')
        f.write("data {} {} {} {}".format(ssml_file, audio_file, VOICE_NAME, encoding))
    
    s_time = time.time()
    
    if text != None:
        speech_synthesis_result = speech_synthesizer.speak_text_async(text).get()
    else:
        speech_synthesis_result = speech_synthesizer.speak_ssml_async(ssml).get()
    
    if speech_synthesis_result.reason == speechsdk.ResultReason.SynthesizingAudioCompleted:
        
        if text != None:
            # print("Speech synthesized for text [{}]".format(text))
            pass
        else:
            # print("Speech synthesized for ssml [{}]".format(ssml))
            pass
        
        e_time = time.time()
        # print('Total process time: {:.3f} sec.'.format(e_time - s_time))
        # pp.pprint(viseme_list)
        
    elif speech_synthesis_result.reason == speechsdk.ResultReason.Canceled:
        cancellation_details = speech_synthesis_result.cancellation_details
        print("Speech synthesis canceled: {}".format(cancellation_details.reason))
        if cancellation_details.reason == speechsdk.CancellationReason.Error:
            if cancellation_details.error_details:
                print("Error details: {}".format(cancellation_details.error_details))
                print("Did you set the speech resource key and region values?")

def modifySSML4AzureTTS(ssml, VOICE_NAME):
    
    ssml = ssml.replace("<mark name", "<bookmark mark")
    ssml = re.sub("(<prosody ).+?(>)", "<voice name=\"{}\">".format(VOICE_NAME), ssml)
    ssml = ssml.replace("</prosody>", "</voice>")
    
    return ssml
                
if __name__ == '__main__':
    ssml_file = sys.argv[1]
    audio_file = sys.argv[2]
    VOICE_NAME = sys.argv[3]
    encoding = sys.argv[4]
    main(ssml_file, audio_file, VOICE_NAME, encoding)