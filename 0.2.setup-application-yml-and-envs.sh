#!/bin/bash
# ======================================================
# 🧩 setup-application-yml-and-envs.sh
# Generates application.yml for:
#   - Auth Service
#   - Asset Service
#   - Notification Service
#   - Common Service
#   - Helpdesk Service
#
# Supports: local, cloud_local, cloud, OWN_SERVER
# Database (all modes): Supabase Postgres — see SUPABASE_* vars below; modes only differ in service URLs/ports/IPs.
# ======================================================

set -euo pipefail

MODE="${1:-local}"
REGION="ap-south-1"

ROOT_DIR="$(pwd)"
AUTH_DIR="$ROOT_DIR/auth-service"
ASSET_DIR="$ROOT_DIR/asset-service"
NOTIF_DIR="$ROOT_DIR/notification-service"
COMMON_DIR="$ROOT_DIR/common-service"
HELPDESK_DIR="$ROOT_DIR/helpdesk-service"

# ----- Supabase Postgres (database layer for every MODE; schemas isolate services) -----
SUPABASE_DB_HOST="aws-1-ap-northeast-1.pooler.supabase.com"
SUPABASE_DB_PORT="6543"
SUPABASE_DB_NAME="postgres"
SUPABASE_DB_USER="postgres.sefwxyysecmfeawhvlbu"
SUPABASE_DB_PASS="BYQnqzYByLGv1DtF"

  RDS_AUTH_SCHEMA_NAME="authdb"
  RDS_ASSET_SCHEMA_NAME="assetdb"
  RDS_NOTIFY_SCHEMA_NAME="notificationdb"
  RDS_HELPDESK_SCHEMA_NAME="helpdeskdb"
# ======================================================
# 🔧 CONFIGURATION SWITCH
# ======================================================
if [ "$MODE" = "cloud" ]; then
  echo "🌩️ Generating Cloud YAML..."

  # RDS_HOST="db-auth.c5csym0gc4my.ap-south-1.rds.amazonaws.com"
  RDS_HOST="${SUPABASE_DB_HOST}"
  RDS_DB_PORT="${SUPABASE_DB_PORT}"
  RDS_DB_NAME="${SUPABASE_DB_NAME}"
  
  RDS_AUTH_DB="jdbc:postgresql://${RDS_HOST}:${RDS_DB_PORT}/${RDS_DB_NAME}?sslmode=require&prepareThreshold=0"
  RDS_ASSET_DB="jdbc:postgresql://${RDS_HOST}:${RDS_DB_PORT}/${RDS_DB_NAME}?sslmode=require&prepareThreshold=0"
  RDS_NOTIFY_DB="jdbc:postgresql://${RDS_HOST}:${RDS_DB_PORT}/${RDS_DB_NAME}?sslmode=require&prepareThreshold=0"
  RDS_HELPDESK_DB="jdbc:postgresql://${RDS_HOST}:${RDS_DB_PORT}/${RDS_DB_NAME}?sslmode=require&prepareThreshold=0"

  RDS_AUTH_USER="${SUPABASE_DB_USER}"
  RDS_ASSET_USER="${SUPABASE_DB_USER}"
  RDS_NOTIFY_USER="${SUPABASE_DB_USER}"
  RDS_HELPDESK_USER="${SUPABASE_DB_USER}"

  RDS_AUTH_PASS="${SUPABASE_DB_PASS}"
  RDS_ASSET_PASS="${SUPABASE_DB_PASS}"
  RDS_NOTIFY_PASS="${SUPABASE_DB_PASS}"
  RDS_HELPDESK_PASS="${SUPABASE_DB_PASS}"


  # ===== Cloud ports (local debug mode of EB) =====
  COMMON_PORT=${PORT:-6000}
  AUTH_PORT=${PORT:-6001}
  NOTIF_PORT=${PORT:-6002}
  ASSET_PORT=${PORT:-6003}
  HELPDESK_PORT=${PORT:-6004}

  # ===== CLOUD SERVICE IPs =====
  COMMON_AUTH_IP="13.204.61.111"
  AUTH_IP="13.204.61.111"
  ASSET_IP="13.233.124.217"
  NOTIF_IP="13.232.146.140"
  HELPDESK_IP="13.204.61.111"

  COMMON_AUTH_URL="http://auth-service-env.${REGION}.elasticbeanstalk.com"
  AUTH_URL="http://${AUTH_IP}:${AUTH_PORT}"
  ASSET_URL="http://${ASSET_IP}:${ASSET_PORT}"
  NOTIF_URL="http://${NOTIF_IP}:${NOTIF_PORT}"
  HELPDESK_URL="http://${HELPDESK_IP}:${HELPDESK_PORT}"

