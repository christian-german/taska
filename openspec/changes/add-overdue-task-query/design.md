## Context

Android widgets construct overdue work by requesting a generic list ending yesterday. A request with only `to` does not trigger recurrence expansion. The Angular Today view loads only today and tomorrow, then searches locally for overdue work in that incomplete result.

The occurrence service already expands a range, applies `SKIPPED` and `COMPLETED` state, and resolves the effective date of a rescheduled occurrence. The backend uses the zone configured by `taska.calendar.time-zone` to convert calendar dates into instants.

## Goals / Non-Goals

**Goals:**

- Give clients that display overdue work a single exhaustive API source.
- Include non-recurring tasks and open recurring occurrences, accounting for rescheduling.
- Preserve stable occurrence identity and existing representations.
- Make Android widgets and the web Today view consume the authoritative source.

**Non-Goals:**

- Paginate, age-limit, or archive overdue work.
- Change completion, rescheduling, or recurrence rules.
- Change today or tomorrow task selection.

## Decisions

### Exposer une ressource de vue nommée

The backend will expose `GET /tasks/overdue` rather than an open-ended `to` filter. The intent is explicit in the contract and avoids imposing an arbitrary historical boundary on clients. The resource will return non-recurring tasks and open recurring occurrences whose effective date is before the start of the current date in `taska.calendar.time-zone`, in ascending chronological order.

The alternative of accepting `to` without `from` retains an ambiguous API: a definition list and an occurrence list would share the same path, and incomplete parameters would produce a difficult-to-predict result.

### Réutiliser l'expansion des occurrences

The occurrence service will introduce a dedicated read operation that computes the historical range required per series, then reuses existing state and rescheduling resolution. Recurring results will always be occurrences, never series definitions. Completed tasks or occurrences and skipped occurrences will be excluded.

Client-side expansion or one request per day is rejected: both duplicate RRULE rules, fail for rescheduled occurrences, and create network cost proportional to history.

### Préserver l'appartenance au groupe overdue dans les clients

Android and Angular will keep `/tasks/overdue` results separate from their current-date lists until building groups. They will use this authoritative membership for overdue-group order and styling instead of recalculating it with the client clock or zone. Entries will be deduplicated by the existing `(taskId, occurrenceScheduledAt)` identity when combined.

This separation prevents a device configured with a different zone from contradicting the backend's decision.

## Risks / Trade-offs

- [An old daily series produces many occurrences] → The contract intentionally requires complete history without pagination; tests will cover a series and volume limits will be reconsidered before introducing pagination.
- [Current and overdue lists overlap around a day transition] → Clients will deduplicate using the task or occurrence identity already used by widgets.
- [Additional calls can leave a view partially loaded] → Each client will update its view only after receiving all required data.
