/// Shared JSON helpers (React `parseJson` / `ResponseWrapper`).
Map<String, dynamic> jsonMapOf(dynamic value) {
  if (value is Map<String, dynamic>) return value;
  if (value is Map) return Map<String, dynamic>.from(value);
  return {};
}

/// Spring page shape from asset search (React `SpringPage<T>`).
class SpringPageDto<T> {
  SpringPageDto({
    required this.content,
    required this.totalElements,
    required this.totalPages,
    required this.size,
    required this.number,
  });

  final List<T> content;
  final int totalElements;
  final int totalPages;
  final int size;
  final int number;

  static SpringPageDto<Map<String, dynamic>> fromJsonMap(Map<String, dynamic> json) {
    final content = (json['content'] as List?)?.whereType<Map>().map((e) => Map<String, dynamic>.from(e)).toList() ??
        <Map<String, dynamic>>[];
    return SpringPageDto(
      content: content,
      totalElements: (json['totalElements'] as num?)?.toInt() ?? 0,
      totalPages: (json['totalPages'] as num?)?.toInt() ?? 0,
      size: (json['size'] as num?)?.toInt() ?? 0,
      number: (json['number'] as num?)?.toInt() ?? 0,
    );
  }
}

/// React `types.ts` — `NotificationItem`
class NotificationItemDto {
  NotificationItemDto({
    required this.id,
    this.title,
    this.message,
    this.templateCode,
    this.createdAt,
    this.read,
    this.priority,
  });

  final int id;
  final String? title;
  final String? message;
  final String? templateCode;
  final String? createdAt;
  final bool? read;
  final String? priority;

  factory NotificationItemDto.fromJson(Map<String, dynamic> json) {
    return NotificationItemDto(
      id: (json['id'] as num?)?.toInt() ?? 0,
      title: json['title'] as String?,
      message: json['message'] as String?,
      templateCode: json['templateCode'] as String?,
      createdAt: json['createdAt'] as String?,
      read: json['read'] as bool? ?? json['isRead'] as bool?,
      priority: json['priority'] as String?,
    );
  }
}

/// React `types.ts` — `IssueItem`
class IssueItemDto {
  IssueItemDto({
    required this.id,
    this.title,
    this.description,
    this.status,
    this.priority,
    this.relatedService,
    this.createdAt,
    this.updatedAt,
    this.resolvedAt,
    this.resolution,
    this.reportedBy,
    this.assignedTo,
    this.assetId,
    this.issueMasterId,
    this.loginUserId,
  });

  final int id;
  final String? title;
  final String? description;
  final String? status;
  final String? priority;
  final String? relatedService;
  final String? createdAt;
  final String? updatedAt;
  final String? resolvedAt;
  final String? resolution;
  final String? reportedBy;
  final String? assignedTo;
  final int? assetId;
  final int? issueMasterId;
  final int? loginUserId;

  factory IssueItemDto.fromJson(Map<String, dynamic> json) {
    return IssueItemDto(
      id: (json['id'] as num?)?.toInt() ?? 0,
      title: json['title'] as String?,
      description: json['description'] as String?,
      status: json['status'] as String?,
      priority: json['priority'] as String?,
      relatedService: json['relatedService'] as String?,
      createdAt: json['createdAt'] as String?,
      updatedAt: json['updatedAt'] as String?,
      resolvedAt: json['resolvedAt'] as String?,
      resolution: json['resolution'] as String?,
      reportedBy: json['reportedBy'] as String?,
      assignedTo: json['assignedTo'] as String?,
      assetId: (json['assetId'] as num?)?.toInt(),
      issueMasterId: (json['issueMasterId'] as num?)?.toInt(),
      loginUserId: (json['loginUserId'] as num?)?.toInt(),
    );
  }
}

/// React `types.ts` — `HelpdeskQueryItem`
class HelpdeskQueryItemDto {
  HelpdeskQueryItemDto({
    required this.id,
    this.question,
    this.answer,
    this.status,
    this.relatedService,
    this.createdAt,
    this.answeredAt,
    this.loginUserId,
  });

