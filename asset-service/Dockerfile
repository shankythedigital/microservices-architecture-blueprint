# syntax=docker/dockerfile:1
# asset-service-lxip — same app as asset-service; use for Render (e.g. asset-service-lxip.onrender.com)
# or any host that sets PORT, SPRING_DATASOURCE_URL, AUTH_SERVICE_URL, etc.
#
# Build from repository root:
#   docker build -f asset-service/Dockerfile.lxip -t asset-service-lxip:latest .
#
# From asset-service/:
#   docker build -f Dockerfile.lxip -t asset-service-lxip:latest ..
#
# Runtime: server.port uses ${PORT:7075} in application.yml — Render injects PORT automatically.

FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY . .
RUN mvn -B -DskipTests clean package -pl asset-service -am

FROM eclipse-temurin:17-jre-jammy

RUN apt-get update && apt-get install -y --no-install-recommends \
    tesseract-ocr \
    tesseract-ocr-eng \
    && rm -rf /var/lib/apt/lists/*

RUN useradd -m -u 1000 app
WORKDIR /app

COPY --from=builder /build/asset-service/target/*.jar app.jar

USER app
EXPOSE 7075

ENV TESSDATA_PREFIX=/usr/share/tesseract-ocr/4.00/tessdata
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENV PORT=7075

ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
