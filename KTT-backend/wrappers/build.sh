#!/usr/bin/env bash
set -e

echo "Building Java project..."

# Point mise to repo root to read `.tool-versions` This ensures mise reads the correct .tool-versions file from root no matter where you run the script from.
mise use -y --path "$(git rev-parse --show-toplevel)"

mvn clean install
