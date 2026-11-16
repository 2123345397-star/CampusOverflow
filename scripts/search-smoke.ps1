param(
    [string]$LogPath = (Join-Path $PSScriptRoot "..\docs\scrum\sprint-3-search-hit.log")
)

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

$script:logLines = New-Object System.Collections.Generic.List[string]
function Write-Both([string]$text) {
    Write-Host $text
    $script:logLines.Add($text)
}

function From-Base64([string]$value) {
    return [System.Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($value))
}

$base = "http://localhost:8080"
$username = "2023001"
$password = "Campus2026"

$title1 = From-Base64("5pCc57Si6aqM5pS277ya57qi6buR5qCR5o+S5YWl5ZCO5Li65LuA5LmI5b+F6aG75peL6L2s77yf")
$body1 = From-Base64("6L+Z5pivIHNlYXJjaC1zbW9rZS5wczEg6Ieq5Yqo5Yib5bu655qE6Zeu6aKY77yM55So5LqO6aqM6K+B5Lit5paH5YWo5paH5qOA57Si44CB5pCc57Si5oqV5b2x5ZKM55u45YWz6Zeu6aKY5o6o6I2Q44CC")
$title2 = From-Base64("5pCc57Si6aqM5pS277ya57qi6buR5qCR55qE6buR6auY5Li65LuA5LmI5LiN5Y+Y6YeP5LiN6IO956C05Z2P77yf")
$tag = From-Base64("5pWw5o2u57uT5p6E")

$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession

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
    $parsed = if ([string]::IsNullOrWhiteSpace($resp.Content)) { $null } else { $resp.Content | ConvertFrom-Json }
    Write-Both ("{0,-4} {1,-42} -> {2}" -f $method, $path, $resp.StatusCode)
    if ($null -ne $parsed) { Write-Both ($parsed | ConvertTo-Json -Compress) }
    Write-Both ""
    [pscustomobject]@{ Status = $resp.StatusCode; Json = $parsed }
}

Write-Both "=== CampusOverflow Sprint 3 search smoke ==="
Write-Both ("Time: {0}" -f (Get-Date -Format "yyyy-MM-dd HH:mm:ss"))
Write-Both ""

# 1. Log in as a demo student and create two questions with a shared Chinese tag.
$csrf = Get-Csrf
$login = Send-Json "POST" "/api/v1/auth/login" @{ username = $username; password = $password } $csrf

$csrf = Get-Csrf
$courses = Invoke-RestMethod -Uri "$base/api/v1/courses" -Method Get -WebSession $session -UseBasicParsing
if ($courses.Count -eq 0) { throw "No course found; demo seed is not ready" }
$courseId = $courses[0].id

$q1 = Send-Json "POST" "/api/v1/questions" @{
    courseId = $courseId
    title = $title1
    body = $body1
    tags = @($tag, "search-test")
} $csrf
$questionId1 = $q1.Json.id

$q2 = Send-Json "POST" "/api/v1/questions" @{
    courseId = $courseId
    title = $title2
    body = $body1
    tags = @($tag, "search-test")
} $csrf
$questionId2 = $q2.Json.id

# 2. Wait for the outbox relay to project both questions into the search index.
$term = From-Base64("57qi6buR5qCR") # red-black tree
$encoded = [uri]::EscapeDataString($term)
$hits = $null
for ($i = 0; $i -lt 20; $i++) {
    Start-Sleep -Seconds 1
    $hits = Invoke-RestMethod -Uri "$base/api/v1/search/questions?q=$encoded" -Method Get -WebSession $session -UseBasicParsing
    if ($hits.total -ge 2) { break }
}

Write-Both "Search term: $term"
Write-Both "Search total: $($hits.total)"
if ($hits.total -lt 2) {
    throw "Search index did not return both questions; total=$($hits.total)"
}
foreach ($item in $hits.items) {
    Write-Both ("  hit id={0}, title={1}" -f $item.id, $item.title)
}
Write-Both ""

# 3. Related questions for the first question.
$related = Invoke-RestMethod -Uri "$base/api/v1/questions/$questionId1/related?limit=5" -Method Get -WebSession $session -UseBasicParsing
Write-Both "Related count: $($related.Count)"
foreach ($item in $related) {
    Write-Both ("  related id={0}, similarity={1}, title={2}" -f $item.id, $item.similarity, $item.title)
}
if ($related.Count -eq 0) {
    throw "Related question list is empty"
}

Write-Both ""
Write-Both "=== All checks passed ==="
$script:logLines | Set-Content -Path $LogPath -Encoding UTF8
