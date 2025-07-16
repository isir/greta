@echo off
call conda create -n diffsheg_env python=3.9 -y
call conda activate diffsheg_env

REM "You might need to activate this line if error happens"
REM set "CUDA_HOME=%CONDA_PREFIX%"

cd /d %~dp0

call conda install -c conda-forge cudatoolkit-dev=11.7 -y
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
