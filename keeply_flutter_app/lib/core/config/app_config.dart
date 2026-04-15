import 'package:flutter/foundation.dart' show kIsWeb, defaultTargetPlatform, TargetPlatform;

/// App Configuration
/// Centralized configuration for API endpoints and app settings.
///
/// Parity with React `keeply_react_app/src/config.ts`: per-service base URLs.
/// Map `VITE_AUTH_BASE` → `AUTH_SERVICE_URL`, `VITE_ASSET_BASE` → `ASSET_SERVICE_URL`,
/// `VITE_NOTIFICATION_BASE` → `NOTIFICATION_SERVICE_URL`, `VITE_HELPDESK_BASE` → `HELPDESK_SERVICE_URL`.
/// Prefer `keeplyApiUrl` / `keeplyServiceBase` from `core/api/keeply_service_url.dart` (React `url()` / `getServiceBase()`).
class AppConfig {
  // Detect platform and set appropriate base URLs
  static String get _defaultHost {
    if (kIsWeb) {
      // Flutter Web: localhost works when backend is on same machine
      // CORS: backends use shared KeeplyCorsConfiguration (common-service) via each *-service CorsConfig.
      return 'localhost';
    }
    if (defaultTargetPlatform == TargetPlatform.android) {
      // Android emulator: localhost refers to emulator itself
      // 10.0.2.2 is the special alias to host machine's loopback
      return '10.0.2.2';
    }
    // iOS simulator, macOS, Windows, Linux: localhost works
    return 'localhost';
  }

  // API Base URLs - Can be overridden via environment variables.
  // Flutter web from another machine / LAN: use full URLs, e.g.
  // `--dart-define=AUTH_SERVICE_URL=http://192.168.1.5:8081` (must match CORS allowed patterns on services).
  static String get authServiceBaseUrl {
    const envUrl = String.fromEnvironment('AUTH_SERVICE_URL');
    if (envUrl.isNotEmpty) return envUrl;
    return 'http://$_defaultHost:8081';
  }

  static String get notificationServiceBaseUrl {
    const envUrl = String.fromEnvironment('NOTIFICATION_SERVICE_URL');
    if (envUrl.isNotEmpty) return envUrl;
    return 'http://$_defaultHost:8082';
  }

  static String get assetServiceBaseUrl {
    const envUrl = String.fromEnvironment('ASSET_SERVICE_URL');
    if (envUrl.isNotEmpty) return envUrl;
    return 'http://$_defaultHost:8085';
  }

  static String get helpdeskServiceBaseUrl {
    const envUrl = String.fromEnvironment('HELPDESK_SERVICE_URL');
    if (envUrl.isNotEmpty) return envUrl;
    return 'http://$_defaultHost:8084';
  }

  // API Endpoints
  static const String authBasePath = '/api/auth';
  static const String notificationBasePath = '/api/notifications';
  static const String assetBasePath = '/api/asset/v1';
  static const String helpdeskBasePath = '/api/helpdesk';

  // Timeouts
  static const Duration connectTimeout = Duration(seconds: 30);
  static const Duration receiveTimeout = Duration(seconds: 30);
  static const Duration sendTimeout = Duration(seconds: 30);

  // Retry Configuration
  static const int maxRetries = 3;
  static const Duration retryDelay = Duration(seconds: 2);

  // Pagination
  static const int defaultPageSize = 20;
  static const int maxPageSize = 100;

  // File Upload
  static const int maxFileSize = 50 * 1024 * 1024; // 50MB
  static const List<String> allowedImageTypes = ['image/jpeg', 'image/png', 'image/jpg'];
  static const List<String> allowedDocumentTypes = [
    'application/pdf',
    'application/msword',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    'application/vnd.ms-excel',
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  ];

  // Cache Configuration
  static const Duration cacheExpiration = Duration(hours: 24);
  static const int maxCacheSize = 100 * 1024 * 1024; // 100MB

  // Security
  static const int tokenRefreshThreshold = 300; // Refresh 5 minutes before expiry
  static const int maxLoginAttempts = 5;
  static const Duration lockoutDuration = Duration(minutes: 15);

  // Validation
  static const int minPasswordLength = 8;
  static const int maxPasswordLength = 128;
  static const int minUsernameLength = 3;
  static const int maxUsernameLength = 50;

  // Notification
  static const Duration notificationTimeout = Duration(seconds: 10);
  static const int maxNotificationRetries = 3;

  // Project Types
  static const String defaultProjectType = 'ASSET';

  // Environment
  static bool get isProduction => const bool.fromEnvironment('dart.vm.product');
  static bool get isDevelopment => !isProduction;

  // Debug Mode
  static bool get isDebugMode => isDevelopment;
}

