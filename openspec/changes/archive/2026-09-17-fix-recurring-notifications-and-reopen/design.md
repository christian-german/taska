## Context

The notification scheduler currently loads persisted `Task` rows whose `scheduledAt` is before its look-ahead cutoff and sets the row-level `isNotified` flag after dispatch. A recurring `Task` row is a series definition, so that flag suppresses every later occurrence after the first dispatch. Virtual recurring occurrences have no `TaskInstance`, and materializing one solely for notification would incorrectly change its business representation.

Completing an occurrence reuses an existing `TaskInstance` and changes its status to `DONE`, preserving any override columns. Reopening currently deletes the row unconditionally and therefore loses those overrides; a missing occurrence identity also falls through to mutation of the recurring series definition.

## Goals / Non-Goals

**Goals:**

- Treat every upcoming recurring occurrence as an independent notification target.
- Dispatch at most one notification batch for each stable occurrence identity.
- Keep notification delivery state separate from occurrence business state.
- Preserve occurrence overrides across completion and reopening.
- Make recurring close/reopen validation symmetric.

**Non-Goals:**

- Backfill notifications for recurring occurrences already in the past when this change is deployed.
- Guarantee Firebase delivery or add retries beyond the scheduler's existing dispatch semantics.
- Rename or extract the task and occurrence services.
- Change REST or MCP endpoint shapes.

## Decisions

### Store recurring notification markers separately

Add a notification-owned persistence record keyed uniquely by `(taskId, occurrenceScheduledAt)`. The scheduler claims that identity before dispatching to device tokens. The marker is technical delivery state and does not affect `TaskInstance`, `instanceId`, `isVirtual`, completion, or occurrence overrides.

Alternative considered: add `isNotified` to `TaskInstance`. This would require materializing virtual occurrences only because a notification was sent and would make transport-visible virtual state depend on a technical side effect.

### Build a common notification candidate model

The scheduler will consume notification candidates containing stable identity, effective content, description, and effective scheduled time. Non-recurring tasks and recurring occurrences will pass through the same dispatch path after candidate discovery. Recurring candidates will use the existing recurrence expansion and occurrence-resolution rules, including moved occurrences, while excluding completed and skipped occurrences and all-day series.

Only occurrences whose effective scheduled time falls within the scheduler run's upcoming look-ahead window are candidates. This avoids a notification storm for historical occurrences when the marker table is first introduced.

### Reset an occurrence marker when its effective schedule changes

Replacing an occurrence with a different effective `scheduledAt` will remove its existing notification marker, matching the existing non-recurring behavior that resets `isNotified` when `scheduledAt` changes. Reopening an occurrence will not reset notification state, matching non-recurring reopening behavior.

### Preserve sparse overrides when reopening

Closing a modified occurrence will retain its override columns while setting status `DONE` and a completion timestamp. Reopening such an occurrence will restore status `MODIFIED`, clear `completedAt`, and preserve the overrides. Reopening a completed occurrence without overrides will delete its `TaskInstance`, restoring a virtual occurrence.

Recurring close and reopen operations will require and validate `occurrenceScheduledAt`. Reopening an occurrence that is not currently completed will be rejected without mutation.

## Risks / Trade-offs

- **Concurrent scheduler executions could select the same occurrence** → enforce a database uniqueness constraint and claim the marker before dispatch.
- **Persisting the marker before asynchronous Firebase completion can suppress a retry after delivery failure** → retain the scheduler's existing at-most-once dispatch semantics; delivery retries remain out of scope.
- **A scheduler outage can miss an occurrence outside a later look-ahead window** → keep this change focused on normal upcoming scheduling and specify no historical backfill.
- **Occurrence discovery can duplicate date-range expansion logic** → reuse the recurrence service and effective occurrence result rather than reimplementing override resolution.

## Migration Plan

1. Add the recurring-occurrence notification marker table and unique constraint.
2. Deploy the scheduler changes; the empty table means only future upcoming occurrences are considered.
3. Deploy close/reopen changes without data migration because existing modified `DONE` rows already retain their override columns.

Rollback removes the scheduler use of marker records. The marker table can remain harmlessly or be removed in a later migration; no task or occurrence business data depends on it.

## Open Questions

None.
