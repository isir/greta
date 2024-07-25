REM start /MIN charniak-windows\parseIt.exe charniak-windows/DATA/EN/ -T40 %1
IF %1==FR (
    start /MIN charniak-windows\parseIt.exe charniak-windows/DATA/FR/ -T40
) ELSE (
    start /MIN charniak-windows\parseIt.exe charniak-windows/DATA/EN/ -T40
)