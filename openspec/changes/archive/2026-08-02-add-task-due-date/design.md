## Context

The recently introduced `scheduled_at` field is the task's calendar-placement time: it drives calendar display, date filtering, recurrence scheduling, and notifications. A deadline has a distinct meaning and must not reuse that field. Task data is persisted by the backend and represented through REST, MCP, web, and Android contracts; recurring occurrences can also carry field overrides through `task_instances`.

## Goals / Non-Goals

**Goals:**

- Introduce a nullable `due_at`/`dueAt` deadline that is independent of `scheduled_at`/`scheduledAt`.
- Preserve the deadline through task creation, updates, retrieval, and client/MCP representations.
- Support recurrence occurrence representations and scoped occurrence updates consistently with other mutable task fields.
- Preserve all existing scheduled-time behavior.

**Non-Goals:**

- Change task ordering, calendar display, date filters, reminders, or notifications based on the deadline.
- Modify priority calculation or establish a deadline-derived priority policy.
- Backfill deadlines from existing scheduled timestamps.

## Decisions

### Use a nullable UTC instant called `due_at`/`dueAt`

Persist the field as nullable `due_at` and expose it as nullable `dueAt` in Java and client contracts. This matches the established timestamp convention while making the deadline a distinct, explicit property. Reusing `scheduled_at` would conflate calendar placement with completion expectation; representing a date-only value would discard the existing API's Instant precision and introduce a new timezone policy.

### Keep scheduled-time behavior isolated

Only `scheduledAt` determines calendar display, schedule-based queries, recurrence generation, all-day semantics, and notification timing. `dueAt` is stored and displayed as deadline information but has no effect on those workflows. This prevents adding a deadline from unexpectedly moving a task or causing notifications.

### Model recurring occurrence deadlines as overrides

Add a nullable due-date override to persisted recurring-task instances. Virtual occurrences inherit the parent task's `dueAt`; a `THIS_ONLY` update that provides `dueAt` stores an instance override, and the occurrence representation returns that override when present. This follows the existing model for mutable occurrence fields and avoids altering the entire recurring series for an occurrence-specific deadline. A `FROM_THIS` change carries the supplied deadline into the new series.

### Add rather than rename data

Use a forward-only Flyway migration to add nullable deadline columns without changing `scheduled_at` or copying existing values. Existing tasks and instances therefore have no deadline until one is explicitly assigned. A rollback requires a compensating migration or leaving the nullable columns unused; production migrations are not rolled back in place.

## Risks / Trade-offs

- [Clients treat due dates as scheduled times] → Use separate labels and contract tests showing both timestamps can differ.
- [Recurring virtual and modified occurrences diverge unexpectedly] → Test inherited and override precedence, including scoped updates.
- [A later priority algorithm needs a date-only interpretation] → Retain the precise instant now; define any priority policy separately when that algorithm is introduced.
