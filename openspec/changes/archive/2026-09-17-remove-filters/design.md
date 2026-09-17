## Context

Taska currently has two related filter surfaces: persisted saved filters represented by a dedicated database table, Spring domain, REST endpoints, and Angular screens; and a named `filter` input on task listing and MCP tools for `today`, `overdue`, and `upcoming`. Explicit project, label, completion, single-date, and date-range task queries also narrow results but are general task-query capabilities rather than the retired filter feature.

The working tree already contains the completed local `remove-sections-and-time-tracking` change. This change must compose with those edits and must not rewrite published Flyway migrations or archived OpenSpec history.

## Goals / Non-Goals

**Goals:**

- Remove saved-filter persistence, server code, REST/OpenAPI contracts, and first-party UI.
- Remove the named `filter` parameter and its date-shortcut implementation from REST, MCP, and clients.
- Remove all maintained feature documentation and tests while preserving coverage for remaining task queries.
- Make existing saved-filter data disappear on upgrade through a forward migration.

**Non-Goals:**

- Removing project, label, completion, explicit single-date, or date-range task query parameters.
- Removing task search, collection predicates used internally by clients, servlet/security filter chains, CSS effects, or generic documentation headings using the word “filter.”
- Changing scheduling, recurrence, deadlines, calendars, notifications, or estimates.
- Rewriting immutable historical migrations, archived OpenSpec changes, or dated historical evidence.

## Decisions

1. Add a new forward migration after V21 that drops the `filters` table. Published migration files remain untouched so existing Flyway checksums stay valid.
2. Delete the entire Spring saved-filter package because no remaining domain behavior depends on it.
3. Remove the named task-list `filter` input end-to-end rather than retaining an undocumented server shortcut. Explicit date/range queries continue to provide date-oriented task lists.
4. Delete the Angular saved-filter service, list/detail screens, routes, startup loading, sidebar state/navigation, and model. Regenerated utility CSS may lose classes used only by those screens.
5. Android requires only API-signature cleanup if its task-list contract still exposes the named parameter; it has no saved-filter user experience to replace.
6. Remove filter API material from current OpenAPI and documentation. Canonical OpenSpec is synchronized after implementation; historical migration and archive content remains historical.

## Risks / Trade-offs

- [Existing saved filters are irreversibly deleted] → Make the destructive migration explicit and keep rollback dependent on a database backup.
- [Removing named shortcuts could affect date views] → Retain and test explicit single-date/date-range APIs used by maintained clients.
- [The broad term “filter” appears in unrelated programming and platform concepts] → Residual scans target saved-filter symbols, endpoints, schema names, routes, and named task parameters rather than generic language/library operations.
- [Concurrent uncommitted work exists] → Preserve unrelated edits and review only scoped diffs without resetting the worktree.
