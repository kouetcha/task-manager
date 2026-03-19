#!/bin/sh
set -e

BACKEND_URL=${BACKEND_URL:-http://backend:9000}
ONLYOFFICE_URL=${ONLYOFFICE_URL:-http://onlyoffice:80}
ONLYOFFICE_INSTALL=${ONLYOFFICE_INSTALL:-false}
FRONTEND_PORT=${FRONTEND_PORT:-4400}

cp /etc/nginx/conf.d/default.conf.template /etc/nginx/conf.d/default.conf

# Remplacer les placeholders
sed -i "s|__BACKEND_URL__|${BACKEND_URL}|g"       /etc/nginx/conf.d/default.conf
sed -i "s|__ONLYOFFICE_URL__|${ONLYOFFICE_URL}|g" /etc/nginx/conf.d/default.conf
sed -i "s|__FRONTEND_PORT__|${FRONTEND_PORT}|g"   /etc/nginx/conf.d/default.conf

if [ "$ONLYOFFICE_INSTALL" = "true" ]; then
    # Supprimer uniquement les marqueurs de bloc
    sed -i '/# ONLYOFFICE_BLOCK_START/d' /etc/nginx/conf.d/default.conf
    sed -i '/# ONLYOFFICE_BLOCK_END/d'   /etc/nginx/conf.d/default.conf
    echo "✅ OnlyOffice activé — proxy vers ${ONLYOFFICE_URL}"
else
    # Supprimer tout le bloc OnlyOffice entre les marqueurs
    sed -i '/# ONLYOFFICE_BLOCK_START/,/# ONLYOFFICE_BLOCK_END/d' /etc/nginx/conf.d/default.conf
    echo "⚠️  OnlyOffice désactivé — blocs proxy supprimés"
fi

echo "✅ BACKEND_URL        = ${BACKEND_URL}"
echo "✅ FRONTEND_PORT      = ${FRONTEND_PORT}"
echo "✅ ONLYOFFICE_INSTALL = ${ONLYOFFICE_INSTALL}"

exec nginx -g "daemon off;"