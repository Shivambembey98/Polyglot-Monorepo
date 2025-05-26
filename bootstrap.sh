#!/bin/bash
set -e

echo "🚀 Unified Bootstrap for Monorepo Setup"

# Step 1: Ensure mise is present
if ! command -v mise &> /dev/null; then
  echo "❌ mise is not installed. Install it first: https://mise.jdx.dev"
  exit 1
fi

# Step 2: Install all tools as per .tool-versions
echo "🔧 Installing tools using mise..."
mise install

# Step 3: Define install functions
install_python() {
  echo "🐍 Installing Python deps in $1"
  pip install --upgrade pip
  pip install -r "$1/requirements.txt"
}

install_node() {
  echo "🟢 Installing Node.js deps in $1"
  cd "$1"
  npm install --legacy-peer-deps
  cd - > /dev/null
}

install_java() {
  echo "☕ Installing Java deps in $1"
  cd "$1"
  mvn clean install -DskipTests
  cd - > /dev/null
}

# Step 4: Walk through known folders and detect language
for dir in */; do
  if [[ -f "${dir}requirements.txt" ]]; then
    install_python "$dir"
  elif [[ -f "${dir}package.json" ]]; then
    install_node "$dir"
  elif [[ -f "${dir}pom.xml" ]]; then
    install_java "$dir"
  else
    echo "❓ Skipping $dir — no known dependency files found"
  fi
done

echo "✅ All dependencies installed for all projects!"
