package com.taska.android.ui.drawer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taska.android.data.api.RetrofitClient
import com.taska.android.data.model.ProjectDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DrawerUiState(
    val childrenMap: Map<String?, List<ProjectDto>> = emptyMap(),
    val expandedIds: Set<String> = emptySet(),
    val isLoading: Boolean = false
)

class DrawerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DrawerUiState())
    val uiState: StateFlow<DrawerUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val projects = RetrofitClient.api.getProjects()
                    .filter { it.isInboxProject != true }
                    .sortedBy { it.order }
                _uiState.update {
                    it.copy(childrenMap = projects.groupBy { p -> p.parentId }, isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun toggleExpand(projectId: String) {
        _uiState.update { state ->
            val next = state.expandedIds.toMutableSet()
            if (projectId in next) next.remove(projectId) else next.add(projectId)
            state.copy(expandedIds = next)
        }
    }
}
