Add-Type -AssemblyName System.Drawing
$sourcePath = "$env:USERPROFILE\Downloads\Track-It Nasugbu Map Icon.png"
$img = [System.Drawing.Image]::FromFile($sourcePath)

$size = 512
$padding = 90 # Generous safe-zone padding to prevent any launcher cropping
$newSize = $size - (2 * $padding)

$bmp = New-Object System.Drawing.Bitmap $size, $size
$graphics = [System.Drawing.Graphics]::FromImage($bmp)
$graphics.Clear([System.Drawing.Color]::White)
$graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
$graphics.DrawImage($img, $padding, $padding, $newSize, $newSize)

$outputPath = "app\src\main\res\drawable\app_custom_logo.png"
$bmp.Save($outputPath, [System.Drawing.Imaging.ImageFormat]::Png)

$graphics.Dispose()
$bmp.Dispose()
$img.Dispose()
Write-Host "Padded uncropped logo generated successfully!"
