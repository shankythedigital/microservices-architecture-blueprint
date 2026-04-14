import 'dart:async';
import 'package:flutter/foundation.dart' show kIsWeb;
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:mobile_scanner/mobile_scanner.dart';
import 'package:keeply_app/core/utils/permission_helper.dart';
import 'package:keeply_app/features/asset/data/models/asset_models.dart';
import 'package:keeply_app/features/asset/presentation/bloc/asset_bloc.dart';

/// Asset Scan Page
/// Scan barcode/QR code and display asset/product information
class AssetScanPage extends StatefulWidget {
  const AssetScanPage({super.key});

  @override
  State<AssetScanPage> createState() => _AssetScanPageState();
}

class _AssetScanPageState extends State<AssetScanPage> {
  final TextEditingController _manualCodeController = TextEditingController();
  MobileScannerController? _scannerController;
  bool _isScanning = true;
  String? _lastScannedValue;
  Timer? _scanDebounce;

  @override
  void initState() {
    super.initState();
    if (!kIsWeb) {
      _scannerController = MobileScannerController(
        detectionSpeed: DetectionSpeed.normal,
        detectionTimeoutMs: 500,
        facing: CameraFacing.back,
      );
    }
  }

  @override
  void dispose() {
    _manualCodeController.dispose();
    _scannerController?.dispose();
    _scanDebounce?.cancel();
    super.dispose();
  }

  void _onBarcodeDetected(BarcodeCapture capture) {
    if (!_isScanning) return;
    final barcodes = capture.barcodes;
    if (barcodes.isEmpty) return;

    final barcode = barcodes.first;
    final value = barcode.rawValue;
    if (value == null || value.trim().isEmpty) return;

    // Debounce rapid scans
    if (_lastScannedValue == value) return;
    _lastScannedValue = value;

    _scanDebounce?.cancel();
    _scanDebounce = Timer(const Duration(milliseconds: 800), () {
      _lastScannedValue = null;
    });

    setState(() => _isScanning = false);
    context.read<AssetBloc>().add(ScanAssetEvent(value.trim()));
  }

  void _onManualScan() {
    final value = _manualCodeController.text.trim();
    if (value.isEmpty) return;

    setState(() => _isScanning = false);
    context.read<AssetBloc>().add(ScanAssetEvent(value));
  }

