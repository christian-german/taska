package com.taska.android.widget

import com.taska.android.data.model.TaskDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

class WeekWidgetItemsTest {
    private val utc = ZoneId.of("UTC")

    @Test fun `tasks are ordered and grouped under one header per local date`() {
        val items = WeekWidgetItems.build(
            listOf(task("late", "2026-06-18T16:00:00Z"), task("early", "2026-06-17T10:00:00Z"), task("same-day", "2026-06-17T15:00:00Z")),
            utc,
            LocalDate.of(2026, 6, 17),
        )

        assertEquals(
            listOf("2026-06-17", "early", "same-day", "2026-06-18", "late"),
            items.map { if (it is WeekWidgetItem.DateHeader) it.date.toString() else (it as WeekWidgetItem.Task).task.id },
        )
    }

    @Test fun `grouping uses the supplied device time zone at a date boundary`() {
        val task = task("boundary", "2026-06-17T23:30:00Z")

        val header = WeekWidgetItems.build(listOf(task), ZoneId.of("Europe/Paris"), LocalDate.of(2026, 6, 18)).first() as WeekWidgetItem.DateHeader

        assertEquals(LocalDate.of(2026, 6, 18), header.date)
    }

    @Test fun `overdue tasks use one leading header without original date headers`() {
        val items = WeekWidgetItems.build(
            listOf(
                task("today", "2026-06-17T09:00:00Z"),
                task("oldest", "2026-06-14T09:00:00Z"),
                task("older", "2026-06-16T09:00:00Z"),
            ),
            utc,
            LocalDate.of(2026, 6, 17),
        )

        assertEquals(
            listOf("Overdue", "oldest", "older", "2026-06-17", "today"),
            items.map {
                when (it) {
                    WeekWidgetItem.OverdueHeader -> "Overdue"
                    is WeekWidgetItem.DateHeader -> it.date.toString()
                    is WeekWidgetItem.Task -> it.task.id
                }
            },
        )
    }

    @Test fun `no overdue header is emitted when all tasks are current`() {
        val items = WeekWidgetItems.build(
            listOf(task("today", "2026-06-17T09:00:00Z")), utc, LocalDate.of(2026, 6, 17),
        )

        assertFalse(items.any { it is WeekWidgetItem.OverdueHeader })
    }

    @Test fun `header is localized and task rows contain time and title only`() {
        val task = task("meeting", "2026-06-17T10:00:00Z")

        assertEquals("Wed 17/06", WeekWidgetItems.header(LocalDate.of(2026, 6, 17), Locale.ENGLISH))
        val text = WeekWidgetItems.taskText(task, utc, Locale.ENGLISH)
        assertEquals("10:00 AM  meeting", text)
        assertFalse(text.contains("Wed"))
        assertFalse(text.contains("17/06"))
    }

    private fun task(id: String, scheduledAt: String) = TaskDto(
        id = id, content = id, description = null, projectId = null, sectionId = null,
        parentId = null, order = 0, priority = null, labels = emptyList(), isCompleted = false,
        scheduledAt = scheduledAt, estimateMinutes = null, isRecurring = false,
        createdAt = null, updatedAt = null, completedAt = null,
    )
}
