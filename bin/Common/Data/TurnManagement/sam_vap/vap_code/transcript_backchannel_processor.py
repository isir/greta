"""
Transcript-based Backchannel Processor for NoXi Paris Dataset
Processes word-level transcripts to extract backchannel annotations
and integrate them with the existing train/valid/test CSV structure
"""

import pandas as pd
import numpy as np
import torch
import re
import json
import os
from typing import Dict, List, Tuple, Optional
from dataclasses import dataclass

@dataclass
class BackchannelAnnotation:
    """Structure for a single backchannel annotation"""
    start_time: float
    end_time: float
    bc_type: int  # 0=no_bc, 1=reactive, 2=emotional
    speaker: str  # 'expert' or 'novice'
    word: str
    confidence: float = 1.0

class TranscriptBackchannelProcessor:
    """
    Processes transcript files to extract backchannel annotations
    and integrate them with the NoXi Paris dataset structure
    """
    
    def __init__(self, frame_rate: int = 50):
        """
        Args:
            frame_rate: Frame rate for output labels (default 50Hz to match VAP)
        """
        self.frame_rate = frame_rate
        
        # MaAI backchannel classification patterns
        self.reactive_patterns = [
            # Reactive backchannels - simple acknowledgments
            r'\bmm-?hmm?\b', r'\buh-?huh\b', r'\byeah\b', r'\byep\b',
            r'\bright\b', r'\bokay\b', r'\bok\b', r'\bmhm\b', r'\bmm\b',
            r'\byes\b', r'\bsure\b', r'\bexactly\b', r'\babsolutely\b',
            r'\bof course\b', r'\btrue\b', r'\bI see\b', r'\bgot it\b',
            r'\byep\b', r'\byup\b', r'\buh huh\b', r'\bmm hmm\b'
        ]
        
        self.emotional_patterns = [
            # Emotional backchannels - express surprise, interest, emotion
            r'\bwow\b', r'\boh\b', r'\bah\b', r'\booh\b', r'\bwhoa\b',
            r'\breally\??\b', r'\bno way\b', r'\bamazing\b', r'\bseriously\??\b',
            r'\boh my\b', r'\bincredible\b', r'\bunbelievable\b',
            r'\bawesome\b', r'\bthat\'s crazy\b', r'\bholy\b',
            r'\bgosh\b', r'\bgeez\b', r'\bjesus\b', r'\bchrist\b',
            r'\bthat\'s wild\b', r'\binsane\b', r'\bmad\b'
        ]
        
        # Compile patterns for efficiency
        self.reactive_regex = re.compile('|'.join(self.reactive_patterns), re.IGNORECASE)
        self.emotional_regex = re.compile('|'.join(self.emotional_patterns), re.IGNORECASE)
    
    def load_transcript_file(self, transcript_path: str) -> List[Dict]:
        """
        Load word-level transcript file
        
        Expected format variations:
        1. CSV: word, start_time, end_time
        2. JSON: [{"word": "hello", "start": 1.0, "end": 1.5}, ...]
        3. TSV: word\tstart\tend
        
        Args:
            transcript_path: Path to transcript file
            
        Returns:
            List of word dictionaries with 'word', 'start', 'end' keys
        """
        
        if not os.path.exists(transcript_path):
            print(f"Warning: Transcript file not found: {transcript_path}")
            return []
        
        words = []
        
        try:
            if transcript_path.endswith('.json'):
                # JSON format
                with open(transcript_path, 'r', encoding='utf-8') as f:
                    data = json.load(f)
                
                for item in data:
                    words.append({
                        'word': item.get('word', item.get('text', '')),
                        'start': float(item.get('start', item.get('start_time', 0))),
                        'end': float(item.get('end', item.get('end_time', 0)))
                    })
                    
            elif transcript_path.endswith('.csv'):
                # CSV format
                df = pd.read_csv(transcript_path)
                
                # Try to find the right columns
                word_col = None
                start_col = None
                end_col = None
                
                for col in df.columns:
                    col_lower = col.lower()
                    if 'word' in col_lower or 'text' in col_lower:
                        word_col = col
                    elif 'start' in col_lower:
                        start_col = col
                    elif 'end' in col_lower:
                        end_col = col
                
                if not all([word_col, start_col, end_col]):
                    # Fallback: assume first 3 columns are word, start, end
                    if len(df.columns) >= 3:
                        word_col, start_col, end_col = df.columns[:3]
                    else:
                        print(f"Error: Cannot parse CSV format in {transcript_path}")
                        return []
                
                for _, row in df.iterrows():
                    words.append({
                        'word': str(row[word_col]),
                        'start': float(row[start_col]),
                        'end': float(row[end_col])
                    })
                    
            else:
                # TSV or space-separated format
                with open(transcript_path, 'r', encoding='utf-8') as f:
                    for line in f:
                        line = line.strip()
                        if not line or line.startswith('#'):
                            continue
                        
                        parts = line.split('\t') if '\t' in line else line.split()
                        
                        if len(parts) >= 3:
                            words.append({
                                'word': parts[0],
                                'start': float(parts[1]),
                                'end': float(parts[2])
                            })
        
        except Exception as e:
            print(f"Error loading transcript {transcript_path}: {e}")
            return []
        
        return words
    
    def classify_backchannel(self, word: str) -> int:
        """
        Classify a word as backchannel type using MaAI classification
        
        Args:
            word: The word to classify
            
        Returns:
            0: no_backchannel
            1: reactive_backchannel
            2: emotional_backchannel
        """
        
        word_clean = word.lower().strip('.,!?";:')
        
        if self.emotional_regex.search(word_clean):
            return 2  # Emotional backchannel
        elif self.reactive_regex.search(word_clean):
            return 1  # Reactive backchannel
        else:
            return 0  # Not a backchannel
    
    def extract_backchannels_from_transcript(self, transcript_path: str, speaker: str) -> List[BackchannelAnnotation]:
        """
        Extract backchannel annotations from a single transcript file
        
        Args:
            transcript_path: Path to the transcript file
            speaker: 'expert' or 'novice'
            
        Returns:
            List of BackchannelAnnotation objects
        """
        
        words = self.load_transcript_file(transcript_path)
        backchannels = []
        
        for word_data in words:
            bc_type = self.classify_backchannel(word_data['word'])
            
            if bc_type > 0:  # Is a backchannel
                annotation = BackchannelAnnotation(
                    start_time=word_data['start'],
                    end_time=word_data['end'],
                    bc_type=bc_type,
                    speaker=speaker,
                    word=word_data['word']
                )
                backchannels.append(annotation)
        
        return backchannels
    
    def process_session_transcripts(self, 
                                  expert_transcript_path: str, 
                                  novice_transcript_path: str,
                                  session_id: str) -> List[BackchannelAnnotation]:
        """
        Process both expert and novice transcripts for a session
        
        Args:
            expert_transcript_path: Path to expert transcript
            novice_transcript_path: Path to novice transcript
            session_id: Session identifier (e.g., "Paris_01")
            
        Returns:
            Combined list of backchannel annotations from both speakers
        """
        
        print(f"Processing session {session_id}")
        
        # Extract backchannels from both speakers
        expert_bcs = self.extract_backchannels_from_transcript(expert_transcript_path, 'expert')
        novice_bcs = self.extract_backchannels_from_transcript(novice_transcript_path, 'novice')
        
        # Combine and sort by time
        all_bcs = expert_bcs + novice_bcs
        all_bcs.sort(key=lambda x: x.start_time)
        
        print(f"  Found {len(expert_bcs)} expert backchannels, {len(novice_bcs)} novice backchannels")
        
        return all_bcs
    
    def create_frame_labels(self, 
                           backchannels: List[BackchannelAnnotation],
                           start_time: float,
                           end_time: float) -> torch.Tensor:
        """
        Create frame-level backchannel labels for a specific time segment
        
        Args:
            backchannels: List of backchannel annotations
            start_time: Start time of the segment
            end_time: End time of the segment
            
        Returns:
            Tensor of shape (T,) with backchannel labels
        """
        
        duration = end_time - start_time
        num_frames = int(duration * self.frame_rate)
        labels = torch.zeros(num_frames, dtype=torch.long)
        
        for bc in backchannels:
            # Check if backchannel overlaps with this segment
            if bc.end_time < start_time or bc.start_time > end_time:
                continue
            
            # Calculate frame indices
            bc_start_relative = max(0, bc.start_time - start_time)
            bc_end_relative = min(duration, bc.end_time - start_time)
            
            start_frame = int(bc_start_relative * self.frame_rate)
            end_frame = int(bc_end_relative * self.frame_rate)
            
            # Ensure we don't go out of bounds
            start_frame = max(0, min(start_frame, num_frames - 1))
            end_frame = max(start_frame, min(end_frame, num_frames))
            
            # Set the labels
            labels[start_frame:end_frame] = bc.bc_type
        
        return labels
    
    def augment_dataset_csv(self, 
                           csv_path: str,
                           transcript_dir: str,
                           output_dir: str = "tmp_cache") -> str:
        """
        Augment an existing dataset CSV with backchannel labels
        
        Args:
            csv_path: Path to the original CSV (train/valid/test_paris_extended.csv)
            transcript_dir: Directory containing transcript files
            output_dir: Directory to save backchannel label files
            
        Returns:
            Path to the augmented CSV file
        """
        
        print(f"Augmenting dataset: {csv_path}")
        print(f"Using transcripts from: {transcript_dir}")
        
        # Load the original dataset
        df = pd.read_csv(csv_path)
        
        # Add new column for backchannel labels
        df['backchannel_labels_path'] = ''
        
        # Create output directory
        os.makedirs(output_dir, exist_ok=True)
        
        # Process each sample
        processed_sessions = {}  # Cache to avoid reprocessing same session
        
        for idx, row in df.iterrows():
            session = row['id'].split('_')[0] + '_' + row['id'].split('_')[1]  # e.g., "Paris_01"
            start_time = float(row['start'])
            end_time = float(row['end'])
            
            # Load backchannel annotations for this session (with caching)
            if session not in processed_sessions:
                expert_transcript = os.path.join(transcript_dir, f"{session}_expert_transcript.csv")
                novice_transcript = os.path.join(transcript_dir, f"{session}_novice_transcript.csv")
                
                # Alternative naming conventions
                if not os.path.exists(expert_transcript):
                    expert_transcript = os.path.join(transcript_dir, f"{session}-expert.csv")
                if not os.path.exists(novice_transcript):
                    novice_transcript = os.path.join(transcript_dir, f"{session}-novice.csv")
                
                backchannels = self.process_session_transcripts(
                    expert_transcript, novice_transcript, session
                )
                processed_sessions[session] = backchannels
            else:
                backchannels = processed_sessions[session]
            
            # Create frame-level labels for this segment
            labels = self.create_frame_labels(backchannels, start_time, end_time)
            
            # Save labels to file
            labels_filename = f"{row['id']}_{start_time}_{end_time}_bc_labels.pt"
            labels_path = os.path.join(output_dir, labels_filename)
            torch.save(labels, labels_path)
            
            # Update dataframe
            df.at[idx, 'backchannel_labels_path'] = labels_path
            
            if idx % 100 == 0:
                print(f"  Processed {idx}/{len(df)} samples...")
        
        # Save augmented dataset
        output_csv = csv_path.replace('.csv', '_with_bc_labels.csv')
        df.to_csv(output_csv, index=False)
        
        print(f"✅ Saved augmented dataset to: {output_csv}")
        return output_csv
    
    def generate_backchannel_statistics(self, 
                                      transcript_dir: str,
                                      session_pattern: str = "Paris_*") -> Dict:
        """
        Generate statistics about backchannel patterns in the dataset
        
        Args:
            transcript_dir: Directory containing transcript files
            session_pattern: Pattern to match session files
            
        Returns:
            Dictionary with statistics
        """
        
        import glob
        
        # Find all expert transcript files
        expert_files = glob.glob(os.path.join(transcript_dir, f"{session_pattern}_expert*"))
        
        stats = {
            'total_sessions': 0,
            'total_expert_bcs': 0,
            'total_novice_bcs': 0,
            'reactive_bcs': 0,
            'emotional_bcs': 0,
            'sessions_processed': [],
            'bc_words': {},  # Word frequency
            'temporal_distribution': []  # Time stamps of all backchannels
        }
        
        for expert_file in expert_files:
            # Extract session name
            session = os.path.basename(expert_file).replace('_expert_transcript.csv', '').replace('-expert.csv', '')
            
            # Find corresponding novice file
            novice_file = expert_file.replace('expert', 'novice')
            
            if not os.path.exists(novice_file):
                print(f"Warning: No novice transcript found for {session}")
                continue
            
            print(f"Analyzing {session}...")
            
            # Process this session
            backchannels = self.process_session_transcripts(expert_file, novice_file, session)
            
            # Update statistics
            stats['total_sessions'] += 1
            stats['sessions_processed'].append(session)
            
            for bc in backchannels:
                if bc.speaker == 'expert':
                    stats['total_expert_bcs'] += 1
                else:
                    stats['total_novice_bcs'] += 1
                
                if bc.bc_type == 1:
                    stats['reactive_bcs'] += 1
                elif bc.bc_type == 2:
                    stats['emotional_bcs'] += 1
                
                # Word frequency
                word_key = bc.word.lower()
                stats['bc_words'][word_key] = stats['bc_words'].get(word_key, 0) + 1
                
                # Temporal info
                stats['temporal_distribution'].append(bc.start_time)
        
        return stats
    
    def print_statistics_report(self, stats: Dict):
        """Print a comprehensive statistics report"""
        
        print("\n" + "="*60)
        print("BACKCHANNEL ANALYSIS REPORT")
        print("="*60)
        
        if stats['total_sessions'] == 0:
            print("❌ No sessions were processed!")
            return
        
        total_bcs = stats['total_expert_bcs'] + stats['total_novice_bcs']
        
        print(f"📊 Dataset Overview:")
        print(f"  Sessions processed: {stats['total_sessions']}")
        print(f"  Total backchannels found: {total_bcs:,}")
        print(f"  Expert backchannels: {stats['total_expert_bcs']:,}")
        print(f"  Novice backchannels: {stats['total_novice_bcs']:,}")
        
        if total_bcs > 0:
            print(f"\n📈 Backchannel Type Distribution:")
            print(f"  Reactive: {stats['reactive_bcs']:,} ({stats['reactive_bcs']/total_bcs*100:.1f}%)")
            print(f"  Emotional: {stats['emotional_bcs']:,} ({stats['emotional_bcs']/total_bcs*100:.1f}%)")
            
            print(f"\n📝 Most Common Backchannel Words:")
            sorted_words = sorted(stats['bc_words'].items(), key=lambda x: x[1], reverse=True)
            for word, count in sorted_words[:10]:
                print(f"  '{word}': {count} times")
            
            # Rate analysis
            avg_bcs_per_session = total_bcs / stats['total_sessions']
            print(f"\n⏱️  Rate Analysis:")
            print(f"  Average backchannels per session: {avg_bcs_per_session:.1f}")
            
            if len(stats['temporal_distribution']) > 0:
                import numpy as np
                times = np.array(stats['temporal_distribution'])
                print(f"  Temporal spread: {times.min():.1f}s to {times.max():.1f}s")
        
        print(f"\n🎯 Assessment:")
        if total_bcs < 50:
            print("❌ Very few backchannels detected - check transcript format or patterns")
        elif total_bcs < 200:
            print("⚠️  Moderate backchannel detection - may need pattern refinement")
        else:
            print("✅ Good backchannel detection rate - suitable for training!")
        
        print(f"\n🚀 Next Steps:")
        print("1. Review detected patterns and adjust if needed")
        print("2. Run augment_dataset_csv() to add labels to training data")
        print("3. Update dataset.py to load backchannel labels")
        print("4. Start training with backchannel prediction enabled")

