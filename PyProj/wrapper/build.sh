#!/usr/bin/env bash
set -e
echo "Building Python project..."
mise use -y
pip install -r requirements.txt
python setup.py build
