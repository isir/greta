@echo off
call conda create -n diffsheg_env python=3.9 -y
call conda activate diffsheg_env

cd /d %~dp0

call pip install torch torchvision torchaudio --index-url https://download.pytorch.org/whl/cu117

if errorlevel 1 goto ERROR

call pip install -r requirements.txt

goto END

:ERROR
echo CUDA install failed — falling back to CPU-only PyTorch
call pip install torch torchvision torchaudio
call pip install -r requirements.txt

:END
python -c "import torch; print(torch.__version__); print('CUDA available:', torch.cuda.is_available())"

echo Setup complete
