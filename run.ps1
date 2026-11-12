# Little Horizons ERP - Windows run script
# Compiles if needed and launches the console application with UTF-8 output.

$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot

$jdbcJar = Join-Path $projectRoot "lib\mysql-connector-j-8.4.0.jar"
$mainClass = Join-Path $projectRoot "bin\com\littlehorizons\erp\Main.class"

if (-not (Test-Path $mainClass)) {
    Write-Host "No se encontraron clases compiladas. Ejecutando build.ps1..." -ForegroundColor Yellow
    & (Join-Path $projectRoot "build.ps1")
}

# Configure UTF-8 so accents and box-drawing characters render correctly.
chcp 65001 | Out-Null
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding = [System.Text.Encoding]::UTF8

& java "-Dfile.encoding=UTF-8" "-Dstdout.encoding=UTF-8" "-Dstderr.encoding=UTF-8" -cp "bin;$jdbcJar" com.littlehorizons.erp.Main
