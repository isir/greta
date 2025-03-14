import numpy as np
import torch
import torch.nn.functional as F
from utilities.Network import Network
from utilities.ReplayBuffer import ReplayBuffer


class Subpolicies:



    def __init__(self, observation_space, action_space,len_replay_buffer=1000000,replay_mini_batch_size=1000000,num_subpolicies=5,learning_rate=10**-4):
        self.observation_space = observation_space
        self.action_space = action_space
        self.n_subpolicies = num_subpolicies
        self.ALPHA_INITIAL = 0.1
        self.REPLAY_BUFFER_BATCH_SIZE = replay_mini_batch_size
        self.DISCOUNT_RATE = 0.99
        self.LEARNING_RATE = learning_rate
        self.SOFT_UPDATE_INTERPOLATION_FACTOR = 0.005
        self.state_dim = self.observation_space.shape[0]
        self.action_dim = self.action_space.n
        self.seeds = [42, 2004, 2302, 1505, 4687, 3573]
        self.critic_local = [Network(input_dimension=self.state_dim,
                                    output_dimension=self.action_dim) for i in range(self.n_subpolicies)]


        self.critic_local2 = [Network(input_dimension=self.state_dim,
                                    output_dimension=self.action_dim) for i in range(self.n_subpolicies)]
        self.critic_optimiser = [torch.optim.Adam(self.critic_local[i].parameters(), lr=self.LEARNING_RATE/10) for i in range(self.n_subpolicies)]
        self.critic_optimiser2 = [torch.optim.Adam(self.critic_local2[i].parameters(), lr=self.LEARNING_RATE/10) for i in range(self.n_subpolicies)]

        self.critic_target = [Network(input_dimension=self.state_dim,
                                    output_dimension=self.action_dim) for i in range(self.n_subpolicies)]
        self.critic_target2 = [Network(input_dimension=self.state_dim,
                                    output_dimension=self.action_dim) for i in range(self.n_subpolicies)]
        for i in range(self.n_subpolicies):
            self.critic_local[i].init_weights(self.seeds[i])
            self.critic_local2[i].init_weights(self.seeds[i])
            self.critic_target[i].init_weights(self.seeds[i])
            self.critic_target2[i].init_weights(self.seeds[i])

        self.soft_update_target_networks(tau=1.)

        self.actor_local = [Network(
            input_dimension=self.state_dim,
            output_dimension=self.action_dim,
            output_activation=torch.nn.Softmax(dim=-1)
        ) for i in range(self.n_subpolicies)]

        for i in range(self.n_subpolicies):
            self.critic_local[i].init_weights(self.seeds[i])
            self.critic_local2[i].init_weights(self.seeds[i])
            self.critic_target[i].init_weights(self.seeds[i])
            self.critic_target2[i].init_weights(self.seeds[i])
            self.actor_local[i].init_weights(self.seeds[i])
        self.actor_optimiser = [torch.optim.AdamW(self.actor_local[i].parameters(), lr=self.LEARNING_RATE) for i in range(self.n_subpolicies)]

        self.replay_buffer = [ReplayBuffer(self.observation_space, self.action_space, capacity=len_replay_buffer,with_subpolicy=True) for i in range(self.n_subpolicies)]

        self.target_entropy = 0.98 * -np.log(1 / self.action_space.n)
        self.log_alpha = torch.tensor(np.log(self.ALPHA_INITIAL), requires_grad=True)
        self.alpha = self.log_alpha
        self.alpha_optimiser = torch.optim.AdamW([self.log_alpha], lr=self.LEARNING_RATE)

    def save(self, path):

        for i in range(self.n_subpolicies):
            torch.save(self.actor_local[i].state_dict(), path + "/actor_local"+str(i)+".pth")
            torch.save(self.critic_local[i].state_dict(), path + "/critic_local.pth")
            torch.save(self.critic_local2[i].state_dict(), path + "/critic_local2.pth")
        torch.save(self.log_alpha, path + "/log_alpha.pth")
    def get_next_action(self, subpolicy,state, evaluation_episode=False):
        if evaluation_episode:
            discrete_action = self.get_action_deterministically(subpolicy,state)
        else:
            discrete_action = self.get_action_nondeterministically(subpolicy,state)
        return discrete_action

    def get_action_nondeterministically(self,subpolicy, state):
        action_probabilities = self.get_action_probabilities(subpolicy,state)
        if len(action_probabilities.shape)>1:
            discrete_action = [np.random.choice(range(self.action_dim), p=action_probabilities[i]) for i in range(len(action_probabilities))]
        else:
            discrete_action = np.random.choice(range(self.action_dim), p=action_probabilities)
        if np.random.rand() < 0:
            discrete_action = np.random.choice(range(self.action_dim))
        return discrete_action

    def get_action_deterministically(self,subpolicy, state):
        action_probabilities = self.get_action_probabilities(subpolicy, state)
        if len(action_probabilities.shape) > 1:
            discrete_action = [np.random.choice(range(self.action_dim), p=action_probabilities[i]) for i in
                               range(len(action_probabilities))]
        else:
            discrete_action = np.argmax(action_probabilities)

        return discrete_action


    def train_on_transition(self, transition):

        self.train_networks(transition)

    def add_transition(self, state, discrete_action, reward, next_state, done,subpolicy):
        for i in range(len(state)):
            transition = (state[i], discrete_action[i], reward[i], next_state[i], done[i],subpolicy[i])
            self.replay_buffer[subpolicy[i]].add_transition(transition)


    def train_networks(self, transition):
        # Set all the gradients stored in the optimisers to zero.
        subpolicy = int(transition[-1])
        self.critic_optimiser[subpolicy].zero_grad()
        self.critic_optimiser2[subpolicy].zero_grad()
        self.actor_optimiser[subpolicy].zero_grad()
        self.alpha_optimiser.zero_grad()
        # Calculate the loss for this transition.
        self.replay_buffer[subpolicy].add_transition(transition)
        # Compute the gradients based on this loss, i.e. the gradients of the loss with respect to the Q-network
        # parameters.
        if self.replay_buffer[subpolicy].get_size() >= self.REPLAY_BUFFER_BATCH_SIZE:
            # get minibatch of 100 transitions from replay buffer
            minibatch = self.replay_buffer[subpolicy].sample_minibatch(self.REPLAY_BUFFER_BATCH_SIZE)
            minibatch_separated = list(map(list, zip(*minibatch)))

            # unravel transitions to get states, actions, rewards and next states
            states_tensor = torch.tensor(np.array(minibatch_separated[0]))
            actions_tensor = torch.tensor(np.array(minibatch_separated[1]))
            rewards_tensor = torch.tensor(np.array(minibatch_separated[2])).float()
            next_states_tensor = torch.tensor(np.array(minibatch_separated[3]))
            done_tensor = torch.tensor(np.array(minibatch_separated[4]))

            critic_loss, critic2_loss = \
                self.critic_loss(states_tensor, actions_tensor, rewards_tensor, next_states_tensor, done_tensor,subpolicy)

            critic_loss.backward()
            critic2_loss.backward()
            self.critic_optimiser[subpolicy].step()
            self.critic_optimiser2[subpolicy].step()

            actor_loss, log_action_probabilities = self.actor_loss(subpolicy,states_tensor)
            diversity_loss = self.diversity_loss(states_tensor)
            actor_loss = actor_loss + diversity_loss
            actor_loss.backward()
            self.actor_optimiser[subpolicy].step()

            #alpha_loss = self.temperature_loss(log_action_probabilities)

            #alpha_loss.backward()
            #self.alpha_optimiser.step()
            #self.alpha = self.log_alpha.exp()

            self.soft_update_target_networks()

    def critic_loss(self, states_tensor, actions_tensor, rewards_tensor, next_states_tensor, done_tensor,subpolicy):
        with torch.no_grad():
            action_probabilities, log_action_probabilities = self.get_action_info(subpolicy,next_states_tensor)
            next_q_values_target = self.critic_target[subpolicy].forward(next_states_tensor)
            next_q_values_target2 = self.critic_target2[subpolicy].forward(next_states_tensor)
            soft_state_values = (action_probabilities * (
                    torch.min(next_q_values_target, next_q_values_target2) - self.alpha * log_action_probabilities
            )).sum(dim=1)

            next_q_values = rewards_tensor + ~done_tensor * self.DISCOUNT_RATE*soft_state_values
        actions_tensor = actions_tensor.to(torch.int64)
        soft_q_values = self.critic_local[subpolicy](states_tensor).gather(1, actions_tensor.unsqueeze(-1)).squeeze(-1)
        soft_q_values2 = self.critic_local2[subpolicy](states_tensor).gather(1, actions_tensor.unsqueeze(-1)).squeeze(-1)
        critic_square_error = torch.nn.MSELoss(reduction="none")(soft_q_values, next_q_values)
        critic2_square_error = torch.nn.MSELoss(reduction="none")(soft_q_values2, next_q_values)
        weight_update = [min(l1.item(), l2.item()) for l1, l2 in zip(critic_square_error, critic2_square_error)]
        self.replay_buffer[subpolicy].update_weights(weight_update)
        critic_loss = critic_square_error.mean()
        critic2_loss = critic2_square_error.mean()
        return critic_loss, critic2_loss

    def actor_loss(self, subpolicy,states_tensor,):
        action_probabilities, log_action_probabilities = self.get_action_info(subpolicy,states_tensor)
        q_values_local = self.critic_local[subpolicy](states_tensor)
        q_values_local2 = self.critic_local2[subpolicy](states_tensor)
        inside_term = self.alpha * log_action_probabilities - torch.min(q_values_local, q_values_local2)
        policy_loss = (action_probabilities * inside_term).sum(dim=1).mean()
        return policy_loss, log_action_probabilities

    def temperature_loss(self, log_action_probabilities):
        alpha_loss = -(self.log_alpha * (log_action_probabilities + self.target_entropy).detach()).mean()
        return alpha_loss

    def get_action_info(self,subpolicy, states_tensor):
        action_probabilities = self.actor_local[subpolicy].forward(states_tensor)
        z = action_probabilities == 0.0
        z = z.float() * 1e-8
        log_action_probabilities = torch.log(action_probabilities + z)
        return action_probabilities, log_action_probabilities

    def get_action_probabilities(self, subpolicy,state):
        state_tensor = torch.tensor(state, dtype=torch.float32)
        action_probabilities = [self.actor_local[subpolicy[i]].forward(state_tensor[i]).squeeze().detach().numpy() for i in range(len(subpolicy))]
        return np.array(action_probabilities)

    def soft_update_target_networks(self, tau=None):
        if tau is None:
            tau = self.SOFT_UPDATE_INTERPOLATION_FACTOR
        self.soft_update(self.critic_target, self.critic_local, tau)
        self.soft_update(self.critic_target2, self.critic_local2, tau)

    def soft_update(self, target_model, origin_model, tau):
        for i in range(len(target_model)):
            for target_param, local_param in zip(target_model[i].parameters(), origin_model[i].parameters()):
                target_param.data.copy_(tau * local_param.data + (1 - tau) * target_param.data)

    def predict_q_values(self, subpolicy,state):
        q_values = self.critic_local[subpolicy](state)
        q_values2 = self.critic_local2[subpolicy](state)
        return torch.min(q_values, q_values2)


    def diversity_loss(self,batch):
        logits = [model(batch) for model in self.actor_local]
        loss = 0
        for i in range(len(self.actor_local)):
            for j in range(i + 1, len(self.actor_local)):
                loss -= F.kl_div(F.log_softmax(logits[i], dim=-1),
                                 F.softmax(logits[j].detach(), dim=-1), reduction='batchmean')
        return loss*1000