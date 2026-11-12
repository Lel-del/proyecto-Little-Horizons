# Little Horizons ERP - Windows build script
# Compiles all Java sources into the bin/ directory using the bundled MySQL JDBC driver.

$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot

$jdbcJar = Join-Path $projectRoot "lib\mysql-connector-j-8.4.0.jar"
if (-not (Test-Path $jdbcJar)) {
    throw "No se encontró el driver JDBC en $jdbcJar. Descárguelo desde Maven Central."
}

if (Test-Path "bin") {
    Remove-Item -Recurse -Force "bin"
}
New-Item -ItemType Directory -Force -Path "bin" | Out-Null

$sources = Get-ChildItem -Path "src" -Recurse -Filter "*.java" | ForEach-Object { $_.FullName }
if ($sources.Count -eq 0) {
    throw "No se encontraron archivos .java en src/"
}

& javac -encoding UTF-8 -d bin -cp $jdbcJar $sources
if ($LASTEXITCODE -ne 0) {
    throw "La compilación falló con código $LASTEXITCODE"
}

Write-Host "Compilación exitosa. Clases generadas en bin/." -ForegroundColor Green
