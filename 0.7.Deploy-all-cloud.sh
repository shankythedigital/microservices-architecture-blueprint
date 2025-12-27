#!/usr/bin/env bash
# ======================================================
# 🚀 deploy_all_services_cloud.sh — PART 1
# Header, configuration, and helper functions
# Full safe deploy — common, auth, asset, notification
# Platform: Corretto 17 running on 64bit Amazon Linux 2023
# ======================================================

set -euo pipefail
IFS=$'\n\t'

LOGFILE="deploy_$(date +%Y%m%d_%H%M%S).log"
exec > >(tee -a "$LOGFILE") 2>&1

# -------------------------
# CONFIG (edit to your values)
# -------------------------
MODE="${1:-cloud}"                           # cloud | local
REGION="ap-south-1"
PROFILE="eb-deployer"
INSTANCE_TYPE="t3.micro"

# Use solution-stack-name (older AWS CLI) or platform ARN / name. Keep this value as a human-readable
# solution stack or platform name that works with `aws elasticbeanstalk create-environment --solution-stack-name`.
PLATFORM="Corretto 17 running on 64bit Amazon Linux 2023"

RETRY_LIMIT=1
HEALTH_TIMEOUT=180
HEALTH_POLL_INTERVAL=10
KEEP_VERSIONS=3

ROOT_DIR="$(pwd)"
S3_LOG_BUCKET=""   # optional: upload logs to S3 (set to bucket name to enable)

# Service folders (relative to repo root)
COMMON_DIR="$ROOT_DIR/common-service"
AUTH_DIR="$ROOT_DIR/auth-service"
ASSET_DIR="$ROOT_DIR/asset-service"
NOTIF_DIR="$ROOT_DIR/notification-service"

# RDS config (customize)
RDS_COMMON_HOST="db-auth.c5csym0gc4my.ap-south-1.rds.amazonaws.com"
RDS_AUTH_HOST="db-auth.c5csym0gc4my.ap-south-1.rds.amazonaws.com"
RDS_ASSET_HOST="db-asset.c5csym0gc4my.ap-south-1.rds.amazonaws.com"
RDS_NOTIFY_HOST="db-auth.c5csym0gc4my.ap-south-1.rds.amazonaws.com"

RDS_COMMON_USER="adminAuth"
RDS_AUTH_USER="adminAuth"
RDS_ASSET_USER="adminAsset"
RDS_NOTIFY_USER="adminAuth"

RDS_COMMON_PASS="AuthPass123"
RDS_AUTH_PASS="AuthPass123"
RDS_ASSET_PASS="AssetPass123"
RDS_NOTIFY_PASS="AuthPass123"

# EB app names
EB_APP_COMMON="common-service"
EB_APP_AUTH="auth-service"
EB_APP_ASSET="asset-service"
EB_APP_NOTIF="notification-service"

# -------------------------
# Helper functions
# -------------------------
hr(){ printf '%0.s=' $(seq 1 72); echo; }

# Ensure required CLI tools exist
ensure_cmds(){
  local need=(aws eb mvn zip curl nc mysql)
  for c in "${need[@]}"; do
    if ! command -v "$c" >/dev/null 2>&1; then
      echo "❌ Required command missing: $c"
      echo "   Install it and re-run. (e.g. brew install awscli awsebcli maven mysql-client jq)"
      exit 1
    fi
  done
  echo "✅ Required commands OK"
}

# Wait until EB environment Status == Ready (polls describe-environments)
# Usage: wait_env_ready "app-env-name" 300
wait_env_ready(){
  local envname="$1"; local timeout="${2:-240}"
  local end=$((SECONDS+timeout))
  while [ $SECONDS -lt $end ]; do
    local status
    status=$(aws elasticbeanstalk describe-environments \
      --environment-names "$envname" \
      --region "$REGION" --profile "$PROFILE" \
      --query 'Environments[0].Status' \
      --output text 2>/dev/null || echo "NotFound")
    if [[ "$status" == "Ready" ]]; then
      echo "✅ Env $envname Ready"
      return 0
    fi
    printf "  - waiting (%s)...\n" "$status"
    sleep 5
  done
  echo "⚠️ Timeout waiting for $envname"
  return 1
}

# Return environment Status (or NotFound)
get_env_status() {
  aws elasticbeanstalk describe-environments \
    --application-name "$1" --environment-names "$2" \
    --region "$REGION" --profile "$PROFILE" \
    --query 'Environments[0].Status' \
    --output text 2>/dev/null || echo "NotFound"
}

