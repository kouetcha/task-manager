# ===============================
# STAGE 1 — BUILD
# ===============================
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copier le pom.xml et télécharger les dépendances en cache
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copier le code source
COPY src ./src

# Forcer la recompilation à chaque build
#ARG CACHEBUST=1
RUN mvn package -DskipTests

# ===============================
# STAGE 2 — RUN
# ===============================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Créer les dossiers media
RUN mkdir -p ./media/document/tache \
             ./media/document/activite \
             ./media/document/projet \
             ./media/commentaire/tache \
             ./media/commentaire/activite \
             ./media/commentaire/projet \
             ./media/userProfil

# Copier le jar compilé
COPY --from=build /app/target/*.jar app.jar

EXPOSE 9080

ENTRYPOINT ["java", "-jar", "app.jar"]