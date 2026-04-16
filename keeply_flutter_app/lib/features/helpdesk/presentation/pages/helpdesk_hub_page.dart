import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:keeply_app/core/api/keeply_api_models.dart';
import 'package:keeply_app/core/api/keeply_helpdesk_api.dart';
import 'package:keeply_app/core/exceptions/api_exception.dart';
import 'package:keeply_app/core/sync/app_data_refresh_cubit.dart';
import 'package:keeply_app/core/theme/keeply_tokens.dart';
import 'package:keeply_app/features/shell/presentation/pages/tips_hub_page.dart';

/// Help & support hub — helpdesk-service integration aligned with React `HelpdeskPage.tsx`.
class HelpdeskHubPage extends StatefulWidget {
  const HelpdeskHubPage({super.key});

  @override
  State<HelpdeskHubPage> createState() => _HelpdeskHubPageState();
}

class _HelpdeskHubPageState extends State<HelpdeskHubPage> {
  final KeeplyHelpdeskApi _api = KeeplyHelpdeskApi();
  final TextEditingController _searchCtrl = TextEditingController();

  bool _loading = true;
  String? _err;
  int? _issueCount;
  int? _queryCount;
  List<FaqItemDto> _faqPreview = [];
  List<FaqItemDto>? _searchResults;
  bool _searchBusy = false;
  int? _helpfulBusyId;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => _load());
  }

  @override
  void dispose() {
    _searchCtrl.dispose();
    super.dispose();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _err = null;
    });
    try {
      final issues = await _safeList(() => _api.listMyIssues());
      final queries = await _safeList(() => _api.listMyQueries());
      final faqs = await _safeList(() => _api.listFaqs());
      if (!mounted) return;
      setState(() {
        _issueCount = issues.length;
        _queryCount = queries.length;
        _faqPreview = faqs.take(5).toList();
        _loading = false;
      });
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _loading = false;
        _err = e is ApiException ? e.message : 'Could not load helpdesk';
      });
    }
  }

  Future<List<T>> _safeList<T>(Future<List<T>> Function() fn) async {
    try {
      return await fn();
    } catch (_) {
      return [];
    }
  }

  Future<void> _onSearch() async {
    final q = _searchCtrl.text.trim();
    if (q.isEmpty) {
      setState(() => _searchResults = null);
      return;
    }
    setState(() {
      _searchBusy = true;
      _err = null;
    });
    try {
      final list = await _api.searchFaqs(q);
      if (!mounted) return;
      setState(() {
        _searchResults = list;
        _searchBusy = false;
      });
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _searchBusy = false;
        _err = e is ApiException ? e.message : 'Search failed';
        _searchResults = [];
      });
    }
  }

  Future<void> _onHelpful(int? id) async {
    if (id == null) return;
    setState(() => _helpfulBusyId = id);
    try {
      await _api.markFaqHelpful(id);
      if (!mounted) return;
      List<FaqItemDto> bumpList(List<FaqItemDto> list) {
        return list
            .map(
              (e) => e.id == id
                  ? FaqItemDto(
                      id: e.id,
                      question: e.question,
                      answer: e.answer,
                      category: e.category,
                      relatedService: e.relatedService,
                      helpfulCount: (e.helpfulCount ?? 0) + 1,
                      viewCount: e.viewCount,
                      isFavourite: e.isFavourite,
                    )
                  : e,
            )
            .toList();
      }

      setState(() {
        _faqPreview = bumpList(_faqPreview);
        final sr = _searchResults;
        if (sr != null) _searchResults = bumpList(sr);
        _helpfulBusyId = null;
      });
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _helpfulBusyId = null;
        _err = e is ApiException ? e.message : 'Could not record feedback';
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    final showResults = _searchResults != null;

    return BlocListener<AppDataRefreshCubit, AppDataRefreshState>(
      listenWhen: (previous, current) => previous.helpdeskTick != current.helpdeskTick,
      listener: (context, state) {
        _load();
      },
      child: Scaffold(
      appBar: AppBar(title: const Text('Help & support')),
      body: RefreshIndicator(
        onRefresh: _load,
        child: ListView(
          padding: const EdgeInsets.all(18),
          physics: const AlwaysScrollableScrollPhysics(),
          children: [
            Text(
              'Service tickets, expert Q&A, and FAQs from the helpdesk — scoped to your signed-in account where applicable.',
              style: t.bodyMedium?.copyWith(color: KeeplyTokens.muted, height: 1.45),
            ),
            const SizedBox(height: 16),
            if (_err != null)
              Container(
                margin: const EdgeInsets.only(bottom: 12),
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: KeeplyTokens.danger.withValues(alpha: 0.08),
                  borderRadius: BorderRadius.circular(KeeplyTokens.radiusXs),
                  border: Border.all(color: KeeplyTokens.danger.withValues(alpha: 0.25)),
                ),
                child: Text(_err!, style: t.bodySmall?.copyWith(color: KeeplyTokens.danger)),
              ),
            if (_loading)
              const Padding(
                padding: EdgeInsets.symmetric(vertical: 24),
                child: Center(child: CircularProgressIndicator(strokeWidth: 2)),
              )
            else ...[
              Row(
                children: [
                  Expanded(
                    child: _statCard(
                      context,
                      value: _issueCount != null ? '$_issueCount' : '—',
                      label: 'My tickets',
                      onTap: () => Navigator.of(context).push<void>(
                        MaterialPageRoute<void>(
                          builder: (_) => HelpdeskMyIssuesPage(api: _api),
                        ),
                      ),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: _statCard(
                      context,
                      value: _queryCount != null ? '$_queryCount' : '—',
                      label: 'My questions',
                      onTap: () => Navigator.of(context).push<void>(
                        MaterialPageRoute<void>(
                          builder: (_) => HelpdeskMyQueriesPage(api: _api),
                        ),
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 16),
              _actionTile(
                context,
                title: 'Raise a service issue',
                subtitle: 'Appliance problems, repairs, warranty — creates a tracked ticket.',
                onTap: () => Navigator.of(context).push<void>(
                  MaterialPageRoute<void>(
                    builder: (_) => HelpdeskMyIssuesPage(api: _api, highlightCreate: true),
                  ),
                ),
              ),
              _actionTile(
                context,
                title: 'Ask a question',
                subtitle: 'Submit a question to support (separate from a repair ticket).',
                onTap: () => Navigator.of(context).push<void>(
                  MaterialPageRoute<void>(
                    builder: (_) => HelpdeskNewQueryPage(api: _api),
                  ),
                ),
              ),
              _actionTile(
                context,
                title: 'Videos & community tips',
                subtitle: 'Know-how articles and recommended clips.',
                onTap: () => Navigator.of(context).push<void>(
                  MaterialPageRoute<void>(builder: (_) => const TipsHubPage()),
                ),
              ),
              const SizedBox(height: 20),
              Text('Search FAQs', style: t.titleSmall?.copyWith(fontWeight: FontWeight.w700)),
              const SizedBox(height: 8),
              Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Expanded(
                    child: TextField(
                      controller: _searchCtrl,
                      decoration: const InputDecoration(
                        labelText: 'Keyword',
                        hintText: 'e.g. warranty, installation',
                        border: OutlineInputBorder(),
                        isDense: true,
                      ),
                      onSubmitted: (_) => _onSearch(),
                    ),
                  ),
                  const SizedBox(width: 10),
                  Padding(
                    padding: const EdgeInsets.only(top: 4),
                    child: FilledButton(
                      onPressed: (_searchBusy || _searchCtrl.text.trim().isEmpty) ? null : _onSearch,
                      child: Text(_searchBusy ? '…' : 'Search'),
                    ),
                  ),
                ],
              ),
              if (showResults) ...[
                const SizedBox(height: 16),
                if (_searchResults!.isEmpty)
                  Text('No FAQs match that keyword.', style: t.bodySmall?.copyWith(color: KeeplyTokens.muted))
                else
                  ..._searchResults!.map((f) => _faqTile(context, f, showHelpful: true)),
              ],
              if (!showResults && _faqPreview.isNotEmpty) ...[
                const SizedBox(height: 20),
                Text('From the FAQ library', style: t.titleSmall?.copyWith(fontWeight: FontWeight.w700)),
                const SizedBox(height: 8),
                ..._faqPreview.map((f) => _faqTile(context, f, showHelpful: true)),
              ],
            ],
          ],
        ),
      ),
    ),
    );
  }

  Widget _statCard(
    BuildContext context, {
    required String value,
    required String label,
    required VoidCallback onTap,
  }) {
    final t = Theme.of(context).textTheme;
    return Material(
      color: KeeplyTokens.surface,
      borderRadius: BorderRadius.circular(KeeplyTokens.radiusSm),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(KeeplyTokens.radiusSm),
        child: Container(
          padding: const EdgeInsets.symmetric(vertical: 16, horizontal: 12),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(KeeplyTokens.radiusSm),
            border: Border.all(color: KeeplyTokens.line),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(value, style: t.headlineSmall?.copyWith(fontWeight: FontWeight.w800)),
              const SizedBox(height: 4),
              Text(label, style: t.bodySmall?.copyWith(color: KeeplyTokens.muted)),
            ],
          ),
        ),
      ),
    );
  }

  Widget _actionTile(
    BuildContext context, {
    required String title,
    required String subtitle,
    required VoidCallback onTap,
  }) {
    return Card(
      margin: const EdgeInsets.only(bottom: 10),
      child: ListTile(
        title: Text(title, style: const TextStyle(fontWeight: FontWeight.w600)),
        subtitle: Text(
          subtitle,
          style: const TextStyle(color: KeeplyTokens.muted, fontSize: 13, height: 1.35),
        ),
        trailing: const Icon(Icons.chevron_right_rounded),
        onTap: onTap,
      ),
    );
  }

  Widget _faqTile(BuildContext context, FaqItemDto f, {required bool showHelpful}) {
    final t = Theme.of(context).textTheme;
    final id = f.id;
    return Card(
      margin: const EdgeInsets.only(bottom: 8),
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(f.question ?? '—', style: t.titleSmall?.copyWith(fontWeight: FontWeight.w600)),
            if (f.answer != null && f.answer!.trim().isNotEmpty)
              Padding(
                padding: const EdgeInsets.only(top: 6),
                child: Text(f.answer!, style: t.bodySmall?.copyWith(color: KeeplyTokens.muted, height: 1.35)),
              ),
            const SizedBox(height: 8),
            Wrap(
              spacing: 8,
              runSpacing: 6,
              crossAxisAlignment: WrapCrossAlignment.center,
              children: [
                if (f.category != null && f.category!.isNotEmpty)
                  Chip(
                    label: Text(f.category!, style: t.labelSmall),
                    visualDensity: VisualDensity.compact,
                    padding: EdgeInsets.zero,
                    labelPadding: const EdgeInsets.symmetric(horizontal: 8),
                  ),
                if (f.helpfulCount != null)
                  Text(
                    '${f.helpfulCount} found helpful',
                    style: t.labelSmall?.copyWith(color: KeeplyTokens.muted),
                  ),
                if (showHelpful && id != null)
                  TextButton(
                    onPressed: _helpfulBusyId == id ? null : () => _onHelpful(id),
                    child: Text(_helpfulBusyId == id ? 'Thanks…' : 'Helpful'),
                  ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

/// Lists `GET /api/helpdesk/issues/my-issues` (React issues list entry).
class HelpdeskMyIssuesPage extends StatefulWidget {
  const HelpdeskMyIssuesPage({super.key, required this.api, this.highlightCreate = false});

  final KeeplyHelpdeskApi api;
  final bool highlightCreate;

  @override
  State<HelpdeskMyIssuesPage> createState() => _HelpdeskMyIssuesPageState();
}

class _HelpdeskMyIssuesPageState extends State<HelpdeskMyIssuesPage> {
  bool _loading = true;
  String? _err;
  List<IssueItemDto> _rows = [];

  Future<void> _openCreateCustom() async {
    final created = await Navigator.of(context).push<bool>(
      MaterialPageRoute(builder: (_) => HelpdeskCreateCustomIssuePage(api: widget.api)),
    );
    if (created == true && mounted) await _load();
  }

  Future<void> _openCreateFromCatalog() async {
    final created = await Navigator.of(context).push<bool>(
      MaterialPageRoute(builder: (_) => HelpdeskCatalogIssuePage(api: widget.api)),
    );
    if (created == true && mounted) await _load();
  }

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _err = null;
    });
    try {
      final list = await widget.api.listMyIssues();
      if (!mounted) return;
      setState(() {
        _rows = list;
        _loading = false;
      });
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _loading = false;
        _err = e is ApiException ? e.message : 'Could not load tickets';
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    return Scaffold(
      appBar: AppBar(title: const Text('My tickets')),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: _openCreateCustom,
        icon: const Icon(Icons.add),
        label: const Text('New ticket'),
      ),
      body: RefreshIndicator(
        onRefresh: _load,
        child: ListView(
          padding: const EdgeInsets.fromLTRB(16, 16, 16, 96),
          physics: const AlwaysScrollableScrollPhysics(),
          children: [
            if (widget.highlightCreate)
              Padding(
                padding: const EdgeInsets.only(bottom: 12),
                child: Text(
                  'Open a repair or warranty ticket below—the same helpdesk APIs as the web app. You can still use Ask a question on the hub for general enquiries.',
                  style: t.bodySmall?.copyWith(color: KeeplyTokens.muted, height: 1.4),
                ),
              ),
            Text('Start a ticket', style: t.titleSmall?.copyWith(fontWeight: FontWeight.w600)),
            const SizedBox(height: 8),
            Card(
              margin: const EdgeInsets.only(bottom: 10),
              child: ListTile(
                leading: const Icon(Icons.edit_note_outlined),
                title: const Text('Describe a new problem', style: TextStyle(fontWeight: FontWeight.w600)),
                subtitle: Text(
                  'Custom ticket with title and details',
                  style: t.bodySmall?.copyWith(color: KeeplyTokens.muted, height: 1.35),
                ),
                trailing: const Icon(Icons.chevron_right_rounded),
                onTap: _openCreateCustom,
              ),
            ),
            Card(
              margin: const EdgeInsets.only(bottom: 16),
              child: ListTile(
                leading: const Icon(Icons.library_books_outlined),
                title: const Text('Raise from catalog', style: TextStyle(fontWeight: FontWeight.w600)),
                subtitle: Text(
                  'Pick a predefined issue type from the catalog',
                  style: t.bodySmall?.copyWith(color: KeeplyTokens.muted, height: 1.35),
                ),
                trailing: const Icon(Icons.chevron_right_rounded),
                onTap: _openCreateFromCatalog,
              ),
            ),
            Text('Your tickets', style: t.titleSmall?.copyWith(fontWeight: FontWeight.w600)),
            const SizedBox(height: 8),
            if (_err != null)
              Padding(
                padding: const EdgeInsets.only(bottom: 8),
                child: Text(_err!, style: t.bodySmall?.copyWith(color: KeeplyTokens.danger)),
              ),
            if (_loading)
              const Center(child: Padding(padding: EdgeInsets.all(24), child: CircularProgressIndicator(strokeWidth: 2)))
            else if (_rows.isEmpty)
              Text('No tickets yet.', style: t.bodyMedium?.copyWith(color: KeeplyTokens.muted))
            else
              ..._rows.map(
                (i) => Card(
                  margin: const EdgeInsets.only(bottom: 8),
                  child: ListTile(
                    title: Text(i.title ?? 'Ticket #${i.id}', style: const TextStyle(fontWeight: FontWeight.w600)),
                    subtitle: Text(
                      [if (i.status != null) i.status!, if (i.priority != null) i.priority!].join(' · '),
                      style: const TextStyle(fontSize: 12, color: KeeplyTokens.muted),
                    ),
                  ),
                ),
              ),
          ],
        ),
      ),
    );
  }
}

/// `POST /api/helpdesk/issues` — custom issue (`title`, `description`, `priority`, `relatedService`).
class HelpdeskCreateCustomIssuePage extends StatefulWidget {
  const HelpdeskCreateCustomIssuePage({super.key, required this.api});

  final KeeplyHelpdeskApi api;

  @override
  State<HelpdeskCreateCustomIssuePage> createState() => _HelpdeskCreateCustomIssuePageState();
}

class _HelpdeskCreateCustomIssuePageState extends State<HelpdeskCreateCustomIssuePage> {
  final _titleCtrl = TextEditingController();
  final _descCtrl = TextEditingController();
  final _assetIdCtrl = TextEditingController();
  String _priority = 'MEDIUM';
  String _related = 'ASSET_SERVICE';
  bool _submitting = false;
  String? _err;

  static const _priorities = <String, String>{
    'LOW': 'Low',
    'MEDIUM': 'Medium',
    'HIGH': 'High',
    'CRITICAL': 'Critical',
  };

  static const _services = <String, String>{
    'ASSET_SERVICE': 'Assets',
    'AUTH_SERVICE': 'Sign-in & account',
    'NOTIFICATION_SERVICE': 'Notifications',
    'HELPDESK_SERVICE': 'Helpdesk',
    'UPCOMING_PROJECT': 'Other',
  };

  @override
  void dispose() {
    _titleCtrl.dispose();
    _descCtrl.dispose();
    _assetIdCtrl.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    final title = _titleCtrl.text.trim();
    final desc = _descCtrl.text.trim();
    if (title.isEmpty || desc.isEmpty) {
      setState(() => _err = 'Title and description are required.');
      return;
    }
    setState(() {
      _submitting = true;
      _err = null;
    });
    try {
      final body = <String, dynamic>{
        'title': title,
        'description': desc,
        'priority': _priority,
        'relatedService': _related,
      };
      final rawAsset = _assetIdCtrl.text.trim();
      if (rawAsset.isNotEmpty) {
        final id = int.tryParse(rawAsset);
        if (id == null) {
          setState(() {
            _submitting = false;
            _err = 'Asset ID must be a whole number.';
          });
          return;
        }
        body['assetId'] = id;
      }
      await widget.api.createIssue(body);
      if (!mounted) return;
      context.read<AppDataRefreshCubit>().bump(KeeplyDataChannel.helpdesk);
      final messenger = ScaffoldMessenger.maybeOf(context);
      Navigator.of(context).pop(true);
      messenger?.showSnackBar(const SnackBar(content: Text('Ticket created.')));
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _submitting = false;
        _err = e is ApiException ? e.message : 'Could not create ticket';
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    return Scaffold(
      appBar: AppBar(title: const Text('New ticket')),
      body: ListView(
        padding: const EdgeInsets.all(18),
        children: [
          Text(
            'Creates a custom issue via POST /api/helpdesk/issues (same contract as the web app).',
            style: t.bodySmall?.copyWith(color: KeeplyTokens.muted, height: 1.4),
          ),
          const SizedBox(height: 16),
          if (_err != null)
            Padding(
              padding: const EdgeInsets.only(bottom: 12),
              child: Text(_err!, style: t.bodySmall?.copyWith(color: KeeplyTokens.danger)),
            ),
          TextField(
            controller: _titleCtrl,
            decoration: const InputDecoration(
              labelText: 'Title',
              border: OutlineInputBorder(),
            ),
          ),
          const SizedBox(height: 12),
          TextField(
            controller: _descCtrl,
            maxLines: 5,
            decoration: const InputDecoration(
              labelText: 'Description',
              border: OutlineInputBorder(),
              alignLabelWithHint: true,
            ),
          ),
          const SizedBox(height: 12),
          TextField(
            controller: _assetIdCtrl,
            keyboardType: TextInputType.number,
            decoration: const InputDecoration(
              labelText: 'Asset ID (optional)',
              border: OutlineInputBorder(),
            ),
          ),
          const SizedBox(height: 16),
          InputDecorator(
            decoration: const InputDecoration(
              labelText: 'Priority',
              border: OutlineInputBorder(),
            ),
            child: DropdownButtonHideUnderline(
              child: DropdownButton<String>(
                isExpanded: true,
                value: _priority,
                items: _priorities.entries
                    .map((e) => DropdownMenuItem(value: e.key, child: Text(e.value)))
                    .toList(),
                onChanged: _submitting ? null : (v) => setState(() => _priority = v ?? _priority),
              ),
            ),
          ),
          const SizedBox(height: 16),
          InputDecorator(
            decoration: const InputDecoration(
              labelText: 'Related service',
              border: OutlineInputBorder(),
            ),
            child: DropdownButtonHideUnderline(
              child: DropdownButton<String>(
                isExpanded: true,
                value: _related,
                items: _services.entries
                    .map((e) => DropdownMenuItem(value: e.key, child: Text(e.value)))
                    .toList(),
                onChanged: _submitting ? null : (v) => setState(() => _related = v ?? _related),
              ),
            ),
          ),
          const SizedBox(height: 24),
          FilledButton(
            onPressed: _submitting ? null : _submit,
            child: _submitting
                ? const SizedBox(height: 20, width: 20, child: CircularProgressIndicator(strokeWidth: 2))
                : const Text('Create ticket'),
          ),
        ],
      ),
    );
  }
}

/// `GET /api/helpdesk/issue-master` + `POST /api/helpdesk/issues` with `issueMasterId`.
class HelpdeskCatalogIssuePage extends StatefulWidget {
  const HelpdeskCatalogIssuePage({super.key, required this.api});

  final KeeplyHelpdeskApi api;

  @override
  State<HelpdeskCatalogIssuePage> createState() => _HelpdeskCatalogIssuePageState();
}

class _HelpdeskCatalogIssuePageState extends State<HelpdeskCatalogIssuePage> {
  bool _loading = true;
  String? _err;
  List<IssueMasterItemDto> _masters = [];
  IssueMasterItemDto? _selected;
  final _extraDescCtrl = TextEditingController();
  String _priority = 'MEDIUM';
  String _related = 'ASSET_SERVICE';
  bool _submitting = false;
  String? _submitErr;

  static const _priorities = <String, String>{
    'LOW': 'Low',
    'MEDIUM': 'Medium',
    'HIGH': 'High',
    'CRITICAL': 'Critical',
  };

  static const _services = <String, String>{
    'ASSET_SERVICE': 'Assets',
    'AUTH_SERVICE': 'Sign-in & account',
    'NOTIFICATION_SERVICE': 'Notifications',
    'HELPDESK_SERVICE': 'Helpdesk',
    'UPCOMING_PROJECT': 'Other',
  };

  @override
  void dispose() {
    _extraDescCtrl.dispose();
    super.dispose();
  }

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _err = null;
    });
    try {
      final list = await widget.api.listIssueMasters();
      if (!mounted) return;
      setState(() {
        _masters = list;
        _loading = false;
      });
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _loading = false;
        _err = e is ApiException ? e.message : 'Could not load catalog';
      });
    }
  }

  void _select(IssueMasterItemDto m) {
    _extraDescCtrl.clear();
    setState(() {
      _selected = m;
      _submitErr = null;
      _priority = 'MEDIUM';
      _related = 'ASSET_SERVICE';
    });
  }

  Future<void> _submitFromMaster() async {
    final sel = _selected;
    final id = sel?.id;
    if (id == null) return;
    setState(() {
      _submitting = true;
      _submitErr = null;
    });
    try {
      final body = <String, dynamic>{
        'issueMasterId': id,
        'priority': _priority,
        'relatedService': _related,
      };
      final extra = _extraDescCtrl.text.trim();
      if (extra.isNotEmpty) {
        body['description'] = extra;
      }
      await widget.api.createIssue(body);
      if (!mounted) return;
      context.read<AppDataRefreshCubit>().bump(KeeplyDataChannel.helpdesk);
      final messenger = ScaffoldMessenger.maybeOf(context);
      Navigator.of(context).pop(true);
      messenger?.showSnackBar(const SnackBar(content: Text('Ticket created from catalog.')));
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _submitting = false;
        _submitErr = e is ApiException ? e.message : 'Could not create ticket';
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    final sel = _selected;
    return Scaffold(
      appBar: AppBar(title: const Text('Catalog')),
      body: RefreshIndicator(
        onRefresh: _load,
        child: ListView(
          padding: const EdgeInsets.all(16),
          physics: const AlwaysScrollableScrollPhysics(),
          children: [
            Text(
              'Choose an issue type, then confirm priority and service.',
              style: t.bodySmall?.copyWith(color: KeeplyTokens.muted, height: 1.4),
            ),
            const SizedBox(height: 12),
            if (_err != null)
              Padding(
                padding: const EdgeInsets.only(bottom: 8),
                child: Text(_err!, style: t.bodySmall?.copyWith(color: KeeplyTokens.danger)),
              ),
            if (sel != null) ...[
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(14),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      Text(sel.issueTitle ?? 'Issue', style: t.titleSmall?.copyWith(fontWeight: FontWeight.w600)),
                      if (sel.issueDescription != null && sel.issueDescription!.trim().isNotEmpty)
                        Padding(
                          padding: const EdgeInsets.only(top: 6),
                          child: Text(
                            sel.issueDescription!,
                            style: t.bodySmall?.copyWith(color: KeeplyTokens.muted, height: 1.35),
                          ),
                        ),
                      const SizedBox(height: 12),
                      TextField(
                        controller: _extraDescCtrl,
                        maxLines: 3,
                        enabled: !_submitting,
                        decoration: const InputDecoration(
                          labelText: 'Additional details (optional)',
                          border: OutlineInputBorder(),
                          alignLabelWithHint: true,
                        ),
                      ),
                      const SizedBox(height: 12),
                      InputDecorator(
                        decoration: const InputDecoration(
                          labelText: 'Priority',
                          border: OutlineInputBorder(),
                        ),
                        child: DropdownButtonHideUnderline(
                          child: DropdownButton<String>(
                            isExpanded: true,
                            value: _priority,
                            items: _priorities.entries
                                .map((e) => DropdownMenuItem(value: e.key, child: Text(e.value)))
                                .toList(),
                            onChanged: _submitting ? null : (v) => setState(() => _priority = v ?? _priority),
                          ),
                        ),
                      ),
                      const SizedBox(height: 12),
                      InputDecorator(
                        decoration: const InputDecoration(
                          labelText: 'Related service',
                          border: OutlineInputBorder(),
                        ),
                        child: DropdownButtonHideUnderline(
                          child: DropdownButton<String>(
                            isExpanded: true,
                            value: _related,
                            items: _services.entries
                                .map((e) => DropdownMenuItem(value: e.key, child: Text(e.value)))
                                .toList(),
                            onChanged: _submitting ? null : (v) => setState(() => _related = v ?? _related),
                          ),
                        ),
                      ),
                      if (_submitErr != null)
                        Padding(
                          padding: const EdgeInsets.only(top: 8),
                          child: Text(_submitErr!, style: t.bodySmall?.copyWith(color: KeeplyTokens.danger)),
                        ),
                      const SizedBox(height: 12),
                      Row(
                        children: [
                          TextButton(
                            onPressed: _submitting ? null : () => setState(() => _selected = null),
                            child: const Text('Back to list'),
                          ),
                          const Spacer(),
                          FilledButton(
                            onPressed: _submitting ? null : _submitFromMaster,
                            child: _submitting
                                ? const SizedBox(
                                    height: 20,
                                    width: 20,
                                    child: CircularProgressIndicator(strokeWidth: 2),
                                  )
                                : const Text('Create ticket'),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 8),
              Text('Catalog', style: t.labelLarge?.copyWith(color: KeeplyTokens.muted)),
              const SizedBox(height: 8),
            ],
            if (_loading)
              const Center(child: Padding(padding: EdgeInsets.all(24), child: CircularProgressIndicator(strokeWidth: 2)))
            else if (_masters.isEmpty && _err == null)
              Text('No catalog entries yet.', style: t.bodyMedium?.copyWith(color: KeeplyTokens.muted))
            else
              ..._masters.where((m) => m.id != null).map(
                    (m) => Card(
                      margin: const EdgeInsets.only(bottom: 8),
                      child: ListTile(
                        title: Text(m.issueTitle ?? 'Issue #${m.id}', style: const TextStyle(fontWeight: FontWeight.w600)),
                        subtitle: m.issueDescription != null && m.issueDescription!.trim().isNotEmpty
                            ? Text(
                                m.issueDescription!,
                                maxLines: 2,
                                overflow: TextOverflow.ellipsis,
                                style: const TextStyle(fontSize: 13, color: KeeplyTokens.muted, height: 1.3),
                              )
                            : null,
                        trailing: const Icon(Icons.chevron_right_rounded),
                        onTap: _submitting ? null : () => _select(m),
                      ),
                    ),
                  ),
          ],
        ),
      ),
    );
  }
}

/// Lists `GET /api/helpdesk/queries/my-queries`.
class HelpdeskMyQueriesPage extends StatefulWidget {
  const HelpdeskMyQueriesPage({super.key, required this.api});

  final KeeplyHelpdeskApi api;

  @override
  State<HelpdeskMyQueriesPage> createState() => _HelpdeskMyQueriesPageState();
}

class _HelpdeskMyQueriesPageState extends State<HelpdeskMyQueriesPage> {
  bool _loading = true;
  String? _err;
  List<HelpdeskQueryItemDto> _rows = [];

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _err = null;
    });
    try {
      final list = await widget.api.listMyQueries();
      if (!mounted) return;
      setState(() {
        _rows = list;
        _loading = false;
      });
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _loading = false;
        _err = e is ApiException ? e.message : 'Could not load questions';
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    return Scaffold(
      appBar: AppBar(title: const Text('My questions')),
      body: RefreshIndicator(
        onRefresh: _load,
        child: ListView(
          padding: const EdgeInsets.all(16),
          physics: const AlwaysScrollableScrollPhysics(),
          children: [
            if (_err != null)
              Text(_err!, style: t.bodySmall?.copyWith(color: KeeplyTokens.danger)),
            if (_loading)
              const Center(child: Padding(padding: EdgeInsets.all(24), child: CircularProgressIndicator(strokeWidth: 2)))
            else if (_rows.isEmpty)
              Text('No questions yet.', style: t.bodyMedium?.copyWith(color: KeeplyTokens.muted))
            else
              ..._rows.map(
                (q) => Card(
                  margin: const EdgeInsets.only(bottom: 8),
                  child: ListTile(
                    title: Text(q.question ?? 'Query #${q.id}', style: const TextStyle(fontWeight: FontWeight.w600)),
                    subtitle: Text(
                      q.status ?? '',
                      style: const TextStyle(fontSize: 12, color: KeeplyTokens.muted),
                    ),
                  ),
                ),
              ),
          ],
        ),
      ),
    );
  }
}

