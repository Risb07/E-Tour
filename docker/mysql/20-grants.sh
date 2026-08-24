#!/bin/bash
# =============================================================================
# Re-grant the application user's rights after the seed dump has run, and create
# the notification service's database and user.
#
# Why this exists:
#   MySQL's entrypoint creates MYSQL_USER and grants it rights on MYSQL_DATABASE
#   BEFORE anything in /docker-entrypoint-initdb.d runs. The eTour dump then
#   opens with:
#
#       DROP DATABASE IF EXISTS `etour`;
#       CREATE DATABASE `etour`;
#
#   Recreating the database out from under that grant is exactly the situation
#   where access is easy to lose. Rather than depend on MySQL's behaviour here,
#   this asserts the user and its privileges again, afterwards.
#
#   Every statement is idempotent, so it is a no-op when the grant did survive.
#
# Runs once, on first start with an empty data volume, after 10-seed.sql -
# init files execute in alphabetical order.
#
# NOTE on style: deliberately no `set -euo pipefail`. The MySQL entrypoint
# executes this file only if it carries the executable bit and otherwise
# SOURCES it - and bind mounts from Windows routinely drop that bit. Sourced,
# any shell options set here would persist into the rest of the entrypoint,
# where an unbound-variable reference under `set -u` would abort startup. So
# the exit status is checked explicitly instead.
# =============================================================================

DB_NAME="${MYSQL_DATABASE:-etour}"
DB_USER="${MYSQL_USER:-etour}"
DB_PASS="${MYSQL_PASSWORD:-etourpassword}"

# The notification microservice's own database and user, on this same server.
# Scoped to its own schema, so it cannot read or write an eTour table - see
# ../../notification-service/README.md.
N_NAME="${NOTIFY_DB_NAME:-etour_notify}"
N_USER="${NOTIFY_DB_USERNAME:-notify}"
N_PASS="${NOTIFY_DB_PASSWORD:-notifypassword}"

echo "[20-grants] ensuring '${DB_USER}' can reach '${DB_NAME}'"
echo "[20-grants] ensuring '${N_USER}' can reach '${N_NAME}'"

mysql --protocol=socket -uroot -p"${MYSQL_ROOT_PASSWORD}" <<-EOSQL
	CREATE DATABASE IF NOT EXISTS \`${DB_NAME}\`
	    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

	CREATE USER IF NOT EXISTS '${DB_USER}'@'%' IDENTIFIED BY '${DB_PASS}';
	ALTER USER '${DB_USER}'@'%' IDENTIFIED BY '${DB_PASS}';

	GRANT ALL PRIVILEGES ON \`${DB_NAME}\`.* TO '${DB_USER}'@'%';

	CREATE DATABASE IF NOT EXISTS \`${N_NAME}\`
	    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

	CREATE USER IF NOT EXISTS '${N_USER}'@'%' IDENTIFIED BY '${N_PASS}';
	ALTER USER '${N_USER}'@'%' IDENTIFIED BY '${N_PASS}';

	-- Only its own schema. ALL here also covers CREATE on that database, which
	-- is what lets the service's ddl-auto build its tables on first run.
	GRANT ALL PRIVILEGES ON \`${N_NAME}\`.* TO '${N_USER}'@'%';

	FLUSH PRIVILEGES;
EOSQL

if [ $? -ne 0 ]; then
	echo "[20-grants] FAILED - the backend will not be able to connect" >&2
	exit 1
fi

echo "[20-grants] done"
