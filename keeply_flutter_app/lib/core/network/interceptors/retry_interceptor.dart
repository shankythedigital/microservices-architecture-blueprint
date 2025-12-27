import 'package:dio/dio.dart';
import 'package:keeply_app/core/config/app_config.dart';
import 'package:keeply_app/core/utils/logger.dart';

/// Retry Interceptor
/// Automatically retries failed requests with exponential backoff
class RetryInterceptor extends Interceptor {
  @override
  void onError(DioException err, ErrorInterceptorHandler handler) async {
    // Don't retry on certain conditions
    if (_shouldNotRetry(err)) {
      return handler.next(err);
    }

    final retryCount = err.requestOptions.extra['retryCount'] ?? 0;
    
    if (retryCount >= AppConfig.maxRetries) {
      AppLogger.warning('Max retries reached for ${err.requestOptions.path}');
      return handler.next(err);
    }

    // Calculate delay with exponential backoff
    final delay = AppConfig.retryDelay * (retryCount + 1);
    
    AppLogger.info(
      'Retrying request ${err.requestOptions.path} (attempt ${retryCount + 1}/${AppConfig.maxRetries}) after ${delay.inSeconds}s',
    );

    await Future.delayed(delay);

    try {
      final response = await _retry(err.requestOptions);
      return handler.resolve(response);
    } catch (e) {
      // Update retry count
      err.requestOptions.extra['retryCount'] = retryCount + 1;
      return handler.next(err);
    }
  }

  bool _shouldNotRetry(DioException err) {
    // Don't retry on connection errors (likely CORS or backend not running)
    if (err.type == DioExceptionType.connectionError) {
      final error = err.error?.toString().toLowerCase() ?? '';
      if (error.contains('xmlhttprequest') || 
          error.contains('cors') ||
          error.contains('network')) {
        return true; // Don't retry connection errors
      }
    }

    // Don't retry on 4xx errors (except 408, 429)
    if (err.response?.statusCode != null) {
      final statusCode = err.response!.statusCode!;
      if (statusCode >= 400 && statusCode < 500) {
        if (statusCode != 408 && statusCode != 429) {
          return true;
        }
      }
    }

    // Don't retry on cancellation
    if (err.type == DioExceptionType.cancel) {
      return true;
    }

    return false;
  }

  Future<Response> _retry(RequestOptions requestOptions) async {
    // Create a new Dio instance with base configuration
    final dio = Dio(
      BaseOptions(
        baseUrl: requestOptions.baseUrl,
        connectTimeout: AppConfig.connectTimeout,
        receiveTimeout: AppConfig.receiveTimeout,
        sendTimeout: AppConfig.sendTimeout,
        headers: {
          ...requestOptions.headers,
        },
      ),
    );

    final options = Options(
      method: requestOptions.method,
      headers: requestOptions.headers,
      contentType: requestOptions.contentType,
      responseType: requestOptions.responseType,
      followRedirects: requestOptions.followRedirects,
      validateStatus: requestOptions.validateStatus,
      extra: requestOptions.extra,
    );

    return await dio.request(
      requestOptions.path,
      data: requestOptions.data,
      queryParameters: requestOptions.queryParameters,
      options: options,
    );
  }
}

