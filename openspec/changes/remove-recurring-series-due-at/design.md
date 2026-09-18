## Context

`tasks.due_at` currently serves both non-recurring task deadlines and recurring-series definitions. Occurrence rendering falls back from `TaskOccurrenceState.dueAt` to the series value, so one absolute instant is repeated across every occurrence. The HTTP and client task models also place `dueAt` in their common base shape, which incorrectly implies that every task variant supports it.

The change crosses persistence, application services, HTTP/OpenAPI, MCP, Angular, and Android. It must preserve valid non-recurring deadlines and occurrence-specific overrides while eliminating legacy series values.

## Goals / Non-Goals

**Goals:**
- Make `due_at IS NULL` an invariant for every recurring-series row.
- Keep absolute deadlines for non-recurring tasks and explicit occurrence-state overrides.
- Remove `dueAt` from the recurring-series HTTP and maintained-client representations.
- Reject contradictory recurring-series writes instead of silently discarding an explicit deadline.
- Migrate existing data safely and protect the invariant at both service and database boundaries.

**Non-Goals:**
- Introduce relative recurring deadlines such as `dueAfter`.
- Change recurrence scheduling, notification timing, or occurrence identity.
- Remove the shared `tasks.due_at` column, because non-recurring tasks still use it.
- Remove `dueAt` from the flat MCP output schema; MCP series outputs remain compatible with a null value.

## Decisions

### Persist null for recurring-series rows

A Flyway migration clears existing `tasks.due_at` values where `is_recurring = true`, then adds a check constraint requiring recurring rows to keep `due_at` null. Keeping the shared column avoids splitting task persistence while the database constraint prevents future bypasses of service validation.

### Reject explicit series deadlines

Task creation and complete replacement reject a non-null `dueAt` when the resulting task is recurring. Following-series replacement has the same rule. A partial conversion from non-recurring to recurring clears any previously stored deadline when no new deadline is supplied; an explicitly supplied non-null deadline is rejected.

This is preferred to silently ignoring input because clients receive immediate feedback when they express an invalid domain state.

### Resolve occurrence deadlines only from occurrence state

`RecurringTaskOccurrenceResult.resolvedDueAt()` returns the persisted occurrence override or null. Virtual occurrences and materialized occurrences without a due-date override therefore have no deadline. Other occurrence overrides continue to fall back to series values where those values remain meaningful.

### Make the HTTP representation variant-specific

`RecurringTaskSeriesDto` and its OpenAPI schema no longer contain `dueAt`. The common HTTP task interface and OpenAPI base schema stop declaring the property; `NonRecurringTaskDto` and `RecurringTaskOccurrenceDto` declare it themselves. Angular and Android mirror this discriminated shape.

Android may retain a non-serialized convenience getter returning null on its sealed interface so shared UI code can read a deadline safely, but the concrete recurring-series data class does not deserialize or serialize a `dueAt` property.

### Preserve flat MCP compatibility

The MCP output is intentionally flat rather than discriminated. Removing one field only for series would require a separate MCP output union, which is outside this change. MCP series outputs therefore retain the field with a permanent null value, while occurrence outputs expose only explicit occurrence overrides.

## Risks / Trade-offs

- [Breaking HTTP response shape for recurring series] → Update OpenAPI, Angular, Android, and representation contract tests in the same change.
- [Legacy clients send a series deadline] → Return a clear 400 validation response rather than persisting or ignoring it.
- [Existing recurring rows violate the new constraint] → Clear legacy values before adding the constraint in the same migration.
- [Generic client code assumes every task has `dueAt`] → Narrow by discriminator or use a convenience getter that resolves series deadlines to null without adding the field to its wire model.

## Migration Plan

1. Deploy the migration that nulls existing recurring-series deadlines and adds the database constraint.
2. Deploy backend validation and variant-specific response mapping with the same release.
3. Update generated/manual client contracts and UI behavior so recurring-series detail does not offer a deadline.
4. Rollback requires dropping the check constraint; cleared legacy series deadlines are intentionally not restored because they had no valid semantic meaning.

## Open Questions

None. Relative recurring deadlines require a separate future product decision and contract.
