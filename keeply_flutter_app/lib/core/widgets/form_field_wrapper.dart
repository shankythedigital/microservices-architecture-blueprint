import 'package:flutter/material.dart';

/// Form Field Wrapper
/// Wraps form fields with consistent styling and error handling
class FormFieldWrapper extends StatelessWidget {
  final Widget child;
  final String? errorText;
  final String? helperText;

  const FormFieldWrapper({
    super.key,
    required this.child,
    this.errorText,
    this.helperText,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        child,
        if (errorText != null) ...[
          const SizedBox(height: 4),
          Text(
            errorText!,
            style: TextStyle(
              color: Colors.red[700],
              fontSize: 12,
            ),
          ),
        ],
        if (helperText != null && errorText == null) ...[
          const SizedBox(height: 4),
          Text(
            helperText!,
            style: TextStyle(
              color: Colors.grey[600],
              fontSize: 12,
            ),
          ),
        ],
      ],
    );
  }
}

