import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:keeply_app/core/network/api_client.dart';
import 'package:keeply_app/core/utils/logger.dart';
import 'package:keeply_app/features/auth/data/datasources/auth_remote_datasource.dart';
import 'package:keeply_app/features/auth/presentation/bloc/auth_bloc.dart';
import 'package:keeply_app/features/asset/data/datasources/asset_remote_datasource.dart';
import 'package:keeply_app/features/asset/presentation/bloc/asset_bloc.dart';
import 'package:keeply_app/features/auth/presentation/pages/login_page.dart';
import 'package:keeply_app/features/auth/presentation/pages/register_page.dart';
import 'package:keeply_app/features/auth/presentation/pages/splash_page.dart';

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

class KeeplyApp extends StatelessWidget {
  const KeeplyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiBlocProvider(
      providers: [
        // Auth BLoC
        BlocProvider(
          create: (context) => AuthBloc(
            authDataSource: AuthRemoteDataSource(),
            apiClient: ApiClient(),
          )..add(CheckAuthEvent()),
        ),
        // Asset BLoC
        BlocProvider(
          create: (context) => AssetBloc(
            assetDataSource: AssetRemoteDataSource(),
          ),
        ),
      ],
      child: MaterialApp(
        title: 'Keeply - Asset Management',
        debugShowCheckedModeBanner: false,
        theme: ThemeData(
          primarySwatch: Colors.blue,
          useMaterial3: true,
          colorScheme: ColorScheme.fromSeed(seedColor: Colors.blue),
        ),
        home: const SplashPage(),
        routes: {
          '/login': (context) => const LoginPage(),
          '/register': (context) => const RegisterPage(),
        },
      ),
    );
  }
}

