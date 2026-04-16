import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:keeply_app/core/api/keeply_auth_api.dart';
import 'package:keeply_app/core/preferences/keeply_app_preferences.dart';
import 'package:keeply_app/core/theme/keeply_tokens.dart';
import 'package:keeply_app/features/auth/presentation/bloc/auth_bloc.dart';
import 'package:keeply_app/features/auth/presentation/pages/welcome_page.dart';
import 'package:keeply_app/features/helpdesk/presentation/pages/helpdesk_hub_page.dart';
import 'package:keeply_app/features/master_data/presentation/pages/master_data_catalog_page.dart';
import 'package:keeply_app/features/shell/presentation/pages/account_profile_page.dart';
import 'package:keeply_app/features/shell/presentation/pages/alerts_hub_page.dart';
import 'package:keeply_app/features/shell/presentation/pages/tips_hub_page.dart';
import 'package:keeply_app/features/home/presentation/keeply_dashboard_tour.dart';
import 'package:keeply_app/features/shell/presentation/pages/support_chat_page.dart';
import 'package:keeply_app/features/shell/presentation/pages/voice_catalog_assist_page.dart';
import 'package:package_info_plus/package_info_plus.dart';

/// Parity with `keeply_react_app/src/pages/SettingsPage.tsx` — device preferences + shortcuts + security.
class AccountSettingsPage extends StatefulWidget {
  const AccountSettingsPage({super.key});

  @override
  State<AccountSettingsPage> createState() => _AccountSettingsPageState();
}

class _AccountSettingsPageState extends State<AccountSettingsPage> {
  String? _versionLine;
  bool _signOutAllBusy = false;

  @override
  void initState() {
    super.initState();
    PackageInfo.fromPlatform().then((p) {
      if (!mounted) return;
      setState(() => _versionLine = '${p.appName} ${p.version} (${p.buildNumber})');
    });
  }

