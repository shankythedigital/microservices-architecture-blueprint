#!/bin/bash
# ============================================================
# 🧭 AWS + Elastic Beanstalk Environment Verifier
# Author: Shashank Naik & ChatGPT (AWS Cloud SME)
# Compatible with macOS Ventura 13+ / AWS CLI v2 / EB CLI 3+
# ============================================================

set -e

echo "🔍 Verifying AWS & Elastic Beanstalk setup..."

# --------- Check 1: AWS CLI installed ---------
if ! command -v aws &>/dev/null; then
  echo "❌ AWS CLI not found!"
  echo "👉 Install it with:"
  echo "   brew install awscli  OR"
  echo "   curl 'https://awscli.amazonaws.com/AWSCLIV2.pkg' -o 'AWSCLIV2.pkg'"
  echo "   sudo installer -pkg AWSCLIV2.pkg -target /"
  exit 1
fi
echo "✅ AWS CLI detected: $(aws --version)"

# --------- Check 2: Elastic Beanstalk CLI installed ---------
if ! command -v eb &>/dev/null; then
  echo "❌ Elastic Beanstalk CLI (eb) not found!"
  echo "👉 Install it with:"
  echo "   pip3 install --user awsebcli"
  echo "   echo 'export PATH=\"\$HOME/Library/Python/3.10/bin:\$PATH\"' >> ~/.zshrc"
  exit 1
fi
echo "✅ Elastic Beanstalk CLI detected: $(eb --version)"

# --------- Check 3: AWS Credentials Configured ---------
if ! aws sts get-caller-identity &>/dev/null; then
  echo "❌ AWS credentials not configured or invalid!"
  echo "👉 Run: aws configure"
  echo "   and enter your Access Key, Secret Key, region (ap-south-1), and output format (json)"
  exit 1
fi

ACCOUNT_INFO=$(aws sts get-caller-identity --output json)
echo "✅ AWS credentials verified!"
echo "🧾 Account info:"
echo "$ACCOUNT_INFO"

# --------- Check 4: Java Installed (for Spring Boot/Maven) ---------
if ! command -v java &>/dev/null; then
  echo "⚠️  Java not found. Spring Boot builds may fail."
  echo "👉 Install Java 17 via: brew install openjdk@17"
else
  echo "✅ Java detected: $(java -version 2>&1 | head -n1)"
fi

# --------- Check 5: Maven Installed ---------
if ! command -v mvn &>/dev/null; then
  echo "⚠️  Maven not found. Build commands may fail."
  echo "👉 Install via: brew install maven"
else
  echo "✅ Maven detected: $(mvn -v | head -n1)"
fi

# --------- Check 6: Region Check ---------
DEFAULT_REGION=$(aws configure get region)
if [[ -z "$DEFAULT_REGION" ]]; then
  echo "⚠️  Default region not set."
  echo "👉 Run: aws configure set region ap-south-1"
else
  echo "✅ AWS region: $DEFAULT_REGION"
fi

# --------- Summary ---------
echo ""
echo "🎯 All critical checks passed!"
echo "You’re ready to deploy your Spring Boot microservices via Elastic Beanstalk 🚀"
echo ""
