import numpy as np
from MLSH_agent3 import MLSH_agent
import gym

gym.register(
    id='DialogueEnv-v0',
    entry_point='DialogueEnvs.DialogueEnvMIparra:DialogueEnvMI'
)
class RL_client:

    def __init__(self):
        self.state = 0
        self.action = 0

        self.user_da_list = ["SharingpersonalinformationorDescribepastevent",
             "Changingunhealthybehaviorinthefuture",
             "Sustainingunhealthybehaviorinthefuture",
             "Sharingnegativefeelingoremotion",
             "Sharingpositivefeelingoremotion",
             "UnderstandingorNewPerspective",
             "GreetingorClosing",
             "Backchannel","AskingforMedicalInformation",'Unknown']

        self.agent_da_en = ["Reflection",
             "Ask for Information",
             "Invite to Shift Outlook",
             "Ask about current Emotions",
             "Give Solution",
             "Planning with the Patient",
             "Experience Normalization",
             "Medical Education",
             "Greeting or Closing",
             "Backchannel",
             "Ask for Consent",
             "Progress Acknowledgment",
             "Empathic Reaction","Unknown"]

        self.agent_da_fr = ["Réflexion",
             "Demande d'information",
             "Invitation à changer de perspective",
             "Questions sur les émotions actuelles",
             "Donner une solution",
             "Planification avec le patient",
             "Normalisation et réassurance de l'expérience",
             "Formation et orientation médicales",
             "Accueil ou clôture",
             "Backchannel",
             "Demander le consentement ou la validation",
             "Reconnaissance des progrès et encouragement",
             "Réaction empathique","Inconnue"]

        self.da_to_id = {da:i for i,da in enumerate(self.user_da_list)}
        
        self.turn_id = 0
        self.rapport=0
        self.perspective = 0
        self.context = 0
        self.user_da = np.zeros((1,len(self.user_da_list)-1))
        self.agent_da = np.zeros((1,len(self.agent_da_en)-1))
        self.user_da[0,-1] = 6
        self.agent_da[0,12] = 8
        self.env =  gym.make('DialogueEnv-v0',n_parra=1)

        self.agents = [MLSH_agent(self.env,4,master_len_replay_buffer=500,sub_len_replay_buffer=500,master_replay_mini_batch_size=10,sub_replay_mini_batch_size=10,master_learning_rate=10**-7,sub_learning_rate=10**-6,master_mbpo=False,use_maml=True),
                       MLSH_agent(self.env,4,master_len_replay_buffer=500,sub_len_replay_buffer=500,master_replay_mini_batch_size=10,sub_replay_mini_batch_size=10,master_learning_rate=10**-7,sub_learning_rate=10**-6,master_mbpo=False,use_maml=True),
                       MLSH_agent(self.env,4,master_len_replay_buffer=500,sub_len_replay_buffer=500,master_replay_mini_batch_size=10,sub_replay_mini_batch_size=10,master_learning_rate=10**-7,sub_learning_rate=10**-6,master_mbpo=False,use_maml=True)]
        self.agent = self.agents[0]
        self.subpolicy = [0]
        self.master_ob = np.array([[self.context/3,self.rapport/3,self.perspective/3,self.turn_id/40] ])
        self.ob = np.concatenate([self.master_ob,self.user_da,self.agent_da],axis=1)
        self.type_to_id = {'Resistant':0,"Open":1,"Hesitant":2}
        self.type = 0

    def reset(self):
        self.turn_id = 0
        self.rapport=0
        self.perspective = 0
        self.context = 0
        self.user_da = np.zeros((1,len(self.user_da_list)-1))
        self.agent_da = np.zeros((1,len(self.agent_da_en)-1))
        self.user_da[0,-1] = 6
        self.agent_da[0,12] = 8
        self.subpolicy = [0]
        self.master_ob = np.array([[self.context/3,self.rapport/3,self.perspective/3,self.turn_id/40] ])
        self.ob = np.concatenate([self.master_ob,self.user_da,self.agent_da],axis=1)
    def set_type(self,type):

        self.type = self.type_to_id[type]
        self.agent= self.agents[self.type]

    def update_obs(self):
        self.master_ob = np.array([[self.context/3,self.rapport/3,self.perspective/3,self.turn_id/40] ])
        self.ob = np.concatenate([self.master_ob,self.user_da,self.agent_da],axis=1)

    def step(self,da,lang='EN'):
        self.turn_id += 1 
        r = self.get_reward(da.replace(" ",""))
        try:
            self.user_state = self.da_to_id[da.replace(" ","")]
        except:
            self.user_state = self.da_to_id["Unknown"]
        self.update_obs()
        if self.turn_id %3 == 0:
            self.subpolicy =  self.agent.get_next_master_action(self.master_ob, evaluation_episode=True)
        self.action =  self.agent.get_next_action(self.ob,self.subpolicy, evaluation_episode=True)

        if lang=="FR":
            return self.agent_da_fr[self.action[0]]
        else:
             return self.agent_da_en[self.action[0]]


    def get_reward(self, action):
        r=0

        if action == "Sharingpositivefeelingoremotion" or action == "Partagerunsentimentouuneémotionpositive":
            self.rapport += 1
            r += 1
        elif action == "UnderstandingorNew Perspective" or action=="Compréhensionounouvelleperspective":
            r += 5
            self.perspective += 1
        elif action == "GreetingorClosing"or action=="Accueilouclôture":
            r += 0
        elif action == "Backchannel":
            r += 0
        elif action == "Sharingnegativefeelingoremotion"or action=="Partagerunsentimentouuneémotionnégative":
            r += 1
            self.rapport += 1
        elif action == "Changingunhealthybehaviorinthefuture"or action=="Modifierlescomportementsmalsainsàl'avenir":
            r += 10
        elif action == "Sustainingunhealthybehaviorinthefuture"or action=="Lemaintiend'uncomportementmalsainàl'avenir":
            r -= 10
        elif action == "SharingpersonalinformationorDescribepastevent"or action=="Partagerdesinformationspersonnellesoudécrireunévénementpassé":
            r += 1
            self.context += 1

        return r
