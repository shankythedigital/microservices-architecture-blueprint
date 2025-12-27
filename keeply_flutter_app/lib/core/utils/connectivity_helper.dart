import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:keeply_app/core/utils/logger.dart';

/// Connectivity Helper
/// Monitors network connectivity and provides status
class ConnectivityHelper {
  final Connectivity _connectivity = Connectivity();

  /// Check if device is connected to internet
  Future<bool> isConnected() async {
    try {
      final result = await _connectivity.checkConnectivity();
      return result != ConnectivityResult.none;
    } catch (e) {
      AppLogger.error('Connectivity check failed: $e');
      return false;
    }
  }

  /// Get current connectivity status
  Future<ConnectivityResult> getConnectivityStatus() async {
    try {
      return await _connectivity.checkConnectivity();
    } catch (e) {
      AppLogger.error('Get connectivity status failed: $e');
      return ConnectivityResult.none;
    }
  }

  /// Stream of connectivity changes
  Stream<ConnectivityResult> get connectivityStream {
    return _connectivity.onConnectivityChanged;
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

