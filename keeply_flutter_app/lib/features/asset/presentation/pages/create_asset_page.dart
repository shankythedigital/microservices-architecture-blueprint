import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:keeply_app/core/utils/validation_helper.dart';
import 'package:keeply_app/core/widgets/error_widget.dart';
import 'package:keeply_app/core/widgets/loading_widget.dart';
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

  int? _selectedCategoryId;
  int? _selectedSubCategoryId;
  int? _selectedMakeId;
  int? _selectedModelId;

  List<Category> _categories = [];
  List<dynamic> _subCategories = [];
  List<dynamic> _makes = [];
  List<dynamic> _models = [];

  @override
  void initState() {
    super.initState();
    // Load categories
    context.read<AssetBloc>().add(LoadCategoriesEvent());
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
            if (state is AssetLoading && _categories.isEmpty) {
              return const AppLoadingWidget(message: 'Loading categories...');
            }

            if (state is CategoriesLoaded) {
              _categories = state.categories;
            }

            return SingleChildScrollView(
              padding: const EdgeInsets.all(24.0),
              child: Form(
                key: _formKey,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
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
                    // Category Dropdown
                    DropdownButtonFormField<int>(
                      value: _selectedCategoryId,
                      decoration: const InputDecoration(
                        labelText: 'Category *',
                        prefixIcon: Icon(Icons.category),
                        border: OutlineInputBorder(),
                      ),
                      items: _categories.map((category) {
                        return DropdownMenuItem<int>(
                          value: category.categoryId,
                          child: Text(category.categoryName),
                        );
                      }).toList(),
                      onChanged: (value) {
                        setState(() {
                          _selectedCategoryId = value;
                          _selectedSubCategoryId = null;
                          _selectedMakeId = null;
                          _selectedModelId = null;
                          // Load subcategories
                        });
                      },
                      validator: (value) {
                        if (value == null) {
                          return 'Category is required';
                        }
                        return null;
                      },
                    ),
                    const SizedBox(height: 16),
                    // SubCategory Dropdown
                    DropdownButtonFormField<int>(
                      value: _selectedSubCategoryId,
                      decoration: const InputDecoration(
                        labelText: 'SubCategory *',
                        prefixIcon: Icon(Icons.subdirectory_arrow_right),
                        border: OutlineInputBorder(),
                      ),
                      items: _subCategories.isEmpty
                          ? null
                          : _subCategories.map((subCategory) {
                              return DropdownMenuItem<int>(
                                value: subCategory['subCategoryId'],
                                child: Text(subCategory['subCategoryName']),
                              );
                            }).toList(),
                      onChanged: _subCategories.isEmpty
                          ? null
                          : (value) {
                              setState(() {
                                _selectedSubCategoryId = value;
                                _selectedMakeId = null;
                                _selectedModelId = null;
                                // Load makes
                              });
                            },
                      validator: (value) {
                        if (value == null) {
                          return 'SubCategory is required';
                        }
                        return null;
                      },
                    ),
                    const SizedBox(height: 16),
                    // Make Dropdown
                    DropdownButtonFormField<int>(
                      value: _selectedMakeId,
                      decoration: const InputDecoration(
                        labelText: 'Make *',
                        prefixIcon: Icon(Icons.build),
                        border: OutlineInputBorder(),
                      ),
                      items: _makes.isEmpty
                          ? null
                          : _makes.map((make) {
                              return DropdownMenuItem<int>(
                                value: make['makeId'],
                                child: Text(make['makeName']),
                              );
                            }).toList(),
                      onChanged: _makes.isEmpty
                          ? null
                          : (value) {
                              setState(() {
                                _selectedMakeId = value;
                                _selectedModelId = null;
                                // Load models
                              });
                            },
                      validator: (value) {
                        if (value == null) {
                          return 'Make is required';
                        }
                        return null;
                      },
                    ),
                    const SizedBox(height: 16),
                    // Model Dropdown
                    DropdownButtonFormField<int>(
                      value: _selectedModelId,
                      decoration: const InputDecoration(
                        labelText: 'Model *',
                        prefixIcon: Icon(Icons.model_training),
                        border: OutlineInputBorder(),
                      ),
                      items: _models.isEmpty
                          ? null
                          : _models.map((model) {
                              return DropdownMenuItem<int>(
                                value: model['modelId'],
                                child: Text(model['modelName']),
                              );
                            }).toList(),
                      onChanged: _models.isEmpty
                          ? null
                          : (value) {
                              setState(() {
                                _selectedModelId = value;
                              });
                            },
                      validator: (value) {
                        if (value == null) {
                          return 'Model is required';
                        }
                        return null;
                      },
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

