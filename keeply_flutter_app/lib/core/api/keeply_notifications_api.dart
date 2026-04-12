import 'package:dio/dio.dart';
import 'package:keeply_app/core/api/keeply_api_models.dart';
import 'package:keeply_app/core/api/keeply_service_url.dart';
import 'package:keeply_app/core/exceptions/api_exception.dart';
import 'package:keeply_app/core/network/api_client.dart';

/// Parity with `keeply_react_app/src/api/notificationsApi.ts`.
class KeeplyNotificationsApi {
  KeeplyNotificationsApi({ApiClient? apiClient}) : _client = apiClient ?? ApiClient();

  final ApiClient _client;

  /// `GET /api/notifications/list` — optional `days` query (React `notificationList`).
  Future<List<NotificationItemDto>> notificationList({int? days}) async {
    final q = <String, dynamic>{};
    if (days != null) q['days'] = days;
    final response = await _client.dio.get(
      keeplyApiUrl(KeeplyApiService.notification, '/api/notifications/list'),
      queryParameters: q.isEmpty ? null : q,
    );
    final root = jsonMapOf(response.data);
    if (root['success'] == false) {
      throw ApiException(
        message: root['message'] as String? ?? 'Request failed',
        type: ApiExceptionType.badRequest,
      );
    }
    final raw = root['data'];
    if (raw is! List) return [];
    return raw
        .whereType<Map>()
        .map((e) => NotificationItemDto.fromJson(Map<String, dynamic>.from(e)))
        .toList();
  }

  /// `GET /api/notifications/count` (React `notificationCount`).
  Future<int> notificationCount() async {
    final response = await _client.dio.get(
      keeplyApiUrl(KeeplyApiService.notification, '/api/notifications/count'),
    );
    final root = jsonMapOf(response.data);
    if (root['success'] == false) return 0;
    final data = root['data'];
    if (data is Map<String, dynamic>) {
      return (data['count'] as num?)?.toInt() ?? 0;
    }
    return 0;
  }

  /// `PUT /api/notifications/read/{id}` (React `markNotificationRead`).
  Future<void> markNotificationRead(int id) async {
    final response = await _client.dio.put(
      keeplyApiUrl(KeeplyApiService.notification, '/api/notifications/read/$id'),
    );
    if (response.statusCode != null && response.statusCode! >= 200 && response.statusCode! < 300) return;
    _throwFromResponse(response);
  }

  /// `PUT /api/notifications/read-all` (React `markAllNotificationsRead`).
  Future<void> markAllNotificationsRead() async {
    final response = await _client.dio.put(
      keeplyApiUrl(KeeplyApiService.notification, '/api/notifications/read-all'),
    );
    if (response.statusCode != null && response.statusCode! >= 200 && response.statusCode! < 300) return;
    _throwFromResponse(response);
  }

  void _throwFromResponse(Response response) {
    final body = response.data;
    String msg = response.statusMessage ?? 'Request failed';
    if (body is Map) {
      msg = (body['error'] ?? body['message'] ?? msg).toString();
    }
    throw ApiException(message: msg, statusCode: response.statusCode, type: ApiExceptionType.badRequest);
  }
}
