call conda create -n py311_mic python==3.11 -y
call conda activate py311_mic
call cd /d %~dp0
call pip install -r requirements.txt