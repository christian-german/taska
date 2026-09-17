# task-notification-scheduling Specification

## Purpose
TBD - created by archiving change fix-recurring-notifications-and-reopen. Update Purpose after archive.
## Requirements
### Requirement: Scheduled notifications treat recurring occurrences independently

The system SHALL treat each open recurring occurrence as an independent notification target identified by its backing series ID and `occurrenceScheduledAt`. An eligible occurrence SHALL be notified using its effective content and effective `scheduledAt`, after applying persisted occurrence overrides, without changing whether the occurrence is virtual or materialized.

#### Scenario: Virtual recurring occurrence becomes eligible
- **WHEN** a non-all-day virtual occurrence has an effective `scheduledAt` within the scheduler's upcoming notification window
- **THEN** the system SHALL dispatch its notification using the occurrence's effective task values
- **AND** it SHALL NOT create or modify occurrence business state solely because of the notification

#### Scenario: Modified occurrence uses its effective values
- **WHEN** an open modified occurrence has content or scheduling overrides and its effective `scheduledAt` enters the upcoming notification window
- **THEN** the system SHALL dispatch the notification using the resolved occurrence content and effective schedule

#### Scenario: Completed or skipped occurrence is not eligible
- **WHEN** a recurring occurrence is completed or skipped
- **THEN** the notification scheduler SHALL NOT dispatch an upcoming-task notification for that occurrence

#### Scenario: All-day recurring occurrence is not eligible
- **WHEN** a recurring series is all-day
- **THEN** none of its occurrences SHALL be eligible for scheduled-time notifications

### Requirement: Each recurring occurrence is dispatched at most once

The system SHALL persist recurring-occurrence notification delivery identity separately from the recurring series and occurrence business state. A scheduler run SHALL dispatch at most one notification batch for a given `(taskId, occurrenceScheduledAt)`, including when the occurrence appears in multiple scheduler runs or concurrent executions.

#### Scenario: Scheduler sees the same occurrence again
- **WHEN** an occurrence has already been claimed for notification and a later scheduler run selects the same occurrence
- **THEN** the system SHALL NOT dispatch another notification batch for it

#### Scenario: Scheduler executions overlap
- **WHEN** concurrent scheduler executions attempt to claim the same occurrence
- **THEN** at most one execution SHALL dispatch notifications for that occurrence

#### Scenario: No device token exists
- **WHEN** an occurrence enters the notification window but no device token is registered
- **THEN** the system SHALL NOT record the occurrence as notified
- **AND** a later scheduler run MAY dispatch it if it is still eligible and a device token exists

### Requirement: Rescheduling restores notification eligibility

Changing an occurrence's effective `scheduledAt` SHALL clear any prior notification marker for that stable occurrence identity when the value actually changes. Completing or reopening an occurrence without changing its effective schedule SHALL NOT clear that marker.

#### Scenario: Notified occurrence is moved
- **WHEN** an occurrence that was previously notified is assigned a different effective `scheduledAt`
- **THEN** its prior notification marker SHALL be removed
- **AND** it SHALL be eligible for one notification at the replacement schedule

#### Scenario: Completed occurrence is reopened
- **WHEN** a notified completed occurrence is reopened without changing its effective `scheduledAt`
- **THEN** its notification marker SHALL remain recorded
- **AND** reopening SHALL NOT cause the same scheduled occurrence to be notified again

### Requirement: Non-recurring notification behavior is preserved

The scheduler SHALL retain the existing eligibility, dispatch, and `isNotified` behavior for non-recurring tasks while sharing the same notification-dispatch path with recurring occurrence candidates.

#### Scenario: Non-recurring task becomes eligible
- **WHEN** an incomplete, non-notified, non-all-day task reaches the existing scheduled notification cutoff
- **THEN** the system SHALL dispatch its notification and mark that task as notified according to the existing behavior

