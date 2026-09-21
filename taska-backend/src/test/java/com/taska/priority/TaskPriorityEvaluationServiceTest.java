package com.taska.priority;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.taska.priority.application.PriorityAssessmentClient;
import com.taska.priority.application.PriorityAssessmentValidator;
import com.taska.priority.application.TaskPriorityEvaluationService;
import com.taska.priority.application.TaskPriorityScorer;
import com.taska.priority.model.PriorityAssessmentBatchResult;
import com.taska.priority.model.PriorityLevel;
import com.taska.priority.model.TaskPriorityEvaluation;
import com.taska.priority.persistence.TaskPriorityEvaluationRepository;
import com.taska.task.model.Task;
import com.taska.task.model.TaskType;
import com.taska.task.persistence.TaskRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class TaskPriorityEvaluationServiceTest {

    @Mock
    TaskRepository taskRepository;
    @Mock
    TaskPriorityEvaluationRepository evaluationRepository;
    @Mock
    PriorityAssessmentClient assessmentClient;
    @Spy
    PriorityAssessmentValidator priorityAssessmentValidator = new PriorityAssessmentValidator();
    @Spy
    TaskPriorityScorer taskPriorityScorer = new TaskPriorityScorer();
    @Spy
    JsonMapper objectMapper = new JsonMapper();
    @InjectMocks
    TaskPriorityEvaluationService taskPriorityEvaluationService;

    @Test
    void validAssessmentIsPersistedWithCalculatedScore() {
        Task task = task();
        var assessment = assessment(task.getId());
        when(assessmentClient.assess(any())).thenReturn(new PriorityAssessmentBatchResult(List.of(assessment)));
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        taskPriorityEvaluationService.evaluate(List.of(task));

        var saved = ArgumentCaptor.forClass(TaskPriorityEvaluation.class);
        verify(evaluationRepository).save(saved.capture());
        assertThat(saved.getValue().getScore()).isEqualTo(95);
        assertThat(saved.getValue().getComponents().get("impact").get("source").asString()).isEqualTo("LLM");
    }

    @Test
    void invalidEnvelopeDoesNotPersistAnyResult() {
        Task task = task();
        when(assessmentClient.assess(any())).thenReturn(new PriorityAssessmentBatchResult(List.of(assessment(UUID.randomUUID()))));

        assertThatThrownBy(() -> taskPriorityEvaluationService.evaluate(List.of(task))).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(evaluationRepository);
    }

    @Test
    void missingIndividualResultDoesNotPreventValidPeerPersistence() {
        Task first = task(), second = task();
        when(assessmentClient.assess(any())).thenReturn(new PriorityAssessmentBatchResult(List.of(assessment(first.getId()))));
        when(taskRepository.findById(first.getId())).thenReturn(Optional.of(first));
        taskPriorityEvaluationService.evaluate(List.of(first, second));
        verify(evaluationRepository).save(any());
        verify(taskRepository, never()).findById(second.getId());
    }

    @Test
    void providerFailureDoesNotPersistResults() {
        when(assessmentClient.assess(any())).thenThrow(new IllegalStateException("provider unavailable"));
        assertThatThrownBy(() -> taskPriorityEvaluationService.evaluate(List.of(task()))).isInstanceOf(IllegalStateException.class);
        verifyNoInteractions(evaluationRepository);
    }

    @Test
    void staleTaskDoesNotPersistResult() {
        Task selected = task();
        Task current = task();
        current.setId(selected.getId());
        current.setUpdatedAt(selected.getUpdatedAt().plusSeconds(1));
        when(assessmentClient.assess(any())).thenReturn(new PriorityAssessmentBatchResult(List.of(assessment(selected.getId()))));
        when(taskRepository.findById(selected.getId())).thenReturn(Optional.of(current));
        taskPriorityEvaluationService.evaluate(List.of(selected));
        verifyNoInteractions(evaluationRepository);
    }

    @Test
    void appointmentTaskDoesNotPersistResult() {
        Task selected = task(), appointment = task();
        appointment.setId(selected.getId());
        appointment.setUpdatedAt(selected.getUpdatedAt());
        appointment.setType(TaskType.APPOINTMENT);
        when(assessmentClient.assess(any())).thenReturn(new PriorityAssessmentBatchResult(List.of(assessment(selected.getId()))));
        when(taskRepository.findById(selected.getId())).thenReturn(Optional.of(appointment));

        taskPriorityEvaluationService.evaluate(List.of(selected));

        verifyNoInteractions(evaluationRepository);
    }

    private Task task() {
        Task task = new Task();
        task.setId(UUID.randomUUID());
        task.setContent("Call doctor");
        task.setType(TaskType.TODO);
        task.setIsCompleted(false);
        task.setIsRecurring(false);
        task.setUpdatedAt(Instant.now());
        task.setCreatedAt(Instant.now());
        return task;
    }

    private PriorityAssessmentBatchResult.Assessment assessment(UUID id) {
        return new PriorityAssessmentBatchResult.Assessment(
                id,
                PriorityLevel.CRITICAL,
                PriorityLevel.HIGH,
                PriorityLevel.HIGH,
                20,
                .9,
                .9,
                .9,
                .9,
                "soon",
                "important",
                "harm",
                "quick");
    }
}
