#!/usr/bin/env bash
set -e

echo "Building Java project..."

cd "$(dirname "$0")/.."  # Move from wrappers/ to project root
mvn clean install
