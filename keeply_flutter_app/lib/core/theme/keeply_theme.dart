import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:keeply_app/core/theme/keeply_tokens.dart';

/// Material 3 theme mirroring Keeply React (`index.css` — Plus Jakarta Sans, teal accent).
abstract final class KeeplyTheme {
  static ThemeData light({VisualDensity? visualDensity}) {
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
      visualDensity: visualDensity ?? VisualDensity.standard,
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

  static ThemeData dark({VisualDensity? visualDensity}) {
    const base = ColorScheme.dark(
      primary: Color(0xFF2DD4BF),
      onPrimary: Color(0xFF042F2E),
      secondary: Color(0xFF5EEAD4),
      onSecondary: Color(0xFF042F2E),
      surface: Color(0xFF1E293B),
      onSurface: Color(0xFFF1F5F9),
      error: Color(0xFFF87171),
      onError: Color(0xFF450A0A),
      outline: Color(0xFF475569),
    );

    final textTheme = GoogleFonts.plusJakartaSansTextTheme(ThemeData(brightness: Brightness.dark).textTheme).apply(
      bodyColor: const Color(0xFFF1F5F9),
      displayColor: const Color(0xFFF1F5F9),
    );

    return ThemeData(
      useMaterial3: true,
      brightness: Brightness.dark,
      visualDensity: visualDensity ?? VisualDensity.standard,
      colorScheme: base,
      scaffoldBackgroundColor: const Color(0xFF0F172A),
      textTheme: textTheme,
      appBarTheme: AppBarTheme(
        elevation: 0,
        scrolledUnderElevation: 0,
        backgroundColor: const Color(0xFF1E293B).withValues(alpha: 0.92),
        foregroundColor: const Color(0xFFF1F5F9),
        titleTextStyle: textTheme.titleMedium?.copyWith(
          fontWeight: FontWeight.w600,
          color: const Color(0xFFF1F5F9),
        ),
      ),
      cardTheme: CardThemeData(
        elevation: 0,
        color: const Color(0xFF1E293B),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(KeeplyTokens.radiusSm),
          side: const BorderSide(color: Color(0xFF334155)),
        ),
        shadowColor: const Color(0x0F000000),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: const Color(0xFF0F172A),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(KeeplyTokens.radiusXs),
          borderSide: const BorderSide(color: Color(0xFF334155)),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(KeeplyTokens.radiusXs),
          borderSide: const BorderSide(color: Color(0xFF334155)),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(KeeplyTokens.radiusXs),
          borderSide: const BorderSide(color: Color(0xFF2DD4BF), width: 2),
        ),
        contentPadding: const EdgeInsets.symmetric(horizontal: 14, vertical: 14),
      ),
      filledButtonTheme: FilledButtonThemeData(
        style: FilledButton.styleFrom(
          backgroundColor: const Color(0xFF2DD4BF),
          foregroundColor: const Color(0xFF042F2E),
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
          foregroundColor: const Color(0xFF5EEAD4),
          side: const BorderSide(color: Color(0xFF475569)),
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 14),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(KeeplyTokens.radiusSm),
          ),
          textStyle: const TextStyle(fontWeight: FontWeight.w600, fontSize: 15),
        ),
      ),
      textButtonTheme: TextButtonThemeData(
        style: TextButton.styleFrom(foregroundColor: const Color(0xFF5EEAD4)),
      ),
      chipTheme: ChipThemeData(
        backgroundColor: const Color(0xFF334155),
        selectedColor: const Color(0xFF2DD4BF).withValues(alpha: 0.22),
        labelStyle: textTheme.labelLarge,
        side: const BorderSide(color: Color(0xFF475569)),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(999)),
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 0),
      ),
      dividerTheme: const DividerThemeData(color: Color(0xFF334155), thickness: 1),
    );
  }
}
