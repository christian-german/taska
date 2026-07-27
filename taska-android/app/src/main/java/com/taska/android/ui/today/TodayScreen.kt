package com.taska.android.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taska.android.data.model.TaskDto
import com.taska.android.data.model.RecurrenceScope
import com.taska.android.ui.shared.RecurrenceScopeDialog
import com.taska.android.ui.shared.TaskItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val AppBackground = Color(0xFFF6F8FA)
private val TextPrimary = Color(0xFF17233D)
private val TextSecondary = Color(0xFF78828F)
private val DividerColor = Color(0xFFD9E1E8)
private val OverdueRed = Color(0xFFDD4433)

@Composable
fun TodayScreen(
    viewModel: TodayViewModel,
    onTaskClick: (taskId: String, scheduledAt: String?) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .background(AppBackground)
            .statusBarsPadding()
    ) {
        TodayHeader(
            overdueCount = uiState.overdueTasks.size,
            todayCount = uiState.todayTasks.size
        )

        Box(modifier = Modifier.weight(1f)) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = TextPrimary
                    )
                }
                uiState.error != null -> {
                    Text(
                        text = "Erreur : ${uiState.error}",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
                else -> {
                    TodayList(
                        uiState = uiState,
                        onTaskClick = onTaskClick,
                        onTaskToggle = { task ->
                            if (task.isCompleted == true) viewModel.reopenTask(task)
                            else viewModel.closeTask(task)
                        }
                    )
                }
            }
        }
    }

    uiState.pendingDeleteTask?.let { task ->
        RecurrenceScopeDialog(
            title = "Supprimer la récurrence",
            onThisOnly = { viewModel.confirmDeleteTask(task, RecurrenceScope.THIS_ONLY) },
            onFromThis = { viewModel.confirmDeleteTask(task, RecurrenceScope.FROM_THIS) },
            onDismiss = { viewModel.dismissDeleteScope() }
        )
    }
}

@Composable
private fun TodayHeader(overdueCount: Int, todayCount: Int) {
    val dateStr = SimpleDateFormat("EEE d MMM", Locale.FRENCH).format(Date())
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Aujourd'hui",
                style = TextStyle(
                    fontFamily = com.taska.android.ui.theme.Archivo,
                    fontStyle = FontStyle.Italic,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextPrimary
                )
            )
            Text(
                text = "$dateStr · $todayCount tâche${if (todayCount != 1) "s" else ""}",
                style = TextStyle(
                    fontFamily = com.taska.android.ui.theme.Archivo,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            )
            if (overdueCount > 0) {
                Text(
                    text = "  · $overdueCount en retard",
                    style = TextStyle(
                        fontFamily = com.taska.android.ui.theme.Archivo,
                        fontSize = 13.sp,
                        color = OverdueRed
                    )
                )
            }
        }
        IconButton(onClick = {}) {
            Icon(
                imageVector = Icons.Outlined.MoreHoriz,
                contentDescription = "Options",
                tint = TextSecondary
            )
        }
    }
}

@Composable
private fun TodayList(
    uiState: TodayUiState,
    onTaskClick: (taskId: String, scheduledAt: String?) -> Unit,
    onTaskToggle: (TaskDto) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        if (uiState.overdueTasks.isNotEmpty()) {
            item {
                SectionHeader(title = "En retard", count = uiState.overdueTasks.size, titleColor = OverdueRed)
            }
            items(uiState.overdueTasks, key = { occurrenceKey(it) }) { task ->
                TaskItem(
                    task = task,
                    project = task.projectId?.let { uiState.projects[it] },
                    isOverdue = true,
                    onToggle = { onTaskToggle(task) },
                    onClick = { onTaskClick(task.id, task.scheduledAt) }
                )
                HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }

        if (uiState.todayTasks.isNotEmpty()) {
            item {
                SectionHeader(title = "Aujourd'hui", count = uiState.todayTasks.size, titleColor = TextPrimary)
            }
            items(uiState.todayTasks, key = { occurrenceKey(it) }) { task ->
                TaskItem(
                    task = task,
                    project = task.projectId?.let { uiState.projects[it] },
                    isOverdue = false,
                    onToggle = { onTaskToggle(task) },
                    onClick = { onTaskClick(task.id, task.scheduledAt) }
                )
                HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }

        if (uiState.tomorrowTasks.isNotEmpty()) {
            item {
                SectionHeader(title = "Demain", count = uiState.tomorrowTasks.size, titleColor = TextPrimary)
            }
            items(uiState.tomorrowTasks, key = { occurrenceKey(it) }) { task ->
                TaskItem(
                    task = task,
                    project = task.projectId?.let { uiState.projects[it] },
                    isOverdue = false,
                    onToggle = { onTaskToggle(task) },
                    onClick = { onTaskClick(task.id, task.scheduledAt) }
                )
                HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int, titleColor: Color) {
    Text(
        text = "$title  ($count)",
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        style = TextStyle(
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = titleColor,
            letterSpacing = (-0.2).sp
        )
    )
}

private fun occurrenceKey(task: TaskDto): String =
    task.instanceId ?: if (task.scheduledAt != null) "${task.id}:${task.scheduledAt}" else task.id
