call conda create -n py311_vad python==3.11 -y
call conda activate py311_vad
call cd /d %~dp0

call conda install cudatoolkit==11.8 -y

if errorlevel 1 goto ERROR

call pip install torch torchvision torchaudio --index-url https://download.pytorch.org/whl/cu118
call pip install -r requirements.txt

:ERROR
echo installation without cuda
call pip install torch torchvision torchaudio
call pip install -r requirements.txt
