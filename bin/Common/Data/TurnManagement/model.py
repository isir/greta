import torch
import torch.nn as nn
import torch.nn.functional as F
from torch import Tensor

from dataclasses import dataclass, field
from typing import Dict, Tuple, List, Optional, Union

from encoder import EncoderCPC, EncoderFormerDFER
from objective import ObjectiveVAP
from modules import GPT, GPTStereo, Linear, LinearStereo, TransformerLayer, TransformerConcatLayer
from utils import (
    everything_deterministic,
    vad_fill_silences,
    vad_omit_spikes,
)

import math

import gc

BIN_TIMES: list = [0.2, 0.4, 0.6, 0.8]

everything_deterministic()

# TODO: @dataclass and CLI arguments or is hydra the way to go?
# TODO: Easy finetune task
# TODO: What to evaluate


def load_older_state_dict(
    path="example/VAP_3mmz3t0u_50Hz_ad20s_134-epoch9-val_2.56.ckpt",
):
    sd = torch.load(path)["state_dict"]
    new_sd = {}
    for k, v in sd.items():
        if "VAP.codebook" in k:
            continue
        if "vap_head" in k:
            k = k.replace("vap_head.projection_head", "vap_head")
        new_sd[k.replace("net.", "")] = v
    return new_sd


@dataclass
class VapConfig:
    sample_rate: int = 16_000
    frame_hz: int = 50
    bin_times: List[float] = field(default_factory=lambda: BIN_TIMES)

    # Encoder (training flag)
    encoder_type: str = "cpc"
    wav2vec_type: str = "mms"
    hubert_model: str = "hubert_jp"
    
    # cpc_model_pt: str = "default"
    
    pretrained_vap: str = None
    pretrained_cpc: str = None
    
    freeze_encoder: int = 1  # stupid but works (--vap_freeze_encoder 1)
    load_pretrained: int = 1  # stupid but works (--vap_load_pretrained 1)
    only_feature_extraction: int = 0

    # GPT
    dim: int = 256
    channel_layers: int = 1
    cross_layers: int = 3
    num_heads: int = 4
    dropout: float = 0.1
    context_limit: int = -1
    
    # Onishi2023
    onishi: bool = False
    dim_nonverbal: int = 64
    dim_gaze: int = 2 #without confidence score
    dim_head: int = 3 #without confidence score
    dim_face: int = 17 #without confidence score
    dim_body: int = 14 #without confidence score
    # dim_input_nonverbal: int = dim_gaze + dim_head + dim_face + dim_body
    face_only: bool = False
    
    # Saga2024
    use_face_encoder: bool = False
    pretrained_face_encoder: str = None
    dim_face_encoder: int = 512
    # mode: int = 0 # audio encoder, face encoder, gaze, head, body
    mode: int = 1 # audio encoder, face encoder
    

    multimodal: bool = False

    context_limit_cpc_sec: float = -1

    # Added Multi-task
    lid_classify: int = 0   # 1...last layer, 2...middle layer
    lid_classify_num_class: int = 3
    lid_classify_adversarial: int = 0
    lang_cond: int = 0

    @staticmethod
    def add_argparse_args(parser, fields_added=[]):
        for k, v in VapConfig.__dataclass_fields__.items():
            if k == "bin_times":
                parser.add_argument(
                    f"--vap_{k}", nargs="+", type=float, default=v.default_factory()
                )
            # elif k == "mode":
            #     parser.add_argument(
            #         f"--vap_{k}", nargs="+", type=int, default=v.default_factory()
            #     )
            elif (k == 'onishi') or (k == 'multimodal') or (k == 'use_face_encoder') or (k == 'face_only'):
                parser.add_argument(f'--vap_{k}', action='store_true')
            else:
                parser.add_argument(f"--vap_{k}", type=v.type, default=v.default)
            fields_added.append(k)
                
        return parser, fields_added

    @staticmethod
    def args_to_conf(args):
        return VapConfig(
            **{
                k.replace("vap_", ""): v
                for k, v in vars(args).items()
                if k.startswith("vap_")
            }
        )


@dataclass
class VapMonoConfig:
    sample_rate: int = 16_000
    frame_hz: int = 50
    bin_times: List[float] = field(default_factory=lambda: BIN_TIMES)
    mono: bool = True  # INFO: only used in mono model
    va_history: bool = False  # INFO: only used in mono model
    va_history_bins: int = 5  # INFO: only used in mono model

    # Encoder
    freeze_encoder: bool = True
    load_pretrained: bool = True

    # GPT
    dim: int = 256
    channel_layers: int = 1
    cross_layers: int = 3
    num_heads: int = 4
    dropout: float = 0.1

    @staticmethod
    def add_argparse_args(parser, fields_added=[]):
        for k, v in VapConfig.__dataclass_fields__.items():
            if k == "bin_times":
                parser.add_argument(
                    f"--vap_{k}", nargs="+", type=float, default=v.default_factory()
                )
            else:
                parser.add_argument(f"--vap_{k}", type=v.type, default=v.default)
            fields_added.append(k)
        return parser, fields_added

    @staticmethod
    def args_to_conf(args):
        return VapConfig(
            **{
                k.replace("vap_", ""): v
                for k, v in vars(args).items()
                if k.startswith("vap_")
            }
        )


class VapGPTMono(nn.Module):
    def __init__(self, conf: Optional[VapMonoConfig] = None):
        super().__init__()
        if conf is None:
            conf = VapMonoConfig()
        self.conf = conf
        self.sample_rate = conf.sample_rate
        self.frame_hz = conf.frame_hz

        # Audio Encoder
        self.encoder = EncoderCPC(freeze=conf.freeze_encoder)
        self.init_va_conditioning()

        # Single channel
        self.ar_channel = GPT(
            dim=conf.dim,
            dff_k=3,
            num_layers=conf.channel_layers,
            num_heads=conf.num_heads,
            dropout=conf.dropout,
        )

        # Cross channel
        self.ar = GPT(
            dim=conf.dim,
            dff_k=3,
            num_layers=conf.cross_layers,
            num_heads=conf.num_heads,
            dropout=conf.dropout,
        )

        self.objective = ObjectiveVAP(bin_times=conf.bin_times, frame_hz=conf.frame_hz)

        # Outputs
        # Voice activity objective -> z -> logits ->  BCE
        self.vap_head = nn.Linear(conf.dim, self.objective.n_classes)

    def init_va_conditioning(self) -> None:
        self.va_condition = nn.Linear(2, self.conf.dim)
        self.va_cond_ln = nn.LayerNorm(self.conf.dim)
        nn.init.orthogonal_(self.va_condition.weight.data)

        if self.conf.va_history:
            self.va_cond_history = nn.Linear(self.conf.va_history_bins, self.conf.dim)

    @torch.no_grad()
    def probs(
        self,
        waveform: Tensor,
        vad: Tensor,
        now_lims: List[int] = [0, 1],
        future_lims: List[int] = [2, 3],
    ) -> Dict[str, Tensor]:
        out = self(waveform, vad)
        probs = out["logits"].softmax(dim=-1)

        # Calculate entropy over each projection-window prediction (i.e. over
        # frames/time) If we have C=256 possible states the maximum bit entropy
        # is 8 (2^8 = 256) this means that the model have a one in 256 chance
        # to randomly be right. The model can't do better than to uniformly
        # guess each state, it has learned (less than) nothing. We want the
        # model to have low entropy over the course of a dialog, "thinks it
        # understands how the dialog is going", it's a measure of how close the
        # information in the unseen data is to the knowledge encoded in the
        # training data.
        h = -probs * probs.log2()  # Entropy
        H = h.sum(dim=-1)  # average entropy per frame

        # first two bins
        p_now = self.objective.probs_next_speaker_aggregate(
            probs, from_bin=now_lims[0], to_bin=now_lims[-1]
        )
        p_future = self.objective.probs_next_speaker_aggregate(
            probs, from_bin=future_lims[0], to_bin=future_lims[1]
        )
        return {
            "probs": probs,
            "vad": vad,
            "p_now": p_now,
            "p_future": p_future,
            "H": H,
        }

    def encode_va(self, va: Tensor, va_history: Optional[Tensor] = None) -> Tensor:
        v_cond = self.va_condition(va)
        # Add vad-history information
        if self.conf.va_history and va_history is not None:
            v_cond += self.va_cond_history(va_history)
        return self.va_cond_ln(v_cond)

    def encode_audio(self, audio: Tensor) -> Tuple[Tensor, Tensor]:
        assert (
            audio.shape[1] == 1
        ), f"audio VAP ENCODER: {audio.shape} != (B, 1, n_samples)"
        return self.encoder(audio)  # speaker 1

    def forward(
        self,
        waveform: Tensor,
        va: Tensor,
        va_history: Optional[Tensor] = None,
        attention: bool = False,
    ) -> Dict[str, Tensor]:
        assert not attention, "Attention Mono model is not implemented"
        x = self.encode_audio(waveform)

        # Ugly: sometimes you may get an extra frame from waveform encoding
        # x = x[:, : vad.shape[1]]
        # Add Vad conditioning
        x = x + self.encode_va(va, va_history)

        # Autoregressive
        x = self.ar_channel(x)["x"]
        x = self.ar(x)["x"]

        # Outputs
        logits = self.vap_head(x)
        ret = {"logits": logits, "vad": va}
        # if attention:
        #     ret["self_attn"] = torch.stack([o1["attn"], o2["attn"]], dim=1)
        #     ret["cross_attn"] = out["cross_attn"]
        #     ret["cross_self_attn"] = out["self_attn"]
        return ret

