## Context

`Task` currently represents either a persisted non-recurring task or a persisted recurring-series definition. `TaskInstance` optionally materializes one occurrence, while unmodified occurrences remain virtual. The HTTP adapter flattens all three concepts into `TaskDto`; nullable recurrence fields then act as an undocumented discriminator. The same DTO is also reused by the MCP adapter and both maintained clients.

This is a breaking HTTP-contract change spanning the backend, OpenAPI document, Angular client, and Android client. Persistence and recurrence expansion remain unchanged.

## Goals / Non-Goals

**Goals:**

- Make every HTTP task representation self-describing through a required `kind` discriminator.
- Encode non-recurring tasks, recurring-series definitions, and recurring occurrences as distinct DTO types.
- Make the application result variants exhaustive so mapping cannot silently confuse a series definition with a non-recurring task.
- Preserve effective occurrence values, including moved occurrences and virtual/materialized state.
- Keep Angular and Android behavior working against the new contract.
- Keep MCP task output stable and owned by the MCP adapter.

**Non-Goals:**

- Changing the database schema, recurrence expansion, series splitting, or occurrence mutation semantics.
- Introducing separate persistence entities for non-recurring tasks and recurring series.
- Fixing completion/reopen defects that are independent of response representation.
- Changing endpoint paths or splitting the existing task-list endpoint.

## Decisions

### Use a three-variant discriminated response union

The HTTP adapter will expose a sealed `TaskDto` contract with `NonRecurringTaskDto`, `RecurringTaskSeriesDto`, and `RecurringTaskOccurrenceDto`. Their discriminator values will be `NON_RECURRING`, `RECURRING_SERIES`, and `RECURRING_OCCURRENCE`.

All variants retain the task ID as `id`. For an occurrence this is the backing series ID; the occurrence's stable identity remains the composite `(id, occurrenceScheduledAt)`. This preserves mutation URLs and avoids inventing an identifier for virtual occurrences.

Occurrence-only fields are absent from non-occurrence JSON. `occurrenceScheduledAt` and `isVirtual` are required on every occurrence. `instanceId` remains nullable because a virtual occurrence has no persisted instance. Recurrence rule metadata remains on series and occurrence representations because clients display recurrence and use it when replacing the following series. Completion fields are absent from a recurring-series definition because a definition cannot itself be completed.

The redundant `isRecurring` property is removed from responses. Clients branch on `kind` instead.

Alternative considered: retain one DTO and add only `kind`. That would identify the cases but would continue admitting meaningless nullable-field combinations, so it does not meet the goal.

### Make `TaskResult` exhaustive across all three concepts

`TaskResult` will become a sealed application result with three variants. A base-result factory will inspect the persisted task's recurrence flag and create either a non-recurring result or a recurring-series result; an occurrence factory will require its occurrence identity. `TaskMapper` will use an exhaustive switch to select the HTTP DTO mapping.

Alternative considered: model only non-recurring versus occurrence results. That is incorrect for mutation methods such as following-series replacement, which return a recurring-series definition as a base result.

### Keep the existing endpoint routing but constrain its variants

Undated task lists, task retrieval, creation, base replacement, and subtask lists may return non-recurring tasks or recurring-series definitions. Date and date-range queries may return non-recurring tasks or recurring occurrences, never a series definition. Single-occurrence mutation endpoints return an occurrence representation.

The OpenAPI `TaskDto` schema remains the reusable union, while endpoint descriptions and tests enforce the narrower allowed sets where applicable.

### Use native discriminated unions in both clients

Angular will model `Task` as a TypeScript discriminated union and use type guards for recurrence-specific behavior. Android will model `TaskDto` as a sealed interface with three data-class implementations and register a Gson deserializer keyed by `kind`. Shared client-side accessors may provide harmless derived values for UI code, but the wire classes will match the variant-specific JSON fields.

### Give MCP its own output type

MCP will not expose the HTTP sealed DTO union. Its adapter will map application results to an MCP-owned flat output record, preserving current MCP schema and behavior. This also restores the transport-boundary ownership expected by the project architecture.

## Risks / Trade-offs

- **Breaking response contract** → Update OpenAPI, backend, Angular, and Android atomically and cover each discriminator variant with contract/model tests.
- **Android polymorphic deserialization** → Register one discriminator-based Gson adapter and reuse the configured Gson instance for Retrofit and widget persistence.
- **More DTO mapping code** → Keep shared fields in the sealed contract and centralize all HTTP conversion in `TaskMapper`.
- **Variant-specific fields require client narrowing** → Provide explicit type guards/derived accessors rather than reintroducing nullable wire fields.
- **MCP and HTTP outputs may evolve independently** → Keep derivation in their respective adapter mappers and test both mappings.

## Migration Plan

1. Add the OpenAPI union and backend result/DTO variants.
2. Update backend endpoints, mapper tests, and contract tests.
3. Update Angular models and recurrence/completion branches.
4. Update Android sealed models, Gson configuration, and client tests.
5. Run backend, frontend, Android, OpenSpec, and contract validation before deployment.

Rollback requires reverting backend and both clients together because the response change is intentionally breaking.

## Open Questions

None. The discriminator names and endpoint-specific variants are defined by this change.
