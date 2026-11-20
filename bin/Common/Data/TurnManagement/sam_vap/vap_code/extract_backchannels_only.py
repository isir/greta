#!/usr/bin/env python3
"""
Extract only backchannel words from transcript files with their categories and timings
Creates separate CSV files for expert and novice speakers
"""

import pandas as pd
import re
from pathlib import Path
from typing import List, Dict, Tuple
import csv

# French backchannel patterns (from french_backchannel_processor.py)
REACTIVE_PATTERNS_FR = [
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

EMOTIONAL_PATTERNS_FR = [
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
    r'\boh là\b', r'\bah là\b', r'\boh là là\b',
    r'\bputain\b', r'\bmerde\b', r'\bmince\b',
    r'\boh non\b', r'\bah non\b', r'\boh oui\b',
]

# Pair to Paris mapping
PAIR_TO_PARIS = {
    1: "Paris_01", 2: "Paris_02", 3: "Paris_03", 4: "Paris_04", 5: "Paris_05",
    6: "Paris_06", 7: "Paris_07", 8: "Paris_08", 9: "Paris_09", 10: "Paris_10",
    11: "Paris_11", 12: "Paris_12", 13: "Paris_13", 14: "Paris_14", 15: "Paris_15",
    16: "Paris_16", 17: "Paris_17", 18: "Paris_18", 19: "Paris_19", 20: "Paris_20",
    21: "Paris_21", 22: "Paris_22", 23: "Paris_23", 24: "Paris_24", 25: "Paris_25"
}

class BackchannelExtractor:
    def __init__(self):
        self.transcripts_dir = Path("Transcripts")
        self.output_dir = Path("backchannel_extracts")
        self.output_dir.mkdir(exist_ok=True)
        
        # Compile regex patterns for efficiency
        self.reactive_patterns = [re.compile(pattern, re.IGNORECASE) for pattern in REACTIVE_PATTERNS_FR]
        self.emotional_patterns = [re.compile(pattern, re.IGNORECASE) for pattern in EMOTIONAL_PATTERNS_FR]
    
    def extract_backchannels_from_transcript(self, csv_file: Path, speaker_type: str) -> List[Dict]:
        """Extract backchannels from a single transcript CSV file"""
        backchannels = []
        
        try:
            df = pd.read_csv(csv_file)
            
            # Find the transcription column (look for common names)
            trans_col = None
            for col in df.columns:
                if any(keyword in col.lower() for keyword in ['transcript', 'text', 'word', 'utterance', 'content']):
                    trans_col = col
                    break
            
            if trans_col is None:
                print(f"  ⚠️  No transcription column found in {csv_file.name}")
                return backchannels
            
            # Look for timing columns
            start_col = None
            end_col = None
            for col in df.columns:
                if any(keyword in col.lower() for keyword in ['start', 'begin', 'onset']):
                    start_col = col
                elif any(keyword in col.lower() for keyword in ['end', 'finish', 'offset']):
                    end_col = col
            
            print(f"  📝 Processing {speaker_type} transcript: {len(df)} rows")
            print(f"      Transcription column: {trans_col}")
            print(f"      Start column: {start_col}")
            print(f"      End column: {end_col}")
            
            for idx, row in df.iterrows():
                text = str(row[trans_col]) if pd.notna(row[trans_col]) else ""
                
                if not text or text.strip() == "":
                    continue
                
                # Get timing information
                start_time = row[start_col] if start_col and pd.notna(row[start_col]) else None
                end_time = row[end_col] if end_col and pd.notna(row[end_col]) else None
                
                # Check for emotional backchannels first (higher priority)
                for pattern in self.emotional_patterns:
                    matches = pattern.finditer(text)
                    for match in matches:
                        backchannels.append({
                            'speaker': speaker_type,
                            'word': match.group().strip(),
                            'category': 'emotional',
                            'start_time': start_time,
                            'end_time': end_time,
                            'context': text[max(0, match.start()-20):match.end()+20],
                            'row_index': idx,
                            'full_text': text
                        })
                
                # Check for reactive backchannels (avoid duplicates)
                for pattern in self.reactive_patterns:
                    matches = pattern.finditer(text)
                    for match in matches:
                        word = match.group().strip()
                        # Avoid duplicates (if word already found as emotional)
                        if not any(bc['word'] == word and bc['row_index'] == idx for bc in backchannels):
                            backchannels.append({
                                'speaker': speaker_type,
                                'word': word,
                                'category': 'reactive',
                                'start_time': start_time,
                                'end_time': end_time,
                                'context': text[max(0, match.start()-20):match.end()+20],
                                'row_index': idx,
                                'full_text': text
                            })
            
            print(f"  ✅ Found {len(backchannels)} backchannels")
            
        except Exception as e:
            print(f"  ❌ Error processing {csv_file}: {e}")
        
        return backchannels
    
    def process_pair_directory(self, pair_dir: Path):
        """Process a single pair directory"""
        pair_num = int(pair_dir.name.replace('pair', ''))
        session_id = PAIR_TO_PARIS.get(pair_num, f"Paris_{pair_num:02d}")
        
        print(f"\n📁 Processing: {pair_dir.name}")
        print(f"  Pair: {pair_num} -> {session_id}")
        
        expert_file = pair_dir / "expert.csv"
        novice_file = pair_dir / "novice.csv"
        
        all_backchannels = []
        
        # Process expert file
        if expert_file.exists():
            expert_bcs = self.extract_backchannels_from_transcript(expert_file, "expert")
            all_backchannels.extend(expert_bcs)
        else:
            print(f"  ⚠️  Expert file not found: {expert_file}")
        
        # Process novice file
        if novice_file.exists():
            novice_bcs = self.extract_backchannels_from_transcript(novice_file, "novice")
            all_backchannels.extend(novice_bcs)
        else:
            print(f"  ⚠️  Novice file not found: {novice_file}")
        
        # Save combined backchannels
        if all_backchannels:
            self.save_backchannels_csv(all_backchannels, session_id)
        
        return all_backchannels
    
    def save_backchannels_csv(self, backchannels: List[Dict], session_id: str):
        """Save backchannels to CSV files"""
        # Separate by speaker
        expert_bcs = [bc for bc in backchannels if bc['speaker'] == 'expert']
        novice_bcs = [bc for bc in backchannels if bc['speaker'] == 'novice']
        
        # Save expert backchannels
        if expert_bcs:
            expert_file = self.output_dir / f"{session_id}_backchannels_expert.csv"
            self._write_backchannels_csv(expert_bcs, expert_file)
            print(f"  💾 Expert backchannels saved: {expert_file.name} ({len(expert_bcs)} items)")
        
        # Save novice backchannels
        if novice_bcs:
            novice_file = self.output_dir / f"{session_id}_backchannels_novice.csv"
            self._write_backchannels_csv(novice_bcs, novice_file)
            print(f"  💾 Novice backchannels saved: {novice_file.name} ({len(novice_bcs)} items)")
    
    def _write_backchannels_csv(self, backchannels: List[Dict], output_file: Path):
        """Write backchannels to CSV file"""
        with open(output_file, 'w', newline='', encoding='utf-8') as csvfile:
            fieldnames = ['speaker', 'word', 'category', 'start_time', 'end_time', 'context', 'row_index', 'full_text']
            writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
            
            writer.writeheader()
            for bc in backchannels:
                writer.writerow(bc)
    
    def generate_summary_report(self, all_backchannels: List[Dict]):
        """Generate a summary report of all extracted backchannels"""
        summary_file = self.output_dir / "backchannel_extraction_summary.txt"
        
        with open(summary_file, 'w', encoding='utf-8') as f:
            f.write("BACKCHANNEL EXTRACTION SUMMARY\n")
            f.write("=" * 40 + "\n\n")
            
            # Overall statistics
            total_bcs = len(all_backchannels)
            expert_bcs = [bc for bc in all_backchannels if bc['speaker'] == 'expert']
            novice_bcs = [bc for bc in all_backchannels if bc['speaker'] == 'novice']
            
            f.write(f"Total backchannels extracted: {total_bcs}\n")
            f.write(f"Expert backchannels: {len(expert_bcs)}\n")
            f.write(f"Novice backchannels: {len(novice_bcs)}\n\n")
            
            # Category breakdown
            reactive_bcs = [bc for bc in all_backchannels if bc['category'] == 'reactive']
            emotional_bcs = [bc for bc in all_backchannels if bc['category'] == 'emotional']
            
            f.write("Category breakdown:\n")
            f.write(f"  Reactive: {len(reactive_bcs)} ({len(reactive_bcs)/total_bcs*100:.1f}%)\n")
            f.write(f"  Emotional: {len(emotional_bcs)} ({len(emotional_bcs)/total_bcs*100:.1f}%)\n\n")
            
            # Most common words
            from collections import Counter
            word_counts = Counter([bc['word'].lower() for bc in all_backchannels])
            
            f.write("Most common backchannel words:\n")
            for word, count in word_counts.most_common(10):
                f.write(f"  {word}: {count} occurrences\n")
            
            # Session breakdown
            session_counts = {}
            for bc in all_backchannels:
                # Extract session from context or use a default
                session = "unknown"
                if 'Paris_' in str(bc.get('context', '')):
                    session = "Paris_session"
                session_counts[session] = session_counts.get(session, 0) + 1
            
            f.write(f"\nSessions processed: {len(session_counts)}\n")
            for session, count in session_counts.items():
                f.write(f"  {session}: {count} backchannels\n")
        
        print(f"\n📊 Summary report saved: {summary_file}")
    
    def run_extraction(self):
        """Run the complete backchannel extraction process"""
        print("🚀 STARTING BACKCHANNEL EXTRACTION")
        print("=" * 50)
        
        if not self.transcripts_dir.exists():
            print(f"❌ Transcripts directory not found: {self.transcripts_dir}")
            return
        
        pair_dirs = [d for d in self.transcripts_dir.iterdir() if d.is_dir() and d.name.startswith('pair')]
        pair_dirs.sort(key=lambda x: int(x.name.replace('pair', '')))
        
        print(f"Found {len(pair_dirs)} pair directories")
        
        all_backchannels = []
        
        for pair_dir in pair_dirs:
            pair_backchannels = self.process_pair_directory(pair_dir)
            all_backchannels.extend(pair_backchannels)
        
        # Generate summary report
        self.generate_summary_report(all_backchannels)
        
        print(f"\n🎉 EXTRACTION COMPLETE!")
        print(f"📁 Results saved to: {self.output_dir}")
        print(f"📊 Total backchannels extracted: {len(all_backchannels)}")
        
        # List generated files
        csv_files = list(self.output_dir.glob("*.csv"))
        print(f"\n📋 Generated files ({len(csv_files)}):")
        for file in sorted(csv_files):
            print(f"  - {file.name}")

def main():
    extractor = BackchannelExtractor()
    extractor.run_extraction()

if __name__ == "__main__":
    main()
