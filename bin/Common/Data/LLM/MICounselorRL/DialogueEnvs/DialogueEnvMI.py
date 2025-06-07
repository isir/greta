import gym
from gym import spaces
import numpy as np
from Users.UserMI_text_fake import UserMI
import torch
class DialogueEnvMI(gym.Env):
    metadata = {}

    def __init__(self, render_mode=None):


        self.user = UserMI()
        self.random_seed = 0
        self.agent_das =self.user.agent_action_space
        self.user_das = self.user.action_space
        self.n_user_da = len(self.user_das)
        self.n_agent_da = len(self.agent_das)
        self.turn_id =0
        self.last_last_user_da=0
        self.agent_last_da = 0
        self.last_user_da = 0

        high = np.array(
            [
                1,
            ]*( self.n_agent_da+ 2*self.n_user_da),
            dtype=np.int32,
        )
        low = np.array(
            [
                0,
            ] * (self.n_agent_da + 2*self.n_user_da),
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
        user_da = np.zeros((self.n_user_da))
        user_da[self.last_user_da] = 1
        last_user_da = np.zeros((self.n_user_da))
        last_user_da[self.last_user_da] = 1
        last_last_user_da = np.zeros((self.n_user_da))
        last_last_user_da[int(self.last_last_user_da)] = 1
        agent_da = np.zeros(self.n_agent_da)
        agent_da[int(self.agent_last_da)] = 1
        master_ob = np.array(
            [self.user.context / 40, self.user.rapport / 40, self.user.perspective / 40])
        ob = np.concatenate([user_da,agent_da,last_last_user_da])


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
        self.last_agent_da = 0
        self.last_user_da = 0
        self.user.reset(seed = self.random_seed)
        self.turn_id = 0
        master_observation,observation = self._get_obs()

        if return_info:
            return observation, {}
        return  observation




    def ob_master(self):
        return self._get_obs()[0]

    def step(self, action):



        # An episode is done iff the agent has reached the target

        if (self.agent_das[action] == "GreetingorClosing"):
            terminated = True
        else:
            terminated = False


        if self.turn_id > 80:
            terminated = True
        if self.turn_id < 2:
            terminated = False
        self.last_last_user_da=self.last_user_da
        self.agent_last_da = action
        self.last_user_da = self.user.react(action,self.turn_id)
        reward = self.user.get_reward(self.last_user_da)
        self.turn_id = self.turn_id + 2
        master_observation,observation = self._get_obs()

        return np.array(master_observation),np.array(observation), reward, terminated, {}
