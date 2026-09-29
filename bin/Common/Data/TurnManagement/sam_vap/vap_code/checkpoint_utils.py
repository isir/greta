#!/usr/bin/env python3
"""
Utility functions for loading PyTorch checkpoints with config class compatibility
"""

import torch
import sys
from typing import Dict, Any

def load_vap_checkpoint(checkpoint_path: str, map_location: str = "cpu") -> Dict[str, Any]:
    """
    Load VAP checkpoint with PyTorch 2.6+ compatibility
    
    Args:
        checkpoint_path: Path to checkpoint file
        map_location: Device to map tensors to
        
    Returns:
        Loaded checkpoint dictionary
    """
    # Import all config classes
    from model import VapConfig
    from finetune import OptConfig, DataConfig
    from events import EventConfig
    
    # Add config classes to current module namespace for pickle compatibility
    current_module = sys.modules[__name__]
    current_module.VapConfig = VapConfig
    current_module.OptConfig = OptConfig
    current_module.DataConfig = DataConfig
    current_module.EventConfig = EventConfig
    
    # Also add to __main__ module if it exists
    if '__main__' in sys.modules:
        main_module = sys.modules['__main__']
        main_module.VapConfig = VapConfig
        main_module.OptConfig = OptConfig
        main_module.DataConfig = DataConfig
        main_module.EventConfig = EventConfig
    
    # Add safe globals for PyTorch 2.6+
    torch.serialization.add_safe_globals([VapConfig, OptConfig, DataConfig, EventConfig])
    
    # Load checkpoint
    try:
        checkpoint = torch.load(checkpoint_path, map_location=map_location, weights_only=False)
        return checkpoint
    except Exception as e:
        print(f"❌ Failed to load checkpoint {checkpoint_path}: {e}")
        raise

def setup_config_classes_globally():
    """
    Setup config classes in global namespace for pickle compatibility
    This should be called before any checkpoint loading
    """
    # Import all config classes
    from model import VapConfig
    from finetune import OptConfig, DataConfig
    from events import EventConfig
    
    # Add to all relevant modules
    modules_to_update = ['__main__', 'checkpoint_utils']
    
    for module_name in modules_to_update:
        if module_name in sys.modules:
            module = sys.modules[module_name]
            module.VapConfig = VapConfig
            module.OptConfig = OptConfig
            module.DataConfig = DataConfig
            module.EventConfig = EventConfig
    
    # Add safe globals for PyTorch 2.6+
    torch.serialization.add_safe_globals([VapConfig, OptConfig, DataConfig, EventConfig])
    
    print("✅ Config classes setup globally for checkpoint loading")
