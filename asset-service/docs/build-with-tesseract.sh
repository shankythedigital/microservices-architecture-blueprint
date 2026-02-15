#!/bin/bash

# ============================================================
# 🔨 Build Script for Asset Service with Tesseract Integration
# ============================================================
# This script ensures Tesseract is properly configured before building

set -e

echo "🔍 Checking Tesseract installation..."

# Check if tesseract is installed
if ! command -v tesseract &> /dev/null; then
    echo "❌ Tesseract is not installed or not in PATH"
    echo "📝 Please install Tesseract first:"
    echo "   cd asset-service/docs && ./install-tesseract.sh"
    exit 1
fi

# Get tesseract version
TESSERACT_VERSION=$(tesseract --version 2>&1 | head -n1)
echo "✅ Tesseract found: $TESSERACT_VERSION"

# Find tessdata path
TESSDATA_PATH=""
if [ -d "/opt/local/share/tessdata" ]; then
    TESSDATA_PATH="/opt/local/share/tessdata"
elif [ -d "/opt/homebrew/share/tessdata" ]; then
    TESSDATA_PATH="/opt/homebrew/share/tessdata"
elif [ -d "/usr/local/share/tessdata" ]; then
    TESSDATA_PATH="/usr/local/share/tessdata"
fi

if [ -n "$TESSDATA_PATH" ]; then
    echo "✅ Tessdata found at: $TESSDATA_PATH"
    export TESSDATA_PREFIX="$TESSDATA_PATH"
else
    echo "⚠️  Warning: Could not find tessdata directory"
    echo "   Make sure Tesseract is properly installed"
fi

# Set library path for native libraries
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS - add MacPorts/Homebrew lib paths
    export DYLD_LIBRARY_PATH="/opt/local/lib:/opt/homebrew/lib:/usr/local/lib:$DYLD_LIBRARY_PATH"
    echo "📝 Set DYLD_LIBRARY_PATH for macOS"
fi

echo ""
echo "🔨 Building Asset Service..."
echo ""

# Navigate to asset-service directory
cd "$(dirname "$0")/.."

# Build with Maven
mvn clean package -DskipTests

echo ""
echo "✅ Build completed successfully!"
echo ""
echo "📝 To run the application:"
echo "   mvn spring-boot:run"
echo ""
echo "   Or with JAR:"
echo "   java -jar target/asset-service-*.jar"
echo ""

