## Why

Taska currently exposes a manual priority but cannot explain which open work deserves attention across competing tasks. A persisted, explainable priority evaluation provides a focused foundation for later prioritization and scheduling features without delaying normal task writes on an LLM call.

## What Changes

- Add asynchronous priority evaluations for incomplete, non-recurring `TODO` items only.
- Assess urgency, impact, risk, and duration with an LLM, while the backend validates the result and computes the final score from configured point mappings.
- Use Spring AI for LLM communication and structured batch evaluation requests.
- Persist one current evaluation per eligible task, including its calculated score, criteria explanation JSON, and computation timestamp.
- Remove an evaluation when its task is updated, completed, deleted, or changed from `TODO` to `MEETING`.
- Add a scheduled background worker that finds missing evaluations and processes eligible tasks in LLM batches.
- Exclude recurring tasks, meetings, aging, manual-priority scoring, user-entered estimate scoring, and time-driven reevaluation from this V1.

## Capabilities

### New Capabilities

- `task-priority-evaluation`: Generate, persist, invalidate, and retrieve explainable priority evaluations for eligible tasks.

### Modified Capabilities

None.

## Impact

- Backend task domain: a new evaluation entity, repository, scoring/evaluation services, scheduled worker, and task lifecycle integration.
- Database: a Flyway migration creating `task_priority_evaluation` with a task foreign key and JSON evaluation payload.
- API and clients: task evaluation data/retrieval and priority-based presentation will require defined API contracts and client updates.
- AI integration: adds a Spring AI model client, strict structured-output validation, batching, retry handling, and provider configuration.
