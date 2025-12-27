/// API Exception Types
enum ApiExceptionType {
  network,
  timeout,
  badRequest,
  unauthorized,
  forbidden,
  notFound,
  conflict,
  validation,
  rateLimit,
  server,
  cancelled,
  unknown,
}

/// Custom API Exception
/// Provides structured error information from API responses
class ApiException implements Exception {
  final String message;
  final ApiExceptionType type;
  final int? statusCode;
  final dynamic data;

  ApiException({
    required this.message,
    required this.type,
    this.statusCode,
    this.data,
  });

  /// Get user-friendly error message
  String get userMessage {
    switch (type) {
      case ApiExceptionType.network:
        return 'No internet connection. Please check your network.';
      case ApiExceptionType.timeout:
        return 'Request timeout. Please try again.';
      case ApiExceptionType.badRequest:
        return message.isNotEmpty ? message : 'Invalid request. Please check your input.';
      case ApiExceptionType.unauthorized:
        return 'Session expired. Please login again.';
      case ApiExceptionType.forbidden:
        return 'You do not have permission to perform this action.';
      case ApiExceptionType.notFound:
        return 'Resource not found.';
      case ApiExceptionType.conflict:
        return 'Resource already exists.';
      case ApiExceptionType.validation:
        return message.isNotEmpty ? message : 'Validation failed. Please check your input.';
      case ApiExceptionType.rateLimit:
        return 'Too many requests. Please try again later.';
      case ApiExceptionType.server:
        return 'Server error. Please try again later.';
      case ApiExceptionType.cancelled:
        return 'Request cancelled.';
      case ApiExceptionType.unknown:
        return message.isNotEmpty ? message : 'An unexpected error occurred.';
    }
  }

  /// Check if error is retryable
  bool get isRetryable {
    return type == ApiExceptionType.network ||
           type == ApiExceptionType.timeout ||
           type == ApiExceptionType.server ||
           statusCode == 408 ||
           statusCode == 429;
  }

  @override
  String toString() => 'ApiException: $message (Type: $type, Status: $statusCode)';
}

