@REM call conda activate py311_vad
call conda activate py311_vap
cd /d %~dp0
@REM call python turnManager_vap_audio_faceEmbed.py
call python turnManager_vap_audio_faceEmbed_refactored.py
@REM echo %1
@REM pause