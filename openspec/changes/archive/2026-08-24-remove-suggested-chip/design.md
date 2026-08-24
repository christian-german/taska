## Context

The Today component currently computes a set containing the first two same-day
tasks after sorting candidates by priority and estimate. The task list converts
membership in that set into a boolean task-row input, and the task row uses the
input only to render the "suggéré" chip. No user action or domain behavior depends
on this presentation state.

## Goals / Non-Goals

**Goals:**

- Remove the suggestion marker from every web-client task row.
- Remove the presentation-only suggestion data path so it cannot render in a
  different task-list context later.
- Retain all task-list behavior and all other row metadata.

**Non-Goals:**

- Change task ordering, priority, estimates, or scheduling.
- Change appointment, project, label, recurrence, due-date, or mention metadata.
- Change backend or Android behavior.

## Decisions

### Remove the suggestion presentation contract

Remove the Today-level suggestion computation, the task-list suggestion input and
binding, and the task-row suggestion input and conditional chip. Removing the
entire presentation contract is preferable to hard-coding the chip as hidden: it
eliminates unused state and prevents another caller from accidentally restoring
the marker through the same API.

### Verify absence and retained metadata

Component tests will render task rows and lists with representative task data,
assert that the suggestion label and icon are absent, and retain coverage for
unrelated metadata and interactions. This checks the observable requirement while
guarding against an overly broad removal of task-row chips.

## Risks / Trade-offs

- Removing shared component inputs requires updating every call site. Static
  Angular compilation and focused component tests will detect stale bindings.
- Tests must identify the suggestion marker specifically rather than prohibiting
  every chip, because appointment and task metadata chips remain supported.
