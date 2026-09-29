import os
import openai
import time
import socket
import pickle 
import argparse
import json
import struct
from openai import OpenAI
import sys
import io

# Force stdout to latin-1 so that French accented characters (é, ç, à, etc.)
# are output as single bytes matching Java's ISO-8859-1 reader.
#sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='latin-1', errors='replace', line_buffering=True)

from utils_mistral_online import LOCAL_MODE, LOCAL_BASE_URL, LOCAL_MODEL, ONLINE_MODEL
from therapist_behavior_inference import get_therapist_intent
from client_behavior_inference import get_client_intent
from RL_client import RL_client
import unicodedata
import re

import threading
from deep_translator import GoogleTranslator
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

current_silence = [0, 0, 0, 0]


def clean_answer(text: str) -> str:
    """
    Clean LLM output: fix mojibake accents (Mistral), normalize punctuation,
    strip control characters.
    """
    if not isinstance(text, str):
        text = str(text)

    # ------------------------------------------------------------------
    # 1) Repair "mojibake": UTF-8 bytes accidentally decoded as Latin-1.
    #    Symptoms: 'à' shows up as 'Ã ', 'é' as 'Ã©', 'ç' as 'Ã§', etc.
    # ------------------------------------------------------------------
    if any(marker in text for marker in ('Ã', 'Â', 'â\x80')):
        try:
            repaired = text.encode('latin-1', errors='strict').decode('utf-8', errors='strict')
            text = repaired
        except (UnicodeEncodeError, UnicodeDecodeError):
            # Mixed content — keep original, replacements below will still run.
            pass

    # ------------------------------------------------------------------
    # 2) Normalize to NFC so decomposed accents (e + U+0300) become the
    #    single precomposed codepoint (à) that text.replace() expects.
    # ------------------------------------------------------------------
    text = unicodedata.normalize('NFC', text)

    # First handle the replacement char explicitly
    text = text.replace('\ufffd', "'")  # U+FFFD → '

    # Normalize "fancy" punctuation to basic ASCII
    replacements = {
        '\u2019': "'",  # ’
        '\u2018': "'",  # ‘
        '\u201b': "'",  # ‛
        '\u201c': '"',  # “
        '\u201d': '"',  # ”
        '\u2013': '-',  # –
        '\u2014': '-',  # —
        '\u2026': '...',  # …
        '\xa0': ' ',
        ':' : '.',
        '(' : '.',
        ')' : '.',
        'déjà' : 'daija',
        'début' : 'dai bu',
        'à': 'a',
        'û': 'u',
        'Û': 'u',
        'À': 'a',
        'â': 'a',
        'contrôle': 'control',
        'peut-être' : 'peutaitre',
        'fatigué' : 'fatiguai',
        'gé': 'jai',
        'gè': 'jai',
        'Gè' : 'jai',
        'Gé' : 'jai',
        'cé' : 'ssai',
        'cè' : 'ssai',
        'tée' : 'tai',
        'té' : 'tai',
        'vée' : 'vai',
        'vé' : 'vai',
        'ée': 'ai',
        'là': 'la',
        'ï' : 'i',
        'î' : 'i',
        'é': 'ai',
        'è': 'ai',
        'ç': 'ss',
        'Ç': 'ss',
        'ça': 'cela',
        'Ça': 'cela',
        'É': 'ai',
        'Ê': 'ai',
        'ù': 'u',
        'ô': 'o',
        'ê': 'ai'

        # non-breaking space
    }
    for src, dst in replacements.items():
        text = text.replace(src, dst)

    # ------------------------------------------------------------------
    # Strip Markdown formatting that Mistral Nemo sometimes emits:
    #   **bold** / *italic* / leading "* " bullets / "- " bullets / "# " headers
    # The agent's TTS chokes on these characters when reading lists aloud.
    # ------------------------------------------------------------------
    # Bold/italic markers: drop the surrounding '*' but keep the inner text
    text = re.sub(r'\*\*([^*]+?)\*\*', r'\1', text)   # **bold**
    text = re.sub(r'\*([^*\n]+?)\*', r'\1', text)     # *italic*
    # Bullet lines: turn "  * foo" or "- foo" or "# foo" at line start into "foo"
    text = re.sub(r'(?m)^\s*[\*\-#]+\s+', '', text)
    # Any remaining stray '*' (incomplete pairs) -> space
    text = text.replace('*', ' ')

    # (Optional) strip control chars except common whitespaces
    text = ''.join(
        ch if (ch >= ' ' or ch in '\n\r\t') else ' '
        for ch in text
    )

    # Drop characters outside ISO-8859-1 range so the Java reader stays happy;
    # all French accented letters (e, c, a, etc.) survive this step.
    #text = text.encode('latin-1', errors='ignore').decode('latin-1')

    return text


