import os
import openai
import time
import socket
import pickle 
import argparse


def GPT_Completion(texts):
    ## Call the API key under your account (in a secure way)
    openai.api_key = ""
    response = openai.Completion.create(
        engine="text-davinci-003",
        prompt =  texts,
        temperature = 0.5,
        max_tokens = 1000,
        n=1,
        stop=None
    )
    return response.choices[0].text

parser=argparse.ArgumentParser()
parser.add_argument("port", help="server port", type=int, default="4000")


args=parser.parse_args()

port = args.port
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.connect((socket.gethostname(),port))
while(True):
    msg=s.recv(1024)
    msg=msg.decode()
    if(len(msg)>0):
        if(msg=="exit"):
            break
        answ=GPT_Completion(msg)
        print(answ)
        s.send(answ.encode())
  
  