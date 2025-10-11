#!/usr/bin/env bash
# -------------------------------------------------------------------
# test-mysql.sh  —  Compile & run JdbcTest.java safely
# -------------------------------------------------------------------

JAR="mysql-connector-j-8.0.33.jar"
JAVA_FILE="JdbcTest.java"
CLASS_FILE="JdbcTest.class"

# Detect OS path separator (':' for Unix, ';' for Windows/WSL)
SEP=":"
[[ "$OSTYPE" == "msys" || "$OSTYPE" == "win"* ]] && SEP=";"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo "------------------------------------------------------------"
echo "🔍 Checking environment..."
echo "------------------------------------------------------------"

# --- Check Java installation ---
if ! command -v java >/dev/null 2>&1; then
  echo -e "${RED}❌ Java not found!${NC}"
  echo "➡️  Install OpenJDK 17:"
  echo "   brew install openjdk@17"
  exit 1
fi

if ! command -v javac >/dev/null 2>&1; then
  echo -e "${RED}❌ javac not found (Java compiler missing)!${NC}"
  exit 1
fi

# --- Check driver jar ---
if [ ! -f "$JAR" ]; then
  echo -e "${RED}❌ MySQL Connector JAR not found:${NC} $JAR"
  echo "➡️  Download from: https://dev.mysql.com/downloads/connector/j/"
  exit 1
fi

# --- Compile if needed ---
if [ ! -f "$CLASS_FILE" ] || [ "$JAVA_FILE" -nt "$CLASS_FILE" ]; then
  echo "🧩 Compiling $JAVA_FILE..."
  javac -cp "$JAR" "$JAVA_FILE" || { echo -e "${RED}❌ Compilation failed${NC}"; exit 1; }
fi

# --- Run the test ---
echo "🚀 Running JDBC connection test..."
echo "------------------------------------------------------------"
java -cp ".${SEP}${JAR}" JdbcTest
RESULT=$?
echo "------------------------------------------------------------"

if [ $RESULT -eq 0 ]; then
  echo -e "${GREEN}✅ JDBC connection test finished successfully.${NC}"
else
  echo -e "${RED}❌ JDBC connection test failed (code $RESULT).${NC}"
fi
