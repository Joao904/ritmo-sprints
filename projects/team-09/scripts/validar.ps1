param([switch]$ComEmulador)
$ErrorActionPreference = 'Stop'
$projectPath = Join-Path $PSScriptRoot '..\app'
Push-Location $projectPath
try {
    $tasks = @('assembleDebug', 'lintDebug')
    if ($ComEmulador) { $tasks += 'connectedDebugAndroidTest' }
    & .\gradlew.bat @tasks --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'Falha na validação Gradle; consulte a saída acima.' }
    Write-Host 'Validação concluída. APK: app\build\outputs\apk\debug\app-debug.apk'
} finally {
    Pop-Location
}
