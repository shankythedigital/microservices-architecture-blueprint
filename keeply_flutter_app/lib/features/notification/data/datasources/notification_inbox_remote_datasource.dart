import 'package:keeply_app/core/api/keeply_notifications_api.dart';

/// Maps `keeply_react_app/src/api/notificationsApi.ts` `notificationCount` (full API: [KeeplyNotificationsApi]).
class NotificationInboxRemoteDataSource {
  NotificationInboxRemoteDataSource({KeeplyNotificationsApi? api}) : _api = api ?? KeeplyNotificationsApi();

  final KeeplyNotificationsApi _api;

  Future<int> notificationCount() => _api.notificationCount();

  /// Expose full React parity API on the same datasource.
  KeeplyNotificationsApi get client => _api;
}
