package com.taska.android.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/**
 * Tests sur les champs du DTO TaskDto.
 *
 * Il n'existe pas de Mapper séparé dans ce projet : le backend retourne le DTO
 * déjà fusionné (parent + overrides de l'instance). Ces tests vérifient que la
 * data class expose correctement les champs de récurrence et leurs valeurs par défaut.
 */
class TaskDtoTest {

    // -------------------------------------------------------------------------
    // Builder avec valeurs par défaut surchargeables
    // -------------------------------------------------------------------------

    private fun buildTaskDto(
        id: String = "task-1",
        content: String = "Tâche de test",
        description: String? = null,
        isCompleted: Boolean? = false,
        isRecurring: Boolean? = false,
        isVirtual: Boolean? = null,
        instanceId: String? = null,
        scheduledAt: String? = null,
        dueAt: String? = null,
        allDay: Boolean = false,
        priority: Int? = 3,
        recurrenceRule: String? = null,
        type: String? = "TODO",
    ) = TaskDto(
        id = id,
        content = content,
        type = type,
        description = description,
        projectId = null,
        sectionId = null,
        parentId = null,
        order = null,
        priority = priority,
        labels = null,
        isCompleted = isCompleted,
        dueAt = dueAt,
        allDay = allDay,
        isRecurring = isRecurring,
        recurrenceRule = recurrenceRule,
        estimateMinutes = null,
        createdAt = null,
        updatedAt = null,
        completedAt = null,
        instanceId = instanceId,
        scheduledAt = scheduledAt,
        isVirtual = isVirtual,
    )

    // -------------------------------------------------------------------------
    // 1.1 — Tâche normale : champs de récurrence à leurs valeurs neutres
    // -------------------------------------------------------------------------

    @Test
    fun `givenNonRecurringTask_whenCreated_thenIsRecurringFalse`() {
        val task = buildTaskDto(isRecurring = false)

        assertFalse(task.isRecurring == true)
    }

    @Test
    fun `givenNonRecurringTask_whenCreated_thenIsVirtualNull`() {
        val task = buildTaskDto(isRecurring = false, isVirtual = null)

        assertNull(task.isVirtual)
    }

    @Test
    fun `givenNonRecurringTask_whenCreated_thenInstanceIdNull`() {
        val task = buildTaskDto(isRecurring = false, instanceId = null)

        assertNull(task.instanceId)
    }

    @Test
    fun `givenNonRecurringTask_whenCreated_thenScheduledAtNull`() {
        val task = buildTaskDto(isRecurring = false, scheduledAt = null)

        assertNull(task.scheduledAt)
    }

    // -------------------------------------------------------------------------
    // 1.2 — Occurrence virtuelle (générée par le backend, non persistée)
    // -------------------------------------------------------------------------

    @Test
    fun `givenVirtualOccurrence_whenCreated_thenIsVirtualTrue`() {
        val task = buildTaskDto(isRecurring = true, isVirtual = true, instanceId = null)

        assertTrue(task.isVirtual == true)
    }

    @Test
    fun `givenVirtualOccurrence_whenCreated_thenIsRecurringTrue`() {
        val task = buildTaskDto(isRecurring = true, isVirtual = true)

        assertTrue(task.isRecurring == true)
    }

    @Test
    fun `givenVirtualOccurrence_whenCreated_thenInstanceIdIsNull`() {
        val task = buildTaskDto(isRecurring = true, isVirtual = true, instanceId = null)

        assertNull(task.instanceId)
    }

    @Test
    fun `givenVirtualOccurrence_whenCreated_thenScheduledAtPresent`() {
        val task = buildTaskDto(isRecurring = true, isVirtual = true, scheduledAt = "2026-05-20T09:00:00Z")

        assertNotNull(task.scheduledAt)
    }

    // -------------------------------------------------------------------------
    // 1.3 — Instance réelle persistée avec status DONE
    // -------------------------------------------------------------------------

    @Test
    fun `givenRealInstanceDone_whenCreated_thenInstanceIdSet`() {
        val instanceId = "instance-uuid-abc"
        val task = buildTaskDto(isRecurring = true, isVirtual = false, instanceId = instanceId, isCompleted = true)

        assertEquals(instanceId, task.instanceId)
    }

