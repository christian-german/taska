package com.taska.domain.priority;

import com.taska.domain.task.Task;
import com.taska.domain.task.TaskRepository;
import com.taska.domain.task.TaskType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskPriorityEvaluationServiceTest {
    @Mock TaskRepository taskRepository;
    @Mock TaskPriorityEvaluationRepository evaluationRepository;
    @Mock OpenAiPriorityAssessmentClient assessmentClient;
    @Spy PriorityAssessmentValidator validator = new PriorityAssessmentValidator();
    @Spy TaskPriorityScorer scorer = new TaskPriorityScorer();
    @Spy JsonMapper objectMapper = new JsonMapper();
    @InjectMocks TaskPriorityEvaluationService service;

    @Test void validAssessmentIsPersistedWithCalculatedScore() {
        Task task = task();
        var assessment = assessment(task.getId());
        when(assessmentClient.assess(any())).thenReturn(new PriorityEvaluationBatchResponse(List.of(assessment)));
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        service.evaluate(List.of(task));

        var saved = ArgumentCaptor.forClass(TaskPriorityEvaluation.class);
        verify(evaluationRepository).save(saved.capture());
        assertThat(saved.getValue().getScore()).isEqualTo(95);
        assertThat(saved.getValue().getComponents().get("impact").get("source").asText()).isEqualTo("LLM");
    }

    @Test void invalidEnvelopeDoesNotPersistAnyResult() {
        Task task = task();
        when(assessmentClient.assess(any())).thenReturn(new PriorityEvaluationBatchResponse(List.of(assessment(UUID.randomUUID()))));

        assertThatThrownBy(() -> service.evaluate(List.of(task))).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(evaluationRepository);
    }

    @Test void missingIndividualResultDoesNotPreventValidPeerPersistence() {
        Task first = task(), second = task();
        when(assessmentClient.assess(any())).thenReturn(new PriorityEvaluationBatchResponse(List.of(assessment(first.getId()))));
        when(taskRepository.findById(first.getId())).thenReturn(Optional.of(first));
        service.evaluate(List.of(first, second));
        verify(evaluationRepository).save(any());
        verify(taskRepository, never()).findById(second.getId());
    }

    @Test void providerFailureDoesNotPersistResults() {
        when(assessmentClient.assess(any())).thenThrow(new IllegalStateException("provider unavailable"));
        assertThatThrownBy(() -> service.evaluate(List.of(task()))).isInstanceOf(IllegalStateException.class);
        verifyNoInteractions(evaluationRepository);
    }

    @Test void staleTaskDoesNotPersistResult() {
        Task selected = task(), current = task(); current.setId(selected.getId()); current.setUpdatedAt(selected.getUpdatedAt().plusSeconds(1));
        when(assessmentClient.assess(any())).thenReturn(new PriorityEvaluationBatchResponse(List.of(assessment(selected.getId()))));
        when(taskRepository.findById(selected.getId())).thenReturn(Optional.of(current));
        service.evaluate(List.of(selected));
        verifyNoInteractions(evaluationRepository);
    }

    private Task task() {
        Task task = new Task(); task.setId(UUID.randomUUID()); task.setContent("Call doctor"); task.setType(TaskType.TODO);
        task.setIsCompleted(false); task.setIsRecurring(false); task.setUpdatedAt(Instant.now()); task.setCreatedAt(Instant.now()); return task;
    }
    private PriorityEvaluationBatchResponse.Assessment assessment(UUID id) {
        return new PriorityEvaluationBatchResponse.Assessment(id, PriorityLevel.CRITICAL, PriorityLevel.HIGH, PriorityLevel.HIGH, 20,
                .9, .9, .9, .9, "soon", "important", "harm", "quick");
    }
}
