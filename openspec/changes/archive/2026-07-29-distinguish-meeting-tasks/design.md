## Context

Taska currently presents all tasks as the same kind of work item. A task needs a durable classification so people can distinguish work they must perform from meetings they must attend, without changing existing task completion, scheduling, recurrence, or project behavior.

## Goals / Non-Goals

**Goals:**

- Persist an explicit task type with the two initial values `TODO` and `MEETING`.
- Preserve existing tasks and clients by treating an absent legacy value as `TODO`.
- Support selecting and changing the type in task create/edit flows.
- Surface the type consistently anywhere a task is identified in the UI.

**Non-Goals:**

- Creating calendar events, invitations, attendees, availability, or notifications.
- Replacing due dates, durations, recurrence, completion, or priority behavior.
- Adding custom task types or a taxonomy beyond to-do and meeting.
- Changing task ordering or filtering semantics in this change.

## Decisions

### Model type as a closed task-domain enum

Add a `TaskType` field with `TODO` and `MEETING` values to the task domain and API/persistence representations. A closed enum prevents arbitrary labels from becoming incompatible client data and gives each client a stable, localizable presentation mapping. Custom strings were considered but would require validation, migration rules, and a product decision about custom type management.

### Default missing values to `TODO` at every boundary

Create operations that omit the field, deserialization of legacy records, and migration/backfill behavior will resolve the type to `TODO`. This keeps existing tasks and older clients working without user action. Requiring callers to specify a value would make the API breaking, while leaving null meaningful would spread legacy handling through the application.

### Expose a compact explicit selector and a consistent visual marker

Task forms will provide a two-choice type selector with To-do as the initial choice. Task rows and task details will show a distinct meeting marker and accessible text when the value is `MEETING`; to-do tasks retain the familiar task treatment. Mobile daily and weekly views SHALL reuse the same meeting icon already used by the mobile Today view, and the web tracker SHALL reuse the same meeting identifier used by other web task presentations. This makes the classification useful during scanning without changing the meaning of completion controls. Relying on titles or due-time heuristics was rejected because both are ambiguous and not durable data.

### Carry type through all task transport paths

The type will be included in task create/update input and task output used by every supported client, including non-UI integrations. Transport mapping will be centralized with existing task mapping logic so a client cannot silently lose the value on update.

## Risks / Trade-offs

- [Older clients omit or fail to render the new field] → Default omitted input to `TODO` and retain a compatible response shape while clients are updated.
- [A migration leaves records without a value] → Use a database default/backfill where supported and enforce the application-level fallback.
- [Meeting styling becomes inconsistent across views] → Reuse the established Today-view icon on mobile and the existing web meeting identifier in the tracker, with accessible textual labels rather than color alone.
- [The two types later prove insufficient] → Keep the enum and UI mapping isolated so a future type can be added through an explicit compatibility change.

## Migration Plan

1. Add the nullable/defaulted persistence field and backfill existing records as `TODO`.
2. Deploy backend/domain support that reads missing values as `TODO` and returns an explicit type.
3. Update create/edit and task-presentation clients to select and display the type.
4. Roll back application code safely because legacy/missing values remain interpretable as `TODO`; if a schema rollback is necessary, preserve the data column until the forward deployment is restored.

## Open Questions

- None for the initial two-type release; meeting-specific calendar behavior remains intentionally out of scope.
