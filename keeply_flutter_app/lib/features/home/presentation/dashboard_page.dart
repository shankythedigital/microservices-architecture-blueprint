import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:keeply_app/core/preferences/keeply_app_preferences.dart';
import 'package:keeply_app/core/sync/app_data_refresh_cubit.dart';
import 'package:keeply_app/core/theme/keeply_tokens.dart';
import 'package:keeply_app/core/view_layout/view_layout_scope.dart';
import 'package:keeply_app/core/widgets/keeply_asset_views.dart';
import 'package:keeply_app/features/asset/data/datasources/asset_remote_datasource.dart';
import 'package:keeply_app/features/asset/data/models/asset_models.dart';
import 'package:keeply_app/features/auth/presentation/bloc/auth_bloc.dart';
import 'package:keeply_app/features/helpdesk/data/datasources/helpdesk_remote_datasource.dart';
import 'package:keeply_app/features/notification/data/datasources/notification_inbox_remote_datasource.dart';
import 'package:keeply_app/core/api/keeply_helpdesk_api.dart';
import 'package:keeply_app/features/asset/presentation/pages/asset_detail_page.dart';
import 'package:keeply_app/features/asset/presentation/pages/create_asset_page.dart';
import 'package:keeply_app/features/helpdesk/presentation/pages/helpdesk_hub_page.dart';
import 'package:keeply_app/features/home/presentation/dashboard_engagement.dart';
import 'package:keeply_app/features/home/presentation/keeply_dashboard_tour.dart';
import 'package:keeply_app/features/shell/presentation/pages/alerts_hub_page.dart';

/// Mirrors React `DashboardPage` layout (hero, CTA, reminders, appliance strip).
class DashboardPage extends StatefulWidget {
  const DashboardPage({super.key});

  @override
  State<DashboardPage> createState() => _DashboardPageState();
}

class _CoverageReminder {
  _CoverageReminder({
    required this.kind,
    required this.assetId,
    required this.assetName,
    required this.endDate,
    required this.daysLeft,
  });

  final String kind;
  final int assetId;
  final String assetName;
  final String endDate;
  final int daysLeft;
}

class _DashboardPageState extends State<DashboardPage> {
  final _assetDs = AssetRemoteDataSource();
  final _notifyDs = NotificationInboxRemoteDataSource();
  final _helpDs = HelpdeskRemoteDataSource();
  final GlobalKey _tourHeroKey = GlobalKey();
  final GlobalKey _tourCtaKey = GlobalKey();
  final GlobalKey _tourRemindersKey = GlobalKey();

  String? _err;
  int? _alertCount;
  int? _issueOpen;
  List<_CoverageReminder> _reminders = [];
  bool? _nyaOk;
  List<AssetMaster> _assets = [];
  String _room = 'All';
  bool _tourBusy = false;

  static const _rooms = ['All', 'Kitchen', 'Living room', 'Laundry', 'Other'];

  @override
  void initState() {
    super.initState();
    keeplyDashboardTourReplayBus.addListener(_onTourReplaySignal);
    WidgetsBinding.instance.addPostFrameCallback((_) async {
      await _load();
      if (!mounted) return;
      try {
        await KeeplyAppPrefsScope.of(context).recordDashboardVisit();
      } catch (_) {}
      await _queueHandsonTour();
    });
  }

  @override
  void dispose() {
    keeplyDashboardTourReplayBus.removeListener(_onTourReplaySignal);
    super.dispose();
  }

  void _onTourReplaySignal() {
    _queueHandsonTour();
  }

  Future<void> _queueHandsonTour() async {
    if (_tourBusy || !mounted) return;
    final prefs = KeeplyAppPrefsScope.of(context);
    if (prefs.handsonTourDone || prefs.reduceMotion) return;
    _tourBusy = true;
    await Future<void>.delayed(const Duration(milliseconds: 520));
    if (!mounted) {
      _tourBusy = false;
      return;
    }
    final p = KeeplyAppPrefsScope.of(context);
    if (p.handsonTourDone || p.reduceMotion) {
      _tourBusy = false;
      return;
    }
    if (!context.mounted) {
      _tourBusy = false;
      return;
    }
    startDashboardHandsonTour(
      context,
      heroKey: _tourHeroKey,
      ctaKey: _tourCtaKey,
      remindersKey: _tourRemindersKey,
      onFinished: () {
        p.setHandsonTourDone(true);
        _tourBusy = false;
      },
    );
  }

