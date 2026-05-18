@echo off
title Build Lab Equipment Release
setlocal enabledelayedexpansion

echo ============================================================
echo Build Lab Equipment Management System Release
echo ============================================================
echo.

cd /d "%~dp0"

set "PROJECT_ROOT=%~dp0"
set "BACKEND_DIR=%PROJECT_ROOT%backend"
set "FRONTEND_DIR=%PROJECT_ROOT%frontend"
set "STATIC_DIR=%BACKEND_DIR%\src\main\resources\static"
set "RELEASE_DIR=%PROJECT_ROOT%release"
set "START_B64_FILE=%TEMP%\lab_equipment_start_b64.txt"

echo Project root:
echo %PROJECT_ROOT%
echo.

if not exist "%BACKEND_DIR%\pom.xml" (
    echo [ERROR] Cannot find backend\pom.xml.
    echo Please put this file in the project root directory.
    pause
    exit /b 1
)

if not exist "%FRONTEND_DIR%" (
    echo [ERROR] Cannot find frontend directory.
    pause
    exit /b 1
)

echo [1/5] Copy frontend files into Spring Boot static directory...
if exist "%STATIC_DIR%" rmdir /s /q "%STATIC_DIR%"
mkdir "%STATIC_DIR%"

if exist "%FRONTEND_DIR%\*.html" copy /Y "%FRONTEND_DIR%\*.html" "%STATIC_DIR%\" >nul

if exist "%FRONTEND_DIR%\assets" (
    xcopy "%FRONTEND_DIR%\assets" "%STATIC_DIR%\assets" /E /I /Y >nul
)

if exist "%FRONTEND_DIR%\css" (
    xcopy "%FRONTEND_DIR%\css" "%STATIC_DIR%\css" /E /I /Y >nul
)

if exist "%FRONTEND_DIR%\js" (
    xcopy "%FRONTEND_DIR%\js" "%STATIC_DIR%\js" /E /I /Y >nul
)

if exist "%FRONTEND_DIR%\login.html" (
    copy /Y "%FRONTEND_DIR%\login.html" "%STATIC_DIR%\login.html" >nul
) else if exist "%FRONTEND_DIR%\sign up and  sign in v2.html" (
    copy /Y "%FRONTEND_DIR%\sign up and  sign in v2.html" "%STATIC_DIR%\login.html" >nul
) else (
    echo [WARN] No login.html or sign up page found in frontend.
)

(
echo ^<!DOCTYPE html^>
echo ^<html^>
echo ^<head^>
echo     ^<meta charset="UTF-8"^>
echo     ^<title^>Lab Equipment Management System^</title^>
echo     ^<script^>window.location.href="/login.html";^</script^>
echo ^</head^>
echo ^<body^>Redirecting to login page...^</body^>
echo ^</html^>
) > "%STATIC_DIR%\index.html"

echo Frontend files are now packed into backend static directory.
echo.

echo [2/5] Check Maven...
call mvn -version
if errorlevel 1 (
    echo [ERROR] Maven not found. Build machine must have Maven.
    echo User machine does NOT need Maven after release is built.
    pause
    exit /b 1
)

echo.
echo [3/5] Package Spring Boot jar...
cd /d "%BACKEND_DIR%"
call mvn clean package -DskipTests
if errorlevel 1 (
    echo [ERROR] Maven package failed.
    pause
    exit /b 1
)

echo.
echo [4/5] Create release folder...
cd /d "%PROJECT_ROOT%"

if exist "%RELEASE_DIR%" rmdir /s /q "%RELEASE_DIR%"
mkdir "%RELEASE_DIR%"
mkdir "%RELEASE_DIR%\database"

for %%f in ("%BACKEND_DIR%\target\*.jar") do (
    copy /Y "%%f" "%RELEASE_DIR%\lab-equipment.jar" >nul
)

if not exist "%RELEASE_DIR%\lab-equipment.jar" (
    echo [ERROR] Cannot find packaged jar in backend\target.
    pause
    exit /b 1
)

