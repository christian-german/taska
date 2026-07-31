## 1. Persistence and contracts

- [x] 1.1 Add a Flyway migration for `task_priority_evaluation` with a unique cascading task foreign key, score, JSONB components document, computation timestamp, and indexes for missing-evaluation selection.
- [x] 1.2 Implement the priority-evaluation entity, repository, DTOs, and JSON mapping for the four common component fields.
- [x] 1.3 Add a task-scoped read endpoint/service contract that returns an existing evaluation or an explicit no-evaluation result.

## 2. Deterministic scoring and LLM integration

- [x] 2.1 Implement the specified 0-100 value domains and backend point mappings for urgency, impact, risk, and duration, including final-score calculation.
- [x] 2.2 Add the Spring AI OpenAI model starter and `spring.ai.openai.api-key=${OPENAI_API_KEY}` configuration, then implement a Spring AI structured batch request/response contract that identifies every task by UUID and requests the four assessments without using manual priority or user-entered estimate values.
- [x] 2.3 Implement validation for the batch envelope and each task result's ID, required criteria, values, confidence ranges, and reasons before calculating or persisting scores.
- [x] 2.4 Implement the evaluation service to build minimal task context, invoke the LLM, calculate points/score, and persist only current eligible task results.

## 3. Background batch processing

- [x] 3.1 Add an indexed repository query for incomplete, non-recurring `TODO` tasks that have no evaluation and fetch bounded batches of at most ten.
- [x] 3.2 Add a configurable Spring scheduler that processes missing evaluations in structured LLM batches without participating in task write transactions.
- [x] 3.3 Protect persistence against in-flight task changes by verifying eligibility and task freshness before writing each batch result.
- [x] 3.4 Add failure logging and retry-safe behavior: failed requests or malformed envelopes retry the whole batch, while invalid, missing, or stale individual results remain missing and are retried independently.

## 4. Task lifecycle integration

- [x] 4.1 Remove a current evaluation whenever an eligible non-recurring TODO is updated.
- [x] 4.2 Remove a current evaluation when a TODO becomes a meeting or when its task is completed or deleted.
- [x] 4.3 Preserve asynchronous behavior for new TODOs and for meetings converted into eligible TODOs so the scheduler creates their evaluations later.

## 5. Verification

- [x] 5.1 Add repository and migration integration tests for one evaluation per task and cascade cleanup.
- [x] 5.2 Add unit tests for point mapping, score calculation, structured-response validation, and the impact-versus-risk explanation contract.
- [x] 5.3 Add service tests for successful batch persistence, malformed envelopes, invalid or missing individual results, Spring AI/provider failures, and stale results after task mutation.
- [x] 5.4 Add controller tests for retrieving existing and missing evaluations and task lifecycle invalidation.
- [x] 5.5 Run the relevant backend test suite and OpenSpec validation for the change.
