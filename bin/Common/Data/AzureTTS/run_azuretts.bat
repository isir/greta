cd /d %~dp0
call conda activate py310_azuretts
call python run_azuretts.py %1 %2 %3 %4