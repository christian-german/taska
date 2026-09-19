## Why

The frontend's unit tests verify selected request shapes with an in-memory HTTP backend, but no automated check currently sends the Angular client's real outgoing requests through the authoritative OpenAPI contract. A frontend-only contract path is needed so request drift is detected without starting Taska or its authentication and database dependencies.

## What Changes

- Add an official Stoplight Prism mock service to the centralized `contract-tests` Docker Compose stack and serve the maintained Taska OpenAPI document with request errors enabled.
- Add a centralized frontend contract suite that invokes every HTTP-producing Angular service method against Prism without adding Prism code or dependencies to `taska-frontend`.
- Add `contract-tests/verify-frontend.sh` to start and wait for Prism, run the suite with a test-only API URL override, render a deterministic agent-friendly Markdown report, propagate failures, and always clean up.
- Document how frontend request validation complements the existing Schemathesis backend validation.

## Capabilities

### New Capabilities

- `frontend-openapi-contract-testing`: Validate all outgoing Angular service requests against the maintained OpenAPI specification through an isolated Prism mock and report actionable violations.

### Modified Capabilities

None.

## Impact

The change affects only `contract-tests`, its generated reports, and local OpenSpec documentation. It reuses the frontend's installed Angular and Vitest toolchain and source services but does not modify production frontend code, frontend package dependencies, the OpenAPI location, or backend startup behavior.
