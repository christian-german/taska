## Purpose

Define asynchronous, explainable priority evaluations for eligible tasks.

## Requirements

### Requirement: Eligible tasks have one current priority evaluation
The system SHALL support one current priority evaluation for each incomplete, non-recurring task whose type is `TODO`. An evaluation SHALL store the task identifier, calculated score, criteria JSON document, and computation timestamp. A task SHALL have at most one current evaluation.

#### Scenario: Evaluation is stored for an eligible task
- **WHEN** the system successfully evaluates an incomplete, non-recurring `TODO`
- **THEN** it SHALL store one current evaluation linked to that task with a score, criteria JSON document, and computation timestamp

#### Scenario: Ineligible task is not evaluated
- **WHEN** a task is recurring, completed, or has type `MEETING`
- **THEN** the system SHALL not create a priority evaluation for that task

### Requirement: Evaluations explain four assessed criteria
The criteria JSON document SHALL contain `urgency`, `impact`, `risk`, and `duration`. Each criterion SHALL contain `value`, `source`, `confidence`, `reason`, and `points`. V1 criterion sources SHALL be `LLM`; the backend SHALL calculate and persist the points and final score after validating the LLM assessment.

Urgency SHALL use `LOW`, `MEDIUM`, `HIGH`, or `CRITICAL` and contribute 0, 10, 20, or 30 points respectively. Impact SHALL use `LOW`, `MEDIUM`, or `HIGH` and contribute 0, 15, or 30 points respectively. Risk SHALL use `LOW`, `MEDIUM`, or `HIGH` and contribute 0, 12, or 25 points respectively. Duration SHALL be an integer number of minutes and contribute 15 points at 15 minutes or less, 10 points from 16 through 30 minutes, 5 points from 31 through 60 minutes, and 0 points above 60 minutes. The calculated final score SHALL range from 0 through 100.

#### Scenario: Evaluation provides an explanation
- **WHEN** an evaluation is retrieved
- **THEN** it SHALL include all four criteria with value, source, confidence, reason, and points

#### Scenario: Maximum-valued assessment is scored
- **WHEN** an assessment has CRITICAL urgency, HIGH impact, HIGH risk, and duration of 15 minutes or less
- **THEN** the system SHALL calculate a final score of 100

#### Scenario: LLM response has an invalid criterion
- **WHEN** an LLM response contains an unsupported value, invalid confidence, missing criterion, or mismatched task identifier
- **THEN** the system SHALL not persist an evaluation from that response for the affected task

### Requirement: Impact and risk have distinct meanings
The system SHALL assess impact as the value created by completing a task and risk as the consequences of not completing a task in time.

#### Scenario: Evaluation distinguishes benefit from consequence
- **WHEN** the system evaluates a task
- **THEN** the impact explanation SHALL address the value of completion and the risk explanation SHALL address the consequence of delay or non-completion

### Requirement: Task changes invalidate evaluations
The system SHALL remove an existing priority evaluation when its task is updated, completed, deleted, or changed from `TODO` to `MEETING`. Creating a non-recurring `TODO` SHALL not synchronously create an evaluation.

#### Scenario: Eligible task is updated
- **WHEN** an incomplete, non-recurring `TODO` with an evaluation is updated
- **THEN** the system SHALL remove its current evaluation

#### Scenario: Task becomes a meeting
- **WHEN** a task with an evaluation is changed from `TODO` to `MEETING`
- **THEN** the system SHALL remove its current evaluation

#### Scenario: Task is completed or deleted
- **WHEN** a task with an evaluation is completed or deleted
- **THEN** the system SHALL remove its current evaluation

### Requirement: Missing evaluations are generated asynchronously in batches
The system SHALL use a scheduled background process to find eligible tasks without an evaluation and request assessments in batches of no more than ten tasks per LLM request. The system SHALL use Spring AI's OpenAI integration for LLM communication, with `spring.ai.openai.api-key` sourced from `OPENAI_API_KEY`. The background process SHALL not block task creation or task updates on LLM work.

#### Scenario: Scheduler evaluates missing tasks
- **WHEN** the scheduled process finds eligible tasks without evaluations
- **THEN** it SHALL submit up to ten tasks in one LLM request and persist validated evaluations for the successful batch results

#### Scenario: Batch request or envelope fails
- **WHEN** an LLM request fails or its batch response envelope cannot be validated
- **THEN** the system SHALL persist no evaluations from that batch and SHALL leave its tasks without evaluations for a later scheduled retry

#### Scenario: Individual batch result is invalid
- **WHEN** one task result in an otherwise valid batch is missing, invalid, or stale
- **THEN** the system SHALL not persist an evaluation for that task, SHALL persist other validated current results from the batch, and SHALL leave the skipped task eligible for a later scheduled retry

#### Scenario: Task changes during evaluation
- **WHEN** a task is updated, completed, deleted, becomes recurring, or becomes a meeting after it is selected for a batch and before its result is stored
- **THEN** the system SHALL discard that stale result and SHALL not create an evaluation for the task

### Requirement: Scores are snapshot-based in V1
The system SHALL not include aging in a priority evaluation and SHALL not automatically refresh an existing evaluation merely because time has passed. Urgency SHALL represent the LLM assessment at the computation timestamp.

#### Scenario: Time passes without task mutation
- **WHEN** an evaluated task remains unchanged while time passes
- **THEN** the system SHALL retain its existing evaluation without automatic reevaluation

### Requirement: Priority evaluations can be retrieved
The system SHALL provide a task-scoped read operation that returns the current priority evaluation when one exists and represents its absence when the task has no current evaluation.

#### Scenario: Existing evaluation is requested
- **WHEN** a client requests the priority evaluation for an eligible task with a current evaluation
- **THEN** the system SHALL return that evaluation

#### Scenario: Missing evaluation is requested
- **WHEN** a client requests the priority evaluation for a task without a current evaluation
- **THEN** the system SHALL represent that no current evaluation exists without treating it as a score of zero
