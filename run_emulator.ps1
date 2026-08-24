# Run Android Emulator and Install Status PRO App
# PowerShell script for Status-PRO

$ErrorActionPreference = "Stop"

$SdkDir = "C:\Users\anon\AppData\Local\Android\Sdk"
if (-not (Test-Path $SdkDir)) {
    if ($env:ANDROID_HOME -and (Test-Path $env:ANDROID_HOME)) {
        $SdkDir = $env:ANDROID_HOME
    } elseif ($env:ANDROID_SDK_ROOT -and (Test-Path $env:ANDROID_SDK_ROOT)) {
        $SdkDir = $env:ANDROID_SDK_ROOT
    }
}

$AdbPath = Join-Path $SdkDir "platform-tools\adb.exe"
$EmulatorPath = Join-Path $SdkDir "emulator\emulator.exe"
$AvdManagerPath = Join-Path $SdkDir "cmdline-tools\latest\bin\avdmanager.bat"
$SdkManagerPath = Join-Path $SdkDir "cmdline-tools\latest\bin\sdkmanager.bat"

$AvdName = "StatusPRO_Emulator"
$PackageName = "in.digitalkid.statuspro"
$MainActivity = "com.example.MainActivity"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "   Status PRO - Emulator & Install Tool   " -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

# 1. Check if AVD exists
Write-Host "`n[1/5] Checking Android Virtual Devices..." -ForegroundColor Yellow
$avdList = & $AvdManagerPath list avd
if ($avdList -notmatch $AvdName) {
    Write-Host "AVD '$AvdName' not found. Creating AVD with Android 35 (Google APIs)..." -ForegroundColor Yellow
    echo "no" | & $AvdManagerPath create avd -n $AvdName -k "system-images;android-35;google_apis;x86_64" --device "pixel_7" --force
    if ($LASTEXITCODE -ne 0) {
        echo "no" | & $AvdManagerPath create avd -n $AvdName -k "system-images;android-35;google_apis;x86_64" --force
    }
    Write-Host "AVD '$AvdName' created successfully!" -ForegroundColor Green
} else {
    Write-Host "AVD '$AvdName' is ready." -ForegroundColor Green
}

# 2. Check if emulator is already running
Write-Host "`n[2/5] Checking running emulator devices..." -ForegroundColor Yellow
$devices = & $AdbPath devices
$isEmulatorRunning = $devices -match "emulator-\d+\s+device"

if (-not $isEmulatorRunning) {
    Write-Host "Starting emulator '$AvdName' in background..." -ForegroundColor Yellow
    Start-Process -FilePath $EmulatorPath -ArgumentList "-avd $AvdName -no-snapshot-load" -WindowStyle Normal
    
    Write-Host "Waiting for emulator to connect..." -ForegroundColor Yellow
    & $AdbPath wait-for-device
    
    Write-Host "Waiting for Android OS to finish booting..." -ForegroundColor Yellow
    $bootCompleted = $false
    $timeout = 180
    $timer = 0
    while (-not $bootCompleted -and $timer -lt $timeout) {
        Start-Sleep -Seconds 3
        $timer += 3
        $bootProp = & $AdbPath shell getprop sys.boot_completed
        if ($bootProp -match "1") {
            $bootCompleted = $true
        } else {
            Write-Host "Booting OS... ($($timer)s elapsed)" -ForegroundColor Gray
        }
    }
    if ($bootCompleted) {
        Write-Host "Emulator booted successfully!" -ForegroundColor Green
    } else {
        Write-Warning "Emulator boot check timed out, attempting to proceed..."
    }
} else {
    Write-Host "Emulator is already running." -ForegroundColor Green
}

# 3. Build the project
Write-Host "`n[3/5] Building Status PRO debug APK..." -ForegroundColor Yellow
$rootDir = $PSScriptRoot
Set-Location $rootDir
$buildResult = .\gradlew.bat assembleDebug
if ($LASTEXITCODE -ne 0) {
    Write-Error "Gradle build failed! Please review build errors."
    exit 1
}
Write-Host "Build completed successfully!" -ForegroundColor Green

# 4. Install APK
Write-Host "`n[4/5] Installing APK on emulator..." -ForegroundColor Yellow
$ApkPath = Join-Path $rootDir "app\build\outputs\apk\debug\app-debug.apk"
if (-not (Test-Path $ApkPath)) {
    Write-Error "APK file not found at $ApkPath"
    exit 1
}
& $AdbPath install -r $ApkPath
if ($LASTEXITCODE -ne 0) {
    Write-Error "Failed to install APK on emulator"
    exit 1
}
Write-Host "APK installed successfully!" -ForegroundColor Green

# 5. Launch App
Write-Host "`n[5/5] Launching Status PRO..." -ForegroundColor Yellow
& $AdbPath shell am start -n "$PackageName/$MainActivity"
Write-Host "`n>>> Status PRO is now running on the emulator! <<<" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
