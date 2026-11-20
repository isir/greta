# vap/modules/encoder_whisper.py
import torch
import torch.nn as nn
import whisper
import torch.nn.functional as F
from transformers import WhisperFeatureExtractor, WhisperModel

class EncoderWhisper(nn.Module):
    """
    Wrapper around Whisper Tiny’s encoder. Takes raw waveform,
    computes log‑mels, and returns the encoder’s last hidden states.
    """

    def __init__(
            self,
            model_name: str = "openai/whisper-tiny.en",
            freeze: bool = True
    ):
        super().__init__()
        # Feature extractor handles log‑mel + padding

        self.fe = WhisperFeatureExtractor.from_pretrained(model_name)
        # The actual encoder model
        self.model = WhisperModel.from_pretrained(model_name)
        self.encoder = self.model.encoder
        self.dim = self.model.config.hidden_size  # usually 384 for tiny
        self.max_length = 3000

        if freeze:
            self.model.eval()
            for p in self.model.parameters():
                p.requires_grad_(False)

    def forward(self, waveform: torch.Tensor) -> torch.Tensor:
        """
        waveform: (B,1,T) or (B,T), float32 in [-1,1]
        Returns: Tensor of shape (B, L, dim)
        """
        x = waveform
        if x.ndim == 3:
            # collapse channel if stereo: (B,1,T) -> (B,T)
            x = x[:, 0, :]

        # HF extractor expects list or batch of numpy arrays
        inputs = self.fe(
            x.detach().cpu().numpy().tolist(),
            sampling_rate=16000,
            return_tensors="pt",
            padding=True
        )

        input_features = inputs.input_features.to(waveform.device)

        mel = inputs.input_features.to(x.device)  # (B, n_mels, L)

        B, n_mels, L = mel.shape
        if L < self.max_length:
            # pad right side to max_len
            pad_amt = self.max_length - L
            mel = F.pad(mel, (0, pad_amt), "constant", 0.0)
        elif L > self.max_length:
            # truncate to max_len
            mel = mel[:, :, : self.max_length]


        # Move inputs back to device
          # (B, n_feats, T_feat)
        # Run through the encoder; HF model returns last_hidden_state = (B, seq_len, dim)
        outputs = self.encoder(input_features=mel)
        return outputs.last_hidden_state

    @property
    def name(self):
        return "whisper"
