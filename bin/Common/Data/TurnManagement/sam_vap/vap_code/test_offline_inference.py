#!/usr/bin/env python3
"""
Test script for offline VAP inference system
Creates synthetic audio files for testing if no real audio is available
"""

import torch
import torchaudio
import numpy as np
from pathlib import Path
import argparse

def create_synthetic_audio(duration: float = 10.0, sample_rate: int = 16000, 
                          speaker_id: int = 1) -> torch.Tensor:
    """
    Create synthetic audio for testing
    
    Args:
        duration: Duration in seconds
        sample_rate: Sample rate
        speaker_id: Speaker identifier (affects frequency)
        
    Returns:
        Synthetic audio tensor
    """
    t = torch.linspace(0, duration, int(duration * sample_rate))
    
    # Create different patterns for different speakers
    if speaker_id == 1:
        # Speaker 1: Lower frequency with some pauses
        freq = 200 + 50 * torch.sin(0.5 * t)
        audio = 0.3 * torch.sin(2 * np.pi * freq * t)
        
        # Add some silence periods (turn-taking)
        silence_mask = (torch.sin(0.3 * t) < -0.5)
        audio[silence_mask] *= 0.1
        
    else:
        # Speaker 2: Higher frequency, different timing
        freq = 300 + 30 * torch.cos(0.7 * t)
        audio = 0.25 * torch.sin(2 * np.pi * freq * t)
        
        # Different silence pattern
        silence_mask = (torch.cos(0.4 * t) < -0.6)
        audio[silence_mask] *= 0.1
    
    # Add some noise
    noise = 0.02 * torch.randn_like(audio)
    audio += noise
    
    return audio

def create_test_audio_files(output_dir: str = "test_audio"):
    """Create test audio files for inference"""
    output_path = Path(output_dir)
    output_path.mkdir(exist_ok=True)
    
    print("🎵 Creating synthetic test audio files...")
    
    # Create two different audio files
    audio1 = create_synthetic_audio(duration=15.0, speaker_id=1)
    audio2 = create_synthetic_audio(duration=15.0, speaker_id=2)
    
    # Save as WAV files
    audio1_path = output_path / "speaker1_test.wav"
    audio2_path = output_path / "speaker2_test.wav"
    
    torchaudio.save(str(audio1_path), audio1.unsqueeze(0), 16000)
    torchaudio.save(str(audio2_path), audio2.unsqueeze(0), 16000)
    
    print(f"✅ Created test audio files:")
    print(f"   Speaker 1: {audio1_path}")
    print(f"   Speaker 2: {audio2_path}")
    
    return str(audio1_path), str(audio2_path)

def test_inference_system(model_path: str):
    """Test the offline inference system with synthetic audio"""
    
    print("🧪 Testing Offline VAP Inference System")
    print("=" * 50)
    
    # Create test audio files
    audio1_path, audio2_path = create_test_audio_files()
    
    # Test the inference system
    try:
        from offline_inference import OfflineVAPInference, VAPVisualizer
        
        print("\n🔮 Testing VAP inference...")
        
        # Initialize inference system
        vap_inference = OfflineVAPInference(model_path, device="cpu")  # Use CPU for testing
        
        # Run inference
        results = vap_inference.inference(audio1_path, audio2_path)
        
        print(f"✅ Inference successful!")
        print(f"   Output shape: {results['vap_probs']['p_now'].shape}")
        print(f"   Duration: {results['duration']:.2f}s")
        
        # Test visualization
        print("\n📊 Testing visualization...")
        visualizer = VAPVisualizer()
        
        # Create visualization (don't save, just test)
        import matplotlib
        matplotlib.use('Agg')  # Use non-interactive backend for testing
        
        visualizer.plot_audio_and_vap(results, save_path="test_output.png")
        
        print("✅ Visualization test successful!")
        
        return True
        
    except Exception as e:
        print(f"❌ Test failed: {e}")
        import traceback
        traceback.print_exc()
        return False

def main():
    parser = argparse.ArgumentParser(description='Test offline VAP inference')
    parser.add_argument('--model_path', type=str, required=True,
                       help='Path to trained VAP model checkpoint')
    
    args = parser.parse_args()
    
    success = test_inference_system(args.model_path)
    
    if success:
        print("\n🎉 All tests passed! The offline inference system is ready.")
        print("\n📝 Usage example:")
        print(f"python offline_inference.py \\")
        print(f"    --audio1 test_audio/speaker1_test.wav \\")
        print(f"    --audio2 test_audio/speaker2_test.wav \\")
        print(f"    --model_path {args.model_path}")
    else:
        print("\n❌ Tests failed. Please check the errors above.")

if __name__ == "__main__":
    main()

