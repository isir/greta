import numpy as np


class ReplayBuffer:

    def __init__(self, observation_space,action_space, capacity=5000,with_subpolicy=False):
        self.observation_space = observation_space
        self.action_space = action_space
        transition_type_str = self.get_transition_type_str(observation_space,action_space,with_subpolicy=with_subpolicy)
        self.buffer = np.zeros(capacity, dtype=transition_type_str)
        self.weights = np.zeros(capacity)
        self.head_idx = 0
        self.count = 0
        self.capacity = capacity
        self.max_weight = 10**-2
        self.delta = 10**-4
        self.indices = None

    def empty(self):
        self.count = 0
        self.head_idx = 0
        self.buffer = np.zeros(self.capacity, dtype=self.buffer.dtype)
        self.weights = np.zeros(self.capacity)
    def get_transition_type_str(self, observation_space,action_space,with_subpolicy=False):
        state_dim = observation_space.shape[0]
        state_dim_str = '' if state_dim == () else str(state_dim)
        state_type_str = observation_space.sample().dtype.name
        action_dim = action_space.shape
        action_dim_str = '' if action_dim == () else str(action_dim)
        action_type_str = action_space.sample().__class__.__name__

        if with_subpolicy:
            # type str for transition = 'state type, action type, reward type, state type, bool, int'
            transition_type_str = '{0}{1}, {2}{3}, float32, {0}{1}, bool, int'.format(state_dim_str, state_type_str,
                                                                             action_dim_str, action_type_str)
        else:
            # type str for transition = 'state type, action type, reward type, state type'
            transition_type_str = '{0}{1}, {2}{3}, float32, {0}{1}, bool'.format(state_dim_str, state_type_str,
                                                                             action_dim_str, action_type_str)

        return transition_type_str


    def add_transition(self, transition):
        self.buffer[self.head_idx] = transition
        self.weights[self.head_idx] = self.max_weight

        self.head_idx = (self.head_idx + 1) % self.capacity
        self.count = min(self.count + 1, self.capacity)

    def sample_minibatch(self, size=100):
        set_weights = self.weights[:self.count] + self.delta
        probabilities = set_weights / sum(set_weights)
        self.indices = np.random.choice(range(self.count), size, p=probabilities, replace=False)
        return self.buffer[self.indices]

    def update_weights(self, prediction_errors):
        max_error = max(prediction_errors)
        self.max_weight = max(self.max_weight, max_error)
        self.weights[self.indices] = prediction_errors[:len(self.indices)]

    def get_size(self):
        return self.count
    def sample_obs(self,n_obs):
        set_weights = self.weights[:self.count] + self.delta
        probabilities = set_weights / sum(set_weights)
        obs = []
        for i in range(n_obs):
            ind = np.random.choice(range(self.count), p=probabilities)
            obs.append(self.buffer[ind][0])

        return obs
    def get_all_data(self):
        # return m_obs, m_actions, m_rewards, m_next_obs, m_dones
        return self.buffer[:self.count]