from argparse import ArgumentParser
from os import environ
from os import path
import os
from typing import Dict, Union
from dataclasses import dataclass
import time
import math

import torch
import torch.nn.functional as F
import pytorch_lightning as pl
from pytorch_lightning.callbacks import (
    ModelCheckpoint,
    EarlyStopping,
    LearningRateMonitor,
)
from pytorch_lightning.loggers.wandb import WandbLogger
from pytorch_lightning.loggers.tensorboard import TensorBoardLogger

from pytorch_lightning.strategies.ddp import DDPStrategy
from torchmetrics.classification import Accuracy, F1Score
from torchmetrics.metric import Metric

from datamodule import VapDataModule
from callbacks import SymmetricSpeakersCallback, AudioAugmentationCallback, ResetEpochCallback, OverrideEpochStepCallback
from events import TurnTakingEvents, EventConfig
from model import VapGPT, VapConfig

from torch import nn
import argparse
import copy

import pprint as pp


def focal_loss(inputs, targets, alpha=None, gamma=2.0, reduction='mean'):
    """
    Focal Loss implementation for multi-class classification
    
    Args:
        inputs: Logits tensor of shape (N, C) where N is batch size and C is number of classes
        targets: Target tensor of shape (N,) with class indices
        alpha: Class weights tensor of shape (C,) or None
        gamma: Focusing parameter (default: 2.0)
        reduction: 'mean', 'sum', or 'none'
    
    Returns:
        Focal loss tensor
    """
    # Compute cross entropy
    ce_loss = F.cross_entropy(inputs, targets, weight=alpha, reduction='none')
    
    # Compute probabilities
    pt = torch.exp(-ce_loss)
    
    # Compute focal loss
    focal_loss = (1 - pt) ** gamma * ce_loss
    
    if reduction == 'mean':
        return focal_loss.mean()
    elif reduction == 'sum':
        return focal_loss.sum()
    else:
        return focal_loss

import multiprocessing
from multiprocessing import Manager

from pytorch_lightning.tuner.tuning import Tuner

from adopt import ADOPT

# import platform

#torch.multiprocessing.set_sharing_strategy('file_system')

# base_model = None

@dataclass
class OptConfig:

    #find_learning_rate: bool = False # default
    find_learning_rate: bool = True # default
    
    # use_adopt: bool = True
    # # learning_rate: float = 1e-4
    # learning_rate: float = 1.9054607179632464e-05
    
    use_adopt: bool = False
    # learning_rate: float = 3.63e-4
    learning_rate: float = 1e-4
    # find_learning_rate: bool = True
    betas = [0.9, 0.999]
    weight_decay: float = 0.001
    
    # lr_scheduler_interval: str = "step"
    # lr_scheduler_freq: int = 100
    # lr_scheduler_tmax: int = 2500
    lr_scheduler_patience: int = 2
    lr_scheduler_factor: float = 0.5

    # early stopping
    early_stopping: int = 1
    patience: int = 5
    
    monitor: str = "loss_val_vap"
    mode: str = "min"
    save_top_k: int = -1
    max_epochs: int = -1
    
    log_dir: str = "lightning_logs"

    # saved_dir: str = "/n/work1/inoue/vap/runs"
    saved_dir: str = "runs"
    
    logger: bool = True
    
    save_init_model: str = ''
    
    resume_ckpt: str = ''

    @staticmethod
    def add_argparse_args(parser):
        for k, v in OptConfig.__dataclass_fields__.items():
            parser.add_argument(f"--opt_{k}", type=v.type, default=v.default)
        return parser

    @staticmethod
    def args_to_conf(args):
        return OptConfig(
            **{
                k.replace("opt_", ""): v
                for k, v in vars(args).items()
                if k.startswith("opt_")
            }
        )


@dataclass
class DataConfig:
    train_path: str = "../vap_dataset/data/sliding_train.csv"
    val_path: str = "../vap_dataset/data/sliding_val.csv"
    test_path: str = "../vap_dataset/data/sliding_test.csv"
    flip_channels: bool = True
    flip_probability: float = 0.5
    mask_vad: bool = True
    mask_vad_probability: float = 0.5

    # global_batch_size: int = 16
    global_batch_size: int = 256
    # global_batch_size: int = 64

    # batch_size: int = 32
    # batch_size: int = 16
    # batch_size: int = 8
    batch_size: int = 4
    # batch_size: int = 1
    # batch_size: int = 128
        
    #if os.cpu_count() >= 16:
    #    num_workers: int = 10
    #elif os.cpu_count() <= 4:
    #    num_workers = 0
    #else:
    #    num_workers = 2
    
    num_workers: int = 4
    
    assert global_batch_size%batch_size == 0, "(global_batch_size / batch_size) should be 0"
    
    # grad_accum: int = 1
    # grad_accum: int = 2    
    # grad_accum: int = int(global_batch_size/batch_size)

    # not used for datamodule
    audio_duration: float = 20

    #perturbation: str = "none"

    test_perturbation: int = 0
    # 0: no perturbation
    # 1: flat pitch
    # 2: low pass

    limit_train_batches: Union[float, None] = None
    limit_val_batches: Union[float, None] = None
    limit_test_batches: Union[float, None] = None
    # limit_train_batches = 0.1
    # limit_val_batches = 0.1
    # limit_test_batches = 0.1
    # limit_train_batches = 0.01
    # limit_val_batches = 0.01
    # limit_test_batches = 0.001

    use_cache: bool = False
    exclude_av_cache: bool = False
    preload_av: bool = False
    cache_dir: str = 'tmp_cache'

    @staticmethod
    def add_argparse_args(parser):
        for k, v in DataConfig.__dataclass_fields__.items():
            if (k == 'use_cache') or (k == 'exclude_av_cache') or (k == 'preload_av'):
                parser.add_argument(f'--data_{k}', action='store_true')
            else:
                parser.add_argument(f"--data_{k}", type=v.type, default=v.default)
        return parser

    @staticmethod
    def args_to_conf(args):
        return DataConfig(
            **{
                k.replace("data_", ""): v
                for k, v in vars(args).items()
                if k.startswith("data_")
            }
        )


def get_args():
    parser = ArgumentParser("VoiceActivityProjection")
    parser.add_argument("--debug", action="store_true")
    parser.add_argument("--seed", type=int, default=1)
    #parser = pl.Trainer.add_argparse_args(parser)
    parser = OptConfig.add_argparse_args(parser)
    parser = DataConfig.add_argparse_args(parser)
    parser, fields_added = VapConfig.add_argparse_args(parser)
    print('### Added vap fields')
    # pp.pprint(fields_added)
    parser, fields_added = EventConfig.add_argparse_args(parser, fields_added)
    parser.add_argument("--devices", type=str, default='0')
    
    # NOTE: backchannel_prediction_shift_ms is automatically added by VapConfig.add_argparse_args()
    
    # Focal loss for backchannel prediction (COMMENTED OUT - uncomment when needed)
    # parser.add_argument("--vap_backchannel_focal_loss", nargs=3, type=float, default=None,
    #                    help="Enable focal loss for backchannel prediction with class weights [no_bc, reactive, emotional]. Example: --vap_backchannel_focal_loss 0.04 0.24 0.72")

    # parser.add_argument("--pretrained_vap_model", type=str, default='../asset/vap/vap_state_dict_jp_20hz_2500msec.pt')
    # parser.add_argument("--pretrained_cpc_model", type=str, default='../asset/cpc/60k_epoch4-d0f474de.pt')

    args = parser.parse_args()
    
    # Process focal loss arguments (COMMENTED OUT - uncomment when needed)
    # if args.vap_backchannel_focal_loss is not None:
    #     if len(args.vap_backchannel_focal_loss) != 3:
    #         raise ValueError("--vap_backchannel_focal_loss requires exactly 3 weights: [no_bc, reactive, emotional]")
    #     if not all(w > 0 for w in args.vap_backchannel_focal_loss):
    #         raise ValueError("All focal loss weights must be positive")
    #     if abs(sum(args.vap_backchannel_focal_loss) - 1.0) > 1e-6:
    #         print(f"Warning: Focal loss weights sum to {sum(args.vap_backchannel_focal_loss)}, not 1.0. Normalizing...")
    #         total = sum(args.vap_backchannel_focal_loss)
    #         args.vap_backchannel_focal_loss = [w/total for w in args.vap_backchannel_focal_loss]
    #     
    #     # Set focal loss parameters
    #     focal_weights = args.vap_backchannel_focal_loss
    #     args.vap_backchannel_focal_loss = True
    #     args.vap_backchannel_focal_alpha = focal_weights
    
    # pp.pprint(args.__dict__)

    args_orig = copy.deepcopy(args)

    model_conf = VapConfig.args_to_conf(args)
    opt_conf = OptConfig.args_to_conf(args)
    data_conf = DataConfig.args_to_conf(args)
    event_conf = EventConfig.args_to_conf(args)

    # data_conf.grad_accum = int(data_conf.global_batch_size/data_conf.batch_size)
    
    print('Num workers:', data_conf.num_workers)

    # Remove all non trainer args
    cfg_dict = vars(args)
    
    for k, _ in list(cfg_dict.items()):
        if (
            k.startswith("data_")
            or k.startswith("vap_")
            or k.startswith("opt_")
            or k.startswith("event_")
            or k.startswith("pretrained_")
        ):
            cfg_dict.pop(k)
    
    return {
        "args": args_orig,
        "cfg_dict": cfg_dict,
        "model": model_conf,
        "event": event_conf,
        "opt": opt_conf,
        "data": data_conf,
    }


def get_run_name(configs) -> str:
    s = "VapGPT"
    s += f"_{configs['model'].frame_hz}Hz"
    s += f"_ad{configs['data'].audio_duration}s"
    s += f"_{configs['model'].channel_layers}"
    s += str(configs["model"].cross_layers)
    s += str(configs["model"].num_heads)
    
    # if configs["model"].encoder_type == "cpc":
    #     s += "_cpc"
        
    #     # For original CPC model
    #     if configs["model"].cpc_model_pt != "" and configs["model"].cpc_model_pt != "default":
    #         #print(os.path.basename(configs["model"].cpc_model_pt))
    #         epoch_ = os.path.basename(configs["model"].cpc_model_pt).split('_')[1].split('.')[0]
    #         data_ = configs["model"].cpc_model_pt.split('/')[-2]
    #         s += '_' + data_ + '_' + epoch_
    
    if configs["model"].freeze_encoder == 0:
        s += "_enc-tuned"
    
    return s


