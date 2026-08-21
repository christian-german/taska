package com.taska.android.widget

import com.taska.android.data.model.TaskDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TodayWidgetGroupsTest {
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
