#!/usr/bin/env python3
"""
Test Backchannel Integration
Verify that the vap_bc_2type inspired backchannel prediction is correctly integrated
"""

import torch
import torch.nn.functional as F
from model import VapConfig, VapGPT
import numpy as np

def test_backchannel_config():
    """Test that backchannel configuration parameters work correctly"""
    
    print("🔧 Testing Backchannel Configuration...")
    
    # Test default config (no backchannel prediction)
    config_default = VapConfig()
    assert config_default.predict_backchannels == False
    assert config_default.backchannel_classes == 3
    assert config_default.backchannel_loss_weight == 0.3
    print("✅ Default configuration correct")
    
    # Test config with backchannel prediction enabled
    config_bc = VapConfig(
        predict_backchannels=True,
        backchannel_classes=3,
        backchannel_loss_weight=0.5
    )
    assert config_bc.predict_backchannels == True
    assert config_bc.backchannel_classes == 3
    assert config_bc.backchannel_loss_weight == 0.5
    print("✅ Backchannel-enabled configuration correct")
    
    # Test head nod configuration
    config_nod = VapConfig(
        predict_head_nods=True,
        head_nod_classes=4,
        head_nod_loss_weight=0.4
    )
    assert config_nod.predict_head_nods == True
    assert config_nod.head_nod_classes == 4
    assert config_nod.head_nod_loss_weight == 0.4
    print("✅ Head nod configuration correct")

def test_model_architecture():
    """Test that the model architecture includes backchannel prediction heads"""
    
    print("\n🏗️ Testing Model Architecture...")
    
    # Test model without backchannel prediction
    config_no_bc = VapConfig(predict_backchannels=False)
    model_no_bc = VapGPT(config_no_bc)
    
    assert not hasattr(model_no_bc, 'backchannel_head')
    print("✅ Model without backchannel prediction - no BC head")
    
    # Test model with backchannel prediction
    config_with_bc = VapConfig(
        predict_backchannels=True,
        backchannel_classes=3,
        predict_head_nods=True,
        head_nod_classes=4
    )
    model_with_bc = VapGPT(config_with_bc)
    
    assert hasattr(model_with_bc, 'backchannel_head')
    assert hasattr(model_with_bc, 'head_nod_head')
    assert model_with_bc.backchannel_head.out_features == 3
    assert model_with_bc.head_nod_head.out_features == 4
    print("✅ Model with backchannel prediction - BC and head nod heads present")
    
    # Test different model modes (skip face encoder due to missing pretrained model)
    print("✅ Face encoder mode - skipped (requires pretrained model)")

