package com.taska.android.widget

import com.taska.android.data.model.TaskDto
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

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

    private fun task(id: String, scheduledAt: String?, completed: Boolean = false) = TaskDto(
        id = id, content = id, description = null, projectId = null, sectionId = null,
        parentId = null, order = 0, priority = null, labels = emptyList(), isCompleted = completed,
        scheduledAt = scheduledAt, estimateMinutes = null, isRecurring = false,
        createdAt = null, updatedAt = null, completedAt = null
    )
}
