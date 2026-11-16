param(
    [string]$LogPath = (Join-Path $PSScriptRoot "..\docs\scrum\sprint-2-bounty-settle.log")
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
$askerUsername = "2023001"
$answererUsername = "2023002"
$teacherUsername = "T2001"
$password = "Campus2026"

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

function Login([string]$username) {
    $csrf = Get-Csrf
    $r = Send-Json "POST" "/api/v1/auth/login" @{ username = $username; password = $password } $csrf
    return $r.Json
}

Write-Both "=== CampusOverflow Sprint 2 bounty settlement smoke ==="
Write-Both ("Time: {0}" -f (Get-Date -Format "yyyy-MM-dd HH:mm:ss"))
Write-Both ""

# 1. Asker creates a question on the demo course.
$asker = Login $askerUsername
$csrf = Get-Csrf
$courses = Invoke-RestMethod -Uri "$base/api/v1/courses" -Method Get -WebSession $session -UseBasicParsing
if ($courses.Count -eq 0) { throw "No course found; demo seed is not ready" }
$courseId = $courses[0].id

$question = Send-Json "POST" "/api/v1/questions" @{
    courseId = $courseId
    title = "S2 settlement demo: how does the answer get accepted?"
    body = "This question is created by bounty-settle-smoke.ps1 to verify bounty settlement after an answer is accepted."
    tags = @("smoke", "settlement")
} $csrf
$questionId = $question.Json.id

# 2. Answerer submits an answer.
$answerer = Login $answererUsername
$csrf = Get-Csrf
$answer = Send-Json "POST" "/api/v1/questions/$questionId/answers" @{
    body = "This is the accepted answer used by the settlement smoke test. It is long enough for validation."
} $csrf
$answerId = $answer.Json.id

# 3. Teacher opens a 50-point / 7-day bounty.
$teacher = Login $teacherUsername
$csrf = Get-Csrf
$teacherBefore = Invoke-RestMethod -Uri "$base/api/v1/users/$($teacher.id)/reputation" -Method Get -WebSession $session -UseBasicParsing
$answererBefore = Login $answererUsername
$answererProfileBefore = Invoke-RestMethod -Uri "$base/api/v1/users/$($answererBefore.id)/reputation" -Method Get -WebSession $session -UseBasicParsing
Write-Both "Answerer reputation before: $($answererProfileBefore.reputation)"
Write-Both ""

$teacher = Login $teacherUsername
$csrf = Get-Csrf
$bounty = Send-Json "POST" "/api/v1/questions/$questionId/bounty" @{ points = 50; days = 7 } $csrf
$bountyId = $bounty.Json.id

# 4. Asker accepts the answer. Bounty settlement is driven by AnswerAcceptedEvent.
$asker = Login $askerUsername
$csrf = Get-Csrf
$accepted = Send-Json "PUT" "/api/v1/questions/$questionId/accepted-answer" @{ answerId = $answerId } $csrf

# 5. Wait for the outbox relay and handlers.
$latest = $null
for ($i = 0; $i -lt 15; $i++) {
    Start-Sleep -Seconds 1
    $latest = Invoke-RestMethod -Uri "$base/api/v1/questions/$questionId/bounty" -Method Get -WebSession $session -UseBasicParsing
    if ($latest.status -eq "AWARDED") { break }
}
Write-Both "Bounty after accept: status=$($latest.status), winnerId=$($latest.winnerId), answerId=$($latest.answerId)"
Write-Both ""

# 6. Check winner reputation and sponsor account.
$answerer = Login $answererUsername
$answererProfile = Invoke-RestMethod -Uri "$base/api/v1/users/$($answerer.id)/reputation" -Method Get -WebSession $session -UseBasicParsing
$teacher = Login $teacherUsername
$teacherAfter = Invoke-RestMethod -Uri "$base/api/v1/users/$($teacher.id)/reputation" -Method Get -WebSession $session -UseBasicParsing

Write-Both "=== Summary ==="
Write-Both "questionId=$questionId, answerId=$answerId, bountyId=$bountyId"
Write-Both "answerer before: reputation=$($answererProfileBefore.reputation)"
Write-Both "answerer after : reputation=$($answererProfile.reputation)"
Write-Both "teacher before: available=$($teacherBefore.availablePoints), frozen=$($teacherBefore.frozenPoints)"
Write-Both "teacher after : available=$($teacherAfter.availablePoints), frozen=$($teacherAfter.frozenPoints)"
Write-Both "bounty        : status=$($latest.status), points=$($latest.points)"

$answererGain = [int]$answererProfile.reputation - [int]$answererProfileBefore.reputation
if ($latest.status -ne "AWARDED" -or [int]$latest.points -ne 50) {
    throw "Bounty state mismatch: status=$($latest.status), points=$($latest.points)"
}
if ($answererGain -lt 65) {
    throw "Answerer reputation gain too small: $answererGain (expected accepted + bounty = 65)"
}
if ([int]$teacherAfter.frozenPoints -ne 0) {
    throw "Teacher frozen points should be zero after settlement, got $($teacherAfter.frozenPoints)"
}

Write-Both "=== All checks passed ==="
$script:logLines | Set-Content -Path $LogPath -Encoding UTF8
