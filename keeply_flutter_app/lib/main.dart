import 'dart:async';

import 'package:flutter/foundation.dart' show kDebugMode;
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:hive_flutter/hive_flutter.dart';
import 'package:keeply_app/core/config/app_config.dart';
import 'package:keeply_app/core/network/api_client.dart';
import 'package:keeply_app/core/network/dev_api_reachability.dart';
import 'package:keeply_app/core/preferences/keeply_app_preferences.dart';
import 'package:keeply_app/core/sync/app_data_refresh_cubit.dart';
import 'package:keeply_app/core/utils/logger.dart';
import 'package:keeply_app/core/view_layout/view_layout_scope.dart';
import 'package:keeply_app/features/auth/data/datasources/auth_remote_datasource.dart';
import 'package:keeply_app/features/auth/presentation/bloc/auth_bloc.dart';
import 'package:keeply_app/features/asset/data/datasources/asset_remote_datasource.dart';
import 'package:keeply_app/features/asset/presentation/bloc/asset_bloc.dart';
import 'package:keeply_app/core/theme/keeply_theme.dart';
import 'package:keeply_app/features/auth/presentation/pages/login_page.dart';
import 'package:keeply_app/features/auth/presentation/pages/register_page.dart';
import 'package:keeply_app/features/auth/presentation/pages/splash_page.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Hive.initFlutter();

  // Initialize API Client
  try {
    await ApiClient().initialize();
    AppLogger.info('Application initialized');
    if (kDebugMode) {
      AppLogger.info(
        'Debug API bases — auth: ${AppConfig.authServiceBaseUrl}, '
        'notification: ${AppConfig.notificationServiceBaseUrl}, '
        'asset: ${AppConfig.assetServiceBaseUrl}, '
        'helpdesk: ${AppConfig.helpdeskServiceBaseUrl}',
      );
      unawaited(logDevApiReachabilityHint());
    }
  } catch (e) {
    AppLogger.error('Failed to initialize API Client: $e');
  }

  runApp(const KeeplyApp());
}

class KeeplyApp extends StatefulWidget {
  const KeeplyApp({super.key});

  @override
  State<KeeplyApp> createState() => _KeeplyAppState();
}

class _KeeplyAppState extends State<KeeplyApp> {
  late final ValueNotifier<ViewLayoutMode> _viewLayoutNotifier;
  late final KeeplyAppPreferences _appPrefs;

  @override
  void initState() {
    super.initState();
    _appPrefs = KeeplyAppPreferences();
    _appPrefs.load();
    // Default: list layout everywhere the [ViewLayoutToggle] applies; user choice is persisted.
    _viewLayoutNotifier = ValueNotifier(ViewLayoutMode.list);
    _viewLayoutNotifier.addListener(_persistLayout);
    SharedPreferences.getInstance().then((prefs) {
      final raw = prefs.getString('keeply_view_layout');
      if (!mounted) return;
      if (raw == ViewLayoutMode.card.name) {
        _viewLayoutNotifier.value = ViewLayoutMode.card;
      } else if (raw == ViewLayoutMode.list.name) {
        _viewLayoutNotifier.value = ViewLayoutMode.list;
      }
    });
  }

  void _persistLayout() {
    SharedPreferences.getInstance().then(
      (prefs) => prefs.setString('keeply_view_layout', _viewLayoutNotifier.value.name),
    );
  }

  @override
  void dispose() {
    _viewLayoutNotifier.removeListener(_persistLayout);
    _viewLayoutNotifier.dispose();
    _appPrefs.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return ViewLayoutScope(
      notifier: _viewLayoutNotifier,
      child: MultiBlocProvider(
        providers: [
          BlocProvider(
            create: (context) => AuthBloc(
              authDataSource: AuthRemoteDataSource(),
              apiClient: ApiClient(),
            )..add(CheckAuthEvent()),
          ),
          BlocProvider(create: (_) => AppDataRefreshCubit()),
          BlocProvider(
            create: (context) => AssetBloc(
              assetDataSource: AssetRemoteDataSource(),
            ),
          ),
        ],
        child: KeeplyAppPrefsScope(
          preferences: _appPrefs,
          child: ListenableBuilder(
            listenable: _appPrefs,
            builder: (context, _) {
              final density = _appPrefs.compactUi ? VisualDensity.compact : VisualDensity.standard;
              return MaterialApp(
                title: 'Keeply - Asset Management',
                debugShowCheckedModeBanner: false,
                themeMode: _appPrefs.materialThemeMode,
                theme: KeeplyTheme.light(visualDensity: density),
                darkTheme: KeeplyTheme.dark(visualDensity: density),
                builder: (context, child) {
                  final mq = MediaQuery.of(context);
                  return MediaQuery(
                    data: mq.copyWith(disableAnimations: _appPrefs.reduceMotion),
                    child: child ?? const SizedBox.shrink(),
                  );
                },
                home: const SplashPage(),
                routes: {
                  '/login': (context) => const LoginPage(),
                  '/register': (context) => const RegisterPage(),
                },
              );
            },
          ),
        ),
      ),
    );
  }
}

