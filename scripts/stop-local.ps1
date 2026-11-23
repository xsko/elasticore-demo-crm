# Stops the server run-local.ps1 started - that process only, by its id.
Set-Location (Split-Path $PSScriptRoot -Parent)
if (Test-Path build\app.pid) {
    Stop-Process -Id (Get-Content build\app.pid) -ErrorAction SilentlyContinue
    Remove-Item build\app.pid
    'stopped'
}