elif [ "$MODE" = "cloud_local" ]; then
  echo "🌥️ Generating Cloud DB + Local Services..."

  RDS_HOST="${SUPABASE_DB_HOST}"
  RDS_DB_PORT="${SUPABASE_DB_PORT}"
  RDS_DB_NAME="${SUPABASE_DB_NAME}"

  RDS_AUTH_DB="jdbc:postgresql://${RDS_HOST}:${RDS_DB_PORT}/${RDS_DB_NAME}?sslmode=require&prepareThreshold=0"
  RDS_ASSET_DB="jdbc:postgresql://${RDS_HOST}:${RDS_DB_PORT}/${RDS_DB_NAME}?sslmode=require&prepareThreshold=0"
  RDS_NOTIFY_DB="jdbc:postgresql://${RDS_HOST}:${RDS_DB_PORT}/${RDS_DB_NAME}?sslmode=require&prepareThreshold=0"
  RDS_HELPDESK_DB="jdbc:postgresql://${RDS_HOST}:${RDS_DB_PORT}/${RDS_DB_NAME}?sslmode=require&prepareThreshold=0"

  RDS_AUTH_USER="${SUPABASE_DB_USER}"
  RDS_ASSET_USER="${SUPABASE_DB_USER}"
  RDS_NOTIFY_USER="${SUPABASE_DB_USER}"
  RDS_HELPDESK_USER="${SUPABASE_DB_USER}"

  RDS_AUTH_PASS="${SUPABASE_DB_PASS}"
  RDS_ASSET_PASS="${SUPABASE_DB_PASS}"
  RDS_NOTIFY_PASS="${SUPABASE_DB_PASS}"
  RDS_HELPDESK_PASS="${SUPABASE_DB_PASS}"

  AUTH_PORT=${PORT:-7071}
  NOTIF_PORT=${PORT:-7072}
  ASSET_PORT=${PORT:-7075}
  HELPDESK_PORT=${PORT:-7074}

  AUTH_URL="http://localhost:${AUTH_PORT}"
  ASSET_URL="http://localhost:${ASSET_PORT}"
  NOTIF_URL="http://localhost:${NOTIF_PORT}"
  HELPDESK_URL="http://localhost:${HELPDESK_PORT}"



elif [ "$MODE" = "OWN_SERVER" ]; then
  echo "🌥️ Generating Own Server DB + Local Services..."

  RDS_HOST="${SUPABASE_DB_HOST}"
  RDS_DB_PORT="${SUPABASE_DB_PORT}"
  RDS_DB_NAME="${SUPABASE_DB_NAME}"

  RDS_AUTH_DB="jdbc:postgresql://${RDS_HOST}:${RDS_DB_PORT}/${RDS_DB_NAME}?sslmode=require&prepareThreshold=0"
  RDS_ASSET_DB="jdbc:postgresql://${RDS_HOST}:${RDS_DB_PORT}/${RDS_DB_NAME}?sslmode=require&prepareThreshold=0"
  RDS_NOTIFY_DB="jdbc:postgresql://${RDS_HOST}:${RDS_DB_PORT}/${RDS_DB_NAME}?sslmode=require&prepareThreshold=0"
  RDS_HELPDESK_DB="jdbc:postgresql://${RDS_HOST}:${RDS_DB_PORT}/${RDS_DB_NAME}?sslmode=require&prepareThreshold=0"

  RDS_AUTH_USER="${SUPABASE_DB_USER}"
  RDS_ASSET_USER="${SUPABASE_DB_USER}"
  RDS_NOTIFY_USER="${SUPABASE_DB_USER}"
  RDS_HELPDESK_USER="${SUPABASE_DB_USER}"

  RDS_AUTH_PASS="${SUPABASE_DB_PASS}"
  RDS_ASSET_PASS="${SUPABASE_DB_PASS}"
  RDS_NOTIFY_PASS="${SUPABASE_DB_PASS}"
  RDS_HELPDESK_PASS="${SUPABASE_DB_PASS}"

  AUTH_PORT=${PORT:-8080}
  NOTIF_PORT=${PORT:-7072}
  ASSET_PORT=${PORT:-7075}
  HELPDESK_PORT=${PORT:-7074}


  COMMON_AUTH_IP="194.163.173.37"
  AUTH_IP="https://auth-service-7qm8.onrender.com"
  ASSET_IP="194.163.173.37"
  NOTIF_IP="194.163.173.37"
  HELPDESK_IP="194.163.173.37"


  AUTH_URL="${AUTH_IP}:${AUTH_PORT}"
  ASSET_URL="http://${ASSET_IP}:${ASSET_PORT}"
  NOTIF_URL="http://${NOTIF_IP}:${NOTIF_PORT}"
  HELPDESK_URL="http://${HELPDESK_IP}:${HELPDESK_PORT}"

