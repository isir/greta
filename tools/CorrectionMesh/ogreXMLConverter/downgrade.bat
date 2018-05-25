@echo off
set ConverterPath=%~dp0
set OGRE_NEW="%ConverterPath%1.7\OgreXMLConverter.exe"
set OGRE_OLD="%ConverterPath%1.6\OgreXMLConverter.exe"

echo %OGRE_NEW%
echo %OGRE_OLD%


REM set /p msg=let start

REM set /p msg=????
:loop
if %10 == 0 (
	REM set /p msg=no more file to proceed.
	goto continue
)
REM set /p msg=%1
REM set down = ko
if %~x1 == .mesh (
	REM set /p msg=Extention is mesh.
	goto down
)
REM set /p msg=Extention is not mesh.
if %~x1 == .skeleton (
	REM set /p msg=Extention is skeleton.
	goto down
)
REM set /p msg=unknown extention.
goto wrong

:down
REM set /p msg=downgrade %1
%OGRE_NEW% %1
%OGRE_OLD% -e "%~1.xml"
goto end

:wrong
REM set /p msg=Wrong file format.

:end

shift

goto loop
:continue

REM set /p msg=end.

