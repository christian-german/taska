package com.taska.priority;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.taska.priority.application.TaskPriorityScorer;
import com.taska.priority.model.PriorityLevel;
import org.junit.jupiter.api.Test;

class TaskPriorityScorerTest {
    private final TaskPriorityScorer taskPriorityScorer = new TaskPriorityScorer();

    @Test
    void maximumAssessmentScoresOneHundred() {
        assertThat(taskPriorityScorer.total(PriorityLevel.CRITICAL, PriorityLevel.HIGH, PriorityLevel.HIGH, 15)).isEqualTo(100);
    }

    @Test
    void durationBandsAreMappedDeterministically() {
        assertThat(taskPriorityScorer.durationPoints(15)).isEqualTo(15);
        assertThat(taskPriorityScorer.durationPoints(30)).isEqualTo(10);
        assertThat(taskPriorityScorer.durationPoints(60)).isEqualTo(5);
        assertThat(taskPriorityScorer.durationPoints(61)).isZero();
        assertThatThrownBy(() -> taskPriorityScorer.durationPoints(0)).isInstanceOf(IllegalArgumentException.class);
    }
}
