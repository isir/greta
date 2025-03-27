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
import threading
from utils_mistral_online import *
TIMEOUT = 5
intent_detail_list = read_prompt_csv('therapist')
intent_detail_list_fr = read_prompt_csv('therapist_fr')
intent_definition_list = []
intent_definition_list_fr = []
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
for intent_detail in intent_detail_list_fr:
    intent_text = intent_detail['intent']
    definition_text = intent_detail['definition'].replace("\\", "")
    positive_example_list = intent_detail['positive_examples']
    for ex in positive_example_list:
        try:
            if len(ex) > 3:
                intent_example_list.append(f"{ex}\n Category: {intent_text}")
        except:
            pass
    intent_definition_list_fr.append(f' {intent_text}: {definition_text} ')
intent_definition_fr = ";\n".join(intent_definition_list_fr)

class DA_Server:
    def __init__(self, port, address='localhost'):
        self.port = port
        self.address = address
        self.server_socket = None
        self.client_socket = None
        self.lock = threading.Lock()
        self.stop_event = threading.Event()
        self.server_thread = threading.Thread(target=self.run_server, daemon=True)
        self.server_thread.start()

    def run_server(self):
        # Set up the server socket
        self.server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.server_socket.bind((self.address, self.port))
        self.server_socket.listen(1)
        #print(f"DA_Server listening on {self.address}:{self.port}")
        self.server_socket.settimeout(1.0)  # Timeout to periodically check for stop_event

        while not self.stop_event.is_set():
            try:
                try:
                    client_socket, client_address = self.server_socket.accept()
                except socket.timeout:
                    continue  # Retry accept if timeout occurs
                with self.lock:
                    self.client_socket = client_socket
                #print(f"DA_Server accepted connection from {client_address}")
                # Start a thread to handle the client
                client_thread = threading.Thread(target=self.handle_client, args=(client_socket,), daemon=True)
                client_thread.start()
            except Exception as e:
                print(f"DA_Server encountered an error: {e}")
                break

        self.close_server()

    def handle_client(self, client_socket):
        while not self.stop_event.is_set():
            try:
                # Optionally handle incoming data from the client
                time.sleep(1)
            except Exception as e:
                print(f"Error in client connection: {e}")
                break

        with self.lock:
            if self.client_socket == client_socket:
                self.client_socket.close()
                self.client_socket = None

    # def send_message(self, data):
    #     with self.lock:
    #         if self.client_socket:
    #             try:
    #                 # Ensure the data is 8 bytes long, padded or truncated
    #                 data_bytes = data.encode('utf-8')
    #                 data_bytes = data_bytes.ljust(8, b'\0')[:8]
    #                 self.client_socket.sendall(data_bytes)
    #                 #print(f"DA_Server sent data: {data}")
    #             except Exception as e:
    #                 print(f"DA_Server failed to send data: {e}")
    #                 self.client_socket.close()
    #                 self.client_socket = None
    #         else:
    #             pass
    #             #print("DA_Server: No client connected to send data")

    def send_message(self, strings_list):
        """Send a list of strings to the client."""
        with self.lock:
            if self.client_socket:
                try:
                    # Serialize the list to a JSON-formatted string
                    data = json.dumps(strings_list)
                    data_bytes = data.encode('utf-8')

                    # Send the length of the data first (4 bytes, network byte order)
                    data_length = len(data_bytes)
                    length_prefix = struct.pack('!I', data_length)

                    # Send the length prefix followed by the data
                    self.client_socket.sendall(length_prefix + data_bytes)
                except Exception as e:
                    #print(f"DA_Server failed to send data: {e}")
                    self.client_socket.close()
                    self.client_socket = None
            else:
                #print("DA_Server: No client connected to send data")
                # print("Oh")
                print("")

    def close_server(self):
        self.stop_event.set()
        with self.lock:
            if self.client_socket:
                self.client_socket.close()
                self.client_socket = None
        if self.server_socket:
            self.server_socket.close()
            self.server_socket = None
        print("DA_Server closed")

    def __del__(self):
        self.close_server()


messages = None
messages_online = None
n_change = 0
api_key_file = os.path.join(os.path.dirname(__file__), 'api_key.txt')
with open(api_key_file, 'r') as f:
    MISTRAL_API_KEY = f.read()

