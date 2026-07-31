package com.taska.domain.priority;

import org.springframework.stereotype.Component;

@Component
public class TaskPriorityScorer {

    public int urgencyPoints(PriorityLevel value) {
        return switch (value) {
            case LOW -> 0;
            case MEDIUM -> 10;
            case HIGH -> 20;
            case CRITICAL -> 30;
        };
    }

    public int impactPoints(PriorityLevel value) {
        return switch (requireThreeLevel(value, "impact")) {
            case LOW -> 0;
            case MEDIUM -> 15;
            case HIGH -> 30;
            case CRITICAL -> throw new IllegalStateException("unreachable");
        };
    }

    public int riskPoints(PriorityLevel value) {
        return switch (requireThreeLevel(value, "risk")) {
            case LOW -> 0;
            case MEDIUM -> 12;
            case HIGH -> 25;
            case CRITICAL -> throw new IllegalStateException("unreachable");
        };
    }

    public int durationPoints(int minutes) {
        if (minutes <= 0) throw new IllegalArgumentException("duration must be positive");
        if (minutes <= 15) return 15;
        if (minutes <= 30) return 10;
        if (minutes <= 60) return 5;
        return 0;
    }

    public int total(PriorityLevel urgency, PriorityLevel impact, PriorityLevel risk, int durationMinutes) {
        return urgencyPoints(urgency) + impactPoints(impact) + riskPoints(risk) + durationPoints(durationMinutes);
    }

    private PriorityLevel requireThreeLevel(PriorityLevel value, String component) {
        if (value == null || value == PriorityLevel.CRITICAL) {
            throw new IllegalArgumentException(component + " must be LOW, MEDIUM, or HIGH");
        }
        return value;
    }
}
