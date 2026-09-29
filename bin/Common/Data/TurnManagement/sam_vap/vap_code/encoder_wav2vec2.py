import torch
import torch.nn as nn
import torch.nn.functional as F
from transformers import (
    AutoFeatureExtractor, Wav2Vec2BertModel
)




class EncoderWav2Vec2Bert(nn.Module):
    """
    Wrapper for the Wav2Vec2-BERT model (audio + BERT fusion).
    Takes raw waveform, extracts features, and returns cross-modal embeddings.
    """
    def __init__(
        self,
        model_name: str = "facebook/wav2vec2-bert-rel-pos-large",
        freeze: bool = True
    ):
        super().__init__()
        self.fe    = AutoFeatureExtractor.from_pretrained(model_name)
        self.model = Wav2Vec2BertModel.from_pretrained(model_name)
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
        audio_feats = inputs.input_values.to(x.device)
        outputs = self.model(input_features=audio_feats)
        return outputs.last_hidden_state