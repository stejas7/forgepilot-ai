FROM node:22-alpine AS frontend-build
WORKDIR /workspace/frontend
COPY frontend/package.json ./
RUN npm install --no-audit --no-fund
COPY frontend/ ./
RUN npm run build

FROM maven:3.9.11-eclipse-temurin-21 AS backend-build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline
COPY src src
COPY --from=frontend-build /workspace/frontend/dist /workspace/src/main/resources/static
RUN mvn -B -q -DskipTests clean package

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 10001 forgepilot
COPY --from=backend-build /workspace/target/forgepilot-ai-0.1.0-SNAPSHOT.jar app.jar
USER forgepilot
EXPOSE 8080
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75.0","-jar","/app/app.jar"]
