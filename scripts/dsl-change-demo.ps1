# The demo of what this project is about: one line added to the DSL reaches the database,
# the API, the screens (list, search, form), the Excel export, the labels in both languages,
# the Ossie semantic model and the AI assistant - with no other file changed.
#
#   1. adds the line without an English label: the build stops and says which label is missing
#   2. adds @label(en=...): the build passes; the generated files are checked
#   3. starts the server and runs the browser check (web/e2e/dsl-change.spec.ts)
#   4. takes the line out again and rebuilds (unless -Keep)
#
#   powershell -File scripts/dsl-change-demo.ps1 [-Keep]
param([switch]$Keep, [int]$Port = 18090)
$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
$model = 'src/main/resources/blueprint/crm/model/sales.yml'
$anchor = '      note:          text @label(en=Note) -- 메모'
$line = '      segment:       string(30) @search(like) @label(en=Segment) -- 고객 세그먼트'
$noEnglish = '      segment:       string(30) @search(like) -- 고객 세그먼트'
$utf8 = New-Object Text.UTF8Encoding $false
$original = [IO.File]::ReadAllText("$PWD/$model")
if (-not $original.Contains($anchor)) { throw "the anchor line is not in $model" }

function Step($text) { Write-Host "`n== $text" -ForegroundColor Cyan }
function Build {
    $ErrorActionPreference = 'Continue'   # a refused build writes to stderr: that is the point of step 1
    (.\gradlew.bat bootJar --console=plain 2>&1 | ForEach-Object { "$_" }) -join "`n"
}

try {
    Step '1. a field without its English label'
    [IO.File]::WriteAllText("$PWD/$model", $original.Replace($anchor, "$noEnglish`n$anchor"), $utf8)
    $out = Build
    if ($out -notmatch 'Account.segment has no label in en') { throw "expected the build to stop on the missing label`n$out" }
    Write-Host 'the build stopped: Account.segment has no label in en'

    Step '2. the same field with @label(en=Segment)'
    [IO.File]::WriteAllText("$PWD/$model", $original.Replace($anchor, "$line`n$anchor"), $utf8)
    $out = Build
    if ($out -notmatch 'BUILD SUCCESSFUL') { throw "the build failed`n$out" }
    $checks = @(
        @('src/main/java/io/elasticore/demo/crm/domain/model/Account.java', 'private String segment'),
        @('src/main/java/io/elasticore/demo/crm/application/dto/AccountSrchDTO.java', 'segment'),
        @('src/main/ts/AccountDTO.ts', 'segment?: string'),
        @('src/main/ts/AccountUi.ts', 'name: "segment"'),
        @('src/main/resources/i18n/crm-labels.properties', 'Account.segment='),
        @('src/main/resources/i18n/crm-labels_en.properties', 'Account.segment=Segment'),
        @('src/main/resources/blueprint/semantic_model.yml', 'name: segment')
    )
    foreach ($c in $checks) {
        if (-not (Select-String -Path $c[0] -SimpleMatch $c[1] -Quiet)) { throw "$($c[0]) has no $($c[1])" }
        Write-Host "  $($c[0]): $($c[1])"
    }

    Step '3. the screens'
    & powershell -NoProfile -ExecutionPolicy Bypass -File scripts/run-local.ps1 -NoBuild -Port $Port | Select-Object -Last 1
    Push-Location web
    try {
        $env:CRM_URL = "http://localhost:$Port"; $env:DSL_FIELD = 'segment'; $env:DSL_LABEL_KO = '고객 세그먼트'; $env:DSL_LABEL_EN = 'Segment'
        & npx playwright test e2e/dsl-change.spec.ts
        if ($LASTEXITCODE -ne 0) { throw 'the browser check failed' }
    } finally {
        Remove-Item Env:DSL_FIELD, Env:DSL_LABEL_KO, Env:DSL_LABEL_EN -ErrorAction SilentlyContinue
        Pop-Location
    }
    Write-Host "`nthe field reached the list, the search, the form, Excel and both languages" -ForegroundColor Green
} finally {
    if (-not $Keep) {
        Step '4. taking the field out again'
        & powershell -NoProfile -ExecutionPolicy Bypass -File scripts/stop-local.ps1
        [IO.File]::WriteAllText("$PWD/$model", $original, $utf8)
        Build | Select-String 'BUILD' | ForEach-Object { $_.Line }
    }
}
