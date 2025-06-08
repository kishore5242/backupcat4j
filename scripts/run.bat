@echo off
setlocal

REM Resolve script directory
set SCRIPT_DIR=%~dp0
set JAR=%SCRIPT_DIR%..\backupcat4j.jar
set FFMPEG=%SCRIPT_DIR%..\ffmpeg\ffmpeg.exe
set FFPROBE=%SCRIPT_DIR%..\ffmpeg\ffprobe.exe

REM Prompt user for SOURCE folder
set /p SOURCE=Enter source folder (default: D:\projects\temp\source):
if "%SOURCE%"=="" set SOURCE=D:\projects\temp\source

REM Prompt user for TARGET folder
set /p TARGET=Enter target folder (default: D:\projects\temp\destination):
if "%TARGET%"=="" set TARGET=D:\projects\temp\destination

REM Prompt user for organize mode with default FULL
set /p ORGANIZE=Enter organize mode [FULL, IGNORING_FIRST_FOLDER, NONE] (default: FULL):
if "%ORGANIZE%"=="" set ORGANIZE=FULL

REM Prompt user for bitrate with default 3000000
set /p BITRATE=Enter bitrate (default: 3000000):
if "%BITRATE%"=="" set BITRATE=3000000

REM Run the backup JAR with user inputs
java -jar "%JAR%" ^
source="%SOURCE%" ^
target="%TARGET%" ^
ffmpeg="%FFMPEG%" ^
ffprobe="%FFPROBE%" ^
organize=%ORGANIZE% ^
--compress ^
--resume ^
--replace ^
--bitrate=%BITRATE%

pause
endlocal