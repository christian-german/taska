## MODIFIED Requirements

### Requirement: Eligible tasks have one current priority evaluation
The system SHALL support one current priority evaluation for each incomplete, non-recurring task whose type is `TODO`. An evaluation SHALL store the task identifier, calculated score, criteria JSON document, and computation timestamp. A task SHALL have at most one current evaluation.

#### Scenario: Evaluation is stored for an eligible task
- **WHEN** the system successfully evaluates an incomplete, non-recurring `TODO`
- **THEN** it SHALL store one current evaluation linked to that task with a score, criteria JSON document, and computation timestamp

#### Scenario: Ineligible task is not evaluated
- **WHEN** a task is recurring, completed, or has type `APPOINTMENT`
- **THEN** the system SHALL not create a priority evaluation for that task

### Requirement: Task changes invalidate evaluations
The system SHALL remove an existing priority evaluation when its task is updated, completed, deleted, or changed from `TODO` to `APPOINTMENT`. Creating a non-recurring `TODO` SHALL not synchronously create an evaluation.

#### Scenario: Eligible task is updated
- **WHEN** an incomplete, non-recurring `TODO` with an evaluation is updated
- **THEN** the system SHALL remove its current evaluation

#### Scenario: Task becomes an appointment
- **WHEN** a task with an evaluation is changed from `TODO` to `APPOINTMENT`
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
- **WHEN** a task is updated, completed, deleted, becomes recurring, or becomes an appointment after it is selected for a batch and before its result is stored
- **THEN** the system SHALL discard that stale result and SHALL not create an evaluation for the task
