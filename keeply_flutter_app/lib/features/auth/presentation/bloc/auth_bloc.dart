import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:keeply_app/core/exceptions/api_exception.dart';
import 'package:keeply_app/core/utils/logger.dart';
import 'package:keeply_app/features/auth/data/datasources/auth_remote_datasource.dart';
import 'package:keeply_app/features/auth/data/models/auth_models.dart';
import 'package:keeply_app/core/network/api_client.dart';
import 'package:local_auth/local_auth.dart';
import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:local_auth_android/local_auth_android.dart';
import 'package:local_auth_darwin/local_auth_darwin.dart';

/// Authentication BLoC
class AuthBloc extends Bloc<AuthEvent, AuthState> {
  final AuthRemoteDataSource _authDataSource;
  final ApiClient _apiClient;
  final LocalAuthentication _localAuth;
  final Connectivity _connectivity;

  AuthBloc({
    required AuthRemoteDataSource authDataSource,
    required ApiClient apiClient,
    LocalAuthentication? localAuth,
    Connectivity? connectivity,
  })  : _authDataSource = authDataSource,
        _apiClient = apiClient,
        _localAuth = localAuth ?? LocalAuthentication(),
        _connectivity = connectivity ?? Connectivity(),
        super(AuthInitial()) {
    on<LoginEvent>(_onLogin);
    on<RegisterEvent>(_onRegister);
    on<LogoutEvent>(_onLogout);
    on<CheckAuthEvent>(_onCheckAuth);
    on<RefreshTokenEvent>(_onRefreshToken);
    on<SendOtpEvent>(_onSendOtp);
    on<LoginWithOtpEvent>(_onLoginWithOtp);
    on<ChangePasswordEvent>(_onChangePassword);
    on<ForgotPasswordEvent>(_onForgotPassword);
    on<BiometricLoginEvent>(_onBiometricLogin);
  }

  // ============================================================
  // LOGIN
  // ============================================================
  Future<void> _onLogin(LoginEvent event, Emitter<AuthState> emit) async {
    emit(AuthLoading());

    try {
      final connectivityResult = await _connectivity.checkConnectivity();
      if (connectivityResult == ConnectivityResult.none) {
        emit(AuthError('No internet connection.'));
        return;
      }

      if (event.loginType == 'PASSWORD') {
        if (event.username?.isEmpty ?? true) {
          emit(AuthError('Username is required'));
          return;
        }
        if (event.password?.isEmpty ?? true) {
          emit(AuthError('Password is required'));
          return;
        }
        if (event.password!.length < 8) {
          emit(AuthError('Password must be at least 8 characters'));
          return;
        }
      }

      if (event.loginType == 'OTP') {
        if (event.otp == null ||
            event.otp!.isEmpty ||
            event.otp!.length != 6 ||
            !RegExp(r'^\d+$').hasMatch(event.otp!)) {
          emit(AuthError('Invalid OTP'));
          return;
        }
      }

      if (event.loginType == 'MPIN') {
        if (event.mpin == null ||
            event.mpin!.length != 4 ||
            !RegExp(r'^\d+$').hasMatch(event.mpin!)) {
          emit(AuthError('MPIN must be 4 digits'));
          return;
        }
      }

      final authResponse = await _authDataSource.login(
        LoginRequest(
          loginType: event.loginType,
          username: event.username,
          password: event.password,
          otp: event.otp,
          mpin: event.mpin,
          rsaChallenge: event.rsaChallenge,
          signature: event.signature,
          credentialId: event.credentialId,
          authCode: event.authCode,
          deviceInfo: event.deviceInfo,
        ),
      );

      if (authResponse.accessToken.isEmpty) {
        emit(AuthError('Invalid response from server'));
        return;
      }

      final user = await _authDataSource.getCurrentUser();

      emit(AuthAuthenticated(user: user, authResponse: authResponse));
    } catch (e) {
      emit(AuthError('Login failed. Please try again.'));
    }
  }

