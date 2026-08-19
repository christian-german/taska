package com.taska.android.widget

import com.taska.android.data.model.TaskDto
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class TaskWidgetRefreshTest {
    @Test fun `calendar week starts on Monday and ends on Sunday`() {
        assertEquals(
            LocalDate.of(2026, 8, 3) to LocalDate.of(2026, 8, 9),
            TaskWidgetRefresh.currentWeek(LocalDate.of(2026, 8, 5))
        )
    }

    @Test fun `only incomplete scheduled tasks inside the week are retained`() {
        val week = LocalDate.of(2026, 8, 3) to LocalDate.of(2026, 8, 9)
        val result = TaskWidgetRefresh.filterScheduledTasks(
            listOf(
                task("scheduled", "2026-08-04T09:00:00Z"),
                task("unscheduled", null),
                task("completed", "2026-08-05T09:00:00Z", completed = true),
                task("outside", "2026-08-10T09:00:00Z")
            ),
            week,
            today = LocalDate.of(2026, 8, 3),
        )
        assertEquals(listOf("scheduled"), result.map { it.id })
    }

    @Test fun `today range contains only the local current day`() {
        assertEquals(
            LocalDate.of(2026, 8, 5) to LocalDate.of(2026, 8, 5),
            TaskWidgetRefresh.currentDay(LocalDate.of(2026, 8, 5))
        )
    }

    @Test fun `today keeps completed and incomplete tasks planned for today`() {
        val today = LocalDate.of(2026, 8, 5) to LocalDate.of(2026, 8, 5)
        val result = TaskWidgetRefresh.filterScheduledTasks(
            listOf(
                task("incomplete", "2026-08-05T09:00:00Z"),
                task("completed", "2026-08-05T10:00:00Z", completed = true),
                task("tomorrow", "2026-08-06T09:00:00Z"),
                task("unscheduled", null),
            ),
            today,
            today = today.first,
            includeCompleted = true,
        )

        assertEquals(listOf("incomplete", "completed"), result.map { it.id })
    }

    @Test fun `next refresh is the next local midnight including Monday`() {
        val zone = ZoneId.of("Europe/Paris")
        val now = ZonedDateTime.of(2026, 8, 2, 23, 45, 0, 0, zone)

        assertEquals(
            Instant.parse("2026-08-02T22:00:00Z"),
            TaskWidgetRefresh.nextLocalDayBoundary(now)
        )
    }

    @Test fun `week retains more than eight tasks while today keeps its capacity`() {
        val range = LocalDate.of(2026, 8, 3) to LocalDate.of(2026, 8, 9)
        val tasks = (0..9).map { task("task-$it", "2026-08-05T${it.toString().padStart(2, '0')}:00:00Z") }

        assertEquals(10, TaskWidgetRefresh.filterScheduledTasks(tasks, range, today = range.first).size)
        assertEquals(8, TaskWidgetRefresh.filterScheduledTasks(tasks, range, today = range.first, includeCompleted = true).size)
    }

    @Test fun `overdue tasks are incomplete deduplicated and ordered before today`() {
        val today = LocalDate.of(2026, 8, 5)
        val occurrence = task("recurring", "2026-08-03T09:00:00Z", occurrence = "2026-08-03T09:00:00Z")
        val result = TaskWidgetRefresh.filterScheduledTasks(
            listOf(
                task("today", "2026-08-05T08:00:00Z"),
                task("older", "2026-08-01T10:00:00Z"),
                task("historical-completed", "2026-08-02T10:00:00Z", completed = true),
                task("deadline-only", null),
                occurrence,
                occurrence,
            ),
            today to today,
            today = today,
            includeCompleted = true,
            zone = ZoneId.of("UTC"),
        )

        assertEquals(listOf("older", "recurring", "today"), result.map { it.id })
    }

    @Test fun `local date determines whether a task is overdue`() {
        val today = LocalDate.of(2026, 8, 5)
        val result = TaskWidgetRefresh.filterScheduledTasks(
            listOf(task("local-today", "2026-08-04T22:30:00Z")),
            today to today,
            today = today,
            zone = ZoneId.of("Europe/Paris"),
        )

        assertEquals(listOf("local-today"), result.map { it.id })
    }

    @Test fun `today capacity is selected from overdue first order`() {
        val today = LocalDate.of(2026, 8, 5)
        val overdue = (1..8).map { day -> task("overdue-$day", "2026-07-${day.toString().padStart(2, '0')}T09:00:00Z") }
        val result = TaskWidgetRefresh.filterScheduledTasks(
            overdue + task("today", "2026-08-05T09:00:00Z"),
            today to today,
            today = today,
            includeCompleted = true,
            zone = ZoneId.of("UTC"),
        )

        assertEquals(overdue.map { it.id }, result.map { it.id })
    }

    private fun task(id: String, scheduledAt: String?, completed: Boolean = false, occurrence: String? = null) = TaskDto(
        id = id, content = id, description = null, projectId = null, sectionId = null,
        parentId = null, order = 0, priority = null, labels = emptyList(), isCompleted = completed,
        scheduledAt = scheduledAt, estimateMinutes = null, isRecurring = false,
        createdAt = null, updatedAt = null, completedAt = null, occurrenceScheduledAt = occurrence,
    )
}
