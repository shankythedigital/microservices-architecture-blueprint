import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:keeply_app/core/theme/keeply_tokens.dart';
import 'package:keeply_app/core/widgets/keeply_auth_screen_background.dart';
import 'package:keeply_app/features/auth/data/datasources/auth_remote_datasource.dart';
import 'package:keeply_app/features/auth/presentation/bloc/auth_bloc.dart';
import 'package:keeply_app/features/shell/presentation/keeply_mobile_shell.dart';

/// Matches React `LoginPage` — welcome gradient, card, segmented Mobile OTP / Password, two-step OTP.
class LoginPage extends StatefulWidget {
  const LoginPage({super.key, this.prefilledUsername});

  /// After registration, password tab can open with this username filled in.
  final String? prefilledUsername;

  @override
  State<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  final _authDs = AuthRemoteDataSource();
  final _mobileController = TextEditingController();
  final _otpController = TextEditingController();
  late final TextEditingController _usernameController;
  final _passwordController = TextEditingController();

  bool _otpMode = true;
  bool _otpStepPhone = true;
  bool _busy = false;
  String? _sendErr;
  String? _devOtpHint;
  bool _obscurePassword = true;

  @override
  void initState() {
    super.initState();
    final u = widget.prefilledUsername?.trim();
    _usernameController = TextEditingController(text: u != null && u.isNotEmpty ? u : '');
    if (u != null && u.isNotEmpty) {
      _otpMode = false;
    }
  }

