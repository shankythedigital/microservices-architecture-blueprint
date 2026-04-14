import 'package:keeply_app/core/config/app_config.dart';
import 'package:email_validator/email_validator.dart';

/// Validation Helper
/// Comprehensive input validation utilities
class ValidationHelper {
  /// Validate username
  static String? validateUsername(String? value) {
    if (value == null || value.isEmpty) {
      return 'Username is required';
    }

    final trimmed = value.trim();

    if (trimmed.length < AppConfig.minUsernameLength) {
      return 'Username must be at least ${AppConfig.minUsernameLength} characters';
    }

    if (trimmed.length > AppConfig.maxUsernameLength) {
      return 'Username must be less than ${AppConfig.maxUsernameLength} characters';
    }

    // Allow alphanumeric and underscore
    if (!RegExp(r'^[a-zA-Z0-9_]+$').hasMatch(trimmed)) {
      return 'Username can only contain letters, numbers, and underscores';
    }

    return null;
  }

  /// Validate password
  static String? validatePassword(String? value) {
    if (value == null || value.isEmpty) {
      return 'Password is required';
    }

    if (value.length < AppConfig.minPasswordLength) {
      return 'Password must be at least ${AppConfig.minPasswordLength} characters';
    }

    if (value.length > AppConfig.maxPasswordLength) {
      return 'Password must be less than ${AppConfig.maxPasswordLength} characters';
    }

    return null;
  }

  /// Validate email
  static String? validateEmail(String? value, {bool required = true}) {
    if (value == null || value.isEmpty) {
      if (required) {
        return 'Email is required';
      }
      return null;
    }

    if (!EmailValidator.validate(value)) {
      return 'Invalid email format';
    }

    return null;
  }

  /// Validate mobile number
  static String? validateMobile(String? value, {bool required = true}) {
    if (value == null || value.isEmpty) {
      if (required) {
        return 'Mobile number is required';
      }
      return null;
    }

    // E.164 format: +[country code][number]
    final mobileRegex = RegExp(r'^\+?[1-9]\d{1,14}$');
    if (!mobileRegex.hasMatch(value)) {
      return 'Invalid mobile number format';
    }

    return null;
  }

  /// National mobile (no country prefix), aligned with auth-service `MobileValidationUtil`.
  static String? validateNationalMobile(String? value, String countryCode) {
    if (value == null || value.trim().isEmpty) {
      return 'Mobile number is required';
    }
    final cc = countryCode.trim();
    if (cc.isEmpty) {
      return 'Country code is required';
    }
    final cleaned = value.replaceAll(RegExp(r'[\s\-()]'), '');
    if (!RegExp(r'^\d+$').hasMatch(cleaned)) {
      return 'Mobile must contain only digits';
    }
    final key = cc.toUpperCase();
    final pattern = _nationalMobilePatterns[key] ?? _nationalMobilePatterns['DEFAULT']!;
    if (!pattern.hasMatch(cleaned)) {
      return 'Invalid mobile number format for $cc';
    }
    return null;
  }

  static final Map<String, RegExp> _nationalMobilePatterns = <String, RegExp>{
    '+91': RegExp(r'^[6-9]\d{9}$'),
    '+1': RegExp(r'^\d{10}$'),
    '+44': RegExp(r'^[1-9]\d{9,10}$'),
    '+61': RegExp(r'^[1-9]\d{8}$'),
    '+49': RegExp(r'^[1-9]\d{9,10}$'),
    '+33': RegExp(r'^[1-9]\d{8}$'),
    '+86': RegExp(r'^1\d{10}$'),
    '+81': RegExp(r'^[1-9]\d{9,10}$'),
    '+55': RegExp(r'^[1-9]\d{9,10}$'),
    '+971': RegExp(r'^[1-9]\d{8}$'),
    '+65': RegExp(r'^\d{8}$'),
    'DEFAULT': RegExp(r'^\d{7,15}$'),
  };

  /// Validate OTP
  static String? validateOtp(String? value) {
    if (value == null || value.isEmpty) {
      return 'OTP is required';
    }

    if (value.length != 6) {
      return 'OTP must be 6 digits';
    }

    if (!RegExp(r'^\d+$').hasMatch(value)) {
      return 'OTP must contain only digits';
    }

    return null;
  }

  /// Validate MPIN
  static String? validateMpin(String? value) {
    if (value == null || value.isEmpty) {
      return 'MPIN is required';
    }

    if (value.length != 4) {
      return 'MPIN must be 4 digits';
    }

    if (!RegExp(r'^\d+$').hasMatch(value)) {
      return 'MPIN must contain only digits';
    }

    return null;
  }

  /// Validate required field
  static String? validateRequired(String? value, String fieldName) {
    if (value == null || value.trim().isEmpty) {
      return '$fieldName is required';
    }
    return null;
  }

  /// Validate number
  static String? validateNumber(String? value, {int? min, int? max}) {
    if (value == null || value.isEmpty) {
      return 'Number is required';
    }

    final number = int.tryParse(value);
    if (number == null) {
      return 'Invalid number format';
    }

    if (min != null && number < min) {
      return 'Number must be at least $min';
    }

    if (max != null && number > max) {
      return 'Number must be at most $max';
    }

    return null;
  }

  /// Validate file size
  static String? validateFileSize(int sizeInBytes) {
    if (sizeInBytes > AppConfig.maxFileSize) {
      final maxSizeMB = AppConfig.maxFileSize ~/ (1024 * 1024);
      return 'File size must be less than ${maxSizeMB}MB';
    }
    return null;
  }

  /// Validate file type
  static String? validateFileType(String fileName, List<String> allowedTypes) {
    final extension = fileName.split('.').last.toLowerCase();
    if (!allowedTypes.contains(extension)) {
      return 'File type not allowed. Allowed types: ${allowedTypes.join(', ')}';
    }
    return null;
  }
}

