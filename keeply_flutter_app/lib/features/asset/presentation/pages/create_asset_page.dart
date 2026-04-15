import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:keeply_app/core/api/keeply_api_models.dart';
import 'package:keeply_app/core/api/keeply_categories_api.dart';
import 'package:keeply_app/core/api/keeply_master_data_api.dart';
import 'package:keeply_app/core/theme/keeply_tokens.dart';
import 'package:keeply_app/core/utils/validation_helper.dart';
import 'package:keeply_app/core/view_layout/view_layout_scope.dart';
import 'package:keeply_app/core/widgets/loading_widget.dart';
import 'package:keeply_app/core/widgets/selectable_option_picker.dart';
import 'package:keeply_app/features/asset/data/models/asset_models.dart';
import 'package:keeply_app/features/asset/presentation/bloc/asset_bloc.dart';
import 'package:keeply_app/features/auth/presentation/bloc/auth_bloc.dart';

/// Create Asset Page
/// Form for creating new assets with comprehensive validation
class CreateAssetPage extends StatefulWidget {
  const CreateAssetPage({super.key});

  @override
  State<CreateAssetPage> createState() => _CreateAssetPageState();
}

class _CreateAssetPageState extends State<CreateAssetPage> {
  final _formKey = GlobalKey<FormState>();
  final _assetNameController = TextEditingController();
  final KeeplyMasterDataApi _masterApi = KeeplyMasterDataApi();
  final KeeplyCategoriesApi _categoriesApi = KeeplyCategoriesApi();

  int? _selectedCategoryId;
  int? _selectedSubCategoryId;
  int? _selectedMakeId;
  int? _selectedModelId;

  List<Category> _categories = [];
  List<SubCategoryDto> _allSubCategories = [];
  List<MakeDto> _allMakes = [];
  List<ModelDto> _allModels = [];
  bool _categoriesLoading = true;
  String? _categoriesError;
  bool _masterLoading = true;
  String? _masterError;

