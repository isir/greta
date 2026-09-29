#!/usr/bin/env python3
"""
Standalone VAP Results Visualizer

This script loads saved inference results and creates visualizations
without re-running the inference. Useful for re-plotting or analyzing
previously computed results.

Usage:
    python visualize_results.py --results_path output/vap_analysis_results.npy
    python visualize_results.py --results_path output/vap_analysis_results.npy --output_dir custom_output
"""

import argparse
import numpy as np
import matplotlib.pyplot as plt
import matplotlib.patches as patches
from pathlib import Path
from typing import Dict, Tuple, Optional, List
import sys
import os
import torch
from sklearn.metrics import f1_score, accuracy_score, precision_score, recall_score, confusion_matrix

# Add the current directory to Python path to import model classes
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

# Import the VAPVisualizer from offline_inference to avoid code duplication
from offline_inference import VAPVisualizer

class VAPResultsVisualizer:
    """
    Standalone visualization system for VAP inference results
    """
    
    def __init__(self, figsize: Tuple[int, int] = (15, 10)):
        """
        Initialize the visualizer
        
        Args:
            figsize: Figure size for plots
        """
        self.figsize = figsize
        # Use the main VAPVisualizer for plotting
        self.visualizer = VAPVisualizer(figsize=figsize)
    
    def load_results(self, results_path: str) -> Dict:
        """
        Load inference results from saved file
        
        Args:
            results_path: Path to saved results .npy file
            
        Returns:
            Dictionary containing inference results
        """
        print(f"📂 Loading results from: {results_path}")
        
        if not Path(results_path).exists():
            raise FileNotFoundError(f"Results file not found: {results_path}")
        
        # Load results
        results = np.load(results_path, allow_pickle=True).item()
        
        print(f"✅ Results loaded successfully!")
        print(f"   Duration: {results['duration']:.2f}s")
        print(f"   Sample rate: {results['sample_rate']}")
        print(f"   Frame rate: {results['frame_hz']} Hz")
        print(f"   Total frames: {len(results['time_axis'])}")
        
        # Check available predictions
        if results.get('bc_predictions') is not None:
            print(f"   BC predictions: ✅ Available")
        if results.get('hn_predictions') is not None:
            print(f"   Head nod predictions: ✅ Available")
        if results.get('multimodal_used'):
            print(f"   Multimodal data: ✅ Used")
        
        return results
    
    def plot_audio_and_vap(self, results: Dict, save_path: Optional[str] = None,
                          bc_visualization: str = 'category', hn_visualization: str = 'category') -> None:
        """
        Create comprehensive visualization of VAP results
        Delegates to the main VAPVisualizer for consistent visualization
        
        Args:
            results: Dictionary containing inference results
            save_path: Optional path to save the plot
            bc_visualization: 'probability' or 'category' for BC visualization mode
            hn_visualization: 'probability' or 'category' for head nod visualization mode
        """
        # Delegate to the main VAPVisualizer which has the updated separated plot structure
        self.visualizer.plot_audio_and_vap(results, save_path, bc_visualization, hn_visualization)
    
    def compute_metrics(self, results: Dict) -> Dict[str, Dict[str, float]]:
        """
        Compute evaluation metrics (F1, Accuracy, Precision, Recall) for BC and HN predictions
        against ground truth if available.
        
        Args:
            results: Dictionary containing inference results and ground truth
            
        Returns:
            Dictionary with metrics for BC and HN (expert only)
        """
        print("⚠️  Ground truth metrics computation is currently disabled.")
        return {}
        # The original implementation has been commented out temporarily:
        # metrics = {}
        # 
        # if 'ground_truth' not in results:
        #     print("⚠️  No ground truth available for metrics computation")
        #     return metrics
        # 
        # gt = results['ground_truth']
        # 
        # ... (metrics computation code)

    
    def print_metrics(self, metrics: Dict[str, Dict[str, float]]) -> None:
        """
        Print evaluation metrics in a formatted table
        
        Args:
            metrics: Dictionary with metrics computed by compute_metrics()
        """
        if not metrics:
            print("⚠️  No metrics to display")
            return
        
        print("\n" + "="*80)
        print("📊 EVALUATION METRICS")
        print("="*80)
        
        # BC Expert Metrics
        if 'bc_expert' in metrics:
            m = metrics['bc_expert']
            print("\n🎯 Backchannel - Expert Speaker")
            print(f"   Samples: {m['n_samples']} BC frames")
            print(f"   Accuracy:     {m['accuracy']:.4f}")
            print(f"   F1 (macro):   {m['f1_macro']:.4f}")
            print(f"   F1 (weighted):{m['f1_weighted']:.4f}")
            print(f"   F1 Reactive:  {m['f1_reactive']:.4f}")
            print(f"   F1 Emotional: {m['f1_emotional']:.4f}")
            print(f"   Precision:    {m['precision']:.4f}")
            print(f"   Recall:       {m['recall']:.4f}")
            print(f"   Confusion Matrix:")
            print(f"                Pred Reactive  Pred Emotional")
            print(f"   GT Reactive   {m['confusion_matrix'][0,0]:>4}         {m['confusion_matrix'][0,1]:>4}")
            print(f"   GT Emotional  {m['confusion_matrix'][1,0]:>4}         {m['confusion_matrix'][1,1]:>4}")
        
        # HN Expert Metrics
        if 'hn_expert' in metrics:
            m = metrics['hn_expert']
            print("\n🎯 Head Nod - Expert Speaker")
            print(f"   Samples: {m['n_samples']} HN frames")
            print(f"   Accuracy:     {m['accuracy']:.4f}")
            print(f"   F1 (macro):   {m['f1_macro']:.4f}")
            print(f"   F1 (weighted):{m['f1_weighted']:.4f}")
            print(f"   F1 Short:     {m['f1_short']:.4f}")
            print(f"   F1 Long:      {m['f1_long']:.4f}")
            print(f"   F1 Long+:     {m['f1_long_p']:.4f}")
            print(f"   Precision:    {m['precision']:.4f}")
            print(f"   Recall:       {m['recall']:.4f}")
            print(f"   Confusion Matrix:")
            print(f"              Pred Short  Pred Long  Pred Long+")
            print(f"   GT Short   {m['confusion_matrix'][0,0]:>4}       {m['confusion_matrix'][0,1]:>4}      {m['confusion_matrix'][0,2]:>4}")
            print(f"   GT Long    {m['confusion_matrix'][1,0]:>4}       {m['confusion_matrix'][1,1]:>4}      {m['confusion_matrix'][1,2]:>4}")
            print(f"   GT Long+   {m['confusion_matrix'][2,0]:>4}       {m['confusion_matrix'][2,1]:>4}      {m['confusion_matrix'][2,2]:>4}")
        
        print("\n" + "="*80 + "\n")
    
    def create_animation(self, results: Dict, output_path: str, fps: int = 10) -> None:
        """
        Create animated visualization (placeholder for future implementation)
        
        Args:
            results: Dictionary containing inference results
            output_path: Path to save animation
            fps: Frames per second for animation
        """
        print(f"🎬 Animation creation not yet implemented")
        print(f"   Use the animated_visualization.py script for animations")