  // ============================================================
  // REGISTER
  // ============================================================
  Future<void> _onRegister(RegisterEvent event, Emitter<AuthState> emit) async {
    emit(AuthLoading());
    try {
      if (event.username.isEmpty || event.username.length < 3) {
        emit(AuthError('Invalid username'));
        return;
      }
      if (event.password.length < 8) {
        emit(AuthError('Password must be at least 8 characters'));
        return;
      }

      final authResponse = await _authDataSource.register(
        RegisterRequest(
          username: event.username,
          password: event.password,
          email: event.email,
          mobile: event.mobile,
          projectType: event.projectType,
        ),
      );

      final user = await _authDataSource.getCurrentUser();
      emit(AuthAuthenticated(user: user, authResponse: authResponse));
    } catch (e) {
      emit(AuthError('Registration failed'));
    }
  }

  // ============================================================
  // LOGOUT
  // ============================================================
  Future<void> _onLogout(LogoutEvent event, Emitter<AuthState> emit) async {
    await _authDataSource.logout();
    emit(AuthUnauthenticated());
  }

  // ============================================================
  // CHECK AUTH
  // ============================================================
  Future<void> _onCheckAuth(CheckAuthEvent event, Emitter<AuthState> emit) async {
    try {
      final isAuth = await _apiClient.isAuthenticated();
      if (!isAuth) {
        emit(AuthUnauthenticated());
        return;
      }

      final user = await _authDataSource.getCurrentUser();
      emit(AuthAuthenticated(user: user));
    } catch (_) {
      emit(AuthUnauthenticated());
    }
  }

  // ============================================================
  // REFRESH TOKEN
  // ============================================================
  Future<void> _onRefreshToken(RefreshTokenEvent event, Emitter<AuthState> emit) async {
    try {
      final refreshToken = await _apiClient.getRefreshToken();
      if (refreshToken == null) {
        emit(AuthUnauthenticated());
        return;
      }

      final auth = await _authDataSource.refreshToken(refreshToken);
      final user = await _authDataSource.getCurrentUser();
      emit(AuthAuthenticated(user: user, authResponse: auth));
    } catch (_) {
      emit(AuthUnauthenticated());
    }
  }

  // ============================================================
  // SEND OTP
  // ============================================================
  Future<void> _onSendOtp(SendOtpEvent event, Emitter<AuthState> emit) async {
    emit(AuthLoading());
    try {
      final otpResponse = await _authDataSource.sendOtp(
        OtpRequest(
          userId: event.userId,
          purpose: event.purpose,
          channel: event.channel,
        ),
      );

      emit(OtpSent(otpResponse: otpResponse));
    } catch (_) {
      emit(AuthError('Failed to send OTP'));
    }
  }

  // ============================================================
  // LOGIN VIA OTP
  // ============================================================
  Future<void> _onLoginWithOtp(LoginWithOtpEvent event, Emitter<AuthState> emit) async {
    emit(AuthLoading());
    try {
      if (event.otp.length != 6) {
        emit(AuthError('Invalid OTP'));
        return;
      }

      final authResponse = await _authDataSource.login(
        LoginRequest(
          loginType: 'OTP',
          username: event.username,
          otp: event.otp,
        ),
      );

      final user = await _authDataSource.getCurrentUser();
      emit(AuthAuthenticated(user: user, authResponse: authResponse));
    } catch (_) {
      emit(AuthError('OTP login failed'));
    }
  }

  // ============================================================
  // CHANGE PASSWORD
  // ============================================================
  Future<void> _onChangePassword(ChangePasswordEvent event, Emitter<AuthState> emit) async {
    emit(AuthLoading());
    try {
      await _authDataSource.changePassword(
        userId: event.userId,
        currentPassword: event.currentPassword,
        newPassword: event.newPassword,
      );
      emit(PasswordChanged());
    } catch (_) {
      emit(AuthError('Failed to change password'));
    }
  }

  // ============================================================
  // FORGOT PASSWORD
  // ============================================================
  Future<void> _onForgotPassword(ForgotPasswordEvent event, Emitter<AuthState> emit) async {
    emit(AuthLoading());
    try {
      await _authDataSource.forgotPassword(
        username: event.username,
        projectType: event.projectType,
      );
      emit(PasswordResetSent());
    } catch (_) {
      emit(AuthError('Failed to send reset email'));
    }
  }

