package com.taska.task;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.taska.task.model.Task;
import com.taska.task.persistence.TaskRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@DataJpaTest(properties = {"spring.test.database.replace=none", "spring.flyway.enabled=true"})
@ActiveProfiles("tests")
class TaskPersistenceInvariantTest {

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void recurringSeriesCannotPersistAnAbsoluteDeadline() {
        Task series = task(true);
        series.setDueAt(Instant.parse("2026-05-21T17:00:00Z"));

        assertThatThrownBy(() -> taskRepository.saveAndFlush(series)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void nonRecurringTaskCanPersistAnAbsoluteDeadline() {
        Task task = task(false);
        task.setDueAt(Instant.parse("2026-05-21T17:00:00Z"));

        assertThatCode(() -> taskRepository.saveAndFlush(task)).doesNotThrowAnyException();
    }

    @Test
    void aSeriesCannotPersistWithoutARecurrenceRule() {
        Task series = task(true);
        series.setRecurrenceRule(null);

        assertThatThrownBy(() -> taskRepository.saveAndFlush(series)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void aSeriesCannotPersistWithABlankRecurrenceRule() {
        Task series = task(true);
        series.setRecurrenceRule("   ");

        assertThatThrownBy(() -> taskRepository.saveAndFlush(series)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void aSeriesCannotPersistWithoutASchedule() {
        Task series = task(true);
        series.setScheduledAt(null);

        assertThatThrownBy(() -> taskRepository.saveAndFlush(series)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void aNonRecurringTaskCanPersistWithoutASchedule() {
        Task task = task(false);
        task.setScheduledAt(null);

        assertThatCode(() -> taskRepository.saveAndFlush(task)).doesNotThrowAnyException();
    }

    private Task task(boolean recurring) {
        Task task = new Task();
        task.setContent(recurring ? "Recurring" : "One-off");
        task.setIsRecurring(recurring);
        task.setRecurrenceRule(recurring ? "FREQ=DAILY" : null);
        task.setScheduledAt(Instant.parse("2026-05-20T09:00:00Z"));
        return task;
    }
}
