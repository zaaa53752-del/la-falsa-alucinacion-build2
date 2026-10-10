@echo off
setlocal EnableExtensions EnableDelayedExpansion
set "ROOT=%~dp0"
cd /d "%ROOT%"
set "GRADLE_VERSION=8.8"
set "CACHE=%ROOT%.gradle-local"
set "GRADLE_HOME=%CACHE%\gradle-%GRADLE_VERSION%"
set "JAVA17_HOME="

echo ==============================================
echo  La Falsa Alucinacion - compilador Forge 1.20.1
echo ==============================================

rem Try the currently configured JAVA_HOME first.
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" (
  "%JAVA_HOME%\bin\java.exe" -version 2>&1 | findstr /C:"17." >nul
  if not errorlevel 1 set "JAVA17_HOME=%JAVA_HOME%"
)

rem If PATH still points to Java 8, look for a common JDK 17 installation.
if not defined JAVA17_HOME (
  for /d %%J in ("%ProgramFiles%\Java\jdk-17*" "%ProgramFiles%\Eclipse Adoptium\jdk-17*" "%ProgramFiles%\Microsoft\jdk-17*" "%LOCALAPPDATA%\Programs\Eclipse Adoptium\jdk-17*") do (
    if exist "%%~J\bin\java.exe" (
      "%%~J\bin\java.exe" -version 2>&1 | findstr /C:"17." >nul
      if not errorlevel 1 set "JAVA17_HOME=%%~J"
    )
  )
)

if not defined JAVA17_HOME (
  java -version 2>&1 | findstr /C:"17." >nul
  if not errorlevel 1 (
    rem If Java 17 is on PATH but JAVA_HOME is empty, derive its JDK folder.
    set "JAVA17_BIN="
    for /f "delims=" %%J in ('where java 2^>nul') do if not defined JAVA17_BIN set "JAVA17_BIN=%%~dpJ"
    if defined JAVA17_BIN for %%J in ("!JAVA17_BIN!..") do set "JAVA17_HOME=%%~fJ"
  )
)
if not defined JAVA17_HOME (
  echo ERROR: no se encontro Java 17.
  echo Comprueba donde esta instalado el JDK 17 y vuelve a ejecutar este archivo.
  echo Si sabes la carpeta del JDK, configura JAVA_HOME para esa carpeta.
  echo Minecraft 1.20.1 con ForgeGradle no se compila con Java 8.
  pause
  exit /b 1
)
set "JAVA_HOME=%JAVA17_HOME%"
set "PATH=%JAVA_HOME%\bin;%PATH%"
echo Usando JDK: %JAVA_HOME%
java -version

if not exist "%GRADLE_HOME%\bin\gradle.bat" (
  if not exist "%CACHE%" mkdir "%CACHE%"
  echo Descargando Gradle %GRADLE_VERSION%...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%CACHE%\gradle.zip'; Expand-Archive -LiteralPath '%CACHE%\gradle.zip' -DestinationPath '%CACHE%' -Force"
  if errorlevel 1 (
    echo ERROR: no se pudo descargar Gradle. Comprueba internet y vuelve a intentarlo.
    pause
    exit /b 1
  )
)
call "%GRADLE_HOME%\bin\gradle.bat" --no-daemon build
if errorlevel 1 (
  echo.
  echo ERROR: fallo la compilacion. Revisa el error anterior y que internet permita acceder a Forge Maven.
  pause
  exit /b 1
)
echo.
echo Build terminado. Busca el JAR en build\libs\la_falsa_alucinacion-1.0.0.jar
pause