  // ============================================================
  // BIOMETRIC LOGIN (FIXED)
  // ============================================================
  Future<void> _onBiometricLogin(BiometricLoginEvent event, Emitter<AuthState> emit) async {
    emit(AuthLoading());

    try {
      final canCheck = await _localAuth.canCheckBiometrics;
      final supported = await _localAuth.isDeviceSupported();

      if (!canCheck || !supported) {
        emit(AuthError('Biometric authentication not supported'));
        return;
      }

      final didAuthenticate = await _localAuth.authenticate(
        localizedReason: 'Authenticate to continue',
        biometricOnly: true,
        authMessages: const [
          AndroidAuthMessages(
            cancelButton: 'Cancel',
            signInTitle: 'Authentication required',
            signInHint: 'Touch sensor',
          ),
          IOSAuthMessages(
            cancelButton: 'Cancel',
            localizedFallbackTitle: 'Use passcode',
          ),
        ],
      );

      if (!didAuthenticate) {
        emit(AuthError('Biometric authentication failed'));
        return;
      }

      emit(AuthError('Please enter password to complete login'));
    } catch (e) {
      emit(AuthError('Biometric authentication failed'));
    }
  }
}


// ============================================================
// EVENTS
// ============================================================
abstract class AuthEvent extends Equatable {
  const AuthEvent();

  @override
  List<Object?> get props => [];
}

class LoginEvent extends AuthEvent {
  final String loginType;
  final String? username;
  final String? password;
  final String? otp;
  final String? mpin;
  final String? rsaChallenge;
  final String? signature;
  final String? credentialId;
  final String? authCode;
  final String? deviceInfo;

  LoginEvent({
    required this.loginType,
    this.username,
    this.password,
    this.otp,
    this.mpin,
    this.rsaChallenge,
    this.signature,
    this.credentialId,
    this.authCode,
    this.deviceInfo,
  });

  @override
  List<Object?> get props => [
        loginType,
        username,
        password,
        otp,
        mpin,
        rsaChallenge,
        signature,
        credentialId,
        authCode,
        deviceInfo,
      ];
}

class RegisterEvent extends AuthEvent {
  final String username;
  final String password;
  final String? email;
  final String? mobile;
  final String projectType;

  RegisterEvent({
    required this.username,
    required this.password,
    this.email,
    this.mobile,
    required this.projectType,
  });

  @override
  List<Object?> get props => [username, password, email, mobile, projectType];
}

class LogoutEvent extends AuthEvent {}

class CheckAuthEvent extends AuthEvent {}

class RefreshTokenEvent extends AuthEvent {}

class SendOtpEvent extends AuthEvent {
  final int userId;
  final String purpose;
  final String channel;

  SendOtpEvent({
    required this.userId,
    required this.purpose,
    required this.channel,
  });

  @override
  List<Object?> get props => [userId, purpose, channel];
}

class LoginWithOtpEvent extends AuthEvent {
  final String username;
  final String otp;

  LoginWithOtpEvent({
    required this.username,
    required this.otp,
  });

  @override
  List<Object?> get props => [username, otp];
}

class ChangePasswordEvent extends AuthEvent {
  final int userId;
  final String currentPassword;
  final String newPassword;

  ChangePasswordEvent({
    required this.userId,
    required this.currentPassword,
    required this.newPassword,
  });

  @override
  List<Object?> get props => [userId, currentPassword, newPassword];
}

class ForgotPasswordEvent extends AuthEvent {
  final String username;
  final String projectType;

  ForgotPasswordEvent({
    required this.username,
    required this.projectType,
  });

  @override
  List<Object?> get props => [username, projectType];
}

class BiometricLoginEvent extends AuthEvent {}

// ============================================================
// STATES
// ============================================================
abstract class AuthState extends Equatable {
  const AuthState();

  @override
  List<Object?> get props => [];
}

class AuthInitial extends AuthState {}

class AuthLoading extends AuthState {}

class AuthAuthenticated extends AuthState {
  final UserDto user;
  final AuthResponse? authResponse;

  AuthAuthenticated({
    required this.user,
    this.authResponse,
  });

  @override
  List<Object?> get props => [user, authResponse];
}

class AuthUnauthenticated extends AuthState {}

class AuthError extends AuthState {
  final String message;

  AuthError(this.message);

  @override
  List<Object?> get props => [message];
}

class OtpSent extends AuthState {
  final OtpResponse otpResponse;

  OtpSent({required this.otpResponse});

  @override
  List<Object?> get props => [otpResponse];
}

class PasswordChanged extends AuthState {}

class PasswordResetSent extends AuthState {}

