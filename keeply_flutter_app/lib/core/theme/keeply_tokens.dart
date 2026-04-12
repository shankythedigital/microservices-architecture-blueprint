import 'package:flutter/material.dart';

/// Design tokens aligned with `keeply_react_app/src/index.css` (:root).
abstract final class KeeplyTokens {
  static const Color ink = Color(0xFF141C26);
  static const Color muted = Color(0xFF5C6674);
  static const Color bg = Color(0xFFF3F6FA);
  static const Color surface = Color(0xFFFFFFFF);
  static const Color surfaceMuted = Color(0xFFF8FAFC);
  static const Color line = Color(0xFFE2E8F0);
  static const Color lineStrong = Color(0xFFCBD5E1);
  static const Color accent = Color(0xFF0D9488);
  static const Color accentInk = Color(0xFF0F7669);
  /// `rgba(13, 148, 136, 0.11)` from React `--accent-soft`
  static const Color accentSoft = Color(0x1C0D9488);
  static const Color danger = Color(0xFFB42318);
  static const Color navBg = Color(0xFF1A3D38);
  static const Color navBgTop = Color(0xFF1F4540);
  static const Color navInk = Color(0xE6FFFFFF);
  static const Color navActive = Color(0xFFD8FF65);
  static const Color fab = Color(0xFF143B36);
  static const Color fabRing = Color(0xFFF3F6FA);
  static const Color badgeBg = Color(0xFFD4FF4A);
  static const Color badgeInk = Color(0xFF143B36);

  static const double radius = 18;
  static const double radiusSm = 14;
  static const double radiusXs = 10;
  static const double maxAppWidth = 430;
}
