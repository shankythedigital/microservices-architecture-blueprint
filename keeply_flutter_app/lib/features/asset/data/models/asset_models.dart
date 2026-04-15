/// Asset Models
/// Data models for asset management
library;

class AssetRequest {
  final int categoryId;
  final int subCategoryId;
  final int makeId;
  final int modelId;
  final String assetNameUdv;
  final int? userId;
  final String? username;
  final String? projectType;

  AssetRequest({
    required this.categoryId,
    required this.subCategoryId,
    required this.makeId,
    required this.modelId,
    required this.assetNameUdv,
    this.userId,
    this.username,
    this.projectType,
  });

  /// Matches asset-service `AssetRequest` + Jackson binding to [AssetMaster] relations
  /// (same shape as `buildCreateAssetPayload` in `keeply_react_app/src/api/assetsApi.ts`).
  Map<String, dynamic> toJson() => {
        if (userId != null) 'userId': userId,
        if (username != null) 'username': username,
        if (projectType != null) 'projectType': projectType,
        'asset': {
          'assetNameUdv': assetNameUdv,
          'assetStatus': 'ACTIVE',
          'category': {'categoryId': categoryId},
          'subCategory': {'subCategoryId': subCategoryId},
          'make': {'makeId': makeId},
          'model': {'modelId': modelId},
        },
      };
}

class AssetMaster {
  final int? assetId;
  final String assetNameUdv;
  final Map<String, dynamic>? category;
  final Map<String, dynamic>? subCategory;
  final Map<String, dynamic>? make;
  final Map<String, dynamic>? model;
  final bool? active;
  final DateTime? createdAt;
  final DateTime? updatedAt;

  AssetMaster({
    this.assetId,
    required this.assetNameUdv,
    this.category,
    this.subCategory,
    this.make,
    this.model,
    this.active,
    this.createdAt,
    this.updatedAt,
  });

  factory AssetMaster.fromJson(Map<String, dynamic> json) {
    return AssetMaster(
      assetId: (json['assetId'] as num?)?.toInt(),
      assetNameUdv: json['assetNameUdv'] as String? ?? '',
      category: json['category'] as Map<String, dynamic>?,
      subCategory: json['subCategory'] as Map<String, dynamic>?,
      make: json['make'] as Map<String, dynamic>?,
      model: json['model'] as Map<String, dynamic>?,
      active: json['active'] as bool?,
      createdAt: json['createdAt'] != null
          ? DateTime.parse(json['createdAt'] as String)
          : null,
      updatedAt: json['updatedAt'] != null
          ? DateTime.parse(json['updatedAt'] as String)
          : null,
    );
  }
}

class CategoryRequest {
  final String categoryName;
  final String? description;

  CategoryRequest({
    required this.categoryName,
    this.description,
  });

  Map<String, dynamic> toJson() => {
        'categoryName': categoryName,
        if (description != null) 'description': description,
      };
}

class Category {
  final int? categoryId;
  final String categoryName;
  final String? description;
  final bool? active;

  Category({
    this.categoryId,
    required this.categoryName,
    this.description,
    this.active,
  });

  factory Category.fromJson(Map<String, dynamic> json) {
    return Category(
      categoryId: json['categoryId'] as int?,
      categoryName: json['categoryName'] as String,
      description: json['description'] as String?,
      active: json['active'] as bool?,
    );
  }
}

class BulkUploadResponse<T> {
  final int total;
  final int success;
  final int failed;
  final List<T>? results;
  final List<String>? errors;

  BulkUploadResponse({
    required this.total,
    required this.success,
    required this.failed,
    this.results,
    this.errors,
  });

  factory BulkUploadResponse.fromJson(
    Map<String, dynamic> json,
    T Function(Map<String, dynamic>) fromJsonT,
  ) {
    return BulkUploadResponse<T>(
      total: json['total'] as int,
      success: json['success'] as int,
      failed: json['failed'] as int,
      results: json['results'] != null
          ? (json['results'] as List).map((item) => fromJsonT(item as Map<String, dynamic>)).toList()
          : null,
      errors: json['errors'] != null
          ? List<String>.from(json['errors'] as List)
          : null,
    );
  }
}

