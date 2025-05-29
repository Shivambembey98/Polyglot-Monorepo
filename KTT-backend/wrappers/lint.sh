#!/usr/bin/env bash
set -e
echo "Running lint for Java..."

cd "$(dirname "$0")/.."

# Example using Checkstyle if configured
if [ -f "pom.xml" ]; then
  mvn checkstyle:check || echo "Lint errors found."
else
  echo "No pom.xml found. Skipping lint."
fi
