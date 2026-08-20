## Context

The native Android client hosts its primary task views in separate Inbox, Today, Day, Week, Project, and Tracker activities. Those views share a crowded five-control bottom navigation, while Inbox and projects are also reachable through a drawer. Search has no Android destination today.

The Android data layer already supports retrieving tasks with `show_completed=true`, and existing task UI supports completed styling and navigation to `TaskDetailActivity`. The browser/Tauri command palette uses a case-insensitive substring match on task content, but also mixes tasks with navigation, projects, and actions. The approved Android behavior is deliberately task-only.

## Goals / Non-Goals

**Goals:**

- Make search discoverable from every primary Android task screen without changing bottom navigation.
- Provide one dedicated full-screen search experience optimized for touch interaction.
- Find active and completed tasks by case-insensitive substring matching of task content.
- Preserve familiar task presentation and task-detail navigation.
- Provide clear loading, failure, and no-match feedback.

**Non-Goals:**

- Reproduce the browser/Tauri command palette's navigation, project, or action results on Android.
- Search task descriptions, labels, project names, or other metadata.
- Change browser/Tauri search, the backend API, persistence, synchronization, or task ordering semantics.
- Add search to the Android bottom navigation or project drawer.

## Decisions

### Launch one full-screen search from primary-screen top bars

Inbox, Today, Day, Week, Project, and Tracker will each expose a consistently presented and accessibility-labelled search action in their top bar. Activating it opens the same dedicated full-screen search destination. This keeps search visible without adding a sixth bottom-navigation control and avoids duplicating search behavior across activities.

The search screen provides standard back navigation to the screen from which it was opened. It is a focused task-search surface rather than a mobile copy of the desktop command palette.

### Retrieve the complete task set and filter task content locally

The search flow will retrieve tasks through the existing Android task repository with completed tasks included. It will compare the entered query with each task's `content` using a case-insensitive substring match. Results contain tasks only and retain the order supplied by the existing task retrieval path; search does not introduce a new relevance ranking.

Filtering locally matches the established GUI matching model and avoids an unnecessary backend endpoint. Loading, retrieval failure, and a completed search with no matching tasks are represented explicitly so the full-screen experience does not appear unresponsive or ambiguous.

### Reuse task presentation and details

Each result uses the established Android task-row conventions, including the visual treatment for completed tasks. Selecting either an active or completed result opens the existing task-detail activity for that task. Returning from details returns to the search flow rather than creating a separate detail behavior.

## Risks / Trade-offs

- [A complete task list may be large] → Reuse the existing retrieval contract and perform lightweight content filtering in Android; backend search remains a future cross-client decision if scale requires it.
- [Top-bar implementations can drift across separate activities] → Use shared search action/navigation integration where practical and test every primary task host.
- [Results can become stale while details are open] → Refresh through the search screen's normal lifecycle/data-loading path without changing synchronization semantics.
- [Android and desktop result types differ] → Keep the distinction explicit: matching semantics align, while Android remains task-only by product decision.

## Migration Plan

1. Add the dedicated Android search destination and its task-loading/filtering state.
2. Add the shared top-bar search action to every primary Android task screen.
3. Connect results to the existing task row and task-detail flow.
4. Verify active/completed matching, primary-screen entry points, states, accessibility, and navigation.
5. Roll back by removing the Android search destination and top-bar actions; no stored data or API migration is required.

## Open Questions

- None.
