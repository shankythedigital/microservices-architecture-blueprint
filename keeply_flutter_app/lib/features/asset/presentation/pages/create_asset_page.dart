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

  /// Prefer master-catalog category named "Master"; otherwise first by [categoryName].
  int? _pickDefaultCategoryId(List<Category> categories) {
    final withIds = categories.where((c) => c.categoryId != null).toList();
    if (withIds.isEmpty) return null;
    const master = 'master';
    for (final c in withIds) {
      if (c.categoryName.trim().toLowerCase() == master) {
        return c.categoryId;
      }
    }
    withIds.sort((a, b) => a.categoryName.compareTo(b.categoryName));
    return withIds.first.categoryId;
  }

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
      int? defaultId;
      if (_selectedCategoryId == null && list.isNotEmpty) {
        defaultId = _pickDefaultCategoryId(list);
      }
      setState(() {
        _categories = list;
        _categoriesLoading = false;
        if (defaultId != null) {
          _selectedCategoryId = defaultId;
        }
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
                    SelectableOptionPicker<int>(
                      label: 'Category *',
                      prefixIcon: Icons.category,
                      value: _selectedCategoryId,
                      enabled: !_categoriesLoading && _categoriesError == null && _categories.isNotEmpty,
                      emptyHint: _categoriesLoading
                          ? 'Loading categories…'
                          : _categoriesError != null
                              ? 'Fix the error above, then retry'
                              : _categories.isEmpty
                                  ? 'No categories available'
                                  : 'Select a category',
                      options: _categories
                          .where((c) => c.categoryId != null)
                          .map(
                            (c) => SelectableOption<int>(
                              value: c.categoryId!,
                              title: c.categoryName,
                            ),
                          )
                          .toList(),
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
                    SelectableOptionPicker<int>(
                      key: ValueKey<String>('sub_${_selectedCategoryId ?? 'none'}'),
                      label: 'SubCategory *',
                      prefixIcon: Icons.subdirectory_arrow_right,
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
                      options: _subcategoriesForCategory
                          .map(
                            (sub) => SelectableOption<int>(
                              value: sub.subCategoryId!,
                              title: sub.subCategoryName ?? 'Subcategory ${sub.subCategoryId}',
                            ),
                          )
                          .toList(),
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

