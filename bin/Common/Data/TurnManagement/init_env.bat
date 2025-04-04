echo ###########################
echo Start installing py311_vad
echo ###########################

call conda create -n py311_vad python==3.11 -y
call conda activate py311_vad
cd /d %~dp0
call pip install -r requirements_vad.txt

echo ###########################
echo End installing py311_vap
echo ###########################

echo ###########################
echo Start installing py311_vap
echo ###########################

call conda create -n py311_vap python==3.11 -y
call conda activate py311_vap
call cd /d %~dp0

call conda install cudatoolkit=11.8 dlib ffmpeg numpy=1.26 matplotlib opencv=4.11 -y

if errorlevel 1 goto error

call pip install torch torchvision torchaudio --index-url https://download.pytorch.org/whl/cu118
call pip install -r env/requirements_vap.txt

goto end

:error
echo installation without cuda
call pip install torch torchvision torchaudio
call pip install -r env/requirements_vap.txt

goto end

:end
echo ###########################
echo End installing py311_vap
echo ###########################
