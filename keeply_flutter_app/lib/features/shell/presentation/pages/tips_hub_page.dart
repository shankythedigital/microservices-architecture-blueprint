import 'package:flutter/material.dart';
import 'package:keeply_app/core/theme/keeply_tokens.dart';

/// React route `/home/tips` — tips and knowledge entry (no helpdesk import to avoid circular routes).
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
        const SizedBox(height: 16),
        Text(
          'Open Help & support from the home screen summary, the dashboard, or your account hub to search FAQs and contact the helpdesk.',
          style: t.bodySmall?.copyWith(color: KeeplyTokens.muted, height: 1.4),
        ),
      ],
    );
  }
}
