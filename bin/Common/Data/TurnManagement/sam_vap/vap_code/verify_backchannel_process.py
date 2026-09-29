#!/usr/bin/env python3
"""
Backchannel Process Verification Script

This script verifies the backchannel classification process by examining
intermediate files and providing detailed step-by-step analysis.

Usage:
    python verify_backchannel_process.py
"""

import os
import pandas as pd
import numpy as np
from pathlib import Path
from collections import Counter
import re

def verify_transcript_processing():
    """Verify the transcript processing step"""
    print("🔍 VERIFYING TRANSCRIPT PROCESSING")
    print("=" * 50)
    
    transcripts_dir = Path("Transcripts")
    if not transcripts_dir.exists():
        print("❌ Transcripts directory not found!")
        return False
    
    # Look for pair directories
    pair_dirs = list(transcripts_dir.glob("pair*"))
    print(f"Found {len(pair_dirs)} pair directories")
    
    if not pair_dirs:
        print("❌ No pair directories found! Expected structure: Transcripts/pair01/, Transcripts/pair02/, etc.")
        return False
    
    # Analyze each pair directory
    for pair_dir in sorted(pair_dirs):
        print(f"\n📁 Analyzing: {pair_dir.name}")
        
        # Check for expert and novice CSV files
        expert_file = pair_dir / "expert.csv"
        novice_file = pair_dir / "novice.csv"
        
        if not expert_file.exists():
            print(f"  ❌ Missing expert.csv in {pair_dir}")
            continue
        if not novice_file.exists():
            print(f"  ❌ Missing novice.csv in {pair_dir}")
            continue
        
        # Analyze both files
        for csv_file, speaker in [(expert_file, "expert"), (novice_file, "novice")]:
            print(f"  📄 {speaker}.csv:")
            try:
                import pandas as pd
                df = pd.read_csv(csv_file)
                print(f"    Shape: {df.shape}")
                print(f"    Columns: {list(df.columns)}")
                
                # Find transcription column
                text_col = None
                for col in df.columns:
                    if 'transcription' in col.lower() or 'text' in col.lower():
                        text_col = col
                        break
                
                if text_col:
                    all_text = ' '.join(df[text_col].dropna().astype(str))
                    words = all_text.lower().split()
                    print(f"    Total words: {len(words)}")
                    
                    # Check for backchannel patterns (simplified)
                    reactive_count = sum(1 for word in words if any(pattern in word for pattern in ['oui', 'ouais', 'mhm', 'mm', 'ok', 'd\'accord']))
                    emotional_count = sum(1 for word in words if any(pattern in word for pattern in ['oh', 'ah', 'wow', 'incroyable', 'génial']))
                    
                    print(f"    Potential reactive words: {reactive_count}")
                    print(f"    Potential emotional words: {emotional_count}")
                else:
                    print(f"    ❌ No transcription column found")
                    
            except Exception as e:
                print(f"    ❌ Error reading {csv_file}: {e}")
    
    return True

def verify_bc_labels_creation():
    """Verify the bc_labels file creation"""
    print("\n\n🏷️ VERIFYING BC_LABELS CREATION")
    print("=" * 50)
    
    noxi_dir = Path("noxi")
    bc_files = list(noxi_dir.glob("*-bc_labels_*.csv"))
    
    if not bc_files:
        print("❌ No bc_labels files found in noxi directory!")
        return False
    
    print(f"Found {len(bc_files)} bc_labels files")
    
    total_frames = 0
    class_distribution = Counter()
    
    for bc_file in bc_files:
        print(f"\n📊 Analyzing: {bc_file.name}")
        
        try:
            df = pd.read_csv(bc_file)
            print(f"  Shape: {df.shape}")
            print(f"  Columns: {list(df.columns)}")
            
            if 'backchannel_class' in df.columns:
                class_counts = df['backchannel_class'].value_counts().sort_index()
                print(f"  Class distribution:")
                for class_id, count in class_counts.items():
                    class_name = {0: 'no_bc', 1: 'reactive', 2: 'emotional'}[class_id]
                    percentage = (count / len(df)) * 100
                    print(f"    {class_name}: {count} ({percentage:.1f}%)")
                
                total_frames += len(df)
                for class_id, count in class_counts.items():
                    class_distribution[class_id] += count
            else:
                print("  ❌ Missing 'backchannel_class' column!")
                
        except Exception as e:
            print(f"  ❌ Error reading file: {e}")
    
    # Overall statistics
    print(f"\n📈 OVERALL STATISTICS")
    print(f"Total frames across all files: {total_frames:,}")
    print(f"Overall class distribution:")
    for class_id in sorted(class_distribution.keys()):
        count = class_distribution[class_id]
        percentage = (count / total_frames) * 100 if total_frames > 0 else 0
        class_name = {0: 'no_bc', 1: 'reactive', 2: 'emotional'}[class_id]
        print(f"  {class_name}: {count:,} ({percentage:.1f}%)")
    
    return True

