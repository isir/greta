import torch
import torch.nn as nn
import torch.nn.functional as F


class VQVAE(nn.Module):
    def __init__(self,
                 in_features: int = 17,
                 d_model: int = 128,
                 dz: int = 128,
                 K: int = 64,
                 T: int = 18,
                 n_heads: int = 4,
                 num_layers: int = 3,
                 dropout: float = 0.3,
                 beta: float = 0.25):
        super().__init__()

        self.K = K
        self.dz = dz
        self.T = T
        self.T_down = T // 2  # 18 → 9
        self.beta = beta

        #encoder
        self.input_proj = nn.Linear(in_features, d_model)

        #temporal downsampling (2) 
        self.encoder_conv = nn.Sequential(
            nn.Conv1d(d_model, d_model, kernel_size=3, stride=2, padding=1),
            nn.GELU()
        )  # [B, d_model, T] → [B, d_model, T//2]

        self.encoder_pos = nn.Parameter(torch.randn(1, T // 2, d_model) * 0.02)

        self.encoder_transformer = nn.TransformerEncoder(
            nn.TransformerEncoderLayer(
                d_model=d_model,
                nhead=n_heads,
                dim_feedforward=d_model * 2,
                dropout=dropout,
                batch_first=True
            ),
            num_layers=num_layers
        )

        self.encoder_out = nn.Linear(d_model, dz)

        #codebook
        self.codebook = nn.Embedding(K, dz)
        nn.init.normal_(self.codebook.weight, mean=0, std=0.1)

        #decoder
        self.decoder_input = nn.Linear(dz, d_model)

        self.decoder_pos = nn.Parameter(torch.randn(1, T // 2, d_model) * 0.02)

        self.decoder_transformer = nn.TransformerEncoder(
            nn.TransformerEncoderLayer(
                d_model=d_model,
                nhead=n_heads,
                dim_feedforward=d_model * 2,
                dropout=dropout,
                batch_first=True
            ),
            num_layers=num_layers
        )

        # temporal upsampling (2)
        self.decoder_conv = nn.Sequential(
            nn.ConvTranspose1d(d_model, d_model, kernel_size=4, stride=2, padding=1),
            nn.GELU()
        )  # [B, d_model, T//2] → [B, d_model, T]

        self.output_proj = nn.Linear(d_model, in_features)

    def encode(self, x):
        # x: [B, T, 17]
        x = self.input_proj(x)                    # [B, T, d_model]
        x = x.permute(0, 2, 1)                    # [B, d_model, T]
        x = self.encoder_conv(x)                  # [B, d_model, T//2]
        x = x.permute(0, 2, 1)                    # [B, T//2, d_model]
        x = x + self.encoder_pos                  # positional encoding
        x = self.encoder_transformer(x)           # [B, T//2, d_model]
        z = self.encoder_out(x)                   # [B, T//2, dz]
        return z

    def quantize(self, z):
        # z: [B, T//2, dz]
        B, Td, dz = z.shape
        z_flat = z.reshape(B * Td, dz)            # [B*T//2, dz]

        # distance to codebook entries
        d = (
            z_flat.pow(2).sum(dim=-1, keepdim=True)
            - 2 * z_flat @ self.codebook.weight.t()
            + self.codebook.weight.pow(2).sum(dim=-1)
        )  # [B*T//2, K]

        indices = d.argmin(dim=-1)                # [B*T//2]
        zq_flat = self.codebook(indices)          # [B*T//2, dz]

        # losses
        codebook_loss   = F.mse_loss(z_flat.detach(), zq_flat)
        commitment_loss = self.beta * F.mse_loss(z_flat, zq_flat.detach())

        # straight-through estimator
        zq_st = z_flat + (zq_flat - z_flat).detach()
        zq_st = zq_st.reshape(B, Td, dz)          # [B, T//2, dz]
        indices = indices.reshape(B, Td)           # [B, T//2]

        return zq_st, indices, codebook_loss, commitment_loss

    def decode(self, zq):
        # zq: [B, T//2, dz]
        x = self.decoder_input(zq)                # [B, T//2, d_model]
        x = x + self.decoder_pos                  # positional encoding
        x = self.decoder_transformer(x)           # [B, T//2, d_model]
        x = x.permute(0, 2, 1)                    # [B, d_model, T//2]
        x = self.decoder_conv(x)                  # [B, d_model, T]
        x = x.permute(0, 2, 1)                    # [B, T, d_model]
        x = self.output_proj(x)                   # [B, T, 17]
        return x

    def forward(self, x):
        # x: [B, T, 17]
        z = self.encode(x)
        zq, indices, codebook_loss, commitment_loss = self.quantize(z)
        x_recon = self.decode(zq)
        return x_recon, indices, codebook_loss, commitment_loss

    def get_indices(self, x):
        '''inference, returns codebook indices for a chunk'''
        with torch.no_grad():
            z = self.encode(x)
            _, indices, _, _ = self.quantize(z)
            return indices  # [B, T//2]

    def decode_indices(self, indices):
        '''inference, decodes codebook indices to AUs'''
        with torch.no_grad():
            zq = self.codebook(indices)   # [B, T//2, dz]
            return self.decode(zq)        # [B, T, 17]

    def save(self, path):
        torch.save(self.state_dict(), path)

    def load(self, path, device='cpu'):
        self.load_state_dict(torch.load(path, map_location=device))