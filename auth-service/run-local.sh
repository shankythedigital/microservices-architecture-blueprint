#!/usr/bin/env bash
# Run auth-service from the repo root with variables from auth-service/.env
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ENV_FILE="$(cd "$(dirname "$0")" && pwd)/.env"
if [[ ! -f "$ENV_FILE" ]]; then
  echo "No auth-service/.env — copying from .env.example (edit values as needed)."
  cp "$(dirname "$0")/.env.example" "$ENV_FILE"
fi
set -a
# shellcheck disable=SC1090
source "$ENV_FILE"
set +a
cd "$ROOT"
exec mvn -pl auth-service spring-boot:run
