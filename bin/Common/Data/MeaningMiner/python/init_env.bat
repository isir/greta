call conda create -n py310_mm python=3.10 -y
call conda activate py310_mm
cd %~dp0
call pip install -r requirements.txt
call python -c "import nltk;nltk.download('bcp47')"