# Return environment CNAME (or empty)
get_env_cname() {
  aws elasticbeanstalk describe-environments \
    --application-name "$1" --environment-names "$2" \
    --region "$REGION" --profile "$PROFILE" \
    --query 'Environments[0].CNAME' \
    --output text 2>/dev/null || echo ""
}

# Short helper to print and run a command (keeps logs readable)
run_cmd() {
  echo "+ $*"
  "$@"
}

# End of Part 1


# ======================================================
# deploy_all_services_cloud.sh — PART 2
# Safe log collector, .ebignore writer, env.config writer, DB helper, safe platform hook
# ======================================================

# -------------------------
# Safe EB logs collector
# -------------------------
collect_eb_logs() {
  local svc="$1" env="$2"

  local ts; ts=$(date +%Y%m%d_%H%M%S)
  local logfile_dir="logs/${svc}"
  local file="${logfile_dir}/eb-logs-${ts}.txt"

  # Ensure logs/<svc> folder exists
  if [ ! -d "$logfile_dir" ]; then
    echo "📁 Creating logs directory: $logfile_dir"
    mkdir -p "$logfile_dir"
  fi

  # Ensure log file exists (touch)
  if [ ! -f "$file" ]; then
    echo "📝 Creating empty log file: $file"
    touch "$file"
  fi

  # Skip log collection if environment does not exist
  local env_exists
  env_exists=$(aws elasticbeanstalk describe-environments \
    --environment-names "$env" \
    --region "$REGION" --profile "$PROFILE" \
    --query 'Environments[0].EnvironmentName' \
    --output text 2>/dev/null || echo "NotFound")

  if [[ "$env_exists" == "NotFound" ]]; then
    echo "ℹ️ Environment $env not found — skipping EB logs for $svc."
    return 0
  fi

  # Ensure service folder exists to avoid eb logs failure
  if [ ! -d "${ROOT_DIR}/${svc}" ]; then
    echo "ℹ️ Creating missing service folder: ${ROOT_DIR}/${svc}"
    mkdir -p "${ROOT_DIR}/${svc}"
  fi

  echo "📥 Collecting EB logs for ${svc} → ${file}"

  (
    cd "${ROOT_DIR}/${svc}" \
      && eb logs "$env" --profile "$PROFILE" > "../${file##*/}" 2>&1
  ) || echo "⚠️ eb logs failed for ${svc}"

  if [[ -n "$S3_LOG_BUCKET" ]]; then
    aws s3 cp "$file" "s3://${S3_LOG_BUCKET}/${svc}-eb-logs-${ts}.txt" \
      --region "$REGION" --profile "$PROFILE" || true
  fi
}

