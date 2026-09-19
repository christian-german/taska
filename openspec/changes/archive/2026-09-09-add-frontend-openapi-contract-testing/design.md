## Context

Backend contract testing already lives under `contract-tests`: Docker Compose owns external services, shell scripts own lifecycle and exit semantics, the maintained schema is mounted read-only from `docs/openapi`, and reports are generated below `contract-tests/reports`. Angular service unit tests use `HttpTestingController`; they are valuable behavior tests but intentionally intercept requests before the network, so they cannot exercise a contract-validating mock server.

The frontend has six services that produce HTTP traffic. Their API roots are captured from the mutable `environment` object when each service is instantiated. The installed frontend toolchain already includes Angular, TypeScript, and Vitest, allowing an external test suite to import and instantiate those services without adding a package or source file to the Angular project.

## Goals / Non-Goals

**Goals:**

- Exercise every public HTTP-producing frontend service method through the real Angular `HttpClient` against Prism.
- Make Prism the validator for bodies, paths, queries, headers, required fields, types, and unexpected object properties.
- Keep the mock, suite, configuration, renderer, raw artifacts, and orchestration under `contract-tests`.
- Produce a deterministic Markdown summary with actionable details for every failed test.
- Preserve the backend Schemathesis workflow unchanged.

**Non-Goals:**

- Validate response compatibility or browser user journeys.
- Replace existing Angular unit tests or Schemathesis backend tests.
- Add Prism, a contract-test script, or a contract-test source file to `taska-frontend`.
- Start the backend, databases, or identity provider for frontend contract validation.

## Decisions

1. **Run the official Prism image as a Compose tool service.** `stoplight/prism:5` will mount the existing `docs/openapi` tree read-only, run `mock` with `--errors`, publish port 4010 by default, and expose a health check. This matches the containerized Schemathesis convention and makes invalid requests fail at the HTTP boundary. A host-installed Prism dependency was rejected because it would decentralize prerequisites.

2. **Keep one service-level integration suite under `contract-tests/frontend`.** The suite will mutate only the imported test process's `environment.apiUrl` before Angular services are instantiated, use the real fetch-backed Angular HTTP client, add deterministic test authorization and response-preference headers, and invoke every HTTP-producing service method. Existing `HttpTestingController` suites remain the detailed unit tests; copying them or converting them to network tests was rejected because it would duplicate coverage and weaken unit isolation.

3. **Use the frontend's existing Vitest installation from outside its project.** A contract-local Vitest configuration and setup file will compile the suite and imported services. The frontend package manifest, Angular workspace, environments, and application sources remain unchanged. A new frontend npm script/configuration was rejected because all Prism-specific entry points must remain centralized.

4. **Make request objects closed in the OpenAPI document.** Prism can reject unexpected JSON properties only when request schemas declare `additionalProperties: false`, including nested request objects. The relevant existing request schemas will be tightened so the stated contract is enforceable by the single source of truth rather than by a second hand-maintained validator.

5. **Render from structured Vitest output and captured Prism diagnostics.** The suite will enrich network failures with the method, endpoint, serialized request, Prism response, and a stable recommended fix. A small contract-local renderer will normalize Vitest JSON and Prism logs into `frontend-contract-report.md` with no timestamp or host-specific paths, making successful and failing reports deterministic.

6. **Use a cleanup trap and preserve the test status.** `verify-frontend.sh` will validate prerequisites, replace prior frontend artifacts, start only Prism, wait through Compose health checks, run Vitest, render the report, and always remove Prism. Startup, test, and report failures all yield non-zero status.

## Risks / Trade-offs

- [Prism-generated GET responses drive multi-request service methods] → Request deterministic 2xx responses and use schema-valid identifiers and payloads; keep response validation explicitly out of scope.
- [A new frontend service method is added without contract coverage] → Keep the suite organized one test per public HTTP method and document that service additions require extending the centralized matrix.
- [Closed request schemas expose previously tolerated extra fields] → Apply `additionalProperties: false` only to inbound request objects used by maintained endpoints; any resulting failure identifies real contract drift.
- [Port 4010 is occupied] → Allow `PRISM_PORT` to override the published host port while retaining the container's dedicated port.
- [Prism startup fails or the test runner is interrupted] → Capture startup logs and use an EXIT/INT/TERM trap that always executes Compose cleanup.