def finetune(manager,lock) -> None:
        
    # global base_model
    configs = get_args()
    cfg_dict = configs["cfg_dict"]
    args = configs['args']
    
    # HACK: workaround. somehow keyword argument from commandline call was not affected
    # configs["model"].onishi = False
    # configs["model"].multimodal = False

    pl.seed_everything(cfg_dict["seed"])
    local_rank = environ.get("LOCAL_RANK", 0)

    if configs["model"].onishi and (not configs["model"].multimodal):
        configs["model"].multimodal = True
    
    # if configs["model"].multimodal:
    #     base_model = VapGPTmulti
    # else:
    #     base_model = VapGPT
    
    pp.pprint(configs)
        
    model = VAPModel(
        configs["model"], opt_conf=configs["opt"], event_conf=configs["event"]
    )
    
    # model.load_pretrained_parameters(args.pretrained_vap_model, args.pretrained_cpc_model)
    model.load_pretrained_parameters(configs["model"].pretrained_vap, configs["model"].pretrained_cpc, configs["model"].pretrained_face_encoder)
    
    if len(configs["opt"].save_init_model) != 0:
        torch.save(model, configs["opt"].save_init_model)
        print('saved to ', configs["opt"].save_init_model)
        import sys
        sys.exit()

    if configs["args"].devices is None:
        gpu_devices = -1
    else:
        gpu_devices = [int(d.strip()) for d in configs["args"].devices.split(",")]

    # Actual Training
    if torch.cuda.is_available():
        #cfg_dict["accelerator"] = "gpu"
        cfg_dict["accelerator"] = "cuda"
        cfg_dict["devices"] = gpu_devices
        
        if len(gpu_devices) == 1:
            strategy = 'auto'
        else:
            # strategy = "ddp"
            strategy = "ddp_find_unused_parameters_true"  # this should be used for models including pre-trained (freezed) encoders
        
    else:
        cfg_dict["accelerator"] = "cpu"
        cfg_dict["devices"] = 1
        strategy = 'auto'

    
    print('accelerator:', cfg_dict['accelerator'])
    print('devices:', cfg_dict['devices'])
    print('Strategy:', strategy)

    name = get_run_name(configs)

    dconf = configs["data"]
    dm = VapDataModule(
        train_path=dconf.train_path,
        val_path=dconf.val_path,
        test_path=dconf.test_path,
        horizon=2,
        
        batch_size=dconf.batch_size,
        num_workers=dconf.num_workers,
        
        frame_hz=configs["model"].frame_hz,
        multimodal=configs["model"].multimodal,
        use_face_encoder=configs["model"].use_face_encoder,
        use_cache=dconf.use_cache,
        exclude_av_cache=dconf.exclude_av_cache,
        preload_av=dconf.preload_av,
        cache_dir=dconf.cache_dir,
        manager=manager,
        lock=lock,
        # linux=linux
    )
    # dm.prepare_data()
    
    if dconf.limit_train_batches != None:
        train_batches_limit = int(len(dm.train_dset)/dconf.batch_size*dconf.limit_train_batches)
    else:
        train_batches_limit = 1

    if dconf.limit_val_batches != None:
        val_batches_limit = int(len(dm.val_dset)/dconf.batch_size*dconf.limit_val_batches)
    else:
        val_batches_limit = 1

    if dconf.limit_test_batches != None:
        test_batches_limit = int(len(dm.test_dset)/dconf.batch_size*dconf.limit_test_batches)
    else:
        test_batches_limit = 1
        
    print('max_steps: {}, {}, {}'.format(int(len(dm.train_dset)/dconf.batch_size), int(len(dm.val_dset)/dconf.batch_size), int(len(dm.test_dset)/dconf.batch_size)))
    print('batches_limit: {}, {}, {}'.format(train_batches_limit, val_batches_limit, test_batches_limit))
    # input()

    # if configs["args"].auto_select_gpus is not None and configs["args"].auto_select_gpus == 1:
    #     auto_select_gpus = True
    #     gpu_devices = int(configs["args"].devices)
    # else:
    #     auto_select_gpus = False
    
    # if cfg_dict["debug"]:
    #     environ["WANDB_MODE"] = "offline"
    #     print("DEBUG -> OFFLINE MODE")

    # if cfg_dict["fast_dev_run"]:
    #     print("NAME: " + name)
    #     for n in ["logger", "strategy", "debug", "seed", "wandb_project"]:
    #         cfg_dict.pop(n)
    #     trainer = pl.Trainer(**cfg_dict)
    #     trainer.fit(model, datamodule=dm)
    # else:

    oconf = configs["opt"]

    # Callbacks & Logger
    logger = None
    
    ckpt_path = None
    os.makedirs(oconf.saved_dir, exist_ok=True)
    list_filenames = []
    files = sorted(list(os.listdir(oconf.saved_dir)))
    for f in files:
        if name + '-epoch' in f:
            tmp = os.path.join(oconf.saved_dir, f)
            print(tmp)
            list_filenames.append(tmp)
            
    if 'last.ckpt' in files:
        tmp = os.path.join(oconf.saved_dir, 'last.ckpt')
    
    if len(list_filenames) > 1:
        
        print('############################################################################')
        print(f'Already trained checkpoints exists. Resuming from {tmp} in 10 seconds')
        print('############################################################################')
        for i in range(10):
            time.sleep(1)
        
        ckpt_path = tmp
        
    #     # Delete esiisting files
    #     prompt = 'Already existing files in the save directory. Do you want to delete them? [yes/no]'
    #     while True:
    #         user_input = input(prompt).lower()
    #         if user_input == 'yes' or user_input == 'no':
    #             break
    #         else:
    #             print('Please enter either "yes" or "no".')
    #     if user_input == 'yes':
    #         for f in list_filenames:
    #             os.remove(f)
    #             print("Removed file: " + f)
    
    # pass

    callbacks = [
        SymmetricSpeakersCallback(),
        OverrideEpochStepCallback(),
    ]
    if ckpt_path == None:
        callbacks.append(ResetEpochCallback())

    if configs["model"].context_limit_cpc_sec < 0:
        # callbacks.append(AudioAugmentationCallback(device="cpu"))
        callbacks.append(AudioAugmentationCallback(device=cfg_dict["accelerator"]))
    
    if oconf.early_stopping == 1:
        print("Eearly Stopping applied")
        callbacks.append(
            EarlyStopping(
                monitor=oconf.monitor,
                mode=oconf.mode,
                patience=oconf.patience,
                strict=True,  # crash if "monitor" is not found in val metrics
                verbose=True,
            )
        )

    # if not cfg_dict["debug"]:
    #     logger = WandbLogger(
    #         project=cfg_dict["wandb_project"],
    #         name=name,
    #         log_model=False,
    #         save_dir="runs",
    #     )
    #     callbacks.append(LearningRateMonitor())
    
    logger = TensorBoardLogger(save_dir=configs["opt"].log_dir)

    if local_rank == 0:
        print("#" * 40)
        print(f"Early stopping (patience={oconf.patience})")
        print("#" * 40)

    for n in ["logger", "strategy", "debug", "seed", "wandb_project"]:
        if n in cfg_dict:
            cfg_dict.pop(n)
    
    cfg_dict['max_epochs'] = oconf.max_epochs
    # cfg_dict['max_epochs'] = 1
    
    if configs["model"].context_limit_cpc_sec > 0:
        bool_find_unused_parameters = True
    else:
        bool_find_unused_parameters = False

    grad_accum = int(dconf.global_batch_size/dconf.batch_size)
    print('Grad accum: ', grad_accum)

    # Find Best Learning Rate
    if oconf.find_learning_rate and (ckpt_path == None):
        print('Start learning rate finder')

        trainer = pl.Trainer(
            logger=None,
            callbacks=callbacks,
            strategy=strategy,
            # accumulate_grad_batches=dconf.grad_accum,
            accumulate_grad_batches=grad_accum,
            limit_train_batches=dconf.limit_train_batches, 
            limit_val_batches=dconf.limit_val_batches,
            limit_test_batches=dconf.limit_test_batches,
            log_every_n_steps=min(50, train_batches_limit, val_batches_limit, test_batches_limit),
            **cfg_dict        
        )
        
        tuner = Tuner(trainer)

        # finds learning rate automatically
        # sets hparams.lr or hparams.learning_rate to that learning rate
        lr_finder = tuner.lr_find(model, datamodule=dm)
        # datamodule=None
        
        # lr_finder = lr_finder.lr_find(model, dm)
        # model.learning_rate = lr_finder.suggestion()
        model.opt_conf.learning_rate = lr_finder.suggestion()
    
    if ckpt_path == None:
        print("Learning Rate: ", model.opt_conf.learning_rate)
        print("#" * 40)
    
    callbacks.append(
        ModelCheckpoint(
            dirpath=oconf.saved_dir,
            mode=oconf.mode,
            monitor=oconf.monitor,
            save_top_k=oconf.save_top_k,
            auto_insert_metric_name=False,
            filename=name + "-epoch{epoch}-val_{loss_val_vap:.5f}",
            save_last=True
        )
    )

    #HINT: trainer
    trainer = pl.Trainer(
        logger=logger,
        callbacks=callbacks,
        strategy=strategy,
        # accumulate_grad_batches=dconf.grad_accum,
        accumulate_grad_batches=grad_accum,
        # fast_dev_run=3,
        limit_train_batches=dconf.limit_train_batches, 
        limit_val_batches=dconf.limit_val_batches,
        limit_test_batches=dconf.limit_test_batches,
        log_every_n_steps=min(50, train_batches_limit, val_batches_limit, test_batches_limit),
        # profiler="simple",
        # profiler="advanced",
        # profiler="pytorch",
        # precision=16,
        # progress_bar_refresh_rate=0,
        **cfg_dict
        
    )

    trainer.fit(model, datamodule=dm, ckpt_path=ckpt_path)
    
    trainer.test(model, datamodule=dm)

