@echo off

set programdir=%CD%\..\..
set packagedir=%programdir%\Packages
set repodir=%programdir%\Git
set forgedir=%programdir%\Forge
set mcpdir=%forgedir%\mcp
set euryscore=%repodir%\SlimevoidLibrary
set elevators=%repodir%\DynamicElevators-FML
cd %mcpdir%

if not exist %euryscore% GOTO :ECFAIL
GOTO :EC

:EC
if exist %mcpdir%\src GOTO :COPYSRC
GOTO :ECFAIL

:COPYSRC
if not exist "%mcpdir%\src-workd" GOTO :CREATESRC
GOTO :ECFAIL

:CREATESRC
mkdir "%mcpdir%\src-work"
xcopy "%mcpdir%\src\*.*" "%mcpdir%\src-work\" /S
if exist "%mcpdir%\src-work" GOTO :COPYEC
GOTO :ECFAIL

:COPYEC
xcopy "%euryscore%\SV-common\*.*" "%mcpdir%\src\minecraft" /S
xcopy "%elevators%\DE-source\*.*" "%mcpdir%\src\minecraft" /S
pause
call %mcpdir%\recompile.bat
call %mcpdir%\reobfuscate.bat
echo Recompile and Reobf Completed Successfully
pause

:REPACKAGE
if not exist "%mcpdir%\reobf" GOTO :ECFAIL
if exist "%packagedir%\DynamicElevators-FML" (
del "%packagedir%\DynamicElevators-FML\*.*" /S /Q
rmdir "%packagedir%\DynamicElevators-FML" /S /Q
)
mkdir "%packagedir%\DynamicElevators-FML\slimevoid\elevators"
xcopy "%mcpdir%\reobf\minecraft\slimevoid\elevators\*.*" "%packagedir%\DynamicElevators-FML\slimevoid\elevators\" /S
xcopy "%elevators%\DE-resources\*.*" "%packagedir%\DynamicElevators-FML\" /S
echo "Dynamic Elevators Packaged Successfully
pause
ren "%mcpdir%\src" src-old
echo Recompiled Source folder renamed
pause
ren "%mcpdir%\src-work" src
echo Original Source folder restored
pause
del "%mcpdir%\src-old" /S /Q
del "%mcpdir%\reobf" /S /Q
if exist "%mcpdir%\src-old" rmdir "%mcpdir%\src-old" /S /Q
if exist "%mcpdir%\reobf" rmdir "%mcpdir%\reobf" /S /Q
echo Folder structure reset
GOTO :ECCOMPLETE

:ECFAIL
echo Could not compile Dynamic Elevators
pause
GOTO :EOF

:ECCOMPLETE
echo Dynamic Elevators completed compile successfully
pause
GOTO :EOF

:EOF