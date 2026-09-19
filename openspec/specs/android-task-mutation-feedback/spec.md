# android-task-mutation-feedback Specification

## Purpose

Define visible, transient Android feedback for failed task-detail mutations.

## Requirements

### Requirement: Android task detail surfaces mutation failures

The Android task-detail experience SHALL keep the loaded task visible when a mutation fails and SHALL present the failure as transient, accessible feedback. It SHALL prefer a non-blank Problem Details `detail` returned by the backend and SHALL use a safe fallback when no actionable detail is available. After presentation begins, the failure SHALL be consumed so unrelated recomposition does not display it repeatedly.

#### Scenario: Recurrence-rule mutation is rejected
- **WHEN** the backend rejects a recurrence-rule edit because the series has persisted occurrence state
- **THEN** Android SHALL keep the current task detail visible
- **AND** it SHALL display the backend Problem Details `detail`

#### Scenario: Mutation returns an undecodable failure
- **WHEN** a task-detail mutation fails without a non-blank decodable Problem Details `detail`
- **THEN** Android SHALL display an available exception message or a stable generic fallback

#### Scenario: Displayed mutation error is consumed
- **WHEN** the task-detail screen begins presenting a mutation failure
- **THEN** it SHALL consume that failure from UI state
- **AND** an unrelated recomposition SHALL NOT present the same failure again
