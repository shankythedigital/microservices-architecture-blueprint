# ============================================================
# Asset Service with Tesseract OCR (no install needed on host)
# ============================================================
# Build the JAR first (no Tesseract required on build server):
#   From repo root: mvn clean package -DskipTests
#   Or from asset-service: mvn clean package -DskipTests (if parent is available)
#
# Then build this image (from repo root):
#   docker build -f asset-service/Dockerfile -t asset-service:latest .
#   (Ensure asset-service/target/*.jar exists from the Maven build above)
#
# Run: Tesseract is inside the image; the server host does not need Tesseract.

FROM eclipse-temurin:17-jre-jammy AS runtime

# Install Tesseract in the image (so host server does not need it)
RUN apt-get update && apt-get install -y --no-install-recommends \
    tesseract-ocr \
    tesseract-ocr-eng \
    && rm -rf /var/lib/apt/lists/*

# App runs as non-root
RUN useradd -m -u 1000 app
WORKDIR /app

# Copy the pre-built JAR (build context must include asset-service/target/*.jar)
# From repo root: docker build -f asset-service/Dockerfile .
COPY asset-service/target/*.jar app.jar

USER app
EXPOSE 8083

# Tesseract CLI and tessdata path (Debian/Ubuntu)
ENV TESSDATA_PREFIX=/usr/share/tesseract-ocr/4.00/tessdata

ENTRYPOINT ["java", "-jar", "app.jar"]
