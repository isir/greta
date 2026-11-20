# encoder_dual.py
import torch
import torch.nn as nn
from torch import Tensor

from encoder import EncoderCPC
from encoder_wavlm import EncoderWavLM


class EncoderDual(nn.Module):
    def __init__(self, hidden_dim: int = 256, context_limit_cpc_sec: float = -1.0,
                 freeze_cpc: bool = True,
                 freeze_wlm: bool = True):
        super().__init__()
        self.cpc   = EncoderCPC(load_pretrained=True, freeze=freeze_cpc)
        self.wavlm = EncoderWavLM(freeze=freeze_wlm)
        self.proj = nn.Linear(self.cpc.dim + self.wavlm.dim, hidden_dim)
        self.dim = hidden_dim

        # how many seconds of audio to keep (–1 means “all”)
        self.context_limit_cpc_sec = context_limit_cpc_sec


    def forward(self, waveform: Tensor):
        # waveform: (B, 1, T)
        B, C, T = waveform.shape
        if self.context_limit_cpc_sec > 0:
            max_samples = int(self.context_limit_cpc_sec * 16000)
            if T > max_samples:
                # keep only the last max_samples for both encoders
                waveform = waveform[:, :, -max_samples:]  # shape (B,1,max_samples)

        # 1) CPC branch
        z_cpc = self.cpc(waveform)  # yields (B, L, 256), where L = max_samples/320

        # 2) WavLM branch
        # collapse channel
        mono = waveform[:, 0, :]  # (B, max_samples)
        z_wlm = self.wavlm(mono)  # yields (B, L, 768), same L

        # 3) fuse & project

        L = 550   #AUDIO TARGET SAMPLE LENGTH

        z = torch.cat([z_cpc[:, :L], z_wlm[:, :L]], dim=-1)  # (B, L, 256+768)
        return self.proj(z)  # (B, L, hidden_dim)

    def freeze(self) -> None:
        """Called by the VAP harness to freeze the encoder branch."""
        # Freeze CPC
        try:
            self.cpc.freeze()
        except AttributeError:
            for p in self.cpc.parameters():
                p.requires_grad_(False)
        # Freeze WavLM
        try:
            self.wavlm.model.eval()
            for p in self.wavlm.model.parameters():
                p.requires_grad_(False)
        except AttributeError:
            for p in self.wavlm.parameters():
                p.requires_grad_(False)

    def unfreeze(self) -> None:
        """If you ever want to unfreeze for later fine‐tuning."""
        for p in self.cpc.parameters():
            p.requires_grad_(True)
        for p in self.wavlm.parameters():
            p.requires_grad_(True)
