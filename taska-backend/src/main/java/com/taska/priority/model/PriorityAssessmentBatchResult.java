package com.taska.priority.model;

import java.util.List;
import java.util.UUID;

public record PriorityAssessmentBatchResult(List<Assessment> evaluations) {
    public record Assessment(UUID taskId, PriorityLevel urgency, PriorityLevel impact, PriorityLevel risk, Integer durationMinutes,
            Double urgencyConfidence, Double impactConfidence, Double riskConfidence, Double durationConfidence, String urgencyReason,
            String impactReason, String riskReason, String durationReason) {
    }
}
