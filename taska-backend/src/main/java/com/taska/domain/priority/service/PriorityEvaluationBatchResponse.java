package com.taska.domain.priority.service;

import com.taska.domain.priority.PriorityLevel;
import java.util.List;
import java.util.UUID;

public record PriorityEvaluationBatchResponse(List<Assessment> evaluations) {
  public record Assessment(
      UUID taskId,
      PriorityLevel urgency,
      PriorityLevel impact,
      PriorityLevel risk,
      Integer durationMinutes,
      Double urgencyConfidence,
      Double impactConfidence,
      Double riskConfidence,
      Double durationConfidence,
      String urgencyReason,
      String impactReason,
      String riskReason,
      String durationReason) {}
}
