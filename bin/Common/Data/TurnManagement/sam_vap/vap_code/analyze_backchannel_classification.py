#!/usr/bin/env python3
"""
Backchannel Classification Analysis Script

This script analyzes the backchannel classification process from raw transcripts
to final bc_labels files, providing detailed verification and statistics.

Usage:
    python analyze_backchannel_classification.py
"""

import os
import pandas as pd
import numpy as np
import torch
from pathlib import Path
import matplotlib.pyplot as plt
import seaborn as sns
from collections import Counter, defaultdict
import re
from typing import Dict, List, Tuple, Optional

# Set up paths
TRANSCRIPTS_DIR = Path("Transcripts")
NOXI_DIR = Path("noxi")
OUTPUT_DIR = Path("backchannel_analysis")
OUTPUT_DIR.mkdir(exist_ok=True)

# French backchannel word lists (from french_backchannel_processor.py)
REACTIVE_PATTERNS = [
    # Basic acknowledgments
    r'\bmm?-?hmm?\b', r'\beuh\b', r'\bouais?\b', r'\boui\b',
    r'\bd\'accord\b', r'\bokay?\b', r'\bok\b', r'\bmm\b',
    r'\bexactement\b', r'\bvoilà\b', r'\bc\'est ça\b',
    r'\btout à fait\b', r'\bbien sûr\b', r'\beffectivement\b',
    
    # Agreement particles
    r'\bah oui\b', r'\bah ouais\b', r'\bje vois\b',
    r'\bc\'est vrai\b', r'\bc\'est sûr\b', r'\bbien\b',
    r'\bparfait\b', r'\btrès bien\b', r'\bentends?\b',
    
    # Minimal responses
    r'\bmhm\b', r'\bmm\b', r'\buh-?huh\b', r'\byeah?\b',
    r'\bcompris\b', r'\bd\'acc?\b', r'\bvrai\b',
]

EMOTIONAL_PATTERNS = [
    # Surprise and amazement
    r'\boh\b', r'\bah\b', r'\bwah?\b', r'\bwaou?h?\b',
    r'\boh là là\b', r'\boh mon dieu\b', r'\bmon dieu\b',
    r'\bputain\b', r'\bmerde\b', r'\bfuck\b',
    
    # Interest and engagement
    r'\bvraiment\?\b', r'\bsérieux\?\b', r'\bc\'est fou\b',
    r'\bc\'est dingue\b', r'\bc\'est incroyable\b',
    r'\bpas possible\b', r'\btu déconnes?\b',
    
    # Strong reactions
    r'\boh putain\b', r'\boh merde\b', r'\bah merde\b',
    r'\bc\'est génial\b', r'\bc\'est super\b', r'\bc\'est top\b',
    r'\bincroyable\b', r'\bfantastique\b', r'\bextraordinaire\b',
    
    # Questions of disbelief
    r'\bvraiment\?\!\b', r'\bc\'est vrai\?\b', r'\btu rigoles?\b',
    r'\bsans blague\?\b', r'\bj\'y crois pas\b',
    
    # Emotional interjections
    r'\boh non\b', r'\bah bon\b', r'\bah ouah\b',
    r'\boh si\b', r'\bah si\b', r'\boh bah\b',
]

# Pair to Paris mapping
PAIR_TO_PARIS = {
    1: "Paris_01", 2: "Paris_02", 3: "Paris_03", 4: "Paris_04", 5: "Paris_05",
    6: "Paris_06", 7: "Paris_07", 8: "Paris_08", 9: "Paris_09", 10: "Paris_10",
    11: "Paris_11", 12: "Paris_12", 13: "Paris_13", 14: "Paris_14", 15: "Paris_15",
    16: "Paris_16", 17: "Paris_17", 18: "Paris_18", 19: "Paris_19", 20: "Paris_20",
    21: "Paris_21", 22: "Paris_22", 23: "Paris_23", 24: "Paris_24", 25: "Paris_25"
}

