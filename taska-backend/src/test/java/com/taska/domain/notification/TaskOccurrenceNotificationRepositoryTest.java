package com.taska.domain.notification;

import static org.assertj.core.api.Assertions.assertThat;

import com.taska.domain.notification.repository.TaskOccurrenceNotificationRepository;
import com.taska.domain.task.occurrence.TaskOccurrenceState;
import com.taska.domain.task.occurrence.TaskOccurrenceStatus;
import com.taska.domain.task.occurrence.repository.TaskOccurrenceStateRepository;
import com.taska.domain.task.repository.Task;
import com.taska.domain.task.repository.TaskRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@DataJpaTest(properties = {"spring.test.database.replace=none", "spring.flyway.enabled=true"})
@ActiveProfiles("tests")
class TaskOccurrenceNotificationRepositoryTest {

  @Autowired private TaskRepository taskRepository;
  @Autowired private TaskOccurrenceStateRepository occurrenceStateRepository;
  @Autowired private TaskOccurrenceNotificationRepository repository;
  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void claimIsUniqueUntilItsMarkerIsRemoved() {
    Task series = createRecurringSeries();
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");

    assertThat(claim(series.getId(), occurrenceScheduledAt)).isEqualTo(1);
    assertThat(claim(series.getId(), occurrenceScheduledAt)).isZero();

    repository.deleteBySeriesIdAndOccurrenceScheduledAt(series.getId(), occurrenceScheduledAt);
    repository.flush();

    assertThat(claim(series.getId(), occurrenceScheduledAt)).isEqualTo(1);
  }

  @Test
  void migrationUsesOccurrenceStateAndSeriesVocabulary() {
    assertThat(countTable("task_occurrence_states")).isOne();
    assertThat(countTable("task_instances")).isZero();
    assertThat(countColumn("task_occurrence_states", "series_id")).isOne();
    assertThat(countColumn("task_occurrence_states", "task_id")).isZero();
    assertThat(countColumn("task_occurrence_notifications", "series_id")).isOne();
    assertThat(countColumn("task_occurrence_notifications", "task_id")).isZero();

    Task series = createRecurringSeries();
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
    TaskOccurrenceState state = new TaskOccurrenceState();
    state.setSeriesId(series.getId());
    state.setOccurrenceScheduledAt(occurrenceScheduledAt);
    state.setStatus(TaskOccurrenceStatus.MODIFIED);
    state = occurrenceStateRepository.saveAndFlush(state);

    assertThat(
            occurrenceStateRepository.findBySeriesIdAndOccurrenceScheduledAt(
                series.getId(), occurrenceScheduledAt))
        .contains(state);
  }

  private Task createRecurringSeries() {
    Task series = new Task();
    series.setContent("Recurring task");
    series.setIsRecurring(true);
    series.setRecurrenceRule("FREQ=DAILY");
    series.setScheduledAt(Instant.parse("2026-05-01T10:00:00Z"));
    return taskRepository.saveAndFlush(series);
  }

  private long countTable(String tableName) {
    return jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM information_schema.tables "
            + "WHERE table_schema = 'public' AND table_name = ?",
        Long.class,
        tableName);
  }

  private long countColumn(String tableName, String columnName) {
    return jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM information_schema.columns "
            + "WHERE table_schema = 'public' AND table_name = ? AND column_name = ?",
        Long.class,
        tableName,
        columnName);
  }

  private int claim(UUID seriesId, Instant occurrenceScheduledAt) {
    return repository.claim(
        UUID.randomUUID(), seriesId, occurrenceScheduledAt, Instant.parse("2026-05-20T09:45:00Z"));
  }
}
