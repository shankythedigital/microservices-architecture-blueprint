import 'package:flutter/material.dart';
import 'package:keeply_app/core/theme/keeply_tokens.dart';

/// Entry for helpdesk flows (React `HelpdeskPage` / queries / issues).
class HelpdeskHubPage extends StatelessWidget {
  const HelpdeskHubPage({super.key});

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    return Scaffold(
      appBar: AppBar(title: const Text('Help & support')),
      body: ListView(
        padding: const EdgeInsets.all(18),
        children: [
          Text(
            'Get help with Keeply services, open a query, or review knowledge articles.',
            style: t.bodyMedium?.copyWith(color: KeeplyTokens.muted, height: 1.45),
          ),
          const SizedBox(height: 20),
          _tile(context, title: 'My queries', subtitle: 'List and create helpdesk queries', onTap: () {}),
          _tile(context, title: 'Service issues', subtitle: 'Track issues you have raised', onTap: () {}),
          _tile(context, title: 'Tips & knowledge', subtitle: 'Browse service knowledge base', onTap: () {}),
        ],
      ),
    );
  }

  Widget _tile(
    BuildContext context, {
    required String title,
    required String subtitle,
    required VoidCallback onTap,
  }) {
    return Card(
      margin: const EdgeInsets.only(bottom: 10),
      child: ListTile(
        title: Text(title, style: const TextStyle(fontWeight: FontWeight.w600)),
        subtitle: Text(
          subtitle,
          style: const TextStyle(color: KeeplyTokens.muted, fontSize: 13),
        ),
        trailing: const Icon(Icons.chevron_right_rounded),
        onTap: onTap,
      ),
    );
  }
}
