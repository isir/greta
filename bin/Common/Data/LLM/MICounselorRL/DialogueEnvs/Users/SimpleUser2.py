import numpy as np


class SimpleUser :

    def __init__(self,type=0):

        self.type = type
        self.action_space = ["Inform", "Answer"]
        self.agent_action_space = [ "Inform", "Ask","Closing"]
        self.last_action = 0
        self.e = 0
        self.m = 0
        self.current_action = 0
        self.type = np.random.randint(3)
        self.nes = [0,5,10]
        self.random_seed = 0

    def react(self,agent_da):
        action = np.random.randint(len(self.action_space))
        self.last_action = self.current_action
        self.current_action = action
        return action

    def get_reward(self,action,last_action,turn_id):

        r=0

        if self.action_space[self.current_action] == "Inform":
            if self.agent_action_space[action] == "Inform":
                if self.e >= self.nes[self.type]:
                    self.m+=1
                    self.m = min(self.m,10)
                    r+=10
                else:

                    self.m = max(0,self.m)

        if self.action_space[self.current_action] == "Inform":
            if self.agent_action_space[action] == "Ask":
                self.e +=1
                r+=1

        if self.action_space[self.current_action] == "Answer":
            if self.agent_action_space[action] == "Inform":
                self.e += 1
                r += 1

        if self.action_space[self.current_action] == "Answer":
            if self.agent_action_space[action] == "Ask":
                if self.e >= self.nes[self.type]:
                    self.m += 1
                    r+=10
                    self.m = min(self.m, 10)
                else:

                    self.m = max(0, self.m)
        self.e = min(15,self.e)

        if turn_id > 25 and self.agent_action_space[action] != "Closing":
            r-=20
        if self.agent_action_space[action] == "Closing":
            if turn_id<20:
                r-=20
            else:
                r+=30
        return r

    def reset(self,seed=None):
        self.last_action = 0
        self.e = 0
        self.m = 0
        self.current_action = 0
        self.random_seed = seed

    def seed(self,seed):
        self.random_seed = seed
