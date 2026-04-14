import 'package:flutter/material.dart';
import 'package:keeply_app/core/api/keeply_api_models.dart';
import 'package:keeply_app/core/api/keeply_categories_api.dart';
import 'package:keeply_app/core/api/keeply_master_data_api.dart';
import 'package:keeply_app/core/theme/keeply_tokens.dart';
import 'package:keeply_app/core/view_layout/view_layout_scope.dart';

/// Browse asset service master data: categories, subcategories, makes, models.
/// Layout follows [ViewLayoutScope] (list vs cards), same toggle as the rest of the app.
class MasterDataCatalogPage extends StatefulWidget {
  const MasterDataCatalogPage({super.key});

  @override
  State<MasterDataCatalogPage> createState() => _MasterDataCatalogPageState();
}

class _MasterDataCatalogPageState extends State<MasterDataCatalogPage>
    with SingleTickerProviderStateMixin {
  late final TabController _tabController;
  final _categoriesApi = KeeplyCategoriesApi();
  final _masterApi = KeeplyMasterDataApi();

  bool _loading = true;
  String? _error;
  List<CategoryDto> _categories = [];
  List<SubCategoryDto> _subCategories = [];
  List<MakeDto> _makes = [];
  List<ModelDto> _models = [];

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 4, vsync: this);
    _load();
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final results = await Future.wait([
        _categoriesApi.listCategories(),
        _masterApi.listSubCategories(),
        _masterApi.listMakes(),
        _masterApi.listModels(),
      ]);
      if (!mounted) return;
      setState(() {
        _loading = false;
        _categories = results[0] as List<CategoryDto>;
        _subCategories = results[1] as List<SubCategoryDto>;
        _makes = results[2] as List<MakeDto>;
        _models = results[3] as List<ModelDto>;
      });
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _loading = false;
        _error = '$e';
      });
    }
  }

  String? _categoryName(int? id) {
    if (id == null) return null;
    for (final c in _categories) {
      if (c.categoryId == id) return c.categoryName;
    }
    return null;
  }

  String? _subCategoryName(int? id) {
    if (id == null) return null;
    for (final s in _subCategories) {
      if (s.subCategoryId == id) return s.subCategoryName;
    }
    return null;
  }

  String? _makeName(int? id) {
    if (id == null) return null;
    for (final m in _makes) {
      if (m.makeId == id) return m.makeName;
    }
    return null;
  }

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Master data'),
        actions: const [
          Padding(
            padding: EdgeInsets.only(right: 8),
            child: ViewLayoutToggle(compact: true),
          ),
        ],
        bottom: TabBar(
          controller: _tabController,
          isScrollable: true,
          tabAlignment: TabAlignment.start,
          tabs: const [
            Tab(text: 'Categories'),
            Tab(text: 'Subcategories'),
            Tab(text: 'Makes'),
            Tab(text: 'Models'),
          ],
        ),
      ),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : _error != null
              ? Center(
                  child: Padding(
                    padding: const EdgeInsets.all(24),
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Text(
                          'Could not load master data',
                          style: t.titleMedium?.copyWith(fontWeight: FontWeight.w700),
                          textAlign: TextAlign.center,
                        ),
                        const SizedBox(height: 12),
                        Text(
                          _error!,
                          style: t.bodySmall?.copyWith(color: KeeplyTokens.muted),
                          textAlign: TextAlign.center,
                        ),
                        const SizedBox(height: 20),
                        FilledButton.icon(
                          onPressed: _load,
                          icon: const Icon(Icons.refresh),
                          label: const Text('Retry'),
                        ),
                      ],
                    ),
                  ),
                )
              : TabBarView(
                  controller: _tabController,
                  children: [
                    _MasterDataTab(
                      emptyLabel: 'No categories returned.',
                      itemCount: _categories.length,
                      itemBuilder: (context, index) {
                        final c = _categories[index];
                        final id = c.categoryId;
                        final title = c.categoryName ?? 'Category';
                        final sub = [
                          if (id != null) 'ID $id',
                          if (c.description != null && c.description!.trim().isNotEmpty)
                            c.description!.trim(),
                        ].join(' · ');
                        return _MasterRow(
                          icon: Icons.category_outlined,
                          title: title,
                          subtitle: sub.isEmpty ? null : sub,
                        );
                      },
                      cardBuilder: (context, index) {
                        final c = _categories[index];
                        return _MasterCard(
                          icon: Icons.category_outlined,
                          title: c.categoryName ?? 'Category',
                          lines: [
                            if (c.categoryId != null) 'ID: ${c.categoryId}',
                            if (c.description != null && c.description!.trim().isNotEmpty)
                              c.description!.trim(),
                          ],
                        );
                      },
                    ),
                    _MasterDataTab(
                      emptyLabel: 'No subcategories returned.',
                      itemCount: _subCategories.length,
                      itemBuilder: (context, index) {
                        final s = _subCategories[index];
                        final cid = s.categoryId;
                        final cname = _categoryName(cid);
                        final title = s.subCategoryName ?? 'Subcategory';
                        final sub = [
                          if (s.subCategoryId != null) 'ID ${s.subCategoryId}',
                          if (cname != null) 'Category: $cname'
                          else if (cid != null)
                            'Category ID: $cid',
                        ].join(' · ');
                        return _MasterRow(
                          icon: Icons.subdirectory_arrow_right,
                          title: title,
                          subtitle: sub,
                        );
                      },
                      cardBuilder: (context, index) {
                        final s = _subCategories[index];
                        final cid = s.categoryId;
                        final cname = _categoryName(cid);
                        return _MasterCard(
                          icon: Icons.subdirectory_arrow_right,
                          title: s.subCategoryName ?? 'Subcategory',
                          lines: [
                            if (s.subCategoryId != null) 'ID: ${s.subCategoryId}',
                            if (cname != null)
                              'Category: $cname'
                            else if (cid != null)
                              'Category ID: $cid',
                          ],
                        );
                      },
                    ),
                    _MasterDataTab(
                      emptyLabel: 'No makes returned.',
                      itemCount: _makes.length,
                      itemBuilder: (context, index) {
                        final m = _makes[index];
                        final sid = m.subCategoryId;
                        final sname = _subCategoryName(sid);
                        final title = m.makeName ?? 'Make';
                        final sub = [
                          if (m.makeId != null) 'ID ${m.makeId}',
                          if (sname != null) 'Subcategory: $sname'
                          else if (sid != null)
                            'Subcategory ID: $sid',
                        ].join(' · ');
                        return _MasterRow(
                          icon: Icons.precision_manufacturing_outlined,
                          title: title,
                          subtitle: sub,
                        );
                      },
                      cardBuilder: (context, index) {
                        final m = _makes[index];
                        final sid = m.subCategoryId;
                        final sname = _subCategoryName(sid);
                        return _MasterCard(
                          icon: Icons.precision_manufacturing_outlined,
                          title: m.makeName ?? 'Make',
                          lines: [
                            if (m.makeId != null) 'ID: ${m.makeId}',
                            if (sname != null)
                              'Subcategory: $sname'
                            else if (sid != null)
                              'Subcategory ID: $sid',
                          ],
                        );
                      },
                    ),
                    _MasterDataTab(
                      emptyLabel: 'No models returned.',
                      itemCount: _models.length,
                      itemBuilder: (context, index) {
                        final m = _models[index];
                        final mid = m.makeId;
                        final mkname = _makeName(mid);
                        final title = m.modelName ?? 'Model';
                        final sub = [
                          if (m.modelId != null) 'ID ${m.modelId}',
                          if (mkname != null) 'Make: $mkname'
                          else if (mid != null)
                            'Make ID: $mid',
                        ].join(' · ');
                        return _MasterRow(
                          icon: Icons.model_training_outlined,
                          title: title,
                          subtitle: sub,
                        );
                      },
                      cardBuilder: (context, index) {
                        final m = _models[index];
                        final mid = m.makeId;
                        final mkname = _makeName(mid);
                        return _MasterCard(
                          icon: Icons.model_training_outlined,
                          title: m.modelName ?? 'Model',
                          lines: [
                            if (m.modelId != null) 'ID: ${m.modelId}',
                            if (mkname != null)
                              'Make: $mkname'
                            else if (mid != null)
                              'Make ID: $mid',
                          ],
                        );
                      },
                    ),
                  ],
                ),
    );
  }
}

