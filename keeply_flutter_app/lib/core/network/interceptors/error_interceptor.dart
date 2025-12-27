import 'package:dio/dio.dart';
import 'package:keeply_app/core/exceptions/api_exception.dart';
import 'package:keeply_app/core/utils/logger.dart';

/// Error Interceptor
/// Transforms HTTP errors into custom exceptions with proper error messages
class ErrorInterceptor extends Interceptor {
  @override
  void onError(DioException err, ErrorInterceptorHandler handler) {
    AppLogger.error('API Error: ${err.message}', error: err);

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
        // Enhanced connection error handling
        String errorMessage = 'Connection failed. ';
        final error = err.error;
        
        if (error != null) {
          final errorString = error.toString().toLowerCase();
          
          if (errorString.contains('xmlhttprequest') || 
              errorString.contains('cors') ||
              errorString.contains('network')) {
            errorMessage += 'Please ensure:\n'
                '1. Backend services are running\n'
                '2. CORS is enabled on the server\n'
                '3. Using correct API endpoint (not localhost for web)\n'
                '4. Network connectivity is available';
          } else if (errorString.contains('localhost') || 
                     errorString.contains('127.0.0.1')) {
            errorMessage += 'Cannot connect to localhost. '
                'For web platform, use your machine\'s IP address or hostname instead of localhost.';
          } else {
            errorMessage += 'Network error: ${err.message ?? 'Unknown error'}';
          }
        } else {
          errorMessage += 'Please check your network connection and ensure backend services are running.';
        }
        
        apiException = ApiException(
          message: errorMessage,
          type: ApiExceptionType.network,
          statusCode: err.response?.statusCode,
        );
        break;

      default:
        apiException = ApiException(
          message: 'An unexpected error occurred. Please try again.',
          statusCode: err.response?.statusCode,
          type: ApiExceptionType.unknown,
        );
    }

    handler.reject(
      DioException(
        requestOptions: err.requestOptions,
        response: err.response,
        type: err.type,
        error: apiException,
      ),
    );
  }

  ApiException _handleResponseError(DioException err) {
    final statusCode = err.response?.statusCode;
    final data = err.response?.data;

    String message = 'An error occurred';
    
    // Try to extract error message from response
    if (data is Map<String, dynamic>) {
      message = data['message'] ?? 
                data['error'] ?? 
                data['errorMessage'] ?? 
                message;
    } else if (data is String) {
      message = data;
    }

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
