## 1. Enforceable OpenAPI request contract

- [x] 1.1 Close every frontend-used request object in the maintained OpenAPI schemas so Prism rejects unexpected properties
- [x] 1.2 Add the official Prism mock service, schema mount, dedicated port, error mode, and health check to the existing contract-test Compose stack

## 2. Centralized frontend request suite

- [x] 2.1 Add contract-local Vitest configuration and Angular test setup with a test-only API URL override and request diagnostic capture
- [x] 2.2 Exercise every public HTTP-producing method in the Angular comment, label, planning-calendar, project, task, and version services against Prism

## 3. Orchestration and reporting

- [x] 3.1 Add a deterministic renderer for frontend test totals and actionable Prism contract violations
- [x] 3.2 Add `verify-frontend.sh` with prerequisite checks, Prism startup/readiness, frontend test execution, report generation, status propagation, and unconditional cleanup

## 4. Documentation and verification

- [x] 4.1 Document the frontend command, Prism behavior, reports, isolation, and relationship to Schemathesis in the contract-test README
- [x] 4.2 Run frontend unit tests, frontend contract tests, shell/static checks, and strict OpenSpec validation
