package com.taska.android.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taska.android.data.model.TaskDto
import com.taska.android.data.repository.TaskRepository
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val tasks: List<TaskDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val results: List<TaskDto>
        get() = filterTasks(tasks, query)
}

internal fun filterTasks(tasks: List<TaskDto>, query: String): List<TaskDto> {
    val normalizedQuery = query.trim().lowercase(Locale.ROOT)
    if (normalizedQuery.isEmpty()) return emptyList()
    return tasks.filter { it.content.lowercase(Locale.ROOT).contains(normalizedQuery) }
}

class SearchViewModel(private val taskRepository: TaskRepository) : ViewModel() {
    constructor() : this(TaskRepository())

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val tasks = taskRepository.getTasks(showCompleted = true)
                _uiState.update { it.copy(tasks = tasks, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Impossible de charger les tâches") }
            }
        }
    }

    fun updateQuery(query: String) = _uiState.update { it.copy(query = query) }
}
