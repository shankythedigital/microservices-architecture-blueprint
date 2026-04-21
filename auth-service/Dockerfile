# syntax=docker/dockerfile:1
# Auth Service — Spring Boot 3, Java 17
#
# Build (from repo root; needs parent POM + common-service):
#   docker build -f auth-service/Dockerfile -t auth-service:latest .
#
# From auth-service/:
#   docker build -f Dockerfile -t auth-service:latest ..
#
# Run:
#   docker run --rm -p 8081:8081 -e SPRING_DATASOURCE_URL -e JWT_SECRET auth-service:latest

FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY . .
RUN mvn -B -DskipTests clean package -pl auth-service -am

FROM eclipse-temurin:17-jre-jammy

RUN useradd -m -u 1000 app
WORKDIR /app

COPY --from=builder /build/auth-service/target/*.jar app.jar

USER app
EXPOSE 8081

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENV PORT=8081

ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
