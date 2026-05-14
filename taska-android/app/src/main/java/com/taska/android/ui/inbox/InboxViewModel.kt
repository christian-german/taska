package com.taska.android.ui.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taska.android.data.api.RetrofitClient
import com.taska.android.data.model.LabelDto
import com.taska.android.data.model.TaskDto
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InboxUiState(
    val tasks: List<TaskDto> = emptyList(),
    val labels: Map<String, LabelDto> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class InboxViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(InboxUiState())
    val uiState: StateFlow<InboxUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val projectsDeferred = async { RetrofitClient.api.getProjects() }
                val labelsDeferred = async { RetrofitClient.api.getLabels() }

                val projects = projectsDeferred.await()
                val labelsList = labelsDeferred.await()
                val labelsMap = labelsList.associateBy { it.name }

                val inboxProject = projects.firstOrNull { it.isInboxProject == true }
                val tasks = if (inboxProject != null) {
                    RetrofitClient.api.getTasks(projectId = inboxProject.id)
                } else {
                    emptyList()
                }

                _uiState.update {
                    it.copy(tasks = tasks, labels = labelsMap, isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun closeTask(taskId: String) {
        viewModelScope.launch {
            try {
                RetrofitClient.api.closeTask(taskId)
                _uiState.update { state ->
                    state.copy(tasks = state.tasks.filter { it.id != taskId })
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
