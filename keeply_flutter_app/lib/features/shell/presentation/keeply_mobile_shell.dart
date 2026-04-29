import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:keeply_app/core/api/keeply_service_url.dart';
import 'package:keeply_app/core/sync/app_data_refresh_cubit.dart';
import 'package:keeply_app/core/theme/keeply_tokens.dart';
import 'package:keeply_app/core/view_layout/view_layout_scope.dart';
import 'package:keeply_app/features/asset/presentation/bloc/asset_bloc.dart';
import 'package:keeply_app/features/asset/presentation/pages/create_asset_page.dart';
import 'package:keeply_app/features/auth/presentation/bloc/auth_bloc.dart';
import 'package:keeply_app/features/home/presentation/browse_rooms_page.dart';
import 'package:keeply_app/features/home/presentation/dashboard_page.dart';
import 'package:keeply_app/features/notification/data/datasources/notification_inbox_remote_datasource.dart';
import 'package:keeply_app/features/shell/presentation/pages/account_hub_page.dart';
import 'package:keeply_app/features/shell/presentation/pages/alerts_hub_page.dart';
import 'package:keeply_app/features/shell/presentation/pages/tips_hub_page.dart';

/// Matches React `MobileShell` — max-width phone frame, header, scroll body, bottom nav.
class KeeplyMobileShell extends StatefulWidget {
  const KeeplyMobileShell({super.key});

  @override
  State<KeeplyMobileShell> createState() => _KeeplyMobileShellState();
}

class _KeeplyMobileShellState extends State<KeeplyMobileShell> {
  int _tab = 0;
  int? _inboxCount;

  @override
  void initState() {
    super.initState();
    _refreshInbox();
  }

  Future<void> _refreshInbox() async {
    final n = await NotificationInboxRemoteDataSource().notificationCount();
    if (mounted) setState(() => _inboxCount = n);
  }

  String? _photoUrl(String? profilePhotoUrl) {
    if (profilePhotoUrl == null || profilePhotoUrl.isEmpty) return null;
    if (profilePhotoUrl.startsWith('http://') || profilePhotoUrl.startsWith('https://')) {
      return profilePhotoUrl;
    }
    final base = keeplyServiceBase(KeeplyApiService.auth);
    final path = profilePhotoUrl.startsWith('/') ? profilePhotoUrl : '/$profilePhotoUrl';
    return '$base$path';
  }

