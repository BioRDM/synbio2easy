@echo off

rem SynBioHub-CLI

rem This script opens a Command Prompt window and launches the SynBioHub-CLI jar with Java

rem set the version of jdk you would like to use (1.4, 1.5, 1.6, etc)
set JDK_Version=16

echo.
echo Locating JDK %JDK_Version%

for /d %%i in ("%ProgramFiles%\Java\jdk-%jdk_Version%*") do (set Located=%%i)
rem check if JDK was located
if "%Located%" == "" goto else
rem if JDK located display message to user
rem update %JAVA_HOME%
set JAVA_HOME=%Located%
echo     Located JDK %jdk_Version%
echo     JAVA_HOME has been set to:
echo         %JAVA_HOME%
goto endif

:else
rem if JDK was not located
rem if %JAVA_HOME% has been defined then use the existing value
echo     Could not locate JDK %JDK_Version%
if "%JAVA_HOME%" == "" goto NoExistingJavaHome
echo     Existing value of JAVA_HOME will be used:
echo         %JAVA_HOME%
goto endif

:NoExistingJavaHome
rem display message to the user that %JAVA_HOME% is not available
echo     No Existing value of JAVA_HOME is available
goto endif

:endif
rem clear the variables used by this script
set JDK_Version=
set Located=

if NOT "%JAVA_HOME%" == "" {
	start cmd /k "%JAVA_HOME%/bin/java" -jar %cd%/SynBio2Easy.jar
}
