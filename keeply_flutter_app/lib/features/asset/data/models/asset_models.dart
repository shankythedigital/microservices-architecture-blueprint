/// Asset Models
/// Data models for asset management

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

  Map<String, dynamic> toJson() => {
        'categoryId': categoryId,
        'subCategoryId': subCategoryId,
        'makeId': makeId,
        'modelId': modelId,
        'assetNameUdv': assetNameUdv,
        if (userId != null) 'userId': userId,
        if (username != null) 'username': username,
        if (projectType != null) 'projectType': projectType,
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
      assetId: json['assetId'] as int?,
      assetNameUdv: json['assetNameUdv'] as String,
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

