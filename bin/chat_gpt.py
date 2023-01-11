import os
import openai
import time
import socket
import numpy as np
import pickle 
import argparse


def GPT_Completion(texts):
    ## Call the API key under your account (in a secure way)
    openai.api_key = "your API key"
    response = openai.Completion.create(
        engine="text-davinci-002",
        prompt =  texts,
        temperature = 0.6,
        top_p = 1,
        max_tokens = 64,
        frequency_penalty = 0,
        presence_penalty = 0
    )
    return print(response.choices[0].text)

parser=argparse.ArgumentParser()
parser.add_argument("port", help="server port", type=str, default="4000")
parser.add_argument("address", help="server address", type=str, default="localhost")

args=parser.parse_args()

ip = args.address
port = args.port
s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, 0)
while(True)
    msg=s.recv(2048)
    if(len(msg)>0):
        if(msg=="exit"):
            break
        answ=GPT_Completion(msg)
        s.sendto(answ, (ip, port))
      
  