# vap/modules/encoder_dual_wav2vec2.py
import torch
import torch.nn as nn
from encoder import EncoderCPC
from encoder_wav2vec2 import EncoderWav2Vec2Bert

class EncoderDualWav2Vec2(nn.Module):
    """
    Early‐fusion of CPC + Wav2Vec2-BERT embeddings.
    Concatenate along the feature dim and project down to hidden_dim.
    """

    def __init__(self,
                 hidden_dim: int = 256,
                 freeze_cpc: bool = True,
                 freeze_wav2vec2: bool = True):
        super().__init__()
        # CPC branch (frozen by default)
        self.cpc      = EncoderCPC(load_pretrained=True, freeze=freeze_cpc)
        # Wav2Vec2-BERT branch (frozen by default)
        self.wav2vec2 = EncoderWav2Vec2Bert(freeze=freeze_wav2vec2)
        # Project [256 + wav2vec2_dim] → hidden_dim
        self.proj     = nn.Linear(self.cpc.dim + self.wav2vec2.dim, hidden_dim)
        self.dim      = hidden_dim

    def forward(self, waveform: torch.Tensor) -> torch.Tensor:
        """
        waveform: (B,1,T) stereo/mono
        Returns: (B, L, hidden_dim)
        """
        # CPC
        z_cpc = self.cpc(waveform)           # (B, N1, 256)
        # Wav2Vec2-BERT
        z_w2v = self.wav2vec2(waveform)      # (B, N2, wav2vec2_dim)

        # truncate to min length
        L = 550  # Fixed length for consistency
        z_cpc = z_cpc[:, :L]
        z_w2v = z_w2v[:, :L]

        # fuse & project
        z_cat = torch.cat([z_cpc, z_w2v], dim=-1)  # (B, L, 256+wav2vec2_dim)
        return self.proj(z_cat)                     # (B, L, hidden_dim)

    def freeze(self):
        """Used by the VAP harness to freeze both encoders."""
        self.cpc.freeze()
        self.wav2vec2.eval()
        for p in self.wav2vec2.parameters():
            p.requires_grad_(False)

    def unfreeze(self):
        for p in self.cpc.parameters():
            p.requires_grad_(True)
        for p in self.wav2vec2.model.parameters():
            p.requires_grad_(True) 