import 'package:flutter/foundation.dart' show kDebugMode, kIsWeb;

import 'dev_api_reachability_stub.dart'
    if (dart.library.io) 'dev_api_reachability_io.dart' as impl;

/// Debug-only: logs if the auth service port is not accepting connections. No UI or API contract change.
Future<void> logDevApiReachabilityHint() async {
  if (kIsWeb || !kDebugMode) return;
  await impl.probeAuthServiceTcp();
}
