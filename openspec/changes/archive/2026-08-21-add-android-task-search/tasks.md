## 1. Search data and state

- [x] 1.1 Add Android search state that retrieves tasks with completed tasks included and exposes loading, success, and failure outcomes.
- [x] 1.2 Filter results using a case-insensitive substring match against task `content` only, preserving repository order and excluding non-task result types.

## 2. Search experience

- [x] 2.1 Add a dedicated full-screen Android search destination with an accessible text input, back navigation, and loading, error, and no-match states.
- [x] 2.2 Present active and completed results using established Android task-row conventions, including completed styling.
- [x] 2.3 Open the existing task-detail experience when any search result is selected and preserve return navigation to search.

## 3. Primary-screen integration

- [x] 3.1 Add a consistently presented, accessibility-labelled search action to the top bar of Inbox, Today, Day, Week, Project, and Tracker.
- [x] 3.2 Connect every primary-screen search action to the single dedicated search destination without changing the existing bottom navigation or drawer.

## 4. Verification

- [x] 4.1 Add Android unit tests for case-insensitive content substring matching, completed-task inclusion, content-only matching, result ordering, and loading/failure/no-match state.
- [x] 4.2 Add Android UI or integration tests for every primary-screen entry point, search/back navigation, accessible controls, completed result presentation, and opening task details.
- [x] 4.3 Run relevant Android tests and static checks plus strict OpenSpec validation.
