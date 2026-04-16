import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:keeply_app/core/api/keeply_service_url.dart';
import 'package:keeply_app/core/exceptions/api_exception.dart';
import 'package:keeply_app/core/theme/keeply_tokens.dart';
import 'package:keeply_app/features/auth/data/datasources/auth_remote_datasource.dart';
import 'package:keeply_app/features/auth/data/models/auth_models.dart';

/// Loads `GET /api/auth/profile/me` — account hub “Profile” entry.
class AccountProfilePage extends StatefulWidget {
  const AccountProfilePage({super.key});

  @override
  State<AccountProfilePage> createState() => _AccountProfilePageState();
}

class _AccountProfilePageState extends State<AccountProfilePage> {
  final _ds = AuthRemoteDataSource();
  bool _loading = true;
  String? _err;
  UserDto? _user;

  @override
  void initState() {
    super.initState();
    _load();
  }

  String? _resolvePhoto(String? profilePhotoUrl) {
    if (profilePhotoUrl == null || profilePhotoUrl.isEmpty) return null;
    if (profilePhotoUrl.startsWith('http://') || profilePhotoUrl.startsWith('https://')) {
      return profilePhotoUrl;
    }
    final base = keeplyServiceBase(KeeplyApiService.auth);
    final path = profilePhotoUrl.startsWith('/') ? profilePhotoUrl : '/$profilePhotoUrl';
    return '$base$path';
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _err = null;
    });
    try {
      final u = await _ds.getCurrentUser();
      if (!mounted) return;
      setState(() {
        _user = u;
        _loading = false;
      });
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _loading = false;
        _err = e is ApiException ? e.userMessage : (e is Exception ? e.toString() : 'Could not load profile');
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    final u = _user;
    return Scaffold(
      appBar: AppBar(title: const Text('Profile')),
      body: RefreshIndicator(
        onRefresh: _load,
        child: ListView(
          padding: const EdgeInsets.all(18),
          physics: const AlwaysScrollableScrollPhysics(),
          children: [
            if (_loading)
              const Padding(
                padding: EdgeInsets.all(32),
                child: Center(child: CircularProgressIndicator(strokeWidth: 2)),
              )
            else if (_err != null)
              Text(_err!, style: t.bodyMedium?.copyWith(color: KeeplyTokens.danger))
            else if (u != null) ...[
              Center(
                child: CircleAvatar(
                  radius: 52,
                  backgroundColor: Theme.of(context).colorScheme.primary.withValues(alpha: 0.15),
                  child: ClipOval(
                    child: _resolvePhoto(u.profilePhotoUrl) != null
                        ? CachedNetworkImage(
                            imageUrl: _resolvePhoto(u.profilePhotoUrl)!,
                            width: 104,
                            height: 104,
                            fit: BoxFit.cover,
                            memCacheWidth: 220,
                            placeholder: (_, __) => const Padding(
                              padding: EdgeInsets.all(28),
                              child: CircularProgressIndicator(strokeWidth: 2),
                            ),
                            errorWidget: (_, __, ___) => Text(
                              (u.username ?? 'U').isNotEmpty ? (u.username!.substring(0, 1).toUpperCase()) : 'U',
                              style: TextStyle(
                                fontSize: 40,
                                fontWeight: FontWeight.w800,
                                color: Theme.of(context).colorScheme.primary,
                              ),
                            ),
                          )
                        : Text(
                            (u.username ?? 'U').isNotEmpty ? (u.username!.substring(0, 1).toUpperCase()) : 'U',
                            style: TextStyle(
                              fontSize: 40,
                              fontWeight: FontWeight.w800,
                              color: Theme.of(context).colorScheme.primary,
                            ),
                          ),
                  ),
                ),
              ),
              const SizedBox(height: 16),
              Text('Account', style: t.titleSmall?.copyWith(color: KeeplyTokens.muted)),
              const SizedBox(height: 6),
              Text(u.username ?? '—', style: t.headlineSmall?.copyWith(fontWeight: FontWeight.w800)),
              const SizedBox(height: 20),
              _tile(t, 'User ID', '${u.userId}'),
              if (u.email != null && u.email!.trim().isNotEmpty) _tile(t, 'Email', u.email!),
              if (u.mobile != null && u.mobile!.trim().isNotEmpty) _tile(t, 'Mobile', u.mobile!),
              if (u.projectType != null && u.projectType!.trim().isNotEmpty) _tile(t, 'Project', u.projectType!),
            ],
          ],
        ),
      ),
    );
  }

  Widget _tile(TextTheme t, String label, String value) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 14),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(label, style: t.labelLarge?.copyWith(color: KeeplyTokens.muted)),
          const SizedBox(height: 4),
          Text(value, style: t.bodyLarge),
        ],
      ),
    );
  }
}
