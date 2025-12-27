#!/usr/bin/env bash
set -euo pipefail

if [ $# -ne 1 ]; then
  echo "Usage: $0 <challenge-string>"
  exit 1
fi

CHALLENGE=$1
OUT_DIR="keys"
PRIVATE_PEM="jwt-private.pem"

mkdir -p "$OUT_DIR"

if [ ! -f "$OUT_DIR/$PRIVATE_PEM" ]; then
  echo "❌ Error: $OUT_DIR/$PRIVATE_PEM not found. Run generate-rsa-keypair.sh first."
  exit 1
fi

# Sign the challenge with SHA256 + RSA using $PRIVATE_PEM
SIGNATURE=$(echo -n "$CHALLENGE" \
  | openssl dgst -sha256 -sign "$OUT_DIR/$PRIVATE_PEM" \
  | openssl base64 -A)

echo "✔️ Challenge signed successfully"
echo "Signature (Base64):"
echo "$SIGNATURE"

# Save to file
echo -n "$SIGNATURE" > "$OUT_DIR/signature.base64.txt"
echo "Saved to $OUT_DIR/signature.base64.txt"

echo ""
echo "👉 Use this signature in Postman /auth/credential/rsa/verify body:"
echo "{"
echo "  \"userId\": 1,"
echo "  \"challenge\": \"$CHALLENGE\","
echo "  \"signature\": \"$SIGNATURE\""
echo "}"
