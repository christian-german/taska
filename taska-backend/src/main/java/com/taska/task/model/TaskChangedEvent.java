package com.taska.task.model;

/**
 * Signals that an account's task list no longer matches what its devices hold.
 *
 * <p>
 * The task module knows that something changed and for whom; it does not know that anyone cares, nor how a device is reached. Publishing this instead
 * of calling a notification component keeps the dependency one-way: notification reads the task model, the task module never reads notification.
 *
 * @param accountSubject account whose task list changed
 */
public record TaskChangedEvent(String accountSubject) {
}