else
  echo "💻 Generating Local YAML..."

  RDS_HOST="${SUPABASE_DB_HOST}"
  RDS_DB_PORT="${SUPABASE_DB_PORT}"
  RDS_DB_NAME="${SUPABASE_DB_NAME}"
  RDS_AUTH_DB="jdbc:postgresql://${RDS_HOST}:${RDS_DB_PORT}/${RDS_DB_NAME}?sslmode=require&prepareThreshold=0"
  RDS_ASSET_DB="jdbc:postgresql://${RDS_HOST}:${RDS_DB_PORT}/${RDS_DB_NAME}?sslmode=require&prepareThreshold=0"
  RDS_NOTIFY_DB="jdbc:postgresql://${RDS_HOST}:${RDS_DB_PORT}/${RDS_DB_NAME}?sslmode=require&prepareThreshold=0"
  RDS_HELPDESK_DB="jdbc:postgresql://${RDS_HOST}:${RDS_DB_PORT}/${RDS_DB_NAME}?sslmode=require&prepareThreshold=0"

  RDS_AUTH_USER="${SUPABASE_DB_USER}"
  RDS_ASSET_USER="${SUPABASE_DB_USER}"
  RDS_NOTIFY_USER="${SUPABASE_DB_USER}"
  RDS_HELPDESK_USER="${SUPABASE_DB_USER}"

  RDS_AUTH_PASS="${SUPABASE_DB_PASS}"
  RDS_ASSET_PASS="${SUPABASE_DB_PASS}"
  RDS_NOTIFY_PASS="${SUPABASE_DB_PASS}"
  RDS_HELPDESK_PASS="${SUPABASE_DB_PASS}"

  AUTH_PORT=${PORT:-7071}
  NOTIF_PORT=${PORT:-7072}
  ASSET_PORT=${PORT:-7075}
  HELPDESK_PORT=${PORT:-7074}

  AUTH_URL="http://localhost:${AUTH_PORT}"
  ASSET_URL="http://localhost:${ASSET_PORT}"
  NOTIF_URL="http://localhost:${NOTIF_PORT}"
  HELPDESK_URL="http://localhost:${HELPDESK_PORT}"
fi

# Common configuration values
JWT_SECRET="yNnC7M3ZqgV4bD0lFJm9Q2w5tSe8XpR1pWc7UjK4oHs="
AUTH_ENC_KEY="SLOqKf8lS2hidTDsXQe25ZSaoaGcczUX6gySXUxjE1M="
AUTH_HMAC_KEY="krFcA7/MYPXQWbtSGMM87Dzxu2euOsRckVFeUyOC6dw="
NOTIFY_ENC_KEY="yfwZM8WwHraV8LhcSNFZ7UuIpLwxpX6lthpH4CflI3U="
NOTIFY_HMAC_KEY="SyHeAe8KeKETQihKAGFfpKipF9mysIjTsh01NaDiDpc="

mkdir -p "$AUTH_DIR/src/main/resources" "$ASSET_DIR/src/main/resources" "$NOTIF_DIR/src/main/resources" "$COMMON_DIR/src/main/resources" "$HELPDESK_DIR/src/main/resources"

# ======================================================
# 🔐 AUTH-SERVICE YAML
# ======================================================
cat > "$AUTH_DIR/src/main/resources/application.yml" <<YAML
# Auth-service
server:
  port: ${AUTH_PORT}

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
  jackson:
    serialization:
      write-dates-as-timestamps: false
    deserialization:
      adjust-dates-to-context-time-zone: false
  datasource:
    url: ${RDS_AUTH_DB}
    username: ${RDS_AUTH_USER}
    password: ${RDS_AUTH_PASS}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        # Keep app tables out of Supabase's reserved `auth` schema (search_path / metadata clashes).
        default_schema: ${RDS_AUTH_SCHEMA_NAME}
        hbm2ddl:
          create_namespaces: true



common:
  notification:
    enabled: true

JWT_PRIVATE_KEY_PATH: classpath:keys/jwt-private.pem
JWT_PUBLIC_KEY_PATH: classpath:keys/jwt-public.pem
JWT_SECRET: ${JWT_SECRET}
JWT_ACCESS_TOKEN_VALIDITY_SECONDS: 900
JWT_REFRESH_TOKEN_VALIDITY_SECONDS: 1209600
AUTH_ENC_KEY: ${AUTH_ENC_KEY}
AUTH_HMAC_KEY: ${AUTH_HMAC_KEY}
ENCRYPTION_KEY: ${AUTH_ENC_KEY}
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
# 🧱 ASSET-SERVICE YAML
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
    # Upload directory - use relative path (project/uploads/amc-docs) or absolute path
    # If the configured path is not writable, the code will fallback to project/uploads/amc-docs
    dir: uploads/amc-docs

spring:
  application:
    name: asset-service
  jackson:
    serialization:
      write-dates-as-timestamps: false
    deserialization:
      adjust-dates-to-context-time-zone: false
  datasource:
    url: ${RDS_ASSET_DB}
    username: ${RDS_ASSET_USER}
    password: ${RDS_ASSET_PASS} 
    driver-class-name: org.postgresql.Driver
    hikari:
      max-lifetime: 300000   # 5 min - recycle before server closes (avoids "connection closed" validation failures)
      connection-timeout: 20000
      keepalive-time: 60000   # 1 min - keep connections alive during long operations
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        # Keep app tables out of Supabase's reserved `asset` schema (search_path / metadata clashes).
        default_schema: ${RDS_ASSET_SCHEMA_NAME}
        hbm2ddl:
          create_namespaces: true
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
    public-key-path: classpath:keys/jwt-public.pem
    issuer: "auth-service"
    audience: "asset-service"

common:
  notification:
    enabled: true

JWT_PUBLIC_KEY_PATH: classpath:keys/jwt-public.pem
JWT_SECRET: ${JWT_SECRET}
AUTH_ENC_KEY: ${AUTH_ENC_KEY}
AUTH_HMAC_KEY: ${AUTH_HMAC_KEY}
AUTH_SERVICE_URL: ${AUTH_URL}
NOTIFICATION_SERVICE_URL: ${NOTIF_URL}


# Tesseract OCR Configuration
tesseract:
  enabled: true
  # Use external process instead of native library (recommended on macOS Ventura)
  use-process: true
  # Tesseract data path (tessdata directory)
  # MacPorts: /opt/local/share/tessdata
  # Homebrew (Intel): /usr/local/share/tessdata
  # Homebrew (Apple Silicon): /opt/homebrew/share/tessdata
  data-path: /opt/local/share/tessdata
  # Tesseract executable path (optional, will use system PATH if not specified)
  executable-path: /opt/local/bin/tesseract
  # Language for OCR (default: eng)
  language: eng
  # Page segmentation mode (1 = Automatic with OSD)
  page-seg-mode: 1
  # OCR engine mode (1 = Neural nets LSTM engine only)
  ocr-engine-mode: 1

# LLM / Agentic AI for document extraction (OpenAI-compatible API: OpenAI, Azure OpenAI, Ollama, etc.)
app:
  llm:
    enabled: true
    api-url: https://api.openai.com/v1
    api-key: \${OPENAI_API_KEY:-}
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

echo "✅ Asset-service YAML generated"

# ======================================================
# 📢 NOTIFICATION-SERVICE YAML
# ======================================================
cat > "$NOTIF_DIR/src/main/resources/application.yml" <<YAML
# Notification-service
server:
  port: ${NOTIF_PORT}

