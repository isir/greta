cd %~dp0

REM this line might show error if env exists, but that is normal

call conda activate py312_modiff
if %ERRORLEVEL% neq 0 goto ProcessError

echo py312_modiff found
call python RealTimePipeFinal.py --buffer=64 --N=20 --K=20 --M=5
exit /b 0

:ProcessError
echo py312_modiff not found, creating env from .yml
call conda env create -f env/py312_modiff.yml -y
call conda activate py312_modiff
call python RealTimePipeFinal.py --buffer=64 --N=20 --K=20 --M=5

pause