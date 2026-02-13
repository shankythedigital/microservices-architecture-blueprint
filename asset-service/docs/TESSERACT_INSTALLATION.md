# Tesseract OCR Installation Guide for macOS 13

## Problem
On macOS 13 (Ventura) with outdated Xcode/Command Line Tools, Homebrew fails to install Tesseract due to compiler issues. This guide provides multiple solutions.

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

After installing Tesseract, restart your Spring Boot application:

```bash
cd asset-service
mvn spring-boot:run
```

The `OcrService` will automatically detect Tesseract if it's in the system PATH.

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
