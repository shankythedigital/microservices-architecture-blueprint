import 'package:flutter/material.dart';
import 'package:keeply_app/core/theme/keeply_tokens.dart';
import 'package:keeply_app/core/widgets/keeply_auth_screen_background.dart';
import 'package:keeply_app/features/auth/presentation/pages/login_page.dart';
import 'package:keeply_app/features/auth/presentation/pages/register_page.dart';

/// Matches React `WelcomePage` — marketing card + primary/secondary CTAs.
class WelcomePage extends StatelessWidget {
  const WelcomePage({super.key});

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    final scheme = Theme.of(context).colorScheme;

    return Scaffold(
      backgroundColor: scheme.surface,
      body: KeeplyAuthBackground(
        child: SafeArea(
          child: Center(
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: KeeplyTokens.maxAppWidth),
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 24),
                child: DecoratedBox(
                  decoration: BoxDecoration(
                    color: scheme.surface,
                    borderRadius: BorderRadius.circular(KeeplyTokens.radius),
                    boxShadow: const [
                      BoxShadow(
                        color: Color(0x140F172A),
                        blurRadius: 28,
                        offset: Offset(0, 12),
                      ),
                    ],
                    border: Border.all(color: scheme.outline.withValues(alpha: 0.22)),
                  ),
                  child: Padding(
                    padding: const EdgeInsets.fromLTRB(22, 28, 22, 24),
                    child: Column(
                      mainAxisSize: MainAxisSize.min,
                      crossAxisAlignment: CrossAxisAlignment.stretch,
                      children: [
                        Row(
                          children: [
                            Container(
                              width: 44,
                              height: 44,
                              alignment: Alignment.center,
                              decoration: BoxDecoration(
                                borderRadius: BorderRadius.circular(14),
                                gradient: const LinearGradient(
                                  colors: [Color(0xFF0F7669), KeeplyTokens.accent],
                                ),
                              ),
                              child: const Icon(Icons.inventory_2_rounded, color: Colors.white, size: 24),
                            ),
                            const SizedBox(width: 12),
                            Expanded(
                              child: Text(
                                'Keeply',
                                style: t.headlineSmall?.copyWith(
                                  fontWeight: FontWeight.w800,
                                  color: scheme.onSurface,
                                ),
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 18),
                        Text(
                          'Keep track of appliances, invoices, warranties, and service reminders—'
                          'so you never miss an expiry.',
                          style: t.bodyMedium?.copyWith(
                            color: scheme.onSurfaceVariant,
                            height: 1.45,
                          ),
                        ),
                        const SizedBox(height: 12),
                        Text(
                          'New here? Create a free account first. Already registered? Sign in to open '
                          'your home dashboard.',
                          style: t.bodySmall?.copyWith(
                            color: scheme.onSurfaceVariant,
                            height: 1.45,
                          ),
                        ),
                        const SizedBox(height: 16),
                        ...[
                          'Add appliances by scan or manual entry',
                          'Store invoice documents',
                          'Get alerts before warranty or service due',
                          'Raise service issues and track status',
                        ].map(
                          (s) => Padding(
                            padding: const EdgeInsets.only(bottom: 6),
                            child: Row(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text('• ', style: t.bodySmall?.copyWith(color: scheme.onSurfaceVariant)),
                                Expanded(
                                  child: Text(
                                    s,
                                    style: t.bodySmall?.copyWith(color: scheme.onSurfaceVariant, height: 1.35),
                                  ),
                                ),
                              ],
                            ),
                          ),
                        ),
                        const SizedBox(height: 18),
                        FilledButton(
                          onPressed: () {
                            Navigator.of(context).push(
                              MaterialPageRoute<void>(builder: (_) => const RegisterPage()),
                            );
                          },
                          child: const Text('Create an account'),
                        ),
                        const SizedBox(height: 10),
                        OutlinedButton(
                          onPressed: () {
                            Navigator.of(context).push(
                              MaterialPageRoute<void>(builder: (_) => const LoginPage()),
                            );
                          },
                          child: const Text('Sign in'),
                        ),
                        const SizedBox(height: 14),
                        Text(
                          'Registration verifies your mobile (OTP). Password login is available for '
                          'admin/dev accounts after you register.',
                          style: t.labelSmall?.copyWith(color: scheme.onSurfaceVariant, height: 1.4),
                          textAlign: TextAlign.center,
                        ),
                      ],
                    ),
                  ),
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
