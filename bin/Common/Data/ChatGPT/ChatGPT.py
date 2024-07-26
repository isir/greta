import os
import openai
import time
import socket
import pickle 
import argparse

import sys

# API_KEY ="PUT_YOUR_OPENAI_API_KEY_HERE"
api_key_file = 'api_key.txt'
with open(api_key_file, 'r') as f:
    API_KEY = f.read()

start_message = [{"role":"system", "content":"You are a good friend."},
                  {"role":"user", "content":"Hello, how are you?"},
                  {"role":"assistant", "content":"I am doing great. How can I help you?"},
                  ]

messages = None

client = openai.OpenAI(api_key = API_KEY)
completion = client.chat.completions

def ask(question, messages=None):
    if messages is None:
        messages = start_message

    messages.append({"role":"user", "content":question})
    prompt = messages

    response = completion.create(
        
        messages=prompt, 
        model = "gpt-3.5-turbo",

        # temperature=0,top_p=1, frequency_penalty=1, presence_penalty=0.6, best_of=1,max_tokens=100
        )
    
    # answer = response.choices[0].text.strip()
    
    answer = response.choices[0].message.content

    return answer
    
def append_interaction_to_chat_log(question, answer, messages=None):
    if messages is None:
        messages = start_message
    messages.append({"role":"assistant", "content":"answer"})
    return messages

# if __name__ == "__main__":
#     answer = ask("hello")
#     print(answer)
#     sys.exit()

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
        answ=ask(msg, messages)
        messages = append_interaction_to_chat_log(msg ,answ, messages)
        print(answ)
        s.send(answ.encode())
        message_reciv=False
  
  