def main():
    """Main function for standalone visualization"""
    parser = argparse.ArgumentParser(description='Visualize VAP inference results')
    
    # Required arguments
    parser.add_argument('--results_path', type=str, required=True,
                       help='Path to saved inference results (.npy file)')
    
    # Optional arguments
    parser.add_argument('--output_dir', type=str, default='output',
                       help='Output directory for visualizations')
    parser.add_argument('--save_plot', action='store_true',
                       help='Save the plot to file')
    parser.add_argument('--figsize', type=int, nargs=2, default=[15, 10],
                       help='Figure size [width, height]')
    parser.add_argument('--bc_visualization', type=str, default='category', choices=['probability', 'category'],
                       help='BC visualization mode: probability (show prob curves) or category (show predicted classes)')
    parser.add_argument('--hn_visualization', type=str, default='category', choices=['probability', 'category'],
                       help='Head nod visualization mode: probability (show prob curves) or category (show predicted classes)')
    
    args = parser.parse_args()
    
    # Create output directory
    output_dir = Path(args.output_dir)
    output_dir.mkdir(exist_ok=True)
    
    # Initialize visualizer
    visualizer = VAPResultsVisualizer(figsize=tuple(args.figsize))
    
    try:
        # Load results
        results = visualizer.load_results(args.results_path)
        
        # Generate output filename
        results_name = Path(args.results_path).stem
        if 'results' in results_name:
            plot_name = results_name.replace('_results', '_visualization')
        else:
            plot_name = f"{results_name}_visualization"
        
        plot_path = output_dir / f"{plot_name}.png" if args.save_plot else None
        
        # # Compute and print metrics if ground truth is available
        # if 'ground_truth' in results:
        #     print(f"\n📊 Computing evaluation metrics...")
        #     metrics = visualizer.compute_metrics(results)
        #     visualizer.print_metrics(metrics)
        
        # Create visualization
        visualizer.plot_audio_and_vap(results, plot_path,
                                     bc_visualization=args.bc_visualization,
                                     hn_visualization=args.hn_visualization)
        
        print(f"✅ Visualization completed successfully!")
        
    except Exception as e:
        print(f"❌ Error during visualization: {e}")
        return 1
    
    return 0


if __name__ == "__main__":
    exit(main())
