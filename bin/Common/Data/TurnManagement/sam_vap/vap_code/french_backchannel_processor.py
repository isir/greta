"""
French Backchannel Processor for NoXi Paris Dataset
Processes French word-level transcripts to extract backchannel annotations
and integrate them with the existing train/valid/test CSV structure
Compatible with vap_bc_2type structure and multimodal training
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

class FrenchBackchannelProcessor:
    """
    Processes French transcript files to extract backchannel annotations
    following the vap_bc_2type structure (reactive vs emotional)
    """
    
    def __init__(self, frame_rate: int = 50):
        """
        Args:
            frame_rate: Frame rate for output labels (default 50Hz to match VAP)
        """
        self.frame_rate = frame_rate
        
        # French reactive backchannels - simple acknowledgments and agreements
        self.reactive_patterns_fr = [
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
        
        # French emotional backchannels - express surprise, interest, emotion  
        self.emotional_patterns_fr = [
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
        
        # Compile patterns for efficiency
        self.reactive_regex = re.compile('|'.join(self.reactive_patterns_fr), re.IGNORECASE)
        self.emotional_regex = re.compile('|'.join(self.emotional_patterns_fr), re.IGNORECASE)
        
        # Mapping from pair numbers to Paris session names
        self.pair_to_paris = {
            1: "Paris_01", 2: "Paris_02", 3: "Paris_03", 4: "Paris_04", 5: "Paris_05",
            6: "Paris_06", 7: "Paris_07", 8: "Paris_08", 9: "Paris_09", 10: "Paris_10",
            11: "Paris_11", 12: "Paris_12", 13: "Paris_13", 14: "Paris_14", 15: "Paris_15",
            16: "Paris_16", 17: "Paris_17", 18: "Paris_18", 19: "Paris_19", 20: "Paris_20",
            21: "Paris_21", 22: "Paris_22", 23: "Paris_23", 24: "Paris_24", 25: "Paris_25"
        }
    
    def load_transcript_file(self, transcript_path: str) -> List[Dict]:
        """
        Load French transcript file from your Transcripts folder structure
        
        Args:
            transcript_path: Path to transcript CSV file (expert.csv or novice.csv)
            
        Returns:
            List of word dictionaries with 'word', 'start', 'end' keys
        """
        
        if not os.path.exists(transcript_path):
            print(f"Warning: Transcript file not found: {transcript_path}")
            return []
        
        words = []
        
        try:
            df = pd.read_csv(transcript_path)
            
            # Expected columns based on your format:
            # "Begin Time - msec", "End Time - msec", "expert_transcription" or "novice_transcription"
            
            time_start_col = "Begin Time - msec"
            time_end_col = "End Time - msec"
            
            # Find the transcription column
            text_col = None
            for col in df.columns:
                if 'transcription' in col:
                    text_col = col
                    break
            
            if text_col is None:
                print(f"Error: No transcription column found in {transcript_path}")
                return []
            
            for _, row in df.iterrows():
                try:
                    # Convert milliseconds to seconds
                    start_ms = float(row[time_start_col])
                    end_ms = float(row[time_end_col])
                    
                    word = str(row[text_col]).strip()
                    
                    # Skip empty words or punctuation-only
                    if not word or word in ['.', ',', '!', '?', ';', ':', '"', "'", '-']:
                        continue
                    
                    words.append({
                        'word': word,
                        'start': start_ms / 1000.0,  # Convert to seconds
                        'end': end_ms / 1000.0
                    })
                    
                except (ValueError, KeyError) as e:
                    print(f"Warning: Skipping invalid row in {transcript_path}: {e}")
                    continue
        
        except Exception as e:
            print(f"Error loading transcript {transcript_path}: {e}")
            return []
        
        return words
    
    def classify_backchannel(self, word: str) -> int:
        """
        Classify a French word as backchannel type following vap_bc_2type structure
        
        Args:
            word: The French word to classify
            
        Returns:
            0: no_backchannel
            1: reactive_backchannel  
            2: emotional_backchannel
        """
        
        word_clean = word.lower().strip('.,!?";:-()[]{}')
        
        # Check for emotional backchannels first (more specific)
        if self.emotional_regex.search(word_clean):
            return 2  # Emotional backchannel
        elif self.reactive_regex.search(word_clean):
            return 1  # Reactive backchannel
        else:
            return 0  # Not a backchannel
    
    def extract_backchannels_from_transcript(self, transcript_path: str, speaker: str) -> List[BackchannelAnnotation]:
        """
        Extract backchannel annotations from a single French transcript file
        
        Args:
            transcript_path: Path to the transcript file (expert.csv or novice.csv)
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
    
    def process_pair_transcripts(self, pair_number: int) -> List[BackchannelAnnotation]:
        """
        Process both expert and novice transcripts for a pair
        
        Args:
            pair_number: Pair number (1-25)
            
        Returns:
            Combined list of backchannel annotations from both speakers
        """
        
        pair_dir = f"Transcripts/pair{pair_number:02d}"
        expert_path = os.path.join(pair_dir, "expert.csv")
        novice_path = os.path.join(pair_dir, "novice.csv")
        
        paris_session = self.pair_to_paris.get(pair_number, f"Paris_{pair_number:02d}")
        print(f"Processing pair {pair_number:02d} → {paris_session}")
        
        # Extract backchannels from both speakers
        expert_bcs = self.extract_backchannels_from_transcript(expert_path, 'expert')
        novice_bcs = self.extract_backchannels_from_transcript(novice_path, 'novice')
        
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
        Compatible with vap_bc_2type structure
        
        Args:
            backchannels: List of backchannel annotations
            start_time: Start time of the segment
            end_time: End time of the segment
            
        Returns:
            Tensor of shape (T,) with backchannel labels (0=no_bc, 1=reactive, 2=emotional)
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
            end_frame = max(start_frame + 1, min(end_frame, num_frames))
            
            # Set the labels (take max if overlapping backchannels)
            labels[start_frame:end_frame] = torch.max(labels[start_frame:end_frame], 
                                                     torch.tensor(bc.bc_type, dtype=torch.long))
        
        return labels
    
    def augment_dataset_csv(self, 
                           csv_path: str,
                           output_dir: str = "tmp_cache") -> str:
        """
        Augment an existing dataset CSV with French backchannel labels
        Compatible with existing multimodal training structure
        
        Args:
            csv_path: Path to the original CSV (train/valid/test_paris_extended.csv)
            output_dir: Directory to save backchannel label files
            
        Returns:
            Path to the augmented CSV file
        """
        
        print(f"Augmenting dataset: {csv_path}")
        print(f"Processing French transcripts from Transcripts/ folder")
        
        # Load the original dataset
        df = pd.read_csv(csv_path)
        
        # Add new column for backchannel labels
        if 'backchannel_labels_path' not in df.columns:
            df['backchannel_labels_path'] = ''
        
        # Create output directory
        os.makedirs(output_dir, exist_ok=True)
        
        # Process each sample
        processed_pairs = {}  # Cache to avoid reprocessing same pair
        total_samples = len(df)
        samples_with_bc = 0
        
        for idx, row in df.iterrows():
            # Extract pair number from session ID
            session_id = str(row['id'])
            
            # Handle different session ID formats
            if session_id.startswith('Paris_'):
                pair_num = int(session_id.split('_')[1])
            else:
                # Try to extract number from other formats
                import re
                match = re.search(r'(\d+)', session_id)
                if match:
                    pair_num = int(match.group(1))
                else:
                    print(f"Warning: Could not extract pair number from {session_id}")
                    continue
            
            start_time = float(row['start'])
            end_time = float(row['end'])
            
            # Load backchannel annotations for this pair (with caching)
            if pair_num not in processed_pairs:
                backchannels = self.process_pair_transcripts(pair_num)
                processed_pairs[pair_num] = backchannels
            else:
                backchannels = processed_pairs[pair_num]
            
            # Create frame-level labels for this segment
            labels = self.create_frame_labels(backchannels, start_time, end_time)
            
            # Save labels to file (following tmp_cache structure)
            labels_filename = f"{session_id}_{start_time}_{end_time}_bc_labels.pt"
            labels_path = os.path.join(output_dir, labels_filename)
            torch.save(labels, labels_path)
            
            # Update dataframe
            df.at[idx, 'backchannel_labels_path'] = labels_path
            
            # Track statistics
            if labels.sum() > 0:  # Has some backchannels
                samples_with_bc += 1
            
            if (idx + 1) % 100 == 0:
                print(f"  Processed {idx + 1}/{total_samples} samples...")
        
        # Save augmented dataset
        base_name = os.path.basename(csv_path)
        output_csv = base_name.replace('.csv', '_with_bc_labels.csv')
        df.to_csv(output_csv, index=False)
        
        print(f"✅ Saved augmented dataset to: {output_csv}")
        print(f"📊 Samples with backchannels: {samples_with_bc}/{total_samples} ({samples_with_bc/total_samples*100:.1f}%)")
        
        return output_csv
    
    def generate_french_backchannel_statistics(self) -> Dict:
        """
        Generate comprehensive statistics about French backchannel patterns
        
        Returns:
            Dictionary with detailed statistics
        """
        
        # Find all available pairs
        available_pairs = []
        for i in range(1, 26):
            pair_dir = f"Transcripts/pair{i:02d}"
            if os.path.exists(pair_dir):
                expert_path = os.path.join(pair_dir, "expert.csv")
                novice_path = os.path.join(pair_dir, "novice.csv")
                if os.path.exists(expert_path) and os.path.exists(novice_path):
                    available_pairs.append(i)
        
        print(f"Found {len(available_pairs)} available pairs: {available_pairs}")
        
        stats = {
            'total_pairs': len(available_pairs),
            'pairs_processed': [],
            'total_expert_bcs': 0,
            'total_novice_bcs': 0,
            'reactive_bcs': 0,
            'emotional_bcs': 0,
            'bc_words_reactive': {},  # Word frequency for reactive
            'bc_words_emotional': {},  # Word frequency for emotional
            'temporal_distribution': [],
            'pair_statistics': {}  # Per-pair breakdown
        }
        
        for pair_num in available_pairs:
            print(f"Analyzing pair {pair_num:02d}...")
            
            backchannels = self.process_pair_transcripts(pair_num)
            paris_session = self.pair_to_paris.get(pair_num, f"Paris_{pair_num:02d}")
            
            # Per-pair stats
            pair_stats = {
                'expert_bcs': 0,
                'novice_bcs': 0,
                'reactive_bcs': 0,
                'emotional_bcs': 0,
                'total_bcs': len(backchannels)
            }
            
            for bc in backchannels:
                # Speaker statistics
                if bc.speaker == 'expert':
                    stats['total_expert_bcs'] += 1
                    pair_stats['expert_bcs'] += 1
                else:
                    stats['total_novice_bcs'] += 1
                    pair_stats['novice_bcs'] += 1
                
                # Type statistics
                if bc.bc_type == 1:
                    stats['reactive_bcs'] += 1
                    pair_stats['reactive_bcs'] += 1
                    
                    # Word frequency for reactive
                    word_key = bc.word.lower()
                    stats['bc_words_reactive'][word_key] = stats['bc_words_reactive'].get(word_key, 0) + 1
                    
                elif bc.bc_type == 2:
                    stats['emotional_bcs'] += 1
                    pair_stats['emotional_bcs'] += 1
                    
                    # Word frequency for emotional
                    word_key = bc.word.lower()
                    stats['bc_words_emotional'][word_key] = stats['bc_words_emotional'].get(word_key, 0) + 1
                
                # Temporal info
                stats['temporal_distribution'].append(bc.start_time)
            
            stats['pair_statistics'][paris_session] = pair_stats
            stats['pairs_processed'].append(pair_num)
        
        return stats
    
    def print_french_statistics_report(self, stats: Dict):
        """Print a comprehensive French backchannel statistics report"""
        
        print("\n" + "="*70)
        print("RAPPORT D'ANALYSE DES BACKCHANNELS FRANÇAIS")
        print("FRENCH BACKCHANNEL ANALYSIS REPORT")
        print("="*70)
        
        if stats['total_pairs'] == 0:
            print("❌ Aucune paire n'a été traitée! / No pairs were processed!")
            return
        
        total_bcs = stats['total_expert_bcs'] + stats['total_novice_bcs']
        
        print(f"📊 Vue d'ensemble du dataset / Dataset Overview:")
        print(f"  Paires traitées / Pairs processed: {stats['total_pairs']}")
        print(f"  Total backchannels trouvés / Total backchannels found: {total_bcs:,}")
        print(f"  Backchannels expert: {stats['total_expert_bcs']:,}")
        print(f"  Backchannels novice: {stats['total_novice_bcs']:,}")
        
        if total_bcs > 0:
            print(f"\n📈 Distribution par type / Type Distribution:")
            reactive_pct = stats['reactive_bcs']/total_bcs*100
            emotional_pct = stats['emotional_bcs']/total_bcs*100
            print(f"  Réactifs / Reactive: {stats['reactive_bcs']:,} ({reactive_pct:.1f}%)")
            print(f"  Émotionnels / Emotional: {stats['emotional_bcs']:,} ({emotional_pct:.1f}%)")
            
            print(f"\n📝 Mots backchannels réactifs les plus fréquents / Most Common Reactive Backchannels:")
            sorted_reactive = sorted(stats['bc_words_reactive'].items(), key=lambda x: x[1], reverse=True)
            for word, count in sorted_reactive[:10]:
                print(f"  '{word}': {count} fois / times")
            
            print(f"\n😮 Mots backchannels émotionnels les plus fréquents / Most Common Emotional Backchannels:")
            sorted_emotional = sorted(stats['bc_words_emotional'].items(), key=lambda x: x[1], reverse=True)
            for word, count in sorted_emotional[:10]:
                print(f"  '{word}': {count} fois / times")
            
            # Rate analysis
            avg_bcs_per_pair = total_bcs / stats['total_pairs']
            print(f"\n⏱️  Analyse du taux / Rate Analysis:")
            print(f"  Backchannels moyens par paire / Average per pair: {avg_bcs_per_pair:.1f}")
            
            # Show some detailed pair statistics
            print(f"\n📋 Détail par paire (premières 5) / Per-pair details (first 5):")
            for i, (session, pair_stats) in enumerate(list(stats['pair_statistics'].items())[:5]):
                total_pair = pair_stats['total_bcs']
                reactive_pair = pair_stats['reactive_bcs']
                emotional_pair = pair_stats['emotional_bcs']
                print(f"  {session}: {total_pair} total ({reactive_pair} réactifs, {emotional_pair} émotionnels)")
        
        print(f"\n🎯 Évaluation / Assessment:")
        if total_bcs < 100:
            print("❌ Très peu de backchannels détectés - vérifier les patterns")
            print("❌ Very few backchannels detected - check patterns")
        elif total_bcs < 500:
            print("⚠️  Détection modérée - peut nécessiter un ajustement des patterns")
            print("⚠️  Moderate detection - may need pattern refinement")
        else:
            print("✅ Bonne détection de backchannels - adapté pour l'entraînement!")
            print("✅ Good backchannel detection - suitable for training!")
        
        print(f"\n🚀 Prochaines étapes / Next Steps:")
        print("1. Vérifier les patterns détectés / Review detected patterns")
        print("2. Lancer augment_dataset_csv() / Run augment_dataset_csv()")
        print("3. Mettre à jour dataset.py / Update dataset.py")
        print("4. Commencer l'entraînement / Start training with backchannel prediction")

