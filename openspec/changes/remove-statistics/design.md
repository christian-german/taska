## Context

Taska exposes aggregate task statistics through `GET /stats/overview`, a Spring controller/service/DTO, and an Angular statistics page. The web entry points include a route, sidebar action, command-palette item, and keyboard shortcut. Android does not consume the endpoint, but its project screen independently presents active and overdue counts as a statistics summary. Current OpenAPI and backend audit documents describe the retired endpoint and implementation.

The working tree already contains completed local changes removing filters, sections, and time tracking. This removal must compose with those edits without resetting or rewriting them. Statistics has no entity or database table, so no schema migration is needed.

## Goals / Non-Goals

**Goals:**

- Remove aggregate statistics behavior from backend and first-party clients.
- Ensure former statistics routes and navigation entry points no longer resolve to feature behavior.
- Remove statistics-only code and maintained API/audit documentation.
- Preserve unrelated edits already present in shared files.

**Non-Goals:**

- Removing task completion state, estimates, scheduled dates, overdue presentation, or project task lists.
- Removing ordinary per-view counts that are integral to non-statistics workflows, except the Android project statistics summary explicitly in scope.
- Rewriting archived OpenSpec history or immutable historical evidence.
- Adding a database migration when no statistics persistence exists.

## Decisions

1. Delete the whole Spring statistics package. Keeping an unused internal service or DTO would retain the feature's computation and maintenance burden without a caller.
2. Remove task repository methods used only by `StatsService`. This completes backend service removal while retaining repository operations used by normal task workflows.
3. Delete the Angular screen and HTTP service, then remove its models and every route/navigation/shortcut entry point. A compatibility redirect is not added; the wildcard route supplies the application's standard fallback for the former URL.
4. Remove the active/overdue summary block from the Android project screen. Android has no statistics endpoint, model, service, route, or dedicated screen, so no other Android replacement is needed.
5. Delete the dedicated OpenAPI path/schema files and their root references. Update the maintained backend audit so it no longer reports the endpoint, statistics package, or remediation work against removed code.
6. Record absence as a new `statistics-removal` capability. Archived specifications and migrations remain untouched as historical records.

## Risks / Trade-offs

- [A stats-only symbol is missed in a shared file] → Scan maintained frontend, Android, backend, OpenAPI, and current documentation for statistics route/type/feature identifiers after deletion.
- [A repository method is still used outside statistics] → Verify symbol references before deleting each method and compile/test the backend afterward.
- [Concurrent removal work is overwritten] → Patch current file contents narrowly and review statistics-scoped diffs plus the full worktree status.
- [Former `/stats` links remain bookmarked] → Accept the application's existing wildcard fallback; no compatibility behavior is retained for a removed feature.

## Migration Plan

Deploy the client and backend removals together. The backend stops routing `/stats/overview`, while updated clients stop linking to or calling it. Rollback is a code rollback only because no persisted statistics data or schema is changed.

## Open Questions

None.
