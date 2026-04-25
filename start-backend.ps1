# InteractOut - Backend Quick Start (Windows PowerShell)
# Run from project root: .\start-backend.ps1

Write-Host "=== InteractOut Backend ===" -ForegroundColor Green

# 1. Check Java 21+
Write-Host "[1/3] Checking Java..." -ForegroundColor Cyan
$java = java -version 2>&1 | Select-String "version"
if ($java) {
    Write-Host "  $java" -ForegroundColor Green
} else {
    Write-Host "  ERROR: Java not found. Install JDK 21+ from https://adoptium.net" -ForegroundColor Red
    exit 1
}

# 2. Check Maven
Write-Host "[2/3] Checking Maven..." -ForegroundColor Cyan
$mvn = mvn -version 2>&1 | Select-String "Apache Maven"
if ($mvn) {
    Write-Host "  $mvn" -ForegroundColor Green
} else {
    Write-Host "  Maven not found - install from https://maven.apache.org" -ForegroundColor Yellow
}

# 3. Start Spring Boot
Write-Host "[3/3] Starting backend..." -ForegroundColor Cyan
Write-Host "  API:        http://10.0.2.2:8080/api" -ForegroundColor White
Write-Host "  H2 Console: http://10.0.2.2:8080/h2-console" -ForegroundColor White
Write-Host "  Ctrl+C to stop" -ForegroundColor White

Set-Location "backend\springboot"
mvn spring-boot:run
