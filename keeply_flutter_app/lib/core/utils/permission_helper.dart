import 'package:permission_handler/permission_handler.dart' as ph;
import 'package:keeply_app/core/utils/logger.dart';

/// Permission Helper
/// Manages app permissions with proper handling
class PermissionHelper {
  /// Request camera permission
  static Future<bool> requestCameraPermission() async {
    try {
      final status = await Permission.camera.request();
      return status.isGranted;
    } catch (e) {
      AppLogger.error('Camera permission request failed: $e');
      return false;
    }
  }

  /// Request storage permission
  static Future<bool> requestStoragePermission() async {
    try {
      final status = await Permission.storage.request();
      return status.isGranted;
    } catch (e) {
      AppLogger.error('Storage permission request failed: $e');
      return false;
    }
  }

  /// Request photos permission
  static Future<bool> requestPhotosPermission() async {
    try {
      final status = await Permission.photos.request();
      return status.isGranted;
    } catch (e) {
      AppLogger.error('Photos permission request failed: $e');
      return false;
    }
  }

  /// Check if permission is granted
  static Future<bool> isPermissionGranted(Permission permission) async {
    try {
      final status = await permission.status;
      return status.isGranted;
    } catch (e) {
      AppLogger.error('Permission check failed: $e');
      return false;
    }
  }

  /// Open app settings
  static Future<bool> openAppSettings() async {
    try {
      return await ph.openAppSettings();
    } catch (e) {
      AppLogger.error('Open app settings failed: $e');
      return false;
    }
  }
}

