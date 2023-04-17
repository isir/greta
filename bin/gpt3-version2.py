import os
import openai
import time
import socket
import pickle 
import argparse

openai.api_key ="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"
messages=[]
parser=argparse.ArgumentParser()
parser.add_argument("port", help="server port", type=int, default="4000")
args=parser.parse_args()
port = args.port
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.connect((socket.gethostname(),port))
last_message=""
while(True):
    msg=s.recv(1024)
    msg=msg.decode()
    if(msg and len(msg)>0):
        if(msg=="exit"):
            break
        content = "User:"+msg
        messages.append({"role": "user", "content": content})
        if(last_message != msg):
            completion = openai.ChatCompletion.create(
                model="gpt-3.5-turbo",
                messages=messages
            )
            chat_response = completion.choices[0].message.content
            print(f'ChatGPT: {chat_response}')
            messages.append({"role": "assistant", "content": chat_response})
            print(chat_response)
            s.send(chat_response.encode())
            last_message=msg
 
  