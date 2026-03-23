#!/bin/bash

# Script to run tests with MySQL database in Docker
# Database is created fresh for each run and deleted on completion

set -e

echo "🧪 Lexand API - Test Runner with MySQL"
echo "======================================"
echo ""

# Cleanup function
cleanup() {
    echo ""
    echo "🧹 Cleaning up test containers..."
    docker-compose --profile test down -v
    echo "✅ Cleanup completed"
}

# Register cleanup function to run on exit
trap cleanup EXIT

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Parse arguments - Now accepts multiple space-separated tests
if [ $# -eq 0 ]; then
    echo "📋 Running ALL tests..."
    TEST_COMMAND="./mvnw clean test"
else
    # Join all arguments with comma for -Dtest
    TEST_FILTER=$(IFS=,; echo "$*")
    echo "📋 Running tests: ${YELLOW}${TEST_FILTER}${NC}"
    TEST_COMMAND="./mvnw clean test -Dtest=${TEST_FILTER}"
fi

echo ""
echo "🐳 Step 1: Starting test database (MySQL)..."
docker-compose --profile test up -d mysql-test

echo ""
echo "⏳ Step 2: Waiting for MySQL to be ready..."
until docker-compose --profile test exec -T mysql-test mysqladmin ping -h localhost -u root -ptestroot --silent; do
    echo "   Waiting for MySQL..."
    sleep 2
done

echo ""
echo "${GREEN}✅ MySQL ready!${NC}"
echo ""
echo "🔧 Step 3: Running tests..."
echo "   Command: ${TEST_COMMAND}"
echo ""

# Run tests
if docker-compose --profile test run --rm app-test ${TEST_COMMAND}; then
    echo ""
    echo "${GREEN}✅ Tests completed successfully!${NC}"
    exit 0
else
    echo ""
    echo "${RED}❌ Tests failed. See logs above.${NC}"
    exit 1
fi
