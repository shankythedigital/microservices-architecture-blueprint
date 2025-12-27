import 'dart:async';

/// Throttler
/// Throttles function calls to limit execution frequency
class Throttler {
  final Duration delay;
  Timer? _timer;
  bool _isThrottled = false;

  Throttler({this.delay = const Duration(milliseconds: 300)});

  void call(VoidCallback callback) {
    if (!_isThrottled) {
      callback();
      _isThrottled = true;
      _timer = Timer(delay, () {
        _isThrottled = false;
      });
    }
  }

  void cancel() {
    _timer?.cancel();
    _timer = null;
    _isThrottled = false;
  }

  void dispose() {
    cancel();
  }
}

