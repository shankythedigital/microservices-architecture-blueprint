import 'package:keeply_app/core/config/app_config.dart';

/// Microservice keys matching React `config.ts` `ServiceName` / `url(service, path)`.
enum KeeplyApiService {
  auth,
  asset,
  notification,
  helpdesk,
}

/// Full URL for a microservice path (same contract as React `url('asset', '/api/asset/v1/...')`).
String keeplyApiUrl(KeeplyApiService service, String path) {
  final p = path.startsWith('/') ? path : '/$path';
  switch (service) {
    case KeeplyApiService.auth:
      return '${AppConfig.authServiceBaseUrl.replaceAll(RegExp(r'/$'), '')}$p';
    case KeeplyApiService.asset:
      return '${AppConfig.assetServiceBaseUrl.replaceAll(RegExp(r'/$'), '')}$p';
    case KeeplyApiService.notification:
      return '${AppConfig.notificationServiceBaseUrl.replaceAll(RegExp(r'/$'), '')}$p';
    case KeeplyApiService.helpdesk:
      return '${AppConfig.helpdeskServiceBaseUrl.replaceAll(RegExp(r'/$'), '')}$p';
  }
}
