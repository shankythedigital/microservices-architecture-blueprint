import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:keeply_app/core/utils/logger.dart';

/// Collapse [ConnectivityResult] list from `connectivity_plus` 6+ to one value
/// for UI that still expects a single [ConnectivityResult].
ConnectivityResult collapseConnectivityResults(List<ConnectivityResult> results) {
  if (results.isEmpty) {
    return ConnectivityResult.none;
  }
  if (results.every((r) => r == ConnectivityResult.none)) {
    return ConnectivityResult.none;
  }
  const priority = <ConnectivityResult>[
    ConnectivityResult.wifi,
    ConnectivityResult.ethernet,
    ConnectivityResult.mobile,
    ConnectivityResult.vpn,
    ConnectivityResult.other,
    ConnectivityResult.bluetooth,
  ];
  for (final p in priority) {
    if (results.contains(p)) return p;
  }
  return results.firstWhere(
    (r) => r != ConnectivityResult.none,
    orElse: () => ConnectivityResult.none,
  );
}

/// Connectivity Helper
/// Monitors network connectivity and provides status
class ConnectivityHelper {
  final Connectivity _connectivity = Connectivity();

  /// Check if device is connected to internet
  Future<bool> isConnected() async {
    try {
      final results = await _connectivity.checkConnectivity();
      return !results.every((r) => r == ConnectivityResult.none);
    } catch (e) {
      AppLogger.error('Connectivity check failed: $e');
      return false;
    }
  }

  /// Get current connectivity status
  Future<ConnectivityResult> getConnectivityStatus() async {
    try {
      final results = await _connectivity.checkConnectivity();
      return collapseConnectivityResults(results);
    } catch (e) {
      AppLogger.error('Get connectivity status failed: $e');
      return ConnectivityResult.none;
    }
  }

  /// Stream of connectivity changes
  Stream<ConnectivityResult> get connectivityStream {
    return _connectivity.onConnectivityChanged.map(collapseConnectivityResults);
  }

  /// Check if connected via WiFi
  Future<bool> isConnectedViaWiFi() async {
    final result = await getConnectivityStatus();
    return result == ConnectivityResult.wifi;
  }

  /// Check if connected via mobile data
  Future<bool> isConnectedViaMobile() async {
    final result = await getConnectivityStatus();
    return result == ConnectivityResult.mobile;
  }

  /// Get user-friendly connectivity message
  String getConnectivityMessage(ConnectivityResult result) {
    switch (result) {
      case ConnectivityResult.wifi:
        return 'Connected via WiFi';
      case ConnectivityResult.mobile:
        return 'Connected via Mobile Data';
      case ConnectivityResult.ethernet:
        return 'Connected via Ethernet';
      case ConnectivityResult.none:
        return 'No Internet Connection';
      default:
        return 'Unknown Connection';
    }
  }
}

