#!/bin/bash
# ======================================================
# 🧩 generate-application-yml.sh
# Generates application.yml for:
#   - Auth Service
#   - Asset Service
#   - Notification Service
#
# EXACT YAML output (duplicate keys preserved)
# Supports: local, cloud_local, cloud
# ======================================================

set -euo pipefail

MODE="${1:-local}"
REGION="ap-south-1"

ROOT_DIR="$(pwd)"
AUTH_DIR="$ROOT_DIR/auth-service"
ASSET_DIR="$ROOT_DIR/asset-service"
NOTIF_DIR="$ROOT_DIR/notification-service"

# ======================================================
# 🔧 CONFIGURATION SWITCH
# ======================================================
if [ "$MODE" = "cloud" ]; then
  echo "🌩️ Generating Cloud YAML..."

  RDS_HOST="db-auth.c5csym0gc4my.ap-south-1.rds.amazonaws.com"

  RDS_AUTH_DB="jdbc:mysql://${RDS_HOST}:3306/authdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
  RDS_ASSET_DB="jdbc:mysql://${RDS_HOST}:3306/assetdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
  RDS_NOTIFY_DB="jdbc:mysql://${RDS_HOST}:3306/notificationdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"

  RDS_AUTH_USER="adminAuth"
  RDS_ASSET_USER="adminAuth"
  RDS_NOTIFY_USER="adminAuth"

  RDS_AUTH_PASS="AuthPass123"
  RDS_ASSET_PASS="AuthPass123"
  RDS_NOTIFY_PASS="AuthPass123"
  

  # ===== Cloud ports (local debug mode of EB) =====
  COMMON_PORT="6000"
  AUTH_PORT="6001"
  NOTIF_PORT="6002"
  ASSET_PORT="6003"

  # ===== CLOUD SERVICE IPs =====
  COMMON_AUTH_IP="13.127.199.97"
  AUTH_IP="13.127.199.97"
  ASSET_IP="13.127.199.97"
  NOTIF_IP="13.233.230.24"

  COMMON_AUTH_URL="http://auth-service-env.${REGION}.elasticbeanstalk.com"
  AUTH_URL="http://${AUTH_IP}:${AUTH_PORT}"
  ASSET_URL="http://${ASSET_IP}:${ASSET_PORT}"
  NOTIF_URL="http://${NOTIF_IP}:${NOTIF_PORT}"

elif [ "$MODE" = "cloud_local" ]; then
  echo "🌥️ Generating Cloud DB + Local Services..."

  RDS_HOST="db-auth.c5csym0gc4my.ap-south-1.rds.amazonaws.com"

  RDS_AUTH_DB="jdbc:mysql://${RDS_HOST}:3306/authdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
  RDS_ASSET_DB="jdbc:mysql://${RDS_HOST}:3306/assetdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
  RDS_NOTIFY_DB="jdbc:mysql://${RDS_HOST}:3306/notificationdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"

  RDS_AUTH_USER="adminAuth"
  RDS_ASSET_USER="adminAuth"
  RDS_NOTIFY_USER="adminAuth"

  RDS_AUTH_PASS="AuthPass123"
  RDS_ASSET_PASS="AuthPass123"
  RDS_NOTIFY_PASS="AuthPass123"

  AUTH_PORT="8081"
  NOTIF_PORT="8082"
  ASSET_PORT="8083"

  AUTH_URL="http://localhost:${AUTH_PORT}"
  ASSET_URL="http://localhost:${ASSET_PORT}"
  NOTIF_URL="http://localhost:${NOTIF_PORT}"

else
  echo "💻 Generating Local YAML..."

  RDS_AUTH_DB="jdbc:mysql://localhost:3306/authdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
  RDS_ASSET_DB="jdbc:mysql://localhost:3306/assetdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
  RDS_NOTIFY_DB="jdbc:mysql://localhost:3306/notificationdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"

  RDS_AUTH_USER="root"
  RDS_ASSET_USER="root"
  RDS_NOTIFY_USER="root"

  RDS_AUTH_PASS="Snmysql@1110"
  RDS_ASSET_PASS="Snmysql@1110"
  RDS_NOTIFY_PASS="Snmysql@1110"

  AUTH_PORT="8081"
  NOTIF_PORT="8082"
  ASSET_PORT="8083"

  AUTH_URL="http://localhost:${AUTH_PORT}"
  ASSET_URL="http://localhost:${ASSET_PORT}"
  NOTIF_URL="http://localhost:${NOTIF_PORT}"
fi

JWT_SECRET="yNnC7M3ZqgV4bD0lFJm9Q2w5tSe8XpR1pWc7UjK4oHs="

mkdir -p "$AUTH_DIR/src/main/resources" "$ASSET_DIR/src/main/resources" "$NOTIF_DIR/src/main/resources"

# ======================================================
# 🔐 AUTH-SERVICE YAML (EXACT AS PROVIDED)
# ======================================================
cat > "$AUTH_DIR/src/main/resources/application.yml" <<YAML
# Auth-service
server:
  port: ${AUTH_PORT}  # CLOUD uses EB port, LOCAL uses 8081

auth:
  service:
    url: ${AUTH_URL}

notification:
  service:
    url: ${NOTIF_URL}/api/notifications

asset:
  service:
    url: ${ASSET_URL}

spring:
  application:
    name: auth-service
  datasource:
    url: ${RDS_AUTH_DB}
    username: ${RDS_AUTH_USER}
    password: ${RDS_AUTH_PASS}
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false