  Future<void> _loadCategories() async {
    setState(() {
      _categoriesLoading = true;
      _categoriesError = null;
    });
    try {
      final dtos = await _categoriesApi.listCategories();
      if (!mounted) return;
      final list = dtos
          .where(
            (d) =>
                d.categoryId != null &&
                d.categoryName != null &&
                d.categoryName!.trim().isNotEmpty,
          )
          .map(
            (d) => Category(
              categoryId: d.categoryId,
              categoryName: d.categoryName!.trim(),
              description: d.description,
              active: null,
            ),
          )
          .toList();
      list.sort((a, b) => a.categoryName.compareTo(b.categoryName));
      setState(() {
        _categories = list;
        _categoriesLoading = false;
        if (!_masterLoading && _masterError == null) {
          _fillCascadeDefaults();
        }
      });
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _categoriesLoading = false;
        _categoriesError = '$e';
      });
    }
  }

  List<SubCategoryDto> get _subcategoriesForCategory {
    final cid = _selectedCategoryId;
    if (cid == null) return [];
    final rows = _allSubCategories
        .where((s) => s.subCategoryId != null && s.categoryId == cid)
        .toList();
    rows.sort((a, b) => (a.subCategoryName ?? '').compareTo(b.subCategoryName ?? ''));
    return rows;
  }

  List<MakeDto> get _makesForSubcategory {
    final sid = _selectedSubCategoryId;
    if (sid == null) return [];
    final rows = _allMakes.where((m) => m.makeId != null && m.subCategoryId == sid).toList();
    rows.sort((a, b) => (a.makeName ?? '').compareTo(b.makeName ?? ''));
    return rows;
  }

  List<ModelDto> get _modelsForMake {
    final mid = _selectedMakeId;
    if (mid == null) return [];
    final rows = _allModels.where((m) => m.modelId != null && m.makeId == mid).toList();
    rows.sort((a, b) => (a.modelName ?? '').compareTo(b.modelName ?? ''));
    return rows;
  }

  /// When a parent id is set and master data lists are available, pick the first
  /// valid child at each level so Category → Subcategory → Make → Model stays aligned.
  void _fillCascadeDefaults() {
    if (_selectedCategoryId != null && _selectedSubCategoryId == null) {
      final subs = _subcategoriesForCategory;
      if (subs.isNotEmpty) {
        _selectedSubCategoryId = subs.first.subCategoryId;
      }
    }
    if (_selectedSubCategoryId != null && _selectedMakeId == null) {
      final ms = _makesForSubcategory;
      if (ms.isNotEmpty) {
        _selectedMakeId = ms.first.makeId;
      }
    }
    if (_selectedMakeId != null && _selectedModelId == null) {
      final mods = _modelsForMake;
      if (mods.isNotEmpty) {
        _selectedModelId = mods.first.modelId;
      }
    }
  }

  @override
  void initState() {
    super.initState();
    _loadCategories();
    _loadMasterData();
  }

  Future<void> _loadMasterData() async {
    setState(() {
      _masterLoading = true;
      _masterError = null;
    });
    try {
      final results = await Future.wait([
        _masterApi.listSubCategories(),
        _masterApi.listMakes(),
        _masterApi.listModels(),
      ]);
      if (!mounted) return;
      setState(() {
        _masterLoading = false;
        _allSubCategories = results[0] as List<SubCategoryDto>;
        _allMakes = results[1] as List<MakeDto>;
        _allModels = results[2] as List<ModelDto>;
        _fillCascadeDefaults();
      });
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _masterLoading = false;
        _masterError = '$e';
      });
    }
  }

  @override
  void dispose() {
    _assetNameController.dispose();
    super.dispose();
  }

  void _handleCreate() {
    if (!_formKey.currentState!.validate()) {
      return;
    }

    // Edge case: Validate all selections
    if (_selectedCategoryId == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please select a category')),
      );
      return;
    }

    if (_selectedSubCategoryId == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please select a subcategory')),
      );
      return;
    }

    if (_selectedMakeId == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please select a make')),
      );
      return;
    }

    if (_selectedModelId == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please select a model')),
      );
      return;
    }

    final user = (context.read<AuthBloc>().state as AuthAuthenticated).user;

    context.read<AssetBloc>().add(
          CreateAssetEvent(
            AssetRequest(
              categoryId: _selectedCategoryId!,
              subCategoryId: _selectedSubCategoryId!,
              makeId: _selectedMakeId!,
              modelId: _selectedModelId!,
              assetNameUdv: _assetNameController.text.trim(),
              userId: user.userId,
              username: user.username,
              projectType: user.projectType ?? 'ASSET',
            ),
          ),
        );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Create Asset'),
        actions: const [
          Padding(
            padding: EdgeInsets.only(right: 8),
            child: ViewLayoutToggle(compact: true),
          ),
        ],
      ),
      body: BlocListener<AssetBloc, AssetState>(
        listener: (context, state) {
          if (state is AssetCreated) {
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(
                content: Text('Asset created successfully'),
                backgroundColor: Colors.green,
              ),
            );
            Navigator.of(context).pop();
          } else if (state is AssetError) {
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(
                content: Text(state.message),
                backgroundColor: Colors.red,
              ),
            );
          }
        },
        child: BlocBuilder<AssetBloc, AssetState>(
          builder: (context, state) {
            if (_categoriesLoading && _categories.isEmpty) {
              return const AppLoadingWidget(message: 'Loading categories...');
            }

            return SingleChildScrollView(
              padding: const EdgeInsets.all(24.0),
              child: Form(
                key: _formKey,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    if (_categoriesError != null) ...[
                      Container(
                        padding: const EdgeInsets.all(12),
                        decoration: BoxDecoration(
                          color: KeeplyTokens.danger.withValues(alpha: 0.08),
                          borderRadius: BorderRadius.circular(KeeplyTokens.radiusXs),
                          border: Border.all(color: KeeplyTokens.danger.withValues(alpha: 0.25)),
                        ),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              'Could not load categories.',
                              style: Theme.of(context).textTheme.titleSmall?.copyWith(
                                    color: KeeplyTokens.danger,
                                    fontWeight: FontWeight.w600,
                                  ),
                            ),
                            const SizedBox(height: 6),
                            Text(_categoriesError!, style: Theme.of(context).textTheme.bodySmall),
                            const SizedBox(height: 8),
                            TextButton.icon(
                              onPressed: _loadCategories,
                              icon: const Icon(Icons.refresh, size: 18),
                              label: const Text('Retry'),
                            ),
                          ],
                        ),
                      ),
                      const SizedBox(height: 16),
                    ],
                    if (_masterError != null) ...[
                      Container(
                        padding: const EdgeInsets.all(12),
                        decoration: BoxDecoration(
                          color: KeeplyTokens.danger.withValues(alpha: 0.08),
                          borderRadius: BorderRadius.circular(KeeplyTokens.radiusXs),
                          border: Border.all(color: KeeplyTokens.danger.withValues(alpha: 0.25)),
                        ),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              'Could not load subcategories, makes, or models.',
                              style: Theme.of(context).textTheme.titleSmall?.copyWith(
                                    color: KeeplyTokens.danger,
                                    fontWeight: FontWeight.w600,
                                  ),
                            ),
                            const SizedBox(height: 6),
                            Text(_masterError!, style: Theme.of(context).textTheme.bodySmall),
                            const SizedBox(height: 8),
                            TextButton.icon(
                              onPressed: _loadMasterData,
                              icon: const Icon(Icons.refresh, size: 18),
                              label: const Text('Retry'),
                            ),
                          ],
                        ),
                      ),
                      const SizedBox(height: 16),
                    ],
                    if (_masterLoading) ...[
                      Row(
                        children: [
                          const SizedBox(
                            width: 20,
                            height: 20,
                            child: CircularProgressIndicator(strokeWidth: 2),
                          ),
                          const SizedBox(width: 12),
                          Expanded(
                            child: Text(
                              'Loading subcategories, makes, and models…',
                              style: Theme.of(context).textTheme.bodySmall?.copyWith(color: KeeplyTokens.muted),
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 16),
                    ],
                    // Asset Name
                    TextFormField(
                      controller: _assetNameController,
                      decoration: const InputDecoration(
                        labelText: 'Asset Name *',
                        prefixIcon: Icon(Icons.inventory_2),
                        border: OutlineInputBorder(),
                      ),
                      validator: (value) =>
                          ValidationHelper.validateRequired(value, 'Asset name'),
                    ),
                    const SizedBox(height: 16),
                    _CategoryBottomSheetField(
                      label: 'Category *',
                      prefixIcon: Icons.category,
                      categories: _categories,
                      value: _selectedCategoryId,
                      enabled: !_categoriesLoading && _categoriesError == null && _categories.isNotEmpty,
                      emptyHint: _categoriesLoading
                          ? 'Loading categories…'
                          : _categoriesError != null
                              ? 'Fix the error above, then retry'
                              : _categories.isEmpty
                                  ? 'No categories available'
                                  : 'Select a category',
                      onChanged: (value) {
                        setState(() {
                          _selectedCategoryId = value;
                          _selectedSubCategoryId = null;
                          _selectedMakeId = null;
                          _selectedModelId = null;
                          _fillCascadeDefaults();
                        });
                      },
                      validator: (value) => value == null ? 'Category is required' : null,
                    ),
                    const SizedBox(height: 16),
                    _SubcategoryCardListField(
                      key: ValueKey<String>('sub_${_selectedCategoryId ?? 'none'}'),
                      label: 'SubCategory *',
                      prefixIcon: Icons.subdirectory_arrow_right,
                      subcategories: _subcategoriesForCategory,
                      value: _selectedSubCategoryId,
                      enabled: !_masterLoading &&
                          _masterError == null &&
                          _selectedCategoryId != null &&
                          _subcategoriesForCategory.isNotEmpty,
                      emptyHint: _masterLoading
                          ? 'Loading…'
                          : _masterError != null
                              ? 'Fix the error above, then retry'
                              : _selectedCategoryId == null
                                  ? 'Select a category first'
                                  : 'No subcategories for this category',
                      onChanged: (value) {
                        setState(() {
                          _selectedSubCategoryId = value;
                          _selectedMakeId = null;
                          _selectedModelId = null;
                          _fillCascadeDefaults();
                        });
                      },
                      validator: (value) => value == null ? 'SubCategory is required' : null,
                    ),
                    const SizedBox(height: 16),
                    SelectableOptionPicker<int>(
                      key: ValueKey<String>('make_${_selectedSubCategoryId ?? 'none'}'),
                      label: 'Make *',
                      prefixIcon: Icons.build,
                      value: _selectedMakeId,
                      enabled: !_masterLoading &&
                          _masterError == null &&
                          _selectedSubCategoryId != null &&
                          _makesForSubcategory.isNotEmpty,
                      emptyHint: _masterLoading
                          ? 'Loading…'
                          : _masterError != null
                              ? 'Fix the error above, then retry'
                              : _selectedSubCategoryId == null
                                  ? 'Select a subcategory first'
                                  : 'No makes for this subcategory',
                      options: _makesForSubcategory
                          .map(
                            (m) => SelectableOption<int>(
                              value: m.makeId!,
                              title: m.makeName ?? 'Make ${m.makeId}',
                            ),
                          )
                          .toList(),
                      onChanged: (value) {
                        setState(() {
                          _selectedMakeId = value;
                          _selectedModelId = null;
                          _fillCascadeDefaults();
                        });
                      },
                      validator: (value) => value == null ? 'Make is required' : null,
                    ),
                    const SizedBox(height: 16),
                    SelectableOptionPicker<int>(
                      key: ValueKey<String>('model_${_selectedMakeId ?? 'none'}'),
                      label: 'Model *',
                      prefixIcon: Icons.model_training,
                      value: _selectedModelId,
                      enabled: !_masterLoading &&
                          _masterError == null &&
                          _selectedMakeId != null &&
                          _modelsForMake.isNotEmpty,
                      emptyHint: _masterLoading
                          ? 'Loading…'
                          : _masterError != null
                              ? 'Fix the error above, then retry'
                              : _selectedMakeId == null
                                  ? 'Select a make first'
                                  : 'No models for this make',
                      options: _modelsForMake
                          .map(
                            (m) => SelectableOption<int>(
                              value: m.modelId!,
                              title: m.modelName ?? 'Model ${m.modelId}',
                            ),
                          )
                          .toList(),
                      onChanged: (value) => setState(() => _selectedModelId = value),
                      validator: (value) => value == null ? 'Model is required' : null,
                    ),
                    const SizedBox(height: 24),
                    // Create Button
                    BlocBuilder<AssetBloc, AssetState>(
                      builder: (context, state) {
                        final isLoading = state is AssetLoading;
                        return ElevatedButton(
                          onPressed: isLoading ? null : _handleCreate,
                          style: ElevatedButton.styleFrom(
                            padding: const EdgeInsets.symmetric(vertical: 16),
                          ),
                          child: isLoading
                              ? const SizedBox(
                                  height: 20,
                                  width: 20,
                                  child: CircularProgressIndicator(strokeWidth: 2),
                                )
                              : const Text('Create Asset'),
                        );
                      },
                    ),
                  ],
                ),
              ),
            );
          },
        ),
      ),
    );
  }
}

