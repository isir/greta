cd %~dp0

REM this line might show error if env exists, but that is normal

call conda activate greta_modiff_vqvae
if %ERRORLEVEL% neq 0 goto ProcessError

echo greta_modiff_vqvae found
:: set PYTORCH_CUDA_ALLOC_CONF=expandable_segments:True
call python RealTimePipeVQ.py
::call python RealTimePipeVQ_copy.py

:: call python RealTimePipeFinal.py --buffer=64 --N=20 --K=20 --M=10

pause
exit /b 0

:ProcessError
echo greta_modiff_vqvae not found, creating env from .yml
call conda env create -f env/greta_modiff_vqvae.yml -y
call conda activate greta_modiff_vqvae
set PYTORCH_CUDA_ALLOC_CONF=expandable_segments:True
:: call python RealTimePipeFinal.py --buffer=64 --N=20 --K=20 --M=10
call python test_pipeline.py --mode check_aus

pause