param(
    [string]$Out = (Join-Path $PSScriptRoot "..\..\docs\scrum\sprint-2-burndown.png")
)

Add-Type -AssemblyName System.Drawing

$width = 1000
$height = 600
$left = 90
$top = 70
$right = $width - 60
$bottom = $height - 90
$max = 45

$dates = @("11-09", "11-10", "11-12", "11-14", "11-15")
$ideal = @(42, 38, 34, 30, 26)
$actual = @(42, 37, 32, 29, 24)

$bmp = New-Object System.Drawing.Bitmap($width, $height)
$g = [System.Drawing.Graphics]::FromImage($bmp)
$g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
$g.Clear([System.Drawing.Color]::White)

$titleFont = New-Object System.Drawing.Font("Segoe UI", 18, [System.Drawing.FontStyle]::Bold)
$labelFont = New-Object System.Drawing.Font("Segoe UI", 10)
$smallFont = New-Object System.Drawing.Font("Segoe UI", 9)
$axisPen = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(80, 80, 80), 1.5)
$gridPen = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(225, 225, 225), 1)
$idealPen = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(70, 120, 200), 2)
$idealPen.DashStyle = [System.Drawing.Drawing2D.DashStyle]::Dash
$actualPen = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(230, 140, 50), 3)
$pointBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(230, 140, 50))
$textBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(40, 40, 40))
$mutedBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(110, 110, 110))

$g.DrawString("Sprint 2 Burndown (through 2026-11-15)", $titleFont, $textBrush, 90, 22)
$g.DrawString("Story points remaining", $labelFont, $mutedBrush, 20, 80)

for ($v = 0; $v -le 40; $v += 10) {
    $y = $bottom - ($v / $max) * ($bottom - $top)
    $g.DrawLine($gridPen, $left, $y, $right, $y)
    $g.DrawString([string]$v, $smallFont, $mutedBrush, $left - 32, $y - 8)
}

$g.DrawLine($axisPen, $left, $top, $left, $bottom)
$g.DrawLine($axisPen, $left, $bottom, $right, $bottom)

function X([int]$i) {
    return $left + $i * ($right - $left) / ($dates.Count - 1)
}
function Y([int]$v) {
    return $bottom - ($v / $max) * ($bottom - $top)
}

for ($i = 0; $i -lt $dates.Count; $i++) {
    $g.DrawString($dates[$i], $smallFont, $mutedBrush, (X $i) - 14, $bottom + 12)
}

for ($i = 1; $i -lt $dates.Count; $i++) {
    $g.DrawLine($idealPen, (X ($i - 1)), (Y $ideal[$i - 1]), (X $i), (Y $ideal[$i]))
    $g.DrawLine($actualPen, (X ($i - 1)), (Y $actual[$i - 1]), (X $i), (Y $actual[$i]))
}
for ($i = 0; $i -lt $dates.Count; $i++) {
    $g.FillEllipse($pointBrush, (X $i) - 4, (Y $actual[$i]) - 4, 8, 8)
}

$g.DrawLine($idealPen, 700, 28, 740, 28)
$g.DrawString("Ideal", $smallFont, $mutedBrush, 748, 20)
$g.DrawLine($actualPen, 810, 28, 850, 28)
$g.DrawString("Actual", $smallFont, $mutedBrush, 858, 20)

$bmp.Save($Out, [System.Drawing.Imaging.ImageFormat]::Png)
$g.Dispose()
$bmp.Dispose()
Write-Host "Saved $Out"
