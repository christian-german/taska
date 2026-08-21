## MODIFIED Requirements

### Requirement: Widget supports one-tap task completion
The widget SHALL provide an immediately accessible completion action for every displayed task. Invoking the action SHALL immediately render that task's circular completion control as checked, before the authenticated completion request finishes, while leaving the task row otherwise unchanged. The action SHALL use the task ID and, for a recurring occurrence, its `occurrenceScheduledAt` to complete precisely that task or occurrence through the authenticated task API. The widget SHALL refresh after successful completion. If initialization, authentication, transport, or API completion fails, the invoked control SHALL return to its unchecked presentation without requiring a successful network refresh, and the widget SHALL NOT present the rejected completion as complete.

#### Scenario: Completion shows immediate feedback
- **WHEN** the user invokes the completion action for a displayed incomplete task
- **THEN** that task's circular completion control SHALL appear checked before the completion request succeeds or fails
- **AND** the row's visibility, text presentation, grouping, ordering, and task identity SHALL remain unchanged while the request is pending

#### Scenario: Complete a non-recurring task
- **WHEN** the completion request for a non-recurring task succeeds
- **THEN** the system SHALL refresh the widget and remove the completed task from its authoritative contents

#### Scenario: Complete one recurring occurrence
- **WHEN** the user invokes the completion action for a displayed recurring occurrence
- **THEN** the system SHALL optimistically check that occurrence's control and complete only the occurrence identified by its `occurrenceScheduledAt`

#### Scenario: Completion request fails
- **WHEN** initialization, authentication, transport, or the task API prevents the completion request from succeeding
- **THEN** the invoked task's completion control SHALL return to unchecked
- **AND** the widget SHALL retain the task as incomplete and make a subsequent completion or refresh possible
