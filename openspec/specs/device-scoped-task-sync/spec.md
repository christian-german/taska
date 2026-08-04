## Purpose

Define account-scoped Firebase task-change synchronization for Android devices.

## Requirements

### Requirement: Device tokens are registered to the authenticated account
The system SHALL associate every registered Android Firebase device token with the subject of the authenticated account that registered it. Re-registering an existing token SHALL update its account association. A token without an authenticated account association SHALL NOT receive task-change synchronization events.

#### Scenario: Register an authenticated device
- **WHEN** an authenticated Android client registers its Firebase device token
- **THEN** the system SHALL persist the token with that account's subject

#### Scenario: Token is registered by a different account
- **WHEN** an existing device token is registered by a different authenticated account
- **THEN** the system SHALL update the token's account association to the newly authenticated account

### Requirement: Task changes emit opaque account-targeted sync events
After an authenticated task create, update, delete, completion, or reopen succeeds, the system SHALL publish an opaque `tasks_changed` Firebase data message to registered Android device tokens associated with the account that performed the mutation. The event SHALL contain no task content, descriptions, or task identifiers and SHALL NOT be sent to tokens associated with other accounts.

#### Scenario: Task created from the web
- **WHEN** an authenticated account creates a task through the web client
- **THEN** the system SHALL send a `tasks_changed` event to that account's registered Android devices

#### Scenario: Completion from the Android widget
- **WHEN** an authenticated account completes a task from an Android widget
- **THEN** the system SHALL send a `tasks_changed` event to that account's registered Android devices

#### Scenario: Other account is not notified
- **WHEN** one authenticated account changes a task
- **THEN** the system SHALL NOT send the task-change event to a token registered by another account

#### Scenario: Invalid FCM token
- **WHEN** Firebase reports a target token as permanently invalid while sending a task-change event
- **THEN** the system SHALL remove or deactivate that token so it is not targeted by subsequent events
