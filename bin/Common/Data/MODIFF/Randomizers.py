import numpy as np
import torch
import random


def generate_random_series_sequence(sequence_length, max_series=4):
    sequence = [0] * sequence_length
    possible_values = [1, 2, 3]
    max_series_length = sequence_length // 2
    series_count = 0

    while series_count < max_series:
        value = random.choice(possible_values)
        series_length = random.randint(5, max_series_length)
        start_position = random.randint(0, sequence_length - series_length)

        for i in range(start_position, start_position + series_length):
            sequence[i] = value

        series_count += 1
    return sequence

def generate_random_tensors_numpy(batch_size=1):
    z_tensor = np.zeros((batch_size, 4))
    z_tensor[:, 0] = 12
    z_tensor[:, 1] = 2
    z_tensor[:, 2] = 12
    z_tensor[:, 3] = 0

    chunk_descriptor_tensor = np.zeros((batch_size, 3))
    chunk_descriptor_tensor[:, 0] = np.random.random(size=batch_size)
    chunk_descriptor_tensor[:, 1:] = np.random.choice([-1, 0, 1], size=(batch_size, 2))

    z_tensor = torch.tensor(z_tensor, dtype=torch.float32)
    chunk_descriptor_tensor = torch.tensor(chunk_descriptor_tensor, dtype=torch.float32)

    return z_tensor, chunk_descriptor_tensor
