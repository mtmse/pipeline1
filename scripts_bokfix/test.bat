@echo off

call tweaker.bat D:\books\TBF\C80340 D:\books\TBF\out D:\books\TBF\log.txt true

IF ERRORLEVEL 1 GOTO Error

echo Test: OK!

GOTO End

:Error

echo Test: error!

:End
