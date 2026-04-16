import 'package:flutter/foundation.dart';
import 'package:keeply_app/core/config/app_config.dart';
import 'package:url_launcher/url_launcher.dart';

/// Opens WhatsApp click-to-chat when [AppConfig.whatsappSupportE164] is configured.
Future<bool> openWhatsAppSupport() async {
  final raw = AppConfig.whatsappSupportE164.replaceAll(RegExp(r'[^\d]'), '');
  if (raw.isEmpty) {
    if (kDebugMode) {
      debugPrint('WHATSAPP_SUPPORT_E164 is not set — add --dart-define=WHATSAPP_SUPPORT_E164=15551234567');
    }
    return false;
  }
  final uri = Uri.parse('https://wa.me/$raw');
  if (await canLaunchUrl(uri)) {
    return launchUrl(uri, mode: LaunchMode.externalApplication);
  }
  return false;
}
