import torch
import torch.nn as nn
from dpm_solver_pytorch import NoiseScheduleVP, DPM_Solver, model_wrapper
#from diffusers import LMSDiscreteScheduler, DDIMScheduler


class ChunkDescriptorEmbedder(nn.Module):
    def __init__(self, continious_embedding_dim=16 ,valence_embedding_dim=8,  output_dim=64):
        super(ChunkDescriptorEmbedder, self).__init__()
        self.time_fc = nn.Linear(1, continious_embedding_dim)
        self.valence_embedding = nn.Embedding(3, valence_embedding_dim)
        self.mapping = {0: 1, -1: 0, 1: 2}
        self.output_dim = output_dim

        self.merge_fc = nn.Linear(continious_embedding_dim + valence_embedding_dim * 2, output_dim)
        self.norm = nn.LayerNorm(output_dim)
        self.relu = nn.ReLU()
        self.residual_fc = nn.Linear(continious_embedding_dim + valence_embedding_dim * 2, output_dim)

    def forward(self, x):
        time_embed = self.relu(self.time_fc(x[:, 0].unsqueeze(-1)))

        # Correct approach for mapping
        valence_1_mapped = torch.tensor([self.mapping[val.item()] for val in x[:, 1]], dtype=torch.long, device=x.device)
        valence_2_mapped = torch.tensor([self.mapping[val.item()] for val in x[:, 2]], dtype=torch.long, device=x.device)

        valence_embed_1 = self.valence_embedding(valence_1_mapped)
        valence_embed_2 = self.valence_embedding(valence_2_mapped)

        combined = torch.cat((time_embed, valence_embed_1, valence_embed_2), dim=1)
        merged = self.merge_fc(combined)
        residual = self.residual_fc(combined)
        output = self.norm(merged + residual)
        return output

class TimeSiren(nn.Module):
    def __init__(self, input_dim, emb_dim):
        super(TimeSiren, self).__init__()
        # just a fully connected NN with sin activations
        self.lin1 = nn.Linear(input_dim, emb_dim, bias=False)
        self.lin2 = nn.Linear(emb_dim, emb_dim)
        self.output_dim = emb_dim
    def forward(self, x):
        x = torch.sin(self.lin1(x))
        x = self.lin2(x)
        return x


class SpeakingTurnDescriptorEmbedder(nn.Module):
    def __init__(self, num_event_types, embedding_dim, output_dim):
        super(SpeakingTurnDescriptorEmbedder, self).__init__()
        # Embedding layer for each category in the descriptor
        self.embedding = nn.Embedding(num_event_types, embedding_dim)
        self.output_dim = output_dim
        # Increase the model's capacity with more layers and non-linearities
        self.fc1 = nn.Linear(embedding_dim * 2, embedding_dim * 4)  # Increase intermediate size
        self.relu = nn.ReLU()
        self.fc2 = nn.Linear(embedding_dim * 4, self.output_dim)

        # Optionally, add dropout for regularization
        self.dropout = nn.Dropout(0.5)

    def forward(self, x):
        elem1, elem2 = x[:, 0].long(), x[:, 1].long()
        embed_1, embed_2 = self.embedding(elem1), self.embedding(elem2)
        concatenated = torch.cat((embed_1, embed_2), dim=1)

        # Apply additional layers with non-linear activation functions
        x = self.relu(self.fc1(concatenated))
        x = self.dropout(x)  # Apply dropout
        output = self.fc2(x)
        return output

