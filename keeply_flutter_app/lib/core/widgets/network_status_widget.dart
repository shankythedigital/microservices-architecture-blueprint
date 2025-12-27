import 'package:flutter/material.dart';
import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:keeply_app/core/utils/connectivity_helper.dart';

/// Network Status Widget
/// Shows network connectivity status
class NetworkStatusWidget extends StatefulWidget {
  const NetworkStatusWidget({super.key});

  @override
  State<NetworkStatusWidget> createState() => _NetworkStatusWidgetState();
}

class _NetworkStatusWidgetState extends State<NetworkStatusWidget> {
  final ConnectivityHelper _connectivityHelper = ConnectivityHelper();
  ConnectivityResult _connectivityResult = ConnectivityResult.none;

  @override
  void initState() {
    super.initState();
    _checkConnectivity();
    _connectivityHelper.connectivityStream.listen((result) {
      if (mounted) {
        setState(() => _connectivityResult = result);
      }
    });
  }

  Future<void> _checkConnectivity() async {
    final result = await _connectivityHelper.getConnectivityStatus();
    if (mounted) {
      setState(() => _connectivityResult = result);
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_connectivityResult == ConnectivityResult.none) {
      return Container(
        width: double.infinity,
        padding: const EdgeInsets.symmetric(vertical: 8, horizontal: 16),
        color: Colors.red,
        child: const Row(
          children: [
            Icon(Icons.wifi_off, color: Colors.white, size: 20),
            SizedBox(width: 8),
            Text(
              'No Internet Connection',
              style: TextStyle(color: Colors.white),
            ),
          ],
        ),
      );
    }

    return const SizedBox.shrink();
  }
}

