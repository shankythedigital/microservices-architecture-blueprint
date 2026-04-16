import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:keeply_app/core/api/keeply_service_url.dart';
import 'package:keeply_app/core/preferences/keeply_app_preferences.dart';
import 'package:keeply_app/core/theme/keeply_tokens.dart';
import 'package:keeply_app/features/asset/data/models/asset_models.dart';

String keeplyAssetTitle(AssetMaster a) {
  return a.assetNameUdv.isNotEmpty ? a.assetNameUdv : 'Appliance ${a.assetId ?? ''}';
}

String? keeplyAssetCategoryLabel(AssetMaster a) {
  final n = a.category?['categoryName'] as String?;
  if (n == null || n.isEmpty) return null;
  return n;
}

String? keeplyNestedName(Map<String, dynamic>? m, String primaryKey) {
  if (m == null) return null;
  final v = m[primaryKey];
  if (v is String && v.trim().isNotEmpty) return v.trim();
  return null;
}

/// Same priority as React `assetListThumbnailUrl` (public URLs only — not document-id photos).
String? keeplyAssetListThumbnailUrl(AssetMaster a) {
  String? p(String? s) {
    final t = s?.trim();
    return (t != null && t.isNotEmpty) ? t : null;
  }

  return p(a.imageUrl) ??
      p(a.modelImageUrl) ??
      p(a.makeImageUrl) ??
      p(a.categoryImageUrl) ??
      p(a.subCategoryImageUrl);
}

String? keeplyResolveAssetServiceUrl(String? raw) {
  if (raw == null) return null;
  final t = raw.trim();
  if (t.isEmpty) return null;
  if (t.startsWith('http://') || t.startsWith('https://')) return t;
  final base = keeplyServiceBase(KeeplyApiService.asset);
  return t.startsWith('/') ? '$base$t' : '$base/$t';
}

String _formatShortDate(DateTime? d) {
  if (d == null) return '—';
  return '${d.year}-${d.month.toString().padLeft(2, '0')}-${d.day.toString().padLeft(2, '0')}';
}

bool _showThumbs(BuildContext context) {
  try {
    return KeeplyAppPrefsScope.of(context).showListThumbnails;
  } catch (_) {
    return true;
  }
}

class _KeeplyThumbBox extends StatelessWidget {
  const _KeeplyThumbBox({required this.url, this.size = 48, this.iconSize = 28});

  final String? url;
  final double size;
  final double iconSize;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    final resolved = keeplyResolveAssetServiceUrl(url);
    final border = scheme.outline.withValues(alpha: 0.35);
    if (resolved != null) {
      return ClipRRect(
        borderRadius: BorderRadius.circular(KeeplyTokens.radiusXs),
        child: SizedBox(
          width: size,
          height: size,
          child: CachedNetworkImage(
            imageUrl: resolved,
            fit: BoxFit.cover,
            memCacheWidth: (size * MediaQuery.devicePixelRatioOf(context)).round().clamp(48, 256),
            placeholder: (_, __) => ColoredBox(
              color: scheme.surfaceContainerHighest.withValues(alpha: 0.35),
              child: const Center(child: SizedBox(width: 18, height: 18, child: CircularProgressIndicator(strokeWidth: 2))),
            ),
            errorWidget: (_, __, ___) => _KeeplyThumbFallback(size: size, iconSize: iconSize, border: border),
          ),
        ),
      );
    }
    return _KeeplyThumbFallback(size: size, iconSize: iconSize, border: border);
  }
}

class _KeeplyThumbFallback extends StatelessWidget {
  const _KeeplyThumbFallback({required this.size, required this.iconSize, required this.border});

  final double size;
  final double iconSize;
  final Color border;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Container(
      width: size,
      height: size,
      decoration: BoxDecoration(
        color: scheme.surfaceContainerHighest.withValues(alpha: 0.4),
        borderRadius: BorderRadius.circular(KeeplyTokens.radiusXs),
        border: Border.all(color: border),
      ),
      child: Icon(Icons.kitchen_outlined, size: iconSize, color: scheme.onSurfaceVariant),
    );
  }
}

