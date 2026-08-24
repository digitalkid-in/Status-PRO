@echo off
setlocal enabledelayedexpansion

echo ==========================================
echo    Status PRO - Emulator ^& Install Tool   
echo ==========================================

set "SDK_DIR=C:\Users\anon\AppData\Local\Android\Sdk"
if not exist "%SDK_DIR%" (
    if defined ANDROID_HOME set "SDK_DIR=%ANDROID_HOME%"
    if defined ANDROID_SDK_ROOT set "SDK_DIR=%ANDROID_SDK_ROOT%"
)

set "ADB=%SDK_DIR%\platform-tools\adb.exe"
set "EMULATOR=%SDK_DIR%\emulator\emulator.exe"
set "AVDMANAGER=%SDK_DIR%\cmdline-tools\latest\bin\avdmanager.bat"
set "AVD_NAME=StatusPRO_Emulator"
set "PACKAGE_NAME=in.digitalkid.statuspro"
set "ACTIVITY_NAME=com.example.MainActivity"

echo.
echo [1/5] Checking Android Virtual Devices...
call "%AVDMANAGER%" list avd | findstr /C:"%AVD_NAME%" >nul
if %errorlevel% neq 0 (
    echo AVD '%AVD_NAME%' not found. Creating AVD with Android 35 (Google APIs)...
    echo no | call "%AVDMANAGER%" create avd -n "%AVD_NAME%" -k "system-images;android-35;google_apis;x86_64" --device "pixel_7" --force
    if !errorlevel! neq 0 (
        echo no | call "%AVDMANAGER%" create avd -n "%AVD_NAME%" -k "system-images;android-35;google_apis;x86_64" --force
    )
    echo AVD '%AVD_NAME%' created successfully!
) else (
    echo AVD '%AVD_NAME%' is ready.
)

echo.
echo [2/5] Checking running emulator devices...
"%ADB%" devices | findstr /R "emulator-[0-9]*.*device$" >nul
if %errorlevel% neq 0 (
    echo Starting emulator '%AVD_NAME%' in background...
    start "" "%EMULATOR%" -avd "%AVD_NAME%" -no-snapshot-load
    echo Waiting for emulator device to respond...
    "%ADB%" wait-for-device
    echo Waiting for Android OS to finish booting...
    :wait_boot
    timeout /t 3 /nobreak >nul
    for /f "tokens=*" %%a in ('"%ADB%" shell getprop sys.boot_completed 2^>nul') do (
        set "BOOTED=%%a"
    )
    if not "!BOOTED!"=="1" (
        echo Booting OS...
        goto wait_boot
    )
    echo Emulator booted successfully!
) else (
    echo Emulator is already running.
)

echo.
echo [3/5] Building Status PRO debug APK...
cd /d "%~dp0"
call gradlew.bat assembleDebug
if %errorlevel% neq 0 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo [4/5] Installing APK on emulator...
"%ADB%" install -r "%~dp0app\build\outputs\apk\debug\app-debug.apk"
if %errorlevel% neq 0 (
    echo Failed to install APK!
    pause
    exit /b 1
)

echo.
echo [5/5] Launching Status PRO...
"%ADB%" shell am start -n "%PACKAGE_NAME%/%ACTIVITY_NAME%"

echo.
echo ==========================================
echo  Status PRO is now running on the emulator!
echo ==========================================
pause
