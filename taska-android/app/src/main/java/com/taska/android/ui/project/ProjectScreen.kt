package com.taska.android.ui.project

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taska.android.ui.shared.TaskItem
import com.taska.android.ui.shared.parseHexColor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val AppBackground = Color(0xFFF6F8FA)
private val TextPrimary = Color(0xFF17233D)
private val TextSecondary = Color(0xFF78828F)
private val DividerColor = Color(0xFFD9E1E8)

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
            .background(MaterialTheme.colorScheme.background)
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
                    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                        .format(Calendar.getInstance().time)

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            // En-tête projet
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
                                        fontFamily = com.taska.android.ui.theme.Archivo,
                                        fontStyle = FontStyle.Italic,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = TextPrimary
                                    )
                                )
                            }

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
                                    fontFamily = com.taska.android.ui.theme.Archivo,
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                            )
                            HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                        }

                        items(uiState.tasks, key = { it.id }) { task ->
                            val isOverdue = task.isCompleted != true &&
                                task.dueAt != null && task.dueAt.substring(0, 10) < todayStr

                            // Le projet n'est pas affiché (redondant dans la vue projet)
                            TaskItem(
                                task = task,
                                project = null,
                                isOverdue = isOverdue,
                                onToggle = { viewModel.closeTask(task.id) },
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
