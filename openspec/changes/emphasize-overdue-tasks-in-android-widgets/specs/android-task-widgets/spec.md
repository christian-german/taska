## ADDED Requirements

### Requirement: Calendar-week widget visually emphasizes overdue content
The calendar-week widget SHALL render the visible text of its `Overdue` header and every overdue task row in a legible overdue-red color and bold font weight. This treatment SHALL apply to the task row's time and title text and SHALL NOT recolor its completion control or appointment indicator. Ordinary date headers and non-overdue task text SHALL retain their existing styles.

#### Scenario: Overdue group is emphasized
- **WHEN** the calendar-week widget displays its `Overdue` group
- **THEN** the `Overdue` header text and every task's time/title text in that group SHALL be overdue red and bold

#### Scenario: Other Week content retains its style
- **WHEN** the calendar-week widget displays a current-week date header and non-overdue task row
- **THEN** neither the date header nor the task's time/title text SHALL receive the overdue color or bold treatment

#### Scenario: Non-text row controls retain their presentation
- **WHEN** an overdue Week row includes its completion control or an appointment indicator
- **THEN** those controls SHALL retain their existing presentation rather than receive the overdue text treatment

#### Scenario: Overdue emphasis works in each widget theme
- **WHEN** the device renders the calendar-week widget in either its light or dark theme
- **THEN** the overdue-red header and task text SHALL remain legible against the widget surface
