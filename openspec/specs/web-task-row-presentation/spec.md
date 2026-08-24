# web-task-row-presentation Specification

## Purpose
TBD - created by archiving change remove-suggested-chip. Update Purpose after archive.

## Requirements

### Requirement: Web task rows omit suggestion markers

The web client SHALL render task-list rows without a "suggested" chip, label, icon,
or replacement suggestion marker. This SHALL apply regardless of a task's
priority, estimate, schedule, position, or task-list group.

#### Scenario: A high-priority estimated task appears on Today

- **WHEN** the web client renders a task on the Today screen that would otherwise qualify as suggested based on its priority and estimate
- **THEN** the task row SHALL NOT display a "suggested" chip, label, icon, or replacement suggestion marker

#### Scenario: Multiple qualifying tasks appear in a task group

- **WHEN** the web client renders multiple tasks that would otherwise qualify as suggested
- **THEN** none of their task rows SHALL display a suggestion marker

### Requirement: Removing the suggestion marker preserves task rows

The web client SHALL preserve task content, ordering, supported metadata, selection,
editing, and completion behavior when it removes the suggestion marker.

#### Scenario: A task has other visible metadata

- **WHEN** the web client renders a task with supported metadata such as an appointment type, due date, estimate, recurrence, project, mention, or label
- **THEN** the task row SHALL continue to display that metadata according to its existing rules
- **AND** it SHALL NOT display a suggestion marker

#### Scenario: A user interacts with a task without a suggestion marker

- **WHEN** a user selects, edits, completes, or reopens a task from a web task row
- **THEN** the interaction SHALL retain its existing behavior