# -------------------------
# .ebignore writer
# -------------------------
write_ebignore() {
  cat > .ebignore <<'EOF'
*
!*.jar
!Procfile
!.platform/**
!.ebextensions/**
node_modules/
.git/
.idea/
.vscode/
*.log
*.tmp
*.swp
*.md
*.iml
**/target/**
logs/
build/
dist/
docker/
images/
tmp/
coverage/
EOF
  echo "✅ .ebignore written"
}

# -------------------------
# Write safe .ebextensions env.config (includes JWT/encryption/log vars)
# -------------------------
write_env_config() {
  # NOTE: This writes a flat list of environment variables (allowed by EB).
  mkdir -p .ebextensions-safe

  cat > .ebextensions-safe/env.config <<EOF
option_settings:

  # -----------------------
  # DATABASE
  # -----------------------
  - namespace: aws:elasticbeanstalk:application:environment
    option_name: RDS_HOST
    value: ${RDS_HOST}

  - namespace: aws:elasticbeanstalk:application:environment
    option_name: RDS_DBNAME
    value: ${RDS_DB}

  - namespace: aws:elasticbeanstalk:application:environment
    option_name: RDS_USERNAME
    value: ${RDS_USER}

  - namespace: aws:elasticbeanstalk:application:environment
    option_name: RDS_PASSWORD
    value: ${RDS_PASS}

  # -----------------------
  # JWT / KEYS / SECURITY
  # -----------------------
  - namespace: aws:elasticbeanstalk:application:environment
    option_name: JWT_PRIVATE_KEY_PATH
    value: classpath:keys/jwt-private.pem

  - namespace: aws:elasticbeanstalk:application:environment
    option_name: JWT_PUBLIC_KEY_PATH
    value: classpath:keys/jwt-public.pem

  - namespace: aws:elasticbeanstalk:application:environment
    option_name: JWT_SECRET
    value: SuperSecureJWTKey_ChangeThisForProduction_1234567890!

  - namespace: aws:elasticbeanstalk:application:environment
    option_name: JWT_ACCESS_TOKEN_VALIDITY_SECONDS
    value: 900

  - namespace: aws:elasticbeanstalk:application:environment
    option_name: JWT_REFRESH_TOKEN_VALIDITY_SECONDS
    value: 1209600

  - namespace: aws:elasticbeanstalk:application:environment
    option_name: AUTH_ENC_KEY
    value: SLOqKf8lS2hidTDsXQe25ZSaoaGcczUX6gySXUxjE1M=

  - namespace: aws:elasticbeanstalk:application:environment
    option_name: AUTH_HMAC_KEY
    value: krFcA7/MYPXQWbtSGMM87Dzxu2euOsRckVFeUyOC6dw=

  - namespace: aws:elasticbeanstalk:application:environment
    option_name: ENCRYPTION_KEY
    value: SLOqKf8lS2hidTDsXQe25ZSaoaGcczUX6gySXUxjE1M=

  # -----------------------
  # ACCESS TOKENS
  # -----------------------
  - namespace: aws:elasticbeanstalk:application:environment
    option_name: ACCESS_TOKEN
    value: change_this_token

  - namespace: aws:elasticbeanstalk:application:environment
    option_name: FEIGN_ACCESS_TOKEN
    value: ""

  # -----------------------
  # LOGGING (flattened)
  # -----------------------
  # Spring will need to map this to logging.level.com.example in application.yml
  - namespace: aws:elasticbeanstalk:application:environment
    option_name: LOGGING_LEVEL_COM_EXAMPLE
    value: DEBUG

EOF

  echo "✅ .ebextensions-safe/env.config written (with JWT/encryption/log vars)"
}

# -------------------------
# Create tiny safe .platform hook (logs java -version)
# -------------------------
write_platform_safe() {
  mkdir -p .platform-safe/hooks/prebuild
  cat > .platform-safe/hooks/prebuild/00_log_java_version.sh <<'SH'
#!/bin/bash
set -e
echo ">>> [prebuild hook] java -version:" >> /var/log/eb-activity.log 2>&1 || true
java -version >> /var/log/eb-activity.log 2>&1 || echo "java not found" >> /var/log/eb-activity.log 2>&1 || true
SH
  chmod +x .platform-safe/hooks/prebuild/00_log_java_version.sh
  echo "✅ .platform-safe created"
}

# -------------------------
# DB helper (best-effort create)
# -------------------------
create_db_if_missing(){
  local host="$1" user="$2" pass="$3" db="$4"
  echo "🔍 Ensuring DB ${db} exists on ${host}..."

  if nc -z -w5 "$host" 3306 >/dev/null 2>&1; then
    if mysql -h "$host" -u "$user" -p"$pass" \
      -e "CREATE DATABASE IF NOT EXISTS \`${db}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" \
      >/dev/null 2>&1; then
      echo "✅ DB ${db} ready on ${host}"
    else
      echo "⚠️ DB create failed (credentials/permissions?)"
    fi
  else
    echo "⚠️ DB host ${host} not reachable (skipping create)"
  fi
}

# End of Part 2
# ======================================================
# deploy_all_services_cloud.sh — PART 3
# Full deploy_service() logic
# ======================================================

deploy_service(){
  local SERVICE_DIR="$1"
  local APP_NAME="$2"
  local RDS_HOST="$3"
  local RDS_DB="$4"
  local RDS_USER="$5"
  local RDS_PASS="$6"

  echo
  hr
  printf "🚀 Starting deploy for %s\n" "$APP_NAME"
  hr

  if [[ ! -d "$SERVICE_DIR" ]]; then
    echo "❌ Service folder missing: $SERVICE_DIR"
    return 1
  fi

  pushd "$SERVICE_DIR" >/dev/null

  # -----------------------------
  # Ensure EB Application exists
  # -----------------------------
  if ! aws elasticbeanstalk describe-applications \
        --application-names "$APP_NAME" \
        --region "$REGION" --profile "$PROFILE" \
        --query 'Applications[0].ApplicationName' \
        --output text 2>/dev/null | grep -q "$APP_NAME"; then
        
    echo "🌍 Creating EB Application: $APP_NAME"
    run_cmd aws elasticbeanstalk create-application \
      --application-name "$APP_NAME" \
      --region "$REGION" --profile "$PROFILE" >/dev/null || true
  fi

  # -----------------------------
  # Detect environment existence
  # -----------------------------
  ENV_NAME="${APP_NAME}-env"

  local env_exists
  env_exists=$(
    aws elasticbeanstalk describe-environments \
      --environment-names "$ENV_NAME" \
      --region "$REGION" --profile "$PROFILE" \
      --query 'Environments[0].EnvironmentName' \
      --output text 2>/dev/null || echo "NotFound"
  )

  if [[ "$env_exists" == "NotFound" || "$env_exists" == "None" ]]; then
    echo "🌱 Environment $ENV_NAME does not exist — will CREATE after bundle upload."
  else
    echo "🔄 Environment exists — will UPDATE $ENV_NAME after creating version"
  fi

  # -----------------------------
  # Build service JAR
  # -----------------------------
  echo "🔨 Building: mvn clean package -DskipTests"
  run_cmd mvn -q clean package -DskipTests

  JAR_FILE=$(ls target/*.jar 2>/dev/null | grep -vE 'sources|javadoc|original' | head -n1 || true)
  if [ -z "$JAR_FILE" ]; then
    echo "❌ No jar found in target/ — build failed"
    popd >/dev/null
    return 1
  fi
  JAR_NAME=$(basename "$JAR_FILE")

  # -----------------------------
  # Write Procfile
  # -----------------------------
  echo "web: java -Xmx512m -Dserver.port=\$PORT -jar /var/app/current/${JAR_NAME}" > Procfile

  # -----------------------------
  # Write safe .ebextensions + .platform
  # -----------------------------
  write_env_config   # from Part 2
  write_platform_safe

  # -----------------------------
  # Ensure EB config.yml
  # -----------------------------
  mkdir -p .elasticbeanstalk
  cat > .elasticbeanstalk/config.yml <<EOF
branch-defaults:
  default:
    environment: ${ENV_NAME}
global:
  application_name: ${APP_NAME}
  default_region: ${REGION}
  profile: ${PROFILE}
EOF

  # -----------------------------
  # Ensure DB exists (best-effort)
  # -----------------------------
  if [[ "$MODE" == "cloud" ]]; then
    create_db_if_missing "$RDS_HOST" "$RDS_USER" "$RDS_PASS" "$RDS_DB"
  fi

  # -----------------------------
  # Build minimal safe ZIP bundle
  # -----------------------------
  TMPDIR=$(mktemp -d)
  trap 'rm -rf "$TMPDIR"' EXIT

  cp "$JAR_FILE" "$TMPDIR/"
  cp Procfile "$TMPDIR/"

  mkdir -p "$TMPDIR/.ebextensions"
  cp .ebextensions-safe/env.config "$TMPDIR/.ebextensions/env.config"

  mkdir -p "$TMPDIR/.platform/hooks/prebuild"
  cp .platform-safe/hooks/prebuild/00_log_java_version.sh \
     "$TMPDIR/.platform/hooks/prebuild/00_log_java_version.sh"

  VERSION_LABEL="${APP_NAME}-$(date +%y%m%d_%H%M%S)"
  ZIP_FILE="${TMPDIR}/${VERSION_LABEL}.zip"

  (cd "$TMPDIR" && zip -r "$ZIP_FILE" . >/dev/null)
  echo "📦 Bundle built → $ZIP_FILE"

  # -----------------------------
  # Upload ZIP to S3
  # -----------------------------
  S3_BUCKET="${APP_NAME}-bundle-${PROFILE}-${REGION}"
  if ! aws s3api head-bucket --bucket "$S3_BUCKET" --profile "$PROFILE" >/dev/null 2>&1; then
    echo "ℹ️ Creating S3 bucket: $S3_BUCKET"
    run_cmd aws s3api create-bucket --bucket "$S3_BUCKET" \
      --region "$REGION" \
      --create-bucket-configuration LocationConstraint="$REGION" \
      --profile "$PROFILE" >/dev/null || true
  fi

  echo "📤 Uploading → s3://${S3_BUCKET}/${VERSION_LABEL}.zip"
  run_cmd aws s3 cp "$ZIP_FILE" "s3://${S3_BUCKET}/${VERSION_LABEL}.zip" \
    --region "$REGION" --profile "$PROFILE"

  # -----------------------------
  # Register new application version
  # -----------------------------
  run_cmd aws elasticbeanstalk create-application-version \
    --application-name "$APP_NAME" \
    --version-label "$VERSION_LABEL" \
    --source-bundle S3Bucket="$S3_BUCKET",S3Key="${VERSION_LABEL}.zip" \
    --region "$REGION" --profile "$PROFILE" >/dev/null || true

  # -----------------------------
  # CREATE or UPDATE environment
  # -----------------------------
  if [[ "$env_exists" == "NotFound" || "$env_exists" == "None" ]]; then
      
    echo "🌱 Creating environment $ENV_NAME from version $VERSION_LABEL"

    # Use AWS CLI create-environment (stable) instead of EB CLI
    if ! aws elasticbeanstalk create-environment \
      --application-name "$APP_NAME" \
      --environment-name "$ENV_NAME" \
      --version-label "$VERSION_LABEL" \
      --solution-stack-name "$PLATFORM" \
      --region "$REGION" --profile "$PROFILE" \
      --option-settings Namespace=aws:autoscaling:launchconfiguration,OptionName=InstanceType,Value="${INSTANCE_TYPE}" >/dev/null; then
        
      echo "❌ Failed to create environment: $ENV_NAME"
      collect_eb_logs "$APP_NAME" "$ENV_NAME"
      popd >/dev/null
      return 1
    fi

    wait_env_ready "$ENV_NAME" 600 || true

  else
    echo "🚀 Updating environment $ENV_NAME to version $VERSION_LABEL"

    if ! aws elasticbeanstalk update-environment \
      --environment-name "$ENV_NAME" \
      --version-label "$VERSION_LABEL" \
      --region "$REGION" --profile "$PROFILE" >/dev/null 2>&1; then
        
      echo "⚠️ update-environment failed for $ENV_NAME"
      collect_eb_logs "$APP_NAME" "$ENV_NAME"
    fi

    wait_env_ready "$ENV_NAME" 300 || true
  fi

  # -----------------------------
  # Health check (if CNAME available)
  # -----------------------------
  local CNAME
  CNAME=$(get_env_cname "$APP_NAME" "$ENV_NAME")

  if [[ -z "$CNAME" || "$CNAME" == "None" ]]; then
    echo "⚠️ No CNAME available for $ENV_NAME — skipping HTTP health check"
  else
    echo "🔎 Checking health @ http://${CNAME}/actuator/health"
    local end=$((SECONDS + HEALTH_TIMEOUT))
    local healthy=0

    while [ $SECONDS -lt $end ]; do
      if curl -fs "http://${CNAME}/actuator/health" >/dev/null 2>&1; then
        healthy=1
        break
      fi
      sleep "$HEALTH_POLL_INTERVAL"
    done

    if (( healthy == 0 )); then
      echo "⚠️ Health failed — collecting logs"
      collect_eb_logs "$APP_NAME" "$ENV_NAME"
    else
      echo "✅ Deploy OK for ${APP_NAME}"
    fi
  fi

  # -----------------------------
  # Cleanup
  # -----------------------------
  rm -rf .ebextensions-safe .platform-safe
  clean_old_versions "$APP_NAME"

  popd >/dev/null
}

# End of Part 3
# ======================================================
# deploy_all_services_cloud.sh — PART 4
# Main execution block
# ======================================================

hr
echo "🚀 Starting deployment: $(date)"
hr

# Ensure tools like aws, eb, mvn, mysql, jq exist
ensure_cmds

# Write the global .ebignore in repo root
write_ebignore

if [[ "$MODE" == "cloud" ]]; then

  deploy_service "$COMMON_DIR" "$EB_APP_COMMON" "$RDS_COMMON_HOST" "commondb" "$RDS_COMMON_USER" "$RDS_COMMON_PASS"

  deploy_service "$AUTH_DIR" "$EB_APP_AUTH" "$RDS_AUTH_HOST" "authdb" "$RDS_AUTH_USER" "$RDS_AUTH_PASS"

  deploy_service "$ASSET_DIR" "$EB_APP_ASSET" "$RDS_ASSET_HOST" "assetdb" "$RDS_ASSET_USER" "$RDS_ASSET_PASS"

  deploy_service "$NOTIF_DIR" "$EB_APP_NOTIF" "$RDS_NOTIFY_HOST" "notificationdb" "$RDS_NOTIFY_USER" "$RDS_NOTIFY_PASS"

else
  echo "💻 Local mode — only .ebignore written. No deployment performed."
fi

hr
echo "✔ Done: $(date)"
echo "✔ Full deployment log saved to: $LOGFILE"
hr

# End of FULL SCRIPT
