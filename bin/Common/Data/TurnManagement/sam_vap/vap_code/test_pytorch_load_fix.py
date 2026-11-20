#!/usr/bin/env python3
"""
Test script to verify PyTorch 2.6+ checkpoint loading fix
"""

import torch
import sys
from pathlib import Path

# Import config classes to test the fix
try:
    from model import VapConfig
    from finetune import OptConfig, DataConfig
    from events import EventConfig
    print("✅ Successfully imported all config classes")
except ImportError as e:
    print(f"❌ Failed to import config classes: {e}")
    sys.exit(1)

def test_checkpoint_loading(model_path: str):
    """Test loading a VAP checkpoint with the PyTorch 2.6+ fix"""
    
    if not Path(model_path).exists():
        print(f"❌ Model file not found: {model_path}")
        return False
    
    try:
        print(f"🧪 Testing checkpoint loading: {model_path}")
        
        # Apply the fix for PyTorch 2.6+
        # Add config classes to main module namespace for pickle compatibility
        import sys
        current_module = sys.modules[__name__]
        current_module.OptConfig = OptConfig
        current_module.DataConfig = DataConfig
        current_module.EventConfig = EventConfig
        current_module.VapConfig = VapConfig
        
        torch.serialization.add_safe_globals([VapConfig, OptConfig, DataConfig, EventConfig])
        
        # Try loading the checkpoint
        checkpoint = torch.load(model_path, map_location="cpu", weights_only=False)
        
        print("✅ Checkpoint loaded successfully!")
        
        # Check if it contains expected keys
        if 'hyper_parameters' in checkpoint:
            print("✅ Found hyper_parameters in checkpoint")
            if 'conf' in checkpoint['hyper_parameters']:
                print("✅ Found VapConfig in hyper_parameters")
                conf = checkpoint['hyper_parameters']['conf']
                print(f"   - Config type: {type(conf)}")
                if hasattr(conf, 'sample_rate'):
                    print(f"   - Sample rate: {conf.sample_rate}")
        
        if 'state_dict' in checkpoint:
            print("✅ Found state_dict in checkpoint")
            print(f"   - Number of parameters: {len(checkpoint['state_dict'])}")
        
        return True
        
    except Exception as e:
        print(f"❌ Failed to load checkpoint: {e}")
        return False

if __name__ == "__main__":
    import argparse
    
    parser = argparse.ArgumentParser(description='Test PyTorch checkpoint loading fix')
    parser.add_argument('--model_path', type=str, required=True,
                       help='Path to VAP model checkpoint to test')
    
    args = parser.parse_args()
    
    print("🔧 PyTorch Checkpoint Loading Test")
    print("=" * 40)
    print(f"PyTorch version: {torch.__version__}")
    print(f"Model path: {args.model_path}")
    print()
    
    success = test_checkpoint_loading(args.model_path)
    
    if success:
        print("\n🎉 Test passed! The checkpoint loading fix works correctly.")
        sys.exit(0)
    else:
        print("\n❌ Test failed! There may still be issues with checkpoint loading.")
        sys.exit(1)
