import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:keeply_app/core/utils/logger.dart';

/// Authentication Interceptor
/// Automatically adds Bearer token to requests and handles token refresh
class AuthInterceptor extends Interceptor {
  final FlutterSecureStorage _secureStorage;

  AuthInterceptor(this._secureStorage);

  @override
  void onRequest(RequestOptions options, RequestInterceptorHandler handler) async {
    // Skip auth for public endpoints
    if (_isPublicEndpoint(options.path)) {
      return handler.next(options);
    }

    // Get access token
    final token = await _secureStorage.read(key: 'access_token');
    
    if (token != null && token.isNotEmpty) {
      options.headers['Authorization'] = 'Bearer $token';
      AppLogger.debug('Added Bearer token to request: ${options.path}');
    } else {
      AppLogger.warning('No token available for request: ${options.path}');
    }

    handler.next(options);
  }

  @override
  void onError(DioException err, ErrorInterceptorHandler handler) async {
    // Handle 401 Unauthorized - Token expired or invalid
    if (err.response?.statusCode == 401) {
      AppLogger.warning('Received 401, attempting token refresh');
      
      final refreshToken = await _secureStorage.read(key: 'refresh_token');
      
      if (refreshToken != null && refreshToken.isNotEmpty) {
        try {
          // Attempt to refresh token
          final dio = Dio();
          final response = await dio.post(
            '${err.requestOptions.baseUrl}/api/auth/refresh',
            queryParameters: {'refreshToken': refreshToken},
          );

          if (response.statusCode == 200 && response.data != null) {
            final newAccessToken = response.data['accessToken'];
            final newRefreshToken = response.data['refreshToken'] ?? refreshToken;

            // Save new tokens
            await _secureStorage.write(key: 'access_token', value: newAccessToken);
            await _secureStorage.write(key: 'refresh_token', value: newRefreshToken);

            // Retry original request with new token
            final opts = err.requestOptions;
            opts.headers['Authorization'] = 'Bearer $newAccessToken';
            
            final cloneReq = await dio.request(
              opts.path,
              options: Options(
                method: opts.method,
                headers: opts.headers,
              ),
              data: opts.data,
              queryParameters: opts.queryParameters,
            );

            return handler.resolve(cloneReq);
          }
        } catch (e) {
          AppLogger.error('Token refresh failed: $e');
          // Clear tokens on refresh failure
          await _secureStorage.delete(key: 'access_token');
          await _secureStorage.delete(key: 'refresh_token');
        }
      }
    }

    handler.next(err);
  }

  bool _isPublicEndpoint(String path) {
    final publicPaths = [
      '/api/auth/register',
      '/api/auth/login',
      '/api/auth/otp/send',
      '/swagger-ui',
      '/v3/api-docs',
    ];

    return publicPaths.any((publicPath) => path.contains(publicPath));
  }
}
