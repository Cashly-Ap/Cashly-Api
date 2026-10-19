#!/usr/bin/env bash
# Destructive, explicitly targeted seed reset; never removes Docker volumes or Flyway history.
set -euo pipefail
if [[ $# != 2 ]]; then
  echo "Usage: $0 <beta|dev|test> <mydatabase|cashly|cashly_beta|cashly_dev|cashly_test>" >&2
  exit 2
fi
seed_environment=$1
seed_database=$2
case "$seed_environment" in beta|dev|test) ;; *) echo 'Only beta/dev/test targets are allowed.' >&2; exit 2;; esac
case "$seed_database" in mydatabase|cashly|cashly_beta|cashly_dev|cashly_test) ;; *) echo 'Database is not an allowed disposable target.' >&2; exit 2;; esac
cd "$(dirname "$0")/.."
# Both shell and SQL validate the target. Password remains inside the MySQL container.
# stop the API separately before running: no traffic may race with a reset.
docker compose --env-file .env exec -T -e CASHLY_SEED_ENVIRONMENT="$seed_environment" -e CASHLY_SEED_DATABASE="$seed_database" mysql sh -eu -s <<'CASHLY_RESET'
  [ "$MYSQL_DATABASE" = "$CASHLY_SEED_DATABASE" ] || { echo "Database differs from explicitly acknowledged target" >&2; exit 2; }
  export MYSQL_PWD="$MYSQL_PASSWORD"
  seed_input=$(mktemp)
  trap 'rm -f "$seed_input"' EXIT
  printf "SET @cashly_seed_environment=\047%s\047; SET @cashly_seed_database=\047%s\047;\n" "$CASHLY_SEED_ENVIRONMENT" "$CASHLY_SEED_DATABASE" > "$seed_input"
  awk '/^SOURCE / {
    source_file=$2
    sub(/;$/, "", source_file)
    while ((source_status=(getline source_line < source_file)) > 0) print source_line
    close(source_file)
    if (source_status < 0) exit 1
    next
  } { print }' /seed.sql >> "$seed_input"
  mysql --batch --default-character-set=utf8mb4 --user="$MYSQL_USER" "$MYSQL_DATABASE" < "$seed_input"
CASHLY_RESET
