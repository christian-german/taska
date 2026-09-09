# frontend-openapi-contract-testing Specification

## Purpose

Define isolated validation of all maintained Angular API requests against the authoritative OpenAPI contract and deterministic reporting of violations.

## Requirements

### Requirement: Isolated Prism frontend contract server
The frontend contract workflow SHALL run the official Stoplight Prism Docker image in mock mode against `docs/openapi/taska.openapi.yaml`, SHALL enable request error responses, SHALL expose a dedicated configurable host port, and SHALL not start or require Taska backend, database, or authentication services.

#### Scenario: Frontend validator starts
- **WHEN** the frontend contract entry point is executed with Docker available
- **THEN** it starts only Prism, waits until the mock is healthy, and serves the maintained OpenAPI document

#### Scenario: Prism receives an invalid request
- **WHEN** an outgoing request violates a declared body, path, query, header, required-property, property-type, or closed-object constraint
- **THEN** Prism returns a request validation error that fails the associated contract test

### Requirement: Complete outgoing Angular request coverage
The frontend contract suite SHALL instantiate the maintained Angular HTTP services with the real Angular HTTP client, SHALL direct them to Prism using test-only configuration, and SHALL execute every public service method that sends an API request without duplicating the existing frontend unit suites.

#### Scenario: Frontend contract suite runs
- **WHEN** the contract entry point invokes the centralized suite
- **THEN** every request emitted by every maintained Angular API service method is sent to Prism and validated against the OpenAPI specification

#### Scenario: Frontend application configuration remains unchanged
- **WHEN** frontend contract support is installed
- **THEN** no Prism dependency, Prism-specific source, contract-test npm script, or production API URL change is added to the Angular project

### Requirement: Cleanup-safe frontend contract entry point
`contract-tests/verify-frontend.sh` SHALL be the complete frontend contract-validation entry point, SHALL always stop its Prism stack after success, failure, or interruption, and SHALL return zero only when startup, all tests, and report generation succeed.

#### Scenario: All contract tests pass
- **WHEN** Prism starts, every frontend contract test passes, and the report renders successfully
- **THEN** the script removes Prism and exits with status zero

#### Scenario: Contract validation fails
- **WHEN** Prism rejects any frontend request or another test fails
- **THEN** the script renders the available diagnostics, removes Prism, and exits with a non-zero status

### Requirement: Deterministic agent-friendly report
Each frontend contract run SHALL replace `contract-tests/reports/frontend-contract-report.md` with a concise deterministic report containing the executed, passed, and failed test counts and the contract violation count. Every contract violation SHALL identify the affected endpoint, failing request, Prism validation message, and recommended fix.

#### Scenario: Successful report
- **WHEN** every frontend request satisfies the contract
- **THEN** the report contains stable zero-failure totals and states that no contract violation was reported

#### Scenario: Violation report
- **WHEN** Prism rejects one or more frontend requests
- **THEN** the report contains one stable section per failed request with its endpoint, request details, Prism message, and remediation guidance

### Requirement: Complementary contract architecture
The contract-test documentation SHALL explain how the Prism frontend request workflow complements the existing Schemathesis workflow that validates backend behavior against the same maintained OpenAPI source.

#### Scenario: Developer reads contract-test documentation
- **WHEN** a developer opens `contract-tests/README.md`
- **THEN** they can identify the frontend command, prerequisites, Prism behavior, report location, isolation from the backend, and relationship to backend contract testing
