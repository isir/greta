# Copyright 2023-2024 Deepgram SDK contributors. All Rights Reserved.
# Use of this source code is governed by a MIT license that can be found in the LICENSE file.
# SPDX-License-Identifier: MIT

from dotenv import load_dotenv
import logging
from deepgram.utils import verboselogs
from time import sleep
import socket
from waiting import wait
import argparse
from deepgram import (
    DeepgramClient,
    DeepgramClientOptions,
    LiveTranscriptionEvents,
    LiveOptions,
    Microphone,
)
import os

import numpy as np
from threading import Thread, Lock

load_dotenv()

# We will collect the is_final=true messages here so we can use them when the person finishes speaking
is_finals = []
api_key_file = os.path.join(os.path.dirname(__file__), 'api_key.txt')
with open(api_key_file, 'r') as f:
    DEEPGRAM_API_KEY = f.read()
deepgram: DeepgramClient = DeepgramClient(DEEPGRAM_API_KEY)

dg_connection = deepgram.listen.live.v("1")
STOP = False
def main(language):
    
    RATE = 16000

    mic_host = socket.gethostname()  # as both code is running on same pc
    mic_port = 9000  # socket server port number
    BUFFER_SIZE = 4096

    print("language : "+language)
    if language == "EN":
        language = "en-US"
        
    else:
        language = "fr"
    print("language : "+language)
    try:
        # example of setting up a client config. logging values: WARNING, VERBOSE, DEBUG, SPAM
        # config = DeepgramClientOptions(
        #     verbose=verboselogs.DEBUG, options={"keepalive": "true"}
        # )
        # deepgram: DeepgramClient = DeepgramClient("", config)
        # otherwise, use default config
        

        def on_open(self, open, **kwargs):
            print(f"Connection Open")

        def on_message(self, result, **kwargs):
            global is_finals
            sentence = result.channel.alternatives[0].transcript
            if len(sentence) == 0:
                return 
            if result.is_final:
                # We need to collect these and concatenate them together when we get a speech_final=true
                # See docs: https://developers.deepgram.com/docs/understand-endpointing-interim-results
                is_finals.append(sentence)

                # Speech Final means we have detected sufficent silence to consider this end of speech
                # Speech final is the lowest latency result as it triggers as soon an the endpointing value has triggered
                if result.speech_final:
                    utterance = " ".join(is_finals)
                    print(f"Speech Final: {utterance}")
                    is_finals = []
                    s.send(utterance.encode('iso-8859-1'))
                else:
                    # These are useful if you need real time captioning and update what the Interim Results produced
                    print(f"Is Final: {sentence}")
            else:
                # These are useful if you need real time captioning of what is being spoken
                print(f"Interim Results: {sentence}")

        def on_metadata(self, metadata, **kwargs):
            print(f"Metadata: {metadata}")

        def on_speech_started(self, speech_started, **kwargs):
            print(f"Speech Started")

        def on_utterance_end(self, utterance_end, **kwargs):
            print(f"Utterance End")
            global is_finals
            if len(is_finals) > 0:
                utterance = " ".join(is_finals)
                print(f"Utterance End: {utterance}")
                is_finals = []

        def on_close(self, close, **kwargs):
            print(f"Connection Closed")

        def on_error(self, error, **kwargs):
            print(f"Handled Error: {error}")

        def on_unhandled(self, unhandled, **kwargs):
            print(f"Unhandled Websocket Message: {unhandled}")

        dg_connection.on(LiveTranscriptionEvents.Open, on_open)
        dg_connection.on(LiveTranscriptionEvents.Transcript, on_message)
        dg_connection.on(LiveTranscriptionEvents.Metadata, on_metadata)
        dg_connection.on(LiveTranscriptionEvents.SpeechStarted, on_speech_started)
        dg_connection.on(LiveTranscriptionEvents.UtteranceEnd, on_utterance_end)
        dg_connection.on(LiveTranscriptionEvents.Close, on_close)
        dg_connection.on(LiveTranscriptionEvents.Error, on_error)
        dg_connection.on(LiveTranscriptionEvents.Unhandled, on_unhandled)

        options: LiveOptions = LiveOptions(
            model="nova-3",
            language=language,
            # Apply smart formatting to the output
            smart_format=True,
            # Raw audio format details
            encoding="linear16",
            channels=1,
            sample_rate=RATE,
            # To get UtteranceEnd, the following must be set:
            interim_results=True,
            utterance_end_ms="1000",
            vad_events=True,
            # Time in milliseconds of silence to wait for before finalizing speech
            endpointing=500,
        )

        addons = {
            # Prevent waiting for additional numbers
            "no_delay": "true"
        }

        print("\n\nPress Enter to stop recording...\n\n")
        if dg_connection.start(options, addons=addons) is False:
            print("Failed to connect to Deepgram")
            return

        # Open a microphone stream on the default input device
        # microphone = Microphone(dg_connection.send)
        microphone = Microphone_client(dg_connection.send, mic_host, mic_port, BUFFER_SIZE)

        # start microphone
        microphone.start()

        # wait until finished
        wait(received_STOP , timeout_seconds=120, waiting_for="Button STOP to be pressed")

        # Wait for the microphone to close
        microphone.finish()

        # Indicate that we've finished
        dg_connection.finish()

        print("Finished")
        # sleep(30)  # wait 30 seconds to see if there is any additional socket activity
        # print("Really done!")

    except Exception as e:
        print(f"Could not open socket: {e}")
        return

class Microphone_client(object):
    
    def __init__(self, send_func, mic_host, mic_port, BUFFER_SIZE):
        
        self.mic_socket = socket.socket()  # instantiate
        self.mic_socket.connect((mic_host, mic_port))  # connect to the server
        
        self.runnning = False
        self.BUFFER_SIZE = BUFFER_SIZE
        self.send_func = send_func
    
    def start(self):
        
        self.running = True
        
        def mic_thread():

            while self.running:
                
                message = 'ok'
                self.mic_socket.send(message.encode())
                data = self.mic_socket.recv(self.BUFFER_SIZE)  # receive response
                self.send_func(data)
        
        self.thread = Thread(target = mic_thread)
        self.thread.start()
        
        print('Mic thead started')
            
    def finish(self):
        
        self.running = False
        try:
            self.thread.close()
        except Exception as e:
            print(e)
        print('Mic thead ended')

def received_STOP():
    
    language=s.recv(1024)
    language=language.decode('iso-8859-1').strip()
    
    message_reciv=True
    print("NOT SOPPING"+language)
    if(len(language)>0 and message_reciv):
            
       if(language == "STOP"):
            print("STOPPING")
            return True
    
    return False


if __name__ == "__main__":
    
    parser=argparse.ArgumentParser()
    parser.add_argument("port", help="server port", type=int, default="4040")

    args=parser.parse_args()

    port = args.port
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.connect(("localhost",port))

    message_reciv=False
    while(True):
        language=s.recv(1024)
        language=language.decode('iso-8859-1').strip()
        message_reciv=True
        if(len(language)>0 and message_reciv):
            print("Test Language "+language)
            if(language=="exit"):
                break
            elif(language == "STOP"):
                STOP=True
                print("STOPPING")
            else:
                STOP = False
                main(language)
           
            message_reciv=False
