/// Authentication Models
/// Data models for authentication requests and responses
library;

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

/// Body for `POST /api/auth/register` (JSON) — parity with auth-service `RegisterRequest`.
class RegisterRequest {
  final String username;
  final String password;
  final String? email;
  /// National significant number only (no country prefix); validated with [countryCode].
  final String mobile;
  final String countryCode;
  final String projectType;
  final bool acceptTc;
  final String? firstName;
  final String? lastName;
  final String? pincode;
  final String? city;
  final String? state;
  final String? country;
  final String? address1;
  final String? address2;
  final String? address3;

  RegisterRequest({
    required this.username,
    required this.password,
    this.email,
    required this.mobile,
    required this.countryCode,
    required this.projectType,
    required this.acceptTc,
    this.firstName,
    this.lastName,
    this.pincode,
    this.city,
    this.state,
    this.country,
    this.address1,
    this.address2,
    this.address3,
  });

  Map<String, dynamic> toJson() {
    String? nz(String? s) {
      final t = s?.trim();
      if (t == null || t.isEmpty) return null;
      return t;
    }

    final map = <String, dynamic>{
      'username': username.trim(),
      'password': password,
      'mobile': mobile.trim(),
      'countryCode': countryCode.trim(),
      'projectType': projectType.trim(),
      'acceptTc': acceptTc,
    };
    final e = nz(email);
    if (e != null) map['email'] = e;
    final fn = nz(firstName);
    if (fn != null) map['firstName'] = fn;
    final ln = nz(lastName);
    if (ln != null) map['lastName'] = ln;
    final pc = nz(pincode);
    if (pc != null) map['pincode'] = pc;
    final cty = nz(city);
    if (cty != null) map['city'] = cty;
    final st = nz(state);
    if (st != null) map['state'] = st;
    final ctry = nz(country);
    if (ctry != null) map['country'] = ctry;
    final a1 = nz(address1);
    if (a1 != null) map['address1'] = a1;
    final a2 = nz(address2);
    if (a2 != null) map['address2'] = a2;
    final a3 = nz(address3);
    if (a3 != null) map['address3'] = a3;
    return map;
  }
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
  /// From `GET /api/auth/profile/me` when auth-service exposes it.
  final String? profilePhotoUrl;

  UserDto({
    required this.userId,
    this.username,
    this.email,
    this.mobile,
    this.projectType,
    this.enabled,
    this.roles,
    this.lastLoginDate,
    this.profilePhotoUrl,
  });

  static int? _parseUserId(Map<String, dynamic> json) {
    final raw = json['userId'] ?? json['id'];
    if (raw == null) return null;
    if (raw is int) return raw;
    if (raw is num) return raw.toInt();
    return int.tryParse('$raw');
  }

  factory UserDto.fromJson(Map<String, dynamic> json) {
    final uid = _parseUserId(json) ?? 0;
    return UserDto(
      userId: uid,
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
      profilePhotoUrl: json['profilePhotoUrl'] as String?,
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

