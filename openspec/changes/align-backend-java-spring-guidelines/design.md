## Context

The backend is organized by feature at its top level, but most feature packages still mix controllers, HTTP DTOs, services, repositories, entities, and integration types. Several services accept controller request records or return controller response records, and REST and MCP adapters sometimes duplicate validation or use different mutation paths. Labels already contain a partial local conversion that establishes operation-specific request and application-parameter records, but its entity and exception handler are not yet in the packages prescribed by the target conventions.

Task, project, planning-calendar, and client contracts are manually maintained across Java, OpenAPI, Angular, Android, and MCP. Existing OpenSpec task requirements already mandate complete REST task replacement, while label, project, and planning-calendar PUT behavior remains partial or ambiguous.

## Goals / Non-Goals

**Goals:**

- Make each HTTP creation and replacement shape explicit, strict, and operation-specific.
- Keep HTTP DTOs out of application services and repositories.
- Give each feature controller, service, and repository subpackages while retaining entities, value objects, and domain exceptions at the feature root.
- Reuse one authoritative application mutation behavior from REST, MCP, schedulers, and other internal callers.
- Preserve server-controlled state and all behavior not explicitly changed by the delta specifications.
- Keep OpenAPI and first-party clients synchronized with changed REST payloads.

**Non-Goals:**

- Changing persistence schemas or moving to a multi-module/domain-framework architecture.
- Inventing new resource operations, authorization rules, optimistic locking, or user-visible workflows.
- Changing the already approved complete task replacement field set or recurring-occurrence semantics.
- Renaming API-facing properties or paths beyond removing the project-only `clearParent` workaround.

## Decisions

### Use feature-first technical subpackages

Each maintained feature owns `controller`, `service`, and `repository` subpackages when it has those responsibilities. Entities, domain value objects, and domain exceptions remain directly in the feature package. API DTOs, request types, controller advice, and boundary mappers live under `controller`; application services and input/result records live under `service`; Spring Data interfaces live under `repository`.

Alternative considered: retain flat feature packages and enforce only dependency direction. Rejected because package location would continue to hide responsibility and permit accidental controller-to-service leakage.

### Map every transport into application parameters

REST boundary mappers convert create/update/action requests into operation-specific application records. MCP tools construct the same application records without importing controller types. Services return entities or immutable application results; controllers and MCP adapters map those results into their own output representations.

Alternative considered: move current request records into the service package and reuse them everywhere. Rejected because HTTP validation/presence annotations and transport evolution would remain coupled to business use cases.

### Use complete PUT replacement and dedicated action semantics

Label, project, and planning-calendar PUT payloads contain every mutable property. Comment PUT contains only its one mutable property, `content`. Nullable replacement properties must still be present; `null` is applied explicitly. Project `parentId: null` removes the parent, replacing `clearParent`. Creation payloads retain their documented defaults and omit operation-only or server-controlled properties. Existing task close, reopen, occurrence, reorder, and similar action endpoints remain dedicated operations.

Alternative considered: keep partial PUT semantics and merely split Java record names. Rejected because omitted and explicit-null values would remain ambiguous and contradict the requested mutation rules.

### Enforce strict JSON at the application boundary

Writable request records reject unknown properties through the application's Jackson configuration or explicit request handling, and tests verify the behavior. Required nullable PUT properties use presence-aware deserialization so omission is rejected while explicit `null` remains valid.

Alternative considered: use per-record `@JsonAnySetter` methods. Rejected as the primary mechanism because repeating rejection code in every request type is noisy; it remains acceptable only if framework configuration cannot provide the required behavior.

### Preserve partial MCP tools behind complete application behavior

Where the published MCP input is partial, the adapter may request the current resource state and resolve a complete target application parameter record before invoking the same replacement behavior used by REST. Business rules and side effects remain centralized even though transport ergonomics differ.

Alternative considered: make MCP inputs breaking complete replacements. Rejected because no approved MCP behavior requires that public break.

## Risks / Trade-offs

- [Large package movement causes missed imports or reflection-sensitive references] → Compile after each feature group and update reflective/API naming tests alongside moves.
- [Full replacement breaks stale first-party callers] → Update OpenAPI, Angular, and Android request construction in the same change and run contract/build tests.
- [Explicit nullable values collapse into omission during deserialization] → Add controller tests for every required nullable property with both omitted and explicit-null payloads.
- [MCP and REST drift despite shared parameter records] → Add tests asserting that overlapping operations invoke the same application service inputs and preserve server-controlled state.
- [Concurrent dirty work is overwritten] → Build on the existing label edits, use narrow patches, and review staged and unstaged diffs without resetting.

## Migration Plan

Deploy backend and first-party client updates together. No database migration is required. External REST clients must replace legacy shared request schemas with the new create/update schemas and send complete PUT payloads; project clients remove `clearParent` and send `parentId: null` to clear the relationship. Rollback is a coordinated code/client rollback because stored data is unchanged.

## Open Questions

None.
