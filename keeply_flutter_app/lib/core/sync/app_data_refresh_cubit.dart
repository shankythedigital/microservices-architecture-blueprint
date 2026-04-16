import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

/// Domains whose server-backed lists should reload across tabs after a mutation.
enum KeeplyDataChannel {
  assets,
  helpdesk,
}

/// Lightweight cross-tab refresh signal (no caching layer — screens keep their own fetch logic).
class AppDataRefreshState extends Equatable {
  const AppDataRefreshState({
    this.assetsTick = 0,
    this.helpdeskTick = 0,
  });

  final int assetsTick;
  final int helpdeskTick;

  AppDataRefreshState copyWith({int? assetsTick, int? helpdeskTick}) {
    return AppDataRefreshState(
      assetsTick: assetsTick ?? this.assetsTick,
      helpdeskTick: helpdeskTick ?? this.helpdeskTick,
    );
  }

  @override
  List<Object?> get props => [assetsTick, helpdeskTick];
}

class AppDataRefreshCubit extends Cubit<AppDataRefreshState> {
  AppDataRefreshCubit() : super(const AppDataRefreshState());

  void bump(KeeplyDataChannel channel) {
    switch (channel) {
      case KeeplyDataChannel.assets:
        emit(state.copyWith(assetsTick: state.assetsTick + 1));
      case KeeplyDataChannel.helpdesk:
        emit(state.copyWith(helpdeskTick: state.helpdeskTick + 1));
    }
  }
}