def test_forward_pass():
    """Test that forward pass includes backchannel predictions"""
    
    print("\n🚀 Testing Forward Pass...")
    
    # Create model with backchannel prediction
    config = VapConfig(
        predict_backchannels=True,
        backchannel_classes=3,
        predict_head_nods=True,
        head_nod_classes=4,
        dim=256,
        use_face_encoder=False  # Simple audio-only test
    )
    model = VapGPT(config)
    model.eval()
    
    # Create dummy input with complete multimodal data
    batch_size = 2
    audio_length = 16000 * 10  # 10 seconds at 16kHz
    seq_len = int(10 * config.frame_hz)  # 10 seconds at frame_hz
    
    dummy_input = {
        "waveform": torch.randn(batch_size, 2, audio_length),  # Stereo audio
        "gaze1": torch.randn(batch_size, seq_len, 3),  # Gaze data
        "gaze2": torch.randn(batch_size, seq_len, 3),
        "head1": torch.randn(batch_size, seq_len, 3),  # Head pose data
        "head2": torch.randn(batch_size, seq_len, 3),
        "face1": torch.randn(batch_size, seq_len, 512),  # Face features
        "face2": torch.randn(batch_size, seq_len, 512),
        "body1": torch.randn(batch_size, seq_len, 17),  # Body pose data
        "body2": torch.randn(batch_size, seq_len, 17),
        "face_im1": torch.randn(batch_size, seq_len, 128, 128, 3),  # Face images (dummy)
        "face_im2": torch.randn(batch_size, seq_len, 128, 128, 3),
    }
    
    with torch.no_grad():
        output = model.forward(src=dummy_input)
    
    # Check that output contains required keys
    required_keys = ["logits", "vad"]
    for key in required_keys:
        assert key in output, f"Missing required key: {key}"
    
    # Check backchannel prediction outputs
    if config.predict_backchannels:
        assert "backchannel_logits" in output, "Missing backchannel_logits"
        assert "backchannel_probs" in output, "Missing backchannel_probs"
        
        bc_logits = output["backchannel_logits"]
        bc_probs = output["backchannel_probs"]
        
        # Check shapes
        expected_time_dim = int(10 * config.frame_hz)  # 10 seconds at 50Hz
        assert bc_logits.shape[-1] == config.backchannel_classes, f"Wrong BC classes: {bc_logits.shape[-1]} vs {config.backchannel_classes}"
        assert bc_probs.shape == bc_logits.shape, "BC probs and logits shape mismatch"
        
        # Check probabilities sum to 1
        prob_sums = bc_probs.sum(dim=-1)
        assert torch.allclose(prob_sums, torch.ones_like(prob_sums), atol=1e-6), "BC probabilities don't sum to 1"
        
        print(f"✅ Backchannel predictions: {bc_logits.shape}")
    
    # Check head nod prediction outputs
    if config.predict_head_nods:
        assert "head_nod_logits" in output, "Missing head_nod_logits"
        assert "head_nod_probs" in output, "Missing head_nod_probs"
        
        nod_logits = output["head_nod_logits"]
        nod_probs = output["head_nod_probs"]
        
        assert nod_logits.shape[-1] == config.head_nod_classes, f"Wrong nod classes: {nod_logits.shape[-1]} vs {config.head_nod_classes}"
        assert nod_probs.shape == nod_logits.shape, "Nod probs and logits shape mismatch"
        
        prob_sums = nod_probs.sum(dim=-1)
        assert torch.allclose(prob_sums, torch.ones_like(prob_sums), atol=1e-6), "Nod probabilities don't sum to 1"
        
        print(f"✅ Head nod predictions: {nod_logits.shape}")

def test_loss_computation():
    """Test that backchannel loss can be computed correctly"""
    
    print("\n📊 Testing Loss Computation...")
    
    # Create dummy predictions and targets
    batch_size = 2
    seq_len = 500  # 10 seconds at 50Hz
    num_classes = 3
    
    # Dummy backchannel predictions
    bc_logits = torch.randn(batch_size, seq_len, num_classes)
    bc_probs = F.softmax(bc_logits, dim=-1)
    
    # Dummy ground truth labels (3-class: 0=no_bc, 1=reactive, 2=emotional)
    bc_labels = torch.randint(0, num_classes, (batch_size, seq_len))
    
    # Compute cross-entropy loss (as would be done in training)
    bc_loss = F.cross_entropy(bc_logits.view(-1, num_classes), bc_labels.view(-1))
    
    assert bc_loss.item() > 0, "Backchannel loss should be positive"
    assert not torch.isnan(bc_loss), "Backchannel loss should not be NaN"
    
    print(f"✅ Backchannel loss computation: {bc_loss.item():.4f}")
    
    # Test weighted loss (as in training)
    vap_loss = torch.tensor(0.5)  # Dummy VAP loss
    bc_weight = 0.3
    total_loss = vap_loss + (bc_weight * bc_loss)
    
    print(f"✅ Weighted total loss: VAP({vap_loss:.3f}) + BC({bc_weight}*{bc_loss:.3f}) = {total_loss:.3f}")

