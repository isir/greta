import torch
import torch.nn as nn
import torch.nn.functional as F
from transformers import (
    AutoFeatureExtractor, HubertModel,
)


class EncoderHubert(nn.Module):
    """
    Wrapper for HuBERT base encoder from HuggingFace.
    """
    def __init__(self,
                 model_name: str = "facebook/hubert-base-ls960",
                 freeze: bool = True):
        super().__init__()
        self.fe    = AutoFeatureExtractor.from_pretrained(model_name)
        self.model = HubertModel.from_pretrained(model_name)
        self.dim   = self.model.config.hidden_size
        if freeze:
            self.model.eval()
            for p in self.model.parameters():
                p.requires_grad_(False)

    def forward(self, waveform: torch.Tensor) -> torch.Tensor:
        x = waveform.squeeze(1) if waveform.ndim==3 else waveform
        inputs = self.fe(
            x.detach().cpu().numpy().tolist(),
            sampling_rate=16000,
            return_tensors="pt",
            padding=True
        )
        feats = inputs.input_values.to(x.device)
        outputs = self.model(input_values=feats)
        return outputs.last_hidden_state


