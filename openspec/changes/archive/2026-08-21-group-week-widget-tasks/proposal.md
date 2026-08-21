## Why

The Android calendar-week widget currently repeats a weekday abbreviation on every task row and limits the rendered list to eight tasks. This makes day boundaries difficult to scan and can hide part of a busy week.

## What Changes

- Group calendar-week widget tasks under centered date headers such as `Wed 17/06`.
- Show only the scheduled time and task title within each task row because the date is supplied by its group header.
- Make the weekly task area vertically scrollable when its date headers and task rows exceed the available widget height, so every task in the current week remains reachable.
- Leave the Today widget's presentation and task behavior unchanged.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `android-task-widgets`: The calendar-week widget will group tasks by local scheduled date and expose every weekly task through a vertically scrollable list.

## Impact

- Android calendar-week widget collection data, `RemoteViews` layouts, date/time formatting, and widget tests.
- The calendar-week widget will no longer enforce the current fixed eight-row display limit.
- No task API, filtering, sorting, completion, navigation, refresh, or Today-widget behavior changes.
