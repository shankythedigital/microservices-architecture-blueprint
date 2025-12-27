import 'package:dio/dio.dart';
import 'package:keeply_app/core/config/app_config.dart';
import 'package:keeply_app/core/network/api_client.dart';
import 'package:keeply_app/features/auth/data/models/auth_models.dart';

/// Authentication Remote Data Source
/// Handles all authentication API calls with proper error handling
class AuthRemoteDataSource {
  final ApiClient _apiClient = ApiClient();

  /// Register a new user
  Future<AuthResponse> register(RegisterRequest request) async {
    try {
      final response = await _apiClient.dio.post(
        '${AppConfig.authServiceBaseUrl}${AppConfig.authBasePath}/register',
        data: request.toJson(),
      );

      if (response.statusCode == 200) {
        // Registration successful, now login
        return await login(LoginRequest(
          loginType: 'PASSWORD',
          username: request.username,
          password: request.password,
        ));
      }

      throw Exception('Registration failed');
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  /// Login with various methods
  Future<AuthResponse> login(LoginRequest request) async {
    try {
      final response = await _apiClient.dio.post(
        '${AppConfig.authServiceBaseUrl}${AppConfig.authBasePath}/login',
        data: request.toJson(),
      );

      if (response.statusCode == 200 && response.data != null) {
        final authResponse = AuthResponse.fromJson(response.data as Map<String, dynamic>);
        
        // Save tokens
        await _apiClient.saveTokens(
          authResponse.accessToken,
          authResponse.refreshToken,
        );

        return authResponse;
      }

      throw Exception('Login failed');
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  /// Refresh access token
  Future<AuthResponse> refreshToken(String refreshToken) async {
    try {
      final response = await _apiClient.dio.post(
        '${AppConfig.authServiceBaseUrl}${AppConfig.authBasePath}/refresh',
        queryParameters: {'refreshToken': refreshToken},
      );

      if (response.statusCode == 200 && response.data != null) {
        final authResponse = AuthResponse.fromJson(response.data as Map<String, dynamic>);
        
        // Save new tokens
        await _apiClient.saveTokens(
          authResponse.accessToken,
          authResponse.refreshToken,
        );

        return authResponse;
      }

      throw Exception('Token refresh failed');
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  /// Get current user profile
  Future<UserDto> getCurrentUser() async {
    try {
      final response = await _apiClient.dio.get(
        '${AppConfig.authServiceBaseUrl}/api/users/me',
      );

      if (response.statusCode == 200 && response.data != null) {
        return UserDto.fromJson(response.data as Map<String, dynamic>);
      }

      throw Exception('Failed to get user profile');
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  /// Get user by ID
  Future<UserDto> getUserById(int userId) async {
    try {
      final response = await _apiClient.dio.get(
        '${AppConfig.authServiceBaseUrl}/api/users/$userId',
      );

      if (response.statusCode == 200 && response.data != null) {
        return UserDto.fromJson(response.data as Map<String, dynamic>);
      }

      throw Exception('Failed to get user');
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  /// Send OTP
  Future<OtpResponse> sendOtp(OtpRequest request) async {
    try {
      final response = await _apiClient.dio.post(
        '${AppConfig.authServiceBaseUrl}${AppConfig.authBasePath}/otp/send',
        data: request.toJson(),
      );

      if (response.statusCode == 200 && response.data != null) {
        return OtpResponse.fromJson(response.data as Map<String, dynamic>);
      }

      throw Exception('Failed to send OTP');
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  /// Change password
  Future<void> changePassword({
    required int userId,
    required String currentPassword,
    required String newPassword,
  }) async {
    try {
      final response = await _apiClient.dio.post(
        '${AppConfig.authServiceBaseUrl}${AppConfig.authBasePath}/password/change',
        data: {
          'userId': userId,
          'currentPassword': currentPassword,
          'newPassword': newPassword,
        },
      );

      if (response.statusCode != 200) {
        throw Exception('Password change failed');
      }
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  /// Forgot password
  Future<void> forgotPassword({
    required String username,
    required String projectType,
  }) async {
    try {
      final response = await _apiClient.dio.post(
        '${AppConfig.authServiceBaseUrl}${AppConfig.authBasePath}/password/forgot',
        data: {
          'username': username,
          'projectType': projectType,
        },
      );

      if (response.statusCode != 200) {
        throw Exception('Failed to send password reset');
      }
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  /// Logout (clear tokens locally)
  Future<void> logout() async {
    await _apiClient.clearTokens();
  }

  /// Handle API errors
  Exception _handleError(DioException e) {
    if (e.response != null) {
      final message = e.response?.data?['message'] ?? 
                     e.response?.data?['error'] ?? 
                     'An error occurred';
      return Exception(message);
    }
    return Exception(e.message ?? 'Network error');
  }
}