  void _resetScan() {
    setState(() {
      _isScanning = true;
      _lastScannedValue = null;
    });
    context.read<AssetBloc>().add(ResetScanEvent());
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Scan Asset'),
        actions: [
          if (!_isScanning)
            IconButton(
              icon: const Icon(Icons.qr_code_scanner),
              onPressed: _resetScan,
              tooltip: 'Scan again',
            ),
        ],
      ),
      body: BlocConsumer<AssetBloc, AssetState>(
        listenWhen: (prev, curr) =>
            curr is AssetScanLoaded || curr is AssetScanNotFound || curr is AssetError,
        listener: (context, state) {
          if (state is AssetScanNotFound) {
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(
                content: Text(state.message),
                backgroundColor: Colors.orange,
              ),
            );
          }
        },
        buildWhen: (prev, curr) =>
            curr is AssetScanLoading ||
            curr is AssetScanLoaded ||
            curr is AssetScanNotFound ||
            curr is AssetInitial ||
            curr is AssetError,
        builder: (context, state) {
          if (state is AssetScanLoaded) {
            return _buildResultView(context, state.result);
          }

          return SingleChildScrollView(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                // Scanner (mobile only)
                if (!kIsWeb && _scannerController != null) ...[
                  _buildScannerSection(context),
                  const SizedBox(height: 24),
                ],
                // Manual entry
                _buildManualEntrySection(context),
                if (state is AssetScanLoading) ...[
                  const SizedBox(height: 24),
                  const Center(
                    child: Column(
                      children: [
                        CircularProgressIndicator(),
                        SizedBox(height: 16),
                        Text('Looking up asset...'),
                      ],
                    ),
                  ),
                ],
              ],
            ),
          );
        },
      ),
    );
  }

  Widget _buildScannerSection(BuildContext context) {
    return FutureBuilder<bool>(
      future: PermissionHelper.requestCameraPermission(),
      builder: (context, snapshot) {
        if (snapshot.data != true) {
          return Card(
            child: Padding(
              padding: const EdgeInsets.all(24),
              child: Column(
                children: [
                  Icon(Icons.camera_alt_outlined, size: 48, color: Colors.grey[400]),
                  const SizedBox(height: 16),
                  Text(
                    'Camera permission required for scanning',
                    textAlign: TextAlign.center,
                    style: Theme.of(context).textTheme.bodyMedium,
                  ),
                  const SizedBox(height: 16),
                  FilledButton.icon(
                    onPressed: () async {
                      final granted = await PermissionHelper.requestCameraPermission();
                      if (granted && mounted) setState(() {});
                    },
                    icon: const Icon(Icons.settings),
                    label: const Text('Grant Permission'),
                  ),
                ],
              ),
            ),
          );
        }

        return ClipRRect(
          borderRadius: BorderRadius.circular(12),
          child: SizedBox(
            height: 280,
            child: Stack(
              children: [
                MobileScanner(
                  controller: _scannerController!,
                  onDetect: _onBarcodeDetected,
                ),
                // Scan overlay
                Center(
                  child: Container(
                    width: 240,
                    height: 240,
                    decoration: BoxDecoration(
                      border: Border.all(color: Colors.white54, width: 2),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: const Center(
                      child: Icon(
                        Icons.qr_code_scanner,
                        size: 64,
                        color: Colors.white54,
                      ),
                    ),
                  ),
                ),
              ],
            ),
          ),
        );
      },
    );
  }

  Widget _buildManualEntrySection(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Or enter code manually',
              style: Theme.of(context).textTheme.titleMedium,
            ),
            const SizedBox(height: 12),
            TextField(
              controller: _manualCodeController,
              decoration: const InputDecoration(
                hintText: 'Enter barcode, QR code, or asset ID (e.g. AST-LAP-001)',
                border: OutlineInputBorder(),
                prefixIcon: Icon(Icons.tag),
              ),
              textInputAction: TextInputAction.go,
              onSubmitted: (_) => _onManualScan(),
            ),
            const SizedBox(height: 12),
            SizedBox(
              width: double.infinity,
              child: FilledButton.icon(
                onPressed: _onManualScan,
                icon: const Icon(Icons.search),
                label: const Text('Look Up'),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildResultView(BuildContext context, AssetScanResult result) {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          // Success header
          Card(
            color: Colors.green.shade50,
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Row(
                children: [
                  Icon(Icons.check_circle, color: Colors.green.shade700, size: 48),
                  const SizedBox(width: 16),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'Asset Found',
                          style: Theme.of(context).textTheme.titleLarge?.copyWith(
                                fontWeight: FontWeight.bold,
                                color: Colors.green.shade800,
                              ),
                        ),
                        Text(
                          'Source: ${result.source}',
                          style: Theme.of(context).textTheme.bodySmall?.copyWith(
                                color: Colors.green.shade700,
                              ),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 24),
          // Result in required format
          Text(
            'Asset Details',
            style: Theme.of(context).textTheme.titleMedium?.copyWith(
                  fontWeight: FontWeight.bold,
                ),
          ),
          const SizedBox(height: 12),
          _buildResultCard(context, result),
          const SizedBox(height: 24),
          // JSON preview
          Text(
            'Response Format',
            style: Theme.of(context).textTheme.titleMedium?.copyWith(
                  fontWeight: FontWeight.bold,
                ),
          ),
          const SizedBox(height: 8),
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: Colors.grey.shade100,
              borderRadius: BorderRadius.circular(8),
              border: Border.all(color: Colors.grey.shade300),
            ),
            child: SelectableText(
              _formatJson(result.toJson()),
              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                    fontFamily: 'monospace',
                  ),
            ),
          ),
          const SizedBox(height: 24),
          Row(
            children: [
              Expanded(
                child: OutlinedButton.icon(
                  onPressed: _resetScan,
                  icon: const Icon(Icons.qr_code_scanner),
                  label: const Text('Scan Again'),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: FilledButton.icon(
                  onPressed: _resetScan,
                  icon: const Icon(Icons.refresh),
                  label: const Text('New Scan'),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildResultCard(BuildContext context, AssetScanResult result) {
    final items = [
      _ResultItem(label: 'Asset Code', value: result.assetCode, icon: Icons.tag),
      _ResultItem(label: 'Product Name', value: result.productName, icon: Icons.inventory_2),
      _ResultItem(label: 'Category', value: result.category, icon: Icons.category),
      _ResultItem(label: 'Subcategory', value: result.subcategory, icon: Icons.subdirectory_arrow_right),
      _ResultItem(label: 'Status', value: result.status, icon: Icons.check_circle_outline),
      _ResultItem(label: 'Source', value: result.source, icon: Icons.source),
    ];

    return Card(
      child: Column(
        children: items
            .map(
              (item) => ListTile(
                leading: Icon(item.icon, color: Theme.of(context).primaryColor),
                title: Text(
                  item.label,
                  style: Theme.of(context).textTheme.bodySmall?.copyWith(
                        color: Colors.grey[600],
                      ),
                ),
                subtitle: Text(
                  item.value,
                  style: Theme.of(context).textTheme.titleMedium,
                ),
              ),
            )
            .toList(),
      ),
    );
  }

  String _formatJson(Map<String, dynamic> json) {
    final buffer = StringBuffer();
    buffer.writeln('{');
    json.forEach((key, value) {
      buffer.writeln('  "$key": "$value",');
    });
    final str = buffer.toString();
    return str.endsWith(',\n')
        ? '${str.substring(0, str.length - 2)}\n}'
        : '$str}';
  }
}

class _ResultItem {
  final String label;
  final String value;
  final IconData icon;

  _ResultItem({required this.label, required this.value, required this.icon});
}
