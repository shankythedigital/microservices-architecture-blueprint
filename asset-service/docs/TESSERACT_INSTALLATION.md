# Tesseract OCR Installation Guide for macOS 13

## Build vs runtime: no Tesseract required on the server

- **Build:** Maven build does **not** require Tesseract. You can run `mvn clean package` on any machine (CI, server, laptop) without installing Tesseract. The app compiles and packages normally.
- **Runtime:** Tesseract is only needed at **runtime** when the application performs image OCR. If Tesseract is not installed, the app still starts; OCR features are disabled and return a clear error when used.
- **Server without host install:** To run the app on a server **without** installing Tesseract on the host, use the provided Docker image (see [Server deployment with Docker](#server-deployment-with-docker)) which includes Tesseract inside the container.

## Problem (macOS 13 only)
On macOS 13 (Ventura) with outdated Xcode/Command Line Tools, Homebrew may fail to install Tesseract due to compiler issues. This guide is for **local/developer** machines where you want OCR. The server does not need this if you use Docker or accept “OCR disabled”.

## Quick Fix (Recommended for macOS 13)

### Option 1: Install MacPorts (BEST for macOS 13) ⭐

MacPorts supports older macOS versions and doesn't require Xcode updates.

1. **Download MacPorts:**
   - Visit: https://www.macports.org/install.php
   - Download the appropriate package:
     - **Intel Mac**: `MacPorts-2.10.0-13-Ventura.pkg`
     - **Apple Silicon**: `MacPorts-2.10.0-13-Ventura-arm64.pkg`

2. **Install MacPorts:**
   - Double-click the downloaded `.pkg` file
   - Follow the installation wizard
   - Enter your admin password when prompted

3. **Install Tesseract:**
   ```bash
   sudo port install tesseract
   ```

4. **Verify Installation:**
   ```bash
   tesseract --version
   ```

5. **Add to PATH (if needed):**
   ```bash
   echo 'export PATH="/opt/local/bin:/opt/local/sbin:$PATH"' >> ~/.zshrc
   source ~/.zshrc
   ```

### Option 2: Update Command Line Tools, then Homebrew

1. **Remove old Command Line Tools:**
   ```bash
   sudo rm -rf /Library/Developer/CommandLineTools
   ```

2. **Install new Command Line Tools:**
   ```bash
   sudo xcode-select --install
   ```
   - A dialog will appear - click "Install"
   - Wait for installation to complete (10-15 minutes)

3. **Verify Xcode path:**
   ```bash
   xcode-select -p
   ```
   Should show: `/Library/Developer/CommandLineTools` or `/Applications/Xcode.app/Contents/Developer`

4. **Install Tesseract via Homebrew:**
   ```bash
   brew install tesseract
   ```

5. **Verify Installation:**
   ```bash
   tesseract --version
   ```

### Option 3: Install Miniconda, then Tesseract

1. **Download Miniconda:**
   - Visit: https://docs.conda.io/en/latest/miniconda.html
   - Download the macOS installer for your architecture

2. **Install Miniconda:**
   ```bash
   bash Miniconda3-latest-MacOSX-x86_64.sh  # Intel
   # OR
   bash Miniconda3-latest-MacOSX-arm64.sh   # Apple Silicon
   ```

3. **Install Tesseract:**
   ```bash
   conda install -c conda-forge tesseract -y
   ```

4. **Verify Installation:**
   ```bash
   tesseract --version
   ```

## Automated Installation Scripts

### Script 1: Main Installation Script
```bash
cd asset-service/docs
./install-tesseract.sh
```

This script tries multiple installation methods automatically.

### Script 2: Interactive Fix Script
```bash
cd asset-service/docs
./fix-and-install-tesseract.sh
```

This script guides you through the installation process step-by-step.

## Verification

After installation, verify Tesseract is working:

```bash
# Check version
tesseract --version

# Test OCR on an image (if you have one)
tesseract test-image.png stdout
```

## Troubleshooting

### Issue: "tesseract: command not found"

**Solution:**
1. Check if tesseract is in your PATH:
   ```bash
   which tesseract
   ```

2. If using MacPorts, add to PATH:
   ```bash
   echo 'export PATH="/opt/local/bin:/opt/local/sbin:$PATH"' >> ~/.zshrc
   source ~/.zshrc
   ```

3. If using Conda, activate the environment:
   ```bash
   conda activate base
   ```

### Issue: "Tesseract data path not found"

**Solution:**
The Java application looks for tessdata in these locations:
- `/opt/homebrew/share/tessdata` (Homebrew - Apple Silicon)
- `/usr/local/share/tessdata` (Homebrew - Intel)
- `/opt/local/share/tessdata` (MacPorts)

Set the environment variable if needed:
```bash
export TESSDATA_PREFIX="/opt/local/share/tessdata"  # MacPorts
# OR
export TESSDATA_PREFIX="/opt/homebrew/share/tessdata"  # Homebrew
```

### Issue: Homebrew still fails after updating Command Line Tools

**Solution:**
1. Try updating Homebrew:
   ```bash
   brew update
   brew upgrade
   ```

2. Clean Homebrew cache:
   ```bash
   brew cleanup
   ```

3. If still failing, use MacPorts (Option 1) instead.

## For Spring Boot Application

After installing Tesseract (on your **local** machine), restart your Spring Boot application:

```bash
cd asset-service
mvn spring-boot:run
```

The `OcrService` uses **process mode** by default: it calls the `tesseract` executable (no native library). It auto-detects the binary from config or PATH.

## Server deployment with Docker (no Tesseract on host)

To run the asset service on a server **without** installing Tesseract on the host:

1. **Build the JAR** (on CI or any machine; **Tesseract not required**):
   ```bash
   # From repo root
   mvn clean package -DskipTests
   ```
2. **Build the Docker image** (from repo root; Tesseract is installed inside the image):
   ```bash
   docker build -f asset-service/Dockerfile -t asset-service:latest .
   ```
3. **Run the container.** Tesseract is inside the image; the host does not need it:
   ```bash
   docker run -p 8083:8083 -e SPRING_DATASOURCE_URL=... asset-service:latest
   ```

The Dockerfile installs `tesseract-ocr` in the image, so the server only needs Docker—no Tesseract install on the host.

## Summary

| Context | Tesseract required? |
|--------|----------------------|
| Maven build (`mvn clean package`) | **No** |
| App startup | **No** (app runs; OCR disabled if missing) |
| Image OCR at runtime | **Yes** (or use Docker image that includes it) |
| Server host (when using Docker) | **No** (Tesseract is inside the container) |

## Additional Resources

- **Tesseract Official**: https://github.com/tesseract-ocr/tesseract
- **MacPorts**: https://www.macports.org
- **Homebrew**: https://brew.sh
- **Conda**: https://docs.conda.io

## Support

If you encounter issues:
1. Check the installation logs
2. Verify Tesseract is in your PATH
3. Check the Spring Boot application logs for OCR service initialization messages
4. Ensure the tessdata directory exists and contains language files
