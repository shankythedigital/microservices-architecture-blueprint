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
