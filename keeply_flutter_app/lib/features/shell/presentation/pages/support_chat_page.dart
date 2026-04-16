import 'package:flutter/material.dart';
import 'package:keeply_app/core/ai/keeply_ai_knowledge_base.dart';
import 'package:keeply_app/core/ai/ollama_chat_client.dart';
import 'package:keeply_app/core/config/app_config.dart';
import 'package:keeply_app/core/support/support_whatsapp.dart';

/// In-app chat using your local / LAN **Ollama** endpoint (same as the voice assistant card).
class SupportChatPage extends StatefulWidget {
  const SupportChatPage({super.key});

  @override
  State<SupportChatPage> createState() => _SupportChatPageState();
}

class _Msg {
  _Msg({required this.role, required this.text});
  final String role;
  final String text;
}

class _SupportChatPageState extends State<SupportChatPage> {
  final _client = OllamaChatClient();
  final _kb = KeeplyAiKnowledgeBase.instance;
  final _ctrl = TextEditingController();
  final _scroll = ScrollController();
  final _msgs = <_Msg>[
    _Msg(
      role: 'assistant',
      text:
          'Hi — I am Keeply’s assistant powered by Ollama at ${AppConfig.ollamaBaseUrl} using model “${AppConfig.ollamaModel}”. '
          'Ask about warranties, adding assets, or anything in the app. I am not a human agent; for billing or account locks, use WhatsApp if your team configured it.',
    ),
  ];
  bool _busy = false;

  @override
  void dispose() {
    _ctrl.dispose();
    _scroll.dispose();
    super.dispose();
  }

  Future<void> _send() async {
    final q = _ctrl.text.trim();
    if (q.isEmpty || _busy) return;
    setState(() {
      _msgs.add(_Msg(role: 'user', text: q));
      _ctrl.clear();
      _busy = true;
    });
    _scrollBottom();
    try {
      final cached = await _kb.findBestAnswer(q, relatedService: 'HELPDESK_SERVICE');
      if (cached != null && cached.trim().isNotEmpty) {
        if (!mounted) return;
        setState(
          () => _msgs.add(
            _Msg(
              role: 'assistant',
              text:
                  '$cached\n\n(Instant answer from your local Keeply knowledge base. Ask again with more details if needed.)',
            ),
          ),
        );
        await _kb.recordEntry(
          type: 'query',
          question: q,
          answer: cached,
          relatedService: 'HELPDESK_SERVICE',
          incrementFrequencyBy: 1,
        );
        return;
      }

      final history = <Map<String, String>>[
        {
          'role': 'system',
          'content':
              'You are Keeply mobile app support. Be concise, friendly, and accurate. If the question needs human support, say so and suggest WhatsApp when available.',
        },
        for (final m in _msgs) {'role': m.role, 'content': m.text},
      ];
      final reply = await _client.chat(messages: history);
      if (!mounted) return;
      setState(() => _msgs.add(_Msg(role: 'assistant', text: reply)));
      await _kb.recordEntry(
        type: 'query',
        question: q,
        answer: reply,
        relatedService: 'HELPDESK_SERVICE',
      );
    } catch (e) {
      if (!mounted) return;
      setState(
        () => _msgs.add(
          _Msg(
            role: 'assistant',
            text:
                'Could not reach Ollama ($e). Check that Ollama is running and that OLLAMA_BASE_URL / OLLAMA_MODEL dart-defines match your setup.',
          ),
        ),
      );
    } finally {
      if (mounted) setState(() => _busy = false);
      _scrollBottom();
    }
  }

  void _scrollBottom() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!_scroll.hasClients) return;
      _scroll.animateTo(
        _scroll.position.maxScrollExtent,
        duration: const Duration(milliseconds: 280),
        curve: Curves.easeOut,
      );
    });
  }

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Scaffold(
      appBar: AppBar(
        title: const Text('Keeply chat'),
        actions: [
          TextButton(
            onPressed: () async {
              final ok = await openWhatsAppSupport();
              if (!context.mounted) return;
              if (!ok) {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text('Set WHATSAPP_SUPPORT_E164 at build time (digits only, no +).'),
                  ),
                );
              }
            },
            child: const Text('WhatsApp'),
          ),
        ],
      ),
      body: Column(
        children: [
          Expanded(
            child: ListView.builder(
              controller: _scroll,
              padding: const EdgeInsets.fromLTRB(16, 12, 16, 16),
              itemCount: _msgs.length,
              itemBuilder: (_, i) {
                final m = _msgs[i];
                final user = m.role == 'user';
                return Align(
                  alignment: user ? Alignment.centerRight : Alignment.centerLeft,
                  child: Container(
                    margin: const EdgeInsets.only(bottom: 10),
                    padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
                    constraints: const BoxConstraints(maxWidth: 320),
                    decoration: BoxDecoration(
                      color: user ? scheme.primaryContainer : scheme.surfaceContainerHighest,
                      borderRadius: BorderRadius.circular(14),
                    ),
                    child: Text(m.text, style: TextStyle(color: user ? scheme.onPrimaryContainer : scheme.onSurface)),
                  ),
                );
              },
            ),
          ),
          if (_busy) const LinearProgressIndicator(minHeight: 2),
          SafeArea(
            child: Padding(
              padding: const EdgeInsets.fromLTRB(12, 0, 12, 12),
              child: Row(
                children: [
                  Expanded(
                    child: TextField(
                      controller: _ctrl,
                      minLines: 1,
                      maxLines: 4,
                      textInputAction: TextInputAction.send,
                      onSubmitted: (_) => _send(),
                      decoration: const InputDecoration(
                        hintText: 'Message…',
                        border: OutlineInputBorder(),
                        isDense: true,
                      ),
                    ),
                  ),
                  const SizedBox(width: 8),
                  IconButton.filled(
                    onPressed: _busy ? null : _send,
                    icon: const Icon(Icons.send_rounded),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}
