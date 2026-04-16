import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

String _ymd(DateTime d) =>
    '${d.year}-${d.month.toString().padLeft(2, '0')}-${d.day.toString().padLeft(2, '0')}';

/// Device-local preferences (parity with React `useKeeplyPreferences` + wallet gamification).
class KeeplyAppPreferences extends ChangeNotifier {
  static const _kAppearance = 'keeply_pref_appearance';
  static const _kCompact = 'keeply_pref_compact_ui';
  static const _kReduceMotion = 'keeply_pref_reduce_motion';
  static const _kThumbs = 'keeply_pref_show_list_thumbnails';
  static const _kPush = 'keeply_pref_push_notifications';
  static const _kEmail = 'keeply_pref_email_reminders';
  static const _kWarranty = 'keeply_pref_asset_warranty_alerts';
  static const _kHelpdesk = 'keeply_pref_helpdesk_activity';
  static const _kDigest = 'keeply_pref_weekly_digest';
  static const _kPoints = 'keeply_loyalty_points';
  static const _kTourDone = 'keeply_handson_tour_v1_done';
  static const _kVisitStreak = 'keeply_visit_streak';
  static const _kLastVisitYmd = 'keeply_last_visit_ymd';

  String _appearance = 'system';
  bool _compactUi = false;
  bool _reduceMotion = false;
  bool _showListThumbnails = true;
  bool _pushNotifications = true;
  bool _emailReminders = false;
  bool _assetWarrantyAlerts = true;
  bool _helpdeskActivityAlerts = true;
  bool _weeklyDigest = false;
  int _loyaltyPoints = 0;
  bool _handsonTourDone = false;
  int _visitStreak = 0;
  String? _lastVisitYmd;

  String get appearance => _appearance;
  bool get compactUi => _compactUi;
  bool get reduceMotion => _reduceMotion;
  bool get showListThumbnails => _showListThumbnails;
  bool get pushNotifications => _pushNotifications;
  bool get emailReminders => _emailReminders;
  bool get assetWarrantyAlerts => _assetWarrantyAlerts;
  bool get helpdeskActivityAlerts => _helpdeskActivityAlerts;
  bool get weeklyDigest => _weeklyDigest;
  int get loyaltyPoints => _loyaltyPoints;
  bool get handsonTourDone => _handsonTourDone;
  int get visitStreakDays => _visitStreak;
  String? get lastVisitYmd => _lastVisitYmd;

  /// Gamification “level” from wallet points (local only).
  int get rewardLevel => (_loyaltyPoints ~/ 100) + 1;

  ThemeMode get materialThemeMode {
    switch (_appearance) {
      case 'light':
        return ThemeMode.light;
      case 'dark':
        return ThemeMode.dark;
      default:
        return ThemeMode.system;
    }
  }

  Future<void> load() async {
    final p = await SharedPreferences.getInstance();
    _appearance = p.getString(_kAppearance) ?? 'system';
    _compactUi = p.getBool(_kCompact) ?? false;
    _reduceMotion = p.getBool(_kReduceMotion) ?? false;
    _showListThumbnails = p.getBool(_kThumbs) ?? true;
    _pushNotifications = p.getBool(_kPush) ?? true;
    _emailReminders = p.getBool(_kEmail) ?? false;
    _assetWarrantyAlerts = p.getBool(_kWarranty) ?? true;
    _helpdeskActivityAlerts = p.getBool(_kHelpdesk) ?? true;
    _weeklyDigest = p.getBool(_kDigest) ?? false;
    _loyaltyPoints = p.getInt(_kPoints) ?? 0;
    _handsonTourDone = p.getBool(_kTourDone) ?? false;
    _visitStreak = p.getInt(_kVisitStreak) ?? 0;
    _lastVisitYmd = p.getString(_kLastVisitYmd);
    notifyListeners();
  }

  Future<void> setHandsonTourDone(bool v) async {
    _handsonTourDone = v;
    await (await SharedPreferences.getInstance()).setBool(_kTourDone, v);
    notifyListeners();
  }

