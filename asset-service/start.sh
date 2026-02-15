#!/bin/bash

# ============================================================
# 🚀 Start Asset Service with Tesseract OCR Support
# ============================================================
# This script MUST be used to start the application on macOS
# It sets DYLD_LIBRARY_PATH BEFORE the JVM starts

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

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

# CRITICAL: Set DYLD_LIBRARY_PATH BEFORE starting JVM (macOS requirement)
if [[ "$OSTYPE" == "darwin"* ]]; then
    LIB_PATHS=""
    
    if [ -d "/opt/local/lib" ]; then
        LIB_PATHS="/opt/local/lib"
    fi
    
    if [ -d "/opt/homebrew/lib" ]; then
        if [ -n "$LIB_PATHS" ]; then
            LIB_PATHS="$LIB_PATHS:/opt/homebrew/lib"
        else
            LIB_PATHS="/opt/homebrew/lib"
        fi
    fi
    
    if [ -d "/usr/local/lib" ]; then
        if [ -n "$LIB_PATHS" ]; then
            LIB_PATHS="$LIB_PATHS:/usr/local/lib"
        else
            LIB_PATHS="/usr/local/lib"
        fi
    fi
    
    # Add existing library path
    if [ -n "$DYLD_LIBRARY_PATH" ]; then
        LIB_PATHS="$LIB_PATHS:$DYLD_LIBRARY_PATH"
    fi
    
    if [ -n "$LIB_PATHS" ]; then
        export DYLD_LIBRARY_PATH="$LIB_PATHS"
        echo "📚 Set DYLD_LIBRARY_PATH: $LIB_PATHS"
    fi
    
    # Build java.library.path for JVM
    JAVA_LIB_PATH="$LIB_PATHS"
    if [ -n "$JAVA_LIB_PATH" ]; then
        JAVA_OPTS="-Djava.library.path=$JAVA_LIB_PATH"
        if [ -n "$TESSDATA_PREFIX" ]; then
            JAVA_OPTS="$JAVA_OPTS -DTESSDATA_PREFIX=$TESSDATA_PREFIX"
        fi
        export MAVEN_OPTS="$JAVA_OPTS"
        echo "📚 JVM library path: $JAVA_LIB_PATH"
        echo "📝 Using MAVEN_OPTS: $MAVEN_OPTS"
    fi
fi

echo ""
echo "🚀 Starting Asset Service..."
echo ""

# Run Maven - environment variables are already exported
# The JVM will inherit DYLD_LIBRARY_PATH from the shell environment
mvn spring-boot:run

