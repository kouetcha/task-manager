# ===============================
# Task Manager — Script de démarrage
# Usage : .\start.ps1
# ===============================

if (-Not (Test-Path ".env")) {
    Write-Host "❌ Fichier .env introuvable. Copie .env.example en .env et renseigne les valeurs." -ForegroundColor Red
    exit 1
}

# Parser les variables du .env
$envVars = @{}
Get-Content ".env" | ForEach-Object {
    if ($_ -match "^\s*([^#][^=]+)=([^#]*)") {
        $envVars[$matches[1].Trim()] = $matches[2].Trim()
    }
}

$installOnlyOffice = $envVars["ONLYOFFICE_INSTALL"]

if ($installOnlyOffice -eq "true") {
    Write-Host "🚀 Démarrage avec OnlyOffice (port 8070)..." -ForegroundColor Cyan
    docker-compose --profile onlyoffice up --build
} else {
    Write-Host "🚀 Démarrage sans OnlyOffice (URL externe : $($envVars['ONLYOFFICE_URL']))..." -ForegroundColor Cyan
    docker-compose up --build
}