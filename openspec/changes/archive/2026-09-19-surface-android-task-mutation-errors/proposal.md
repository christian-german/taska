## Why

Android task-detail mutations currently swallow request failures, so a rejected recurrence-rule
change leaves the screen unchanged without explaining what happened. Users need visible feedback
when the backend rejects an edit, while keeping the loaded task available for correction.

## What Changes

- Surface task-detail mutation failures as transient Android feedback without replacing the task
  detail content.
- Extract the backend Problem Details `detail` value for actionable HTTP failures, with a safe
  fallback for network or malformed error responses.
- Clear consumed mutation errors so the same failure is not shown again after recomposition.
- Keep recurrence mutation routing and backend series-history protection unchanged.

## Capabilities

### New Capabilities

- `android-task-mutation-feedback`: Defines visible, consumable error feedback for failed Android
  task-detail mutations.

### Modified Capabilities

None.

## Impact

- Android task-detail view model, Compose screen, API error decoding, and unit/UI tests.
- No backend, persistence, HTTP contract, or recurrence-routing change.
