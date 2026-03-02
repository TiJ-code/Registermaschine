@echo off
set DIR=%~dp0
start "" "%DIR%jre\bin\javaw.exe" -jar "%DIR%app.jar"
exit