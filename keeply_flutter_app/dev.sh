#!/bin/sh
# Run flutter with a known-good SDK path if PATH is broken in your terminal.
set -e
export PATH="/Users/neilnaik/development/bin:$PATH"
cd "$(dirname "$0")"
exec flutter "$@"
