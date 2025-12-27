/// Edge Case Handler
/// Utility class for handling common edge cases across the app

class EdgeCaseHandler {
  /// Validate and sanitize input string
  static String? validateAndSanitizeString(
    String? value, {
    required String fieldName,
    int? minLength,
    int? maxLength,
    bool allowEmpty = false,
    RegExp? pattern,
  }) {
    // Edge case: Null check
    if (value == null) {
      if (allowEmpty) return null;
      return '$fieldName is required';
    }

    // Edge case: Empty string
    if (value.trim().isEmpty) {
      if (allowEmpty) return null;
      return '$fieldName cannot be empty';
    }

    final trimmed = value.trim();

    // Edge case: Min length
    if (minLength != null && trimmed.length < minLength) {
      return '$fieldName must be at least $minLength characters';
    }

    // Edge case: Max length
    if (maxLength != null && trimmed.length > maxLength) {
      return '$fieldName must be less than $maxLength characters';
    }

    // Edge case: Pattern validation
    if (pattern != null && !pattern.hasMatch(trimmed)) {
      return 'Invalid $fieldName format';
    }

    return null;
  }

  /// Validate email format
  static String? validateEmail(String? email) {
    if (email == null || email.isEmpty) {
      return 'Email is required';
    }

    final emailRegex = RegExp(r'^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$');
    if (!emailRegex.hasMatch(email)) {
      return 'Invalid email format';
    }

    return null;
  }

  /// Validate mobile number format
  static String? validateMobile(String? mobile) {
    if (mobile == null || mobile.isEmpty) {
      return 'Mobile number is required';
    }

    // E.164 format: +[country code][number]
    final mobileRegex = RegExp(r'^\+?[1-9]\d{1,14}$');
    if (!mobileRegex.hasMatch(mobile)) {
      return 'Invalid mobile number format';
    }

    return null;
  }

  /// Validate password strength
  static String? validatePassword(String? password) {
    if (password == null || password.isEmpty) {
      return 'Password is required';
    }

    if (password.length < 8) {
      return 'Password must be at least 8 characters';
    }

    if (password.length > 128) {
      return 'Password must be less than 128 characters';
    }

    // Optional: Add more strength checks
    // if (!RegExp(r'[A-Z]').hasMatch(password)) {
    //   return 'Password must contain at least one uppercase letter';
    // }
    // if (!RegExp(r'[a-z]').hasMatch(password)) {
    //   return 'Password must contain at least one lowercase letter';
    // }
    // if (!RegExp(r'[0-9]').hasMatch(password)) {
    //   return 'Password must contain at least one number';
    // }

    return null;
  }

  /// Validate OTP format
  static String? validateOtp(String? otp) {
    if (otp == null || otp.isEmpty) {
      return 'OTP is required';
    }

    if (otp.length != 6) {
      return 'OTP must be 6 digits';
    }

    if (!RegExp(r'^\d+$').hasMatch(otp)) {
      return 'OTP must contain only digits';
    }

    return null;
  }

  /// Validate MPIN format
  static String? validateMpin(String? mpin) {
    if (mpin == null || mpin.isEmpty) {
      return 'MPIN is required';
    }

    if (mpin.length != 4) {
      return 'MPIN must be 4 digits';
    }

    if (!RegExp(r'^\d+$').hasMatch(mpin)) {
      return 'MPIN must contain only digits';
    }

    return null;
  }

  /// Validate ID (must be positive integer)
  static String? validateId(int? id, {String fieldName = 'ID'}) {
    if (id == null) {
      return '$fieldName is required';
    }

    if (id <= 0) {
      return 'Invalid $fieldName';
    }

    return null;
  }

  /// Handle network errors with retry logic
  static Future<T> withRetry<T>({
    required Future<T> Function() operation,
    int maxRetries = 3,
    Duration delay = const Duration(seconds: 2),
  }) async {
    int attempts = 0;
    Exception? lastException;

    while (attempts < maxRetries) {
      try {
        return await operation();
      } catch (e) {
        lastException = e is Exception ? e : Exception(e.toString());
        attempts++;

        if (attempts < maxRetries) {
          await Future.delayed(delay * attempts); // Exponential backoff
        }
      }
    }

    throw lastException ?? Exception('Operation failed after $maxRetries attempts');
  }

  /// Debounce function calls
  static void debounce(
    VoidCallback callback, {
    Duration delay = const Duration(milliseconds: 500),
  }) {
    // This is a simplified version - in production, use a proper debounce utility
    Future.delayed(delay, callback);
  }

  /// Format error message for display
  static String formatErrorMessage(dynamic error) {
    if (error is String) {
      return error;
    }

    if (error is Exception) {
      return error.toString().replaceFirst('Exception: ', '');
    }

    return 'An unexpected error occurred';
  }

  /// Check if value is null or empty
  static bool isNullOrEmpty(dynamic value) {
    if (value == null) return true;
    if (value is String) return value.trim().isEmpty;
    if (value is List) return value.isEmpty;
    if (value is Map) return value.isEmpty;
    return false;
  }

  /// Safe parse integer
  static int? safeParseInt(String? value) {
    if (value == null || value.isEmpty) return null;
    return int.tryParse(value);
  }

  /// Safe parse double
  static double? safeParseDouble(String? value) {
    if (value == null || value.isEmpty) return null;
    return double.tryParse(value);
  }

  /// Truncate string with ellipsis
  static String truncate(String value, int maxLength) {
    if (value.length <= maxLength) return value;
    return '${value.substring(0, maxLength)}...';
  }

  /// Validate file size
  static String? validateFileSize(int sizeInBytes, {int maxSizeInMB = 50}) {
    final maxSizeInBytes = maxSizeInMB * 1024 * 1024;
    if (sizeInBytes > maxSizeInBytes) {
      return 'File size must be less than ${maxSizeInMB}MB';
    }
    return null;
  }

  /// Validate file type
  static String? validateFileType(
    String fileName,
    List<String> allowedExtensions,
  ) {
    final extension = fileName.split('.').last.toLowerCase();
    if (!allowedExtensions.contains(extension)) {
      return 'File type not allowed. Allowed types: ${allowedExtensions.join(', ')}';
    }
    return null;
  }
}

