call conda activate diffsheg_env
if %ERRORLEVEL% neq 0 goto ProcessError
cd /d %~dp0
echo diffsheg_env found

call python diffsheg.py

:ProcessError
echo diffsheg_env not found, creating it
call init_env.bat
call conda activate diffsheg_env
call python runner.py