@echo off
title Lab Equipment Backend Launcher

echo ============================================================
echo Lab Equipment Backend Launcher
echo ============================================================
echo.

cd /d "%~dp0"

echo [1/4] Check Java
java -version
if errorlevel 1 (
    echo Java not found. Please install JDK 17.
    pause
    exit /b 1
)

echo.
echo [2/4] Check Maven
call mvn -version
if errorlevel 1 (
    echo Maven not found. Please install Maven.
    pause
    exit /b 1
)

echo.
echo [3/4] Check MySQL80 service
sc query MySQL80 | find "RUNNING" >nul
if errorlevel 1 (
    echo MySQL80 is not running. Trying to start it...
    net start MySQL80
)

echo.
echo [4/4] Set MySQL environment
set /p MYSQL_USERNAME=MySQL username, press Enter for root: 
if "%MYSQL_USERNAME%"=="" set "MYSQL_USERNAME=root"

set /p MYSQL_PASSWORD=MySQL password, press Enter for 123456: 
if "%MYSQL_PASSWORD%"=="" set "MYSQL_PASSWORD=123456"

echo.
echo Starting backend...
echo Backend URL: http://localhost:8080
echo Press Ctrl + C to stop backend.
echo ============================================================
echo.

call mvn spring-boot:run

pause