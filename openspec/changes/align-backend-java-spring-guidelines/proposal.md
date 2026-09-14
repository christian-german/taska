## Why

Taska's backend still mixes HTTP contracts, application services, and persistence concerns, and several resources reuse partial request shapes for both creation and replacement. Aligning these boundaries now makes mutation semantics explicit, keeps REST, MCP, OpenAPI, and first-party clients consistent, and prevents omitted values from silently preserving stale state.

## What Changes

- **BREAKING** Split label, comment, project, and planning-calendar creation and replacement payloads into operation-specific contracts.
- **BREAKING** Make label, project, and planning-calendar `PUT` operations complete replacements of their documented mutable fields; omitted replacement properties are rejected, explicit nullable values are applied, and empty collections clear the corresponding state.
- **BREAKING** Remove the project replacement-only `clearParent` workaround; `parentId: null` explicitly removes the parent relationship.
- Reject unknown JSON properties on writable REST contracts and keep server-controlled state absent from those contracts.
- Reorganize backend features into feature-owned controller, service, and repository packages while keeping entities, value objects, and domain exceptions at the feature root.
- Map REST and MCP inputs to application parameter types at their boundaries; application services no longer depend on controller request/response types.
- Update OpenAPI, Angular callers, Android callers where applicable, and automated contract/application tests to match the resulting contracts.

## Capabilities

### New Capabilities

- `backend-resource-contracts`: Defines operation-specific creation and full-replacement contracts for maintained backend resources and their first-party clients.
- `backend-application-boundaries`: Defines feature package ownership and transport-independent application-service boundaries shared by REST, MCP, scheduled, and internal callers.

### Modified Capabilities

- `planning-calendar-management`: Clarifies complete planning-calendar and project-calendar replacement behavior.
- `taska-mcp-server`: Requires MCP mutations to map into the same application inputs and business behavior as REST without depending on HTTP DTOs.

## Impact

Affected areas include Spring feature packages and tests, request/response mapping, application-service inputs, MCP adapters, the maintained OpenAPI schemas and paths, Angular label/project/comment/planning-calendar services and callers, and Android API models only where a changed resource contract is consumed. Persistence schemas and stored data do not change.
