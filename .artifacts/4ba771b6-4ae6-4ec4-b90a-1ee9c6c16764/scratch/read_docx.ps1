Add-Type -AssemblyName System.IO.Compression.FileSystem
$path = "$env:USERPROFILE\Downloads\OOP_Project_Documentation_Template.docx"
if (Test-Path $path) {
    $zip = [System.IO.Compression.ZipFile]::OpenRead($path)
    $entry = $zip.GetEntry("word/document.xml")
    if ($entry) {
        $stream = $entry.Open()
        $reader = New-Object System.IO.StreamReader($stream)
        $xml = $reader.ReadToEnd()
        $reader.Close()
        $stream.Close()
        $zip.Dispose()
        $text = [System.Text.RegularExpressions.Regex]::Replace($xml, "<[^>]+>", " ")
        Write-Output $text
    } else {
        Write-Host "Document.xml not found in docx"
    }
} else {
    Write-Host "Docx template not found at $path"
}
