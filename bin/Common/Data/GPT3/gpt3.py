import os
import openai
import time
import socket
import pickle 
import argparse

openai.api_key ="Your API Key"
start_chat_log = '''Human: Hello, who are you?
AI: I am doing great. How can I help you today?
'''
chat_log = None
completion = openai.Completion()
def ask(question, chat_log=None):
    if chat_log is None:
        chat_log = start_chat_log
    prompt = f'{chat_log}Human: {question}\nAI:'
    response = completion.create(
        prompt=prompt, engine="text-davinci-003", stop=['\nHuman'], temperature=0,
        top_p=1, frequency_penalty=1, presence_penalty=0.6, best_of=1,
        max_tokens=100)
    answer = response.choices[0].text.strip()
    return answer
    
def append_interaction_to_chat_log(question, answer, chat_log=None):
    if chat_log is None:
        chat_log = start_chat_log
    return f'{chat_log}Human: {question}\nAI: {answer}\n'

parser=argparse.ArgumentParser()
parser.add_argument("port", help="server port", type=int, default="4000")


args=parser.parse_args()

port = args.port
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.connect((socket.gethostname(),port))
message_reciv=False
while(True):
    msg=s.recv(1024)
    msg=msg.decode()
    message_reciv=True
    if(len(msg)>0 and message_reciv):
        if(msg=="exit"):
            break
        answ=ask(msg,chat_log)
        chat_log = append_interaction_to_chat_log(msg ,answ, chat_log)
        print(answ)
        s.send(answ.encode())
        message_reciv=False
  
  