if exist "%PROJECT_ROOT%database\lab_equipment_create.sql" (
    copy /Y "%PROJECT_ROOT%database\lab_equipment_create.sql" "%RELEASE_DIR%\database\lab_equipment_create.sql" >nul
)

if exist "%PROJECT_ROOT%database\lab_equipment_data.sql" (
    copy /Y "%PROJECT_ROOT%database\lab_equipment_data.sql" "%RELEASE_DIR%\database\lab_equipment_data.sql" >nul
)

if exist "%PROJECT_ROOT%database\migrations" (
    xcopy "%PROJECT_ROOT%database\migrations" "%RELEASE_DIR%\database\migrations" /E /I /Y >nul
)

echo Release folder created.
echo.

echo [5/5] Create release\start.bat with frontend startup code...
(
echo QGVjaG8gb2ZmCnRpdGxlIExhYiBFcXVpcG1lbnQgTWFuYWdlbWVudCBTeXN0ZW0Kc2V0bG9jYWwK
echo CmNkIC9kICIlfmRwMCIKCnNldCAiTVlTUUxfRVhFPUM6XFByb2dyYW0gRmlsZXNcTXlTUUxcTXlT
echo UUwgU2VydmVyIDguMFxiaW5cbXlzcWwuZXhlIgoKZWNobyA9PT09PT09PT09PT09PT09PT09PT09
echo PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT0KZWNobyBMYWIgRXF1aXBtZW50
echo IE1hbmFnZW1lbnQgU3lzdGVtCmVjaG8gPT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09
echo PT09PT09PT09PT09PT09PT09PT09PT09PT09CmVjaG8uCgplY2hvIFsxLzRdIENoZWNrIEphdmEu
echo Li4KamF2YSAtdmVyc2lvbgppZiBlcnJvcmxldmVsIDEgKAogICAgZWNobyBbRVJST1JdIEphdmEg
echo bm90IGZvdW5kLiBQbGVhc2UgaW5zdGFsbCBKREsvSlJFIDE3IG9yIG5ld2VyLgogICAgcGF1c2UK
echo ICAgIGV4aXQgL2IgMQopCgplY2hvLgplY2hvIFsyLzRdIENoZWNrIE15U1FMODAgc2VydmljZS4u
echo LgpzYyBxdWVyeSBNeVNRTDgwIHwgZmluZCAiUlVOTklORyIgPm51bAppZiBlcnJvcmxldmVsIDEg
echo KAogICAgZWNobyBNeVNRTDgwIGlzIG5vdCBydW5uaW5nLiBUcnlpbmcgdG8gc3RhcnQgaXQuLi4K
echo ICAgIG5ldCBzdGFydCBNeVNRTDgwCiAgICBpZiBlcnJvcmxldmVsIDEgKAogICAgICAgIGVjaG8g
echo W0VSUk9SXSBGYWlsZWQgdG8gc3RhcnQgTXlTUUw4MC4gUGxlYXNlIHN0YXJ0IE15U1FMIG1hbnVh
echo bGx5LgogICAgICAgIHBhdXNlCiAgICAgICAgZXhpdCAvYiAxCiAgICApCikgZWxzZSAoCiAgICBl
echo Y2hvIE15U1FMODAgaXMgcnVubmluZy4KKQoKZWNoby4KZWNobyBbMy80XSBTZXQgTXlTUUwgY29u
echo ZmlnLi4uCnNldCAvcCBNWVNRTF9VU0VSTkFNRT1NeVNRTCB1c2VybmFtZSwgcHJlc3MgRW50ZXIg
echo Zm9yIHJvb3Q6IAppZiAiJU1ZU1FMX1VTRVJOQU1FJSI9PSIiIHNldCAiTVlTUUxfVVNFUk5BTUU9
echo cm9vdCIKCnNldCAvcCBNWVNRTF9QQVNTV09SRD1NeVNRTCBwYXNzd29yZCwgcHJlc3MgRW50ZXIg
echo Zm9yIDEyMzQ1NjogCmlmICIlTVlTUUxfUEFTU1dPUkQlIj09IiIgc2V0ICJNWVNRTF9QQVNTV09S
echo RD0xMjM0NTYiCgpzZXQgIk1ZU1FMX0hPU1Q9bG9jYWxob3N0IgpzZXQgIk1ZU1FMX1BPUlQ9MzMw
echo NiIKc2V0ICJNWVNRTF9EQVRBQkFTRT1sYWJfZXF1aXBtZW50IgpzZXQgIk1ZU1FMX1VTRVI9JU1Z
echo U1FMX1VTRVJOQU1FJSIKCmVjaG8uCmVjaG8gSW1wb3J0IG9yIHJlc2V0IGRhdGFiYXNlPwplY2hv
echo IEZpcnN0IHJ1bjogdHlwZSBZLiBOb3JtYWwgc3RhcnR1cDogcHJlc3MgRW50ZXIuCnNldCAvcCBJ
echo TVBPUlRfREI9VHlwZSBZIHRvIGltcG9ydCBkYXRhYmFzZSwgcHJlc3MgRW50ZXIgdG8gc2tpcDog
echo CgppZiAvSSAiJUlNUE9SVF9EQiUiPT0iWSIgKAogICAgaWYgZXhpc3QgIiVNWVNRTF9FWEUlIiAo
echo CiAgICAgICAgZWNoby4KICAgICAgICBlY2hvIEltcG9ydGluZyBkYXRhYmFzZSBzdHJ1Y3R1cmUu
echo Li4KICAgICAgICAiJU1ZU1FMX0VYRSUiIC11ICVNWVNRTF9VU0VSTkFNRSUgLXAlTVlTUUxfUEFT
echo U1dPUkQlIC0tZGVmYXVsdC1jaGFyYWN0ZXItc2V0PXV0ZjhtYjQgPCAiZGF0YWJhc2VcbGFiX2Vx
echo dWlwbWVudF9jcmVhdGUuc3FsIgogICAgICAgIGlmIGVycm9ybGV2ZWwgMSAoCiAgICAgICAgICAg
echo IGVjaG8gW0VSUk9SXSBGYWlsZWQgdG8gaW1wb3J0IGxhYl9lcXVpcG1lbnRfY3JlYXRlLnNxbC4K
echo ICAgICAgICAgICAgcGF1c2UKICAgICAgICAgICAgZXhpdCAvYiAxCiAgICAgICAgKQoKICAgICAg
echo ICBlY2hvLgogICAgICAgIGVjaG8gRml4aW5nIG5vdGljZS5pc190b3AgY29sdW1uIGlmIG5lZWRl
echo ZC4uLgogICAgICAgICIlTVlTUUxfRVhFJSIgLXUgJU1ZU1FMX1VTRVJOQU1FJSAtcCVNWVNRTF9Q
echo QVNTV09SRCUgLS1kZWZhdWx0LWNoYXJhY3Rlci1zZXQ9dXRmOG1iNCBsYWJfZXF1aXBtZW50IC1l
echo ICJBTFRFUiBUQUJMRSBub3RpY2UgQUREIENPTFVNTiBJRiBOT1QgRVhJU1RTIGlzX3RvcCBUSU5Z
echo SU5UIERFRkFVTFQgMCBDT01NRU5UICdpcyB0b3AnOyIKCiAgICAgICAgZWNoby4KICAgICAgICBl
echo Y2hvIEltcG9ydGluZyBpbml0aWFsIGRhdGEuLi4KICAgICAgICAiJU1ZU1FMX0VYRSUiIC11ICVN
echo WVNRTF9VU0VSTkFNRSUgLXAlTVlTUUxfUEFTU1dPUkQlIC0tZGVmYXVsdC1jaGFyYWN0ZXItc2V0
echo PXV0ZjhtYjQgbGFiX2VxdWlwbWVudCA8ICJkYXRhYmFzZVxsYWJfZXF1aXBtZW50X2RhdGEuc3Fs
echo IgogICAgICAgIGlmIGVycm9ybGV2ZWwgMSAoCiAgICAgICAgICAgIGVjaG8gW0VSUk9SXSBGYWls
echo ZWQgdG8gaW1wb3J0IGxhYl9lcXVpcG1lbnRfZGF0YS5zcWwuCiAgICAgICAgICAgIHBhdXNlCiAg
echo ICAgICAgICAgIGV4aXQgL2IgMQogICAgICAgICkKCiAgICAgICAgaWYgZXhpc3QgImRhdGFiYXNl
echo XG1pZ3JhdGlvbnNcMjAyNjA1MTBfYmFja2VuZF9pbmRleGVzLnNxbCIgKAogICAgICAgICAgICBl
echo Y2hvLgogICAgICAgICAgICBlY2hvIEltcG9ydGluZyBkYXRhYmFzZSBpbmRleGVzLi4uCiAgICAg
echo ICAgICAgICIlTVlTUUxfRVhFJSIgLXUgJU1ZU1FMX1VTRVJOQU1FJSAtcCVNWVNRTF9QQVNTV09S
echo RCUgLS1kZWZhdWx0LWNoYXJhY3Rlci1zZXQ9dXRmOG1iNCBsYWJfZXF1aXBtZW50IDwgImRhdGFi
echo YXNlXG1pZ3JhdGlvbnNcMjAyNjA1MTBfYmFja2VuZF9pbmRleGVzLnNxbCIKICAgICAgICApCiAg
echo ICApIGVsc2UgKAogICAgICAgIGVjaG8gW1dBUk5dIG15c3FsLmV4ZSBub3QgZm91bmQ6CiAgICAg
echo ICAgZWNobyAlTVlTUUxfRVhFJQogICAgICAgIGVjaG8gU2tpcCBkYXRhYmFzZSBpbXBvcnQuCiAg
echo ICApCikgZWxzZSAoCiAgICBlY2hvIFNraXAgZGF0YWJhc2UgaW1wb3J0LgopCgplY2hvLgplY2hv
echo IFs0LzRdIFN0YXJ0IGJhY2tlbmQgYW5kIGZyb250ZW5kIHBhZ2UuLi4KZWNobyBCYWNrZW5kIFVS
echo TDogaHR0cDovL2xvY2FsaG9zdDo4MDgwCmVjaG8gRnJvbnRlbmQgbG9naW4gcGFnZTogaHR0cDov
echo L2xvY2FsaG9zdDo4MDgwL2xvZ2luLmh0bWwKZWNoby4KZWNobyBTdGFydGluZyBmcm9udGVuZCBw
echo YWdlIGluIGJyb3dzZXIgYWZ0ZXIgYmFja2VuZCBpbml0aWFsaXphdGlvbi4uLgpzdGFydCAiIiBj
echo bWQgL2MgInRpbWVvdXQgL3QgOCAvbm9icmVhayA+bnVsICYmIHN0YXJ0IGh0dHA6Ly9sb2NhbGhv
echo c3Q6ODA4MC9sb2dpbi5odG1sIgoKZWNobyBTdGFydGluZyBiYWNrZW5kIGphci4uLgpqYXZhIC1q
echo YXIgbGFiLWVxdWlwbWVudC5qYXIKCnBhdXNlCg==
) > "%START_B64_FILE%"

powershell -NoProfile -ExecutionPolicy Bypass -Command "$b=(Get-Content -Raw $env:START_B64_FILE); [IO.File]::WriteAllBytes((Join-Path $env:RELEASE_DIR 'start.bat'), [Convert]::FromBase64String($b))"
if errorlevel 1 (
    echo [ERROR] Failed to create release\start.bat.
    pause
    exit /b 1
)

del /q "%START_B64_FILE%" >nul 2>nul

echo ============================================================
echo Release build finished.
echo Output folder:
echo %RELEASE_DIR%
echo.
echo The generated release\start.bat will:
echo 1. Start backend jar on port 8080
echo 2. Open frontend login page:
echo    http://localhost:8080/login.html
echo ============================================================
pause