# HINT: VapGPT
class VapGPT(nn.Module):
    def __init__(self, conf: Optional[VapConfig] = None):
        super().__init__()
        if conf is None:
            conf = VapConfig()
        self.conf = conf
        self.sample_rate = conf.sample_rate
        self.frame_hz = conf.frame_hz

        self.temp_elapse_time = []

        # if torch.cuda.is_available():
        #     self.device = torch.device('cuda')
        # else:
        #     self.device = torch.device('cpu')

        # Audio Encoder
        if self.conf.encoder_type == "cpc":
            self.encoder = EncoderCPC(
                # cpc_model_pt=self.conf.cpc_model_pt,
                cpc_model_pt = self.conf.pretrained_cpc,
                load_pretrained=True if conf.load_pretrained == 1 else False,
                freeze=conf.freeze_encoder,
                lim_context_sec=conf.context_limit_cpc_sec,
                frame_hz = self.conf.frame_hz
            )
        
        elif self.conf.encoder_type == "wav2vec2":
            from vap.customwav2vec2 import W2V2Transformers
            self.encoder = W2V2Transformers(model_type=self.conf.wav2vec_type)
        
        elif self.conf.encoder_type == "hubert":
            from vap.customhubert import HubertEncoder
            self.encoder = HubertEncoder(model_type= self.conf.hubert_model)

        if self.conf.encoder_type == "wav2vec2":
                
            if self.conf.only_feature_extraction == 1:
                self.decrease_dimension = nn.Linear(512, 256)
            else:
                self.decrease_dimension = nn.Linear(1024, 256)
        
        elif self.conf.encoder_type == "hubert":
            
            if self.conf.hubert_model == "hubert_ja":

                if self.conf.only_feature_extraction == 1:
                    self.decrease_dimension = nn.Linear(512, 256)
                else:
                    self.decrease_dimension = nn.Linear(768, 256)
            
            elif self.conf.hubert_model == "hubert_en_large":
                
                if self.conf.only_feature_extraction == 1:
                    self.decrease_dimension = nn.Linear(512, 256)
                else:
                    self.decrease_dimension = nn.Linear(1024, 256)

        # Single channel
        self.ar_channel = GPT(
            dim=conf.dim,
            dff_k=3,
            num_layers=conf.channel_layers,
            num_heads=conf.num_heads,
            dropout=conf.dropout,
            context_limit=conf.context_limit,
        )

        # Cross channel
        self.ar = GPTStereo(
            dim=conf.dim,
            dff_k=3,
            num_layers=conf.cross_layers,
            num_heads=conf.num_heads,
            dropout=conf.dropout,
            context_limit=conf.context_limit,
        )
                
        if self.conf.onishi:
            
            if self.conf.face_only:
        
                self.ar_channel_face = Linear(
                    dim_in=conf.dim_face,
                    dim_out=conf.dim_nonverbal,
                    dropout=conf.dropout
                )
                
                ### Dual channel (intra-modal)
        
                self.ar_face = LinearStereo(
                    dim_in=conf.dim_nonverbal*2,
                    dim_out=conf.dim_nonverbal,
                    dropout=conf.dropout
                )
                
                num_nonverbal = 0
                for nonverbal_name in ['ar_channel_face']:
                    if hasattr(self, nonverbal_name):
                        num_nonverbal += 1
            
            else:
            
                ### Single channel
                
                self.ar_channel_gaze = Linear(
                    dim_in=conf.dim_gaze,
                    dim_out=conf.dim_nonverbal,
                    dropout=conf.dropout
                )
        
                self.ar_channel_head = Linear(
                    dim_in=conf.dim_head,
                    dim_out=conf.dim_nonverbal,
                    dropout=conf.dropout
                )
        
                self.ar_channel_face = Linear(
                    dim_in=conf.dim_face,
                    dim_out=conf.dim_nonverbal,
                    dropout=conf.dropout
                )
        
                self.ar_channel_body = Linear(
                    dim_in=conf.dim_body,
                    dim_out=conf.dim_nonverbal,
                    dropout=conf.dropout
                )
                
                ### Dual channel (intra-modal)
        
                self.ar_gaze = LinearStereo(
                    dim_in=conf.dim_nonverbal*2,
                    dim_out=conf.dim_nonverbal,
                    dropout=conf.dropout
                )
        
                self.ar_head = LinearStereo(
                    dim_in=conf.dim_nonverbal*2,
                    dim_out=conf.dim_nonverbal,
                    dropout=conf.dropout
                )
        
                self.ar_face = LinearStereo(
                    dim_in=conf.dim_nonverbal*2,
                    dim_out=conf.dim_nonverbal,
                    dropout=conf.dropout
                )
        
                self.ar_body = LinearStereo(
                    dim_in=conf.dim_nonverbal*2,
                    dim_out=conf.dim_nonverbal,
                    dropout=conf.dropout
                )
                
                num_nonverbal = 0
                for nonverbal_name in ['ar_channel_gaze', 'ar_channel_head', 'ar_channel_face', 'ar_channel_body']:
                    if hasattr(self, nonverbal_name):
                        num_nonverbal += 1
                
                # print(conf.dim)
                # print(num_nonverbal)
            
            ### Multi channel (inter-modal excluding audio)        
            self.nonverbal_transformer = TransformerConcatLayer(
                dim=conf.dim_nonverbal*num_nonverbal,
                ffn_dim=conf.dim_nonverbal*num_nonverbal*3,
                num_heads=conf.num_heads,
                dropout=conf.dropout,
                context_limit=conf.context_limit
            )
            
            ### Multi channel (inter-modal including audio)
            self.audio_nonverbal_transformer = TransformerConcatLayer(
                dim=conf.dim_nonverbal*num_nonverbal + conf.dim,
                ffn_dim=conf.dim_nonverbal*num_nonverbal*3,
                num_heads=conf.num_heads,
                dropout=conf.dropout,
                context_limit=conf.context_limit
            )
            
        elif self.conf.use_face_encoder:
            
            if self.conf.mode == 0:
            
                self.face_encoder = EncoderFormerDFER()
                
                if conf.load_pretrained:
                    self.face_encoder.load_state_dict(torch.load(conf.pretrained_face_encoder, weights_only=True), strict=False)
                    if conf.freeze_encoder:
                        self.face_encoder.eval()
                
                self.ar_channel_face_encoder = GPT(
                    dim=conf.dim_face_encoder,
                    dff_k=3,
                    num_layers=conf.channel_layers,
                    num_heads=conf.num_heads,
                    dropout=conf.dropout,
                    context_limit=conf.context_limit,
                )
                
                # self.ar_channel_face = Linear(
                #     dim_in=conf.dim_face_encoder,
                #     dim_out=conf.dim,
                #     dropout=conf.dropout
                # )
    
                self.ar_channel_gaze = Linear(
                    dim_in=conf.dim_gaze,
                    dim_out=conf.dim_nonverbal,
                    dropout=conf.dropout
                )
        
                self.ar_channel_head = Linear(
                    dim_in=conf.dim_head,
                    dim_out=conf.dim_nonverbal,
                    dropout=conf.dropout
                )
        
                self.ar_channel_body = Linear(
                    dim_in=conf.dim_body,
                    dim_out=conf.dim_nonverbal,
                    dropout=conf.dropout
                )
                
                num_nonverbal = 0
                for nonverbal_name in ['ar_channel_gaze', 'ar_channel_head', 'ar_channel_body']:
                    if hasattr(self, nonverbal_name):
                        num_nonverbal += 1
                
                ###
                ### Multi channel (inter-modal excluding audio)
                ###
                
                dim_intermodal = conf.dim_nonverbal*num_nonverbal + conf.dim_face_encoder + conf.dim
                dim_interperson = dim_intermodal * 2
                
                self.intermodal_transformer = TransformerConcatLayer(
                    dim=dim_intermodal,
                    ffn_dim=conf.dim_nonverbal*num_nonverbal*3,
                    num_heads=conf.num_heads,
                    dropout=conf.dropout,
                    context_limit=conf.context_limit
                )
    
                self.interperson_transformer = TransformerConcatLayer(
                    dim=dim_interperson,
                    # ffn_dim=conf.dim,
                    ffn_dim=conf.dim_nonverbal*num_nonverbal*3,
                    num_heads=conf.num_heads,
                    dropout=conf.dropout,
                    context_limit=conf.context_limit
                )
                
                # dim_intermodal = conf.dim_nonverbal*num_nonverbal + conf.dim_face_encoder + conf.dim
                
                # self.intermodal_GPT = GPT(
                #     dim=dim_intermodal,
                #     dff_k=3,
                #     num_layers=conf.channel_layers,
                #     num_heads=conf.num_heads,
                #     dropout=conf.dropout,
                #     context_limit=conf.context_limit,
                # )
                
                # ###
                # ### Multi channel (inter-modal excluding audio)        
                # ###
                # # self.interperson_transformer = TransformerConcatLayer(
                # #     dim=(conf.dim_nonverbal*num_nonverbal + conf.dim_face_encoder + conf.dim)*2,
                # #     ffn_dim=conf.dim_nonverbal*num_nonverbal*3,
                # #     num_heads=conf.num_heads,
                # #     dropout=conf.dropout,
                # #     context_limit=conf.context_limit
                # # )
                # self.interperson_GPTStereo = GPTStereo(
                #     dim=dim_intermodal,
                #     dff_k=3,
                #     num_layers=conf.cross_layers,
                #     num_heads=conf.num_heads,
                #     dropout=conf.dropout,
                #     context_limit=conf.context_limit,
                # )
            
            elif self.conf.mode == 1:

                self.face_encoder = EncoderFormerDFER()
                
                if conf.load_pretrained:
                    self.face_encoder.load_state_dict(torch.load(conf.pretrained_face_encoder, weights_only=True), strict=False)
                    if conf.freeze_encoder:
                        self.face_encoder.eval()
    
                self.compress_face = Linear(
                    dim_in=conf.dim_face_encoder,
                    dim_out=conf.dim,
                    dropout=conf.dropout
                )
    
                self.ar_channel_face_encoder = GPT(
                    dim=conf.dim,
                    dff_k=3,
                    num_layers=conf.channel_layers,
                    num_heads=conf.num_heads,
                    dropout=conf.dropout,
                    context_limit=conf.context_limit,
                )
                
                num_nonverbal = 0
                for nonverbal_name in ['ar_channel_gaze', 'ar_channel_head', 'ar_channel_body']:
                    if hasattr(self, nonverbal_name):
                        num_nonverbal += 1
                
                # fusion_dim = conf.dim * 2
                
                dim_intermodal = conf.dim
                dim_interperson = conf.dim
                
                self.intermodal_GPT = GPTStereo(
                    dim=dim_intermodal,
                    dff_k=3,
                    num_layers=conf.cross_layers,
                    num_heads=conf.num_heads,
                    dropout=conf.dropout,
                    context_limit=conf.context_limit,
                )
                
                ###
                ### Multi channel (inter-modal excluding audio)        
                ###
                # self.interperson_transformer = TransformerConcatLayer(
                #     dim=(conf.dim_nonverbal*num_nonverbal + conf.dim_face_encoder + conf.dim)*2,
                #     ffn_dim=conf.dim_nonverbal*num_nonverbal*3,
                #     num_heads=conf.num_heads,
                #     dropout=conf.dropout,
                #     context_limit=conf.context_limit
                # )
                self.interperson_GPTStereo = GPTStereo(
                    dim=dim_interperson,
                    dff_k=3,
                    num_layers=conf.cross_layers,
                    num_heads=conf.num_heads,
                    dropout=conf.dropout,
                    context_limit=conf.context_limit,
                )

            if self.conf.mode == 2:
            
                self.face_encoder = EncoderFormerDFER()
                
                if conf.load_pretrained:
                    self.face_encoder.load_state_dict(torch.load(conf.pretrained_face_encoder, weights_only=True), strict=False)
                    if conf.freeze_encoder:
                        self.face_encoder.eval()

                self.compress_face = Linear(
                    dim_in=conf.dim_face_encoder,
                    dim_out=conf.dim,
                    dropout=conf.dropout
                )
                
                self.ar_channel_face_encoder = GPT(
                    dim=conf.dim,
                    dff_k=3,
                    num_layers=conf.channel_layers,
                    num_heads=conf.num_heads,
                    dropout=conf.dropout,
                    context_limit=conf.context_limit,
                )
                    
                self.ar_channel_gaze = Linear(
                    dim_in=conf.dim_gaze,
                    dim_out=conf.dim_nonverbal,
                    dropout=conf.dropout
                )
        
                self.ar_channel_head = Linear(
                    dim_in=conf.dim_head,
                    dim_out=conf.dim_nonverbal,
                    dropout=conf.dropout
                )
        
                self.ar_channel_body = Linear(
                    dim_in=conf.dim_body,
                    dim_out=conf.dim_nonverbal,
                    dropout=conf.dropout
                )
                
                num_nonverbal = 0
                for nonverbal_name in ['ar_channel_gaze', 'ar_channel_head', 'ar_channel_body']:
                    if hasattr(self, nonverbal_name):
                        num_nonverbal += 1
                
                ###
                ### Multi channel (inter-modal excluding audio)
                ###
                
                # num_nonverbal * 3 (gaze, head, body) + dim (compressed face) + dim (audio)
                dim_intermodal = conf.dim_nonverbal*num_nonverbal + conf.dim + conf.dim
                dim_interperson = dim_intermodal
                
                self.intermodal_transformer = TransformerConcatLayer(
                    dim=dim_intermodal,
                    ffn_dim=conf.dim_nonverbal*num_nonverbal*3,
                    num_heads=conf.num_heads,
                    dropout=conf.dropout,
                    context_limit=conf.context_limit
                )
    
                self.interperson_GPTStereo = GPTStereo(
                    dim=dim_interperson,
                    dff_k=3,
                    num_layers=conf.cross_layers,
                    num_heads=conf.num_heads,
                    dropout=conf.dropout,
                    context_limit=conf.context_limit,
                )

            if self.conf.mode == 3:
            
                self.face_encoder = EncoderFormerDFER()
                
                if conf.load_pretrained:
                    self.face_encoder.load_state_dict(torch.load(conf.pretrained_face_encoder, weights_only=True), strict=False)
                    if conf.freeze_encoder:
                        self.face_encoder.eval()

                self.compress_face = Linear(
                    dim_in=conf.dim_face_encoder,
                    dim_out=conf.dim,
                    dropout=conf.dropout
                )
                
                self.ar_channel_face_encoder = GPT(
                    dim=conf.dim,
                    dff_k=3,
                    num_layers=conf.channel_layers,
                    num_heads=conf.num_heads,
                    dropout=conf.dropout,
                    context_limit=conf.context_limit,
                )

                self.ar_channel_face = Linear(
                    dim_in=conf.dim_face,
                    dim_out=conf.dim_nonverbal,
                    dropout=conf.dropout
                )
                    
                self.ar_channel_gaze = Linear(
                    dim_in=conf.dim_gaze,
                    dim_out=conf.dim_nonverbal,
                    dropout=conf.dropout
                )
        
                self.ar_channel_head = Linear(
                    dim_in=conf.dim_head,
                    dim_out=conf.dim_nonverbal,
                    dropout=conf.dropout
                )
        
                self.ar_channel_body = Linear(
                    dim_in=conf.dim_body,
                    dim_out=conf.dim_nonverbal,
                    dropout=conf.dropout
                )
                
                num_nonverbal = 0
                for nonverbal_name in ['ar_channel_face', 'ar_channel_gaze', 'ar_channel_head', 'ar_channel_body']:
                    if hasattr(self, nonverbal_name):
                        num_nonverbal += 1
                
                ###
                ### Multi channel (inter-modal excluding audio)
                ###
                
                # num_nonverbal * 3 (gaze, head, body) + dim (compressed face) + dim (audio)
                dim_intermodal = conf.dim_nonverbal*num_nonverbal + conf.dim + conf.dim
                dim_interperson = dim_intermodal
                
                self.intermodal_transformer = TransformerConcatLayer(
                    dim=dim_intermodal,
                    ffn_dim=conf.dim_nonverbal*num_nonverbal*3,
                    num_heads=conf.num_heads,
                    dropout=conf.dropout,
                    context_limit=conf.context_limit
                )
    
                self.interperson_GPTStereo = GPTStereo(
                    dim=dim_interperson,
                    dff_k=3,
                    num_layers=conf.cross_layers,
                    num_heads=conf.num_heads,
                    dropout=conf.dropout,
                    context_limit=conf.context_limit,
                )
            
        if self.conf.freeze_encoder == 1:
            print('freeze encoder')
            self.encoder.freeze()
            self.encoder.eval()
            if conf.use_face_encoder:
                self.face_encoder.freeze()
                self.face_encoder.eval()

        self.objective = ObjectiveVAP(bin_times=conf.bin_times, frame_hz=conf.frame_hz)

        # Outputs
        # Voice activity objective -> x1, x2 -> logits ->  BCE
        if self.conf.onishi:
            self.va_classifier = nn.Linear(conf.dim_nonverbal*num_nonverbal+conf.dim, 2)
            self.vap_head = nn.Linear(conf.dim_nonverbal*num_nonverbal+conf.dim, self.objective.n_classes)
        elif self.conf.use_face_encoder:            
            self.va_classifier = nn.Linear(dim_interperson, 2)
            self.vap_head = nn.Linear(dim_interperson, self.objective.n_classes)            
        else:
            self.va_classifier = nn.Linear(conf.dim, 1)
            self.vap_head = nn.Linear(conf.dim, self.objective.n_classes)
        
        if self.conf.lid_classify == 1:
            self.lid_classifier = nn.Linear(conf.dim, conf.lid_classify_num_class)
        
        elif self.conf.lid_classify == 2:
            self.lid_classifier_middle = nn.Linear(conf.dim*2, conf.lid_classify_num_class)
        
        if self.conf.lang_cond == 1:
            self.lang_condition = nn.Linear(conf.lid_classify_num_class, conf.dim)
        
        # self.stream1 = torch.cuda.Stream()
        # self.stream2 = torch.cuda.Stream()

    def load_encoder_CPC(self, cpc_model):
        
        # Audio Encoder
        #if self.conf.encoder_type == "cpc":
        self.encoder1 = EncoderCPC(
            load_pretrained=True if self.conf.load_pretrained == 1 else False,
            freeze=self.conf.freeze_encoder,
            cpc_model_pt=cpc_model
        )
        self.encoder1 = self.encoder1.eval()
        #print(self.encoder1)
        #self.encoder1 = self.encoder1.half()
        
        self.encoder2 = EncoderCPC(
            load_pretrained=True if self.conf.load_pretrained == 1 else False,
            freeze=self.conf.freeze_encoder,
            cpc_model_pt=cpc_model
        )

        self.encoder2 = self.encoder2.eval()
        #self.encoder2 = self.encoder2.half()
        
        if self.conf.freeze_encoder == 1:
            print('freeze encoder')
            self.encoder1.freeze()
            self.encoder2.freeze()
    
    def load_encoder_FormerDFER(self, model_path):
        
        assert model_path != None, "Error: FormerDFER path is None"
        
        self.face_encoder1 = EncoderFormerDFER()
        self.face_encoder1.load_state_dict(torch.load(model_path, weights_only=True), strict=False)
        self.face_encoder1.eval()

        self.face_encoder2 = EncoderFormerDFER()
        self.face_encoder2.load_state_dict(torch.load(model_path, weights_only=True), strict=False)
        self.face_encoder2.eval()

        if self.conf.freeze_encoder == 1:
            print('freeze encoder')
            self.face_encoder1.freeze()
            self.face_encoder2.freeze()

    @property
    def horizon_time(self):
        return self.objective.horizon_time

    def encode_audio(self, audio: torch.Tensor) -> Tuple[Tensor, Tensor]:
        assert (
            audio.shape[1] == 2
        ), f"audio VAP ENCODER: {audio.shape} != (B, 2, n_samples)"
        
        # print('encode_audio', audio.shape)
        
        # torch.cuda.synchronize()

        # with torch.cuda.stream(self.stream1):
        #     x1 = self.encoder(audio[:, :1], only_feature_extractor=self.conf.only_feature_extraction)  # speaker 1
        # with torch.cuda.stream(self.stream2):
        #     x2 = self.encoder(audio[:, 1:], only_feature_extractor=self.conf.only_feature_extraction)  # speaker 2
        
        # torch.cuda.synchronize()

        x1 = self.encoder(audio[:, :1], only_feature_extractor=self.conf.only_feature_extraction)  # speaker 1
        x2 = self.encoder(audio[:, 1:], only_feature_extractor=self.conf.only_feature_extraction)  # speaker 2
        
        # print(audio[:, :1, 0:320])
        # print(x1[0][0])
        # print(x1[0][0].shape)
        # input("check encoder")
        
        #input("check encoder")
        return x1, x2
    
    def encode_gaze(self, src: Tensor):
        
        return src


    def encode_head(self, src: Tensor):
        
        return src

    def encode_face(self, src1: Tensor, chunk_size = 16):
        
        # print('### Encode start')
        
        ###
        ### option1: non overlap approach
        ###

        # print(src1.shape, flush=True)

        # src1: (batch, seq_len, channel, height, width)
        
        pad_size = chunk_size * math.ceil(src1.size(1) / chunk_size) - src1.size(1)
        
        # print(src1.shape)
        # print(src1.size(0), pad_size, src1.size(2), src1.size(3), src1.size(4))

        padding = torch.zeros(src1.size(0), pad_size, src1.size(2), src1.size(3), src1.size(4)).to(dtype=src1.dtype).to(self.device)
        
        # print(padding.shape, flush=True)
        
        # concat_src: (batch, seq_len+, channel, height, width)
        concat_src = torch.concat([src1, padding], dim = 1)
        
        # print(concat_src.shape, flush=True)
        
        # concat_src: (batch+, 16, channel, height, width)
        concat_src = concat_src.view(-1, chunk_size, src1.size(2), src1.size(3), src1.size(4)).contiguous()

        # print(concat_src.shape, flush=True)
        
        # x1: (batch+, 16, dim_face_encoder)
        # x1 = self.face_encoder1(concat_src)
        
        prev_type = concat_src.type()
        # concat_src = concat_src.float()
        
        # print(concat_src.type())
        
        x1 = self.face_encoder(concat_src)
        # print('face encoder:', x1.shape, flush=True)
        
        x1 = x1.type(prev_type)
        
        # x1: (batch, seq_len+, dim_face_encoder)
        x1 = x1.view(src1.size(0), -1, self.conf.dim_face_encoder)
        # print('after view:', x1.shape, flush=True)

        # x1: (batch, seq_len+, dim_face_encoder)
        x1 = x1[:, :src1.size(1), :].contiguous()
        # print('final:', x1.shape, flush=True)

        # final output: (batch, seq_len, dim_face_encoder)

        # print('### Encode end')


        ###
        ### option2: sliding window approach (require stride parameter)
        ###

        # preds_chunks1 = []
        # preds_chunks2 = []

        # for i in range(0, len(src1) - chunk_size + 1, stride):

        #     chunk1 = src1[i : i + chunk_size]
        #     chunk2 = src2[i : i + chunk_size]

        #     preds1 = self.face_encoder1(chunk1)
        #     preds2 = self.face_encoder2(chunk2)

        #     preds_chunks1.append(preds1)
        #     preds_chunks2.append(preds2)

        #     if len(src1) < (i + stride):
                
        #         chunk = src1[i + chunk_size:]
        #         # Pad the last chunk if it's smaller than chunk_size
        #         padding = torch.zeros(chunk_size - chunk.size(0), chunk.size(1), chunk.size(2), chunk.size(3))
        #         chunk = torch.cat([chunk, padding], dim=0)
                
        #         preds1 = self.face_encoder1(chunk1)
        #         preds_chunks1.append(preds1)

        #     if len(src2) < (i + stride):
                
        #         chunk = src2[i + chunk_size:]
        #         # Pad the last chunk if it's smaller than chunk_size
        #         padding = torch.zeros(chunk_size - chunk.size(0), chunk.size(1), chunk.size(2), chunk.size(3))
        #         chunk = torch.cat([chunk, padding], dim=0)
                
        #         preds2 = self.face_encoder2(chunk2)
        #         preds_chunks2.append(preds2)

        # x1 = torch.stack(preds_chunks1)  # (Batch_Size, Chunk_Length, Feature_Dim)
        # x2 = torch.stack(preds_chunks2)  # (Batch_Size, Chunk_Length, Feature_Dim)
        
        return x1

    def encode_body(self, src: Tensor):
        
        return src
    

    def vad_loss(self, vad_output, vad):
        return F.binary_cross_entropy_with_logits(vad_output, vad)

    # def freeze(self):
    #     for p in self.encoder.parameters():
    #         p.requires_grad_(False)
    #     print(f"Froze {self.__class__.__name__}!")

    @torch.no_grad()
    def probs(
        self,
        waveform: Tensor,
        vad: Optional[Tensor] = None,
        now_lims: List[int] = [0, 1],
        future_lims: List[int] = [2, 3],
    ) -> Dict[str, Tensor]:
        
        out = self(waveform)
        probs = out["logits"].softmax(dim=-1)
        vad = out["vad"].sigmoid()
        
        if self.conf.lid_classify >= 1:
            lid = out["lid"].softmax(dim=-1)

        # Calculate entropy over each projection-window prediction (i.e. over
        # frames/time) If we have C=256 possible states the maximum bit entropy
        # is 8 (2^8 = 256) this means that the model have a one in 256 chance
        # to randomly be right. The model can't do better than to uniformly
        # guess each state, it has learned (less than) nothing. We want the
        # model to have low entropy over the course of a dialog, "thinks it
        # understands how the dialog is going", it's a measure of how close the
        # information in the unseen data is to the knowledge encoded in the
        # training data.
        h = -probs * probs.log2()  # Entropy
        H = h.sum(dim=-1)  # average entropy per frame

        # first two bins
        p_now = self.objective.probs_next_speaker_aggregate(
            probs, from_bin=now_lims[0], to_bin=now_lims[-1]
        )
        p_future = self.objective.probs_next_speaker_aggregate(
            probs, from_bin=future_lims[0], to_bin=future_lims[1]
        )

        ret = {
            "probs": probs,
            "vad": vad,
            "p_now": p_now,
            "p_future": p_future,
            "H": H,
        }

        if self.conf.lid_classify >= 1:
            ret.add({"lid": lid})

        if vad is not None:
            labels = self.objective.get_labels(vad)
            ret["loss"] = self.objective.loss_vap(
                out["logits"], labels, reduction="none"
            )
        return ret

    @torch.no_grad()
    def vad(
        self,
        waveform: Tensor,
        max_fill_silence_time: float = 0.02,
        max_omit_spike_time: float = 0.02,
        vad_cutoff: float = 0.5,
    ) -> Tensor:
        """
        Extract (binary) Voice Activity Detection from model
        """
        vad = (self(waveform)["vad"].sigmoid() >= vad_cutoff).float()
        for b in range(vad.shape[0]):
            # TODO: which order is better?
            vad[b] = vad_fill_silences(
                vad[b], max_fill_time=max_fill_silence_time, frame_hz=self.frame_hz
            )
            vad[b] = vad_omit_spikes(
                vad[b], max_omit_time=max_omit_spike_time, frame_hz=self.frame_hz
            )
        return vad
    
    def forward(
        self,
        # waveform: Tensor,
        src: Union[Tensor, Dict[str, Union[Tensor, None]], None] = None,
        waveform: Union[Tensor, None] = None,
        attention: bool = False,
        lang_info: list = None,
    ) -> Dict[str, Tensor]:
        
        assert (waveform != None) or (src != None), "Either waveform or src should be not None"

        if waveform != None:
            waveform = waveform
            gaze1 = None
            head1 = None
            face1 = None
            body1 = None
            face_im1 = None
            gaze2 = None
            head2 = None
            face2 = None
            body2 = None
            face_im2 = None
        else:
            waveform = src['waveform']
            gaze1 = src['gaze1']
            head1 = src['head1']
            face1 = src['face1']
            body1 = src['body1']
            gaze2 = src['gaze2']
            head2 = src['head2']
            face2 = src['face2']
            body2 = src['body2']
            
            if self.conf.use_face_encoder:
                face_im1 = src['face_im1']
                face_im2 = src['face_im2']
            else:
                face_im1 = None
                face_im2 = None

        # print(waveform.shape)
        
        # if self.multimodal:
        #     print(gaze1.shape)
        #     print(head1.shape)
        #     print(face1.shape)
        #     print(body1.shape)

        # Measure time
        # import time
        # start = time.time()
        
        s_time = torch.cuda.Event(enable_timing=True)
        s_time.record()
        
        # if not (self.conf.use_face_encoder and self.conf.mode == 1):
        #     x1, x2 = self.encode_audio(waveform)

        x1, x2 = self.encode_audio(waveform)
        
        # e_time = torch.cuda.Event(enable_timing=True)
        # e_time.record()
        # torch.cuda.synchronize()
        # print("encode_audio {:.2f} ms".format(s_time.elapsed_time(e_time)))
        # s_time = torch.cuda.Event(enable_timing=True)
        # s_time.record()

        # print(x1.shape)
        # print(x2.shape)
        
        # Autoregressive
        # if not (self.conf.use_face_encoder and self.conf.mode == 1):
        #     o1 = self.ar_channel(x1, attention=attention)  # ["x"]
        #     o2 = self.ar_channel(x2, attention=attention)  # ["x"]

        o1 = self.ar_channel(x1, attention=attention)  # ["x"]
        o2 = self.ar_channel(x2, attention=attention)  # ["x"]

        # e_time = torch.cuda.Event(enable_timing=True)
        # e_time.record()
        # torch.cuda.synchronize()
        # print("audio ar {:.2f} ms".format(s_time.elapsed_time(e_time)))
        # s_time = torch.cuda.Event(enable_timing=True)
        # s_time.record()
        
        if not self.conf.use_face_encoder:
            out = self.ar(o1["x"], o2["x"], attention=attention)
        
        # print('audio')
        # print(o1["x"].shape)
        # print(out["x"].shape)

        # TODO!:
        
        if self.conf.onishi:
            
            if self.conf.face_only:

                x1_face = face1
                x2_face = face2
                
                o1_face = self.ar_channel_face(x1_face)
                o2_face = self.ar_channel_face(x2_face)
                
                out_face = self.ar_face(o1_face["x"], o2_face["x"])
                
                out_nonverbal, _, _ = self.nonverbal_transformer([out_face["x"]])
                # print('out_nonverbal', out_nonverbal.shape)

            else:

                x1_gaze = gaze1
                x1_head = head1
                x1_face = face1
                x1_body = body1
    
                x2_gaze = gaze2
                x2_head = head2
                x2_face = face2
                x2_body = body2
                
                o1_gaze = self.ar_channel_gaze(x1_gaze)
                o1_head = self.ar_channel_head(x1_head)
                o1_face = self.ar_channel_face(x1_face)
                o1_body = self.ar_channel_body(x1_body)
    
                o2_gaze = self.ar_channel_gaze(x2_gaze)
                o2_head = self.ar_channel_head(x2_head)
                o2_face = self.ar_channel_face(x2_face)
                o2_body = self.ar_channel_body(x2_body)
                
                out_gaze = self.ar_gaze(o1_gaze["x"], o2_gaze["x"])
                out_head = self.ar_head(o1_head["x"], o2_head["x"])
                out_face = self.ar_face(o1_face["x"], o2_face["x"])
                out_body = self.ar_body(o1_body["x"], o2_body["x"])
                
                out_nonverbal, _, _ = self.nonverbal_transformer([out_gaze["x"], out_head["x"], out_face["x"], out_body["x"]])
                # print('out_nonverbal', out_nonverbal.shape)
            
            out_audio_nonverbal, _, _ = self.audio_nonverbal_transformer([out["x"], out_nonverbal])
            
            vad = self.va_classifier(out_audio_nonverbal)
            v1 = vad[:, 0]
            v2 = vad[:, 1]
            logits = self.vap_head(out_audio_nonverbal)
            
            ret = {"logits": logits, "vad": vad}
        
        elif self.conf.use_face_encoder:
            
            if self.conf.mode == 0:
            
                torch.cuda.synchronize()

                with torch.cuda.stream(self.stream1):

                    o1_gaze = self.ar_channel_gaze(gaze1)
                    o1_head = self.ar_channel_head(head1)
                    o1_body = self.ar_channel_body(body1)
    
                    x1_face = self.encode_face(face_im1)
                    # x1_face = self.compress_face(x1_face)
                    
                    # print(type(x1_face))
                    # print(x1.keys())
                    
                    o1_face = self.ar_channel_face_encoder(x1_face, attention=attention)
    
                with torch.cuda.stream(self.stream2):

                    o2_gaze = self.ar_channel_gaze(gaze2)
                    o2_head = self.ar_channel_head(head2)
                    o2_body = self.ar_channel_body(body2)
        
                    x2_face = self.encode_face(face_im2)
                    # x2_face = self.compress_face(x2_face)
                    o2_face = self.ar_channel_face_encoder(x2_face, attention=attention)
                
                torch.cuda.synchronize()
                
                out1_intermodal, _, _ = self.intermodal_transformer([o1["x"], o1_gaze["x"], o1_head["x"], o1_face["x"], o1_body["x"]])
                out2_intermodal, _, _ = self.intermodal_transformer([o2["x"], o2_gaze["x"], o2_head["x"], o2_face["x"], o2_body["x"]])
                out_interperson, _, _ = self.interperson_transformer([out1_intermodal, out2_intermodal])
                
                # concat_tensor = torch.concat([o1["x"], o1_gaze["x"], o1_head["x"], o1_face["x"], o1_body["x"]], dim=2)
                # out1_intermodal, _, _ = self.intermodal_transformer(concat_tensor)
    
                # concat_tensor = torch.concat([o2["x"], o2_gaze["x"], o2_head["x"], o2_face["x"], o2_body["x"]], dim=2)
                # out2_intermodal, _, _ = self.intermodal_transformer(concat_tensor)
                
                # out_interperson, _, _ = self.interperson_transformer(out1_intermodal, out2_intermodal)
    
                vad = self.va_classifier(out_interperson)
                v1 = vad[:, 0]
                v2 = vad[:, 1]
                logits = self.vap_head(out_interperson)
                
                ret = {"logits": logits, "vad": vad}
            
            elif self.conf.mode == 1:
                
                """
                encode_audio 128.57 ms
                audio ar 74.88 ms
                intra/inter modality 2271.07 ms
                classification 10.58 ms
                ALL: 2.88, PREP: 0.25 (0.09%), CALC1: 2.54 (0.88%), CALC2: 0.08 (0.03%), POST: 0.01 (0.00%)
                """

                # torch.cuda.synchronize()

                # with torch.cuda.stream(self.stream1):
                    
                #     x1_face = self.encode_face(face_im1)
                #     x1_face = self.compress_face(x1_face)
                #     o1_face = self.ar_channel_face_encoder(x1_face["x"], attention=attention)
                #     out1_intermodal = self.intermodal_GPT(o1["x"], o1_face["x"], attention=attention)    

                # with torch.cuda.stream(self.stream2):
    
                #     x2_face = self.encode_face(face_im2)
                #     x2_face = self.compress_face(x2_face)
                #     o2_face = self.ar_channel_face_encoder(x2_face["x"], attention=attention)
                #     out2_intermodal = self.intermodal_GPT(o2["x"], o2_face["x"], attention=attention)                

                # torch.cuda.synchronize()

                # x1 = self.encoder(waveform[:, :1], only_feature_extractor=self.conf.only_feature_extraction)  # speaker 1
                # o1 = self.ar_channel(x1, attention=attention)  # ["x"]
                x1_face = self.encode_face(face_im1)
                x1_face = self.compress_face(x1_face)
                o1_face = self.ar_channel_face_encoder(x1_face["x"], attention=attention)
                out1_intermodal = self.intermodal_GPT(o1["x"], o1_face["x"], attention=attention) 
            

                # x2 = self.encoder(waveform[:, 1:], only_feature_extractor=self.conf.only_feature_extraction)  # speaker 2
                # o2 = self.ar_channel(x2, attention=attention)  # ["x"]
                x2_face = self.encode_face(face_im2)
                x2_face = self.compress_face(x2_face)
                o2_face = self.ar_channel_face_encoder(x2_face["x"], attention=attention)
                out2_intermodal = self.intermodal_GPT(o2["x"], o2_face["x"], attention=attention)
                
                out_interperson = self.interperson_GPTStereo(out1_intermodal["x"], out2_intermodal["x"], attention=attention)

                # e_time = torch.cuda.Event(enable_timing=True)
                # e_time.record()
                # torch.cuda.synchronize()
                # print("face processes {:.2f} ms".format(s_time.elapsed_time(e_time)))
                # s_time = torch.cuda.Event(enable_timing=True)
                # s_time.record()

                # e_time = torch.cuda.Event(enable_timing=True)
                # e_time.record()
                # torch.cuda.synchronize()
                # print("intra/inter modality {:.2f} ms".format(s_time.elapsed_time(e_time)))
                # s_time = torch.cuda.Event(enable_timing=True)
                # s_time.record()

    
                vad = self.va_classifier(out_interperson["x"])
                v1 = vad[:, 0]
                v2 = vad[:, 1]
                logits = self.vap_head(out_interperson["x"])

                # e_time = torch.cuda.Event(enable_timing=True)
                # e_time.record()
                # torch.cuda.synchronize()
                # print("classification {:.2f} ms".format(s_time.elapsed_time(e_time)))
                # s_time = torch.cuda.Event(enable_timing=True)
                # s_time.record()
                
                # input()

                
                ret = {"logits": logits, "vad": vad}
            
            elif self.conf.mode == 2:

                x1_face = self.encode_face(face_im1)                
                x1_face = self.compress_face(x1_face)
                o1_face = self.ar_channel_face_encoder(x1_face["x"], attention=attention)

                o1_gaze = self.ar_channel_gaze(gaze1)
                o1_head = self.ar_channel_head(head1)
                o1_body = self.ar_channel_body(body1)    

                x2_face = self.encode_face(face_im2)
                x2_face = self.compress_face(x2_face)
                o2_face = self.ar_channel_face_encoder(x2_face["x"], attention=attention)

                o2_gaze = self.ar_channel_gaze(gaze2)
                o2_head = self.ar_channel_head(head2)
                o2_body = self.ar_channel_body(body2)

                out1_intermodal, _, _ = self.intermodal_transformer([o1["x"], o1_gaze["x"], o1_head["x"], o1_face["x"], o1_body["x"]])
                out2_intermodal, _, _ = self.intermodal_transformer([o2["x"], o2_gaze["x"], o2_head["x"], o2_face["x"], o2_body["x"]])
                out_interperson = self.interperson_GPTStereo(out1_intermodal, out2_intermodal, attention=attention)

                vad = self.va_classifier(out_interperson["x"])
                v1 = vad[:, 0]
                v2 = vad[:, 1]
                logits = self.vap_head(out_interperson["x"])
                
                ret = {"logits": logits, "vad": vad}

                # self.face_encoder
                # self.compress_face
                # self.ar_channel_face_encoder
                # self.ar_channel_gaze
                # self.ar_channel_head
                # self.ar_channel_body
                # self.intermodal_transformer
                # self.interperson_GPTStereo

            elif self.conf.mode == 3:

                x1_face_im = self.encode_face(face_im1)                
                x1_face_im = self.compress_face(x1_face_im)
                o1_face_im = self.ar_channel_face_encoder(x1_face_im["x"], attention=attention)

                o1_face = self.ar_channel_face(face1)
                o1_gaze = self.ar_channel_gaze(gaze1)
                o1_head = self.ar_channel_head(head1)
                o1_body = self.ar_channel_body(body1)    

                x2_face_im = self.encode_face(face_im2)
                x2_face_im = self.compress_face(x2_face_im)
                o2_face_im = self.ar_channel_face_encoder(x2_face_im["x"], attention=attention)

                o2_face = self.ar_channel_face(face2)
                o2_gaze = self.ar_channel_gaze(gaze2)
                o2_head = self.ar_channel_head(head2)
                o2_body = self.ar_channel_body(body2)

                out1_intermodal, _, _ = self.intermodal_transformer([o1["x"], o1_face_im["x"], o1_gaze["x"], o1_head["x"], o1_face["x"], o1_body["x"]])
                out2_intermodal, _, _ = self.intermodal_transformer([o2["x"], o2_face_im["x"], o2_gaze["x"], o2_head["x"], o2_face["x"], o2_body["x"]])
                out_interperson = self.interperson_GPTStereo(out1_intermodal, out2_intermodal, attention=attention)

                vad = self.va_classifier(out_interperson["x"])
                v1 = vad[:, 0]
                v2 = vad[:, 1]
                logits = self.vap_head(out_interperson["x"])
                
                ret = {"logits": logits, "vad": vad}

        
        else:
            
            v1 = self.va_classifier(out["x1"])
            v2 = self.va_classifier(out["x2"])
            vad = torch.cat((v1, v2), dim=-1)
            logits = self.vap_head(out["x"])

            # print(logits.shape)
    
            ret = {"logits": logits, "vad": vad}
    
            # Language identification at the middle layer
            if self.conf.lid_classify == 2:
                lid_middle = self.lid_classifier_middle(torch.cat((o1["x"], o2["x"]), dim=-1))
                ret["lid"] = lid_middle
            
            # Language identification at the last layer
            if self.conf.lid_classify == 1:
                lid = self.lid_classifier(out["x"])
                ret["lid"] = lid
    
            if attention:
                ret["self_attn"] = torch.stack([o1["attn"], o2["attn"]], dim=1)
                ret["cross_attn"] = out["cross_attn"]
                ret["cross_self_attn"] = out["self_attn"]
        
        # del waveform
        # del gaze1, head1, face1, body1, face_im1
        # del gaze2, head2, face2, body2, face_im2
        # torch.cuda.empty_cache()
        # gc.collect()
        
        return ret


