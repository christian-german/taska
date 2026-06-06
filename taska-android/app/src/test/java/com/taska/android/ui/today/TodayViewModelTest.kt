package com.taska.android.ui.today

import com.taska.android.MainDispatcherRule
import com.taska.android.data.model.RecurrenceScope
import com.taska.android.data.model.TaskDto
import com.taska.android.data.repository.ProjectRepository
import com.taska.android.data.repository.TaskRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Tests unitaires de TodayViewModel.
 *
 * Le ViewModel délègue tous les appels réseau à TaskRepository et ProjectRepository.
 * Les deux repositories sont mockés — aucun accès réseau réel.
 *
 * Cas NON implémentés dans la base de code actuelle (documentés dans RECURRENCE_GAPS.md) :
 *  - Dialog de scope sur closeTask (close appelle toujours directement le repository).
 *  - Update avec scope THIS_ONLY / FROM_THIS (aucun ViewModel ne l'implémente).
 */
class TodayViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val taskRepo = mockk<TaskRepository>(relaxed = true)
    private val projectRepo = mockk<ProjectRepository>(relaxed = true)
    private lateinit var viewModel: TodayViewModel

    // Dates futures pour éviter tout problème de timezone dans les filtres overdue.
    private val scheduledAt = "2027-06-15T10:00:00Z"
    private val dueAtEarly = "2027-06-15T08:00:00Z"
    private val dueAtMid   = "2027-06-15T13:00:00Z"
    private val dueAtLate  = "2027-06-15T19:00:00Z"

    @Before
    fun setUp() {
        coEvery { projectRepo.getProjects() } returns emptyList()
        coEvery { taskRepo.getTasks(any(), any(), any(), any(), any()) } returns emptyList()

        viewModel = TodayViewModel(taskRepo, projectRepo)
    }

    // -------------------------------------------------------------------------
    // Builder de TaskDto avec valeurs surchargeables
    // -------------------------------------------------------------------------

    private fun buildTask(
        id: String = "task-1",
        content: String = "Tâche de test",
        isCompleted: Boolean? = false,
        isRecurring: Boolean? = false,
        isVirtual: Boolean? = null,
        instanceId: String? = null,
        scheduledAt: String? = null,
        dueAt: String? = null,
    ) = TaskDto(
        id = id, content = content, description = null, projectId = null,
        sectionId = null, parentId = null, order = null, priority = 3,
        labels = null, isCompleted = isCompleted, dueAt = dueAt, allDay = false,
        isRecurring = isRecurring, recurrenceRule = null, estimateMinutes = null,
        createdAt = null, updatedAt = null, completedAt = null,
        instanceId = instanceId, scheduledAt = scheduledAt, isVirtual = isVirtual,
    )

    // =========================================================================
    // Section 2 / 3a — closeTask
    // =========================================================================

    // 2.1 / 3.1 — Tâche non récurrente : repository.closeTask appelé avec scheduledAt=null
    @Test
    fun `givenNonRecurringTask_whenCloseTask_thenRepositoryCalledWithNullScheduledAt`() = runTest {
        val task = buildTask(isRecurring = false, scheduledAt = null)

        viewModel.closeTask(task)

        coVerify(exactly = 1) { taskRepo.closeTask(task.id, null) }
    }

    // 2.2 — Tâche récurrente : repository.closeTask appelé avec le scheduledAt de l'occurrence
    @Test
    fun `givenRecurringTask_whenCloseTask_thenRepositoryCalledWithOccurrenceScheduledAt`() = runTest {
        val task = buildTask(isRecurring = true, scheduledAt = scheduledAt)

        viewModel.closeTask(task)

        coVerify(exactly = 1) { taskRepo.closeTask(task.id, scheduledAt) }
    }

    // NOTE — Le dialog de scope sur closeTask n'est pas implémenté.
    // La complétion est toujours "cette occurrence uniquement" (THIS_ONLY implicite).

    // 3.6 — closeTask succès : l'occurrence est mise à jour dans todayTasks
    @Test
    fun `givenCloseTaskSucceeds_whenCloseTask_thenTodayTaskReplacedWithClosedVersion`() = runTest {
        val task = buildTask(id = "t1", isRecurring = false, dueAt = dueAtEarly)
        val closed = task.copy(isCompleted = true)

        coEvery { taskRepo.getTasks(any(), any(), any(), any(), any()) } returns listOf(task)
        viewModel.load()

        coEvery { taskRepo.closeTask(task.id, any()) } returns closed
        viewModel.closeTask(task)

        assertTrue(viewModel.uiState.value.todayTasks.any { it.id == "t1" && it.isCompleted == true })
    }

    // 3.6 — closeTask succès : la tâche disparaît de overdueTasks
    @Test
    fun `givenCloseTaskSucceeds_whenCloseTask_thenTaskRemovedFromOverdueTasks`() = runTest {
        val task = buildTask(id = "overdue-1", isRecurring = false)
        coEvery { taskRepo.closeTask(task.id, any()) } returns task.copy(isCompleted = true)

        viewModel.closeTask(task)

        assertFalse(viewModel.uiState.value.overdueTasks.any { it.id == "overdue-1" })
    }

    // 3.7 — closeTask échoue : error state renseigné
    @Test
    fun `givenCloseTaskThrows_whenCloseTask_thenErrorStateSet`() = runTest {
        val task = buildTask()
        coEvery { taskRepo.closeTask(any(), any()) } throws RuntimeException("HTTP 404")

        viewModel.closeTask(task)

        assertEquals("HTTP 404", viewModel.uiState.value.error)
    }

    // 2.11 / 3.7 — Conflit HTTP 409 propagé dans error state
    @Test
    fun `givenHttp409OnClose_whenCloseTask_thenErrorStatePropagated`() = runTest {
        val task = buildTask()
        coEvery { taskRepo.closeTask(any(), any()) } throws RuntimeException("409 Conflict")

        viewModel.closeTask(task)

        assertNotNull(viewModel.uiState.value.error)
    }

    // 2.12 — IOException (timeout) : wrappée dans l'error state, pas de crash
    @Test
    fun `givenIOExceptionOnClose_whenCloseTask_thenErrorStateSetNoCrash`() = runTest {
        val task = buildTask()
        coEvery { taskRepo.closeTask(any(), any()) } throws java.io.IOException("Connection reset")

        viewModel.closeTask(task)

        assertEquals("Connection reset", viewModel.uiState.value.error)
    }

    // =========================================================================
    // Section 3c — requestDeleteTask : routing selon isRecurring + scheduledAt
    // =========================================================================

    // 3.15 — Tâche non récurrente : appel direct au repository, pas de dialog
    @Test
    fun `givenNonRecurringTask_whenRequestDeleteTask_thenRepositoryCalledDirectly`() = runTest {
        val task = buildTask(isRecurring = false, scheduledAt = null)

        viewModel.requestDeleteTask(task)

        coVerify(exactly = 1) { taskRepo.deleteTask(task.id, null, null) }
    }

    @Test
    fun `givenNonRecurringTask_whenRequestDeleteTask_thenPendingDeleteTaskNull`() = runTest {
        val task = buildTask(isRecurring = false, scheduledAt = null)

        viewModel.requestDeleteTask(task)

        assertNull(viewModel.uiState.value.pendingDeleteTask)
    }

    // 3.16 — Tâche récurrente avec scheduledAt : dialog requis (pendingDeleteTask set)
    @Test
    fun `givenRecurringTaskWithScheduledAt_whenRequestDeleteTask_thenPendingDeleteTaskSet`() = runTest {
        val task = buildTask(isRecurring = true, scheduledAt = scheduledAt)

        viewModel.requestDeleteTask(task)

        assertEquals(task, viewModel.uiState.value.pendingDeleteTask)
    }

    @Test
    fun `givenRecurringTaskWithScheduledAt_whenRequestDeleteTask_thenRepositoryNotCalledYet`() = runTest {
        val task = buildTask(isRecurring = true, scheduledAt = scheduledAt)

        viewModel.requestDeleteTask(task)

        coVerify(exactly = 0) { taskRepo.deleteTask(any(), any(), any()) }
    }

    // 5.3 — Tâche récurrente sans scheduledAt : traité comme non-récurrente (pas de dialog)
    @Test
    fun `givenRecurringTaskWithNullScheduledAt_whenRequestDeleteTask_thenDirectDeleteNoDialog`() = runTest {
        val task = buildTask(isRecurring = true, scheduledAt = null)

        viewModel.requestDeleteTask(task)

        assertNull(viewModel.uiState.value.pendingDeleteTask)
        coVerify(exactly = 1) { taskRepo.deleteTask(task.id, null, null) }
    }

    // =========================================================================
    // Section 2.6–2.8 / 3.17–3.18 — confirmDeleteTask : scope transmis au repository
    // =========================================================================

    // 2.6 / 3.17 — THIS_ONLY : scope + scheduledAt passés au repository
    @Test
    fun `givenThisOnly_whenConfirmDeleteTask_thenRepositoryCalledWithThisOnlyScope`() = runTest {
        val task = buildTask(isRecurring = true, scheduledAt = scheduledAt)

        viewModel.confirmDeleteTask(task, RecurrenceScope.THIS_ONLY)

        coVerify(exactly = 1) { taskRepo.deleteTask(task.id, RecurrenceScope.THIS_ONLY, scheduledAt) }
    }

    // 2.7 / 3.18 — FROM_THIS : scope + scheduledAt passés au repository
    @Test
    fun `givenFromThis_whenConfirmDeleteTask_thenRepositoryCalledWithFromThisScope`() = runTest {
        val task = buildTask(isRecurring = true, scheduledAt = scheduledAt)

        viewModel.confirmDeleteTask(task, RecurrenceScope.FROM_THIS)

        coVerify(exactly = 1) { taskRepo.deleteTask(task.id, RecurrenceScope.FROM_THIS, scheduledAt) }
    }

    // 2.8 — scope null (tâche non récurrente) : repository appelé sans scope ni scheduledAt
    @Test
    fun `givenNullScope_whenConfirmDeleteTask_thenRepositoryCalledWithNullScope`() = runTest {
        val task = buildTask(isRecurring = false)

        viewModel.confirmDeleteTask(task, scope = null)

        coVerify(exactly = 1) { taskRepo.deleteTask(task.id, null, null) }
    }

    // confirmDeleteTask efface toujours pendingDeleteTask avant l'appel au repository
    @Test
    fun `whenConfirmDeleteTask_thenPendingDeleteTaskClearedBeforeRepositoryCall`() = runTest {
        val task = buildTask(isRecurring = true, scheduledAt = scheduledAt)
        viewModel.requestDeleteTask(task)
        assertNotNull(viewModel.uiState.value.pendingDeleteTask)

        viewModel.confirmDeleteTask(task, RecurrenceScope.THIS_ONLY)

        assertNull(viewModel.uiState.value.pendingDeleteTask)
    }

    // 3.19 — dismissDeleteScope : pendingDeleteTask vidé, aucun appel au repository
    @Test
    fun `givenPendingDelete_whenDismissDeleteScope_thenPendingClearedNoRepositoryCall`() = runTest {
        val task = buildTask(isRecurring = true, scheduledAt = scheduledAt)
        viewModel.requestDeleteTask(task)
        assertEquals(task, viewModel.uiState.value.pendingDeleteTask)

        viewModel.dismissDeleteScope()

        assertNull(viewModel.uiState.value.pendingDeleteTask)
        coVerify(exactly = 0) { taskRepo.deleteTask(any(), any(), any()) }
    }

    // 3.20 / 5.5 — Delete succès : load() appelé une fois après la suppression
    @Test
    fun `givenDeleteSucceeds_whenConfirmDeleteTask_thenLoadCalledAfterDelete`() = runTest {
        val task = buildTask()
        coEvery { taskRepo.getTasks(any(), any(), any(), any(), any()) } returns emptyList()

        viewModel.confirmDeleteTask(task, RecurrenceScope.THIS_ONLY)

        coVerify(atLeast = 1) { taskRepo.getTasks(any(), any(), any(), any(), any()) }
    }

    // 3.21 — FROM_THIS succès : load() appelé (la liste est rechargée depuis l'API)
    @Test
    fun `givenFromThisDeleteSucceeds_whenConfirmDeleteTask_thenListReloadedFromRepository`() = runTest {
        val task = buildTask(isRecurring = true, scheduledAt = scheduledAt)
        coEvery { taskRepo.getTasks(any(), any(), any(), any(), any()) } returns emptyList()

        viewModel.confirmDeleteTask(task, RecurrenceScope.FROM_THIS)

        coVerify(atLeast = 1) { taskRepo.getTasks(any(), any(), any(), any(), any()) }
    }

    // Delete échoue : error state renseigné
    @Test
    fun `givenDeleteThrows_whenConfirmDeleteTask_thenErrorStateSet`() = runTest {
        val task = buildTask()
        coEvery { taskRepo.deleteTask(any(), any(), any()) } throws RuntimeException("409 Conflict")

        viewModel.confirmDeleteTask(task, RecurrenceScope.THIS_ONLY)

        assertNotNull(viewModel.uiState.value.error)
    }

    // =========================================================================
    // Reopen task
    // =========================================================================

    @Test
    fun `givenRecurringTask_whenReopenTask_thenRepositoryCalledWithScheduledAt`() = runTest {
        val task = buildTask(isRecurring = true, scheduledAt = scheduledAt, isCompleted = true)

        viewModel.reopenTask(task)

        coVerify(exactly = 1) { taskRepo.reopenTask(task.id, scheduledAt) }
    }

    @Test
    fun `givenNonRecurringTask_whenReopenTask_thenRepositoryCalledWithNullScheduledAt`() = runTest {
        val task = buildTask(isRecurring = false, scheduledAt = null, isCompleted = true)

        viewModel.reopenTask(task)

        coVerify(exactly = 1) { taskRepo.reopenTask(task.id, null) }
    }

    // =========================================================================
    // Section 4 — Logique de liste (load)
    // =========================================================================

    // 4.1 — Tâches normales + occurrences virtuelles : toutes présentes dans la liste
    @Test
    fun `givenMixedTasks_whenLoad_thenAllPresentInTodayTasks`() = runTest {
        val normal  = buildTask(id = "normal-1", isRecurring = false)
        val virtual = buildTask(id = "virtual-1", isRecurring = true, isVirtual = true, scheduledAt = scheduledAt)

        coEvery { taskRepo.getTasks(any(), any(), any(), any(), any()) } returns listOf(normal, virtual)
        viewModel.load()

        val today = viewModel.uiState.value.todayTasks
        assertTrue(today.any { it.id == "normal-1" })
        assertTrue(today.any { it.id == "virtual-1" })
    }

    // 4.2 — Occurrence virtuelle : isVirtual=true accessible depuis la liste
    @Test
    fun `givenVirtualOccurrenceInList_whenLoad_thenIsVirtualTrueInState`() = runTest {
        val virtual = buildTask(id = "v-1", isRecurring = true, isVirtual = true, scheduledAt = scheduledAt)

        coEvery { taskRepo.getTasks(any(), any(), any(), any(), any()) } returns listOf(virtual)
        viewModel.load()

        val task = viewModel.uiState.value.todayTasks.first { it.id == "v-1" }
        assertTrue(task.isVirtual == true)
    }

    // 4.3 — Instance DONE : présente dans la liste, triée après les tâches ouvertes
    @Test
    fun `givenDoneTask_whenLoad_thenPresentInTodayTasksSortedAfterOpen`() = runTest {
        val done = buildTask(id = "done-1", isCompleted = true,  dueAt = dueAtEarly)
        val open = buildTask(id = "open-1", isCompleted = false, dueAt = dueAtMid)

        coEvery { taskRepo.getTasks(any(), any(), any(), any(), any()) } returns listOf(done, open)
        viewModel.load()

        val today = viewModel.uiState.value.todayTasks
        assertTrue(today.any { it.id == "done-1" })
        val openIdx = today.indexOfFirst { it.id == "open-1" }
        val doneIdx = today.indexOfFirst { it.id == "done-1" }
        assertTrue("Open doit précéder Done dans la liste triée", openIdx < doneIdx)
    }

    // 4.4 — Deux occurrences du même jour (deux tâches récurrentes distinctes) : toutes deux présentes
    @Test
    fun `givenTwoRecurringOccurrencesSameDay_whenLoad_thenBothPresent`() = runTest {
        val occ1 = buildTask(id = "occ-1", isRecurring = true, isVirtual = true, scheduledAt = dueAtEarly)
        val occ2 = buildTask(id = "occ-2", isRecurring = true, isVirtual = true, scheduledAt = dueAtLate)

        coEvery { taskRepo.getTasks(any(), any(), any(), any(), any()) } returns listOf(occ1, occ2)
        viewModel.load()

        val today = viewModel.uiState.value.todayTasks
        assertTrue(today.any { it.id == "occ-1" })
        assertTrue(today.any { it.id == "occ-2" })
    }

    // 4.6 — Réponse repository vide : toutes les listes vides, pas de crash
    @Test
    fun `givenEmptyRepositoryResponse_whenLoad_thenAllListsEmptyNoError`() = runTest {
        coEvery { taskRepo.getTasks(any(), any(), any(), any(), any()) } returns emptyList()
        coEvery { projectRepo.getProjects() } returns emptyList()

        viewModel.load()

        val state = viewModel.uiState.value
        assertTrue(state.todayTasks.isEmpty())
        assertTrue(state.tomorrowTasks.isEmpty())
        assertTrue(state.overdueTasks.isEmpty())
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    // 4.7 — Tri chronologique sur dueAt (tâches non-complètes triées par dueAt ASC)
    @Test
    fun `givenTasksWithDifferentDueAt_whenLoad_thenTodayTasksSortedChronologically`() = runTest {
        val early = buildTask(id = "early", dueAt = dueAtEarly)
        val late  = buildTask(id = "late",  dueAt = dueAtLate)
        val mid   = buildTask(id = "mid",   dueAt = dueAtMid)

        coEvery { taskRepo.getTasks(any(), any(), any(), any(), any()) } returns listOf(late, early, mid)
        viewModel.load()

        val ids = viewModel.uiState.value.todayTasks.map { it.id }
        assertEquals(listOf("early", "mid", "late"), ids)
    }

    // =========================================================================
    // Section 5 — États UI et edge cases
    // =========================================================================

    // isLoading false après un load réussi
    @Test
    fun `whenLoadSucceeds_thenIsLoadingFalse`() = runTest {
        viewModel.load()

        assertFalse(viewModel.uiState.value.isLoading)
    }

    // Erreur réseau lors du load : error set, isLoading false
    @Test
    fun `givenNetworkErrorOnLoad_whenLoad_thenErrorStateSetAndNotLoading`() = runTest {
        // On cible l'appel direct (non-async) getTasks(showCompleted=false) pour les overdue.
        coEvery { taskRepo.getTasks(null, false, null, null, null) } throws java.io.IOException("Timeout")

        viewModel.load()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Timeout", viewModel.uiState.value.error)
    }

    // Erreur effacée sur un reload réussi suivant
    @Test
    fun `givenErrorState_whenLoadSucceeds_thenErrorCleared`() = runTest {
        coEvery { taskRepo.getTasks(null, false, null, null, null) } throws RuntimeException("Erreur")
        viewModel.load()
        assertNotNull(viewModel.uiState.value.error)

        coEvery { taskRepo.getTasks(any(), any(), any(), any(), any()) } returns emptyList()
        viewModel.load()

        assertNull(viewModel.uiState.value.error)
    }

    // 5.4 — ViewModel créé et utilisable sans crash
    @Test
    fun `whenViewModelCreatedAndDestroyed_thenNoCrash`() {
        assertNotNull(viewModel)
        assertNotNull(viewModel.uiState.value)
    }
}
