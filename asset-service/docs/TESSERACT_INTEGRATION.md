# Tesseract OCR Integration in Asset Service

## Overview

Tesseract OCR is integrated into the Asset Service for extracting text from images and documents. The system uses the **tess4j** Java library to interface with the native Tesseract installation.

## Configuration

### Application Configuration (`application.yml`)

Tesseract is configured in `src/main/resources/application.yml`:

```yaml
tesseract:
  enabled: true
  data-path: /opt/local/share/tessdata  # MacPorts installation
  executable-path: /opt/local/bin/tesseract
  language: eng
  page-seg-mode: 1
  ocr-engine-mode: 1
```

### Configuration Properties

- **enabled**: Enable/disable Tesseract OCR (default: `true`)
- **data-path**: Path to tessdata directory containing language files
- **executable-path**: Path to tesseract executable (optional, uses system PATH if not specified)
- **language**: OCR language code (default: `eng`)
- **page-seg-mode**: Page segmentation mode (default: `1` = Automatic with OSD)
- **ocr-engine-mode**: OCR engine mode (default: `1` = Neural nets LSTM engine only)

## Installation Paths

The system automatically detects Tesseract in these locations (in order of priority):

1. **Configuration** (`application.yml` - `tesseract.data-path`)
2. **Environment Variable** (`TESSDATA_PREFIX`)
3. **Auto-detection**:
   - MacPorts: `/opt/local/share/tessdata`
   - Homebrew (Apple Silicon): `/opt/homebrew/share/tessdata`
   - Homebrew (Intel): `/usr/local/share/tessdata`
   - Linux: `/usr/share/tesseract-ocr/5/tessdata`
   - Windows: `C:\Program Files\Tesseract-OCR\tessdata`

## Build Configuration

### Maven Dependencies

The project includes `tess4j` in `pom.xml`:

```xml
<dependency>
    <groupId>net.sourceforge.tess4j</groupId>
    <artifactId>tess4j</artifactId>
    <version>5.8.0</version>
</dependency>
```

### Build Script

Use the provided build script to ensure Tesseract is available:

```bash
cd asset-service/docs
./build-with-tesseract.sh
```

This script:
- Verifies Tesseract installation
- Sets environment variables (`TESSDATA_PREFIX`)
- Configures library paths for native libraries
- Builds the project with Maven

### Manual Build

If building manually:

```bash
# Set environment variables
export TESSDATA_PREFIX="/opt/local/share/tessdata"
export DYLD_LIBRARY_PATH="/opt/local/lib:/opt/homebrew/lib:/usr/local/lib:$DYLD_LIBRARY_PATH"

# Build
mvn clean package
```

## Runtime Configuration

### Running the Application

The application automatically detects and initializes Tesseract on startup. Check the logs for:

```
✅ Tesseract OCR initialized and verified. Data path: /opt/local/share/tessdata
```

### Environment Variables

You can override configuration using environment variables:

```bash
export TESSDATA_PREFIX="/opt/local/share/tessdata"
mvn spring-boot:run
```

### Docker/Container Deployment

For containerized deployments, ensure Tesseract is installed in the container:

```dockerfile
# Example Dockerfile
FROM openjdk:17-jdk-slim

# Install Tesseract
RUN apt-get update && \
    apt-get install -y tesseract-ocr && \
    rm -rf /var/lib/apt/lists/*

# Set environment variable
ENV TESSDATA_PREFIX=/usr/share/tesseract-ocr/5/tessdata

# Copy application
COPY target/asset-service-*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
```

## Usage in Code

The `OcrService` is automatically injected and can be used:

```java
@Autowired
private OcrService ocrService;

// Check if Tesseract is available
if (ocrService.isTesseractAvailable()) {
    String text = ocrService.extractText(multipartFile);
}
```

## Verification

### Check Installation

```bash
# Verify Tesseract is installed
tesseract --version

# Verify tessdata directory exists
ls -la /opt/local/share/tessdata
```

### Test in Application

1. Start the application
2. Check logs for Tesseract initialization message
3. Upload an image via the API
4. Verify OCR text extraction works

## Troubleshooting

### Issue: "Tesseract OCR library not found"

**Solution:**
- Ensure Tesseract is installed and in PATH
- Verify `tessdata` directory exists
- Check `application.yml` configuration

### Issue: "Tesseract data path does not exist"

**Solution:**
- Update `tesseract.data-path` in `application.yml`
- Or set `TESSDATA_PREFIX` environment variable
- Verify the path contains language files (e.g., `eng.traineddata`)

### Issue: Native library loading errors

**Solution:**
- Ensure native libraries are in library path
- On macOS: Set `DYLD_LIBRARY_PATH`
- On Linux: Set `LD_LIBRARY_PATH`
- Use the build script which handles this automatically

## Files Modified

1. **`application.yml`**: Added Tesseract configuration section
2. **`TesseractConfig.java`**: Configuration properties class
3. **`OcrService.java`**: Updated to use configuration properties
4. **`pom.xml`**: Added Maven Surefire plugin configuration for native libraries
5. **`build-with-tesseract.sh`**: Build script with Tesseract setup

## Next Steps

1. **Build the project**: `./docs/build-with-tesseract.sh`
2. **Run the application**: `mvn spring-boot:run`
3. **Test OCR**: Upload an image via the API endpoint
4. **Verify logs**: Check for successful Tesseract initialization

