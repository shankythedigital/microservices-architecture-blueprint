Auth Service scaffold created by setup-auth-service.sh

Steps:
1. Update application.yml or set env: DB credentials, ENCRYPTION_KEY(32 bytes), HMAC_KEY, JWT_SECRET
2. Create DB: CREATE DATABASE authdb;
3. mvn -f auth-service/pom.xml clean package
4. java -jar auth-service/target/auth-service-0.0.1-SNAPSHOT.jar

Notes:
- Replace secrets with secure values and use a secrets manager (Vault/KMS).
- Implement SMS/email provider for OTP.
- WebAuthn/passkey requires front-end and a FIDO2 server library.

echo "✔️ setup-auth-service.sh finished. Project created at ./auth-service"
echo "Next steps: edit application.yml to provide real secrets and DB credentials, then build."

