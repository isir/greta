import torch
import torch.nn as nn
from model.encoders import AUsEncoder, DialogActEncoder, AudioEncoder, SinusoidalEmbeddings, MotionAudioCrossAttention
#from encoders import AUsEncoder, DialogActEncoder, AudioEncoder, SinusoidalEmbeddings, MotionAudioCrossAttention

class AllPipeline(nn.Module):
    def __init__(self,
            client_aus_encoder: AUsEncoder,
            therapist_aus_encoder: AUsEncoder,
            pos_encoder: SinusoidalEmbeddings,
            dialog_act_encoder: DialogActEncoder,
            audio_encoder: AudioEncoder = None,
            W: int = 8,
            d_model: int = 128,
            n_heads: int = 8,
            num_layers_transformer: int = 2,
            dropout: float = 0.3,
            vqvae = None,
            K: int = 64,
            T_down: int = 9,
            use_therapist_prev: bool = True,
            use_vqvae_enc: bool = False,
            p_mask_therap:float = 0.5):
        
        super().__init__()
        self.client_aus_enc = client_aus_encoder
        self.therapist_aus_enc = therapist_aus_encoder
        self.pos_enc = pos_encoder
        self.audio_enc = audio_encoder
        self.dialog_act_enc = dialog_act_encoder
        self.temporal_model =nn.TransformerEncoder(
            nn.TransformerEncoderLayer(
                d_model=d_model,
                nhead=n_heads,
                dim_feedforward=256,
                dropout=dropout,
                batch_first=True
            ),
            num_layers=num_layers_transformer
        )
        self.head=nn.Linear(d_model, K * T_down)  # Predicting the next chunk of AUs (flattened)
        self.K = K
        self.T_down = T_down
        self.W = W
        self.use_therapist_prev = use_therapist_prev

        self.p_mask_therap = p_mask_therap

        self.vqvae = vqvae
        if vqvae is not None:
            for param in self.vqvae.parameters():
                param.requires_grad = False

        self.fusion_proj = nn.Sequential(
            nn.Linear(d_model * 2, d_model),  # AUs + dialog acts
            nn.LayerNorm(d_model),
            nn.GELU(),
            nn.Dropout(dropout)
        )

        self.use_vqvae_enc = use_vqvae_enc
        if self.use_vqvae_enc:
            self.therapist_idx_embed = nn.Embedding(K, d_model)

        self.therap_audio_cross_attn = MotionAudioCrossAttention(
            d_model=d_model, n_heads=n_heads, dropout=dropout
        )
        
        self.motion_audio_cross_attn = MotionAudioCrossAttention(
            d_model=d_model, n_heads=n_heads, dropout=dropout
        )

        '''
        self.audio_fusion_proj = nn.Sequential(
            nn.Linear(d_model * 2, d_model),  # audio_client + audio_therapist
            nn.LayerNorm(d_model),
            nn.GELU(),
            nn.Dropout(dropout)
        )
        '''

        self.sos_token = nn.Parameter(torch.zeros(1, 1, d_model))
        
    def forward(self, x_au_client, x_au_therapist_prev, 
                x_dialog_acts=None, 
                x_audio_therapist=None,x_audio_client=None,
                is_new_session=None):
        """
        x_au_client:          [B, W, T, A]  : W chunks of client AUs
        x_au_therapist_prev:  [B, T, A]     : last chunk of therapist AUs (context)
        x_dialog_acts:        dict [B, W, T] tensors : W chunks of dialog act features (context)
        """

        #encoding AUs client
        x = self.client_aus_enc(x_au_client)   # [B, W, T, D]
        x = x.max(dim=2).values                # [B, W, D]

        # Dialog acts as context 
        if x_dialog_acts is not None:
            da = self.dialog_act_enc(x_dialog_acts)  # [B, W, D] 
            x  = self.fusion_proj(torch.cat([x, da], dim=-1))

        # audio as context
        if x_audio_client is not None and x_audio_client.dim() > 1: 
            #print('client shape:',x_audio_client.shape)
            audio_emb = self.audio_enc(x_audio_client)
            x = self.motion_audio_cross_attn(motion=x, audio=audio_emb)

        if self.use_therapist_prev:

            if self.use_vqvae_enc:
                with torch.no_grad():
                    idx = self.vqvae.get_indices(x_au_therapist_prev.float())  # [B, T_down]
                tok = self.therapist_idx_embed(idx)  # [B, T_down, D]
                therap_token = tok.mean(dim=1, keepdim=True)  # [B, 1, D]
            else:
                x_au_therapist_prev = x_au_therapist_prev.unsqueeze(1)  # [B, 1, T, A]
                therap_token = self.therapist_aus_enc(
                    x_au_therapist_prev   # [B, 1, T, A]
                ).max(dim=2).values                    # [B, 1, D]

            if is_new_session is not None:
                    mask = is_new_session.bool()  # [B]
                    if mask.any():
                        sos = self.sos_token.view(1, 1, -1)  # [1, 1, D]
                        therap_token[mask] = sos.expand(mask.sum(), 1, -1)

            B = therap_token.size(0)
            if self.training and self.p_mask_therap > 0.0:
                drop = torch.rand(B, device=therap_token.device) < self.p_mask_therap
                if drop.any():
                    sos = self.sos_token.view(1, 1, -1)
                    therap_token[drop] = sos.expand(drop.sum(), 1, -1)

        else:
            B = x.size(0)
            therap_token = self.sos_token.expand(B, 1, -1)

        if x_audio_therapist is not None and x_audio_therapist.dim() > 1: 
            x_audio_therapist = x_audio_therapist.unsqueeze(1)
            #print('therapist shape:',x_audio_therapist.shape)
            audio_therap = self.audio_enc(x_audio_therapist)
            therap_token = self.therap_audio_cross_attn(
                motion=therap_token, audio=audio_therap
            )
     

        #concat at the start so the transformer can attend to it from any position
        x = torch.cat([therap_token, x], dim=1)  # [B, W+1, D]

        # Positional encoding
        x = self.pos_enc(x)  # [B, W+1, D]

        # Causal mask: W+1 tokens
        causal_mask = nn.Transformer.generate_square_subsequent_mask(
            x.size(1), device=x.device
        )
        
        x = self.temporal_model(x, mask=causal_mask, is_causal=True)  # [B, W+1, D]

        logits = self.head(x[:, -1])  # [B, K*T_down]
        logits = logits.view(-1, self.T_down, self.K)  # [B, T_down, K]
        return logits
    
    def predict_aus(self, x_au_client, x_au_therapist_prev, 
                x_dialog_acts=None, 
                x_audio_therapist=None,x_audio_client=None,
                is_new_session=None, temperature=1.0, sample=False):
        logits = self.forward(x_au_client, x_au_therapist_prev,
                x_dialog_acts, x_audio_therapist, x_audio_client, is_new_session)
        
        if sample:
            probs = torch.softmax(logits / temperature, dim=-1)
            B, T_down, K = probs.shape
            indices = torch.multinomial(probs.view(-1, K), 1).view(B, T_down)
        else:
            indices = logits.argmax(dim=-1)
        
        with torch.no_grad():
            zq  = self.vqvae.codebook(indices)
            aus = self.vqvae.decode(zq)
        return aus

    def load_model(self, path):
        checkpoint = torch.load(path, map_location='cpu')
        self.load_state_dict(checkpoint['model_state'])