def debug():
    from torch.utils.data import DataLoader
    from vap_dataset.dataset import VapDataset

    conf = VapConfig()

    model = VapGPT(conf)

    dset = VapDataset(path="data/sliding_val.csv", mono=True)
    dloader = DataLoader(dset, batch_size=4, num_workers=1, shuffle=False)
    batch = next(iter(dloader))
    out = model(batch["waveform"], batch["vad"][:, :-100])


def debug_mono():
    from torch.utils.data import DataLoader
    from vap_dataset.dataset import VapDataset

    conf = VapMonoConfig(mono=True, va_history=True)
    model = VapGPTMono(conf)
    dset = VapDataset(path="data/sliding_val.csv", mono=True)
    dloader = DataLoader(dset, batch_size=4, num_workers=1, shuffle=False)
    batch = next(iter(dloader))
    out = model(batch["waveform"], batch["vad"][:, :-100])

# class VapGPT(nn.Module):
#     def __init__(self, conf: Optional[VapConfig] = None):
#         super().__init__()
#         if conf is None:
#             conf = VapConfig()
#         self.conf = conf
#         self.sample_rate = conf.sample_rate
#         self.frame_hz = conf.frame_hz

#         self.temp_elapse_time = []

#         # Audio Encoder
#         if self.conf.encoder_type == "cpc":
#             self.encoder = EncoderCPC(
#                 cpc_model_pt=self.conf.cpc_model_pt,
#                 load_pretrained=True if conf.load_pretrained == 1 else False,
#                 freeze=conf.freeze_encoder,
#                 lim_context_sec=conf.context_limit_cpc_sec,
#                 frame_hz = self.conf.frame_hz
#             )
        
