@echo off
REM Launcher that uses the jlink runtime image created under target\focus-system
set IMAGE_DIR=%~dp0target\focus-system
"%IMAGE_DIR%\bin\javaw.exe" -m com.focussystem/com.focussystem.Launcher
pause