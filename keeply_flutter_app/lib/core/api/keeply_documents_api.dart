import 'package:dio/dio.dart';
import 'package:keeply_app/core/api/keeply_api_models.dart';
import 'package:keeply_app/core/api/keeply_service_url.dart';
import 'package:keeply_app/core/constants/project_constants.dart';
import 'package:keeply_app/core/exceptions/api_exception.dart';
import 'package:keeply_app/core/network/api_client.dart';

/// Parity with `keeply_react_app/src/api/documentsApi.ts`.
abstract final class KeeplyDocumentsApi {
  /// `document_type_master` code for user appliance photos.
  static const String assetPhotoDocType = 'asset_photo';
}

/// Asset-service document download & upload (React `documentsApi.ts`).
class KeeplyDocumentsRemoteApi {
  KeeplyDocumentsRemoteApi({ApiClient? apiClient}) : _client = apiClient ?? ApiClient();

  final ApiClient _client;

  /// `GET /api/asset/v1/documents/download/{documentId}` — returns raw bytes + content-type.
  Future<({List<int> bytes, String? contentType})> fetchDocumentBytes(int documentId) async {
    final response = await _client.dio.get<List<int>>(
      keeplyApiUrl(KeeplyApiService.asset, '/api/asset/v1/documents/download/$documentId'),
      options: Options(responseType: ResponseType.bytes, headers: const {'Accept': '*/*'}),
    );
    if (response.statusCode != 200 || response.data == null) {
      throw ApiException(
        message: 'Download failed',
        statusCode: response.statusCode,
        type: ApiExceptionType.badRequest,
      );
    }
    final ct = response.headers.value(Headers.contentTypeHeader);
    return (bytes: response.data!, contentType: ct);
  }

  /// `POST /api/asset/v1/documents/upload` (React `uploadAssetDocument`).
  Future<AssetDocumentSummaryDto> uploadAssetDocument({
    required int assetId,
    required int userId,
    required String username,
    required String filePath,
    required String fileName,
    required String docType,
    String projectType = ProjectConstants.defaultProjectType,
  }) async {
    final form = FormData.fromMap({
      'file': await MultipartFile.fromFile(filePath, filename: fileName),
      'entityType': 'ASSET',
      'entityId': assetId.toString(),
      'userId': userId.toString(),
      'username': username,
      'projectType': projectType.trim(),
      'docType': docType.trim(),
    });

    final response = await _client.dio.post(
      keeplyApiUrl(KeeplyApiService.asset, '/api/asset/v1/documents/upload'),
      data: form,
      options: Options(headers: const {'Accept': 'application/json'}),
    );

    final root = jsonMapOf(response.data);
    if (response.statusCode != 200 || root['success'] == false) {
      throw ApiException(
        message: root['message'] as String? ?? 'Upload failed',
        type: ApiExceptionType.badRequest,
      );
    }
    final data = root['data'];
    if (data is! Map<String, dynamic>) {
      throw ApiException(message: 'Invalid upload response', type: ApiExceptionType.server);
    }
    return AssetDocumentSummaryDto.fromJson(data);
  }
}
