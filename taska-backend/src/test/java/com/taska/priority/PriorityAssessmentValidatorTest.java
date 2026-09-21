package com.taska.priority;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.taska.priority.application.PriorityAssessmentValidator;
import com.taska.priority.model.PriorityAssessmentBatchResult;
import com.taska.priority.model.PriorityLevel;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PriorityAssessmentValidatorTest {
    private final PriorityAssessmentValidator priorityAssessmentValidator = new PriorityAssessmentValidator();
    private final UUID id = UUID.randomUUID();

    @Test
    void validAssessmentIsAccepted() {
        assertThat(priorityAssessmentValidator.isValid(assessment(id))).isTrue();
    }

    @Test
    void duplicateEnvelopeIdsAreRejected() {
        var response = new PriorityAssessmentBatchResult(List.of(assessment(id), assessment(id)));
        assertThatThrownBy(() -> priorityAssessmentValidator.validateEnvelope(response, Set.of(id))).isInstanceOf(IllegalArgumentException.class);
    }

    private PriorityAssessmentBatchResult.Assessment assessment(UUID taskId) {
        return new PriorityAssessmentBatchResult.Assessment(
                taskId,
                PriorityLevel.HIGH,
                PriorityLevel.HIGH,
                PriorityLevel.MEDIUM,
                20,
                .9,
                .8,
                .7,
                .6,
                "deadline",
                "benefit",
                "consequence",
                "short");
    }
}
