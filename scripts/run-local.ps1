# Builds the jar and starts the CRM in the background on a port (default 18090), writing
# its process id to build\app.pid and its log to build\app.log. Stop it with stop-local.ps1.
param([int]$Port = 18090, [switch]$NoBuild)
$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
if (Test-Path build\app.pid) {
    Stop-Process -Id (Get-Content build\app.pid) -ErrorAction SilentlyContinue
    Remove-Item build\app.pid
    Start-Sleep 1
}
if (-not $NoBuild) {
    # npm and Gradle write warnings to stderr; only the build's own result decides.
    $ErrorActionPreference = 'Continue'
    $out = & .\gradlew.bat bootJar --console=plain 2>&1 | ForEach-Object { "$_" }
    $ErrorActionPreference = 'Stop'
    $out | Select-String 'BUILD|error:|FAIL' | ForEach-Object { $_.Line }
    if (-not ($out -match 'BUILD SUCCESSFUL')) { 'the build failed'; exit 1 }
}
$jar = Get-ChildItem build\libs\*.jar | Where-Object { $_.Name -notlike '*-plain.jar' } | Select-Object -First 1
New-Item -ItemType Directory -Force build\tmp | Out-Null
if (Test-Path build\app.log) { Remove-Item build\app.log }
$app = Start-Process java -PassThru -WindowStyle Hidden -WorkingDirectory $PWD -ArgumentList `
    "`"-Djava.io.tmpdir=$PWD\build\tmp`"", '-jar', "`"$($jar.FullName)`"", "--server.port=$Port", "`"--logging.file.name=$PWD\build\app.log`""
$app.Id | Set-Content build\app.pid
for ($i = 0; $i -lt 150; $i++) {
    if (-not (Get-Process -Id $app.Id -ErrorAction SilentlyContinue)) {
        'the server stopped:'; Get-Content build\app.log -Tail 30 -ErrorAction SilentlyContinue; exit 1
    }
    try { Invoke-WebRequest -UseBasicParsing -TimeoutSec 2 "http://localhost:$Port/api/crm/product/1" | Out-Null; break } catch { Start-Sleep 1 }
}
"started pid $($app.Id) on http://localhost:$Port"
