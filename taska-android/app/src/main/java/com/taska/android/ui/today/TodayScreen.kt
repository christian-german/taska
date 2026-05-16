package com.taska.android.ui.today

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Schedule
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taska.android.data.model.ProjectDto
import com.taska.android.data.model.TaskDto
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val AppBackground = Color(0xFFEAE5DC)
private val TextPrimary = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF9A9A9A)
private val DividerColor = Color(0xFFD5D0C8)
private val CheckboxBorder = Color(0xFFAAAAAA)
private val OverdueRed = Color(0xFFDD4433)
private val CompletedGreen = Color(0xFF4CAF50)

@Composable
fun TodayScreen(
    viewModel: TodayViewModel,
    onTaskClick: (String) -> Unit = {},
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
                            if (task.isCompleted == true) viewModel.reopenTask(task.id)
                            else viewModel.closeTask(task.id)
                        }
                    )
                }
            }
        }
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
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextPrimary
                )
            )
            Text(
                text = "$dateStr · $todayCount tâche${if (todayCount != 1) "s" else ""}",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            )
            if (overdueCount > 0) {
                Text(
                    text = "  · $overdueCount en retard",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
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
    onTaskClick: (String) -> Unit,
    onTaskToggle: (TaskDto) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        if (uiState.overdueTasks.isNotEmpty()) {
            item {
                SectionHeader(title = "En retard", count = uiState.overdueTasks.size, titleColor = OverdueRed)
            }
            items(uiState.overdueTasks, key = { it.id }) { task ->
                TodayTaskItem(
                    task = task,
                    project = task.projectId?.let { uiState.projects[it] },
                    isOverdue = true,
                    onToggle = { onTaskToggle(task) },
                    onClick = { onTaskClick(task.id) }
                )
                HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }

        if (uiState.todayTasks.isNotEmpty()) {
            item {
                SectionHeader(title = "Aujourd'hui", count = uiState.todayTasks.size, titleColor = TextPrimary)
            }
            items(uiState.todayTasks, key = { it.id }) { task ->
                TodayTaskItem(
                    task = task,
                    project = task.projectId?.let { uiState.projects[it] },
                    isOverdue = false,
                    onToggle = { onTaskToggle(task) },
                    onClick = { onTaskClick(task.id) }
                )
                HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }

        if (uiState.tomorrowTasks.isNotEmpty()) {
            item {
                SectionHeader(title = "Demain", count = uiState.tomorrowTasks.size, titleColor = TextPrimary)
            }
            items(uiState.tomorrowTasks, key = { it.id }) { task ->
                TodayTaskItem(
                    task = task,
                    project = task.projectId?.let { uiState.projects[it] },
                    isOverdue = false,
                    onToggle = { onTaskToggle(task) },
                    onClick = { onTaskClick(task.id) }
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

@Composable
private fun TodayTaskItem(
    task: TaskDto,
    project: ProjectDto?,
    isOverdue: Boolean,
    onToggle: () -> Unit,
    onClick: () -> Unit
) {
    val isCompleted = task.isCompleted == true
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clickable { onToggle() },
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Terminé",
                    tint = CompletedGreen,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, if (isOverdue) OverdueRed else CheckboxBorder, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column {
            Text(
                text = task.content,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isCompleted) TextSecondary else TextPrimary,
                    lineHeight = 22.sp,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
            )
            TaskMeta(task = task, project = project, isOverdue = isOverdue)
        }
    }
}

@Composable
private fun TaskMeta(task: TaskDto, project: ProjectDto?, isOverdue: Boolean) {
    val timeStr = if (!task.allDay) task.dueAt?.let { formatTime(it) } else null
    val estimateStr = task.estimateMinutes?.let { formatEstimate(it) }
    val relativeDateStr = if (isOverdue) task.dueAt?.substring(0, 10)?.let { formatRelativeDate(it) } else null
    val projectColor = project?.color?.let { parseHexColor(it) } ?: Color(0xFF9A9A9A)
    val projectDisplay = buildProjectDisplay(task, project)

    if (timeStr == null && estimateStr == null && relativeDateStr == null && projectDisplay == null) return

    val accentColor = if (isOverdue) OverdueRed else TextSecondary

    Row(
        modifier = Modifier.padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (timeStr != null) {
            Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(13.dp)
            )
            Text(text = timeStr, style = TextStyle(fontSize = 13.sp, color = accentColor))
        }
        if (estimateStr != null) {
            Text(text = estimateStr, style = TextStyle(fontSize = 13.sp, color = accentColor))
        }
        if (relativeDateStr != null) {
            Text(text = relativeDateStr, style = TextStyle(fontSize = 13.sp, color = accentColor))
        }
        if (projectDisplay != null) {
            Canvas(modifier = Modifier.size(8.dp)) { drawCircle(color = projectColor) }
            Text(text = projectDisplay, style = TextStyle(fontSize = 13.sp, color = TextSecondary))
        }
    }
}

private fun buildProjectDisplay(task: TaskDto, project: ProjectDto?): String? = project?.name

private fun formatTime(isoDateTime: String): String? = try {
    val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(isoDateTime.take(19))
        ?: return null
    SimpleDateFormat("HH:mm", Locale.FRENCH).format(date)
} catch (e: Exception) {
    null
}

private fun formatEstimate(minutes: Int): String = when {
    minutes < 60 -> "${minutes}m"
    minutes % 60 == 0 -> "${minutes / 60}h"
    else -> "${minutes / 60}h${minutes % 60}m"
}

private fun formatRelativeDate(dueDateStr: String): String = try {
    val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val yesterdayStr = fmt.format(Calendar.getInstance().also { it.add(Calendar.DAY_OF_YEAR, -1) }.time)
    if (dueDateStr == yesterdayStr) "hier"
    else SimpleDateFormat("d MMM", Locale.FRENCH).format(fmt.parse(dueDateStr)!!)
} catch (e: Exception) {
    dueDateStr
}

private fun parseHexColor(hex: String): Color? = try {
    Color(android.graphics.Color.parseColor(if (hex.startsWith("#")) hex else "#$hex"))
} catch (e: Exception) {
    null
}