#         elif self.conf.encoder_type == "wav2vec2":
#             from vap.customwav2vec2 import W2V2Transformers
#             self.encoder = W2V2Transformers(model_type=self.conf.wav2vec_type)
        
#         elif self.conf.encoder_type == "hubert":
#             from vap.customhubert import HubertEncoder
#             self.encoder = HubertEncoder(model_type= self.conf.hubert_model)

#         # Single channel
#         self.ar_channel = GPT(
#             dim=conf.dim,
#             dff_k=3,
#             num_layers=conf.channel_layers,
#             num_heads=conf.num_heads,
#             dropout=conf.dropout,
#             context_limit=conf.context_limit,
#         )

#         # Cross channel
#         self.ar = GPTStereo(
#             dim=conf.dim,
#             dff_k=3,
#             num_layers=conf.cross_layers,
#             num_heads=conf.num_heads,
#             dropout=conf.dropout,
#             context_limit=conf.context_limit,
#         )

#         self.objective = ObjectiveVAP(bin_times=conf.bin_times, frame_hz=conf.frame_hz)

#         # Outputs
#         # Voice activity objective -> x1, x2 -> logits ->  BCE
#         self.va_classifier = nn.Linear(conf.dim, 1)
        
#         if self.conf.lid_classify == 1:
#             self.lid_classifier = nn.Linear(conf.dim, conf.lid_classify_num_class)
        
