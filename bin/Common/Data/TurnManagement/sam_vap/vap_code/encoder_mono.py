
import torch
import torch.nn as nn
# EncoderCPC was originally used in early experiments; now Mono encoder focuses on a single foundation model
from encoder_whisper import EncoderWhisper
from encoder_hubert import EncoderHubert
from encoder_wav2vec2 import EncoderWav2Vec2Bert

class EncoderMono(nn.Module):
    """
    Generic *mono* audio encoder wrapper.

    Depending on the ``encoder_name`` argument, it delegates the heavy lifting to
    either Whisper, HuBERT, Wav2Vec2-BERT (or any future foundation model).

    Returned tensor shape: ``(B, L, C)`` where *C* equals the selected model's
    hidden size. No CPC is involved – this is a single-branch encoder.
    """

    def __init__(self,
                 encoder_name: str = "whisper",
                 hidden_dim: int = 256,
                 freeze_encoder: bool = True):
        super().__init__()
        # Dynamically choose the underlying foundation model
        name = encoder_name.lower()

        self.freeze_encoder = freeze_encoder


        if "whisper" in name:
            self.encoder = EncoderWhisper(freeze=self.freeze_encoder)
        elif "hubert" in name:
            self.encoder = EncoderHubert(freeze=self.freeze_encoder)
        elif "wav2vec" in name or "w2v" in name:
            self.encoder = EncoderWav2Vec2Bert(freeze=self.freeze_encoder)
        else:
            raise ValueError(f"Unknown encoder_name '{encoder_name}'. Supported: whisper | hubert | wav2vec*")


        # Expose hidden size for downstream projection if needed
        self.dim = getattr(self.encoder, "dim", None)
        self.proj = nn.Linear(self.dim, hidden_dim)

    def forward(self, waveform: torch.Tensor) -> torch.Tensor:
        """
        waveform: (B,1,T) stereo/mono

        """

        # Encoder
        z_enc  = self.encoder(waveform)   # (B, L, C)

        # (Optional) truncate to a fixed maximum length to align with downstream requirements
        L_max = 550
        if z_enc.size(1) > L_max:
            z_enc = z_enc[:, :L_max]
        out = self.proj(z_enc)

        return out       # (B, L, hidden_dim)

    def freeze(self):
        """Used by the VAP harness to freeze both encoders."""
        self.encoder.eval()
        for p in self.encoder.parameters():
            p.requires_grad_(False)

    def unfreeze(self):
        for p in self.encoder.parameters():
            p.requires_grad_(True)
