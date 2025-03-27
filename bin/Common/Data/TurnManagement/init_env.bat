call conda create -n py311_vad python==3.11 -y
call conda activate py311_vad
cd /d %~dp0
call pip install -r requirements.txt