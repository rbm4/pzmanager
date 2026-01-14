# Build and Deploy Script
# This script compiles the project, commits changes, and pushes to git

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Building and Deploying Manager Project" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Clean and compile with Maven
Write-Host "[1/5] Compiling project with Maven..." -ForegroundColor Yellow
mvn clean package

if ($LASTEXITCODE -ne 0) {
    Write-Host " Maven build failed!" -ForegroundColor Red
    exit 1
}

Write-Host " Build successful!" -ForegroundColor Green
Write-Host ""

# Step 2: Check for compiled JAR and create startup script
Write-Host "[2/5] Creating Linux startup script..." -ForegroundColor Yellow

$jarFile = Get-ChildItem -Path "./target" -Filter "*.jar" -ErrorAction SilentlyContinue | Select-Object -First 1

if ($null -eq $jarFile) {
    Write-Host " No JAR file found in /target folder!" -ForegroundColor Red
    exit 1
}

$jarName = $jarFile.Name
$resourcesPath = "./src/main/resources"
$scriptPath = "$resourcesPath/script.sh"

if (-not (Test-Path $resourcesPath)) {
    New-Item -ItemType Directory -Path $resourcesPath -Force | Out-Null
}

$scriptContent = @"
#!/bin/bash
cd /home/pzuser/pzmanager
git pull
java -Dspring.profiles.active=prod -jar target/$jarName
"@

Set-Content -Path $scriptPath -Value $scriptContent -Encoding UTF8
Write-Host " Startup script created at $scriptPath" -ForegroundColor Green
Write-Host ""

# Step 3: Git add all changes
Write-Host "[3/5] Adding changes to git..." -ForegroundColor Yellow
git add .

if ($LASTEXITCODE -ne 0) {
    Write-Host " Git add failed!" -ForegroundColor Red
    exit 1
}

Write-Host " Changes staged!" -ForegroundColor Green
Write-Host ""

# Step 4: Git commit
Write-Host "[4/5] Committing changes..." -ForegroundColor Yellow

# Prompt for commit message
$commitMessage = Read-Host "Enter commit message (or press Enter for default)"

if ([string]::IsNullOrWhiteSpace($commitMessage)) {
    $commitMessage = "Update: Build and deploy $(Get-Date -Format 'yyyy-MM-dd HH:mm')"
}

git commit -m "$commitMessage"

if ($LASTEXITCODE -ne 0) {
    Write-Host " Nothing to commit or commit failed" -ForegroundColor Yellow
}
else {
    Write-Host " Changes committed!" -ForegroundColor Green
}

Write-Host ""

# Step 5: Git push
Write-Host "[5/5] Pushing to remote..." -ForegroundColor Yellow
git push

if ($LASTEXITCODE -ne 0) {
    Write-Host " Git push failed!" -ForegroundColor Red
    exit 1
}

Write-Host " Changes pushed successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Deployment completed successfully!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan

