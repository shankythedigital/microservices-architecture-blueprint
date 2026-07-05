#!/usr/bin/env bash
# ======================================================
# 0.2.applicationyml.sh
# Writes application.yml for auth, asset, notification, and helpdesk services.
#
# Modes (first argument, default: local):
#   local        — hardcoded ports, URLs, JDBC, schemas, JWT, app names (no ${…} placeholders)
#   cloud-cloud  — YAML uses env placeholders for cloud deploy (DB + service URLs + secrets)
#   cloud-local  — YAML uses env placeholders; point SPRING_DATASOURCE_* at cloud DB and
#                  AUTH_SERVICE_URL etc. at http://localhost:...
#   local-cloud  — YAML uses env placeholders; local DB credentials in env + cloud service URLs
#
# Aliases: cloud (cloud-cloud), hybrid-db (cloud-local), hybrid-api (local-cloud)
#
# --- Non-local modes (cloud / hybrid): set at runtime via platform env or .env ---
#   PORT
#   SPRING_APPLICATION_NAME  (optional; defaults per service in YAML via Spring ${VAR:default})
#   SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD
#     (YAML defaults: ${SPRING_DATASOURCE_USERNAME:postgres}, ${SPRING_DATASOURCE_PASSWORD:postgres})
#   AUTH_SERVICE_URL, NOTIFICATION_SERVICE_URL, ASSET_SERVICE_URL, HELPDESK_SERVICE_URL
#     (YAML defaults: http://localhost + AUTH_PORT_LOCAL..HELPDESK_PORT_LOCAL; set env for cloud)
#   SUPABASE_AUTH_SCHEMA, ASSET_DB_SCHEMA, NOTIFICATION_DB_SCHEMA, HELPDESK_DB_SCHEMA
#   JWT_SECRET, AUTH_ENC_KEY, AUTH_HMAC_KEY
#   issuer, asset_audience, helpdesk_audience  (JWT validation; often auth-service / asset-service / helpdesk-service)
# Optional: OPENAI_API_KEY
# Optional (Supabase Storage): SUPABASE_IMAGES_URL, SUPABASE_IMAGES_SERVICE_ROLE_KEY,
#   SUPABASE_IMAGES_BUCKET (YAML uses ${VAR:} so unset is empty; does not override explicit env)
#
# --- Local mode: optional overrides ---
#   LOCAL_DB_HOST, LOCAL_DB_PORT, LOCAL_DB_NAME, LOCAL_DB_USER, LOCAL_DB_PASS
#   AUTH_PORT_LOCAL, NOTIF_PORT_LOCAL, ASSET_PORT_LOCAL, HELPDESK_PORT_LOCAL (defaults 7071/7072/7075/7074)
#   SCHEMA_AUTH, SCHEMA_ASSET, SCHEMA_NOTIFICATION, SCHEMA_HELPDESK
#   JWT_SECRET, AUTH_ENC_KEY, AUTH_HMAC_KEY (optional; else script dev defaults are baked in)
#   Non-local modes: YAML uses ${SPRING_DATASOURCE_*} , ${*_SERVICE_URL:…}, ${JWT_SECRET:…}, etc.
#
# Optional before run:  set -a; source ./.env.applicationyml; set +a
# ======================================================

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
AUTH_DIR="$ROOT_DIR/auth-service"
ASSET_DIR="$ROOT_DIR/asset-service"
NOTIF_DIR="$ROOT_DIR/notification-service"
HELPDESK_DIR="$ROOT_DIR/helpdesk-service"

usage() {
  sed -n '1,45p' "$0" | tail -n +2
  exit "${1:-0}"
}

RAW="${1:-local}"
case "$(printf '%s' "$RAW" | tr '[:upper:]' '[:lower:]')" in
  help|-h|--help) usage 0 ;;
  local|l) MODE="local" ;;
  cloud-cloud|cloud|cc) MODE="cloud-cloud" ;;
  cloud-local|cloud-db-local|hybrid-db|cl) MODE="cloud-local" ;;
  local-cloud|hybrid-api|lc) MODE="local-cloud" ;;
  *) echo "Unknown mode: $RAW" >&2; usage 1 ;;
esac

# --- Ports (defaults for ${PORT:…} and for service URL fallbacks http://localhost:…) ---
AUTH_PORT_LOCAL="${AUTH_PORT_LOCAL:-7071}"
NOTIF_PORT_LOCAL="${NOTIF_PORT_LOCAL:-7072}"
ASSET_PORT_LOCAL="${ASSET_PORT_LOCAL:-7075}"
HELPDESK_PORT_LOCAL="${HELPDESK_PORT_LOCAL:-7074}"

# --- Local DB ---
LOCAL_DB_HOST="${LOCAL_DB_HOST:-localhost}"
LOCAL_DB_PORT="${LOCAL_DB_PORT:-5432}"
LOCAL_DB_NAME="${LOCAL_DB_NAME:-postgres}"
LOCAL_DB_USER="${LOCAL_DB_USER:-postgres}"
LOCAL_DB_PASS="${LOCAL_DB_PASS:-postgres}"
LOCAL_JDBC="jdbc:postgresql://${LOCAL_DB_HOST}:${LOCAL_DB_PORT}/${LOCAL_DB_NAME}"

# --- Schema names (local mode: expanded; non-local: env names in YAML) ---
SCHEMA_AUTH="${SCHEMA_AUTH:-authdb}"
SCHEMA_ASSET="${SCHEMA_ASSET:-assetdb}"
SCHEMA_NOTIFICATION="${SCHEMA_NOTIFICATION:-notificationdb}"
SCHEMA_HELPDESK="${SCHEMA_HELPDESK:-helpdeskdb}"

# --- Dev defaults (used as Spring ${ENV:default} fallbacks in generated YAML; export env to override) ---
JWT_SECRET="${JWT_SECRET:-yNnC7M3ZqgV4bD0lFJm9Q2w5tSe8XpR1pWc7UjK4oHs=}"
AUTH_ENC_KEY="${AUTH_ENC_KEY:-SLOqKf8lS2hidTDsXQe25ZSaoaGcczUX6gySXUxjE1M=}"
AUTH_HMAC_KEY="${AUTH_HMAC_KEY:-krFcA7/MYPXQWbtSGMM87Dzxu2euOsRckVFeUyOC6dw=}"
SUPABASE_IMAGES_URL="${SUPABASE_IMAGES_URL:-}"
SUPABASE_IMAGES_SERVICE_ROLE_KEY="${SUPABASE_IMAGES_SERVICE_ROLE_KEY:-}"
SUPABASE_IMAGES_BUCKET="${SUPABASE_IMAGES_BUCKET:-images-storage}"

issuer="${issuer:-auth-service}"
asset_audience="${asset_audience:-asset-service}"
helpdesk_audience="${helpdesk_audience:-helpdesk-service}"

# SPRING_DATASOURCE_PASSWORD="${SPRING_DATASOURCE_PASSWORD:-yjLOMdVdPhv9qKpO8qKLqwKUTIRlDp51}"
# SPRING_DATASOURCE_URL="${SPRING_DATASOURCE_URL:-jdbc:postgresql://dpg-d894iv5ckfvc73884ngg-a.oregon-postgres.render.com/authdb_a3qi}"
# SPRING_DATASOURCE_USERNAME="${SPRING_DATASOURCE_USERNAME:-asset}"

if [ "$MODE" = "local" ]; then
  # Fully hardcoded YAML (resolved when this script runs; optional env overrides via LOCAL_*, SCHEMA_*, JWT_* before invoke)
  DS_URL_YML="$LOCAL_JDBC"
  DS_USER_YML="$LOCAL_DB_USER"
  DS_PASS_YML="$LOCAL_DB_PASS"
  AUTH_PORT_YML="$AUTH_PORT_LOCAL"
  NOTIF_PORT_YML="$NOTIF_PORT_LOCAL"
  ASSET_PORT_YML="$ASSET_PORT_LOCAL"
  HELPDESK_PORT_YML="$HELPDESK_PORT_LOCAL"
  AUTH_URL_YML="http://localhost:${AUTH_PORT_LOCAL}"
  ASSET_URL_YML="http://localhost:${ASSET_PORT_LOCAL}"
  NOTIF_URL_YML="http://localhost:${NOTIF_PORT_LOCAL}"
  HELPDESK_URL_YML="http://localhost:${HELPDESK_PORT_LOCAL}"
  SCHEMA_AUTH_YML="$SCHEMA_AUTH"
  SCHEMA_ASSET_YML="$SCHEMA_ASSET"
  SCHEMA_NOTIFICATION_YML="$SCHEMA_NOTIFICATION"
  SCHEMA_HELPDESK_YML="$SCHEMA_HELPDESK"
  JWT_SECRET_YML="$JWT_SECRET"
  AUTH_ENC_KEY_YML="$AUTH_ENC_KEY"
  AUTH_HMAC_KEY_YML="$AUTH_HMAC_KEY"
  JWT_ISSUER_YML="auth-service"
  JWT_AUDIENCE_ASSET_YML="asset-service"
  JWT_AUDIENCE_HELPDESK_YML="helpdesk-service"
  SUPABASE_IMAGES_URL_YML="$SUPABASE_IMAGES_URL"
  SUPABASE_IMAGES_KEY_YML="$SUPABASE_IMAGES_SERVICE_ROLE_KEY"
  SUPABASE_IMAGES_BUCKET_YML="$SUPABASE_IMAGES_BUCKET"
  OPENAI_API_KEY_YML='""'
  SPRING_APP_NAME_AUTH_YML="auth-service"
  SPRING_APP_NAME_ASSET_YML="asset-service"
  SPRING_APP_NAME_NOTIFICATION_YML="notification-service"
  SPRING_APP_NAME_HELPDESK_YML="helpdesk-service"
else
  DS_URL_YML="\${SPRING_DATASOURCE_URL:${LOCAL_JDBC}}"
  DS_USER_YML="\${SPRING_DATASOURCE_USERNAME:${LOCAL_DB_USER}}"
  DS_PASS_YML="\${SPRING_DATASOURCE_PASSWORD:${LOCAL_DB_PASS}}"
  AUTH_PORT_YML="\${PORT:${AUTH_PORT_LOCAL}}"
  NOTIF_PORT_YML="\${PORT:${NOTIF_PORT_LOCAL}}"
  ASSET_PORT_YML="\${PORT:${ASSET_PORT_LOCAL}}"
  HELPDESK_PORT_YML="\${PORT:${HELPDESK_PORT_LOCAL}}"
  AUTH_URL_YML="\${AUTH_SERVICE_URL:http://localhost:${AUTH_PORT_LOCAL}}"
  ASSET_URL_YML="\${ASSET_SERVICE_URL:http://localhost:${ASSET_PORT_LOCAL}}"
  NOTIF_URL_YML="\${NOTIFICATION_SERVICE_URL:http://localhost:${NOTIF_PORT_LOCAL}}"
  HELPDESK_URL_YML="\${HELPDESK_SERVICE_URL:http://localhost:${HELPDESK_PORT_LOCAL}}"
  SCHEMA_AUTH_YML="\${SUPABASE_AUTH_SCHEMA:${SCHEMA_AUTH}}"
  SCHEMA_ASSET_YML="\${ASSET_DB_SCHEMA:${SCHEMA_ASSET}}"
  SCHEMA_NOTIFICATION_YML="\${NOTIFICATION_DB_SCHEMA:${SCHEMA_NOTIFICATION}}"
  SCHEMA_HELPDESK_YML="\${HELPDESK_DB_SCHEMA:${SCHEMA_HELPDESK}}"
  JWT_SECRET_YML="\${JWT_SECRET:${JWT_SECRET}}"
  AUTH_ENC_KEY_YML="\${AUTH_ENC_KEY:${AUTH_ENC_KEY}}"
  AUTH_HMAC_KEY_YML="\${AUTH_HMAC_KEY:${AUTH_HMAC_KEY}}"
  JWT_ISSUER_YML="\${issuer:${issuer}}"
  JWT_AUDIENCE_ASSET_YML="\${asset_audience:${asset_audience}}"
  JWT_AUDIENCE_HELPDESK_YML="\${helpdesk_audience:${helpdesk_audience}}"
  SUPABASE_IMAGES_URL_YML='${SUPABASE_IMAGES_URL:}'
  SUPABASE_IMAGES_KEY_YML='${SUPABASE_IMAGES_SERVICE_ROLE_KEY:}'
  SUPABASE_IMAGES_BUCKET_YML='${SUPABASE_IMAGES_BUCKET:}'
  OPENAI_API_KEY_YML='${OPENAI_API_KEY:}'
  SPRING_APP_NAME_AUTH_YML='${SPRING_APPLICATION_NAME:auth-service}'
  SPRING_APP_NAME_ASSET_YML='${SPRING_APPLICATION_NAME:asset-service}'
  SPRING_APP_NAME_NOTIFICATION_YML='${SPRING_APPLICATION_NAME:notification-service}'
  SPRING_APP_NAME_HELPDESK_YML='${SPRING_APPLICATION_NAME:helpdesk-service}'
fi

mkdir -p "$AUTH_DIR/src/main/resources" "$ASSET_DIR/src/main/resources" "$NOTIF_DIR/src/main/resources" "$HELPDESK_DIR/src/main/resources"

echo "Writing application.yml files for mode: $MODE"

# ======================================================
# AUTH
# ======================================================
cat > "$AUTH_DIR/src/main/resources/application.yml" <<YAML
# Auth-service (${MODE})
server:
  port: ${AUTH_PORT_YML}

auth:
  service:
    url: ${AUTH_URL_YML}

notification:
  service:
    url: ${NOTIF_URL_YML}/api/notifications

asset:
  service:
    url: ${ASSET_URL_YML}

spring:
  application:
    name: ${SPRING_APP_NAME_AUTH_YML}
  jackson:
    serialization:
      write-dates-as-timestamps: false
    deserialization:
      adjust-dates-to-context-time-zone: false
  datasource:
    url: ${DS_URL_YML}
    username: ${DS_USER_YML}
    password: ${DS_PASS_YML}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        default_schema: ${SCHEMA_AUTH_YML}
        hbm2ddl:
          create_namespaces: true



common:
  notification:
    enabled: true

JWT_PRIVATE_KEY_PATH: classpath:keys/jwt-private.pem
JWT_PUBLIC_KEY_PATH: classpath:keys/jwt-public.pem
JWT_SECRET: ${JWT_SECRET_YML}
JWT_ACCESS_TOKEN_VALIDITY_SECONDS: 900
JWT_REFRESH_TOKEN_VALIDITY_SECONDS: 1209600
AUTH_ENC_KEY: ${AUTH_ENC_KEY_YML}
AUTH_HMAC_KEY: ${AUTH_HMAC_KEY_YML}
ENCRYPTION_KEY: ${AUTH_ENC_KEY_YML}
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

# ======================================================
# ASSET
# ======================================================
cat > "$ASSET_DIR/src/main/resources/application.yml" <<YAML
# Asset-service (${MODE})
server:
  port: ${ASSET_PORT_YML}

auth:
  service:
    url: ${AUTH_URL_YML}

notification:
  service:
    url: ${NOTIF_URL_YML}/api/notifications

asset:
  service:
    url: ${ASSET_URL_YML}
  upload:
    dir: uploads/amc-docs
    supabase:
      enabled: false
      url: ${SUPABASE_IMAGES_URL_YML}
      service-role-key: ${SUPABASE_IMAGES_KEY_YML}
      bucket: ${SUPABASE_IMAGES_BUCKET_YML}

spring:
  application:
    name: ${SPRING_APP_NAME_ASSET_YML}
  jackson:
    serialization:
      write-dates-as-timestamps: false
    deserialization:
      adjust-dates-to-context-time-zone: false
  datasource:
    url: ${DS_URL_YML}
    username: ${DS_USER_YML}
    password: ${DS_PASS_YML}
    driver-class-name: org.postgresql.Driver
    hikari:
      max-lifetime: 300000
      connection-timeout: 20000
      keepalive-time: 60000

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        default_schema: "${SCHEMA_ASSET_YML}"
        hbm2ddl:
          create_namespaces: true
  servlet:
    multipart:
      enabled: true
      max-file-size: 50MB
      max-request-size: 50MB

services:
  auth:
    base-url: ${AUTH_URL_YML}
  notification:
    base-url: ${NOTIF_URL_YML}

security:
  jwt:
    public-key-path: classpath:keys/jwt-public.pem
    issuer: ${JWT_ISSUER_YML}
    audience: ${JWT_AUDIENCE_ASSET_YML}

common:
  notification:
    enabled: true

JWT_PUBLIC_KEY_PATH: classpath:keys/jwt-public.pem
JWT_SECRET: ${JWT_SECRET_YML}
AUTH_ENC_KEY: ${AUTH_ENC_KEY_YML}
AUTH_HMAC_KEY: ${AUTH_HMAC_KEY_YML}
AUTH_SERVICE_URL: ${AUTH_URL_YML}
NOTIFICATION_SERVICE_URL: ${NOTIF_URL_YML}


logging:
  level:
    com.example: DEBUG
# Tesseract OCR Configuration
tesseract:
  enabled: true
  use-process: true
  data-path: /opt/local/share/tessdata
  executable-path: /opt/local/bin/tesseract
  language: eng
  page-seg-mode: 1
  ocr-engine-mode: 1

