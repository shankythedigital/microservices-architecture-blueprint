import 'package:flutter/material.dart';

/// Global list vs card layout for selectable fields and asset grids.
enum ViewLayoutMode {
  list,
  card,
}

/// Provides [ValueNotifier] for [ViewLayoutMode]; dependents rebuild when it changes.
class ViewLayoutScope extends InheritedNotifier<ValueNotifier<ViewLayoutMode>> {
  const ViewLayoutScope({
    super.key,
    required ValueNotifier<ViewLayoutMode> notifier,
    required super.child,
  }) : super(notifier: notifier);

  static ValueNotifier<ViewLayoutMode> notifierOf(BuildContext context) {
    final scope = context.dependOnInheritedWidgetOfExactType<ViewLayoutScope>();
    assert(scope != null, 'ViewLayoutScope not found — wrap the app with ViewLayoutScope');
    return scope!.notifier!;
  }

  static ViewLayoutMode modeOf(BuildContext context) => notifierOf(context).value;
}

/// Segmented control bound to [ViewLayoutScope].
class ViewLayoutToggle extends StatelessWidget {
  const ViewLayoutToggle({super.key, this.compact = false});

  final bool compact;

  @override
  Widget build(BuildContext context) {
    final nn = ViewLayoutScope.notifierOf(context);
    final style = SegmentedButton.styleFrom(
      visualDensity: compact ? VisualDensity.compact : VisualDensity.standard,
      tapTargetSize: compact ? MaterialTapTargetSize.shrinkWrap : MaterialTapTargetSize.padded,
      padding: EdgeInsets.symmetric(horizontal: compact ? 6 : 12, vertical: compact ? 4 : 8),
    );
    if (compact) {
      return SegmentedButton<ViewLayoutMode>(
        style: style,
        showSelectedIcon: false,
        segments: const [
          ButtonSegment<ViewLayoutMode>(
            value: ViewLayoutMode.list,
            tooltip: 'List view',
            icon: Icon(Icons.view_list_rounded, size: 18),
          ),
          ButtonSegment<ViewLayoutMode>(
            value: ViewLayoutMode.card,
            tooltip: 'Card view',
            icon: Icon(Icons.grid_view_rounded, size: 18),
          ),
        ],
        selected: {nn.value},
        onSelectionChanged: (s) {
          if (s.isEmpty) return;
          nn.value = s.first;
        },
      );
    }
    return SegmentedButton<ViewLayoutMode>(
      style: style,
      showSelectedIcon: false,
      segments: const [
        ButtonSegment<ViewLayoutMode>(
          value: ViewLayoutMode.list,
          label: Text('List'),
          icon: Icon(Icons.view_list_rounded, size: 18),
        ),
        ButtonSegment<ViewLayoutMode>(
          value: ViewLayoutMode.card,
          label: Text('Cards'),
          icon: Icon(Icons.grid_view_rounded, size: 18),
        ),
      ],
      selected: {nn.value},
      onSelectionChanged: (s) {
        if (s.isEmpty) return;
        nn.value = s.first;
      },
    );
  }
}
