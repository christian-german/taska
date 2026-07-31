package com.taska.domain.priority;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PriorityAssessmentValidatorTest {
    private final PriorityAssessmentValidator validator = new PriorityAssessmentValidator();
    private final UUID id = UUID.randomUUID();

    @Test void validAssessmentIsAccepted() { assertThat(validator.isValid(assessment(id))).isTrue(); }

    @Test void duplicateEnvelopeIdsAreRejected() {
        var response = new PriorityEvaluationBatchResponse(List.of(assessment(id), assessment(id)));
        assertThatThrownBy(() -> validator.validateEnvelope(response, Set.of(id))).isInstanceOf(IllegalArgumentException.class);
    }

    private PriorityEvaluationBatchResponse.Assessment assessment(UUID taskId) {
        return new PriorityEvaluationBatchResponse.Assessment(taskId, PriorityLevel.HIGH, PriorityLevel.HIGH, PriorityLevel.MEDIUM, 20,
                .9, .8, .7, .6, "deadline", "benefit", "consequence", "short");
    }
}