def strip_end_markers(text):
    """
    Remove the conversation-control tokens the LLM is prompted to append
    (END_CONVO / END_CONVERSATION / ENDCONVO) so they are never spoken by the
    agent or shown. These are internal control signals, not dialogue content.

    NOTE: this is applied ONLY to the text that is streamed to TTS/MODIFF.
    The raw token must be kept in `answer` so the end-of-conversation detection
    (`"END_CONVO" in answer`) keeps working.
    """
    if not isinstance(text, str):
        return text
    # Match the longest variant first; the token is often glued to the next word
    # (e.g. "END_CONVOMerci"), so we must NOT rely on a trailing word boundary.
    return re.sub(r'END[_ ]?CONVERSATION|END[_ ]?CONVO|END[_ ]?CONV', '', text, flags=re.IGNORECASE)


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
if LOCAL_MODE:
    model = LOCAL_MODEL
    client_online = OpenAI(base_url=LOCAL_BASE_URL, api_key="ollama")
else:
    from mistralai.client import Mistral
    api_key_file = os.path.join(os.path.dirname(__file__), 'api_key.txt')
    with open(api_key_file, 'r', encoding='utf-8-sig') as f:
        MISTRAL_API_KEY = f.read().strip()
    model = ONLINE_MODEL
    client_online = Mistral(api_key=MISTRAL_API_KEY)
start_turns = False
RL = RL_client()
n_turn=0
name_user=""
name_log = "log_"+ time.strftime("%Y%m%d_%H%M%S")


def _parse_micounselor_incoming_message(raw):
    """
    This Python script is launched by MICounselorRL.java, which sends:
      model#SEP#language#SEP#condition#SEP#type#SEP#subject#SEP#user_text#SEP#system_prompt
    (7 fields = 6 '#SEP#' separators)

    user_text and system_prompt may legitimately contain '#SEP#', so we:
      - rsplit once to peel off system_prompt (trailing field)
      - split the prefix with maxsplit=5 so the 6th piece keeps any extra
        '#SEP#' that happened to be inside the user utterance.

    NO hardcoded fallbacks: if the message is truncated (e.g. socket cut),
    we must fail loudly rather than silently substitute "Drinking" / "Baseline"
    which would force the wrong topic prompt and leak fake words into the LLM.
    """
    if raw is None or "#SEP#" not in raw:
        raise ValueError(f"MICounselorIncrementalDA: malformed message (missing #SEP#): {raw!r}")
    prefix, system_prompt = raw.rsplit("#SEP#", 1)
    head = prefix.split("#SEP#", 5)
    if len(head) != 6:
        snippet = raw if len(raw) <= 200 else raw[:200] + "...[truncated]"
        raise ValueError(
            f"MICounselorIncrementalDA: expected 7 #SEP#-separated fields "
            f"(model|lang|condition|type|subject|user_text|system_prompt), "
            f"got {len(head)} header parts. Raw len={len(raw)} bytes. Snippet: {snippet!r}"
        )
    model, language, condition, type_, subject, user_question = head
    return model, language, condition, type_, subject, user_question, system_prompt


def ask(question,messages=None,messages_online=None,start_turns=False,n_change=0,n_turn=0,name_user=""):
    
    # print(question)

    model, language, condition, type, subject, question, system_prompt = _parse_micounselor_incoming_message(question)
    try:
        question = question.encode().decode('utf-8')
    except:
        pass
    
    RL.set_type(type)


    if question == "LAUNCHCONV":
        start_phrase = open(os.path.join(os.path.dirname(__file__), "prompts/" + language + "_start.txt"), "r").read()
        serv.send_message([start_phrase, ""])
        print(start_phrase)

       # en = GoogleTranslator(source='fr', target='en').translate(text=start_phrase)
        en = start_phrase
        RL.update_therapist_text(en)
        print("STOP")
        return "",start_phrase,False,True,name_user,"",""
    if question == "STOPCONV":
        if n_change > 2:
            stop_phrase = open(os.path.join(os.path.dirname(__file__), "prompts/" + language + "_stop_good.txt"), "r").read()
        else:
            stop_phrase = open(os.path.join(os.path.dirname(__file__), "prompts/" + language + "_stop_bad.txt"), "r").read()
        print(stop_phrase)
        #en = GoogleTranslator(source='fr', target='en').translate(text=stop_phrase)
        en = stop_phrase
        RL.update_therapist_text(en)
        print("STOP")
        return "",stop_phrase,False,True,name_user,"",""
    if question == "STOPCONVGOOD":

        stop_phrase = open(os.path.join(os.path.dirname(__file__), "prompts/" + language + "_stop_good.txt"), "r").read()

        print(stop_phrase)
        #en = GoogleTranslator(source='fr', target='en').translate(text=stop_phrase)
        en = stop_phrase
        RL.update_therapist_text(en)
        print("STOP")
        return "",stop_phrase,False,True,name_user,"",""
    if model == 'Local':
        print('"error')
    else:
        q,a,c,name_user,da,therapist_da = ask_online_chunk(question,language,condition,type,subject,system_prompt, messages_online,start_turns,n_change,n_turn,name_user)
        return q,a,c,False,name_user,da,therapist_da


