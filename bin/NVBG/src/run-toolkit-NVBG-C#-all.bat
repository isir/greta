
@if !%1==! (set PARSER=charniak) else set PARSER=%1

@if !%PARSER%==!charniak goto charniak
@if !%PARSER%==!stanford goto stanford

:charniak
call run-toolkit-nvb-parser.bat
goto nvbg

:stanford
REM use "call run-toolkit-nvb-parser-stanford -V2" for sending parser_result2 messages for Cerebella
call run-toolkit-nvb-parser-stanford
goto nvbg


:nvbg
pushd %~dp0\NVBG\bin\Release

start /MIN NVBG.exe -write_to_file false -data_folder_path data/nvbg-common -parsetree_cachefile_path data/cache/brad_rachel_parse.txt -create_character Brad Brad.ini -create_character Rachel Rachel.ini -storypoint toolkitsession

@popd

exit 
