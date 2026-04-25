# syntax=docker/dockerfile:1
# Notification Service — Spring Boot 3, Java 17
#
# Build (from repo root; needs parent POM + common-service):
#   docker build -f notification-service/Dockerfile -t notification-service:latest .
#
# From notification-service/:
#   docker build -f Dockerfile -t notification-service:latest ..
#
# application.yml uses server.port: ${PORT} — this image sets PORT=8082 by default (override at run time, e.g. Render).
# Run (set SPRING_DATASOURCE_URL, NOTIFICATION_SERVICE_URL, ASSET_SERVICE_URL, AUTH_SERVICE_URL, etc.):
#   docker run --rm -p 8082:8082 --env-file .env notification-service:latest

FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY . .
RUN mvn -B -DskipTests clean package -pl notification-service -am

FROM eclipse-temurin:17-jre-jammy

RUN useradd -m -u 1000 app
WORKDIR /app

COPY --from=builder /build/notification-service/target/*.jar app.jar

USER app
EXPOSE 8082

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENV PORT=8082

ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
