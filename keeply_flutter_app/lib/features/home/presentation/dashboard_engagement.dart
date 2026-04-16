import 'package:flutter/material.dart';
import 'package:keeply_app/core/ai/ollama_chat_client.dart';
import 'package:keeply_app/core/config/app_config.dart';
import 'package:keeply_app/core/preferences/keeply_app_preferences.dart';
import 'package:keeply_app/core/support/support_whatsapp.dart';
import 'package:keeply_app/core/theme/keeply_tokens.dart';
import 'package:keeply_app/features/shell/presentation/pages/support_chat_page.dart';
import 'package:speech_to_text/speech_to_text.dart';

/// Streak, level, and wallet — local gamification on the dashboard home tab.
class DashboardGamificationPanel extends StatelessWidget {
  const DashboardGamificationPanel({super.key});

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return ListenableBuilder(
      listenable: KeeplyAppPrefsScope.of(context),
      builder: (context, _) {
        final p = KeeplyAppPrefsScope.of(context);
        final tier = p.rewardLevel;
        final progress = (p.loyaltyPoints % 100) / 100.0;
        return Card(
          color: scheme.primaryContainer.withValues(alpha: Theme.of(context).brightness == Brightness.dark ? 0.22 : 0.35),
          child: Padding(
            padding: const EdgeInsets.all(14),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Icon(Icons.emoji_events_outlined, color: scheme.primary, size: 30),
                    const SizedBox(width: 10),
                    Expanded(
                      child: Text(
                        'Rewards & streak',
                        style: Theme.of(context).textTheme.titleSmall?.copyWith(fontWeight: FontWeight.w800),
                      ),
                    ),
                    Chip(
                      label: Text('Lv $tier'),
                      visualDensity: VisualDensity.compact,
                      padding: EdgeInsets.zero,
                    ),
                  ],
                ),
                const SizedBox(height: 8),
                Text(
                  '${p.loyaltyPoints} points · ${p.visitStreakDays} day visit streak',
                  style: Theme.of(context).textTheme.bodySmall?.copyWith(height: 1.35),
                ),
                const SizedBox(height: 10),
                ClipRRect(
                  borderRadius: BorderRadius.circular(999),
                  child: LinearProgressIndicator(value: progress.clamp(0.02, 1.0), minHeight: 8),
                ),
                const SizedBox(height: 6),
                Text(
                  'Next level in ${100 - (p.loyaltyPoints % 100)} pts · add appliances & check in daily',
                  style: Theme.of(context).textTheme.labelSmall?.copyWith(color: scheme.onSurfaceVariant),
                ),
              ],
            ),
          ),
        );
      },
    );
  }
}

/// Ollama + microphone: guided guesses for category stack (inference, not weight training).
class DashboardOllamaVoiceCard extends StatefulWidget {
  const DashboardOllamaVoiceCard({super.key});

  @override
  State<DashboardOllamaVoiceCard> createState() => _DashboardOllamaVoiceCardState();
}

class _DashboardOllamaVoiceCardState extends State<DashboardOllamaVoiceCard> {
  final _text = TextEditingController();
  final _speech = SpeechToText();
  bool _speechReady = false;
  bool _listening = false;
  bool _busy = false;
  String? _reply;

  @override
  void initState() {
    super.initState();
    _speech.initialize(
      onStatus: (s) {
        if (s == 'done' || s == 'notListening') {
          if (mounted) setState(() => _listening = false);
        }
      },
      onError: (_) {
        if (mounted) setState(() => _listening = false);
      },
    ).then((ok) {
      if (mounted) setState(() => _speechReady = ok);
    });
  }

  @override
  void dispose() {
    _text.dispose();
    super.dispose();
  }

  Future<void> _toggleListen() async {
    if (!_speechReady) return;
    if (_listening) {
      await _speech.stop();
      setState(() => _listening = false);
      return;
    }
    setState(() {
      _listening = true;
      _reply = null;
    });
    await _speech.listen(
      onResult: (r) {
        if (!mounted) return;
        setState(() => _text.text = r.recognizedWords);
      },
      pauseFor: const Duration(seconds: 2),
      listenOptions: SpeechListenOptions(
        listenMode: ListenMode.dictation,
        partialResults: true,
      ),
    );
  }

