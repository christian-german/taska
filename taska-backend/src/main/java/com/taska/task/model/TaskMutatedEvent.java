package com.taska.task.model;

import java.util.UUID;

/**
 * Signals that a stored task definition was written or removed.
 *
 * <p>
 * Anything derived from a task's content — today its computed priority evaluation — is stale once this fires. Listeners run inside the publishing
 * transaction, so a derived row is purged before the mutation commits, exactly as an inline call would have done.
 *
 * @param taskId stored task or recurring-series identifier
 */
public record TaskMutatedEvent(UUID taskId) {
}
