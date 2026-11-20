#!/usr/bin/env python3
"""
Backchannel Cache Inspector
Inspect and analyze the .pt files created in tmp_cache/ by the French backchannel processor
"""

import torch
import os
import glob
import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
from pathlib import Path

def inspect_backchannel_file(pt_file_path: str):
    """
    Inspect a single backchannel .pt file
    
    Args:
        pt_file_path: Path to the .pt file
    """
    
    print(f"\n🔍 Inspecting: {os.path.basename(pt_file_path)}")
    print("-" * 60)
    
    try:
        # Load the tensor
        bc_labels = torch.load(pt_file_path)
        
        # Basic info
        print(f"📊 Basic Information:")
        print(f"   Type: {type(bc_labels)}")
        print(f"   Shape: {bc_labels.shape}")
        print(f"   Dtype: {bc_labels.dtype}")
        print(f"   Device: {bc_labels.device}")
        
        # Content analysis
        unique_labels, counts = bc_labels.unique(return_counts=True)
        total_frames = len(bc_labels)
        
        print(f"\n📈 Content Analysis:")
        print(f"   Total frames: {total_frames:,}")
        print(f"   Duration (at 50Hz): {total_frames/50:.2f} seconds")
        
        print(f"\n🏷️ Label Distribution:")
        label_names = {0: "no_backchannel", 1: "reactive", 2: "emotional"}
        
        for label, count in zip(unique_labels.tolist(), counts.tolist()):
            percentage = count / total_frames * 100
            label_name = label_names.get(label, f"unknown_{label}")
            print(f"   {label} ({label_name}): {count:,} frames ({percentage:.2f}%)")
        
        # Timing analysis
        backchannel_frames = bc_labels > 0
        if backchannel_frames.any():
            # Find backchannel segments
            bc_starts = []
            bc_ends = []
            bc_types = []
            
            in_bc = False
            start_idx = 0
            current_type = 0
            
            for i, is_bc in enumerate(backchannel_frames):
                if is_bc and not in_bc:
                    # Start of backchannel
                    start_idx = i
                    current_type = bc_labels[i].item()
                    in_bc = True
                elif not is_bc and in_bc:
                    # End of backchannel
                    bc_starts.append(start_idx / 50.0)  # Convert to seconds
                    bc_ends.append(i / 50.0)
                    bc_types.append(current_type)
                    in_bc = False
            
            # Handle case where backchannel extends to end
            if in_bc:
                bc_starts.append(start_idx / 50.0)
                bc_ends.append(len(bc_labels) / 50.0)
                bc_types.append(current_type)
            
            print(f"\n⏱️ Backchannel Segments:")
            print(f"   Number of segments: {len(bc_starts)}")
            
            if bc_starts:
                durations = [end - start for start, end in zip(bc_starts, bc_ends)]
                print(f"   Average duration: {np.mean(durations):.3f}s")
                print(f"   Duration range: {np.min(durations):.3f}s - {np.max(durations):.3f}s")
                
                print(f"\n📋 Individual Segments:")
                for i, (start, end, bc_type) in enumerate(zip(bc_starts, bc_ends, bc_types)):
                    duration = end - start
                    type_name = label_names.get(bc_type, f"unknown_{bc_type}")
                    print(f"   {i+1:2d}. {start:6.2f}s - {end:6.2f}s ({duration:.3f}s) - {type_name}")
                    if i >= 9:  # Limit to first 10 segments
                        remaining = len(bc_starts) - 10
                        if remaining > 0:
                            print(f"   ... and {remaining} more segments")
                        break
        
        else:
            print(f"\n⏱️ No backchannels detected in this segment")
        
        # Sample values
        print(f"\n🔢 Sample Values (first 20 frames):")
        sample_values = bc_labels[:20].tolist()
        print(f"   {sample_values}")
        
        return {
            'file': os.path.basename(pt_file_path),
            'total_frames': total_frames,
            'duration_seconds': total_frames / 50.0,
            'no_bc_frames': counts[unique_labels == 0].item() if 0 in unique_labels else 0,
            'reactive_frames': counts[unique_labels == 1].item() if 1 in unique_labels else 0,
            'emotional_frames': counts[unique_labels == 2].item() if 2 in unique_labels else 0,
            'total_bc_frames': (bc_labels > 0).sum().item(),
            'num_segments': len(bc_starts) if 'bc_starts' in locals() else 0
        }
        
    except Exception as e:
        print(f"❌ Error inspecting {pt_file_path}: {e}")
        return None

