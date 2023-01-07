@echo off
set USER_HOME=-Duser.home=%~dp0
start javaw %USER_HOME% -jar "%~dp0\lib\jfs.jar" %*
