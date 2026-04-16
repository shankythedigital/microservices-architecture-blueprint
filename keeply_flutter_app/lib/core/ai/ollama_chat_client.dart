import 'package:dio/dio.dart';
import 'package:keeply_app/core/config/app_config.dart';

/// HTTP client for [Ollama](https://github.com/ollama/ollama) `/api/chat` (non-streaming).
///
/// This uses **prompted inference** with your UI context (category, make, etc.). It does not
/// fine-tune weights on the device; connect Ollama on your LAN or host and choose a model.
class OllamaChatClient {
  OllamaChatClient({Dio? dio}) : _dio = dio ?? Dio();

  final Dio _dio;

  String get _base => AppConfig.ollamaBaseUrl.replaceAll(RegExp(r'/$'), '');

  /// Single assistant reply from Ollama.
  Future<String> chat({
    required List<Map<String, String>> messages,
    String? model,
  }) async {
    final url = '$_base/api/chat';
    final response = await _dio.post<Map<String, dynamic>>(
      url,
      data: <String, dynamic>{
        'model': model ?? AppConfig.ollamaModel,
        'messages': messages,
        'stream': false,
      },
      options: Options(
        headers: const {'Content-Type': 'application/json'},
        receiveTimeout: const Duration(seconds: 120),
        sendTimeout: const Duration(seconds: 30),
      ),
    );
    final data = response.data;
    if (data == null) return 'Empty response from Ollama.';
    final msg = data['message'];
    if (msg is Map && msg['content'] is String) {
      return (msg['content'] as String).trim();
    }
    return data.toString();
  }

  /// System prompt for “agent-style” appliance guidance (tips, FAQs, short video search phrases).
  static String applianceGuideSystemPrompt({
    String? category,
    String? subCategory,
    String? make,
    String? model,
  }) {
    final ctx = [
      if (category != null && category.isNotEmpty) 'Category: $category',
      if (subCategory != null && subCategory.isNotEmpty) 'Subcategory: $subCategory',
      if (make != null && make.isNotEmpty) 'Make: $make',
      if (model != null && model.isNotEmpty) 'Model: $model',
    ].join('\n');
    return '''
You are Keeply's in-app assistant. The user manages home appliances and warranties.
Use clear, concise language. If unsure, say what you would verify and suggest safe maintenance habits.

Catalog context:
${ctx.isEmpty ? '(none provided)' : ctx}

Respond with short sections using these headings exactly:
## Tips
## FAQs
## Knowledge
## Suggested searches
Under **Suggested searches**, list 3–6 concrete web or YouTube search phrases (no URLs required) that would help a newcomer learn care, manuals, or troubleshooting for this appliance type.
''';
  }
}
