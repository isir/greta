import time

import numpy as np
from model_da import Patient_DA_prediction, model_config

import torch

import json


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
             "Backchannel"]#, "AskingforMedicalInformation"]
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

        self.last_action = 0
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
        self.user_model = torch.load('user.pt',map_location=torch.device('cpu'))
        self.n_action = len(self.action_space)
        self.past_value_da = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
        self.past_time_features = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
        self.past_observed_mask = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
        self.rapport = 0
        self.context = 0
        self.perspective = 0
        self.last_therapist_text = ""
        self.therapist_text = ""
        self.last_patient_text = ""
        self.patient_text = ""
        self.text_context = "Context: "
        self.turn = 0
        #self.client = OpenAI(base_url="http://localhost:1234/v1", api_key="lm-studio")
        self.encoded_text = torch.zeros(768)
        #self.llm = LLM(model="/home/galland/mistral_models/NemoInstruct", max_model_len=120000,
        #               tokenizer_mode="mistral", load_format="mistral", config_format="mistral")

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

    def create_context_therapist(self, turn):
        if self.last_therapist_text == "":
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

    def react(self, agent_da, turn_id):
        self.last_therapist_text = self.therapist_text
        self.turn += 1


        self.text_context += "Therapist: "
        self.text_context += self.therapist_text
        self.past_value_da.append(agent_da + self.n_action + 2)
        self.past_time_features.append(turn_id)
        self.past_observed_mask.append(1)
        self.past_value_da = self.past_value_da[2:]
        self.past_time_features = self.past_time_features[2:]
        self.past_observed_mask = self.past_observed_mask[2:]

        # self.encoded_text = self.client.embeddings.create(input=[self.create_context()], model="nomic-ai/nomic-embed-text-v1.5-GGUF").data[0].embedding
        action = self.user_model.generate(torch.tensor(self.past_value_da).unsqueeze(0), torch.tensor(self.past_time_features).unsqueeze(0).unsqueeze(0).permute((0,2,1)), torch.tensor(self.past_observed_mask).unsqueeze(0), torch.tensor(self.type).unsqueeze(0).unsqueeze(0),
                 torch.tensor(turn_id+1).unsqueeze(0).unsqueeze(0).unsqueeze(0).permute((0,2,1)))["pred"].item()

        self.text_context += "Patient: "
        self.text_context += self.patient_text
       # print("Patient: ", self.patient_text)
        self.past_observed_mask.append(1)
        self.past_value_da.append(action)
        self.past_time_features.append(turn_id + 1)

        self.last_action = self.current_action
        self.current_action = action
        self.last_patient_text = self.patient_text

        return action

    def get_reward(self, action):
        r = 0
       # print(self.action_space[action])
        if "[WRONG]" in self.patient_text:
            r -= 15
        if self.action_space[action] == "Sharingpositivefeelingoremotion":
            self.rapport += 1
            r += 1
        elif self.action_space[action] == "UnderstandingorNew Perspective":
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
        self.past_value_da = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
        self.past_time_features = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
        self.past_observed_mask = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
        self.rapport = 0
        self.context = 0
        self.text_context = "Context: "
        self.perspective = 0
        self.last_therapist_text = ""
        self.therapist_text = ""
        self.last_patient_text = ""
        self.patient_text = ""
        self.theme = np.random.randint(0, 3)

    def set_type(self, type):
        self.type = type

    def seed(self, seed):
        self.random_seed = seed
