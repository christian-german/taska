package com.taska.android.ui.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.taska.android.ui.shared.TaskItem

@Composable
fun SearchScreen(viewModel: SearchViewModel, onBack: () -> Unit, onTaskClick: (String) -> Unit) {
    val state by viewModel.uiState.collectAsState()
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::updateQuery,
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            placeholder = { Text("Rechercher une tâche") },
            leadingIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Retour")
                }
            },
            trailingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            singleLine = true,
        )
        Box(Modifier.fillMaxSize()) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.error != null -> Text("Erreur : ${state.error}", Modifier.align(Alignment.Center).padding(16.dp))
                state.query.isBlank() -> Text("Saisissez du texte pour rechercher vos tâches", Modifier.align(Alignment.Center).padding(16.dp))
                state.results.isEmpty() -> Text("Aucune tâche trouvée", Modifier.align(Alignment.Center).padding(16.dp))
                else -> LazyColumn(Modifier.fillMaxSize()) {
                    items(state.results, key = { it.id }) { task ->
                        TaskItem(
                            task = task,
                            project = null,
                            isOverdue = false,
                            onToggle = {},
                            onClick = { onTaskClick(task.id) },
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}
