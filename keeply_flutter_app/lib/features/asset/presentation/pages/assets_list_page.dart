import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:keeply_app/features/asset/presentation/bloc/asset_bloc.dart';

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
    return Scaffold(
      appBar: AppBar(
        title: const Text('Assets'),
        actions: [
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
              child: ListView.builder(
                controller: _scrollController,
                itemCount: state.assets.length + 1,
                itemBuilder: (context, index) {
                  if (index == state.assets.length) {
                    return const Padding(
                      padding: EdgeInsets.all(16.0),
                      child: Center(child: CircularProgressIndicator()),
                    );
                  }

                  final asset = state.assets[index];
                  return Card(
                    margin: const EdgeInsets.symmetric(
                      horizontal: 16,
                      vertical: 8,
                    ),
                    child: ListTile(
                      leading: const Icon(Icons.inventory_2),
                      title: Text(asset.assetNameUdv),
                      subtitle: Text(
                        'ID: ${asset.assetId ?? 'N/A'}',
                      ),
                      trailing: const Icon(Icons.chevron_right),
                      onTap: () {
                        // Navigate to asset details
                      },
                    ),
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
          // Navigate to create asset
        },
        child: const Icon(Icons.add),
      ),
    );
  }
}

