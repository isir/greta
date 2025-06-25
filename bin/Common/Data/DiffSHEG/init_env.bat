@echo off
call conda create -n diffsheg_env python=3.9 -y
call conda activate diffsheg_env

cd /d %~dp0

call conda install cudatoolkit=11.7 -y

if errorlevel 1 goto ERROR

call pip install torch torchvision torchaudio --index-url https://download.pytorch.org/whl/cu117

call pip install -r requirements.txt

goto END

:ERROR
echo CUDA install failed — falling back to CPU-only PyTorch
call pip install torch torchvision torchaudio
call pip install -r requirements.txt

:END
echo Setup complete
