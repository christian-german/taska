## MODIFIED Requirements

### Requirement: Base task updates replace the complete mutable task representation

The system SHALL expose `PUT /tasks/{taskId}` as a full replacement operation for a non-occurrence task. The request body SHALL be a typed `TaskUpdateRequest` containing every mutable base-task property: content, type, description, projectId, parentId, order, priority, labels, scheduledAt, dueAt, allDay, isRecurring, estimateMinutes, mentionContext, and recurrenceRule. The request SHALL NOT contain a section identifier or output-only task fields such as ID, completion state, timestamps, or occurrence metadata. The system SHALL reject incomplete or invalid replacement requests without changing the task.

#### Scenario: Replace a task with complete mutable values
- **WHEN** a client sends a valid complete `TaskUpdateRequest` to `PUT /tasks/{taskId}`
- **THEN** the system SHALL replace every mutable base-task property with the supplied value
- **AND** it SHALL return the resulting task representation without a section identifier

#### Scenario: Partial base-task update is rejected
- **WHEN** a client omits a required mutable property from `PUT /tasks/{taskId}`
- **THEN** the system SHALL reject the request
- **AND** it SHALL leave the existing task unchanged

#### Scenario: Output-only and retired task state is not client-controlled
- **WHEN** a client sends a base-task replacement request
- **THEN** the request SHALL NOT be able to replace a section identifier, task ID, completion state, created timestamp, updated timestamp, completed timestamp, or occurrence metadata