  Future<void> _askOllama() async {
    final q = _text.text.trim();
    if (q.isEmpty || _busy) return;
    setState(() {
      _busy = true;
      _reply = null;
    });
    try {
      final client = OllamaChatClient();
      final system = '''
You help users catalogue appliances. The user may speak loosely. Infer likely category, subcategory, make, and model labels.
Respond with short sections: **Category guess**, **Subcategory guess**, **Make guess**, **Model guess**, then **Why** (2 sentences max).
Model in use: ${AppConfig.ollamaModel}. Endpoint is the user's own Ollama (inference only — not training new weights).
''';
      final out = await client.chat(
        messages: [
          {'role': 'system', 'content': system},
          {'role': 'user', 'content': q},
        ],
      );
      if (!mounted) return;
      setState(() => _reply = out);
      try {
        await KeeplyAppPrefsScope.of(context).addLoyaltyPoints(2);
      } catch (_) {}
    } catch (e) {
      if (!mounted) return;
      setState(() => _reply = 'Could not reach Ollama: $e\nCheck ${AppConfig.ollamaBaseUrl} and model ${AppConfig.ollamaModel}.');
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    final t = Theme.of(context).textTheme;
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(Icons.psychology_outlined, color: scheme.primary),
                const SizedBox(width: 8),
                Expanded(
                  child: Text(
                    'Voice & Ollama assistant',
                    style: t.titleSmall?.copyWith(fontWeight: FontWeight.w800),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 6),
            Text(
              'Speak or type what you are installing. Ollama (${AppConfig.ollamaModel}) suggests labels using inference on your network — connect Ollama on your PC or server.',
              style: t.bodySmall?.copyWith(color: scheme.onSurfaceVariant, height: 1.4),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: _text,
              minLines: 2,
              maxLines: 4,
              decoration: InputDecoration(
                hintText: 'e.g. “Samsung French door fridge in the kitchen”',
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(KeeplyTokens.radiusXs)),
              ),
            ),
            const SizedBox(height: 10),
            Row(
              children: [
                IconButton.filledTonal(
                  tooltip: _listening ? 'Stop' : 'Speak',
                  onPressed: _speechReady ? _toggleListen : null,
                  icon: Icon(_listening ? Icons.stop_rounded : Icons.mic_none_rounded),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: FilledButton.icon(
                    onPressed: _busy ? null : _askOllama,
                    icon: _busy
                        ? const SizedBox(width: 18, height: 18, child: CircularProgressIndicator(strokeWidth: 2))
                        : const Icon(Icons.auto_awesome_rounded, size: 20),
                    label: Text(_busy ? 'Thinking…' : 'Ask Ollama'),
                  ),
                ),
              ],
            ),
            if (_reply != null) ...[
              const SizedBox(height: 12),
              Text(_reply!, style: t.bodySmall?.copyWith(height: 1.45)),
            ],
          ],
        ),
      ),
    );
  }
}

/// WhatsApp + full chatbot entry.
class DashboardSupportStrip extends StatelessWidget {
  const DashboardSupportStrip({super.key});

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Expanded(
          child: OutlinedButton.icon(
            onPressed: () {
              Navigator.of(context).push<void>(
                MaterialPageRoute<void>(builder: (_) => const SupportChatPage()),
              );
            },
            icon: const Icon(Icons.chat_bubble_outline),
            label: const Text('Chatbot'),
          ),
        ),
        const SizedBox(width: 10),
        Expanded(
          child: FilledButton.icon(
            onPressed: () async {
              final ok = await openWhatsAppSupport();
              if (!context.mounted) return;
              if (!ok) {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text('Configure WHATSAPP_SUPPORT_E164 (digits only) to enable WhatsApp.'),
                  ),
                );
              }
            },
            icon: const Icon(Icons.chat_outlined),
            label: const Text('WhatsApp'),
          ),
        ),
      ],
    );
  }
}
