import 'package:flutter/material.dart';
import 'package:keeply_app/core/theme/keeply_tokens.dart';
import 'package:keeply_app/features/asset/data/datasources/asset_remote_datasource.dart';
import 'package:keeply_app/features/asset/data/models/asset_models.dart';

/// Mirrors React `AssetsPage` — search toolbar, category chips, grid cards.
class AppliancesPage extends StatefulWidget {
  const AppliancesPage({super.key});

  @override
  State<AppliancesPage> createState() => _AppliancesPageState();
}

class _AppliancesPageState extends State<AppliancesPage> {
  final _ds = AssetRemoteDataSource();
  final _keyword = TextEditingController();
  List<AssetMaster> _items = [];
  int _total = 0;
  bool _loading = false;
  String? _err;
  String _cat = 'All';

  @override
  void initState() {
    super.initState();
    _load();
  }

  @override
  void dispose() {
    _keyword.dispose();
    super.dispose();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _err = null;
    });
    try {
      final page = await _ds.searchAssets(
        keyword: _keyword.text.trim().isEmpty ? null : _keyword.text.trim(),
        page: 0,
        size: 12,
      );
      if (mounted) {
        setState(() {
          _items = page.content;
          _total = page.totalElements;
          _cat = 'All';
        });
      }
    } catch (e) {
      if (mounted) setState(() => _err = '$e');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  List<String> get _categoryList {
    final s = <String>{};
    for (final a in _items) {
      final n = a.category?['categoryName'] as String?;
      if (n != null && n.isNotEmpty) s.add(n);
    }
    final sorted = s.toList()..sort();
    return ['All', ...sorted];
  }

  List<AssetMaster> get _visible {
    if (_cat == 'All') return _items;
    return _items.where((a) => (a.category?['categoryName'] as String?) == _cat).toList();
  }

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;

    return ListView(
      padding: const EdgeInsets.fromLTRB(18, 18, 18, 100),
      children: [
        Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('My appliances', style: t.headlineSmall?.copyWith(fontWeight: FontWeight.w800)),
                  const SizedBox(height: 4),
                  Text(
                    'Search, filter, and open appliance details.',
                    style: t.bodySmall?.copyWith(color: KeeplyTokens.muted),
                  ),
                ],
              ),
            ),
            Text('$_total', style: t.bodySmall?.copyWith(color: KeeplyTokens.muted)),
          ],
        ),
        const SizedBox(height: 14),
        Row(
          children: [
            Expanded(
              child: TextField(
                controller: _keyword,
                decoration: const InputDecoration(
                  hintText: 'Search by name, model, category…',
                  isDense: true,
                ),
                onSubmitted: (_) => _load(),
              ),
            ),
            const SizedBox(width: 10),
            FilledButton(
              onPressed: _loading ? null : _load,
              child: Text(_loading ? '…' : 'Search'),
            ),
          ],
        ),
        if (_err != null) ...[
          const SizedBox(height: 10),
          Text(_err!, style: t.bodySmall?.copyWith(color: KeeplyTokens.danger)),
        ],
        const SizedBox(height: 14),
        SizedBox(
          height: 40,
          child: ListView.separated(
            scrollDirection: Axis.horizontal,
            itemCount: _categoryList.length,
            separatorBuilder: (_, __) => const SizedBox(width: 8),
            itemBuilder: (_, i) {
              final c = _categoryList[i];
              final on = _cat == c;
              return FilterChip(
                label: Text(c),
                selected: on,
                onSelected: (_) => setState(() => _cat = c),
              );
            },
          ),
        ),
        const SizedBox(height: 16),
        Text(_cat == 'All' ? 'All assets' : _cat, style: t.titleSmall?.copyWith(fontWeight: FontWeight.w600)),
        const SizedBox(height: 12),
        if (_visible.isEmpty && !_loading)
          Padding(
            padding: const EdgeInsets.symmetric(vertical: 32),
            child: Center(
              child: Text('No appliances match.', style: t.bodyMedium?.copyWith(color: KeeplyTokens.muted)),
            ),
          )
        else
          GridView.builder(
            shrinkWrap: true,
            physics: const NeverScrollableScrollPhysics(),
            gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
              crossAxisCount: 2,
              mainAxisSpacing: 12,
              crossAxisSpacing: 12,
              childAspectRatio: 0.78,
            ),
            itemCount: _visible.length,
            itemBuilder: (_, i) {
              final a = _visible[i];
              final title = a.assetNameUdv.isNotEmpty ? a.assetNameUdv : 'Asset ${a.assetId ?? ''}';
              final cat = a.category?['categoryName'] as String? ?? '';
              return Material(
                color: KeeplyTokens.surface,
                borderRadius: BorderRadius.circular(KeeplyTokens.radiusSm),
                child: InkWell(
                  onTap: () {},
                  borderRadius: BorderRadius.circular(KeeplyTokens.radiusSm),
                  child: Container(
                    padding: const EdgeInsets.all(12),
                    decoration: BoxDecoration(
                      borderRadius: BorderRadius.circular(KeeplyTokens.radiusSm),
                      border: Border.all(color: KeeplyTokens.line),
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Expanded(
                          child: Container(
                            decoration: BoxDecoration(
                              color: KeeplyTokens.surfaceMuted,
                              borderRadius: BorderRadius.circular(KeeplyTokens.radiusXs),
                            ),
                            child: const Center(
                              child: Icon(Icons.kitchen_outlined, size: 40, color: KeeplyTokens.muted),
                            ),
                          ),
                        ),
                        const SizedBox(height: 8),
                        Text(
                          title,
                          maxLines: 2,
                          overflow: TextOverflow.ellipsis,
                          style: t.labelLarge?.copyWith(fontWeight: FontWeight.w600),
                        ),
                        if (cat.isNotEmpty)
                          Text(cat, style: t.labelSmall?.copyWith(color: KeeplyTokens.muted)),
                      ],
                    ),
                  ),
                ),
              );
            },
          ),
      ],
    );
  }
}
