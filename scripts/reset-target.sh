#!/bin/bash

# Script to wipe the compiled target/ directory and recreate the API container.
# Useful after refactoring packages (renaming/moving classes) because stale
# .class files left in the bind-mounted target/ cause Spring to fail at startup
# with ConflictingBeanDefinitionException (duplicate bean names).
#
# The target/ directory is owned by root (the dev container writes it as root),
# so it is removed through a throwaway Alpine container instead of a plain rm.

set -e

COMPOSE_CMD=(docker compose)
if ! docker compose version >/dev/null 2>&1; then
    COMPOSE_CMD=(docker-compose)
fi

echo "🧹 Resetting compiled output and recreating the API container..."
echo ""

echo "🛑 Step 1: Stopping the app container (MySQL is left untouched)..."
"${COMPOSE_CMD[@]}" stop app 2>/dev/null || true

echo ""
echo "🗑️  Step 2: Removing target/ (root-owned, deleted via Docker)..."
docker run --rm -v "$PWD":/app alpine rm -rf /app/target

if [ -e target ]; then
    echo "❌ Could not remove target/ — aborting"
    exit 1
fi

echo ""
echo "🐳 Step 3: Recreating the app container (rebuild, keep DB data)..."
"${COMPOSE_CMD[@]}" up -d --build --force-recreate --no-deps app

echo ""
echo "⏳ Waiting for Spring Boot to start..."
sleep 20

if "${COMPOSE_CMD[@]}" ps app | grep -q "Up"; then
    echo ""
    echo "✅ Container is up. Verifying startup in logs..."
    "${COMPOSE_CMD[@]}" logs app --tail=200 2>&1 | grep -E "Started CashlyApplication" && {
        echo ""
        echo "✅ Cashly API started successfully!"
        exit 0
    }
    echo ""
    echo "⚠️  Container is up but 'Started CashlyApplication' not found yet."
    echo "   Inspect logs with: ${COMPOSE_CMD[*]} logs -f app"
    exit 1
fi

echo ""
echo "❌ App container is not running. Inspect logs with:"
echo "   ${COMPOSE_CMD[*]} logs app --tail=200"
exit 1
