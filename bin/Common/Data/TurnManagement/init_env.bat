call conda create -n py311_vap python==3.11 -y
call conda activate py311_vap
call cd /d %~dp0

call conda install cudatoolkit==11.8 dlib ffmpeg -y

if errorlevel 1 goto ERROR

call pip install torch torchvision torchaudio --index-url https://download.pytorch.org/whl/cu118
call pip install -r env/requirements.txt

:ERROR
echo installation without cuda
call pip install torch torchvision torchaudio
call pip install -r env/requirements.txt
