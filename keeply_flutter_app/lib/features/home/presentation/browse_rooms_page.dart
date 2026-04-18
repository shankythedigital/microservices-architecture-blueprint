import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:keeply_app/core/sync/app_data_refresh_cubit.dart';
import 'package:keeply_app/core/theme/keeply_tokens.dart';
import 'package:keeply_app/core/view_layout/view_layout_scope.dart';
import 'package:keeply_app/core/widgets/keeply_asset_views.dart';
import 'package:keeply_app/features/asset/data/datasources/asset_remote_datasource.dart';
import 'package:keeply_app/features/asset/data/models/asset_models.dart';
import 'package:keeply_app/features/asset/presentation/pages/asset_detail_page.dart';
import 'package:keeply_app/features/auth/presentation/bloc/auth_bloc.dart';
import 'package:keeply_app/features/home/presentation/appliances_page.dart';

/// Dedicated surface for the shell tab "Browse by rooms" (same filters as dashboard strip).
class BrowseRoomsPage extends StatefulWidget {
  const BrowseRoomsPage({super.key});

  @override
  State<BrowseRoomsPage> createState() => _BrowseRoomsPageState();
}

class _BrowseRoomsPageState extends State<BrowseRoomsPage> {
  final _assetDs = AssetRemoteDataSource();
  List<AssetMaster> _assets = [];
  String _room = 'All';
  String? _err;
  bool _loading = true;

  static const _rooms = ['All', 'Kitchen', 'Living room', 'Laundry', 'Other'];

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => _load());
  }

  Future<void> _load() async {
    final auth = context.read<AuthBloc>().state;
    if (auth is! AuthAuthenticated) {
      if (mounted) setState(() => _loading = false);
      return;
    }
    final tokenUserId = auth.user.userId;

    setState(() {
      _loading = true;
      _err = null;
    });

    try {
      Map<String, dynamic>? nya;
      try {
        nya = await _assetDs.getNeedYourAttention();
      } catch (_) {}

      var list = <AssetMaster>[];
      if (tokenUserId > 0) {
        try {
          list = await _assetDs.fetchAssetsAssignedToUser(tokenUserId);
        } catch (_) {}
      }
      if (list.isEmpty && nya != null) {
        final raw = nya['assets'];
        if (raw is List) {
          list = raw
              .whereType<Map>()
              .map((e) => AssetMaster.fromJson(Map<String, dynamic>.from(e)))
              .toList();
        }
      }
      if (list.isEmpty && tokenUserId <= 0) {
        try {
          final page = await _assetDs.searchAssets(page: 0, size: 50);
          list = page.content;
        } catch (_) {}
      }

      if (!mounted) return;
      setState(() {
        _assets = list;
        _loading = false;
      });
    } catch (e) {
      if (mounted) {
        setState(() {
          _err = '$e';
          _loading = false;
        });
      }
    }
  }

  List<AssetMaster> get _filtered {
    if (_room == 'All') return _assets;
    final r = _room.toLowerCase();
    return _assets.where((a) {
      final cat = (a.category?['categoryName'] as String?) ?? '';
      return cat.toLowerCase().contains(r.substring(0, r.length.clamp(0, 4)));
    }).toList();
  }

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;

    return BlocListener<AppDataRefreshCubit, AppDataRefreshState>(
      listenWhen: (previous, current) => previous.assetsTick != current.assetsTick,
      listener: (context, state) => _load(),
      child: RefreshIndicator(
        onRefresh: _load,
        child: ListView(
          padding: const EdgeInsets.fromLTRB(18, 18, 18, 28),
          physics: const AlwaysScrollableScrollPhysics(),
          children: [
            Row(
              children: [
                Expanded(
                  child: Text(
                    'Browse by room',
                    style: t.titleMedium?.copyWith(fontWeight: FontWeight.w700),
                  ),
                ),
                TextButton(
                  onPressed: () {
                    Navigator.of(context).push<void>(
                      MaterialPageRoute<void>(
                        builder: (_) => Scaffold(
                          appBar: AppBar(title: const Text('Appliance catalog')),
                          body: const AppliancesPage(),
                        ),
                      ),
                    );
                  },
                  child: const Text('Catalog'),
                ),
              ],
            ),
            const SizedBox(height: 8),
            Text(
              'Filter your appliances the same way as on Home.',
              style: t.bodySmall?.copyWith(color: Theme.of(context).colorScheme.onSurfaceVariant),
            ),
            if (_err != null) ...[
              const SizedBox(height: 12),
              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: KeeplyTokens.danger.withValues(alpha: 0.08),
                  borderRadius: BorderRadius.circular(KeeplyTokens.radiusXs),
                  border: Border.all(color: KeeplyTokens.danger.withValues(alpha: 0.25)),
                ),
                child: Text(_err!, style: t.bodySmall?.copyWith(color: KeeplyTokens.danger)),
              ),
            ],
            if (_loading)
              const Padding(
                padding: EdgeInsets.symmetric(vertical: 48),
                child: Center(child: CircularProgressIndicator()),
              )
            else if (_assets.isEmpty)
              Padding(
                padding: const EdgeInsets.symmetric(vertical: 32),
                child: Text(
                  'No appliances yet. Add one from the Add Asset tab.',
                  style: t.bodyMedium?.copyWith(color: Theme.of(context).colorScheme.onSurfaceVariant),
                  textAlign: TextAlign.center,
                ),
              )
            else ...[
              const SizedBox(height: 16),
              SizedBox(
                height: 40,
                child: ListView.separated(
                  scrollDirection: Axis.horizontal,
                  itemCount: _rooms.length,
                  separatorBuilder: (_, __) => const SizedBox(width: 8),
                  itemBuilder: (_, i) {
                    final label = _rooms[i];
                    final on = _room == label;
                    return FilterChip(
                      label: Text(label),
                      selected: on,
                      onSelected: (_) => setState(() => _room = label),
                    );
                  },
                ),
              ),
              const SizedBox(height: 14),
              if (_filtered.isEmpty)
                Padding(
                  padding: const EdgeInsets.symmetric(vertical: 24),
                  child: Text(
                    _room == 'All' ? 'No appliances to show.' : 'No appliances in this room.',
                    style: t.bodyMedium?.copyWith(color: Theme.of(context).colorScheme.onSurfaceVariant),
                    textAlign: TextAlign.center,
                  ),
                )
              else
                ListenableBuilder(
                  listenable: ViewLayoutScope.notifierOf(context),
                  builder: (context, _) {
                    final preview = _filtered;
                    if (ViewLayoutScope.modeOf(context) == ViewLayoutMode.list) {
                      return Column(
                        children: [
                          for (final a in preview)
                            KeeplyAssetListRow(
                              asset: a,
                              onTap: () => AssetDetailPage.pushIfValid(context, a.assetId),
                            ),
                        ],
                      );
                    }
                    return GridView.builder(
                      shrinkWrap: true,
                      physics: const NeverScrollableScrollPhysics(),
                      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                        crossAxisCount: 2,
                        mainAxisSpacing: 12,
                        crossAxisSpacing: 12,
                        childAspectRatio: 0.78,
                      ),
                      itemCount: preview.length,
                      itemBuilder: (_, i) {
                        final a = preview[i];
                        return KeeplyAssetGridCard(
                          asset: a,
                          onTap: () => AssetDetailPage.pushIfValid(context, a.assetId),
                        );
                      },
                    );
                  },
                ),
            ],
          ],
        ),
      ),
    );
  }
}
