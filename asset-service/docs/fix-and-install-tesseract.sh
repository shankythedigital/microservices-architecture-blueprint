#!/bin/bash

# ============================================================
# 🔧 Fix macOS 13 Issues and Install Tesseract OCR (LOCAL / DEV)
# ============================================================
# Use this script on your LOCAL or DEVELOPER machine to install
# Tesseract for image OCR. It is NOT required on the server.
#
# - Build: mvn clean package works WITHOUT Tesseract.
# - Server: Use the Docker image (see Dockerfile) so the server
#   does not need Tesseract installed on the host.
#
# Run with: ./fix-and-install-tesseract.sh

set +e

echo "🔍 Tesseract install (for local/dev OCR). Server does not need this if using Docker."
echo "   Build never requires Tesseract."
echo ""
echo "🔍 Diagnosing macOS 13 Tesseract Installation Issues..."
echo ""

# Check if tesseract is already installed
if command -v tesseract &> /dev/null; then
    echo "✅ Tesseract is already installed!"
    tesseract --version
    exit 0
fi

MACOS_VERSION=$(sw_vers -productVersion)
echo "📱 macOS Version: $MACOS_VERSION"
echo ""

# Step 1: Fix Command Line Tools
echo "🔧 Step 1: Fixing Command Line Tools..."
echo "   This is the root cause of the Homebrew installation failure."
echo ""
echo "⚠️  You need to run these commands manually (requires admin password):"
echo ""
echo "   sudo rm -rf /Library/Developer/CommandLineTools"
echo "   sudo xcode-select --install"
echo ""
echo "   OR update via System Settings > Software Update"
echo ""
read -p "Have you updated Command Line Tools? (y/n): " updated_tools

if [[ "$updated_tools" =~ ^[Yy]$ ]]; then
    echo ""
    echo "🔄 Retrying Homebrew installation..."
    if brew install tesseract 2>&1; then
        if command -v tesseract &> /dev/null; then
            echo ""
            echo "✅ Tesseract installed successfully via Homebrew!"
            tesseract --version
            exit 0
        fi
    fi
    echo "⚠️  Homebrew still failing. Trying alternative methods..."
    echo ""
fi

# Step 2: Try MacPorts (Best for macOS 13)
echo "📥 Step 2: Installing via MacPorts (Recommended for macOS 13)"
echo ""

if command -v port &> /dev/null; then
    echo "✅ MacPorts is installed!"
    echo "📥 Installing Tesseract via MacPorts..."
    sudo port install tesseract
    echo "📥 Installing English language data (required for OCR)..."
    sudo port install tesseract-eng 2>/dev/null || true
    
    if command -v tesseract &> /dev/null; then
        TESSDATA="${TESSDATA_PREFIX:-/opt/local/share/tessdata}"
        if [[ -f "$TESSDATA/eng.traineddata" ]]; then
            echo ""
            echo "✅ Tesseract + English data installed successfully via MacPorts!"
            tesseract --version
            exit 0
        else
            echo ""
            echo "⚠️  Tesseract is installed but eng.traineddata is missing. Run:"
            echo "   sudo port install tesseract-eng"
            echo "   Then restart the asset-service."
        fi
    fi
else
    echo "❌ MacPorts not found."
    echo ""
    echo "📝 To install MacPorts:"
    echo "   1. Download from: https://www.macports.org/install.php"
    echo "   2. Choose: MacPorts-2.10.0-13-Ventura.pkg (for Intel) or"
    echo "              MacPorts-2.10.0-13-Ventura-arm64.pkg (for Apple Silicon)"
    echo "   3. Install the .pkg file"
    echo "   4. Run: sudo port install tesseract"
    echo ""
fi

# Step 3: Try Conda
echo "📥 Step 3: Installing via Conda/Miniconda"
echo ""

if command -v conda &> /dev/null; then
    echo "✅ Conda is installed!"
    echo "📥 Installing Tesseract via Conda..."
    conda install -c conda-forge tesseract -y
    
    if command -v tesseract &> /dev/null; then
        echo ""
        echo "✅ Tesseract installed successfully via Conda!"
        tesseract --version
        exit 0
    fi
else
    echo "❌ Conda not found."
    echo ""
    echo "📝 To install Miniconda:"
    echo "   1. Download from: https://docs.conda.io/en/latest/miniconda.html"
    echo "   2. Install Miniconda"
    echo "   3. Run: conda install -c conda-forge tesseract -y"
    echo ""
fi

# Step 4: Pre-built binary option
echo "📥 Step 4: Using Pre-built Binary (Quickest for macOS 13)"
echo ""
echo "📝 Quick Installation Guide:"
echo ""
echo "Option A: Install MacPorts (RECOMMENDED - 5 minutes)"
echo "   1. Open: https://www.macports.org/install.php"
echo "   2. Download the Ventura .pkg file for your architecture"
echo "   3. Double-click to install"
echo "   4. Open Terminal and run:"
echo "      sudo port install tesseract"
echo ""
echo "Option B: Update Command Line Tools, then Homebrew (10-15 minutes)"
echo "   1. Run these commands:"
echo "      sudo rm -rf /Library/Developer/CommandLineTools"
echo "      sudo xcode-select --install"
echo "   2. Wait for installation to complete"
echo "   3. Run: brew install tesseract"
echo ""
echo "Option C: Server deployment WITHOUT installing Tesseract on the host"
echo "   Use the asset-service Dockerfile (includes tesseract in the image):"
echo "   cd asset-service && docker build -f Dockerfile -t asset-service:latest ."
echo "   docker run -p 7073:7073 asset-service:latest"
echo "   See docs/TESSERACT_INSTALLATION.md#server-deployment-with-docker"
echo ""

echo "✅ Script completed. Choose one of the options above to install Tesseract (local only)."
echo "   For servers: use Docker so the host does not need Tesseract installed."

