#!/usr/bin/env bash

set -Eeuo pipefail

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
REPOSITORY_DIR="$(CDPATH= cd -- "${SCRIPT_DIR}/.." && pwd)"
FRONTEND_DIR="${REPOSITORY_DIR}/taska-frontend"
REPORTS_DIR="${SCRIPT_DIR}/reports"
RAW_REPORTS_DIR="${REPORTS_DIR}/raw"
PROJECT_NAME="taska-frontend-contract-tests"
STARTUP_TIMEOUT="${FRONTEND_CONTRACT_STARTUP_TIMEOUT:-60}"
PRISM_PORT="${PRISM_PORT:-4010}"
VITEST="${FRONTEND_DIR}/node_modules/.bin/vitest"
COMPOSE=(docker compose --project-directory "${SCRIPT_DIR}" --project-name "${PROJECT_NAME}" --profile tools -f "${SCRIPT_DIR}/docker-compose.yml")

cleanup() {
  local original_status=$?
  trap - EXIT INT TERM
  printf 'Cleaning up the frontend contract-test stack...\n' >&2
  "${COMPOSE[@]}" down --remove-orphans >/dev/null 2>&1 || true
  exit "${original_status}"
}

trap cleanup EXIT
trap 'exit 1' INT TERM

if ! command -v docker >/dev/null 2>&1; then
  printf 'docker is required to run the frontend OpenAPI contract tests.\n' >&2
  exit 1
fi
if ! docker compose version >/dev/null 2>&1; then
  printf 'The Docker Compose plugin is required to run the frontend OpenAPI contract tests.\n' >&2
  exit 1
fi
if ! command -v python3 >/dev/null 2>&1; then
  printf 'python3 is required to render the frontend contract report.\n' >&2
  exit 1
fi
if [[ ! -x "${VITEST}" ]]; then
  printf 'Frontend dependencies are missing; run npm ci in %s first.\n' "${FRONTEND_DIR}" >&2
  exit 1
fi
if [[ ! "${PRISM_PORT}" =~ ^[0-9]+$ ]] || (( PRISM_PORT < 1 || PRISM_PORT > 65535 )); then
  printf 'PRISM_PORT must be an integer from 1 through 65535.\n' >&2
  exit 1
fi

mkdir -p "${RAW_REPORTS_DIR}"
rm -f \
  "${RAW_REPORTS_DIR}/frontend-vitest.json" \
  "${RAW_REPORTS_DIR}/frontend-vitest.log" \
  "${RAW_REPORTS_DIR}/prism.log" \
  "${REPORTS_DIR}/frontend-contract-report.md"

export PRISM_PORT

printf 'Starting Prism against docs/openapi/taska.openapi.yaml...\n'
if ! "${COMPOSE[@]}" up --detach --force-recreate --wait --wait-timeout "${STARTUP_TIMEOUT}" prism; then
  "${COMPOSE[@]}" logs --no-color prism >"${RAW_REPORTS_DIR}/prism.log" 2>&1 || true
  python3 "${SCRIPT_DIR}/render-frontend-report.py" \
    --vitest "${RAW_REPORTS_DIR}/frontend-vitest.json" \
    --prism-log "${RAW_REPORTS_DIR}/prism.log" \
    --markdown "${REPORTS_DIR}/frontend-contract-report.md" \
    --vitest-exit-code 1
  printf 'Prism failed to become healthy. See %s.\n' "${RAW_REPORTS_DIR}/prism.log" >&2
  exit 1
fi

printf 'Running Angular service requests against Prism...\n'
set +e
TASKA_CONTRACT_API_URL="http://127.0.0.1:${PRISM_PORT}" \
  "${VITEST}" run \
  --config "${SCRIPT_DIR}/frontend/vitest.config.mjs" \
  --reporter json \
  --outputFile "${RAW_REPORTS_DIR}/frontend-vitest.json" \
  >"${RAW_REPORTS_DIR}/frontend-vitest.log" 2>&1
VITEST_STATUS=$?
set -e

"${COMPOSE[@]}" logs --no-color prism >"${RAW_REPORTS_DIR}/prism.log" 2>&1 || true

set +e
python3 "${SCRIPT_DIR}/render-frontend-report.py" \
  --vitest "${RAW_REPORTS_DIR}/frontend-vitest.json" \
  --prism-log "${RAW_REPORTS_DIR}/prism.log" \
  --markdown "${REPORTS_DIR}/frontend-contract-report.md" \
  --vitest-exit-code "${VITEST_STATUS}"
RENDER_STATUS=$?
set -e

if (( RENDER_STATUS != 0 )); then
  printf 'Frontend contract report conversion failed. See %s.\n' \
    "${RAW_REPORTS_DIR}/frontend-vitest.log" >&2
  exit 1
fi

if (( VITEST_STATUS != 0 )); then
  printf 'Frontend contract validation failed. See %s.\n' \
    "${REPORTS_DIR}/frontend-contract-report.md" >&2
  exit 1
fi

printf 'Frontend contract validation passed. Report: %s\n' \
  "${REPORTS_DIR}/frontend-contract-report.md"
