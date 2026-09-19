## Context

Taska represents a recurring series as one persisted `Task` definition whose `scheduledAt` is the anchor supplied to recurrence expansion. `TaskDefinitionRules` already centralizes two related invariants: a recurring definition must have a recurrence rule and cannot carry a series-level `dueAt`. It does not currently require the anchor, so REST or MCP can persist a row that claims to recur but can never generate occurrences.

The database independently enforces the existing recurrence-rule and deadline invariants. Existing installations may already contain recurring rows without `scheduled_at`, so adding the corresponding constraint requires deterministic data repair.

## Goals / Non-Goals

**Goals:**

- Require a non-null `scheduledAt` for every resulting recurring-series definition.
- Apply the rule through one authoritative domain rule reused by every service mutation path.
- Reject invalid mutations before writing or restructuring persisted series state.
- Enforce the invariant independently in PostgreSQL.
- Repair existing invalid rows without inventing a schedule.

**Non-Goals:**

- Require schedules for non-recurring tasks.
- Require occurrence-level schedule overrides; virtual occurrences continue to derive their schedule from the series.
- Infer a schedule from creation time, update time, deadlines, or recurrence state.
- Change REST or MCP field names or add a new endpoint.

## Decisions

### Validate the resulting definition in `TaskDefinitionRules`

Add an `assertScheduledAtRequired(recurring, scheduledAt)` rule beside the existing recurrence-definition invariants. Creation and complete replacement validate their supplied values directly. Partial updates validate the effective result obtained by combining supplied values with the stored definition, so converting an unscheduled task to recurring is rejected while unrelated updates to a valid series remain allowed.

The following-series replacement path validates the supplied schedule before truncating the original series or detaching occurrence state. Its later shared complete-replacement mapping retains the same check as defense in depth. Partial series splitting already falls back to the required occurrence cut identity when no replacement schedule is supplied, yielding a non-null successor anchor.

Conditional controller-only validation was considered and rejected as the authoritative mechanism because MCP and internal callers must receive the same guarantee. The OpenAPI schemas will nevertheless describe and validate the same conditional contract.

### Add a database check after repairing invalid rows

A forward Flyway migration converts each `is_recurring = TRUE AND scheduled_at IS NULL` row into a non-recurring task and clears `recurrence_rule` and `rrule_ends_at`. No reliable schedule can be inferred, and preserving the task as a one-off item avoids deleting user content.

After repair, PostgreSQL adds `CHECK (is_recurring IS NOT TRUE OR scheduled_at IS NOT NULL)`. This protects persistence paths that bypass application services and matches the existing constraints for recurring deadlines and recurrence rules.

### Preserve optional schedules outside series definitions

`scheduledAt` remains nullable for non-recurring tasks, and occurrence-state `scheduledAt` remains an optional override. Clearing the schedule through a complete replacement remains valid only when the resulting base task is non-recurring.

## Risks / Trade-offs

- [Existing invalid series lose recurrence metadata] → Preserve the task itself as a non-recurring item; do not invent a date or silently delete it.
- [Following-series replacement could mutate the original before discovering an invalid successor] → Validate the successor schedule at method entry before truncation or detachment.
- [REST and service validation could drift] → Keep the service rule authoritative and cover both the OpenAPI conditional schema and service mutation paths with tests.

## Migration Plan

1. Add service-level validation and unit tests for creation, partial mutation, complete replacement, and following-series replacement.
2. Update the OpenAPI conditional schemas for recurring creation and replacement.
3. Add the forward Flyway data repair and check constraint with persistence coverage.
4. Format and run backend, contract, OpenSpec, and diff verification.

Rollback requires removing the new check constraint before deploying code that permits invalid rows. Rows converted to non-recurring cannot have their missing schedule reconstructed automatically.

## Open Questions

None.
