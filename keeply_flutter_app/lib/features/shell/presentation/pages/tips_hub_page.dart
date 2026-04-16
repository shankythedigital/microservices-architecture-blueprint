import 'package:flutter/material.dart';
import 'package:keeply_app/core/ai/keeply_ai_knowledge_base.dart';
import 'package:keeply_app/core/ai/ollama_chat_client.dart';
import 'package:keeply_app/core/config/app_config.dart';
import 'package:keeply_app/features/asset/data/datasources/asset_remote_datasource.dart';
import 'package:keeply_app/features/asset/data/models/asset_models.dart';

/// React route `/home/tips` — tips, knowledge, and **Ollama**-guided newcomer content from master-data labels.
class TipsHubPage extends StatefulWidget {
  const TipsHubPage({super.key});

  @override
  State<TipsHubPage> createState() => _TipsHubPageState();
}

class _TipsHubPageState extends State<TipsHubPage> {
  final _ds = AssetRemoteDataSource();
  final _client = OllamaChatClient();
  final _kb = KeeplyAiKnowledgeBase.instance;
  final _makeCtrl = TextEditingController();
  final _modelCtrl = TextEditingController();
  List<Category> _categories = [];
  bool _catLoading = true;
  String? _selectedCategory;
  bool _busy = false;
  String? _ollamaOut;
  List<Map<String, dynamic>> _topFaqs = [];
  List<Map<String, dynamic>> _topKnowledge = [];

  @override
  void initState() {
    super.initState();
    _loadCats();
    _loadKbRows();
  }

  @override
  void dispose() {
    _makeCtrl.dispose();
    _modelCtrl.dispose();
    super.dispose();
  }

  Future<void> _loadCats() async {
    try {
      final c = await _ds.getCategories();
      if (!mounted) return;
      setState(() {
        _categories = c;
        _catLoading = false;
        if (_selectedCategory == null && c.isNotEmpty) {
          _selectedCategory = c.first.categoryName;
        }
      });
    } catch (_) {
      if (mounted) setState(() => _catLoading = false);
    }
  }

  Future<void> _loadKbRows() async {
    final faqs = await _kb.topEntries(type: 'faq', limit: 6);
    final know = await _kb.topEntries(type: 'knowledge', limit: 6);
    if (!mounted) return;
    setState(() {
      _topFaqs = faqs;
      _topKnowledge = know;
    });
  }

