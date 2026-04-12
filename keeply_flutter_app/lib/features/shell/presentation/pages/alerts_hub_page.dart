import 'package:flutter/material.dart';
import 'package:keeply_app/core/theme/keeply_tokens.dart';
import 'package:keeply_app/features/notification/data/datasources/notification_inbox_remote_datasource.dart';

/// React `/home/alerts` + notification inbox summary.
class AlertsHubPage extends StatefulWidget {
  const AlertsHubPage({super.key});

  @override
  State<AlertsHubPage> createState() => _AlertsHubPageState();
}

class _AlertsHubPageState extends State<AlertsHubPage> {
  final _ds = NotificationInboxRemoteDataSource();
  int? _count;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    final n = await _ds.notificationCount();
    if (mounted) setState(() => _count = n);
  }

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    return RefreshIndicator(
      onRefresh: _load,
      child: ListView(
        padding: const EdgeInsets.fromLTRB(18, 18, 18, 100),
        physics: const AlwaysScrollableScrollPhysics(),
        children: [
          Text('Alerts', style: t.headlineSmall?.copyWith(fontWeight: FontWeight.w800)),
          const SizedBox(height: 8),
          Text(
            'In-app notification count from the notification service.',
            style: t.bodyMedium?.copyWith(color: KeeplyTokens.muted, height: 1.45),
          ),
          const SizedBox(height: 24),
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(20),
            decoration: BoxDecoration(
              color: KeeplyTokens.surface,
              borderRadius: BorderRadius.circular(KeeplyTokens.radiusSm),
              border: Border.all(color: KeeplyTokens.line),
            ),
            child: Column(
              children: [
                Text(
                  _count == null ? '—' : '$_count',
                  style: t.displaySmall?.copyWith(fontWeight: FontWeight.w800, color: KeeplyTokens.accentInk),
                ),
                const SizedBox(height: 4),
                Text('Unread-style inbox items', style: t.bodySmall?.copyWith(color: KeeplyTokens.muted)),
              ],
            ),
          ),
          const SizedBox(height: 16),
          Text(
            'Full notification list UI can mirror React `NotificationsPage` next.',
            style: t.bodySmall?.copyWith(color: KeeplyTokens.muted, fontStyle: FontStyle.italic),
          ),
        ],
      ),
    );
  }
}
