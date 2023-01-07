@echo off

if "%1" == "" (
  start javaw -Xmx512m -jar "%~dp0\lib\jfs.jar" %*
) else (
  java -jar -Xmx512m "%~dp0\lib\jfs.jar" %*
)
