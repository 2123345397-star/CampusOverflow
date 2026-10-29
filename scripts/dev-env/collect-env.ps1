<#
.SYNOPSIS
    CampusOverflow - development environment collector (Lab 1 / role card E3)

.DESCRIPTION
    Collects JDK / Maven / Node.js / Docker / Git / OS versions and prints one
    Markdown row that can be pasted into docs/lab1-submission/env-matrix.md.
    Missing tools print "not installed" and never abort the script.

    NOTE: keep this file ASCII-only. Windows PowerShell 5.1 reads .ps1 files as
    ANSI (GBK) when they have no BOM, so non-ASCII text would break the parser.

.PARAMETER Name
    Member name, e.g. fangguodong

.PARAMETER GitHub
    GitHub username without @, e.g. 2177023844

.EXAMPLE
    powershell -NoProfile -ExecutionPolicy Bypass -File scripts/dev-env/collect-env.ps1 -Name fangguodong -GitHub 2177023844

.NOTES
    Owner: fangguodong (Dev-2)   Use: Lab 1 deliverable / E3 environment screenshots
#>
[CmdletBinding()]
param(
    [Parameter(Position = 0)][string]$Name = '(unnamed)',
    [Parameter(Position = 1)][string]$GitHub = '-'
)

$ErrorActionPreference = 'Continue'

function Get-FirstLine {
    param(
        [Parameter(Mandatory = $true)][string]$Command,
        [string[]]$Arguments = @('--version'),
        [string]$Regex
    )

    try {
        $output = (& $Command @Arguments 2>&1) | Out-String
    }
    catch {
        return 'not installed'
    }

    if ([string]::IsNullOrWhiteSpace($output)) { return 'not installed' }

    if ($Regex) {
        $match = [regex]::Match($output, $Regex)
        if ($match.Success) { return $match.Groups[1].Value.Trim() }
        return 'unknown'
    }

    return ($output.Trim() -split "`r?`n")[0].Trim()
}

# Git is often installed outside PATH; fall back to absolute paths.
$gitCommand = 'git'
foreach ($candidate in @(
        'D:\Program Files\Git\cmd\git.exe',
        'C:\Program Files\Git\cmd\git.exe',
        "$env:LOCALAPPDATA\Programs\Git\cmd\git.exe")) {
    if (Test-Path -LiteralPath $candidate) { $gitCommand = $candidate; break }
}

$javaVersion   = Get-FirstLine -Command 'java'   -Arguments @('-version') -Regex 'version "([^"]+)"'
$javaCompiler  = Get-FirstLine -Command 'javac'  -Arguments @('-version') -Regex 'javac ([0-9][^\s]*)'
$mavenVersion  = Get-FirstLine -Command 'mvn'    -Arguments @('-v')       -Regex 'Apache Maven ([0-9][^\s]*)'
$nodeVersion   = Get-FirstLine -Command 'node'   -Arguments @('-v')
$npmVersion    = Get-FirstLine -Command 'npm'    -Arguments @('-v')
$dockerVersion = Get-FirstLine -Command 'docker' -Arguments @('--version') -Regex 'Docker version ([^,]+)'
$gitVersion    = Get-FirstLine -Command $gitCommand -Arguments @('--version') -Regex 'git version ([^\s]+)'

$osDescription = try {
    [System.Runtime.InteropServices.RuntimeInformation]::OSDescription
}
catch { [System.Environment]::OSVersion.VersionString }

$today = Get-Date -Format 'yyyy-MM-dd'

Write-Host ''
Write-Host '=== CampusOverflow development environment ===' -ForegroundColor Cyan
Write-Host ("Name   : {0}" -f $Name)
Write-Host ("GitHub : {0}" -f $GitHub)
Write-Host ("OS     : {0}" -f $osDescription)
Write-Host ("JDK    : java {0} / javac {1}" -f $javaVersion, $javaCompiler)
Write-Host ("Maven  : {0}" -f $mavenVersion)
Write-Host ("Node   : {0} / npm {1}" -f $nodeVersion, $npmVersion)
Write-Host ("Docker : {0}" -f $dockerVersion)
Write-Host ("Git    : {0}" -f $gitVersion)
Write-Host ''
Write-Host '=== Paste the line below into env-matrix.md section 2 ===' -ForegroundColor Yellow

$markdownRow = '| {0} | `{1}` | {2} | {3} | {4} | {5} | {6} | {7} | [ ] |' -f `
    $Name, $GitHub, $osDescription, $javaVersion, $mavenVersion, $nodeVersion, $dockerVersion, $today

Write-Output $markdownRow
Write-Host ''

Write-Host '=== Checklist ===' -ForegroundColor Cyan

function Test-AtLeast {
    param([string]$Value, [double]$Minimum)
    $parsed = 0.0
    $digits = [regex]::Match($Value, '\d+(\.\d+)?').Value
    if ([double]::TryParse($digits, [ref]$parsed)) {
        return $parsed -ge $Minimum
    }
    return $false
}

$checks = @(
    @{ Name = 'JDK 21';      Pass = (Test-AtLeast $javaVersion  21);   Actual = $javaVersion },
    @{ Name = 'Maven 3.9+';  Pass = (Test-AtLeast $mavenVersion 3.9);  Actual = $mavenVersion },
    @{ Name = 'Node.js 20+'; Pass = (Test-AtLeast $nodeVersion  20);   Actual = $nodeVersion },
    @{ Name = 'Git 2.40+';   Pass = (Test-AtLeast $gitVersion   2.40); Actual = $gitVersion }
)

foreach ($check in $checks) {
    $flag = if ($check.Pass) { 'PASS' } else { 'FAIL' }
    $color = if ($check.Pass) { 'Green' } else { 'Red' }
    Write-Host ("[{0}] {1,-12} {2}" -f $flag, $check.Name, $check.Actual) -ForegroundColor $color
}

if ($dockerVersion -eq 'not installed') {
    Write-Host '[SKIP] Docker       not installed or not in PATH' -ForegroundColor DarkYellow
}
else {
    Write-Host ("[INFO] Docker       {0}  (run 'docker info' to confirm the daemon is reachable)" -f $dockerVersion) -ForegroundColor Gray
}

Write-Host ''
Write-Host 'Next: run "cd backend; mvn clean test" in the same window and screenshot it.' -ForegroundColor DarkGray
Write-Host 'One screenshot then covers both the versions and the test result (E3).' -ForegroundColor DarkGray
