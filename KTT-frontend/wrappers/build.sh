#!/usr/bin/env bash
set -e
echo "Building Node.js project..."
cd "$(dirname "$0")/.."  # Move from wrappers/ to project root
npm ci
npm run build
