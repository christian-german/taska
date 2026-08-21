## Context

`WeekWidgetItems.taskText` currently formats the task instant with `DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)`, so its output varies by locale. `TaskWidgetRefresh`, which renders the Today widget, uses the explicit `HH:mm` pattern.

## Goals / Non-Goals

**Goals:**
- Make calendar-week widget times consistently use zero-padded 24-hour clock text.
- Continue converting UTC instants to the device time zone before display.
- Verify that American and European locales produce identical clock text.

**Non-Goals:**
- Changing date headers or Today widget output.
- Changing task selection, ordering, or interactions.
- Introducing user-configurable clock preferences.

## Decisions

Use `DateTimeFormatter.ofPattern("HH:mm")` in the calendar-week task text formatter. An explicit pattern makes the observable format locale-independent and matches the Today widget implementation. Retain the injectable `ZoneId` used by tests and production.

## Risks / Trade-offs

- The widget intentionally ignores a user's 12/24-hour system preference. This is required by the issue's request for European format and matches the Today widget.
