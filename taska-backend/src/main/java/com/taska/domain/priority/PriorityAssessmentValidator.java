package com.taska.domain.priority;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Component
public class PriorityAssessmentValidator {
    public void validateEnvelope(PriorityEvaluationBatchResponse response, Set<UUID> requestedIds) {
        if (response == null || response.evaluations() == null) {
            throw new IllegalArgumentException("Missing evaluation batch");
        }
        Set<UUID> returnedTaskIds = new HashSet<>();
        for (var assessment : response.evaluations()) {
            if (assessment == null || assessment.taskId() == null || !requestedIds.contains(assessment.taskId())
                    || !returnedTaskIds.add(assessment.taskId())) {
                throw new IllegalArgumentException("Invalid evaluation batch envelope");
            }
        }
    }

    public boolean isValid(PriorityEvaluationBatchResponse.Assessment assessment) {
        return assessment != null && assessment.taskId() != null && assessment.urgency() != null
                && isThreeLevel(assessment.impact())
                && isThreeLevel(assessment.risk())
                && assessment.durationMinutes() != null && assessment.durationMinutes() > 0
                && confidence(assessment.urgencyConfidence()) && confidence(assessment.impactConfidence())
                && confidence(assessment.riskConfidence()) && confidence(assessment.durationConfidence())
                && nonBlank(assessment.urgencyReason()) && nonBlank(assessment.impactReason())
                && nonBlank(assessment.riskReason()) && nonBlank(assessment.durationReason());
    }

    private boolean isThreeLevel(PriorityLevel value) {
        return value != null && value != PriorityLevel.CRITICAL;
    }

    private boolean confidence(Double value) {
        return value != null && value >= 0 && value <= 1;
    }

    private boolean nonBlank(String value) {
        return value != null && !value.isBlank();
    }
}
