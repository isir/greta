#!/usr/bin/env python3
"""
CPU-safe encoder patch for PyTorch models
Ensures all encoder loading happens on CPU regardless of original device
"""

import torch
import sys

def patch_torch_load_for_cpu():
    """
    Patch torch.load to always use CPU mapping
    This ensures that any torch.load calls will map to CPU
    """
    original_torch_load = torch.load
    
    def cpu_safe_torch_load(f, map_location=None, **kwargs):
        # Force CPU mapping for all loads
        if map_location is None:
            map_location = 'cpu'
        elif isinstance(map_location, str) and 'cuda' in map_location:
            map_location = 'cpu'
        elif callable(map_location):
            # If it's a function, wrap it to return CPU
            original_map_fn = map_location
            map_location = lambda storage, loc: original_map_fn(storage, loc).cpu()
        
        return original_torch_load(f, map_location=map_location, **kwargs)
    
    # Replace torch.load temporarily
    torch.load = cpu_safe_torch_load
    return original_torch_load

def restore_torch_load(original_torch_load):
    """Restore original torch.load function"""
    torch.load = original_torch_load

def patch_cuda_availability():
    """
    Temporarily disable CUDA availability to force CPU-only operations
    """
    original_cuda_available = torch.cuda.is_available
    torch.cuda.is_available = lambda: False
    return original_cuda_available

def restore_cuda_availability(original_cuda_available):
    """Restore original CUDA availability function"""
    torch.cuda.is_available = original_cuda_available

class CPUSafeContext:
    """Context manager for CPU-safe model loading"""
    
    def __init__(self):
        self.original_torch_load = None
        self.original_cuda_available = None
    
    def __enter__(self):
        # Patch torch.load for CPU-only loading
        self.original_torch_load = patch_torch_load_for_cpu()
        
        # Patch CUDA availability
        self.original_cuda_available = patch_cuda_availability()
        
        print("🔧 CPU-safe context activated - all loading forced to CPU")
        return self
    
    def __exit__(self, exc_type, exc_val, exc_tb):
        # Restore original functions
        if self.original_torch_load:
            restore_torch_load(self.original_torch_load)
        
        if self.original_cuda_available:
            restore_cuda_availability(self.original_cuda_available)
        
        print("✅ CPU-safe context deactivated - original functions restored")

# Convenience function
def with_cpu_safe_loading(func):
    """Decorator to run a function with CPU-safe loading"""
    def wrapper(*args, **kwargs):
        with CPUSafeContext():
            return func(*args, **kwargs)
    return wrapper

if __name__ == "__main__":
    # Test the CPU-safe context
    print("Testing CPU-safe loading context...")
    
    with CPUSafeContext():
        print(f"CUDA available (should be False): {torch.cuda.is_available()}")
        
        # Test torch.load behavior
        test_tensor = torch.randn(3, 3)
        torch.save(test_tensor, "test_tensor.pt")
        
        loaded_tensor = torch.load("test_tensor.pt")
        print(f"Loaded tensor device: {loaded_tensor.device}")
        
        import os
        os.remove("test_tensor.pt")
    
    print(f"CUDA available (restored): {torch.cuda.is_available()}")
    print("✅ CPU-safe context test completed!")

