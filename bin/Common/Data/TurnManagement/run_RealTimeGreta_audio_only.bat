@echo off
chcp 65001 >nul 2>&1
set PYTHONIOENCODING=utf-8
call conda activate py311_rvap
cd /d %~dp0





set PYTORCH_CUDA_ALLOC_CONF=expandable_segments:True
call python RealTimeGreta.py ^
    --checkpoint "sam_vap\checkpoints\VapGPT_25Hz_ad20s_134-epoch9-val_0.07268_Audio_only_5s_25hz_double_double_Shift_1000.ckpt" ^
    --device auto ^
    --predict_backchannels ^
    --predict_hn ^
    --mode 1 ^

    


REM ========================================================================
REM  Available arguments (uncomment / adjust as needed):
REM ========================================================================
REM 
REM audio_Only_Path sam_vap\checkpoints\VapGPT_25Hz_ad20s_134-epoch9-val_0.07268_Audio_only_5s_25hz_double_double_Shift_1000.ckpt

REM --- Model architecture flags (override checkpoint config) ---
REM   --predict_backchannels          Enable BC prediction heads
REM   --predict_hn                    Enable HN prediction heads
REM   --multimodal                    Enable multimodal input (face AUs, gaze, head, body)
REM   --use_face_encoder              Enable face encoder (EncoderFormerDFER)
REM   --use_hubert_semantics          Enable HuBERT semantic encoder
REM   --use_whisper_semantics         Enable Whisper semantic encoder
REM   --mode 3                       Architecture mode (3=full, 1=audio+face, etc.)
REM
REM --- Pretrained model paths (override checkpoint-embedded paths) ---
REM   --face_encoder_path "C:\path\to\face_encoder.pth"
REM   --pretrained_vap "C:\path\to\vap_state_dict.pt"
REM   --pretrained_cpc "C:\path\to\cpc_model.pt"
REM   --pretrained_hubert_model "facebook/hubert-base-ls960"
REM   --pretrained_whisper_model "openai/whisper-tiny.en"
REM
REM --- Network ports ---
REM   --mic_port 9000                 Microphone socket port
REM   --openface_port 5963            OpenFace feature receiver port
REM
REM --- Inference parameters ---
REM   --context_duration 5.0          Context window (seconds)
REM   --stride_duration 1.0           Inference stride (seconds)
REM   --bc_threshold 0.5              BC detection threshold
REM   --hn_threshold 0.5              HN detection threshold
REM   --shift_threshold 0.6           VAP turn shift threshold
REM   --bc_cooldown 3.0               Min seconds between BCs
REM   --hn_cooldown 2.0               Min seconds between HNs
REM   --shift_cooldown 5.0            Min seconds between turn shifts
REM ========================================================================
