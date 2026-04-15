import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:keeply_app/core/api/keeply_service_url.dart';
import 'package:keeply_app/core/theme/keeply_tokens.dart';
import 'package:keeply_app/core/view_layout/view_layout_scope.dart';
import 'package:keeply_app/features/asset/presentation/pages/create_asset_page.dart';
import 'package:keeply_app/features/auth/presentation/bloc/auth_bloc.dart';
import 'package:keeply_app/features/home/presentation/dashboard_page.dart';
import 'package:keeply_app/features/home/presentation/appliances_page.dart';
import 'package:keeply_app/features/notification/data/datasources/notification_inbox_remote_datasource.dart';
import 'package:keeply_app/features/shell/presentation/pages/account_hub_page.dart';
import 'package:keeply_app/features/shell/presentation/pages/alerts_hub_page.dart';
import 'package:keeply_app/features/shell/presentation/pages/tips_hub_page.dart';

/// Matches React `MobileShell` — max-width phone frame, header, scroll body, bottom nav + FAB.
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

    return Scaffold(
      backgroundColor: KeeplyTokens.surface,
      body: SafeArea(
        bottom: false,
        child: Center(
          child: ConstrainedBox(
            constraints: const BoxConstraints(maxWidth: KeeplyTokens.maxAppWidth),
            child: Container(
              decoration: const BoxDecoration(
                color: KeeplyTokens.surface,
                boxShadow: [
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
                      decoration: const BoxDecoration(
                        gradient: LinearGradient(
                          begin: Alignment.topCenter,
                          end: Alignment.bottomCenter,
                          colors: [
                            Color(0xEBF8FAFC),
                            Color(0xFFF3F6FA),
                            Color(0xFFF1F5F9),
                          ],
                          stops: [0.0, 0.48, 1.0],
                        ),
                      ),
                      child: IndexedStack(
                        index: _tab,
                        children: const [
                          DashboardPage(),
                          AppliancesPage(),
                          TipsHubPage(),
                          AlertsHubPage(),
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
                    onFab: () async {
                      await Navigator.of(context).push<void>(
                        MaterialPageRoute(builder: (_) => const CreateAssetPage()),
                      );
                      if (mounted) _refreshInbox();
                    },
                  ),
                ],
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
    return Material(
      color: Colors.white.withValues(alpha: 0.86),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
        decoration: const BoxDecoration(
          border: Border(bottom: BorderSide(color: Color(0xE6E2E8F0))),
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
                      width: 36,
                      height: 36,
                      alignment: Alignment.center,
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        color: KeeplyTokens.accent.withValues(alpha: 0.14),
                        border: Border.all(color: KeeplyTokens.line),
                      ),
                      clipBehavior: Clip.antiAlias,
                      child: avatarUrl != null
                          ? Image.network(
                              avatarUrl!,
                              fit: BoxFit.cover,
                              width: 36,
                              height: 36,
                              errorBuilder: (_, __, ___) => Text(
                                avatarLetter,
                                style: const TextStyle(
                                  fontWeight: FontWeight.w700,
                                  color: KeeplyTokens.accentInk,
                                ),
                              ),
                            )
                          : Text(
                              avatarLetter,
                              style: const TextStyle(
                                fontWeight: FontWeight.w700,
                                color: KeeplyTokens.accentInk,
                              ),
                            ),
                    ),
                    const SizedBox(width: 8),
                    Text(
                      'Account',
                      style: Theme.of(context).textTheme.labelSmall?.copyWith(color: KeeplyTokens.muted),
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
    required this.onFab,
  });

  final int tab;
  final int? inboxCount;
  final void Function(int) onSelect;
  final VoidCallback onFab;

  @override
  Widget build(BuildContext context) {
    final bottom = MediaQuery.paddingOf(context).bottom;
    return Stack(
      clipBehavior: Clip.none,
      alignment: Alignment.bottomCenter,
      children: [
        Container(
          decoration: const BoxDecoration(
            gradient: LinearGradient(
              begin: Alignment.topCenter,
              end: Alignment.bottomCenter,
              colors: [KeeplyTokens.navBgTop, KeeplyTokens.navBg],
            ),
            borderRadius: BorderRadius.vertical(top: Radius.circular(28)),
            boxShadow: [
              BoxShadow(
                color: Color(0x240F172A),
                blurRadius: 32,
                offset: Offset(0, -12),
              ),
            ],
            border: Border(top: BorderSide(color: Color(0x0FFFFFFF))),
          ),
          padding: EdgeInsets.fromLTRB(10, 10, 10, 12 + bottom),
          child: Row(
            children: [
              Expanded(child: _NavBtn(icon: Icons.home_rounded, active: tab == 0, onTap: () => onSelect(0))),
              Expanded(child: _NavBtn(icon: Icons.grid_view_rounded, active: tab == 1, onTap: () => onSelect(1))),
              const SizedBox(width: 56),
              Expanded(child: _NavBtn(icon: Icons.public_rounded, active: tab == 2, onTap: () => onSelect(2))),
              Expanded(
                child: _NavBtn(
                  icon: Icons.notifications_none_rounded,
                  active: tab == 3,
                  badge: inboxCount != null && inboxCount! > 0 ? (inboxCount! > 99 ? '99+' : '$inboxCount') : null,
                  onTap: () => onSelect(3),
                ),
              ),
            ],
          ),
        ),
        Positioned(
          bottom: 18 + bottom,
          child: Material(
            elevation: 10,
            color: Colors.transparent,
            shape: const CircleBorder(),
            child: InkWell(
              customBorder: const CircleBorder(),
              onTap: onFab,
              child: Ink(
                width: 64,
                height: 64,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  border: Border.all(color: KeeplyTokens.fabRing, width: 10),
                  gradient: const LinearGradient(
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                    colors: [Color(0xFF1A5249), KeeplyTokens.fab],
                  ),
                  boxShadow: const [
                    BoxShadow(color: Color(0x330F172A), blurRadius: 28, offset: Offset(0, 12)),
                  ],
                ),
                child: const Center(
                  child: Text('+', style: TextStyle(color: Colors.white, fontSize: 28, height: 1)),
                ),
              ),
            ),
          ),
        ),
      ],
    );
  }
}

class _NavBtn extends StatelessWidget {
  const _NavBtn({
    required this.icon,
    required this.active,
    required this.onTap,
    this.badge,
  });

  final IconData icon;
  final bool active;
  final VoidCallback onTap;
  final String? badge;

  @override
  Widget build(BuildContext context) {
    final c = active ? KeeplyTokens.navActive : KeeplyTokens.navInk;
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(12),
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 8),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Stack(
              clipBehavior: Clip.none,
              children: [
                Icon(icon, color: c, size: 24),
                if (badge != null)
                  Positioned(
                    right: -8,
                    top: -4,
                    child: Container(
                      padding: const EdgeInsets.symmetric(horizontal: 5),
                      constraints: const BoxConstraints(minWidth: 18, minHeight: 18),
                      decoration: BoxDecoration(
                        color: KeeplyTokens.badgeBg,
                        borderRadius: BorderRadius.circular(999),
                        boxShadow: const [BoxShadow(color: Color(0x330F172A), blurRadius: 6, offset: Offset(0, 2))],
                      ),
                      alignment: Alignment.center,
                      child: Text(
                        badge!,
                        style: const TextStyle(
                          fontSize: 10,
                          fontWeight: FontWeight.w800,
                          color: KeeplyTokens.badgeInk,
                          height: 1.2,
                        ),
                      ),
                    ),
                  ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
