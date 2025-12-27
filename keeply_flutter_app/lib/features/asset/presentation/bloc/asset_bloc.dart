import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:keeply_app/core/exceptions/api_exception.dart';
import 'package:keeply_app/core/utils/logger.dart';
import 'package:keeply_app/features/asset/data/datasources/asset_remote_datasource.dart';
import 'package:keeply_app/features/asset/data/models/asset_models.dart';
import 'package:connectivity_plus/connectivity_plus.dart';

/// Asset BLoC
/// Handles all asset management logic with comprehensive edge case handling
class AssetBloc extends Bloc<AssetEvent, AssetState> {
  final AssetRemoteDataSource _assetDataSource;
  final Connectivity _connectivity;

  AssetBloc({
    required AssetRemoteDataSource assetDataSource,
    Connectivity? connectivity,
  })  : _assetDataSource = assetDataSource,
        _connectivity = connectivity ?? Connectivity(),
        super(AssetInitial()) {
    on<LoadAssetsEvent>(_onLoadAssets);
    on<LoadAssetByIdEvent>(_onLoadAssetById);
    on<CreateAssetEvent>(_onCreateAsset);
    on<UpdateAssetEvent>(_onUpdateAsset);
    on<DeleteAssetEvent>(_onDeleteAsset);
    on<LoadCategoriesEvent>(_onLoadCategories);
    on<CreateCategoryEvent>(_onCreateCategory);
    on<BulkCreateCategoriesEvent>(_onBulkCreateCategories);
    on<UploadCategoriesExcelEvent>(_onUploadCategoriesExcel);
  }

  // ============================================================
  // LOAD ASSETS
  // ============================================================
  Future<void> _onLoadAssets(LoadAssetsEvent event, Emitter<AssetState> emit) async {
    emit(AssetLoading());

    try {
      // Edge case: Check internet connection
      final connectivityResult = await _connectivity.checkConnectivity();
      if (connectivityResult == ConnectivityResult.none) {
        emit(AssetError('No internet connection. Please check your network.'));
        return;
      }

      // Edge case: Validate pagination parameters
      final page = event.page ?? 0;
      final size = event.size ?? 20;

      if (page < 0) {
        emit(AssetError('Page number must be non-negative'));
        return;
      }
      if (size <= 0 || size > 100) {
        emit(AssetError('Page size must be between 1 and 100'));
        return;
      }

      final assets = await _assetDataSource.getAssets(
        page: page,
        size: size,
        sort: event.sort,
      );

      emit(AssetsLoaded(assets: assets));
      AppLogger.info('Loaded ${assets.length} assets');
    } on ApiException catch (e) {
      AppLogger.error('Load assets failed: ${e.message}');
      emit(AssetError(e.userMessage));
    } catch (e) {
      AppLogger.error('Unexpected load assets error: $e');
      emit(AssetError('Failed to load assets. Please try again.'));
    }
  }

  // ============================================================
  // LOAD ASSET BY ID
  // ============================================================
  Future<void> _onLoadAssetById(LoadAssetByIdEvent event, Emitter<AssetState> emit) async {
    emit(AssetLoading());

    try {
      // Edge case: Validate asset ID
      if (event.assetId <= 0) {
        emit(AssetError('Invalid asset ID'));
        return;
      }

      // Edge case: Check internet connection
      final connectivityResult = await _connectivity.checkConnectivity();
      if (connectivityResult == ConnectivityResult.none) {
        emit(AssetError('No internet connection. Please check your network.'));
        return;
      }

      final asset = await _assetDataSource.getAssetById(event.assetId);
      emit(AssetLoaded(asset: asset));
      AppLogger.info('Loaded asset: ${asset.assetId}');
    } on ApiException catch (e) {
      AppLogger.error('Load asset failed: ${e.message}');
      emit(AssetError(e.userMessage));
    } catch (e) {
      AppLogger.error('Unexpected load asset error: $e');
      emit(AssetError('Failed to load asset. Please try again.'));
    }
  }