#OPTION 1
class ObservationEmbedder(nn.Module):
     def __init__(self, num_facial_types, facial_embedding_dim, cnn_output_dim, lstm_hidden_dim, sequence_length):
         super(ObservationEmbedder, self).__init__()
         self.embedding = nn.Embedding(num_facial_types, facial_embedding_dim)

         # Define CNN layers
         self.conv1 = nn.Conv1d(in_channels=facial_embedding_dim, out_channels=64, kernel_size=3, padding=1)
         self.conv2 = nn.Conv1d(in_channels=64, out_channels=cnn_output_dim, kernel_size=3, padding=1)
         self.pool = nn.MaxPool1d(kernel_size=2, stride=2)
         self.output_dim = lstm_hidden_dim

         # Calculate the dimensionality after CNN and pooling layers
         cnn_flattened_dim = cnn_output_dim * (sequence_length // 2 // 2)

         # Define LSTM layer
         self.lstm = nn.LSTM(input_size=cnn_flattened_dim, hidden_size=lstm_hidden_dim, batch_first=True)

         # Optional: Additional layer(s) can be added here to further process LSTM output if needed

     def forward(self, x):

         x = x.long()
         # Embedding layer
         x = self.embedding(x)  # Expecting x to be [batch_size, sequence_length]
         x = x.permute(0, 2, 1)  # Change to [batch_size, embedding_dim, sequence_length] for CNN

         # CNN layers
         x = torch.relu(self.conv1(x))
         x = self.pool(x)
         x = torch.relu(self.conv2(x))
         x = self.pool(x)

         # Flatten output for LSTM
         x = x.view(x.size(0), 1, -1)  # Flatten CNN output

         # LSTM layer
         lstm_out, (hidden, cell) = self.lstm(x)

         # Here you can use hidden or lstm_out depending on your need
         # For simplicity, we'll consider 'hidden' as the final feature representation

         return hidden[-1] # Returning the last hidden state of LSTM


def ddpm_schedules(beta1, beta2, T, is_linear=True):
    """
    Returns pre-computed schedules for DDPM sampling, training process.
    """
    assert beta1 < beta2 < 1.0, "beta1 and beta2 must be in (0, 1)"

    # beta_t = (beta2 - beta1) * torch.arange(0, T + 1, dtype=torch.float32) / T + beta1
    # beta_t = (beta2 - beta1) * torch.arange(-1, T + 1, dtype=torch.float32) / T + beta1
    if is_linear:
        beta_t = (beta2 - beta1) * torch.arange(-1, T, dtype=torch.float32) / (T - 1) + beta1
    else:
        beta_t = (beta2 - beta1) * torch.square(torch.arange(-1, T, dtype=torch.float32)) / torch.max(
            torch.square(torch.arange(-1, T, dtype=torch.float32))) + beta1
    beta_t[0] = beta1  # modifying this so that beta_t[1] = beta1, and beta_t[n_T]=beta2, while beta[0] is never used
    # this is as described in Denoising Diffusion Probabilistic Models paper, section 4
    sqrt_beta_t = torch.sqrt(beta_t)
    alpha_t = 1 - beta_t
    log_alpha_t = torch.log(alpha_t)
    alphabar_t = torch.cumsum(log_alpha_t, dim=0).exp()

    sqrtab = torch.sqrt(alphabar_t)
    oneover_sqrta = 1 / torch.sqrt(alpha_t)

    sqrtmab = torch.sqrt(1 - alphabar_t)
    mab_over_sqrtmab_inv = (1 - alpha_t) / sqrtmab

    return {
        "alpha_t": alpha_t,  # \alpha_t
        "oneover_sqrta": oneover_sqrta,  # 1/\sqrt{\alpha_t}
        "sqrt_beta_t": sqrt_beta_t,  # \sqrt{\beta_t}
        "alphabar_t": alphabar_t,  # \bar{\alpha_t}
        "sqrtab": sqrtab,  # \sqrt{\bar{\alpha_t}}
        "sqrtmab": sqrtmab,  # \sqrt{1-\bar{\alpha_t}}
        "mab_over_sqrtmab": mab_over_sqrtmab_inv,  # (1-\alpha_t)/\sqrt{1-\bar{\alpha_t}}
    }

class Model_Cond_Diffusion(nn.Module):
    def __init__(self, nn_model, observation_embedder, mi_embedder, chunk_embedder, betas, n_T, device, x_dim, y_dim, drop_prob=0.1, guide_w=0.0):
        super(Model_Cond_Diffusion, self).__init__()
        for k, v in ddpm_schedules(betas[0], betas[1], n_T).items():
            self.register_buffer(k, v)

        self.betas = betas
        self.nn_model = nn_model
        self.n_T = n_T
        self.device = device
        self.observation_embedder = observation_embedder
        self.mi_embedder = mi_embedder
        self.chunk_embedder = chunk_embedder
        self.drop_prob = drop_prob
        self.loss_mse = nn.MSELoss()
        self.x_dim = x_dim
        self.y_dim = y_dim
        self.guide_w = guide_w

    def loss_on_batch(self, x_batch, y_batch, z_batch, chunk_descriptor):

        _ts = torch.randint(1, self.n_T + 1, (y_batch.shape[0], 1)).to(self.device)

        # dropout context with some probability
        context_mask = torch.bernoulli(torch.zeros(x_batch.shape[0]) + self.drop_prob).to(self.device)
        #context_mask2 = torch.bernoulli(torch.zeros(z_batch.shape[0]) + self.drop_prob).to(self.device)

        # randomly sample some noise, noise ~ N(0, 1)
        noise = torch.randn_like(y_batch).to(self.device)
        self.y_dim = noise.shape

        # add noise to clean target actions
        y_t = self.sqrtab[_ts] * y_batch + self.sqrtmab[_ts] * noise
        # use nn model to predict noise
        noise_pred_batch = self.nn_model(y_t, x_batch, z_batch, chunk_descriptor,  _ts / self.n_T, context_mask) #ici possible d'ajouter context_mask en input

        # return mse between predicted and true noise
        return self.loss_mse(noise, noise_pred_batch)
    #

    # def sample(self, x_batch, z_batch, chunk_descriptor, return_y_trace=False):
    #
    # # also use this as a shortcut to avoid doubling batch when guide_w is zero
    #     is_zero = False
    #     if self.guide_w > -1e-3 and self.guide_w < 1e-3:
    #         is_zero = True
    #
    #     # how many noisy actions to begin with
    #     n_sample = x_batch.shape[0]
    #
    #     y_shape = (n_sample, x_batch.shape[1])
    #
    #     # sample initial noise, y_0 ~ N(0, 1),
    #     y_i = torch.randn(y_shape).to(self.device)
    #
    #     if not is_zero:
    #         if len(x_batch.shape) > 2:
    #             # repeat x_batch twice, so can use guided diffusion
    #             x_batch = x_batch.repeat(2, 1, 1, 1)
    #             z_batch = z_batch.repeat(2, 1, 1, 1)
    #             chunk_descriptor = chunk_descriptor.repeat(2, 1, 1, 1)
    #         else:
    #             # repeat x_batch twice, so can use guided diffusion
    #             x_batch = x_batch.repeat(2, 1)
    #             z_batch = z_batch.repeat(2, 1)
    #             chunk_descriptor = chunk_descriptor.repeat(2, 1,)
    #
    #         # half of context will be zero
    #         context_mask = torch.zeros(x_batch.shape[0]).to(self.device)
    #         context_mask[n_sample:] = 1.0  # makes second half of batch context free
    #         #context_mask2 = torch.zeros(z_batch.shape[0]).to(self.device)
    #         #context_mask2[n_sample:] = 1.0  # makes second half of batch context free
    #     else:
    #         context_mask = torch.zeros(x_batch.shape[0]).to(self.device)
    #         #context_mask2 = torch.zeros(z_batch.shape[0]).to(self.device)
    #
    #
    #     #     #x_embed = self.nn_model.embed_context(x_batch)
    #     #     x = self.event_embedder(x_batch)
    #     #     x_embed = self.x_sequence_transformer(x)
    #
    #     # run denoising chain
    #     y_i_store = []  # if want to trace how y_i evolved
    #     for i in range(self.n_T, 0, -1):
    #         t_is = torch.tensor([i / self.n_T]).to(self.device)
    #         t_is = t_is.repeat(n_sample, 1)
    #
    #         if not is_zero:
    #             # double batch
    #             y_i = y_i.repeat(2, 1)
    #             t_is = t_is.repeat(2, 1)
    #
    #         z = torch.randn(y_shape).to(self.device) if i > 1 else 0
    #
    #
    #         # if extract_embedding:
    #         #     eps = self.nn_model(y_i, x_batch, t_is) #ici possible d'input le context_mask
    #
    #         eps = self.nn_model(y_i, x_batch, z_batch, chunk_descriptor, t_is, context_mask)
    #         if not is_zero:
    #             eps1 = eps[:n_sample]
    #             eps2 = eps[n_sample:]
    #             eps = (1 + self.guide_w) * eps1 - self.guide_w * eps2
    #             y_i = y_i[:n_sample]
    #         y_i = self.oneover_sqrta[i] * (y_i - eps * self.mab_over_sqrtmab[i]) + self.sqrt_beta_t[i] * z
    #         if return_y_trace and (i % 20 == 0 or i == self.n_T or i < 8):
    #             y_i_store.append(y_i.detach().cpu().numpy())
    #
    #     if return_y_trace:
    #         return y_i, y_i_store
    #     else:
    #         return y_i
    # #HYBRIDE
    def sample(self, x_batch, z_batch, chunk_descriptor, initial_steps=50, ode_steps=5, return_y_trace=False):
        # Use the usual sampling method for initial_steps
        n_sample = x_batch.shape[0]
        y_shape = (n_sample, x_batch.shape[1])
        y_i = torch.randn(y_shape).to(self.device)
        context_mask = torch.zeros(x_batch.shape[0]).to(self.device) if self.guide_w == 0.0 else torch.cat([
            torch.zeros(x_batch.shape[0] // 2), torch.ones(x_batch.shape[0] // 2)
        ]).to(self.device)
        is_zero = self.guide_w > -1e-3 and self.guide_w < 1e-3

        if not is_zero:
            if len(x_batch.shape) > 2:
                x_batch = x_batch.repeat(2, 1, 1, 1)
                z_batch = z_batch.repeat(2, 1, 1, 1)
                chunk_descriptor = chunk_descriptor.repeat(2, 1, 1, 1)
            else:
                x_batch = x_batch.repeat(2, 1)
                z_batch = z_batch.repeat(2, 1)
                chunk_descriptor = chunk_descriptor.repeat(2, 1)
            context_mask = torch.zeros(x_batch.shape[0]).to(self.device)
            context_mask[n_sample:] = 1.0

        y_i_store = []  # Define y_i_store for tracing if required
        for i in range(self.n_T, self.n_T - initial_steps, -1):
            t_is = torch.tensor([i / self.n_T]).to(self.device).repeat(n_sample, 1)
            if not is_zero:
                y_i = y_i.repeat(2, 1)
                t_is = t_is.repeat(2, 1)
            z = torch.randn(y_shape).to(self.device) if i > 1 else 0
            eps = self.nn_model(y_i, x_batch, z_batch, chunk_descriptor, t_is, context_mask)
            if not is_zero:
                eps1 = eps[:n_sample]
                eps2 = eps[n_sample:]
                eps = (1 + self.guide_w) * eps1 - self.guide_w * eps2
                y_i = y_i[:n_sample]
            y_i = self.oneover_sqrta[i] * (y_i - eps * self.mab_over_sqrtmab[i]) + self.sqrt_beta_t[i] * z
            if return_y_trace and (i % 20 == 0 or i == self.n_T or i < 8):
                y_i_store.append(y_i.detach().cpu().numpy())

        # Use DPM solver for the remaining steps
        noise_schedule = NoiseScheduleVP(schedule='linear', alphas_cumprod=(self.alphabar_t[initial_steps:]))
        additional_context = {
            'x_batch': x_batch,
            'z_batch': z_batch,
            'chunk_descriptor': chunk_descriptor,
            'context_mask': context_mask
        }
        model_fn = model_wrapper(self.nn_model, noise_schedule, model_type='noise',
                                 additional_context=additional_context)
        dpm_solver = DPM_Solver(model_fn, noise_schedule)
        y_i = dpm_solver.sample(
            y_i,
            steps=ode_steps,
            method='multistep',
            order=2,
            skip_type='time_uniform',
            return_intermediate=return_y_trace,
            solver_type='dpmsolver'
        )

        if return_y_trace:
            return y_i[0], y_i_store + y_i[1]  # Combine the traces from both methods
        else:
            return y_i

    def sample_update(self, x_batch, betas, n_T, return_y_trace=False):
        original_nT = self.n_T

        # set new schedule
        self.n_T = n_T
        for k, v in ddpm_schedules(betas[0], betas[1], self.n_T).items():
            self.register_buffer(k, v.to(self.device))

        # also use this as a shortcut to avoid doubling batch when guide_w is zero
        is_zero = False
        if self.guide_w > -1e-3 and self.guide_w < 1e-3:
            is_zero = True

        # how many noisy actions to begin with
        n_sample = x_batch.shape[0]

        y_shape = (n_sample, self.y_dim)

        # sample initial noise, y_0 ~ N(0, 1),
        y_i = torch.randn(y_shape).to(self.device)

        if not is_zero:
            if len(x_batch.shape) > 2:
                # repeat x_batch twice, so can use guided diffusion
                x_batch = x_batch.repeat(2, 1, 1, 1)
            else:
                # repeat x_batch twice, so can use guided diffusion
                x_batch = x_batch.repeat(2, 1)
            # half of context will be zero
            context_mask = torch.zeros(x_batch.shape[0]).to(self.device)
            context_mask[n_sample:] = 1.0  # makes second half of batch context free
        else:
            context_mask = torch.zeros(x_batch.shape[0]).to(self.device)

        # run denoising chain
        y_i_store = []  # if want to trace how y_i evolved
        for i in range(self.n_T, 0, -1):
            t_is = torch.tensor([i / self.n_T]).to(self.device)
            t_is = t_is.repeat(n_sample, 1)

            if not is_zero:
                # double batch
                y_i = y_i.repeat(2, 1)
                t_is = t_is.repeat(2, 1)

            # I'm a bit confused why we are adding noise during denoising?
            z = torch.randn(y_shape).to(self.device) if i > 1 else 0

            # split predictions and compute weighting
            eps = self.nn_model(y_i, x_batch, t_is, context_mask)
            if not is_zero:
                eps1 = eps[:n_sample]
                eps2 = eps[n_sample:]
                eps = (1 + self.guide_w) * eps1 - self.guide_w * eps2
                y_i = y_i[:n_sample]
            y_i = self.oneover_sqrta[i] * (y_i - eps * self.mab_over_sqrtmab[i]) + self.sqrt_beta_t[i] * z
            if return_y_trace and (i % 20 == 0 or i == self.n_T or i < 8):
                y_i_store.append(y_i.detach().cpu().numpy())

        # reset original schedule
        self.n_T = original_nT
        for k, v in ddpm_schedules(betas[0], betas[1], self.n_T).items():
            self.register_buffer(k, v.to(self.device))

        if return_y_trace:
            return y_i, y_i_store
        else:
            return y_i

    def sample_extra(self, x_batch, extra_steps=4, return_y_trace=False):
        # also use this as a shortcut to avoid doubling batch when guide_w is zero
        is_zero = False
        if self.guide_w > -1e-3 and self.guide_w < 1e-3:
            is_zero = True

        # how many noisy actions to begin with
        n_sample = x_batch.shape[0]

        y_shape = (n_sample, self.y_dim)

        # sample initial noise, y_0 ~ N(0, 1),
        y_i = torch.randn(y_shape).to(self.device)

        if not is_zero:
            if len(x_batch.shape) > 2:
                # repeat x_batch twice, so can use guided diffusion
                x_batch = x_batch.repeat(2, 1, 1, 1)
            else:
                # repeat x_batch twice, so can use guided diffusion
                x_batch = x_batch.repeat(2, 1)
            # half of context will be zero
            context_mask = torch.zeros(x_batch.shape[0]).to(self.device)
            context_mask[n_sample:] = 1.0  # makes second half of batch context free
        else:
            # context_mask = torch.zeros_like(x_batch[:,0]).to(self.device)
            context_mask = torch.zeros(x_batch.shape[0]).to(self.device)

        # run denoising chain
        y_i_store = []  # if want to trace how y_i evolved
        # for i_dummy in range(self.n_T, 0, -1):
        for i_dummy in range(self.n_T, -extra_steps, -1):
            i = max(i_dummy, 1)
            t_is = torch.tensor([i / self.n_T]).to(self.device)
            t_is = t_is.repeat(n_sample, 1)

            if not is_zero:
                # double batch
                y_i = y_i.repeat(2, 1)
                t_is = t_is.repeat(2, 1)

            z = torch.randn(y_shape).to(self.device) if i > 1 else 0

            # split predictions and compute weighting
            eps = self.nn_model(y_i, x_batch, t_is, context_mask)
            if not is_zero:
                eps1 = eps[:n_sample]
                eps2 = eps[n_sample:]
                eps = (1 + self.guide_w) * eps1 - self.guide_w * eps2
                y_i = y_i[:n_sample]
            y_i = self.oneover_sqrta[i] * (y_i - eps * self.mab_over_sqrtmab[i]) + self.sqrt_beta_t[i] * z
            if return_y_trace and (i % 20 == 0 or i == self.n_T or i < 8):
                y_i_store.append(y_i.detach().cpu().numpy())

        if return_y_trace:
            return y_i, y_i_store
        else:
            return y_i

#
class FCBlock(nn.Module):
    def __init__(self, in_feats, out_feats):
        super().__init__()
        # one layer of non-linearities (just a useful building block to use below)
        self.model = nn.Sequential(
            nn.Linear(in_feats, out_feats),
            nn.BatchNorm1d(num_features=out_feats),
            nn.GELU(),
        )

    def forward(self, x):
        return self.model(x)


class Merger(nn.Module):
    def __init__(self, observation_dim, speaking_turn_dim, chunk_descriptor_dim, output_dim, num_heads = 8):
        super(Merger, self).__init__()
        self.input_dim = observation_dim + speaking_turn_dim + chunk_descriptor_dim
        self.multihead_attn = nn.MultiheadAttention(embed_dim=self.input_dim, num_heads=num_heads, batch_first=True)
        intermediate_dim = (self.input_dim + output_dim) // 2  # Example intermediate size

        self.fc1 = nn.Linear(self.input_dim, intermediate_dim)
        self.fc2 = nn.Linear(intermediate_dim, output_dim)
        self.relu = nn.ReLU()
        self.residual_connection = nn.Linear(self.input_dim, output_dim)

        self.norm1 = nn.LayerNorm(intermediate_dim)
        self.norm2 = nn.LayerNorm(output_dim)

    def forward(self, observation_embedding, speaking_turn_embedding, chunk_embedding):

        #OPTION 2
        #speaking_turn_embedding_extended = speaking_turn_embedding.unsqueeze(1).repeat(1, observation_embedding.size(1), 1)
        # Concatenate along the feature dimension
        #combined = torch.cat((observation_embedding, speaking_turn_embedding_extended), dim=2)

        #OPTION 1
        combined = torch.cat((observation_embedding, speaking_turn_embedding, chunk_embedding), dim=1).unsqueeze(1)


        # Apply multihead attention
        attn_output, _ = self.multihead_attn(combined, combined, combined)

        #OPTION1
        x = self.fc1(attn_output.squeeze(1))
        # OPTION2
        #x = self.fc1(attn_output)
        x = self.relu(x)
        x = self.norm1(x)
        # Second layer with residual connection
        x = self.fc2(x)
        # OPTION1
        residual = self.residual_connection(attn_output.squeeze(1))
        # OPTION2
        #residual = self.residual_connection(attn_output)

        x += residual  # Add residual
        x = self.relu(x)
        x = self.norm2(x)

        return x
class TransformerEncoderBlock(nn.Module):
    def __init__(self, trans_emb_dim, transformer_dim, nheads):
        super(TransformerEncoderBlock, self).__init__()
        # mainly going off of https://jalammar.github.io/illustrated-transformer/

        self.trans_emb_dim = trans_emb_dim
        self.transformer_dim = transformer_dim
        self.nheads = nheads

        self.input_to_qkv1 = nn.Linear(self.trans_emb_dim, self.transformer_dim * 3)
        self.multihead_attn1 = nn.MultiheadAttention(self.transformer_dim, num_heads=self.nheads)
        self.attn1_to_fcn = nn.Linear(self.transformer_dim, self.trans_emb_dim)
        self.attn1_fcn = nn.Sequential(
            nn.Linear(self.trans_emb_dim, self.trans_emb_dim * 4),
            nn.GELU(),
            nn.Linear(self.trans_emb_dim * 4, self.trans_emb_dim),
        )
        self.norm1a = nn.BatchNorm1d(self.trans_emb_dim)
        self.norm1b = nn.BatchNorm1d(self.trans_emb_dim)

    def split_qkv(self, qkv):
        assert qkv.shape[-1] == self.transformer_dim * 3
        q = qkv[:, :, :self.transformer_dim]
        k = qkv[:, :, self.transformer_dim: 2 * self.transformer_dim]
        v = qkv[:, :, 2 * self.transformer_dim:]
        return (q, k, v)



    def forward(self, inputs: torch.Tensor) -> torch.Tensor:

        qkvs1 = self.input_to_qkv1(inputs)
        # shape out = [3, batchsize, transformer_dim*3]

        qs1, ks1, vs1 = self.split_qkv(qkvs1)
        # shape out = [3, batchsize, transformer_dim]

        attn1_a = self.multihead_attn1(qs1, ks1, vs1, need_weights=False)
        attn1_a = attn1_a[0]
        # shape out = [3, batchsize, transformer_dim = trans_emb_dim x nheads]

        attn1_b = self.attn1_to_fcn(attn1_a)
        attn1_b = attn1_b / 1.414 + inputs / 1.414  # add residual
        # shape out = [3, batchsize, trans_emb_dim]

        # normalise
        attn1_b = self.norm1a(attn1_b.transpose(0, 2).transpose(0, 1))
        attn1_b = attn1_b.transpose(0, 1).transpose(0, 2)
        # batchnorm likes shape = [batchsize, trans_emb_dim, 3]
        # so have to shape like this, then return

        # fully connected layer
        attn1_c = self.attn1_fcn(attn1_b) / 1.414 + attn1_b / 1.414
        # shape out = [3, batchsize, trans_emb_dim]

        # normalise
        # attn1_c = self.norm1b(attn1_c)
        attn1_c = self.norm1b(attn1_c.transpose(0, 2).transpose(0, 1))
        attn1_c = attn1_c.transpose(0, 1).transpose(0, 2)
        return attn1_c

class Model_mlp_diff(nn.Module):
    def __init__(self, observation_embedder, mi_embedder, chunk_embedder, sequence_length, net_type="transformer"):
        super(Model_mlp_diff, self).__init__()
        self.observation_embedder = observation_embedder
        self.mi_embedder = mi_embedder
        self.chunk_embedder = chunk_embedder
        self.time_siren = TimeSiren(1, mi_embedder.output_dim)
        self.net_type = net_type

        # Transformer specific initialization
        self.nheads = 16  # Number of heads in multihead attention
        self.trans_emb_dim = 256 #Transformer embedding dimension
        self.transformer_dim = self.trans_emb_dim * self.nheads

        # Initialize SequenceTransformers for y and x
        self.merger = Merger(observation_embedder.output_dim, mi_embedder.output_dim, chunk_embedder.output_dim, 128)
        #OPTION2
        #self.reducer = SequenceReducer(128, 128)


        # Linear layers to project embeddings to transformer dimension
        self.t_to_input = nn.Linear(mi_embedder.output_dim, self.trans_emb_dim)
        self.y_to_input = nn.Linear(sequence_length, self.trans_emb_dim)
        self.x_to_input = nn.Linear(128, self.trans_emb_dim)

        # Positional embedding for transformer
        self.pos_embed = TimeSiren(1, self.trans_emb_dim)

        # Transformer blocks
        self.transformer_block1 = TransformerEncoderBlock(self.trans_emb_dim, self.transformer_dim, self.nheads)
        self.transformer_block2 = TransformerEncoderBlock(self.trans_emb_dim, self.transformer_dim, self.nheads)
        self.transformer_block3 = TransformerEncoderBlock(self.trans_emb_dim, self.transformer_dim, self.nheads)
        self.transformer_block4 = TransformerEncoderBlock(self.trans_emb_dim, self.transformer_dim, self.nheads)


        # Final layer to project transformer output to desired output dimension
        self.final = nn.Linear(self.trans_emb_dim * 3, sequence_length)  # Adjust the output dimension as needed

    def forward(self, y, x, z,chunk_descriptor, t, context_mask):

        embedded_t = self.time_siren(t)
        
        x = self.observation_embedder(x)

        z = self.mi_embedder(z)

        chunk_descriptor = self.chunk_embedder(chunk_descriptor)

        x = self.merger(x, z,chunk_descriptor)
        #Option2
        #x = self.reducer(x)

        context_mask = context_mask.repeat(x.shape[1], 1).T
        x = x * (-1 * (1 - context_mask))
        
        # Project embeddings to transformer dimension
        t_input = self.t_to_input(embedded_t)
        y_input = self.y_to_input(y)
        x_input = self.x_to_input(x)


        #t_input = t_input.unsqueeze(1).repeat(1, x.shape[1], 1)

        # Add positional encoding
        t_input += self.pos_embed(torch.zeros(x.shape[0], 1).to(x.device) + 1.0)
        y_input += self.pos_embed(torch.zeros(y.shape[0], 1).to(x.device) + 2.0)
        x_input += self.pos_embed(torch.zeros(x.shape[0], 1).to(x.device) + 3.0)

        # Concatenate inputs for transformer
        inputs = torch.cat((t_input[None, :, :], y_input[None, :, :], x_input[None, :, :]), 0)

        # Pass through transformer blocks
        block_output = self.transformer_block1(inputs)
        block_output1 = self.transformer_block2(block_output)
        block_output2 = self.transformer_block3(block_output1)
        block_output3 = self.transformer_block4(block_output2)


        # Flatten and add final linear layer
        transformer_out = block_output3.transpose(0, 1)  # Roll batch to first dim

        flat = torch.flatten(transformer_out, start_dim=1, end_dim=2)
        out = self.final(flat)
        return out
