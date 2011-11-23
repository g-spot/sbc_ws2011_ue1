@echo off
rem ---------------------------------------------------------------------------
rem Run script for the OpenJMS examples
rem
rem Required Environment Variables
rem
rem   JAVA_HOME       Points to the Java Development Kit installation.
rem
rem Optional Environment Variables
rem 
rem   OPENJMS_HOME    Points to the OpenJMS installation directory.
rem
rem $Id: run.bat,v 1.2 2005/06/13 14:45:09 tanderson Exp $
rem ---------------------------------------------------------------------------

if "%OS%" == "Windows_NT" setlocal

if not "%JAVA_HOME%" == "" goto gotJavaHome
echo The JAVA_HOME environment variable is not set.
echo This is required to run the examples.
exit /B 1

:gotJavaHome

if exist "%JAVA_HOME%\bin\java.exe" goto okJavaHome
echo The JAVA_HOME environment variable is not set correctly.
echo This is required to run the examples.
exit /B 1

:okJavaHome

set _RUNJAVA="%JAVA_HOME%\bin\java.exe"

rem Guess OPENJMS_HOME if it is not set
if not "%OPENJMS_HOME%" == "" goto gotOpenJMSHome
set OPENJMS_HOME=.
if exist "%OPENJMS_HOME%\lib\openjms-0.7.7-beta-1.jar" goto okOpenJMSHome
set OPENJMS_HOME=..\..
if exist "%OPENJMS_HOME%\lib\openjms-0.7.7-beta-1.jar" goto okOpenJMSHome
echo The OPENJMS_HOME variable is not set.
echo This is required to run the examples.
exit /B 1

:gotOpenJMSHome

if exist "%OPENJMS_HOME%\lib\openjms-0.7.7-beta-1.jar" goto okOpenJMSHome
echo The OPENJMS_HOME variable is not set correctly.
echo This is required to run the examples.
exit /B 1

:okOpenJMSHome

set CLASSPATH=.\;%OPENJMS_HOME%\lib\openjms-0.7.7-beta-1.jar
set POLICY_FILE=%OPENJMS_HOME%\config\openjms.policy

if ""%1"" == """" goto usage
goto execCmd

:usage

echo usage: run.bat (classname)
exit /B 1

:execCmd
rem Execute the command

echo Using OPENJMS_HOME: %OPENJMS_HOME%
echo Using JAVA_HOME:    %JAVA_HOME%
echo Using CLASSPATH:    %CLASSPATH%

set MAINCLASS=%1%
shift

rem Get remaining unshifted command line arguments and save them 
set CMD_LINE_ARGS=
:setArgs
if ""%1""=="""" goto doneSetArgs
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto setArgs
:doneSetArgs

rem Execute Java with the applicable properties

%_RUNJAVA% -classpath "%CLASSPATH%" -Djava.security.manager -Djava.security.policy="%POLICY_FILE%" %MAINCLASS% %CMD_LINE_ARGS%
