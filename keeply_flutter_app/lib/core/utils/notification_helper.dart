import 'package:keeply_app/core/utils/logger.dart';
import 'package:keeply_app/features/notification/data/datasources/notification_remote_datasource.dart';
import 'package:keeply_app/features/notification/data/models/notification_models.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';

/// Notification Helper
/// Manages local and remote notifications
class NotificationHelper {
  static final NotificationHelper _instance = NotificationHelper._internal();
  factory NotificationHelper() => _instance;
  NotificationHelper._internal();

  final NotificationRemoteDataSource _notificationDataSource =
      NotificationRemoteDataSource();
  final FlutterLocalNotificationsPlugin _localNotifications =
      FlutterLocalNotificationsPlugin();

  bool _initialized = false;

  /// Initialize local notifications
  Future<void> initialize() async {
    if (_initialized) return;

    try {
      const androidSettings = AndroidInitializationSettings('@mipmap/ic_launcher');
      const iosSettings = DarwinInitializationSettings();
      const initSettings = InitializationSettings(
        android: androidSettings,
        iOS: iosSettings,
      );

      await _localNotifications.initialize(
        initSettings,
        onDidReceiveNotificationResponse: _onNotificationTapped,
      );

      _initialized = true;
      AppLogger.info('Local notifications initialized');
    } catch (e) {
      AppLogger.error('Local notifications initialization failed: $e');
    }
  }

  /// Send SMS notification
  Future<bool> sendSms({
    required String mobile,
    required String templateCode,
    Map<String, String>? variables,
  }) async {
    try {
      await _notificationDataSource.sendSms(
        mobile: mobile,
        templateCode: templateCode,
        variables: variables,
      );
      return true;
    } catch (e) {
      AppLogger.error('Send SMS failed: $e');
      return false;
    }
  }

  /// Send Email notification
  Future<bool> sendEmail({
    required String email,
    required String templateCode,
    Map<String, String>? variables,
  }) async {
    try {
      await _notificationDataSource.sendEmail(
        email: email,
        templateCode: templateCode,
        variables: variables,
      );
      return true;
    } catch (e) {
      AppLogger.error('Send Email failed: $e');
      return false;
    }
  }

  /// Send WhatsApp notification
  Future<bool> sendWhatsApp({
    required String mobile,
    required String templateCode,
    Map<String, String>? variables,
  }) async {
    try {
      await _notificationDataSource.sendWhatsApp(
        mobile: mobile,
        templateCode: templateCode,
        variables: variables,
      );
      return true;
    } catch (e) {
      AppLogger.error('Send WhatsApp failed: $e');
      return false;
    }
  }

  /// Send In-App notification
  Future<bool> sendInApp({
    required String userId,
    required String templateCode,
    Map<String, String>? variables,
  }) async {
    try {
      await _notificationDataSource.sendInApp(
        userId: userId,
        templateCode: templateCode,
        variables: variables,
      );
      return true;
    } catch (e) {
      AppLogger.error('Send In-App notification failed: $e');
      return false;
    }
  }

  /// Show local notification
  Future<void> showLocalNotification({
    required int id,
    required String title,
    required String body,
    String? payload,
  }) async {
    try {
      const androidDetails = AndroidNotificationDetails(
        'keeply_channel',
        'Keeply Notifications',
        channelDescription: 'Notifications for Keeply app',
        importance: Importance.high,
        priority: Priority.high,
      );

      const iosDetails = DarwinNotificationDetails();

      const notificationDetails = NotificationDetails(
        android: androidDetails,
        iOS: iosDetails,
      );

      await _localNotifications.show(
        id,
        title,
        body,
        notificationDetails,
        payload: payload,
      );
    } catch (e) {
      AppLogger.error('Show local notification failed: $e');
    }
  }

  /// Handle notification tap
  void _onNotificationTapped(NotificationResponse response) {
    AppLogger.info('Notification tapped: ${response.payload}');
    // Handle notification tap - navigate to relevant screen
  }

  /// Cancel notification
  Future<void> cancelNotification(int id) async {
    await _localNotifications.cancel(id);
  }

  /// Cancel all notifications
  Future<void> cancelAllNotifications() async {
    await _localNotifications.cancelAll();
  }
}