/// Spring-style page payload from `GET .../assets/search` (matches React `SpringPage`).
class AssetSearchPage {
  final List<AssetMaster> content;
  final int totalElements;

  AssetSearchPage({
    required this.content,
    required this.totalElements,
  });
}

class ResponseWrapper<T> {
  final bool success;
  final String message;
  final T? data;

  ResponseWrapper({
    required this.success,
    required this.message,
    this.data,
  });

  factory ResponseWrapper.fromJson(
    Map<String, dynamic> json,
    T? Function(Map<String, dynamic>)? fromJsonT,
  ) {
    return ResponseWrapper<T>(
      success: json['success'] as bool,
      message: json['message'] as String,
      data: json['data'] != null && fromJsonT != null
          ? fromJsonT(json['data'] as Map<String, dynamic>)
          : null,
    );
  }
}

/// Asset Scan Response from API
/// Raw response from POST /api/asset/v1/scan
class AssetScanResponse {
  final int? assetId;
  final String? assetNameUdv;
  final String? serialNumber;
  final String? assetStatus;
  final String? purchaseDate;
  final String? categoryName;
  final String? subCategoryName;
  final String? makeName;
  final String? modelName;
  final String? matchedBy;
  final String? scanValue;
  final String? scanType;
  final String? source;

  AssetScanResponse({
    this.assetId,
    this.assetNameUdv,
    this.serialNumber,
    this.assetStatus,
    this.purchaseDate,
    this.categoryName,
    this.subCategoryName,
    this.makeName,
    this.modelName,
    this.matchedBy,
    this.scanValue,
    this.scanType,
    this.source,
  });

  factory AssetScanResponse.fromJson(Map<String, dynamic> json) {
    return AssetScanResponse(
      assetId: json['assetId'] as int?,
      assetNameUdv: json['assetNameUdv'] as String?,
      serialNumber: json['serialNumber'] as String?,
      assetStatus: json['assetStatus'] as String?,
      purchaseDate: json['purchaseDate'] as String?,
      categoryName: json['categoryName'] as String?,
      subCategoryName: json['subCategoryName'] as String?,
      makeName: json['makeName'] as String?,
      modelName: json['modelName'] as String?,
      matchedBy: json['matchedBy'] as String?,
      scanValue: json['scanValue'] as String?,
      scanType: json['scanType'] as String?,
      source: json['source'] as String?,
    );
  }

  /// Transform to required display format
  AssetScanResult toScanResult({String? source}) {
    final productName = [
      if (makeName != null && makeName!.isNotEmpty) makeName,
      if (modelName != null && modelName!.isNotEmpty) modelName,
      if (assetNameUdv != null && assetNameUdv!.isNotEmpty) assetNameUdv,
    ].where((e) => e != null && e.isNotEmpty).join(' ').trim();

    return AssetScanResult(
      assetCode: scanValue ?? assetNameUdv ?? serialNumber ?? '—',
      productName: productName.isNotEmpty ? productName : (assetNameUdv ?? '—'),
      category: categoryName ?? '—',
      subcategory: subCategoryName ?? '—',
      status: assetStatus ?? 'Active',
      source: source ?? this.source ?? 'Asset Database',
      rawResponse: this,
    );
  }
}

/// Asset Scan Result - Display format
/// Transformed response for UI display
class AssetScanResult {
  final String assetCode;
  final String productName;
  final String category;
  final String subcategory;
  final String status;
  final String source;
  final AssetScanResponse? rawResponse;

  AssetScanResult({
    required this.assetCode,
    required this.productName,
    required this.category,
    required this.subcategory,
    required this.status,
    required this.source,
    this.rawResponse,
  });

  Map<String, dynamic> toJson() => {
        'asset_code': assetCode,
        'product_name': productName,
        'category': category,
        'subcategory': subcategory,
        'status': status,
        'source': source,
      };
}