model = "mistral-large-latest"
client_online = MistralClient(api_key=MISTRAL_API_KEY)
start_turns = False
RL = RL_client()
n_turn=0
name_user=""
name_log = "log_"+ time.strftime("%Y%m%d_%H%M%S")
def ask(question,messages=None,messages_online=None,start_turns=False,n_change=0,n_turn=0,name_user=""):
    
    # print(question)

    lquestion = question.split('#SEP#')
    model = lquestion[0]
    language=lquestion[1]
    type = lquestion[3]
    condition= lquestion[2]
    subject = lquestion[4]
    question=lquestion[5]
    
    system_prompt=lquestion[6]
    RL.set_type(type)

    if question == "LAUNCHCONV":
        start_phrase = open(os.path.join(os.path.dirname(__file__), "prompts/" + language + "_start.txt"), "r").read()
        print(start_phrase)
        print("STOP")
        return "",start_phrase,False,True,name_user
    if question == "STOPCONV":
        if n_change > 2:
            stop_phrase = open(os.path.join(os.path.dirname(__file__), "prompts/" + language + "_stop_good.txt"), "r").read()
        else:
            stop_phrase = open(os.path.join(os.path.dirname(__file__), "prompts/" + language + "_stop_bad.txt"), "r").read()
        print(stop_phrase)
        print("STOP")
        return "",stop_phrase,False,True,name_user
    if model == 'Local':
        print('"error')
    else:
        q,a,c,name_user = ask_online_chunk(question,language,condition,type,subject,system_prompt, messages_online,start_turns,n_change,n_turn,name_user)
        return q,a,c,False,name_user


def get_name(question,language):

    prompt =  open(os.path.join(os.path.dirname(__file__), "prompts/" + language + "_get_name.txt"), "r").read()
    chat_response = client_online.chat(
    model = model,
    messages = [
        {
            "role": "user",
            "content": prompt+question,
        },
    ]
)
    return chat_response.choices[0].message.content


 
