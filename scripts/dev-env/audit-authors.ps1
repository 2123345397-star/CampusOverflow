<#
.SYNOPSIS
    CampusOverflow - commit author audit (Lab 1 / role card E4)

.DESCRIPTION
    Summarises every "Name <Email>" pair found in the commit history and flags
    emails that probably are not bound to a GitHub account (i.e. not a noreply
    address and not in the whitelist below).

    NOTE: keep this file ASCII-only. Windows PowerShell 5.1 reads .ps1 files as
    ANSI (GBK) when they have no BOM, so non-ASCII text would break the parser.

.PARAMETER GitHub
    Your GitHub username without @, used to suggest a noreply address.

.EXAMPLE
    powershell -NoProfile -ExecutionPolicy Bypass -File scripts/dev-env/audit-authors.ps1 -GitHub 2177023844
#>
[CmdletBinding()]
param(
    [string]$GitHub = ''
)

$ErrorActionPreference = 'Continue'

# Emails confirmed as bound to a GitHub account. Fill in after verification.
$KnownEmails = @(
    # 'cmz666-BOT@users.noreply.github.com',
    # 'Liujunfeng520@users.noreply.github.com'
)

$gitCommand = 'git'
foreach ($candidate in @(
        'D:\Program Files\Git\cmd\git.exe',
        'C:\Program Files\Git\cmd\git.exe',
        "$env:LOCALAPPDATA\Programs\Git\cmd\git.exe")) {
    if (Test-Path -LiteralPath $candidate) { $gitCommand = $candidate; break }
}

$repoRoot = ''
try {
    $repoRoot = ((& $gitCommand rev-parse --show-toplevel 2>&1) | Out-String).Trim()
}
catch { $repoRoot = '' }

if ([string]::IsNullOrWhiteSpace($repoRoot) -or $repoRoot -match 'fatal|not a git repository') {
    Write-Host '[ERROR] Not a git repository, or git is unavailable. Run this inside the repo root.' -ForegroundColor Red
    exit 1
}

Write-Host ("Repo: {0}" -f $repoRoot) -ForegroundColor Cyan
Write-Host ''

$raw = & $gitCommand -C $repoRoot log --all --format='%ae|%an' 2>&1

if (-not $raw) {
    Write-Host '[WARN] No commits found.' -ForegroundColor Yellow
    exit 0
}

$authors = $raw |
    Where-Object { $_ -match '\|' } |
    ForEach-Object {
        $parts = $_ -split '\|', 2
        [pscustomobject]@{ Email = $parts[0].Trim(); Name = $parts[1].Trim() }
    } |
    Group-Object Email |
    ForEach-Object {
        [pscustomobject]@{
            Email = $_.Name
            Names = (($_.Group | Select-Object -ExpandProperty Name -Unique) -join ' / ')
            Count = $_.Count
        }
    } |
    Sort-Object Count -Descending

Write-Host '| Commits | Author | Email | noreply | Verdict |'
Write-Host '| --- | --- | --- | --- | --- |'

foreach ($author in $authors) {
    $isNoreply = $author.Email -match '@users\.noreply\.github\.com$'
    $isKnown = $KnownEmails -contains $author.Email
    $isWebCommit = $author.Email -eq 'noreply@github.com'

    if ($isWebCommit) { $verdict = 'web commit (normal)' }
    elseif ($isNoreply -or $isKnown) { $verdict = 'OK' }
    else { $verdict = 'needs verification' }

    $noreplyFlag = if ($isNoreply) { 'yes' } else { 'no' }

    Write-Host ("| {0} | {1} | {2} | {3} | {4} |" -f $author.Count, $author.Names, $author.Email, $noreplyFlag, $verdict)
}

Write-Host ''
Write-Host '=== Local identity ===' -ForegroundColor Cyan
Write-Host ("user.name  : {0}" -f ((& $gitCommand config --global user.name) -join ''))
Write-Host ("user.email : {0}" -f ((& $gitCommand config --global user.email) -join ''))
Write-Host ("last commit author : {0}" -f ((& $gitCommand -C $repoRoot log -1 --format='%an <%ae>') -join ''))

if ($GitHub) {
    Write-Host ''
    Write-Host '=== Suggested settings ===' -ForegroundColor Yellow
    Write-Host ("git config --global user.name  `"{0}`"" -f ((& $gitCommand config --global user.name) -join ''))
    Write-Host ("git config --global user.email `"{0}+{0}@users.noreply.github.com`"" -f $GitHub)
    Write-Host '(confirm this noreply address under GitHub -> Settings -> Emails)' -ForegroundColor DarkGray
}

Write-Host ''
Write-Host 'Rules and remediation: docs/lab1-submission/identity-audit.md' -ForegroundColor DarkGray