  Future<void> _load() async {
    final auth = context.read<AuthBloc>().state;
    if (auth is! AuthAuthenticated) return;
    final tokenUserId = auth.user.userId;

    setState(() {
      _err = null;
      _nyaOk = null;
    });

    try {
      final nc = await _notifyDs.notificationCount();
      var issues = <Map<String, dynamic>>[];
      try {
        issues = await _helpDs.listMyIssues();
      } catch (_) {}
      Map<String, dynamic>? nya;
      try {
        nya = await _assetDs.getNeedYourAttention();
        if (mounted) setState(() => _nyaOk = true);
      } catch (_) {
        if (mounted) setState(() => _nyaOk = false);
      }

      final reminders = _extractReminders(nya, maxDays: 14);
      final active = {'OPEN', 'IN_PROGRESS', 'REOPENED'};
      var openIssues = 0;
      for (final i in issues) {
        final s = i['status']?.toString();
        if (s != null && active.contains(s)) openIssues++;
      }

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
        _alertCount = nc;
        _issueOpen = openIssues;
        _reminders = reminders;
        _assets = list;
      });
    } catch (e) {
      if (mounted) setState(() => _err = '$e');
    }
  }

  List<_CoverageReminder> _extractReminders(Map<String, dynamic>? nya, {required int maxDays}) {
    if (nya == null) return [];
    final att = nya['attention'];
    if (att is! Map) return [];
    final today = DateTime.now();
    final startToday = DateTime(today.year, today.month, today.day);
    final rows = <_CoverageReminder>[];

    void pushList(dynamic list, String kind, String endKey) {
      if (list is! List) return;
      for (final item in list) {
        if (item is! Map) continue;
        final m = Map<String, dynamic>.from(item);
        final aid = (m['assetId'] as num?)?.toInt();
        if (aid == null) continue;
        final end = _parseDate(m[endKey]);
        if (end == null) continue;
        final startEnd = DateTime(end.year, end.month, end.day);
        final daysLeft = startEnd.difference(startToday).inDays;
        if (daysLeft < 0 || daysLeft > maxDays) continue;
        final name = (m['assetName'] as String?)?.trim();
        final rawEnd = m[endKey];
        final endIso = rawEnd is String && rawEnd.length <= 10 && rawEnd.isNotEmpty
            ? rawEnd.substring(0, rawEnd.length.clamp(0, 10))
            : '${startEnd.year}-${startEnd.month.toString().padLeft(2, '0')}-${startEnd.day.toString().padLeft(2, '0')}';
        rows.add(
          _CoverageReminder(
            kind: kind,
            assetId: aid,
            assetName: (name != null && name.isNotEmpty) ? name : 'Appliance',
            endDate: endIso,
            daysLeft: daysLeft,
          ),
        );
      }
    }

    pushList(att['expiringWarranties'], 'warranty', 'warrantyEndDate');
    pushList(att['expiringAmcs'], 'amc', 'amcEndDate');
    rows.sort((a, b) {
      final c = a.daysLeft.compareTo(b.daysLeft);
      if (c != 0) return c;
      return a.assetName.compareTo(b.assetName);
    });
    return rows;
  }

  DateTime? _parseDate(dynamic raw) {
    if (raw == null) return null;
    if (raw is String && raw.isNotEmpty) {
      final d = DateTime.tryParse(raw.length <= 10 ? '${raw}T12:00:00' : raw);
      return d;
    }
    if (raw is List && raw.length >= 3) {
      final y = (raw[0] as num).toInt();
      final mo = (raw[1] as num).toInt();
      final day = (raw[2] as num).toInt();
      return DateTime(y, mo, day);
    }
    return null;
  }

  String _daysLeftLabel(int d) {
    if (d <= 0) return 'today';
    if (d == 1) return 'tomorrow';
    return 'in $d days';
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
      listenWhen: (previous, current) =>
          previous.assetsTick != current.assetsTick || previous.helpdeskTick != current.helpdeskTick,
      listener: (context, state) {
        _load();
      },
      child: RefreshIndicator(
      onRefresh: _load,
      child: ListView(
        padding: const EdgeInsets.fromLTRB(18, 18, 18, 28),
        physics: const AlwaysScrollableScrollPhysics(),
        children: [
          KeyedSubtree(
            key: _tourHeroKey,
            child: _Hero(t: t),
          ),
          if (_err != null) ...[
            const SizedBox(height: 10),
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
          const SizedBox(height: 16),
          KeyedSubtree(
            key: _tourCtaKey,
            child: _CtaCard(
              onTap: () {
                Navigator.of(context).push<void>(
                  MaterialPageRoute(builder: (_) => const CreateAssetPage()),
                );
              },
            ),
          ),
          const SizedBox(height: 14),
          const DashboardGamificationPanel(),
          const SizedBox(height: 14),
          const DashboardOllamaVoiceCard(),
          const SizedBox(height: 10),
          const DashboardSupportStrip(),
          const SizedBox(height: 20),
          KeyedSubtree(
            key: _tourRemindersKey,
            child: _RemindersSection(
              t: t,
              reminders: _reminders,
              nyaOk: _nyaOk,
              alertCount: _alertCount,
              issueOpen: _issueOpen,
              daysLeftLabel: _daysLeftLabel,
              onOpenAsset: (assetId) => AssetDetailPage.pushIfValid(context, assetId),
            ),
          ),
          const SizedBox(height: 12),
          _HelpCard(t: t),
          if (_assets.isNotEmpty) ...[
            const SizedBox(height: 20),
            Text('Browse by room', style: t.titleSmall?.copyWith(fontWeight: FontWeight.w600)),
            const SizedBox(height: 10),
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
                  _room == 'All'
                      ? 'No appliances to show yet.'
                      : 'No appliances in this room.',
                  style: t.bodyMedium?.copyWith(color: Theme.of(context).colorScheme.onSurfaceVariant),
                  textAlign: TextAlign.center,
                ),
              )
            else
              ListenableBuilder(
                listenable: ViewLayoutScope.notifierOf(context),
                builder: (context, _) {
                  final preview = _filtered.take(12).toList();
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

class _Hero extends StatelessWidget {
  const _Hero({required this.t});

  final TextTheme t;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(KeeplyTokens.radiusSm),
        gradient: LinearGradient(
          colors: [
            KeeplyTokens.accent.withValues(alpha: 0.12),
            scheme.surface,
          ],
        ),
        border: Border.all(color: scheme.outline.withValues(alpha: 0.28)),
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Container(
                      width: 12,
                      height: 12,
                      decoration: const BoxDecoration(
                        color: KeeplyTokens.accent,
                        shape: BoxShape.circle,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 10),
                Text(
                  'Manage your home assets',
                  style: t.titleLarge?.copyWith(fontWeight: FontWeight.w800, letterSpacing: -0.03),
                ),
                const SizedBox(height: 6),
                Text(
                  'Keep your appliances organized with reminders for service and warranties.',
                  style: t.bodySmall?.copyWith(color: scheme.onSurfaceVariant, height: 1.45),
                ),
              ],
            ),
          ),
          const SizedBox(width: 8),
          Container(
            width: 72,
            height: 72,
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(20),
              gradient: LinearGradient(
                colors: [
                  KeeplyTokens.accent.withValues(alpha: 0.35),
                  KeeplyTokens.accent.withValues(alpha: 0.08),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _CtaCard extends StatelessWidget {
  const _CtaCard({required this.onTap});

  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Material(
      color: scheme.primary.withValues(alpha: 0.12),
      borderRadius: BorderRadius.circular(KeeplyTokens.radiusSm),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(KeeplyTokens.radiusSm),
        child: Container(
          padding: const EdgeInsets.symmetric(vertical: 16, horizontal: 18),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(KeeplyTokens.radiusSm),
            border: Border.all(color: scheme.primary.withValues(alpha: 0.35)),
          ),
          child: Row(
            children: [
              Container(
                width: 44,
                height: 44,
                decoration: BoxDecoration(
                  color: scheme.primary.withValues(alpha: 0.2),
                  borderRadius: BorderRadius.circular(14),
                ),
                child: Icon(Icons.add, color: scheme.primary),
              ),
              const SizedBox(width: 14),
              Expanded(
                child: Text(
                  'Add new assets',
                  style: TextStyle(fontWeight: FontWeight.w700, fontSize: 16, color: scheme.onSurface),
                ),
              ),
              Icon(Icons.chevron_right_rounded, color: scheme.onSurfaceVariant),
            ],
          ),
        ),
      ),
    );
  }
}

class _RemindersSection extends StatelessWidget {
  const _RemindersSection({
    required this.t,
    required this.reminders,
    required this.nyaOk,
    required this.alertCount,
    required this.issueOpen,
    required this.daysLeftLabel,
    required this.onOpenAsset,
  });

  final TextTheme t;
  final List<_CoverageReminder> reminders;
  final bool? nyaOk;
  final int? alertCount;
  final int? issueOpen;
  final String Function(int) daysLeftLabel;
  final void Function(int assetId) onOpenAsset;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: scheme.surface,
        borderRadius: BorderRadius.circular(KeeplyTokens.radiusSm),
        border: Border.all(color: scheme.outline.withValues(alpha: 0.28)),
        boxShadow: [
          BoxShadow(color: scheme.shadow.withValues(alpha: 0.08), blurRadius: 14, offset: const Offset(0, 4)),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text('Upcoming reminders', style: t.titleSmall?.copyWith(fontWeight: FontWeight.w700)),
          const SizedBox(height: 6),
          Wrap(
            crossAxisAlignment: WrapCrossAlignment.center,
            spacing: 4,
            children: [
              Text(
                'Alerts in the current window ',
                style: t.bodySmall?.copyWith(color: scheme.onSurfaceVariant, height: 1.4),
              ),
              if (alertCount != null)
                Text(
                  '$alertCount',
                  style: t.bodySmall?.copyWith(fontWeight: FontWeight.w700, color: scheme.onSurface),
                ),
              if (alertCount != null)
                Text(' in-app items · ', style: t.bodySmall?.copyWith(color: scheme.onSurfaceVariant)),
              if (alertCount == null)
                Text('— · ', style: t.bodySmall?.copyWith(color: scheme.onSurfaceVariant)),
              InkWell(
                onTap: () {
                  Navigator.of(context).push<void>(
                    MaterialPageRoute<void>(builder: (_) => const AlertsHubPage(wrapWithScaffold: true)),
                  );
                },
                child: Text(
                  'Open alerts',
                  style: t.bodySmall?.copyWith(color: scheme.primary, fontWeight: FontWeight.w600),
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          if (reminders.isNotEmpty)
            ...reminders.take(8).map(
                  (r) => InkWell(
                    onTap: () => onOpenAsset(r.assetId),
                    child: Padding(
                      padding: const EdgeInsets.only(bottom: 8),
                      child: Text.rich(
                        TextSpan(
                          style: t.bodySmall?.copyWith(height: 1.35),
                          children: [
                            TextSpan(text: r.assetName, style: const TextStyle(fontWeight: FontWeight.w700)),
                            TextSpan(
                              text:
                                  ' — ${r.kind == 'warranty' ? 'Warranty' : 'AMC'} ends ${r.endDate} (${daysLeftLabel(r.daysLeft)}).',
                            ),
                          ],
                        ),
                      ),
                    ),
                  ),
                ),
          if (reminders.isEmpty && nyaOk == null)
            Text('Loading reminders…', style: t.bodySmall?.copyWith(color: scheme.onSurfaceVariant)),
          if (reminders.isEmpty && nyaOk == false)
            Text(
              'Could not load coverage reminders. Open My appliances to check warranty and AMC dates.',
              style: t.bodySmall?.copyWith(color: scheme.onSurfaceVariant, height: 1.4),
            ),
          if (reminders.isEmpty && nyaOk == true)
            Text(
              'No warranty or AMC expiry in the next 14 days.',
              style: t.bodySmall?.copyWith(color: scheme.onSurfaceVariant),
            ),
          const SizedBox(height: 12),
          OutlinedButton(
            onPressed: () {
              Navigator.of(context).push<void>(
                MaterialPageRoute<void>(
                  builder: (_) => HelpdeskMyIssuesPage(api: KeeplyHelpdeskApi()),
                ),
              );
            },
            child: Text('Service issues${issueOpen != null ? ' ($issueOpen)' : ''}'),
          ),
        ],
      ),
    );
  }
}

class _HelpCard extends StatelessWidget {
  const _HelpCard({required this.t});

  final TextTheme t;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Material(
      color: scheme.surface,
      borderRadius: BorderRadius.circular(KeeplyTokens.radiusSm),
      child: InkWell(
        onTap: () {
          Navigator.of(context).push<void>(
            MaterialPageRoute(builder: (_) => const HelpdeskHubPage()),
          );
        },
        borderRadius: BorderRadius.circular(KeeplyTokens.radiusSm),
        child: Container(
          width: double.infinity,
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(KeeplyTokens.radiusSm),
            border: Border.all(color: scheme.outline.withValues(alpha: 0.28)),
          ),
          child: Row(
            children: [
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('Help & support', style: t.titleSmall?.copyWith(fontWeight: FontWeight.w700)),
                    const SizedBox(height: 4),
                    Text(
                      'FAQs, queries, and knowledge articles.',
                      style: t.bodySmall?.copyWith(color: scheme.onSurfaceVariant),
                    ),
                  ],
                ),
              ),
              Icon(Icons.chevron_right_rounded, color: scheme.onSurfaceVariant),
            ],
          ),
        ),
      ),
    );
  }
}

