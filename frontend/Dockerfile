# ===============================
# STAGE 1 — BUILD
# ===============================
FROM node:20-alpine AS build
WORKDIR /app

# Variables injectées depuis docker-compose (URLs publiques pour Angular)
ARG API_URL=http://localhost:9060
ARG ONLYOFFICE_URL=http://localhost:4400/onlyoffice
ARG INTERNAL_API_URL=http://tasksmanager-backend:9000/tasksmanager

# Installer les dépendances
COPY package*.json ./
RUN npm install

# Copier le code source
COPY . .

# Injecter les URLs dans environment.ts avant le build
RUN sed -i "s|API_URL:.*|API_URL: '${API_URL}',|g" src/environments/environment.ts
RUN sed -i "s|ONLY_OFFICE_URL:.*|ONLY_OFFICE_URL: '${ONLYOFFICE_URL}',|g" src/environments/environment.ts
RUN sed -i "s|INTERNAL_API_URL:.*|INTERNAL_API_URL: '${INTERNAL_API_URL}'|g" src/environments/environment.ts

# Build production
RUN npm run build -- --configuration=production

# ===============================
# STAGE 2 — SERVE avec Nginx
# ===============================
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# Supprimer la config Nginx par défaut
RUN rm -rf ./*

# Copier les fichiers buildés
COPY --from=build /app/dist/gestion_de_taches_frontend/browser/ .

# Copier la config Nginx comme template (les placeholders seront remplacés au runtime)
COPY nginx.conf /etc/nginx/conf.d/default.conf.template

# Copier l'entrypoint
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

EXPOSE 80

CMD ["/entrypoint.sh"]