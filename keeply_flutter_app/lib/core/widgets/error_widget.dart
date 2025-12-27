import 'package:flutter/material.dart';
import 'package:keeply_app/core/exceptions/api_exception.dart';

/// Error Widget
/// Displays user-friendly error messages with retry option
class AppErrorWidget extends StatelessWidget {
  final String message;
  final VoidCallback? onRetry;
  final IconData? icon;
  final Color? iconColor;

  const AppErrorWidget({
    super.key,
    required this.message,
    this.onRetry,
    this.icon,
    this.iconColor,
  });

  factory AppErrorWidget.fromException(
    ApiException exception, {
    VoidCallback? onRetry,
  }) {
    IconData errorIcon = Icons.error_outline;
    Color errorColor = Colors.red[300]!;

    switch (exception.type) {
      case ApiExceptionType.network:
        errorIcon = Icons.wifi_off;
        errorColor = Colors.orange[300]!;
        break;
      case ApiExceptionType.timeout:
        errorIcon = Icons.timer_off;
        errorColor = Colors.orange[300]!;
        break;
      case ApiExceptionType.unauthorized:
        errorIcon = Icons.lock_outline;
        errorColor = Colors.red[300]!;
        break;
      default:
        errorIcon = Icons.error_outline;
        errorColor = Colors.red[300]!;
    }

    return AppErrorWidget(
      message: exception.userMessage,
      onRetry: onRetry,
      icon: errorIcon,
      iconColor: errorColor,
    );
  }

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              icon ?? Icons.error_outline,
              size: 64,
              color: iconColor ?? Colors.red[300],
            ),
            const SizedBox(height: 16),
            Text(
              message,
              style: const TextStyle(fontSize: 16),
              textAlign: TextAlign.center,
            ),
            if (onRetry != null) ...[
              const SizedBox(height: 24),
              ElevatedButton.icon(
                onPressed: onRetry,
                icon: const Icon(Icons.refresh),
                label: const Text('Retry'),
              ),
            ],
          ],
        ),
      ),
    );
  }
}