class _MasterDataTab extends StatelessWidget {
  const _MasterDataTab({
    required this.emptyLabel,
    required this.itemCount,
    required this.itemBuilder,
    required this.cardBuilder,
  });

  final String emptyLabel;
  final int itemCount;
  final Widget Function(BuildContext context, int index) itemBuilder;
  final Widget Function(BuildContext context, int index) cardBuilder;

  @override
  Widget build(BuildContext context) {
    if (itemCount == 0) {
      return Center(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Text(
            emptyLabel,
            style: Theme.of(context).textTheme.bodyLarge?.copyWith(color: KeeplyTokens.muted),
            textAlign: TextAlign.center,
          ),
        ),
      );
    }

    return ListenableBuilder(
      listenable: ViewLayoutScope.notifierOf(context),
      builder: (context, _) {
        final cardMode = ViewLayoutScope.modeOf(context) == ViewLayoutMode.card;
        if (!cardMode) {
          return ListView.separated(
            padding: const EdgeInsets.fromLTRB(16, 12, 16, 24),
            itemCount: itemCount,
            separatorBuilder: (_, __) => const SizedBox(height: 8),
            itemBuilder: itemBuilder,
          );
        }
        return GridView.builder(
          padding: const EdgeInsets.fromLTRB(16, 12, 16, 24),
          gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
            crossAxisCount: 2,
            mainAxisSpacing: 12,
            crossAxisSpacing: 12,
            childAspectRatio: 1.15,
          ),
          itemCount: itemCount,
          itemBuilder: cardBuilder,
        );
      },
    );
  }
}

