## Context

`PUT /tasks/{id}` currently accepts a partial `TaskRequest` and applies only non-null fields, except for a raw-JSON presence check used to clear `priority`. Web clients intentionally send partial objects, while Android serializes null fields and reconstructs only part of the task in several flows. Consequently, a nullable property cannot consistently mean “replace with null”.

Recurring updates have two distinct domain operations. `FROM_THIS` splits a series into an ended original and a new following series. `THIS_ONLY` persists a `TaskInstance`, whose supported overrides are limited to title, priority, scheduled time, and deadline.

## Goals / Non-Goals

**Goals:**

- Make ordinary task and following-series update requests complete replacements with a typed `TaskUpdateRequest`.
- Allow explicit `null` replacement values for priority, schedule, deadline, and other nullable mutable fields.
- Give single-occurrence updates an endpoint and DTO that accurately model the properties a `TaskInstance` can override.
- Make Android and web construct requests that conform to each endpoint's contract.

**Non-Goals:**

- Add per-occurrence overrides for properties not stored by `TaskInstance`.
- Change task creation, completion, deletion, notification, or recurrence-generation behavior.
- Preserve compatibility for partial clients of the existing base-task PUT endpoint.

## Decisions

### Use `TaskUpdateRequest` for full base-task replacement

`TaskUpdateRequest` SHALL contain every mutable base-task property and no output-only representation fields. The backend SHALL replace all of those properties rather than ignoring null values. This removes the need for `JsonNode` field-presence inspection.

Alternative considered: retain partial PUT and track field presence for every nullable property. Rejected because it keeps PATCH semantics behind a PUT method and makes the contract increasingly complex.

### Separate base, following-series, and single-occurrence endpoints

`PUT /tasks/{id}` replaces a base task. A distinct following-series endpoint accepts the same `TaskUpdateRequest` plus occurrence identity in the path, ends the old series before that occurrence, and creates a new series from the full request. A distinct single-occurrence endpoint accepts `OccurrenceUpdateRequest`, exposing only title, priority, scheduledAt, and dueAt.

Alternative considered: retain `scope` and `occurrenceScheduledAt` inside `TaskUpdateRequest`. Rejected because a full base-task DTO falsely implies that all properties can be replaced on a `TaskInstance`; ignoring unsupported properties would silently discard user changes.

### Represent a single occurrence by its supported override resource

The occurrence endpoint SHALL reject base-task properties because its persistence model cannot override them independently. Its response SHALL continue to identify the targeted occurrence through `occurrenceScheduledAt`.

Alternative considered: mutate the parent task after creating a `TaskInstance`. Rejected because every subsequent generated occurrence would inherit the changed parent properties.

## Risks / Trade-offs

- [Existing partial clients receive validation failures] → Update all first-party Android and web callers and add contract tests before deployment.
- [A full request overwrites stale client state] → Clients start from the task representation currently being edited and adopt the server response after each successful update.
- [Following-series split loses an intended field] → Construct the new series exclusively from `TaskUpdateRequest` and test every mutable property.
- [Occurrence callers attempt unsupported edits] → Expose a narrow DTO and return a validation error for unknown or invalid input.

## Migration Plan

1. Add the typed replacement and occurrence DTOs, endpoints, validation, and backend tests.
2. Migrate Android and web update flows to send their full mutable task representation or the dedicated occurrence request.
3. Remove the partial-update implementation and `JsonNode` handling after all first-party clients use the new contract.

Rollback restores the former partial endpoint behavior and client calls as one coordinated release; no stored-data migration is required.

## Open Questions

- None.
