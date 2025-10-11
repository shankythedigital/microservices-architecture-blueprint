#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
mvn -DskipTests package
java -jar target/asset-service-1.0.0.jar