class KeeplyAssetListRow extends StatelessWidget {
  const KeeplyAssetListRow({super.key, required this.asset, this.onTap});

  final AssetMaster asset;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    final scheme = Theme.of(context).colorScheme;
    final title = keeplyAssetTitle(asset);
    final cat = keeplyAssetCategoryLabel(asset);
    final thumbs = _showThumbs(context);
    final thumbUrl = thumbs ? keeplyAssetListThumbnailUrl(asset) : null;
    return Padding(
      padding: const EdgeInsets.only(bottom: 10),
      child: Material(
        color: scheme.surface,
        borderRadius: BorderRadius.circular(KeeplyTokens.radiusXs),
        child: ListTile(
          contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(KeeplyTokens.radiusXs),
            side: BorderSide(color: scheme.outline.withValues(alpha: 0.28)),
          ),
          leading: _KeeplyThumbBox(url: thumbUrl, size: 52, iconSize: 28),
          title: Text(title, style: t.titleSmall?.copyWith(fontWeight: FontWeight.w600)),
          subtitle: cat != null ? Text(cat, style: t.bodySmall?.copyWith(color: scheme.onSurfaceVariant)) : null,
          trailing: Icon(Icons.chevron_right_rounded, color: scheme.onSurfaceVariant),
          onTap: onTap,
        ),
      ),
    );
  }
}

class KeeplyAssetGridCard extends StatelessWidget {
  const KeeplyAssetGridCard({super.key, required this.asset, this.onTap});

  final AssetMaster asset;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    final scheme = Theme.of(context).colorScheme;
    final title = keeplyAssetTitle(asset);
    final cat = keeplyAssetCategoryLabel(asset) ?? '';
    final thumbs = _showThumbs(context);
    final thumbUrl = thumbs ? keeplyAssetListThumbnailUrl(asset) : null;
    return Material(
      color: scheme.surface,
      borderRadius: BorderRadius.circular(KeeplyTokens.radiusSm),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(KeeplyTokens.radiusSm),
        child: Container(
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(KeeplyTokens.radiusSm),
            border: Border.all(color: scheme.outline.withValues(alpha: 0.28)),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Expanded(
                child: LayoutBuilder(
                  builder: (context, c) {
                    final side = c.biggest.shortestSide.clamp(56.0, 160.0);
                    return Center(
                      child: _KeeplyThumbBox(url: thumbUrl, size: side, iconSize: 32),
                    );
                  },
                ),
              ),
              const SizedBox(height: 8),
              Text(
                title,
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
                style: t.labelLarge?.copyWith(fontWeight: FontWeight.w600),
              ),
              if (cat.isNotEmpty) Text(cat, style: t.labelSmall?.copyWith(color: scheme.onSurfaceVariant)),
            ],
          ),
        ),
      ),
    );
  }
}

/// List row with full hierarchy labels (category → model) for browse-by-room style layouts.
class KeeplyAssetDetailListRow extends StatelessWidget {
  const KeeplyAssetDetailListRow({super.key, required this.asset, this.onTap});

  final AssetMaster asset;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    final scheme = Theme.of(context).colorScheme;
    final title = keeplyAssetTitle(asset);
    final sub = keeplyNestedName(asset.subCategory, 'subCategoryName');
    final make = keeplyNestedName(asset.make, 'makeName');
    final model = keeplyNestedName(asset.model, 'modelName');
    final cat = keeplyAssetCategoryLabel(asset);
    final thumbs = _showThumbs(context);
    final thumbUrl = thumbs ? keeplyAssetListThumbnailUrl(asset) : null;

