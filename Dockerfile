# ÉTAPE 1 : Image de base (JDK 17)
FROM eclipse-temurin:17-jre-alpine

# ÉTAPE 2 : Métadonnées (informations sur l'image)
LABEL maintainer="owileblack@gmail.com"
LABEL authors="nyerdi"
LABEL version="1.0"
LABEL description="Java Products Lab - Spring Boot Application"

# ÉTAPE 3 : Création d'un utilisateur non-root (sécurité)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# ÉTAPE 4 : Répertoire de travail dans le conteneur
WORKDIR /app

# ÉTAPE 5 : Copier le JAR buildé par Maven
COPY target/*.jar app.jar

# ÉTAPE 6 : Changer le propriétaire du fichier
RUN chown appuser:appgroup app.jar

# ÉTAPE 7 : Basculer vers l'utilisateur non-root
USER appuser

# ÉTAPE 8 : Port exposé (Spring Boot par défaut = 8080)
EXPOSE 8084

# ÉTAPE 9 : Healthcheck (vérifier que l'app est vivante)
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8084/actuator/health || exit 1

# ÉTAPE 10 : Commande de démarrage
ENTRYPOINT ["java", "-jar", "app.jar"]
