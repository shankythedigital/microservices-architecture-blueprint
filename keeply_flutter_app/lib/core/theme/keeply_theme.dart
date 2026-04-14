import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:keeply_app/core/theme/keeply_tokens.dart';

/// Material 3 theme mirroring Keeply React (`index.css` — Plus Jakarta Sans, teal accent).
abstract final class KeeplyTheme {
  static ThemeData light() {
    const base = ColorScheme.light(
      primary: KeeplyTokens.accent,
      onPrimary: Colors.white,
      secondary: KeeplyTokens.accentInk,
      onSecondary: Colors.white,
      surface: KeeplyTokens.surface,
      onSurface: KeeplyTokens.ink,
      error: KeeplyTokens.danger,
      onError: Colors.white,
      outline: KeeplyTokens.line,
    );

    final textTheme = GoogleFonts.plusJakartaSansTextTheme().apply(
      bodyColor: KeeplyTokens.ink,
      displayColor: KeeplyTokens.ink,
    );

    return ThemeData(
      useMaterial3: true,
      colorScheme: base,
      scaffoldBackgroundColor: KeeplyTokens.bg,
      textTheme: textTheme,
      appBarTheme: AppBarTheme(
        elevation: 0,
        scrolledUnderElevation: 0,
        backgroundColor: KeeplyTokens.surface.withValues(alpha: 0.86),
        foregroundColor: KeeplyTokens.ink,
        titleTextStyle: textTheme.titleMedium?.copyWith(
          fontWeight: FontWeight.w600,
          color: KeeplyTokens.ink,
        ),
      ),
      cardTheme: CardThemeData(
        elevation: 0,
        color: KeeplyTokens.surface,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(KeeplyTokens.radiusSm),
          side: const BorderSide(color: Color(0xFFE6EAF0)),
        ),
        shadowColor: const Color(0x0F0F172A),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: KeeplyTokens.surface,
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(KeeplyTokens.radiusXs),
          borderSide: const BorderSide(color: KeeplyTokens.line),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(KeeplyTokens.radiusXs),
          borderSide: const BorderSide(color: KeeplyTokens.line),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(KeeplyTokens.radiusXs),
          borderSide: const BorderSide(color: KeeplyTokens.accent, width: 2),
        ),
        contentPadding: const EdgeInsets.symmetric(horizontal: 14, vertical: 14),
      ),
      filledButtonTheme: FilledButtonThemeData(
        style: FilledButton.styleFrom(
          backgroundColor: KeeplyTokens.accent,
          foregroundColor: Colors.white,
          elevation: 0,
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 14),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(KeeplyTokens.radiusSm),
          ),
          textStyle: const TextStyle(fontWeight: FontWeight.w600, fontSize: 15),
        ),
      ),
      outlinedButtonTheme: OutlinedButtonThemeData(
        style: OutlinedButton.styleFrom(
          foregroundColor: KeeplyTokens.accentInk,
          side: const BorderSide(color: KeeplyTokens.lineStrong),
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 14),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(KeeplyTokens.radiusSm),
          ),
          textStyle: const TextStyle(fontWeight: FontWeight.w600, fontSize: 15),
        ),
      ),
      textButtonTheme: TextButtonThemeData(
        style: TextButton.styleFrom(foregroundColor: KeeplyTokens.accent),
      ),
      chipTheme: ChipThemeData(
        backgroundColor: KeeplyTokens.surfaceMuted,
        selectedColor: KeeplyTokens.accent.withValues(alpha: 0.12),
        labelStyle: textTheme.labelLarge,
        side: const BorderSide(color: KeeplyTokens.line),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(999)),
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 0),
      ),
      dividerTheme: const DividerThemeData(color: KeeplyTokens.line, thickness: 1),
    );
  }
}
