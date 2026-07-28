#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
ENV_FILE="${REPO_ROOT}/apps/ingestion/.env"

if [[ -f "${ENV_FILE}" ]]; then
  set -a
  source "${ENV_FILE}"
  set +a
fi

export SERVER_PORT="${SERVER_PORT:-8081}"

cd "${REPO_ROOT}"
./mvnw -f apps/ingestion/pom.xml spring-boot:run -DskipTests
