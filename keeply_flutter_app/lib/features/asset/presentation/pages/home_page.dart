import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:keeply_app/core/api/keeply_helpdesk_api.dart';
import 'package:keeply_app/core/theme/keeply_tokens.dart';
import 'package:keeply_app/features/home/presentation/dashboard_engagement.dart';
import 'package:keeply_app/core/view_layout/view_layout_scope.dart';
import 'package:keeply_app/core/widgets/keeply_asset_views.dart';
import 'package:keeply_app/features/asset/data/datasources/asset_remote_datasource.dart';
import 'package:keeply_app/features/asset/data/models/asset_models.dart';
import 'package:keeply_app/features/auth/presentation/bloc/auth_bloc.dart';
import 'package:keeply_app/features/asset/presentation/pages/asset_detail_page.dart';
import 'package:keeply_app/features/asset/presentation/pages/assets_list_page.dart';
import 'package:keeply_app/features/asset/presentation/pages/asset_scan_page.dart';
import 'package:keeply_app/features/asset/presentation/pages/create_asset_page.dart';
import 'package:keeply_app/features/helpdesk/presentation/pages/helpdesk_hub_page.dart';
import 'package:keeply_app/features/master_data/presentation/pages/master_data_catalog_page.dart';
import 'package:keeply_app/features/shell/presentation/pages/alerts_hub_page.dart';

