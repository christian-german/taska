## ADDED Requirements

### Requirement: Manual priority is independent of calculated evaluation
The system SHALL treat a task's nullable manual `priority` property independently from its current calculated priority evaluation. An absent manual priority SHALL NOT be represented as a calculated score of zero and SHALL NOT change the stored evaluation's criteria or score semantics.

#### Scenario: Retrieve a task with no manual priority and an evaluation
- **WHEN** an eligible task has `priority: null` and a current priority evaluation
- **THEN** the task representation SHALL retain `priority: null` and the evaluation retrieval SHALL return its calculated evaluation

#### Scenario: Retrieve a task with neither value
- **WHEN** a task has no manual priority and no current priority evaluation
- **THEN** the system SHALL represent the absent manual priority and absent evaluation independently
