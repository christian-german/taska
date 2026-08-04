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
            week
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

    private fun task(id: String, scheduledAt: String?, completed: Boolean = false) = TaskDto(
        id = id, content = id, description = null, projectId = null, sectionId = null,
        parentId = null, order = 0, priority = null, labels = emptyList(), isCompleted = completed,
        scheduledAt = scheduledAt, estimateMinutes = null, isRecurring = false,
        createdAt = null, updatedAt = null, completedAt = null
    )
}
