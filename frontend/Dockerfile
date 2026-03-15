# ===============================
# STAGE 1 — BUILD
# ===============================
FROM node:20-alpine AS build
WORKDIR /app

# Variables injectées depuis docker-compose / .env
ARG API_URL=http://localhost:9080
ARG ONLY_OFFICE_URL=http://localhost:8080

# Installer les dépendances
COPY package*.json ./
RUN npm install

# Copier le code source
COPY . .

# Injecter les URLs dans environment.ts avant le build
RUN sed -i "s|API_URL:.*|API_URL: '${API_URL}',|g" src/environments/environment.ts
RUN sed -i "s|ONLY_OFFICE_URL:.*|ONLY_OFFICE_URL: '${ONLY_OFFICE_URL}'|g" src/environments/environment.ts

# Build production
RUN npm run build -- --configuration=production

# ===============================
# STAGE 2 — SERVE avec Nginx
# ===============================
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# Supprimer la config Nginx par défaut
RUN rm -rf ./*

# Copier les fichiers buildés — nom du projet Angular : gestion_de_taches_frontend
COPY --from=build /app/dist/gestion_de_taches_frontend/browser/ .

# Copier la config Nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]