/// Scrollable category rows for the modal bottom sheet with a visible scrollbar when content overflows.
class _CategoryModalList extends StatefulWidget {
  const _CategoryModalList({
    required this.categories,
    required this.selectedId,
    required this.onSelected,
  });

  final List<Category> categories;
  final int? selectedId;
  final ValueChanged<int> onSelected;

  @override
  State<_CategoryModalList> createState() => _CategoryModalListState();
}

class _CategoryModalListState extends State<_CategoryModalList> {
  late final ScrollController _scrollController;

  @override
  void initState() {
    super.initState();
    _scrollController = ScrollController();
  }

  @override
  void dispose() {
    _scrollController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scrollbar(
      controller: _scrollController,
      thumbVisibility: true,
      thickness: 6,
      radius: const Radius.circular(3),
      child: ListView.builder(
        controller: _scrollController,
        itemCount: widget.categories.length,
        itemBuilder: (context, index) {
          final c = widget.categories[index];
          final id = c.categoryId!;
          final selected = widget.selectedId == id;
          return ListTile(
            title: Text(c.categoryName, maxLines: 2, overflow: TextOverflow.ellipsis),
            trailing: selected
                ? const Icon(Icons.check_circle, color: KeeplyTokens.accent, size: 22)
                : null,
            onTap: () => widget.onSelected(id),
          );
        },
      ),
    );
  }
}

