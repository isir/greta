# encoder_wavlm.py
import torch.nn as nn
from transformers import WavLMModel, AutoFeatureExtractor

import torch
import torch.nn as nn
from transformers import AutoFeatureExtractor, WavLMModel


class EncoderWavLM(nn.Module):
    def __init__(self,
                 model_name: str = "microsoft/wavlm-base-plus",
                 freeze: bool = True):
        super().__init__()
        # Generic feature extractor knows how to prep WavLM inputs
        self.fe = AutoFeatureExtractor.from_pretrained(model_name)
        self.model = WavLMModel.from_pretrained(model_name)
        self.dim = self.model.config.hidden_size  # typically 768

        if freeze:
            for p in self.model.parameters():
                p.requires_grad_(False)

    def forward(self, waveform: torch.Tensor) -> torch.Tensor:
        # waveform: (B, 1, T) or (B, T)
        # 1) Collapse any channel dim -> (B, T)
        if waveform.ndim == 3:
            x = waveform[:, 0, :]  # (B, T)
        else:
            x = waveform  # already (B, T)

        # 2) Convert to CPU numpy for the extractor
        x_cpu = x.detach().cpu().numpy()

        # 3) Run feature extractor -> returns dict of numpy/torch, padded to same length
        inputs = self.fe(x_cpu,
                         sampling_rate=16000,
                         return_tensors="pt",
                         padding=True)

        # 4) Grab only the input_values, move back to original device
        input_values = inputs["input_values"].to(waveform.device)  # (B, L)
        #print("WavLM input_values shape:", input_values.shape)  # should be [B, L]

        # 5) Forward through the WavLM model by keyword only
        outputs = self.model(input_values=input_values)
        # last_hidden_state: (B, L, dim)
        #print("WavLM last_hidden_state:", outputs.last_hidden_state.shape)  # [B, L, 768]

        return outputs.last_hidden_state



