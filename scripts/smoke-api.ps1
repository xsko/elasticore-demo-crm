# Calls the running CRM (run-local.ps1) as a browser would - cookie session plus the
# X-XSRF-TOKEN header - and checks who sees what. Prints PASS/FAIL per check; exit 1 on a FAIL.
param([string]$Base = 'http://localhost:18090')
$ErrorActionPreference = 'Stop'
$script:fails = 0

function New-Browser { New-Object Microsoft.PowerShell.Commands.WebRequestSession }

function Xsrf($s) {
    $c = $s.Cookies.GetCookies([Uri]$Base) | Where-Object Name -eq 'XSRF-TOKEN' | Select-Object -First 1
    if ($c) { $c.Value } else { '' }
}

function Call($s, [string]$method, [string]$path, $body = $null, [string]$lang = 'ko', [switch]$NoToken) {
    $h = @{ 'Accept-Language' = $lang }
    if (-not $NoToken) { $h['X-XSRF-TOKEN'] = (Xsrf $s) }
    $s.Headers.Clear()   # PowerShell 5.1 keeps -Headers in the session otherwise
    $p = @{ Uri = "$Base$path"; Method = $method; WebSession = $s; Headers = $h; UseBasicParsing = $true; ContentType = 'application/json; charset=utf-8' }
    if ($null -ne $body) { $p.Body = [Text.Encoding]::UTF8.GetBytes(($body | ConvertTo-Json -Depth 8 -Compress)) }
    try {
        $r = Invoke-WebRequest @p
        $text = [Text.Encoding]::UTF8.GetString($r.RawContentStream.ToArray())
        return [pscustomobject]@{ Status = [int]$r.StatusCode; Json = $(if ($text) { $text | ConvertFrom-Json } else { $null }); Text = $text }
    } catch [System.Net.WebException] {
        $resp = $_.Exception.Response
        if (-not $resp) { throw }
        $sr = New-Object IO.StreamReader($resp.GetResponseStream(), [Text.Encoding]::UTF8)
        $text = $sr.ReadToEnd()
        $json = $null; try { $json = $text | ConvertFrom-Json } catch {}
        return [pscustomobject]@{ Status = [int]$resp.StatusCode; Json = $json; Text = $text }
    }
}

function Check([string]$name, [bool]$ok, [string]$detail = '') {
    if ($ok) { "PASS  $name" } else { $script:fails++; "FAIL  $name  $detail" }
}

function SignIn($s, $login, $password = 'demo1234') {
    Call $s GET '/api/crm/member/me' | Out-Null      # gets the XSRF-TOKEN cookie
    Call $s POST '/api/crm/member/signin' @{ login = $login; password = $password }
}

# --- visitor ---------------------------------------------------------------------
$v = New-Browser
$r = Call $v GET '/api/crm/member/me'
Check 'visitor: me is 401' ($r.Status -eq 401) $r.Status
Check 'visitor: an XSRF-TOKEN cookie is given' ((Xsrf $v) -ne '')
$r = Call $v POST '/api/crm/product/search' @{}
Check 'visitor: the catalog is public' ($r.Status -eq 200 -and $r.Json.totalElements -gt 0) $r.Status
$r = Call $v POST '/api/crm/account/search' @{}
Check 'visitor: accounts need a sign-in (401)' ($r.Status -eq 401) $r.Status
$r = Call $v POST '/api/crm/member/signin' @{ login = 'minjun'; password = 'demo1234' } -NoToken
Check 'csrf: a write without X-XSRF-TOKEN is 403' ($r.Status -eq 403) "$($r.Status) $($r.Text)"

