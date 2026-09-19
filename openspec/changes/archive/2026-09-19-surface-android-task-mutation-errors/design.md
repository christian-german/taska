## Context

`TaskDetailViewModel` exposes load failures through its UI state, but most mutation paths catch and
discard exceptions. A rejected recurrence-rule replacement therefore produces neither state nor UI
feedback. Retrofit's default exception message also omits the actionable `detail` returned by the
backend's RFC 9457-style Problem Details response.

The loaded task must remain visible after a mutation failure: the user may need its occurrence
context to understand or correct the edit.

## Goals / Non-Goals

**Goals:**

- Preserve task-detail content when a mutation fails.
- Display one transient, accessible error message for the failed mutation.
- Prefer the backend Problem Details `detail`, with a stable fallback when it cannot be decoded.
- Cover the recurrence-rule rejection and shared decoding behavior with automated tests.

**Non-Goals:**

- Change recurrence-rule routing or automatically choose a following-series boundary.
- Translate backend validation messages in this change.
- Redesign loading-error presentation or every Android screen's error handling.

## Decisions

### Keep load and mutation errors separate

Add a consumable mutation-error value alongside the existing load `error`. The Compose screen keeps
rendering task content and presents mutation failures through a Material snackbar. Reusing the
existing load error was rejected because its current rendering branch replaces the whole detail
screen.

### Decode Problem Details at the Android API boundary

Use a small shared Android error-message helper that recognizes Retrofit `HttpException`, consumes
its error body once, and reads the optional JSON `detail`. Empty, malformed, non-HTTP, and bodyless
errors fall back to the exception message and finally to a stable French generic message. This avoids
matching backend message strings or coupling the view model to JSON parsing.

### Consume feedback explicitly

The screen clears the mutation error as it starts displaying the snackbar. This makes the state an
explicit one-shot event and prevents unrelated recompositions from replaying the same failure.

## Risks / Trade-offs

- [Backend details are currently English] → Showing the authoritative detail remains more actionable
  than `HTTP 400`; localization can later use stable problem types or codes.
- [Reading an HTTP error body is destructive] → Decoding occurs once in the shared helper and returns
  a plain string to the UI layer.
- [Other screens still swallow some mutations] → This change is deliberately scoped to task detail;
  the helper can be reused in later cleanups.