# LLM / Agentic AI for document extraction
app:
  llm:
    enabled: true
    api-url: https://api.openai.com/v1
    api-key: ${OPENAI_API_KEY_YML}
    model: gpt-4o-mini
    max-tokens: 4096
    timeout-seconds: 60


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

# ======================================================
# NOTIFICATION
# ======================================================
cat > "$NOTIF_DIR/src/main/resources/application.yml" <<YAML
# Notification-service (${MODE})
server:
  port: ${NOTIF_PORT_YML}

notification:
  service:
    url: ${NOTIF_URL_YML}/api/notifications
  opt-out-check:
    enabled: true
  list:
    display-days: 30
    max-results: 100

asset:
  service:
    url: ${ASSET_URL_YML}
  upload:
    supabase:
      enabled: false
      url: ${SUPABASE_IMAGES_URL_YML}
      service-role-key: ${SUPABASE_IMAGES_KEY_YML}
      bucket: ${SUPABASE_IMAGES_BUCKET_YML}

fileupload:
  type: standard

spring:
  application:
    name: ${SPRING_APP_NAME_NOTIFICATION_YML}
  jackson:
    serialization:
      write-dates-as-timestamps: false
    deserialization:
      adjust-dates-to-context-time-zone: false
  datasource:
    url: ${DS_URL_YML}
    username: ${DS_USER_YML}
    password: ${DS_PASS_YML}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        default_schema: ${SCHEMA_NOTIFICATION_YML}
        hbm2ddl:
          create_namespaces: true

auth:
  service:
    url: ${AUTH_URL_YML}/api/
  client-id: notification-service
  client-secret: notify-secret

notify:
  enc:
    key: ${AUTH_ENC_KEY_YML}
  hmac:
    key: ${AUTH_HMAC_KEY_YML}

jwt:
  public-key-path: classpath:keys/jwt-public.pem

JWT_SECRET: ${JWT_SECRET_YML}
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

# ======================================================
# HELPDESK
# ======================================================
cat > "$HELPDESK_DIR/src/main/resources/application.yml" <<YAML
# Helpdesk-service (${MODE})
server:
  port: ${HELPDESK_PORT_YML}

auth:
  service:
    url: ${AUTH_URL_YML}

notification:
  service:
    url: ${NOTIF_URL_YML}/api/notifications

asset:
  service:
    url: ${ASSET_URL_YML}

helpdesk:
  service:
    url: ${HELPDESK_URL_YML}
  chatbot:
    enabled: true
    max-context-length: 1000

spring:
  application:
    name: ${SPRING_APP_NAME_HELPDESK_YML}
  jackson:
    serialization:
      write-dates-as-timestamps: false
    deserialization:
      adjust-dates-to-context-time-zone: false
  datasource:
    url: ${DS_URL_YML}
    username: ${DS_USER_YML}
    password: ${DS_PASS_YML}
    driver-class-name: org.postgresql.Driver
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000
      pool-name: HelpdeskHikariPool
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        default_schema: ${SCHEMA_HELPDESK_YML}
        hbm2ddl:
          create_namespaces: true
  servlet:
    multipart:
      enabled: true
      max-file-size: 50MB
      max-request-size: 50MB

services:
  auth:
    base-url: ${AUTH_URL_YML}
  notification:
    base-url: ${NOTIF_URL_YML}
  asset:
    base-url: ${ASSET_URL_YML}

security:
  jwt:
    public-key-path: classpath:keys/jwt-public.pem
    issuer: ${JWT_ISSUER_YML}
    audience: ${JWT_AUDIENCE_HELPDESK_YML}

common:
  notification:
    enabled: true

JWT_PUBLIC_KEY_PATH: classpath:keys/jwt-public.pem
JWT_SECRET: ${JWT_SECRET_YML}
AUTH_ENC_KEY: ${AUTH_ENC_KEY_YML}
AUTH_HMAC_KEY: ${AUTH_HMAC_KEY_YML}
AUTH_SERVICE_URL: ${AUTH_URL_YML}
NOTIFICATION_SERVICE_URL: ${NOTIF_URL_YML}
ASSET_SERVICE_URL: ${ASSET_URL_YML}

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
    - group: 'helpdesk-service'
      display-name: 'Helpdesk Service API'
      paths-to-match: '/api/**'
YAML

echo "Done."
echo "  $AUTH_DIR/src/main/resources/application.yml"
echo "  $ASSET_DIR/src/main/resources/application.yml"
echo "  $NOTIF_DIR/src/main/resources/application.yml"
echo "  $HELPDESK_DIR/src/main/resources/application.yml"