/// `POST /api/helpdesk/queries` — React `NewQueryPage` minimal field set.
class HelpdeskNewQueryPage extends StatefulWidget {
  const HelpdeskNewQueryPage({super.key, required this.api});

  final KeeplyHelpdeskApi api;

  @override
  State<HelpdeskNewQueryPage> createState() => _HelpdeskNewQueryPageState();
}

class _HelpdeskNewQueryPageState extends State<HelpdeskNewQueryPage> {
  final _questionCtrl = TextEditingController();
  String _related = 'ASSET_SERVICE';
  bool _submitting = false;
  String? _err;

  static const _services = <String, String>{
    'ASSET_SERVICE': 'Assets',
    'AUTH_SERVICE': 'Sign-in & account',
    'NOTIFICATION_SERVICE': 'Notifications',
    'HELPDESK_SERVICE': 'Helpdesk',
    'UPCOMING_PROJECT': 'Other',
  };

  @override
  void dispose() {
    _questionCtrl.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    final q = _questionCtrl.text.trim();
    if (q.isEmpty) {
      setState(() => _err = 'Please enter a question.');
      return;
    }
    setState(() {
      _submitting = true;
      _err = null;
    });
    try {
      await widget.api.createQuery({'question': q, 'relatedService': _related});
      if (!mounted) return;
      context.read<AppDataRefreshCubit>().bump(KeeplyDataChannel.helpdesk);
      final messenger = ScaffoldMessenger.maybeOf(context);
      Navigator.of(context).pop(true);
      messenger?.showSnackBar(const SnackBar(content: Text('Your question was submitted.')));
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _submitting = false;
        _err = e is ApiException ? e.message : 'Submit failed';
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    return Scaffold(
      appBar: AppBar(title: const Text('Ask a question')),
      body: ListView(
        padding: const EdgeInsets.all(18),
        children: [
          Text(
            'Submit a question to the helpdesk team (same API as the web app).',
            style: t.bodySmall?.copyWith(color: KeeplyTokens.muted, height: 1.4),
          ),
          const SizedBox(height: 16),
          if (_err != null)
            Padding(
              padding: const EdgeInsets.only(bottom: 12),
              child: Text(_err!, style: t.bodySmall?.copyWith(color: KeeplyTokens.danger)),
            ),
          TextField(
            controller: _questionCtrl,
            maxLines: 5,
            decoration: const InputDecoration(
              labelText: 'Your question',
              border: OutlineInputBorder(),
              alignLabelWithHint: true,
            ),
          ),
          const SizedBox(height: 16),
          InputDecorator(
            decoration: const InputDecoration(
              labelText: 'Related service',
              border: OutlineInputBorder(),
            ),
            child: DropdownButtonHideUnderline(
              child: DropdownButton<String>(
                isExpanded: true,
                value: _related,
                items: _services.entries
                    .map((e) => DropdownMenuItem(value: e.key, child: Text(e.value)))
                    .toList(),
                onChanged: _submitting ? null : (v) => setState(() => _related = v ?? _related),
              ),
            ),
          ),
          const SizedBox(height: 24),
          FilledButton(
            onPressed: _submitting ? null : _submit,
            child: _submitting
                ? const SizedBox(height: 20, width: 20, child: CircularProgressIndicator(strokeWidth: 2))
                : const Text('Submit'),
          ),
        ],
      ),
    );
  }
}
