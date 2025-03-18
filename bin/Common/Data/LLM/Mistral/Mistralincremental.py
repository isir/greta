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

TIMEOUT = 5

messages = None
messages_online = None

api_key_file = os.path.join(os.path.dirname(__file__), 'api_key.txt')
with open(api_key_file, 'r') as f:
    MISTRAL_API_KEY = f.read()

model = "mistral-large-latest"
# client_online = MistralClient(api_key=MISTRAL_API_KEY)
# client = OpenAI(base_url="http://localhost:1234/v1", api_key="lm-studio")
client_online = None
client = None

def ask(question,messages=None,messages_online=None):
    
    # print(question)

    lquestion = question.split('#SEP#')
    model = lquestion[0]
    language=lquestion[1]
    question=lquestion[2]
    system_prompt=lquestion[3]
    if model == 'Local':
        return ask_local_chunk(question,language,system_prompt, messages)
    else:
        return ask_online_chunk(question,language,system_prompt, messages_online)


def ask_local_chunk(question,language, system_prompt, messages=None):

    global client
    
    if client == None:
        
        client = OpenAI(base_url="http://localhost:1234/v1", api_key="lm-studio")
        
    if language == 'FR':
        prompt=[
        {"role": "system", "content": "Tu es un assistant virtuel qui réponds en français avec des phrases courtes de style oral. Réponds uniquement en français. "+system_prompt}
         ]
    else:
          prompt=[
        {"role": "system", "content": "You are a virtual assistant, answering the provided question with the shortest answer. Use an oral style. "+system_prompt}
         ] 
    if messages is not None:
        for msg in messages:
            prompt.append(msg)
    prompt.append({"role":"user", "content":question})
    response = client.chat.completions.create(
        model="TheBloke/Mistral-7B-Instruct-v0.2-GGUF",
        messages=prompt,
        temperature=0.7,
        stream = True
    )
    
    answer = ""
    curr_sent= ""
    FIRST_SENTENCE = True
    s_time = time.time()
    for chunk in response:

        if chunk.choices is None:
            continue
        
        elif chunk.choices[0].delta.content is None:
            continue
            
        elif chunk.choices[0].delta.content in [".","?","!",";"," ?"]:
            curr_sent+=chunk.choices[0].delta.content
            answer += curr_sent

            if FIRST_SENTENCE:
                print("START:" + curr_sent)
                FIRST_SENTENCE = False
            else:
                print(curr_sent)

            curr_sent = ""

        else:
            curr_sent+=chunk.choices[0].delta.content

        if (time.time() - s_time) > TIMEOUT:
            answer = "Response time over. Sorry, some errors happened."
            break
    if curr_sent !="":
        if FIRST_SENTENCE:
                print("START:" + curr_sent)
                FIRST_SENTENCE = False
        else:
            print(curr_sent)
        answer += curr_sent
    print("STOP")
    answer = answer.replace('\n', ' ')
    answer = answer.replace('[', ' ')
    answer = answer.replace(']', ' ')
    return question,answer
 
def ask_online_chunk(question,language,system_prompt,messages=None):

    global client_online
    
    if client_online == None:
        
        client_online = MistralClient(api_key=MISTRAL_API_KEY)    

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

    response = client_online.chat_stream(
         model=model,
           messages=prompt
    )
    answer = ""
    curr_sent= ""
    min_response_time = 1
    start = time.perf_counter()
    FIRST_SENTENCE = True
    for chunk in response:
        
        if chunk.choices[0].delta.content is None:
            pass
        elif chunk.choices[0].delta.content in [".","?","!",";"," ?"]:
            curr_sent+=chunk.choices[0].delta.content
            if answer != "":
                response_time = time.perf_counter() -start
                if response_time < min_response_time  :
                    time.sleep(min_response_time  - response_time)
            start = time.perf_counter()
            answer += curr_sent
            
            if FIRST_SENTENCE:
                print("START:" + curr_sent)
                FIRST_SENTENCE = False
            else:
                print(curr_sent)
            
            curr_sent = ""
        else:
            curr_sent+=chunk.choices[0].delta.content
    time.sleep(min_response_time )
    if curr_sent != "":
        answer += curr_sent
        print(curr_sent)
    print("STOP")
    answer = answer.replace('\n', ' ')
    answer = answer.replace('[', ' ')
    answer = answer.replace(']', ' ')
    if answer == "":
        answer = "NOTHING"
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
        message_reciv=False
  
  
