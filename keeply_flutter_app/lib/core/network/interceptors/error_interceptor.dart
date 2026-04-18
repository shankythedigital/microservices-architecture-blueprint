import 'package:dio/dio.dart';
import 'package:keeply_app/core/exceptions/api_exception.dart';
import 'package:keeply_app/core/network/dio_error_util.dart';
import 'package:keeply_app/core/utils/logger.dart';

/// Error Interceptor
/// Transforms HTTP errors into custom exceptions with proper error messages
class ErrorInterceptor extends Interceptor {
  @override
  void onError(DioException err, ErrorInterceptorHandler handler) {
    AppLogger.error('API Error: ${describeDioException(err)}', error: err);

    ApiException apiException;

    switch (err.type) {
      case DioExceptionType.connectionTimeout:
      case DioExceptionType.sendTimeout:
      case DioExceptionType.receiveTimeout:
        apiException = ApiException(
          message: 'Connection timeout. Please check your internet connection.',
          statusCode: err.response?.statusCode,
          type: ApiExceptionType.timeout,
        );
        break;

      case DioExceptionType.badResponse:
        apiException = _handleResponseError(err);
        break;

      case DioExceptionType.cancel:
        apiException = ApiException(
          message: 'Request cancelled',
          type: ApiExceptionType.cancelled,
        );
        break;

      case DioExceptionType.connectionError:
        apiException = ApiException(
          message: describeDioException(err),
          type: ApiExceptionType.network,
          statusCode: err.response?.statusCode,
        );
        break;

      case DioExceptionType.badCertificate:
        apiException = ApiException(
          message: describeDioException(err),
          type: ApiExceptionType.network,
          statusCode: err.response?.statusCode,
        );
        break;

      case DioExceptionType.unknown:
        if (err.response != null && err.response!.statusCode != null) {
          apiException = _handleResponseError(err);
        } else {
          apiException = ApiException(
            message: describeDioException(err),
            type: ApiExceptionType.unknown,
            statusCode: err.response?.statusCode,
          );
        }
        break;
    }

    handler.reject(
      DioException(
        requestOptions: err.requestOptions,
        response: err.response,
        type: err.type,
        error: apiException,
        message: apiException.message,
      ),
    );
  }

  ApiException _handleResponseError(DioException err) {
    final statusCode = err.response?.statusCode;
    final data = err.response?.data;

    String message =
        pickMessageFromResponseData(data) ?? 'An error occurred';

    ApiExceptionType type;
    switch (statusCode) {
      case 400:
        type = ApiExceptionType.badRequest;
        message = message.isEmpty ? 'Invalid request. Please check your input.' : message;
        break;
      case 401:
        type = ApiExceptionType.unauthorized;
        message = message.isEmpty ? 'Authentication failed. Please login again.' : message;
        break;
      case 403:
        type = ApiExceptionType.forbidden;
        message = message.isEmpty ? 'You do not have permission to perform this action.' : message;
        break;
      case 404:
        type = ApiExceptionType.notFound;
        message = message.isEmpty ? 'Resource not found.' : message;
        break;
      case 409:
        type = ApiExceptionType.conflict;
        message = message.isEmpty ? 'Resource already exists.' : message;
        break;
      case 422:
        type = ApiExceptionType.validation;
        message = message.isEmpty ? 'Validation failed. Please check your input.' : message;
        break;
      case 429:
        type = ApiExceptionType.rateLimit;
        message = message.isEmpty ? 'Too many requests. Please try again later.' : message;
        break;
      case 500:
      case 502:
      case 503:
        type = ApiExceptionType.server;
        message = message.isEmpty ? 'Server error. Please try again later.' : message;
        break;
      default:
        type = ApiExceptionType.unknown;
    }

    return ApiException(
      message: message,
      statusCode: statusCode,
      type: type,
      data: data,
    );
  }
}
