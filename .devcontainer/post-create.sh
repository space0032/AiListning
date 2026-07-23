#!/bin/bash
set -e

echo "=== AI Listing Generator - Dev Setup ==="

# Java & Maven check
echo "Java: $(java -version 2>&1 | head -1)"
echo "Maven: $(mvn -version 2>&1 | head -1)"
echo "Node: $(node -v)"
echo "npm: $(npm -v)"

# Backend dependencies
echo ""
echo "--- Downloading Maven dependencies ---"
mvn dependency:go-offline -q 2>/dev/null || mvn dependency:go-offline

# Frontend dependencies
echo ""
echo "--- Installing frontend dependencies ---"
cd frontend && npm install && cd ..

# Init Ollama model in background
echo ""
echo "--- Starting Ollama model download (background) ---"
nohup bash -c '
  sleep 10
  until curl -s http://localhost:11434/api/tags > /dev/null 2>&1; do
    sleep 5
  done
  ollama pull qwen3.5:0.8b 2>/dev/null || echo "Ollama not available locally, skipping model pull"
' &>/dev/null &

echo ""
echo "=== Setup complete ==="
echo ""
echo "Run the app with:"
echo "  Backend:   mvn spring-boot:run"
echo "  Frontend:  cd frontend && npm run dev"
echo ""
echo "Or run both: docker compose -f docker-compose.yml up -d (production mode)"
