param(
    [string]$LogPath = (Join-Path $PSScriptRoot "..\docs\scrum\sprint-2-bounty.log")
)

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

$script:logLines = New-Object System.Collections.Generic.List[string]
function Write-Both([string]$text) {
    Write-Host $text
    $script:logLines.Add($text)
}

$base = "http://localhost:8080"
$studentUsername = "2023001"
$studentPassword = "Campus2026"
$teacherUsername = "T2001"
$teacherPassword = "Campus2026"

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
    Write-Both ("{0,-4} {1,-36} -> {2}" -f $method, $path, $resp.StatusCode)
    if ($null -ne $parsed) { Write-Both ($parsed | ConvertTo-Json -Compress) }
    Write-Both ""
    [pscustomobject]@{ Status = $resp.StatusCode; Json = $parsed }
}

Write-Both "=== CampusOverflow Sprint 2 bounty smoke ==="
Write-Both ("Time: {0}" -f (Get-Date -Format "yyyy-MM-dd HH:mm:ss"))
Write-Both ""

# 1. Student login and create a question in the demo course.
$csrf = Get-Csrf
$login = Send-Json "POST" "/api/v1/auth/login" @{ username = $studentUsername; password = $studentPassword } $csrf

$csrf = Get-Csrf
$courses = Invoke-RestMethod -Uri "$base/api/v1/courses" -Method Get -WebSession $session -UseBasicParsing
if ($courses.Count -eq 0) { throw "No course found; demo seed is not ready" }
$courseId = $courses[0].id

$question = Send-Json "POST" "/api/v1/questions" @{
    courseId = $courseId
    title = "S2 bounty demo: why must a red-black tree rotate after insert?"
    body = "This question is created by bounty-smoke.ps1 to verify the teacher bounty, frozen points, and expiry flow."
    tags = @("smoke", "data-structure")
} $csrf
$questionId = $question.Json.id

# 2. Teacher login and record the account before opening the bounty.
$csrf = Get-Csrf
$teacherLogin = Send-Json "POST" "/api/v1/auth/login" @{ username = $teacherUsername; password = $teacherPassword } $csrf
$teacherId = $teacherLogin.Json.id

$csrf = Get-Csrf
$before = Invoke-RestMethod -Uri "$base/api/v1/users/$teacherId/reputation" -Method Get -WebSession $session -UseBasicParsing
$beforeAvailable = [int]$before.availablePoints
$beforeFrozen = [int]$before.frozenPoints
Write-Both "Teacher before: available=$beforeAvailable, frozen=$beforeFrozen"
Write-Both ""

# 3. Open a 50-point / 7-day bounty.
$bounty = Send-Json "POST" "/api/v1/questions/$questionId/bounty" @{ points = 50; days = 7 } $csrf
$bountyId = $bounty.Json.id

$latest = Invoke-RestMethod -Uri "$base/api/v1/questions/$questionId/bounty" -Method Get -WebSession $session -UseBasicParsing
Write-Both "Bounty: status=$($latest.status), points=$($latest.points), expiresAt=$($latest.expiresAt)"
Write-Both ""

# 4. Check the account after opening the bounty.
$after = Invoke-RestMethod -Uri "$base/api/v1/users/$teacherId/reputation" -Method Get -WebSession $session -UseBasicParsing
$afterAvailable = [int]$after.availablePoints
$afterFrozen = [int]$after.frozenPoints

Write-Both "=== Summary ==="
Write-Both "questionId=$questionId, bountyId=$bountyId"
Write-Both "teacher before: available=$beforeAvailable, frozen=$beforeFrozen"
Write-Both "teacher after : available=$afterAvailable, frozen=$afterFrozen"
Write-Both "bounty        : status=$($latest.status), points=$($latest.points)"

if ($afterAvailable -ne $beforeAvailable - 50) {
    throw "Available points mismatch: before=$beforeAvailable, after=$afterAvailable"
}
if ($afterFrozen -ne $beforeFrozen + 50) {
    throw "Frozen points mismatch: before=$beforeFrozen, after=$afterFrozen"
}
if ($latest.status -ne "OPEN" -or $latest.points -ne 50) {
    throw "Bounty state mismatch: status=$($latest.status), points=$($latest.points)"
}

Write-Both "=== All checks passed ==="
$script:logLines | Set-Content -Path $LogPath -Encoding UTF8
