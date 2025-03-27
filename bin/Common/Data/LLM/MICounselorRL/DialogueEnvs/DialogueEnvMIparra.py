import gym
from gym import spaces
import numpy as np
from Users.UserMI_text_fake_parra import UserMI
import torch
class DialogueEnvMI(gym.Env):
    metadata = {}

    def __init__(self,n_parra=5, render_mode=None):


        self.n_parra = n_parra
        self.user = UserMI(self.n_parra,type=0)
        self.random_seed = 0
        self.agent_das =self.user.agent_action_space
        self.user_das = self.user.action_space
        self.n_user_da = len(self.user_das)
        self.n_agent_da = len(self.agent_das)-1
        self.turn_id =[0 for i in range(self.n_parra)]
        self.agent_last_da = [0 for i in range(self.n_parra)]
        self.last_user_da = [0 for i in range(self.n_parra)]
        self.conv = [[] for i in range(self.n_parra)]
        high = np.array(
            [
                1,
            ]*( self.n_agent_da+ self.n_user_da+2),
            dtype=np.int32,
        )
        low = np.array(
            [
                0,
            ] * (self.n_agent_da + self.n_user_da+2),
            dtype=np.int32,
        )
        master_high = np.array(
            [
                1,
            ] * (3),
            dtype=np.int32,
        )
        master_low = np.array(
            [
                0,
            ] * (3),
            dtype=np.int32,
        )
        # Observations are dictionaries with the agent's and the user's last actions.
        self.observation_space =  spaces.Box(low=low, high=high, dtype=np.float32)
        self.master_observation_space = spaces.Box(low=master_low, high=master_high, dtype=np.float32)
        # We have ._agent_da actions, corresponding to the dialogue acts in agent_das
        self.action_space = spaces.Discrete(self.n_agent_da)




        assert render_mode is None or render_mode in self.metadata["render_modes"]
        self.render_mode = render_mode

    def get_action_space(self):
        return self.n_agent_da
    def _get_obs(self):
        user_da = np.zeros((self.n_parra,self.n_user_da))
        for i in range(self.n_parra):
            user_da[i,self.last_user_da[i]] = 1
        last_user_da = np.zeros((self.n_parra,self.n_user_da))
        for i in range(self.n_parra):
            last_user_da[i,self.last_user_da[i]] = 1
        agent_da = np.zeros((self.n_parra,self.n_agent_da))
        for i in range(self.n_parra):
            agent_da[i,int(self.agent_last_da[i])] = 1

        master_ob = np.array([[self.user.context[i]/3,self.user.rapport[i]/3,self.user.perspective[i]/3,self.turn_id[i]/40] for i in range(self.n_parra)])
        print(master_ob)
        ob = np.concatenate([master_ob,user_da,agent_da],axis=1)
        return master_ob,ob

    def seed(self, seed):
        self.random_seed = seed
        self.user.seed(seed)
        return [seed]
    def set_task(self,i=None):
        self.user.reset()
        if i is not None:
            self.user.set_type(i)
        else:
            self.user.set_type(np.random.randint(3))


    def reset(self, seed=None, options=None,return_info=False):
        # We need the following line to seed self.np_random
        if seed is not None:
            self.random_seed = seed
#        super().reset()
        # Choose the agent's location uniformly at random
        self.last_agent_da = [0 for i in range(self.n_parra)]
        self.last_user_da = [0 for i in range(self.n_parra)]
        self.user.reset(seed = self.random_seed)
        self.turn_id = [0 for i in range(self.n_parra)]
        master_observation,observation = self._get_obs()
        self.conv=[ [] for i in range(self.n_parra)]
        if return_info:
            return observation, {}
        return  observation

    def reset_i(self,i,seed=None, options=None, return_info=False):
        # We need the following line to seed self.np_random
        if seed is not None:
            self.random_seed = seed
            #        super().reset()
            # Choose the agent's location uniformly at random
        self.last_agent_da[i] = 0
        self.last_user_da[i] = 0
        self.user.reset_i(i)
        self.turn_id[i] = 0
        master_observation, observation = self._get_obs()
        self.conv[i] = []
        if return_info:
            return observation, {}
        return observation


    def ob_master(self):
        return self._get_obs()[0]

    def step(self, action,baseline=False):



        # An episode is done iff the agent has reached the target

        terminated =  torch.FloatTensor([self.agent_das[int(action[i])] == "GreetingorClosing" for i in range(len(action))])

        for i in range(len(action)):
            if self.turn_id[i] > 40:
                terminated[i] = True
            if self.turn_id[i] < 2:
                terminated[i] = False

        self.agent_last_da = action
        for i in range(self.n_parra):
            self.conv[i].append((self.last_user_da[i],self.agent_last_da[i]))

        self.last_user_da = self.user.react(action,self.turn_id,baseline=baseline)
        reward = self.user.get_reward(self.last_user_da)
        if action == 13:
            reward -=100
        self.turn_id = [self.turn_id[i] + 2 for i in range(self.n_parra)]
        master_observation,observation = self._get_obs()
        print(reward)
        return np.array(master_observation),np.array(observation), reward, terminated, {}
