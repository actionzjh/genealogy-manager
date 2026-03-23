@echo off
setlocal

set "PROJECT_DIR=%~dp0"
cd /d "%PROJECT_DIR%"

if not defined JAVA_HOME (
  set "JAVA_HOME=D:\ProgramFiles\Java\jdk-17.0.1"
)

if not exist "%JAVA_HOME%\bin\java.exe" (
  echo [ERROR] JAVA_HOME is invalid: %JAVA_HOME%
  echo Please set JAVA_HOME to a valid JDK 17 path and try again.
  exit /b 1
)

set "PATH=%JAVA_HOME%\bin;%PATH%"
set "JAR_FILE=%PROJECT_DIR%target\genealogy-manager-1.0.0.jar"

if not exist "%JAR_FILE%" (
  echo [INFO] Packaged jar not found. Building it first...
  if not exist "%PROJECT_DIR%.tools\apache-maven-3.9.5\bin\mvn.cmd" (
    echo [ERROR] Local Maven not found at .tools\apache-maven-3.9.5\bin\mvn.cmd
    exit /b 1
  )

  call "%PROJECT_DIR%.tools\apache-maven-3.9.5\bin\mvn.cmd" -s "%PROJECT_DIR%.m2\settings.xml" package -DskipTests
  if errorlevel 1 exit /b %errorlevel%
)

java -jar "%JAR_FILE%"
