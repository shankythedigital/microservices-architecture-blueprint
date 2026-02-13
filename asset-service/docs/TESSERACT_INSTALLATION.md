# Tesseract OCR Installation Guide

## Overview
Tesseract OCR is required for extracting text from images (JPG, PNG, GIF, etc.) in the Asset Management system.

## Quick Installation (Automated)

### Using the Installation Script (Recommended)
```bash
# Navigate to the asset-service directory
cd asset-service

# Run the installation script
./docs/install-tesseract.sh
```

The script will:
- Detect your operating system
- Install Tesseract using the appropriate package manager
- Verify the installation
- Provide next steps

## Manual Installation Instructions

### macOS

#### Using Homebrew (Recommended)
```bash
# Install Tesseract
brew install tesseract

# Verify installation
tesseract --version

# Check data path (usually /opt/homebrew/share/tessdata for Apple Silicon or /usr/local/share/tessdata for Intel)
ls /opt/homebrew/share/tessdata
```

#### Using MacPorts
```bash
sudo port install tesseract
```

### Linux (Ubuntu/Debian)
```bash
sudo apt-get update
sudo apt-get install tesseract-ocr
```

### Linux (CentOS/RHEL/Fedora)
```bash
# For CentOS/RHEL
sudo yum install tesseract

# For Fedora
sudo dnf install tesseract
```

### Windows
1. Download the installer from: https://github.com/UB-Mannheim/tesseract/wiki
2. Run the installer
3. Default installation path: `C:\Program Files\Tesseract-OCR\tessdata`
4. Add Tesseract to your system PATH

## Environment Variables (Optional)

If Tesseract is installed in a non-standard location, set the `TESSDATA_PREFIX` environment variable:

```bash
# macOS/Linux
export TESSDATA_PREFIX=/path/to/tessdata

# Windows
set TESSDATA_PREFIX=C:\path\to\tessdata
```

## Verification

After installation, restart the application and check the logs. You should see:
```
✅ Tesseract OCR initialized successfully. Data path: /opt/homebrew/share/tessdata
```

If you see an error, ensure:
1. Tesseract is installed correctly
2. The tessdata directory exists and contains language files (e.g., `eng.traineddata`)
3. The application has read permissions to the tessdata directory

## Troubleshooting

### Error: "Unable to load library 'tesseract'"
- **Solution**: Install Tesseract using the instructions above
- **macOS**: `brew install tesseract`
- **Linux**: `sudo apt-get install tesseract-ocr`
- **Windows**: Download and install from the official website

### Error: "Tesseract data path not found"
- **Solution**: Set the `TESSDATA_PREFIX` environment variable to point to your tessdata directory
- Or ensure Tesseract is installed in the default location

### Error: "No language data found"
- **Solution**: Ensure the tessdata directory contains language files (e.g., `eng.traineddata`)
- Download additional language data if needed from: https://github.com/tesseract-ocr/tessdata

## Quick Fix for Current Error

If you're seeing the error: "Tesseract OCR is not installed on the server", follow these steps:

### macOS (Quick Fix)
```bash
# 1. Install Tesseract
brew install tesseract

# 2. Verify installation
tesseract --version

# 3. Restart your Spring Boot application
```

### Linux (Quick Fix)
```bash
# 1. Install Tesseract
sudo apt-get update
sudo apt-get install tesseract-ocr

# 2. Verify installation
tesseract --version

# 3. Restart your Spring Boot application
```

### Windows (Quick Fix)
1. Download installer from: https://github.com/UB-Mannheim/tesseract/wiki
2. Run the installer (default path: `C:\Program Files\Tesseract-OCR`)
3. Add to PATH: Add `C:\Program Files\Tesseract-OCR` to your system PATH
4. Verify: Open Command Prompt and run `tesseract --version`
5. Restart your Spring Boot application

## Notes

- The application will work for PDF, Word, Excel, and PowerPoint files even without Tesseract
- Image OCR (JPG, PNG, GIF) requires Tesseract to be installed
- The application will provide clear error messages if Tesseract is not available
- After installation, **always restart the application** for changes to take effect