def main():
    """Example usage of the transcript backchannel processor"""
    
    print("🎯 Transcript Backchannel Processor")
    print("Processing word-level transcripts to extract backchannel annotations")
    print("="*70)
    
    processor = TranscriptBackchannelProcessor()
    
    # Configuration - ADJUST THESE PATHS TO YOUR SETUP
    transcript_dir = "transcripts"  # Directory with your transcript files
    dataset_files = [
        "train_paris_extended.csv",
        "valid_paris_extended.csv", 
        "test_paris_extended.csv"
    ]
    
    print(f"Looking for transcripts in: {transcript_dir}")
    
    if os.path.exists(transcript_dir):
        # First, analyze the transcripts to understand the data
        print("📊 Analyzing transcript data...")
        stats = processor.generate_backchannel_statistics(transcript_dir)
        processor.print_statistics_report(stats)
        
        # If analysis looks good, augment the dataset files
        if stats['total_sessions'] > 0:
            print("\n🔧 Augmenting dataset files with backchannel labels...")
            
            for dataset_file in dataset_files:
                if os.path.exists(dataset_file):
                    augmented_file = processor.augment_dataset_csv(
                        dataset_file, 
                        transcript_dir,
                        output_dir="tmp_cache"
                    )
                    print(f"✅ Created: {augmented_file}")
                else:
                    print(f"❌ Dataset file not found: {dataset_file}")
        
    else:
        print(f"❌ Transcript directory not found: {transcript_dir}")
        print("\nExpected structure:")
        print("  transcripts/")
        print("    Paris_01_expert_transcript.csv")
        print("    Paris_01_novice_transcript.csv")
        print("    Paris_02_expert_transcript.csv")
        print("    ...")
        print("\nOr alternative naming:")
        print("    Paris_01-expert.csv")
        print("    Paris_01-novice.csv")

if __name__ == "__main__":
    main()

