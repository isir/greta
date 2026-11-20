#!/usr/bin/env python3
"""
Simple test to verify config class imports work
"""

try:
    # Import config classes
    from model import VapConfig
    from finetune import OptConfig, DataConfig
    from events import EventConfig
    
    print("✅ All config classes imported successfully:")
    print(f"   - VapConfig: {VapConfig}")
    print(f"   - OptConfig: {OptConfig}")
    print(f"   - DataConfig: {DataConfig}")
    print(f"   - EventConfig: {EventConfig}")
    
    # Test adding to module namespace
    import sys
    current_module = sys.modules[__name__]
    current_module.VapConfig = VapConfig
    current_module.OptConfig = OptConfig
    current_module.DataConfig = DataConfig
    current_module.EventConfig = EventConfig
    
    print("✅ Config classes added to module namespace")
    
    # Test PyTorch safe globals
    import torch
    torch.serialization.add_safe_globals([VapConfig, OptConfig, DataConfig, EventConfig])
    print("✅ Config classes added to PyTorch safe globals")
    
    print("\n🎉 All setup completed successfully!")
    
except Exception as e:
    print(f"❌ Error: {e}")
    import traceback
    traceback.print_exc()