class _CategoryBottomSheetField extends StatefulWidget {
  const _CategoryBottomSheetField({
    required this.label,
    required this.categories,
    required this.value,
    required this.onChanged,
    this.validator,
    required this.enabled,
    required this.emptyHint,
    this.prefixIcon,
  });

  final String label;
  final List<Category> categories;
  final int? value;
  final ValueChanged<int?> onChanged;
  final FormFieldValidator<int?>? validator;
  final bool enabled;
  final String emptyHint;
  final IconData? prefixIcon;

  @override
  State<_CategoryBottomSheetField> createState() => _CategoryBottomSheetFieldState();
}

class _CategoryBottomSheetFieldState extends State<_CategoryBottomSheetField> {
  final GlobalKey<FormFieldState<int?>> _fieldKey = GlobalKey<FormFieldState<int?>>();

  @override
  void didUpdateWidget(_CategoryBottomSheetField oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.value != widget.value) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        _fieldKey.currentState?.didChange(widget.value);
      });
    }
  }

  String? _labelForId(int? id) {
    if (id == null) return null;
    for (final c in widget.categories) {
      if (c.categoryId == id) return c.categoryName;
    }
    return null;
  }

  Future<void> _openSheet(FormFieldState<int?> field) async {
    final list = widget.categories.where((c) => c.categoryId != null).toList();
    final picked = await showModalBottomSheet<int>(
      context: context,
      isScrollControlled: true,
      showDragHandle: true,
      builder: (sheetCtx) {
        final maxH = MediaQuery.sizeOf(sheetCtx).height * 0.55;
        return SafeArea(
          child: ConstrainedBox(
            constraints: BoxConstraints(maxHeight: maxH),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                Padding(
                  padding: const EdgeInsets.fromLTRB(20, 4, 20, 12),
                  child: Text(
                    'Select category',
                    style: Theme.of(sheetCtx).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w600),
                  ),
                ),
                const Divider(height: 1),
                Expanded(
                  child: _CategoryModalList(
                    categories: list,
                    selectedId: field.value,
                    onSelected: (id) => Navigator.pop(sheetCtx, id),
                  ),
                ),
              ],
            ),
          ),
        );
      },
    );
    if (picked == null || !mounted) return;
    field.didChange(picked);
    widget.onChanged(picked);
  }

  @override
  Widget build(BuildContext context) {
    return FormField<int?>(
      key: _fieldKey,
      initialValue: widget.value,
      validator: (v) => widget.enabled ? widget.validator?.call(v) : null,
      builder: (field) {
        final borderColor = field.hasError ? Theme.of(context).colorScheme.error : KeeplyTokens.line;
        final display = _labelForId(field.value);

        return Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Row(
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                if (widget.prefixIcon != null) ...[
                  Icon(widget.prefixIcon, size: 22, color: KeeplyTokens.muted),
                  const SizedBox(width: 10),
                ],
                Expanded(
                  child: Text(
                    widget.label,
                    style: Theme.of(context).textTheme.titleSmall?.copyWith(fontWeight: FontWeight.w600),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 8),
            if (!widget.enabled)
              Text(
                widget.emptyHint,
                style: Theme.of(context).textTheme.bodySmall?.copyWith(color: KeeplyTokens.muted),
              )
            else if (widget.categories.isEmpty)
              Text(
                widget.emptyHint,
                style: Theme.of(context).textTheme.bodySmall?.copyWith(color: KeeplyTokens.muted),
              )
            else
              Material(
                color: Colors.transparent,
                child: InkWell(
                  onTap: () => _openSheet(field),
                  borderRadius: BorderRadius.circular(KeeplyTokens.radiusXs),
                  child: Container(
                    padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 14),
                    decoration: BoxDecoration(
                      borderRadius: BorderRadius.circular(KeeplyTokens.radiusXs),
                      border: Border.all(color: borderColor),
                    ),
                    child: Row(
                      children: [
                        Expanded(
                          child: Text(
                            display ?? widget.emptyHint,
                            style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                                  color: display == null ? KeeplyTokens.muted : null,
                                ),
                            maxLines: 2,
                            overflow: TextOverflow.ellipsis,
                          ),
                        ),
                        const Icon(Icons.keyboard_arrow_down, color: KeeplyTokens.muted),
                      ],
                    ),
                  ),
                ),
              ),
            if (field.hasError)
              Padding(
                padding: const EdgeInsets.only(top: 6, left: 12),
                child: Text(
                  field.errorText!,
                  style: TextStyle(color: Theme.of(context).colorScheme.error, fontSize: 12),
                ),
              ),
          ],
        );
      },
    );
  }
}

