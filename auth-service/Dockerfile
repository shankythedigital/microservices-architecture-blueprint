# syntax=docker/dockerfile:1
# Auth Service — Spring Boot 3, Java 17
#
# Build (from repo root; needs parent POM + common-service):
#   docker build -f auth-service/Dockerfile -t auth-service:latest .
#
# From auth-service/:
#   docker build -f Dockerfile -t auth-service:latest ..
#
# Run (from auth-service/ with env file):
#   cp .env.example .env && docker compose --env-file .env up --build
# Or:
#   docker run --rm -p 7071:7071 --env-file auth-service/.env auth-service:latest

FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY . .
RUN mvn -B -DskipTests clean package -pl auth-service -am

FROM eclipse-temurin:17-jre-jammy

RUN useradd -m -u 1000 app
WORKDIR /app

COPY --from=builder /build/auth-service/target/*.jar app.jar

USER app
EXPOSE 7071

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENV PORT=7071

ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