    return Padding(
      padding: const EdgeInsets.only(bottom: 10),
      child: Card(
        clipBehavior: Clip.antiAlias,
        elevation: 0,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(KeeplyTokens.radiusSm),
          side: BorderSide(color: scheme.outline.withValues(alpha: 0.28)),
        ),
        child: InkWell(
          onTap: onTap,
          child: Padding(
            padding: const EdgeInsets.all(14),
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                _KeeplyThumbBox(url: thumbUrl, size: 56, iconSize: 30),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Expanded(
                            child: Text(
                              title,
                              style: t.titleSmall?.copyWith(fontWeight: FontWeight.w700),
                            ),
                          ),
                          if (asset.assetId != null)
                            Text(
                              '#${asset.assetId}',
                              style: t.labelSmall?.copyWith(color: scheme.onSurfaceVariant),
                            ),
                        ],
                      ),
                      const SizedBox(height: 10),
                      if (cat != null) _KeeplyDetailLine(label: 'Category', value: cat, t: t),
                      if (sub != null) _KeeplyDetailLine(label: 'Subcategory', value: sub, t: t),
                      if (make != null) _KeeplyDetailLine(label: 'Make', value: make, t: t),
                      if (model != null) _KeeplyDetailLine(label: 'Model', value: model, t: t),
                      _KeeplyDetailLine(
                        label: 'Status',
                        value: asset.active == false ? 'Inactive' : 'Active',
                        t: t,
                      ),
                      if (asset.createdAt != null)
                        _KeeplyDetailLine(
                          label: 'Added',
                          value: _formatShortDate(asset.createdAt),
                          t: t,
                        ),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

/// Full-width detail card for grid / “card” browse layouts.
class KeeplyAssetDetailCard extends StatelessWidget {
  const KeeplyAssetDetailCard({super.key, required this.asset, this.onTap});

  final AssetMaster asset;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    final scheme = Theme.of(context).colorScheme;
    final title = keeplyAssetTitle(asset);
    final sub = keeplyNestedName(asset.subCategory, 'subCategoryName');
    final make = keeplyNestedName(asset.make, 'makeName');
    final model = keeplyNestedName(asset.model, 'modelName');
    final cat = keeplyAssetCategoryLabel(asset);
    final thumbs = _showThumbs(context);
    final thumbUrl = thumbs ? keeplyAssetListThumbnailUrl(asset) : null;

    return Card(
      clipBehavior: Clip.antiAlias,
      elevation: 1,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(KeeplyTokens.radiusSm),
        side: BorderSide(color: scheme.outline.withValues(alpha: 0.28)),
      ),
      child: InkWell(
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _KeeplyThumbBox(url: thumbUrl, size: 56, iconSize: 28),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          title,
                          style: t.titleMedium?.copyWith(fontWeight: FontWeight.w700),
                        ),
                        if (asset.assetId != null)
                          Text(
                            'Asset ID ${asset.assetId}',
                            style: t.bodySmall?.copyWith(color: scheme.onSurfaceVariant),
                          ),
                      ],
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 14),
              Divider(height: 1, color: scheme.outline.withValues(alpha: 0.22)),
              const SizedBox(height: 10),
              if (cat != null) _KeeplyDetailLine(label: 'Category', value: cat, t: t),
              if (sub != null) _KeeplyDetailLine(label: 'Subcategory', value: sub, t: t),
              if (make != null) _KeeplyDetailLine(label: 'Make', value: make, t: t),
              if (model != null) _KeeplyDetailLine(label: 'Model', value: model, t: t),
              _KeeplyDetailLine(
                label: 'Status',
                value: asset.active == false ? 'Inactive' : 'Active',
                t: t,
              ),
              if (asset.createdAt != null)
                _KeeplyDetailLine(label: 'Added', value: _formatShortDate(asset.createdAt), t: t),
              if (asset.updatedAt != null && asset.updatedAt != asset.createdAt)
                _KeeplyDetailLine(label: 'Updated', value: _formatShortDate(asset.updatedAt), t: t),
            ],
          ),
        ),
      ),
    );
  }
}

class _KeeplyDetailLine extends StatelessWidget {
  const _KeeplyDetailLine({required this.label, required this.value, required this.t});

  final String label;
  final String value;
  final TextTheme t;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Padding(
      padding: const EdgeInsets.only(bottom: 6),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 96,
            child: Text(
              label,
              style: t.bodySmall?.copyWith(color: scheme.onSurfaceVariant, fontWeight: FontWeight.w500),
            ),
          ),
          Expanded(
            child: Text(value, style: t.bodySmall?.copyWith(color: scheme.onSurface, height: 1.35)),
          ),
        ],
      ),
    );
  }
}
