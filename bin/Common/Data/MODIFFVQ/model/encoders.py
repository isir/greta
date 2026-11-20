import torch
import torch.nn as nn

class MotionAudioCrossAttention(nn.Module):
    def __init__(self, d_model=128, n_heads=8, dropout=0.1):
        super().__init__()
        self.cross_attn = nn.MultiheadAttention(
            embed_dim=d_model,
            num_heads=n_heads,
            dropout=dropout,
            batch_first=True
        )
        self.norm = nn.LayerNorm(d_model)
        self.dropout = nn.Dropout(dropout)
        

    def forward(self, motion, audio):
        # motion: [B, W, D] - keys and values 
        # audio:  [B, W, D] - queries
        attn_out, _ = self.cross_attn(
            query=motion,   
            key=audio,    
            value=audio
        )
        # residual connection
        return self.norm(motion + self.dropout(attn_out))  # [B, W, D]
    
class AUsEncoder(nn.Module):
    def __init__(self, in_features=17, d_model=128,dropout=0.1):
        super().__init__()
        self.embed = nn.Sequential(
            nn.Linear(in_features, d_model),
            nn.LayerNorm(d_model),
            nn.GELU(),
            nn.Dropout(dropout),
            nn.Linear(d_model, d_model),  #
            nn.LayerNorm(d_model),
            nn.GELU()
        )

    def forward(self, x):
        # x: [B, W, T, A]
        B, W, T, A = x.shape
        x = x.float().reshape(B * W * T, A)
        x = self.embed(x)
        x = x.view(B, W, T, -1)  # [B, W, T, D]
        return x
    
class AudioEncoder(nn.Module):
    def __init__(self,
                in_features: int = 1024,
                d_model: int = 128,
                num_layers: int = 24):

        super().__init__()

        self.in_features = in_features 

        hidden_layer = d_model * 4

        self.layer_weights = nn.Parameter(torch.zeros(num_layers))  

        self.audio_embed = nn.Sequential(
            nn.Linear(in_features, hidden_layer),
            nn.LayerNorm(hidden_layer),
            nn.GELU(),
            nn.Dropout(0.1),
            nn.Linear(hidden_layer, d_model)
        )

        self.temporal_score = nn.Linear(d_model, 1)

    def forward(self, x):
        # x: [B, W, L, T_audio, F]  where L is the number of layers in the audio encoder
        B, W, L, T_audio, F = x.shape         

        w = torch.softmax(self.layer_weights, dim=0)   # [L]
        x = (x * w.view(1, 1, L, 1, 1)).sum(dim=2)     # [B, W, T_audio, F]

        x = x.float().reshape(B * W, T_audio, F)
        x = self.audio_embed(x)
        scores  = self.temporal_score(x)
        weights = torch.softmax(scores, dim=1)
        x = (x * weights).sum(dim=1)
        return x.reshape(B, W, -1)


class DialogActEncoder(nn.Module):
    def __init__(self, d_model=128):
        super().__init__()

        self.d_model = d_model

        # categorical embeddings
        self.client_talk_type = nn.Embedding(4, d_model)

        self.reflection_subtype = nn.Embedding(3, d_model)
        self.question_subtype = nn.Embedding(3, d_model)
        self.main_therapist_behaviour = nn.Embedding(5, d_model)
        self.therapist_input_subtype = nn.Embedding(5, d_model)

        # silences (binary features)
        self.silence_proj = nn.Sequential(
            nn.Linear(4, d_model),
            nn.LayerNorm(d_model),
            nn.GELU()
        )

        # 6 sources * d_model - d_model
        self.fusion = nn.Sequential(
            nn.Linear(d_model * 6, d_model),
            nn.LayerNorm(d_model),
            nn.GELU()
        )

    def forward(self, z):
        """
        z: dict — categorical features [B, W], silences [B, W, 4]
        """
        e1 = self.client_talk_type(z["client_talk_type"].long())              # [B, W, D]
        e2 = self.reflection_subtype(z["reflection_subtype"].long())          # [B, W, D]
        e3 = self.question_subtype(z["question_subtype"].long())              # [B, W, D]
        e4 = self.main_therapist_behaviour(z["main_therapist_behaviour"].long())  # [B, W, D]
        e5 = self.therapist_input_subtype(z["therapist_input_subtype"].long())    # [B, W, D]
        e6 = self.silence_proj(z["silences"].float())                         # [B, W, D]

        x = torch.cat([e1, e2, e3, e4, e5, e6], dim=-1)  # [B, W, 6*D]
        x = self.fusion(x)                                 # [B, W, D]
        return x 
    
    
class SinusoidalEmbeddings(nn.Module):
    def __init__(self, d_model, max_len=500):
        super().__init__()

        pe = torch.zeros(max_len, d_model)  # [T, D]

        position = torch.arange(0, max_len).unsqueeze(1)  # [T, 1]

        div_term = torch.exp(
            torch.arange(0, d_model, 2) *
            (-torch.log(torch.tensor(10000.0)) / d_model)
        )

        pe[:, 0::2] = torch.sin(position * div_term)
        pe[:, 1::2] = torch.cos(position * div_term)

        self.register_buffer("pe", pe)

    def forward(self, x):
        # x: [B, W, D]
        W = x.size(1)

        pe = self.pe[:W].unsqueeze(0)  # [1, W, D]


        return x + pe
