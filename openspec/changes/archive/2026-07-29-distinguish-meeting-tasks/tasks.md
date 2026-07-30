## 1. Task domain and persistence

- [x] 1.1 Add the closed `TODO`/`MEETING` task-type model and default missing values to `TODO` in domain construction and mapping.
- [x] 1.2 Add a backward-compatible persistence migration for task type, including a `TODO` backfill/default for existing tasks.
- [x] 1.3 Extend task create, update, and response transport contracts so task type is accepted, preserved, and returned.
- [x] 1.4 Add backend tests for explicit meeting creation, omitted-type creation, type changes, and legacy/defaulted task reads.

## 2. Task creation and editing

- [x] 2.1 Add a To-do/Meeting selector to task creation, defaulting to To-do.
- [x] 2.2 Add the same selector to task editing and initialize it from the task's persisted type.
- [x] 2.3 Wire the selected type through task create and update requests without overwriting unrelated task fields.
- [x] 2.4 Add client tests covering meeting creation and changing an existing task between the two types.

## 3. Task presentation

- [x] 3.1 Add a visible, non-color-only meeting marker and accessible label to task-list rows.
- [x] 3.2 Identify meeting tasks in the task-detail view while retaining existing to-do treatment for `TODO` tasks.
- [x] 3.3 Add presentation and accessibility tests for meeting indicators in list and detail views.
- [x] 3.4 Add the existing Today-view meeting icon to mobile daily and weekly task presentations, with the same accessible meeting label.
- [x] 3.5 Add the existing web meeting identifier to tracker task presentation and cover mobile daily/weekly and tracker indicators with tests.

## 4. Verification

- [x] 4.1 Run the relevant backend migration, API, and domain test suites.
- [x] 4.2 Run the supported-client test suites and manually verify an existing task appears and behaves as a To-do after upgrade.
