package com.taska.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.taska.notification.adapter.firebase.TaskNotificationSender;
import com.taska.notification.adapter.scheduler.TaskNotificationScheduler;
import com.taska.notification.application.TaskNotificationCandidateService;
import com.taska.notification.application.TaskOccurrenceNotificationService;
import com.taska.notification.model.DeviceToken;
import com.taska.notification.model.TaskNotificationCandidate;
import com.taska.notification.persistence.DeviceTokenRepository;
import com.taska.task.model.RecurringTaskOccurrenceResult;
import com.taska.task.model.Task;
import com.taska.task.persistence.TaskRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskNotificationSchedulerTest {

    @Mock
    private TaskNotificationCandidateService candidateService;
    @Mock
    private TaskOccurrenceNotificationService occurrenceNotificationService;
    @Mock
    private TaskNotificationSender notificationSender;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private DeviceTokenRepository deviceTokenRepository;
    @InjectMocks
    private TaskNotificationScheduler scheduler;

    @Test
    void recurringOccurrenceIsDispatchedOnceToEveryTokenAfterWinningItsClaim() {
        TaskNotificationCandidate candidate = recurringCandidate();
        when(candidateService.findEligible(any(), any())).thenReturn(List.of(candidate));
        when(deviceTokenRepository.findAll()).thenReturn(List.of(token("one"), token("two")));
        when(occurrenceNotificationService.claim(candidate.taskId(), candidate.occurrenceScheduledAt())).thenReturn(true);

        scheduler.checkUpcomingTasks();

        verify(notificationSender, times(2)).send(anyString(), same(candidate));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void recurringOccurrenceAlreadyClaimedByAnotherExecutionIsNotDispatchedAgain() {
        TaskNotificationCandidate candidate = recurringCandidate();
        when(candidateService.findEligible(any(), any())).thenReturn(List.of(candidate));
        when(deviceTokenRepository.findAll()).thenReturn(List.of(token("one")));
        when(occurrenceNotificationService.claim(candidate.taskId(), candidate.occurrenceScheduledAt())).thenReturn(false);

        scheduler.checkUpcomingTasks();

        verify(notificationSender, never()).send(anyString(), any());
    }

    @Test
    void noDeviceTokenLeavesTheOccurrenceUnclaimed() {
        TaskNotificationCandidate candidate = recurringCandidate();
        when(candidateService.findEligible(any(), any())).thenReturn(List.of(candidate));
        when(deviceTokenRepository.findAll()).thenReturn(List.of());

        scheduler.checkUpcomingTasks();

        verify(occurrenceNotificationService, never()).claim(any(), any());
        verify(notificationSender, never()).send(anyString(), any());
    }

    @Test
    void nonRecurringTaskKeepsItsExistingNotificationFlagBehavior() {
        Task task = new Task();
        task.setId(UUID.randomUUID());
        task.setContent("One-off task");
        task.setIsRecurring(false);
        task.setScheduledAt(Instant.parse("2026-05-20T10:10:00Z"));
        TaskNotificationCandidate candidate = TaskNotificationCandidate.nonRecurring(task);
        when(candidateService.findEligible(any(), any())).thenReturn(List.of(candidate));
        when(deviceTokenRepository.findAll()).thenReturn(List.of(token("one")));

        scheduler.checkUpcomingTasks();

        assertThat(task.getIsNotified()).isTrue();
        verify(notificationSender).send("one", candidate);
        verify(taskRepository).save(task);
        verify(occurrenceNotificationService, never()).claim(any(), any());
    }

    private TaskNotificationCandidate recurringCandidate() {
        Task task = new Task();
        task.setId(UUID.randomUUID());
        task.setContent("Recurring task");
        task.setIsRecurring(true);
        task.setScheduledAt(Instant.parse("2026-05-01T10:00:00Z"));
        Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:10:00Z");
        return TaskNotificationCandidate.recurring(new RecurringTaskOccurrenceResult(task, null, occurrenceScheduledAt));
    }

    private DeviceToken token(String value) {
        DeviceToken token = new DeviceToken();
        token.setToken(value);
        return token;
    }
}
