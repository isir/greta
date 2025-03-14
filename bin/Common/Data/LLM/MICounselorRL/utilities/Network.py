import torch

import torch.nn as nn




class Network(torch.nn.Module):

    def __init__(self, input_dimension, output_dimension, output_activation=torch.nn.Identity()):
        super(Network, self).__init__()
        self.layer_1 = torch.nn.Linear(in_features=input_dimension, out_features=64)
        self.output_layer = torch.nn.Linear(in_features=64, out_features=output_dimension)
        self.output_activation = output_activation
        self.dropout = torch.nn.Dropout(p=0.2)

    def forward(self, inpt):
        layer_1_output = torch.nn.functional.leaky_relu(self.layer_1(inpt))
        layer_2_output = self.dropout(layer_1_output)
        output = self.output_activation(self.output_layer(layer_2_output))
        return output

    def init_weights(self, seed):
        torch.manual_seed(seed)
        nn.init.xavier_uniform_(self.layer_1.weight)
        nn.init.xavier_uniform_(self.output_layer.weight)