  Future<void> _signOutAllDevices() async {
    final auth = context.read<AuthBloc>().state;
    if (auth is! AuthAuthenticated) return;
    setState(() => _signOutAllBusy = true);
    try {
      await KeeplyAuthApi().logoutAllDevicesOnServer();
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Server sign-out failed; signing out on this device only.')),
        );
      }
    } finally {
      if (mounted) {
        setState(() => _signOutAllBusy = false);
        context.read<AuthBloc>().add(LogoutEvent());
        Navigator.of(context).pushAndRemoveUntil(
          MaterialPageRoute<void>(builder: (_) => const WelcomePage()),
          (_) => false,
        );
      }
    }
  }

  Widget _sectionTitle(String text) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(4, 20, 4, 10),
      child: Text(text, style: Theme.of(context).textTheme.titleSmall?.copyWith(fontWeight: FontWeight.w800)),
    );
  }

  Widget _footnote(String text) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(4, 8, 4, 0),
      child: Text(
        text,
        style: Theme.of(context).textTheme.bodySmall?.copyWith(color: KeeplyTokens.muted, height: 1.45),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    return Scaffold(
      appBar: AppBar(title: const Text('Settings')),
      body: ListenableBuilder(
        listenable: KeeplyAppPrefsScope.of(context),
        builder: (context, _) {
          final prefs = KeeplyAppPrefsScope.of(context);
          return ListView(
        padding: const EdgeInsets.fromLTRB(18, 8, 18, 32),
        children: [
          Text(
            'Preferences are saved on this device. Connect a profile API later to sync some of these across sign-in.',
            style: t.bodySmall?.copyWith(color: KeeplyTokens.muted, height: 1.45),
          ),
          _sectionTitle('Display & accessibility'),
          _selectAppearance(prefs),
          ListTile(
            contentPadding: EdgeInsets.zero,
            leading: const Icon(Icons.flag_outlined),
            title: const Text('Replay home tour'),
            subtitle: const Text('Coach marks on the first tab (Home) next time you open it'),
            trailing: const Icon(Icons.chevron_right_rounded),
            onTap: () async {
              await prefs.setHandsonTourDone(false);
              requestKeeplyDashboardTourReplay();
              if (context.mounted) {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('Switch to the Home tab if the tour does not appear immediately.')),
                );
              }
            },
          ),
          _toggle(
            label: 'Compact layout',
            description: 'Tighter padding on lists and screens to show more at once.',
            value: prefs.compactUi,
            onChanged: prefs.setCompactUi,
          ),
          _toggle(
            label: 'Reduce motion',
            description: 'Shorten animations and transitions for comfort or accessibility.',
            value: prefs.reduceMotion,
            onChanged: prefs.setReduceMotion,
          ),
          _toggle(
            label: 'Photos on appliance list',
            description: 'Show thumbnails on lists and cards when images are available.',
            value: prefs.showListThumbnails,
            onChanged: prefs.setShowListThumbnails,
          ),
          _sectionTitle('Notifications'),
          _toggle(
            label: 'Push-style alerts in Keeply',
            description: 'Bell counts and highlights reflect new items when this is on.',
            value: prefs.pushNotifications,
            onChanged: prefs.setPushNotifications,
          ),
          _toggle(
            label: 'Email reminders',
            description: 'Placeholder for warranty and service email — requires backend integration.',
            value: prefs.emailReminders,
            onChanged: prefs.setEmailReminders,
          ),
          _toggle(
            label: 'Asset & warranty alerts',
            description: 'Surface in-app messages about expiring coverage and scheduled care.',
            value: prefs.assetWarrantyAlerts,
            onChanged: prefs.setAssetWarrantyAlerts,
          ),
          _toggle(
            label: 'Helpdesk activity hints',
            description: 'Prefer showing tickets and questions you care about in summaries.',
            value: prefs.helpdeskActivityAlerts,
            onChanged: prefs.setHelpdeskActivityAlerts,
          ),
          _toggle(
            label: 'Weekly digest',
            description: 'Summary of assets and open issues (when messaging is connected).',
            value: prefs.weeklyDigest,
            onChanged: prefs.setWeeklyDigest,
          ),
          _footnote('Open notification center or alerts inbox to read messages.'),
          _sectionTitle('Voice & catalog'),
          ListTile(
            contentPadding: EdgeInsets.zero,
            leading: const Icon(Icons.mic_none_rounded),
            title: const Text('Voice assistant (roadmap)'),
            subtitle: const Text('Hands-free category, make, and model — privacy-first plan'),
            trailing: const Icon(Icons.chevron_right_rounded),
            onTap: () {
              Navigator.of(context).push<void>(
                MaterialPageRoute<void>(builder: (_) => const VoiceCatalogAssistPage()),
              );
            },
          ),
          _sectionTitle('Help & account shortcuts'),
          _linkTile(
            icon: Icons.person_outline,
            title: 'Profile',
            subtitle: 'Name, contact, and avatar',
            onTap: () => Navigator.of(context).push<void>(
              MaterialPageRoute<void>(builder: (_) => const AccountProfilePage()),
            ),
          ),
          _linkTile(
            icon: Icons.notifications_none_rounded,
            title: 'Notification center',
            subtitle: 'In-app messages',
            onTap: () => Navigator.of(context).push<void>(
              MaterialPageRoute<void>(builder: (_) => const AlertsHubPage(wrapWithScaffold: true)),
            ),
          ),
          _linkTile(
            icon: Icons.chat_bubble_outline,
            title: 'Keeply chat (Ollama)',
            subtitle: 'In-app assistant — same engine as on Home',
            onTap: () => Navigator.of(context).push<void>(
              MaterialPageRoute<void>(builder: (_) => const SupportChatPage()),
            ),
          ),
          _linkTile(
            icon: Icons.support_agent_outlined,
            title: 'Help & support',
            subtitle: 'Support hub',
            onTap: () => Navigator.of(context).push<void>(
              MaterialPageRoute<void>(builder: (_) => const HelpdeskHubPage()),
            ),
          ),
          _linkTile(
            icon: Icons.confirmation_number_outlined,
            title: 'My service tickets',
            subtitle: 'Issues you raised',
            onTap: () => Navigator.of(context).push<void>(
              MaterialPageRoute<void>(builder: (_) => const HelpdeskHubPage()),
            ),
          ),
          _linkTile(
            icon: Icons.chat_bubble_outline,
            title: 'My questions',
            subtitle: 'Helpdesk queries',
            onTap: () => Navigator.of(context).push<void>(
              MaterialPageRoute<void>(builder: (_) => const HelpdeskHubPage()),
            ),
          ),
          _linkTile(
            icon: Icons.lightbulb_outline,
            title: 'Tips & knowledge',
            subtitle: 'FAQs and articles',
            onTap: () => Navigator.of(context).push<void>(
              MaterialPageRoute<void>(builder: (_) => const TipsHubPage()),
            ),
          ),
          _linkTile(
            icon: Icons.notifications_active_outlined,
            title: 'Alerts inbox',
            subtitle: 'Warnings and reminders',
            onTap: () => Navigator.of(context).push<void>(
              MaterialPageRoute<void>(builder: (_) => const AlertsHubPage(wrapWithScaffold: true)),
            ),
          ),
          _linkTile(
            icon: Icons.dataset_linked_outlined,
            title: 'Master data',
            subtitle: 'Categories, subcategories, makes, models',
            onTap: () => Navigator.of(context).push<void>(
              MaterialPageRoute<void>(builder: (_) => const MasterDataCatalogPage()),
            ),
          ),
          _sectionTitle('About this app'),
          Text(
            'Keeply connects to your asset and helpdesk services. Version and legal copy can be added here for store releases.',
            style: t.bodySmall?.copyWith(color: KeeplyTokens.muted, height: 1.45),
          ),
          if (_versionLine != null) ...[
            const SizedBox(height: 8),
            Text(_versionLine!, style: t.bodySmall?.copyWith(color: KeeplyTokens.muted)),
          ],
          _sectionTitle('Security'),
          Text(
            'Revokes every active sign-in for your account (other phones and browsers too), then returns you to the welcome screen on this device.',
            style: t.bodySmall?.copyWith(color: KeeplyTokens.muted, height: 1.45),
          ),
          const SizedBox(height: 12),
          OutlinedButton(
            onPressed: context.watch<AuthBloc>().state is! AuthAuthenticated || _signOutAllBusy ? null : _signOutAllDevices,
            child: _signOutAllBusy
                ? const SizedBox(
                    height: 20,
                    width: 20,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  )
                : const Text('Sign out on all devices'),
          ),
        ],
          );
        },
      ),
    );
  }

  Widget _selectAppearance(KeeplyAppPreferences prefs) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text('Appearance', style: Theme.of(context).textTheme.titleSmall?.copyWith(fontWeight: FontWeight.w600)),
          const SizedBox(height: 4),
          Text(
            'Light, dark, or follow your device setting.',
            style: Theme.of(context).textTheme.bodySmall?.copyWith(color: KeeplyTokens.muted, height: 1.35),
          ),
          const SizedBox(height: 8),
          SegmentedButton<String>(
            segments: const [
              ButtonSegment(value: 'system', label: Text('System'), icon: Icon(Icons.brightness_auto, size: 18)),
              ButtonSegment(value: 'light', label: Text('Light'), icon: Icon(Icons.light_mode_outlined, size: 18)),
              ButtonSegment(value: 'dark', label: Text('Dark'), icon: Icon(Icons.dark_mode_outlined, size: 18)),
            ],
            selected: {prefs.appearance},
            onSelectionChanged: (s) => prefs.setAppearance(s.first),
          ),
        ],
      ),
    );
  }

  Widget _toggle({
    required String label,
    required String description,
    required bool value,
    required ValueChanged<bool> onChanged,
  }) {
    return SwitchListTile(
      contentPadding: EdgeInsets.zero,
      title: Text(label, style: const TextStyle(fontWeight: FontWeight.w600)),
      subtitle: Text(description, style: Theme.of(context).textTheme.bodySmall?.copyWith(color: KeeplyTokens.muted, height: 1.35)),
      value: value,
      onChanged: onChanged,
    );
  }

  Widget _linkTile({
    required IconData icon,
    required String title,
    required String subtitle,
    required VoidCallback onTap,
  }) {
    return ListTile(
      contentPadding: EdgeInsets.zero,
      leading: Icon(icon),
      title: Text(title),
      subtitle: Text(subtitle, style: Theme.of(context).textTheme.bodySmall?.copyWith(color: KeeplyTokens.muted)),
      trailing: const Icon(Icons.chevron_right_rounded),
      onTap: onTap,
    );
  }
}
