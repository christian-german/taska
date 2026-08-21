## Why

The Android mobile application's date-picker calendars currently inherit a locale whose week begins on Sunday. The mobile calendar should present weeks in the expected Monday-through-Sunday order.

## What Changes

- Configure every task date-picker calendar in the Android application with a Monday-first locale.
- Apply the same ordering to calendars opened while creating a task and while editing scheduled or due dates.
- Preserve date selection, displayed dates, task scheduling, and all non-calendar behavior.

## Capabilities

### New Capabilities

- `android-mobile-date-picker`: Defines consistent Monday-first calendar ordering for Android task date pickers.

### Modified Capabilities

None.

## Impact

- Android task creation and task detail date-picker state configuration.
- Focused unit coverage for the shared calendar locale.
- No backend, web, widget, task storage, or date conversion changes.
