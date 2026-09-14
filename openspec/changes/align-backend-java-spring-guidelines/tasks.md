## 1. Resource application boundaries

- [x] 1.1 Complete the label feature package layout, request-to-parameter mapping, replacement behavior, and tests without losing the existing local work
- [x] 1.2 Refactor comments into controller/service/repository layers with separate create/update requests and transport-independent service parameters
- [x] 1.3 Refactor planning calendars into controller/service/repository layers with immutable application results and complete create/update contracts
- [x] 1.4 Refactor projects into controller/service/repository layers with create/update/reorder application parameters and complete replacement behavior

## 2. Task and supporting feature boundaries

- [x] 2.1 Refactor task create/update controllers, requests, mappers, repositories, services, and recurrence types so services do not depend on HTTP DTOs
- [x] 2.2 Refactor priority evaluation and notification components into their feature-owned layers and remove direct controller persistence access
- [x] 2.3 Update MCP project and task tools to map into the same application parameter types and authoritative mutation behavior
- [x] 2.4 Add automated architecture checks for package responsibility, transport independence, repository direction, and constructor-injected final dependencies

## 3. Contracts and first-party clients

- [x] 3.1 Update OpenAPI component and path schemas for operation-specific strict create/update contracts and complete replacement requirements
- [x] 3.2 Update Angular label, comment, project, planning-calendar, and task services/callers to send the new request contracts
- [x] 3.3 Update Android task creation models/callers for the operation-specific request contract

## 4. Verification

- [x] 4.1 Add or update backend controller, service, MCP, and contract tests for complete replacement, omitted/null properties, unknown properties, and server-controlled state
- [x] 4.2 Run the backend test suite and static diff checks
- [x] 4.3 Bundle and validate OpenAPI, build/test Angular, and run relevant Android unit/compile checks
- [x] 4.4 Validate the OpenSpec change and review all changes for scope and contract consistency
