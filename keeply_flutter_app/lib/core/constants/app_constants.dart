/// App Constants
/// Centralized constants used across the app

class AppConstants {
  // Storage Keys
  static const String keyAccessToken = 'access_token';
  static const String keyRefreshToken = 'refresh_token';
  static const String keyUserId = 'user_id';
  static const String keyUsername = 'username';
  static const String keyProjectType = 'project_type';
  static const String keyBiometricEnabled = 'biometric_enabled';
  static const String keyLastSyncTime = 'last_sync_time';

  // Login Types
  static const String loginTypePassword = 'PASSWORD';
  static const String loginTypeOtp = 'OTP';
  static const String loginTypeMpin = 'MPIN';
  static const String loginTypeRsa = 'RSA';
  static const String loginTypePasskey = 'PASSKEY';
  static const String loginTypeAuthCode = 'AUTHCODE';

  // Notification Channels
  static const String channelSms = 'SMS';
  static const String channelEmail = 'EMAIL';
  static const String channelWhatsapp = 'WHATSAPP';
  static const String channelInapp = 'INAPP';

  // Notification Purposes
  static const String purposeLogin = 'LOGIN';
  static const String purposeResetPassword = 'RESET_PASSWORD';
  static const String purposeChangeMobile = 'CHANGE_MOBILE';
  static const String purposeChangeEmail = 'CHANGE_EMAIL';

  // Entity Types
  static const String entityTypeAsset = 'ASSET';
  static const String entityTypeComponent = 'COMPONENT';
  static const String entityTypeModel = 'MODEL';
  static const String entityTypeMake = 'MAKE';
  static const String entityTypeAmc = 'AMC';
  static const String entityTypeWarranty = 'WARRANTY';
  static const String entityTypeDocument = 'DOCUMENT';

  // Project Types
  static const String projectTypeAsset = 'ASSET';
  static const String projectTypeEcom = 'ECOM';
  static const String projectTypePortal = 'PORTAL';
  static const String projectTypeAdminConsole = 'ADMIN_CONSOLE';

  // Date Formats
  static const String dateFormatDisplay = 'dd MMM yyyy';
  static const String dateFormatApi = 'yyyy-MM-dd';
  static const String dateTimeFormatDisplay = 'dd MMM yyyy, HH:mm';
  static const String dateTimeFormatApi = "yyyy-MM-dd'T'HH:mm:ss";

  // Validation Patterns
  static final RegExp usernamePattern = RegExp(r'^[a-zA-Z0-9_]+$');
  static final RegExp emailPattern = RegExp(r'^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$');
  static final RegExp mobilePattern = RegExp(r'^\+?[1-9]\d{1,14}$');
  static final RegExp otpPattern = RegExp(r'^\d{6}$');
  static final RegExp mpinPattern = RegExp(r'^\d{4}$');

  // Error Messages
  static const String errorNetwork = 'No internet connection. Please check your network.';
  static const String errorTimeout = 'Request timeout. Please try again.';
  static const String errorUnauthorized = 'Session expired. Please login again.';
  static const String errorForbidden = 'You do not have permission to perform this action.';
  static const String errorNotFound = 'Resource not found.';
  static const String errorServer = 'Server error. Please try again later.';
  static const String errorUnknown = 'An unexpected error occurred.';

  // Success Messages
  static const String successAssetCreated = 'Asset created successfully';
  static const String successAssetUpdated = 'Asset updated successfully';
  static const String successAssetDeleted = 'Asset deleted successfully';
  static const String successCategoryCreated = 'Category created successfully';
  static const String successPasswordChanged = 'Password changed successfully';
  static const String successOtpSent = 'OTP sent successfully';

  // Loading Messages
  static const String loadingAssets = 'Loading assets...';
  static const String loadingCategories = 'Loading categories...';
  static const String creatingAsset = 'Creating asset...';
  static const String updatingAsset = 'Updating asset...';
  static const String deletingAsset = 'Deleting asset...';
  static const String uploadingFile = 'Uploading file...';
}

