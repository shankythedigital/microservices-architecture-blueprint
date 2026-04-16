import 'dart:io' show File;

import 'package:dio/dio.dart';
import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:intl/intl.dart';
import 'package:keeply_app/core/api/keeply_api_models.dart';
import 'package:keeply_app/core/api/keeply_categories_api.dart';
import 'package:keeply_app/core/api/keeply_master_data_api.dart';
import 'package:keeply_app/core/exceptions/api_exception.dart';
import 'package:keeply_app/core/sync/app_data_refresh_cubit.dart';
import 'package:keeply_app/core/theme/keeply_tokens.dart';
import 'package:keeply_app/core/view_layout/view_layout_scope.dart';
import 'package:keeply_app/core/widgets/loading_widget.dart';
import 'package:keeply_app/core/widgets/selectable_option_picker.dart';
import 'package:keeply_app/features/asset/data/datasources/asset_remote_datasource.dart';
import 'package:keeply_app/features/asset/data/models/asset_models.dart';
import 'package:keeply_app/features/asset/presentation/bloc/asset_bloc.dart';
import 'package:keeply_app/features/auth/data/models/auth_models.dart';
import 'package:keeply_app/features/auth/presentation/bloc/auth_bloc.dart';

/// Parity with React [AddAssetManualPage.tsx]: catalog selections, warranty, serial,
/// invoice + optional photo, `POST /api/asset/v1/assets/complete` (multipart).
class CreateAssetPage extends StatefulWidget {
  const CreateAssetPage({super.key});

  @override
  State<CreateAssetPage> createState() => _CreateAssetPageState();
}

class _CreateAssetPageState extends State<CreateAssetPage> {
  static const int _maxInvoiceBytes = 10 * 1024 * 1024;
  static const int _maxPhotoBytes = 10 * 1024 * 1024;
  static const int _assetNameMax = 255;
  static const int _serialMax = 120;
  /// React `DEFAULT_PROJECT_TYPE` in `keeply_react_app/src/constants/project.ts`
  static const String _defaultProjectType = 'ECOM';

  final _formKey = GlobalKey<FormState>();
  final _assetNameController = TextEditingController();
  final _serialCtrl = TextEditingController();
  final _warrantyProviderCtrl = TextEditingController();
  final KeeplyMasterDataApi _masterApi = KeeplyMasterDataApi();
  final KeeplyCategoriesApi _categoriesApi = KeeplyCategoriesApi();
  final AssetRemoteDataSource _assetDs = AssetRemoteDataSource();

  int? _selectedCategoryId;
  int? _selectedSubCategoryId;
  int? _selectedMakeId;
  int? _selectedModelId;

  DateTime? _warrantyStart;
  DateTime? _warrantyEnd;
  /// '' | MANUFACTURER | EXTENDED | AMC — optional; sent as `warrantyStatus` when set.
  String _warrantyType = '';

  String? _invoicePath;
  String? _invoiceName;
  String? _photoPath;
  String? _photoName;

  bool _submitting = false;
  String? _submitError;

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

  String get _suggestedName {
    var makeName = '';
    for (final m in _makesForSubcategory) {
      if (m.makeId == _selectedMakeId) {
        makeName = (m.makeName ?? '').trim();
        break;
      }
    }
    var modelName = '';
    for (final m in _modelsForMake) {
      if (m.modelId == _selectedModelId) {
        modelName = (m.modelName ?? '').trim();
        break;
      }
    }
    var base = '$makeName $modelName'.trim();
    final ser = _serialCtrl.text.trim();
    if (base.isEmpty && ser.isEmpty) return '';
    if (ser.isNotEmpty) {
      base = base.isEmpty ? ser : '$base ($ser)';
    }
    return base.trim();
  }

  String _fileExt(String path) {
    final i = path.lastIndexOf('.');
    if (i < 0 || i >= path.length - 1) return '';
    return path.substring(i + 1).toLowerCase();
  }

