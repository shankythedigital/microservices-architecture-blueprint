import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:keeply_app/core/theme/keeply_tokens.dart';
import 'package:keeply_app/features/auth/presentation/bloc/auth_bloc.dart';
import 'package:keeply_app/features/auth/presentation/pages/welcome_page.dart';
import 'package:keeply_app/features/auth/presentation/pages/register_page.dart';
import 'package:keeply_app/features/helpdesk/presentation/pages/helpdesk_hub_page.dart';
import 'package:keeply_app/features/master_data/presentation/pages/master_data_catalog_page.dart';
import 'package:keeply_app/features/shell/presentation/pages/account_profile_page.dart';
import 'package:keeply_app/features/shell/presentation/pages/account_settings_page.dart';
import 'package:keeply_app/features/shell/presentation/pages/alerts_hub_page.dart';

/// React `/home/account` hub — profile, settings, notifications links + sign out.
class AccountHubPage extends StatelessWidget {
  const AccountHubPage({super.key});

  @override
  Widget build(BuildContext context) {
    final auth = context.watch<AuthBloc>().state;
    final user = auth is AuthAuthenticated ? auth.user : null;

    return Scaffold(
      appBar: AppBar(title: const Text('Account')),
      body: ListView(
        padding: const EdgeInsets.all(18),
        children: [
          if (user != null)
            ListTile(
              contentPadding: EdgeInsets.zero,
              title: Text(user.username ?? 'User', style: const TextStyle(fontWeight: FontWeight.w700)),
              subtitle: Text(
                [user.email, user.mobile].whereType<String>().where((e) => e.isNotEmpty).join(' · '),
                style: const TextStyle(color: KeeplyTokens.muted),
              ),
            ),
          const Divider(height: 32),
          ListTile(
            leading: const Icon(Icons.person_outline),
            title: const Text('Profile'),
            subtitle: const Text('View and edit your profile'),
            trailing: const Icon(Icons.chevron_right_rounded),
            onTap: () {
              Navigator.of(context).push<void>(
                MaterialPageRoute<void>(builder: (_) => const AccountProfilePage()),
              );
            },
          ),
          ListTile(
            leading: const Icon(Icons.settings_outlined),
            title: const Text('Settings'),
            trailing: const Icon(Icons.chevron_right_rounded),
            onTap: () {
              Navigator.of(context).push<void>(
                MaterialPageRoute<void>(builder: (_) => const AccountSettingsPage()),
              );
            },
          ),
          ListTile(
            leading: const Icon(Icons.notifications_none_rounded),
            title: const Text('Notifications'),
            trailing: const Icon(Icons.chevron_right_rounded),
            onTap: () {
              Navigator.of(context).push<void>(
                MaterialPageRoute<void>(builder: (_) => const AlertsHubPage()),
              );
            },
          ),
          ListTile(
            leading: const Icon(Icons.support_agent_outlined),
            title: const Text('Help & support'),
            trailing: const Icon(Icons.chevron_right_rounded),
            onTap: () {
              Navigator.of(context).push<void>(
                MaterialPageRoute(builder: (_) => const HelpdeskHubPage()),
              );
            },
          ),
          ListTile(
            leading: const Icon(Icons.dataset_linked_outlined),
            title: const Text('Master data'),
            subtitle: const Text('Categories, subcategories, makes, models'),
            trailing: const Icon(Icons.chevron_right_rounded),
            onTap: () {
              Navigator.of(context).push<void>(
                MaterialPageRoute<void>(builder: (_) => const MasterDataCatalogPage()),
              );
            },
          ),
          ListTile(
            leading: const Icon(Icons.person_add_outlined),
            title: const Text('Create another account'),
            onTap: () {
              Navigator.of(context).push<void>(
                MaterialPageRoute(builder: (_) => const RegisterPage()),
              );
            },
          ),
          const SizedBox(height: 24),
          OutlinedButton.icon(
            onPressed: () async {
              context.read<AuthBloc>().add(LogoutEvent());
              if (context.mounted) {
                Navigator.of(context).pushAndRemoveUntil(
                  MaterialPageRoute<void>(builder: (_) => const WelcomePage()),
                  (_) => false,
                );
              }
            },
            icon: const Icon(Icons.logout),
            label: const Text('Sign out'),
          ),
        ],
      ),
    );
  }
}
