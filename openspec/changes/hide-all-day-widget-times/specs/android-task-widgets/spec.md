## ADDED Requirements

### Requirement: Calendar-week widget omits time for all-day tasks
The calendar-week widget SHALL use each displayed task's `allDay` value to determine whether its row includes a clock time. A task whose `allDay` value is true SHALL display its title without a clock time or a time-specific separator. A task whose `allDay` value is false SHALL retain its localized scheduled-time text. This distinction SHALL NOT change task selection, date grouping, chronological ordering, completion actions, recurring-occurrence identity, or task-detail navigation.

#### Scenario: All-day task is displayed in the calendar-week widget
- **WHEN** the calendar-week widget renders a qualifying task whose `allDay` value is true
- **THEN** the task row SHALL display the task title without a clock time
- **AND** the row SHALL NOT reserve a blank time prefix or display a time-specific separator

#### Scenario: Timed task is displayed in the calendar-week widget
- **WHEN** the calendar-week widget renders a qualifying task whose `allDay` value is false
- **THEN** the task row SHALL display the task's localized scheduled time and title

#### Scenario: All-day task remains actionable
- **WHEN** the calendar-week widget omits the time from an all-day task row
- **THEN** the task SHALL retain its existing completion and task-detail actions, including recurring-occurrence identity when present
