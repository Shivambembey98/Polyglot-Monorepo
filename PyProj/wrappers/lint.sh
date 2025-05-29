#!/usr/bin/env bash
set -e
echo "Running Python linters (Ruff + Black)..."

cd "$(dirname "$0")/.."
# Auto-fix with Ruff and Black
ruff . --fix || true
black . || true
