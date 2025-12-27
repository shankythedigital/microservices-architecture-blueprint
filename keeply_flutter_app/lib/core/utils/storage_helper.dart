import 'package:shared_preferences/shared_preferences.dart';
import 'package:keeply_app/core/utils/logger.dart';

/// Storage Helper
/// Manages local storage with SharedPreferences
class StorageHelper {
  static final StorageHelper _instance = StorageHelper._internal();
  factory StorageHelper() => _instance;
  StorageHelper._internal();

  SharedPreferences? _prefs;

  /// Initialize SharedPreferences
  Future<void> initialize() async {
    try {
      _prefs = await SharedPreferences.getInstance();
      AppLogger.info('Storage initialized');
    } catch (e) {
      AppLogger.error('Storage initialization failed: $e');
    }
  }

  /// Get string value
  String? getString(String key) {
    try {
      return _prefs?.getString(key);
    } catch (e) {
      AppLogger.error('Get string failed: $e');
      return null;
    }
  }

  /// Set string value
  Future<bool> setString(String key, String value) async {
    try {
      return await _prefs?.setString(key, value) ?? false;
    } catch (e) {
      AppLogger.error('Set string failed: $e');
      return false;
    }
  }

  /// Get int value
  int? getInt(String key) {
    try {
      return _prefs?.getInt(key);
    } catch (e) {
      AppLogger.error('Get int failed: $e');
      return null;
    }
  }

  /// Set int value
  Future<bool> setInt(String key, int value) async {
    try {
      return await _prefs?.setInt(key, value) ?? false;
    } catch (e) {
      AppLogger.error('Set int failed: $e');
      return false;
    }
  }

  /// Get bool value
  bool? getBool(String key) {
    try {
      return _prefs?.getBool(key);
    } catch (e) {
      AppLogger.error('Get bool failed: $e');
      return null;
    }
  }

  /// Set bool value
  Future<bool> setBool(String key, bool value) async {
    try {
      return await _prefs?.setBool(key, value) ?? false;
    } catch (e) {
      AppLogger.error('Set bool failed: $e');
      return false;
    }
  }

  /// Remove value
  Future<bool> remove(String key) async {
    try {
      return await _prefs?.remove(key) ?? false;
    } catch (e) {
      AppLogger.error('Remove failed: $e');
      return false;
    }
  }

  /// Clear all data
  Future<bool> clear() async {
    try {
      return await _prefs?.clear() ?? false;
    } catch (e) {
      AppLogger.error('Clear failed: $e');
      return false;
    }
  }

  /// Check if key exists
  bool containsKey(String key) {
    return _prefs?.containsKey(key) ?? false;
  }

  /// Get all keys
  Set<String> getAllKeys() {
    return _prefs?.getKeys() ?? {};
  }
}

