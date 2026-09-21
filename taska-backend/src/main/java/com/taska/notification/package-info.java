/**
 * Scheduled push notifications for upcoming tasks, and device registration.
 *
 * <p>
 * Sweeps the task model on a timer and reacts to {@link com.taska.task.model.TaskChangedEvent} and
 * {@link com.taska.task.model.TaskOccurrenceRescheduledEvent}. The task module publishes those without knowing this module exists.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Notification", allowedDependencies = {"platform :: config", "platform :: exception",
        "task :: model", "task :: persistence", "task :: recurrence"})
package com.taska.notification;