class _MasterRow extends StatelessWidget {
  const _MasterRow({
    required this.icon,
    required this.title,
    this.subtitle,
  });

  final IconData icon;
  final String title;
  final String? subtitle;

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    return Material(
      color: KeeplyTokens.surface,
      borderRadius: BorderRadius.circular(KeeplyTokens.radiusXs),
      child: ListTile(
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(KeeplyTokens.radiusXs),
          side: const BorderSide(color: KeeplyTokens.line),
        ),
        leading: Icon(icon, color: KeeplyTokens.accentInk),
        title: Text(title, style: t.titleSmall?.copyWith(fontWeight: FontWeight.w600)),
        subtitle: subtitle != null && subtitle!.isNotEmpty
            ? Text(subtitle!, style: t.bodySmall?.copyWith(color: KeeplyTokens.muted))
            : null,
        dense: true,
      ),
    );
  }
}

class _MasterCard extends StatelessWidget {
  const _MasterCard({
    required this.icon,
    required this.title,
    required this.lines,
  });

  final IconData icon;
  final String title;
  final List<String> lines;

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    final filtered = lines.where((e) => e.trim().isNotEmpty).toList();
    return Material(
      color: KeeplyTokens.surface,
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
            Icon(icon, color: KeeplyTokens.accentInk, size: 28),
            const SizedBox(height: 8),
            Text(
              title,
              maxLines: 2,
              overflow: TextOverflow.ellipsis,
              style: t.labelLarge?.copyWith(fontWeight: FontWeight.w700),
            ),
            const SizedBox(height: 6),
            ...filtered.map(
              (line) => Padding(
                padding: const EdgeInsets.only(bottom: 2),
                child: Text(
                  line,
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                  style: t.labelSmall?.copyWith(color: KeeplyTokens.muted, height: 1.25),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
