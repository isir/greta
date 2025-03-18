import math
import torch
import torch.nn as nn
import torch.nn.functional as F
from einops.layers.torch import Rearrange
from typing import Dict, Optional, Tuple, List

import numpy as np


def ffn_block(
    din: int,
    dff: int,
    activation: str = "GELU",
    dropout: float = 0.0,
    bias: bool = False,
) -> nn.Sequential:
    return nn.Sequential(
        nn.Linear(din, dff, bias=bias),
        getattr(nn, activation)(),
        nn.Dropout(p=dropout),
        nn.Linear(dff, din, bias=bias),
    )


class MultiHeadAttention(nn.Module):
    """
    A vanilla multi-head masked self-attention layer with a projection at the end.
    It is possible to use torch.nn.MultiheadAttention here but I am including an
    explicit implementation here to show that there is nothing too scary here.
    """

    def __init__(self, dim: int, num_heads: int, dropout: float, bias: bool = False):
        super().__init__()
        
        # print(dim)
        # print(num_heads)
        # print(dim % num_heads)
        
        assert dim % num_heads == 0
        self.num_heads = num_heads
        self.dim = dim

        # key, query, value projections for all heads
        self.key = nn.Linear(dim, dim, bias=bias)
        self.query = nn.Linear(dim, dim, bias=bias)
        self.value = nn.Linear(dim, dim, bias=bias)

        # head re-shapers
        self.unstack_heads = Rearrange("b t (h d) -> b h t d", h=self.num_heads)
        self.stack_heads = Rearrange("b h t d -> b t (h d)")

        # regularization
        self.attn_drop = nn.Dropout(dropout)
        self.resid_drop = nn.Dropout(dropout)

        # output projection
        self.proj = nn.Linear(dim, dim, bias=bias)
        self.scale = 1.0 / math.sqrt(dim)

    def get_scores(self, q: torch.Tensor, k: torch.Tensor):
        """
        Arguments:
            q: (B, heads, T, D)
            k: (B, heads, T, D)

        Return:
            QK:     (B, heads, T, T)
        """
        return torch.einsum("bhid,bhjd->bhij", q, k)

    @staticmethod
    def prepare_causal_mask(T, device="cpu", dtype=torch.float32):
        mask = torch.tril(torch.ones((T, T), device=device, dtype=dtype)).view(
            1, 1, T, T
        )
        mask.requires_grad_(False)
        return mask

    def mask_scores(self, qk: torch.Tensor, mask=None):
        T = qk.size(-1)
        if mask is None:
            mask = MultiHeadAttention.prepare_causal_mask(
                T, device=qk.device, dtype=qk.dtype
            )
        qk = qk.masked_fill(mask == 0, float("-inf"))
        return qk

    def forward(
        self,
        Q: torch.Tensor,
        K: torch.Tensor,
        V: torch.Tensor,
        mask: Optional[torch.Tensor] = None,
    ):
        # batch size, sequence length, embedding dimensionality (n_embd)
        B, T, D = Q.size()

        # calculate query, key, values for all heads in batch and move head forward to be the batch dim
        k = self.unstack_heads(self.key(K))  # (B, heads, T, D_head)
        q = self.unstack_heads(self.query(Q))  # (B, heads, T, D_head)
        v = self.unstack_heads(self.value(V))  # (B, heads, T, D_head)

        # QK
        att = self.get_scores(q, k) * self.scale  #  (B, nh, T, T)
        att = self.mask_scores(att, mask)
        att = F.softmax(att, dim=-1)

        # Softmax, dropout, values
        y = self.attn_drop(att) @ v  # (B, nh, T, T) x (B, nh, T, hs) -> (B, nh, T, hs)

        # re-assemble all head outputs side by side
        y = self.stack_heads(y)

        # output projection
        y = self.resid_drop(self.proj(y))
        return y, att


