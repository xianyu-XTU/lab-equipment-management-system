@echo off
title Lab Equipment System - Start Backend and Open Login

echo ============================================================
echo Lab Equipment Management System - Start
echo ============================================================
echo.

set "PROJECT_ROOT=%~dp0"
set "BACKEND_DIR=%PROJECT_ROOT%backend"
set "LOGIN_URL=%PROJECT_ROOT%/frontend/login.html"

echo Project root:
echo %PROJECT_ROOT%
echo.

if not exist "%BACKEND_DIR%\pom.xml" (
    echo [ERROR] Cannot find backend\pom.xml.
    echo Please put this bat file in the project root directory.
    pause
    exit /b 1
)

echo [1/4] Check Java...
java -version
if errorlevel 1 (
    echo [ERROR] Java not found. Please install JDK 17 or newer.
    pause
    exit /b 1
)

echo.
echo [2/4] Check Maven...
call mvn -version
if errorlevel 1 (
    echo [ERROR] Maven not found. Please install Maven and add it to PATH.
    pause
    exit /b 1
)

echo.
echo [3/4] Check MySQL80 service...
sc query MySQL80 | find "RUNNING" >nul
if errorlevel 1 (
    echo MySQL80 is not running. Trying to start it...
    net start MySQL80
    if errorlevel 1 (
        echo [ERROR] Failed to start MySQL80. Please start MySQL manually.
        pause
        exit /b 1
    )
) else (
    echo MySQL80 is running.
)

echo.
echo [4/4] Set MySQL environment variables...
set /p MYSQL_USERNAME=MySQL username, press Enter for root: 
if "%MYSQL_USERNAME%"=="" set "MYSQL_USERNAME=root"

set /p MYSQL_PASSWORD=MySQL password, press Enter for 123456: 
if "%MYSQL_PASSWORD%"=="" set "MYSQL_PASSWORD=123456"

set "MYSQL_USER=%MYSQL_USERNAME%"

echo.
echo MySQL username: %MYSQL_USERNAME%
echo.

echo Starting backend on port 8080...
start "Lab Backend - 8080" cmd /k "cd /d "%BACKEND_DIR%" && echo Backend URL: http://localhost:8080 && call mvn spring-boot:run"

echo.
echo Waiting for backend to start...
timeout /t 8 /nobreak >nul

echo Opening login page:
echo %LOGIN_URL%
start "" "%LOGIN_URL%"

echo.
echo ============================================================
echo Backend started in a new window.
echo Login page opened on port 8080.
echo Keep the backend window open while using the system.
echo ============================================================
echo.
pause
