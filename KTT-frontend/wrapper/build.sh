#!/usr/bin/env bash
set -e
echo "Building Node.js project..."
mise use -y
npm ci
npm run build
