call conda create -n py310_azuretts python=3.10 -y
call conda activate py310_azuretts
call cd %~dp0
call pip install -r requirements.txt
