/// Maps `keeply_react_app/src/api/*.ts` modules to Dart entry points in `keeply_flutter_app`.
///
/// | React (TS) | Flutter (Dart) |
/// |------------|----------------|
/// | `api/http.ts` | `core/network/api_client.dart` + `interceptors/*` |
/// | `config.ts` `url(service, path)` | `core/api/keeply_service_url.dart` `keeplyApiUrl` |
/// | `api/types.ts` | `core/api/keeply_api_models.dart` + `features/auth/data/models/auth_models.dart` |
/// | `api/authApi.ts` | `features/auth/data/datasources/auth_remote_datasource.dart` + `core/api/keeply_auth_api.dart` |
/// | `api/assetsApi.ts` | `features/asset/data/datasources/asset_remote_datasource.dart` |
/// | `api/documentsApi.ts` | `core/api/keeply_documents_api.dart` |
/// | `api/notificationsApi.ts` | `core/api/keeply_notifications_api.dart` |
/// | `api/issuesApi.ts` + `knowledgeApi.ts` + `faqsApi.ts` + `queriesApi.ts` | `core/api/keeply_helpdesk_api.dart` |
/// | `api/masterDataApi.ts` | `core/api/keeply_master_data_api.dart` |
/// | `api/categoriesApi.ts` | `core/api/keeply_categories_api.dart` |
/// | `constants/helpdesk.ts` | `core/constants/helpdesk_related_service.dart` |
/// | `constants/project.ts` | `core/constants/project_constants.dart` |

export 'keeply_api_models.dart';
export 'keeply_auth_api.dart';
export 'keeply_categories_api.dart';
export 'keeply_documents_api.dart';
export 'keeply_helpdesk_api.dart';
export 'keeply_master_data_api.dart';
export 'keeply_notifications_api.dart';
export 'keeply_service_url.dart';
