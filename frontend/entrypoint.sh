#!/bin/sh
BACKEND_URL=${BACKEND_URL:-http://backend:9000}
sed -i "s|__BACKEND_URL__|${BACKEND_URL}|g" /etc/nginx/conf.d/default.conf
echo "=== nginx.conf après substitution ==="
cat /etc/nginx/conf.d/default.conf
echo "======================================"
nginx -g "daemon off;"