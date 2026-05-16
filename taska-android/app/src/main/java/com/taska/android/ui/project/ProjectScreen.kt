package com.taska.android.ui.project

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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.FolderOpen
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taska.android.data.model.TaskDto
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val AppBackground = Color(0xFFEAE5DC)
private val TextPrimary = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF9A9A9A)
private val DividerColor = Color(0xFFD5D0C8)
private val CheckboxBorder = Color(0xFFAAAAAA)
private val OverdueRed = Color(0xFFDD4433)

@Composable
fun ProjectScreen(
    viewModel: ProjectViewModel,
    onBack: () -> Unit,
    onTaskClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .background(AppBackground)
            .statusBarsPadding()
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Retour",
                    tint = TextPrimary
                )
            }
            Icon(
                imageVector = Icons.Outlined.FolderOpen,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            val breadcrumb = buildString {
                uiState.parentProject?.let { append(it.name); append(" / ") }
                uiState.project?.let { append(it.name) }
            }
            Text(
                text = breadcrumb,
                style = TextStyle(fontSize = 13.sp, color = TextSecondary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Outlined.MoreHoriz,
                    contentDescription = "Options",
                    tint = TextSecondary
                )
            }
        }

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
                    val project = uiState.project
                    val dotColor = project?.color?.let { parseHexColor(it) } ?: TextSecondary
                    val activeCount = uiState.tasks.count { it.isCompleted != true }

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            // Project title
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Canvas(modifier = Modifier.size(12.dp)) {
                                    drawCircle(color = dotColor)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = project?.name ?: "",
                                    style = TextStyle(
                                        fontFamily = FontFamily.Serif,
                                        fontStyle = FontStyle.Italic,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = TextPrimary
                                    )
                                )
                            }

                            // Stats
                            val statsText = buildString {
                                append("$activeCount tâche${if (activeCount != 1) "s" else ""}")
                                if (uiState.overdueCount > 0) {
                                    append(" · ${uiState.overdueCount} en retard")
                                }
                            }
                            Text(
                                text = statsText,
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                            )
                            HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                        }

                        items(uiState.tasks, key = { it.id }) { task ->
                            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                                .format(Calendar.getInstance().time)
                            val isOverdue = task.isCompleted != true &&
                                task.dueAt != null && task.dueAt.substring(0, 10) < todayStr

                            ProjectTaskItem(
                                task = task,
                                isOverdue = isOverdue,
                                onComplete = { viewModel.closeTask(task.id) },
                                onClick = { onTaskClick(task.id) }
                            )
                            HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectTaskItem(
    task: TaskDto,
    isOverdue: Boolean,
    onComplete: () -> Unit,
    onClick: () -> Unit
) {
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
                .clip(CircleShape)
                .border(1.5.dp, if (isOverdue) OverdueRed else CheckboxBorder, CircleShape)
                .clickable { onComplete() }
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column {
            Text(
                text = task.content,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isOverdue) OverdueRed else TextPrimary,
                    lineHeight = 22.sp
                )
            )
            TaskMetaRow(task = task, isOverdue = isOverdue)
        }
    }
}

@Composable
private fun TaskMetaRow(task: TaskDto, isOverdue: Boolean) {
    val timeStr = if (!task.allDay) task.dueAt?.let { formatTime(it) } else null
    val estimateStr = task.estimateMinutes?.let { formatEstimate(it) }
    if (timeStr == null && estimateStr == null) return

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
    }
}

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

private fun parseHexColor(hex: String): Color = try {
    Color(android.graphics.Color.parseColor(if (hex.startsWith("#")) hex else "#$hex"))
} catch (e: Exception) {
    Color(0xFF9A9A9A)
}
