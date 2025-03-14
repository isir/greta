import numpy as np


class SimpleUser :

    def __init__(self,type=0):

        self.type = type
        self.action_space = ["Greeting", "Ask", "Request", "Negate", "Closing"]
        self.agent_action_space = ["Greeting", "Inform", "Answer","Request","Confirm","Deny", "Closing"]
        self.last_action = 0
        self.current_action = 0
        self.type = np.random.randint(2)


    def react(self,agent_da):
        action = np.random.randint(len(self.action_space))
        self.last_action = self.current_action
        self.current_action = action
        return action

    def get_reward(self,action,last_action,turn_id):

        r=0
        if self.action_space[self.current_action] == "Ask":
            if self.agent_action_space[action] == "Answer":
                r+=2

        if self.action_space[self.current_action] == "Request":
            if self.agent_action_space[action] == "Inform":
                r+=2

        if self.action_space[self.current_action] == "Negate":
            if self.agent_action_space[action] == "Inform":
                r+=2

        if self.action_space[self.current_action] == "Closing":
            if self.agent_action_space[action] == "Closing":
                r+=2

        if self.action_space[self.current_action] == "Greeting":
            if self.agent_action_space[action] == "Greeting":
                r+=2

        if  self.action_space[self.last_action] == "Ask" and self.action_space[self.current_action] == "Ask":
            if self.agent_action_space[last_action] == "Answer" and self.agent_action_space[action] == "Request":
                r+=10
        if  self.action_space[self.last_action] == "Request" and self.action_space[self.current_action] == "Request":
            if self.agent_action_space[last_action] == "Inform" and self.agent_action_space[action] == "Confirm":
                r+=10
        if  self.action_space[self.last_action] == "Negate" and self.action_space[self.current_action] == "Negate":
            if self.agent_action_space[last_action] == "Inform" and self.agent_action_space[action] == "Deny":
                r+=10
        if  self.action_space[self.last_action] == "Greeting" and self.action_space[self.current_action] == "Greeting":
            if self.agent_action_space[last_action] == "Greeting" and self.agent_action_space[action] == "Ask":
                r+=10

        if turn_id > 20 and self.agent_action_space[action] != "Closing":
            r-=12

        return r

    def reset(self,seed=None):
        pass
    def seed(self,seed):
        pass