#!/bin/bash

# ============================================================
# 📦 Tesseract OCR Installation Script (Enhanced for macOS 13)
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

# Function to update Command Line Tools (macOS only)
update_command_line_tools() {
    echo "🔧 Attempting to update Command Line Tools..."
    echo "   This may require administrator privileges."
    echo ""
    
    # Check if update is needed
    if softwareupdate --list 2>/dev/null | grep -q "Command Line Tools"; then
        echo "📥 Command Line Tools update available. Installing..."
        sudo softwareupdate --install "Command Line Tools for Xcode" 2>/dev/null || {
            echo "⚠️  Automatic update failed. Please run manually:"
            echo "   sudo rm -rf /Library/Developer/CommandLineTools"
            echo "   sudo xcode-select --install"
            return 1
        }
        return 0
    else
        echo "💡 No Command Line Tools update found via softwareupdate."
        echo "   You may need to update manually:"
        echo "   sudo rm -rf /Library/Developer/CommandLineTools"
        echo "   sudo xcode-select --install"
        return 1
    fi
}

# Function to install via conda/miniconda
install_via_conda() {
    if command -v conda &> /dev/null; then
        echo "📥 Installing Tesseract OCR via conda..."
        if conda install -c conda-forge tesseract -y; then
            return 0
        fi
    fi
    
    # Try to install miniconda if not present
    if [ ! -d "$HOME/miniconda3" ] && [ ! -d "$HOME/anaconda3" ]; then
        echo "💡 Conda not found. Would you like to install Miniconda? (y/n)"
        echo "   This is a good option for macOS 13."
        read -r response
        if [[ "$response" =~ ^[Yy]$ ]]; then
            echo "📥 Downloading Miniconda..."
            curl -O https://repo.anaconda.com/miniconda/Miniconda3-latest-MacOSX-x86_64.sh 2>/dev/null || \
            curl -O https://repo.anaconda.com/miniconda/Miniconda3-latest-MacOSX-arm64.sh 2>/dev/null
            
            if [ -f Miniconda3-*.sh ]; then
                bash Miniconda3-*.sh -b -p "$HOME/miniconda3"
                source "$HOME/miniconda3/bin/activate"
                conda install -c conda-forge tesseract -y
                rm Miniconda3-*.sh
                return 0
            fi
        fi
    fi
    return 1
}

