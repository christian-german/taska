## MODIFIED Requirements

### Requirement: Widget task list
The system SHALL display scheduled incomplete tasks in a compact list within the widget. Each task SHALL display its scheduled time converted to the device time zone using a zero-padded 24-hour `HH:mm` clock, followed by the task title. The widget SHALL include tasks scheduled from the start of the current local day through the end of the seventh local calendar day, inclusive, and SHALL exclude tasks without a scheduled date or tasks whose effective occurrence is completed.

#### Scenario: American locale uses European time format
- **WHEN** a task scheduled for 1:05 PM in the device time zone is rendered in the calendar-week widget while the device uses an American locale
- **THEN** the task row displays `13:05` before the task title
- **AND** the task row does not display an AM or PM marker

#### Scenario: European locale uses the same time format
- **WHEN** the same task is rendered while the device uses a European locale
- **THEN** the task row displays `13:05` before the task title

#### Scenario: Scheduled instant is converted to device time zone
- **WHEN** the widget renders a scheduled task
- **THEN** the displayed `HH:mm` value represents that instant in the device time zone
