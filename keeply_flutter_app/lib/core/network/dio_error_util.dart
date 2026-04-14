import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart' show kIsWeb;
import 'package:keeply_app/core/exceptions/api_exception.dart';

bool _isBlankOrLiteralNull(String s) {
  final t = s.trim();
  return t.isEmpty || t.toLowerCase() == 'null';
}

/// True when [o] looks like a browser/XHR failure (Flutter web maps these to [DioExceptionType.unknown]).
bool isWebBrowserOrTransportFailure(Object? o) {
  if (o == null) return false;
  final s = o.toString().toLowerCase();
  return s.contains('operationerror') ||
      s.contains('operation error') ||
      s.contains('progression') ||
      s.contains('progress event') ||
      s.contains('xmlhttprequest') ||
      s.contains('xhr') ||
      s.contains('failed to fetch') ||
      s.contains('networkerror') ||
      s.contains('network request failed') ||
      s.contains('load failed') ||
      s.contains('connection refused');
}

/// Short guidance for CORS / unreachable API (web + same symptoms on some emulators).
String keeplyWebNetworkAndCorsHelp() {
  return 'Could not reach the API from this browser (common causes: CORS, wrong URL, or service not running). '
      'Run all Keeply microservices; ensure CORS uses KeeplyCorsConfiguration (allowed origins include localhost '
      'and typical LAN patterns). For Flutter web on another machine, set '
      '--dart-define=AUTH_SERVICE_URL=http://<host-ip>:8081 (and matching URLs for other ports).';
}

/// Human-readable type label (matches Dio’s default style without importing private extensions).
String dioExceptionTypeLabel(DioExceptionType type) {
  switch (type) {
    case DioExceptionType.connectionTimeout:
      return 'connection timeout';
    case DioExceptionType.sendTimeout:
      return 'send timeout';
    case DioExceptionType.receiveTimeout:
      return 'receive timeout';
    case DioExceptionType.badCertificate:
      return 'bad certificate';
    case DioExceptionType.badResponse:
      return 'bad response';
    case DioExceptionType.cancel:
      return 'cancel';
    case DioExceptionType.connectionError:
      return 'connection error';
    case DioExceptionType.unknown:
      return 'unknown';
  }
}

/// Installs a global [DioException.readableStringBuilder] so logs / `toString()` never show
/// `DioException [unknown]: null` (Dio’s default prints [message] which is often null on web).
void installKeeplyDioExceptionReadableString() {
  DioException.readableStringBuilder = keeplyDioExceptionReadableString;
}

/// Same shape as Dio’s default builder, but the summary line uses [describeDioException].
String keeplyDioExceptionReadableString(DioException e) {
  final summary = describeDioException(e);
  final buffer = StringBuffer(
    'DioException [${dioExceptionTypeLabel(e.type)}]: $summary',
  );
  final err = e.error;
  // Avoid duplicating opaque "OperationError" after we already expanded it in [summary].
  if (err != null &&
      err is! ApiException &&
      !isWebBrowserOrTransportFailure(err)) {
    buffer.writeln();
    buffer.write('Error: $err');
  }
  return buffer.toString();
}

/// Reads common API error fields from a response body (JSON map or plain text).
String? pickMessageFromResponseData(dynamic data) {
  if (data == null) return null;
  if (data is String) {
    final s = data.trim();
    return _isBlankOrLiteralNull(s) ? null : s;
  }
  if (data is Map) {
    for (final key in const [
      'message',
      'error',
      'errorMessage',
      'detail',
      'title',
    ]) {
      final v = data[key];
      if (v == null) continue;
      if (v is String) {
        final t = v.trim();
        if (!_isBlankOrLiteralNull(t)) return t;
      } else {
        final t = v.toString().trim();
        if (!_isBlankOrLiteralNull(t)) return t;
      }
    }
  }
  return null;
}

/// Non-null, user-safe description for any [DioException] (fixes `[unknown]: null` / OperationError on web).
String describeDioException(DioException e, {int unwrapDepth = 0}) {
  if (unwrapDepth > 6) {
    return 'Network request failed (nested errors). Please try again.';
  }

  // Inner DioException (e.g. retry / nested client) — describe the leaf failure.
  final innerDio = e.error;
  if (innerDio is DioException) {
    return describeDioException(innerDio, unwrapDepth: unwrapDepth + 1);
  }

  final nested = e.error;
  if (nested is ApiException) {
    final m = nested.message.trim();
    if (!_isBlankOrLiteralNull(m)) return m;
  }

  if (nested is String) {
    final t = nested.trim();
    if (!_isBlankOrLiteralNull(t)) return t;
  }

  // Flutter web: XHR failures surface as unknown + OperationError / similar with null message.
  if (kIsWeb &&
      (e.type == DioExceptionType.unknown ||
          e.type == DioExceptionType.connectionError) &&
      e.response == null &&
      isWebBrowserOrTransportFailure(nested)) {
    return keeplyWebNetworkAndCorsHelp();
  }

  final fromDio = e.message?.trim() ?? '';
  if (!_isBlankOrLiteralNull(fromDio)) return fromDio;

  if (nested != null) {
    final s = nested.toString().trim();
    if (!_isBlankOrLiteralNull(s) &&
        s != 'Instance of \'Object\'' &&
        s != 'Instance of Object') {
      // If it’s a raw browser error string, still prefer CORS/help text on web.
      if (kIsWeb &&
          e.response == null &&
          isWebBrowserOrTransportFailure(nested)) {
        return keeplyWebNetworkAndCorsHelp();
      }
      return s;
    }
  }

  final fromBody = pickMessageFromResponseData(e.response?.data);
  if (fromBody != null && !_isBlankOrLiteralNull(fromBody)) return fromBody;

  switch (e.type) {
    case DioExceptionType.connectionTimeout:
    case DioExceptionType.sendTimeout:
    case DioExceptionType.receiveTimeout:
      return 'The request timed out. Please check your connection and try again.';
    case DioExceptionType.badResponse:
      final c = e.response?.statusCode;
      return c != null
          ? 'The server returned an error ($c). Please try again.'
          : 'The server returned an invalid response.';
    case DioExceptionType.cancel:
      return 'The request was cancelled.';
    case DioExceptionType.connectionError:
      if (kIsWeb && e.response == null) {
        return keeplyWebNetworkAndCorsHelp();
      }
      return 'Could not connect to the server. Check that it is running and that you are online.';
    case DioExceptionType.badCertificate:
      return 'A secure connection could not be established.';
    case DioExceptionType.unknown:
      if (kIsWeb && e.response == null) {
        return keeplyWebNetworkAndCorsHelp();
      }
      return 'Something went wrong with the network request. Please try again.';
  }
}
