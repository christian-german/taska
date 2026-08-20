## Context

Both Android widgets send completion actions to `TaskWidgetCompletionReceiver`, which performs the authenticated request asynchronously. The checked state is currently derived only during a later data refresh. The Week widget then removes successfully completed tasks, while the Today widget retains and renders completed tasks. A rejected request causes no optimistic state today.

The requested behavior is specifically immediate visual acknowledgement of an incomplete task's completion tap, with reversal if the call fails. Completed-task reopening in the Today widget is outside that request and remains unchanged.

## Goals / Non-Goals

**Goals:**

- Render the invoked incomplete task's completion control as checked before waiting for the network response.
- Preserve the exact task and recurring-occurrence identity used by the completion request.
- Reconcile successful completion through the existing refresh behavior.
- Restore the invoked control to unchecked when initialization, authentication, transport, or API processing fails.
- Cover both Week and Today widget renderers.

**Non-Goals:**

- Change the task title styling before the response arrives.
- Optimistically remove or reorder a Week-widget row.
- Make reopening a completed Today task optimistic.
- Add notifications, retry UI, offline completion queues, or API changes.
- Change completion behavior outside Android home-screen widgets.

## Decisions

### Update only the invoked completion presentation optimistically

The completion receiver will receive enough widget and row identity to update the control that initiated the action. It will render that control with the existing checked completion asset before starting the authenticated request. The row remains visible and otherwise unchanged until normal refresh reconciliation, avoiding speculative changes to grouping, ordering, counts, or title styling.

### Treat the server response as authoritative

On success, the existing repository-driven widget refresh remains responsible for the final presentation. This removes the completed row from the Week widget and renders authoritative completed data in the Today widget. The optimistic update does not alter cached task data.

### Roll back every failed completion path

If application/API initialization or the completion request throws, the receiver will update the same control back to the existing unchecked asset. Failure rollback must not replace the widget's task content with an error or require a successful network refresh, because the original incomplete row remains the last authoritative local presentation.

### Keep reopen behavior unchanged

The Today widget's checked control can reopen an already-completed task. This change does not provide an optimistic unchecked state for that action; its current request and refresh behavior remains intact.

## Risks / Trade-offs

- [A launcher delivers an action after widget content has changed] → Carry widget/row identity with the action and limit the update to the initiating widget instance; subsequent authoritative refreshes still reconcile the entire widget.
- [A completion request is slow] → Retain the checked control for the duration, which is the intended immediate feedback, without speculatively removing the row.
- [A request fails after optimistic rendering] → Roll back directly from the failure path without depending on another network call.
- [Week and Today widgets use different `RemoteViews` structures] → Add focused coverage for both the collection row and fixed Today row paths.

## Migration Plan

1. Include initiating widget and row identity in incomplete-task completion actions.
2. Apply the checked asset synchronously when handling a valid completion action.
3. Preserve successful refresh reconciliation and add direct failure rollback.
4. Verify non-recurring and recurring completion in both widgets and unchanged Today reopen behavior.

No data migration is required. Rollback restores response-driven rendering.

## Open Questions

- None.
