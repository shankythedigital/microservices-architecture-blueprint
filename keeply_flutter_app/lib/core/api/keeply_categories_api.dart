import 'package:keeply_app/core/api/keeply_api_models.dart';
import 'package:keeply_app/core/api/keeply_service_url.dart';
import 'package:keeply_app/core/exceptions/api_exception.dart';
import 'package:keeply_app/core/network/api_client.dart';

/// Parity with `keeply_react_app/src/api/categoriesApi.ts`.
class KeeplyCategoriesApi {
  KeeplyCategoriesApi({ApiClient? apiClient}) : _client = apiClient ?? ApiClient();

  final ApiClient _client;

  /// `GET /api/asset/v1/categories` — returns `data` list from `ResponseWrapper`.
  Future<List<CategoryDto>> listCategories() async {
    final response = await _client.dio.get(
      keeplyApiUrl(KeeplyApiService.asset, '/api/asset/v1/categories'),
    );
    final root = jsonMapOf(response.data);
    if (root['success'] == false) {
      throw ApiException(
        message: root['message'] as String? ?? 'Request failed',
        type: ApiExceptionType.badRequest,
      );
    }
    final data = root['data'];
    if (data is! List) return [];
    return data.whereType<Map>().map((e) => CategoryDto.fromJson(Map<String, dynamic>.from(e))).toList();
  }
}
