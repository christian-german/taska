package com.taska.domain.priority;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Component
public class PriorityAssessmentValidator {
    public void validateEnvelope(PriorityEvaluationBatchResponse response, Set<UUID> requestedIds) {
        if (response == null || response.evaluations() == null) throw new IllegalArgumentException("Missing evaluation batch");
        Set<UUID> returned = new HashSet<>();
        for (var assessment : response.evaluations()) {
            if (assessment == null || assessment.taskId() == null || !requestedIds.contains(assessment.taskId())
                    || !returned.add(assessment.taskId())) {
                throw new IllegalArgumentException("Invalid evaluation batch envelope");
            }
        }
    }

    public boolean isValid(PriorityEvaluationBatchResponse.Assessment a) {
        return a != null && a.taskId() != null && a.urgency() != null && isThreeLevel(a.impact())
                && isThreeLevel(a.risk()) && a.durationMinutes() != null && a.durationMinutes() > 0
                && confidence(a.urgencyConfidence()) && confidence(a.impactConfidence())
                && confidence(a.riskConfidence()) && confidence(a.durationConfidence())
                && nonBlank(a.urgencyReason()) && nonBlank(a.impactReason())
                && nonBlank(a.riskReason()) && nonBlank(a.durationReason());
    }

    private boolean isThreeLevel(PriorityLevel value) { return value != null && value != PriorityLevel.CRITICAL; }
    private boolean confidence(Double value) { return value != null && value >= 0 && value <= 1; }
    private boolean nonBlank(String value) { return value != null && !value.isBlank(); }
}
