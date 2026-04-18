import 'package:flutter/material.dart';
import 'package:keeply_app/core/theme/keeply_tokens.dart';

/// Same gradient family as [KeeplyMobileShell] body so pre-login and auth screens
/// feel continuous with the signed-in app (light + dark).
LinearGradient keeplyAuthBackgroundGradient(BuildContext context) {
  final isDark = Theme.of(context).brightness == Brightness.dark;
  if (isDark) {
    return const LinearGradient(
      begin: Alignment.topCenter,
      end: Alignment.bottomCenter,
      colors: [
        Color(0xFF1E293B),
        Color(0xFF0F172A),
        Color(0xFF0B1220),
      ],
      stops: [0.0, 0.48, 1.0],
    );
  }
  return const LinearGradient(
    begin: Alignment.topCenter,
    end: Alignment.bottomCenter,
    colors: [
      Color(0xEBF8FAFC),
      Color(0xFFF3F6FA),
      Color(0xFFF1F5F9),
    ],
    stops: [0.0, 0.48, 1.0],
  );
}

/// Full-width gradient layer; wrap with [SafeArea] / [Scaffold] as needed.
class KeeplyAuthBackground extends StatelessWidget {
  const KeeplyAuthBackground({super.key, required this.child});

  final Widget child;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      decoration: BoxDecoration(gradient: keeplyAuthBackgroundGradient(context)),
      child: child,
    );
  }
}

/// Brand row: accent dot + “Keeply” (aligned with shell header).
class KeeplyAuthWordmark extends StatelessWidget {
  const KeeplyAuthWordmark({super.key, this.size = 'large'});

  /// `large` for splash; `medium` for cards.
  final String size;

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    final isLarge = size == 'large';
    final dot = isLarge ? 12.0 : 10.0;
    final titleStyle = isLarge
        ? t.headlineMedium?.copyWith(fontWeight: FontWeight.w800, letterSpacing: -0.02)
        : t.titleLarge?.copyWith(fontWeight: FontWeight.w800, letterSpacing: -0.02);

    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Container(
          width: dot,
          height: dot,
          decoration: const BoxDecoration(
            color: KeeplyTokens.accent,
            shape: BoxShape.circle,
          ),
        ),
        SizedBox(width: isLarge ? 10 : 8),
        Text('Keeply', style: titleStyle?.copyWith(color: Theme.of(context).colorScheme.onSurface)),
      ],
    );
  }
}
