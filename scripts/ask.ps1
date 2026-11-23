param([string]$Base = 'http://localhost:18090', [string]$Login = 'customer', [string]$Lang = 'ko', [string[]]$Questions)
$ErrorActionPreference = 'Stop'
$s = New-Object Microsoft.PowerShell.Commands.WebRequestSession
try { Invoke-WebRequest -UseBasicParsing "$Base/api/crm/member/me" -WebSession $s | Out-Null } catch { }
function Tok { ($s.Cookies.GetCookies([Uri]$Base) | Where-Object Name -eq 'XSRF-TOKEN').Value }
function Post($path, $body) {
  $s.Headers.Clear()
  $r = Invoke-WebRequest -UseBasicParsing "$Base$path" -Method Post -WebSession $s -ContentType 'application/json; charset=utf-8' -Headers @{ 'X-XSRF-TOKEN' = (Tok); 'Accept-Language' = $Lang } -Body ([Text.Encoding]::UTF8.GetBytes(($body | ConvertTo-Json -Compress -Depth 5)))
  [Text.Encoding]::UTF8.GetString($r.RawContentStream.ToArray()) | ConvertFrom-Json
}
Post '/api/crm/member/signin' @{ login = $Login; password = 'demo1234' } | Out-Null
$conv = $null
foreach ($q in $Questions) {
  $t0 = Get-Date
  $a = Post '/api/crm/assistant' @{ message = $q; conversationId = $conv; language = $Lang }
  $conv = $a.conversationId
  "Q: $q  ($([int]((Get-Date) - $t0).TotalSeconds)s)"
  "A: $($a.answer)"
  "   sources: $(($a.sources | ForEach-Object { $_.entity + ':' + $_.title }) -join ' | ')"
  ''
}