class MultiHeadAttentionAlibi(MultiHeadAttention):
    def __init__(self, dim: int, num_heads: int, dropout: float, bias: bool = False, context_limit: int = -1):
        super().__init__(dim, num_heads, dropout, bias)
        # self.m = torch.tensor(MultiHeadAttentionAlibi.get_slopes(num_heads))
        self.register_parameter(
            "m",
            nn.Parameter(torch.tensor(MultiHeadAttentionAlibi.get_slopes(num_heads))),
        )
        self.m.requires_grad_(False)
        self.mask = None
        self.context_limit = context_limit

    @staticmethod
    def get_slopes(n):
        """
        * aLiBi slopes for heads.
        * m in Figure 3.
        * Source:
            - https://github.com/ofirpress/attention_with_linear_biases/blob/5b327adc6d131e28b40ba58906b30bb469483519/fairseq/models/transformer.py#L742

        Comments:

        In the paper, we only train models that have 2^a heads for some a. This function has
        some good properties that only occur when the input is a power of 2.
        To maintain that even closest_power_of_2 = 2**math.floor(math.log2(n))
        when the number of heads is not a power of 2, we use this workaround.
        """

        def get_slopes_power_of_2(n):
            start = 2 ** (-(2 ** -(math.log2(n) - 3)))
            ratio = start
            return [start * ratio ** i for i in range(n)]

        # In the paper, we only train models that have 2^a heads for some a. This function has
        # some good properties that only occur when the input is a power of 2. To maintain that even
        # when the number of heads is not a power of 2, we use this workaround.
        if math.log2(n).is_integer():
            slopes = get_slopes_power_of_2(n)
        else:
            closest_power_of_2 = 2 ** math.floor(math.log2(n))
            slopes = (
                get_slopes_power_of_2(closest_power_of_2)
                + MultiHeadAttentionAlibi.get_slopes(2 * closest_power_of_2)[0::2][
                    : n - closest_power_of_2
                ]
            )
        return slopes

    @staticmethod
    def get_relative_bias_matrix(n, num_heads, device="cpu", dtype=torch.float32):
        """Relative Bias matrix for aLiBi embeddings"""
        return (
            torch.arange(n, device=device, dtype=dtype)
            .view(1, 1, -1)
            .expand(1, num_heads, -1)
        )

    def get_alibi_mask(self, T: int, device="cpu", dtype=torch.float32):
        rel_bias_mat = MultiHeadAttentionAlibi.get_relative_bias_matrix(
            T, self.num_heads, device, dtype
        )
        alibi = rel_bias_mat * self.m.unsqueeze(0).unsqueeze(-1).to(device)

        # Causal mask (standard GPT pask)
        # lower triangle = 1
        # upper triangle = 0
        mask = MultiHeadAttention.prepare_causal_mask(T, device, dtype)  # (1, 1, T, T)
        # Repeat to get a mask for each head
        mask = mask.repeat(1, self.num_heads, 1, 1)  # (1, num_heads, T, T)
        # fill "future" information with negative infinity
        mask.masked_fill_(mask == 0, float("-inf"))

        # Add causality mask to alibi  (1, num_heads, T, T)
        alibi = alibi.unsqueeze(-2) + mask
        alibi.requires_grad_(False)  # this should not be trained
        return alibi

    def mask_scores(self, qk: torch.Tensor, mask=None):
        T = qk.size(-1)
        if mask is None:
            if self.mask is None or self.mask.shape[-1] < T:
                mask = self.get_alibi_mask(T, device=qk.device, dtype=qk.dtype)
                if self.context_limit > 0:
                    for j in range(mask.shape[2]):
                        del_mask_start = 0
                        del_mask_end = max(0, j - self.context_limit + 1)
                        for n in range(del_mask_start, del_mask_end):
                            mask[..., j, n] = float("-inf")
                
                self.mask = mask
            else:
                mask = self.mask[..., :T, :T]
            # print(mask)
            # print("mask: ", tuple(mask.shape))

        # add aLiBi-mask to qk (see Figure 3.)
        # Addition/translation does not effect softmax (over each row)
        # mentioned in the original representation
        qk = qk + mask.to(qk.device)
        return qk


