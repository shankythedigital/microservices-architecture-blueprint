import 'package:keeply_app/core/api/keeply_api_models.dart';
import 'package:keeply_app/core/api/keeply_service_url.dart';
import 'package:keeply_app/core/exceptions/api_exception.dart';
import 'package:keeply_app/core/network/api_client.dart';

/// Parity with `keeply_react_app/src/api/masterDataApi.ts` (`ResponseWrapper` list payloads).
class KeeplyMasterDataApi {
  KeeplyMasterDataApi({ApiClient? apiClient}) : _client = apiClient ?? ApiClient();

  final ApiClient _client;

  Future<List<SubCategoryDto>> listSubCategories() async {
    return _unwrapWrapperList(
      keeplyApiUrl(KeeplyApiService.asset, '/api/asset/v1/subcategories'),
      SubCategoryDto.fromJson,
    );
  }

  Future<List<MakeDto>> listMakes() async {
    return _unwrapWrapperList(
      keeplyApiUrl(KeeplyApiService.asset, '/api/asset/v1/makes'),
      MakeDto.fromJson,
    );
  }

  Future<List<ModelDto>> listModels() async {
    return _unwrapWrapperList(
      keeplyApiUrl(KeeplyApiService.asset, '/api/asset/v1/models'),
      ModelDto.fromJson,
    );
  }

  Future<List<T>> _unwrapWrapperList<T>(
    String url,
    T Function(Map<String, dynamic>) fromJson,
  ) async {
    final response = await _client.dio.get(url);
    final root = jsonMapOf(response.data);
    if (root['success'] == false) {
      throw ApiException(
        message: root['message'] as String? ?? 'Request failed',
        type: ApiExceptionType.badRequest,
      );
    }
    final data = root['data'];
    if (data is! List) return [];
    return data.whereType<Map>().map((e) => fromJson(Map<String, dynamic>.from(e))).toList();
  }
}
