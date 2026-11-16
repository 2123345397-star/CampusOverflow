param(
    [string]$LogPath = (Join-Path $PSScriptRoot "..\docs\lab4-submission\screenshots\qingcen\qa-vote-comment.log")
)

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

$script:logLines = New-Object System.Collections.Generic.List[string]
function Write-Both([string]$text) {
    Write-Host $text
    $script:logLines.Add($text)
}

Write-Both "=== CampusOverflow qa.http smoke: vote + comment ==="
Write-Both ("Time: {0}" -f (Get-Date -Format "yyyy-MM-dd HH:mm:ss"))
Write-Both ""

$base = "http://localhost:8080"
$username = "student2026"
$password = "Passw0rd123"
$questionId = 1

$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$results = @()

function Get-Csrf {
    $r = Invoke-RestMethod -Uri "$base/api/v1/auth/csrf" -Method Get -WebSession $session -UseBasicParsing
    $cookie = $session.Cookies.GetCookies([uri]$base)["XSRF-TOKEN"]
    [pscustomobject]@{ Header = $r.headerName; Token = $cookie.Value }
}

function Send-Json([string]$method, [string]$path, $body, $csrf) {
    $json = $body | ConvertTo-Json -Compress
    $bytes = [System.Text.Encoding]::UTF8.GetBytes($json)
    $resp = Invoke-WebRequest -Uri "$base$path" -Method $method -WebSession $session `
        -Headers @{ $csrf.Header = $csrf.Token } `
        -ContentType "application/json; charset=utf-8" -Body $bytes -UseBasicParsing
    Write-Both ("{0,-4} {1,-24} -> {2}" -f $method, $path, $resp.StatusCode)
    Write-Both $resp.Content
    Write-Both ""
    $script:results += [pscustomobject]@{ Method = $method; Path = $path; Status = $resp.StatusCode; Body = $resp.Content }
}

$csrf = Get-Csrf
Send-Json "POST" "/api/v1/auth/login" @{ username = $username; password = $password } $csrf

$csrf = Get-Csrf
Send-Json "PUT" "/api/v1/votes" @{ targetType = "QUESTION"; targetId = $questionId; value = 1 } $csrf
Send-Json "PUT" "/api/v1/votes" @{ targetType = "QUESTION"; targetId = $questionId; value = -1 } $csrf
Send-Json "PUT" "/api/v1/votes" @{ targetType = "QUESTION"; targetId = $questionId; value = 0 } $csrf
Send-Json "POST" "/api/v1/comments" @{ targetType = "QUESTION"; targetId = $questionId; body = "这条评论来自 qa.http 冒烟脚本" } $csrf

Write-Both "=== Summary ==="
foreach ($r in $results) {
    Write-Both ("{0,-4} {1,-24} -> {2}  {3}" -f $r.Method, $r.Path, $r.Status, $r.Body)
}
Write-Both "=== All requests completed ==="

$logDir = Split-Path $LogPath -Parent
if (-not (Test-Path $logDir)) {
    New-Item -ItemType Directory -Path $logDir | Out-Null
}
$script:logLines | Set-Content -Path $LogPath -Encoding UTF8