class TransformerLayer(nn.Module):
    """
    Transformer Layer

    Using pre-layer-normalization: https://arxiv.org/pdf/2002.04745.pdf
    Inspiration: https://nn.labml.ai/transformers/models.html
    AliBI Attention: https://ofir.io/train_short_test_long.pdf
    """

    def __init__(
        self,
        dim: int = 256,
        ffn_dim: int = 768,
        num_heads: int = 4,
        ffn_activation: str = "GELU",
        dropout: float = 0.1,
        cross_attention: bool = False,
        context_limit: int = -1
    ):
        super().__init__()
        self.dim = dim
        self.ffn_dim = ffn_dim
        self.num_heads = num_heads
        self.dropout_p = dropout
        self.cross_attention = cross_attention

        self.dropout = nn.Dropout(p=dropout)
        self.ln_self_attn = nn.LayerNorm(dim)
        self.ln_ffnetwork = nn.LayerNorm(dim)
        self.mha = MultiHeadAttentionAlibi(
            dim=dim, num_heads=num_heads, dropout=dropout, context_limit=context_limit
        )
        self.ffnetwork = ffn_block(
            dim, ffn_dim, activation=ffn_activation, dropout=dropout
        )

        if cross_attention:
            self.ln_src_attn = nn.LayerNorm(dim)
            self.mha_cross = MultiHeadAttentionAlibi(
                dim=dim, num_heads=num_heads, dropout=dropout, context_limit=context_limit
            )

    def forward(
        self,
        x: torch.Tensor,
        src: Optional[torch.Tensor] = None,
        mask: Optional[torch.Tensor] = None,
    ) -> Tuple[torch.Tensor, torch.Tensor, Optional[torch.Tensor]]:
        """
        Using pre-layer-normalization: https://arxiv.org/pdf/2002.04745.pdf
        """

        # Self-attention
        z = self.ln_self_attn(x)
        self_attn, self_attn_weights = self.mha(Q=z, K=z, V=z, mask=mask)

        # Residual
        x = x + self.dropout(self_attn)

        # Cross-attention
        cross_attn_weights = None
        if self.cross_attention and src is not None:
            z = self.ln_src_attn(x)
            # https://nn.labml.ai/transformers/models.html#section-16
            # Don't normalize src... why?
            cross_attn, cross_attn_weights = self.mha_cross(
                Q=z, K=src, V=src, mask=mask
            )
            x = x + self.dropout(cross_attn)

        x = x + self.dropout(self.ffnetwork(self.ln_ffnetwork(x)))
        return x, self_attn_weights, cross_attn_weights


class TransformerStereoLayer(TransformerLayer):
    def forward(
        self,
        x1: torch.Tensor,
        x2: torch.Tensor,
        mask: Optional[torch.Tensor] = None,
    ):
        # sa1w: self-attention-weights 1
        # ca1w: cross-attention-weights 1
        z1, sa1w, ca1w = super().forward(x=x1, src=x2, mask=mask)
        z2, sa2w, ca2w = super().forward(x=x2, src=x1, mask=mask)
        return z1, z2, [sa1w, ca1w, sa2w, ca2w]

class TransformerConcatLayer(TransformerLayer):
    def forward(
        self,
        x_list: List[torch.Tensor],
        mask: Optional[torch.Tensor] = None,
    ):
        # print('TransformerConcatLayer: start')
        # sa1w: self-attention-weights 1
        # ca1w: cross-attention-weights 1
        x = x_list[0]
        
        if len(x_list) > 1:
            for x_tmp in x_list[1:]:
                # print('###')
                # print(x_tmp.shape)
                # print(x_tmp.shape)
                x = torch.concat([x, x_tmp], dim=2)
            
        x, self_attn_weights, cross_attn_weights = super().forward(x=x, mask=mask)
        # print('TransformerConcatLayer: end')
        
        return x, self_attn_weights, cross_attn_weights

class GPT(nn.Module):
    """
    GPT like transformer Decoder-only class.

    * Uses AliBi attention (no positional embeddings or max-sequence-length)
    """

    def __init__(
        self,
        dim: int,
        dff_k: int = 3,
        num_layers: int = 4,
        num_heads: int = 4,
        activation: str = "GELU",
        dropout: float = 0.1,
        context_limit: int = -1,
    ):
        super().__init__()
        self.dim = dim
        self.dff = int(dim * dff_k)
        self.num_layers = num_layers
        self.num_heads = num_heads
        self.activation = activation
        self.dropout = dropout
        self.context_limit = context_limit

        self._build_layers()
        self.apply(self._init_weights)

    def _build_layers(self):
        layers = []
        for _ in range(self.num_layers):
            layers.append(
                TransformerLayer(
                    dim=self.dim,
                    ffn_dim=self.dff,
                    num_heads=self.num_heads,
                    ffn_activation=self.activation,
                    dropout=self.dropout,
                    context_limit=self.context_limit
                )
            )
        self.layers = nn.ModuleList(layers)

    def _init_weights(self, module):
        if isinstance(module, (nn.Linear, nn.Embedding)):
            torch.nn.init.normal_(module.weight, mean=0.0, std=0.02)
            if isinstance(module, nn.Linear) and module.bias is not None:
                torch.nn.init.zeros_(module.bias)
        elif isinstance(module, nn.LayerNorm):
            torch.nn.init.zeros_(module.bias)
            torch.nn.init.ones_(module.weight)

    def forward(
        self, x: torch.Tensor, attention: bool = False
    ) -> Dict[str, torch.Tensor]:
        all_attention = []
        
        # print('GPT: input_dim', x.shape)

        for layer in self.layers:
            x, self_attn_weights, _ = layer(x)
            if attention:
                all_attention.append(self_attn_weights)

        # print('GPT: output_dim', x.shape)

        ret = {"x": x}

        if attention:
            self_attn_weights = torch.stack(all_attention, dim=1)
            ret["attn"] = self_attn_weights

        return ret


