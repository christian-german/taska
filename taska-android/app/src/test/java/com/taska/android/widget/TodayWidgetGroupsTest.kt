package com.taska.android.widget

import com.taska.android.data.model.TaskDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class TodayWidgetGroupsTest {
    @Test fun `today all-day rows omit the clock time while timed rows retain it`() {
        val scheduledAt = "2026-06-17T10:00:00Z"

        assertEquals("Wed  holiday", TaskWidgetRefresh.todayTaskText(task("holiday", scheduledAt).copy(allDay = true)))
        val expectedTime = Instant.parse(scheduledAt).atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("HH:mm"))
        assertEquals("Wed  $expectedTime meeting", TaskWidgetRefresh.todayTaskText(task("meeting", scheduledAt)))
    }

    @Test fun `overdue and today tasks have a label and one boundary divider`() {
        val groups = TodayWidgetGroups.from(
            listOf(task("oldest", "2020-01-01T09:00:00Z"), task("older", "2020-01-02T09:00:00Z"), task("today", "2999-01-01T09:00:00Z")),
        )

        assertTrue(groups.showOverdueHeader)
        assertEquals(1, groups.dividerAfterRow)
    }

    @Test fun `only overdue tasks have a label without a trailing divider`() {
        val groups = TodayWidgetGroups.from(listOf(task("overdue", "2020-01-01T09:00:00Z")))

        assertTrue(groups.showOverdueHeader)
        assertNull(groups.dividerAfterRow)
    }

    @Test fun `tasks without overdue work have neither label nor dividers`() {
        val groups = TodayWidgetGroups.from(listOf(task("today", "2999-01-01T09:00:00Z")))

        assertFalse(groups.showOverdueHeader)
        assertNull(groups.dividerAfterRow)
    }

    private fun task(id: String, scheduledAt: String) = TaskDto(
        id = id, content = id, description = null, projectId = null, sectionId = null,
        parentId = null, order = 0, priority = null, labels = emptyList(), isCompleted = false,
        scheduledAt = scheduledAt, estimateMinutes = null, isRecurring = false,
        createdAt = null, updatedAt = null, completedAt = null,
    )
}
