import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:keeply_app/core/config/app_config.dart';
import 'package:keeply_app/core/theme/keeply_tokens.dart';
import 'package:keeply_app/core/utils/validation_helper.dart';
import 'package:keeply_app/core/widgets/keeply_auth_screen_background.dart';
import 'package:keeply_app/core/view_layout/view_layout_scope.dart';
import 'package:keeply_app/core/widgets/selectable_option_picker.dart';
import 'package:keeply_app/features/auth/presentation/bloc/auth_bloc.dart';
import 'package:keeply_app/features/auth/presentation/pages/login_page.dart';

/// Register page — collects fields for `POST /api/auth/register` (JSON),
/// then on success navigates to [LoginPage] so the user can sign in.
class RegisterPage extends StatefulWidget {
  const RegisterPage({super.key});

  @override
  State<RegisterPage> createState() => _RegisterPageState();
}

class _RegisterPageState extends State<RegisterPage> {
  final _formKey = GlobalKey<FormState>();

  final _usernameController = TextEditingController();
  final _passwordController = TextEditingController();
  final _confirmPasswordController = TextEditingController();
  final _emailController = TextEditingController();
  final _mobileController = TextEditingController();
  final _firstNameController = TextEditingController();
  final _lastNameController = TextEditingController();
  final _pincodeController = TextEditingController();
  final _cityController = TextEditingController();
  final _stateController = TextEditingController();
  final _countryController = TextEditingController();
  final _address1Controller = TextEditingController();
  final _address2Controller = TextEditingController();
  final _address3Controller = TextEditingController();

  bool _obscurePassword = true;
  bool _obscureConfirmPassword = true;
  bool _acceptTc = false;
  bool _isLoading = false;

  String _projectType = AppConfig.defaultProjectType;
  String _countryCode = '+91';

  static const List<SelectableOption<String>> _countryCodeOptions = [
    SelectableOption(value: '+91', title: 'India (+91)'),
    SelectableOption(value: '+1', title: 'USA / Canada (+1)'),
    SelectableOption(value: '+44', title: 'United Kingdom (+44)'),
    SelectableOption(value: '+61', title: 'Australia (+61)'),
    SelectableOption(value: '+971', title: 'UAE (+971)'),
    SelectableOption(value: '+65', title: 'Singapore (+65)'),
    SelectableOption(value: '+86', title: 'China (+86)'),
    SelectableOption(value: '+49', title: 'Germany (+49)'),
    SelectableOption(value: '+33', title: 'France (+33)'),
  ];

  static const List<SelectableOption<String>> _projectTypeOptions = [
    SelectableOption(value: 'ASSET', title: 'Asset Management'),
    SelectableOption(value: 'ECOM', title: 'E-Commerce'),
    SelectableOption(value: 'PORTAL', title: 'Portal'),
  ];

  @override
  void dispose() {
    _usernameController.dispose();
    _passwordController.dispose();
    _confirmPasswordController.dispose();
    _emailController.dispose();
    _mobileController.dispose();
    _firstNameController.dispose();
    _lastNameController.dispose();
    _pincodeController.dispose();
    _cityController.dispose();
    _stateController.dispose();
    _countryController.dispose();
    _address1Controller.dispose();
    _address2Controller.dispose();
    _address3Controller.dispose();
    super.dispose();
  }