notification:
  service:
    url: ${NOTIF_URL}/api/notifications
  # When true and userId + Bearer token are present, skips sending if user opted out of that channel (auth-service).
  opt-out-check:
    enabled: true
  list:
    # Number of days to display notifications in notification icons
    # Default: 30 days (notifications older than 30 days will not be shown)
    display-days: 30
    # Maximum number of notifications to return (for pagination/performance)
    max-results: 100

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
  jackson:
    serialization:
      write-dates-as-timestamps: false
    deserialization:
      adjust-dates-to-context-time-zone: false
  datasource:
    url: ${RDS_NOTIFY_DB}
    username: ${RDS_NOTIFY_USER}
    password: ${RDS_NOTIFY_PASS}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        # Keep app tables out of Supabase's reserved `notification` schema (search_path / metadata clashes).
        default_schema: ${RDS_NOTIFY_SCHEMA_NAME}
        hbm2ddl:
          create_namespaces: true

auth:
  service:
    url: ${AUTH_URL}/api/
  client-id: notification-service
  client-secret: notify-secret

notify:
  enc:
    key: ${NOTIFY_ENC_KEY}
  hmac:
    key: ${NOTIFY_HMAC_KEY}

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

# ======================================================
# 🔗 COMMON-SERVICE YAML
# ======================================================
cat > "$COMMON_DIR/src/main/resources/application.yml" <<YAML
spring:
  application:
    name: common-service
  jackson:
    serialization:
      write-dates-as-timestamps: false
    deserialization:
      adjust-dates-to-context-time-zone: false
auth:
  service:
    url: ${AUTH_URL}

asset:
  service:
    url: ${ASSET_URL}

notification:
  service:
    url: ${NOTIF_URL}/api/notifications

JWT_PRIVATE_KEY_PATH: classpath:keys/jwt-private.pem
JWT_PUBLIC_KEY_PATH: classpath:keys/jwt-public.pem
JWT_SECRET: ${JWT_SECRET}
JWT_ACCESS_TOKEN_VALIDITY_SECONDS: 900
JWT_REFRESH_TOKEN_VALIDITY_SECONDS: 1209600
AUTH_ENC_KEY: ${AUTH_ENC_KEY}
AUTH_HMAC_KEY: ${AUTH_HMAC_KEY}
YAML

echo "✅ Common-service YAML generated"

# ======================================================
# 🎫 HELPDESK-SERVICE YAML
# ======================================================
cat > "$HELPDESK_DIR/src/main/resources/application.yml" <<YAML
# Helpdesk-service
server:
  port: ${HELPDESK_PORT}

auth:
  service:
    url: ${AUTH_URL}

notification:
  service:
    url: ${NOTIF_URL}/api/notifications

asset:
  service:
    url: ${ASSET_URL}

helpdesk:
  service:
    url: ${HELPDESK_URL}
  chatbot:
    enabled: true
    max-context-length: 1000

spring:
  application:
    name: helpdesk-service  
  jackson:
    serialization:
      write-dates-as-timestamps: false
    deserialization:
      adjust-dates-to-context-time-zone: false
  datasource:
    url: ${RDS_HELPDESK_DB}
    username: ${RDS_HELPDESK_USER}
    password: ${RDS_HELPDESK_PASS}
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
        # Keep app tables out of Supabase's reserved `helpdesk` schema (search_path / metadata clashes).
        default_schema: ${RDS_HELPDESK_SCHEMA_NAME}
        hbm2ddl:
          create_namespaces: true
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
  asset:
    base-url: ${ASSET_URL}

security:
  jwt:
    public-key-path: classpath:keys/jwt-public.pem
    issuer: "auth-service"
    audience: "helpdesk-service"

common:
  notification:
    enabled: true

JWT_PUBLIC_KEY_PATH: classpath:keys/jwt-public.pem
JWT_SECRET: ${JWT_SECRET}
AUTH_ENC_KEY: ${AUTH_ENC_KEY}
AUTH_HMAC_KEY: ${AUTH_HMAC_KEY}
AUTH_SERVICE_URL: ${AUTH_URL}
NOTIFICATION_SERVICE_URL: ${NOTIF_URL}
ASSET_SERVICE_URL: ${ASSET_URL}

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

echo "✅ Helpdesk-service YAML generated"

echo "🎉 ALL application.yml files generated successfully for mode: $MODE"
echo ""
echo "📋 Generated files:"
echo "   ✓ auth-service/src/main/resources/application.yml"
echo "   ✓ asset-service/src/main/resources/application.yml"
echo "   ✓ notification-service/src/main/resources/application.yml"
echo "   ✓ common-service/src/main/resources/application.yml"
echo "   ✓ helpdesk-service/src/main/resources/application.yml"
