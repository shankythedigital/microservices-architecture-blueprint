import 'package:dio/dio.dart';
import 'package:keeply_app/core/config/app_config.dart';
import 'package:keeply_app/core/utils/logger.dart';

/// Logging Interceptor
/// Logs all HTTP requests and responses for debugging
class LoggingInterceptor extends Interceptor {
  @override
  void onRequest(RequestOptions options, RequestInterceptorHandler handler) {
    if (AppConfig.isDebugMode) {
      AppLogger.debug(
        'REQUEST[${options.method}] => PATH: ${options.path}',
      );
      if (options.queryParameters.isNotEmpty) {
        AppLogger.debug('Query Parameters: ${options.queryParameters}');
      }
      if (options.data != null) {
        AppLogger.debug('Request Body: ${options.data}');
      }
    }
    handler.next(options);
  }

  @override
  void onResponse(Response response, ResponseInterceptorHandler handler) {
    if (AppConfig.isDebugMode) {
      AppLogger.debug(
        'RESPONSE[${response.statusCode}] => PATH: ${response.requestOptions.path}',
      );
      if (response.data != null) {
        AppLogger.debug('Response Data: ${response.data}');
      }
    }
    handler.next(response);
  }

  @override
  void onError(DioException err, ErrorInterceptorHandler handler) {
    if (AppConfig.isDebugMode) {
      AppLogger.error(
        'ERROR[${err.response?.statusCode}] => PATH: ${err.requestOptions.path}',
        error: err,
      );
      if (err.response?.data != null) {
        AppLogger.error('Error Data: ${err.response?.data}');
      }
    }
    handler.next(err);
  }
}
