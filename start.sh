#!/bin/bash
# ===============================
# Task Manager — Script de démarrage
# Usage : ./start.sh
# ===============================

# Lire le fichier .env
if [ ! -f ".env" ]; then
    echo "❌ Fichier .env introuvable. Copie .env.example en .env et renseigne les valeurs."
    exit 1
fi

# Parser ONLYOFFICE_INSTALL depuis .env
ONLYOFFICE_INSTALL=$(grep -E "^ONLYOFFICE_INSTALL=" .env | cut -d'=' -f2 | tr -d '[:space:]')
ONLYOFFICE_URL=$(grep -E "^ONLYOFFICE_URL=" .env | cut -d'=' -f2 | tr -d '[:space:]')

if [ "$ONLYOFFICE_INSTALL" = "true" ]; then
    echo "🚀 Démarrage avec OnlyOffice..."
    docker-compose --profile onlyoffice up --build
else
    echo "🚀 Démarrage sans OnlyOffice (URL externe : $ONLYOFFICE_URL)..."
    docker-compose up --build
fi