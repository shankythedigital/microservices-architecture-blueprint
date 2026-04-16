import 'package:flutter/material.dart';
import 'package:keeply_app/core/theme/keeply_tokens.dart';
import 'package:keeply_app/features/master_data/presentation/pages/master_data_catalog_page.dart';
import 'package:keeply_app/features/shell/presentation/pages/alerts_hub_page.dart';
import 'package:package_info_plus/package_info_plus.dart';

/// Account hub “Settings” — links to in-app surfaces backed by services.
class AccountSettingsPage extends StatefulWidget {
  const AccountSettingsPage({super.key});

  @override
  State<AccountSettingsPage> createState() => _AccountSettingsPageState();
}

class _AccountSettingsPageState extends State<AccountSettingsPage> {
  String? _versionLine;

  @override
  void initState() {
    super.initState();
    PackageInfo.fromPlatform().then((p) {
      if (!mounted) return;
      setState(() => _versionLine = '${p.appName} ${p.version} (${p.buildNumber})');
    });
  }

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    return Scaffold(
      appBar: AppBar(title: const Text('Settings')),
      body: ListView(
        padding: const EdgeInsets.all(18),
        children: [
          ListTile(
            contentPadding: EdgeInsets.zero,
            leading: const Icon(Icons.notifications_active_outlined),
            title: const Text('Alerts & inbox'),
            subtitle: const Text('Notification service summary'),
            trailing: const Icon(Icons.chevron_right_rounded),
            onTap: () {
              Navigator.of(context).push<void>(
                MaterialPageRoute<void>(builder: (_) => const AlertsHubPage()),
              );
            },
          ),
          ListTile(
            contentPadding: EdgeInsets.zero,
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
          const Divider(height: 32),
          if (_versionLine != null)
            Text(_versionLine!, style: t.bodySmall?.copyWith(color: KeeplyTokens.muted)),
        ],
      ),
    );
  }
}
