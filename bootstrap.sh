#!/usr/bin/env bash
set -e

echo "Installing tools via mise..."
mise install

for dir in */; do
  if [ -f "$dir/wrappers/build.sh" ]; then
    echo "Found project: $dir"
    (cd "$dir" && chmod +x wrappers/*.sh)
  fi
done
