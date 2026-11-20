#!/usr/bin/env python3
"""
Quick VAP Results Visualizer

Simple script to quickly visualize VAP inference results.
Just provide the results file path and it will create the visualization.

Usage:
    python quick_visualize.py output/vap_analysis_Paris_06-audio_expert_vs_Paris_06-audio_novice_results.npy
"""

import sys
from pathlib import Path
from visualize_results import VAPResultsVisualizer

def main():
    if len(sys.argv) != 2:
        print("Usage: python quick_visualize.py <results_file.npy>")
        print("Example: python quick_visualize.py output/vap_analysis_results.npy")
        return 1
    
    results_path = sys.argv[1]
    
    if not Path(results_path).exists():
        print(f"❌ Results file not found: {results_path}")
        return 1
    
    print(f"🎨 Quick VAP Results Visualizer")
    print(f"📂 Loading: {results_path}")
    
    try:
        # Initialize visualizer
        visualizer = VAPResultsVisualizer()
        
        # Load and visualize results
        results = visualizer.load_results(results_path)
        visualizer.plot_audio_and_vap(results)
        
        print(f"✅ Visualization completed!")
        
    except Exception as e:
        print(f"❌ Error: {e}")
        return 1
    
    return 0

if __name__ == "__main__":
    exit(main())

