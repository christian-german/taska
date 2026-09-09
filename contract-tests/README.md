# OpenAPI contract tests

This directory contains the standalone Taska API contract-test infrastructure. It deliberately
does not participate in the backend Maven build.

## Contract-testing architecture

Both contract paths use `docs/openapi/taska.openapi.yaml` as their single source of truth and keep
their containers, orchestration, suites, and reports in this directory:

- **Backend provider validation:** Schemathesis generates requests from OpenAPI and sends them to a
  real Taska, PostgreSQL, and Authentik stack. This verifies that the backend implements the
  documented contract.
- **Frontend consumer validation:** the maintained Angular HTTP services emit their real requests
  through Angular's fetch-backed `HttpClient` to a Prism mock. Prism validates each request and
  supplies schema-generated responses, so no backend, database, or identity provider is started.

Prism runs only in Docker. No Prism package, script, environment, or test source is added to the
Angular project. The centralized suite reuses the frontend's installed Angular/Vitest toolchain and
sets `environment.apiUrl` only inside the isolated test process before services are instantiated.

## Run backend contract tests

From the repository root:

```bash
./contract-tests/verify-contract.sh
```

Docker Engine and the Docker Compose plugin are the only host prerequisites. The command:

1. builds the backend from the current `taska-backend` sources;
2. starts isolated PostgreSQL databases for Taska and Authentik;
3. starts Authentik Server and Worker with the existing
   `taska-backend/authentik/blueprints/taska.yaml` blueprint;
4. waits on real container health checks and obtains an OAuth2 token for the development
   `user` / `pass` account through Authorization Code + PKCE;
5. runs `ghcr.io/schemathesis/schemathesis:stable` in positive generation mode against
   `docs/openapi/taska.openapi.yaml`;
6. writes reports and always removes the containers and volumes.

The script returns `0` only when Schemathesis succeeds. Contract failures, startup errors,
authentication errors, and report-conversion errors all return `1`. The stack is removed by an
exit trap, including after interruption. Set `CONTRACT_TEST_STARTUP_TIMEOUT` to change the
default 300-second container startup deadline.

## Run frontend contract tests

Install the normal frontend development dependencies once, then run from the repository root:

```bash
cd taska-frontend && npm ci && cd ..
./contract-tests/verify-frontend.sh
```

`verify-frontend.sh` starts only the official `stoplight/prism:5` image on host port `4010`, waits
for its health check, and runs the centralized service-level suite. Prism serves
`docs/openapi/taska.openapi.yaml` from a read-only mount and uses mock mode with request errors
enabled. Invalid bodies, path and query parameters, headers, missing or incorrectly typed
properties, and unexpected properties therefore fail the corresponding frontend test.

The script always removes Prism, including when startup, a test, report rendering, or an interrupt
fails. It exits `0` only when all 32 frontend contract cases and report generation succeed. Use
`PRISM_PORT=<port>` to change the published host port, or
`FRONTEND_CONTRACT_STARTUP_TIMEOUT=<seconds>` to change the default 60-second readiness deadline.

The suite has one case for every public method and meaningful request branch in the Angular
comment, label, planning-calendar, project, task, and version services. Existing Angular unit tests
remain unchanged: they continue to use `HttpTestingController` for detailed client behavior, while
this suite verifies the network requests against OpenAPI.

## Reports

Generated reports are local artifacts under `contract-tests/reports/`:

- `frontend-contract-report.md`: deterministic frontend summary with test totals and, for every
  violation, the endpoint, redacted request, Prism message, and recommended fix;
- `contract-report.html`: standalone human-readable report;
- `contract-report.md`: concise report suitable for review by a person or coding agent;
- `contract-report.json`: structured failures, operations, diagnostics, reproduction commands,
  and source artifact paths for automated remediation;
- `raw/junit.xml`: sanitized Schemathesis JUnit report;
- `raw/events.ndjson`: sanitized Schemathesis event stream;
- `raw/schema-coverage.html`: Schemathesis HTML coverage report;
- `raw/schemathesis.log`: sanitized console output for infrastructure diagnostics.
- `raw/startup.log`: service logs when the stack fails its startup health checks.
- `raw/frontend-vitest.json`: structured frontend test results used by the Markdown renderer;
- `raw/frontend-vitest.log`: frontend runner diagnostics;
- `raw/prism.log`: Prism startup and request-validation diagnostics.

Reports are replaced on each run and are not committed.
