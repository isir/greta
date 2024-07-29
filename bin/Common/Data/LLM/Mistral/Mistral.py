import os
import openai
import time
import socket
import pickle 
import argparse
from openai import OpenAI
import sys
from mistralai.client import MistralClient
from mistralai.models.chat_completion import ChatMessage


messages = None
messages_online = None

# MISTRAL_API_KEY = "Jg5LiL8bYyB4wlb5Rhj30pc2IXKBYXQ0"
api_key_file = os.path.join(os.path.dirname(__file__), 'api_key.txt')
with open(api_key_file, 'r') as f:
    MISTRAL_API_KEY = f.read()

model = "mistral-large-latest"
client_online = MistralClient(api_key=MISTRAL_API_KEY)
client = OpenAI(base_url="http://localhost:1234/v1", api_key="lm-studio")

def ask(question,messages=None,messages_online=None):

    lquestion = question.split('#SEP#')
    model = lquestion[0]
    language=lquestion[1]
    question=lquestion[2]
    system_prompt=lquestion[3]
    if model == 'Local':
        return ask_local(question,language,system_prompt, messages)
    else:
        return ask_online(question,language,system_prompt, messages_online)
def ask_local(question,language, system_prompt, messages=None):
    
    if language == 'French':
        prompt=[
        {"role": "system", "content": "Tu es un assistant virtuel qui réponds en français avec des phrases courtes de style oral. Réponds uniquement en français. "+system_prompt}
         ]
    else:
          prompt=[
        {"role": "system", "content": "You are a virtual assistant, answer with short answer. Use an oral style. "+system_prompt}
         ] 
    if messages is not None:
        for msg in messages:
            prompt.append(msg)
    prompt.append({"role":"user", "content":question})
    response = client.chat.completions.create(
        model="TheBloke/Mistral-7B-Instruct-v0.2-GGUF",
        messages=prompt,
        temperature=0.7,
    )
    
  
    answer = response.choices[0].message.content
    answer = answer.replace('\n', ' ')
    answer = answer.replace('[', ' ')
    answer = answer.replace(']', ' ')
    return question,answer
 
def ask_online(question,language,system_prompt,messages=None):


    if language == 'French':
        prompt=[
         ChatMessage(role= "system", content= "Tu es un assistant virtuel qui réponds en français avec des phrases courtes de style oral. Réponds uniquement en français. "+system_prompt)
         ]
    else:
          prompt=[
        ChatMessage(role= "system", content= "You are a virtual assistant, answer with short answer. Use an oral style. "+system_prompt)
         ] 
    if messages is not None:
        for msg in messages:
            prompt.append(msg)
    prompt.append(ChatMessage(role="user", content=question))

    response = client_online.chat(
         model=model,
           messages=prompt,
    )
    
  
    answer = response.choices[0].message.content
    answer = answer.replace('\n', ' ')
    answer = answer.replace('[', ' ')
    answer = answer.replace(']', ' ')
    return question,answer   
def append_interaction_to_chat_log(question, answer, messages=None,messages_online=None):
    if messages is None:
        messages = []
    if messages_online is None:
        messages_online = []
    messages.append({"role":"user", "content":question})
    messages.append({"role":"assistant", "content":answer})
    messages_online.append(ChatMessage(role='user',content=question))
    messages_online.append(ChatMessage(role='assistant',content=answer))
    return messages,messages_online

# if __name__ == "__main__":
#     answer = ask("hello")
#     print(answer)
#     sys.exit()

parser=argparse.ArgumentParser()
parser.add_argument("port", help="server port", type=int, default="4000")


args=parser.parse_args()

port = args.port
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.connect(("localhost",port))
message_reciv=False
while(True):
    msg=s.recv(1024)
    msg=msg.decode('iso-8859-1')
    message_reciv=True
    if(len(msg)>0 and message_reciv):
        if(msg=="exit"):
            break
        question,answ=ask(msg, messages,messages_online)
        messages,messages_online = append_interaction_to_chat_log(question ,answ, messages,messages_online)
        print(answ)
        s.send(answ.encode('iso-8859-1'))
        message_reciv=False
  
  