def main():
    """Main function to process French transcripts and augment datasets"""
    
    print("🇫🇷 Processeur de Backchannels Français / French Backchannel Processor")
    print("Processing French word-level transcripts for NoXi Paris dataset")
    print("="*80)
    
    processor = FrenchBackchannelProcessor()
    
    # First, analyze the French transcripts to understand the data
    print("📊 Analyse des transcriptions françaises / Analyzing French transcripts...")
    stats = processor.generate_french_backchannel_statistics()
    processor.print_french_statistics_report(stats)
    
    # If analysis looks good, augment the dataset files
    if stats['total_pairs'] > 0:
        print("\n🔧 Augmentation des fichiers dataset avec les labels backchannels...")
        print("🔧 Augmenting dataset files with backchannel labels...")
        
        dataset_files = [
            "train_paris_extended.csv",
            "valid_paris_extended.csv", 
            "test_paris_extended.csv"
        ]
        
        augmented_files = []
        for dataset_file in dataset_files:
            if os.path.exists(dataset_file):
                print(f"\nTraitement / Processing {dataset_file}...")
                augmented_file = processor.augment_dataset_csv(
                    dataset_file,
                    output_dir="tmp_cache"
                )
                augmented_files.append(augmented_file)
                print(f"✅ Créé / Created: {augmented_file}")
            else:
                print(f"❌ Fichier dataset non trouvé / Dataset file not found: {dataset_file}")
        
        print(f"\n🎉 Traitement terminé! / Processing complete!")
        print(f"📁 Fichiers augmentés / Augmented files:")
        for file in augmented_files:
            print(f"  - {file}")
        
        print(f"\n🚀 Pour utiliser avec l'entraînement / To use with training:")
        print(f"python finetune.py \\")
        print(f"  --train_csv {augmented_files[0] if augmented_files else 'train_paris_extended_with_bc_labels.csv'} \\")
        print(f"  --valid_csv {augmented_files[1] if len(augmented_files) > 1 else 'valid_paris_extended_with_bc_labels.csv'} \\")
        print(f"  --vap_predict_backchannels \\")
        print(f"  --vap_backchannel_loss_weight=0.3")
    
    else:
        print("\n❌ Aucune paire trouvée dans le dossier Transcripts/")
        print("❌ No pairs found in Transcripts/ folder")
        print("\nStructure attendue / Expected structure:")
        print("  Transcripts/")
        print("    pair01/expert.csv, pair01/novice.csv")
        print("    pair02/expert.csv, pair02/novice.csv")
        print("    ...")

if __name__ == "__main__":
    main()

