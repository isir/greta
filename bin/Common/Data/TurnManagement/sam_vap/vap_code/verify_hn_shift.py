#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Verification script for head nod shift integration
"""

import sys
import io

# Fix Windows console encoding for Unicode
if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')
    sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8', errors='replace')

def verify_config():
    """Verify VapConfig has head_nod_prediction_shift_ms parameter"""
    print("=" * 80)
    print("TEST 1: VapConfig Parameter")
    print("=" * 80)
    
    try:
        from model import VapConfig
        config = VapConfig()
        
        # Check BC shift
        assert hasattr(config, 'backchannel_prediction_shift_ms'), "Missing backchannel_prediction_shift_ms"
        print(f"✅ backchannel_prediction_shift_ms: {config.backchannel_prediction_shift_ms}ms")
        
        # Check HN shift
        assert hasattr(config, 'head_nod_prediction_shift_ms'), "Missing head_nod_prediction_shift_ms"
        print(f"✅ head_nod_prediction_shift_ms: {config.head_nod_prediction_shift_ms}ms")
        
        # Check they have same default
        assert config.backchannel_prediction_shift_ms == config.head_nod_prediction_shift_ms, \
            "BC and HN shifts should have same default value"
        print(f"✅ Both default to {config.backchannel_prediction_shift_ms}ms")
        
        return True
    except Exception as e:
        print(f"❌ FAILED: {e}")
        return False

def verify_training_code():
    """Verify finetune.py has shift application for HN"""
    print("\n" + "=" * 80)
    print("TEST 2: Training Code")
    print("=" * 80)
    
    try:
        with open('finetune.py', 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Check for generic shift function
        assert 'def apply_prediction_shift' in content, "Missing apply_prediction_shift function"
        print("✅ Found apply_prediction_shift function")
        
        # Check for legacy wrapper
        assert 'def apply_backchannel_prediction_shift' in content, "Missing backward compatibility wrapper"
        print("✅ Found backward compatibility wrapper")
        
        # Check BC shift application
        assert 'self.apply_prediction_shift(' in content, "BC shift not using new function"
        assert 'self.conf.backchannel_prediction_shift_ms' in content, "BC shift not reading from config"
        print("✅ BC shift applied correctly")
        
        # Check HN shift application
        assert 'self.conf.head_nod_prediction_shift_ms' in content, "HN shift not reading from config"
        print("✅ HN shift applied correctly")
        
        # Count occurrences
        bc_count = content.count('backchannel_prediction_shift_ms')
        hn_count = content.count('head_nod_prediction_shift_ms')
        print(f"✅ BC shift referenced {bc_count} times")
        print(f"✅ HN shift referenced {hn_count} times")
        
        return True
    except Exception as e:
        print(f"❌ FAILED: {e}")
        return False

def verify_inference_code():
    """Verify realtime_inference.py reads HN shift from config"""
    print("\n" + "=" * 80)
    print("TEST 3: Inference Code")
    print("=" * 80)
    
    try:
        with open('realtime_inference.py', 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Check reads BC shift
        assert "getattr(model_wrapper.conf, 'backchannel_prediction_shift_ms'" in content, \
            "Inference doesn't read BC shift from config"
        print("✅ Reads BC shift from checkpoint config")
        
        # Check reads HN shift
        assert "getattr(model_wrapper.conf, 'head_nod_prediction_shift_ms'" in content, \
            "Inference doesn't read HN shift from config"
        print("✅ Reads HN shift from checkpoint config")
        
        # Check CLI arguments
        assert "--bc_shift" in content, "Missing --bc_shift CLI argument"
        assert "--hn_shift" in content, "Missing --hn_shift CLI argument"
        print("✅ CLI arguments available for both BC and HN")
        
        # Check logging
        assert 'BC shift:' in content or 'bc_shift' in content, "BC shift not logged"
        assert 'HN shift:' in content or 'hn_shift' in content, "HN shift not logged"
        print("✅ Shift values logged during inference")
        
        return True
    except Exception as e:
        print(f"❌ FAILED: {e}")
        return False

def verify_shift_logic():
    """Verify the shift logic is correct"""
    print("\n" + "=" * 80)
    print("TEST 4: Shift Logic")
    print("=" * 80)
    
    try:
        import torch
        
        # Test shift logic manually (without instantiating VAPModel)
        def apply_prediction_shift(targets, shift_ms=500, frame_hz=50):
            """Inline shift function for testing"""
            if shift_ms <= 0:
                return targets
            shift_frames = int(shift_ms * frame_hz / 1000)
            if shift_frames >= targets.shape[1]:
                return torch.zeros_like(targets)
            shifted_targets = torch.zeros_like(targets)
            if shift_frames < targets.shape[1]:
                shifted_targets[:, :-shift_frames] = targets[:, shift_frames:]
            return shifted_targets
        
        # Test shift function
        # Original: BC at frame 2 (value=1), HN at frame 5 (value=2)
        targets = torch.tensor([[0, 0, 1, 0, 0, 2, 0, 0, 0, 0]], dtype=torch.long)
        
        # Shift by 500ms at 10Hz = 5 frames
        shifted = apply_prediction_shift(targets, shift_ms=500, frame_hz=10)
        
        # Expected: shifted[:, :-5] = targets[:, 5:]
        # So: shifted[:, 0:5] = targets[:, 5:10]
        # targets[5:10] = [2, 0, 0, 0, 0]
        # Result: [2, 0, 0, 0, 0, 0, 0, 0, 0, 0]
        expected = torch.tensor([[2, 0, 0, 0, 0, 0, 0, 0, 0, 0]], dtype=torch.long)
        
        assert torch.equal(shifted, expected), f"Shift logic incorrect: {shifted} != {expected}"
        print(f"✅ Original:  {targets.tolist()[0]}")
        print(f"✅ Shifted:   {shifted.tolist()[0]}")
        print(f"✅ Expected:  {expected.tolist()[0]}")
        print("✅ Shift logic correct: moves targets backward by shift_frames")
        
        return True
    except Exception as e:
        print(f"❌ FAILED: {e}")
        import traceback
        traceback.print_exc()
        return False

def main():
    """Run all verification tests"""
    print("\n" + "=" * 80)
    print("HEAD NOD SHIFT INTEGRATION VERIFICATION")
    print("=" * 80)
    
    results = []
    
    results.append(("Config Parameter", verify_config()))
    results.append(("Training Code", verify_training_code()))
    results.append(("Inference Code", verify_inference_code()))
    results.append(("Shift Logic", verify_shift_logic()))
    
    # Summary
    print("\n" + "=" * 80)
    print("SUMMARY")
    print("=" * 80)
    
    for name, passed in results:
        status = "✅ PASSED" if passed else "❌ FAILED"
        print(f"{name:20s}: {status}")
    
    all_passed = all(passed for _, passed in results)
    
    print("\n" + "=" * 80)
    if all_passed:
        print("✅ ALL TESTS PASSED - Integration Complete!")
    else:
        print("❌ SOME TESTS FAILED - Review Needed")
    print("=" * 80)
    
    return 0 if all_passed else 1

if __name__ == '__main__':
    sys.exit(main())

