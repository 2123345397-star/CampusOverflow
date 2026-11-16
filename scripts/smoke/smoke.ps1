<#
.SYNOPSIS
    CampusOverflow 端到端冒烟脚本（T1-15）：一条命令跑通
    CSRF → 注册 → 登录 → 发问题 → 另一账号回答 → 投票 → 采纳 → 详情校验。

.DESCRIPTION
    依赖一个已启动的后端（dev 或 demo profile，需 MySQL + Redis）。
    每次运行会注册两个带时间戳后缀的临时账号，并留下一条已采纳的问题数据。

.PARAMETER BaseUrl
    后端地址，默认 http://localhost:8080

.PARAMETER Password
    临时账号密码，默认 Passw0rd123

.EXAMPLE
    pwsh -File scripts/smoke/smoke.ps1
    pwsh -File scripts/smoke/smoke.ps1 -BaseUrl http://localhost:18080
#>
[CmdletBinding()]
param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$Password = "Passw0rd123",
    [int]$TimeoutSec = 20
)

$ErrorActionPreference = "Stop"
$BaseUrl = $BaseUrl.TrimEnd('/')
$suffix = (Get-Date -Format "MMddHHmmss") + (Get-Random -Minimum 100 -Maximum 999)
$userA = "smokeA$suffix"
$userB = "smokeB$suffix"
$nameA = "冒烟甲$suffix"
$nameB = "冒烟乙$suffix"
$script:session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$script:started = Get-Date

function Write-Step([int]$n, [string]$text) {
    Write-Host ("[{0}/9] {1}" -f $n, $text) -ForegroundColor Cyan
}

function Get-Csrf {
    $resp = Invoke-RestMethod -Uri "$BaseUrl/api/v1/auth/csrf" -WebSession $script:session `
        -TimeoutSec $TimeoutSec
    $cookie = $script:session.Cookies.GetCookies([Uri]$BaseUrl) |
        Where-Object { $_.Name -eq "XSRF-TOKEN" } |
        Select-Object -First 1
    if (-not $cookie) {
        throw "CSRF 失败：响应里没有 XSRF-TOKEN Cookie"
    }
    return @{ Header = $resp.headerName; Token = $cookie.Value }
}

function Invoke-Json {
    param(
        [string]$Method,
        [string]$Path,
        $Body,
        $Csrf
    )
    $params = @{
        Uri        = "$BaseUrl$Path"
        Method     = $Method
        WebSession = $script:session
        TimeoutSec = $TimeoutSec
        Headers    = @{ $Csrf.Header = $Csrf.Token }
    }
    if ($null -ne $Body) {
        $json = $Body | ConvertTo-Json -Depth 6
        $params.ContentType = "application/json; charset=utf-8"
        $params.Body = [System.Text.Encoding]::UTF8.GetBytes($json)
    }
    try {
        return Invoke-RestMethod @params
    }
    catch {
        $detail = $_.ErrorDetails.Message
        throw ("{0} {1} 失败：{2} {3}" -f $Method, $Path, $_.Exception.Message, $detail)
    }
}

try {
    Write-Step 1 "获取 CSRF"
    $csrf = Get-Csrf

    Write-Step 2 "注册提问者 $userA"
    $regA = Invoke-Json POST "/api/v1/auth/register" @{
        username = $userA; displayName = $nameA; email = "$userA@example.com"
        password = $Password; college = "计算机学院"; className = "软工2401"
    } $csrf

    Write-Step 3 "登录提问者"
    $csrf = Get-Csrf
    $meA = Invoke-Json POST "/api/v1/auth/login" @{ username = $userA; password = $Password } $csrf

    Write-Step 4 "发布问题"
    $csrf = Get-Csrf
    $question = Invoke-Json POST "/api/v1/questions" @{
        title = "冒烟测试：红黑树插入后为什么必须旋转？"
        body  = "这是冒烟脚本创建的问题正文，长度需要超过二十个字符才能通过校验。"
        tags  = @("冒烟测试", "数据结构")
    } $csrf
    $questionId = [long]$question.id

    Write-Step 5 "注册回答者 $userB"
    $csrf = Get-Csrf
    $regB = Invoke-Json POST "/api/v1/auth/register" @{
        username = $userB; displayName = $nameB; email = "$userB@example.com"
        password = $Password; college = "计算机学院"; className = "软工2401"
    } $csrf

    Write-Step 6 "切换登录回答者并提交回答"
    $csrf = Get-Csrf
    $meB = Invoke-Json POST "/api/v1/auth/login" @{ username = $userB; password = $Password } $csrf
    $csrf = Get-Csrf
    $answer = Invoke-Json POST "/api/v1/questions/$questionId/answers" @{
        body = "这是冒烟脚本提交的回答正文，长度也超过二十个字符，用来验证回答链路。"
    } $csrf
    $answerId = [long]$answer.id

    Write-Step 7 "回答者给问题投 +1"
    $csrf = Get-Csrf
    Invoke-Json PUT "/api/v1/votes" @{
        targetType = "QUESTION"; targetId = $questionId; value = 1
    } $csrf | Out-Null

    Write-Step 8 "切回提问者并采纳回答"
    $csrf = Get-Csrf
    $meA2 = Invoke-Json POST "/api/v1/auth/login" @{ username = $userA; password = $Password } $csrf
    $csrf = Get-Csrf
    Invoke-Json PUT "/api/v1/questions/$questionId/accepted-answer" @{ answerId = $answerId } $csrf | Out-Null

    Write-Step 9 "读取详情并校验"
    $detail = Invoke-RestMethod -Uri "$BaseUrl/api/v1/questions/$questionId" -WebSession $script:session `
        -TimeoutSec $TimeoutSec
    if ([long]$detail.acceptedAnswerId -ne $answerId) {
        throw "采纳未生效：acceptedAnswerId=$($detail.acceptedAnswerId)，期望 $answerId"
    }
    if (@($detail.answers).Count -lt 1) {
        throw "详情里没有回答"
    }
    if ([int]$detail.score -lt 1) {
        throw "问题分数未因投票增加：score=$($detail.score)"
    }

    $elapsed = ((Get-Date) - $script:started).TotalSeconds
    Write-Host ""
    Write-Host "SMOKE PASS" -ForegroundColor Green
    Write-Host ("  用户 A={0} (id={1})  用户 B={2} (id={3})" -f $userA, $regA.id, $userB, $regB.id)
    Write-Host ("  问题 id={0}  回答 id={1}  score={2}  acceptedAnswerId={3}" -f `
        $questionId, $answerId, $detail.score, $detail.acceptedAnswerId)
    Write-Host ("  耗时 {0:N1}s  base={1}" -f $elapsed, $BaseUrl)
    exit 0
}
catch {
    Write-Host ""
    Write-Host "SMOKE FAIL" -ForegroundColor Red
    Write-Host ("  {0}" -f $_.Exception.Message) -ForegroundColor Red
    exit 1
}