class GPTStereo(GPT):
    def _build_layers(self):
        layers = []
        for _ in range(self.num_layers):
            layers.append(
                TransformerStereoLayer(
                    dim=self.dim,
                    ffn_dim=self.dff,
                    num_heads=self.num_heads,
                    ffn_activation=self.activation,
                    dropout=self.dropout,
                    cross_attention=True,
                    context_limit=self.context_limit
                )
            )
        self.layers = nn.ModuleList(layers)

        # Combine output from both 'towers'
        self.combinator = Combinator(dim=self.dim, activation="GELU")

    def forward(
        self, x1: torch.Tensor, x2: torch.Tensor, attention: bool = False
    ) -> Dict[str, torch.Tensor]:

        self_attn_a = []
        self_attn_b = []
        cross_attn_a = []
        cross_attn_b = []
        for layer in self.layers:
            x1, x2, attn_list = layer(x1=x1, x2=x2)
            if attention:
                # [sa1w, ca1w, sa2w, ca2w] = attn_list
                self_attn_a.append(attn_list[0])
                cross_attn_a.append(attn_list[1])
                self_attn_b.append(attn_list[2])
                cross_attn_b.append(attn_list[3])

        x = self.combinator(x1, x2)
        ret = {"x": x, "x1": x1, "x2": x2}

        if attention:
            # B, num_layers, num_heads, N, N
            self_attn_a = torch.stack(self_attn_a, dim=1)  # stack on layer dim
            self_attn_b = torch.stack(self_attn_b, dim=1)  # stack on layer dim
            cross_attn_a = torch.stack(cross_attn_a, dim=1)  # stack on layer dim
            cross_attn_b = torch.stack(cross_attn_b, dim=1)  # stack on layer dim
            ret["self_attn"] = torch.stack([self_attn_a, self_attn_b], dim=1)
            ret["cross_attn"] = torch.stack([cross_attn_a, cross_attn_b], dim=1)
        return ret


class Combinator(nn.Module):
    """
    Combines the "ego-centric" representations from identical 'towers'
    processing channel 0 and 1. The towers are identical (shared weights)
    and therefore channel agnostic, e.g. they don't know if they process information
    from the view of speaker A or B.

    Here we have specific layers associated with each channel to join the representations
    into a single coherent space with channel information included.
    """

    def __init__(self, dim: int, activation: str = "GELU"):
        super().__init__()
        self.dim = dim

        # Channel information
        self.h0_a = nn.Linear(dim, dim, bias=False)  # Channel 0
        self.h0_b = nn.Linear(dim, dim, bias=False)  # Channel 1
        self.ln = nn.LayerNorm(self.dim)

        # Activation
        self.activation = getattr(nn, activation)()

    def forward(self, x1: torch.Tensor, x2: torch.Tensor) -> torch.Tensor:
        """
        Combines the hidden states from identical 'towers' which have processed
        each channel from an 'ego-centric' view. However, the towers are channel agnostic
        by default (shared weights) so in this step we process the information from channel 0, 1
        separately into a joint representation.

        The final representation will (see GPTStereo -> ProjectionModel) go into a final linear
        layer to produce logits.
        """

        # Channel specific information
        ha = self.activation(self.ln(self.h0_a(x1)))
        hb = self.activation(self.ln(self.h0_b(x2)))
        h = ha + hb  # combine estimations from both parties
        return h

class Linear(nn.Module):
    
    def __init__(self,
            dim_in: int = 256,
            dim_out: int = 768,
            activation: str = "GELU",
            dropout: float = 0.1,
        ):
        
        super().__init__()
        self.dim_in = dim_in
        self.dim_out = dim_out
        self.dropout_p = dropout

        self.dropout = nn.Dropout(p=dropout)
        
            
        self.linear = nn.Linear(dim_in, dim_out)
        self.ln = nn.LayerNorm(dim_out)

    def forward(self, x1, x2=None):
        
        ret = {}
        
        if x2 == None:

            z = self.linear(x1)
            z = self.ln(z)
            
            ret["x"] = z
            
        else:
            
            z = torch.concat([x1, x2], dim=2)
            z = self.linear(z)
            z = self.ln(z)
            
            ret["x1"] = x1
            ret["x2"] = x2
            ret["x"] = z
            
        return ret

class LinearStereo(Linear):

    def forward(self, x1, x2):
        
        # print(x1.shape, x2.shape)
        
        z = super().forward(x1, x2)
        
        return z



if __name__ == "__main__":

    pass
