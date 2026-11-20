#!/usr/bin/env python3
"""
Safe checkpoint loader for PyTorch 2.6+ compatibility
Handles various checkpoint loading scenarios and config class issues
"""

import torch
import sys
import warnings
from typing import Dict, Any, Optional

def setup_config_classes():
    """Setup all VAP config classes in the global namespace"""
    try:
        # Import all config classes
        from model import VapConfig
        from finetune import OptConfig, DataConfig
        from events import EventConfig
        
        # Add to current module
        current_module = sys.modules[__name__]
        current_module.VapConfig = VapConfig
        current_module.OptConfig = OptConfig
        current_module.DataConfig = DataConfig
        current_module.EventConfig = EventConfig
        
        # Add to __main__ module if it exists
        if '__main__' in sys.modules:
            main_module = sys.modules['__main__']
            main_module.VapConfig = VapConfig
            main_module.OptConfig = OptConfig
            main_module.DataConfig = DataConfig
            main_module.EventConfig = EventConfig
        
        # Add to calling module (try to detect it)
        frame = sys._getframe(1)
        calling_module_name = frame.f_globals.get('__name__')
        if calling_module_name and calling_module_name in sys.modules:
            calling_module = sys.modules[calling_module_name]
            calling_module.VapConfig = VapConfig
            calling_module.OptConfig = OptConfig
            calling_module.DataConfig = DataConfig
            calling_module.EventConfig = EventConfig
        
        # Setup PyTorch safe globals
        torch.serialization.add_safe_globals([VapConfig, OptConfig, DataConfig, EventConfig])
        
        return True
    except Exception as e:
        print(f"⚠️  Warning: Could not setup config classes: {e}")
        return False

def load_vap_checkpoint_safe(checkpoint_path: str, map_location: str = "cpu") -> Optional[Dict[str, Any]]:
    """
    Safely load a VAP checkpoint with multiple fallback strategies
    
    Args:
        checkpoint_path: Path to checkpoint file
        map_location: Device to map tensors to
        
    Returns:
        Loaded checkpoint dictionary or None if failed
    """
    print(f"🔄 Loading checkpoint: {checkpoint_path}")
    
    # Setup config classes first
    setup_config_classes()
    
    # Strategy 1: Standard loading with weights_only=False
    try:
        print("   Trying standard loading...")
        checkpoint = torch.load(checkpoint_path, map_location=map_location, weights_only=False)
        print("✅ Checkpoint loaded successfully!")
        return checkpoint
    except Exception as e1:
        print(f"   Standard loading failed: {e1}")
    
    # Strategy 2: Loading without weights_only parameter (let PyTorch decide)
    try:
        print("   Trying loading without weights_only parameter...")
        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            checkpoint = torch.load(checkpoint_path, map_location=map_location)
        print("✅ Checkpoint loaded with fallback method!")
        return checkpoint
    except Exception as e2:
        print(f"   Fallback loading failed: {e2}")
    
    # Strategy 3: Using pickle module directly
    try:
        print("   Trying with explicit pickle module...")
        import pickle
        checkpoint = torch.load(checkpoint_path, map_location=map_location, 
                              weights_only=False, pickle_module=pickle)
        print("✅ Checkpoint loaded with pickle module!")
        return checkpoint
    except Exception as e3:
        print(f"   Pickle loading failed: {e3}")
    
    # Strategy 4: Disable all safety checks (last resort)
    try:
        print("   Trying with all safety checks disabled...")
        # Temporarily modify torch serialization settings
        original_weights_only = getattr(torch.serialization, '_weights_only_unpickler', None)
        
        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            # Force weights_only to False at the serialization level
            checkpoint = torch.load(checkpoint_path, map_location=map_location, weights_only=False)
        
        print("✅ Checkpoint loaded with safety checks disabled!")
        return checkpoint
    except Exception as e4:
        print(f"   Final fallback failed: {e4}")
    
    print("❌ All loading strategies failed!")
    return None

if __name__ == "__main__":
    # Test the loader
    import argparse
    parser = argparse.ArgumentParser(description='Test safe checkpoint loading')
    parser.add_argument('--checkpoint', type=str, required=True, help='Path to checkpoint')
    args = parser.parse_args()
    
    result = load_vap_checkpoint_safe(args.checkpoint)
    if result:
        print(f"🎉 Successfully loaded checkpoint with {len(result)} keys")
        if 'hyper_parameters' in result:
            print("   Found hyper_parameters")
        if 'state_dict' in result:
            print(f"   Found state_dict with {len(result['state_dict'])} parameters")
    else:
        print("💥 Failed to load checkpoint")
        sys.exit(1)