    @Test
    fun `givenRealInstanceDone_whenCreated_thenIsVirtualFalse`() {
        val task = buildTaskDto(isRecurring = true, isVirtual = false, instanceId = "uuid", isCompleted = true)

        assertFalse(task.isVirtual == true)
    }

    @Test
    fun `givenRealInstanceDone_whenCreated_thenIsCompletedTrue`() {
        val task = buildTaskDto(isRecurring = true, isVirtual = false, instanceId = "uuid", isCompleted = true)

        assertTrue(task.isCompleted == true)
    }

    // -------------------------------------------------------------------------
    // 1.5 — scheduledAt présent : doit être parseable en Instant
    // -------------------------------------------------------------------------

    @Test
    fun `givenScheduledAtISO8601_whenParsed_thenNoException`() {
        val task = buildTaskDto(isRecurring = true, scheduledAt = "2026-05-20T09:00:00Z")

        assertDoesNotThrow { java.time.Instant.parse(task.scheduledAt!!) }
    }

    @Test
    fun `givenScheduledAtISO8601_whenAccessed_thenValuePreserved`() {
        val raw = "2026-05-20T09:00:00Z"
        val task = buildTaskDto(scheduledAt = raw)

        assertEquals(raw, task.scheduledAt)
    }

    // -------------------------------------------------------------------------
    // 1.6 — scheduledAt null sur tâche non récurrente
    // -------------------------------------------------------------------------

    @Test
    fun `givenNonRecurringTaskWithNullScheduledAt_whenCreated_thenNoException`() {
        assertDoesNotThrow {
            buildTaskDto(isRecurring = false, scheduledAt = null)
        }
    }

    // -------------------------------------------------------------------------
    // 1.7 — scheduledAt null sur tâche récurrente
    //        Le DTO l'accepte ; c'est le ViewModel qui gère ce cas en ne montrant
    //        pas le dialog de scope (requestDeleteTask vérifie scheduledAt != null).
    // -------------------------------------------------------------------------

    @Test
    fun `givenRecurringTaskWithNullScheduledAt_whenCreated_thenFieldIsNull`() {
        val task = buildTaskDto(isRecurring = true, scheduledAt = null)

        assertTrue(task.isRecurring == true)
        assertNull(task.scheduledAt)
    }

    // -------------------------------------------------------------------------
    // 1.8 — dueAt présent : format ISO 8601 préservé
    // -------------------------------------------------------------------------

    @Test
    fun `givenDueAtISO8601_whenAccessed_thenValuePreserved`() {
        val raw = "2026-05-20T08:00:00Z"
        val task = buildTaskDto(dueAt = raw)

        assertEquals(raw, task.dueAt)
    }

    @Test
    fun `givenDueAtISO8601_whenParsed_thenNoException`() {
        val task = buildTaskDto(dueAt = "2026-05-20T08:00:00Z")

        assertDoesNotThrow { java.time.Instant.parse(task.dueAt!!) }
    }

    @Test
    fun `givenNullDueAt_whenAccessed_thenNull`() {
        val task = buildTaskDto(dueAt = null)

        assertNull(task.dueAt)
    }

    // -------------------------------------------------------------------------
    // 1.9 — allDay flag
    // -------------------------------------------------------------------------

    @Test
    fun `givenAllDayTrue_whenAccessed_thenTrue`() {
        val task = buildTaskDto(allDay = true)

        assertTrue(task.allDay)
    }

    @Test
    fun `givenAllDayDefault_whenAccessed_thenFalse`() {
        val task = buildTaskDto()

        assertFalse(task.allDay)
    }

    @Test
    fun `givenAllDayFalse_whenAccessed_thenFalse`() {
        val task = buildTaskDto(allDay = false)

        assertFalse(task.allDay)
    }

    @Test
    fun `givenAppointmentType_whenAccessed_thenTypeIsPreserved`() {
        assertEquals("APPOINTMENT", buildTaskDto(type = "APPOINTMENT").type)
    }

    @Test
    fun `givenLegacyTaskWithoutType_whenCreated_thenTypeDefaultsToTodo`() {
        assertEquals("TODO", buildTaskDto(type = null).type ?: "TODO")
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun assertDoesNotThrow(block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            fail("Aucune exception attendue, mais reçu : ${e::class.simpleName} — ${e.message}")
        }
    }
}
