::[Bat To Exe Converter]
::
::YAwzoRdxOk+EWAjk
::fBw5plQjdCuDJEmW+0g1Kw9HcA2GOWqGBLQf4/3r7OuT9kchR/A8RILa07qyMuUA5VD2dpM+6ntKiIYFDxRWMBuoYW8=
::YAwzuBVtJxjWCl3EqQJgSA==
::ZR4luwNxJguZRRnk
::Yhs/ulQjdF+5
::cxAkpRVqdFKZSTk=
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
set JAVA=%SCRIPT_DIR%\bin\jre\bin\java.exe
set JAR=%SCRIPT_DIR%\bin\backupcat4j-fx.jar

REM Run the Java FX jar
"%JAVA%" ^
--enable-native-access=ALL-UNNAMED ^
--add-modules javafx.controls,javafx.fxml ^
-cp "%JAR%" ^
org.kapps.AppUI

endlocal