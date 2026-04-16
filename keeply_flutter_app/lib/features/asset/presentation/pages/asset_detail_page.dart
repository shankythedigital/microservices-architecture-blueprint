import 'package:flutter/material.dart';
import 'package:keeply_app/core/exceptions/api_exception.dart';
import 'package:keeply_app/core/theme/keeply_tokens.dart';
import 'package:keeply_app/features/asset/data/datasources/asset_remote_datasource.dart';
import 'package:keeply_app/features/asset/data/models/asset_models.dart';

/// `GET /api/asset/v1/assets/{id}` — opened from appliance lists and reminders.
class AssetDetailPage extends StatefulWidget {
  const AssetDetailPage({super.key, required this.assetId});

  final int assetId;

  /// Opens detail when [assetId] is a positive server id; otherwise shows a short message.
  static void pushIfValid(BuildContext context, int? assetId) {
    if (assetId == null || assetId <= 0) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Details are not available for this item.')),
      );
      return;
    }
    Navigator.of(context).push<void>(
      MaterialPageRoute<void>(builder: (_) => AssetDetailPage(assetId: assetId)),
    );
  }

  @override
  State<AssetDetailPage> createState() => _AssetDetailPageState();
}

class _AssetDetailPageState extends State<AssetDetailPage> {
  final _ds = AssetRemoteDataSource();
  bool _loading = true;
  String? _err;
  AssetMaster? _asset;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _err = null;
    });
    try {
      final a = await _ds.getAssetById(widget.assetId);
      if (!mounted) return;
      setState(() {
        _asset = a;
        _loading = false;
      });
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _loading = false;
        _err = e is ApiException ? e.userMessage : 'Could not load asset';
      });
    }
  }

  String? _mapName(Map<String, dynamic>? m, String key) {
    if (m == null) return null;
    final v = m[key];
    return v is String && v.trim().isNotEmpty ? v.trim() : null;
  }

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    return Scaffold(
      appBar: AppBar(title: Text('Asset #${widget.assetId}')),
      body: RefreshIndicator(
        onRefresh: _load,
        child: ListView(
          padding: const EdgeInsets.all(18),
          physics: const AlwaysScrollableScrollPhysics(),
          children: [
            if (_loading)
              const Padding(
                padding: EdgeInsets.all(32),
                child: Center(child: CircularProgressIndicator(strokeWidth: 2)),
              )
            else if (_err != null)
              Text(_err!, style: t.bodyMedium?.copyWith(color: KeeplyTokens.danger))
            else if (_asset != null) ...[
              Text(
                _asset!.assetNameUdv,
                style: t.titleLarge?.copyWith(fontWeight: FontWeight.w800),
              ),
              const SizedBox(height: 16),
              _row(t, 'Category', _mapName(_asset!.category, 'categoryName')),
              _row(t, 'Subcategory', _mapName(_asset!.subCategory, 'subCategoryName')),
              _row(t, 'Make', _mapName(_asset!.make, 'makeName')),
              _row(t, 'Model', _mapName(_asset!.model, 'modelName')),
              if (_asset!.active != null)
                _row(t, 'Active', _asset!.active! ? 'Yes' : 'No'),
              if (_asset!.createdAt != null)
                _row(t, 'Created', _asset!.createdAt!.toLocal().toString().split('.').first),
            ],
          ],
        ),
      ),
    );
  }

  Widget _row(TextTheme t, String label, String? value) {
    if (value == null || value.isEmpty) return const SizedBox.shrink();
    return Padding(
      padding: const EdgeInsets.only(bottom: 10),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 110,
            child: Text(label, style: t.labelLarge?.copyWith(color: KeeplyTokens.muted)),
          ),
          Expanded(child: Text(value, style: t.bodyLarge)),
        ],
      ),
    );
  }
}
