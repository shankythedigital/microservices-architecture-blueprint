import 'package:flutter/material.dart';
import 'package:tutorial_coach_mark/tutorial_coach_mark.dart';

/// Increment from Settings “Replay home tour” so [DashboardPage] can show coach marks again.
final ValueNotifier<int> keeplyDashboardTourReplayBus = ValueNotifier<int>(0);

void requestKeeplyDashboardTourReplay() {
  keeplyDashboardTourReplayBus.value++;
}

/// Short hands-on tour for the main dashboard (first open per install).
void startDashboardHandsonTour(
  BuildContext context, {
  required GlobalKey heroKey,
  required GlobalKey ctaKey,
  required GlobalKey remindersKey,
  required VoidCallback onFinished,
}) {
  final skipStyle = TextStyle(color: Colors.white.withValues(alpha: 0.92));

  TutorialCoachMark(
    targets: [
      TargetFocus(
        identify: 'hero',
        keyTarget: heroKey,
        shape: ShapeLightFocus.RRect,
        radius: 14,
        contents: [
          TargetContent(
            align: ContentAlign.bottom,
            child: _tourCopy(
              'Welcome to Keeply',
              'This area highlights your session. Pull down anywhere on the dashboard to refresh counts and lists.',
            ),
          ),
        ],
      ),
      TargetFocus(
        identify: 'cta',
        keyTarget: ctaKey,
        shape: ShapeLightFocus.RRect,
        radius: 14,
        contents: [
          TargetContent(
            align: ContentAlign.bottom,
            child: _tourCopy(
              'Add an appliance',
              'Capture warranty dates, invoice, and optional photo in one guided flow.',
            ),
          ),
        ],
      ),
      TargetFocus(
        identify: 'reminders',
        keyTarget: remindersKey,
        shape: ShapeLightFocus.RRect,
        radius: 14,
        contents: [
          TargetContent(
            align: ContentAlign.top,
            child: _tourCopy(
              'Stay ahead of issues',
              'Expiring coverage, alerts, and open tickets show up here so nothing slips.',
            ),
          ),
        ],
      ),
    ],
    colorShadow: Colors.black,
    opacityShadow: 0.88,
    textSkip: 'SKIP',
    textStyleSkip: skipStyle,
    onFinish: onFinished,
    onSkip: () {
      onFinished();
      return true;
    },
  ).show(context: context);
}

Widget _tourCopy(String title, String body) {
  return ConstrainedBox(
    constraints: const BoxConstraints(maxWidth: 320),
    child: Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      mainAxisSize: MainAxisSize.min,
      children: [
        Text(title, style: const TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.w800)),
        const SizedBox(height: 8),
        Text(body, style: const TextStyle(color: Colors.white, fontSize: 15, height: 1.35)),
      ],
    ),
  );
}