#         elif self.conf.lid_classify == 2:
#             self.lid_classifier_middle = nn.Linear(conf.dim*2, conf.lid_classify_num_class)
        
#         if self.conf.lang_cond == 1:
#             self.lang_condition = nn.Linear(conf.lid_classify_num_class, conf.dim)
        
#         self.vap_head = nn.Linear(conf.dim, self.objective.n_classes)
        
#         if self.conf.encoder_type == "wav2vec2":
                
#             if self.conf.only_feature_extraction == 1:
#                 self.decrease_dimension = nn.Linear(512, 256)
#             else:
#                 self.decrease_dimension = nn.Linear(1024, 256)
        
#         elif self.conf.encoder_type == "hubert":
            
#             if self.conf.hubert_model == "hubert_ja":

#                 if self.conf.only_feature_extraction == 1:
#                     self.decrease_dimension = nn.Linear(512, 256)
#                 else:
#                     self.decrease_dimension = nn.Linear(768, 256)
            
#             elif self.conf.hubert_model == "hubert_en_large":
                
#                 if self.conf.only_feature_extraction == 1:
#                     self.decrease_dimension = nn.Linear(512, 256)
#                 else:
#                     self.decrease_dimension = nn.Linear(1024, 256)


#         if self.conf.freeze_encoder == 1:
#             print('freeze encoder')
#             self.encoder.freeze()

