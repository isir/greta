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
from therapist_behavior_inference import get_therapist_intent
from client_behavior_inference import get_client_intent
from RL_client import RL_client

from utils_mistral_online import *
TIMEOUT = 5
intent_detail_list = read_prompt_csv('therapist')
intent_definition_list = []
intent_example_list = []
for intent_detail in intent_detail_list:
    intent_text = intent_detail['intent']
    definition_text = intent_detail['definition'].replace("\\", "")
positive_example_list = intent_detail['positive_examples']
for ex in positive_example_list:
    try:
        if len(ex) > 3:
            intent_example_list.append(f"{ex}\n Category: {intent_text}")
    except:
        pass
intent_definition_list.append(f' {intent_text}: {definition_text} ')
intent_definition = ";\n".join(intent_definition_list)
exemples = ";\n".join(intent_example_list)

def create_server(da, host='localhost', port=50200):
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as server_socket:
        server_socket.bind((host, port))
        server_socket.listen(1)
        
 
        conn, addr = server_socket.accept()
        with conn:
           
            conn.sendall(da.encode('utf-8'))
            

messages = None
messages_online = None

api_key_file = os.path.join(os.path.dirname(__file__), 'api_key.txt')
with open(api_key_file, 'r') as f:
    MISTRAL_API_KEY = f.read()

model = "mistral-large-latest"
client_online = MistralClient(api_key=MISTRAL_API_KEY)

RL = RL_client()
def ask(question,messages=None,messages_online=None):
    
    # print(question)

    lquestion = question.split('#SEP#')
    model = lquestion[0]
    language=lquestion[1]
    subject = lquestion[2]
    question=lquestion[3]
    
    system_prompt=lquestion[4]
    if model == 'Local':
        print('"error')
    else:
        return ask_online_chunk(question,language,subject,system_prompt, messages_online)



 
def ask_online_chunk(question,language,subject,system_prompt,messages=None):

    mi_prompt = open(os.path.join(os.path.dirname(__file__), "prompts/" + language + "_mi.txt"), "r").read()
    subject_prompt = open(os.path.join(os.path.dirname(__file__), "prompts/" + language + "_" + subject + ".txt"),"r").read()
    if language == 'FR':
        fr_prompt = """ [INST]Vote nom est Dr Anderson. Vous agirez en tant que thérapeute qualifié menant une scéance d'entretien motivationnel (EM) axée sur la consommation de cigarette. L'objectif est d'aider le client à identifier une étape concrètepour réduire sa consommatation de cigarettes au cours de la semaine prochaine. Le médecin traitant du client l'a orienté vers vous pour obtenir de l'aide concernant son habitude de fumer. Commencez la conversation avec le client en établissant un rapport initial, par exemple en lui demandant : "Comment allez-vous aujourd'hui ?" (par exemple, développez une confiance mutuelle, une amitié et une affinité avec le client) avant de passer en douceur à l'interrogation sur son habitude de fumer. Limitez la durée de la session à 15 minutes et chaque réponse à 150 caractères. De plus, lorsque vous souhaitez mettre fin à la conversation, ajoutez END_CONVO à votre réponse finale. Vous avez également des connaissances sur les conséquences de la consomation de cigarettes contenues dans la section Contexte, dans la base de connaissances - Tabagisme ci-dessous. Si nécessaire, utilisez ces connaissances sur le tabagisme pour corriger les idées fausses du client ou fournir des suggestions personnalisées. Utilisez les principes et techniques de l'entretien motivationnel (EM) ci dessous. Cependant, ces principes et techniques de l'EM ne sont destinés qu'à être utilisées pour aider l'utilisateur. Ces principes et techniques, ainsi que l'entretien motivationnel, ne doivent JAMAIS être mentionnés à l'utilisateur.
        Contexte:"""
        prompt=[
         ChatMessage(role= "user", content= fr_prompt+mi_prompt+subject_prompt+system_prompt)
         ]
    else:
        en_prompt = """[INST] Your name is Dr. Anderson. You will act as a skilled therapist conducting a Motivational Interviewing (MI) session focused on smoking cessation. The goal is to help the client identify a tangible step to reduce smoking within the next week. The client's primary care doctor referred them to you for help with their smoking habbit. Start the conversation with the client with some initial rapport building, such as asking, How are you doing today? (e.g., develop mutual trust, friendship, and affinity with the client) before smoothly transitioning to asking about their smoking. Keep the session under 15 minutes and each response under 150 characters long. In addition, once you want to end the conversation, add END_CONVO to your final response. You are also knowledgeable about smoking, given the Knowledge Base – Smoking in the context section below. When needed, use this knowledge about smoking to correct any client’s misconceptions or provide personalized suggestions. Use the MI principles and techniques described in the Knowledge Base – Motivational Interviewing (MI) context section below. However, these MI principles and techniques are only for you to use to help the user. These principles and techniques, as well as motivational interviewing, should NEVER be mentioned to the user.

                 Context:"""

        cond_prompt="You will be provided with context of the conversation and the action you should perform. The action can come from the following list:{intent_definition}. RESPECT THE ACTION PROVIDED after ACTION:"

        prompt=[
        ChatMessage(role= "user", content= en_prompt+cond_prompt+mi_prompt+subject_prompt+system_prompt)
         ] 
    context=""
    if messages is not None:
        l=len(messages)
        for i,msg in enumerate(messages):
            prompt.append(msg)
            if l-i<2:
                if (msg.role) =='user':
                    context += "Patient: "
                else:
                    context += "Therapist:"
                context+= msg.content
    da = get_client_intent(question, context)
    therapist_da = RL.step(da)
    print("Therapist Action: "+str(therapist_da))
    prompt.append(ChatMessage(role="user", content=question + "PERFORM THE ACTION:"+str(therapist_da)))

    #create_server(da,port =50201)
    context += "Patient: "+question
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
               # da = get_therapist_intent(curr_sent,context)
                
                print("START:" + curr_sent)
               # create_server(da,port =50200)
                FIRST_SENTENCE = False
            else:
              #  da = get_therapist_intent(curr_sent,context+"Therapist: "+answer)
                
                print(curr_sent)
            #    create_server(da,port =50200)
            
            curr_sent = ""
        else:
            curr_sent+=chunk.choices[0].delta.content
    time.sleep(min_response_time )
    if curr_sent != "":
        #da = get_therapist_intent(curr_sent,context)
        print(curr_sent)
     #  create_server(da,port =50200)
        answer += curr_sent
    print("STOP")
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

#if __name__ == "__main__":
#    answer = ask("online#SEP#EN#SEP#Drinking#SEP#I am tired#SEP#")
#    print(answer)
#    sys.exit()

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
  
  