  /// Call when the main dashboard is opened — awards small daily points once per local day.
  Future<void> recordDashboardVisit() async {
    final today = _ymd(DateTime.now());
    if (_lastVisitYmd == today) return;

    var nextStreak = 1;
    if (_lastVisitYmd != null && _lastVisitYmd!.isNotEmpty) {
      final last = DateTime.tryParse('${_lastVisitYmd}T12:00:00');
      if (last != null) {
        final n = DateTime.now();
        final todayDate = DateTime(n.year, n.month, n.day);
        final lastDate = DateTime(last.year, last.month, last.day);
        final gap = todayDate.difference(lastDate).inDays;
        if (gap == 1) {
          nextStreak = (_visitStreak < 1 ? 1 : _visitStreak) + 1;
        } else if (gap > 1) {
          nextStreak = 1;
        }
      }
    }

    _visitStreak = nextStreak;
    _lastVisitYmd = today;
    final p = await SharedPreferences.getInstance();
    await p.setString(_kLastVisitYmd, today);
    await p.setInt(_kVisitStreak, _visitStreak);
    await addLoyaltyPoints(5);
  }

  Future<void> setAppearance(String v) async {
    if (!const {'system', 'light', 'dark'}.contains(v)) return;
    _appearance = v;
    final p = await SharedPreferences.getInstance();
    await p.setString(_kAppearance, v);
    notifyListeners();
  }

  Future<void> setCompactUi(bool v) async {
    _compactUi = v;
    await (await SharedPreferences.getInstance()).setBool(_kCompact, v);
    notifyListeners();
  }

  Future<void> setReduceMotion(bool v) async {
    _reduceMotion = v;
    await (await SharedPreferences.getInstance()).setBool(_kReduceMotion, v);
    notifyListeners();
  }

  Future<void> setShowListThumbnails(bool v) async {
    _showListThumbnails = v;
    await (await SharedPreferences.getInstance()).setBool(_kThumbs, v);
    notifyListeners();
  }

  Future<void> setPushNotifications(bool v) async {
    _pushNotifications = v;
    await (await SharedPreferences.getInstance()).setBool(_kPush, v);
    notifyListeners();
  }

  Future<void> setEmailReminders(bool v) async {
    _emailReminders = v;
    await (await SharedPreferences.getInstance()).setBool(_kEmail, v);
    notifyListeners();
  }

  Future<void> setAssetWarrantyAlerts(bool v) async {
    _assetWarrantyAlerts = v;
    await (await SharedPreferences.getInstance()).setBool(_kWarranty, v);
    notifyListeners();
  }

  Future<void> setHelpdeskActivityAlerts(bool v) async {
    _helpdeskActivityAlerts = v;
    await (await SharedPreferences.getInstance()).setBool(_kHelpdesk, v);
    notifyListeners();
  }

  Future<void> setWeeklyDigest(bool v) async {
    _weeklyDigest = v;
    await (await SharedPreferences.getInstance()).setBool(_kDigest, v);
    notifyListeners();
  }

  Future<void> addLoyaltyPoints(int delta) async {
    if (delta == 0) return;
    _loyaltyPoints = (_loyaltyPoints + delta).clamp(0, 999999999);
    await (await SharedPreferences.getInstance()).setInt(_kPoints, _loyaltyPoints);
    notifyListeners();
  }
}

/// Provides [KeeplyAppPreferences] below [MaterialApp] route tree.
class KeeplyAppPrefsScope extends InheritedNotifier<KeeplyAppPreferences> {
  const KeeplyAppPrefsScope({
    super.key,
    required KeeplyAppPreferences preferences,
    required super.child,
  }) : super(notifier: preferences);

  static KeeplyAppPreferences of(BuildContext context) {
    final w = context.dependOnInheritedWidgetOfExactType<KeeplyAppPrefsScope>();
    assert(w != null, 'KeeplyAppPrefsScope not found');
    return w!.notifier!;
  }
}
