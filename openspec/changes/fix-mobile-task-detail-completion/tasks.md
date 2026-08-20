## 1. Task-detail completion behavior

- [x] 1.1 Add a task-detail view-model action that closes the displayed active task or reopens the displayed completed task through the existing repository operations.
- [x] 1.2 Pass the displayed occurrence identity to close/reopen, adopt the successful response, retain the prior task on failure, and prevent duplicate mutations while one is pending.

## 2. Task-detail completion control

- [x] 2.1 Make the title-adjacent control invoke the view-model action and render the displayed task's active or completed state.
- [x] 2.2 Give the control state-appropriate accessibility semantics and show the established completed-task title treatment without changing title editing or navigation.

## 3. Verification

- [x] 3.1 Add focused Android view-model tests for close and reopen success, failure, duplicate taps, and recurring-occurrence targeting.
- [x] 3.2 Add Android UI tests for interaction, checked/unchecked and title presentation, and accessibility semantics.
- [ ] 3.3 Run relevant Android tests and static checks, then run strict OpenSpec validation for this change.
