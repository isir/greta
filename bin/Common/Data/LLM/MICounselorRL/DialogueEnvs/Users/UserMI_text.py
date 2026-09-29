import time

import numpy as np
from model_mistral_da import Patient_DA_prediction, model_config
from openai import OpenAI
import torch
from client_behavior_generation import generate_client_intent_vllm
from therapist_behavior_generation import generate_therapist_intent_vllm,generate_therapist_intent_vllm_parra_baseline
import json
from sentence_transformers import SentenceTransformer

class UserMI:

    def __init__(self, type=None):

        self.action_space = \
            ["SharingpersonalinformationorDescribepastevent",
             "Changingunhealthybehaviorinthefuture",
             "Sustainingunhealthybehaviorinthefuture",
             "Sharingnegativefeelingoremotion",
             "Sharingpositivefeelingoremotion",
             "UnderstandingorNewPerspective",
             "GreetingorClosing",
             "Backchannel"]
        self.agent_action_space = \
            ["Reflection",
             "Ask for Information",
             "Invite to Shift Outlook",
             "Ask about current Emotions",
             "Give Solution",
             "Planning with the Patient",
             "Experience Normalization and Reassurance",
             "Medical Education and Guidance",
             "Greeting or Closing",
             "Backchannel",
             "Ask for Consent or Validation",
             "Progress Acknowledgment and Encouragement",
             "Empathic Reaction", "Unknown"]
        self.termination_da = 8
        self.action_to_id = {}
        for i, a in enumerate(self.action_space):
            self.action_to_id[a] = i

        self.last_action =0
        self.current_action = 0
        self.theme = np.random.randint(3)
        self.themes = ['Smoking', 'Drinking', 'Exercice']
        self.types_to_id = {'Receptive': 0, 'Resistant to change': 1, 'Open to change': 2}
        self.id_to_type = ['Receptive', 'Resistant to change', 'Open to change']
        if type is not None:
            self.type = type
        else:
            self.type = np.random.randint(3)
        self.random_seed = 0
        self.user_model = Patient_DA_prediction(model_config)
        self.user_model = torch.load('user_text.pt',map_location='cpu')
        self.n_action = len(self.action_space)
        self.past_value_da = np.zeros((23))
        self.past_time_features = np.zeros((23))
        self.past_observed_mask = np.zeros((23))
        self.rapport = 0
        self.context = 0
        self.perspective = 0
        self.last_therapist_text = ""
        self.therapist_text = ""
        self.last_patient_text = ""
        self.patient_text = ""
        self.text_context = "Context: "
        self.turn = 0
        self.cluster = True

        self.encoded_text = torch.zeros((768))
        if self.cluster:
            from vllm import LLM, SamplingParams
            self.llm = LLM(model="/home/galland/mistral_models/NemoInstruct", max_model_len=5000,
                       tokenizer_mode="mistral", load_format="mistral", config_format="mistral")
            self.embedding_model = SentenceTransformer("/home/galland/mistral_models/nomic",trust_remote_code=True)
        else:
            self.embedding_model = SentenceTransformer("nomic-ai/nomic-embed-text-v1.5",trust_remote_code=True)

    def create_context(self, turn):
        if self.last_therapist_text == "":
            if self.last_patient_text == "":
                if self.therapist_text == "":
                    return ""
                else:
                    return "Context turn " + str(turn) + ": Therapist : " + self.therapist_text
            else:
                return "Context turn " + str(
                    turn) + ": Patient : " + self.last_patient_text + " Therapist : " + self.therapist_text
        else:
            return "Context turn " + str(
                turn) + ": Therapist : " + self.last_therapist_text + " Patient : " + self.patient_text + " Therapist : " + self.therapist_text

    def create_context_therapis(self, turn):
        if self.last_therapist_text== "":
            if self.last_patient_text == "":
                if self.patient_text == "":
                    return ""
                else:
                    return "Context turn " + str(turn) + ": Patient : " + self.patient_text
            else:
                return "Context turn " + str(
                    turn) + ": Therapist : " + self.last_therapist_text + " Patient : " + self.patient_text
        else:
            return "Context turn " + str(
                turn) + ": Patient : " + self.last_patient_text + " Therapist : " + self.therapist_text + " Patient : " + self.patient_text


    def react(self, agent_da, turn_id,baseline=False):
        self.last_therapist_text = self.therapist_text
        self.turn = self.turn +1
        error = True
        essai = 0
        if self.cluster:
            if baseline:
                self.therapist_text,agent_da = generate_therapist_intent_vllm_parra_baseline(self.llm, self.text_context,
                                                                 'DA' ,
                                                                 theme=self.themes[self.theme] )
            else :
                self.therapist_text = generate_therapist_intent_vllm(self.llm, self.text_context,
                                                                 'DA' ,
                                                                 theme=self.themes[self.theme] )
        else:
            self.therapist_text = "text"

        self.text_context += "Therapist: "
        self.text_context+=self.therapist_text
        self.past_value_da= np.concatenate((self.past_value_da[1:],[agent_da + self.n_action + 2]))
        self.past_time_features=np.concatenate((self.past_time_features[1:],[turn_id]))
        self.past_observed_mask=np.concatenate((self.past_observed_mask[1:],[1]))



        self.encoded_text = self.embedding_model.encode(["classification: " +  self.create_context(turn_id)], convert_to_tensor=True)
        self.encoded_text = self.encoded_text.cpu()
        start = time.time()
       # action = [self.user_model.generate(torch.tensor(self.past_value_da[i],dtype=torch.float).unsqueeze(0), torch.tensor(self.past_time_features[i],dtype=torch.float).unsqueeze(0).unsqueeze(0).permute((0,2,1)), torch.tensor(self.past_observed_mask[i],dtype=torch.float).unsqueeze(0), torch.tensor(self.type[i]).unsqueeze(0).unsqueeze(0),
       #     torch.tensor(turn_id[i]+1,dtype=torch.float).unsqueeze(0).unsqueeze(0).unsqueeze(0).permute((0,2,1)),self.encoded_text[i].unsqueeze(0).to(torch.float))["pred"].item() for i in range(self.n_parra)]
        start = time.time()
        action = self.user_model.generate(torch.tensor(self.past_value_da,dtype=torch.float).unsqueeze(0),
                                           torch.tensor(self.past_time_features,dtype=torch.float).unsqueeze(0).unsqueeze(1).permute(
                                               (0, 2, 1)), torch.tensor(self.past_observed_mask,dtype=torch.float).unsqueeze(0),
                                           torch.tensor(self.type).unsqueeze(0).unsqueeze(1),
                                           torch.tensor(turn_id + 1 ,dtype=torch.float).unsqueeze(0).unsqueeze(1).unsqueeze(1).permute(
                                               (0, 2, 1)),self.encoded_text.to(torch.float))["pred"].item()

        error = True
        essai = 0
        type = self.id_to_type[self.type]

        if self.type == 0:
            if turn_id  < 20:
                type += ' beginning of the dialogue'
            else:
                type += " end of the dialogue"
        if self.cluster:
            text = generate_client_intent_vllm(self.llm, self.text_context, 'DA',
                                               type=type,
                                               theme=self.themes[self.theme] ,
                                               intent=self.action_space[action] )
        else:
            text = " test"

        self.patient_text = text

        self.text_context += "Patient: "
        self.text_context += self.patient_text
        self.past_observed_mask = np.concatenate((self.past_observed_mask[1:], [1]))
        self.past_value_da = np.concatenate((self.past_value_da[1:], [action]))
        self.past_time_features = np.concatenate((self.past_time_features[1:], [turn_id + 1]))

        self.last_action = self.current_action
        self.current_action = action
        self.last_patient_text = self.patient_text

        return action

    def get_reward(self, action):
        r = 0
        if "[WRONG]" in self.patient_text:
            r -= 15
        if self.action_space[action] == "Sharingpositivefeelingoremotion":
            self.rapport += 1
            r += 1
        elif self.action_space[action] == "UnderstandingorNewPerspective":
            r += 5
            self.perspective += 1
        elif self.action_space[action] == "GreetingorClosing":
            r += 0
        elif self.action_space[action] == "Backchannel":
            r += 0
        elif self.action_space[action] == "Sharingnegativefeelingoremotion":
            r += 1
            self.rapport += 1
        elif self.action_space[action] == "Changingunhealthybehaviorinthefuture":
            r += 10
        elif self.action_space[action] == "Sustainingunhealthybehaviorinthefuture":
            r -= 10
        elif self.action_space[action] == "SharingpersonalinformationorDescribepastevent":
            r += 1
            self.context += 1

        return r

    def reset(self, seed=None):
        self.last_action = 0
        self.current_action = 0
        self.random_seed = seed
        self.past_value_da = np.zeros(23)
        self.past_time_features = np.zeros(23)
        self.past_observed_mask = np.zeros(23)
        self.rapport = 0
        self.context = 0
        self.text_context = "Context: "
        self.perspective = 0
        self.last_therapist_text = ""
        self.therapist_text = ""
        self.last_patient_text = ""
        self.patient_text = ""
        self.theme = np.random.randint(0,3)


    def set_type(self, type):
        self.type = type

    def seed(self, seed):
        self.random_seed = seed
