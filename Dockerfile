# ---------- FRONTEND BUILD ----------
FROM node:20-alpine AS frontend-build
WORKDIR /app/frontend

COPY frontend/package*.json ./
RUN npm install

COPY frontend .
RUN npm run build


# ---------- BACKEND BUILD ----------
FROM maven:3.9.6-eclipse-temurin-17 AS backend-build
WORKDIR /app

COPY backend/pom.xml .
RUN mvn dependency:go-offline

COPY backend/src ./src

# Copia o build do frontend para o static do Spring
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static

RUN mvn package -DskipTests


# ---------- RUNTIME ----------
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=backend-build /app/target/*.jar app.jar

RUN mkdir -p tokens
VOLUME /app/tokens

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]