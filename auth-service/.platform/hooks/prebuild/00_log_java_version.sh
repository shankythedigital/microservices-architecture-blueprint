#!/bin/bash
set -e
echo ">>> [prebuild hook] java -version:" >> /var/log/eb-activity.log 2>&1 || true
java -version >> /var/log/eb-activity.log 2>&1 || echo "java not found" >> /var/log/eb-activity.log 2>&1 || true
