package com.taska.notification.adapter.events;

import com.taska.notification.adapter.firebase.TaskChangePublisher;
import com.taska.notification.application.TaskOccurrenceNotificationService;
import com.taska.task.model.TaskChangedEvent;
import com.taska.task.model.TaskOccurrenceRescheduledEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Inbound adapter turning task-module events into notification effects.
 *
 * <p>
 * Listeners are synchronous and run inside the publishing transaction, which preserves the ordering the previous direct calls had: a notification
 * claim is reset before the schedule change that invalidated it commits.
 */
@Component
@RequiredArgsConstructor
public class TaskChangeListener {

    private final TaskChangePublisher taskChangePublisher;
    private final TaskOccurrenceNotificationService taskOccurrenceNotificationService;

    /**
     * Invalidates the task list held by an account's devices.
     *
     * @param event account whose task list changed
     */
    @EventListener
    public void onTaskChanged(TaskChangedEvent event) {
        taskChangePublisher.publishFor(event.accountSubject());
    }

    /**
     * Restores notification eligibility for an occurrence that moved.
     *
     * @param event rescheduled occurrence identity
     */
    @EventListener
    public void onOccurrenceRescheduled(TaskOccurrenceRescheduledEvent event) {
        taskOccurrenceNotificationService.clear(event.seriesId(), event.occurrenceScheduledAt());
    }
}
