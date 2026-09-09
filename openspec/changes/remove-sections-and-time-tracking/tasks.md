## 1. Persistence and backend domains

- [x] 1.1 Add a forward Flyway migration that removes task section relationships, sections, and time entries
- [x] 1.2 Delete the Spring section and time-entry domains and remove their project-service/controller dependencies
- [x] 1.3 Remove section fields, filters, repository queries, mapping, and mutation behavior from the task domain

## 2. Public and MCP contracts

- [x] 2.1 Remove section and time-entry paths, schemas, parameters, and references from the OpenAPI contract
- [x] 2.2 Remove section inputs and outputs from MCP task tools and update MCP integration coverage

## 3. Web client

- [x] 3.1 Delete Angular section/time-entry models and services plus the time-tracker route, navigation, and component
- [x] 3.2 Simplify project and task client behavior to remove section loading, grouping, filtering, and payload fields
- [x] 3.3 Update Angular tests and remove feature-only styling or text

## 4. Android client

- [x] 4.1 Delete Android time-entry API, repository, and model code
- [x] 4.2 Remove timer state, actions, controls, tests, and feature-specific design assets from task detail
- [x] 4.3 Remove section fields from Android task models and request construction

## 5. Documentation and residual references

- [x] 5.1 Remove section and time-tracking feature documentation while preserving unrelated scheduling, estimates, generic layout sections, and immutable history
- [x] 5.2 Scan tracked maintained sources for residual retired API, model, UI, and persistence references and remove them

## 6. Verification

- [x] 6.1 Run backend tests and static/build checks
- [x] 6.2 Run web tests/build and Android unit tests/build checks
- [x] 6.3 Validate OpenAPI and OpenSpec contracts and review the complete local diff
