import numpy as np

class RL_client:

    def __init__(self):
        self.state = 0
        self.action = 0

        self.user_da = ["SharingpersonalinformationorDescribepastevent",
             "Changingunhealthybehaviorinthefuture",
             "Sustainingunhealthybehaviorinthefuture",
             "Sharingnegativefeelingoremotion",
             "Sharingpositivefeelingoremotion",
             "UnderstandingorNewPerspective",
             "GreetingorClosing",
             "Backchannel","AskingforMedicalInformation",'Unknown']

        self.agent_da = ["Reflection",
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
             "Empathic Reaction"]

        self.da_to_id = {da:i for i,da in enumerate(self.user_da)}

    def step(self,da):
        try:
            self.user_state = self.da_to_id[da.replace(" ","")]
        except:
            self.user_state = self.da_to_id["Unknown"]
        self.action = np.random.randint(0,12)

        return self.agent_da[self.action]