  @override
  Widget build(BuildContext context) {
    final auth = context.watch<AuthBloc>().state;
    final user = auth is AuthAuthenticated ? auth.user : null;

    return MultiBlocListener(
      listeners: [
        BlocListener<AssetBloc, AssetState>(
          listenWhen: (previous, current) =>
              current is AssetCreated ||
              current is AssetUpdated ||
              current is AssetDeleted ||
              current is CategoryCreated ||
              current is CategoriesBulkCreated ||
              current is CategoriesExcelUploaded,
          listener: (context, state) {
            context.read<AppDataRefreshCubit>().bump(KeeplyDataChannel.assets);
          },
        ),
      ],
      child: Scaffold(
      backgroundColor: Theme.of(context).colorScheme.surface,
      body: SafeArea(
        bottom: false,
        child: Center(
          child: ConstrainedBox(
            constraints: const BoxConstraints(maxWidth: KeeplyTokens.maxAppWidth),
            child: Container(
              decoration: BoxDecoration(
                color: Theme.of(context).colorScheme.surface,
                boxShadow: const [
                  BoxShadow(
                    color: Color(0x0F0F172A),
                    blurRadius: 48,
                    offset: Offset(0, 24),
                  ),
                ],
              ),
              child: Column(
                children: [
                  _Header(
                    avatarUrl: _photoUrl(user?.profilePhotoUrl),
                    avatarLetter: _initial(user),
                    onAccount: () async {
                      await Navigator.of(context).push<void>(
                        MaterialPageRoute(builder: (_) => const AccountHubPage()),
                      );
                      if (mounted) _refreshInbox();
                    },
                  ),
                  Expanded(
                    child: Container(
                      width: double.infinity,
                      decoration: BoxDecoration(
                        gradient: LinearGradient(
                          begin: Alignment.topCenter,
                          end: Alignment.bottomCenter,
                          colors: Theme.of(context).brightness == Brightness.dark
                              ? const [
                                  Color(0xFF1E293B),
                                  Color(0xFF0F172A),
                                  Color(0xFF0B1220),
                                ]
                              : const [
                                  Color(0xEBF8FAFC),
                                  Color(0xFFF3F6FA),
                                  Color(0xFFF1F5F9),
                                ],
                          stops: const [0.0, 0.48, 1.0],
                        ),
                      ),
                      child: IndexedStack(
                        index: _tab,
                        children: [
                          const DashboardPage(),
                          const BrowseRoomsPage(),
                          CreateAssetPage(
                            embeddedDismiss: () => setState(() => _tab = 0),
                          ),
                          const TipsHubPage(),
                          const AlertsHubPage(wrapWithScaffold: false),
                        ],
                      ),
                    ),
                  ),
                  _BottomNav(
                    tab: _tab,
                    inboxCount: _inboxCount,
                    onSelect: (i) {
                      setState(() => _tab = i);
                      _refreshInbox();
                    },
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    ),
    );
  }

  String _initial(dynamic user) {
    if (user == null) return '?';
    final u = user.username;
    if (u != null && u.isNotEmpty) return u.substring(0, 1).toUpperCase();
    return 'U';
  }
}

class _Header extends StatelessWidget {
  const _Header({
    required this.avatarUrl,
    required this.avatarLetter,
    required this.onAccount,
  });

  final String? avatarUrl;
  final String avatarLetter;
  final VoidCallback onAccount;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Material(
      color: scheme.surface.withValues(alpha: 0.92),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
        decoration: BoxDecoration(
          border: Border(bottom: BorderSide(color: Theme.of(context).colorScheme.outline.withValues(alpha: 0.25))),
        ),
        child: Row(
          children: [
            Row(
              children: [
                Container(
                  width: 10,
                  height: 10,
                  decoration: const BoxDecoration(
                    color: KeeplyTokens.accent,
                    shape: BoxShape.circle,
                  ),
                ),
                const SizedBox(width: 8),
                Text(
                  'Keeply',
                  style: Theme.of(context).textTheme.titleMedium?.copyWith(
                        fontWeight: FontWeight.w800,
                        letterSpacing: -0.02,
                      ),
                ),
              ],
            ),
            const Spacer(),
            const ViewLayoutToggle(compact: true),
            const SizedBox(width: 8),
            InkWell(
              onTap: onAccount,
              borderRadius: BorderRadius.circular(999),
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                child: Row(
                  children: [
                    Container(
                      width: 40,
                      height: 40,
                      alignment: Alignment.center,
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        color: scheme.primary.withValues(alpha: 0.14),
                        border: Border.all(color: scheme.outline.withValues(alpha: 0.35)),
                      ),
                      clipBehavior: Clip.antiAlias,
                      child: avatarUrl != null
                          ? CachedNetworkImage(
                              imageUrl: avatarUrl!,
                              fit: BoxFit.cover,
                              width: 40,
                              height: 40,
                              memCacheWidth: 120,
                              placeholder: (_, __) => const SizedBox(
                                width: 18,
                                height: 18,
                                child: CircularProgressIndicator(strokeWidth: 2),
                              ),
                              errorWidget: (_, __, ___) => Text(
                                avatarLetter,
                                style: TextStyle(
                                  fontWeight: FontWeight.w700,
                                  color: scheme.primary,
                                ),
                              ),
                            )
                          : Text(
                              avatarLetter,
                              style: TextStyle(
                                fontWeight: FontWeight.w700,
                                color: scheme.primary,
                              ),
                            ),
                    ),
                    const SizedBox(width: 8),
                    Text(
                      'Account',
                      style: Theme.of(context).textTheme.labelSmall?.copyWith(color: scheme.onSurfaceVariant),
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _BottomNav extends StatelessWidget {
  const _BottomNav({
    required this.tab,
    required this.inboxCount,
    required this.onSelect,
  });

  final int tab;
  final int? inboxCount;
  final void Function(int) onSelect;

  @override
  Widget build(BuildContext context) {
    final bottom = MediaQuery.paddingOf(context).bottom;
    final scheme = Theme.of(context).colorScheme;
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final cardBg = isDark ? const Color(0xFF1E293B) : KeeplyTokens.surface;
    final bubbleColor = isDark ? const Color(0xFF334155) : Colors.white;

    return Padding(
      padding: EdgeInsets.fromLTRB(16, 22, 16, 10 + bottom),
      child: Material(
        color: cardBg,
        elevation: isDark ? 14 : 10,
        shadowColor: scheme.shadow.withValues(alpha: isDark ? 0.55 : 0.2),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(32),
          side: BorderSide(color: scheme.outline.withValues(alpha: isDark ? 0.35 : 0.18)),
        ),
        clipBehavior: Clip.none,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const SizedBox(height: 8),
            Container(
              height: 2,
              margin: const EdgeInsets.symmetric(horizontal: 20),
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(999),
                gradient: LinearGradient(
                  colors: [
                    scheme.outline.withValues(alpha: 0.15),
                    scheme.primary.withValues(alpha: 0.22),
                    scheme.outline.withValues(alpha: 0.12),
                  ],
                ),
              ),
            ),
            Padding(
              padding: const EdgeInsets.fromLTRB(4, 10, 4, 8),
              child: Row(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  Expanded(
                    child: _FloatingNavSlot(
                      selected: tab == 0,
                      icon: Icons.home_outlined,
                      selectedIcon: Icons.home_rounded,
                      label: 'Home',
                      tooltip: 'Home',
                      bubbleColor: bubbleColor,
                      onTap: () => onSelect(0),
                    ),
                  ),
                  Expanded(
                    child: _FloatingNavSlot(
                      selected: tab == 1,
                      icon: Icons.meeting_room_outlined,
                      selectedIcon: Icons.meeting_room_rounded,
                      label: 'Browse by',
                      labelSecond: 'rooms',
                      tooltip: 'Browse by rooms',
                      bubbleColor: bubbleColor,
                      onTap: () => onSelect(1),
                    ),
                  ),
                  Expanded(
                    child: _FloatingNavSlot(
                      selected: tab == 2,
                      icon: Icons.add_circle_outline,
                      selectedIcon: Icons.add_circle_rounded,
                      label: 'Add',
                      labelSecond: 'asset',
                      tooltip: 'Add asset',
                      bubbleColor: bubbleColor,
                      onTap: () => onSelect(2),
                    ),
                  ),
                  Expanded(
                    child: _FloatingNavSlot(
                      selected: tab == 3,
                      icon: Icons.menu_book_outlined,
                      selectedIcon: Icons.menu_book_rounded,
                      label: 'Tips &',
                      labelSecond: 'knowledge',
                      tooltip: 'Tips & knowledge',
                      bubbleColor: bubbleColor,
                      onTap: () => onSelect(3),
                    ),
                  ),
                  Expanded(
                    child: _FloatingNavSlot(
                      selected: tab == 4,
                      icon: Icons.notifications_outlined,
                      selectedIcon: Icons.notifications_rounded,
                      label: 'Notifications',
                      tooltip: 'Notification inbox',
                      bubbleColor: bubbleColor,
                      badge: inboxCount != null && inboxCount! > 0 ? (inboxCount! > 99 ? '99+' : '$inboxCount') : null,
                      onTap: () => onSelect(4),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

/// Floating-card style: selected tab sits in an elevated circle that breaks above the bar.
class _FloatingNavSlot extends StatelessWidget {
  const _FloatingNavSlot({
    required this.selected,
    required this.icon,
    required this.selectedIcon,
    required this.label,
    required this.tooltip,
    required this.bubbleColor,
    required this.onTap,
    this.labelSecond,
    this.badge,
  });

  final bool selected;
  final IconData icon;
  final IconData selectedIcon;
  final String label;
  final String? labelSecond;
  final String tooltip;
  final Color bubbleColor;
  final VoidCallback onTap;
  final String? badge;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    final primary = scheme.primary;
    final muted = scheme.onSurfaceVariant;

    return Tooltip(
      message: tooltip,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(18),
        splashColor: primary.withValues(alpha: 0.12),
        highlightColor: primary.withValues(alpha: 0.06),
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 2, vertical: 2),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              SizedBox(
                height: 46,
                child: Stack(
                  clipBehavior: Clip.none,
                  alignment: Alignment.center,
                  children: [
                    if (selected)
                      Transform.translate(
                        offset: const Offset(0, -16),
                        child: SizedBox(
                          width: 50,
                          height: 50,
                          child: Stack(
                            clipBehavior: Clip.none,
                            alignment: Alignment.center,
                            children: [
                              Material(
                                elevation: 10,
                                shadowColor: Colors.black.withValues(alpha: 0.22),
                                color: bubbleColor,
                                shape: const CircleBorder(),
                                child: SizedBox(
                                  width: 50,
                                  height: 50,
                                  child: Icon(selectedIcon, color: primary, size: 26),
                                ),
                              ),
                              if (badge != null)
                                Positioned(
                                  top: 0,
                                  right: 0,
                                  child: _NavBadge(text: badge!),
                                ),
                            ],
                          ),
                        ),
                      )
                    else
                      Stack(
                        clipBehavior: Clip.none,
                        alignment: Alignment.center,
                        children: [
                          Icon(icon, color: muted, size: 24),
                          if (badge != null)
                            Positioned(
                              right: -2,
                              top: -4,
                              child: _NavBadge(text: badge!),
                            ),
                        ],
                      ),
                  ],
                ),
              ),
              const SizedBox(height: 2),
              FittedBox(
                fit: BoxFit.scaleDown,
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Text(
                      label,
                      maxLines: 1,
                      textAlign: TextAlign.center,
                      style: TextStyle(
                        fontSize: labelSecond == null ? 10 : 9,
                        fontWeight: selected ? FontWeight.w700 : FontWeight.w500,
                        color: selected ? primary : muted,
                        height: 1.05,
                      ),
                    ),
                    if (labelSecond != null)
                      Text(
                        labelSecond!,
                        maxLines: 1,
                        textAlign: TextAlign.center,
                        style: TextStyle(
                          fontSize: 9,
                          fontWeight: selected ? FontWeight.w700 : FontWeight.w500,
                          color: selected ? primary : muted,
                          height: 1.05,
                        ),
                      ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _NavBadge extends StatelessWidget {
  const _NavBadge({required this.text});

  final String text;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 4),
      constraints: const BoxConstraints(minWidth: 16, minHeight: 16),
      decoration: BoxDecoration(
        color: KeeplyTokens.badgeBg,
        borderRadius: BorderRadius.circular(999),
        boxShadow: const [BoxShadow(color: Color(0x330F172A), blurRadius: 6, offset: Offset(0, 2))],
      ),
      alignment: Alignment.center,
      child: Text(
        text,
        style: const TextStyle(
          fontSize: 9,
          fontWeight: FontWeight.w800,
          color: KeeplyTokens.badgeInk,
          height: 1.1,
        ),
      ),
    );
  }
}
