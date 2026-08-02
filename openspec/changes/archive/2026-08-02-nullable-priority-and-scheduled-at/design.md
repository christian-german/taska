## Context

Taska currently persists task and recurring-task-instance `priority` values with a default of `4`; creation also replaces an absent request value with `4`. It exposes the planning timestamp as `dueAt`/`due_at` in the domain, database, REST DTOs and controllers, MCP task inputs and outputs, and both client applications. Recurring occurrence overrides, task filters, notification scheduling, and date-based presentation also use that timestamp.

The change intentionally reserves `due_date` for a later, distinct deadline feature. The priority-evaluation resource is a separately persisted calculated score and must remain independent of the nullable manual task priority.

## Goals / Non-Goals

**Goals:**

- Persist and expose an absent manual task priority without silently converting it to priority `4`.
- Rename the existing planned-time concept consistently to `scheduled_at`/`scheduledAt`, preserving all existing timestamp values and scheduling behavior.
- Keep REST, MCP, recurring-task handling, and both clients on one task-field vocabulary.
- Preserve the priority-evaluation model and its score semantics.

**Non-Goals:**

- Adding a `due_date` field or deadline-specific behavior.
- Changing the valid assigned priority range, priority-evaluation criteria, or evaluation eligibility.
- Providing a backward-compatible `due_at` alias.

## Decisions

### Treat manual priority as an optional value, not a defaulted normal priority

Remove the persistence and creation-service default of `4` and permit `NULL` for manual priority in task records. Continue validating supplied priority values as integers in the existing supported range. Update write-model semantics so clients can represent an unassigned priority as `null`, including clearing a previously assigned value where the update contract supports that field.

This prevents an omitted or cleared value from being misrepresented as normal priority. Keeping the database default would make it impossible to distinguish the two states.

Alternative considered: retain `4` as a sentinel for “no priority.” This is rejected because `4` is already a valid explicit priority and would preserve the ambiguity this change removes.

### Rename the existing planning timestamp end-to-end

Rename the domain property, database columns, repository queries, recurrence-instance override fields, DTO accessors, REST JSON properties, MCP tool schemas, and client models from `dueAt`/`due_at` to `scheduledAt`/`scheduled_at`. The pre-existing recurrence-occurrence selector currently called `scheduledAt` has a distinct meaning, so it becomes `occurrenceScheduledAt` throughout public and internal contracts. Date filters and notification behavior retain their current meaning, but use the renamed terminology. `due_at` is not accepted or emitted after the change.

The database migration will rename columns in place so existing values, nullability, indexes, and relationships are retained. This is a breaking contract change, deliberately avoiding two names for the same concept before `due_date` exists.

Alternative considered: accept both names temporarily. This is rejected because it would make the canonical API ambiguous and add deprecation work with no requested compatibility window.

### Keep priority evaluations independent

Calculated priority evaluations continue to be stored and retrieved separately. An absent manual `priority` is neither a score of zero nor an absence of the evaluation resource, and it does not alter the existing evaluation criteria.

## Risks / Trade-offs

- [Existing REST, MCP, web, and Android callers still use `due_at`/`dueAt`] → Update all in-repository clients and contract tests together; document the breaking rename.
- [A `NULL` priority is accidentally re-defaulted in a mapper, recurrence clone, or instance override] → Remove defaulting at every write boundary and test create, update/clear, recurrence, and DTO round trips.
- [Renaming only the task table leaves recurring occurrences or date queries inconsistent] → Include task instances, repositories, recurrence, filtering, and notifications in the same mechanical rename and migration review.
- [Manual-priority nullability is confused with the calculated evaluation] → Preserve the evaluation API and add explicit contract coverage for their independent absences.

## Migration Plan

1. Add a Flyway migration that drops the task-priority default, permits nullable priority where necessary, and renames persisted `due_at` columns for tasks and task instances to `scheduled_at` while preserving data and dependent indexes.
2. Deploy the backend and API/MCP contract changes with coordinated web and Android client updates; clients must use `scheduled_at`/`scheduledAt` exclusively.
3. Verify migrated timestamps, tasks with no priority, recurring occurrences, date filters, and notification queries through automated migration and integration tests.
4. Roll back application code only with a compatible database migration that restores the prior column names and default if rollback is required; do not roll back after clients have been released against the renamed contract without a compatibility release.

## Open Questions

- None. The planned timestamp remains semantically identical in this change; only its name changes.
