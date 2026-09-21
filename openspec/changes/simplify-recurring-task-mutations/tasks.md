## 1. Contract and backend

- [x] 1.1 Resolve common-series editing and existing-override policies with the user.
- [x] 1.2 Remove successor creation, freeze existing generators and validate series stopping.
- [x] 1.3 Restrict REST and MCP occurrence updates to scheduling while preserving lifecycle and detached history.
- [x] 1.4 Update OpenAPI and contract coverage for removed and restricted operations.

## 2. Maintained clients

- [x] 2.1 Update frontend services, task detail, inline editing, calendars and stop-series wording.
- [x] 2.2 Update Android contracts, repositories, task detail, calendars and stop-series wording.

## 3. Verification

- [x] 3.1 Verify backend rejection, stopping, movement, completion and range-query behavior with tests.
- [x] 3.2 Verify frontend interactions and production build.
- [x] 3.3 Verify Android unit tests and compilation.
- [x] 3.4 Run formatters, contract checks, OpenSpec validation and diff checks; document any environment limitations.

See verification.md for results and existing failures in the broad backend contract suite.