# Function to install MacPorts and tesseract
install_macports() {
    if command -v port &> /dev/null; then
        echo "📥 Installing Tesseract OCR via MacPorts..."
        if sudo port install tesseract; then
            # Add MacPorts to PATH if not already there
            if ! echo "$PATH" | grep -q "/opt/local/bin"; then
                echo "💡 Adding MacPorts to PATH..."
                echo 'export PATH="/opt/local/bin:/opt/local/sbin:$PATH"' >> ~/.zshrc
                export PATH="/opt/local/bin:/opt/local/sbin:$PATH"
            fi
            return 0
        fi
    else
        echo "💡 MacPorts not found. Installing MacPorts..."
        echo "   This is recommended for macOS 13."
        echo ""
        echo "📥 Downloading MacPorts installer..."
        
        # Detect architecture
        ARCH=$(uname -m)
        if [ "$ARCH" = "arm64" ]; then
            MP_URL="https://github.com/macports/macports-base/releases/download/v2.10.0/MacPorts-2.10.0-13-Ventura-arm64.pkg"
        else
            MP_URL="https://github.com/macports/macports-base/releases/download/v2.10.0/MacPorts-2.10.0-13-Ventura.pkg"
        fi
        
        echo "   Downloading from: $MP_URL"
        curl -L -o /tmp/MacPorts.pkg "$MP_URL" 2>/dev/null
        
        if [ -f /tmp/MacPorts.pkg ]; then
            echo "📦 Installing MacPorts (requires admin password)..."
            sudo installer -pkg /tmp/MacPorts.pkg -target /
            
            # Add to PATH
            if [ -f /opt/local/bin/port ]; then
                echo 'export PATH="/opt/local/bin:/opt/local/sbin:$PATH"' >> ~/.zshrc
                export PATH="/opt/local/bin:/opt/local/sbin:$PATH"
                
                # Update MacPorts
                sudo port -v selfupdate
                
                # Install tesseract
                echo "📥 Installing Tesseract via MacPorts..."
                sudo port install tesseract
                return 0
            fi
        else
            echo "❌ Failed to download MacPorts installer"
            echo "   Please install manually from: https://www.macports.org/install.php"
        fi
    fi
    return 1
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
        
        INSTALLED=false
        
        # Strategy 1: Try Homebrew (may fail on macOS 13)
        if command -v brew &> /dev/null; then
            echo ""
            echo "📥 Strategy 1: Attempting to install via Homebrew..."
            if brew install tesseract 2>&1 | tee /tmp/brew_install.log; then
                if verify_installation; then
                    INSTALLED=true
                fi
            else
                # Check if it's a compiler issue
                if grep -q "Compiler clang cannot compile programs" /tmp/brew_install.log; then
                    echo ""
                    echo "⚠️  Homebrew failed due to compiler issues."
                    echo "   This is common on macOS 13 with outdated Xcode."
                    echo ""
                    
                    # Try to update Command Line Tools
                    if [ "$MAJOR_VERSION" -lt 14 ]; then
                        echo "🔧 Attempting to fix Command Line Tools..."
                        update_command_line_tools
                        
                        # Retry Homebrew after update
                        echo ""
                        echo "🔄 Retrying Homebrew installation..."
                        if brew install tesseract 2>&1; then
                            if verify_installation; then
                                INSTALLED=true
                            fi
                        fi
                    fi
                fi
            fi
        fi
        
        # Strategy 2: Try conda/miniconda
        if [ "$INSTALLED" = false ]; then
            echo ""
            echo "📥 Strategy 2: Attempting to install via conda/miniconda..."
            if install_via_conda; then
                if verify_installation; then
                    INSTALLED=true
                fi
            fi
        fi
        
        # Strategy 3: Try MacPorts (best for macOS 13)
        if [ "$INSTALLED" = false ]; then
            echo ""
            echo "📥 Strategy 3: Attempting to install via MacPorts..."
            echo "   (MacPorts is recommended for macOS 13)"
            if install_macports; then
                if verify_installation; then
                    INSTALLED=true
                fi
            fi
        fi
        
        # Strategy 4: Manual installation guide
        if [ "$INSTALLED" = false ]; then
            echo ""
            echo "❌ Automatic installation failed. Manual installation required."
            echo ""
            echo "📝 Recommended Options for macOS 13:"
            echo ""
            echo "Option 1: Install MacPorts (RECOMMENDED)"
            echo "   1. Download from: https://www.macports.org/install.php"
            echo "   2. Install the .pkg file"
            echo "   3. Run: sudo port install tesseract"
            echo ""
            echo "Option 2: Update Xcode/Command Line Tools, then retry Homebrew"
            echo "   1. Update Xcode from App Store to 15.2+, OR"
            echo "   2. Update Command Line Tools:"
            echo "      sudo rm -rf /Library/Developer/CommandLineTools"
            echo "      sudo xcode-select --install"
            echo "   3. Run this script again: ./install-tesseract.sh"
            echo ""
            echo "Option 3: Install Miniconda, then install tesseract"
            echo "   1. Download from: https://docs.conda.io/en/latest/miniconda.html"
            echo "   2. Install Miniconda"
            echo "   3. Run: conda install -c conda-forge tesseract -y"
            echo ""
            echo "Option 4: Use pre-built binary"
            echo "   1. Download from: https://github.com/UB-Mannheim/tesseract/wiki"
            echo "   2. Extract and add to PATH"
            echo ""
            exit 1
        fi
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
