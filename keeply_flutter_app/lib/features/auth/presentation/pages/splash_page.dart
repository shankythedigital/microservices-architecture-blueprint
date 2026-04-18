import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:keeply_app/core/widgets/keeply_auth_screen_background.dart';
import 'package:keeply_app/features/auth/presentation/bloc/auth_bloc.dart';
import 'package:keeply_app/features/auth/presentation/pages/welcome_page.dart';
import 'package:keeply_app/features/shell/presentation/keeply_mobile_shell.dart';

/// Splash Page
/// Shows loading screen and checks authentication status
class SplashPage extends StatefulWidget {
  const SplashPage({super.key});

  @override
  State<SplashPage> createState() => _SplashPageState();
}

class _SplashPageState extends State<SplashPage> {
  @override
  void initState() {
    super.initState();
    // Check auth status is already triggered in main.dart
  }

  @override
  Widget build(BuildContext context) {
    return BlocListener<AuthBloc, AuthState>(
      listener: (context, state) {
        if (state is AuthAuthenticated) {
          Navigator.of(context).pushReplacement(
            MaterialPageRoute<void>(builder: (_) => const KeeplyMobileShell()),
          );
        } else if (state is AuthUnauthenticated) {
          Navigator.of(context).pushReplacement(
            MaterialPageRoute<void>(builder: (_) => const WelcomePage()),
          );
        }
      },
      child: Scaffold(
        backgroundColor: Theme.of(context).colorScheme.surface,
        body: KeeplyAuthBackground(
          child: SafeArea(
            child: Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const KeeplyAuthWordmark(size: 'large'),
                  const SizedBox(height: 14),
                  Text(
                    'Asset management',
                    style: Theme.of(context).textTheme.titleMedium?.copyWith(
                          color: Theme.of(context).colorScheme.onSurfaceVariant,
                          fontWeight: FontWeight.w500,
                        ),
                  ),
                  const SizedBox(height: 40),
                  SizedBox(
                    width: 30,
                    height: 30,
                    child: CircularProgressIndicator(
                      strokeWidth: 2.5,
                      color: Theme.of(context).colorScheme.primary,
                    ),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}

