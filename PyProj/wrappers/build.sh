#!/usr/bin/env bash
set -e
echo "Building Python project..."
cd "$(dirname "$0")/.."  # Move from wrappers/ to project root
pip install -r requirements.txt
python setup.py build
