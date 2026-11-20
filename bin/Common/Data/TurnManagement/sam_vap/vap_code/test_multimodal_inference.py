#!/usr/bin/env python3
"""
Simple test script for multimodal VAP inference
Automatically constructs paths to multimodal CSV files based on session name
"""

import argparse
import subprocess
import sys
from pathlib import Path

def main():
    parser = argparse.ArgumentParser(description='Test multimodal VAP inference')
    parser.add_argument('--model_path', type=str, required=True,
                       help='Path to trained VAP model checkpoint')
    parser.add_argument('--audio1', type=str, required=True,
                       help='Path to speaker 1 audio file')
    parser.add_argument('--audio2', type=str, required=True,
                       help='Path to speaker 2 audio file')
    parser.add_argument('--data_dir', type=str, default='noxi',
                       help='Directory containing multimodal CSV files (default: noxi)')
    parser.add_argument('--session', type=str, required=True,
                       help='Session name (e.g., Paris_01, Augsburg_02)')
    parser.add_argument('--face_encoder_path', type=str,
                       help='Path to face encoder model (if using face encoder)')
    parser.add_argument('--output_dir', type=str, default='inference_output',
                       help='Output directory for results')
    parser.add_argument('--static_only', action='store_true',
                       help='Generate only static PNG plots')
    parser.add_argument('--animated', action='store_true',
                       help='Generate animated visualization')
    parser.add_argument('--device', type=str, default='cuda',
                       help='Device to run inference on (cuda, cuda:0, cuda:1, cpu)')
    
    args = parser.parse_args()
    
    # Construct paths to multimodal CSV files
    data_dir = Path(args.data_dir)
    
    # Check if data directory exists
    if not data_dir.exists():
        print(f"❌ Data directory not found: {data_dir}")
        return 1
    
    # Construct multimodal file paths
    multimodal_files = {
        'face1': data_dir / f"{args.session}-face_expert.csv",
        'gaze1': data_dir / f"{args.session}-gaze_expert.csv", 
        'body1': data_dir / f"{args.session}-head_expert.csv",  # head pose data
        'face2': data_dir / f"{args.session}-face_novice.csv",
        'gaze2': data_dir / f"{args.session}-gaze_novice.csv",
        'body2': data_dir / f"{args.session}-head_novice.csv"   # head pose data
    }
    
    # Check which files exist
    existing_files = {}
    missing_files = []
    
    for key, path in multimodal_files.items():
        if path.exists():
            existing_files[key] = str(path)
            print(f"✅ Found {key}: {path}")
        else:
            missing_files.append(f"{key}: {path}")
            print(f"❌ Missing {key}: {path}")
    
    if missing_files:
        print(f"\n⚠️  Some multimodal files are missing:")
        for missing in missing_files:
            print(f"   - {missing}")
        print("\nContinuing with available files...")
    
    # Build offline_inference.py command
    cmd = [
        sys.executable, 'offline_inference.py',
        '--model_path', args.model_path,
        '--audio1', args.audio1,
        '--audio2', args.audio2,
        '--output_dir', args.output_dir,
        '--device', args.device
    ]
    
    # Add multimodal file paths
    for key, path in existing_files.items():
        cmd.extend([f'--{key}', path])
    
    # Add face encoder path if provided
    if args.face_encoder_path:
        cmd.extend(['--pretrained_face_encoder', args.face_encoder_path])
    
    # Add visualization options
    if args.static_only:
        cmd.append('--static_only')
    elif args.animated:
        cmd.append('--animated')
    
    print(f"\n🚀 Running multimodal inference:")
    print(f"   Session: {args.session}")
    print(f"   Model: {args.model_path}")
    print(f"   Audio files: {args.audio1}, {args.audio2}")
    print(f"   Multimodal files: {len(existing_files)} found")
    print(f"   Command: {' '.join(cmd)}")
    
    # Run the command
    try:
        result = subprocess.run(cmd, check=True)
        print(f"\n🎉 Multimodal inference completed successfully!")
        return result.returncode
    except subprocess.CalledProcessError as e:
        print(f"\n❌ Inference failed with error code {e.returncode}")
        return e.returncode
    except FileNotFoundError:
        print(f"\n❌ offline_inference.py not found in current directory")
        return 1

if __name__ == '__main__':
    sys.exit(main())