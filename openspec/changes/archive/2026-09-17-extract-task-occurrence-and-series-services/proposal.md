## Why

The task mutation signatures now identify their targets explicitly, but `TaskService` still owns base-task behavior, recurring occurrence lifecycle, recurrence expansion, and recurring-series restructuring. Extracting those responsibilities makes the service boundaries match the domain concepts and prevents recurrence-specific dependencies from leaking back into ordinary task operations.

## What Changes

- Introduce `TaskOccurrenceService` for occurrence expansion, validation, overrides, skipping, completion, and reopening.
- Introduce `RecurringTaskSeriesService` for operations that truncate or split a recurring series.
- Keep `TaskService` focused on stored task and series-definition operations shared by ordinary tasks and recurring definitions.
- Update `TaskMutationService` to route explicit operations to the owning service while publishing one change signal.
- Route calendar occurrence queries through `TaskOccurrenceService` without changing HTTP or MCP contracts.

## Capabilities

### New Capabilities

- `task-service-responsibility-separation`: Defines ownership boundaries between stored tasks, recurring occurrences, and recurring-series structure.

### Modified Capabilities

None.

## Impact

- Affects task application services, task controller query delegation, and unit tests.
- Does not change persistence, OpenAPI, MCP inputs or outputs, recurrence rules, notifications, or mutation behavior.
