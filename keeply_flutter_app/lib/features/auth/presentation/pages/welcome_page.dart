import 'package:flutter/material.dart';
import 'package:keeply_app/core/theme/keeply_tokens.dart';
import 'package:keeply_app/features/auth/presentation/pages/login_page.dart';
import 'package:keeply_app/features/auth/presentation/pages/register_page.dart';

/// Matches React `WelcomePage` — marketing card + primary/secondary CTAs.
class WelcomePage extends StatelessWidget {
  const WelcomePage({super.key});

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;

    return Scaffold(
      body: Container(
        width: double.infinity,
        decoration: const BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [
              Color(0x1F0D9488),
              Color(0x0D6366F1),
              Color(0xD9E2E8F0),
              KeeplyTokens.bg,
            ],
            stops: [0.0, 0.2, 0.45, 0.65],
          ),
        ),
        child: SafeArea(
          child: Center(
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: KeeplyTokens.maxAppWidth),
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 24),
                child: DecoratedBox(
                  decoration: BoxDecoration(
                    color: KeeplyTokens.surface,
                    borderRadius: BorderRadius.circular(KeeplyTokens.radius),
                    boxShadow: const [
                      BoxShadow(
                        color: Color(0x140F172A),
                        blurRadius: 28,
                        offset: Offset(0, 12),
                      ),
                    ],
                    border: Border.all(color: const Color(0xFFE6EAF0)),
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
                              decoration: BoxDecoration(
                                borderRadius: BorderRadius.circular(14),
                                gradient: const LinearGradient(
                                  colors: [Color(0xFF0F7669), KeeplyTokens.accent],
                                ),
                              ),
                            ),
                            const SizedBox(width: 12),
                            Text('Keeply', style: t.headlineSmall?.copyWith(fontWeight: FontWeight.w800)),
                          ],
                        ),
                        const SizedBox(height: 18),
                        Text(
                          'Keep track of appliances, invoices, warranties, and service reminders—'
                          'so you never miss an expiry.',
                          style: t.bodyMedium?.copyWith(color: KeeplyTokens.muted, height: 1.45),
                        ),
                        const SizedBox(height: 12),
                        Text(
                          'New here? Create a free account first. Already registered? Sign in to open '
                          'your home dashboard.',
                          style: t.bodySmall?.copyWith(color: KeeplyTokens.muted, height: 1.45),
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
                                Text('• ', style: t.bodySmall?.copyWith(color: KeeplyTokens.muted)),
                                Expanded(
                                  child: Text(
                                    s,
                                    style: t.bodySmall?.copyWith(color: KeeplyTokens.muted, height: 1.35),
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
                          style: t.labelSmall?.copyWith(color: KeeplyTokens.muted, height: 1.4),
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