# HINT: VAPModel
# Used in training (LightningModule) but not required for inference
class VAPModel(VapGPT, pl.LightningModule):
# class VAPModel(base_model, pl.LightningModule):
    def __init__(self, conf, opt_conf=None, event_conf=None):
        super().__init__(conf)
        
        self.model_conf = conf
        self.opt_conf = opt_conf
        self.event_conf = event_conf
        
        self.lr = self.opt_conf.learning_rate

        # Training params
        self.save_hyperparameters()

        # Metrics
        self.event_extractor = None
        if event_conf is not None:
            # self.zero_shot = ZeroShot(bin_times=conf.bin_times, frame_hz=conf.frame_hz)
            self.event_extractor = TurnTakingEvents(event_conf)
        
        self.training_step_outputs = []
        # self.test_perturbation = 0

        # self.test_hs_reference = [0, 0]
        # self.test_hs_confusion_matrix = {"ref=0:pre=0": 0, "ref=0:pre=1": 0, "ref=1:pre=0": 0, "ref=1:pre=1": 0}

        # self.test_hs2_reference = [0, 0]
        # self.test_hs2_confusion_matrix = {"ref=0:pre=0": 0, "ref=0:pre=1": 0, "ref=1:pre=0": 0, "ref=1:pre=1": 0}

        # self.test_pred_shift2_reference = [0, 0]
        # self.test_pred_shift2_confusion_matrix = {"ref=0:pre=0": 0, "ref=0:pre=1": 0, "ref=1:pre=0": 0, "ref=1:pre=1": 0}

        # self.test_pred_backchannel2_reference = [0, 0]
        # self.test_pred_backchannel2_confusion_matrix = {"ref=0:pre=0": 0, "ref=0:pre=1": 0, "ref=1:pre=0": 0, "ref=1:pre=1": 0}

        # self.test_inference_time_all = []
        
        self.onishi = conf.onishi
        self.multimodal = conf.multimodal
        self.use_face_encoder = conf.use_face_encoder
        
        if self.onishi and (not self.multimodal):
            self.multimodal = True
        
        # RTF measurement variables
        self.inference_times = []
        self.audio_durations = []
        
    def compute_rtf(self) -> float:
        """
        Compute Real-Time Factor (RTF) = average_inference_time / average_audio_duration
        RTF < 1.0 means faster than real-time (good for real-time applications)
        """
        if len(self.inference_times) == 0 or len(self.audio_durations) == 0:
            return 0.0
        
        avg_inference_time = sum(self.inference_times) / len(self.inference_times)
        avg_audio_duration = sum(self.audio_durations) / len(self.audio_durations)
        
        # Debug information
        print(f"RTF Debug - Batches processed: {len(self.inference_times)}")
        print(f"RTF Debug - Avg batch inference time: {avg_inference_time:.4f}s")
        print(f"RTF Debug - Audio duration per sample: {avg_audio_duration:.4f}s")
        print(f"RTF Debug - Last batch inference time: {self.inference_times[-1]:.4f}s")
        print(f"RTF Debug - GPU parallelizes batch → batch_time ≈ single_sample_time")
        
        rtf = avg_inference_time / avg_audio_duration if avg_audio_duration > 0 else 0.0
        print(f"RTF Debug - Computed RTF: {rtf:.4f}")
        
        return rtf
    
    def reset_rtf_measurements(self):
        """Reset RTF measurements for each epoch"""
        self.inference_times = []
        self.audio_durations = []
        
    def apply_prediction_shift(self, targets: torch.Tensor, shift_ms: int = 500, frame_hz: int = 50) -> torch.Tensor:
        """
        Apply timing shift to prediction targets (BC or HN).
        Shifts targets forward in time so model predicts events before they occur.
        
        Args:
            targets: Targets of shape [B, T] (backchannel or head nod)
            shift_ms: Shift amount in milliseconds (default: 500ms)
            frame_hz: Frame rate in Hz (default: 50Hz)
            
        Returns:
            Shifted targets of same shape [B, T]
        """
        if shift_ms <= 0:
            return targets
            
        # Convert shift from milliseconds to frames
        shift_frames = int(shift_ms * frame_hz / 1000)
        
        if shift_frames >= targets.shape[1]:
            # If shift is larger than sequence, return all zeros
            return torch.zeros_like(targets)
        
        # Shift targets forward: target[t] = original_target[t + shift_frames]
        # This means we predict at time t what will happen at time t + shift_frames
        shifted_targets = torch.zeros_like(targets)
        if shift_frames < targets.shape[1]:
            shifted_targets[:, :-shift_frames] = targets[:, shift_frames:]
        
        return shifted_targets
    
    def apply_backchannel_prediction_shift(self, targets: torch.Tensor, shift_ms: int = 500, frame_hz: int = 50) -> torch.Tensor:
        """
        Legacy wrapper for apply_prediction_shift (for backward compatibility).
        Apply timing shift to backchannel targets for prediction.
        """
        return self.apply_prediction_shift(targets, shift_ms, frame_hz)
        
    # Focal Loss implementation (COMMENTED OUT - uncomment when needed)
    # def focal_loss(self, inputs, targets, alpha=None, gamma=2.0, reduction='mean'):
    #     """
    #     Focal Loss implementation for backchannel prediction.
    #     
    #     Args:
    #         inputs: Model predictions (logits) of shape (N, C) where N is batch size and C is number of classes
    #         targets: Ground truth labels of shape (N,) with values in [0, C-1]
    #         alpha: Class weights tensor of shape (C,) or None
    #         gamma: Focusing parameter (default: 2.0)
    #         reduction: 'mean', 'sum', or 'none'
    #     
    #     Returns:
    #         Focal loss value
    #     """
    #     import torch.nn.functional as F
    #     
    #     # Convert to probabilities
    #     ce_loss = F.cross_entropy(inputs, targets, reduction='none')
    #     pt = torch.exp(-ce_loss)
    #     
    #     # Apply focal loss formula
    #     focal_loss = (1 - pt) ** gamma * ce_loss
    #     
    #     # Apply class weights if provided
    #     if alpha is not None:
    #         if alpha.dim() == 1:
    #             alpha = alpha[targets]
    #         focal_loss = alpha * focal_loss
    #     
    #     if reduction == 'mean':
    #         return focal_loss.mean()
    #     elif reduction == 'sum':
    #         return focal_loss.sum()
    #     else:
    #         return focal_loss
        
    def load_pretrained_parameters(self, vap_model, cpc_model, face_encoder_path = None):
        
        # Load checkpoint (allow config classes for PyTorch 2.6+ compatibility)
        from events import EventConfig
        torch.serialization.add_safe_globals([VapConfig, OptConfig, DataConfig, EventConfig])
        sd = torch.load(vap_model, map_location=torch.device(self.device), weights_only=False)
        
        # sd['global_step'] = 0
        # sd['epoch'] = 0
        
        # self.load_encoder_CPC(cpc_model=cpc_model)
        
        # if self.conf.use_face_encoder:
        #     self.load_encoder_FormerDFER(face_encoder_path)

        ###########
        # Over-ride parameters with pretrained parameters if shape and name mutch
        ###########
        
        ### original
        #
        # self.load_state_dict(sd, strict=False)
        #

        ### modified
        #
        for name, param in sd.items():
            submodules = name.split(".")
            # param = nn.Parameter(param, requires_grad=False)
            param = nn.Parameter(param, requires_grad=True)
            try:
                if (self.submodules.shape == param.shape):
                    torch_set_attr(self, submodules, param)
            except:
                pass

    def configure_optimizers(self) -> Dict:
        assert self.opt_conf is not None, "configure_optimizers: No Opt conf!"
        
        if self.opt_conf.use_adopt:
            opt = ADOPT(self.parameters(), lr=self.opt_conf.learning_rate, decouple=True)
        else:
            opt = torch.optim.AdamW(
                self.parameters(),
                lr=self.lr,
                betas=self.opt_conf.betas,
                weight_decay=self.opt_conf.weight_decay,
            )
        
        lr_scheduler = {
            "scheduler": torch.optim.lr_scheduler.ReduceLROnPlateau(
                opt,
                mode="min",
                factor=self.opt_conf.lr_scheduler_factor,
                patience=self.opt_conf.lr_scheduler_patience,
            ),
            "monitor": "{}".format(self.opt_conf.monitor),
        }
        
        return {"optimizer": opt, "lr_scheduler": lr_scheduler}

    def get_metrics(self):
        metrics = {"acc": {}, "f1": {}}

        ACC_TASK = 'multiclass'
        # ACC_AVERAGE = 'none'
        ACC_AVERAGE = 'weighted'
        
        ############################################################

        metrics["acc"]["hs"] = Accuracy(
            task=ACC_TASK, num_classes=2, average=ACC_AVERAGE
        ).to(self.device)
        
        metrics["acc"]["hs2"] = Accuracy(
            task=ACC_TASK, num_classes=2, average=ACC_AVERAGE
        ).to(self.device)

        metrics["acc"]["hs_total"] = Accuracy(
            task=ACC_TASK, num_classes=2, average=ACC_AVERAGE
        ).to(self.device)

        ############################################################
        
        metrics["acc"]["ls"] = Accuracy(
            task=ACC_TASK, num_classes=2, average=ACC_AVERAGE
        ).to(self.device)

        ############################################################
        
        metrics["acc"]["sp"] = Accuracy(
            task=ACC_TASK, num_classes=2, average=ACC_AVERAGE
        ).to(self.device)

        metrics["acc"]["sp2"] = Accuracy(
            task=ACC_TASK, num_classes=2, average=ACC_AVERAGE
        ).to(self.device)

        metrics["acc"]["sp_total"] = Accuracy(
            task=ACC_TASK, num_classes=2, average=ACC_AVERAGE
        ).to(self.device)
        
        ############################################################

        metrics["acc"]["bp"] = Accuracy(
            task=ACC_TASK, num_classes=2, average=ACC_AVERAGE
        ).to(self.device)
        
        metrics["acc"]["bp2"] = Accuracy(
            task=ACC_TASK, num_classes=2, average=ACC_AVERAGE
        ).to(self.device)

        metrics["acc"]["bp_total"] = Accuracy(
            task=ACC_TASK, num_classes=2, average=ACC_AVERAGE
        ).to(self.device)

        ############################################################

        metrics["f1"]["hs"] = F1Score(
            task="multiclass",
            num_classes=2,
            average="weighted",
        ).to(self.device)

        metrics["f1"]["hs2"] = F1Score(
            task="multiclass",
            num_classes=2,
            average="weighted",
        ).to(self.device)
        
        metrics["f1"]["ls"] = F1Score(
            task="multiclass",
            num_classes=2,
            average="weighted",
        ).to(self.device)
        
        metrics["f1"]["sp"] = F1Score(
            task="multiclass",
            num_classes=2,
            average="weighted",
        ).to(self.device)
        
        metrics["f1"]["bp"] = F1Score(
            task="multiclass",
            num_classes=2,
            average="weighted",
        ).to(self.device)

        metrics["f1"]["sp2"] = F1Score(
            task="multiclass",
            num_classes=2,
            average="weighted",
        ).to(self.device)
        
        metrics["f1"]["bp2"] = F1Score(
            task="multiclass",
            num_classes=2,
            average="weighted",
        ).to(self.device)
        
        if self.model_conf.lid_classify:
            metrics["f1"]["lid"] = F1Score(
                task="multiclass",
                num_classes=self.conf.lid_classify_num_class,
                average="weighted",
            ).to(self.device)
        
        # NEW: Two-Head Backchannel metrics
        if self.conf.predict_backchannels:
            # Stage 1: BC Detection metrics (no_bc vs any_bc)
            if self.conf.bc_detection_head:
                metrics["acc"]["bc_detection"] = Accuracy(
                    task="binary",  # Binary: no_bc vs any_bc
                    average="weighted",
                ).to(self.device)
                
                metrics["f1"]["bc_detection"] = F1Score(
                    task="binary",
                    average="weighted",
                ).to(self.device)
            
            # Stage 2: BC Type Classification metrics (reactive vs emotional OR no_bc)
            # Evaluated on ALL frames: 0=reactive, 1=emotional, 2=no_bc (includes detection failures)
            if self.conf.bc_type_head:
                metrics["acc"]["bc_type"] = Accuracy(
                    task="multiclass",
                    num_classes=3,  # 3 classes: reactive, emotional, no_bc (-1 mapped to 2)
                    average="weighted",
                ).to(self.device)
                
                metrics["f1"]["bc_type"] = F1Score(
                    task="multiclass",
                    num_classes=3,  # 3 classes: reactive, emotional, no_bc (-1 mapped to 2)
                    average="weighted",
                ).to(self.device)
                
                # Per-class F1 scores for BC type
                metrics["f1"]["bc_reactive"] = F1Score(
                    task="binary",
                    average="weighted",
                ).to(self.device)
                
                metrics["f1"]["bc_emotional"] = F1Score(
                    task="binary",
                    average="weighted",
                ).to(self.device)
            
            # Per-class F1 scores for BC type (binary: class vs not-class)
            metrics["f1"]["bc_reactive"] = F1Score(
                task="binary", 
                average="weighted",
            ).to(self.device)
            
            metrics["f1"]["bc_emotional"] = F1Score(
                task="binary",
                average="weighted", 
            ).to(self.device)
        
        # NEW: Two-Head Head Nod metrics
        if self.conf.predict_head_nods:
            # Stage 1: HN Detection metrics (no_nod vs any_nod)
            if self.conf.hn_detection_head:
                metrics["acc"]["hn_detection"] = Accuracy(
                    task="binary",  # Binary: no_nod vs any_nod
                    average="weighted",
                ).to(self.device)
                
                metrics["f1"]["hn_detection"] = F1Score(
                    task="binary",
                    average="weighted",
                ).to(self.device)
            
            # Stage 2: HN Type Classification metrics (short, long, long_p OR no_nod)
            # Evaluated on ALL frames: 0=short, 1=long, 2=long_p, 3=no_nod (-1 mapped to 3)
            if self.conf.hn_type_head:
                metrics["acc"]["hn_type"] = Accuracy(
                    task="multiclass",
                    num_classes=4,  # 4 classes: short, long, long_p, no_nod (-1 mapped to 3)
                    average="weighted",
                ).to(self.device)
                
                metrics["f1"]["hn_type"] = F1Score(
                    task="multiclass",
                    num_classes=4,  # 4 classes: short, long, long_p, no_nod (-1 mapped to 3)
                    average="weighted",
                ).to(self.device)
                
                # Per-class F1 scores for HN type
                metrics["f1"]["hn_short"] = F1Score(
                    task="binary",
                    average="weighted",
                ).to(self.device)
                
                metrics["f1"]["hn_long"] = F1Score(
                    task="binary",
                    average="weighted",
                ).to(self.device)
                
                metrics["f1"]["hn_long_p"] = F1Score(
                    task="binary",
                    average="weighted",
                ).to(self.device)
            
        
        # pp.pprint(metrics)

        return metrics

    def metrics_step(self, preds, targets, split="val"):
        
        # print('metrics_step_called:', split, flush=True)
        # input()
        
        m = self.val_metrics if split == "val" else self.test_metrics
        
        # print('\n\n\n\n')
        
        hs_tuple_preds = ()
        hs_tuple_targets = ()

        sp_tuple_preds = ()
        sp_tuple_targets = ()

        bp_tuple_preds = ()
        bp_tuple_targets = ()

        # The metrics don't work if the predictions are not rounded
        # I don't know why...
        if preds["hs"] is not None:

            if len(targets["hs"]) >= 1:

                m["f1"]["hs"].update(preds=preds["hs"].round().int(), target=targets["hs"])
                m["acc"]["hs"].update(preds=preds["hs"].round().int(), target=targets["hs"])
                # print('\n\n1')
                
                hs_tuple_preds = hs_tuple_preds + (preds["hs"].round().int(),)
                hs_tuple_targets = hs_tuple_targets + (targets["hs"].round().int(),)
        
        if "hs2" in preds:

            if preds["hs2"] is not None:

                if len(targets["hs2"]) >= 1:

                    m["f1"]["hs2"].update(preds=preds["hs2"].round().int(), target=targets["hs2"])
                    m["acc"]["hs2"].update(preds=preds["hs2"].round().int(), target=targets["hs2"])
                    # print('\n\n2')

                    hs_tuple_preds = hs_tuple_preds + (preds["hs2"].round().int(),)
                    hs_tuple_targets = hs_tuple_targets + (targets["hs2"].round().int(),)
                    
        if preds["pred_shift"] is not None:

            if len(targets["pred_shift"]) >= 1:

                m["f1"]["sp"].update(preds=preds["pred_shift"].round().int(), target=targets["pred_shift"])
                m["acc"]["sp"].update(preds=preds["pred_shift"].round().int(), target=targets["pred_shift"])
                # print('\n\n4')

                sp_tuple_preds = sp_tuple_preds + (preds["pred_shift"].round().int(),)
                sp_tuple_targets = sp_tuple_targets + (targets["pred_shift"].round().int(),)

        if "pred_shift2" in preds:

            if preds["pred_shift2"] is not None:

                if len(targets["pred_shift2"]) >= 1:

                    m["f1"]["sp2"].update(preds=preds["pred_shift2"].round().int(), target=targets["pred_shift2"])
                    m["acc"]["sp2"].update(preds=preds["pred_shift2"].round().int(), target=targets["pred_shift2"])
                    # print('\n\n5')

                    sp_tuple_preds = sp_tuple_preds + (preds["pred_shift2"].round().int(),)
                    sp_tuple_targets = sp_tuple_targets + (targets["pred_shift2"].round().int(),)


        if preds["pred_backchannel"] is not None:
            if len(targets["pred_backchannel"]) >= 1:
                m["f1"]["bp"].update(preds=preds["pred_backchannel"].round().int(), target=targets["pred_backchannel"])
                m["acc"]["bp"].update(preds=preds["pred_backchannel"].round().int(), target=targets["pred_backchannel"])
                # print('\n\n4')

                bp_tuple_preds = bp_tuple_preds + (preds["pred_backchannel"].round().int(),)
                bp_tuple_targets = bp_tuple_targets + (targets["pred_backchannel"].round().int(),)
        

        if "pred_backchannel2" in preds:

            if preds["pred_backchannel2"] is not None:

                if len(targets["pred_backchannel2"]) >= 1:

                    m["f1"]["bp2"].update(preds=preds["pred_backchannel2"].round().int(), target=targets["pred_backchannel2"])
                    m["acc"]["bp2"].update(preds=preds["pred_backchannel2"].round().int(), target=targets["pred_backchannel2"])
                    # print('\n\n6')

                    bp_tuple_preds = bp_tuple_preds + (preds["pred_backchannel2"].round().int(),)
                    bp_tuple_targets = bp_tuple_targets + (targets["pred_backchannel2"].round().int(),)

        if preds["ls"] is not None:

            if len(targets["ls"]) >= 1:

                m["f1"]["ls"].update(preds=preds["ls"].round().int(), target=targets["ls"])
                m["acc"]["ls"].update(preds=preds["ls"].round().int(), target=targets["ls"])
                # print('\n\n3')

        
        if self.model_conf.lid_classify:
            if preds["lid"] is not None:
                if len(targets["lid"]) >= 1:
                    m["f1"]["lid"].update(preds=preds["lid"], target=targets["lid"])
                    # print('\n\n7')
                    
        # NEW: Two-Head Backchannel metrics computation
        if self.conf.predict_backchannels:
            # Stage 1: BC Detection metrics
            if "bc_detection" in preds and "bc_detection" in targets:
                if preds["bc_detection"] is not None and targets["bc_detection"] is not None:
                    if len(targets["bc_detection"]) >= 1:
                        m["f1"]["bc_detection"].update(preds=preds["bc_detection"], target=targets["bc_detection"])
                        m["acc"]["bc_detection"].update(preds=preds["bc_detection"], target=targets["bc_detection"])
            
            # Stage 2: BC Type Classification metrics
            if "bc_type" in preds and "bc_type" in targets:
                if preds["bc_type"] is not None and targets["bc_type"] is not None:
                    if len(targets["bc_type"]) >= 1:
                        m["f1"]["bc_type"].update(preds=preds["bc_type"], target=targets["bc_type"])
                        m["acc"]["bc_type"].update(preds=preds["bc_type"], target=targets["bc_type"])
                        
                        # Per-class metrics for BC type (binary: class X vs not-X)
                        bc_type_preds = preds["bc_type"]
                        bc_type_targets = targets["bc_type"]
                        
                        # Only compute per-class metrics if we have samples
                        if len(bc_type_preds) > 0:
                            # Reactive (class 0)
                            reactive_preds = (bc_type_preds == 0).long()
                            reactive_targets = (bc_type_targets == 0).long()
                            m["f1"]["bc_reactive"].update(preds=reactive_preds, target=reactive_targets)
                            
                            # Emotional (class 1)
                            emotional_preds = (bc_type_preds == 1).long()
                            emotional_targets = (bc_type_targets == 1).long()
                            m["f1"]["bc_emotional"].update(preds=emotional_preds, target=emotional_targets)
            
        
        # NEW: Two-Head Head Nod metrics computation
        if self.conf.predict_head_nods:
            # Stage 1: HN Detection metrics
            if "hn_detection" in preds and "hn_detection" in targets:
                if preds["hn_detection"] is not None and targets["hn_detection"] is not None:
                    if len(targets["hn_detection"]) >= 1:
                        m["f1"]["hn_detection"].update(preds=preds["hn_detection"], target=targets["hn_detection"])
                        m["acc"]["hn_detection"].update(preds=preds["hn_detection"], target=targets["hn_detection"])
            
            # Stage 2: HN Type Classification metrics
            if "hn_type" in preds and "hn_type" in targets:
                if preds["hn_type"] is not None and targets["hn_type"] is not None:
                    if len(targets["hn_type"]) >= 1:
                        m["f1"]["hn_type"].update(preds=preds["hn_type"], target=targets["hn_type"])
                        m["acc"]["hn_type"].update(preds=preds["hn_type"], target=targets["hn_type"])
                        
                        # Compute per-class F1 scores for HN type
                        hn_type_preds = preds["hn_type"]
                        hn_type_targets = targets["hn_type"]
                        
                        # Only compute per-class metrics if we have samples
                        if len(hn_type_preds) > 0:
                            # Short (class 0)
                            short_preds = (hn_type_preds == 0).long()
                            short_targets = (hn_type_targets == 0).long()
                            m["f1"]["hn_short"].update(preds=short_preds, target=short_targets)
                            
                            # Long (class 1)
                            long_preds = (hn_type_preds == 1).long()
                            long_targets = (hn_type_targets == 1).long()
                            m["f1"]["hn_long"].update(preds=long_preds, target=long_targets)
                            
                            # Long+ (class 2)
                            long_p_preds = (hn_type_preds == 2).long()
                            long_p_targets = (hn_type_targets == 2).long()
                            m["f1"]["hn_long_p"].update(preds=long_p_preds, target=long_p_targets)
        
        if len(hs_tuple_preds) != 0:
            tmp_preds_total = torch.concat(hs_tuple_preds, dim=0)
            tmp_targets_total = torch.concat(hs_tuple_targets, dim=0)
            m["acc"]["hs_total"].update(preds=tmp_preds_total, target=tmp_targets_total)

        if len(sp_tuple_preds) != 0:
            tmp_preds_total = torch.concat(sp_tuple_preds, dim=0)
            tmp_targets_total = torch.concat(sp_tuple_targets, dim=0)
            m["acc"]["sp_total"].update(preds=tmp_preds_total, target=tmp_targets_total)

        if len(bp_tuple_preds) != 0:
            tmp_preds_total = torch.concat(bp_tuple_preds, dim=0)
            tmp_targets_total = torch.concat(bp_tuple_targets, dim=0)
            m["acc"]["bp_total"].update(preds=tmp_preds_total, target=tmp_targets_total)

        
        # pp.pprint(m)

    def metrics_epoch(self, split="val", batch_size = 1):
        
        if split == "val":
            metrics = self.val_metrics
        else:
            metrics = self.test_metrics
        
        # f1 = {}
        # for name, metric in m["f1"].items():
        #     f1[name] = metric.compute()

        #     #support = metric._num_examples[name]

        #     metric.reset()

        # # Accuracy
        # acc = {}
        # for name, metric in m["acc"].items():
        #     a, b = metric.compute()
        #     acc[name] = [a, b]
        #     metric.reset()

        # self.log(f"{split}_hs", {"acc": acc["hs"][1], "f1w": f1["hs"]}, prog_bar=True, sync_dist=True)
        # self.log(f"{split}_hs2", {"acc": acc["hs2"][1], "f1w": f1["hs2"]}, prog_bar=True, sync_dist=True)
        
        # self.log(f"{split}_pred_sh", {"acc": acc["sp"][1], "f1w": f1["sp"]}, sync_dist=True)
        # self.log(f"{split}_pred_sh2", {"acc": acc["sp2"][1], "f1w": f1["sp2"]}, sync_dist=True)
        
        # self.log(f"{split}_pred_bc", {"acc": acc["bp"][1], "f1w": f1["bp"]}, sync_dist=True)
        # self.log(f"{split}_pred_bc2", {"acc": acc["bp2"][1], "f1w": f1["bp2"]}, sync_dist=True)

        # self.log(f"{split}_ls", {"short": acc["ls"][1]}, sync_dist=True)
        
        # self.log(f"{split}_lid", {"f1w": f1["lid"]}, sync_dist=True)

        # if split == "test":
        #     # hs2, sh2, bc2のBalanced accuracyを計算
        #     # Balanced accuracy = (TPR + TNR) / 2

        #     hs2_tp = self.test_hs2_confusion_matrix['ref=1:pre=1']
        #     hs2_fp = self.test_hs2_confusion_matrix['ref=0:pre=1']
        #     hs2_fn = self.test_hs2_confusion_matrix['ref=1:pre=0']
        #     hs2_tn = self.test_hs2_confusion_matrix['ref=0:pre=0']
        #     hs2_balanced_accuracy = (hs2_tp / (hs2_tp + hs2_fn) + hs2_tn / (hs2_tn + hs2_fp)) / 2

        #     pred_sh2_tp = self.test_pred_shift2_confusion_matrix['ref=1:pre=1']
        #     pred_sh2_fp = self.test_pred_shift2_confusion_matrix['ref=0:pre=1']
        #     pred_sh2_fn = self.test_pred_shift2_confusion_matrix['ref=1:pre=0']
        #     pred_sh2_tn = self.test_pred_shift2_confusion_matrix['ref=0:pre=0']
        #     pred_sh2_balanced_accuracy = (pred_sh2_tp / (pred_sh2_tp + pred_sh2_fn) + pred_sh2_tn / (pred_sh2_tn + pred_sh2_fp)) / 2

        #     pred_bc2_tp = self.test_pred_backchannel2_confusion_matrix['ref=1:pre=1']
        #     pred_bc2_fp = self.test_pred_backchannel2_confusion_matrix['ref=0:pre=1']
        #     pred_bc2_fn = self.test_pred_backchannel2_confusion_matrix['ref=1:pre=0']
        #     pred_bc2_tn = self.test_pred_backchannel2_confusion_matrix['ref=0:pre=0']
        #     pred_bc2_balanced_accuracy = (pred_bc2_tp / (pred_bc2_tp + pred_bc2_fn) + pred_bc2_tn / (pred_bc2_tn + pred_bc2_fp)) / 2

        #     self.log(f"{split}_hs2_balanced_accuracy", hs2_balanced_accuracy, sync_dist=True)
        #     self.log(f"{split}_pred_sh2_balanced_accuracy", pred_sh2_balanced_accuracy, sync_dist=True)
        #     self.log(f"{split}_pred_bc2_balanced_accuracy", pred_bc2_balanced_accuracy, sync_dist=True)
        
        candidate_dict = {
            "acc_{}_hs".format(split):metrics['acc']['hs'],
            "acc_{}_hs2".format(split):metrics['acc']['hs2'],
            "acc_{}_hs_total".format(split):metrics['acc']['hs_total'],
            "acc_{}_sp".format(split):metrics['acc']['sp'],
            "acc_{}_sp2".format(split):metrics['acc']['sp2'],
            "acc_{}_sp_total".format(split):metrics['acc']['sp_total'],
            "acc_{}_bp".format(split):metrics['acc']['bp'],
            "acc_{}_bp2".format(split):metrics['acc']['bp2'],
            "acc_{}_bp_total".format(split):metrics['acc']['bp_total'],
            "acc_{}_ls".format(split):metrics['acc']['ls'],
            "f1_{}_hs".format(split):metrics['f1']['hs'],
            "f1_{}_hs2".format(split):metrics['f1']['hs2'],
            "f1_{}_sp".format(split):metrics['f1']['sp'],
            "f1_{}_bp".format(split):metrics['f1']['bp'],
            "f1_{}_sp2".format(split):metrics['f1']['sp2'],
            "f1_{}_bp2".format(split):metrics['f1']['bp2'],
            "f1_{}_ls".format(split):metrics['f1']['ls'],
        }
        
        # NEW: Add backchannel metrics if enabled (Two-Head Architecture)
        if self.conf.predict_backchannels:
            bc_metrics = {}
            
            # Stage 1: Detection metrics
            if self.conf.bc_detection_head:
                bc_metrics.update({
                    "acc_{}_bc_detection".format(split): metrics['acc']['bc_detection'],
                    "f1_{}_bc_detection".format(split): metrics['f1']['bc_detection'],
                })
            
            # Stage 2: Type classification metrics
            if self.conf.bc_type_head:
                bc_metrics.update({
                    "acc_{}_bc_type".format(split): metrics['acc']['bc_type'],
                    "f1_{}_bc_type".format(split): metrics['f1']['bc_type'],
                    "f1_{}_bc_reactive".format(split): metrics['f1']['bc_reactive'],
                    "f1_{}_bc_emotional".format(split): metrics['f1']['bc_emotional'],
                })
            
            
            candidate_dict.update(bc_metrics)
        
        # NEW: Add head nod metrics if enabled (Two-Head Architecture)
        if self.conf.predict_head_nods:
            hn_metrics = {}
            
            # Stage 1: Detection metrics
            if self.conf.hn_detection_head:
                hn_metrics.update({
                    "acc_{}_hn_detection".format(split): metrics['acc']['hn_detection'],
                    "f1_{}_hn_detection".format(split): metrics['f1']['hn_detection'],
                })
            
            # Stage 2: Type classification metrics
            if self.conf.hn_type_head:
                hn_metrics.update({
                    "acc_{}_hn_type".format(split): metrics['acc']['hn_type'],
                    "f1_{}_hn_type".format(split): metrics['f1']['hn_type'],
                    "f1_{}_hn_short".format(split): metrics['f1']['hn_short'],
                    "f1_{}_hn_long".format(split): metrics['f1']['hn_long'],
                    "f1_{}_hn_long_p".format(split): metrics['f1']['hn_long_p'],
                })
            
            
            candidate_dict.update(hn_metrics)
        
        # Add RTF (Real-Time Factor) for test split
        if split == "test":
            rtf_value = self.compute_rtf()
            candidate_dict["rtf_{}".format(split)] = rtf_value
        
        log_target_dict = {}
        for key, metric in candidate_dict.items():
            # print(key, candidate_dict[key].update_count)
            if isinstance(metric, Metric):
                try:
                    value = metric.compute()
                    if value is not None and not (isinstance(value, float) and math.isnan(value)):
                        log_target_dict[key] = metric
                except (ValueError, RuntimeError):  # compute() fails if no updates
                    continue
            else:
                log_target_dict[key] = metric
        
        self.log_dict(log_target_dict, batch_size=batch_size, on_epoch=True, sync_dist=True)
        
        # print()
        # print('### Metrics logged ###')
        # pp.pprint(log_target_dict)
        # print('#####################')
        # print()

        # for key in metrics["acc"].keys():
        #     print('acc', key, metrics["acc"][key].compute())
        # for key in metrics["f1"].keys():
        #     print('f1', key, metrics["acc"][key].compute())
        # input()


    def _compute_combined_backchannel_metrics(self, preds, targets):
        """
        Compute combined backchannel metrics that consider both timing (bp) and type (bc).
        
        Combined classes:
        0: no_bc (neither timing nor type prediction)
        1: timing_only (bp predicts backchannel but bc predicts no_bc)
        2: reactive (bp and bc both predict reactive backchannel) 
        3: emotional (bp and bc both predict emotional backchannel)
        """
        if "bp" not in preds or "bp" not in targets:
            return None, None
            
        bp_preds = preds["bp"]  # Binary: 0=no_bc, 1=bc
        bc_preds = preds["bc"]  # 3-class: 0=no_bc, 1=reactive, 2=emotional
        bp_targets = targets["bp"]
        bc_targets = targets["bc"]
        
        # Ensure same length
        min_len = min(len(bp_preds), len(bc_preds), len(bp_targets), len(bc_targets))
        bp_preds = bp_preds[:min_len]
        bc_preds = bc_preds[:min_len]
        bp_targets = bp_targets[:min_len]
        bc_targets = bc_targets[:min_len]
        
        # Compute combined predictions
        combined_preds = torch.zeros_like(bp_preds)
        combined_targets = torch.zeros_like(bp_targets)
        
        # For predictions: combine bp and bc
        for i in range(min_len):
            if bp_preds[i] == 0:  # No backchannel predicted by timing
                combined_preds[i] = 0  # no_bc
            elif bc_preds[i] == 0:  # Timing says yes, but type says no
                combined_preds[i] = 1  # timing_only
            elif bc_preds[i] == 1:  # Timing yes, type reactive
                combined_preds[i] = 2  # reactive
            elif bc_preds[i] == 2:  # Timing yes, type emotional
                combined_preds[i] = 3  # emotional
                
        # For targets: combine bp and bc ground truth
        for i in range(min_len):
            if bp_targets[i] == 0:  # No backchannel in ground truth
                combined_targets[i] = 0  # no_bc
            elif bc_targets[i] == 0:  # Timing says yes, but type says no
                combined_targets[i] = 1  # timing_only
            elif bc_targets[i] == 1:  # Timing yes, type reactive
                combined_targets[i] = 2  # reactive  
            elif bc_targets[i] == 2:  # Timing yes, type emotional
                combined_targets[i] = 3  # emotional
                
        return combined_preds, combined_targets
    
    def _compute_conditional_backchannel_metrics(self, preds, targets):
        """
        Compute conditional backchannel type accuracy.
        Only considers frames where ground truth has an actual backchannel (reactive or emotional).
        Excludes no_bc frames to focus purely on type classification performance.
        """
        bc_preds = preds["bc"]  # 3-class: 0=no_bc, 1=reactive, 2=emotional
        bc_targets = targets["bc"]
        
        # Find frames where target is actually a backchannel (not no_bc)
        actual_bc_mask = bc_targets > 0
        
        if actual_bc_mask.sum() == 0:
            return None, None  # No actual backchannels in this batch
            
        # Extract only frames with actual backchannels
        conditional_preds = bc_preds[actual_bc_mask]
        conditional_targets = bc_targets[actual_bc_mask]
        
        # Convert to binary: 1=reactive, 2=emotional -> 0=reactive, 1=emotional
        conditional_preds = conditional_preds - 1  # 1->0, 2->1
        conditional_targets = conditional_targets - 1  # 1->0, 2->1
        
        # Clamp to valid range [0, 1]
        conditional_preds = torch.clamp(conditional_preds, 0, 1)
        conditional_targets = torch.clamp(conditional_targets, 0, 1)
        
        return conditional_preds, conditional_targets
    
    def _compute_individual_backchannel_metrics(self, preds, targets):
        """
        Compute individual binary classification metrics for each backchannel type.
        POSITIVE CLASSES ONLY - no_bc class removed
        Converts multiclass problem into multiple binary classification problems:
        - reactive: class 0 vs others (1) 
        - emotional: class 1 vs others (0)
        """
        bc_preds = preds["bc"]  # 2-class: 0=reactive, 1=emotional
        bc_targets = targets["bc"]
        
        individual_metrics = {}
        
        # Class 0: reactive vs others  
        reactive_preds = (bc_preds == 0).long()  # 1 if predicted reactive, 0 otherwise
        reactive_targets = (bc_targets == 0).long()  # 1 if actual reactive, 0 otherwise
        individual_metrics["reactive"] = (reactive_preds, reactive_targets)
        
        # Class 1: emotional vs others
        emotional_preds = (bc_preds == 1).long()  # 1 if predicted emotional, 0 otherwise  
        emotional_targets = (bc_targets == 1).long()  # 1 if actual emotional, 0 otherwise
        individual_metrics["emotional"] = (emotional_preds, emotional_targets)
        
        return individual_metrics

    def _compute_individual_head_nod_metrics(self, preds, targets):
        """
        Compute individual binary classification metrics for each head nod type.
        POSITIVE CLASSES ONLY - no_nod class removed
        Converts multiclass problem into multiple binary classification problems:
        - short: class 0 vs others (1,2) 
        - long: class 1 vs others (0,2)
        - long_p: class 2 vs others (0,1)
        """
        hn_preds = preds["hn"]  # 3-class: 0=short, 1=long, 2=long_p
        hn_targets = targets["hn"]
        
        individual_metrics = {}
        
        # Class 0: short vs others  
        short_preds = (hn_preds == 0).long()  # 1 if predicted short, 0 otherwise
        short_targets = (hn_targets == 0).long()  # 1 if actual short, 0 otherwise
        individual_metrics["short"] = (short_preds, short_targets)
        
        # Class 1: long vs others
        long_preds = (hn_preds == 1).long()  # 1 if predicted long, 0 otherwise  
        long_targets = (hn_targets == 1).long()  # 1 if actual long, 0 otherwise
        individual_metrics["long"] = (long_preds, long_targets)
        
        # Class 2: long_p vs others
        long_p_preds = (hn_preds == 2).long()  # 1 if predicted long_p, 0 otherwise  
        long_p_targets = (hn_targets == 2).long()  # 1 if actual long_p, 0 otherwise
        individual_metrics["long_p"] = (long_p_preds, long_p_targets)
        
        return individual_metrics

    def shared_step(
        self, batch: Dict, reduction: str = "mean"
    ) -> Dict[str, torch.Tensor]:
        """
        Arguments:
            batch:      dict, containing 'waveform', va, va_history

        Returns:
            out:        dict, ['logits', 'vad', 'loss_vap', 'loss_vad']
        """
        
        labels = self.objective.get_labels(batch["vad"].to(self.device))
        
        # pp.pprint(batch)
        
        if self.multimodal and self.use_face_encoder:
            out = self(src = {"waveform":batch["waveform"].to(self.device),
                              "gaze1":batch["gaze1"].to(self.device),
                              "head1":batch["head1"].to(self.device),
                              "face1":batch["face1"].to(self.device),
                              "body1":batch["body1"].to(self.device),
                              "face_im1":batch["face_im1"].to(self.device),
                              "gaze2":batch["gaze2"].to(self.device),
                              "head2":batch["head2"].to(self.device),
                              "face2":batch["face2"].to(self.device),
                              "body2":batch["body2"].to(self.device),
                              "face_im2":batch["face_im2"].to(self.device),
                              }
                       )
        elif self.multimodal and not self.use_face_encoder:
            out = self(src = {"waveform":batch["waveform"].to(self.device),
                              "gaze1":batch["gaze1"].to(self.device),
                              "head1":batch["head1"].to(self.device),
                              "face1":batch["face1"].to(self.device),
                              "body1":batch["body1"].to(self.device),
                              "gaze2":batch["gaze2"].to(self.device),
                              "head2":batch["head2"].to(self.device),
                              "face2":batch["face2"].to(self.device),
                              "body2":batch["body2"].to(self.device),
                              }
                       )
        else:
            out = self(waveform=batch["waveform"].to(self.device))

        
        # print(batch['waveform'].shape)

        out["loss_vap"] = self.objective.loss_vap(out["logits"], labels, reduction=reduction)
        out["loss_vad"] = self.objective.loss_vad(out["vad"], batch["vad"])
        
        # NEW: Two-Head Backchannel loss computation
        if self.conf.predict_backchannels and "bc_detection_logits" in out and "backchannel_labels" in batch:
            # Get backchannel targets
            bc_targets = batch["backchannel_labels"].to(self.device)  # [B, T] (-1=no_bc, 0=reactive, 1=emotional)
            
            # Apply prediction shift (default 500ms, configurable)
            bc_targets = self.apply_prediction_shift(
                bc_targets, 
                shift_ms=self.conf.backchannel_prediction_shift_ms,
                frame_hz=self.conf.frame_hz
            )
            
            # Stage 1: BC Detection Loss (no_bc vs any_bc)
            if self.conf.bc_detection_head and "bc_detection_logits" in out:
                bc_detection_logits = out["bc_detection_logits"]  # [B, T, 2]
                
                # Ensure shapes match
                if bc_detection_logits.shape[1] != bc_targets.shape[1]:
                    min_length = min(bc_detection_logits.shape[1], bc_targets.shape[1])
                    bc_detection_logits = bc_detection_logits[:, :min_length, :]
                    bc_targets_truncated = bc_targets[:, :min_length]
                else:
                    bc_targets_truncated = bc_targets
                
                # Convert BC targets to detection targets: -1 → 0 (no_bc), >=0 → 1 (any_bc)
                bc_detection_targets = (bc_targets_truncated >= 0).long()  # [B, T]
                
                # Compute detection loss with class weights
                detection_weights = torch.tensor(self.conf.bc_detection_class_weights, device=self.device)
                loss_bc_detection = F.cross_entropy(
                    bc_detection_logits.view(-1, 2),
                    bc_detection_targets.view(-1),
                    weight=detection_weights,
                    reduction=reduction
                )
                out["loss_bc_detection"] = loss_bc_detection
            else:
                out["loss_bc_detection"] = torch.tensor(0.0, device=self.device)
            
            # Stage 2: BC Type Classification Loss (reactive vs emotional) - only on BC frames
            if self.conf.bc_type_head and "bc_type_logits" in out:
                bc_type_logits = out["bc_type_logits"]  # [B, T, 2]
                
                # Ensure shapes match
                if bc_type_logits.shape[1] != bc_targets.shape[1]:
                    min_length = min(bc_type_logits.shape[1], bc_targets.shape[1])
                    bc_type_logits = bc_type_logits[:, :min_length, :]
                    bc_targets_type = bc_targets[:, :min_length]
                else:
                    bc_targets_type = bc_targets
                
                # Only compute loss on frames with actual BC events (bc_targets >= 0)
                bc_mask = bc_targets_type >= 0
                
                if bc_mask.any():
                    bc_type_logits_masked = bc_type_logits[bc_mask]  # [N, 2] where N = number of BC frames
                    bc_type_targets_masked = bc_targets_type[bc_mask]  # [N]
                    
                    # Compute type classification loss with class weights
                    type_weights = torch.tensor(self.conf.bc_type_class_weights, device=self.device)
                    loss_bc_type = F.cross_entropy(
                        bc_type_logits_masked,
                        bc_type_targets_masked,
                        weight=type_weights,
                        reduction=reduction
                    )
                    out["loss_bc_type"] = loss_bc_type
                else:
                    # No BC frames in this batch
                    out["loss_bc_type"] = torch.tensor(0.0, device=self.device)
            else:
                out["loss_bc_type"] = torch.tensor(0.0, device=self.device)
            
            # Total BC loss = detection loss + type loss (weighted)
            out["loss_backchannel"] = (self.conf.bc_detection_weight * out["loss_bc_detection"] + 
                                      self.conf.bc_type_weight * out["loss_bc_type"])
        else:
            out["loss_backchannel"] = torch.tensor(0.0, device=self.device)
            out["loss_bc_detection"] = torch.tensor(0.0, device=self.device)
            out["loss_bc_type"] = torch.tensor(0.0, device=self.device)
        
        # NEW: Head nod loss computation - TWO-HEAD ARCHITECTURE (if enabled)
        if self.conf.predict_head_nods and "head_nod_labels" in batch:
            hn_targets = batch["head_nod_labels"].to(self.device)  # [B, T] (-1=no_nod, 0=short, 1=long, 2=long_p)
            
            # Apply prediction shift (same as backchannel)
            hn_targets = self.apply_prediction_shift(
                hn_targets,
                shift_ms=self.conf.head_nod_prediction_shift_ms,
                frame_hz=self.conf.frame_hz
            )
            
            # Stage 1: HN Detection Loss (no_nod vs any_nod)
            if self.conf.hn_detection_head and "hn_detection_logits" in out:
                hn_detection_logits = out["hn_detection_logits"]  # [B, T, 2]
                
                # Ensure shapes match (handle context truncation)
                if hn_detection_logits.shape[1] != hn_targets.shape[1]:
                    min_length = min(hn_detection_logits.shape[1], hn_targets.shape[1])
                    hn_detection_logits = hn_detection_logits[:, :min_length, :]
                    hn_targets_truncated = hn_targets[:, :min_length]
                else:
                    hn_targets_truncated = hn_targets
                
                # Convert HN targets to detection targets: -1 → 0 (no_nod), >=0 → 1 (any_nod)
                hn_detection_targets = (hn_targets_truncated >= 0).long()  # [B, T]
                
                # Compute detection loss with class weights
                detection_weights = torch.tensor(self.conf.hn_detection_class_weights, device=self.device)
                loss_hn_detection = F.cross_entropy(
                    hn_detection_logits.view(-1, 2),
                    hn_detection_targets.view(-1),
                    weight=detection_weights,
                    reduction=reduction
                )
                out["loss_hn_detection"] = loss_hn_detection
            else:
                out["loss_hn_detection"] = torch.tensor(0.0, device=self.device)
            
            # Stage 2: HN Type Classification Loss (short, long, long_p) - only on HN frames
            if self.conf.hn_type_head and "hn_type_logits" in out:
                hn_type_logits = out["hn_type_logits"]  # [B, T, 3]
                
                # Ensure shapes match
                if hn_type_logits.shape[1] != hn_targets.shape[1]:
                    min_length = min(hn_type_logits.shape[1], hn_targets.shape[1])
                    hn_type_logits = hn_type_logits[:, :min_length, :]
                    hn_targets_type = hn_targets[:, :min_length]
                else:
                    hn_targets_type = hn_targets
                
                # Only compute loss on frames with actual HN events (hn_targets >= 0)
                hn_mask = hn_targets_type >= 0
                
                if hn_mask.any():
                    hn_type_logits_masked = hn_type_logits[hn_mask]  # [N, 3] where N = number of HN frames
                    hn_type_targets_masked = hn_targets_type[hn_mask]  # [N]
                    
                    # Compute type classification loss with class weights
                    type_weights = torch.tensor(self.conf.hn_type_class_weights, device=self.device)
                    loss_hn_type = F.cross_entropy(
                        hn_type_logits_masked,
                        hn_type_targets_masked,
                        weight=type_weights,
                        reduction=reduction
                    )
                    out["loss_hn_type"] = loss_hn_type
                else:
                    # No HN frames in this batch
                    out["loss_hn_type"] = torch.tensor(0.0, device=self.device)
            else:
                out["loss_hn_type"] = torch.tensor(0.0, device=self.device)
            
            # Total HN loss = detection loss + type loss (weighted)
            out["loss_head_nod"] = (self.conf.hn_detection_weight * out["loss_hn_detection"] + 
                                   self.conf.hn_type_weight * out["loss_hn_type"])
        else:
            out["loss_head_nod"] = torch.tensor(0.0, device=self.device)
            out["loss_hn_detection"] = torch.tensor(0.0, device=self.device)
            out["loss_hn_type"] = torch.tensor(0.0, device=self.device)

        return out
    
    def training_step(self, batch, batch_idx, **kwargs):
        
        out = self.shared_step(batch)
        batch_size = batch["waveform"].shape[0]

        self.log("loss_train_vap", out["loss_vap"], 
                 batch_size=batch_size, on_epoch=True, on_step=True, sync_dist=True, 
                 logger=self.opt_conf.logger if (self.opt_conf != None) else True)
        self.log("loss_train_vad", out["loss_vad"],
                 batch_size=batch_size,  on_epoch=True, on_step=True, sync_dist=True, 
                 logger=self.opt_conf.logger if (self.opt_conf != None) else True)
        
        # NEW: Log backchannel losses (two-head architecture)
        if self.conf.predict_backchannels:
            self.log("loss_train_backchannel", out["loss_backchannel"],
                     batch_size=batch_size, on_epoch=True, on_step=True, sync_dist=True,
                     logger=self.opt_conf.logger if (self.opt_conf != None) else True)
            self.log("loss_train_bc_detection", out["loss_bc_detection"],
                     batch_size=batch_size, on_epoch=True, on_step=True, sync_dist=True,
                     logger=self.opt_conf.logger if (self.opt_conf != None) else True)
            self.log("loss_train_bc_type", out["loss_bc_type"],
                     batch_size=batch_size, on_epoch=True, on_step=True, sync_dist=True,
                     logger=self.opt_conf.logger if (self.opt_conf != None) else True)
        
        # NEW: Log head nod losses (two-head architecture)
        if self.conf.predict_head_nods:
            self.log("loss_train_head_nod", out["loss_head_nod"],
                     batch_size=batch_size, on_epoch=True, on_step=True, sync_dist=True,
                     logger=self.opt_conf.logger if (self.opt_conf != None) else True)
            self.log("loss_train_hn_detection", out["loss_hn_detection"],
                     batch_size=batch_size, on_epoch=True, on_step=True, sync_dist=True,
                     logger=self.opt_conf.logger if (self.opt_conf != None) else True)
            self.log("loss_train_hn_type", out["loss_hn_type"],
                     batch_size=batch_size, on_epoch=True, on_step=True, sync_dist=True,
                     logger=self.opt_conf.logger if (self.opt_conf != None) else True)
        
        # NEW: Multi-task total loss with two-head BC and HN architectures
        total_loss = out["loss_vap"] + out["loss_vad"]
        if self.conf.predict_backchannels:
            # Two-head BC: detection + type (already weighted in loss_backchannel)
            total_loss += out["loss_backchannel"]
        if self.conf.predict_head_nods:
            # Two-head HN: detection + type (already weighted in loss_head_nod)
            total_loss += out["loss_head_nod"]
        
        self.log("loss_train_total", total_loss,
                 batch_size=batch_size, on_epoch=True, on_step=True, sync_dist=True,
                 logger=self.opt_conf.logger if (self.opt_conf != None) else True)

        return {"loss": total_loss}

    def validation_step(self, batch, batch_idx, **kwargs):
        
        """validation step"""
        if not hasattr(self, "val_metrics"):
            self.val_metrics = self.get_metrics()

            for name, events in self.val_metrics.items():
                for event, metric in events.items():
                    strname = f"val_{name}_{event}"
                    self.register_module(strname, metric)

        out = self.shared_step(batch)
        batch_size = batch["waveform"].shape[0]
        
        self.log("loss_val_vap", out["loss_vap"], 
                 batch_size=batch_size, on_epoch=True, on_step=True, sync_dist=True, 
                 logger=self.opt_conf.logger if (self.opt_conf != None) else True)
        self.log("loss_val_vad", out["loss_vad"],
                 batch_size=batch_size,  on_epoch=True, on_step=True, sync_dist=True, 
                 logger=self.opt_conf.logger if (self.opt_conf != None) else True)
        
        # NEW: Log validation backchannel losses (two-head architecture)
        if self.conf.predict_backchannels:
            self.log("loss_val_backchannel", out["loss_backchannel"],
                     batch_size=batch_size, on_epoch=True, on_step=True, sync_dist=True,
                     logger=self.opt_conf.logger if (self.opt_conf != None) else True)
            self.log("loss_val_bc_detection", out["loss_bc_detection"],
                     batch_size=batch_size, on_epoch=True, on_step=True, sync_dist=True,
                     logger=self.opt_conf.logger if (self.opt_conf != None) else True)
            self.log("loss_val_bc_type", out["loss_bc_type"],
                     batch_size=batch_size, on_epoch=True, on_step=True, sync_dist=True,
                     logger=self.opt_conf.logger if (self.opt_conf != None) else True)
        
        # NEW: Log validation head nod losses (two-head architecture)
        if self.conf.predict_head_nods:
            self.log("loss_val_head_nod", out["loss_head_nod"],
                     batch_size=batch_size, on_epoch=True, on_step=True, sync_dist=True,
                     logger=self.opt_conf.logger if (self.opt_conf != None) else True)
            self.log("loss_val_hn_detection", out["loss_hn_detection"],
                     batch_size=batch_size, on_epoch=True, on_step=True, sync_dist=True,
                     logger=self.opt_conf.logger if (self.opt_conf != None) else True)
            self.log("loss_val_hn_type", out["loss_hn_type"],
                     batch_size=batch_size, on_epoch=True, on_step=True, sync_dist=True,
                     logger=self.opt_conf.logger if (self.opt_conf != None) else True)
        
        # NEW: Multi-task total validation loss with two-head BC and HN architectures
        total_val_loss = out["loss_vap"] + out["loss_vad"]
        if self.conf.predict_backchannels:
            # Two-head BC: detection + type (already weighted in loss_backchannel)
            total_val_loss += out["loss_backchannel"]
        if self.conf.predict_head_nods:
            # Two-head HN: detection + type (already weighted in loss_head_nod)
            total_val_loss += out["loss_head_nod"]
        
        self.log("loss_val_total", total_val_loss,
                 batch_size=batch_size, on_epoch=True, on_step=True, sync_dist=True,
                 logger=self.opt_conf.logger if (self.opt_conf != None) else True)

        

        # self.log("val_loss_step", out["loss_vap"], on_step=True, sync_dist=True)
        # self.log("val_loss_va_step", out["loss_vad"], on_step=True, sync_dist=True)

        # Event Metrics
        if self.event_extractor is not None:
            events = self.event_extractor(batch["vad"])
            # probs = self.zero_shot.get_probs(out["logits"], batch["vad"])
            # preds, targets = self.zero_shot.extract_prediction_and_targets(
            #     p=probs["p"], p_bc=probs["p_bc"], events=events
            # )    
            probs = self.objective.get_probs(out["logits"])
            preds, targets = self.objective.extract_prediction_and_targets(
                p_now=probs["p_now"], p_fut=probs["p_future"], events=events
            )
            
            # NEW: Add backchannel predictions and targets
            if self.conf.predict_backchannels and "bc_detection_probs" in out and "backchannel_labels" in batch:
                bc_targets = batch["backchannel_labels"]  # [B, T] (-1=no_bc, 0=reactive, 1=emotional)
                
                # Apply same prediction shift as in training for fair evaluation
                bc_targets = self.apply_backchannel_prediction_shift(
                    bc_targets, 
                    shift_ms=self.conf.backchannel_prediction_shift_ms,
                    frame_hz=self.conf.frame_hz
                )
                
                # Stage 1: BC Detection predictions (no_bc vs any_bc)
                bc_detection_preds = None  # Initialize for cascade logic in type classification
                if self.conf.bc_detection_head and "bc_detection_probs" in out:
                    bc_detection_probs = out["bc_detection_probs"]  # [B, T, 2]
                    
                    # Align shapes if needed
                    if bc_detection_probs.shape[1] != bc_targets.shape[1]:
                        min_length = min(bc_detection_probs.shape[1], bc_targets.shape[1])
                        bc_detection_probs = bc_detection_probs[:, :min_length, :]
                        bc_targets_aligned = bc_targets[:, :min_length]
                    else:
                        bc_targets_aligned = bc_targets
                    
                    # Convert targets to detection targets: -1 → 0 (no_bc), >=0 → 1 (any_bc)
                    bc_detection_targets = (bc_targets_aligned >= 0).long()  # [B, T]
                    
                    # Get predictions
                    bc_detection_preds = torch.argmax(bc_detection_probs, dim=-1)  # [B, T]
                    
                    preds["bc_detection"] = bc_detection_preds.flatten()
                    targets["bc_detection"] = bc_detection_targets.flatten()
                
                # Stage 2: BC Type Classification predictions (reactive vs emotional) - only on BC frames
                if self.conf.bc_type_head and "bc_type_probs" in out:
                    bc_type_probs = out["bc_type_probs"]  # [B, T, 2]
                    
                    # Align shapes if needed
                    if bc_type_probs.shape[1] != bc_targets.shape[1]:
                        min_length = min(bc_type_probs.shape[1], bc_targets.shape[1])
                        bc_type_probs = bc_type_probs[:, :min_length, :]
                        bc_targets_type = bc_targets[:, :min_length]
                    else:
                        bc_targets_type = bc_targets
                    
                    # Get type predictions for all frames
                    bc_type_preds = torch.argmax(bc_type_probs, dim=-1)  # [B, T]
                    
                    # CASCADE: Apply detection decision first (match inference logic!)
                    # Only use type predictions where detection head predicts BC (detection=1)
                    # Get detection predictions if available
                    if bc_detection_preds is not None:
                        has_bc_detected = bc_detection_preds == 1  # [B, T] - where detection head says "any_bc"
                        
                        # Create cascade predictions: -1 where no BC detected, type_pred where BC detected
                        cascade_bc_type_preds = torch.full_like(bc_targets_type, -1, dtype=torch.long)  # Initialize as -1 (no_bc)
                        cascade_bc_type_preds[has_bc_detected] = bc_type_preds[has_bc_detected]
                    else:
                        # Fallback if detection head not enabled: use type predictions directly
                        cascade_bc_type_preds = bc_type_preds
                    
                    # Flatten for metrics
                    cascade_bc_type_preds_flat = cascade_bc_type_preds.flatten()
                    bc_targets_type_flat = bc_targets_type.flatten().long()
                    
                    # Map -1 → 2 (no_bc) for metrics evaluation on ALL frames
                    # This allows metrics to capture detection failures (false positives and misses)
                    bc_type_preds_mapped = cascade_bc_type_preds_flat.clone()
                    bc_type_preds_mapped[bc_type_preds_mapped == -1] = 2  # Map -1 → 2 (no_bc)
                    
                    bc_targets_type_mapped = bc_targets_type_flat.clone()
                    bc_targets_type_mapped[bc_targets_type_mapped == -1] = 2  # Map -1 → 2 (no_bc)
                    
                    # Evaluate on ALL frames to capture full performance (detection + type)
                    preds["bc_type"] = bc_type_preds_mapped  # All frames: 0=reactive, 1=emotional, 2=no_bc
                    targets["bc_type"] = bc_targets_type_mapped  # All frames: 0=reactive, 1=emotional, 2=no_bc
            
            # NEW: Add head nod predictions and targets - TWO-HEAD ARCHITECTURE
            if self.conf.predict_head_nods and "hn_detection_probs" in out and "head_nod_labels" in batch:
                hn_targets = batch["head_nod_labels"]  # [B, T] (-1=no_nod, 0=short, 1=long, 2=long_p)
                
                # Stage 1: HN Detection predictions (no_nod vs any_nod)
                hn_detection_preds = None  # Initialize for cascade logic in type classification
                if self.conf.hn_detection_head and "hn_detection_probs" in out:
                    hn_detection_probs = out["hn_detection_probs"]  # [B, T, 2]
                    
                    # Align shapes if needed
                    if hn_detection_probs.shape[1] != hn_targets.shape[1]:
                        min_length = min(hn_detection_probs.shape[1], hn_targets.shape[1])
                        hn_detection_probs = hn_detection_probs[:, :min_length, :]
                        hn_targets_aligned = hn_targets[:, :min_length]
                    else:
                        hn_targets_aligned = hn_targets
                    
                    # Convert targets to detection targets: -1 → 0 (no_nod), >=0 → 1 (any_nod)
                    hn_detection_targets = (hn_targets_aligned >= 0).long()  # [B, T]
                    
                    # Get predictions
                    hn_detection_preds = torch.argmax(hn_detection_probs, dim=-1)  # [B, T]
                    
                    preds["hn_detection"] = hn_detection_preds.flatten()
                    targets["hn_detection"] = hn_detection_targets.flatten()
                
                # Stage 2: HN Type Classification predictions (short, long, long_p) - only on HN frames
                if self.conf.hn_type_head and "hn_type_probs" in out:
                    hn_type_probs = out["hn_type_probs"]  # [B, T, 3]
                    
                    # Align shapes if needed
                    if hn_type_probs.shape[1] != hn_targets.shape[1]:
                        min_length = min(hn_type_probs.shape[1], hn_targets.shape[1])
                        hn_type_probs = hn_type_probs[:, :min_length, :]
                        hn_targets_type = hn_targets[:, :min_length]
                    else:
                        hn_targets_type = hn_targets
                    
                    # Get type predictions for all frames
                    hn_type_preds = torch.argmax(hn_type_probs, dim=-1)  # [B, T]
                    
                    # CASCADE: Apply detection decision first (match inference logic!)
                    # Only use type predictions where detection head predicts HN (detection=1)
                    # Get detection predictions if available
                    if hn_detection_preds is not None:
                        has_hn_detected = hn_detection_preds == 1  # [B, T] - where detection head says "any_nod"
                        
                        # Create cascade predictions: -1 where no HN detected, type_pred where HN detected
                        cascade_hn_type_preds = torch.full_like(hn_targets_type, -1, dtype=torch.long)  # Initialize as -1 (no_nod)
                        cascade_hn_type_preds[has_hn_detected] = hn_type_preds[has_hn_detected]
                    else:
                        # Fallback if detection head not enabled: use type predictions directly
                        cascade_hn_type_preds = hn_type_preds
                    
                    # Flatten for metrics
                    cascade_hn_type_preds_flat = cascade_hn_type_preds.flatten()
                    hn_targets_type_flat = hn_targets_type.flatten().long()
                    
                    # Map -1 → 3 (no_nod) for metrics evaluation on ALL frames
                    # This allows metrics to capture detection failures (false positives and misses)
                    hn_type_preds_mapped = cascade_hn_type_preds_flat.clone()
                    hn_type_preds_mapped[hn_type_preds_mapped == -1] = 3  # Map -1 → 3 (no_nod)
                    
                    hn_targets_type_mapped = hn_targets_type_flat.clone()
                    hn_targets_type_mapped[hn_targets_type_mapped == -1] = 3  # Map -1 → 3 (no_nod)
                    
                    # Evaluate on ALL frames to capture full performance (detection + type)
                    preds["hn_type"] = hn_type_preds_mapped  # All frames: 0=short, 1=long, 2=long_p, 3=no_nod
                    targets["hn_type"] = hn_targets_type_mapped  # All frames: 0=short, 1=long, 2=long_p, 3=no_nod
            
            # print(probs)
            # print(preds)
            # print(targets)
            # input('Check1')
            
            self.metrics_step(preds, targets, split="val")

    def on_validation_epoch_end(self, *_):
        
        if hasattr(self, "val_metrics"):
            self.metrics_epoch("val")

        avg_loss = self.trainer.callback_metrics['loss_val_vap']
        print(f"Epoch {self.current_epoch}: Validation vap Loss = {avg_loss}")

    def test_step(self, batch, batch_idx, **kwargs):

        """validation step"""
        if not hasattr(self, "test_metrics"):
            self.test_metrics = self.get_metrics()

            for name, events in self.test_metrics.items():
                for event, metric in events.items():
                    strname = f"test_{name}_{event}"
                    self.register_module(strname, metric)
        
        # if not hasattr(self, "test_sh_metrics_conf"):
        #     self.test_sh_metrics_conf = MulticlassConfusionMatrix(2)

        #
        # Perterbation
        #
        
        # import torchaudio
        # torchaudio.save("flatintensity_sample-before.wav", batch["waveform"][0, :, :].cpu(), 16000)

        # #pert = FlatIntensity(min_intensity=20)
        # if self.test_perturbation == 1:
        #     pert = FlatPitch()
        #     batch["waveform"] = pert(batch["waveform"])
        
        # if self.test_perturbation == 2:

        #     # import torchaudio
        #     # torchaudio.save("lowpass_sample-before.wav", batch["waveform"][0, :, :].cpu(), 16000)

        #     pert = LowPass()
        #     batch["waveform"] = pert(batch["waveform"])

        #     # # import torchaudio
        #     # torchaudio.save("lowpass_sample-after.wav", batch["waveform"][0, :, :].cpu(), 16000)
        
        # pert = FlatPitch()
        # batch["waveform"] = pert(batch["waveform"])
        
        # # torchaudio.save("flatintensity_sample-after.wav", batch["waveform"][0, :, :].cpu(), 16000)
        # # input()

        # Measure inference time for RTF calculation
        batch_size = batch["waveform"].shape[0]
        samples_per_audio = batch["waveform"].shape[2] 
        sample_rate = getattr(self.conf, 'sample_rate', 16000)  # Use actual sample rate
        
        # Calculate audio duration per sample
        audio_duration_per_sample = samples_per_audio / sample_rate
        
        torch.cuda.synchronize() if torch.cuda.is_available() else None
        start_time = time.time()
        
        # Only measure model forward pass, not loss computation
        with torch.no_grad():
            out = self.forward(batch)
        
        torch.cuda.synchronize() if torch.cuda.is_available() else None
        end_time = time.time()
        
        inference_time = end_time - start_time
        
        # Store for RTF calculation - GPU BATCH PARALLELIZATION APPROACH
        # On GPU, batch processing is parallelized, so batch_time ≈ single_sample_time
        # RTF should be: batch_inference_time / audio_duration_per_sample
        # This reflects the actual time to process 20s of audio with GPU parallelization
        
        self.inference_times.append(inference_time)  # Full batch time
        self.audio_durations.append(audio_duration_per_sample)  # 20s per sample
        
        # Continue with normal shared_step for training
        out = self.shared_step(batch)
        
        # self.test_inference_time_all.append(out["inf_time"])
        
        # self.log("loss_test_vap", out["loss_vap"], 
        #          batch_size=batch_size, on_epoch=True, on_step=True, sync_dist=True, 
        #          logger=self.opt_conf.logger if (self.opt_conf != None) else True)
        # self.log("loss_test_vap", out["loss_vad"],
        #          batch_size=batch_size,  on_epoch=True, on_step=True, sync_dist=True, 
        #          logger=self.opt_conf.logger if (self.opt_conf != None) else True)

        # Event Metrics
        if self.event_extractor is not None:
            
            events = self.event_extractor(batch["vad"])
            
            # probs = self.zero_shot.get_probs(out["logits"], batch["vad"])
            # preds, targets = self.zero_shot.extract_prediction_and_targets(
            #     p=probs["p"], p_bc=probs["p_bc"], events=events
            # )
            
            probs_ojective = self.objective.get_probs(out["logits"])
            # # print(probs["p_now"])
            # # print(events)
            # # input()
            preds_objective, targets_objective = self.objective.extract_prediction_and_targets(
                p_now=probs_ojective["p_now"], p_fut=probs_ojective["p_future"], events=events
            )
            preds = preds_objective
            targets = targets_objective

            #preds["lid"] = preds_objective["lid"]
            #targets["lid"] = targets_objective["lid"]

            # print(batch['session'])
            # print(batch['vad'][0][:, 0].tolist())
            # print(batch['vad'][0][:, 1].tolist())
            # print(len(batch['vad'][0][:, 0].tolist()))
            # print(events)
            # print(events['shift'])
            # print(events['hold'])
            # print(preds['hs2'])
            # print(targets['hs2'])
            # input()

            # if 'hs2' in targets:
            #     if targets['hs2'] is not None:
            #         for r in targets['hs2']:
            #             self.test_hs2_reference[r] += 1
                    
            #         for p, t in zip(preds["hs2"], targets["hs2"]):
            #             if torch.isnan(p) or torch.isnan(t):
            #                 continue
            #             p_ = torch.round(p)
            #             label = "ref=%d:pre=%d" % (t, p_)
            #             self.test_hs2_confusion_matrix[label] += 1
            
            # if 'hs' in targets:
            #     if targets['hs'] is not None:
            #         for r in targets['hs']:
            #             self.test_hs_reference[r] += 1
                    
            #         for p, t in zip(preds["hs"], targets["hs"]):
            #             p_ = torch.round(p)
            #             label = "ref=%d:pre=%d" % (t, p_)
            #             self.test_hs_confusion_matrix[label] += 1
            
            # label_shift = "pred_shift2"
            # if label_shift in targets:
            #     if targets[label_shift] is not None:
            #         for r in targets[label_shift]:
            #             self.test_pred_shift2_reference[r] += 1
                    
            #         for p, t in zip(preds[label_shift], targets[label_shift]):
            #             p_ = torch.round(p)
            #             label = "ref=%d:pre=%d" % (t, p_)
            #             self.test_pred_shift2_confusion_matrix[label] += 1
            
            # label_backchannel = 'pred_backchannel2'
            # if label_backchannel in targets:
            #     if targets[label_backchannel] is not None:
            #         for r in targets[label_backchannel]:
            #             self.test_pred_backchannel2_reference[r] += 1
                    
            #         for p, t in zip(preds[label_backchannel], targets[label_backchannel]):
            #             p_ = torch.round(p)
            #             label = "ref=%d:pre=%d" % (t, p_)
            #             self.test_pred_backchannel2_confusion_matrix[label] += 1

            # print(batch['vad'][0][820-1:838+1, 0].tolist())
            # print(batch['vad'][0][820-1:838+1, 1].tolist())
            # print(preds['hs'])
            # print(targets['hs'])
            # input()
            # #print(batch["vad"])
            # # print(events)
            # # input()

            # NEW: Add backchannel predictions and targets
            if self.conf.predict_backchannels and "bc_detection_probs" in out and "backchannel_labels" in batch:
                bc_targets = batch["backchannel_labels"]  # [B, T] (-1=no_bc, 0=reactive, 1=emotional)
                
                # Apply same prediction shift as in training for fair evaluation
                bc_targets = self.apply_backchannel_prediction_shift(
                    bc_targets, 
                    shift_ms=self.conf.backchannel_prediction_shift_ms,
                    frame_hz=self.conf.frame_hz
                )
                
                # Stage 1: BC Detection predictions (no_bc vs any_bc)
                bc_detection_preds = None  # Initialize for cascade logic in type classification
                if self.conf.bc_detection_head and "bc_detection_probs" in out:
                    bc_detection_probs = out["bc_detection_probs"]  # [B, T, 2]
                    
                    # Align shapes if needed
                    if bc_detection_probs.shape[1] != bc_targets.shape[1]:
                        min_length = min(bc_detection_probs.shape[1], bc_targets.shape[1])
                        bc_detection_probs = bc_detection_probs[:, :min_length, :]
                        bc_targets_aligned = bc_targets[:, :min_length]
                    else:
                        bc_targets_aligned = bc_targets
                    
                    # Convert targets to detection targets: -1 → 0 (no_bc), >=0 → 1 (any_bc)
                    bc_detection_targets = (bc_targets_aligned >= 0).long()  # [B, T]
                    
                    # Get predictions
                    bc_detection_preds = torch.argmax(bc_detection_probs, dim=-1)  # [B, T]
                    
                    preds["bc_detection"] = bc_detection_preds.flatten()
                    targets["bc_detection"] = bc_detection_targets.flatten()
                
                # Stage 2: BC Type Classification predictions (reactive vs emotional) - only on BC frames
                if self.conf.bc_type_head and "bc_type_probs" in out:
                    bc_type_probs = out["bc_type_probs"]  # [B, T, 2]
                    
                    # Align shapes if needed
                    if bc_type_probs.shape[1] != bc_targets.shape[1]:
                        min_length = min(bc_type_probs.shape[1], bc_targets.shape[1])
                        bc_type_probs = bc_type_probs[:, :min_length, :]
                        bc_targets_type = bc_targets[:, :min_length]
                    else:
                        bc_targets_type = bc_targets
                    
                    # Get type predictions for all frames
                    bc_type_preds = torch.argmax(bc_type_probs, dim=-1)  # [B, T]
                    
                    # CASCADE: Apply detection decision first (match inference logic!)
                    # Only use type predictions where detection head predicts BC (detection=1)
                    # Get detection predictions if available
                    if bc_detection_preds is not None:
                        has_bc_detected = bc_detection_preds == 1  # [B, T] - where detection head says "any_bc"
                        
                        # Create cascade predictions: -1 where no BC detected, type_pred where BC detected
                        cascade_bc_type_preds = torch.full_like(bc_targets_type, -1, dtype=torch.long)  # Initialize as -1 (no_bc)
                        cascade_bc_type_preds[has_bc_detected] = bc_type_preds[has_bc_detected]
                    else:
                        # Fallback if detection head not enabled: use type predictions directly
                        cascade_bc_type_preds = bc_type_preds
                    
                    # Flatten for metrics
                    cascade_bc_type_preds_flat = cascade_bc_type_preds.flatten()
                    bc_targets_type_flat = bc_targets_type.flatten().long()
                    
                    # Map -1 → 2 (no_bc) for metrics evaluation on ALL frames
                    # This allows metrics to capture detection failures (false positives and misses)
                    bc_type_preds_mapped = cascade_bc_type_preds_flat.clone()
                    bc_type_preds_mapped[bc_type_preds_mapped == -1] = 2  # Map -1 → 2 (no_bc)
                    
                    bc_targets_type_mapped = bc_targets_type_flat.clone()
                    bc_targets_type_mapped[bc_targets_type_mapped == -1] = 2  # Map -1 → 2 (no_bc)
                    
                    # Evaluate on ALL frames to capture full performance (detection + type)
                    preds["bc_type"] = bc_type_preds_mapped  # All frames: 0=reactive, 1=emotional, 2=no_bc
                    targets["bc_type"] = bc_targets_type_mapped  # All frames: 0=reactive, 1=emotional, 2=no_bc
            
            # NEW: Add head nod predictions and targets - TWO-HEAD ARCHITECTURE
            if self.conf.predict_head_nods and "hn_detection_probs" in out and "head_nod_labels" in batch:
                hn_targets = batch["head_nod_labels"]  # [B, T] (-1=no_nod, 0=short, 1=long, 2=long_p)
                
                # Stage 1: HN Detection predictions (no_nod vs any_nod)
                hn_detection_preds = None  # Initialize for cascade logic in type classification
                if self.conf.hn_detection_head and "hn_detection_probs" in out:
                    hn_detection_probs = out["hn_detection_probs"]  # [B, T, 2]
                    
                    # Align shapes if needed
                    if hn_detection_probs.shape[1] != hn_targets.shape[1]:
                        min_length = min(hn_detection_probs.shape[1], hn_targets.shape[1])
                        hn_detection_probs = hn_detection_probs[:, :min_length, :]
                        hn_targets_aligned = hn_targets[:, :min_length]
                    else:
                        hn_targets_aligned = hn_targets
                    
                    # Convert targets to detection targets: -1 → 0 (no_nod), >=0 → 1 (any_nod)
                    hn_detection_targets = (hn_targets_aligned >= 0).long()  # [B, T]
                    
                    # Get predictions
                    hn_detection_preds = torch.argmax(hn_detection_probs, dim=-1)  # [B, T]
                    
                    preds["hn_detection"] = hn_detection_preds.flatten()
                    targets["hn_detection"] = hn_detection_targets.flatten()
                
                # Stage 2: HN Type Classification predictions (short, long, long_p) - only on HN frames
                if self.conf.hn_type_head and "hn_type_probs" in out:
                    hn_type_probs = out["hn_type_probs"]  # [B, T, 3]
                    
                    # Align shapes if needed
                    if hn_type_probs.shape[1] != hn_targets.shape[1]:
                        min_length = min(hn_type_probs.shape[1], hn_targets.shape[1])
                        hn_type_probs = hn_type_probs[:, :min_length, :]
                        hn_targets_type = hn_targets[:, :min_length]
                    else:
                        hn_targets_type = hn_targets
                    
                    # Get type predictions for all frames
                    hn_type_preds = torch.argmax(hn_type_probs, dim=-1)  # [B, T]
                    
                    # CASCADE: Apply detection decision first (match inference logic!)
                    # Only use type predictions where detection head predicts HN (detection=1)
                    # Get detection predictions if available
                    if hn_detection_preds is not None:
                        has_hn_detected = hn_detection_preds == 1  # [B, T] - where detection head says "any_nod"
                        
                        # Create cascade predictions: -1 where no HN detected, type_pred where HN detected
                        cascade_hn_type_preds = torch.full_like(hn_targets_type, -1, dtype=torch.long)  # Initialize as -1 (no_nod)
                        cascade_hn_type_preds[has_hn_detected] = hn_type_preds[has_hn_detected]
                    else:
                        # Fallback if detection head not enabled: use type predictions directly
                        cascade_hn_type_preds = hn_type_preds
                    
                    # Flatten for metrics
                    cascade_hn_type_preds_flat = cascade_hn_type_preds.flatten()
                    hn_targets_type_flat = hn_targets_type.flatten().long()
                    
                    # Map -1 → 3 (no_nod) for metrics evaluation on ALL frames
                    # This allows metrics to capture detection failures (false positives and misses)
                    hn_type_preds_mapped = cascade_hn_type_preds_flat.clone()
                    hn_type_preds_mapped[hn_type_preds_mapped == -1] = 3  # Map -1 → 3 (no_nod)
                    
                    hn_targets_type_mapped = hn_targets_type_flat.clone()
                    hn_targets_type_mapped[hn_targets_type_mapped == -1] = 3  # Map -1 → 3 (no_nod)
                    
                    # Evaluate on ALL frames to capture full performance (detection + type)
                    preds["hn_type"] = hn_type_preds_mapped  # All frames: 0=short, 1=long, 2=long_p, 3=no_nod
                    targets["hn_type"] = hn_targets_type_mapped  # All frames: 0=short, 1=long, 2=long_p, 3=no_nod
            
            self.metrics_step(preds, targets, split="test")

            # pp.pprint({
            #     "acc_test_hs":self.test_metrics['acc']['hs'].compute(),
            #     "acc_test_hs2":self.test_metrics['acc']['hs2'].compute(),
            #     "acc_test_ls":self.test_metrics['acc']['ls'].compute(),
            #     "acc_test_sp":self.test_metrics['acc']['sp'].compute(),
            #     "acc_test_bp":self.test_metrics['acc']['bp'].compute(),
            #     "acc_test_sp2":self.test_metrics['acc']['sp2'].compute(),
            #     "acc_test_bp2":self.test_metrics['acc']['bp2'].compute(),
            #     "test_f1_hs":self.test_metrics['f1']['hs'].compute(),
            #     "f1_test_hs2":self.test_metrics['f1']['hs2'].compute(),
            #     "f1_test_ls":self.test_metrics['f1']['ls'].compute(),
            #     "f1_test_sp":self.test_metrics['f1']['sp'].compute(),
            #     "f1_test_bp":self.test_metrics['f1']['bp'].compute(),
            #     "f1_test_sp2":self.test_metrics['f1']['sp2'].compute(),
            #     "f1_test_bp2":self.test_metrics['f1']['bp2'].compute(),
            #     }
            # )

            # metrics["acc"]["hs"] = Accuracy(
            #     task=ACC_TASK, num_classes=2, average=ACC_AVERAGE
            # ).to(self.device)
            
            # metrics["acc"]["hs2"] = Accuracy(
            #     task=ACC_TASK, num_classes=2, average=ACC_AVERAGE
            # ).to(self.device)
            
            # metrics["acc"]["ls"] = Accuracy(
            #     task=ACC_TASK, num_classes=2, average=ACC_AVERAGE
            # ).to(self.device)
            
            # metrics["acc"]["sp"] = Accuracy(
            #     task=ACC_TASK, num_classes=2, average=ACC_AVERAGE
            # ).to(self.device)
            
            # metrics["acc"]["bp"] = Accuracy(
            #     task=ACC_TASK, num_classes=2, average=ACC_AVERAGE
            # ).to(self.device)
    
            # metrics["acc"]["sp2"] = Accuracy(
            #     task=ACC_TASK, num_classes=2, average=ACC_AVERAGE
            # ).to(self.device)
            
            # metrics["acc"]["bp2"] = Accuracy(
            #     task=ACC_TASK, num_classes=2, average=ACC_AVERAGE
            # ).to(self.device)

    def on_test_epoch_start(self):
        """Reset RTF measurements at the start of test epoch"""
        self.reset_rtf_measurements()
    
    def on_test_epoch_end(self, *_):
        
        if hasattr(self, "test_metrics"):
            self.metrics_epoch("test")            

def torch_get_attr(obj, names):
    if len(names) == 1:
        return getattr(obj, names[0])
    else:
        return torch_get_attr(getattr(obj, names[0]), names[1:])

def torch_set_attr(obj, names, val):
    if len(names) == 1:
        return setattr(obj, names[0], val)
    else:
        return torch_set_attr(getattr(obj, names[0]), names[1:], val)

if __name__ == "__main__":

    # For data cache generation
    import platform
    os_name = platform.system()
    #if "linux" in os_name.lower():
    #    multiprocessing.set_start_method('spawn')
    
    torch.set_float32_matmul_precision("medium")

    manager = Manager()
    lock = manager.Lock()
    
    finetune(manager, lock)
