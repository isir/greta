@echo off
chcp 65001 >nul 2>&1
set PYTHONIOENCODING=utf-8

REM ============================================================
REM  HybridRealTime.py launcher
REM  File-based VAP inference + Greta agent + Audio display
REM ============================================================

REM Activate conda environment
call conda activate C:\Users\isir\.conda\envs\py311_rvap

REM Change to script directory
cd /d "%~dp0"

REM ============================================================
REM  REQUIRED arguments (edit paths to match your data)
REM ============================================================
set CHECKPOINT=sam_vap\checkpoints\VapGPT_25Hz_ad20s_134-epoch9-val_0.04519_double_CAascade_double_shift1000_5S_25hz_Agressive_HNw.ckpt
set AUDIO1=C:\path\to\speaker1.wav
set AUDIO2=C:\path\to\speaker2.wav

REM ============================================================
REM  Run HybridRealTime
REM ============================================================
python HybridRealTime.py ^
    --checkpoint sam_vap/checkpoints/VapGPT_25Hz_ad20s_134-epoch9-val_0.04519_double_CAascade_double_shift1000_5S_25hz_Agressive_HNw.ckpt ^
    --audio1 sam_vap/demo/noxi/Paris_14-audio_expert.wav ^
    --audio2 sam_vap/demo/noxi/Paris_14-audio_novice.wav ^
    --face1  sam_vap/demo/noxi/Paris_14-face_expert.csv ^
    --gaze1 sam_vap/demo/noxi/Paris_14-gaze_expert.csv ^
    --head1 sam_vap/demo/noxi/Paris_14-head_expert.csv ^
    --body1 sam_vap/demo/noxi/Paris_14-body_expert.csv ^
    --face_im1 sam_vap/demo/noxi/video_paris/Paris_14-video_expert.npy ^
    --face2  sam_vap/demo/noxi/Paris_14-face_novice.csv ^
    --gaze2 sam_vap/demo/noxi/Paris_14-gaze_novice.csv ^
    --head2 sam_vap/demo/noxi/Paris_14-head_novice.csv ^
    --body2 sam_vap/demo/noxi/Paris_14-body_novice.csv ^
    --face_im2 sam_vap/demo/noxi/video_paris/Paris_14-video_novice.npy ^
    --predict_backchannels ^
    --predict_hn ^
    --multimodal ^
    --use_face_encoder ^
    --use_whisper_semantics ^
    --pretrained_whisper_model openai/whisper-tiny.en ^
    --mode 3 ^
    --context_duration 5.0 ^
    --stride_duration 0.5 ^
    --shift_threshold 0.67 ^
    --bc_threshold 0.15 ^
    --hn_threshold 0.75 ^
    --unmute_energy 0.001


REM ============================================================
REM  Optional arguments (uncomment as needed)
REM ============================================================

REM  Multimodal CSV files:
REM    --face1 "C:\path\to\face1.csv" ^
REM    --gaze1 "C:\path\to\gaze1.csv" ^
REM    --head1 "C:\path\to\head1.csv" ^
REM    --body1 "C:\path\to\body1.csv" ^
REM    --face2 "C:\path\to\face2.csv" ^
REM    --gaze2 "C:\path\to\gaze2.csv" ^
REM    --head2 "C:\path\to\head2.csv" ^
REM    --body2 "C:\path\to\body2.csv" ^
REM
REM  Pretrained model paths:
REM    --face_encoder_path "C:\path\to\face_encoder.pth" ^
REM    --pretrained_cpc "C:\path\to\cpc.pth" ^
REM
REM  Cooldowns:
REM    --bc_cooldown 3.0 ^
REM    --hn_cooldown 2.0 ^
REM    --shift_cooldown 5.0 ^
REM
REM  Disable Greta (audio-only mode):
REM    --no_greta ^
REM
REM  Force CPU:
REM    --device cpu ^

pause
