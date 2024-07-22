
if exist charniak (goto charniak) else (goto stanford)


:charniak
@pushd charniak
call run-parser.bat %1
@popd
goto end

:stanford
@pushd ..\..\bin\StanfordParserWrapper
start /MIN StanfordParserWrapper.exe %1
@popd


:end
