"""
Dataset Integration for French Backchannel Labels
Patch for dataset.py to integrate backchannel loading with existing multimodal pipeline
Compatible with vap_bc_2type structure
"""

import torch
import os
from pathlib import Path

def load_backchannel_labels(self, idx: int, audio_duration_frames: int) -> torch.Tensor:
    """
    Load backchannel labels for a given sample
    
    Args:
        idx: Dataset index
        audio_duration_frames: Expected number of frames based on audio duration
        
    Returns:
        Tensor of shape (T,) with backchannel labels (0=no_bc, 1=reactive, 2=emotional)
    """
    
    d = self.df.iloc[idx]
    
    # Check if backchannel_labels_path column exists and has a value
    if 'backchannel_labels_path' in d and pd.notna(d['backchannel_labels_path']):
        bc_labels_path = str(d['backchannel_labels_path'])
        
        if os.path.exists(bc_labels_path):
            try:
                # Load the backchannel labels
                bc_labels = torch.load(bc_labels_path)
                
                # Ensure correct length to match audio
                if len(bc_labels) != audio_duration_frames:
                    if len(bc_labels) > audio_duration_frames:
                        # Truncate if too long
                        bc_labels = bc_labels[:audio_duration_frames]
                    else:
                        # Pad with zeros if too short
                        padding = torch.zeros(audio_duration_frames - len(bc_labels), dtype=torch.long)
                        bc_labels = torch.cat([bc_labels, padding])
                
                return bc_labels.contiguous()
                
            except Exception as e:
                print(f"Warning: Error loading backchannel labels from {bc_labels_path}: {e}")
                # Return zeros if loading fails
                return torch.zeros(audio_duration_frames, dtype=torch.long)
    
    # Return zeros if no backchannel labels available
    return torch.zeros(audio_duration_frames, dtype=torch.long)

# This is the updated __getitem__ method for VapDataset
# Add this to the existing return dictionary in the multimodal case

BACKCHANNEL_INTEGRATION_CODE = '''
# Add this inside the multimodal return block (around line 580 in dataset.py)

# NEW: Load backchannel labels if available
expected_frames = round(self.frame_hz * duration + self.frame_hz * self.horizon)
backchannel_labels = self.load_backchannel_labels(idx, expected_frames)

return {
    "session": d["session"],
    "waveform": audio,
    "gaze1": gaze1,
    "head1": head1,
    "face1": face1,
    "body1": body1,
    "gaze2": gaze2,
    "head2": head2,
    "face2": face2,
    "body2": body2,
    "face_im1": face_im1,
    "face_im2": face_im2,
    "vad": vad,
    "dataset": d["dataset"],
    "backchannel_labels": backchannel_labels,  # NEW: Add backchannel labels
}
'''

print("To integrate backchannel labels into your dataset.py:")
print("1. Add the load_backchannel_labels method to VapDataset class")
print("2. Update the return dictionary in __getitem__ to include backchannel_labels")
print("3. Import pandas as pd at the top if not already imported")
print("\nCode snippets are provided above.")

