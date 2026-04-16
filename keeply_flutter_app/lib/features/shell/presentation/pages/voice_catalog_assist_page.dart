import 'package:flutter/material.dart';
import 'package:keeply_app/core/theme/keeply_tokens.dart';

/// Roadmap surface for voice-driven catalog selection (no false claims about on-device training yet).
class VoiceCatalogAssistPage extends StatelessWidget {
  const VoiceCatalogAssistPage({super.key});

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    return Scaffold(
      appBar: AppBar(title: const Text('Voice assistant')),
      body: ListView(
        padding: const EdgeInsets.all(20),
        children: [
          Icon(Icons.mic_none_rounded, size: 56, color: Theme.of(context).colorScheme.primary),
          const SizedBox(height: 16),
          Text('Speak to fill appliance details', style: t.titleLarge?.copyWith(fontWeight: FontWeight.w800)),
          const SizedBox(height: 12),
          Text(
            'We plan to combine on-device speech recognition with guided prompts so you can choose category, '
            'subcategory, make, and model hands-free. Training a private offline LLM on your catalog is a larger '
            'product step and will ship only with clear privacy controls.',
            style: t.bodyMedium?.copyWith(color: KeeplyTokens.muted, height: 1.5),
          ),
          const SizedBox(height: 24),
          Text('Today', style: t.titleSmall?.copyWith(fontWeight: FontWeight.w700)),
          const SizedBox(height: 8),
          Text(
            'Use manual entry or master-data pickers. When voice ships, this screen will host recording, '
            'review, and undo before anything is saved.',
            style: t.bodyMedium?.copyWith(height: 1.45),
          ),
        ],
      ),
    );
  }
}
