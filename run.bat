@echo off
cd /d "%~dp0"
echo ========================================================
echo   Tri-distribue GridSim Application
echo ========================================================
javac -cp "lib/gridsim.jar;lib/simjava2.jar" -d bin src\*.java
if %ERRORLEVEL% equ 0 (
    echo [OK] Build successful!
    echo Starting Application...
    start javaw -cp "bin;lib/gridsim.jar;lib/simjava2.jar" SortingUI
) else (
    echo [ERROR] Build failed.
    pause
)