#     def load_encoder(self, cpc_model):
        
#         # Audio Encoder
#         #if self.conf.encoder_type == "cpc":
#         self.encoder1 = EncoderCPC(
#             load_pretrained=True if self.conf.load_pretrained == 1 else False,
#             freeze=self.conf.freeze_encoder,
#             cpc_model_pt=cpc_model
#         )
#         self.encoder1 = self.encoder1.eval()
#         #print(self.encoder1)
#         #self.encoder1 = self.encoder1.half()
        
#         self.encoder2 = EncoderCPC(
#             load_pretrained=True if self.conf.load_pretrained == 1 else False,
#             freeze=self.conf.freeze_encoder,
#             cpc_model_pt=cpc_model
#         )

#         self.encoder2 = self.encoder2.eval()
#         #self.encoder2 = self.encoder2.half()
        
#         if self.conf.freeze_encoder == 1:
#             print('freeze encoder')
#             self.encoder1.freeze()
#             self.encoder2.freeze()        

#     @property
#     def horizon_time(self):
#         return self.objective.horizon_time

#     def encode_audio(self, audio: torch.Tensor) -> Tuple[Tensor, Tensor]:
#         assert (
#             audio.shape[1] == 2
#         ), f"audio VAP ENCODER: {audio.shape} != (B, 2, n_samples)"
        
