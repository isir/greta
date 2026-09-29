# vap/modules/encoder_dual_hubert.py
import torch
import torch.nn as nn
from encoder import EncoderCPC
from encoder_hubert import EncoderHubert

class EncoderDualHubert(nn.Module):
    """
    Early‐fusion of CPC + HuBERT embeddings.
    Concatenate along the feature dim and project down to hidden_dim.
    """

    def __init__(self,
                 hidden_dim: int = 256,
                 freeze_cpc: bool = True,
                 freeze_hubert: bool = True):
        super().__init__()
        # CPC branch (frozen by default)
        self.cpc    = EncoderCPC(load_pretrained=True, freeze=freeze_cpc)
        # HuBERT branch (frozen by default)
        self.hubert = EncoderHubert(freeze=freeze_hubert)
        # Project [256 + hubert_dim] → hidden_dim
        self.proj   = nn.Linear(self.cpc.dim + self.hubert.dim, hidden_dim)
        self.dim    = hidden_dim

    def forward(self, waveform: torch.Tensor) -> torch.Tensor:
        """
        waveform: (B,1,T) stereo/mono
        Returns: (B, L, hidden_dim)
        """
        # CPC
        z_cpc = self.cpc(waveform)         # (B, N1, 256)
        # HuBERT
        z_hub = self.hubert(waveform)      # (B, N2, hubert_dim)

        # truncate to min length
        L = 550  # Fixed length for consistency
        z_cpc = z_cpc[:, :L]
        z_hub = z_hub[:, :L]

        # fuse & project
        z_cat = torch.cat([z_cpc, z_hub], dim=-1)  # (B, L, 256+hubert_dim)
        return self.proj(z_cat)                     # (B, L, hidden_dim)

    def freeze(self):
        """Used by the VAP harness to freeze both encoders."""
        self.cpc.freeze()
        self.hubert.eval()
        for p in self.hubert.parameters():
            p.requires_grad_(False)

    def unfreeze(self):
        for p in self.cpc.parameters():
            p.requires_grad_(True)
        for p in self.hubert.model.parameters():
            p.requires_grad_(True) 