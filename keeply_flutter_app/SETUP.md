# Flutter Project Setup Guide

## Prerequisites

1. **Flutter SDK** (3.0.0 or higher)
   - Download from: https://flutter.dev/docs/get-started/install
   - Verify installation: `flutter doctor`

2. **Android Studio** or **VS Code** with Flutter extensions
3. **Xcode** (for iOS development on macOS)
4. **Android SDK** (for Android development)

## Setup Steps

### 1. Install Dependencies

```bash
cd keeply_flutter_app
flutter pub get
```

### 2. Verify Flutter Installation

```bash
flutter doctor
```

Fix any issues reported by `flutter doctor`.

### 3. Check Connected Devices

```bash
flutter devices
```

Make sure you have:
- An emulator running (Android/iOS), OR
- A physical device connected via USB with USB debugging enabled

### 4. Run the App

**Option A: Using the setup script**
```bash
./setup_and_run.sh
```

**Option B: Manual commands**
```bash
# Clean previous builds
flutter clean

# Get dependencies
flutter pub get

# Analyze code
flutter analyze

# Run the app
flutter run
```

### 5. Build for Production

**Android APK:**
```bash
flutter build apk --release
```

**iOS IPA:**
```bash
flutter build ios --release
```

## Troubleshooting

### Flutter Not Found

If you get "command not found: flutter":
1. Add Flutter to your PATH
2. On macOS/Linux: Add to `~/.zshrc` or `~/.bashrc`:
   ```bash
   export PATH="$PATH:/path/to/flutter/bin"
   ```
3. Reload shell: `source ~/.zshrc` or `source ~/.bashrc`

### iOS Build Issues

1. Install CocoaPods dependencies:
   ```bash
   cd ios
   pod install
   cd ..
   ```

2. Open Xcode and configure signing:
   ```bash
   open ios/Runner.xcworkspace
   ```

### Android Build Issues

1. Accept Android licenses:
   ```bash
   flutter doctor --android-licenses
   ```

2. Set up Android SDK path in `android/local.properties`:
   ```properties
   sdk.dir=/path/to/android/sdk
   ```

### Dependency Conflicts

If you encounter dependency conflicts:
```bash
flutter pub upgrade
flutter pub get
```

## Project Structure

```
lib/
├── core/              # Core utilities, network, config
├── features/          # Feature modules
│   ├── auth/         # Authentication
│   ├── asset/        # Asset management
│   └── notification/ # Notifications
└── main.dart         # App entry point
```

## API Configuration

Update API endpoints in `lib/core/config/app_config.dart`:

```dart
static const String authServiceBaseUrl = 'http://localhost:8081';
static const String notificationServiceBaseUrl = 'http://localhost:8082';
static const String assetServiceBaseUrl = 'http://localhost:8083';
```

## Running Tests

```bash
# Unit tests
flutter test

# Integration tests
flutter test integration_test/
```

## Code Generation

If using code generation (JSON serialization, etc.):
```bash
flutter pub run build_runner build --delete-conflicting-outputs
```

## Common Commands

```bash
# Check for issues
flutter analyze

# Format code
flutter format .

# Get device info
flutter devices

# Check Flutter version
flutter --version

# Clean build
flutter clean
```

## Next Steps

1. Ensure all three microservices are running:
   - Auth Service: http://localhost:8081
   - Notification Service: http://localhost:8082
   - Asset Service: http://localhost:8083

2. Update API endpoints if services are on different ports

3. Run the app and test authentication flow

4. Test asset management features

## Support

For issues or questions:
- Check Flutter documentation: https://flutter.dev/docs
- Review EDGE_CASES.md for handled edge cases
- Check service logs if API calls fail

