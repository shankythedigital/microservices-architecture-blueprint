import 'package:flutter/material.dart';
import 'package:keeply_app/core/theme/keeply_tokens.dart';
import 'package:keeply_app/features/helpdesk/presentation/pages/helpdesk_hub_page.dart';

/// React route `/home/tips` (`KnowledgePage` placeholder — same entry point pattern).
class TipsHubPage extends StatelessWidget {
  const TipsHubPage({super.key});

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    return ListView(
      padding: const EdgeInsets.fromLTRB(18, 18, 18, 100),
      children: [
        Text('Tips & knowledge', style: t.headlineSmall?.copyWith(fontWeight: FontWeight.w800)),
        const SizedBox(height: 8),
        Text(
          'Browse guidance for using asset, auth, notification, and helpdesk services.',
          style: t.bodyMedium?.copyWith(color: KeeplyTokens.muted, height: 1.45),
        ),
        const SizedBox(height: 20),
        FilledButton.tonal(
          onPressed: () {
            Navigator.of(context).push<void>(
              MaterialPageRoute(builder: (_) => const HelpdeskHubPage()),
            );
          },
          child: const Text('Open help & support'),
        ),
      ],
    );
  }
}