  // ============================================================
  // CREATE ASSET
  // ============================================================
  Future<void> _onCreateAsset(CreateAssetEvent event, Emitter<AssetState> emit) async {
    emit(AssetLoading());

    try {
      // Edge case: Check internet connection
      final connectivityResult = await _connectivity.checkConnectivity();
      if (connectivityResult == ConnectivityResult.none) {
        emit(AssetError('No internet connection. Please check your network.'));
        return;
      }

      // Edge case: Validate asset name
      if (event.request.assetNameUdv.isEmpty) {
        emit(AssetError('Asset name is required'));
        return;
      }
      if (event.request.assetNameUdv.length > 255) {
        emit(AssetError('Asset name must be less than 255 characters'));
        return;
      }

      // Edge case: Validate IDs
      if (event.request.categoryId <= 0) {
        emit(AssetError('Category is required'));
        return;
      }
      if (event.request.subCategoryId <= 0) {
        emit(AssetError('SubCategory is required'));
        return;
      }
      if (event.request.makeId <= 0) {
        emit(AssetError('Make is required'));
        return;
      }
      if (event.request.modelId <= 0) {
        emit(AssetError('Model is required'));
        return;
      }

      final asset = await _assetDataSource.createAsset(event.request);
      emit(AssetCreated(asset: asset));
      AppLogger.info('Asset created: ${asset.assetId}');
    } on ApiException catch (e) {
      AppLogger.error('Create asset failed: ${e.message}');
      emit(AssetError(e.userMessage));
    } catch (e) {
      AppLogger.error('Unexpected create asset error: $e');
      emit(AssetError('Failed to create asset. Please try again.'));
    }
  }

  // ============================================================
  // UPDATE ASSET
  // ============================================================
  Future<void> _onUpdateAsset(UpdateAssetEvent event, Emitter<AssetState> emit) async {
    emit(AssetLoading());

    try {
      // Edge case: Validate asset ID
      if (event.assetId <= 0) {
        emit(AssetError('Invalid asset ID'));
        return;
      }

      // Edge case: Check internet connection
      final connectivityResult = await _connectivity.checkConnectivity();
      if (connectivityResult == ConnectivityResult.none) {
        emit(AssetError('No internet connection. Please check your network.'));
        return;
      }

      // Edge case: Validate asset name
      if (event.request.assetNameUdv.isEmpty) {
        emit(AssetError('Asset name is required'));
        return;
      }

      final asset = await _assetDataSource.updateAsset(event.assetId, event.request);
      emit(AssetUpdated(asset: asset));
      AppLogger.info('Asset updated: ${asset.assetId}');
    } on ApiException catch (e) {
      AppLogger.error('Update asset failed: ${e.message}');
      emit(AssetError(e.userMessage));
    } catch (e) {
      AppLogger.error('Unexpected update asset error: $e');
      emit(AssetError('Failed to update asset. Please try again.'));
    }
  }

  // ============================================================
  // DELETE ASSET
  // ============================================================
  Future<void> _onDeleteAsset(DeleteAssetEvent event, Emitter<AssetState> emit) async {
    emit(AssetLoading());

    try {
      // Edge case: Validate asset ID
      if (event.assetId <= 0) {
        emit(AssetError('Invalid asset ID'));
        return;
      }

      // Edge case: Check internet connection
      final connectivityResult = await _connectivity.checkConnectivity();
      if (connectivityResult == ConnectivityResult.none) {
        emit(AssetError('No internet connection. Please check your network.'));
        return;
      }

      await _assetDataSource.deleteAsset(event.assetId);
      emit(AssetDeleted(assetId: event.assetId));
      AppLogger.info('Asset deleted: ${event.assetId}');
    } on ApiException catch (e) {
      AppLogger.error('Delete asset failed: ${e.message}');
      emit(AssetError(e.userMessage));
    } catch (e) {
      AppLogger.error('Unexpected delete asset error: $e');
      emit(AssetError('Failed to delete asset. Please try again.'));
    }
  }

