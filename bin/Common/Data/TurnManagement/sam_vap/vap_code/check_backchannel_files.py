#!/usr/bin/env python3
"""
Check Backchannel Files Script

This script checks what backchannel-related files exist in your project
and shows the file structure for verification.

Usage:
    python check_backchannel_files.py
"""

from pathlib import Path
import os

def check_file_structure():
    """Check the file structure for backchannel processing"""
    print("🔍 CHECKING BACKCHANNEL FILE STRUCTURE")
    print("=" * 50)
    
    # Check Transcripts directory
    transcripts_dir = Path("Transcripts")
    if transcripts_dir.exists():
        transcript_files = list(transcripts_dir.glob("*.txt"))
        print(f"✅ Transcripts directory found with {len(transcript_files)} files")
        for f in transcript_files[:5]:  # Show first 5
            print(f"  - {f.name}")
        if len(transcript_files) > 5:
            print(f"  ... and {len(transcript_files) - 5} more")
    else:
        print("❌ Transcripts directory not found!")
    
    # Check noxi directory for bc_labels files
    noxi_dir = Path("noxi")
    if noxi_dir.exists():
        bc_files = list(noxi_dir.glob("*-bc_labels_*.csv"))
        print(f"\n✅ Noxi directory found with {len(bc_files)} bc_labels files")
        for f in bc_files[:5]:  # Show first 5
            print(f"  - {f.name}")
        if len(bc_files) > 5:
            print(f"  ... and {len(bc_files) - 5} more")
    else:
        print("❌ Noxi directory not found!")
    
    # Check for main CSV files
    main_csvs = ["train_paris_extended.csv", "valid_paris_extended.csv", "test_paris_extended.csv"]
    print(f"\n📊 Main CSV files:")
    for csv_file in main_csvs:
        if Path(csv_file).exists():
            print(f"  ✅ {csv_file}")
        else:
            print(f"  ❌ {csv_file} - NOT FOUND")
    
    # Check for backchannel processor script
    processor_script = Path("french_backchannel_processor.py")
    if processor_script.exists():
        print(f"\n✅ Backchannel processor script found: {processor_script}")
    else:
        print(f"\n❌ Backchannel processor script not found: {processor_script}")

def show_expected_structure():
    """Show the expected file structure"""
    print(f"\n\n📁 EXPECTED FILE STRUCTURE")
    print("=" * 40)
    print("""
Transcripts/
├── pair01/
│   ├── expert.csv
│   └── novice.csv
├── pair02/
│   ├── expert.csv
│   └── novice.csv
├── pair03/
│   ├── expert.csv
│   └── novice.csv
└── ... (more pair directories)

noxi/
├── Paris_01-bc_labels_expert.csv
├── Paris_01-bc_labels_novice.csv
├── Paris_02-bc_labels_expert.csv
├── Paris_02-bc_labels_novice.csv
└── ... (more bc_labels files)

Root directory:
├── train_paris_extended.csv (with backchannel_labels_path column)
├── valid_paris_extended.csv (with backchannel_labels_path column)
├── test_paris_extended.csv (with backchannel_labels_path column)
└── french_backchannel_processor.py
""")

def check_csv_columns():
    """Check if CSV files have the required backchannel columns"""
    print(f"\n\n📋 CHECKING CSV COLUMNS")
    print("=" * 30)
    
    import pandas as pd
    
    csv_files = ["train_paris_extended.csv", "valid_paris_extended.csv", "test_paris_extended.csv"]
    
    for csv_file in csv_files:
        if Path(csv_file).exists():
            try:
                df = pd.read_csv(csv_file)
                print(f"\n📊 {csv_file}:")
                print(f"  Shape: {df.shape}")
                print(f"  Columns: {list(df.columns)}")
                
                # Check for backchannel-related columns
                bc_columns = [col for col in df.columns if 'backchannel' in col.lower()]
                if bc_columns:
                    print(f"  ✅ Backchannel columns found: {bc_columns}")
                else:
                    print(f"  ❌ No backchannel columns found!")
                    
            except Exception as e:
                print(f"  ❌ Error reading {csv_file}: {e}")
        else:
            print(f"\n❌ {csv_file} not found!")

def main():
    """Main function"""
    check_file_structure()
    show_expected_structure()
    check_csv_columns()
    
    print(f"\n\n🎯 NEXT STEPS")
    print("=" * 20)
    print("1. Run: python verify_backchannel_process.py")
    print("2. Run: python analyze_backchannel_classification.py")
    print("3. Check the generated analysis files in backchannel_analysis/")

if __name__ == "__main__":
    main()