  String _docTypeFromInvoicePath(String path) {
    final e = _fileExt(path);
    if (e == 'jpg') return 'jpeg';
    return e.isNotEmpty ? e : 'pdf';
  }

  bool _allowedInvoicePath(String path) {
    final e = _fileExt(path);
    return e == 'pdf' || e == 'jpeg' || e == 'jpg' || e == 'png' || e == 'gif' || e == 'webp';
  }

  bool _allowedPhotoPath(String path) {
    final e = _fileExt(path);
    return e == 'jpeg' || e == 'jpg' || e == 'png' || e == 'gif' || e == 'webp';
  }

  String _resolveUsername(UserDto u) {
    final fromProfile = u.username?.trim();
    if (fromProfile != null && fromProfile.isNotEmpty) return fromProfile;
    if (u.userId > 0) return 'user_${u.userId}';
    return '';
  }

  String? _validateForSubmit(UserDto u) {
    if (_selectedCategoryId == null) return 'Please select a category.';
    if (_selectedSubCategoryId == null) return 'Please select a subcategory.';
    if (_selectedMakeId == null) return 'Please select a brand (make).';
    if (_selectedModelId == null) {
      return 'Please select a model. Full registration requires a catalog model ID.';
    }
    if (u.userId <= 0) {
      return 'Your account ID is missing — sign out and sign in again, then retry.';
    }
    final display = _assetNameController.text.trim();
    final suggested = _suggestedName.trim();
    final name = display.isNotEmpty ? display : suggested;
    if (name.length < 2) {
      return 'Enter a display name (at least 2 characters), or finish selecting model and serial.';
    }
    if (name.length > _assetNameMax) return 'Display name must be at most $_assetNameMax characters.';
    final serialTrim = _serialCtrl.text.trim();
    if (serialTrim.isEmpty) return 'Serial number is required.';
    if (serialTrim.length > _serialMax) return 'Serial number must be at most $_serialMax characters.';
    if (_warrantyStart == null) return 'Warranty start date (purchase / installation) is required.';
    if (_warrantyEnd == null) return 'Warranty end date is required.';
    final start = DateTime(_warrantyStart!.year, _warrantyStart!.month, _warrantyStart!.day);
    final end = DateTime(_warrantyEnd!.year, _warrantyEnd!.month, _warrantyEnd!.day);
    if (!end.isAfter(start)) return 'Warranty end date must be after the start date.';
    final inv = _invoicePath;
    if (inv == null || inv.isEmpty) return 'Purchase invoice or proof document is required for full registration.';
    if (!_allowedInvoicePath(inv)) {
      return 'Invoice must be a PDF or an image (JPEG, PNG, GIF, or WebP).';
    }
    try {
      final invLen = File(inv).lengthSync();
      if (invLen > _maxInvoiceBytes) return 'Invoice file is too large (maximum 10 MB).';
    } catch (_) {
      return 'Could not read the invoice file. Try picking it again.';
    }
    final photo = _photoPath;
    if (photo != null && photo.isNotEmpty) {
      if (!_allowedPhotoPath(photo)) {
        return 'Appliance photo must be an image (JPEG, PNG, GIF, or WebP).';
      }
      try {
        if (File(photo).lengthSync() > _maxPhotoBytes) {
          return 'Appliance photo is too large (maximum 10 MB).';
        }
      } catch (_) {
        return 'Could not read the appliance photo. Try picking it again.';
      }
    }
    final uname = _resolveUsername(u);
    if (uname.isEmpty) {
      return 'Username could not be determined — update your profile or sign in again.';
    }
    return null;
  }

  @override
  void initState() {
    super.initState();
    _serialCtrl.addListener(() => setState(() {}));
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
    _serialCtrl.dispose();
    _warrantyProviderCtrl.dispose();
    super.dispose();
  }

