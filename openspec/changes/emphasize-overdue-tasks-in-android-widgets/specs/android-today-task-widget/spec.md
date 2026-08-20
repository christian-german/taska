## ADDED Requirements

### Requirement: Today widget visually emphasizes overdue tasks
The Today widget SHALL render the visible time and title text of every overdue task row in a legible overdue-red color and bold font weight. It SHALL NOT add an overdue header or recolor the row's completion control or appointment indicator. Tasks planned for the current local date, including completed tasks, SHALL retain their existing text styles.

#### Scenario: Overdue Today task is emphasized
- **WHEN** the Today widget displays a task whose device-local scheduled date is earlier than the current local date
- **THEN** the task's visible time/title text SHALL be overdue red and bold

#### Scenario: Task planned today retains its style
- **WHEN** the Today widget displays an incomplete or completed task planned for the current local date
- **THEN** the task's time/title text SHALL NOT receive the overdue color or bold treatment

#### Scenario: Non-text row controls retain their presentation
- **WHEN** an overdue Today row includes its completion control or an appointment indicator
- **THEN** those controls SHALL retain their existing presentation rather than receive the overdue text treatment

#### Scenario: Overdue emphasis works in each widget theme
- **WHEN** the device renders the Today widget in either its light or dark theme
- **THEN** the overdue-red task text SHALL remain legible against the widget surface
