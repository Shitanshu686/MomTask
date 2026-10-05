@echo off
title MomTask AI

cd /d "%~dp0"

echo.
echo ========================================
echo          MomTask AI
echo     Hindi/Hinglish Reminder Assistant
echo ========================================
echo.

echo [1/3] Checking Ollama...

curl.exe -s http://localhost:11434/api/tags >nul 2>&1

if errorlevel 1 (
    echo Ollama is not running.
    echo Starting Ollama...

    start "" /B ollama serve

    timeout /t 5 /nobreak >nul
)

echo Ollama is ready.
echo.

echo [2/3] Starting MomTask...

if not exist "target\momtask-0.0.1-SNAPSHOT.jar" (
    echo.
    echo ERROR: MomTask JAR not found.
    echo Please run:
    echo mvnw clean package -DskipTests
    echo.
    pause
    exit /b 1
)

start "" "http://localhost:8080"

echo [3/3] MomTask is starting...
echo.
echo Browser will open automatically.
echo.
echo Keep this window open while using MomTask.
echo.

java -jar "target\momtask-0.0.1-SNAPSHOT.jar"

pause
