import 'package:dio/dio.dart';
import 'package:keeply_app/core/config/app_config.dart';
import 'package:keeply_app/core/network/api_client.dart';
import 'package:keeply_app/core/network/dio_error_util.dart';
import 'package:keeply_app/features/auth/data/models/auth_models.dart';

/// Authentication Remote Data Source
/// Handles all authentication API calls with proper error handling
class AuthRemoteDataSource {
  final ApiClient _apiClient = ApiClient();

  /// Register a new user (`POST .../register` only). Does not obtain tokens; caller should navigate to login.
  Future<void> register(RegisterRequest request) async {
    try {
      final response = await _apiClient.dio.post(
        '${AppConfig.authServiceBaseUrl}${AppConfig.authBasePath}/register',
        data: request.toJson(),
      );

      if (response.statusCode == 200) {
        await _apiClient.clearTokens();
        return;
      }

      throw Exception('Registration failed');
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  /// SMS OTP for login (React `sendLoginOtp`).
  Future<Map<String, dynamic>> sendLoginOtp(String mobileDigits) async {
    try {
      final response = await _apiClient.dio.post(
        '${AppConfig.authServiceBaseUrl}${AppConfig.authBasePath}/otp/send',
        data: {
          'type': 'SMS',
          'mobile': mobileDigits,
          'purpose': 'LOGIN',
        },
      );
      if (response.statusCode == 200 && response.data is Map<String, dynamic>) {
        return response.data as Map<String, dynamic>;
      }
      throw Exception('Send OTP failed');
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  /// Login with various methods
  Future<AuthResponse> login(LoginRequest request) async {
    try {
      final body = Map<String, dynamic>.from(request.toJson());
      body.putIfAbsent('deviceInfo', () => 'Keeply Flutter');

      final response = await _apiClient.dio.post(
        '${AppConfig.authServiceBaseUrl}${AppConfig.authBasePath}/login',
        data: body,
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

  /// Current user profile (React `fetchMyProfile` — `GET /api/auth/profile/me`).
  ///
  /// [accessTokenOverride] — use right after login/refresh on web so this call
  /// does not rely on secure storage being readable in the same turn as [saveTokens].
  Future<UserDto> getCurrentUser({String? accessTokenOverride}) async {
    final url =
        '${AppConfig.authServiceBaseUrl}${AppConfig.authBasePath}/profile/me';
    try {
      final response = await _apiClient.dio.get(
        url,
        options: (accessTokenOverride != null &&
                accessTokenOverride.trim().isNotEmpty)
            ? Options(
                headers: {'Authorization': 'Bearer $accessTokenOverride'},
              )
            : null,
      );

      if (response.statusCode == 200 && response.data != null) {
        return UserDto.fromJson(response.data as Map<String, dynamic>);
      }

      throw Exception('Failed to get user profile');
    } on DioException catch (e) {
      throw _handleError(e);
    } catch (e) {
      throw Exception(e.toString());
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

  /// Revoke session on server when possible, then clear local tokens (React `logoutOnServer`).
  Future<void> logout() async {
    try {
      final access = await _apiClient.getAccessToken();
      if (access != null && access.trim().isNotEmpty) {
        await _apiClient.dio.post('${AppConfig.authServiceBaseUrl}${AppConfig.authBasePath}/logout');
      } else {
        final rt = await _apiClient.getRefreshToken();
        if (rt != null && rt.trim().isNotEmpty) {
          await _apiClient.dio.post(
            '${AppConfig.authServiceBaseUrl}${AppConfig.authBasePath}/logout',
            data: {'refreshToken': rt.trim()},
          );
        }
      }
    } catch (_) {
      /* best-effort */
    }
    await _apiClient.clearTokens();
  }

  /// Handle API errors
  Exception _handleError(DioException e) {
    if (e.response != null) {
      final message =
          pickMessageFromResponseData(e.response?.data) ?? 'An error occurred';
      return Exception(message);
    }
    return Exception(describeDioException(e));
  }
}