  final int id;
  final String? question;
  final String? answer;
  final String? status;
  final String? relatedService;
  final String? createdAt;
  final String? answeredAt;
  final int? loginUserId;

  factory HelpdeskQueryItemDto.fromJson(Map<String, dynamic> json) {
    return HelpdeskQueryItemDto(
      id: (json['id'] as num?)?.toInt() ?? 0,
      question: json['question'] as String?,
      answer: json['answer'] as String?,
      status: json['status'] as String?,
      relatedService: json['relatedService'] as String?,
      createdAt: json['createdAt'] as String?,
      answeredAt: json['answeredAt'] as String?,
      loginUserId: (json['loginUserId'] as num?)?.toInt(),
    );
  }
}

/// React `faqsApi.ts` — `FaqItem`
class FaqItemDto {
  FaqItemDto({
    this.id,
    this.question,
    this.answer,
    this.category,
    this.relatedService,
    this.helpfulCount,
    this.viewCount,
    this.isFavourite,
  });

  final int? id;
  final String? question;
  final String? answer;
  final String? category;
  final String? relatedService;
  final int? helpfulCount;
  final int? viewCount;
  final bool? isFavourite;

  factory FaqItemDto.fromJson(Map<String, dynamic> json) {
    return FaqItemDto(
      id: (json['id'] as num?)?.toInt(),
      question: json['question'] as String?,
      answer: json['answer'] as String?,
      category: json['category'] as String?,
      relatedService: json['relatedService'] as String?,
      helpfulCount: (json['helpfulCount'] as num?)?.toInt(),
      viewCount: (json['viewCount'] as num?)?.toInt(),
      isFavourite: json['isFavourite'] as bool?,
    );
  }
}

/// React `knowledgeApi.ts` — `ServiceKnowledgeItem`
class ServiceKnowledgeItemDto {
  ServiceKnowledgeItemDto({
    this.id,
    this.service,
    this.topic,
    this.content,
    this.category,
    this.apiEndpoints,
    this.commonIssues,
    this.troubleshootingSteps,
    this.createdAt,
    this.updatedAt,
  });

  final int? id;
  final String? service;
  final String? topic;
  final String? content;
  final String? category;
  final String? apiEndpoints;
  final String? commonIssues;
  final String? troubleshootingSteps;
  final String? createdAt;
  final String? updatedAt;

  factory ServiceKnowledgeItemDto.fromJson(Map<String, dynamic> json) {
    return ServiceKnowledgeItemDto(
      id: (json['id'] as num?)?.toInt(),
      service: json['service'] as String?,
      topic: json['topic'] as String?,
      content: json['content'] as String?,
      category: json['category'] as String?,
      apiEndpoints: json['apiEndpoints'] as String?,
      commonIssues: json['commonIssues'] as String?,
      troubleshootingSteps: json['troubleshootingSteps'] as String?,
      createdAt: json['createdAt'] as String?,
      updatedAt: json['updatedAt'] as String?,
    );
  }
}

/// React `issuesApi.ts` — `IssueMasterItem`
class IssueMasterItemDto {
  IssueMasterItemDto({
    this.id,
    this.issueTitle,
    this.issueDescription,
    this.categoryId,
    this.subCategoryId,
    this.componentId,
    this.sparePartId,
  });

  final int? id;
  final String? issueTitle;
  final String? issueDescription;
  final int? categoryId;
  final int? subCategoryId;
  final int? componentId;
  final int? sparePartId;

  factory IssueMasterItemDto.fromJson(Map<String, dynamic> json) {
    return IssueMasterItemDto(
      id: (json['id'] as num?)?.toInt(),
      issueTitle: json['issueTitle'] as String?,
      issueDescription: json['issueDescription'] as String?,
      categoryId: (json['categoryId'] as num?)?.toInt(),
      subCategoryId: (json['subCategoryId'] as num?)?.toInt(),
      componentId: (json['componentId'] as num?)?.toInt(),
      sparePartId: (json['sparePartId'] as num?)?.toInt(),
    );
  }
}