# --- a sales rep -----------------------------------------------------------------
$rep = New-Browser
Call $rep GET '/api/crm/member/me' | Out-Null
$before = ($rep.Cookies.GetCookies([Uri]$Base) | Where-Object Name -eq 'CRMSESSION').Value
$r = Call $rep POST '/api/crm/member/signin' @{ login = 'minjun'; password = 'demo1234' }
Check 'rep: sign-in' ($r.Status -eq 200 -and $r.Json.role -eq 'R') "$($r.Status) $($r.Text)"
$after = ($rep.Cookies.GetCookies([Uri]$Base) | Where-Object Name -eq 'CRMSESSION').Value
Check 'rep: the session id changes on sign-in' ($before -ne $after) "before=$before after=$after"
$me = $r.Json.memberId
$r = Call $rep POST '/api/crm/account/search' @{ pageSize = 100 }
$own = @($r.Json.content | Where-Object { $_.ownerMemberId -ne $me }).Count
Check "rep: sees only own accounts ($($r.Json.totalElements))" ($r.Status -eq 200 -and $r.Json.totalElements -gt 0 -and $own -eq 0) "others=$own"
Check 'rep: the answer names the owner (ownerName)' ($r.Json.content[0].ownerName -ne $null)
$r = Call $rep POST '/api/crm/contact/search' @{ pageSize = 50 }
$phone = @($r.Json.content | Where-Object { $_.phone } | Select-Object -First 1).phone
Check "rep: contact phone is masked ($phone)" ($phone -match '\*') $phone

$jihoon = New-Browser; SignIn $jihoon 'jihoon' | Out-Null
$theirs = (Call $jihoon POST '/api/crm/account/search' @{ pageSize = 1 }).Json.content[0].accountId
$r = Call $rep GET "/api/crm/account/$theirs"
Check "rep: another rep's account is 404" ($r.Status -eq 404) $r.Status

$r = Call $rep POST '/api/crm/opportunity/search' @{ pageNumber = 1; pageSize = 7; sortCode = 'amount-' }
$amounts = @($r.Json.content | ForEach-Object { [decimal]$_.amount })
$sorted = @($amounts | Sort-Object -Descending)
Check "paging: page 2 of 7, sorted by amount desc (total $($r.Json.totalElements))" ($r.Json.number -eq 1 -and $r.Json.size -eq 7 -and ($amounts -join ',') -eq ($sorted -join ',')) ($amounts -join ',')

# lifecycle: a new deal moves only along its steps
$acct = (Call $rep POST '/api/crm/account/search' @{ pageSize = 1 }).Json.content[0].accountId
$r = Call $rep POST '/api/crm/opportunity' @{ name = 'Smoke test deal'; accountId = $acct; amount = 1000000 }
$deal = $r.Json.opportunityId
Check 'deal: created in PROSPECT (PRS)' ($r.Status -eq 200 -and $r.Json.stage -eq 'PRS') "$($r.Status) $($r.Text)"
$r = Call $rep POST "/api/crm/opportunity/$deal/win"
Check 'deal: win from PROSPECT is refused (409)' ($r.Status -eq 409) "$($r.Status) $($r.Json.code)"
$r = Call $rep POST "/api/crm/opportunity/$deal/propose"
Check 'deal: propose -> PROPOSAL' ($r.Json.stage -eq 'PRP') $r.Text
$r = Call $rep POST "/api/crm/opportunity/$deal/win"
Check 'deal: win -> WON with wonAt' ($r.Json.stage -eq 'WON' -and $r.Json.wonAt) $r.Text

# lead conversion makes an account, a contact and a deal
$co = 'Smoke Test ' + (Get-Random -Maximum 999999)
$r = Call $rep POST '/api/crm/lead' @{ company = $co; contactName = 'Tester'; estimatedValue = 5000000; source = 'WEB' }
$lead = $r.Json.leadId
Call $rep POST "/api/crm/lead/$lead/qualify" | Out-Null
$r = Call $rep POST "/api/crm/lead/$lead/convert"
Check 'lead: convert -> CONVERTED' ($r.Json.status -eq 'CNV' -and $r.Json.convertedAt) $r.Text
$r = Call $rep POST '/api/crm/account/search' @{ name = $co }
Check 'lead: the conversion made an account owned by the rep' ($r.Json.totalElements -eq 1 -and $r.Json.content[0].ownerMemberId -eq $me) $r.Text

