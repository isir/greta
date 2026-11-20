# vap/modules/encoder_dual_whisper.py
import torch
import torch.nn as nn
from encoder import EncoderCPC
from encoder_whisper import EncoderWhisper

class EncoderDualWhisper(nn.Module):
    """
    Early‐fusion of CPC + Whisper Tiny embeddings.
    Concatenate along the feature dim and project down to hidden_dim.
    """

    def __init__(self,
                 hidden_dim: int = 256,
                 freeze_cpc: bool = True,
                 freeze_whisper: bool = True):
        super().__init__()
        # CPC branch (frozen by default)
        self.cpc     = EncoderCPC(load_pretrained=True, freeze=freeze_cpc)
        # Whisper branch (frozen by default)
        self.whisper = EncoderWhisper(freeze=freeze_whisper)
        # Project [256 + 384] → hidden_dim
        self.proj    = nn.Linear(self.cpc.dim + self.whisper.dim, hidden_dim)
        self.dim     = hidden_dim

    def forward(self, waveform: torch.Tensor) -> torch.Tensor:
        """
        waveform: (B,1,T) stereo/mono
        Returns: (B, L, hidden_dim)
        """
        # CPC
        z_cpc = self.cpc(waveform)       # (B, N1, 256)
        # Whisper
        z_wh  = self.whisper(waveform)   # (B, N2, 384)

        # truncate to min length
        #L = min(z_cpc.size(1), z_wh.size(1))
        L = 550
        z_cpc = z_cpc[:, :L]
        z_wh  = z_wh[:, :L]

        # fuse & project
        z_cat = torch.cat([z_cpc, z_wh], dim=-1)  # (B, L, 256+384)
        return self.proj(z_cat)                   # (B, L, hidden_dim)

    def freeze(self):
        """Used by the VAP harness to freeze both encoders."""
        self.cpc.freeze()
        self.whisper.eval()
        for p in self.whisper.parameters():
            p.requires_grad_(False)

    def unfreeze(self):
        for p in self.cpc.parameters():
            p.requires_grad_(True)
        for p in self.whisper.encoder.parameters():
            p.requires_grad_(True)
