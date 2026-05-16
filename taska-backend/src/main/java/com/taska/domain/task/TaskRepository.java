package com.taska.domain.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findByProjectIdAndIsCompletedFalseOrderByPositionAsc(UUID projectId);

    List<Task> findBySectionIdAndIsCompletedFalseOrderByPositionAsc(UUID sectionId);

    List<Task> findByProjectIdAndSectionIdAndIsCompletedFalseOrderByPositionAsc(UUID projectId, UUID sectionId);

    List<Task> findByProjectIdAndSectionIdIsNullAndIsCompletedFalseOrderByPositionAsc(UUID projectId);

    List<Task> findByDueAtBetweenAndIsCompletedFalseOrderByDueAtAsc(LocalDateTime from, LocalDateTime to);

    List<Task> findByDueAtBeforeAndIsCompletedFalseOrderByDueAtAsc(LocalDateTime before);

    List<Task> findByProjectIdOrderByPositionAsc(UUID projectId);

    List<Task> findBySectionIdOrderByPositionAsc(UUID sectionId);

    List<Task> findByProjectIdAndSectionIdOrderByPositionAsc(UUID projectId, UUID sectionId);

    List<Task> findByProjectIdAndSectionIdIsNullOrderByPositionAsc(UUID projectId);

    List<Task> findByParentIdOrderByPositionAsc(UUID parentId);

    void deleteByProjectId(UUID projectId);

    @Query("SELECT t FROM Task t JOIN t.labels l WHERE l = :label AND t.isCompleted = false ORDER BY t.position ASC")
    List<Task> findByLabelAndIsCompletedFalse(@Param("label") String label);

    @Query("SELECT t FROM Task t JOIN t.labels l WHERE l = :label ORDER BY t.position ASC")
    List<Task> findByLabel(@Param("label") String label);

    @Query("SELECT t FROM Task t WHERE t.isCompleted = false AND t.dueAt IS NOT NULL ORDER BY t.dueAt ASC")
    List<Task> findAllWithDueAtNotCompleted();

    @Query("SELECT t FROM Task t WHERE t.isCompleted = false AND t.dueAt IS NULL ORDER BY t.position ASC")
    List<Task> findAllWithNoDueAtNotCompleted();

    List<Task> findByProjectIdAndDueAtIsNotNullAndIsCompletedFalseOrderByDueAtAsc(UUID projectId);

    List<Task> findByProjectIdAndDueAtIsNullAndIsCompletedFalseOrderByPositionAsc(UUID projectId);

    long countByIsCompletedTrue();

    long countByIsCompletedFalse();

    @Query("SELECT COUNT(t) FROM Task t WHERE t.isCompleted = false AND t.dueAt < :before")
    long countByIsCompletedFalseAndDueAtBefore(@Param("before") LocalDateTime before);

    List<Task> findByCompletedAtAfterOrderByCompletedAtAsc(Instant since);

    List<Task> findByIsCompletedTrueOrderByCompletedAtDesc();

    @Query("SELECT t FROM Task t WHERE t.isCompleted = false AND t.isNotified = false AND t.allDay = false AND t.dueAt IS NOT NULL AND t.dueAt <= :in15min")
    List<Task> findTasksDueAround(@Param("in15min") LocalDateTime in15min);
}
