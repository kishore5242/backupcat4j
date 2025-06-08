::[Bat To Exe Converter]
::
::YAwzoRdxOk+EWAjk
::fBw5plQjdCyDJGyX8VAjFBBdXwyNAE+1EbsQ5+n//NaEo1kiWfcydZbk0rWCLO8E70DqSsR/hisP1sIPA3s=
::YAwzuBVtJxjWCl3EqQJgSA==
::ZR4luwNxJguZRRnk
::Yhs/ulQjdF+5
::cxAkpRVqdFKZSDk=
::cBs/ulQjdF+5
::ZR41oxFsdFKZSDk=
::eBoioBt6dFKZSDk=
::cRo6pxp7LAbNWATEpCI=
::egkzugNsPRvcWATEpCI=
::dAsiuh18IRvcCxnZtBJQ
::cRYluBh/LU+EWAnk
::YxY4rhs+aU+JeA==
::cxY6rQJ7JhzQF1fEqQJQ
::ZQ05rAF9IBncCkqN+0xwdVs0
::ZQ05rAF9IAHYFVzEqQJQ
::eg0/rx1wNQPfEVWB+kM9LVsJDGQ=
::fBEirQZwNQPfEVWB+kM9LVsJDGQ=
::cRolqwZ3JBvQF1fEqQJQ
::dhA7uBVwLU+EWDk=
::YQ03rBFzNR3SWATElA==
::dhAmsQZ3MwfNWATElA==
::ZQ0/vhVqMQ3MEVWAtB9wSA==
::Zg8zqx1/OA3MEVWAtB9wSA==
::dhA7pRFwIByZRRnk
::Zh4grVQjdCuDJEmW+0g1Kw9HcA2GOWqGBLQf4/3r7OuT9kchR+EtcZ/PyYisIeMY/1XHdIJ53DRfgM5s
::YB416Ek+ZG8=
::
::
::978f952a14a936cc963da21a135fa983
@echo off
setlocal

REM Resolve script directory
set SCRIPT_DIR=%~dp0
set JAR=%SCRIPT_DIR%\bin\backupcat4j.jar
set FFMPEG=%SCRIPT_DIR%\bin\ffmpeg.exe
set FFPROBE=%SCRIPT_DIR%\bin\ffprobe.exe

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
set /p BITRATE=Enter maximum bitrate for videos (default: 3000000):
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