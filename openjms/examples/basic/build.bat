@echo off
rem ---------------------------------------------------------------------------
rem Build script for the OpenJMS examples
rem
rem Required Environment Variables
rem
rem   JAVA_HOME       Points to the Java Development Kit installation.
rem
rem Optional Environment Variables
rem 
rem   OPENJMS_HOME    Points to the OpenJMS installation directory.
rem
rem $Id: build.bat,v 1.1 2004/11/26 02:59:08 tanderson Exp $
rem ---------------------------------------------------------------------------

if "%OS%" == "Windows_NT" setlocal

if not "%JAVA_HOME%" == "" goto gotJavaHome
echo The JAVA_HOME environment variable is not set.
echo This is required to build the examples.
exit /B 1

:gotJavaHome

if exist "%JAVA_HOME%\bin\javac.exe" goto okJavaHome
echo The JAVA_HOME environment variable is not set correctly.
echo This is required to build the examples.
exit /B 1

:okJavaHome

set _RUNJAVAC="%JAVA_HOME%\bin\javac.exe"

rem Guess OPENJMS_HOME if it is not set
if not "%OPENJMS_HOME%" == "" goto gotOpenJMSHome
set OPENJMS_HOME=..\..
if exist "%OPENJMS_HOME%\lib\openjms-0.7.7-beta-1.jar" goto okOpenJMSHome
echo The OPENJMS_HOME variable is not set.
echo This is required to build the examples.
exit /B 1

:gotOpenJMSHome

if exist "%OPENJMS_HOME%\lib\openjms-0.7.7-beta-1.jar" goto okOpenJMSHome
echo The OPENJMS_HOME variable is not set correctly.
echo This is required to build the examples.
exit /B 1

:okOpenJMSHome

set CLASSPATH=%OPENJMS_HOME%\lib\openjms-0.7.7-beta-1.jar;%OPENJMS_HOME%\lib\jms-1.1.jar;%OPENJMS_HOME%\lib\jndi-1.2.1.jar

echo Using OPENJMS_HOME: %OPENJMS_HOME%
echo Using JAVA_HOME:    %JAVA_HOME%
echo Using CLASSPATH:    %CLASSPATH%

%_RUNJAVAC% -g -classpath "%CLASSPATH%" *.java
