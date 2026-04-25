# syntax=docker/dockerfile:1
# Notification Service — Spring Boot 3, Java 17
#
# Official Docker filename is "Dockerfile" (capital D only). Some tools mis-spell it
# "DockerFile" and fail on Linux (case-sensitive). Use -f Dockerfile or set your
# platform’s dockerfile path to exactly: Dockerfile
#
# Build (repo root; parent POM + common-service via -am). Use root .dockerignore
# and DOCKER_BUILDKIT=1 (Maven cache mount below):
#   DOCKER_BUILDKIT=1 docker build -f notification-service/Dockerfile -t notification-service:latest .
#
# From notification-service/:
#   DOCKER_BUILDKIT=1 docker build -f Dockerfile -t notification-service:latest ..
#
# application.yml: server.port: ${PORT} — default 8082 here; Render et al. override PORT.
#
# Run (see application.yml for required env):
#   docker run --rm -p 8082:8082 --env-file .env notification-service:latest

FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY . .
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn -B -DskipTests clean package -pl notification-service -am

FROM eclipse-temurin:17-jre-jammy

RUN useradd -m -u 1000 app
WORKDIR /app

COPY --from=builder /build/notification-service/target/*.jar app.jar

USER app
EXPOSE 8082

ENV TZ=UTC
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"
ENV PORT=8082

LABEL org.opencontainers.image.title="notification-service" \
    org.opencontainers.image.description="Notification microservice (Spring Boot 3, Java 17)"

ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