/// Home Page
/// Main dashboard after authentication
class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  final AssetRemoteDataSource _assetDs = AssetRemoteDataSource();

  List<AssetMaster> _assets = [];
  bool _assetsLoading = true;
  String _room = 'All';
  int? _helpdeskIssueCount;
  int? _helpdeskQueryCount;

  static const _rooms = ['All', 'Kitchen', 'Living room', 'Laundry', 'Other'];
  static const int _browsePreviewMax = 24;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => _loadAssets());
  }

  Future<List<AssetMaster>> _fetchAssetList(int tokenUserId) async {
    var list = <AssetMaster>[];
    if (tokenUserId > 0) {
      try {
        list = await _assetDs.fetchAssetsAssignedToUser(tokenUserId);
      } catch (_) {}
    }
    if (list.isEmpty) {
      Map<String, dynamic>? nya;
      try {
        nya = await _assetDs.getNeedYourAttention();
      } catch (_) {}
      if (nya != null) {
        final raw = nya['assets'];
        if (raw is List) {
          list = raw
              .whereType<Map>()
              .map((e) => AssetMaster.fromJson(Map<String, dynamic>.from(e)))
              .toList();
        }
      }
    }
    if (list.isEmpty && tokenUserId <= 0) {
      try {
        final page = await _assetDs.searchAssets(page: 0, size: 50);
        list = page.content;
      } catch (_) {}
    }
    return list;
  }

  Future<(int?, int?)> _fetchHelpdeskSummary() async {
    try {
      final api = KeeplyHelpdeskApi();
      final issues = await api.listMyIssues();
      final queries = await api.listMyQueries();
      return (issues.length, queries.length);
    } catch (_) {
      return (null, null);
    }
  }

  Future<void> _loadAssets() async {
    final auth = context.read<AuthBloc>().state;
    if (auth is! AuthAuthenticated) return;
    final tokenUserId = auth.user.userId;

    setState(() => _assetsLoading = true);
    try {
      final results = await Future.wait([
        _fetchAssetList(tokenUserId),
        _fetchHelpdeskSummary(),
      ]);
      if (!mounted) return;
      final list = results[0] as List<AssetMaster>;
      final hd = results[1] as (int?, int?);
      setState(() {
        _assets = list;
        _helpdeskIssueCount = hd.$1;
        _helpdeskQueryCount = hd.$2;
        _assetsLoading = false;
      });
    } catch (_) {
      if (mounted) setState(() => _assetsLoading = false);
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
    return Scaffold(
      appBar: AppBar(
        title: const Text('Keeply'),
        actions: [
          const Padding(
            padding: EdgeInsets.only(right: 4),
            child: ViewLayoutToggle(compact: true),
          ),
          IconButton(
            icon: const Icon(Icons.logout),
            onPressed: () {
              context.read<AuthBloc>().add(LogoutEvent());
            },
            tooltip: 'Logout',
          ),
        ],
      ),
      body: BlocBuilder<AuthBloc, AuthState>(
        builder: (context, state) {
          if (state is AuthAuthenticated) {
            return RefreshIndicator(
              onRefresh: _loadAssets,
              child: SingleChildScrollView(
                physics: const AlwaysScrollableScrollPhysics(),
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Card(
                      child: Padding(
                        padding: const EdgeInsets.all(16.0),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              'Welcome, ${state.user.username ?? 'User'}!',
                              style: Theme.of(context).textTheme.headlineSmall,
                            ),
                            const SizedBox(height: 8),
                            Text(
                              'Manage your assets efficiently',
                              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                                    color: Theme.of(context).colorScheme.onSurfaceVariant,
                                  ),
                            ),
                          ],
                        ),
                      ),
                    ),
                    const SizedBox(height: 12),
                    const DashboardGamificationPanel(),
                    const SizedBox(height: 12),
                    const DashboardOllamaVoiceCard(),
                    const SizedBox(height: 10),
                    const DashboardSupportStrip(),
                    const SizedBox(height: 16),
                    _buildHelpdeskSummaryCard(context),
                    const SizedBox(height: 24),
                    Text(
                      'Quick Actions',
                      style: Theme.of(context).textTheme.titleLarge?.copyWith(
                            fontWeight: FontWeight.bold,
                          ),
                    ),
                    const SizedBox(height: 16),
                    _buildQuickActions(context),
                    if (_assetsLoading) ...[
                      const SizedBox(height: 24),
                      const Center(
                        child: Padding(
                          padding: EdgeInsets.all(24),
                          child: CircularProgressIndicator(strokeWidth: 2),
                        ),
                      ),
                    ],
                    if (!_assetsLoading && _assets.isNotEmpty) ...[
                      const SizedBox(height: 24),
                      Text(
                        'Browse by room',
                        style: Theme.of(context).textTheme.titleLarge?.copyWith(
                              fontWeight: FontWeight.bold,
                            ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        'List view or detail cards — switch with the layout toggle in the app bar.',
                        style: Theme.of(context).textTheme.bodySmall?.copyWith(color: KeeplyTokens.muted),
                      ),
                      const SizedBox(height: 12),
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
                          child: Center(
                            child: Text(
                              _room == 'All'
                                  ? 'No appliances to show yet.'
                                  : 'No appliances in this room.',
                              style: Theme.of(context).textTheme.bodyMedium?.copyWith(color: KeeplyTokens.muted),
                              textAlign: TextAlign.center,
                            ),
                          ),
                        )
                      else
                        ListenableBuilder(
                          listenable: ViewLayoutScope.notifierOf(context),
                          builder: (context, _) {
                            final preview = _filtered.take(_browsePreviewMax).toList();
                            final mode = ViewLayoutScope.modeOf(context);
                            if (mode == ViewLayoutMode.list) {
                              return Column(
                                children: [
                                  for (final a in preview)
                                    KeeplyAssetDetailListRow(
                                      asset: a,
                                      onTap: () => AssetDetailPage.pushIfValid(context, a.assetId),
                                    ),
                                ],
                              );
                            }
                            return Column(
                              children: [
                                for (final a in preview)
                                  Padding(
                                    padding: const EdgeInsets.only(bottom: 12),
                                    child: KeeplyAssetDetailCard(
                                      asset: a,
                                      onTap: () => AssetDetailPage.pushIfValid(context, a.assetId),
                                    ),
                                  ),
                              ],
                            );
                          },
                        ),
                      if (_filtered.isNotEmpty && _filtered.length > _browsePreviewMax)
                        Padding(
                          padding: const EdgeInsets.only(top: 8),
                          child: Align(
                            alignment: Alignment.center,
                            child: TextButton(
                              onPressed: () {
                                Navigator.push(
                                  context,
                                  MaterialPageRoute(builder: (_) => const AssetsListPage()),
                                );
                              },
                              child: const Text('View all assets'),
                            ),
                          ),
                        ),
                    ],
                  ],
                ),
              ),
            );
          }
          return const Center(child: CircularProgressIndicator());
        },
      ),
    );
  }

  Widget _buildHelpdeskSummaryCard(BuildContext context) {
    final t = Theme.of(context).textTheme;
    return Card(
      child: InkWell(
        borderRadius: BorderRadius.circular(12),
        onTap: () {
          Navigator.push<void>(
            context,
            MaterialPageRoute<void>(builder: (_) => const HelpdeskHubPage()),
          );
        },
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Row(
            children: [
              Icon(Icons.support_agent, color: Theme.of(context).colorScheme.primary, size: 28),
              const SizedBox(width: 14),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('Help & support', style: t.titleMedium?.copyWith(fontWeight: FontWeight.w700)),
                    Text(
                      'Tickets, questions, and FAQs from helpdesk-service.',
                      style: t.bodySmall?.copyWith(color: KeeplyTokens.muted, height: 1.35),
                    ),
                  ],
                ),
              ),
              Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  _helpdeskMiniStat(context, 'Tickets', _helpdeskIssueCount),
                  const SizedBox(width: 14),
                  _helpdeskMiniStat(context, 'Questions', _helpdeskQueryCount),
                ],
              ),
              const Icon(Icons.chevron_right, color: KeeplyTokens.muted),
            ],
          ),
        ),
      ),
    );
  }

  Widget _helpdeskMiniStat(BuildContext context, String label, int? value) {
    final t = Theme.of(context).textTheme;
    final v = value != null ? '$value' : (_assetsLoading ? '…' : '—');
    return Column(
      crossAxisAlignment: CrossAxisAlignment.end,
      children: [
        Text(v, style: t.titleSmall?.copyWith(fontWeight: FontWeight.w800)),
        Text(label, style: t.labelSmall?.copyWith(color: KeeplyTokens.muted)),
      ],
    );
  }

  Widget _buildQuickActions(BuildContext context) {
    return ListenableBuilder(
      listenable: ViewLayoutScope.notifierOf(context),
      builder: (context, _) {
        final actions = <({IconData icon, String title, VoidCallback onTap})>[
          (
            icon: Icons.support_agent,
            title: 'Help & support',
            onTap: () {
              Navigator.push(
                context,
                MaterialPageRoute<void>(builder: (_) => const HelpdeskHubPage()),
              );
            },
          ),
          (
            icon: Icons.category,
            title: 'Categories',
            onTap: () {
              Navigator.push(
                context,
                MaterialPageRoute<void>(builder: (_) => const MasterDataCatalogPage()),
              );
            },
          ),
          (
            icon: Icons.qr_code_scanner,
            title: 'Scan Asset',
            onTap: () {
              Navigator.push(
                context,
                MaterialPageRoute(builder: (_) => const AssetScanPage()),
              );
            },
          ),
          (
            icon: Icons.add_circle,
            title: 'Add Asset',
            onTap: () {
              Navigator.push(
                context,
                MaterialPageRoute(builder: (_) => const CreateAssetPage()),
              );
            },
          ),
          (
            icon: Icons.assignment,
            title: 'Compliance',
            onTap: () {
              Navigator.push(
                context,
                MaterialPageRoute<void>(builder: (_) => const AlertsHubPage()),
              );
            },
          ),
        ];
        if (ViewLayoutScope.modeOf(context) == ViewLayoutMode.list) {
          return Column(
            children: [
              for (final a in actions)
                Card(
                  margin: const EdgeInsets.only(bottom: 8),
                  child: ListTile(
                    leading: Icon(a.icon, color: Theme.of(context).primaryColor),
                    title: Text(a.title),
                    trailing: const Icon(Icons.chevron_right),
                    onTap: a.onTap,
                  ),
                ),
            ],
          );
        }
        return GridView.count(
          crossAxisCount: 2,
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          crossAxisSpacing: 16,
          mainAxisSpacing: 16,
          childAspectRatio: 1.5,
          children: [
            for (final a in actions)
              _buildActionCard(
                context,
                icon: a.icon,
                title: a.title,
                onTap: a.onTap,
              ),
          ],
        );
      },
    );
  }

  Widget _buildActionCard(
    BuildContext context, {
    required IconData icon,
    required String title,
    required VoidCallback onTap,
  }) {
    return Card(
      elevation: 2,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(icon, size: 40, color: Theme.of(context).primaryColor),
              const SizedBox(height: 8),
              Text(
                title,
                style: Theme.of(context).textTheme.titleMedium,
                textAlign: TextAlign.center,
              ),
            ],
          ),
        ),
      ),
    );
  }
}
