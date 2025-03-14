import numpy as np
from Subpolicies2 import Subpolicies
from Discrete_SAC_Agent2 import SACAgent
from gym import spaces
class MLSH_agent:

    def __init__(self,environment,num_subpolicies,master_len_replay_buffer=5000,sub_len_replay_buffer=5000,master_replay_mini_batch_size=100,sub_replay_mini_batch_size=100,master_learning_rate=10**-3,sub_learning_rate=10**-4,master_mbpo=False,use_maml=False):
        self.environment = environment
        self.observation_space = self.environment.observation_space
        self.master_observation_space = self.environment.master_observation_space
        self.action_space = self.environment.action_space
        self.master_action_space = spaces.Discrete(num_subpolicies)
        self.master_policy = SACAgent(self.master_observation_space, self.master_action_space,len_replay_buffer=master_len_replay_buffer,replay_mini_batch_size=master_replay_mini_batch_size,learning_rate=master_learning_rate,mbpo=master_mbpo,use_maml=use_maml)
        self.subpolicies = Subpolicies(self.observation_space, self.action_space,len_replay_buffer=sub_len_replay_buffer,replay_mini_batch_size=sub_replay_mini_batch_size,num_subpolicies=num_subpolicies,learning_rate=sub_learning_rate)

    def save_agent(self, path):
        self.master_policy.save(path+'/master')
        self.subpolicies.save(path+'/subpolicies')    
    def reset_master_policy(self):
        self.master_policy.reset()
    def get_next_master_action(self, master_state, evaluation_episode=False):
        if evaluation_episode:
            discrete_action = self.master_policy.get_action_deterministically(master_state)
        else:
            discrete_action = self.master_policy.get_action_nondeterministically(master_state)
        return discrete_action
    def get_next_action(self, state,subpolicy, evaluation_episode=False):
        if evaluation_episode:
            discrete_action = self.subpolicies.get_action_deterministically(subpolicy,state)
        else:
            discrete_action = self.subpolicies.get_action_nondeterministically(subpolicy,state)
        return discrete_action

    def train_on_transition(self, state, discrete_action, next_state, reward, done,subpolicy):
        if len(state.shape) > 1:
            for i in range(len(state)):
                transition = (state[i], discrete_action[i], reward[i], next_state[i], done[i],subpolicy[i])
                self.subpolicies.train_on_transition(transition)
        else:
            transition = (state, discrete_action, reward, next_state, done,subpolicy)
            self.subpolicies.train_on_transition(transition)

    def train_on_master_transition(self, master_state, subpolicy, next_master_state, master_reward, done):
        if len(master_state.shape)>1:
            for i in range(len(master_state)):
                transition = (master_state[i], subpolicy[i], master_reward[i], next_master_state[i], done[i])
                self.master_policy.train_networks(transition)
        else:
            transition = (master_state, subpolicy, master_reward, next_master_state, done)
            self.master_policy.train_networks(transition)

    def add_master_transition(self, master_state, subpolicy, master_reward, next_master_state, done):
        if len(master_state.shape) > 1:
            for i in range(len(master_state)):
                transition = (master_state[i], subpolicy[i], master_reward[i], next_master_state[i], done[i])
                self.master_policy.replay_buffer.add_transition(transition)
        else:
            transition = (master_state, subpolicy, master_reward, next_master_state, done)
            self.master_policy.replay_buffer.add_transition(transition)