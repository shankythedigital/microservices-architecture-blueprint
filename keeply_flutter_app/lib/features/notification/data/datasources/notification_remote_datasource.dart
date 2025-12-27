import 'package:dio/dio.dart';
import 'package:keeply_app/core/config/app_config.dart';
import 'package:keeply_app/core/network/api_client.dart';
import 'package:keeply_app/core/exceptions/api_exception.dart';
import 'package:keeply_app/features/notification/data/models/notification_models.dart';

/// Notification Remote Data Source
class NotificationRemoteDataSource {
  final ApiClient _apiClient = ApiClient();

  /// Send notification
  Future<NotificationResponse> sendNotification(NotificationRequest request) async {
    try {
      final response = await _apiClient.dio.post(
        '${AppConfig.notificationServiceBaseUrl}${AppConfig.notificationBasePath}',
        data: request.toJson(),
      );

      if (response.statusCode == 202 || response.statusCode == 200) {
        // Notification accepted
        return NotificationResponse(
          status: 'QUEUED',
          channel: request.channel,
          recipient: request.recipient,
          templateCode: request.templateCode,
          queuedAt: DateTime.now(),
        );
      }

      throw ApiException(
        message: 'Failed to send notification',
        type: ApiExceptionType.server,
      );
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  /// Send SMS notification
  Future<NotificationResponse> sendSms({
    required String mobile,
    required String templateCode,
    Map<String, String>? variables,
  }) async {
    return sendNotification(NotificationRequest(
      channel: 'SMS',
      templateCode: templateCode,
      recipient: mobile,
      variables: variables,
    ));
  }

  /// Send Email notification
  Future<NotificationResponse> sendEmail({
    required String email,
    required String templateCode,
    Map<String, String>? variables,
  }) async {
    return sendNotification(NotificationRequest(
      channel: 'EMAIL',
      templateCode: templateCode,
      recipient: email,
      variables: variables,
    ));
  }

  /// Send WhatsApp notification
  Future<NotificationResponse> sendWhatsApp({
    required String mobile,
    required String templateCode,
    Map<String, String>? variables,
  }) async {
    return sendNotification(NotificationRequest(
      channel: 'WHATSAPP',
      templateCode: templateCode,
      recipient: mobile,
      variables: variables,
    ));
  }

  /// Send In-App notification
  Future<NotificationResponse> sendInApp({
    required String userId,
    required String templateCode,
    Map<String, String>? variables,
  }) async {
    return sendNotification(NotificationRequest(
      channel: 'INAPP',
      templateCode: templateCode,
      recipient: userId,
      variables: variables,
    ));
  }

  ApiException _handleError(DioException e) {
    if (e.response != null) {
      final statusCode = e.response!.statusCode;
      final data = e.response!.data;

      String message = 'Failed to send notification';
      if (data is Map<String, dynamic>) {
        message = data['message'] ?? message;
      }

      ApiExceptionType type;
      switch (statusCode) {
        case 400:
          type = ApiExceptionType.badRequest;
          break;
        case 401:
          type = ApiExceptionType.unauthorized;
          break;
        case 500:
          type = ApiExceptionType.server;
          break;
        default:
          type = ApiExceptionType.unknown;
      }

      return ApiException(
        message: message,
        statusCode: statusCode,
        type: type,
      );
    }

    return ApiException(
      message: e.message ?? 'Network error',
      type: ApiExceptionType.network,
    );
  }
}