/// React `categoriesApi.ts` — `CategoryDto`
class CategoryDto {
  CategoryDto({this.categoryId, this.categoryName, this.description, this.imageUrl});

  final int? categoryId;
  final String? categoryName;
  final String? description;
  final String? imageUrl;

  factory CategoryDto.fromJson(Map<String, dynamic> json) {
    return CategoryDto(
      categoryId: (json['categoryId'] as num?)?.toInt(),
      categoryName: json['categoryName'] as String?,
      description: json['description'] as String?,
      imageUrl: json['imageUrl'] as String?,
    );
  }
}

/// React `masterDataApi.ts`
class SubCategoryDto {
  SubCategoryDto({this.subCategoryId, this.subCategoryName, this.imageUrl, this.categoryId});

  final int? subCategoryId;
  final String? subCategoryName;
  final String? imageUrl;
  final int? categoryId;

  factory SubCategoryDto.fromJson(Map<String, dynamic> json) {
    final cat = json['category'];
    int? cid;
    if (cat is Map) {
      final cm = Map<String, dynamic>.from(cat);
      cid = (cm['categoryId'] as num?)?.toInt();
      cid ??= (cm['id'] as num?)?.toInt();
    }
    cid ??= (json['categoryId'] as num?)?.toInt();
    return SubCategoryDto(
      subCategoryId: (json['subCategoryId'] as num?)?.toInt(),
      subCategoryName: json['subCategoryName'] as String?,
      imageUrl: json['imageUrl'] as String?,
      categoryId: cid,
    );
  }
}

class MakeDto {
  MakeDto({this.makeId, this.makeName, this.imageUrl, this.subCategoryId});

  final int? makeId;
  final String? makeName;
  final String? imageUrl;
  final int? subCategoryId;

  factory MakeDto.fromJson(Map<String, dynamic> json) {
    final sc = json['subCategory'];
    int? sid;
    if (sc is Map) {
      final sm = Map<String, dynamic>.from(sc);
      sid = (sm['subCategoryId'] as num?)?.toInt();
      sid ??= (sm['id'] as num?)?.toInt();
    }
    sid ??= (json['subCategoryId'] as num?)?.toInt();
    return MakeDto(
      makeId: (json['makeId'] as num?)?.toInt(),
      makeName: json['makeName'] as String?,
      imageUrl: json['imageUrl'] as String?,
      subCategoryId: sid,
    );
  }
}

class ModelDto {
  ModelDto({this.modelId, this.modelName, this.imageUrl, this.makeId});

  final int? modelId;
  final String? modelName;
  final String? imageUrl;
  final int? makeId;

  factory ModelDto.fromJson(Map<String, dynamic> json) {
    final mk = json['make'];
    int? mid;
    if (mk is Map) {
      final mm = Map<String, dynamic>.from(mk);
      mid = (mm['makeId'] as num?)?.toInt();
      mid ??= (mm['id'] as num?)?.toInt();
    }
    return ModelDto(
      modelId: (json['modelId'] as num?)?.toInt(),
      modelName: json['modelName'] as String?,
      imageUrl: json['imageUrl'] as String?,
      makeId: mid ?? (json['makeId'] as num?)?.toInt(),
    );
  }
}

/// React `documentsApi.ts` — `AssetDocumentSummary`
class AssetDocumentSummaryDto {
  AssetDocumentSummaryDto({
    this.documentId,
    this.fileName,
    this.docType,
    this.entityType,
    this.entityId,
  });

  final int? documentId;
  final String? fileName;
  final String? docType;
  final String? entityType;
  final int? entityId;

  factory AssetDocumentSummaryDto.fromJson(Map<String, dynamic> json) {
    return AssetDocumentSummaryDto(
      documentId: (json['documentId'] as num?)?.toInt(),
      fileName: json['fileName'] as String?,
      docType: json['docType'] as String?,
      entityType: json['entityType'] as String?,
      entityId: (json['entityId'] as num?)?.toInt(),
    );
  }
}
