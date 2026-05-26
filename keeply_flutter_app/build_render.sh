#!/usr/bin/env bash
# Build or run the app against the deployed Render services.
# Does not change default dev behavior: only applies when you use this script.
#
# Usage:
#   ./build_render.sh                 # flutter build apk + defines
#   ./build_render.sh appbundle|ios|web|...
#   ./build_render.sh run             # flutter run + defines
#   ./build_render.sh run -d chrome   # extra args after "run" are forwarded
# Build extras: ./build_render.sh apk --obfuscate --split-debug-info=...
set -euo pipefail
cd "$(dirname "$0")"

RENDER_DEFINES=(
  --dart-define=AUTH_SERVICE_URL=https://auth-service-7qm8.onrender.com
  --dart-define=NOTIFICATION_SERVICE_URL=https://notification-service-ckqu.onrender.com
  --dart-define=ASSET_SERVICE_URL=https://asset-service-y2xk.onrender.com
  --dart-define=HELPDESK_SERVICE_URL=https://helpdesk-service-2pfu.onrender.com
)

if [ "${1:-}" = "run" ]; then
  shift
  exec flutter run "${RENDER_DEFINES[@]}" "$@"
fi

TARGET="${1:-apk}"
[ "$#" -ge 1 ] && shift
exec flutter build "$TARGET" "${RENDER_DEFINES[@]}" "$@"
