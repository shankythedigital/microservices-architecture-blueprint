import 'package:intl/intl.dart';
import 'package:keeply_app/core/constants/app_constants.dart';

/// Date Formatter
/// Utility for formatting dates consistently
class DateFormatter {
  /// Format date for display
  static String formatDate(DateTime? date) {
    if (date == null) return 'N/A';
    return DateFormat(AppConstants.dateFormatDisplay).format(date);
  }

  /// Format date-time for display
  static String formatDateTime(DateTime? dateTime) {
    if (dateTime == null) return 'N/A';
    return DateFormat(AppConstants.dateTimeFormatDisplay).format(dateTime);
  }

  /// Format date for API
  static String formatDateForApi(DateTime date) {
    return DateFormat(AppConstants.dateFormatApi).format(date);
  }

  /// Format date-time for API
  static String formatDateTimeForApi(DateTime dateTime) {
    return DateFormat(AppConstants.dateTimeFormatApi).format(dateTime);
  }

  /// Parse date from API string
  static DateTime? parseDateFromApi(String? dateString) {
    if (dateString == null || dateString.isEmpty) return null;
    try {
      return DateTime.parse(dateString);
    } catch (e) {
      return null;
    }
  }

  /// Get relative time (e.g., "2 hours ago")
  static String getRelativeTime(DateTime dateTime) {
    final now = DateTime.now();
    final difference = now.difference(dateTime);

    if (difference.inDays > 365) {
      return '${(difference.inDays / 365).floor()} years ago';
    } else if (difference.inDays > 30) {
      return '${(difference.inDays / 30).floor()} months ago';
    } else if (difference.inDays > 0) {
      return '${difference.inDays} days ago';
    } else if (difference.inHours > 0) {
      return '${difference.inHours} hours ago';
    } else if (difference.inMinutes > 0) {
      return '${difference.inMinutes} minutes ago';
    } else {
      return 'Just now';
    }
  }
}

