## Context

Taska persists `TODO` and `MEETING` items in the `tasks` table. It already stores a manual numeric priority and an optional user-facing estimate, but neither has a reproducible explanation or an LLM provenance model. Recurring tasks are represented as a series with optional occurrence overrides, which makes a task-level evaluation ambiguous.

The backend already uses Spring AI for its MCP server, but it does not currently configure a generative model client.

The priority engine must not make task creation or updates depend on an external LLM. V1 therefore evaluates only incomplete, non-recurring `TODO` tasks in the background and stores one current, explainable result per eligible task.

## Goals / Non-Goals

**Goals:**

- Persist one current evaluation with a final score, calculation timestamp, and structured component explanation.
- Ask an LLM to assess urgency, impact, risk, and duration for batches of eligible tasks.
- Validate LLM output and calculate component points and the final score in the backend.
- Invalidate an evaluation when its underlying task is changed or no longer eligible.
- Allow the system to recover from missing, failed, or delayed LLM evaluations through scheduled batch processing.

**Non-Goals:**

- Evaluating recurring tasks, occurrences, meetings, completed tasks, or deleted tasks.
- Aging, automatic time-based score refresh, scheduling, or calendar placement.
- Incorporating manual priority, user-entered duration, labels, or other user-entered scoring values.
- Preserving evaluation history, prompt/model versioning, or user overrides in V1.
- Blocking normal task mutations while waiting for an LLM response.

## Decisions

### Store an evaluation separately from the task

Create `task_priority_evaluation` with `id`, `task_id`, `score`, `components_json`, and `computed_at`. `task_id` is a unique foreign key to `tasks` with cascade deletion, enforcing the one-current-evaluation model. `components_json` is PostgreSQL `JSONB` so the explanation can add future criteria without a table migration.

The JSON has four named components: `urgency`, `impact`, `risk`, and `duration`. Every component has exactly the common conceptual fields `value`, `source`, `confidence`, `reason`, and `points`. V1 uses `source: "LLM"`; `reason` describes the assessment; and duration's value is minutes while the other values use bounded categorical values.

An append-only history table was considered but deferred: V1 needs the current explainable ordering, not score analytics or audit history.

### Keep score calculation deterministic in the backend

The LLM returns only each component's value, confidence, and reason. The backend validates the task identifier, allowed values, confidence range, and response completeness; maps values to configured points; and calculates and persists the total score. The persisted JSON includes those backend-calculated points.

V1 uses a 100-point scale: urgency contributes 0/10/20/30 points for `LOW`/`MEDIUM`/`HIGH`/`CRITICAL`; impact contributes 0/15/30 for `LOW`/`MEDIUM`/`HIGH`; risk contributes 0/12/25 for `LOW`/`MEDIUM`/`HIGH`; and duration contributes 15/10/5/0 for `<=15`, `16-30`, `31-60`, and `>60` estimated minutes. The maximum score is therefore 100.

Letting the LLM provide points or the total was rejected because it would make ordering non-deterministic and prevent reliable testing or later tuning of weights.

### Use Spring AI for LLM communication

The evaluation service uses Spring AI's OpenAI model client and structured-output support to issue batch prompts and deserialize the response into the internal batch-result contract. The application configures `spring.ai.openai.api-key=${OPENAI_API_KEY}` and an overridable OpenAI chat model through Spring properties; the key is never committed to the repository.

Calling a provider SDK directly was rejected because it would duplicate provider integration concerns already addressed by Spring AI and make later model changes harder.

### Treat the evaluation as asynchronous derived data

Creating a non-recurring `TODO` does not create an evaluation immediately. Updating an eligible `TODO` removes its current evaluation. Updating a task from `TODO` to `MEETING`, completing it, or deleting it also removes the evaluation. A `MEETING` changed to an eligible `TODO` has no evaluation until the worker processes it.

The worker selects incomplete, non-recurring `TODO` tasks with no evaluation and submits them in batches of up to ten in a single structured LLM request. The request and response identify every task by UUID. Before persistence, the worker confirms that each task still exists, remains eligible, and has not changed since the batch was built; stale results are discarded and the now-missing evaluation remains eligible for a later batch.

Batching reduces request overhead while limiting prompt size and the blast radius of a malformed response. A failed request or malformed batch envelope writes no evaluations for that batch and leaves its tasks missing for a future retry. When an individual result is missing, invalid, or stale, the worker skips only that task while persisting other validated current results; skipped tasks remain missing for later retry. A smaller configurable batch size can be used if a provider's context or rate limit requires it.

### Keep V1 scores as snapshots

V1 deliberately has no aging component and no scheduled refresh of existing evaluations. Urgency is assessed from the task's wording and due date at evaluation time; it does not automatically increase simply because time passes. An evaluation is refreshed only after task invalidation followed by background reevaluation.

This avoids daily LLM cost and score churn. A future version can add aging or time-sensitive urgency with explicit refresh scheduling and evaluation versioning.

### Expose evaluation separately from the task mutation contract

The evaluation is a derived resource, retrieved through a dedicated task-evaluation read contract rather than being required in every existing task DTO. Existing task creation and update clients stay compatible, while a priority-aware view can request evaluations explicitly.

## Risks / Trade-offs

- [Evaluation is eventually consistent] → The UI/API must represent a missing evaluation rather than treating it as score zero; scheduled retries populate it later.
- [LLM output is malformed, incomplete, or semantically unreliable] → Require Spring AI structured output, server-side schema/range validation, and task-ID matching; reject a malformed envelope and skip invalid individual results.
- [A task changes while its batch is in flight] → Compare the task state or update timestamp captured at batch selection before writing the result, and discard stale output.
- [Task content can contain sensitive or adversarial text] → Send only the minimum task context required, treat it as untrusted data, and select an LLM provider/configuration with acceptable data handling before deployment.
- [Urgency can become stale as a due date approaches] → This is an accepted V1 limitation; add explicit refresh rules only when dynamic urgency is introduced.
- [Global scans grow with the task count] → Query only eligible tasks without evaluations and index the eligibility query; retain a bounded batch size.

## Migration Plan

1. Deploy the Flyway migration that creates the evaluation table, unique task foreign key, and indexes.
2. Deploy application support with the scheduler disabled or configured conservatively until LLM credentials and output validation are verified.
3. Enable the scheduler to backfill currently eligible tasks in bounded batches.
4. Roll back application behavior by disabling the scheduler. The derived table can remain safely; a later migration can remove it if rollback requires full schema reversal.

## Open Questions

- Define the API representation and authorization behavior for retrieving a missing or existing evaluation.
- Confirm the default OpenAI model, data-retention controls, retry/backoff policy, and scheduler cadence.