common:
  notification:
    enabled: true

JWT_PRIVATE_KEY_PATH: classpath:keys/jwt-private.pem
JWT_PUBLIC_KEY_PATH: classpath:keys/jwt-public.pem
JWT_SECRET: ${JWT_SECRET}
JWT_ACCESS_TOKEN_VALIDITY_SECONDS: 900
JWT_REFRESH_TOKEN_VALIDITY_SECONDS: 1209600
AUTH_ENC_KEY: SLOqKf8lS2hidTDsXQe25ZSaoaGcczUX6gySXUxjE1M=
AUTH_HMAC_KEY: krFcA7/MYPXQWbtSGMM87Dzxu2euOsRckVFeUyOC6dw=
ENCRYPTION_KEY: SLOqKf8lS2hidTDsXQe25ZSaoaGcczUX6gySXUxjE1M=
ACCESS_TOKEN: change_this_token
FEIGN_ACCESS_TOKEN:
logging:
  level:
    com.example: DEBUG

# Swagger/OpenAPI Configuration
springdoc:
  api-docs:
    path: /api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    operations-sorter: method
    tags-sorter: alpha
    try-it-out-enabled: true
  show-actuator: false
  group-configs:
    - group: 'auth-service'
      display-name: 'Auth Service API'
      paths-to-match: '/api/**'
YAML

echo "✅ Auth-service YAML generated"

# ======================================================
# 🧱 ASSET-SERVICE YAML (EXACT AS PROVIDED)
# ======================================================
cat > "$ASSET_DIR/src/main/resources/application.yml" <<YAML
# Asset-service
server:
  port: ${ASSET_PORT}

auth:
  service:
    url: ${AUTH_URL}
notification:
  service:
    url: ${NOTIF_URL}/api/notifications
asset:
  service:
    url: ${ASSET_URL}
  upload:
    dir: /asset-uploads/amc-docs

spring:
  application:
    name: asset-service
  datasource:
    url: ${RDS_ASSET_DB}
    username: ${RDS_ASSET_USER}
    password: ${RDS_ASSET_PASS}
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
  servlet:
    multipart:
      enabled: true
      max-file-size: 50MB
      max-request-size: 50MB

services:
  auth:
    base-url: ${AUTH_URL}
  notification:
    base-url: ${NOTIF_URL}

security:
  jwt:
    public-key-path: classpath:keys/jwt-private.pem
    issuer: "auth-service"
    audience: "asset-service"

common:
  notification:
    enabled: true

JWT_PUBLIC_KEY_PATH: classpath:keys/jwt-public.pem
JWT_SECRET: ${JWT_SECRET}
AUTH_ENC_KEY: SLOqKf8lS2hidTDsXQe25ZSaoaGcczUX6gySXUxjE1M=
AUTH_HMAC_KEY: krFcA7/MYPXQWbtSGMM87Dzxu2euOsRckVFeUyOC6dw=
AUTH_SERVICE_URL: ${AUTH_URL}
NOTIFICATION_SERVICE_URL: ${NOTIF_URL}

# Swagger/OpenAPI Configuration
springdoc:
  api-docs:
    path: /api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    operations-sorter: method
    tags-sorter: alpha
    try-it-out-enabled: true
  show-actuator: false
  group-configs:
    - group: 'asset-service'
      display-name: 'Asset Service API'
      paths-to-match: '/api/**'
YAML

echo "✅ Asset-service YAML generated"

# ======================================================
# 📢 NOTIFICATION-SERVICE YAML (EXACT AS PROVIDED)
# ======================================================
cat > "$NOTIF_DIR/src/main/resources/application.yml" <<YAML
# Notification-service
server:
  port: ${NOTIF_PORT}

notification:
  service:
    url: ${NOTIF_URL}/api/notifications

asset:
  service:
    url: ${ASSET_URL}
  upload:
    dir: /asset-uploads/amc-docs

fileupload:
  type: standard

spring:
  application:
    name: notification-service
  datasource:
    url: ${RDS_NOTIFY_DB}
    username: ${RDS_NOTIFY_USER}
    password: ${RDS_NOTIFY_PASS}
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false

auth:
  service:
    url: ${AUTH_URL}/api/
  client-id: notification-service
  client-secret: notify-secret

notify:
  enc:
    key: yfwZM8WwHraV8LhcSNFZ7UuIpLwxpX6lthpH4CflI3U=
  hmac:
    key: SyHeAe8KeKETQihKAGFfpKipF9mysIjTsh01NaDiDpc=

jwt:
  public-key-path: classpath:keys/jwt-public.pem

JWT_SECRET: ${JWT_SECRET}
JWT_PUBLIC_KEY_PATH: classpath:keys/jwt-public.pem
JWT_ACCESS_TOKEN_VALIDITY_SECONDS: 900
JWT_REFRESH_TOKEN_VALIDITY_SECONDS: 1209600

logging:
  level:
    com.example: DEBUG

common:
  notification:
    enabled: true

# Swagger/OpenAPI Configuration
springdoc:
  api-docs:
    path: /api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    operations-sorter: method
    tags-sorter: alpha
    try-it-out-enabled: true
  show-actuator: false
  group-configs:
    - group: 'notification-service'
      display-name: 'Notification Service API'
      paths-to-match: '/api/**'
YAML

echo "✅ Notification-service YAML generated"

echo "🎉 ALL application.yml files generated successfully for mode: $MODE"
