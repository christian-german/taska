# overdue-task-query Specification

## Purpose

Provide the complete, open historical task backlog through a dedicated API query.

## Requirements

### Requirement: Overdue task query returns the complete open historical backlog
The HTTP API SHALL expose `GET /tasks/overdue`. It SHALL return every non-recurring task and expanded recurring occurrence that is incomplete, has a non-null effective `scheduledAt`, and whose effective scheduled date is earlier than the current calendar date in `taska.calendar.time-zone`. It SHALL return the complete matching history without pagination, ordered by effective `scheduledAt` ascending. It SHALL exclude completed tasks, completed occurrences, skipped occurrences, and unscheduled or deadline-only tasks.

#### Scenario: Open daily occurrences are overdue
- **WHEN** an uncompleted daily series began before the current calendar date
- **THEN** the response SHALL contain one recurring-occurrence representation for every generated open occurrence before the current calendar date

#### Scenario: A moved occurrence uses its effective date
- **WHEN** an open recurring occurrence has been moved to a date before the current calendar date
- **THEN** the response SHALL include that occurrence with its overridden `scheduledAt` and its original `occurrenceScheduledAt`

#### Scenario: A moved occurrence is not overdue at its original date
- **WHEN** an open recurring occurrence generated before the current calendar date has been moved to the current or a future calendar date
- **THEN** the response SHALL NOT include that occurrence

#### Scenario: Completed and skipped historical work is excluded
- **WHEN** a historical task or occurrence is completed or a recurring occurrence is skipped
- **THEN** the response SHALL NOT include it
