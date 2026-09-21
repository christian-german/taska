package com.taska.task;

import static com.taska.task.TaskMutationFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.taska.planningcalendar.application.PlanningCalendarService;
import com.taska.platform.config.TaskaProperties;
import com.taska.project.application.ProjectService;
import com.taska.task.application.definition.TaskDefinitionService;
import com.taska.task.application.occurrence.RecurringTaskSeriesService;
import com.taska.task.application.occurrence.TaskOccurrenceService;
import com.taska.task.application.recurrence.TaskRecurrenceService;
import com.taska.task.model.*;
import com.taska.task.model.Task;
import com.taska.task.persistence.TaskOccurrenceStateRepository;
import com.taska.task.persistence.TaskRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Series stopping validates the cut and preserves history without creating another series.
 *
 * <p>
 * All repository calls are mocked.
 */
@ExtendWith(MockitoExtension.class)
class RecurringTaskSeriesServiceTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private TaskOccurrenceStateRepository taskOccurrenceStateRepository;
    @Mock
    private TaskRecurrenceService taskRecurrenceService;
    @Mock
    private ProjectService projectService;
    @Mock
    private ApplicationEventPublisher events;
    @Mock
    private PlanningCalendarService planningCalendarService;
    @Mock
    private TaskaProperties taskaProperties;

    private TaskDefinitionService taskService;
    private TaskOccurrenceService taskOccurrenceService;
    private RecurringTaskSeriesService recurringTaskSeriesService;

    @BeforeEach
    void createServicesUnderTest() {
        taskService = new TaskDefinitionService(taskRepository, projectService, events, planningCalendarService);

        taskOccurrenceService = new TaskOccurrenceService(
                taskService,
                taskRepository,
                taskOccurrenceStateRepository,
                new TaskRecurrenceService(),
                taskaProperties,
                events);

        recurringTaskSeriesService = new RecurringTaskSeriesService(taskService, taskOccurrenceService, taskRepository);
    }

    @Test
    void stoppingPreservesMovedAndCompletedHistoryWithoutCreatingASuccessor() {
        UUID id = randomId();
        Task series = buildRecurringTask(id);
        Instant cut = Instant.parse("2026-05-20T10:00:00Z");
        var moved = buildOccurrenceState(id, cut, TaskOccurrenceStatus.MODIFIED);
        moved.setScheduledAt(cut.plusSeconds(3600));
        var done = buildOccurrenceState(id, cut.plusSeconds(86400), TaskOccurrenceStatus.DONE);
        var skipped = buildOccurrenceState(id, cut.plusSeconds(172800), TaskOccurrenceStatus.SKIPPED);
        when(taskRepository.findById(id)).thenReturn(Optional.of(series));
        when(taskOccurrenceStateRepository.findBySeriesIdAndOccurrenceScheduledAtGreaterThanEqual(id, cut)).thenReturn(List.of(moved, done, skipped));

        recurringTaskSeriesService.truncateSeriesFrom(id, cut);

        assertThat(series.getRruleEndsAt()).isEqualTo(cut.minusSeconds(1));
        assertThat(moved.isDetached()).isTrue();
        assertThat(done.isDetached()).isTrue();
        assertThat(moved.getScheduledAt()).isEqualTo(cut.plusSeconds(3600));
        verify(taskOccurrenceStateRepository).delete(skipped);
        verify(taskRepository).save(series);
        verify(taskRepository, times(1)).save(any());
        verify(taskRepository, never()).delete(any());
    }

    @Test
    void stoppingRejectsAnInvalidIdentityWithoutMutations() {
        UUID id = randomId();
        Task series = buildRecurringTask(id);
        when(taskRepository.findById(id)).thenReturn(Optional.of(series));

        assertThatThrownBy(() -> recurringTaskSeriesService.truncateSeriesFrom(id, Instant.parse("2026-05-20T11:00:00Z")))
                .isInstanceOf(com.taska.platform.exception.ResourceNotFoundException.class);
        assertThat(series.getRruleEndsAt()).isNull();
        verify(taskRepository, never()).save(any());
        verifyNoInteractions(taskOccurrenceStateRepository);
    }

    @Test
    void stoppingCannotExtendAnExistingEnd() {
        UUID id = randomId();
        Task series = buildRecurringTask(id);
        Instant end = Instant.parse("2026-05-20T09:59:59Z");
        series.setRruleEndsAt(end);
        when(taskRepository.findById(id)).thenReturn(Optional.of(series));

        assertThatThrownBy(() -> recurringTaskSeriesService.truncateSeriesFrom(id, Instant.parse("2026-05-21T10:00:00Z")))
                .isInstanceOf(com.taska.platform.exception.ResourceNotFoundException.class);
        assertThat(series.getRruleEndsAt()).isEqualTo(end);
        verify(taskRepository, never()).save(any());
    }

    @Test
    void stoppingAtFirstOccurrenceProducesNoFutureOccurrences() {
        UUID id = randomId();
        Task series = buildRecurringTask(id);
        when(taskRepository.findById(id)).thenReturn(Optional.of(series));

        recurringTaskSeriesService.truncateSeriesFrom(id, series.getScheduledAt());

        assertThat(new TaskRecurrenceService().getOccurrencesInRange(series, series.getScheduledAt(), series.getScheduledAt().plusSeconds(86400)))
                .isEmpty();
    }
}
