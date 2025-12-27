/// Authentication Models
/// Data models for authentication requests and responses

class LoginRequest {
  final String loginType;
  final String? username;
  final String? password;
  final String? otp;
  final String? mpin;
  final String? rsaChallenge;
  final String? signature;
  final String? credentialId;
  final String? authCode;
  final String? deviceInfo;

  LoginRequest({
    required this.loginType,
    this.username,
    this.password,
    this.otp,
    this.mpin,
    this.rsaChallenge,
    this.signature,
    this.credentialId,
    this.authCode,
    this.deviceInfo,
  });

  Map<String, dynamic> toJson() {
    final map = <String, dynamic>{'loginType': loginType};
    if (username != null) map['username'] = username;
    if (password != null) map['password'] = password;
    if (otp != null) map['otp'] = otp;
    if (mpin != null) map['mpin'] = mpin;
    if (rsaChallenge != null) map['rsaChallenge'] = rsaChallenge;
    if (signature != null) map['signature'] = signature;
    if (credentialId != null) map['credentialId'] = credentialId;
    if (authCode != null) map['authCode'] = authCode;
    if (deviceInfo != null) map['deviceInfo'] = deviceInfo;
    return map;
  }
}

class RegisterRequest {
  final String username;
  final String password;
  final String? email;
  final String? mobile;
  final String projectType;

  RegisterRequest({
    required this.username,
    required this.password,
    this.email,
    this.mobile,
    required this.projectType,
  });

  Map<String, dynamic> toJson() => {
        'username': username,
        'password': password,
        if (email != null) 'email': email,
        if (mobile != null) 'mobile': mobile,
        'projectType': projectType,
      };
}

class AuthResponse {
  final String accessToken;
  final String refreshToken;
  final String tokenType;
  final int expiresIn;
  final int? userId;
  final String? username;
  final List<String>? roles;

  AuthResponse({
    required this.accessToken,
    required this.refreshToken,
    this.tokenType = 'Bearer',
    required this.expiresIn,
    this.userId,
    this.username,
    this.roles,
  });

  factory AuthResponse.fromJson(Map<String, dynamic> json) {
    return AuthResponse(
      accessToken: json['accessToken'] as String,
      refreshToken: json['refreshToken'] as String? ?? '',
      tokenType: json['tokenType'] as String? ?? 'Bearer',
      expiresIn: json['expiresIn'] as int? ?? 3600,
      userId: json['userId'] as int?,
      username: json['username'] as String?,
      roles: json['roles'] != null
          ? List<String>.from(json['roles'] as List)
          : null,
    );
  }
}

class UserDto {
  final int userId;
  final String? username;
  final String? email;
  final String? mobile;
  final String? projectType;
  final bool? enabled;
  final List<String>? roles;
  final DateTime? lastLoginDate;

  UserDto({
    required this.userId,
    this.username,
    this.email,
    this.mobile,
    this.projectType,
    this.enabled,
    this.roles,
    this.lastLoginDate,
  });

  factory UserDto.fromJson(Map<String, dynamic> json) {
    return UserDto(
      userId: json['userId'] as int,
      username: json['username'] as String?,
      email: json['email'] as String?,
      mobile: json['mobile'] as String?,
      projectType: json['projectType'] as String?,
      enabled: json['enabled'] as bool?,
      roles: json['roles'] != null
          ? List<String>.from(json['roles'] as List)
          : null,
      lastLoginDate: json['lastLoginDate'] != null
          ? DateTime.parse(json['lastLoginDate'] as String)
          : null,
    );
  }
}

class OtpRequest {
  final int userId;
  final String purpose; // LOGIN, RESET_PASSWORD, CHANGE_MOBILE, CHANGE_EMAIL
  final String channel; // SMS, EMAIL

  OtpRequest({
    required this.userId,
    required this.purpose,
    required this.channel,
  });

  Map<String, dynamic> toJson() => {
        'userId': userId,
        'purpose': purpose,
        'channel': channel,
      };
}

class OtpResponse {
  final String status;
  final String message;
  final int? userId;
  final String? channel;
  final String? otp; // Only in dev mode
  final int? expiresInMinutes;

  OtpResponse({
    required this.status,
    required this.message,
    this.userId,
    this.channel,
    this.otp,
    this.expiresInMinutes,
  });

  factory OtpResponse.fromJson(Map<String, dynamic> json) {
    return OtpResponse(
      status: json['status'] as String,
      message: json['message'] as String,
      userId: json['userId'] as int?,
      channel: json['channel'] as String?,
      otp: json['otp'] as String?,
      expiresInMinutes: json['expiresInMinutes'] as int?,
    );
  }
}

