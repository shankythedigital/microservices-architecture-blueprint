import 'dart:io';

import 'package:keeply_app/core/config/app_config.dart';
import 'package:keeply_app/core/utils/logger.dart';

/// Opens a short TCP connection to the configured auth service (default port from URL).
Future<void> probeAuthServiceTcp() async {
  final uri = Uri.parse(AppConfig.authServiceBaseUrl);
  if (uri.host.isEmpty) return;
  final scheme = uri.scheme.toLowerCase();
  if (scheme != 'http' && scheme != 'https') return;
  final port = uri.hasPort ? uri.port : (scheme == 'https' ? 443 : 80);

  try {
    final socket = await Socket.connect(uri.host, port, timeout: const Duration(seconds: 2));
    await socket.close();
  } catch (e) {
    AppLogger.warning(
      'Debug reachability: could not connect to auth API at ${uri.host}:$port '
      '(from ${AppConfig.authServiceBaseUrl}). Start auth-service on your dev machine; '
      'Android emulator uses 10.0.2.2 to reach the host. Error: $e',
    );
  }
}
