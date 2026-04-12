import 'package:keeply_app/core/api/keeply_helpdesk_api.dart';

/// Back-compat facade over [KeeplyHelpdeskApi] (maps `keeply_react_app/src/api/issuesApi.ts` entry used by dashboard).
class HelpdeskRemoteDataSource {
  HelpdeskRemoteDataSource({KeeplyHelpdeskApi? api}) : _api = api ?? KeeplyHelpdeskApi();

  final KeeplyHelpdeskApi _api;

  /// `GET /api/helpdesk/issues/my-issues`
  Future<List<Map<String, dynamic>>> listMyIssues() async {
    final items = await _api.listMyIssues();
    return items
        .map(
          (e) => {
            'id': e.id,
            'title': e.title,
            'description': e.description,
            'status': e.status,
            'priority': e.priority,
            'relatedService': e.relatedService,
            'createdAt': e.createdAt,
            'updatedAt': e.updatedAt,
            'assetId': e.assetId,
            'issueMasterId': e.issueMasterId,
            'loginUserId': e.loginUserId,
          },
        )
        .toList();
  }
}
