package com.taska.notification.adapter.scheduler;

import com.taska.notification.adapter.firebase.TaskNotificationSender;
import com.taska.notification.application.TaskNotificationCandidateService;
import com.taska.notification.application.TaskOccurrenceNotificationService;
import com.taska.notification.model.DeviceToken;
import com.taska.notification.model.TaskNotificationCandidate;
import com.taska.notification.persistence.DeviceTokenRepository;
import com.taska.task.persistence.TaskRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Scheduled adapter that dispatches upcoming task and recurring-occurrence notifications.
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "taska.notification.enabled", havingValue = "true", matchIfMissing = true)
public class TaskNotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(TaskNotificationScheduler.class);

    private final TaskNotificationCandidateService taskNotificationCandidateService;
    private final TaskOccurrenceNotificationService taskOccurrenceNotificationService;
    private final TaskNotificationSender taskNotificationSender;
    private final TaskRepository taskRepository;
    private final DeviceTokenRepository deviceTokenRepository;

    /**
     * Scheduled job that runs at the configured interval to identify tasks due in approximately 15 minutes and send Firebase Cloud Messaging push
     * notifications to all registered devices. Non-recurring tasks use their {@code isNotified} flag while recurring occurrences use an independent
     * claim marker. The job is a no-op when no device tokens are registered.
     */
    @Scheduled(fixedDelayString = "${taska.notification.scheduler-delay}")
    public void checkUpcomingTasks() {
        log.debug("Checking for upcoming tasks to notify");
        Instant windowStart = Instant.now();
        Instant windowEnd = windowStart.plus(15, ChronoUnit.MINUTES);
        log.debug("Checking tasks due between {} and {}", windowStart, windowEnd);
        List<TaskNotificationCandidate> candidates = taskNotificationCandidateService.findEligible(windowStart, windowEnd);
        log.debug("Found {} notification candidates", candidates.size());
        List<String> tokens = deviceTokenRepository.findAll().stream().map(DeviceToken::getToken).toList();
        log.debug("Found {} device tokens", tokens.size());

        // Do not consume a recurring occurrence claim when there is nowhere to deliver
        // it.
        if (tokens.isEmpty()) {
            return;
        }

        for (TaskNotificationCandidate candidate : candidates) {
            // The unique database claim elects a single run before any messages in the
            // batch are sent.
            if (candidate.recurringOccurrence() && !taskOccurrenceNotificationService.claim(candidate.taskId(), candidate.occurrenceScheduledAt())) {
                continue;
            }
            for (String token : tokens) {
                taskNotificationSender.send(token, candidate);
            }
            if (!candidate.recurringOccurrence()) {
                candidate.task().setIsNotified(true);
                taskRepository.save(candidate.task());
            }
        }
    }
}
