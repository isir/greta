import numpy as np
import torch

from utilities.Network import Network
from utilities.ReplayBuffer import ReplayBuffer

import learn2learn as l2l
class SACAgent:



    def __init__(self, observation_space, action_space,len_replay_buffer=1000000,replay_mini_batch_size=1000000,learning_rate=10**-4,mbpo=False,use_maml=False):
        self.observation_space = observation_space
        self.action_space = action_space
        self.mbpo = mbpo
        self.ALPHA_INITIAL = 1.0
        self.REPLAY_BUFFER_BATCH_SIZE = replay_mini_batch_size
        self.DISCOUNT_RATE = 0.99
        self.LEARNING_RATE = learning_rate
        self.SOFT_UPDATE_INTERPOLATION_FACTOR = 0.005
        self.state_dim = self.observation_space.shape[0]
        self.action_dim = self.action_space.n
        self.critic_local = Network(input_dimension=self.state_dim,
                                    output_dimension=self.action_dim)
        self.critic_local2 = Network(input_dimension=self.state_dim,
                                     output_dimension=self.action_dim)
        self.critic_optimiser = torch.optim.AdamW(self.critic_local.parameters(), lr=self.LEARNING_RATE/10)
        self.critic_optimiser2 = torch.optim.AdamW(self.critic_local2.parameters(), lr=self.LEARNING_RATE/10)
        self.num_timesteps = 0
        self.critic_target = Network(input_dimension=self.state_dim,
                                     output_dimension=self.action_dim)
        self.critic_target2 = Network(input_dimension=self.state_dim,
                                      output_dimension=self.action_dim)
        self.dynamics_train_freq=1
        self.use_maml = use_maml
        self.rollout_dynamics_starts = 1

        self.soft_update_target_networks(tau=1.)

        self.actor_local = Network(
            input_dimension=self.state_dim,
            output_dimension=self.action_dim,
            output_activation=torch.nn.Softmax(dim=-1)
        )

        self.actor_local.init_weights(42)
        self.critic_local.init_weights(42)
        self.critic_local2.init_weights(42)
        self.critic_target.init_weights(42)
        self.critic_target2.init_weights(42)

        self.actor_optimiser = torch.optim.AdamW(self.actor_local.parameters(), lr=self.LEARNING_RATE)
        self.actor_maml = l2l.algorithms.MAML(self.actor_local, lr=1e-6, first_order=False)
        self.actor_maml_opt = torch.optim.Adam(self.actor_maml.parameters(), lr=4e-7)
        self.actor_learner = self.actor_maml.clone()
        self.critic_maml = l2l.algorithms.MAML(self.critic_local, lr=1e-6, first_order=False)
        self.critic_maml_opt = torch.optim.Adam(self.critic_maml.parameters(), lr=4e-7)
        self.critic_learner = self.critic_maml.clone()
        self.critic2_maml = l2l.algorithms.MAML(self.critic_local, lr=1e-6, first_order=False)
        self.critic2_maml_opt = torch.optim.Adam(self.critic2_maml.parameters(), lr=4e-7)
        self.critic2_learner = self.critic2_maml.clone()
        self.replay_buffer = ReplayBuffer(self.observation_space, self.action_space, capacity=len_replay_buffer)

        self.target_entropy = 0.98 * -np.log(1 / self.action_space.n)
        self.log_alpha = torch.tensor(np.log(self.ALPHA_INITIAL), requires_grad=True)
        self.alpha = self.log_alpha
        self.alpha_optimiser = torch.optim.AdamW([self.log_alpha], lr=10e-6)
    def save(self, path):
        torch.save(self.actor_local.state_dict(), path + "/actor_local")
        torch.save(self.critic_local.state_dict(), path + "/critic_local")
        torch.save(self.critic_local2.state_dict(), path + "/critic_local2")
        torch.save(self.critic_target.state_dict(), path + "/critic_target")
        torch.save(self.critic_target2.state_dict(), path + "/critic_target2")
        torch.save(self.log_alpha, path + "/log_alpha")
    def reset(self):
        self.actor_local= Network(
            input_dimension=self.state_dim,
            output_dimension=self.action_dim,
            output_activation=torch.nn.Softmax(dim=-1)
        )
        self.critic_local = Network(input_dimension=self.state_dim,
                                    output_dimension=self.action_dim)
        self.critic_local2= Network(input_dimension=self.state_dim,
                                    output_dimension=self.action_dim)
        self.critic_target = Network(input_dimension=self.state_dim,
                                     output_dimension=self.action_dim)
        self.critic_target2= Network(input_dimension=self.state_dim,
                                    output_dimension=self.action_dim)
        self.log_alpha = torch.tensor(np.log(self.ALPHA_INITIAL), requires_grad=True)
    def get_next_action(self, state, evaluation_episode=False):
        if evaluation_episode:
            discrete_action = self.get_action_deterministically(state)
        else:
            discrete_action = self.get_action_nondeterministically(state)
        print(discrete_action)
        return discrete_action

    def get_action_nondeterministically(self, state):
        action_probabilities = self.get_action_probabilities(state)

        if len(action_probabilities.shape)>1:
            discrete_action = [np.random.choice(range(self.action_dim), p=a) for a in action_probabilities]
        else:
            discrete_action = np.random.choice(range(self.action_dim), p=action_probabilities)
        if np.random.rand()< 0:
            if len(action_probabilities.shape)>1:
                discrete_action = [np.random.choice(range(self.action_dim)) for a in action_probabilities]
            else:
                discrete_action = np.random.choice(range(self.action_dim))
        print(discrete_action)
        return discrete_action

    def get_action_deterministically(self, state):
        action_probabilities = self.get_action_probabilities(state)

        if len(action_probabilities.shape) > 1:
            discrete_action = [np.argmax(a) for a in action_probabilities]
        else:
            discrete_action = np.argmax(action_probabilities)
        print(discrete_action)
        return discrete_action


    def train_on_transition(self, state, discrete_action, next_state, reward, done):
        transition = (state, discrete_action, reward, next_state, done)
        self.num_timesteps +=1
        self.train_networks(transition)

    def train_networks(self, transition):
        # Set all the gradients stored in the optimisers to zero.
        print("training")
        self.critic_optimiser.zero_grad()
        self.critic_optimiser2.zero_grad()
        self.actor_optimiser.zero_grad()
        self.alpha_optimiser.zero_grad()
        # Calculate the loss for this transition.
        self.replay_buffer.add_transition(transition)
        # Compute the gradients based on this loss, i.e. the gradients of the loss with respect to the Q-network
        # parameters.



        if self.replay_buffer.get_size() >self.REPLAY_BUFFER_BATCH_SIZE:
            # get minibatch of 100 transitions from replay buffer
            if self.num_timesteps < self.rollout_dynamics_starts:

                minibatch = self.replay_buffer.sample_minibatch(self.REPLAY_BUFFER_BATCH_SIZE)
                minibatch_separated = list(map(list, zip(*minibatch)))

                # unravel transitions to get states, actions, rewards and next states
                states_tensor = torch.tensor(np.array(minibatch_separated[0]))
                actions_tensor = torch.tensor(np.array(minibatch_separated[1]))
                rewards_tensor = torch.tensor(np.array(minibatch_separated[2])).float()
                next_states_tensor = torch.tensor(np.array(minibatch_separated[3]))
                done_tensor = torch.tensor(np.array(minibatch_separated[4]))
            else:
                num_real_samples = self.REPLAY_BUFFER_BATCH_SIZE   # 5% of real world data

                if num_real_samples<0:
                    num_real_samples=1
                real_minibatch = self.replay_buffer.sample_minibatch(num_real_samples)
                minibatch_separated = list(map(list, zip(*real_minibatch)))

                # unravel transitions to get states, actions, rewards and next states
                real_states_tensor = torch.tensor(np.array(minibatch_separated[0]))
                real_actions_tensor = torch.tensor(np.array(minibatch_separated[1]))
                real_rewards_tensor = torch.tensor(np.array(minibatch_separated[2])).float()
                real_next_states_tensor = torch.tensor(np.array(minibatch_separated[3]))
                real_done_tensor = torch.tensor(np.array(minibatch_separated[4]))
                fake_minibatch = self.dynamics_buffer.sample_minibatch(
                    self.REPLAY_BUFFER_BATCH_SIZE*20)
                minibatch_separated = list(map(list, zip(*fake_minibatch)))

                # unravel transitions to get states, actions, rewards and next states
                fake_states_tensor = torch.tensor(np.array(minibatch_separated[0]))
                fake_actions_tensor = torch.tensor(np.array(minibatch_separated[1]))
                fake_rewards_tensor = torch.tensor(np.array(minibatch_separated[2])).float()
                fake_next_states_tensor = torch.tensor(np.array(minibatch_separated[3]))
                fake_done_tensor = torch.tensor(np.array(minibatch_separated[4]))
                states_tensor=torch.concatenate([real_states_tensor, fake_states_tensor], dim=0)
                actions_tensor=torch.concatenate([real_actions_tensor, fake_actions_tensor], dim=0)
                rewards_tensor=torch.concatenate([real_rewards_tensor, fake_rewards_tensor], dim=0)
                next_states_tensor=torch.concatenate([real_next_states_tensor, fake_next_states_tensor], dim=0)
                done_tensor=torch.concatenate([real_done_tensor, fake_done_tensor], dim=0)


            critic_loss, critic2_loss = \
                self.critic_loss(states_tensor, actions_tensor, rewards_tensor, next_states_tensor, done_tensor)
            print(critic_loss)
            if self.use_maml:
                self.critic_learner.adapt(critic_loss,allow_unused=True)
                self.critic2_learner.adapt(critic2_loss,allow_unused=True)
            else:
                critic_loss.backward()
                critic2_loss.backward()
                self.critic_optimiser.step()
                self.critic_optimiser2.step()

            actor_loss, log_action_probabilities = self.actor_loss(states_tensor)
            print(actor_loss)
            if self.use_maml:
                self.actor_learner.adapt(actor_loss,allow_unused=True)
            else:
                actor_loss.backward()
                self.actor_optimiser.step()

            alpha_loss = self.temperature_loss(log_action_probabilities)

            alpha_loss.backward()
            self.alpha_optimiser.step()
            self.alpha = self.log_alpha.exp()
            print("-----------------------------------------------------------",self.alpha)

            self.soft_update_target_networks()



    def critic_loss(self, states_tensor, actions_tensor, rewards_tensor, next_states_tensor, done_tensor):
        with torch.no_grad():
            action_probabilities, log_action_probabilities = self.get_action_info(next_states_tensor)
            next_q_values_target = self.critic_target.forward(next_states_tensor)
            next_q_values_target2 = self.critic_target2.forward(next_states_tensor)
            soft_state_values = (action_probabilities * (
                    torch.min(next_q_values_target, next_q_values_target2) - self.alpha * log_action_probabilities
            )).sum(dim=1)

            next_q_values = rewards_tensor + ~done_tensor * self.DISCOUNT_RATE*soft_state_values
        actions_tensor = actions_tensor.to(torch.int64)
        soft_q_values = self.critic_local(states_tensor).gather(1, actions_tensor.unsqueeze(-1)).squeeze(-1)
        soft_q_values2 = self.critic_local2(states_tensor).gather(1, actions_tensor.unsqueeze(-1)).squeeze(-1)
        critic_square_error = torch.nn.MSELoss(reduction="none")(soft_q_values, next_q_values)
        critic2_square_error = torch.nn.MSELoss(reduction="none")(soft_q_values2, next_q_values)
        weight_update = [min(l1.item(), l2.item()) for l1, l2 in zip(critic_square_error, critic2_square_error)]
        try:
            self.replay_buffer.update_weights(weight_update)
        except:
            pass
        critic_loss = critic_square_error.mean()
        critic2_loss = critic2_square_error.mean()
        # clip the loss to prevent large gradients
        critic_loss = torch.clamp(critic_loss, -1, 1)
        critic2_loss = torch.clamp(critic2_loss, -1, 1)
        return critic_loss, critic2_loss

    def actor_loss(self, states_tensor,):
        action_probabilities, log_action_probabilities = self.get_action_info(states_tensor)
        q_values_local = self.critic_local(states_tensor)
        q_values_local2 = self.critic_local2(states_tensor)
        inside_term = self.alpha * log_action_probabilities - torch.min(q_values_local, q_values_local2)
        policy_loss = (action_probabilities * inside_term).sum(dim=1).mean()
        return policy_loss, log_action_probabilities

    def temperature_loss(self, log_action_probabilities):
        alpha_loss = -(self.log_alpha * (log_action_probabilities + self.target_entropy).detach()).mean()
        return alpha_loss

    def get_action_info(self, states_tensor):

        action_probabilities = self.actor_local.forward(states_tensor)
        z = action_probabilities == 0.0
        z = z.float() * 1e-8
        log_action_probabilities = torch.log(action_probabilities + z)
        return action_probabilities, log_action_probabilities

    def get_action_probabilities(self, state):
        state_tensor = torch.tensor(state, dtype=torch.float32).unsqueeze(0)
        action_probabilities = self.actor_local.forward(state_tensor)
        return action_probabilities.squeeze(0).detach().numpy()

    def soft_update_target_networks(self, tau=None):
        if tau is None:
            tau = self.SOFT_UPDATE_INTERPOLATION_FACTOR
        self.soft_update(self.critic_target, self.critic_local, tau)
        self.soft_update(self.critic_target2, self.critic_local2, tau)

    def soft_update(self, target_model, origin_model, tau):
        for target_param, local_param in zip(target_model.parameters(), origin_model.parameters()):
            target_param.data.copy_(tau * local_param.data + (1 - tau) * target_param.data)

    def predict_q_values(self, state):
        q_values = self.critic_local(state)
        q_values2 = self.critic_local2(state)
        return torch.min(q_values, q_values2)


    def clone(self):
        self.actor_learner = self.actor_maml.clone()
        self.critic_learner = self.critic_maml.clone()
        self.critic2_learner = self.critic2_maml.clone()




    def update_meta(self):

        # Set all the gradients stored in the optimisers to zero.



        if self.replay_buffer.get_size() >0:
            # get minibatch of 100 transitions from replay buffer
            if  self.num_timesteps < self.rollout_dynamics_starts:

                minibatch = self.replay_buffer.get_all_data()
                minibatch_separated = list(map(list, zip(*minibatch)))

                # unravel transitions to get states, actions, rewards and next states
                states_tensor = torch.tensor(np.array(minibatch_separated[0]))
                actions_tensor = torch.tensor(np.array(minibatch_separated[1]))
                rewards_tensor = torch.tensor(np.array(minibatch_separated[2])).float()
                next_states_tensor = torch.tensor(np.array(minibatch_separated[3]))
                done_tensor = torch.tensor(np.array(minibatch_separated[4]))
            else:
                num_real_samples = self.REPLAY_BUFFER_BATCH_SIZE  # 5% of real world data

                if num_real_samples < 0:
                    num_real_samples = 1
                real_minibatch = self.replay_buffer.sample_minibatch(num_real_samples)
                minibatch_separated = list(map(list, zip(*real_minibatch)))

                # unravel transitions to get states, actions, rewards and next states
                real_states_tensor = torch.tensor(np.array(minibatch_separated[0]))
                real_actions_tensor = torch.tensor(np.array(minibatch_separated[1]))
                real_rewards_tensor = torch.tensor(np.array(minibatch_separated[2])).float()
                real_next_states_tensor = torch.tensor(np.array(minibatch_separated[3]))
                real_done_tensor = torch.tensor(np.array(minibatch_separated[4]))
                fake_minibatch = self.dynamics_buffer.sample_minibatch(
                    self.REPLAY_BUFFER_BATCH_SIZE * 20)
                minibatch_separated = list(map(list, zip(*fake_minibatch)))

                # unravel transitions to get states, actions, rewards and next states
                fake_states_tensor = torch.tensor(np.array(minibatch_separated[0]))
                fake_actions_tensor = torch.tensor(np.array(minibatch_separated[1]))
                fake_rewards_tensor = torch.tensor(np.array(minibatch_separated[2])).float()
                fake_next_states_tensor = torch.tensor(np.array(minibatch_separated[3]))
                fake_done_tensor = torch.tensor(np.array(minibatch_separated[4]))
                states_tensor = torch.concatenate([real_states_tensor, fake_states_tensor], dim=0)
                actions_tensor = torch.concatenate([real_actions_tensor, fake_actions_tensor], dim=0)
                rewards_tensor = torch.concatenate([real_rewards_tensor, fake_rewards_tensor], dim=0)
                next_states_tensor = torch.concatenate([real_next_states_tensor, fake_next_states_tensor], dim=0)
                done_tensor = torch.concatenate([real_done_tensor, fake_done_tensor], dim=0)



            actor_loss, log_action_probabilities = self.actor_loss(states_tensor)
            self.actor_maml_opt.zero_grad()
            actor_loss.backward()
            self.actor_maml_opt.step()

            critic_loss, critic2_loss = \
                self.critic_loss(states_tensor, actions_tensor, rewards_tensor, next_states_tensor, done_tensor)

            self.critic_maml_opt.zero_grad()
            self.critic2_maml_opt.zero_grad()
            critic_loss.backward()
            critic2_loss.backward()
            self.critic_maml_opt.step()
            self.critic2_maml_opt.step()