def verify_frame_synchronization():
    """Verify that bc_labels are properly synchronized with other data"""
    print("\n\n⏱️ VERIFYING FRAME SYNCHRONIZATION")
    print("=" * 50)
    
    # Check if we can find corresponding audio files
    noxi_dir = Path("noxi")
    audio_files = list(noxi_dir.glob("*-audio_*.wav"))
    bc_files = list(noxi_dir.glob("*-bc_labels_*.csv"))
    
    print(f"Found {len(audio_files)} audio files")
    print(f"Found {len(bc_files)} bc_labels files")
    
    # Check for matching pairs
    matched_pairs = 0
    for bc_file in bc_files:
        session_id = bc_file.stem.replace('-bc_labels_expert', '').replace('-bc_labels_novice', '')
        speaker_type = "expert" if "expert" in bc_file.name else "novice"
        
        # Look for corresponding audio file
        audio_pattern = f"{session_id}-audio_{speaker_type}.wav"
        matching_audio = [f for f in audio_files if f.name == audio_pattern]
        
        if matching_audio:
            matched_pairs += 1
            print(f"✅ {bc_file.name} <-> {matching_audio[0].name}")
        else:
            print(f"❌ {bc_file.name} - No matching audio file found")
    
    print(f"\nMatched pairs: {matched_pairs}/{len(bc_files)}")
    return matched_pairs > 0

def verify_data_quality():
    """Verify the quality of the backchannel classification"""
    print("\n\n🔍 VERIFYING DATA QUALITY")
    print("=" * 50)
    
    noxi_dir = Path("noxi")
    bc_files = list(noxi_dir.glob("*-bc_labels_*.csv"))
    
    quality_issues = []
    
    for bc_file in bc_files:
        try:
            df = pd.read_csv(bc_file)
            
            # Check for missing values
            if df['backchannel_class'].isnull().any():
                quality_issues.append(f"{bc_file.name}: Contains null values")
            
            # Check for invalid class values
            valid_classes = {0, 1, 2}
            invalid_classes = set(df['backchannel_class'].unique()) - valid_classes
            if invalid_classes:
                quality_issues.append(f"{bc_file.name}: Invalid class values: {invalid_classes}")
            
            # Check class distribution
            class_counts = df['backchannel_class'].value_counts()
            total_frames = len(df)
            
            # Check if too many no_bc frames (potential issue)
            no_bc_ratio = class_counts.get(0, 0) / total_frames
            if no_bc_ratio > 0.95:
                quality_issues.append(f"{bc_file.name}: Very high no_bc ratio ({no_bc_ratio:.1%})")
            
            # Check if too few backchannel frames
            bc_ratio = (class_counts.get(1, 0) + class_counts.get(2, 0)) / total_frames
            if bc_ratio < 0.01:
                quality_issues.append(f"{bc_file.name}: Very low backchannel ratio ({bc_ratio:.1%})")
                
        except Exception as e:
            quality_issues.append(f"{bc_file.name}: Error reading file - {e}")
    
    if quality_issues:
        print("⚠️ QUALITY ISSUES FOUND:")
        for issue in quality_issues:
            print(f"  - {issue}")
    else:
        print("✅ No quality issues found!")
    
    return len(quality_issues) == 0

def generate_sample_analysis():
    """Generate a sample analysis of a few files"""
    print("\n\n📋 GENERATING SAMPLE ANALYSIS")
    print("=" * 50)
    
    noxi_dir = Path("noxi")
    bc_files = list(noxi_dir.glob("*-bc_labels_*.csv"))[:3]  # Analyze first 3 files
    
    for bc_file in bc_files:
        print(f"\n📊 DETAILED ANALYSIS: {bc_file.name}")
        print("-" * 40)
        
        try:
            df = pd.read_csv(bc_file)
            
            # Show first few rows
            print("First 10 rows:")
            print(df.head(10).to_string())
            
            # Show class distribution
            class_counts = df['backchannel_class'].value_counts().sort_index()
            print(f"\nClass distribution:")
            for class_id, count in class_counts.items():
                class_name = {0: 'no_bc', 1: 'reactive', 2: 'emotional'}[class_id]
                percentage = (count / len(df)) * 100
                print(f"  {class_name}: {count} ({percentage:.1f}%)")
            
            # Show some examples of each class
            for class_id in [0, 1, 2]:
                class_name = {0: 'no_bc', 1: 'reactive', 2: 'emotional'}[class_id]
                class_samples = df[df['backchannel_class'] == class_id].head(3)
                if not class_samples.empty:
                    print(f"\nSample {class_name} frames:")
                    print(class_samples.to_string())
                    
        except Exception as e:
            print(f"Error analyzing {bc_file.name}: {e}")

def main():
    """Main verification function"""
    print("🚀 BACKCHANNEL PROCESS VERIFICATION")
    print("=" * 60)
    
    # Run all verification steps
    steps = [
        ("Transcript Processing", verify_transcript_processing),
        ("BC Labels Creation", verify_bc_labels_creation),
        ("Frame Synchronization", verify_frame_synchronization),
        ("Data Quality", verify_data_quality),
    ]
    
    results = {}
    for step_name, step_func in steps:
        print(f"\n{'='*60}")
        print(f"STEP: {step_name}")
        print('='*60)
        results[step_name] = step_func()
    
    # Generate sample analysis
    generate_sample_analysis()
    
    # Summary
    print(f"\n\n📊 VERIFICATION SUMMARY")
    print("=" * 30)
    for step_name, success in results.items():
        status = "✅ PASS" if success else "❌ FAIL"
        print(f"{step_name:20}: {status}")
    
    all_passed = all(results.values())
    if all_passed:
        print(f"\n🎉 ALL VERIFICATIONS PASSED!")
        print("Your backchannel classification process appears to be working correctly.")
    else:
        print(f"\n⚠️ SOME VERIFICATIONS FAILED!")
        print("Please review the issues above before proceeding with training.")

if __name__ == "__main__":
    main()
