#!/usr/bin/env bash
set -e
echo "☕ Building Java project..."
mise use -y
mvn clean install
