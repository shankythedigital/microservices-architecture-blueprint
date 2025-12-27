# Keeply Flutter App

A comprehensive Flutter mobile application for asset management integrated with microservices architecture.

## Features

### Authentication
- Multiple login methods (Password, OTP, MPIN, RSA, WebAuthn)
- User registration
- Token refresh
- Biometric authentication
- Password change and reset
- Session management

### Asset Management
- Create, read, update, delete assets
- Master data management (Categories, Makes, Models, etc.)
- Bulk operations (JSON and Excel upload)
- Asset search and filtering
- Pagination support

### Notifications
- Multi-channel notifications (SMS, Email, WhatsApp, In-App)
- Template-based notifications
- Notification history

### Edge Cases Handled
- Network connectivity checks
- Input validation
- Error handling and retry logic
- Token expiration and refresh
- Offline mode support
- File upload validation
- Pagination edge cases
- Concurrent request handling

## Project Structure

```
lib/
├── core/
│   ├── config/          # App configuration
│   ├── exceptions/      # Custom exceptions
│   ├── network/         # API client and interceptors
│   └── utils/           # Utilities and helpers
├── features/
│   ├── auth/            # Authentication feature
│   │   ├── data/
│   │   │   ├── datasources/
│   │   │   └── models/
│   │   └── presentation/
│   │       ├── bloc/
│   │       └── pages/
│   ├── asset/           # Asset management feature
│   │   ├── data/
│   │   └── presentation/
│   └── notification/    # Notification feature
│       ├── data/
│       └── presentation/
└── main.dart            # App entry point
```

## Setup

### Prerequisites
- Flutter SDK 3.0.0 or higher
- Dart SDK 3.0.0 or higher
- Android Studio / VS Code with Flutter extensions

### Installation

1. **Clone and navigate to the project:**
   ```bash
   cd keeply_flutter_app
   ```

2. **Install dependencies:**
   ```bash
   flutter pub get
   ```

3. **Generate code (for JSON serialization, etc.):**
   ```bash
   flutter pub run build_runner build --delete-conflicting-outputs
   ```

4. **Update API endpoints in `lib/core/config/app_config.dart`:**
   ```dart
   static const String authServiceBaseUrl = 'http://your-auth-service-url';
   static const String notificationServiceBaseUrl = 'http://your-notification-service-url';
   static const String assetServiceBaseUrl = 'http://your-asset-service-url';
   ```

5. **Run the app:**
   ```bash
   flutter run
   ```

## Configuration

### API Endpoints
Update base URLs in `lib/core/config/app_config.dart`:
- Auth Service: Default `http://localhost:8081`
- Notification Service: Default `http://localhost:8082`
- Asset Service: Default `http://localhost:8083`

### Environment Variables
For production, use environment variables or a config file:
```dart
// Example: Use different URLs for production
static const String authServiceBaseUrl = 
    kDebugMode ? 'http://localhost:8081' : 'https://api.production.com/auth';
```

## Architecture

### State Management
- **BLoC Pattern**: Used for state management
- **Equatable**: For value equality in states and events

### Network Layer
- **Dio**: HTTP client
- **Interceptors**: Auth, error handling, retry, logging
- **Automatic token refresh**: Handles 401 errors

### Error Handling
- **Custom exceptions**: `ApiException` with types
- **User-friendly messages**: Transformed error messages
- **Retry logic**: Automatic retry for network errors

### Edge Cases Handled

1. **Network Issues**
   - Connectivity checks before requests
   - Retry with exponential backoff
   - Offline mode detection

2. **Authentication**
   - Token expiration handling
   - Automatic token refresh
   - Multiple login methods validation

3. **Input Validation**
   - Real-time validation
   - Format checking (email, mobile, OTP, MPIN)
   - Length constraints
   - Pattern matching

4. **File Operations**
   - File size validation
   - File type validation
   - Upload progress tracking
   - Error recovery

5. **Pagination**
   - Infinite scroll
   - Page size limits
   - Empty state handling

6. **Concurrent Operations**
   - Request deduplication
   - Loading state management
   - Error state isolation

## Testing

### Unit Tests
```bash
flutter test
```

### Integration Tests
```bash
flutter test integration_test/
```

## Building

### Android APK
```bash
flutter build apk --release
```

### iOS IPA
```bash
flutter build ios --release
```

## Troubleshooting

### Common Issues

1. **Network errors**: Check API endpoints and connectivity
2. **Token expiration**: App automatically refreshes tokens
3. **Build errors**: Run `flutter clean` and `flutter pub get`
4. **Code generation**: Run `flutter pub run build_runner build`

## Security Considerations

- Tokens stored in secure storage
- HTTPS required in production
- Input sanitization
- XSS prevention
- Secure file handling

## License

This project is part of the microservices architecture blueprint.

