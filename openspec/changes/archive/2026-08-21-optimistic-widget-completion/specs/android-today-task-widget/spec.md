## MODIFIED Requirements

### Requirement: Today widget distinguishes and toggles completed tasks
The Today widget SHALL use the existing Taska widget visual design. It SHALL render a completed task with struck-through primary text and a checked circular completion control. Tapping that completed task's control SHALL reopen precisely that task or recurring occurrence through the authenticated task API, then refresh the widget. It SHALL render an incomplete task with the existing unchecked circular completion control and completion action. Invoking an incomplete task's completion action SHALL immediately render only its circular completion control as checked before the request finishes, while leaving the row's other presentation unchanged. The widget SHALL refresh after successful completion. If initialization, authentication, transport, or API completion fails, the invoked control SHALL return to unchecked without requiring a successful network refresh. All displayed task rows SHALL retain navigation to their matching task detail, and completed-task reopening behavior SHALL remain response-driven.

#### Scenario: Completed task row is rendered
- **WHEN** the Today widget renders an authoritatively completed task
- **THEN** its title SHALL be struck through and its circular completion control SHALL appear checked

#### Scenario: Incomplete task completion shows immediate feedback
- **WHEN** the user invokes the completion control for an incomplete Today task
- **THEN** that task's circular completion control SHALL appear checked before the completion request succeeds or fails
- **AND** its row visibility and title presentation SHALL remain unchanged while the request is pending

#### Scenario: Incomplete task can be completed
- **WHEN** the completion request for an incomplete Today task or recurring occurrence succeeds
- **THEN** the system SHALL refresh the Today widget from authoritative task data

#### Scenario: Incomplete task completion fails
- **WHEN** initialization, authentication, transport, or the task API prevents completion from succeeding
- **THEN** the invoked task's circular completion control SHALL return to unchecked
- **AND** the task SHALL remain available for a subsequent completion attempt

#### Scenario: Completed task can be reopened
- **WHEN** the user invokes the checked completion control for a completed Today task
- **THEN** the system SHALL preserve the checked presentation until it reopens the identified task or recurring occurrence and refreshes the Today widget

#### Scenario: Reopening a completed task fails
- **WHEN** the completed task's reopen action cannot be authenticated or the task API rejects it
- **THEN** the widget SHALL retain the task as completed and make a subsequent refresh possible

#### Scenario: Completed task title is tapped
- **WHEN** the user taps a completed task's row or title outside its completion control
- **THEN** the application SHALL open that task's detail without changing its completion state
