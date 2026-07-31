package com.taska.domain.priority;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskPriorityScorerTest {
    private final TaskPriorityScorer scorer = new TaskPriorityScorer();

    @Test void maximumAssessmentScoresOneHundred() {
        assertThat(scorer.total(PriorityLevel.CRITICAL, PriorityLevel.HIGH, PriorityLevel.HIGH, 15)).isEqualTo(100);
    }

    @Test void durationBandsAreMappedDeterministically() {
        assertThat(scorer.durationPoints(15)).isEqualTo(15);
        assertThat(scorer.durationPoints(30)).isEqualTo(10);
        assertThat(scorer.durationPoints(60)).isEqualTo(5);
        assertThat(scorer.durationPoints(61)).isZero();
        assertThatThrownBy(() -> scorer.durationPoints(0)).isInstanceOf(IllegalArgumentException.class);
    }
}