def get_name(question,language):

    prompt =  open(os.path.join(os.path.dirname(__file__), "prompts/" + language + "_get_name.txt"), "r").read()
    msgs = [{"role": "user", "content": prompt+question}]
    if LOCAL_MODE:
        chat_response = client_online.chat.completions.create(model=model, messages=msgs)
    else:
        chat_response = client_online.chat.complete(model=model, messages=msgs)
    return chat_response.choices[0].message.content


 
def ask_online_chunk(question,language,condition,type,subject,system_prompt,messages=None,start_turns=False,n_change=0,n_turn=0,name_user=""):
    welcome=""
    mi_prompt = open(os.path.join(os.path.dirname(__file__), "prompts/" + language + "_mi.txt"), "r",  encoding="utf-8").read()
    subject_prompt = open(os.path.join(os.path.dirname(__file__), "prompts/" + language + "_" + subject + ".txt"),"r",  encoding="utf-8").read()
    descr_type= open(os.path.join(os.path.dirname(__file__), "prompts/" + language + "_" + type + ".txt"),"r",  encoding="utf-8").read()
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
        {"role": "user", "content": "[INST]"+subject_prompt+mi_prompt+system_prompt + cond_prompt+"[/INST]"}
         ] 
    context=""
    if messages is not None:
        l=len(messages)
        for i,msg in enumerate(messages):
            prompt.append(msg)
            if l-i<2:
                if msg["role"] =='user':
                    context += "Patient: "
                else:
                    context += "Therapist:"
                context+= msg["content"]

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
            serv.send_message([welcome,context])
            #print(welcome)
            name_user=""

        else :
            welcome = open(os.path.join(os.path.dirname(__file__), "prompts/" + language + "_name.txt"), "r").read() + name_user
            serv.send_message([welcome,context])
           # print(welcome)
            
        time.sleep(2)
    if condition=="RL":
        therapist_da = RL.step(question,da)

    #print("Therapist Action: "+str(therapist_da))
        if welcome != "":
            if language == "FR":
                prompt.append({"role": "user", "content": question + "Therapist : "+welcome+ "COMPLETE LA FIN DU TOUR EN REALISANT L'ACTION:"+str(therapist_da)})
            else:
                prompt.append({"role": "user", "content": question + "Therapist : "+welcome+ "COMPLEATE THE END OF THE TURN BY PERFORMING THE ACTION:"+str(therapist_da)})
        if language == "FR":
            prompt.append({"role": "user", "content": question + "REALISE L'ACTION:"+str(therapist_da)})
        else:
            prompt.append({"role": "user", "content": question + "PERFORM THE ACTION:"+str(therapist_da)})
    else:
        therapist_da="Baseline"
        if welcome != "":
            if language == "FR":
                prompt.append({"role": "user", "content": question + "Therapist : "+welcome+ "COMPLETE LA FIN DU TOUR AVEC UNE PHRASE"})
            else:
                prompt.append({"role": "user", "content": question + "Therapist : "+welcome+ "COMPLEATE THE END OF THE TURN WITH A SENTENCE"})
        else:
            prompt.append({"role": "user", "content": question})

   
    #create_server(da,port =50201)
    context += "Patient: "+question
    answer = ""
    curr_sent= ""

    min_response_time = 1
    start = time.perf_counter()
    FIRST_SENTENCE = True

    def _run_stream(client_inst):
        """Run the streaming loop.  Returns (answer_so_far, curr_sent_remainder).
        Raises on any stream-level failure."""
        nonlocal answer, curr_sent, FIRST_SENTENCE, start
        if LOCAL_MODE:
            response = client_inst.chat.completions.create(model=model, messages=prompt, stream=True)
        else:
            response = client_inst.chat.stream(model=model, messages=prompt)
        try:
            current_silence = [0, 0, 0, 0]
            serv_da.send_message([da, str(therapist_da), current_silence])
            for event in response:
                chunk = event.data if not LOCAL_MODE else event
                if chunk.choices[0].delta.content is None:
                    pass
                elif chunk.choices[0].delta.content in [".", "?", "!", ";", " ?", " .", ". "]:
                    curr_sent += chunk.choices[0].delta.content
                    if answer != "":
                        answer = clean_answer(answer)
                        response_time = time.perf_counter() - start
                        if response_time < min_response_time:
                            time.sleep(min_response_time - response_time)
                    start = time.perf_counter()
                    answer += curr_sent
                    answer = clean_answer(answer)

                    if FIRST_SENTENCE:
                        curr_sent = clean_answer(curr_sent)
                        curr_sent = strip_end_markers(curr_sent)
                        serv.send_message([curr_sent, context])
                        try:
                            print("START:" + curr_sent.encode().decode('utf-8'))
                        except:
                            print("START:" + curr_sent)
                        FIRST_SENTENCE = False
                    else:
                        curr_sent = clean_answer(curr_sent)
                        curr_sent = strip_end_markers(curr_sent)
                        serv.send_message([curr_sent, context])
                        try:
                            print(curr_sent.encode().decode('utf-8', errors='replace'))
                        except:
                            print(curr_sent)

                    curr_sent = ""
                else:
                    curr_sent += chunk.choices[0].delta.content
                    curr_sent = clean_answer(curr_sent)
        finally:
            # Drain any remaining data so httpx connection pool stays clean
            try:
                if hasattr(response, 'close'):
                    response.close()
                else:
                    for _ in response:
                        pass
            except Exception:
                pass

    # Attempt streaming up to 2 times before falling back to non-streaming
    global client_online
    stream_ok = False
    current_silence = [0, 1, 0, 0]  #client finished, therapist not yet responded
    
    serv_da.send_message([da, str(therapist_da), current_silence])

    for _attempt in range(2):
        try:
            _run_stream(client_online)
            stream_ok = True
            break
        except Exception as e:
            print(f"[MICounselorIncrementalDA] stream attempt {_attempt+1} failed: {e.__class__.__name__}: {e}")
            try:
                if LOCAL_MODE:
                    client_online = OpenAI(base_url=LOCAL_BASE_URL, api_key="ollama")
                else:
                    client_online = Mistral(api_key=MISTRAL_API_KEY)
            except Exception:
                pass
            time.sleep(0.5)

    if not stream_ok:
        print("Laisser moi un instant")
        try:
            if LOCAL_MODE:
                client_online = OpenAI(base_url=LOCAL_BASE_URL, api_key="ollama")
                response_full = client_online.chat.completions.create(model=model, messages=prompt)
            else:
                client_online = Mistral(api_key=MISTRAL_API_KEY)
                response_full = client_online.chat.complete(model=model, messages=prompt)
            full_answer = response_full.choices[0].message.content
            if full_answer is None:
                full_answer = ""
            full_answer = clean_answer(full_answer)
            if len(full_answer) > 0:
                spoken_full = strip_end_markers(full_answer)
                serv.send_message([spoken_full, context])
                try:
                    print("START:" + spoken_full.encode().decode('utf-8', errors='replace'))
                except:
                    print("START:" + spoken_full)
                answer += full_answer
                answer = clean_answer(answer)
            curr_sent = ""
        except Exception as e2:
            safe_fallback = "Desole, je rencontre un probleme technique. Pouvez-vous repeter ?"
            serv.send_message([safe_fallback, context])
            answer += safe_fallback
    time.sleep(min_response_time )
    if curr_sent != "":
        #da = get_therapist_intent(curr_sent,context)
        curr_sent = clean_answer(curr_sent)
        spoken_curr = strip_end_markers(curr_sent)
        serv.send_message([spoken_curr, context])
        try:
            print(spoken_curr.encode().decode('utf-8'))
        except:
            print(spoken_curr)
     #  create_server(da,port =50200)
        answer += curr_sent
        answer = clean_answer(answer)

    answer = answer.replace('\n', ' ')
    answer = answer.replace('[', ' ')
    answer = answer.replace(']', ' ')
    answer = clean_answer(answer)

    if ("END_CONVO" in answer and n_turn >=8 and condition!="RL") or (condition=="RL" and "Closing" in therapist_da and n_turn >=8) or (condition=="RL" and "clôture" in therapist_da and n_turn >=10):
        if n_change > 2:
            stop_phrase = open(os.path.join(os.path.dirname(__file__), "prompts/" + language + "_stop_good.txt"), "r", encoding="utf-8").read()
            stop_phrase = clean_answer(stop_phrase)
        else:
            stop_phrase = open(os.path.join(os.path.dirname(__file__), "prompts/" + language + "_stop_bad.txt"), "r", encoding="utf-8").read()
            stop_phrase = clean_answer(stop_phrase)
        #serv.send_message([stop_phrase, context])
        print(stop_phrase)
    else:
        stop_phrase=""
    # Detection above (line ~591) already consumed the END_CONVO marker; now strip
    # it from the text that is logged and pushed back into the conversation history
    # so the model never sees it again and never repeats it.
    answer = strip_end_markers(answer)
    print("STOP")
    #en = GoogleTranslator(source='fr', target='en').translate(text=answer)
    en=answer
    RL.update_therapist_text(en)
    
    therapist_da_str = str(therapist_da) if condition == "RL" else "other"
    serv_da.send_message([da, therapist_da_str, [0, 0, 0, 0]])

    return question,welcome+answer+stop_phrase  ,change ,name_user,da, therapist_da
