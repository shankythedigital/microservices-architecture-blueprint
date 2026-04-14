import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:keeply_app/core/view_layout/view_layout_scope.dart';
import 'package:keeply_app/features/auth/presentation/bloc/auth_bloc.dart';
import 'package:keeply_app/features/asset/presentation/pages/assets_list_page.dart';
import 'package:keeply_app/features/asset/presentation/pages/asset_scan_page.dart';
import 'package:keeply_app/features/asset/presentation/pages/create_asset_page.dart';

/// Home Page
/// Main dashboard after authentication
class HomePage extends StatelessWidget {
  const HomePage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Keeply'),
        actions: [
          const Padding(
            padding: EdgeInsets.only(right: 4),
            child: ViewLayoutToggle(compact: true),
          ),
          IconButton(
            icon: const Icon(Icons.logout),
            onPressed: () {
              context.read<AuthBloc>().add(LogoutEvent());
            },
            tooltip: 'Logout',
          ),
        ],
      ),
      body: BlocBuilder<AuthBloc, AuthState>(
        builder: (context, state) {
          if (state is AuthAuthenticated) {
            return _buildDashboard(context, state.user);
          }
          return const Center(child: CircularProgressIndicator());
        },
      ),
    );
  }

  Widget _buildDashboard(BuildContext context, user) {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Welcome Card
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Welcome, ${user.username ?? 'User'}!',
                    style: Theme.of(context).textTheme.headlineSmall,
                  ),
                  const SizedBox(height: 8),
                  Text(
                    'Manage your assets efficiently',
                    style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                          color: Colors.grey[600],
                        ),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 24),
          // Quick Actions
          Text(
            'Quick Actions',
            style: Theme.of(context).textTheme.titleLarge?.copyWith(
                  fontWeight: FontWeight.bold,
                ),
          ),
          const SizedBox(height: 16),
          ListenableBuilder(
            listenable: ViewLayoutScope.notifierOf(context),
            builder: (context, _) {
              final actions = <({IconData icon, String title, VoidCallback onTap})>[
                (
                  icon: Icons.inventory_2,
                  title: 'Assets',
                  onTap: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (_) => const AssetsListPage(),
                      ),
                    );
                  },
                ),
                (
                  icon: Icons.category,
                  title: 'Categories',
                  onTap: () {},
                ),
                (
                  icon: Icons.qr_code_scanner,
                  title: 'Scan Asset',
                  onTap: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (_) => const AssetScanPage(),
                      ),
                    );
                  },
                ),
                (
                  icon: Icons.add_circle,
                  title: 'Add Asset',
                  onTap: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (_) => const CreateAssetPage(),
                      ),
                    );
                  },
                ),
                (
                  icon: Icons.assignment,
                  title: 'Compliance',
                  onTap: () {},
                ),
              ];
              if (ViewLayoutScope.modeOf(context) == ViewLayoutMode.list) {
                return Column(
                  children: [
                    for (final a in actions)
                      Card(
                        margin: const EdgeInsets.only(bottom: 8),
                        child: ListTile(
                          leading: Icon(a.icon, color: Theme.of(context).primaryColor),
                          title: Text(a.title),
                          trailing: const Icon(Icons.chevron_right),
                          onTap: a.onTap,
                        ),
                      ),
                  ],
                );
              }
              return GridView.count(
                crossAxisCount: 2,
                shrinkWrap: true,
                physics: const NeverScrollableScrollPhysics(),
                crossAxisSpacing: 16,
                mainAxisSpacing: 16,
                childAspectRatio: 1.5,
                children: [
                  for (final a in actions)
                    _buildActionCard(
                      context,
                      icon: a.icon,
                      title: a.title,
                      onTap: a.onTap,
                    ),
                ],
              );
            },
          ),
        ],
      ),
    );
  }

  Widget _buildActionCard(
    BuildContext context, {
    required IconData icon,
    required String title,
    required VoidCallback onTap,
  }) {
    return Card(
      elevation: 2,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(icon, size: 40, color: Theme.of(context).primaryColor),
              const SizedBox(height: 8),
              Text(
                title,
                style: Theme.of(context).textTheme.titleMedium,
                textAlign: TextAlign.center,
              ),
            ],
          ),
        ),
      ),
    );
  }
}

