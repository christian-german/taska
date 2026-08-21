## ADDED Requirements

### Requirement: Today widget omits time for all-day tasks
The Today widget SHALL use each displayed task's `allDay` value to determine whether its row includes a clock time. A task whose `allDay` value is true SHALL display its title without a clock time or a time-specific separator. A task whose `allDay` value is false SHALL retain its scheduled-time text. This distinction SHALL NOT change task selection, ordering, completion state or actions, recurring-occurrence identity, or task-detail navigation.

#### Scenario: All-day task is displayed in the Today widget
- **WHEN** the Today widget renders a task whose `allDay` value is true
- **THEN** the task row SHALL display the task title without a clock time
- **AND** the row SHALL NOT reserve a blank time prefix or display a time-specific separator

#### Scenario: Timed task is displayed in the Today widget
- **WHEN** the Today widget renders a task whose `allDay` value is false
- **THEN** the task row SHALL display the task's scheduled time and title

#### Scenario: Completed all-day task is displayed in the Today widget
- **WHEN** the Today widget renders a completed task whose `allDay` value is true
- **THEN** the task title SHALL remain struck through and its completion control SHALL remain checked
- **AND** the row SHALL omit the clock time without changing its reopen or task-detail actions
