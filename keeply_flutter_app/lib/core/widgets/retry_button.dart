import 'package:flutter/material.dart';

/// Retry Button Widget
/// Standardized retry button with loading state
class RetryButton extends StatelessWidget {
  final VoidCallback onRetry;
  final bool isLoading;
  final String? label;

  const RetryButton({
    super.key,
    required this.onRetry,
    this.isLoading = false,
    this.label,
  });

  @override
  Widget build(BuildContext context) {
    return ElevatedButton.icon(
      onPressed: isLoading ? null : onRetry,
      icon: isLoading
          ? const SizedBox(
              width: 16,
              height: 16,
              child: CircularProgressIndicator(strokeWidth: 2),
            )
          : const Icon(Icons.refresh),
      label: Text(label ?? 'Retry'),
    );
  }
}

