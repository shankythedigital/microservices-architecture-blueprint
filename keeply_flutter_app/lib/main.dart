import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:keeply_app/core/network/api_client.dart';
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

  // Initialize API Client
  try {
    await ApiClient().initialize();
    AppLogger.info('Application initialized');
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

  @override
  void initState() {
    super.initState();
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
          BlocProvider(
            create: (context) => AssetBloc(
              assetDataSource: AssetRemoteDataSource(),
            ),
          ),
        ],
        child: MaterialApp(
          title: 'Keeply - Asset Management',
          debugShowCheckedModeBanner: false,
          theme: KeeplyTheme.light(),
          home: const SplashPage(),
          routes: {
            '/login': (context) => const LoginPage(),
            '/register': (context) => const RegisterPage(),
          },
        ),
      ),
    );
  }
}

