package com.taska.domain.priority;

import com.taska.domain.priority.service.PriorityAssessmentValidator;
import com.taska.domain.priority.service.PriorityEvaluationBatchResponse;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PriorityAssessmentValidatorTest {
    private final PriorityAssessmentValidator priorityAssessmentValidator = new PriorityAssessmentValidator();
    private final UUID id = UUID.randomUUID();

    @Test
    void validAssessmentIsAccepted() {
        assertThat(priorityAssessmentValidator.isValid(assessment(id))).isTrue();
    }

    @Test
    void duplicateEnvelopeIdsAreRejected() {
        var response = new PriorityEvaluationBatchResponse(List.of(assessment(id), assessment(id)));
        assertThatThrownBy(() -> priorityAssessmentValidator.validateEnvelope(response, Set.of(id))).isInstanceOf(IllegalArgumentException.class);
    }

    private PriorityEvaluationBatchResponse.Assessment assessment(UUID taskId) {
        return new PriorityEvaluationBatchResponse.Assessment(taskId, PriorityLevel.HIGH, PriorityLevel.HIGH, PriorityLevel.MEDIUM, 20,
                .9, .8, .7, .6, "deadline", "benefit", "consequence", "short");
    }
}
