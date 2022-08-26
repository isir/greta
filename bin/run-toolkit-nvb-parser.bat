
goto charniak


:charniak
@pushd %~dp0\charniak
call run-parser.bat %1
@popd
goto end


:end
