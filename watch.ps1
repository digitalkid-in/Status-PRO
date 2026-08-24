# Auto-Reload Watcher for Status PRO
# Automatically recompiles, installs, and restarts the app whenever you save code.

param(
    [string]$WatchPath = "$PSScriptRoot\app\src"
)

$SdkDir = "C:\Users\anon\AppData\Local\Android\Sdk"
if (-not (Test-Path $SdkDir)) {
    if ($env:ANDROID_HOME -and (Test-Path $env:ANDROID_HOME)) {
        $SdkDir = $env:ANDROID_HOME
    }
}
$AdbPath = Join-Path $SdkDir "platform-tools\adb.exe"
$PackageName = "in.digitalkid.statuspro"
$MainActivity = "com.example.MainActivity"

Write-Host "=================================================" -ForegroundColor Cyan
Write-Host "   Status PRO - Live Auto-Reload Watcher Mode    " -ForegroundColor Cyan
Write-Host "=================================================" -ForegroundColor Cyan
Write-Host "Watching for changes in: $WatchPath" -ForegroundColor Yellow
Write-Host "Whenever you save a file (Ctrl+S), the emulator will auto-update!`n" -ForegroundColor Green

function Reload-App {
    param([string]$ChangedFile)
    $nowStr = Get-Date -Format "HH:mm:ss"
    Write-Host "`n[$nowStr] Change detected: $ChangedFile" -ForegroundColor Yellow
    Write-Host "[$nowStr] Building and updating emulator..." -ForegroundColor Gray

    Push-Location $PSScriptRoot
    try {
        & .\gradlew.bat installDebug
        if ($LASTEXITCODE -eq 0) {
            & $AdbPath shell "am force-stop $PackageName && am start -n $PackageName/$MainActivity" | Out-Null
            $doneStr = Get-Date -Format "HH:mm:ss"
            Write-Host "[$doneStr] App updated and reloaded on emulator!" -ForegroundColor Green
        } else {
            $errStr = Get-Date -Format "HH:mm:ss"
            Write-Host "[$errStr] Build failed. Review errors above." -ForegroundColor Red
        }
    } finally {
        Pop-Location
    }
    Write-Host "Waiting for next code change (Ctrl+C to stop)..." -ForegroundColor Cyan
}

$watcher = New-Object System.IO.FileSystemWatcher
$watcher.Path = $WatchPath
$watcher.IncludeSubdirectories = $true
$watcher.NotifyFilter = [System.IO.NotifyFilters]'FileName, LastWrite, Size'

try {
    while ($true) {
        $result = $watcher.WaitForChanged([System.IO.WatcherChangeTypes]::Changed -bor [System.IO.WatcherChangeTypes]::Created, 1000)
        if (-not $result.TimedOut) {
            $file = $result.Name
            if ($file -match '\.(kt|xml|kts|properties)$') {
                # Brief sleep to allow editor file flush and debounce duplicate events
                Start-Sleep -Milliseconds 600
                Reload-App $file
            }
        }
    }
} finally {
    $watcher.Dispose()
}
