import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:keeply_app/core/view_layout/view_layout_scope.dart';
import 'package:keeply_app/core/widgets/keeply_asset_views.dart';
import 'package:keeply_app/features/asset/presentation/bloc/asset_bloc.dart';
import 'package:keeply_app/features/asset/presentation/pages/asset_detail_page.dart';
import 'package:keeply_app/features/asset/presentation/pages/create_asset_page.dart';

/// Assets List Page
/// Displays list of assets with pagination and error handling
class AssetsListPage extends StatefulWidget {
  const AssetsListPage({super.key});

  @override
  State<AssetsListPage> createState() => _AssetsListPageState();
}

class _AssetsListPageState extends State<AssetsListPage> {
  final ScrollController _scrollController = ScrollController();
  int _currentPage = 0;
  final int _pageSize = 20;

  @override
  void initState() {
    super.initState();
    context.read<AssetBloc>().add(LoadAssetsEvent(page: 0, size: _pageSize));

    // Infinite scroll
    _scrollController.addListener(() {
      if (_scrollController.position.pixels >=
          _scrollController.position.maxScrollExtent * 0.8) {
        _loadMore();
      }
    });
  }

  void _loadMore() {
    _currentPage++;
    context.read<AssetBloc>().add(
          LoadAssetsEvent(page: _currentPage, size: _pageSize),
        );
  }

  @override
  void dispose() {
    _scrollController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return BlocListener<AssetBloc, AssetState>(
      listenWhen: (previous, current) =>
          current is AssetCreated ||
          current is AssetUpdated ||
          current is AssetDeleted,
      listener: (context, state) {
        _currentPage = 0;
        context.read<AssetBloc>().add(LoadAssetsEvent(page: 0, size: _pageSize));
      },
      child: Scaffold(
      appBar: AppBar(
        title: const Text('Assets'),
        actions: [
          const Padding(
            padding: EdgeInsets.only(right: 4),
            child: ViewLayoutToggle(compact: true),
          ),
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () {
              _currentPage = 0;
              context.read<AssetBloc>().add(
                    LoadAssetsEvent(page: 0, size: _pageSize),
                  );
            },
          ),
        ],
      ),
      body: BlocBuilder<AssetBloc, AssetState>(
        builder: (context, state) {
          if (state is AssetLoading && _currentPage == 0) {
            return const Center(child: CircularProgressIndicator());
          }

          if (state is AssetError) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(
                    Icons.error_outline,
                    size: 64,
                    color: Colors.red[300],
                  ),
                  const SizedBox(height: 16),
                  Text(
                    state.message,
                    style: const TextStyle(fontSize: 16),
                    textAlign: TextAlign.center,
                  ),
                  const SizedBox(height: 16),
                  ElevatedButton(
                    onPressed: () {
                      _currentPage = 0;
                      context.read<AssetBloc>().add(
                            LoadAssetsEvent(page: 0, size: _pageSize),
                          );
                    },
                    child: const Text('Retry'),
                  ),
                ],
              ),
            );
          }

          if (state is AssetsLoaded) {
            if (state.assets.isEmpty) {
              return const Center(
                child: Text('No assets found'),
              );
            }

            return RefreshIndicator(
              onRefresh: () async {
                _currentPage = 0;
                context.read<AssetBloc>().add(
                      LoadAssetsEvent(page: 0, size: _pageSize),
                    );
              },
              child: ListenableBuilder(
                listenable: ViewLayoutScope.notifierOf(context),
                builder: (context, _) {
                  final assets = state.assets;
                  final listMode = ViewLayoutScope.modeOf(context) == ViewLayoutMode.list;
                  if (listMode) {
                    return ListView.builder(
                      controller: _scrollController,
                      itemCount: assets.length + 1,
                      itemBuilder: (context, index) {
                        if (index == assets.length) {
                          return const Padding(
                            padding: EdgeInsets.all(16.0),
                            child: Center(child: CircularProgressIndicator()),
                          );
                        }
                        final asset = assets[index];
                        return Padding(
                          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                          child: KeeplyAssetListRow(
                            asset: asset,
                            onTap: () => AssetDetailPage.pushIfValid(context, asset.assetId),
                          ),
                        );
                      },
                    );
                  }
                  return GridView.builder(
                    controller: _scrollController,
                    padding: const EdgeInsets.fromLTRB(12, 8, 12, 24),
                    gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                      crossAxisCount: 2,
                      mainAxisSpacing: 12,
                      crossAxisSpacing: 12,
                      childAspectRatio: 0.78,
                    ),
                    itemCount: assets.length + 1,
                    itemBuilder: (context, index) {
                      if (index == assets.length) {
                        return const Center(
                          child: Padding(
                            padding: EdgeInsets.all(16),
                            child: CircularProgressIndicator(),
                          ),
                        );
                      }
                      final asset = assets[index];
                      return KeeplyAssetGridCard(
                        asset: asset,
                        onTap: () => AssetDetailPage.pushIfValid(context, asset.assetId),
                      );
                    },
                  );
                },
              ),
            );
          }

          return const Center(child: Text('No data'));
        },
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () {
          Navigator.of(context).push<void>(
            MaterialPageRoute<void>(builder: (_) => const CreateAssetPage()),
          );
        },
        child: const Icon(Icons.add),
      ),
    ),
    );
  }
}

