@echo off

if "%1" == "" (
  start javaw -jar "%~dp0\lib\jfs.jar" %*
) else (
  java -jar "%~dp0\lib\jfs.jar" %*
)