def append_interaction_to_chat_log(question, answer, messages=None,messages_online=None,name_log=None,name_user="",da="",therapist_da=""):
    if name_log is not None:
        # Open the file in append mode
        answer = clean_answer(answer)
        with open(os.path.join(os.path.dirname(__file__), "logs/" + name_log + ".txt"), 'a+', encoding = "utf-8") as file:
            #Append content to the file
            if question != "":
                if name_user != "":
                    file.write("User: ["+da+']'+question.replace(name_user,"[NAME]")+"\n")
                else:
                    file.write("User: ["+da+']'+question+"\n")
            if name_user != "":
                file.write("Agent: ["+therapist_da+']'+answer.replace(name_user,"[NAME]")+"\n")
            else:
                file.write("Agent: ["+therapist_da+']'+answer+"\n")


    if messages is None:
        messages = []
    if messages_online is None:
        messages_online = []
    messages.append({"role":"user", "content":question})
    messages.append({"role":"assistant", "content":answer})
    messages_online.append({"role": "user", "content": question})
    messages_online.append({"role": "assistant", "content": answer})
    return messages,messages_online

#if __name__ == "__main__":
#     serv = DA_Server(port=5555)
#     serv2 = DA_Server(port=5556)
#     answer = ask("online#SEP#FR#SEP#RL#SEP#Resistant#SEP#Drinking#SEP#Je suis fatiguee#SEP#")
#     print(answer)
#     sys.exit()

