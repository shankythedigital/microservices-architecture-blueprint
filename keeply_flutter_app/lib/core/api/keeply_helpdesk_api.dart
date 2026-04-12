import 'package:keeply_app/core/api/keeply_api_models.dart';
import 'package:keeply_app/core/api/keeply_service_url.dart';
import 'package:keeply_app/core/exceptions/api_exception.dart';
import 'package:keeply_app/core/network/api_client.dart';

/// Parity with:
/// - `keeply_react_app/src/api/issuesApi.ts`
/// - `keeply_react_app/src/api/knowledgeApi.ts`
/// - `keeply_react_app/src/api/faqsApi.ts`
/// - `keeply_react_app/src/api/queriesApi.ts`
class KeeplyHelpdeskApi {
  KeeplyHelpdeskApi({ApiClient? apiClient}) : _client = apiClient ?? ApiClient();

  final ApiClient _client;

  // --- issuesApi ---

  Future<List<IssueMasterItemDto>> listIssueMasters() async {
    return _getJsonList(
      keeplyApiUrl(KeeplyApiService.helpdesk, '/api/helpdesk/issue-master'),
      IssueMasterItemDto.fromJson,
    );
  }

  Future<IssueMasterItemDto> createIssueMaster(Map<String, dynamic> body) async {
    return _postJson(
      keeplyApiUrl(KeeplyApiService.helpdesk, '/api/helpdesk/issue-master'),
      body,
      IssueMasterItemDto.fromJson,
    );
  }

  Future<List<IssueItemDto>> listIssues() async {
    return _getJsonList(
      keeplyApiUrl(KeeplyApiService.helpdesk, '/api/helpdesk/issues'),
      IssueItemDto.fromJson,
    );
  }

  Future<List<IssueItemDto>> listMyIssues() async {
    return _getJsonList(
      keeplyApiUrl(KeeplyApiService.helpdesk, '/api/helpdesk/issues/my-issues'),
      IssueItemDto.fromJson,
    );
  }

  Future<IssueItemDto> getIssue(int id) async {
    return _getJsonOne(
      keeplyApiUrl(KeeplyApiService.helpdesk, '/api/helpdesk/issues/$id'),
      IssueItemDto.fromJson,
    );
  }

  Future<IssueItemDto> createIssue(Map<String, dynamic> body) async {
    return _postJson(
      keeplyApiUrl(KeeplyApiService.helpdesk, '/api/helpdesk/issues'),
      body,
      IssueItemDto.fromJson,
    );
  }

  // --- knowledgeApi ---

  Future<List<ServiceKnowledgeItemDto>> listKnowledgeByService(String service) async {
    return _getJsonList(
      keeplyApiUrl(KeeplyApiService.helpdesk, '/api/helpdesk/knowledge/service/$service'),
      ServiceKnowledgeItemDto.fromJson,
    );
  }

  Future<List<ServiceKnowledgeItemDto>> searchKnowledge(String service, String keyword) async {
    return _getJsonList(
      keeplyApiUrl(KeeplyApiService.helpdesk, '/api/helpdesk/knowledge/service/$service/search'),
      ServiceKnowledgeItemDto.fromJson,
      query: {'keyword': keyword},
    );
  }

  Future<ServiceKnowledgeItemDto> createKnowledge(Map<String, dynamic> body) async {
    return _postJson(
      keeplyApiUrl(KeeplyApiService.helpdesk, '/api/helpdesk/knowledge'),
      body,
      ServiceKnowledgeItemDto.fromJson,
    );
  }

  // --- faqsApi ---

  Future<List<FaqItemDto>> listFaqs() async {
    return _getJsonList(
      keeplyApiUrl(KeeplyApiService.helpdesk, '/api/helpdesk/faqs'),
      FaqItemDto.fromJson,
    );
  }

  Future<FaqItemDto> createFaq(Map<String, dynamic> body) async {
    return _postJson(
      keeplyApiUrl(KeeplyApiService.helpdesk, '/api/helpdesk/faqs'),
      body,
      FaqItemDto.fromJson,
    );
  }

  Future<List<FaqItemDto>> searchFaqs(String keyword) async {
    return _getJsonList(
      keeplyApiUrl(KeeplyApiService.helpdesk, '/api/helpdesk/faqs/search'),
      FaqItemDto.fromJson,
      query: {'keyword': keyword},
    );
  }

  Future<List<FaqItemDto>> searchFaqsByService(String service, String keyword) async {
    return _getJsonList(
      keeplyApiUrl(KeeplyApiService.helpdesk, '/api/helpdesk/faqs/service/$service/search'),
      FaqItemDto.fromJson,
      query: {'keyword': keyword},
    );
  }

  Future<void> markFaqHelpful(int id) async {
    final response = await _client.dio.post(
      keeplyApiUrl(KeeplyApiService.helpdesk, '/api/helpdesk/faqs/$id/helpful'),
    );
    if (response.statusCode != null && response.statusCode! >= 200 && response.statusCode! < 300) return;
    final root = jsonMapOf(response.data);
    throw ApiException(
      message: root['message'] as String? ?? 'Request failed',
      statusCode: response.statusCode,
      type: ApiExceptionType.badRequest,
    );
  }

  // --- queriesApi ---

  Future<List<HelpdeskQueryItemDto>> listMyQueries() async {
    return _getJsonList(
      keeplyApiUrl(KeeplyApiService.helpdesk, '/api/helpdesk/queries/my-queries'),
      HelpdeskQueryItemDto.fromJson,
    );
  }

  Future<HelpdeskQueryItemDto> createQuery(Map<String, dynamic> body) async {
    return _postJson(
      keeplyApiUrl(KeeplyApiService.helpdesk, '/api/helpdesk/queries'),
      body,
      HelpdeskQueryItemDto.fromJson,
    );
  }

  // --- helpers ---

  Future<List<T>> _getJsonList<T>(
    String url,
    T Function(Map<String, dynamic>) fromJson, {
    Map<String, dynamic>? query,
  }) async {
    final response = await _client.dio.get(url, queryParameters: query);
    final data = response.data;
    if (data is List) {
      return data.whereType<Map>().map((e) => fromJson(Map<String, dynamic>.from(e))).toList();
    }
    if (data is Map<String, dynamic>) {
      final root = jsonMapOf(data);
      if (root['success'] == false) {
        throw ApiException(message: root['message'] as String? ?? 'Failed', type: ApiExceptionType.badRequest);
      }
      final inner = root['data'];
      if (inner is List) {
        return inner.whereType<Map>().map((e) => fromJson(Map<String, dynamic>.from(e))).toList();
      }
    }
    return [];
  }

  Future<T> _getJsonOne<T>(String url, T Function(Map<String, dynamic>) fromJson) async {
    final response = await _client.dio.get(url);
    final data = response.data;
    if (data is Map<String, dynamic>) {
      return fromJson(data);
    }
    throw ApiException(message: 'Unexpected response', type: ApiExceptionType.server);
  }

  Future<T> _postJson<T>(
    String url,
    Map<String, dynamic> body,
    T Function(Map<String, dynamic>) fromJson,
  ) async {
    final response = await _client.dio.post(url, data: body);
    final data = response.data;
    if (data is Map<String, dynamic>) {
      return fromJson(data);
    }
    throw ApiException(message: 'Unexpected response', type: ApiExceptionType.server);
  }
}
