# syntax=docker/dockerfile:1
# Notification Service — Spring Boot 3, Java 17
#
# Build (repo root; parent POM + common-service are included via -am). Use a root
# .dockerignore so COPY . . stays small. Prefer BuildKit for layer cache and for
# the Maven cache mount below:
#   DOCKER_BUILDKIT=1 docker build -f notification-service/Dockerfile -t notification-service:latest .
#
# From notification-service/:
#   DOCKER_BUILDKIT=1 docker build -f Dockerfile -t notification-service:latest ..
#
# application.yml: server.port: ${PORT} — default PORT=8082 here; platforms like Render
# override PORT.
#
# Run (required env: SPRING_DATASOURCE_*, NOTIFICATION_SERVICE_URL, ASSET_SERVICE_URL,
# AUTH_SERVICE_URL, SPRING_APPLICATION_NAME, etc. — see application.yml):
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
