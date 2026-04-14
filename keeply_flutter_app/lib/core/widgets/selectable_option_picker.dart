import 'package:flutter/material.dart';
import 'package:keeply_app/core/theme/keeply_tokens.dart';
import 'package:keeply_app/core/view_layout/view_layout_scope.dart';

class SelectableOption<T> {
  const SelectableOption({
    required this.value,
    required this.title,
    this.subtitle,
  });

  final T value;
  final String title;
  final String? subtitle;
}

/// Replaces dropdowns: scrollable list rows or compact cards, driven by [ViewLayoutScope].
class SelectableOptionPicker<T> extends StatefulWidget {
  const SelectableOptionPicker({
    super.key,
    required this.label,
    required this.options,
    required this.value,
    required this.onChanged,
    this.validator,
    this.enabled = true,
    this.emptyHint,
    this.prefixIcon,
  });

  final String label;
  final List<SelectableOption<T>> options;
  final T? value;
  final ValueChanged<T?> onChanged;
  final FormFieldValidator<T?>? validator;
  final bool enabled;
  final String? emptyHint;
  final IconData? prefixIcon;

  @override
  State<SelectableOptionPicker<T>> createState() => _SelectableOptionPickerState<T>();
}

class _SelectableOptionPickerState<T> extends State<SelectableOptionPicker<T>> {
  final GlobalKey<FormFieldState<T?>> _fieldKey = GlobalKey<FormFieldState<T?>>();

  @override
  void didUpdateWidget(SelectableOptionPicker<T> oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.value != widget.value) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        _fieldKey.currentState?.didChange(widget.value);
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return FormField<T?>(
      key: _fieldKey,
      initialValue: widget.value,
      validator: (v) => widget.enabled ? widget.validator?.call(v) : null,
      builder: (field) {
        return ListenableBuilder(
          listenable: ViewLayoutScope.notifierOf(context),
          builder: (context, _) {
            final mode = ViewLayoutScope.notifierOf(context).value;
            final borderColor = field.hasError ? Theme.of(context).colorScheme.error : KeeplyTokens.line;

            return Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                Row(
                  crossAxisAlignment: CrossAxisAlignment.center,
                  children: [
                    if (widget.prefixIcon != null) ...[
                      Icon(widget.prefixIcon, size: 22, color: KeeplyTokens.muted),
                      const SizedBox(width: 10),
                    ],
                    Expanded(
                      child: Text(
                        widget.label,
                        style: Theme.of(context).textTheme.titleSmall?.copyWith(fontWeight: FontWeight.w600),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 8),
                if (!widget.enabled)
                  Text(
                    widget.emptyHint ?? 'Not available',
                    style: Theme.of(context).textTheme.bodySmall?.copyWith(color: KeeplyTokens.muted),
                  )
                else if (widget.options.isEmpty)
                  Text(
                    widget.emptyHint ?? 'No options',
                    style: Theme.of(context).textTheme.bodySmall?.copyWith(color: KeeplyTokens.muted),
                  )
                else
                  Container(
                    constraints: const BoxConstraints(maxHeight: 220),
                    decoration: BoxDecoration(
                      borderRadius: BorderRadius.circular(KeeplyTokens.radiusXs),
                      border: Border.all(color: borderColor),
                    ),
                    clipBehavior: Clip.antiAlias,
                    child: mode == ViewLayoutMode.list
                        ? ListView.separated(
                            shrinkWrap: true,
                            padding: const EdgeInsets.symmetric(vertical: 4),
                            itemCount: widget.options.length,
                            separatorBuilder: (_, __) => const Divider(height: 1),
                            itemBuilder: (context, i) {
                              final o = widget.options[i];
                              final selected = _equals(field.value, o.value);
                              return ListTile(
                                dense: true,
                                selected: selected,
                                selectedTileColor: KeeplyTokens.accentSoft,
                                title: Text(o.title, maxLines: 2, overflow: TextOverflow.ellipsis),
                                subtitle: o.subtitle != null
                                    ? Text(
                                        o.subtitle!,
                                        style: const TextStyle(color: KeeplyTokens.muted, fontSize: 12),
                                      )
                                    : null,
                                trailing: selected ? const Icon(Icons.check_circle, color: KeeplyTokens.accent, size: 20) : null,
                                onTap: () {
                                  field.didChange(o.value);
                                  widget.onChanged(o.value);
                                },
                              );
                            },
                          )
                        : GridView.builder(
                            shrinkWrap: true,
                            padding: const EdgeInsets.all(8),
                            physics: const ClampingScrollPhysics(),
                            gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                              crossAxisCount: 2,
                              mainAxisSpacing: 8,
                              crossAxisSpacing: 8,
                              childAspectRatio: 2.4,
                            ),
                            itemCount: widget.options.length,
                            itemBuilder: (context, i) {
                              final o = widget.options[i];
                              final selected = _equals(field.value, o.value);
                              return Material(
                                color: selected ? KeeplyTokens.accentSoft : KeeplyTokens.surfaceMuted,
                                borderRadius: BorderRadius.circular(KeeplyTokens.radiusXs),
                                child: InkWell(
                                  onTap: () {
                                    field.didChange(o.value);
                                    widget.onChanged(o.value);
                                  },
                                  borderRadius: BorderRadius.circular(KeeplyTokens.radiusXs),
                                  child: Container(
                                    padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 8),
                                    decoration: BoxDecoration(
                                      borderRadius: BorderRadius.circular(KeeplyTokens.radiusXs),
                                      border: Border.all(
                                        color: selected ? KeeplyTokens.accent : KeeplyTokens.line,
                                        width: selected ? 1.5 : 1,
                                      ),
                                    ),
                                    child: Column(
                                      crossAxisAlignment: CrossAxisAlignment.start,
                                      mainAxisAlignment: MainAxisAlignment.center,
                                      children: [
                                        Text(
                                          o.title,
                                          maxLines: 2,
                                          overflow: TextOverflow.ellipsis,
                                          style: Theme.of(context).textTheme.labelLarge?.copyWith(
                                                fontWeight: FontWeight.w600,
                                              ),
                                        ),
                                        if (o.subtitle != null)
                                          Text(
                                            o.subtitle!,
                                            maxLines: 1,
                                            overflow: TextOverflow.ellipsis,
                                            style: Theme.of(context).textTheme.labelSmall?.copyWith(
                                                  color: KeeplyTokens.muted,
                                                ),
                                          ),
                                      ],
                                    ),
                                  ),
                                ),
                              );
                            },
                          ),
                  ),
                if (field.hasError)
                  Padding(
                    padding: const EdgeInsets.only(top: 6, left: 12),
                    child: Text(
                      field.errorText!,
                      style: TextStyle(color: Theme.of(context).colorScheme.error, fontSize: 12),
                    ),
                  ),
              ],
            );
          },
        );
      },
    );
  }

  bool _equals(T? a, T b) {
    if (a == null) return false;
    return a == b;
  }
}
