import time

import numpy as np
from model_mistral_da import Patient_DA_prediction, model_config
from openai import OpenAI
import torch

import json
from sentence_transformers import SentenceTransformer

class UserMI:

    def __init__(self,n_parra, type=None):
        self.n_parra = n_parra
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

        self.last_action = [0 for i in range(self.n_parra)]
        self.current_action = [0 for i in range(self.n_parra)]
        self.theme = [np.random.randint(3) for i in range(self.n_parra)]
        self.themes = ['Smoking', 'Drinking', 'Exercice']
        self.types_to_id = {'Receptive': 0, 'Resistant to change': 1, 'Open to change': 2}
        self.id_to_type = ['Receptive', 'Resistant to change', 'Open to change']
        if type is not None:
            self.type = [type for i in range(self.n_parra)]
        else:
            self.type = [np.random.randint(3) for i in range(self.n_parra)]
        self.random_seed = 0
        self.user_model = Patient_DA_prediction(model_config)
        self.user_model = torch.load('user_text.pt',map_location='cpu')
        self.n_action = len(self.action_space)
        self.past_value_da = np.zeros((n_parra,23))
        self.past_time_features = np.zeros((n_parra,23))
        self.past_observed_mask = np.zeros((n_parra,23))
        self.rapport = [0 for i in range(self.n_parra)]
        self.context = [0 for i in range(self.n_parra)]
        self.perspective = [0 for i in range(self.n_parra)]
        self.last_therapist_text = ["" for i in range(self.n_parra)]
        self.therapist_text = ["" for i in range(self.n_parra)]
        self.last_patient_text = ["" for i in range(self.n_parra)]
        self.patient_text = ["" for i in range(self.n_parra)]
        self.text_context = ["Context: " for i in range(self.n_parra)]
        self.turn = [0 for i in range(self.n_parra)]
        self.cluster = False

        self.encoded_text = torch.zeros((n_parra,768))
        if self.cluster:
            from vllm import LLM, SamplingParams
            self.llm = LLM(model="/home/galland/mistral_models/NemoInstruct", max_model_len=5000,
                       tokenizer_mode="mistral", load_format="mistral", config_format="mistral")
            self.embedding_model = SentenceTransformer("/home/galland/mistral_models/nomic",trust_remote_code=True)
        else:
            self.embedding_model = SentenceTransformer("nomic-ai/nomic-embed-text-v1.5",trust_remote_code=True)

    def create_context_i(self,i, turn):
        if self.last_therapist_text[i] == "":
            if self.last_patient_text[i] == "":
                if self.therapist_text[i] == "":
                    return ""
                else:
                    return "Context turn " + str(turn) + ": Therapist : " + self.therapist_text[i]
            else:
                return "Context turn " + str(
                    turn) + ": Patient : " + self.last_patient_text[i] + " Therapist : " + self.therapist_text[i]
        else:
            return "Context turn " + str(
                turn) + ": Therapist : " + self.last_therapist_text[i] + " Patient : " + self.patient_text[i] + " Therapist : " + self.therapist_text[i]
    def create_context(self, turn):
        res = []
        for i in range(self.n_parra):
            res.append(self.create_context_i(i,turn))
        return res
    def create_context_therapist_i(self,i, turn):
        if self.last_therapist_text[i] == "":
            if self.last_patient_text[i] == "":
                if self.patient_text[i] == "":
                    return ""
                else:
                    return "Context turn " + str(turn) + ": Patient : " + self.patient_text[i]
            else:
                return "Context turn " + str(
                    turn) + ": Therapist : " + self.last_therapist_text[i] + " Patient : " + self.patient_text[i]
        else:
            return "Context turn " + str(
                turn) + ": Patient : " + self.last_patient_text[i] + " Therapist : " + self.therapist_text[i] + " Patient : " + self.patient_text[i]
    def create_context_therapist(self, turn):
        res = []
        for i in range(self.n_parra):
            res.append(self.create_context_therapist_i(i,turn))
        return res

    def react(self, agent_da, turn_id,baseline=False):
        self.last_therapist_text = self.therapist_text
        self.turn = [self.turn[i] +1 for i in range(self.n_parra)]
        error = True
        essai = 0
        self.therapist_text = [" test" for i in range(self.n_parra)]


        for i in range(self.n_parra):
            self.text_context[i] += "Therapist: "
            self.text_context[i]+=self.therapist_text[i]
            self.past_value_da[i]= np.concatenate((self.past_value_da[i,1:],[agent_da[i] + self.n_action + 2]))
            self.past_time_features[i]=np.concatenate((self.past_time_features[i,1:],[turn_id[i]]))
            self.past_observed_mask[i]=np.concatenate((self.past_observed_mask[i,1:],[1]))



        self.encoded_text = self.embedding_model.encode(["classification: " + c for c in self.create_context(turn_id)], convert_to_tensor=True)
        self.encoded_text = self.encoded_text.cpu()
        start = time.time()

        start = time.time()
        action = self.user_model.generate(torch.tensor(self.past_value_da,dtype=torch.float),
                                           torch.tensor(self.past_time_features,dtype=torch.float).unsqueeze(1).permute(
                                               (0, 2, 1)), torch.tensor(self.past_observed_mask,dtype=torch.float),
                                           torch.tensor(self.type).unsqueeze(1),
                                           torch.tensor([turn_id[i] + 1 for i in range(self.n_parra)],dtype=torch.float).unsqueeze(1).unsqueeze(1).permute(
                                               (0, 2, 1)),self.encoded_text.to(torch.float))["pred"]


        error = True
        essai = 0
        type = [self.id_to_type[self.type[i]] for i in range(self.n_parra)]
        for i in range(self.n_parra):
            if self.type[i] == 0:
                if turn_id[i]  < 20:
                    type[i] += ' beginning of the dialogue'
                else:
                    type[i] += " end of the dialogue"
        if self.cluster:
            text = [" test" for i in range(self.n_parra)]

            for i in range(self.n_parra):
                self.patient_text[i] = text[i]

        for i in range(self.n_parra):
            self.text_context[i] += "Patient: "
            self.text_context[i] += self.patient_text[i]
            self.past_observed_mask[i] = np.concatenate((self.past_observed_mask[i,1:], [1]))
            self.past_value_da[i] = np.concatenate((self.past_value_da[i,1:], [action[i]]))
            self.past_time_features[i] = np.concatenate((self.past_time_features[i,1:], [turn_id[i] + 1]))

        self.last_action = self.current_action
        self.current_action = action
        self.last_patient_text = self.patient_text

        return action

    def get_reward(self, action):
        r = np.zeros(self.n_parra)

        for i in range(self.n_parra):
            if "[WRONG]" in self.patient_text:
                r[i] -= 15
            if self.action_space[action[i] ] == "Sharingpositivefeelingoremotion":
                self.rapport[i] += 1
                r[i] += 0
            elif self.action_space[action[i] ] == "UnderstandingorNewPerspective":
                r[i] += 0
                self.perspective[i] += 1
            elif self.action_space[action[i] ] == "GreetingorClosing":
                r[i] += 0
            elif self.action_space[action[i] ] == "Backchannel":
                r[i] += 0
            elif self.action_space[action[i] ] == "Sharingnegativefeelingoremotion":
                r[i] += 0
                self.rapport[i] += 1
            elif self.action_space[action[i] ] == "Changingunhealthybehaviorinthefuture":
                r[i] += 100
            elif self.action_space[action[i] ] == "Sustainingunhealthybehaviorinthefuture":
                r[i] -= 100
            elif self.action_space[action[i] ] == "SharingpersonalinformationorDescribepastevent":
                r[i] += 0
                self.context[i] += 1
            r[i] += self.rapport[i] / 4
            if self.rapport[i] > 5:
                r[i] += self.context[i] / 2
                if self.context[i] > 5:
                    r[i] += self.perspective[i]
            r[i] += min(self.perspective[i], 5) + min(self.context[i], 5)
        return r

    def reset(self, seed=None):
        self.last_action = [0 for i in range(self.n_parra)]
        self.current_action = [0 for i in range(self.n_parra)]
        self.random_seed = seed
        self.past_value_da = np.zeros((self.n_parra,23))
        self.past_time_features = np.zeros((self.n_parra,23))
        self.past_observed_mask = np.zeros((self.n_parra,23))
        self.rapport = [0 for i in range(self.n_parra)]
        self.context = [0 for i in range(self.n_parra)]
        self.text_context = ["Context: " for i in range(self.n_parra)]
        self.perspective = [0 for i in range(self.n_parra)]
        self.last_therapist_text = ["" for i in range(self.n_parra)]
        self.therapist_text =   ["" for i in range(self.n_parra)]
        self.last_patient_text = ["" for i in range(self.n_parra)]
        self.patient_text = ["" for i in range(self.n_parra)]
        self.theme = [np.random.randint(0,3) for i in range(self.n_parra)]

    def reset_i(self, i):
        self.last_action[i] = 0
        self.current_action[i] = 0
        self.past_value_da[i] = np.zeros(23)
        self.past_time_features[i] = np.zeros(23)
        self.past_observed_mask[i] = np.zeros(23)
        self.rapport[i] = 0
        self.context[i] = 0
        self.text_context[i] = "Context: "
        self.perspective[i] = 0
        self.last_therapist_text[i] = ""
        self.therapist_text[i] = ""
        self.last_patient_text[i] = ""
        self.patient_text[i] = ""
        self.theme[i] = np.random.randint(0,3)
    def set_type(self, type):
        self.type = [type for i in range(self.n_parra)]

    def seed(self, seed):
        self.random_seed = seed
