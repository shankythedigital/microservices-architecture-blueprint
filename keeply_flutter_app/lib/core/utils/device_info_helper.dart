import 'package:device_info_plus/device_info_plus.dart';
import 'package:package_info_plus/package_info_plus.dart';
import 'package:keeply_app/core/utils/logger.dart';

/// Device Info Helper
/// Provides device and app information
class DeviceInfoHelper {
  static final DeviceInfoHelper _instance = DeviceInfoHelper._internal();
  factory DeviceInfoHelper() => _instance;
  DeviceInfoHelper._internal();

  DeviceInfoPlugin _deviceInfo = DeviceInfoPlugin();
  PackageInfo? _packageInfo;

  /// Initialize device info
  Future<void> initialize() async {
    try {
      _packageInfo = await PackageInfo.fromPlatform();
      AppLogger.info('Device info initialized');
    } catch (e) {
      AppLogger.error('Device info initialization failed: $e');
    }
  }

  /// Get device ID (for deviceInfo in login)
  Future<String> getDeviceId() async {
    try {
      if (await _isAndroid()) {
        final androidInfo = await _deviceInfo.androidInfo;
        return androidInfo.id;
      } else if (await _isIOS()) {
        final iosInfo = await _deviceInfo.iosInfo;
        return iosInfo.identifierForVendor ?? 'unknown';
      }
      return 'unknown';
    } catch (e) {
      AppLogger.error('Get device ID failed: $e');
      return 'unknown';
    }
  }

  /// Get device model
  Future<String> getDeviceModel() async {
    try {
      if (await _isAndroid()) {
        final androidInfo = await _deviceInfo.androidInfo;
        return '${androidInfo.manufacturer} ${androidInfo.model}';
      } else if (await _isIOS()) {
        final iosInfo = await _deviceInfo.iosInfo;
        return '${iosInfo.utsname.machine}';
      }
      return 'Unknown';
    } catch (e) {
      AppLogger.error('Get device model failed: $e');
      return 'Unknown';
    }
  }

  /// Get OS version
  Future<String> getOsVersion() async {
    try {
      if (await _isAndroid()) {
        final androidInfo = await _deviceInfo.androidInfo;
        return 'Android ${androidInfo.version.release}';
      } else if (await _isIOS()) {
        final iosInfo = await _deviceInfo.iosInfo;
        return 'iOS ${iosInfo.systemVersion}';
      }
      return 'Unknown';
    } catch (e) {
      AppLogger.error('Get OS version failed: $e');
      return 'Unknown';
    }
  }

  /// Get app version
  String getAppVersion() {
    return _packageInfo?.version ?? 'Unknown';
  }

  /// Get app build number
  String getAppBuildNumber() {
    return _packageInfo?.buildNumber ?? 'Unknown';
  }

  /// Get device info string for API
  Future<String> getDeviceInfoString() async {
    final deviceId = await getDeviceId();
    final model = await getDeviceModel();
    final osVersion = await getOsVersion();
    final appVersion = getAppVersion();

    return '$model | $osVersion | App v$appVersion | ID: $deviceId';
  }

  Future<bool> _isAndroid() async {
    try {
      await _deviceInfo.androidInfo;
      return true;
    } catch (_) {
      return false;
    }
  }

  Future<bool> _isIOS() async {
    try {
      await _deviceInfo.iosInfo;
      return true;
    } catch (_) {
      return false;
    }
  }
}

