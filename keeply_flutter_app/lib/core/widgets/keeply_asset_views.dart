import 'package:flutter/material.dart';
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

String _formatShortDate(DateTime? d) {
  if (d == null) return '—';
  return '${d.year}-${d.month.toString().padLeft(2, '0')}-${d.day.toString().padLeft(2, '0')}';
}

class KeeplyAssetListRow extends StatelessWidget {
  const KeeplyAssetListRow({super.key, required this.asset, this.onTap});

  final AssetMaster asset;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    final title = keeplyAssetTitle(asset);
    final cat = keeplyAssetCategoryLabel(asset);
    return Padding(
      padding: const EdgeInsets.only(bottom: 10),
      child: Material(
        color: KeeplyTokens.surface,
        borderRadius: BorderRadius.circular(KeeplyTokens.radiusXs),
        child: ListTile(
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(KeeplyTokens.radiusXs),
            side: const BorderSide(color: KeeplyTokens.line),
          ),
          title: Text(title, style: t.titleSmall?.copyWith(fontWeight: FontWeight.w600)),
          subtitle: cat != null ? Text(cat, style: t.bodySmall?.copyWith(color: KeeplyTokens.muted)) : null,
          trailing: const Icon(Icons.chevron_right_rounded, color: KeeplyTokens.muted),
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
    final title = keeplyAssetTitle(asset);
    final cat = keeplyAssetCategoryLabel(asset) ?? '';
    return Material(
      color: KeeplyTokens.surface,
      borderRadius: BorderRadius.circular(KeeplyTokens.radiusSm),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(KeeplyTokens.radiusSm),
        child: Container(
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(KeeplyTokens.radiusSm),
            border: Border.all(color: KeeplyTokens.line),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Expanded(
                child: Container(
                  decoration: BoxDecoration(
                    color: KeeplyTokens.surfaceMuted,
                    borderRadius: BorderRadius.circular(KeeplyTokens.radiusXs),
                  ),
                  child: const Center(
                    child: Icon(Icons.kitchen_outlined, size: 40, color: KeeplyTokens.muted),
                  ),
                ),
              ),
              const SizedBox(height: 8),
              Text(
                title,
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
                style: t.labelLarge?.copyWith(fontWeight: FontWeight.w600),
              ),
              if (cat.isNotEmpty) Text(cat, style: t.labelSmall?.copyWith(color: KeeplyTokens.muted)),
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
    final title = keeplyAssetTitle(asset);
    final sub = keeplyNestedName(asset.subCategory, 'subCategoryName');
    final make = keeplyNestedName(asset.make, 'makeName');
    final model = keeplyNestedName(asset.model, 'modelName');
    final cat = keeplyAssetCategoryLabel(asset);

    return Padding(
      padding: const EdgeInsets.only(bottom: 10),
      child: Card(
        clipBehavior: Clip.antiAlias,
        elevation: 0,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(KeeplyTokens.radiusSm),
          side: const BorderSide(color: KeeplyTokens.line),
        ),
        child: InkWell(
          onTap: onTap,
          child: Padding(
            padding: const EdgeInsets.all(14),
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
                        style: t.labelSmall?.copyWith(color: KeeplyTokens.muted),
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
    final title = keeplyAssetTitle(asset);
    final sub = keeplyNestedName(asset.subCategory, 'subCategoryName');
    final make = keeplyNestedName(asset.make, 'makeName');
    final model = keeplyNestedName(asset.model, 'modelName');
    final cat = keeplyAssetCategoryLabel(asset);

    return Card(
      clipBehavior: Clip.antiAlias,
      elevation: 1,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(KeeplyTokens.radiusSm),
        side: const BorderSide(color: KeeplyTokens.line),
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
                  Container(
                    width: 48,
                    height: 48,
                    decoration: BoxDecoration(
                      color: KeeplyTokens.accentSoft,
                      borderRadius: BorderRadius.circular(KeeplyTokens.radiusXs),
                    ),
                    child: const Icon(Icons.inventory_2_outlined, color: KeeplyTokens.accentInk, size: 26),
                  ),
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
                            style: t.bodySmall?.copyWith(color: KeeplyTokens.muted),
                          ),
                      ],
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 14),
              const Divider(height: 1),
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
    return Padding(
      padding: const EdgeInsets.only(bottom: 6),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 96,
            child: Text(
              label,
              style: t.bodySmall?.copyWith(color: KeeplyTokens.muted, fontWeight: FontWeight.w500),
            ),
          ),
          Expanded(
            child: Text(value, style: t.bodySmall?.copyWith(color: KeeplyTokens.ink, height: 1.35)),
          ),
        ],
      ),
    );
  }
}
