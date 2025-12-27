#!/bin/bash

# Flutter Project Setup and Run Script
# This script sets up the Flutter project and runs it

set -e

echo "🚀 Setting up Keeply Flutter App..."

# Check if Flutter is installed
if ! command -v flutter &> /dev/null; then
    echo "❌ Flutter is not installed or not in PATH"
    echo "Please install Flutter from https://flutter.dev/docs/get-started/install"
    exit 1
fi

# Get Flutter version
echo "📱 Flutter version:"
flutter --version

# Navigate to project directory
cd "$(dirname "$0")"

# Clean previous builds
echo "🧹 Cleaning previous builds..."
flutter clean

# Get dependencies
echo "📦 Installing dependencies..."
flutter pub get

# Analyze code
echo "🔍 Analyzing code..."
flutter analyze

# Check for connected devices
echo "📱 Checking for connected devices..."
flutter devices

# Run the app
echo "▶️  Running the app..."
echo "Note: Make sure you have a device connected or emulator running"
flutter run

