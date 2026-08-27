## Context

Material 3 date pickers derive weekday ordering from the locale supplied to their state. The affected Android application surfaces create independent date-picker states for task creation, scheduled-date editing, and due-date editing.

## Goals / Non-Goals

**Goals:**
- Make Monday the first visible weekday in every task date-picker calendar.
- Keep the configuration consistent across creation and detail screens.
- Make the locale decision directly testable.

**Non-Goals:**
- Change application language, date formatting, stored timestamps, week-view navigation, or widget behavior.
- Change non-calendar shortcut or time-picker behavior.

## Decisions

- Define one shared date-picker locale whose calendar metadata starts weeks on Monday, and pass it explicitly to each `rememberDatePickerState` call. This avoids dependence on the device locale and prevents the affected surfaces from diverging.
- Test the shared configuration through the Java calendar metadata consumed by locale-aware calendar implementations.

## Risks / Trade-offs

- A fixed Monday-first locale also governs Material date-picker localized labels and formatting. The application already presents its mobile UI in French, so a French Monday-first locale is consistent with the existing interface while satisfying the required weekday order.