  Future<void> _pickWarrantyStart() async {
    final now = DateTime.now();
    final d = await showDatePicker(
      context: context,
      initialDate: _warrantyStart ?? now,
      firstDate: DateTime(1970),
      lastDate: DateTime(now.year + 20),
    );
    if (d != null) setState(() => _warrantyStart = d);
  }

  Future<void> _pickWarrantyEnd() async {
    final now = DateTime.now();
    final d = await showDatePicker(
      context: context,
      initialDate: _warrantyEnd ?? _warrantyStart ?? now,
      firstDate: DateTime(1970),
      lastDate: DateTime(now.year + 30),
    );
    if (d != null) setState(() => _warrantyEnd = d);
  }

  Future<void> _pickInvoice() async {
    final r = await FilePicker.platform.pickFiles(type: FileType.custom, allowedExtensions: ['pdf', 'jpg', 'jpeg', 'png', 'gif', 'webp']);
    if (r == null || r.files.isEmpty) return;
    final f = r.files.single;
    final path = f.path;
    if (path == null) return;
    setState(() {
      _invoicePath = path;
      _invoiceName = f.name;
      _submitError = null;
    });
  }

  Future<void> _pickPhoto() async {
    final r = await FilePicker.platform.pickFiles(type: FileType.image);
    if (r == null || r.files.isEmpty) return;
    final f = r.files.single;
    final path = f.path;
    if (path == null) return;
    setState(() {
      _photoPath = path;
      _photoName = f.name;
      _submitError = null;
    });
  }

  void _clearPhoto() {
    setState(() {
      _photoPath = null;
      _photoName = null;
    });
  }

