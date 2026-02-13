#!/bin/bash

# ============================================================
# 📦 Tesseract OCR Installation Script
# ============================================================
# This script installs Tesseract OCR on macOS, Linux, or Windows (WSL)
# After installation, restart your Spring Boot application

set +e  # Don't exit on errors, we'll handle them gracefully

echo "🔍 Checking system..."
OS="$(uname -s)"

# Function to check if tesseract is already installed
check_tesseract_installed() {
    if command -v tesseract &> /dev/null; then
        echo "✅ Tesseract is already installed!"
        tesseract --version
        return 0
    fi
    return 1
}

# Function to verify installation
verify_installation() {
    echo ""
    echo "✅ Verifying installation..."
    if command -v tesseract &> /dev/null; then
        tesseract --version
        echo ""
        echo "✅ Tesseract OCR installed successfully!"
        echo "📝 Next steps:"
        echo "   1. Restart your Spring Boot application"
        echo "   2. Test the OCR functionality"
        return 0
    else
        return 1
    fi
}

# Check if already installed
if check_tesseract_installed; then
    exit 0
fi

case "${OS}" in
    Linux*)
        echo "📦 Detected: Linux"
        if command -v apt-get &> /dev/null; then
            echo "📥 Installing Tesseract OCR via apt-get..."
            sudo apt-get update
            sudo apt-get install -y tesseract-ocr
        elif command -v yum &> /dev/null; then
            echo "📥 Installing Tesseract OCR via yum..."
            sudo yum install -y tesseract
        elif command -v brew &> /dev/null; then
            echo "📥 Installing Tesseract OCR via Homebrew..."
            brew install tesseract
        else
            echo "❌ Package manager not found. Please install Tesseract manually."
            exit 1
        fi
        ;;
    Darwin*)
        echo "📦 Detected: macOS"
        
        # Check macOS version
        MACOS_VERSION=$(sw_vers -productVersion)
        MAJOR_VERSION=$(echo "$MACOS_VERSION" | cut -d. -f1)
        echo "   macOS Version: $MACOS_VERSION"
        
        if [ "$MAJOR_VERSION" -lt 14 ]; then
            echo "⚠️  Warning: macOS $MACOS_VERSION is outdated (Tier 3)."
            echo "   Homebrew may not support this version."
            echo ""
        fi
        
        # Try Homebrew first
        if command -v brew &> /dev/null; then
            echo "📥 Attempting to install Tesseract OCR via Homebrew..."
            if brew install tesseract 2>&1; then
                if verify_installation; then
                    exit 0
                fi
            else
                echo ""
                echo "❌ Homebrew installation failed (this is common on older macOS versions)"
                echo ""
            fi
        else
            echo "❌ Homebrew not found."
        fi
        
        # Try MacPorts as alternative for older macOS
        if command -v port &> /dev/null; then
            echo "📥 Attempting to install Tesseract OCR via MacPorts..."
            echo "   (MacPorts supports older macOS versions)"
            if sudo port install tesseract; then
                if verify_installation; then
                    exit 0
                fi
            else
                echo "❌ MacPorts installation failed"
            fi
        else
            echo "💡 MacPorts not found. For older macOS versions, consider installing MacPorts:"
            echo "   https://www.macports.org/install.php"
        fi
        
        # Provide manual installation instructions
        echo ""
        echo "📝 Manual Installation Options:"
        echo ""
        echo "Option 1: Install via MacPorts (recommended for macOS 13):"
        echo "   1. Install MacPorts: https://www.macports.org/install.php"
        echo "   2. Run: sudo port install tesseract"
        echo ""
        echo "Option 2: Install pre-built binary:"
        echo "   1. Download from: https://github.com/tesseract-ocr/tesseract/wiki"
        echo "   2. Or use: https://github.com/UB-Mannheim/tesseract/wiki"
        echo ""
        echo "Option 3: Build from source:"
        echo "   1. Install dependencies:"
        echo "      - Xcode Command Line Tools (update if needed)"
        echo "      - cmake, pkg-config, leptonica"
        echo "   2. Clone and build tesseract from source"
        echo ""
        echo "Option 4: Use Docker (if running in containerized environment):"
        echo "   Use a Docker image with tesseract pre-installed"
        echo ""
        echo "⚠️  If you're using macOS 13 with outdated Xcode:"
        echo "   1. Update Xcode to 15.2+ from App Store, OR"
        echo "   2. Update Command Line Tools:"
        echo "      sudo rm -rf /Library/Developer/CommandLineTools"
        echo "      sudo xcode-select --install"
        echo "   3. Then retry: brew install tesseract"
        echo ""
        exit 1
        ;;
    *)
        echo "❌ Unsupported OS: ${OS}"
        echo "📝 Please install Tesseract manually:"
        echo "   macOS: brew install tesseract"
        echo "   Linux: sudo apt-get install tesseract-ocr"
        echo "   Windows: Download from https://github.com/UB-Mannheim/tesseract/wiki"
        exit 1
        ;;
esac

# Final verification
if ! verify_installation; then
    echo "❌ Tesseract installation verification failed"
    echo "📝 Please check the installation and ensure Tesseract is in your PATH"
    exit 1
fi

