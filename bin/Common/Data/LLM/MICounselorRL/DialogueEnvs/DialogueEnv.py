import gym
from gym import spaces
import numpy as np
from Users.SimpleUser2 import SimpleUser
class DialogueEnv(gym.Env):
    metadata = {}

    def __init__(self, render_mode=None):

        self.user = SimpleUser()
        self.random_seed = 0
        self.agent_das =self.user.agent_action_space
        self.user_das = self.user.action_space
        self.n_user_da = len(self.user_das)
        self.n_agent_da = len(self.agent_das)
        self.turn_id =0
        self.agent_last_da =0
        self.last_user_da =0

        high = np.array(
            [
                1,
                1,



            ],
            dtype=np.int32,
        )
        low = np.array(
            [
                0,
                0,

            ],
            dtype=np.int32,
        )
        master_high = np.array(
            [
                1,
                1,


            ],
            dtype=np.int32,
        )
        master_low = np.array(
            [
                0,
                0,


            ],
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
        user_da = np.zeros(self.n_user_da)
        user_da[self.last_user_da] = 1
        last_user_da = np.zeros(self.n_user_da)
        last_user_da[self.user.last_action] = 1
        agent_da = np.zeros(self.n_agent_da)
        agent_da[self.agent_last_da] = 1
        ob = np.concatenate([user_da,[self.user.e/15,self.turn_id/20]])
        master_ob = np.array([self.user.e/15,self.turn_id/20]).astype(np.float32)

        return master_ob,ob

    def seed(self, seed):
        self.random_seed = seed
        self.user.seed(seed)
        return [seed]
    def set_task(self,i=None):
        self.user = SimpleUser()
        if i is not None:
            self.user.type = i

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

        terminated =  self.agent_das[action] == "Closing"

        if self.turn_id > 100:
            terminated = True


        reward = self.user.get_reward(action, self.last_agent_da,self.turn_id)

        self.agent_last_da = action


        self.last_user_da = self.user.react(action)
        self.turn_id += 1
        master_observation,observation = self._get_obs()

        return np.array(master_observation),np.array(observation), reward, terminated, {}