#         # print('encode_audio', audio.shape)
        
#         x1 = self.encoder(audio[:, :1], only_feature_extractor=self.conf.only_feature_extraction)  # speaker 1
#         x2 = self.encoder(audio[:, 1:], only_feature_extractor=self.conf.only_feature_extraction)  # speaker 2
        
#         # print(audio[:, :1, 0:320])
#         # print(x1[0][0])
#         # print(x1[0][0].shape)
#         # input("check encoder")
        
#         #input("check encoder")
#         return x1, x2

#     def vad_loss(self, vad_output, vad):
#         return F.binary_cross_entropy_with_logits(vad_output, vad)

#     # def freeze(self):
#     #     for p in self.encoder.parameters():
#     #         p.requires_grad_(False)
#     #     print(f"Froze {self.__class__.__name__}!")

#     @torch.no_grad()
#     def probs(
#         self,
#         waveform: Tensor,
#         vad: Optional[Tensor] = None,
#         now_lims: List[int] = [0, 1],
#         future_lims: List[int] = [2, 3],
#     ) -> Dict[str, Tensor]:
        
#         out = self(waveform)
#         probs = out["logits"].softmax(dim=-1)
#         vad = out["vad"].sigmoid()
        
#         if self.conf.lid_classify >= 1:
#             lid = out["lid"].softmax(dim=-1)

#         # Calculate entropy over each projection-window prediction (i.e. over
#         # frames/time) If we have C=256 possible states the maximum bit entropy
#         # is 8 (2^8 = 256) this means that the model have a one in 256 chance
#         # to randomly be right. The model can't do better than to uniformly
#         # guess each state, it has learned (less than) nothing. We want the
#         # model to have low entropy over the course of a dialog, "thinks it
#         # understands how the dialog is going", it's a measure of how close the
#         # information in the unseen data is to the knowledge encoded in the
#         # training data.
#         h = -probs * probs.log2()  # Entropy
#         H = h.sum(dim=-1)  # average entropy per frame

#         # first two bins
#         p_now = self.objective.probs_next_speaker_aggregate(
#             probs, from_bin=now_lims[0], to_bin=now_lims[-1]
#         )
#         p_future = self.objective.probs_next_speaker_aggregate(
#             probs, from_bin=future_lims[0], to_bin=future_lims[1]
#         )

