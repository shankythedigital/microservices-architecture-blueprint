#!/bin/bash

# iOS Build Fix Script
# This script fixes common iOS build issues

set -e

echo "🔧 Fixing iOS build issues..."

cd "$(dirname "$0")/ios"

# Clean previous builds
echo "🧹 Cleaning previous builds..."
rm -rf Pods
rm -rf Podfile.lock
rm -rf build
rm -rf ~/Library/Developer/Xcode/DerivedData/*

# Install/Update CocoaPods dependencies
echo "📦 Installing CocoaPods dependencies..."
if command -v pod &> /dev/null; then
    pod deintegrate || true
    pod cache clean --all || true
    pod install --repo-update
    echo "✅ CocoaPods installed successfully"
else
    echo "⚠️  CocoaPods not found. Installing..."
    sudo gem install cocoapods
    pod install --repo-update
fi

# Fix permissions
echo "🔐 Setting up permissions..."
chmod +x ../ios/Podfile

echo "✅ iOS build fixes applied!"
echo ""
echo "Next steps:"
echo "1. Open Xcode: open ios/Runner.xcworkspace"
echo "2. Select a development team in Signing & Capabilities"
echo "3. Build and run from Xcode or use: flutter run"

