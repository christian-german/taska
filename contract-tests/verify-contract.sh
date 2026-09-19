#!/usr/bin/env bash

set -Eeuo pipefail

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
REPORTS_DIR="${SCRIPT_DIR}/reports"
RAW_REPORTS_DIR="${REPORTS_DIR}/raw"
PROJECT_NAME="taska-contract-tests"
STARTUP_TIMEOUT="${CONTRACT_TEST_STARTUP_TIMEOUT:-300}"
COMPOSE=(docker compose --project-directory "${SCRIPT_DIR}" --project-name "${PROJECT_NAME}" -f "${SCRIPT_DIR}/docker-compose.yml")

cleanup() {
  local original_status=$?
  trap - EXIT INT TERM
  printf 'Cleaning up the contract-test stack...\n' >&2
  "${COMPOSE[@]}" down -v --remove-orphans >/dev/null 2>&1 || true
  if (( original_status == 0 )); then
    exit 0
  fi
  exit 1
}

trap cleanup EXIT
trap 'exit 1' INT TERM

if ! command -v docker >/dev/null 2>&1; then
  printf 'docker is required to run the OpenAPI contract tests.\n' >&2
  exit 1
fi
if ! docker compose version >/dev/null 2>&1; then
  printf 'The Docker Compose plugin is required to run the OpenAPI contract tests.\n' >&2
  exit 1
fi

mkdir -p "${RAW_REPORTS_DIR}"
rm -f \
  "${RAW_REPORTS_DIR}/junit.xml" \
  "${RAW_REPORTS_DIR}/events.ndjson" \
  "${RAW_REPORTS_DIR}/schema-coverage.html" \
  "${RAW_REPORTS_DIR}/schemathesis.log" \
  "${RAW_REPORTS_DIR}/startup.log" \
  "${REPORTS_DIR}/contract-report.html" \
  "${REPORTS_DIR}/contract-report.md" \
  "${REPORTS_DIR}/contract-report.json"

export CONTRACT_TEST_UID="$(id -u)"
export CONTRACT_TEST_GID="$(id -g)"

printf 'Starting Taska, PostgreSQL, and Authentik from local sources...\n'
if ! "${COMPOSE[@]}" up \
    --detach \
    --build \
    --wait \
    --wait-timeout "${STARTUP_TIMEOUT}" \
    taska-db db-authentik authentik-server authentik-worker taska-backend; then
  "${COMPOSE[@]}" logs --no-color --timestamps \
    authentik-server authentik-worker taska-backend \
    >"${RAW_REPORTS_DIR}/startup.log" 2>&1 || true
  printf 'Contract-test services failed to become healthy. Recent logs:\n' >&2
  tail -n 80 "${RAW_REPORTS_DIR}/startup.log" >&2 || true
  exit 1
fi

printf 'Obtaining an OAuth2 token from Authentik...\n'
ACCESS_TOKEN="$(
  "${COMPOSE[@]}" run --rm --no-deps --no-TTY \
    --entrypoint python \
    schemathesis \
    /contract-tests/get-token.py
)"
if [[ ! "${ACCESS_TOKEN}" =~ ^[^[:space:]]+\.[^[:space:]]+\.[^[:space:]]+$ ]]; then
  printf 'Authentik returned an invalid access token.\n' >&2
  exit 1
fi

printf 'Running Schemathesis in positive mode...\n'
set +e
"${COMPOSE[@]}" run --rm --no-deps --no-TTY \
  schemathesis \
  run /openapi/taska.openapi.yaml \
  --url http://taska-backend:8080 \
  --mode positive \
  --checks all \
  --continue-on-failure \
  --header "Authorization: Bearer ${ACCESS_TOKEN}" \
  --output-sanitize true \
  --output-truncate false \
  --no-color \
  --report junit,ndjson \
  --report-junit-path /reports/raw/junit.xml \
  --report-ndjson-path /reports/raw/events.ndjson \
  >"${RAW_REPORTS_DIR}/schemathesis.log" 2>&1
SCHEMATHESIS_STATUS=$?
set -e
unset ACCESS_TOKEN

set +e
"${COMPOSE[@]}" run --rm --no-deps --no-TTY \
  --entrypoint python \
  schemathesis \
  /contract-tests/render-report.py \
  --junit /reports/raw/junit.xml \
  --ndjson /reports/raw/events.ndjson \
  --log /reports/raw/schemathesis.log \
  --html /reports/contract-report.html \
  --markdown /reports/contract-report.md \
  --json /reports/contract-report.json \
  --schemathesis-exit-code "${SCHEMATHESIS_STATUS}" \
  --schema docs/openapi/taska.openapi.yaml \
  --base-url http://taska-backend:8080
RENDER_STATUS=$?
set -e

if (( RENDER_STATUS != 0 )); then
  printf 'Contract tests: ERROR — report conversion failed. See %s.\n' \
    "${RAW_REPORTS_DIR}/schemathesis.log" >&2
  exit 1
fi

if (( SCHEMATHESIS_STATUS == 0 )); then
  exit 0
fi
exit 1
