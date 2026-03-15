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
- 🔔 Notifications temps réel via WebSocket
- ⚡ Cache Redis pour les performances
- 🔒 Permissions strictes — l'accès est toujours explicite

---

## Stack technique

| Couche          | Technologie                                      |
|-----------------|--------------------------------------------------|
| Backend         | Spring Boot 3, Spring Security, JPA / Hibernate  |
| Frontend        | Angular, TypeScript                              |
| Base de données | MySQL 8                                          |
| Cache           | Redis                                            |
| Temps réel      | WebSocket (STOMP)                                |
| Auth            | JWT (Access + Refresh token)                     |
| Visualisation   | OnlyOffice Document Server                       |
| Stockage        | Système de fichiers local (configurable)         |

---

## Architecture

```
task-manager/
├── backend/          # API REST Spring Boot
├── frontend/         # SPA Angular
├── docker-compose.yml
├── .env.example
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
cp .env.example .env
```

Éditer le fichier `.env` :

```env
MYSQL_PASSWORD=yourpassword
JWT_SECRET=yourSuperSecretKeyMinimum32Characters
ADMIN_EMAIL=admin@example.com
ADMIN_PASSWORD=adminPassword
```

### 3. Configurer OnlyOffice

La visualisation de documents nécessite une instance OnlyOffice Document Server.

1. Créer un compte gratuit sur [personal.onlyoffice.com](https://personal.onlyoffice.com)
2. Récupérer l'URL de ton instance (ex: `https://ton-compte.onlyoffice.com`)
3. La renseigner dans `frontend/src/environments/environment.ts` :

```typescript
export const environment = {
  onlyofficeUrl: 'https://ton-compte.onlyoffice.com'
};
```

### 4. Lancer le projet

```bash
docker-compose up --build
```

L'application est accessible sur `http://localhost:4200`.

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
client.url=http://localhost:4200
allowed_origin=http://localhost:4200

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

media.userProfil=./media/userProfil/

# ===============================
# SPRING MVC
# ===============================
spring.mvc.pathmatch.matching-strategy=ant-path-matcher
```

</details>

---

## Auteur

**Kouetcha** — [GitHub](https://github.com/kouetcha)