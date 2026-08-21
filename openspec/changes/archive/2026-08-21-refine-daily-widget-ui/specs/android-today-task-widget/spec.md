## ADDED Requirements

### Requirement: Today widget groups overdue tasks visibly
The Today widget SHALL show the established `Overdue` label before its overdue task rows when at least one overdue row is displayed. When both overdue and current-day rows are displayed, it SHALL render exactly one divider between those groups. It SHALL NOT render dividers between individual tasks within either group or show an overdue label or group divider when no overdue row is displayed.

#### Scenario: Overdue and current-day tasks are displayed
- **WHEN** the Today widget displays one or more overdue tasks followed by one or more tasks planned for the current local date
- **THEN** it SHALL show `Overdue` before the overdue rows and exactly one divider between the overdue and current-day groups
- **AND** it SHALL NOT show dividers between tasks within either group

#### Scenario: Only overdue tasks are displayed
- **WHEN** every displayed Today-widget task is overdue
- **THEN** it SHALL show `Overdue` before those rows without a trailing group divider

#### Scenario: No overdue task is displayed
- **WHEN** the Today widget displays only tasks planned for the current local date or displays no tasks
- **THEN** it SHALL hide the overdue label and SHALL NOT show task-row dividers
