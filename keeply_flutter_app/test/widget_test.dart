import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:keeply_app/core/network/dio_error_util.dart';
import 'package:keeply_app/main.dart';

void main() {
  setUpAll(() {
    // Matches production [ApiClient.initialize]; avoids `DioException [unknown]: null` in test logs.
    installKeeplyDioExceptionReadableString();
  });

  testWidgets('KeeplyApp builds MaterialApp', (WidgetTester tester) async {
    WidgetsFlutterBinding.ensureInitialized();
    await tester.pumpWidget(const KeeplyApp());
    await tester.pump();
    expect(find.byType(MaterialApp), findsOneWidget);
  });
}