# messages in the request's language
$r = Call $rep POST '/api/crm/opportunity' @{ accountId = $acct } 'en'
$en = $r.Text
$r = Call $rep POST '/api/crm/opportunity' @{ accountId = $acct } 'ko'
$ko = $r.Text
Check 'i18n: a 400 answers in English for Accept-Language: en' ($en -match 'required|must not' -and $en -ne $ko) $en
Check 'i18n: and in Korean for ko' ($ko -match '[\uAC00-\uD7A3]') $ko

# --- a customer ----------------------------------------------------------------------
$cu = New-Browser
$r = SignIn $cu 'customer'
Check 'customer: sign-in' ($r.Status -eq 200 -and $r.Json.role -eq 'C') $r.Text
$r = Call $cu POST '/api/crm/account' @{ name = 'Should not exist' }
Check 'customer: cannot create an account (403)' ($r.Status -eq 403) "$($r.Status) $($r.Text)"
$r = Call $cu POST '/api/crm/ticket/search' @{ pageSize = 100 }
$cid = (Call $cu GET '/api/crm/member/me').Json.memberId
$foreign = @($r.Json.content | Where-Object { $_.customerMemberId -ne $cid }).Count
Check "customer: sees only own tickets ($($r.Json.totalElements))" ($r.Status -eq 200 -and $foreign -eq 0) "others=$foreign"
$r = Call $cu POST '/api/crm/ticket' @{ subject = 'Smoke inquiry'; body = 'Hello'; category = 'ETC' }
Check 'customer: opens a ticket in OPEN' ($r.Status -eq 200 -and $r.Json.status -eq 'OPN') "$($r.Status) $($r.Text)"
$t = $r.Json.ticketId
$r = Call $cu POST "/api/crm/ticket/$t/answer"
Check 'customer: cannot answer (403)' ($r.Status -eq 403) $r.Status

# --- sign-up --------------------------------------------------------------------------
$nu = New-Browser; Call $nu GET '/api/crm/member/me' | Out-Null
$login = 'smoke' + (Get-Random -Maximum 99999)
$r = Call $nu POST '/api/crm/member' @{ login = $login; password = 'pass12345'; name = 'Smoke'; email = "$login@example.com"; role = 'A' }
Check 'sign-up: role asked as admin is stored as customer' ($r.Status -eq 200 -and $r.Json.role -eq 'C' -and -not $r.Json.password) "$($r.Status) $($r.Text)"
$r = Call $nu POST '/api/crm/member' @{ login = $login + 'x'; password = 'onlyletters'; name = 'Smoke'; email = "x$login@example.com" }
Check 'sign-up: a password without a digit is 400' ($r.Status -eq 400) $r.Status

# --- password guessing ------------------------------------------------------------------
$g = New-Browser; Call $g GET '/api/crm/member/me' | Out-Null
for ($i = 0; $i -lt 5; $i++) { Call $g POST '/api/crm/member/signin' @{ login = 'yuna'; password = 'wrong-pass1' } | Out-Null }
$r = Call $g POST '/api/crm/member/signin' @{ login = 'yuna'; password = 'demo1234' }
Check 'throttle: after 5 wrong passwords the right one is refused too' ($r.Status -eq 401) $r.Status

# --- headers ---------------------------------------------------------------------------
$raw = Invoke-WebRequest -UseBasicParsing "$Base/api/crm/product/search" -Method Post -ContentType 'application/json' -Body '{}' -Headers @{ 'X-XSRF-TOKEN' = (Xsrf $v) } -WebSession $v
Check 'headers: CSP and nosniff' ($raw.Headers['Content-Security-Policy'] -match "frame-ancestors 'none'" -and $raw.Headers['X-Content-Type-Options'] -eq 'nosniff')

if ($script:fails -gt 0) { "`n$($script:fails) check(s) failed"; exit 1 } else { "`nall checks passed" }
