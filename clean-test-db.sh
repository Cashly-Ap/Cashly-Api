#!/bin/bash

# Script to clean test database and recreate it fresh
# Useful when you want to start from scratch without bringing down containers

set -e

echo "🗑️  Cleaning test database..."

# Check if MySQL test container is running
if ! docker-compose --profile test ps mysql-test | grep -q "Up"; then
    echo "❌ mysql-test container is not running"
    echo "   Run first: docker-compose --profile test up -d mysql-test"
    exit 1
fi

echo "📋 Dropping all tables..."

# Execute Flyway clean to remove everything
docker-compose --profile test run --rm app-test ./mvnw flyway:clean -Dflyway.cleanDisabled=false

echo "🔧 Recreating schema with migrations..."

# Execute Flyway migrate to recreate
docker-compose --profile test run --rm app-test ./mvnw flyway:migrate

echo "✅ Database cleaned and recreated!"
echo ""
echo "Now you can run your tests with:"
echo "  ./run-tests.sh"
echo "  or"
echo "  ./run-tests.sh DifferentialControllerIntegrationTest"