def visualize_backchannel_file(pt_file_path: str, save_plot: bool = True):
    """
    Create a visualization of backchannel labels over time
    
    Args:
        pt_file_path: Path to the .pt file
        save_plot: Whether to save the plot as an image
    """
    
    try:
        bc_labels = torch.load(pt_file_path)
        
        # Convert to numpy for plotting
        labels_np = bc_labels.numpy()
        time_axis = np.arange(len(labels_np)) / 50.0  # Convert frames to seconds
        
        # Create the plot
        fig, (ax1, ax2) = plt.subplots(2, 1, figsize=(15, 8))
        
        # Plot 1: Raw label timeline
        colors = ['gray', 'green', 'red']
        color_map = [colors[label] for label in labels_np]
        
        ax1.scatter(time_axis, labels_np, c=color_map, alpha=0.7, s=2)
        ax1.set_ylabel('Backchannel Type')
        ax1.set_yticks([0, 1, 2])
        ax1.set_yticklabels(['No BC', 'Reactive', 'Emotional'])
        ax1.set_title(f'Backchannel Labels Timeline: {os.path.basename(pt_file_path)}')
        ax1.grid(True, alpha=0.3)
        
        # Plot 2: Density over time (sliding window)
        window_size = 50  # 1 second window at 50Hz
        if len(labels_np) > window_size:
            density = []
            time_windows = []
            
            for i in range(0, len(labels_np) - window_size, window_size // 2):
                window = labels_np[i:i + window_size]
                bc_density = (window > 0).sum() / len(window)
                density.append(bc_density)
                time_windows.append((i + window_size // 2) / 50.0)
            
            ax2.plot(time_windows, density, 'b-', linewidth=2)
            ax2.fill_between(time_windows, density, alpha=0.3)
            ax2.set_ylabel('BC Density')
            ax2.set_xlabel('Time (seconds)')
            ax2.set_title('Backchannel Density (1-second sliding window)')
            ax2.grid(True, alpha=0.3)
        else:
            ax2.text(0.5, 0.5, 'Segment too short for density analysis', 
                    ha='center', va='center', transform=ax2.transAxes)
            ax2.set_xlabel('Time (seconds)')
        
        plt.tight_layout()
        
        if save_plot:
            plot_name = f"bc_analysis_{os.path.basename(pt_file_path).replace('.pt', '.png')}"
            plt.savefig(plot_name, dpi=150, bbox_inches='tight')
            print(f"📊 Plot saved: {plot_name}")
        
        plt.show()
        
    except Exception as e:
        print(f"❌ Error visualizing {pt_file_path}: {e}")

def analyze_cache_directory(cache_dir: str = "tmp_cache"):
    """
    Analyze all backchannel .pt files in the cache directory
    
    Args:
        cache_dir: Path to the cache directory
    """
    
    print("🔍 ANALYSEUR DE CACHE BACKCHANNELS / BACKCHANNEL CACHE ANALYZER")
    print("=" * 80)
    
    # Find all backchannel .pt files
    bc_pattern = os.path.join(cache_dir, "*_bc_labels.pt")
    bc_files = glob.glob(bc_pattern)
    
    if not bc_files:
        print(f"❌ No backchannel files found in {cache_dir}")
        print(f"   Pattern searched: {bc_pattern}")
        
        # Show what files are actually there
        all_files = glob.glob(os.path.join(cache_dir, "*.pt"))
        if all_files:
            print(f"   Found {len(all_files)} other .pt files:")
            for f in all_files[:5]:  # Show first 5
                print(f"      {os.path.basename(f)}")
            if len(all_files) > 5:
                print(f"      ... and {len(all_files) - 5} more")
        return
    
    print(f"📁 Found {len(bc_files)} backchannel files in {cache_dir}")
    print(f"📊 Analyzing content...")
    
    # Analyze all files
    results = []
    for i, bc_file in enumerate(bc_files[:10]):  # Limit to first 10 for detailed analysis
        print(f"\n{'='*60}")
        print(f"File {i+1}/{min(10, len(bc_files))}")
        result = inspect_backchannel_file(bc_file)
        if result:
            results.append(result)
    
    if len(bc_files) > 10:
        print(f"\n... and {len(bc_files) - 10} more files (not shown in detail)")
    
    # Summary statistics
    if results:
        print(f"\n📊 RÉSUMÉ / SUMMARY")
        print("=" * 40)
        
        total_frames = sum(r['total_frames'] for r in results)
        total_duration = sum(r['duration_seconds'] for r in results)
        total_bc_frames = sum(r['total_bc_frames'] for r in results)
        total_reactive = sum(r['reactive_frames'] for r in results)
        total_emotional = sum(r['emotional_frames'] for r in results)
        total_segments = sum(r['num_segments'] for r in results)
        
        print(f"Files analyzed: {len(results)}")
        print(f"Total duration: {total_duration:.1f} seconds ({total_duration/60:.1f} minutes)")
        print(f"Total frames: {total_frames:,}")
        print(f"Backchannel frames: {total_bc_frames:,} ({total_bc_frames/total_frames*100:.2f}%)")
        print(f"Reactive frames: {total_reactive:,} ({total_reactive/total_frames*100:.2f}%)")
        print(f"Emotional frames: {total_emotional:,} ({total_emotional/total_frames*100:.2f}%)")
        print(f"Total BC segments: {total_segments}")
        
        if total_segments > 0:
            avg_segment_duration = total_bc_frames / total_segments / 50.0
            print(f"Average segment duration: {avg_segment_duration:.3f} seconds")
    
    # Offer to visualize a sample
    if bc_files:
        print(f"\n🎨 Visualization Option:")
        print(f"To visualize a specific file, run:")
        print(f"visualize_backchannel_file('{bc_files[0]}')")

def main():
    """Main function for cache analysis"""
    
    cache_dir = "tmp_cache"
    
    print("🔍 What do you want to analyze?")
    print("1. Analyze all backchannel files in cache")
    print("2. Inspect a specific file")
    print("3. Visualize a specific file")
    
    choice = input("\nEnter your choice (1-3): ").strip()
    
    if choice == "1":
        analyze_cache_directory(cache_dir)
    
    elif choice == "2":
        bc_files = glob.glob(os.path.join(cache_dir, "*_bc_labels.pt"))
        if bc_files:
            print(f"\nAvailable files:")
            for i, f in enumerate(bc_files[:10]):
                print(f"{i+1:2d}. {os.path.basename(f)}")
            
            try:
                file_idx = int(input(f"\nSelect file (1-{min(10, len(bc_files))}): ")) - 1
                if 0 <= file_idx < len(bc_files):
                    inspect_backchannel_file(bc_files[file_idx])
                else:
                    print("Invalid selection")
            except ValueError:
                print("Invalid input")
        else:
            print(f"No backchannel files found in {cache_dir}")
    
    elif choice == "3":
        bc_files = glob.glob(os.path.join(cache_dir, "*_bc_labels.pt"))
        if bc_files:
            print(f"\nAvailable files:")
            for i, f in enumerate(bc_files[:10]):
                print(f"{i+1:2d}. {os.path.basename(f)}")
            
            try:
                file_idx = int(input(f"\nSelect file (1-{min(10, len(bc_files))}): ")) - 1
                if 0 <= file_idx < len(bc_files):
                    visualize_backchannel_file(bc_files[file_idx])
                else:
                    print("Invalid selection")
            except ValueError:
                print("Invalid input")
        else:
            print(f"No backchannel files found in {cache_dir}")
    
    else:
        print("Invalid choice. Run the script again.")

if __name__ == "__main__":
    # You can also call functions directly:
    
    # Analyze all files
    analyze_cache_directory("tmp_cache")
    
    # Or uncomment to run interactive mode:
    # main()

