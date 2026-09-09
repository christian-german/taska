## 1. Persistence and backend domain

- [x] 1.1 Add a forward Flyway migration that removes saved-filter persistence
- [x] 1.2 Delete the Spring saved-filter domain and its tests
- [x] 1.3 Remove the named task-filter parameter and service behavior while preserving remaining task queries

## 2. Public and MCP contracts

- [x] 2.1 Remove saved-filter paths/schemas and the task-list filter parameter from OpenAPI
- [x] 2.2 Remove named filter input from MCP task tools and update MCP coverage

## 3. Web and Android clients

- [x] 3.1 Delete Angular saved-filter models, service, screens, routes, startup loading, and navigation
- [x] 3.2 Remove named task-filter request support and update affected web tests/styles
- [x] 3.3 Remove any obsolete Android named-filter API input and update affected tests

## 4. Documentation and specifications

- [x] 4.1 Remove saved-filter and named-filter feature documentation from maintained docs and audits
- [x] 4.2 Synchronize the filter-removal and MCP deltas into canonical OpenSpec specifications
- [x] 4.3 Scan maintained sources for residual retired endpoints, types, routes, persistence, and named query inputs

## 5. Verification

- [x] 5.1 Run backend tests and a production-shaped migration/startup check
- [x] 5.2 Run web tests/build and Android unit tests
- [x] 5.3 Bundle/validate OpenAPI, validate OpenSpec, and review the complete local diff