class _SubcategoryCardListField extends StatefulWidget {
  const _SubcategoryCardListField({
    super.key,
    required this.label,
    required this.subcategories,
    required this.value,
    required this.onChanged,
    this.validator,
    required this.enabled,
    required this.emptyHint,
    this.prefixIcon,
  });

  final String label;
  final List<SubCategoryDto> subcategories;
  final int? value;
  final ValueChanged<int?> onChanged;
  final FormFieldValidator<int?>? validator;
  final bool enabled;
  final String emptyHint;
  final IconData? prefixIcon;

  @override
  State<_SubcategoryCardListField> createState() => _SubcategoryCardListFieldState();
}

class _SubcategoryCardListFieldState extends State<_SubcategoryCardListField> {
  final GlobalKey<FormFieldState<int?>> _fieldKey = GlobalKey<FormFieldState<int?>>();
  final ScrollController _subcategoryScrollController = ScrollController();

  @override
  void dispose() {
    _subcategoryScrollController.dispose();
    super.dispose();
  }

  @override
  void didUpdateWidget(_SubcategoryCardListField oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.value != widget.value) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        _fieldKey.currentState?.didChange(widget.value);
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return FormField<int?>(
      key: _fieldKey,
      initialValue: widget.value,
      validator: (v) => widget.enabled ? widget.validator?.call(v) : null,
      builder: (field) {
        final borderColor = field.hasError ? Theme.of(context).colorScheme.error : KeeplyTokens.line;
        final list = widget.subcategories.where((s) => s.subCategoryId != null).toList();

        return Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Row(
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                if (widget.prefixIcon != null) ...[
                  Icon(widget.prefixIcon, size: 22, color: KeeplyTokens.muted),
                  const SizedBox(width: 10),
                ],
                Expanded(
                  child: Text(
                    widget.label,
                    style: Theme.of(context).textTheme.titleSmall?.copyWith(fontWeight: FontWeight.w600),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 8),
            if (!widget.enabled)
              Text(
                widget.emptyHint,
                style: Theme.of(context).textTheme.bodySmall?.copyWith(color: KeeplyTokens.muted),
              )
            else if (list.isEmpty)
              Text(
                widget.emptyHint,
                style: Theme.of(context).textTheme.bodySmall?.copyWith(color: KeeplyTokens.muted),
              )
            else
              Container(
                constraints: const BoxConstraints(maxHeight: 220),
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(KeeplyTokens.radiusXs),
                  border: Border.all(color: borderColor),
                ),
                clipBehavior: Clip.antiAlias,
                child: Scrollbar(
                  controller: _subcategoryScrollController,
                  thumbVisibility: true,
                  thickness: 6,
                  radius: const Radius.circular(3),
                  child: ListView.builder(
                    controller: _subcategoryScrollController,
                    padding: const EdgeInsets.all(8),
                    itemCount: list.length,
                    itemBuilder: (context, i) {
                      final sub = list[i];
                      final id = sub.subCategoryId!;
                      final title = sub.subCategoryName ?? 'Subcategory $id';
                      final selected = field.value == id;
                      return Padding(
                        padding: EdgeInsets.only(bottom: i < list.length - 1 ? 8 : 0),
                        child: Card(
                          margin: EdgeInsets.zero,
                          elevation: selected ? 1 : 0,
                          color: selected ? KeeplyTokens.accentSoft : Theme.of(context).colorScheme.surface,
                          surfaceTintColor: Colors.transparent,
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(KeeplyTokens.radiusXs),
                            side: BorderSide(
                              color: selected ? KeeplyTokens.accent : KeeplyTokens.line,
                              width: selected ? 2 : 1,
                            ),
                          ),
                          clipBehavior: Clip.antiAlias,
                          child: InkWell(
                            onTap: () {
                              field.didChange(id);
                              widget.onChanged(id);
                            },
                            child: Padding(
                              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 12),
                              child: Row(
                                children: [
                                  Expanded(
                                    child: Text(
                                      title,
                                      maxLines: 2,
                                      overflow: TextOverflow.ellipsis,
                                      style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                                            fontWeight: selected ? FontWeight.w600 : FontWeight.normal,
                                          ),
                                    ),
                                  ),
                                  if (selected)
                                    const Icon(Icons.check_circle, color: KeeplyTokens.accent, size: 22),
                                ],
                              ),
                            ),
                          ),
                        ),
                      );
                    },
                  ),
                ),
              ),
            if (field.hasError)
              Padding(
                padding: const EdgeInsets.only(top: 6, left: 12),
                child: Text(
                  field.errorText!,
                  style: TextStyle(color: Theme.of(context).colorScheme.error, fontSize: 12),
                ),
              ),
          ],
        );
      },
    );
  }
}