  Future<void> _submit() async {
    _formKey.currentState?.validate();
    final auth = context.read<AuthBloc>().state;
    if (auth is! AuthAuthenticated) {
      setState(() => _submitError = 'You are not signed in.');
      return;
    }
    final user = auth.user;
    final err = _validateForSubmit(user);
    if (err != null) {
      setState(() => _submitError = err);
      return;
    }
    setState(() {
      _submitting = true;
      _submitError = null;
    });
    final uname = _resolveUsername(user);
    final display = _assetNameController.text.trim();
    final suggested = _suggestedName.trim();
    final assetNameUdv = display.isNotEmpty ? display : suggested;
    final ymd = DateFormat('yyyy-MM-dd');
    final inv = _invoicePath!;
    final map = <String, dynamic>{
      'userId': user.userId,
      'username': uname,
      'projectType': (user.projectType != null && user.projectType!.trim().isNotEmpty)
          ? user.projectType!.trim()
          : _defaultProjectType,
      'assetNameUdv': assetNameUdv,
      'modelId': _selectedModelId,
      'serialNumber': _serialCtrl.text.trim(),
      'warrantyStartDate': ymd.format(_warrantyStart!),
      'warrantyEndDate': ymd.format(_warrantyEnd!),
      'targetUserId': user.userId,
      'targetUsername': uname,
      'document': await MultipartFile.fromFile(inv, filename: _invoiceName ?? 'invoice'),
      'docType': _docTypeFromInvoicePath(inv),
    };
    if (_selectedCategoryId != null) map['categoryId'] = _selectedCategoryId;
    if (_selectedSubCategoryId != null) map['subCategoryId'] = _selectedSubCategoryId;
    if (_selectedMakeId != null) map['makeId'] = _selectedMakeId;
    final wp = _warrantyProviderCtrl.text.trim();
    if (wp.isNotEmpty) map['warrantyProvider'] = wp;
    if (_warrantyType.isNotEmpty) map['warrantyStatus'] = _warrantyType;
    final photo = _photoPath;
    if (photo != null && photo.isNotEmpty) {
      map['assetImage'] = await MultipartFile.fromFile(photo, filename: _photoName ?? 'photo');
    }
    try {
      final data = await _assetDs.createAssetComplete(FormData.fromMap(map));
      if (!mounted) return;
      final savedName = data['assetNameUdv'] as String? ?? assetNameUdv;
      context.read<AppDataRefreshCubit>().bump(KeeplyDataChannel.assets);
      context.read<AssetBloc>().add(LoadAssetsEvent(page: 0, size: 20));
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Saved: $savedName'), backgroundColor: Colors.green.shade700),
      );
      Navigator.of(context).pop();
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _submitting = false;
        _submitError = e is ApiException ? e.userMessage : 'Could not save asset';
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    final suggested = _suggestedName;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Manual entry'),
        actions: const [
          Padding(
            padding: EdgeInsets.only(right: 8),
            child: ViewLayoutToggle(compact: true),
          ),
        ],
      ),
      body: _categoriesLoading && _categories.isEmpty
          ? const AppLoadingWidget(message: 'Loading categories…')
          : SingleChildScrollView(
              padding: const EdgeInsets.all(24.0),
              child: Form(
                key: _formKey,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    Text(
                      'Registers the appliance in one step (asset, warranty, proof document, and assignment to your account). '
                      'You need catalog category → brand → model, warranty dates, serial number, and an invoice or image of proof. '
                      'You may add an optional appliance photo.',
                      style: t.bodySmall?.copyWith(color: KeeplyTokens.muted, height: 1.45),
                    ),
                    const SizedBox(height: 20),
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
                    const SizedBox(height: 20),
                    TextFormField(
                      controller: _assetNameController,
                      maxLength: _assetNameMax,
                      decoration: InputDecoration(
                        labelText: 'Display name',
                        hintText: suggested.isNotEmpty ? suggested : 'Shown in your appliance list',
                        prefixIcon: const Icon(Icons.badge_outlined),
                        border: const OutlineInputBorder(),
                        counterText: '',
                      ),
                      onChanged: (_) => setState(() {}),
                    ),
                    Padding(
                      padding: const EdgeInsets.only(top: 6, left: 4),
                      child: Text(
                        suggested.isNotEmpty
                            ? 'If you leave this blank, we use: $suggested'
                            : 'Select make and model (and serial) to suggest a name, or type your own.',
                        style: t.bodySmall?.copyWith(color: KeeplyTokens.muted, height: 1.35),
                      ),
                    ),
                    const SizedBox(height: 16),
                    TextFormField(
                      controller: _serialCtrl,
                      maxLength: _serialMax,
                      decoration: const InputDecoration(
                        labelText: 'Serial number',
                        prefixIcon: Icon(Icons.numbers),
                        border: OutlineInputBorder(),
                      ),
                    ),
                    const SizedBox(height: 16),
                    Text('Warranty start (purchase / installation)', style: t.titleSmall?.copyWith(fontWeight: FontWeight.w600)),
                    const SizedBox(height: 8),
                    OutlinedButton.icon(
                      onPressed: _pickWarrantyStart,
                      icon: const Icon(Icons.calendar_today_outlined, size: 18),
                      label: Text(
                        _warrantyStart == null
                            ? 'Select date'
                            : DateFormat.yMMMd().format(_warrantyStart!),
                      ),
                    ),
                    const SizedBox(height: 16),
                    Text('Warranty end', style: t.titleSmall?.copyWith(fontWeight: FontWeight.w600)),
                    const SizedBox(height: 8),
                    OutlinedButton.icon(
                      onPressed: _pickWarrantyEnd,
                      icon: const Icon(Icons.event_outlined, size: 18),
                      label: Text(
                        _warrantyEnd == null
                            ? 'Select date'
                            : DateFormat.yMMMd().format(_warrantyEnd!),
                      ),
                    ),
                    const SizedBox(height: 16),
                    TextFormField(
                      controller: _warrantyProviderCtrl,
                      decoration: const InputDecoration(
                        labelText: 'Warranty provider (optional)',
                        hintText: 'e.g. Manufacturer, retailer',
                        prefixIcon: Icon(Icons.storefront_outlined),
                        border: OutlineInputBorder(),
                        counterText: '',
                      ),
                      maxLength: 200,
                    ),
                    const SizedBox(height: 12),
                    Text('Warranty type (optional)', style: t.titleSmall?.copyWith(fontWeight: FontWeight.w600)),
                    const SizedBox(height: 8),
                    Text(
                      'Optional — sent as warranty status when selected.',
                      style: t.bodySmall?.copyWith(color: KeeplyTokens.muted),
                    ),
                    const SizedBox(height: 8),
                    Wrap(
                      spacing: 8,
                      runSpacing: 8,
                      children: [
                        ChoiceChip(
                          label: const Text('Manufacturer'),
                          selected: _warrantyType == 'MANUFACTURER',
                          onSelected: (v) {
                            if (v) setState(() => _warrantyType = 'MANUFACTURER');
                          },
                        ),
                        ChoiceChip(
                          label: const Text('Extended'),
                          selected: _warrantyType == 'EXTENDED',
                          onSelected: (v) {
                            if (v) setState(() => _warrantyType = 'EXTENDED');
                          },
                        ),
                        ChoiceChip(
                          label: const Text('AMC'),
                          selected: _warrantyType == 'AMC',
                          onSelected: (v) {
                            if (v) setState(() => _warrantyType = 'AMC');
                          },
                        ),
                        ActionChip(
                          label: const Text('Clear'),
                          onPressed: () => setState(() => _warrantyType = ''),
                        ),
                      ],
                    ),
                    const SizedBox(height: 20),
                    Text('Invoice or proof (required)', style: t.titleSmall?.copyWith(fontWeight: FontWeight.w600)),
                    const SizedBox(height: 6),
                    Text('PDF or image, up to 10 MB.', style: t.bodySmall?.copyWith(color: KeeplyTokens.muted)),
                    const SizedBox(height: 8),
                    OutlinedButton.icon(
                      onPressed: _submitting ? null : _pickInvoice,
                      icon: const Icon(Icons.upload_file_outlined),
                      label: Text(_invoiceName ?? 'Choose file'),
                    ),
                    const SizedBox(height: 20),
                    Text('Appliance photo (optional)', style: t.titleSmall?.copyWith(fontWeight: FontWeight.w600)),
                    const SizedBox(height: 6),
                    Text(
                      'Image only, up to 10 MB. Shown on your asset card when catalog art is missing.',
                      style: t.bodySmall?.copyWith(color: KeeplyTokens.muted),
                    ),
                    const SizedBox(height: 8),
                    Row(
                      children: [
                        OutlinedButton.icon(
                          onPressed: _submitting ? null : _pickPhoto,
                          icon: const Icon(Icons.photo_camera_outlined),
                          label: Text(_photoName ?? 'Choose image'),
                        ),
                        if (_photoPath != null) ...[
                          const SizedBox(width: 8),
                          TextButton(onPressed: _submitting ? null : _clearPhoto, child: const Text('Remove')),
                        ],
                      ],
                    ),
                    if (_submitError != null) ...[
                      const SizedBox(height: 16),
                      Container(
                        width: double.infinity,
                        padding: const EdgeInsets.all(12),
                        decoration: BoxDecoration(
                          color: KeeplyTokens.danger.withValues(alpha: 0.08),
                          borderRadius: BorderRadius.circular(KeeplyTokens.radiusXs),
                          border: Border.all(color: KeeplyTokens.danger.withValues(alpha: 0.25)),
                        ),
                        child: Text(_submitError!, style: t.bodySmall?.copyWith(color: KeeplyTokens.danger)),
                      ),
                    ],
                    const SizedBox(height: 24),
                    FilledButton(
                      onPressed: _submitting ? null : _submit,
                      style: FilledButton.styleFrom(padding: const EdgeInsets.symmetric(vertical: 16)),
                      child: _submitting
                          ? const SizedBox(
                              height: 22,
                              width: 22,
                              child: CircularProgressIndicator(strokeWidth: 2),
                            )
                          : const Text('Save asset'),
                    ),
                    const SizedBox(height: 24),
                  ],
                ),
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