  // ============================================================
  // LOAD CATEGORIES
  // ============================================================
  Future<void> _onLoadCategories(LoadCategoriesEvent event, Emitter<AssetState> emit) async {
    emit(AssetLoading());

    try {
      // Edge case: Check internet connection
      final connectivityResult = await _connectivity.checkConnectivity();
      if (connectivityResult == ConnectivityResult.none) {
        emit(AssetError('No internet connection. Please check your network.'));
        return;
      }

      final categories = await _assetDataSource.getCategories();
      emit(CategoriesLoaded(categories: categories));
      AppLogger.info('Loaded ${categories.length} categories');
    } on ApiException catch (e) {
      AppLogger.error('Load categories failed: ${e.message}');
      emit(AssetError(e.userMessage));
    } catch (e) {
      AppLogger.error('Unexpected load categories error: $e');
      emit(AssetError('Failed to load categories. Please try again.'));
    }
  }

  // ============================================================
  // CREATE CATEGORY
  // ============================================================
  Future<void> _onCreateCategory(CreateCategoryEvent event, Emitter<AssetState> emit) async {
    emit(AssetLoading());

    try {
      // Edge case: Validate category name
      if (event.request.categoryName.isEmpty) {
        emit(AssetError('Category name is required'));
        return;
      }
      if (event.request.categoryName.length > 255) {
        emit(AssetError('Category name must be less than 255 characters'));
        return;
      }

      // Edge case: Check internet connection
      final connectivityResult = await _connectivity.checkConnectivity();
      if (connectivityResult == ConnectivityResult.none) {
        emit(AssetError('No internet connection. Please check your network.'));
        return;
      }

      final category = await _assetDataSource.createCategory(event.request);
      emit(CategoryCreated(category: category));
      AppLogger.info('Category created: ${category.categoryId}');
    } on ApiException catch (e) {
      AppLogger.error('Create category failed: ${e.message}');
      emit(AssetError(e.userMessage));
    } catch (e) {
      AppLogger.error('Unexpected create category error: $e');
      emit(AssetError('Failed to create category. Please try again.'));
    }
  }

  // ============================================================
  // BULK CREATE CATEGORIES
  // ============================================================
  Future<void> _onBulkCreateCategories(
    BulkCreateCategoriesEvent event,
    Emitter<AssetState> emit,
  ) async {
    emit(AssetLoading());

    try {
      // Edge case: Validate list
      if (event.requests.isEmpty) {
        emit(AssetError('At least one category is required'));
        return;
      }
      if (event.requests.length > 100) {
        emit(AssetError('Cannot create more than 100 categories at once'));
        return;
      }

      // Edge case: Validate each category
      for (var i = 0; i < event.requests.length; i++) {
        final request = event.requests[i];
        if (request.categoryName.isEmpty) {
          emit(AssetError('Category name is required at index $i'));
          return;
        }
      }

      // Edge case: Check internet connection
      final connectivityResult = await _connectivity.checkConnectivity();
      if (connectivityResult == ConnectivityResult.none) {
        emit(AssetError('No internet connection. Please check your network.'));
        return;
      }

      final response = await _assetDataSource.bulkCreateCategories(event.requests);
      emit(CategoriesBulkCreated(response: response));
      AppLogger.info('Bulk created categories: ${response.success}/${response.total}');
    } on ApiException catch (e) {
      AppLogger.error('Bulk create categories failed: ${e.message}');
      emit(AssetError(e.userMessage));
    } catch (e) {
      AppLogger.error('Unexpected bulk create categories error: $e');
      emit(AssetError('Failed to create categories. Please try again.'));
    }
  }

  // ============================================================
  // UPLOAD CATEGORIES EXCEL
  // ============================================================
  Future<void> _onUploadCategoriesExcel(
    UploadCategoriesExcelEvent event,
    Emitter<AssetState> emit,
  ) async {
    emit(AssetLoading());

    try {
      // Edge case: Validate file path
      if (event.filePath.isEmpty) {
        emit(AssetError('File path is required'));
        return;
      }

      // Edge case: Check internet connection
      final connectivityResult = await _connectivity.checkConnectivity();
      if (connectivityResult == ConnectivityResult.none) {
        emit(AssetError('No internet connection. Please check your network.'));
        return;
      }

      final response = await _assetDataSource.uploadCategoriesExcel(event.filePath);
      emit(CategoriesExcelUploaded(response: response));
      AppLogger.info('Excel uploaded: ${response.success}/${response.total}');
    } on ApiException catch (e) {
      AppLogger.error('Upload Excel failed: ${e.message}');
      emit(AssetError(e.userMessage));
    } catch (e) {
      AppLogger.error('Unexpected upload Excel error: $e');
      emit(AssetError('Failed to upload Excel file. Please try again.'));
    }
  }
}

