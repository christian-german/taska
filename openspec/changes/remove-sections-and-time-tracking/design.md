## Context

Sections currently cross the database, task and project REST contracts, MCP task tools, Angular project rendering, and documentation. Time tracking is implemented as a separate `time_entries` backend resource, an Angular tracker page, and an Android task timer. Removing only the visible screens would leave callable APIs and persisted feature state, while deleting only backend packages would break clients and task/project contracts.

The repository also has existing Flyway installations. Published migrations are deployment history and cannot safely be rewritten or deleted without checksum failures, so removal must be expressed by a new forward migration even though historical migration files retain the record of how old schemas were created.

## Goals / Non-Goals

**Goals:**

- Remove every live section and time-tracking capability from backend, web, Android, REST/OpenAPI, MCP, documentation, and maintained tests/assets.
- Remove section fields and filters from task/project representations rather than leaving ignored compatibility fields.
- Delete persisted section assignments, section records, and time entries through a forward migration.
- Leave the remaining clients and backend compiling and their contracts synchronized.

**Non-Goals:**

- Removing task scheduling, deadlines, recurrence, notifications, calendar behavior, or estimates.
- Rewriting immutable historical Flyway migrations, archived OpenSpec history, or dated audit evidence.
- Preserving access to section or time-entry data after upgrade.

## Decisions

### Remove contract members instead of accepting deprecated no-ops

Task `sectionId`, the REST `section_id` filter, project-section endpoints, and MCP section inputs/outputs will be deleted. Accepting but ignoring them would leave the removed feature advertised in generated schemas and client models and could mask stale callers.

Alternative considered: retain nullable compatibility fields. Rejected because the requested outcome is complete feature removal and the API break is intentional.

### Use one forward destructive schema migration

A new Flyway migration will drop the task foreign key/column and its index, then drop `sections` and `time_entries`. Backend entities and repositories will be removed in the same release so no runtime code depends on the dropped objects.

Alternative considered: edit `V1` and delete `V4`. Rejected because existing databases would fail Flyway checksum validation and would never receive an upgrade step that removes stored data.

### Keep time estimates separate from time tracking

`estimateMinutes` and the estimate UI remain. They describe expected task effort and participate in prioritization/planning, while the removed feature records elapsed intervals and exposes start/stop timer controls. Scheduled timestamps and due dates likewise remain calendar/task semantics.

Alternative considered: remove all time-related task fields. Rejected because it would remove unrelated scheduling and planning behavior not represented by the product's time-tracking feature.

### Remove feature-specific clients and assets outright

The Angular `/time` route, navigation item, component, model/service utilities, section service/model, and section rendering will be deleted or simplified. Android time-entry networking and timer state/actions/buttons will be removed. Feature-specific archived timer artwork will be deleted; generic UI “section” terminology and platform timing primitives remain because they do not represent project sections or time tracking.

## Risks / Trade-offs

- [Existing section and time-entry data is irreversibly deleted] → Make the breaking/destructive behavior explicit in the specification and keep the forward migration small and ordered.
- [Stale callers fail after upgrade] → Remove all first-party callers and published OpenAPI/MCP schema members in the same change, then use repository-wide reference scans and contract/build tests.
- [Broad text removal damages unrelated scheduling or generic layout code] → Classify references by domain and retain `estimateMinutes`, scheduled/due timestamps, generic document sections, and CSS transition timing.
- [Dirty pre-existing local work overlaps edited files] → Preserve those edits and apply narrowly targeted patches, reviewing the final diff against the pre-change worktree state.

## Migration Plan

1. Ship client and backend contract removals together with the new Flyway migration.
2. On backend startup, Flyway removes task section assignments, section data, and time-entry data before the updated application begins serving requests.
3. Deploy updated web and Android clients so no first-party caller uses the removed contracts.
4. Rollback requires restoring the prior application and schema from a database backup; the dropped feature data cannot be reconstructed from the migration.

## Open Questions

None.