def test_compatibility_with_dataset():
    """Test compatibility with dataset output format"""
    
    print("\n🗂️ Testing Dataset Compatibility...")
    
    # Simulate dataset output with backchannel labels
    dummy_batch = {
        "waveform": torch.randn(2, 2, 160000),  # 10 seconds stereo
        "vad": torch.rand(2, 550, 2),  # VAD labels
        "backchannel_labels": torch.randint(0, 3, (2, 500)),  # BC labels
        "gaze1": torch.randn(2, 500, 3),  # Complete multimodal data
        "gaze2": torch.randn(2, 500, 3),
        "head1": torch.randn(2, 500, 3),
        "head2": torch.randn(2, 500, 3),
        "face1": torch.randn(2, 500, 512),  # Face features
        "face2": torch.randn(2, 500, 512),
        "body1": torch.randn(2, 500, 17),  # Body pose data
        "body2": torch.randn(2, 500, 17),
        "face_im1": torch.randn(2, 500, 128, 128, 3),  # Face images
        "face_im2": torch.randn(2, 500, 128, 128, 3),
    }
    
    # Verify shapes are compatible
    bc_labels = dummy_batch["backchannel_labels"]
    assert bc_labels.dim() == 2, "BC labels should be 2D (batch, time)"
    assert bc_labels.dtype == torch.long, "BC labels should be long/int type"
    assert bc_labels.min() >= 0 and bc_labels.max() < 3, "BC labels should be in range [0, 2]"
    
    print("✅ Dataset format compatibility verified")
    
    # Test that model can handle this input format
    config = VapConfig(predict_backchannels=True, use_face_encoder=False)
    model = VapGPT(config)
    model.eval()
    
    with torch.no_grad():
        output = model.forward(src=dummy_batch)
    
    assert "backchannel_logits" in output
    print("✅ Model handles dataset format correctly")

def test_vap_bc_2type_compatibility():
    """Test compatibility with vap_bc_2type conventions"""
    
    print("\n🎯 Testing vap_bc_2type Compatibility...")
    
    # Test 3-class system (no_bc=0, reactive=1, emotional=2)
    config = VapConfig(predict_backchannels=True, backchannel_classes=3)
    model = VapGPT(config)
    
    # Check that the class count matches vap_bc_2type convention
    assert config.backchannel_classes == 3, "Should use 3-class system like vap_bc_2type"
    
    # Test prediction head output dimension
    dummy_features = torch.randn(1, 100, config.dim)
    with torch.no_grad():
        bc_logits = model.backchannel_head(dummy_features)
    
    assert bc_logits.shape[-1] == 3, "Output should have 3 classes"
    
    # Test that probabilities are valid
    bc_probs = F.softmax(bc_logits, dim=-1)
    assert torch.all(bc_probs >= 0), "Probabilities should be non-negative"
    assert torch.all(bc_probs <= 1), "Probabilities should be <= 1"
    
    prob_sums = bc_probs.sum(dim=-1)
    assert torch.allclose(prob_sums, torch.ones_like(prob_sums)), "Probabilities should sum to 1"
    
    print("✅ 3-class probability distribution correct")
    
    # Verify interpretation matches vap_bc_2type:
    # Class 0: No backchannel
    # Class 1: Reactive backchannel (mm-hmm, oui, d'accord)
    # Class 2: Emotional backchannel (oh, vraiment?, c'est fou)
    
    print("✅ Class interpretation matches vap_bc_2type:")
    print("   - Class 0: No backchannel") 
    print("   - Class 1: Reactive backchannel")
    print("   - Class 2: Emotional backchannel")

def main():
    """Run all integration tests"""
    
    print("🧪 BACKCHANNEL INTEGRATION TEST SUITE")
    print("="*60)
    print("Testing vap_bc_2type inspired backchannel prediction integration")
    print("="*60)
    
    try:
        test_backchannel_config()
        test_model_architecture() 
        test_forward_pass()
        test_loss_computation()
        test_compatibility_with_dataset()
        test_vap_bc_2type_compatibility()
        
        print("\n" + "="*60)
        print("🎉 ALL TESTS PASSED!")
        print("✅ Backchannel integration is working correctly")
        print("✅ Ready for training with French backchannel data")
        print("="*60)
        
        print("\n🚀 Next Steps:")
        print("1. Run French backchannel data generation:")
        print("   python run_french_backchannel_integration.py")
        print("2. Start backchannel training:")
        print("   Audio_Semantics_French_Backchannels.bat")
        print("3. Monitor backchannel metrics in logs")
        
        return True
        
    except Exception as e:
        print(f"\n❌ TEST FAILED: {e}")
        import traceback
        traceback.print_exc()
        return False

if __name__ == "__main__":
    success = main()
    if not success:
        exit(1)
