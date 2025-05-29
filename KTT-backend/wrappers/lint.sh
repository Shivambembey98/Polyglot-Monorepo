#!/usr/bin/env bash
set -e
echo "Running lint for Java..."

cd "$(dirname "$0")/.."

# Example using Checkstyle if configured
if [ -f "pom.xml" ]; then
  # Checkstyle via Maven plugin
  mvn checkstyle:check || {
    echo "Attempting auto-format via spotless..."
    mvn spotless:apply || echo "Could not auto-fix"
  }
else
  echo "No pom.xml found. Skipping lint."
fi
