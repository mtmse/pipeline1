@echo off
rem ***********************************************
rem * Tweaker.bat
rem * Adds skippability to a Daisy 2.02 book by
rem * running the Skippability Tweaker transformer
rem * in Daisy Pipeline
rem *
rem * Parameters:
rem *   %1 - Input directory
rem *   %2 - Output directory
rem *   %3 - Log file name
rem *   %4 - Remove input directory after completion and replace with output (true/false)
rem *
rem * Updates: 2008-02-29 - Linus Ericson: Initial version
rem *          2008-03-26 - Linus Ericson: Added support for the fourth parameter
rem ***********************************************

rem Set this to the path of the Java 5 exe
set JAVA=C:\Program\Java\jre1.5.0_13\bin\java.exe

rem Set this to the install directory of Daisy Pipeline
set PIPELINE=D:\program\pipeline-20080326


rem DO NOT CHANGE ANYTHING BELOW THIS LINE!

rem Remember current directory
set OLDDIR=%CD%

set IN_DIR=%1
set OUT_DIR=%2
set LOG_FILE=%3
set MOVE_ON_SUCCESS=%4

rem Single book volume or multi book volume?
IF exist %IN_DIR%\discinfo.html set FILENAME=%IN_DIR%\discinfo.html
IF exist %IN_DIR%\ncc.html set FILENAME=%IN_DIR%\ncc.html

rem Error cheking before we start
IF not exist %JAVA% GOTO Error_java
IF not exist %PIPELINE%\pipeline.jar GOTO Error_pipeline
IF not exist %FILENAME% GOTO Error_filename

echo ------------------- >> %LOG_FILE%
date /T >> %LOG_FILE%
time /T >> %LOG_FILE%
echo FILENAME: %FILENAME% >> %LOG_FILE%
echo OUTDIR: %OUT_DIR% >> %LOG_FILE%
echo LOGFILE: %LOG_FILE% >> %LOG_FILE%
echo MOVE ON SUCCESS: %MOVE_ON_SUCCESS% >> %LOG_FILE%

rem Change into Pipeline directory and execute the Skippability Tweaker
chdir /d %PIPELINE%
%JAVA% -classpath "pipeline.jar";"." org.daisy.pipeline.ui.CommandLineUI scripts\bokfix\SkippabilityTweaker.taskScript --input=%FILENAME% --output=%OUT_DIR% >> %LOG_FILE% 2>&1

IF errorlevel 1 GOTO Error_running


rem Do we need to remove the original input dir and replace it with the output dir?
IF NOT "%4"=="true" GOTO No_move

echo Removing original input dir >> %LOG_FILE%
rmdir /Q /S %IN_DIR% >> %LOG_FILE%

echo Moving output dir to original input dir >> %LOG_FILE%
move %OUT_DIR% %IN_DIR% >> %LOG_FILE%

:No_move

echo Everything seems OK >> %LOG_FILE%
GOTO End_ok


rem ***** Something went wrong *****
:Error_java
echo Error: Incorrect install path for Java 5
GOTO End_Error

:Error_pipeline
echo Error: Incorrect install path for Daisy Pipeline >> %LOG_FILE%
GOTO End_Error

:Error_filename
echo Error: Input filename could not be found >> %LOG_FILE%
GOTO End_error

:Error_running
echo Error: Problems occurred while running Daisy Pipeline >> %LOG_FILE%
GOTO End_error



rem ***** Everythings seems OK *****
:End_ok
chdir /d %OLDDIR%
exit /B 0



:End_error
chdir /d %OLDDIR%
exit /B 1