// ============================================================
// EVENTS
// ============================================================
abstract class AssetEvent extends Equatable {
  const AssetEvent();

  @override
  List<Object?> get props => [];
}

class LoadAssetsEvent extends AssetEvent {
  final int? page;
  final int? size;
  final String? sort;

  LoadAssetsEvent({this.page, this.size, this.sort});

  @override
  List<Object?> get props => [page, size, sort];
}

class LoadAssetByIdEvent extends AssetEvent {
  final int assetId;

  LoadAssetByIdEvent(this.assetId);

  @override
  List<Object?> get props => [assetId];
}

class CreateAssetEvent extends AssetEvent {
  final AssetRequest request;

  CreateAssetEvent(this.request);

  @override
  List<Object?> get props => [request];
}

class UpdateAssetEvent extends AssetEvent {
  final int assetId;
  final AssetRequest request;

  UpdateAssetEvent({
    required this.assetId,
    required this.request,
  });

  @override
  List<Object?> get props => [assetId, request];
}

class DeleteAssetEvent extends AssetEvent {
  final int assetId;

  DeleteAssetEvent(this.assetId);

  @override
  List<Object?> get props => [assetId];
}

class LoadCategoriesEvent extends AssetEvent {}

class CreateCategoryEvent extends AssetEvent {
  final CategoryRequest request;

  CreateCategoryEvent(this.request);

  @override
  List<Object?> get props => [request];
}

class BulkCreateCategoriesEvent extends AssetEvent {
  final List<CategoryRequest> requests;

  BulkCreateCategoriesEvent(this.requests);

  @override
  List<Object?> get props => [requests];
}

class UploadCategoriesExcelEvent extends AssetEvent {
  final String filePath;

  UploadCategoriesExcelEvent(this.filePath);

  @override
  List<Object?> get props => [filePath];
}

// ============================================================
// STATES
// ============================================================
abstract class AssetState extends Equatable {
  const AssetState();

  @override
  List<Object?> get props => [];
}

class AssetInitial extends AssetState {}

class AssetLoading extends AssetState {}

class AssetsLoaded extends AssetState {
  final List<AssetMaster> assets;

  AssetsLoaded({required this.assets});

  @override
  List<Object?> get props => [assets];
}

class AssetLoaded extends AssetState {
  final AssetMaster asset;

  AssetLoaded({required this.asset});

  @override
  List<Object?> get props => [asset];
}

class AssetCreated extends AssetState {
  final AssetMaster asset;

  AssetCreated({required this.asset});

  @override
  List<Object?> get props => [asset];
}

class AssetUpdated extends AssetState {
  final AssetMaster asset;

  AssetUpdated({required this.asset});

  @override
  List<Object?> get props => [asset];
}

class AssetDeleted extends AssetState {
  final int assetId;

  AssetDeleted({required this.assetId});

  @override
  List<Object?> get props => [assetId];
}

class AssetError extends AssetState {
  final String message;

  AssetError(this.message);

  @override
  List<Object?> get props => [message];
}

class CategoriesLoaded extends AssetState {
  final List<Category> categories;

  CategoriesLoaded({required this.categories});

  @override
  List<Object?> get props => [categories];
}

class CategoryCreated extends AssetState {
  final Category category;

  CategoryCreated({required this.category});

  @override
  List<Object?> get props => [category];
}

class CategoriesBulkCreated extends AssetState {
  final BulkUploadResponse<Category> response;

  CategoriesBulkCreated({required this.response});

  @override
  List<Object?> get props => [response];
}

class CategoriesExcelUploaded extends AssetState {
  final BulkUploadResponse<Category> response;

  CategoriesExcelUploaded({required this.response});

  @override
  List<Object?> get props => [response];
}

