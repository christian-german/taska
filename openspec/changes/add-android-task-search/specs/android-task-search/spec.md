## ADDED Requirements

### Requirement: Search is available from every primary Android task screen

The Android client SHALL present a consistently identifiable and accessibility-labelled search action in the top bar of the Inbox, Today, Day, Week, Project, and Tracker screens. Activating any of these actions SHALL open the same dedicated full-screen task-search experience. The existing bottom navigation and project drawer SHALL remain unchanged.

#### Scenario: User launches search from a primary task screen

- **WHEN** a user activates the top-bar search action on Inbox, Today, Day, Week, Project, or Tracker
- **THEN** the Android client SHALL open the dedicated full-screen task-search experience
- **AND** the search input SHALL be available for text entry

#### Scenario: User returns from search

- **WHEN** a user invokes back navigation from the dedicated search experience
- **THEN** the Android client SHALL return to the primary task screen from which search was opened

### Requirement: Android search matches task content only

The Android client SHALL return task results whose `content` contains the entered query using a case-insensitive substring comparison. Search results SHALL contain tasks only and SHALL NOT match a task solely because the query occurs in its description, labels, project name, or other metadata. Matching results SHALL retain the order supplied by the existing task retrieval path.

#### Scenario: Content contains the query with different casing

- **GIVEN** a task has content `Prepare Quarterly Report`
- **WHEN** the user searches for `quarterly`
- **THEN** the Android client SHALL include that task in the results

#### Scenario: Query is a content substring

- **GIVEN** a task has content `Book dentist appointment`
- **WHEN** the user searches for `dentist`
- **THEN** the Android client SHALL include that task in the results

#### Scenario: Query appears only outside content

- **GIVEN** a task's content does not contain the query
- **AND** its description, label, or project name contains the query
- **WHEN** the user performs the search
- **THEN** the Android client SHALL NOT include that task on the basis of that metadata

#### Scenario: Search does not return command items

- **WHEN** matching tasks are displayed
- **THEN** the results SHALL NOT contain navigation destinations, projects, or application actions

### Requirement: Search includes active and completed tasks

The Android client SHALL search the task collection with completed tasks included and SHALL display both active and completed tasks that match the query. Completed results SHALL use the established Android completed-task presentation.

#### Scenario: Active and completed tasks both match

- **GIVEN** an active task and a completed task both contain the query in their content
- **WHEN** the user performs the search
- **THEN** both tasks SHALL appear in the results
- **AND** the completed task SHALL be visually identifiable as completed

### Requirement: Search communicates retrieval outcomes

The full-screen search experience SHALL communicate when tasks are loading, when task retrieval fails, and when a completed query has no matching tasks. A failed retrieval SHALL NOT be presented as a successful search with no matches.

#### Scenario: Tasks are loading

- **WHEN** the task retrieval request is pending
- **THEN** the search experience SHALL present a loading state

#### Scenario: Task retrieval fails

- **WHEN** the task retrieval request fails
- **THEN** the search experience SHALL present an error state distinct from the no-match state

#### Scenario: No task matches

- **GIVEN** task retrieval completed successfully
- **WHEN** no task content matches the entered query
- **THEN** the search experience SHALL present a no-match state

### Requirement: Search results open existing task details

Selecting an active or completed search result SHALL open the Android client's existing task-detail experience for that task. Back navigation from task details SHALL return to the search experience.

#### Scenario: User selects a search result

- **WHEN** a user selects an active or completed task in the search results
- **THEN** the Android client SHALL open the existing task-detail experience for the selected task

#### Scenario: User returns from task details

- **GIVEN** task details were opened from a search result
- **WHEN** the user invokes back navigation from task details
- **THEN** the Android client SHALL return to the search experience
