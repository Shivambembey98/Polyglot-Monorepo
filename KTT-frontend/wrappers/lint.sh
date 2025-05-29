#!/usr/bin/env bash
set -e
echo "Running lint for Node.js..."

cd "$(dirname "$0")/.."

if [ -f "package.json" ]; then
  # Run lint with autofix
  npx eslint . --ext .ts,.js --fix
else
  echo "No package.json found. Skipping lint."
fi