def ask_online_chunk(question,language,condition,type,subject,system_prompt,messages=None,start_turns=False,n_change=0,n_turn=0,name_user=""):
    welcome=""
    mi_prompt = open(os.path.join(os.path.dirname(__file__), "prompts/" + language + "_mi.txt"), "r").read()
    subject_prompt = open(os.path.join(os.path.dirname(__file__), "prompts/" + language + "_" + subject + ".txt"),"r").read()
    descr_type= open(os.path.join(os.path.dirname(__file__), "prompts/" + language + "_" + type + ".txt"),"r").read()
    if language == 'FR':
        
        if condition == "RL":
            cond_prompt="Vous serez informé du contexte de la conversation et du nom du patient ainsi que de l'action que vous devez effectuer. L'action peut provenir de la liste suivante:" + intent_definition_fr+". RESPECTEZ L'ACTION FOURNIE après ACTION :"
        else :
             cond_prompt="Vous serez informé du contexte de la conversation et du nom du patient."+descr_type
    else:
        if condition == "RL":
            cond_prompt="You will be provided with context of the conversation and the name of the patient as well as the action you should perform. The action can come from the following list:" +intent_definition+". RESPECT THE ACTION PROVIDED after ACTION :"
        else:
            cond_prompt="You will be provided with context of the conversation and the name of the patient."+descr_type

    prompt=[
        ChatMessage(role= "user", content="[INST]"+subject_prompt+mi_prompt+system_prompt + cond_prompt+"[/INST]")
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

    da = get_client_intent(question, context, language)
    serv2.send_message([question,context])
    time.sleep(2)
    if "Chang" in da or "Modif" in da:
        change =True
    else:
        change = False
    if start_turns :
        start_turns=False
        name_user = get_name(question,language)
        if name_user == "[NON]" or name_user == "[NO]":
            welcome = open(os.path.join(os.path.dirname(__file__), "prompts/" + language + "_no_name.txt"), "r").read()
            serv.send_message([welcome,""])
            print(welcome)
            name_user=""

        else :
            welcome = open(os.path.join(os.path.dirname(__file__), "prompts/" + language + "_name.txt"), "r").read() + name_user
            serv.send_message([welcome,""])
            print(welcome)
            
        time.sleep(2)
    if condition=="RL":
        therapist_da = RL.step(da)
    #print("Therapist Action: "+str(therapist_da))
        if welcome != "":
            if language == "FR":
                prompt.append(ChatMessage(role="user", content=question + "Therapist : "+welcome+ "COMPLETE LA FIN DU TOUR EN REALISANT L'ACTION:"+str(therapist_da)))
            else:
                prompt.append(ChatMessage(role="user", content=question + "Therapist : "+welcome+ "COMPLEATE THE END OF THE TURN BY PERFORMING THE ACTION:"+str(therapist_da)))
        if language == "FR":
            prompt.append(ChatMessage(role="user", content=question + "REALISE L'ACTION:"+str(therapist_da)))
        else:
            prompt.append(ChatMessage(role="user", content=question + "PERFORM THE ACTION:"+str(therapist_da)))
    else:
        if welcome != "":
            if language == "FR":
                prompt.append(ChatMessage(role="user", content=question + "Therapist : "+welcome+ "COMPLETE LA FIN DU TOUR AVEC UNE PHRASE"))
            else:
                prompt.append(ChatMessage(role="user", content=question + "Therapist : "+welcome+ "COMPLEATE THE END OF THE TURN WITH A SENTENCE"))
        else:
            prompt.append(ChatMessage(role="user", content=question))

   
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
                serv.send_message([curr_sent, context])
                print("START:" + curr_sent)
               # create_server(da,port =50200)
                FIRST_SENTENCE = False
            else:
              #  da = get_therapist_intent(curr_sent,context+"Therapist: "+answer)
                serv.send_message([curr_sent, context])
                print(curr_sent)
            #    create_server(da,port =50200)
            
            curr_sent = ""
        else:
            curr_sent+=chunk.choices[0].delta.content
    time.sleep(min_response_time )
    if curr_sent != "":
        #da = get_therapist_intent(curr_sent,context)
        serv.send_message([curr_sent, context])
        print(curr_sent)
     #  create_server(da,port =50200)
        answer += curr_sent

    answer = answer.replace('\n', ' ')
    answer = answer.replace('[', ' ')
    answer = answer.replace(']', ' ')
    if "END_CONVO" in answer or (condition=="RL" and "Closing" in therapist_da and n_turn >=5) or (condition=="RL" and "clôture" in therapist_da and n_turn >=5):
        if n_change > 2:
            stop_phrase = open(os.path.join(os.path.dirname(__file__), "prompts/" + language + "_stop_good.txt"), "r").read()
        else:
            stop_phrase = open(os.path.join(os.path.dirname(__file__), "prompts/" + language + "_stop_bad.txt"), "r").read()
        serv.send_message([stop_phrase, context])
        print(stop_phrase)
    else:
        stop_phrase=""
    print("STOP")
    return question,welcome+answer+stop_phrase  ,change ,name_user
def append_interaction_to_chat_log(question, answer, messages=None,messages_online=None,name_log=None,name_user=""):
    if name_log is not None:
        # Open the file in append mode
        with open(os.path.join(os.path.dirname(__file__), "logs/" + name_log + ".txt"), 'a+') as file:
            #Append content to the file
            if question != "":
                if name_user != "":
                    file.write("User: "+question.replace(name_user,"[NAME]")+"\n")
                else:
                    file.write("User: "+question+"\n")
            if name_user != "":
                file.write("Agent: "+answer.replace(name_user,"[NAME]")+"\n")
            else:
                file.write("Agent: "+answer+"\n")


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
#    answer = ask("online#SEP#FR#SEP#Drinking#SEP#Je suis fatiguee#SEP#")
#    print(answer)
#    sys.exit()

parser=argparse.ArgumentParser()
parser.add_argument("port", help="server port", type=int, default="4000")


args=parser.parse_args()

serv = DA_Server(port=5555)
serv2 = DA_Server(port=5556)
port = args.port
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.connect(("localhost", port))
message_reciv = False
while(True):
    msg=s.recv(1024)
    msg=msg.decode('iso-8859-1')
    message_reciv=True
    if(len(msg)>0 and message_reciv):
        if(msg=="exit"):
            break
        question,answ,change,start_turns,name_user=ask(msg, messages,messages_online,start_turns,n_change,n_turn,name_user)
        if change:
            n_change+=1
        if start_turns:
            messages = None
            messages_online = None
            n_change = 0
            name_log = "log_"+ time.strftime("%Y%m%d_%H%M%S")
            n_turn=0
        n_turn+=1
        messages,messages_online = append_interaction_to_chat_log(question ,answ, messages,messages_online,name_log,name_user)
        message_reciv=False
  
  
