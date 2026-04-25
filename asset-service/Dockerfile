# syntax=docker/dockerfile:1
# Asset Service — Spring Boot 3, Java 17, Tesseract OCR
#
# Build (from repo root; needs parent POM + common-service):
#   docker build -f asset-service/Dockerfile -t asset-service:latest .
#
# From asset-service/:
#   docker build -f Dockerfile -t asset-service:latest ..
#
# Run (host does not need Tesseract; set env for DB and service URLs):
#   docker run --rm -p 8085:8085 -e SPRING_DATASOURCE_URL -e AUTH_SERVICE_URL asset-service:latest

FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY . .
RUN mvn -B -DskipTests clean package -pl asset-service -am

FROM eclipse-temurin:17-jre-jammy

# OCR for product flows (Tesseract in-image; not required on the host)
RUN apt-get update && apt-get install -y --no-install-recommends \
    tesseract-ocr \
    tesseract-ocr-eng \
    && rm -rf /var/lib/apt/lists/*

RUN useradd -m -u 1000 app
WORKDIR /app

COPY --from=builder /build/asset-service/target/*.jar app.jar

USER app
EXPOSE 8085

ENV TESSDATA_PREFIX=/usr/share/tesseract-ocr/4.00/tessdata
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENV PORT=8085

ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
