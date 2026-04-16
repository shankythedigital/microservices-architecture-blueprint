import 'dart:io';
import 'dart:typed_data';

import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:keeply_app/core/api/keeply_documents_api.dart';
import 'package:keeply_app/core/exceptions/api_exception.dart';
import 'package:keeply_app/core/theme/keeply_tokens.dart';
import 'package:keeply_app/core/widgets/keeply_asset_views.dart';
import 'package:keeply_app/features/asset/data/datasources/asset_remote_datasource.dart';
import 'package:keeply_app/features/asset/data/models/asset_models.dart';
import 'package:open_filex/open_filex.dart';

/// `GET /api/asset/v1/assets/{id}` — photo, hierarchy, and downloadable documents.
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
  final _docsApi = KeeplyDocumentsRemoteApi();
  bool _loading = true;
  String? _err;
  AssetMaster? _asset;
  Uint8List? _photoBytes;
  bool _photoBusy = false;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _err = null;
      _photoBytes = null;
    });
    try {
      final a = await _ds.getAssetById(widget.assetId);
      if (!mounted) return;
      setState(() {
        _asset = a;
        _loading = false;
      });
      await _maybeLoadAuthPhoto(a);
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _loading = false;
        _err = e is ApiException ? e.userMessage : 'Could not load asset';
      });
    }
  }

  Future<void> _maybeLoadAuthPhoto(AssetMaster a) async {
    final hasUrl = keeplyResolveAssetServiceUrl(keeplyAssetListThumbnailUrl(a)) != null;
    if (hasUrl) return;
    final id = a.assetPhotoDocumentId;
    if (id == null || id <= 0) return;
    setState(() => _photoBusy = true);
    try {
      final r = await _docsApi.fetchDocumentBytes(id);
      final ct = r.contentType?.toLowerCase() ?? '';
      if (!ct.contains('image') && !ct.contains('octet-stream')) {
        if (!mounted) return;
        setState(() => _photoBusy = false);
        return;
      }
      if (!mounted) return;
      setState(() {
        _photoBytes = Uint8List.fromList(r.bytes);
        _photoBusy = false;
      });
    } catch (_) {
      if (!mounted) return;
      setState(() => _photoBusy = false);
    }
  }

  String? _mapName(Map<String, dynamic>? m, String key) {
    if (m == null) return null;
    final v = m[key];
    return v is String && v.trim().isNotEmpty ? v.trim() : null;
  }

  List<({int id, String label})> _documentActions(AssetMaster a) {
    final out = <({int id, String label})>[];
    void add(int? id, String label) {
      if (id == null || id <= 0) return;
      if (out.any((e) => e.id == id)) return;
      out.add((id: id, label: label));
    }

    add(a.warrantyDocumentId, 'Warranty document');
    add(a.amcDocumentId, 'AMC document');
    for (final d in a.documents) {
      add(d.documentId, d.fileName ?? d.docType ?? 'Document');
    }
    return out;
  }

  String _extFromContentType(String? ct) {
    final c = ct?.toLowerCase() ?? '';
    if (c.contains('pdf')) return '.pdf';
    if (c.contains('jpeg')) return '.jpg';
    if (c.contains('jpg')) return '.jpg';
    if (c.contains('png')) return '.png';
    if (c.contains('webp')) return '.webp';
    return '';
  }

  Future<void> _openDocument(int documentId) async {
    try {
      final r = await _docsApi.fetchDocumentBytes(documentId);
      final dir = Directory.systemTemp;
      final ext = _extFromContentType(r.contentType);
      final f = File('${dir.path}/keeply_doc_${documentId}_${DateTime.now().millisecondsSinceEpoch}$ext');
      await f.writeAsBytes(r.bytes, flush: true);
      await OpenFilex.open(f.path);
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(e is ApiException ? e.userMessage : 'Could not open document')),
      );
    }
  }

  Widget _hero(BuildContext context, AssetMaster a) {
    final scheme = Theme.of(context).colorScheme;
    final url = keeplyResolveAssetServiceUrl(keeplyAssetListThumbnailUrl(a));
    if (_photoBusy && url == null && _photoBytes == null) {
      return SizedBox(
        height: 200,
        child: ColoredBox(
          color: scheme.surfaceContainerHighest.withValues(alpha: 0.35),
          child: const Center(child: CircularProgressIndicator(strokeWidth: 2)),
        ),
      );
    }
    if (_photoBytes != null) {
      return ClipRRect(
        borderRadius: BorderRadius.circular(KeeplyTokens.radiusSm),
        child: Image.memory(
          _photoBytes!,
          height: 220,
          width: double.infinity,
          fit: BoxFit.cover,
        ),
      );
    }
    if (url != null) {
      return ClipRRect(
        borderRadius: BorderRadius.circular(KeeplyTokens.radiusSm),
        child: CachedNetworkImage(
          imageUrl: url,
          height: 220,
          width: double.infinity,
          fit: BoxFit.cover,
          memCacheWidth: (220 * MediaQuery.devicePixelRatioOf(context)).round().clamp(120, 800),
          placeholder: (_, __) => SizedBox(
            height: 220,
            child: ColoredBox(
              color: scheme.surfaceContainerHighest.withValues(alpha: 0.35),
              child: const Center(child: CircularProgressIndicator(strokeWidth: 2)),
            ),
          ),
          errorWidget: (_, __, ___) => _heroPlaceholder(context),
        ),
      );
    }
    return _heroPlaceholder(context);
  }

  Widget _heroPlaceholder(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Container(
      height: 160,
      width: double.infinity,
      alignment: Alignment.center,
      decoration: BoxDecoration(
        color: scheme.surfaceContainerHighest.withValues(alpha: 0.45),
        borderRadius: BorderRadius.circular(KeeplyTokens.radiusSm),
        border: Border.all(color: scheme.outline.withValues(alpha: 0.25)),
      ),
      child: Icon(Icons.photo_camera_outlined, size: 48, color: scheme.onSurfaceVariant),
    );
  }

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    final scheme = Theme.of(context).colorScheme;
    final title = _asset != null ? keeplyAssetTitle(_asset!) : 'Asset';
    return Scaffold(
      appBar: AppBar(title: Text(title)),
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
              Text(_err!, style: t.bodyMedium?.copyWith(color: scheme.error))
            else if (_asset != null) ...[
              _hero(context, _asset!),
              const SizedBox(height: 18),
              Text(
                keeplyAssetTitle(_asset!),
                style: t.titleLarge?.copyWith(fontWeight: FontWeight.w800),
              ),
              const SizedBox(height: 16),
              if (_asset!.serialNumber != null && _asset!.serialNumber!.isNotEmpty)
                _row(t, scheme, 'Serial', _asset!.serialNumber),
              if (_asset!.purchaseDate != null && _asset!.purchaseDate!.isNotEmpty)
                _row(t, scheme, 'Purchase', _asset!.purchaseDate),
              _row(t, scheme, 'Category', _mapName(_asset!.category, 'categoryName')),
              _row(t, scheme, 'Subcategory', _mapName(_asset!.subCategory, 'subCategoryName')),
              _row(t, scheme, 'Make', _mapName(_asset!.make, 'makeName')),
              _row(t, scheme, 'Model', _mapName(_asset!.model, 'modelName')),
              if (_asset!.active != null)
                _row(t, scheme, 'Active', _asset!.active! ? 'Yes' : 'No'),
              if (_asset!.createdAt != null)
                _row(t, scheme, 'Created', _asset!.createdAt!.toLocal().toString().split('.').first),
              if (_documentActions(_asset!).isNotEmpty) ...[
                const SizedBox(height: 20),
                Text('Documents', style: t.titleMedium?.copyWith(fontWeight: FontWeight.w700)),
                const SizedBox(height: 8),
                Text(
                  'Tap to download and open in your viewer (PDF, images, etc.).',
                  style: t.bodySmall?.copyWith(color: scheme.onSurfaceVariant, height: 1.4),
                ),
                const SizedBox(height: 10),
                for (final d in _documentActions(_asset!))
                  Padding(
                    padding: const EdgeInsets.only(bottom: 8),
                    child: OutlinedButton.icon(
                      onPressed: () => _openDocument(d.id),
                      icon: const Icon(Icons.open_in_new_rounded, size: 20),
                      label: Text(d.label, textAlign: TextAlign.start),
                    ),
                  ),
              ],
            ],
          ],
        ),
      ),
    );
  }

  Widget _row(TextTheme t, ColorScheme scheme, String label, String? value) {
    if (value == null || value.isEmpty) return const SizedBox.shrink();
    return Padding(
      padding: const EdgeInsets.only(bottom: 10),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 110,
            child: Text(label, style: t.labelLarge?.copyWith(color: scheme.onSurfaceVariant)),
          ),
          Expanded(child: Text(value, style: t.bodyLarge)),
        ],
      ),
    );
  }
}