#         ret = {
#             "probs": probs,
#             "vad": vad,
#             "p_now": p_now,
#             "p_future": p_future,
#             "H": H,
#         }

#         if self.conf.lid_classify >= 1:
#             ret.add({"lid": lid})

#         if vad is not None:
#             labels = self.objective.get_labels(vad)
#             ret["loss"] = self.objective.loss_vap(
#                 out["logits"], labels, reduction="none"
#             )
#         return ret

#     @torch.no_grad()
#     def vad(
#         self,
#         waveform: Tensor,
#         max_fill_silence_time: float = 0.02,
#         max_omit_spike_time: float = 0.02,
#         vad_cutoff: float = 0.5,
#     ) -> Tensor:
#         """
#         Extract (binary) Voice Activity Detection from model
#         """
#         vad = (self(waveform)["vad"].sigmoid() >= vad_cutoff).float()
#         for b in range(vad.shape[0]):
#             # TODO: which order is better?
#             vad[b] = vad_fill_silences(
#                 vad[b], max_fill_time=max_fill_silence_time, frame_hz=self.frame_hz
#             )
#             vad[b] = vad_omit_spikes(
#                 vad[b], max_omit_time=max_omit_spike_time, frame_hz=self.frame_hz
#             )
#         return vad
    
#     def forward(
#         self,
#         waveform: Tensor,
#         attention: bool = False,
#         lang_info: list = None,
#     ) -> Dict[str, Tensor]:

#         # Measure time
#         import time
#         start = time.time()
        
#         x1, x2 = self.encode_audio(waveform)

#         # print(x1.shape)
#         # print(x2.shape)


#         ## match dimensions
#         if self.conf.encoder_type == 'wav2vec2':
#             x1 = self.decrease_dimension(x1)
#             x1 = torch.relu(x1)
#             x2 = self.decrease_dimension(x2)
#             x2 = torch.relu(x2)

#         elif self.conf.encoder_type == 'hubert':
            
#             x1 = self.decrease_dimension(x1)
#             x1 = torch.relu(x1)
#             x2 = self.decrease_dimension(x2)
#             x2 = torch.relu(x2)
        
#         # 処理時間計測用
#         TEST_INPUT_LENGTH = False
#         if TEST_INPUT_LENGTH:
#             # 入力を短くする

#             # Measure time
#             import time
#             start = time.time()
        
#             x1, x2 = self.encode_audio(waveform)

#             time1 = time.time()
#             time1_elapsed = time1 - start
#             time1_elapsed = time1_elapsed / 1000.
#             #print ("time1:{0}".format(time1_elapsed) + "[sec]")

#             #print(x1.shape)
#             x1_ = x1[:, :, :]
#             x2_ = x2[:, :, :]
#             #print(x1_.shape)

#             # Autoregressive
#             o1 = self.ar_channel(x1_, attention=attention)  # ["x"]
#             o2 = self.ar_channel(x2_, attention=attention)  # ["x"]
#             out = self.ar(o1["x"], o2["x"], attention=attention)

#             time2 = time.time()
#             time2_elapsed = time2 - time1
#             #print ("time2:{0}".format(time2_elapsed) + "[sec]")

#             # Outputs
#             v1 = self.va_classifier(out["x1"])
#             v2 = self.va_classifier(out["x2"])
#             vad = torch.cat((v1, v2), dim=-1)
#             logits = self.vap_head(out["x"])

#             # Measure time (end)
#             time3 = time.time()
#             time3_elapsed = time3 - time2
#             #print ("time3:{0}".format(time3_elapsed) + "[sec]")

#             time_total = time1_elapsed + time2_elapsed + time3_elapsed
#             print ("time_total:{0}".format(time_total*1000) + "[msec]")
#             self.temp_elapse_time.append(time_total*1000)
#             if len(self.temp_elapse_time) == 30:
#                 # 最初の5件は削除
#                 self.temp_elapse_time = self.temp_elapse_time[5:]
#                 import numpy as np
#                 print ("average time_total:{0}".format(np.mean(self.temp_elapse_time)) + "[msec]")
#                 a = input("Press y to continue:")
#                 while a != "y":
#                     a = input("Press y to continue:")

#         # Language condition
#         if self.conf.lang_cond == 1:
#             lang_info_data = torch.zeros(x1.shape[0], x1.shape[1], self.conf.lid_classify_num_class).to(x1.device)
#             for b, lang in enumerate(lang_info):
#                 lang_info_data[b, :, lang] = 1
#             x1 += self.lang_condition(lang_info_data)
#             x2 += self.lang_condition(lang_info_data)
        
#         # Autoregressive
#         o1 = self.ar_channel(x1, attention=attention)  # ["x"]
#         o2 = self.ar_channel(x2, attention=attention)  # ["x"]
#         out = self.ar(o1["x"], o2["x"], attention=attention)
        
#         # print(o1.shape)
#         # print(o2.shape)
#         # print(out.shape)

#         # Outputs
#         v1 = self.va_classifier(out["x1"])
#         v2 = self.va_classifier(out["x2"])
#         vad = torch.cat((v1, v2), dim=-1)
#         logits = self.vap_head(out["x"])

#         # print(logits.shape)

#         ret = {"logits": logits, "vad": vad}

#         # Language identification at the middle layer
#         if self.conf.lid_classify == 2:
#             lid_middle = self.lid_classifier_middle(torch.cat((o1["x"], o2["x"]), dim=-1))
#             ret["lid"] = lid_middle
        
#         # Language identification at the last layer
#         if self.conf.lid_classify == 1:
#             lid = self.lid_classifier(out["x"])
#             ret["lid"] = lid

#         if attention:
#             ret["self_attn"] = torch.stack([o1["attn"], o2["attn"]], dim=1)
#             ret["cross_attn"] = out["cross_attn"]
#             ret["cross_self_attn"] = out["self_attn"]
        
#         return ret


if __name__ == "__main__":

    from vap.audio import load_waveform
    from vap.plot_utils import plot_mel_spectrogram, plot_vad
    from vap.utils import (
        vad_list_to_onehot,
        vad_onehot_to_vad_list,
        get_vad_list_subset,
    )
    import matplotlib.pyplot as plt
    from vap_dataset.corpus import SwbReader

    def plot_compare_vad(w, vad, vad2, frame_hz=50, figsize=(12, 8), plot=True):
        fig, ax = plt.subplots(4, 1, figsize=figsize, sharex=True)

        plot_mel_spectrogram(w, ax=[ax[0], ax[3]])
        plot_mel_spectrogram(w, ax=[ax[1], ax[2]])

        x = torch.arange(vad.shape[0]) / frame_hz
        plot_vad(x, vad[:, 0] * 0.95, ax=ax[0])
        plot_vad(x, vad[:, 1] * 0.95, ax=ax[3])
        plot_vad(x, vad2[:, 0] * 0.95, ax=ax[1], color="r")
        plot_vad(x, vad2[:, 1] * 0.95, ax=ax[2], color="r")

        if plot:
            plt.pause(0.1)
        return fig, ax

    conf = VapConfig()
    model = VapGPT(conf)
    std = load_older_state_dict(
        "example/VAP_3mmz3t0u_50Hz_ad20s_134-epoch9-val_2.56.ckpt"
    )
    model.load_state_dict(std, strict=False)

    sd = model.state_dict()

    model.eval()
    if torch.cuda.is_available():
        model = model.to("cuda")

    reader = SwbReader()
    d = reader[0]
    vad_list = d["vad_list"]

    waveform, sr = load_waveform(d["audio_path"])
    duration = waveform.shape[-1] / sr
    clip_duration = 40

    for start in range(0, int(duration), clip_duration - 5):
        end = start + clip_duration
        if end > duration:
            end = duration
            start = end - clip_duration

        vl = get_vad_list_subset(vad_list, start, end)
        vad_orig = vad_list_to_onehot(vl, duration=end - start, frame_hz=50)

        # waveform
        s = round(sr * start)
        e = round(sr * end)
        w_tmp = waveform[:, s:e].unsqueeze(0)

        # Model VAD
        vad2 = model.vad(
            w_tmp.to("cuda"),
            max_fill_silence_time=0.1,
            max_omit_spike_time=0.04,
            vad_cutoff=0.5,
        )
        vad2_list = vad_onehot_to_vad_list(vad2, frame_hz=model.frame_hz)
        plt.close("all")
        plot_compare_vad(w_tmp[0], vad_orig, vad2[0], plot=False)
        plt.show()

