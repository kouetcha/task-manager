# Task Manager

> Application web de gestion de projets collaboratifs avec contrôle d'accès granulaire par email.

---

## Aperçu

Task Manager permet à une équipe de gérer des **projets**, **activités** et **tâches** avec un système de permissions explicites : chaque membre ne voit que ce à quoi il a été explicitement associé — sans visibilité héritée du parent.

---

## Fonctionnalités

- 🔐 Authentification JWT (access + refresh token)
- 📁 Hiérarchie Projet → Activité → Tâche
- 👥 Invitations par email à chaque niveau de la hiérarchie
- 💬 Commentaires avec pièces jointes sur chaque entité
- 📎 Upload et visualisation de fichiers (OnlyOffice)
- 📅 Vue calendrier et vue détail
- 🔔 Notifications temps réel via WebSocket (à venir)
- ⚡ Cache Redis pour les performances
- 🔒 Permissions strictes — l'accès est toujours explicite

---

## Stack technique

| Couche          | Technologie                                      |
|-----------------|--------------------------------------------------|
| Backend         | Spring Boot 3, Spring Security, JPA / Hibernate  |
| Frontend        | Angular 17+, TypeScript                          |
| Base de données | MySQL 8                                          |
| Cache           | Redis                                            |
| Auth            | JWT (Access + Refresh token)                     |
| Visualisation   | OnlyOffice Document Server                       |
| Stockage        | Système de fichiers local (configurable)         |

---

## Architecture

```
task-manager/
├── backend/          # API REST Spring Boot
│   ├── Dockerfile
│   └── src/
├── frontend/         # SPA Angular
│   ├── Dockerfile
│   ├── nginx.conf
│   └── src/
│       └── environments/
│           ├── environment.ts          # développement
│           └── environment.prod.ts     # production
├── docker-compose.yml
├── .env.example
├── start.ps1         # Script de démarrage Windows
├── start.sh          # Script de démarrage Linux / Mac
└── README.md
```

---

## Modèle de permissions

| Rôle              | Peut voir                               | Peut faire                        |
|-------------------|-----------------------------------------|-----------------------------------|
| Créateur          | Son entité complète                     | Modifier, supprimer               |
| Membre projet     | Le projet + activités où il est associé | Créer des activités               |
| Membre activité   | L'activité + tâches où il est associé   | Créer des tâches                  |
| Membre tâche      | La tâche uniquement                     | Commenter                         |

> ⚠️ **L'accès est toujours explicite — aucune visibilité n'est héritée du parent.**
> Un membre associé à une tâche ne voit ni l'activité ni le projet parent.

---

## Installation

### Prérequis

- Docker et Docker Compose
- Un compte OnlyOffice gratuit — voir section ci-dessous

---

### 1. Cloner le projet

```bash
git clone https://github.com/kouetcha/task-manager.git
cd task-manager
```

### 2. Configurer les variables d'environnement

```bash
# Linux / Mac
cp .env.example .env

# Windows
copy .env.example .env
```

Éditer le fichier `.env` et renseigner toutes les valeurs.

### 3. Configurer OnlyOffice

La visualisation de documents nécessite une instance OnlyOffice Document Server.

**Option A — Tu as déjà une instance OnlyOffice :**
```env
ONLYOFFICE_INSTALL=false
ONLYOFFICE_URL=http://localhost:8070
```

**Option B — Tu n'as pas OnlyOffice (Docker l'installe) :**
```env
ONLYOFFICE_INSTALL=true
ONLYOFFICE_URL=http://localhost:8070
```
> ⚠️ L'image OnlyOffice fait ~2 Go. Le premier lancement sera long.

> ℹ️ L'URL OnlyOffice est appelée directement par le **navigateur** — utilise `localhost` et non l'IP du réseau Docker.

### 4. Configurer le frontend

Les URLs sont injectées au moment du build Docker depuis le `.env` :

```env
API_URL=http://localhost:9060
ONLYOFFICE_URL=http://localhost:8070
```

Elles correspondent aux variables dans `frontend/src/environments/environment.ts` :

```typescript
export const environment = {
  production: false,
  API_URL: 'http://localhost:9060',
  ONLY_OFFICE_URL: 'http://localhost:8070'
};
```

### 5. Lancer le projet

**Windows :**
```powershell
.\start.ps1
```

**Linux / Mac :**
```bash
chmod +x start.sh
./start.sh
```

Le script lit automatiquement `ONLYOFFICE_INSTALL` dans le `.env` et lance la bonne commande Docker.

| Service    | URL                                    |
|------------|----------------------------------------|
| Frontend   | http://localhost:4400                  |
| Backend    | http://localhost:9060/tasksmanager     |
| Swagger    | http://localhost:9060/tasksmanager/swagger-ui/index.html |
| OnlyOffice | http://localhost:8070 (si installé)    |
| MySQL      | localhost:3307                         |
| Redis      | localhost:6380                         |

---

## Configuration complète — référence backend

<details>
<summary>Voir toutes les propriétés disponibles</summary>

```properties
# ===============================
# SERVER
# ===============================
server.port=9000
server.address=0.0.0.0
server.servlet.context-path=/tasksmanager
server.max-http-header-size=65536

# ===============================
# APPLICATION
# ===============================
spring.application.name=TasksManager

# ===============================
# DATABASE (MYSQL)
# ===============================
spring.datasource.url=jdbc:mysql://localhost:3306/tasksmanager_global?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# ===============================
# JPA / HIBERNATE
# ===============================
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# ===============================
# JWT SECURITY
# ===============================
kouetcha.app.jwtSecret=yourSuperSecretKey
kouetcha.app.jwtExpirationMs=86400000

# ===============================
# REDIS
# ===============================
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=

# ===============================
# CACHE
# ===============================
spring.cache.type=redis
spring.cache.redis.time-to-live=600000
spring.cache.redis.key-prefix=tasksmanager::
spring.cache.redis.enable-statistics=true

# ===============================
# CORS / CLIENT
# ===============================
client.url=http://localhost:4400
allowed_origin=http://localhost:4400

# ===============================
# FILE UPLOAD
# ===============================
spring.servlet.multipart.max-file-size=200MB
spring.servlet.multipart.max-request-size=200MB

# ===============================
# WEBSOCKET
# ===============================
spring.websocket.max-text-message-size=128MB
spring.websocket.max-binary-message-size=128MB

# ===============================
# ADMIN DEFAULT ACCOUNT
# ===============================
admin.email=admin@example.com
admin.password=adminPassword

# ===============================
# MEDIA STORAGE
# ===============================
media.document.tache=./media/document/tache/
media.document.activite=./media/document/activite/
media.document.projet=./media/document/projet/

media.commentaire.tache=./media/commentaire/tache/
media.commentaire.activite=./media/commentaire/activite/
media.commentaire.projet=./media/commentaire/projet/

media.user-profil=./media/userProfil/

# ===============================
# SPRING MVC
# ===============================
spring.mvc.pathmatch.matching-strategy=ant-path-matcher
```

</details>

---

## Auteur

**Kouetcha** — [GitHub](https://github.com/kouetcha)