class BackchannelAnalyzer:
    def __init__(self):
        self.stats = {
            'total_sessions': 0,
            'total_frames': 0,
            'class_distribution': Counter(),
            'session_stats': {},
            'word_matches': defaultdict(list),
            'classification_errors': []
        }
        
    def analyze_transcript_files(self):
        """Analyze raw transcript files and their backchannel content"""
        print("🔍 ANALYZING TRANSCRIPT FILES")
        print("=" * 50)
        
        # Look for pair directories
        pair_dirs = list(TRANSCRIPTS_DIR.glob("pair*"))
        print(f"Found {len(pair_dirs)} pair directories")
        
        for pair_dir in sorted(pair_dirs):
            print(f"\n📁 Processing: {pair_dir.name}")
            self._analyze_single_pair(pair_dir)
    
    def _analyze_single_pair(self, pair_dir: Path):
        """Analyze a single pair directory for backchannel content"""
        try:
            # Extract pair number
            pair_name = pair_dir.name
            pair_number = int(pair_name.replace('pair', ''))
            paris_session = PAIR_TO_PARIS.get(pair_number, f"Paris_{pair_number:02d}")
            
            # Check for expert and novice CSV files
            expert_file = pair_dir / "expert.csv"
            novice_file = pair_dir / "novice.csv"
            
            if not expert_file.exists() or not novice_file.exists():
                print(f"  ❌ Missing transcript files in {pair_dir}")
                return
            
            # Analyze both speakers
            expert_data = self._analyze_transcript_csv(expert_file, "expert", paris_session)
            novice_data = self._analyze_transcript_csv(novice_file, "novice", paris_session)
            
            # Store combined session statistics
            self.stats['session_stats'][paris_session] = {
                'pair_number': pair_number,
                'expert': expert_data,
                'novice': novice_data
            }
            
            print(f"  Pair: {pair_number} -> {paris_session}")
            print(f"  Expert - Words: {expert_data['total_words']}, Reactive: {expert_data['reactive_matches']}, Emotional: {expert_data['emotional_matches']}")
            print(f"  Novice - Words: {novice_data['total_words']}, Reactive: {novice_data['reactive_matches']}, Emotional: {novice_data['emotional_matches']}")
                
        except Exception as e:
            print(f"  ❌ Error processing {pair_dir}: {e}")
    
    def _analyze_transcript_csv(self, csv_file: Path, speaker_type: str, session_id: str):
        """Analyze a single transcript CSV file for backchannel content"""
        try:
            import pandas as pd
            
            df = pd.read_csv(csv_file)
            
            # Find the transcription column
            text_col = None
            for col in df.columns:
                if 'transcription' in col.lower() or 'text' in col.lower():
                    text_col = col
                    break
            
            if text_col is None:
                print(f"  ❌ No transcription column found in {csv_file}")
                return {'total_words': 0, 'reactive_matches': 0, 'emotional_matches': 0, 'matches': []}
            
            # Combine all text
            all_text = ' '.join(df[text_col].dropna().astype(str))
            words = all_text.lower().split()
            word_count = len(words)
            
            # Find backchannel matches using regex patterns
            reactive_matches = []
            emotional_matches = []
            
            # Compile regex patterns
            reactive_regex = re.compile('|'.join(REACTIVE_PATTERNS), re.IGNORECASE)
            emotional_regex = re.compile('|'.join(EMOTIONAL_PATTERNS), re.IGNORECASE)
            
            # Find matches in the full text
            for match in reactive_regex.finditer(all_text):
                reactive_matches.append({
                    'word': match.group(),
                    'start': match.start(),
                    'end': match.end(),
                    'context': all_text[max(0, match.start()-20):match.end()+20]
                })
            
            for match in emotional_regex.finditer(all_text):
                emotional_matches.append({
                    'word': match.group(),
                    'start': match.start(),
                    'end': match.end(),
                    'context': all_text[max(0, match.start()-20):match.end()+20]
                })
            
            return {
                'total_words': word_count,
                'reactive_matches': len(reactive_matches),
                'emotional_matches': len(emotional_matches),
                'reactive_details': reactive_matches,
                'emotional_details': emotional_matches
            }
                
        except Exception as e:
            print(f"  ❌ Error processing {csv_file}: {e}")
            return {'total_words': 0, 'reactive_matches': 0, 'emotional_matches': 0, 'matches': []}
    
    def analyze_bc_labels_files(self):
        """Analyze the generated bc_labels files"""
        print("\n\n🏷️ ANALYZING BC_LABELS FILES")
        print("=" * 50)
        
        bc_files = list(NOXI_DIR.glob("*-bc_labels_*.csv"))
        print(f"Found {len(bc_files)} bc_labels files")
        
        for bc_file in bc_files:
            print(f"\n📊 Processing: {bc_file.name}")
            self._analyze_single_bc_file(bc_file)
    
    def _analyze_single_bc_file(self, bc_file: Path):
        """Analyze a single bc_labels file"""
        try:
            df = pd.read_csv(bc_file)
            
            # Extract session info
            session_id = bc_file.stem.replace('-bc_labels_expert', '').replace('-bc_labels_novice', '')
            speaker_type = "expert" if "expert" in bc_file.name else "novice"
            
            # Analyze class distribution
            class_counts = df['backchannel_class'].value_counts().sort_index()
            total_frames = len(df)
            
            # Calculate proportions
            proportions = (class_counts / total_frames * 100).round(2)
            
            print(f"  Speaker: {speaker_type}")
            print(f"  Total frames: {total_frames}")
            print(f"  Class distribution:")
            
            # Show all classes (including missing ones)
            for class_id in [0, 1, 2]:
                count = class_counts.get(class_id, 0)
                percentage = proportions.get(class_id, 0.0)
                class_name = {0: 'no_bc', 1: 'reactive', 2: 'emotional'}[class_id]
                print(f"    {class_name}: {count:,} ({percentage:.2f}%)")
            
            # Highlight if there are actual backchannels
            total_bc = class_counts.get(1, 0) + class_counts.get(2, 0)
            if total_bc > 0:
                print(f"  ✅ Found {total_bc} backchannel frames!")
            else:
                print(f"  ⚠️  No backchannel frames found")
            
            # Store statistics
            self.stats['total_sessions'] += 1
            self.stats['total_frames'] += total_frames
            for class_id, count in class_counts.items():
                self.stats['class_distribution'][class_id] += count
            
            # Store session details
            if session_id not in self.stats['session_stats']:
                self.stats['session_stats'][session_id] = {}
            self.stats['session_stats'][session_id]['bc_labels'] = {
                'total_frames': total_frames,
                'class_counts': class_counts.to_dict(),
                'proportions': proportions.to_dict()
            }
            
        except Exception as e:
            print(f"  ❌ Error processing {bc_file}: {e}")
    
    def _summarize_bc_labels_analysis(self):
        """Add summary statistics for bc_labels analysis"""
        print(f"\n📈 BC_LABELS ANALYSIS SUMMARY")
        print("=" * 40)
        
        total_frames = sum(self.stats['class_distribution'].values())
        total_files = self.stats['total_sessions']
        
        print(f"Total files analyzed: {total_files}")
        print(f"Total frames: {total_frames:,}")
        print(f"Overall class distribution:")
        
        for class_id in [0, 1, 2]:
            count = self.stats['class_distribution'].get(class_id, 0)
            percentage = (count / total_frames) * 100 if total_frames > 0 else 0
            class_name = {0: 'no_bc', 1: 'reactive', 2: 'emotional'}[class_id]
            print(f"  {class_name}: {count:,} ({percentage:.1f}%)")
        
        # Calculate files with backchannels
        files_with_bc = 0
        for session_data in self.stats['session_stats'].values():
            if 'bc_labels' in session_data:
                bc_data = session_data['bc_labels']
                total_bc = bc_data['class_counts'].get(1, 0) + bc_data['class_counts'].get(2, 0)
                if total_bc > 0:
                    files_with_bc += 1
        
        print(f"\nFiles with backchannels: {files_with_bc}/{total_files} ({files_with_bc/total_files*100:.1f}%)")
    
    def cross_validate_classification(self):
        """Cross-validate transcript analysis with bc_labels"""
        print("\n\n🔍 CROSS-VALIDATION ANALYSIS")
        print("=" * 50)
        
        for session_id, session_data in self.stats['session_stats'].items():
            if 'bc_labels' not in session_data:
                continue
                
            print(f"\n📋 Session: {session_id}")
            
            # Get transcript analysis (sum expert and novice)
            expert_data = session_data.get('expert', {})
            novice_data = session_data.get('novice', {})
            
            total_reactive = expert_data.get('reactive_matches', 0) + novice_data.get('reactive_matches', 0)
            total_emotional = expert_data.get('emotional_matches', 0) + novice_data.get('emotional_matches', 0)
            
            # Get bc_labels analysis
            bc_data = session_data.get('bc_labels', {})
            bc_reactive = bc_data.get('class_counts', {}).get(1, 0)
            bc_emotional = bc_data.get('class_counts', {}).get(2, 0)
            
            print(f"  Transcript analysis:")
            print(f"    Expert - Reactive: {expert_data.get('reactive_matches', 0)}, Emotional: {expert_data.get('emotional_matches', 0)}")
            print(f"    Novice - Reactive: {novice_data.get('reactive_matches', 0)}, Emotional: {novice_data.get('emotional_matches', 0)}")
            print(f"    Total - Reactive: {total_reactive}, Emotional: {total_emotional}")
            
            print(f"  BC labels analysis:")
            print(f"    Reactive frames: {bc_reactive}")
            print(f"    Emotional frames: {bc_emotional}")
            
            # Check for potential issues
            if total_reactive > 0 and bc_reactive == 0:
                print(f"  ⚠️  WARNING: Found reactive words in transcript but no reactive frames in bc_labels")
            if total_emotional > 0 and bc_emotional == 0:
                print(f"  ⚠️  WARNING: Found emotional words in transcript but no emotional frames in bc_labels")
            
            # Check for reasonable correlation
            if total_reactive > 0 and bc_reactive > 0:
                ratio = bc_reactive / total_reactive
                if ratio < 0.1:
                    print(f"  ⚠️  WARNING: Very low ratio of frames to words ({ratio:.2f}). This might indicate:")
                    print(f"      - Words are very short in duration")
                    print(f"      - Frame-level mapping issues")
                    print(f"      - Need to check time alignment")
    
    def generate_detailed_reports(self):
        """Generate detailed analysis reports"""
        print("\n\n📊 GENERATING DETAILED REPORTS")
        print("=" * 50)
        
        # 1. Overall statistics
        self._generate_overall_stats()
        
        # 2. Class distribution analysis
        self._generate_class_distribution_analysis()
        
        # 3. Session-by-session analysis
        self._generate_session_analysis()
        
        # 4. Word matching analysis
        self._generate_word_matching_analysis()
        
        # 5. Visualizations
        self._generate_visualizations()
    
    def _generate_overall_stats(self):
        """Generate overall statistics report"""
        report_file = OUTPUT_DIR / "overall_statistics.txt"
        
        with open(report_file, 'w', encoding='utf-8') as f:
            f.write("BACKCHANNEL CLASSIFICATION - OVERALL STATISTICS\n")
            f.write("=" * 60 + "\n\n")
            
            f.write(f"Total sessions analyzed: {self.stats['total_sessions']}\n")
            f.write(f"Total frames analyzed: {self.stats['total_frames']:,}\n\n")
            
            f.write("CLASS DISTRIBUTION:\n")
            f.write("-" * 20 + "\n")
            total_frames = sum(self.stats['class_distribution'].values())
            for class_id in sorted(self.stats['class_distribution'].keys()):
                count = self.stats['class_distribution'][class_id]
                percentage = (count / total_frames * 100) if total_frames > 0 else 0
                class_name = {0: 'no_bc', 1: 'reactive', 2: 'emotional'}[class_id]
                f.write(f"{class_name:12}: {count:8,} frames ({percentage:5.2f}%)\n")
        
        print(f"✅ Overall statistics saved to: {report_file}")
    
    def _generate_class_distribution_analysis(self):
        """Generate class distribution analysis"""
        report_file = OUTPUT_DIR / "class_distribution_analysis.txt"
        
        with open(report_file, 'w', encoding='utf-8') as f:
            f.write("CLASS DISTRIBUTION ANALYSIS\n")
            f.write("=" * 40 + "\n\n")
            
            # Calculate proportions
            total_frames = sum(self.stats['class_distribution'].values())
            no_bc_pct = (self.stats['class_distribution'][0] / total_frames * 100) if total_frames > 0 else 0
            reactive_pct = (self.stats['class_distribution'][1] / total_frames * 100) if total_frames > 0 else 0
            emotional_pct = (self.stats['class_distribution'][2] / total_frames * 100) if total_frames > 0 else 0
            
            f.write("OVERALL PROPORTIONS:\n")
            f.write(f"  No backchannel:     {no_bc_pct:5.2f}%\n")
            f.write(f"  Reactive:           {reactive_pct:5.2f}%\n")
            f.write(f"  Emotional:          {emotional_pct:5.2f}%\n\n")
            
            f.write("CLASS BALANCE ASSESSMENT:\n")
            if no_bc_pct > 90:
                f.write("  ⚠️  WARNING: Very high no_bc proportion (>90%). This may indicate:\n")
                f.write("     - Conservative classification\n")
                f.write("     - Need for more sensitive word detection\n")
                f.write("     - Possible data quality issues\n\n")
            
            if reactive_pct < 1 or emotional_pct < 1:
                f.write("  ⚠️  WARNING: Very low backchannel proportions (<1%). This may indicate:\n")
                f.write("     - Missing backchannel words in dictionary\n")
                f.write("     - Incorrect frame-level mapping\n")
                f.write("     - Need for manual verification\n\n")
            
            # Session-by-session analysis
            f.write("SESSION-BY-SESSION ANALYSIS:\n")
            f.write("-" * 30 + "\n")
            for session_id, session_data in self.stats['session_stats'].items():
                if 'bc_labels' not in session_data:
                    continue
                    
                bc_data = session_data['bc_labels']
                f.write(f"\nSession: {session_id}\n")
                f.write(f"  Total frames: {bc_data['total_frames']}\n")
                for class_id in [0, 1, 2]:
                    count = bc_data['class_counts'].get(class_id, 0)
                    pct = bc_data['proportions'].get(class_id, 0)
                    class_name = {0: 'no_bc', 1: 'reactive', 2: 'emotional'}[class_id]
                    f.write(f"  {class_name:12}: {count:4} ({pct:5.2f}%)\n")
        
        print(f"✅ Class distribution analysis saved to: {report_file}")
    
    def _generate_session_analysis(self):
        """Generate detailed session analysis"""
        report_file = OUTPUT_DIR / "session_analysis.txt"
        
        with open(report_file, 'w', encoding='utf-8') as f:
            f.write("DETAILED SESSION ANALYSIS\n")
            f.write("=" * 30 + "\n\n")
            
            for session_id, session_data in self.stats['session_stats'].items():
                f.write(f"SESSION: {session_id}\n")
                f.write("-" * 20 + "\n")
                
                # Transcript analysis
                expert_data = session_data.get('expert', {})
                novice_data = session_data.get('novice', {})
                
                if expert_data or novice_data:
                    f.write("TRANSCRIPT ANALYSIS:\n")
                    
                    if expert_data:
                        f.write("  Expert:\n")
                        f.write(f"    Total words: {expert_data.get('total_words', 0)}\n")
                        f.write(f"    Reactive matches: {expert_data.get('reactive_matches', 0)}\n")
                        f.write(f"    Emotional matches: {expert_data.get('emotional_matches', 0)}\n")
                        
                        if expert_data.get('reactive_details'):
                            f.write("    Reactive word examples:\n")
                            for match in expert_data['reactive_details'][:3]:  # Show first 3
                                f.write(f"      '{match['word']}' in context: '{match['context']}'\n")
                        
                        if expert_data.get('emotional_details'):
                            f.write("    Emotional word examples:\n")
                            for match in expert_data['emotional_details'][:3]:  # Show first 3
                                f.write(f"      '{match['word']}' in context: '{match['context']}'\n")
                    
                    if novice_data:
                        f.write("  Novice:\n")
                        f.write(f"    Total words: {novice_data.get('total_words', 0)}\n")
                        f.write(f"    Reactive matches: {novice_data.get('reactive_matches', 0)}\n")
                        f.write(f"    Emotional matches: {novice_data.get('emotional_matches', 0)}\n")
                        
                        if novice_data.get('reactive_details'):
                            f.write("    Reactive word examples:\n")
                            for match in novice_data['reactive_details'][:3]:  # Show first 3
                                f.write(f"      '{match['word']}' in context: '{match['context']}'\n")
                        
                        if novice_data.get('emotional_details'):
                            f.write("    Emotional word examples:\n")
                            for match in novice_data['emotional_details'][:3]:  # Show first 3
                                f.write(f"      '{match['word']}' in context: '{match['context']}'\n")
                
                # BC labels analysis
                if 'bc_labels' in session_data:
                    f.write("\nBC_LABELS ANALYSIS:\n")
                    bc_data = session_data['bc_labels']
                    f.write(f"  Total frames: {bc_data['total_frames']}\n")
                    for class_id in [0, 1, 2]:
                        count = bc_data['class_counts'].get(class_id, 0)
                        pct = bc_data['proportions'].get(class_id, 0)
                        class_name = {0: 'no_bc', 1: 'reactive', 2: 'emotional'}[class_id]
                        f.write(f"  {class_name:12}: {count:4} ({pct:5.2f}%)\n")
                
                f.write("\n" + "="*50 + "\n\n")
        
        print(f"✅ Session analysis saved to: {report_file}")
    
    def _generate_word_matching_analysis(self):
        """Generate word matching analysis"""
        report_file = OUTPUT_DIR / "word_matching_analysis.txt"
        
        with open(report_file, 'w', encoding='utf-8') as f:
            f.write("WORD MATCHING ANALYSIS\n")
            f.write("=" * 25 + "\n\n")
            
            f.write("REACTIVE PATTERN DICTIONARY:\n")
            f.write("-" * 30 + "\n")
            for pattern in REACTIVE_PATTERNS:
                f.write(f"  {pattern}\n")
            
            f.write(f"\nTotal reactive patterns: {len(REACTIVE_PATTERNS)}\n\n")
            
            f.write("EMOTIONAL PATTERN DICTIONARY:\n")
            f.write("-" * 30 + "\n")
            for pattern in EMOTIONAL_PATTERNS:
                f.write(f"  {pattern}\n")
            
            f.write(f"\nTotal emotional patterns: {len(EMOTIONAL_PATTERNS)}\n\n")
            
            # Analyze word frequency across sessions
            all_reactive_words = []
            all_emotional_words = []
            
            for session_data in self.stats['session_stats'].values():
                # Check expert data
                expert_data = session_data.get('expert', {})
                if 'reactive_details' in expert_data:
                    all_reactive_words.extend([m['word'] for m in expert_data['reactive_details']])
                if 'emotional_details' in expert_data:
                    all_emotional_words.extend([m['word'] for m in expert_data['emotional_details']])
                
                # Check novice data
                novice_data = session_data.get('novice', {})
                if 'reactive_details' in novice_data:
                    all_reactive_words.extend([m['word'] for m in novice_data['reactive_details']])
                if 'emotional_details' in novice_data:
                    all_emotional_words.extend([m['word'] for m in novice_data['emotional_details']])
            
            f.write("MOST FREQUENT REACTIVE WORDS:\n")
            f.write("-" * 35 + "\n")
            reactive_counter = Counter(all_reactive_words)
            for word, count in reactive_counter.most_common(10):
                f.write(f"  {word:15}: {count:3} occurrences\n")
            
            f.write("\nMOST FREQUENT EMOTIONAL WORDS:\n")
            f.write("-" * 35 + "\n")
            emotional_counter = Counter(all_emotional_words)
            for word, count in emotional_counter.most_common(10):
                f.write(f"  {word:15}: {count:3} occurrences\n")
        
        print(f"✅ Word matching analysis saved to: {report_file}")
    
    def _generate_visualizations(self):
        """Generate visualization plots"""
        print("📊 Generating visualizations...")
        
        # 1. Class distribution pie chart
        plt.figure(figsize=(10, 6))
        class_names = ['No Backchannel', 'Reactive', 'Emotional']
        class_counts = [self.stats['class_distribution'][i] for i in [0, 1, 2]]
        colors = ['lightblue', 'lightcoral', 'lightgreen']
        
        plt.subplot(1, 2, 1)
        plt.pie(class_counts, labels=class_names, colors=colors, autopct='%1.1f%%', startangle=90)
        plt.title('Overall Backchannel Class Distribution')
        
        # 2. Session-by-session bar chart
        plt.subplot(1, 2, 2)
        sessions = []
        reactive_counts = []
        emotional_counts = []
        
        for session_id, session_data in self.stats['session_stats'].items():
            if 'bc_labels' in session_data:
                sessions.append(session_id)
                reactive_counts.append(session_data['bc_labels']['class_counts'].get(1, 0))
                emotional_counts.append(session_data['bc_labels']['class_counts'].get(2, 0))
        
        x = np.arange(len(sessions))
        width = 0.35
        
        plt.bar(x - width/2, reactive_counts, width, label='Reactive', color='lightcoral')
        plt.bar(x + width/2, emotional_counts, width, label='Emotional', color='lightgreen')
        
        plt.xlabel('Sessions')
        plt.ylabel('Frame Count')
        plt.title('Backchannel Counts by Session')
        plt.xticks(x, sessions, rotation=45)
        plt.legend()
        plt.tight_layout()
        
        plt.savefig(OUTPUT_DIR / 'backchannel_analysis.png', dpi=300, bbox_inches='tight')
        plt.close()
        
        print(f"✅ Visualizations saved to: {OUTPUT_DIR / 'backchannel_analysis.png'}")
    
    def run_full_analysis(self):
        """Run the complete analysis pipeline"""
        print("🚀 STARTING BACKCHANNEL CLASSIFICATION ANALYSIS")
        print("=" * 60)
        
        # Step 1: Analyze transcript files
        self.analyze_transcript_files()
        
        # Step 2: Analyze bc_labels files
        self.analyze_bc_labels_files()
        self._summarize_bc_labels_analysis()
        
        # Step 3: Cross-validate
        self.cross_validate_classification()
        
        # Step 4: Generate reports
        self.generate_detailed_reports()
        
        print(f"\n🎉 ANALYSIS COMPLETE!")
        print(f"📁 All results saved to: {OUTPUT_DIR}")
        print(f"\n📋 Generated files:")
        print(f"  - overall_statistics.txt")
        print(f"  - class_distribution_analysis.txt") 
        print(f"  - session_analysis.txt")
        print(f"  - word_matching_analysis.txt")
        print(f"  - backchannel_analysis.png")

def main():
    """Main analysis function"""
    analyzer = BackchannelAnalyzer()
    analyzer.run_full_analysis()

if __name__ == "__main__":
    main()
