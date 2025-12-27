import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:keeply_app/core/config/app_config.dart';
import 'package:keeply_app/core/network/interceptors/auth_interceptor.dart';
import 'package:keeply_app/core/network/interceptors/error_interceptor.dart';
import 'package:keeply_app/core/network/interceptors/logging_interceptor.dart';
import 'package:keeply_app/core/network/interceptors/retry_interceptor.dart';
import 'package:keeply_app/core/utils/logger.dart';

/// Centralized API Client
/// Handles all HTTP requests with interceptors for auth, error handling, retry logic
class ApiClient {
  static final ApiClient _instance = ApiClient._internal();
  factory ApiClient() => _instance;
  ApiClient._internal();

  late Dio _dio;
  final FlutterSecureStorage _secureStorage = const FlutterSecureStorage();

  Dio get dio => _dio;

  /// Initialize API Client with interceptors
  Future<void> initialize() async {
    _dio = Dio(
      BaseOptions(
        connectTimeout: AppConfig.connectTimeout,
        receiveTimeout: AppConfig.receiveTimeout,
        sendTimeout: AppConfig.sendTimeout,
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
        },
      ),
    );

    // Add interceptors in order
    _dio.interceptors.addAll([
      LoggingInterceptor(),
      AuthInterceptor(_secureStorage),
      RetryInterceptor(),
      ErrorInterceptor(),
    ]);

    AppLogger.info('API Client initialized');
  }

  /// Update base URL dynamically
  void updateBaseUrl(String baseUrl) {
    _dio.options.baseUrl = baseUrl;
    AppLogger.info('Base URL updated to: $baseUrl');
  }

  /// Clear all interceptors (useful for testing)
  void clearInterceptors() {
    _dio.interceptors.clear();
  }

  /// Get current access token
  Future<String?> getAccessToken() async {
    return await _secureStorage.read(key: 'access_token');
  }

  /// Get current refresh token
  Future<String?> getRefreshToken() async {
    return await _secureStorage.read(key: 'refresh_token');
  }

  /// Save tokens
  Future<void> saveTokens(String accessToken, String refreshToken) async {
    await Future.wait([
      _secureStorage.write(key: 'access_token', value: accessToken),
      _secureStorage.write(key: 'refresh_token', value: refreshToken),
    ]);
    AppLogger.info('Tokens saved');
  }

  /// Clear tokens
  Future<void> clearTokens() async {
    await Future.wait([
      _secureStorage.delete(key: 'access_token'),
      _secureStorage.delete(key: 'refresh_token'),
    ]);
    AppLogger.info('Tokens cleared');
  }

  /// Check if user is authenticated
  Future<bool> isAuthenticated() async {
    final token = await getAccessToken();
    return token != null && token.isNotEmpty;
  }
}
