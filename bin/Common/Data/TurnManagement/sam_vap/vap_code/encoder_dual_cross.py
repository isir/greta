
# vap/modules/encoder_dual_cross.py
import torch
import torch.nn as nn
import torch.nn.functional as F
from encoder import EncoderCPC
from encoder_whisper import EncoderWhisper
from encoder_hubert import EncoderHubert
from encoder_wav2vec2 import EncoderWav2Vec2Bert

class EncoderDualCross(nn.Module):
    """
    Early cross‐fusion of CPC (prosody) with a foundation model (semantics) via Multi‐Head Attention.
    
    The foundation model can be one of:
    - Whisper: OpenAI's speech-to-text model
    - HuBERT: Facebook's speech representation model
    - Wav2Vec2-BERT: Facebook's speech+text fusion model
    
    Architecture:
    1. Extract features from CPC and chosen foundation model
    2. Project both to common dimension
    3. Self-attention on CPC features
    4. Cross-attention: CPC queries foundation model's memory
    5. Final fusion projection
    """
    def __init__(
        self,
        hidden_dim: int = 256,
        n_heads: int = 4,
        freeze_cpc: bool = True,
        freeze_foundation: bool = True,
        encoder_name: str = "whisper"
    ):
        super().__init__()
        # 1) CPC Encoder (always present)
        self.cpc = EncoderCPC(load_pretrained=True, freeze=freeze_cpc)
        
        # 2) Foundation Model Encoder (chosen by name)
        name = encoder_name.lower()
        if "whisper" in name:
            self.foundation = EncoderWhisper(freeze=freeze_foundation)
        elif "hubert" in name:
            self.foundation = EncoderHubert(freeze=freeze_foundation)
        elif "wav2vec" in name or "w2v" in name:
            self.foundation = EncoderWav2Vec2Bert(freeze=freeze_foundation)
        else:
            raise ValueError(f"Unknown encoder_name '{encoder_name}'. Supported: whisper | hubert | wav2vec*")

        # 3) Projections to common hidden_dim
        self.proj_cpc = nn.Linear(self.cpc.dim, hidden_dim)
        self.proj_foundation = nn.Linear(self.foundation.dim, hidden_dim)

        # 4) Self‐ and cross‐attention layers
        self.self_attn  = nn.MultiheadAttention(hidden_dim, n_heads, batch_first=True)
        self.cross_attn = nn.MultiheadAttention(hidden_dim, n_heads, batch_first=True)

        # 5) Output projection
        self.out_proj = nn.Linear(hidden_dim, hidden_dim)
        self.dim = hidden_dim

    def forward(self, waveform: torch.Tensor) -> torch.Tensor:
        # Encode audio through both paths
        z_cpc = self.cpc(waveform)              # (B, L_cpc, cpc_dim)
        z_fnd = self.foundation(waveform)       # (B, L_fnd, foundation_dim)

        # Project into same feature space
        z_cpc = self.proj_cpc(z_cpc)           # (B, L_cpc, H)
        z_fnd = self.proj_foundation(z_fnd)    # (B, L_fnd, H)

        # Align temporal lengths
        L = 550
        z_cpc = z_cpc[:, :L, :]
        z_fnd = z_fnd[:, :L, :]

        # Self‐attention on CPC features
        z_self, _  = self.self_attn(z_cpc, z_cpc, z_cpc)  # (B, L, H)

        # Cross‐attention: CPC queries foundation model's memory
        z_cross, _ = self.cross_attn(z_self, z_fnd, z_fnd)  # (B, L, H)

        # Final fusion projection
        z_fused = self.out_proj(z_cross)                    # (B, L, H)
        return z_fused

    def freeze(self) -> None:
        """Freeze both encoders for inference or finetuning."""
        self.cpc.freeze()
        self.foundation.eval()
        for p in self.foundation.parameters():
            p.requires_grad_(False)

    def unfreeze(self) -> None:
        """Unfreeze both encoders for training."""
        for p in self.cpc.parameters():
            p.requires_grad_(True)
        # Handle foundation model parameters - they might be nested differently
        if hasattr(self.foundation, "model"):
            for p in self.foundation.model.parameters():
                p.requires_grad_(True)
        else:
            for p in self.foundation.parameters():
                p.requires_grad_(True)
