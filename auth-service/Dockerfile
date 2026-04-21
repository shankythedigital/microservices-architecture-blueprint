# syntax=docker/dockerfile:1
# ============================================================
# Auth Service (Spring Boot, Java 17)
# ============================================================
# Build from repository root (needs parent POM + common-service for -am):
#   docker build -f auth-service/Dockerfile -t auth-service:latest .
#
# From auth-service/:
#   docker build -f Dockerfile -t auth-service:latest ..
#
# Optional: use a root .dockerignore to keep the build context small.
#
# Run (Render/cloud: set PORT, SPRING_DATASOURCE_*, JWT_SECRET, …):
#   docker run --rm -p 8081:8081 auth-service:latest

# --- build fat JAR inside the image (no local mvn required) ---
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY . .
RUN mvn -B -DskipTests clean package -pl auth-service -am

# --- minimal runtime ---
FROM eclipse-temurin:17-jre-jammy

RUN useradd -m -u 1000 app \
    && apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=builder /build/auth-service/target/*.jar app.jar

USER app
EXPOSE 8081

# Container-friendly defaults; override with JAVA_TOOL_OPTIONS if needed
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Spring maps server.port to PORT on many platforms
ENV PORT=8081

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -fsS "http://127.0.0.1:${PORT}/api-docs" >/dev/null || exit 1

ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