  Future<void> _runOllamaGuide() async {
    if (_busy) return;
    setState(() {
      _busy = true;
      _ollamaOut = null;
    });
    try {
      final system = OllamaChatClient.applianceGuideSystemPrompt(
        category: _selectedCategory,
        subCategory: null,
        make: _makeCtrl.text.trim().isEmpty ? null : _makeCtrl.text.trim(),
        model: _modelCtrl.text.trim().isEmpty ? null : _modelCtrl.text.trim(),
      );
      final user = '''
Generate practical guidance for someone new to Keeply managing this appliance type.
If category is unknown, still give general safe-use and warranty tips.
''';
      final text = await _client.chat(
        messages: [
          {'role': 'system', 'content': system},
          {'role': 'user', 'content': user},
        ],
      );
      if (!mounted) return;
      setState(() => _ollamaOut = text);
      await _kb.recordEntry(
        type: 'knowledge',
        question:
            'category=${_selectedCategory ?? ''};make=${_makeCtrl.text.trim()};model=${_modelCtrl.text.trim()}',
        answer: text,
        relatedService: 'ASSET_SERVICE',
      );
      await _loadKbRows();
    } catch (e) {
      if (!mounted) return;
      setState(() => _ollamaOut = 'Could not reach Ollama: $e\nHost: ${AppConfig.ollamaBaseUrl}, model: ${AppConfig.ollamaModel}');
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    final scheme = Theme.of(context).colorScheme;
    final canPop = Navigator.of(context).canPop();

    final body = ListView(
      padding: const EdgeInsets.fromLTRB(18, 18, 18, 100),
      children: [
        if (!canPop) ...[
          Text('Tips & knowledge', style: t.headlineSmall?.copyWith(fontWeight: FontWeight.w800)),
          const SizedBox(height: 8),
        ],
        Text(
          'Browse guidance for using asset, auth, notification, and helpdesk services.',
          style: t.bodyMedium?.copyWith(color: scheme.onSurfaceVariant, height: 1.45),
        ),
        const SizedBox(height: 20),
        Text('AI guide (Ollama)', style: t.titleSmall?.copyWith(fontWeight: FontWeight.w800)),
        const SizedBox(height: 6),
        Text(
          'Pick catalog labels, then ask Ollama for tips, FAQs, knowledge bullets, and suggested video search phrases. '
          'This uses **prompted inference** with your selections — it does not retrain the base model.',
          style: t.bodySmall?.copyWith(color: scheme.onSurfaceVariant, height: 1.4),
        ),
        const SizedBox(height: 12),
        if (_catLoading)
          const Padding(
            padding: EdgeInsets.symmetric(vertical: 12),
            child: Center(child: CircularProgressIndicator(strokeWidth: 2)),
          )
        else if (_categories.isEmpty)
          Text('No categories loaded from asset-service.', style: t.bodySmall?.copyWith(color: scheme.error))
        else
          InputDecorator(
            decoration: const InputDecoration(
              labelText: 'Category',
              border: OutlineInputBorder(),
            ),
            child: DropdownButtonHideUnderline(
              child: DropdownButton<String>(
                isExpanded: true,
                value: _selectedCategory != null && _categories.any((c) => c.categoryName == _selectedCategory)
                    ? _selectedCategory
                    : _categories.first.categoryName,
                items: [
                  for (final c in _categories)
                    DropdownMenuItem(value: c.categoryName, child: Text(c.categoryName)),
                ],
                onChanged: (v) => setState(() => _selectedCategory = v),
              ),
            ),
          ),
        const SizedBox(height: 10),
        TextField(
          controller: _makeCtrl,
          decoration: const InputDecoration(
            labelText: 'Make (optional)',
            border: OutlineInputBorder(),
          ),
        ),
        const SizedBox(height: 10),
        TextField(
          controller: _modelCtrl,
          decoration: const InputDecoration(
            labelText: 'Model (optional)',
            border: OutlineInputBorder(),
          ),
        ),
        const SizedBox(height: 12),
        FilledButton.icon(
          onPressed: _busy ? null : _runOllamaGuide,
          icon: _busy
              ? const SizedBox(width: 18, height: 18, child: CircularProgressIndicator(strokeWidth: 2))
              : const Icon(Icons.auto_stories_outlined),
          label: Text(_busy ? 'Generating…' : 'Generate tips & FAQs'),
        ),
        if (_ollamaOut != null) ...[
          const SizedBox(height: 16),
          SelectableText(_ollamaOut!, style: t.bodyMedium?.copyWith(height: 1.45)),
        ],
        if (_topFaqs.isNotEmpty || _topKnowledge.isNotEmpty) ...[
          const SizedBox(height: 24),
          Text('Local knowledge tables', style: t.titleSmall?.copyWith(fontWeight: FontWeight.w800)),
          const SizedBox(height: 8),
          if (_topFaqs.isNotEmpty) ...[
            Text('Top FAQs', style: t.labelLarge?.copyWith(fontWeight: FontWeight.w700)),
            const SizedBox(height: 6),
            for (final row in _topFaqs.take(4))
              Padding(
                padding: const EdgeInsets.only(bottom: 6),
                child: Text(
                  '• ${(row['question'] ?? '').toString()}',
                  style: t.bodySmall?.copyWith(color: scheme.onSurfaceVariant),
                ),
              ),
          ],
          if (_topKnowledge.isNotEmpty) ...[
            const SizedBox(height: 10),
            Text('Top knowledge prompts', style: t.labelLarge?.copyWith(fontWeight: FontWeight.w700)),
            const SizedBox(height: 6),
            for (final row in _topKnowledge.take(4))
              Padding(
                padding: const EdgeInsets.only(bottom: 6),
                child: Text(
                  '• ${(row['question'] ?? '').toString()}',
                  style: t.bodySmall?.copyWith(color: scheme.onSurfaceVariant),
                ),
              ),
          ],
        ],
        const SizedBox(height: 24),
        Text(
          'Open Help & support from the home screen summary, the dashboard, or your account hub to search FAQs and contact the helpdesk.',
          style: t.bodySmall?.copyWith(color: scheme.onSurfaceVariant, height: 1.4),
        ),
      ],
    );

    if (!canPop) return body;
    return Scaffold(
      appBar: AppBar(title: const Text('Tips & knowledge')),
      body: body,
    );
  }
}
