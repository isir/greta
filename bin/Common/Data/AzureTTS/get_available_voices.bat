@cd /d %~dp0
@call conda activate py310_azuretts
@call python get_available_voices.py