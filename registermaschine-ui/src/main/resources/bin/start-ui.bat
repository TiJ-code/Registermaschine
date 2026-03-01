@echo off
set DIR=%~dp0
start "" "%DIR%jre\bin\javaw.exe" --enable-native-access=ALL-UNNAMED -jar "%DIR%app.jar"
exit