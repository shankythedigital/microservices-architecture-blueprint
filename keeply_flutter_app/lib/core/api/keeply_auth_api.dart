import 'package:dio/dio.dart';
import 'package:keeply_app/core/api/keeply_api_models.dart';
import 'package:keeply_app/core/api/keeply_service_url.dart';
import 'package:keeply_app/core/exceptions/api_exception.dart';
import 'package:keeply_app/core/network/api_client.dart';
/// Remaining parity with `keeply_react_app/src/api/authApi.ts` beyond [AuthRemoteDataSource].
///
/// Use [AuthRemoteDataSource] for login, refresh, profile/me, OTP send (login), logout, etc.
class KeeplyAuthApi {
  KeeplyAuthApi({ApiClient? apiClient}) : _client = apiClient ?? ApiClient();

  final ApiClient _client;

  /// `GET /api/auth/terms-and-conditions` (React `fetchActiveTerms`).
  Future<Map<String, dynamic>?> fetchActiveTerms({String language = 'en', String? projectType}) async {
    final q = <String, dynamic>{'language': language};
    if (projectType != null) q['projectType'] = projectType;
    final response = await _client.dio.get(
      keeplyApiUrl(KeeplyApiService.auth, '/api/auth/terms-and-conditions'),
      queryParameters: q,
    );
    if (response.statusCode == 200 && response.data is Map<String, dynamic>) {
      return response.data as Map<String, dynamic>;
    }
    return null;
  }

  /// React `registerUser` — JSON body (no profile photo). For multipart + photo use [registerUserMultipart].
  Future<Map<String, dynamic>> registerUserJson(Map<String, dynamic> body) async {
    final response = await _client.dio.post(
      keeplyApiUrl(KeeplyApiService.auth, '/api/auth/register'),
      data: body,
    );
    final data = jsonMapOf(response.data);
    if (response.statusCode != 200) {
      throw ApiException(
        message: data['error'] as String? ?? data['message'] as String? ?? 'Registration failed',
        statusCode: response.statusCode,
        type: ApiExceptionType.badRequest,
      );
    }
    return data;
  }

  /// React `registerUser` with `profilePhoto` file.
  Future<Map<String, dynamic>> registerUserMultipart(FormData formData) async {
    final response = await _client.dio.post(
      keeplyApiUrl(KeeplyApiService.auth, '/api/auth/register'),
      data: formData,
    );
    final data = jsonMapOf(response.data);
    if (response.statusCode != 200) {
      throw ApiException(
        message: data['error'] as String? ?? data['message'] as String? ?? 'Registration failed',
        statusCode: response.statusCode,
        type: ApiExceptionType.badRequest,
      );
    }
    return data;
  }

  /// `PUT /api/auth/profile/me` with multipart `profilePhoto` only (React `updateMyProfilePhoto`).
  /// Returns the `profile` object map; map to [UserDto] in the auth feature if needed.
  Future<Map<String, dynamic>> updateMyProfilePhoto(String filePath, {String? filename}) async {
    final form = FormData.fromMap({
      'profilePhoto': await MultipartFile.fromFile(filePath, filename: filename),
    });
    final response = await _client.dio.put(
      keeplyApiUrl(KeeplyApiService.auth, '/api/auth/profile/me'),
      data: form,
    );
    final root = jsonMapOf(response.data);
    if (response.statusCode != 200) {
      throw ApiException(
        message: root['error'] as String? ?? root['message'] as String? ?? 'Update failed',
        type: ApiExceptionType.badRequest,
      );
    }
    final profile = root['profile'];
    if (profile is! Map<String, dynamic>) {
      throw ApiException(message: 'Profile update did not return profile', type: ApiExceptionType.server);
    }
    return profile;
  }

  /// `POST /api/auth/logout?all=true` (React `logoutAllDevicesOnServer`).
  Future<void> logoutAllDevicesOnServer() async {
    await _client.dio.post(keeplyApiUrl(KeeplyApiService.auth, '/api/auth/logout?all=true'));
  }
}
