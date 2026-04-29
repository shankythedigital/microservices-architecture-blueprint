import 'package:flutter/material.dart';
import 'package:keeply_app/core/api/keeply_api_models.dart';
import 'package:keeply_app/core/theme/keeply_tokens.dart';
import 'package:keeply_app/features/notification/data/datasources/notification_inbox_remote_datasource.dart';

/// Notification inbox with **All / Unread / Read** tabs (React `NotificationsPage` parity).
class AlertsHubPage extends StatefulWidget {
  const AlertsHubPage({super.key, this.wrapWithScaffold = false});

  /// When `true`, wraps content in a [Scaffold] with back navigation (pushed routes).
  /// When `false`, only the tab body is returned (embedded in [KeeplyMobileShell]).
  final bool wrapWithScaffold;

  @override
  State<AlertsHubPage> createState() => _AlertsHubPageState();
}

class _AlertsHubPageState extends State<AlertsHubPage> {
  final _ds = NotificationInboxRemoteDataSource();
  List<NotificationItemDto> _items = [];
  bool _loading = true;
  String? _err;
  bool _markAllBusy = false;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _err = null;
    });
    try {
      final list = await _ds.client.notificationList(days: 120);
      if (!mounted) return;
      setState(() {
        _items = list;
        _loading = false;
      });
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _loading = false;
        _err = 'Could not load notifications';
      });
    }
  }

  Future<void> _onTapItem(NotificationItemDto n) async {
    final unread = n.read != true;
    if (!unread) return;
    try {
      await _ds.client.markNotificationRead(n.id);
      if (!mounted) return;
      setState(() {
        _items = [
          for (final x in _items)
            if (x.id == n.id) NotificationItemDto(
                  id: x.id,
                  title: x.title,
                  message: x.message,
                  templateCode: x.templateCode,
                  createdAt: x.createdAt,
                  read: true,
                  priority: x.priority,
                ) else
              x,
        ];
      });
    } catch (_) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Could not mark as read')),
      );
    }
  }

  Future<void> _markAllRead() async {
    setState(() => _markAllBusy = true);
    try {
      await _ds.client.markAllNotificationsRead();
      if (!mounted) return;
      await _load();
    } catch (_) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Could not mark all as read')),
      );
    } finally {
      if (mounted) setState(() => _markAllBusy = false);
    }
  }

  List<NotificationItemDto> _filtered(bool? readFilter) {
    return _items.where((e) {
      if (readFilter == null) return true;
      final r = e.read == true;
      return readFilter ? r : !r;
    }).toList()
      ..sort((a, b) => (b.createdAt ?? '').compareTo(a.createdAt ?? ''));
  }

  Widget _listForFilter(bool? readFilter) {
    final t = Theme.of(context).textTheme;
    final scheme = Theme.of(context).colorScheme;
    if (_loading) {
      return const Center(child: Padding(padding: EdgeInsets.all(32), child: CircularProgressIndicator(strokeWidth: 2)));
    }
    if (_err != null) {
      return ListView(
        physics: const AlwaysScrollableScrollPhysics(),
        padding: const EdgeInsets.all(24),
        children: [
          Text(_err!, style: t.bodyMedium?.copyWith(color: scheme.error)),
          const SizedBox(height: 16),
          FilledButton(onPressed: _load, child: const Text('Retry')),
        ],
      );
    }
    final rows = _filtered(readFilter);
    if (rows.isEmpty) {
      return ListView(
        physics: const AlwaysScrollableScrollPhysics(),
        padding: const EdgeInsets.all(24),
        children: [
          Text(
            readFilter == null
                ? 'No notifications yet.'
                : readFilter == false
                    ? 'You are all caught up.'
                    : 'No read messages.',
            style: t.bodyLarge?.copyWith(color: scheme.onSurfaceVariant),
          ),
        ],
      );
    }
    return ListView.separated(
      padding: const EdgeInsets.fromLTRB(16, 12, 16, 96),
      physics: const AlwaysScrollableScrollPhysics(),
      itemCount: rows.length,
      separatorBuilder: (_, __) => const SizedBox(height: 8),
      itemBuilder: (context, i) {
        final n = rows[i];
        final unread = n.read != true;
        return Material(
          color: scheme.surface,
          borderRadius: BorderRadius.circular(KeeplyTokens.radiusSm),
          child: InkWell(
            borderRadius: BorderRadius.circular(KeeplyTokens.radiusSm),
            onTap: () => _onTapItem(n),
            child: Container(
              width: double.infinity,
              padding: const EdgeInsets.all(14),
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(KeeplyTokens.radiusSm),
                border: Border.all(color: scheme.outline.withValues(alpha: unread ? 0.45 : 0.22)),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      if (unread)
                        Container(
                          width: 8,
                          height: 8,
                          margin: const EdgeInsets.only(right: 8),
                          decoration: const BoxDecoration(color: KeeplyTokens.accent, shape: BoxShape.circle),
                        ),
                      Expanded(
                        child: Text(
                          n.title?.trim().isNotEmpty == true ? n.title!.trim() : 'Notification',
                          style: t.titleSmall?.copyWith(fontWeight: FontWeight.w700),
                        ),
                      ),
                      if (n.createdAt != null && n.createdAt!.isNotEmpty)
                        Text(
                          n.createdAt!.replaceFirst('T', ' ').split('.').first,
                          style: t.labelSmall?.copyWith(color: scheme.onSurfaceVariant),
                        ),
                    ],
                  ),
                  if (n.message != null && n.message!.trim().isNotEmpty) ...[
                    const SizedBox(height: 8),
                    Text(n.message!, style: t.bodyMedium?.copyWith(height: 1.4)),
                  ],
                  if (unread)
                    Padding(
                      padding: const EdgeInsets.only(top: 8),
                      child: Text('Tap to mark as read', style: t.labelSmall?.copyWith(color: scheme.primary)),
                    ),
                ],
              ),
            ),
          ),
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    final body = DefaultTabController(
      length: 3,
      child: Column(
        children: [
          Material(
            color: Theme.of(context).colorScheme.surfaceContainerHighest.withValues(alpha: 0.35),
            child: Row(
              children: [
                const Expanded(
                  child: TabBar(
                    labelStyle: TextStyle(fontWeight: FontWeight.w700, fontSize: 14),
                    tabs: [
                      Tab(text: 'All'),
                      Tab(text: 'Unread'),
                      Tab(text: 'Read'),
                    ],
                  ),
                ),
                IconButton(
                  tooltip: 'Mark all read',
                  onPressed: _markAllBusy ? null : _markAllRead,
                  icon: _markAllBusy
                      ? const SizedBox(
                          width: 18,
                          height: 18,
                          child: CircularProgressIndicator(strokeWidth: 2),
                        )
                      : const Icon(Icons.done_all_rounded),
                ),
              ],
            ),
          ),
          Expanded(
            child: TabBarView(
              children: [
                RefreshIndicator(onRefresh: _load, child: _listForFilter(null)),
                RefreshIndicator(onRefresh: _load, child: _listForFilter(false)),
                RefreshIndicator(onRefresh: _load, child: _listForFilter(true)),
              ],
            ),
          ),
        ],
      ),
    );

    if (!widget.wrapWithScaffold) return body;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Notifications'),
        actions: [
          TextButton(
            onPressed: _markAllBusy ? null : _markAllRead,
            child: _markAllBusy
                ? const SizedBox(
                    width: 18,
                    height: 18,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  )
                : const Text('Mark all read'),
          ),
        ],
      ),
      body: body,
    );
  }
}