parser=argparse.ArgumentParser()
parser.add_argument("port", help="server port", type=int, default="4000")


args=parser.parse_args()
serv = DA_Server(port=5555)
serv2 = DA_Server(port=5556)
serv_da = DA_Server(port=50202)
port = args.port
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.connect(("localhost", port))

# Java's Server.sendMessage uses out.println(msg) on an ISO-8859-1 PrintWriter,
# so each logical message is terminated with '\n'. A single message can easily
# exceed 1024 bytes (long user transcript + 7 #SEP# fields + system_prompt),
# so we MUST accumulate bytes until we see '\n' before parsing — otherwise
# the parser sees a truncated message with missing #SEP# fields.
_recv_buffer = bytearray()

def _recv_line():
    """Block until a complete '\\n'-terminated line is received from Java."""
    global _recv_buffer
    while b'\n' not in _recv_buffer:
        chunk = s.recv(4096)
        if not chunk:
            return None  # Java side closed
        _recv_buffer.extend(chunk)
    line, _, rest = _recv_buffer.partition(b'\n')
    _recv_buffer = bytearray(rest)
    # Strip trailing '\r' if Java emitted CRLF
    if line.endswith(b'\r'):
        line = line[:-1]
    return line.decode('iso-8859-1', errors='replace')

while True:
    msg = _recv_line()
    if msg is None:
        print("[MICounselorIncrementalDA] Java side closed the connection, exiting")
        break
    if len(msg) == 0:
        continue
    if msg == "exit":
        break
    question,answ,change,start_turns,name_user,da,therapist_da=ask(msg, messages,messages_online,start_turns,n_change,n_turn,name_user)
    current_silence = [1, 0, 0, 0]
    serv_da.send_message([da, str(therapist_da), current_silence])
    if change:
        n_change+=1
    if start_turns:
        messages = None
        messages_online = None
        n_change = 0
        name_log = "log_"+ time.strftime("%Y%m%d_%H%M%S")
        n_turn=0
    n_turn+=1
    messages,messages_online = append_interaction_to_chat_log(question ,answ, messages,messages_online,name_log,name_user,da,therapist_da)
  
  