  @override
  void dispose() {
    _mobileController.dispose();
    _otpController.dispose();
    _usernameController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  String _digits(String s) => s.replaceAll(RegExp(r'\D'), '');

  Future<void> _sendOtp() async {
    setState(() {
      _sendErr = null;
      _devOtpHint = null;
      _busy = true;
    });
    try {
      final d = _digits(_mobileController.text);
      if (d.length < 10) {
        setState(() {
          _sendErr = 'Enter a valid mobile number (10–15 digits).';
          _busy = false;
        });
        return;
      }
      final res = await _authDs.sendLoginOtp(d);
      setState(() {
        _otpStepPhone = false;
        _devOtpHint = res['otp'] as String?;
      });
    } catch (e) {
      setState(() => _sendErr = '$e');
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  void _verifyOtp() {
    final d = _digits(_mobileController.text);
    final otp = _otpController.text.trim();
    if (otp.length != 6 || !RegExp(r'^\d+$').hasMatch(otp)) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Enter the 6-digit OTP')),
      );
      return;
    }
    setState(() => _busy = true);
    context.read<AuthBloc>().add(
          LoginEvent(
            loginType: 'OTP',
            username: d,
            otp: otp,
          ),
        );
  }

  void _passwordLogin() {
    final u = _usernameController.text.trim();
    final p = _passwordController.text;
    if (u.isEmpty || p.length < 8) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Username and password (8+ chars) are required')),
      );
      return;
    }
    setState(() => _busy = true);
    context.read<AuthBloc>().add(
          LoginEvent(
            loginType: 'PASSWORD',
            username: u,
            password: p,
          ),
        );
  }

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    final scheme = Theme.of(context).colorScheme;

    return BlocListener<AuthBloc, AuthState>(
      listener: (context, state) {
        if (state is AuthAuthenticated) {
          setState(() => _busy = false);
          Navigator.of(context).pushAndRemoveUntil(
            MaterialPageRoute<void>(builder: (_) => const KeeplyMobileShell()),
            (_) => false,
          );
        } else if (state is AuthError) {
          setState(() => _busy = false);
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text(state.message)),
          );
        } else if (state is AuthLoading) {
          setState(() => _busy = true);
        }
      },
      child: Scaffold(
        backgroundColor: scheme.surface,
        body: KeeplyAuthBackground(
          child: SafeArea(
            child: Center(
              child: SingleChildScrollView(
                padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 24),
                child: ConstrainedBox(
                  constraints: const BoxConstraints(maxWidth: KeeplyTokens.maxAppWidth),
                  child: DecoratedBox(
                    decoration: BoxDecoration(
                      color: scheme.surface,
                      borderRadius: BorderRadius.circular(KeeplyTokens.radius),
                      border: Border.all(color: scheme.outline.withValues(alpha: 0.22)),
                      boxShadow: const [
                        BoxShadow(color: Color(0x140F172A), blurRadius: 28, offset: Offset(0, 12)),
                      ],
                    ),
                    child: Padding(
                      padding: const EdgeInsets.fromLTRB(22, 26, 22, 22),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.stretch,
                        children: [
                          if (Navigator.of(context).canPop())
                            Align(
                              alignment: Alignment.centerLeft,
                              child: IconButton(
                                tooltip: 'Back',
                                onPressed: () => Navigator.of(context).maybePop(),
                                icon: Icon(Icons.arrow_back_rounded, color: scheme.onSurface),
                              ),
                            ),
                          Text(
                            'Sign in',
                            style: t.headlineSmall?.copyWith(
                              fontWeight: FontWeight.w800,
                              color: scheme.onSurface,
                            ),
                          ),
                          const SizedBox(height: 8),
                          Text(
                            'Enter your mobile number to get a one-time passcode.',
                            style: t.bodySmall?.copyWith(color: scheme.onSurfaceVariant, height: 1.4),
                          ),
                          const SizedBox(height: 18),
                          _Segmented(
                            otpSelected: _otpMode,
                            onChanged: (otp) => setState(() {
                              _otpMode = otp;
                              _sendErr = null;
                            }),
                          ),
                          const SizedBox(height: 20),
                          if (_otpMode && _otpStepPhone) ...[
                            TextField(
                              controller: _mobileController,
                              keyboardType: TextInputType.phone,
                              decoration: const InputDecoration(
                                labelText: 'Mobile number',
                                hintText: '10–15 digits',
                              ),
                            ),
                            const SizedBox(height: 8),
                            Text(
                              'Country-aware formatting can mirror KeeplyV1.pdf; API expects digits only.',
                              style: t.labelSmall?.copyWith(color: scheme.onSurfaceVariant, height: 1.35),
                            ),
                            if (_sendErr != null) ...[
                              const SizedBox(height: 10),
                              _Banner(text: _sendErr!, ok: false),
                            ],
                            const SizedBox(height: 16),
                            FilledButton(
                              onPressed: _busy ? null : _sendOtp,
                              child: _busy ? const SizedBox(height: 22, width: 22, child: CircularProgressIndicator(strokeWidth: 2)) : const Text('Send OTP'),
                            ),
                          ],
                          if (_otpMode && !_otpStepPhone) ...[
                            TextField(
                              controller: _otpController,
                              keyboardType: TextInputType.number,
                              decoration: const InputDecoration(labelText: 'OTP from SMS'),
                            ),
                            if (_devOtpHint != null) ...[
                              const SizedBox(height: 8),
                              Text(
                                'Dev: OTP returned by auth-service: $_devOtpHint',
                                style: t.labelSmall?.copyWith(color: KeeplyTokens.accentInk),
                              ),
                            ],
                            const SizedBox(height: 16),
                            FilledButton(
                              onPressed: _busy ? null : _verifyOtp,
                              child: const Text('Verify & continue'),
                            ),
                            const SizedBox(height: 10),
                            TextButton(
                              onPressed: () {
                                setState(() {
                                  _otpStepPhone = true;
                                  _otpController.clear();
                                  _devOtpHint = null;
                                });
                              },
                              child: const Text('Use a different number'),
                            ),
                          ],
                          if (!_otpMode) ...[
                            TextField(
                              controller: _usernameController,
                              decoration: const InputDecoration(labelText: 'Username'),
                              textInputAction: TextInputAction.next,
                            ),
                            const SizedBox(height: 12),
                            TextField(
                              controller: _passwordController,
                              obscureText: _obscurePassword,
                              decoration: InputDecoration(
                                labelText: 'Password',
                                suffixIcon: IconButton(
                                  icon: Icon(_obscurePassword ? Icons.visibility : Icons.visibility_off),
                                  onPressed: () => setState(() => _obscurePassword = !_obscurePassword),
                                ),
                              ),
                            ),
                            const SizedBox(height: 16),
                            FilledButton(
                              onPressed: _busy ? null : _passwordLogin,
                              child: const Text('Sign in'),
                            ),
                          ],
                          const SizedBox(height: 14),
                          TextButton(
                            onPressed: () {
                              Navigator.of(context).pushNamed('/register');
                            },
                            child: const Text('Need an account? Register'),
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
      ),
    );
  }
}

class _Segmented extends StatelessWidget {
  const _Segmented({required this.otpSelected, required this.onChanged});

  final bool otpSelected;
  final void Function(bool otp) onChanged;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Container(
      padding: const EdgeInsets.all(4),
      decoration: BoxDecoration(
        color: scheme.surfaceContainerHighest.withValues(alpha: 0.45),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: scheme.outline.withValues(alpha: 0.28)),
      ),
      child: Row(
        children: [
          Expanded(
            child: _SegBtn(
              label: 'Mobile OTP',
              selected: otpSelected,
              onTap: () => onChanged(true),
            ),
          ),
          Expanded(
            child: _SegBtn(
              label: 'Password',
              selected: !otpSelected,
              onTap: () => onChanged(false),
            ),
          ),
        ],
      ),
    );
  }
}

class _SegBtn extends StatelessWidget {
  const _SegBtn({required this.label, required this.selected, required this.onTap});

  final String label;
  final bool selected;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Material(
      color: selected ? scheme.surface : Colors.transparent,
      borderRadius: BorderRadius.circular(10),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(10),
        child: Container(
          padding: const EdgeInsets.symmetric(vertical: 10),
          alignment: Alignment.center,
          child: Text(
            label,
            style: TextStyle(
              fontWeight: selected ? FontWeight.w700 : FontWeight.w500,
              color: selected ? scheme.onSurface : scheme.onSurfaceVariant,
              fontSize: 13,
            ),
          ),
        ),
      ),
    );
  }
}

class _Banner extends StatelessWidget {
  const _Banner({required this.text, required this.ok});

  final String text;
  final bool ok;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: (ok ? KeeplyTokens.accent : KeeplyTokens.danger).withValues(alpha: 0.1),
        borderRadius: BorderRadius.circular(KeeplyTokens.radiusXs),
        border: Border.all(
          color: (ok ? KeeplyTokens.accent : KeeplyTokens.danger).withValues(alpha: 0.25),
        ),
      ),
      child: Text(
        text,
        style: Theme.of(context).textTheme.bodySmall?.copyWith(
              color: ok ? KeeplyTokens.accentInk : KeeplyTokens.danger,
              height: 1.35,
            ),
      ),
    );
  }
}
