FROM node:22-alpine AS frontend-build
WORKDIR /workspace/frontend
COPY frontend/package.json ./
RUN npm install
COPY frontend/ ./
RUN npm run build

FROM maven:3.9.11-eclipse-temurin-21 AS backend-build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline
COPY src src
COPY --from=frontend-build /workspace/frontend/dist /workspace/src/main/resources/static
RUN mvn -B -q verify package

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 10001 forgepilot
COPY --from=backend-build /workspace/target/forgepilot-ai-0.1.0-SNAPSHOT.jar app.jar
USER forgepilot
EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=5s --start-period=20s --retries=5 CMD wget -qO- http://127.0.0.1:8080/actuator/health || exit 1
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75.0","-jar","/app/app.jar"]
