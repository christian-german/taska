## Why

Calendar-week Android widget task rows use locale-dependent short-time formatting, which displays a 12-hour clock with AM/PM under American locales. The Today widget already presents scheduled times with a consistent European-style 24-hour clock, so the two widgets disagree.

## What Changes

- Display scheduled task times in the calendar-week Android widget using a zero-padded 24-hour `HH:mm` format.
- Keep the scheduled instant conversion in the device's local time zone.
- Preserve task content and all other calendar-week widget behavior.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `android-task-widgets`: Calendar-week task rows will use the same 24-hour time presentation as the Today widget, regardless of device locale.

## Impact

- Android calendar-week widget row formatting and focused unit tests.
- No changes to Today widget formatting, task storage, API representations, widget selection, sorting, actions, or non-widget screens.