  void _handleRegister() {
    if (!_formKey.currentState!.validate()) {
      return;
    }

    if (_passwordController.text != _confirmPasswordController.text) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            'Passwords do not match',
            style: TextStyle(color: Theme.of(context).colorScheme.onError),
          ),
          backgroundColor: Theme.of(context).colorScheme.error,
        ),
      );
      return;
    }

    if (!_acceptTc) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            'Please accept the Terms & Conditions',
            style: TextStyle(color: Theme.of(context).colorScheme.onError),
          ),
          backgroundColor: Theme.of(context).colorScheme.error,
        ),
      );
      return;
    }

    setState(() => _isLoading = true);

    context.read<AuthBloc>().add(
          RegisterEvent(
            username: _usernameController.text.trim(),
            password: _passwordController.text,
            email: _emailController.text.trim().isEmpty ? null : _emailController.text.trim(),
            mobile: _mobileController.text.trim(),
            countryCode: _countryCode,
            projectType: _projectType,
            acceptTc: true,
            firstName: _firstNameController.text.trim().isEmpty ? null : _firstNameController.text.trim(),
            lastName: _lastNameController.text.trim().isEmpty ? null : _lastNameController.text.trim(),
            pincode: _pincodeController.text.trim().isEmpty ? null : _pincodeController.text.trim(),
            city: _cityController.text.trim().isEmpty ? null : _cityController.text.trim(),
            state: _stateController.text.trim().isEmpty ? null : _stateController.text.trim(),
            country: _countryController.text.trim().isEmpty ? null : _countryController.text.trim(),
            address1: _address1Controller.text.trim().isEmpty ? null : _address1Controller.text.trim(),
            address2: _address2Controller.text.trim().isEmpty ? null : _address2Controller.text.trim(),
            address3: _address3Controller.text.trim().isEmpty ? null : _address3Controller.text.trim(),
          ),
        );
  }

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    final scheme = Theme.of(context).colorScheme;

    return BlocListener<AuthBloc, AuthState>(
      listener: (context, state) {
        if (state is RegistrationSuccess) {
          setState(() => _isLoading = false);
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: const Text(
                'Account created. Please sign in.',
                style: TextStyle(color: Colors.white),
              ),
              backgroundColor: KeeplyTokens.accent,
            ),
          );
          Navigator.of(context).pushAndRemoveUntil(
            MaterialPageRoute<void>(
              builder: (_) => LoginPage(prefilledUsername: state.username),
            ),
            (_) => false,
          );
        } else if (state is AuthError) {
          setState(() => _isLoading = false);
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text(
                state.message,
                style: TextStyle(color: Theme.of(context).colorScheme.onError),
              ),
              backgroundColor: Theme.of(context).colorScheme.error,
            ),
          );
        } else if (state is AuthLoading) {
          setState(() => _isLoading = true);
        }
      },
      child: Scaffold(
        backgroundColor: scheme.surface,
        appBar: AppBar(
          title: const Text('Register'),
          scrolledUnderElevation: 0,
          elevation: 0,
          backgroundColor: scheme.surface.withValues(alpha: 0.92),
          foregroundColor: scheme.onSurface,
          actions: const [
            Padding(
              padding: EdgeInsets.only(right: 8),
              child: ViewLayoutToggle(compact: true),
            ),
          ],
        ),
        body: KeeplyAuthBackground(
          child: SafeArea(
            child: SingleChildScrollView(
              padding: const EdgeInsets.all(24.0),
              child: Form(
                key: _formKey,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    const SizedBox(height: 8),
                    Text(
                      'Create Account',
                      style: t.headlineMedium?.copyWith(
                        fontWeight: FontWeight.w800,
                        color: scheme.onSurface,
                      ),
                      textAlign: TextAlign.center,
                    ),
                  const SizedBox(height: 24),
                  Text(
                    'Account',
                    style: t.titleSmall?.copyWith(fontWeight: FontWeight.w600, color: scheme.onSurface),
                  ),
                  const SizedBox(height: 12),
                  TextFormField(
                    controller: _usernameController,
                    decoration: const InputDecoration(
                      labelText: 'Username *',
                      prefixIcon: Icon(Icons.person),
                    ),
                    validator: ValidationHelper.validateUsername,
                    textInputAction: TextInputAction.next,
                  ),
                  const SizedBox(height: 16),
                  TextFormField(
                    controller: _emailController,
                    keyboardType: TextInputType.emailAddress,
                    decoration: const InputDecoration(
                      labelText: 'Email (optional)',
                      prefixIcon: Icon(Icons.email),
                    ),
                    validator: (value) => ValidationHelper.validateEmail(value, required: false),
                    textInputAction: TextInputAction.next,
                  ),
                  const SizedBox(height: 16),
                  TextFormField(
                    controller: _passwordController,
                    obscureText: _obscurePassword,
                    decoration: InputDecoration(
                      labelText: 'Password *',
                      prefixIcon: const Icon(Icons.lock),
                      suffixIcon: IconButton(
                        icon: Icon(_obscurePassword ? Icons.visibility : Icons.visibility_off),
                        onPressed: () => setState(() => _obscurePassword = !_obscurePassword),
                      ),
                    ),
                    validator: ValidationHelper.validatePassword,
                    textInputAction: TextInputAction.next,
                  ),
                  const SizedBox(height: 16),
                  TextFormField(
                    controller: _confirmPasswordController,
                    obscureText: _obscureConfirmPassword,
                    decoration: InputDecoration(
                      labelText: 'Confirm Password *',
                      prefixIcon: const Icon(Icons.lock_outline),
                      suffixIcon: IconButton(
                        icon: Icon(_obscureConfirmPassword ? Icons.visibility : Icons.visibility_off),
                        onPressed: () => setState(() => _obscureConfirmPassword = !_obscureConfirmPassword),
                      ),
                    ),
                    validator: (value) {
                      if (value == null || value.isEmpty) {
                        return 'Please confirm your password';
                      }
                      if (value != _passwordController.text) {
                        return 'Passwords do not match';
                      }
                      return null;
                    },
                    textInputAction: TextInputAction.next,
                  ),
                  const SizedBox(height: 24),
                  Text(
                    'Mobile',
                    style: t.titleSmall?.copyWith(fontWeight: FontWeight.w600, color: scheme.onSurface),
                  ),
                  const SizedBox(height: 12),
                  SelectableOptionPicker<String>(
                    label: 'Country code *',
                    prefixIcon: Icons.flag,
                    value: _countryCode,
                    options: _countryCodeOptions,
                    onChanged: (v) {
                      if (v != null) setState(() => _countryCode = v);
                    },
                    validator: (v) => (v == null || v.isEmpty) ? 'Country code is required' : null,
                  ),
                  const SizedBox(height: 16),
                  TextFormField(
                    controller: _mobileController,
                    keyboardType: TextInputType.phone,
                    decoration: const InputDecoration(
                      labelText: 'Mobile * (without country code)',
                      prefixIcon: Icon(Icons.phone),
                      helperText: 'Example for +91: 10 digits starting with 6–9',
                    ),
                    validator: (value) => ValidationHelper.validateNationalMobile(value, _countryCode),
                    textInputAction: TextInputAction.next,
                  ),
                  const SizedBox(height: 24),
                  Text(
                    'Project',
                    style: t.titleSmall?.copyWith(fontWeight: FontWeight.w600, color: scheme.onSurface),
                  ),
                  const SizedBox(height: 12),
                  SelectableOptionPicker<String>(
                    label: 'Project type *',
                    prefixIcon: Icons.category,
                    value: _projectType,
                    options: _projectTypeOptions,
                    onChanged: (v) {
                      if (v != null) setState(() => _projectType = v);
                    },
                    validator: (v) => (v == null || v.isEmpty) ? 'Project type is required' : null,
                  ),
                  const SizedBox(height: 16),
                  CheckboxListTile(
                    value: _acceptTc,
                    onChanged: (v) => setState(() => _acceptTc = v ?? false),
                    controlAffinity: ListTileControlAffinity.leading,
                    contentPadding: EdgeInsets.zero,
                    title: Text(
                      'I accept the Terms & Conditions *',
                      style: t.bodyMedium?.copyWith(color: scheme.onSurface),
                    ),
                  ),
                  const SizedBox(height: 8),
                  ExpansionTile(
                    tilePadding: EdgeInsets.zero,
                    title: Text(
                      'Profile (optional)',
                      style: t.titleSmall?.copyWith(fontWeight: FontWeight.w600, color: scheme.onSurface),
                    ),
                    children: [
                      const SizedBox(height: 8),
                      TextFormField(
                        controller: _firstNameController,
                        decoration: const InputDecoration(
                          labelText: 'First name',
                        ),
                        textInputAction: TextInputAction.next,
                      ),
                      const SizedBox(height: 12),
                      TextFormField(
                        controller: _lastNameController,
                        decoration: const InputDecoration(
                          labelText: 'Last name',
                        ),
                        textInputAction: TextInputAction.next,
                      ),
                      const SizedBox(height: 8),
                    ],
                  ),
                  ExpansionTile(
                    tilePadding: EdgeInsets.zero,
                    title: Text(
                      'Address (optional)',
                      style: t.titleSmall?.copyWith(fontWeight: FontWeight.w600, color: scheme.onSurface),
                    ),
                    children: [
                      const SizedBox(height: 8),
                      TextFormField(
                        controller: _address1Controller,
                        decoration: const InputDecoration(
                          labelText: 'Address line 1',
                        ),
                        textInputAction: TextInputAction.next,
                      ),
                      const SizedBox(height: 12),
                      TextFormField(
                        controller: _address2Controller,
                        decoration: const InputDecoration(
                          labelText: 'Address line 2',
                        ),
                        textInputAction: TextInputAction.next,
                      ),
                      const SizedBox(height: 12),
                      TextFormField(
                        controller: _address3Controller,
                        decoration: const InputDecoration(
                          labelText: 'Address line 3',
                        ),
                        textInputAction: TextInputAction.next,
                      ),
                      const SizedBox(height: 12),
                      TextFormField(
                        controller: _pincodeController,
                        decoration: const InputDecoration(
                          labelText: 'Pincode',
                        ),
                        textInputAction: TextInputAction.next,
                      ),
                      const SizedBox(height: 12),
                      TextFormField(
                        controller: _cityController,
                        decoration: const InputDecoration(
                          labelText: 'City',
                        ),
                        textInputAction: TextInputAction.next,
                      ),
                      const SizedBox(height: 12),
                      TextFormField(
                        controller: _stateController,
                        decoration: const InputDecoration(
                          labelText: 'State',
                        ),
                        textInputAction: TextInputAction.next,
                      ),
                      const SizedBox(height: 12),
                      TextFormField(
                        controller: _countryController,
                        decoration: const InputDecoration(
                          labelText: 'Country',
                        ),
                        textInputAction: TextInputAction.done,
                        onFieldSubmitted: (_) => _handleRegister(),
                      ),
                      const SizedBox(height: 8),
                    ],
                  ),
                  const SizedBox(height: 24),
                  FilledButton(
                    onPressed: _isLoading ? null : _handleRegister,
                    style: FilledButton.styleFrom(
                      padding: const EdgeInsets.symmetric(vertical: 16),
                    ),
                    child: _isLoading
                        ? SizedBox(
                            height: 20,
                            width: 20,
                            child: CircularProgressIndicator(
                              strokeWidth: 2,
                              color: scheme.onPrimary,
                            ),
                          )
                        : const Text('Register'),
                  ),
                  const SizedBox(height: 16),
                  TextButton(
                    onPressed: () => Navigator.of(context).pop(),
                    child: const Text('Already have an account? Login'),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
      ),
    );
  }
}
