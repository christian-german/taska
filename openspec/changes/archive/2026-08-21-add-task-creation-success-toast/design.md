## Context

Android creates tasks through its shared add-task bottom sheet, which is hosted by several activities. The Angular application creates tasks from multiple user-facing entry points and supplies the same frontend to browsers and Tauri desktop builds. Those flows already react to successful create responses to close an interface, publish a task event, or update nearby content, but they do not provide consistent transient success feedback.

The requested feedback is a client-side acknowledgement of a completed task-creation request. It is distinct from operating-system notifications, scheduled reminders, backend task-change messages, and existing inline error presentation.

## Goals / Non-Goals

**Goals:**

- Confirm successful, user-initiated task creation on Android, web, and Tauri desktop.
- Show feedback only after the create request has succeeded.
- Cover every user-facing task-creation entry point in each supported graphical client.
- Use a non-blocking, accessible toast that does not interrupt the user's next action.

**Non-Goals:**

- Change task validation, persistence, synchronization, refresh, navigation, or error handling.
- Add success feedback to backend, MCP, or other non-graphical API consumers.
- Turn the toast into an operating-system notification or scheduled reminder.
- Add success toasts for task updates, completion, reopening, or deletion.

## Decisions

### Emit feedback from successful client creation outcomes

Each graphical client will request the toast from the successful completion path of its user-facing create operations. A pending request does not qualify as success, and a rejected request retains its existing error behavior without a success toast. This preserves the server response as the source of truth and avoids false confirmation.

### Use one shared presentation mechanism per client

Android will use a shared Compose-level toast/snackbar presentation mechanism reachable from the add-task flow. Angular will use a shared application-level mechanism so browser and Tauri builds, as well as the frontend's different task-creation entry points, receive equivalent feedback without duplicating visual markup.

The implementation may use the platform-appropriate toast or snackbar primitive. In either case, it must be transient, non-modal, visually apparent, exposed to accessibility services as a status update, and must not require dismissal before work continues.

### Confirm each successful user-facing create operation

A success confirmation is associated with each completed user-facing create operation, including creation performed through alternate creation entry points rather than only the primary quick-add control. Internally created or remotely synchronized tasks do not produce a local toast because the user did not initiate a create operation in that client.

## Risks / Trade-offs

- [Several frontend entry points can drift] → Route successful create outcomes through a shared feedback mechanism and cover representative alternate entry points with tests.
- [Feedback could appear before persistence completes] → Trigger it only from the successful create response path.
- [A toast can obscure or block controls] → Use the platform's transient non-modal status presentation and verify continued interaction.
- [Desktop and browser behavior can diverge] → Keep the behavior in shared Angular code and verify both runtime configurations use it.

## Migration Plan

1. Add shared success-feedback presentation to Android and Angular application shells.
2. Connect successful user-facing create outcomes to that presentation.
3. Verify success, pending, and failure paths plus alternate creation entry points.
4. Roll back by removing the presentation hooks; no persisted data or API migration is required.

## Open Questions

- None.
