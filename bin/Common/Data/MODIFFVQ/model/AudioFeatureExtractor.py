import torch
from torch import nn

class AudioFeatureExtractor(nn.Module):
    def __init__(self, bundle,
                 device = None, 
                 d_model: int = 128,
                 num_layers: int = 4, 
                 wav2vec_dim: int = 1024):
        '''
        Extracts the last layer of wav2vec features and projects them to a lower-dimensional space.
        The output features are intended to capture both semantic and prosodic information from the audio.
        
        Args:
            - bundle: The torchaudio pipeline bundle containing the wav2vec model.
            - d (int): The dimensionality of the output features (default: 8).
            - device: The device to run the model on (default: None, which will use CUDA if available, otherwise CPU).
        '''
        super().__init__()
        self.model = bundle.get_model().to(device)
        self.bundle = bundle
        if device is not None:
            self.device = device
        else:
            self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")


        self.norm = nn.LayerNorm(wav2vec_dim)
        self.proj = nn.Linear(wav2vec_dim, d_model)
        self.num_layers = num_layers

        self.to(self.device)
        

    def forward(self, x):
        pass
    
    def get_audio_cut(self,waveform,start_time=0.0, end_time=5.0):
        # Calculate the number of samples per second
        sample_rate = self.bundle.sample_rate
        samples_per_second = sample_rate
        # Calculate the start and end sample indices
        start = start_time
        end = end_time

        start_sample = int(start * samples_per_second)
        end_sample = int(end * samples_per_second)

        #to mono
        if waveform.shape[0] > 1:
            waveform = torch.mean(waveform, dim=0, keepdim=True)

        #print(f"start_sample={start_sample}, end_sample={end_sample}, waveform_len={waveform.shape[1]}")

        # Return the audio cut
        return waveform[:, start_sample:end_sample]
    
    def get_features_time(self, x, start_time, end_time):
        x = self.get_audio_cut(x, start_time, end_time)
        x = x.to(self.device)
        with torch.no_grad():
            features, _ = self.model.extract_features(x)   # list of [1, T, 1024]
        # stack all layers
        x = torch.stack(features[self.num_layers:], dim=0).squeeze(1)        # [L, T, 1024]
        return x
        