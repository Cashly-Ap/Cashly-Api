#!/bin/bash
# Development entrypoint with automatic hot reload
# Watches src/main/java/ for changes and recompiles, triggering DevTools restart

set -e

cleanup() {
    echo ""
    echo "Shutting down..."
    kill $SPRING_PID 2>/dev/null
    kill $WATCHER_PID 2>/dev/null
    wait $SPRING_PID 2>/dev/null
    exit 0
}
trap cleanup SIGTERM SIGINT SIGQUIT

# Start Spring Boot in background (using system mvn — wrapper 3.9.11 has bug on Java 24)
mvn spring-boot:run -DskipTests -Dmaven.test.skip=true -Dsurefire.skip=true -DfailIfNoTests=false &
SPRING_PID=$!

# Wait for initial compilation and startup
echo "===== Waiting for Spring Boot to start (first build takes a moment) ====="
sleep 12

echo "===== Hot reload active — edit any .java file ====="
echo ""

# Start file watcher in background
(
    inotifywait -m -r -q \
        -e modify -e create -e delete -e move \
        --exclude '/\.' \
        --format '%w%f' \
        src/main/java/ | while read -r CHANGED_FILE; do
        echo "[$(date '+%H:%M:%S')] Changed: $CHANGED_FILE → recompiling..."
        mvn compile -DskipTests -q 2>&1
        echo "[$(date '+%H:%M:%S')] Recompile done."
    done
) &
WATCHER_PID=$!

# Wait for Spring Boot to exit (container stops when it does)
wait $SPRING_PID
