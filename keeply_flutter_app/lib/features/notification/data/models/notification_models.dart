/// Notification Models

class NotificationRequest {
  final String channel; // SMS, EMAIL, WHATSAPP, INAPP
  final String templateCode;
  final String recipient;
  final Map<String, String>? variables;
  final String? priority; // LOW, NORMAL, HIGH, URGENT
  final Map<String, String>? metadata;

  NotificationRequest({
    required this.channel,
    required this.templateCode,
    required this.recipient,
    this.variables,
    this.priority,
    this.metadata,
  });

  Map<String, dynamic> toJson() => {
        'channel': channel,
        'templateCode': templateCode,
        'recipient': recipient,
        if (variables != null) 'variables': variables,
        if (priority != null) 'priority': priority,
        if (metadata != null) 'metadata': metadata,
      };
}

class NotificationResponse {
  final String? notificationId;
  final String? status;
  final String? channel;
  final String? recipient;
  final String? templateCode;
  final DateTime? queuedAt;

  NotificationResponse({
    this.notificationId,
    this.status,
    this.channel,
    this.recipient,
    this.templateCode,
    this.queuedAt,
  });

  factory NotificationResponse.fromJson(Map<String, dynamic> json) {
    return NotificationResponse(
      notificationId: json['notificationId'] as String?,
      status: json['status'] as String?,
      channel: json['channel'] as String?,
      recipient: json['recipient'] as String?,
      templateCode: json['templateCode'] as String?,
      queuedAt: json['queuedAt'] != null
          ? DateTime.parse(json['queuedAt'] as String)
          : null,
    );
  }
}

