import torch
import pytorch_lightning as pl
from torch.utils.data import DataLoader
from os.path import exists
from os import cpu_count
from typing import Optional

from dataset import VapDataset

import sys
import csv
import pprint as pp
from pathlib import Path
from multiprocessing import Manager, Lock


class VapDataModule(pl.LightningDataModule):
    def __init__(
        self,
        train_path: Optional[str] = None,
        val_path: Optional[str] = None,
        test_path: Optional[str] = None,
        horizon: float = 2,
        frame_hz: int = 50,
        sample_rate: int = 16000,
        flip_channels: bool = True,
        flip_probability: float = 0.5,
        mask_vad: bool = True,
        mask_vad_probability: float = 0.4,
        mono: bool = False,
        batch_size: int = 4,
        num_workers: int = 2,
        pin_memory: bool = True,
        # pin_memory: bool = False,
        multimodal: bool = False,
        use_face_encoder: bool = False,
        use_cache: bool = False,
        exclude_av_cache: bool = False,
        preload_av: bool = False,
        cache_dir: str = 'tmp_cache',
        manager: Manager = None,
        lock:Lock = None,
        # linux: bool = False,
    ):
        super().__init__()

        # Files
        self.train_path = train_path
        self.val_path = val_path
        self.test_path = test_path

        # values
        self.horizon = horizon
        self.sample_rate = sample_rate
        self.frame_hz = frame_hz

        # Transforms
        self.mono = mono
        self.flip_channels = flip_channels
        self.flip_probability = flip_probability
        self.mask_vad = mask_vad
        self.mask_vad_probability = mask_vad_probability

        # DataLoder
        self.batch_size = batch_size
        self.pin_memory = pin_memory
        self.num_workers = num_workers
        
        self.multimodal = multimodal
        self.use_face_encoder = use_face_encoder
        
        self.use_cache = use_cache
        self.exclude_av_cache = exclude_av_cache
        self.preload_av = preload_av
        self.cache_dir = cache_dir
        
        self.manager = manager
        self.lock = lock
        # self.linux = linux

        self.load_data()

    def load_data(self):
        if self.train_path is not None:
            assert self.train_path is not None, "Train path is NONE."
            assert exists(self.train_path), f"No TRAIN file found: {self.train_path}"
        if self.val_path is not None:
            assert self.val_path is not None, "Validation path is NONE."
            assert exists(self.val_path), f"No VAL file found: {self.val_path}"

        if self.test_path is not None:
            assert self.test_path is not None, "Test path is NONE."
            assert exists(self.test_path), f"No TEST file found: {self.test_path}"

        assert self.train_path is not None, "Train path is NONE."
        self.train_dset = VapDataset(
            self.train_path,
            horizon=self.horizon,
            sample_rate=self.sample_rate,
            frame_hz=self.frame_hz,
            multimodal=self.multimodal,
            use_face_encoder=self.use_face_encoder,
            use_cache=self.use_cache,
            exclude_av_cache = self.exclude_av_cache,
            preload_av=self.preload_av,
            cache_dir=self.cache_dir,
            manager=self.manager,
            lock=self.lock,
            # linux=self.linux
        )

        #self.train_dset.preload()
        #tmp_data = self.train_dset[0]
        #for key in tmp_data.keys():
        #    val = tmp_data[key]
        #    print(key, type(val))
        #    for key in data.keys():
        #        print(type(data[key]))
        #print('Test run completed')
        #sys.exit()

        assert self.val_path is not None, "Validation path is NONE."
        self.val_dset = VapDataset(
            self.val_path,
            horizon=self.horizon,
            sample_rate=self.sample_rate,
            frame_hz=self.frame_hz,
            multimodal=self.multimodal,
            use_face_encoder=self.use_face_encoder,
            use_cache=self.use_cache,
            exclude_av_cache = self.exclude_av_cache,
            preload_av=self.preload_av,
            cache_dir=self.cache_dir,
            manager=self.manager,
            lock=self.lock,
            # linux=self.linux
        )
            
        assert self.test_path is not None, "Test path is NONE."
        self.test_dset = VapDataset(
            self.test_path,
            horizon=self.horizon,
            sample_rate=self.sample_rate,
            frame_hz=self.frame_hz,
            multimodal=self.multimodal,
            use_face_encoder=self.use_face_encoder,
            use_cache=self.use_cache,
            exclude_av_cache = self.exclude_av_cache,
            preload_av=self.preload_av,
            cache_dir=self.cache_dir,
            manager=self.manager,
            lock=self.lock,
            # linux=self.linux
        )
        
        
        self.cache_lookup = {}
        cache_dir = Path(self.cache_dir)

        self.cache_csv = cache_dir / 'cache_multimodal_{}.csv'.format(Path(self.train_path).stem)
        self.cache_lookup = self.load_cache(self.cache_lookup, self.cache_csv)

        self.cache_csv = cache_dir / 'cache_multimodal_{}.csv'.format(Path(self.val_path).stem)
        self.cache_lookup = self.load_cache(self.cache_lookup, self.cache_csv)

        self.cache_csv = cache_dir / 'cache_multimodal_{}.csv'.format(Path(self.test_path).stem)
        self.cache_lookup = self.load_cache(self.cache_lookup, self.cache_csv)
        
        if self.preload_av:
            audio_dict, image_dict  = self.train_dset.preload(cache_lookup=self.cache_lookup)
            _, _                    = self.val_dset.preload(audio_dict=audio_dict, image_dict=image_dict)
            _, _                    = self.test_dset.preload(audio_dict=audio_dict, image_dict=image_dict)

    def setup(self, stage: Optional[str] = "fit"):
        """Loads the datasets"""

        if stage in (None, "fit"):
            pass

        if stage in (None, "test"):
            pass

    def train_dataloader(self):
        return DataLoader(
            self.train_dset,
            batch_size=self.batch_size,
            pin_memory=self.pin_memory,
            num_workers=self.num_workers,
            
            shuffle=True,
            # shuffle=False,
            
            persistent_workers=False if self.num_workers == 0 else True,
            
            drop_last=True
        )

    def val_dataloader(self):
        return DataLoader(
            self.val_dset,
            batch_size=self.batch_size,
            pin_memory=self.pin_memory,
            num_workers=self.num_workers,
            shuffle=False,
            persistent_workers=False if self.num_workers == 0 else True,
            drop_last=True
        )

    def test_dataloader(self):
        return DataLoader(
            self.test_dset,
            batch_size=self.batch_size,
            pin_memory=self.pin_memory,
            num_workers=self.num_workers,
            shuffle=False,
            persistent_workers=False if self.num_workers == 0 else True,
            drop_last=True
        )
    
    def load_cache(self, cache_lookup, cache_csv):
        
        with open(cache_csv, 'r') as f:
            reader = csv.reader(f)
            for data in reader:
                cache_lookup[(data[0], data[1], data[2])] = data[3]

        # print('cache loaded ({})'.format(Path(self.path).stem))
        
        return cache_lookup

    def __repr__(self):
        s = self.__class__.__name__
        s += f"\n\tTrain: {self.train_path}"
        s += f"\n\tVal: {self.val_path}"
        s += f"\n\tTest: {self.test_path}"
        s += f"\n\tHorizon: {self.horizon}"
        s += f"\n\tSample rate: {self.sample_rate}"
        s += f"\n\tFrame Hz: {self.frame_hz}"
        s += f"\nData"
        s += f"\n\tbatch_size: {self.batch_size}"
        s += f"\n\tpin_memory: {self.pin_memory}"
        s += f"\n\tnum_workers: {self.num_workers}"
        s += f"\nTransform"
        s += f"\n\tflip_channels: {self.flip_channels}"
        s += f"\n\tflip_probability: {self.flip_probability}"
        s += f"\n\tmask_vad: {self.mask_vad}"
        s += f"\n\tmask_vad_probability: {self.mask_vad_probability}"
        return s

    @staticmethod
    def add_data_specific_args(parent_parser):
        """argparse arguments for SoSIModel (based on yaml-config)"""
        parser = parent_parser.add_argument_group("ULMProjection")
        parser.add_argument("--train_path", default=None, type=str)
        parser.add_argument("--val_path", default=None, type=str)
        parser.add_argument("--test_path", default=None, type=str)
        parser.add_argument("--batch_size", default=4, type=int)
        parser.add_argument("--num_workers", default=cpu_count(), type=int)
        return parent_parser


if __name__ == "__main__":

    from tqdm import tqdm

    dm = VapDataModule(
        train_path="data/sliding_window_ad20_ov1_ho2_train.csv",
        val_path="data/sliding_window_ad20_ov1_ho2_val.csv",
        batch_size=20,
        num_workers=24,
    )
    dm.prepare_data()
    dm.setup("fit")

    print(dm)
    print("Train: ", len(dm.train_dset))
    print("Val: ", len(dm.val_dset))

    dloader = dm.train_dataloader()

    for batch in tqdm(dloader, total=len(dloader)):
        pass
