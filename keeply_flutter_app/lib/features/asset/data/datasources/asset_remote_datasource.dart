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
  // ASSET SCAN (Barcode / QR Code)
  // ============================================================

  /// Scan asset by barcode or QR code value
  /// Looks up in internal asset database first, then optionally in external product APIs
  Future<AssetScanResult?> scanAsset(String scanValue, {String scanType = 'AUTO'}) async {
    try {
      // 1. Try internal asset database first
      final response = await _apiClient.dio.post(
        '${AppConfig.assetServiceBaseUrl}${AppConfig.assetBasePath}/scan',
        data: {'scanValue': scanValue, 'scanType': scanType},
      );

      if (response.statusCode == 200 && response.data != null) {
        final wrapper = ResponseWrapper.fromJson(
          response.data as Map<String, dynamic>,
          (json) => AssetScanResponse.fromJson(json),
        );
        if (wrapper.success && wrapper.data != null) {
          return wrapper.data!.toScanResult(source: 'Asset Database');
        }
      }

      // 2. If not found and scan value looks like product barcode (EAN/UPC/GTIN - numeric)
      if (_isProductBarcode(scanValue)) {
        final productResult = await _lookupProductBarcode(scanValue);
        if (productResult != null) return productResult;
      }

      return null;
    } on DioException catch (e) {
      if (e.response?.statusCode == 404) {
        // Not found in asset DB - try external product API for numeric barcodes
        if (_isProductBarcode(scanValue)) {
          final productResult = await _lookupProductBarcode(scanValue);
          if (productResult != null) return productResult;
        }
        return null;
      }
      throw _handleError(e);
    }
  }

  bool _isProductBarcode(String value) {
    final trimmed = value.trim();
    if (trimmed.isEmpty) return false;
    return RegExp(r'^\d{8,14}$').hasMatch(trimmed);
  }

  /// Lookup in external product databases (OpenFoodFacts, UPC Item DB)
  Future<AssetScanResult?> _lookupProductBarcode(String barcode) async {
    try {
      // OpenFoodFacts - free, no API key required
      final response = await _apiClient.dio.get(
        'https://world.openfoodfacts.org/api/v0/product/$barcode.json',
      );

      if (response.statusCode == 200 && response.data != null) {
        final data = response.data as Map<String, dynamic>;
        final status = data['status'] as int?;
        if (status == 1) {
          final product = data['product'] as Map<String, dynamic>?;
          if (product != null) {
            final productName = (product['product_name'] ?? product['product_name_en']) as String? ?? 'Unknown Product';
            final categories = product['categories'] as String? ?? '';
            final categoryParts = categories.split(',');
            final category = categoryParts.isNotEmpty ? categoryParts.first.trim() : '—';
            final subcategory = categoryParts.length > 1 ? categoryParts[1].trim() : '—';

            return AssetScanResult(
              assetCode: barcode,
              productName: productName,
              category: category,
              subcategory: subcategory,
              status: 'Product',
              source: 'OpenFoodFacts',
              rawResponse: null,
            );
          }
        }
      }
    } catch (_) {
      // Silently ignore - external API is optional fallback
    }
    return null;
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

  Map<String, dynamic> _asMap(dynamic responseData) {
    if (responseData is Map<String, dynamic>) return responseData;
    if (responseData is Map) return Map<String, dynamic>.from(responseData);
    return {};
  }

  /// `GET /api/asset/v1/assets/search` — same contract as React `searchAssets`.
  Future<AssetSearchPage> searchAssets({
    String? keyword,
    int page = 0,
    int size = 12,
  }) async {
    try {
      final response = await _apiClient.dio.get(
        '${AppConfig.assetServiceBaseUrl}${AppConfig.assetBasePath}/assets/search',
        queryParameters: {
          if (keyword != null && keyword.trim().isNotEmpty) 'keyword': keyword.trim(),
          'page': page,
          'size': size,
        },
      );

      if (response.statusCode == 200 && response.data != null) {
        final root = _asMap(response.data);
        if (root['success'] == false) {
          final msg = root['message'] as String? ?? 'Request failed';
          throw ApiException(message: msg, type: ApiExceptionType.badRequest);
        }
        final data = root['data'];
        if (data is! Map) {
          return AssetSearchPage(content: [], totalElements: 0);
        }
        final dataMap = Map<String, dynamic>.from(data);
        final content = dataMap['content'];
        final total = (dataMap['totalElements'] as num?)?.toInt() ?? 0;
        if (content is! List) {
          return AssetSearchPage(content: [], totalElements: total);
        }
        final list = content
            .whereType<Map>()
            .map((e) => AssetMaster.fromJson(Map<String, dynamic>.from(e)))
            .toList();
        return AssetSearchPage(content: list, totalElements: total);
      }

      throw ApiException(message: 'Asset search failed', type: ApiExceptionType.server);
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  /// `GET /api/asset/v1/user-asset-links/user/{userId}/assets` — React `fetchAssetsAssignedToUser`.
  Future<List<AssetMaster>> fetchAssetsAssignedToUser(int userId) async {
    try {
      final response = await _apiClient.dio.get(
        '${AppConfig.assetServiceBaseUrl}${AppConfig.assetBasePath}/user-asset-links/user/$userId/assets',
      );

      if (response.statusCode == 200 && response.data != null) {
        final root = _asMap(response.data);
        if (root['success'] == false) {
          final msg = root['message'] as String? ?? 'Request failed';
          throw ApiException(message: msg, type: ApiExceptionType.badRequest);
        }
        final raw = root['data'];
        if (raw is! List) return [];
        return raw
            .whereType<Map>()
            .map((e) => AssetMaster.fromJson(Map<String, dynamic>.from(e)))
            .toList();
      }

      return [];
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  /// `GET /api/asset/v1/userlinks/need-your-attention` — raw `data` map for dashboard.
  Future<Map<String, dynamic>?> getNeedYourAttention() async {
    try {
      final response = await _apiClient.dio.get(
        '${AppConfig.assetServiceBaseUrl}${AppConfig.assetBasePath}/userlinks/need-your-attention',
      );

      if (response.statusCode == 200 && response.data != null) {
        final root = _asMap(response.data);
        if (root['success'] == false) return null;
        final d = root['data'];
        if (d is Map<String, dynamic>) return d;
        if (d is Map) return Map<String, dynamic>.from(d);
      }
      return null;
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  /// React `createAssetWithDocument` — `POST /api/asset/v1/assets/with-document`.
  Future<AssetMaster> createAssetWithDocument({
    required Map<String, dynamic> requestPayload,
    required String documentBase64,
    required String docType,
  }) async {
    try {
      final response = await _apiClient.dio.post(
        '${AppConfig.assetServiceBaseUrl}${AppConfig.assetBasePath}/assets/with-document',
        data: {
          'request': requestPayload,
          'document': documentBase64,
          'docType': docType.trim(),
        },
      );

      if (response.statusCode == 200 && response.data != null) {
        final wrapper = ResponseWrapper.fromJson(
          response.data as Map<String, dynamic>,
          (json) => AssetMaster.fromJson(json),
        );
        if (wrapper.success && wrapper.data != null) {
          return wrapper.data!;
        }
        throw ApiException(message: wrapper.message, type: ApiExceptionType.badRequest);
      }

      throw ApiException(message: 'Create asset with document failed', type: ApiExceptionType.server);
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  /// React `createAssetComplete` — `POST /api/asset/v1/assets/complete` (multipart `FormData`).
  Future<Map<String, dynamic>> createAssetComplete(FormData formData) async {
    try {
      final response = await _apiClient.dio.post(
        '${AppConfig.assetServiceBaseUrl}${AppConfig.assetBasePath}/assets/complete',
        data: formData,
        options: Options(headers: const {'Accept': 'application/json'}),
      );

      if (response.statusCode == 200 && response.data != null) {
        final root = _asMap(response.data);
        if (root['success'] == false) {
          throw ApiException(
            message: root['message'] as String? ?? 'Request failed',
            type: ApiExceptionType.badRequest,
          );
        }
        final d = root['data'];
        if (d is Map<String, dynamic>) return d;
        if (d is Map) return Map<String, dynamic>.from(d);
        return {};
      }

      throw ApiException(message: 'Complete asset create failed', type: ApiExceptionType.server);
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

