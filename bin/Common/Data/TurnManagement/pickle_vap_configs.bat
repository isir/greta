call conda activate py311_vap
call cd /d %~dp0

@REM audio
@REM call python pickle_vap_configs.py --vap_encoder_type cpc --vap_pretrained_cpc encoders/cpc/60k_epoch4-d0f474de.pt --vap_freeze_encoder 1 --vap_channel_layers 1 --vap_cross_layers 3 --vap_context_limit -1 --vap_context_limit_cpc_sec -1 --vap_frame_hz 25 --event_frame_hz 25 --checkpoint models/audio_VapGPT_25Hz_ad20s_134-epoch102-val_0.03040.ckpt --devices 0 --seed 0
@REM call python pickle_vap_configs.py --vap_encoder_type cpc --vap_pretrained_cpc encoders/cpc/60k_epoch4-d0f474de.pt --vap_freeze_encoder 1 --vap_channel_layers 1 --vap_cross_layers 3 --vap_context_limit -1 --vap_context_limit_cpc_sec -1 --vap_frame_hz 25 --event_frame_hz 25 --checkpoint models/audio_last.ckpt --devices 0 --seed 0

@REM saga mode1
@REM call python pickle_vap_configs.py --vap_encoder_type cpc --vap_pretrained_cpc encoders/cpc/60k_epoch4-d0f474de.pt --vap_freeze_encoder 1 --vap_channel_layers 1 --vap_cross_layers 3 --vap_context_limit -1 --vap_context_limit_cpc_sec -1 --vap_frame_hz 25 --vap_multimodal --vap_use_face_encoder --vap_pretrained_face_encoder encoders/FormerDFER/DFER_encoder_weight_only.pt --vap_mode 1 --event_frame_hz 25 --checkpoint models/saga-mode1_VapGPT_25Hz_ad20s_134-epoch27-val_0.07306.ckpt --devices 0 --seed 0
call python pickle_vap_configs.py --vap_encoder_type cpc --vap_pretrained_cpc encoders/cpc/60k_epoch4-d0f474de.pt --vap_freeze_encoder 1 --vap_channel_layers 1 --vap_cross_layers 3 --vap_context_limit -1 --vap_context_limit_cpc_sec -1 --vap_frame_hz 25 --vap_multimodal --vap_use_face_encoder --vap_pretrained_face_encoder encoders/FormerDFER/DFER_encoder_weight_only.pt --vap_mode 1 --event_frame_hz 25 --checkpoint models/saga-mode1_last-V1.ckpt --devices 0 --seed 0
