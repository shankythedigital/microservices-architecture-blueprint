#!/usr/bin/env bash
set -euo pipefail

# Output directory
OUT_DIR="rsa-keys"
mkdir -p "$OUT_DIR"

# Step 1. Generate RSA Private Key (2048-bit)
openssl genrsa -out "$OUT_DIR/private.pem" 2048

# Step 2. Extract Public Key (PEM, X.509)
openssl rsa -in "$OUT_DIR/private.pem" -pubout -out "$OUT_DIR/public.pem"

# Step 3. Export Public Key (Base64 single-line, no headers/footers)
PUBKEY=$(awk 'NR>1 && !/-----/ {printf $0}' "$OUT_DIR/public.pem")

# Step 4. Save to a file for Postman use
echo -n "$PUBKEY" > "$OUT_DIR/publicKey.base64.txt"

echo "✔️ RSA keypair generated in $OUT_DIR/"
echo "   - private.pem (keep safe, client-side signing)"
echo "   - public.pem  (standard PEM format)"
echo "   - publicKey.base64.txt (use this string in Postman Register Credential)"
echo ""
echo "Postman body example:"
echo "{"
echo "  \"userId\": 1,"
echo "  \"type\": \"RSA\","
echo "  \"credentialId\": \"rsa-1\","
echo "  \"publicKey\": \"$PUBKEY\""
echo "}"
