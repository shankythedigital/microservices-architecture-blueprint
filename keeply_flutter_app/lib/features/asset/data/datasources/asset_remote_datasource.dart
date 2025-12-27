import 'package:dio/dio.dart';
import 'package:keeply_app/core/config/app_config.dart';
import 'package:keeply_app/core/network/api_client.dart';
import 'package:keeply_app/core/exceptions/api_exception.dart';
import 'package:keeply_app/features/asset/data/models/asset_models.dart';

/// Asset Remote Data Source
/// Handles all asset management API calls with comprehensive error handling
class AssetRemoteDataSource {
  final ApiClient _apiClient = ApiClient();

  // ============================================================
  // ASSET OPERATIONS
  // ============================================================

  /// Create a new asset
  Future<AssetMaster> createAsset(AssetRequest request) async {
    try {
      final response = await _apiClient.dio.post(
        '${AppConfig.assetServiceBaseUrl}${AppConfig.assetBasePath}/assets',
        data: request.toJson(),
      );

      if (response.statusCode == 200 && response.data != null) {
        final wrapper = ResponseWrapper.fromJson(
          response.data as Map<String, dynamic>,
          (json) => AssetMaster.fromJson(json),
        );
        if (wrapper.success && wrapper.data != null) {
          return wrapper.data!;
        }
        throw ApiException(
          message: wrapper.message,
          type: ApiExceptionType.badRequest,
        );
      }

      throw ApiException(
        message: 'Failed to create asset',
        type: ApiExceptionType.server,
      );
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  /// Get asset by ID
  Future<AssetMaster> getAssetById(int assetId) async {
    try {
      final response = await _apiClient.dio.get(
        '${AppConfig.assetServiceBaseUrl}${AppConfig.assetBasePath}/assets/$assetId',
      );

      if (response.statusCode == 200 && response.data != null) {
        final wrapper = ResponseWrapper.fromJson(
          response.data as Map<String, dynamic>,
          (json) => AssetMaster.fromJson(json),
        );
        if (wrapper.success && wrapper.data != null) {
          return wrapper.data!;
        }
        throw ApiException(
          message: wrapper.message,
          type: ApiExceptionType.notFound,
        );
      }

      throw ApiException(
        message: 'Asset not found',
        type: ApiExceptionType.notFound,
      );
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  /// Get all assets with pagination
  Future<List<AssetMaster>> getAssets({
    int page = 0,
    int size = 20,
    String? sort,
  }) async {
    try {
      final queryParams = <String, dynamic>{
        'page': page,
        'size': size,
      };
      if (sort != null) queryParams['sort'] = sort;

      final response = await _apiClient.dio.get(
        '${AppConfig.assetServiceBaseUrl}${AppConfig.assetBasePath}/assets',
        queryParameters: queryParams,
      );

      if (response.statusCode == 200 && response.data != null) {
        final wrapper = ResponseWrapper.fromJson(
          response.data as Map<String, dynamic>,
          null,
        );
        if (wrapper.success && wrapper.data != null) {
          final data = wrapper.data as Map<String, dynamic>;
          final content = data['content'] as List?;
          if (content != null) {
            return content
                .map((item) => AssetMaster.fromJson(item as Map<String, dynamic>))
                .toList();
          }
        }
      }

      return [];
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  /// Update asset
  Future<AssetMaster> updateAsset(int assetId, AssetRequest request) async {
    try {
      final response = await _apiClient.dio.put(
        '${AppConfig.assetServiceBaseUrl}${AppConfig.assetBasePath}/assets/$assetId',
        data: request.toJson(),
      );

      if (response.statusCode == 200 && response.data != null) {
        final wrapper = ResponseWrapper.fromJson(
          response.data as Map<String, dynamic>,
          (json) => AssetMaster.fromJson(json),
        );
        if (wrapper.success && wrapper.data != null) {
          return wrapper.data!;
        }
        throw ApiException(
          message: wrapper.message,
          type: ApiExceptionType.badRequest,
        );
      }

      throw ApiException(
        message: 'Failed to update asset',
        type: ApiExceptionType.server,
      );
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  /// Delete asset
  Future<void> deleteAsset(int assetId) async {
    try {
      final response = await _apiClient.dio.delete(
        '${AppConfig.assetServiceBaseUrl}${AppConfig.assetBasePath}/assets/$assetId',
      );

      if (response.statusCode != 200) {
        throw ApiException(
          message: 'Failed to delete asset',
          type: ApiExceptionType.server,
        );
      }
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  // ============================================================
  // CATEGORY OPERATIONS
  // ============================================================

  /// Get all categories
  Future<List<Category>> getCategories() async {
    try {
      final response = await _apiClient.dio.get(
        '${AppConfig.assetServiceBaseUrl}${AppConfig.assetBasePath}/categories',
      );

      if (response.statusCode == 200 && response.data != null) {
        final wrapper = ResponseWrapper.fromJson(
          response.data as Map<String, dynamic>,
          null,
        );
        if (wrapper.success && wrapper.data != null) {
          final data = wrapper.data as List;
          return data
              .map((item) => Category.fromJson(item as Map<String, dynamic>))
              .toList();
        }
      }

      return [];
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  /// Create category
  Future<Category> createCategory(CategoryRequest request) async {
    try {
      final response = await _apiClient.dio.post(
        '${AppConfig.assetServiceBaseUrl}${AppConfig.assetBasePath}/categories',
        data: request.toJson(),
      );

      if (response.statusCode == 200 && response.data != null) {
        final wrapper = ResponseWrapper.fromJson(
          response.data as Map<String, dynamic>,
          (json) => Category.fromJson(json),
        );
        if (wrapper.success && wrapper.data != null) {
          return wrapper.data!;
        }
        throw ApiException(
          message: wrapper.message,
          type: ApiExceptionType.badRequest,
        );
      }

      throw ApiException(
        message: 'Failed to create category',
        type: ApiExceptionType.server,
      );
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  /// Bulk create categories
  Future<BulkUploadResponse<Category>> bulkCreateCategories(
    List<CategoryRequest> requests,
  ) async {
    try {
      final response = await _apiClient.dio.post(
        '${AppConfig.assetServiceBaseUrl}${AppConfig.assetBasePath}/categories/bulk',
        data: requests.map((r) => r.toJson()).toList(),
      );

      if (response.statusCode == 200 && response.data != null) {
        final wrapper = ResponseWrapper.fromJson(
          response.data as Map<String, dynamic>,
          null,
        );
        if (wrapper.success && wrapper.data != null) {
          final data = wrapper.data as Map<String, dynamic>;
          return BulkUploadResponse.fromJson(
            data,
            (json) => Category.fromJson(json),
          );
        }
      }

      throw ApiException(
        message: 'Bulk upload failed',
        type: ApiExceptionType.server,
      );
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  /// Upload categories via Excel
  Future<BulkUploadResponse<Category>> uploadCategoriesExcel(
    String filePath,
  ) async {
    try {
      final formData = FormData.fromMap({
        'file': await MultipartFile.fromFile(filePath),
      });

      final response = await _apiClient.dio.post(
        '${AppConfig.assetServiceBaseUrl}${AppConfig.assetBasePath}/categories/upload-excel',
        data: formData,
        options: Options(
          headers: {'Content-Type': 'multipart/form-data'},
        ),
      );

      if (response.statusCode == 200 && response.data != null) {
        final wrapper = ResponseWrapper.fromJson(
          response.data as Map<String, dynamic>,
          null,
        );
        if (wrapper.success && wrapper.data != null) {
          final data = wrapper.data as Map<String, dynamic>;
          return BulkUploadResponse.fromJson(
            data,
            (json) => Category.fromJson(json),
          );
        }
      }

      throw ApiException(
        message: 'Excel upload failed',
        type: ApiExceptionType.server,
      );
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  // ============================================================
  // ERROR HANDLING
  // ============================================================

  ApiException _handleError(DioException e) {
    if (e.response != null) {
      final statusCode = e.response!.statusCode;
      final data = e.response!.data;

      String message = 'An error occurred';
      if (data is Map<String, dynamic>) {
        message = data['message'] ?? 
                 data['error'] ?? 
                 message;
      } else if (data is String) {
        message = data;
      }

      ApiExceptionType type;
      switch (statusCode) {
        case 400:
          type = ApiExceptionType.badRequest;
          break;
        case 401:
          type = ApiExceptionType.unauthorized;
          break;
        case 403:
          type = ApiExceptionType.forbidden;
          break;
        case 404:
          type = ApiExceptionType.notFound;
          break;
        case 409:
          type = ApiExceptionType.conflict;
          break;
        case 422:
          type = ApiExceptionType.validation;
          break;
        case 500:
        case 502:
        case 503:
          type = ApiExceptionType.server;
          break;
        default:
          type = ApiExceptionType.unknown;
      }

      return ApiException(
        message: message,
        statusCode: statusCode,
        type: type,
        data: data,
      );
    }

    return ApiException(
      message: e.message ?? 'Network error',
      type: ApiExceptionType.network,
    );
  }
}

