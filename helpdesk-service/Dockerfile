# syntax=docker/dockerfile:1
# Helpdesk Service — Spring Boot 3, Java 17
#
# Use filename "Dockerfile" (capital D only) in platforms; not "DockerFile".
#
# Build (repo root; parent POM + common-service via -am). Root .dockerignore + BuildKit:
#   DOCKER_BUILDKIT=1 docker build -f helpdesk-service/Dockerfile -t helpdesk-service:latest .
#
# From helpdesk-service/:
#   DOCKER_BUILDKIT=1 docker build -f Dockerfile -t helpdesk-service:latest ..
#
# Default port 7074 matches application.yml (server.port). Set PORT at run time if you
# switch config to ${PORT:7074} for cloud hosts.
#
# Run (override DB and service URLs via env or profile; do not bake secrets into the image):
#   docker run --rm -p 7074:7074 helpdesk-service:latest

FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY . .
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn -B -DskipTests clean package -pl helpdesk-service -am

FROM eclipse-temurin:17-jre-jammy

RUN useradd -m -u 1000 app
WORKDIR /app

COPY --from=builder /build/helpdesk-service/target/*.jar app.jar

USER app
EXPOSE 7073

ENV TZ=UTC
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"
ENV PORT=7073

LABEL org.opencontainers.image.title="helpdesk-service" \
    org.opencontainers.image.description="Helpdesk microservice (Spring Boot 3, Java 17)"

ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
