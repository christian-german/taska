package com.taska.android.ui.inbox

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
import androidx.compose.material.icons.outlined.FilterList
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taska.android.data.model.LabelDto
import com.taska.android.data.model.TaskDto

private val AppBackground = Color(0xFFF6F8FA)
private val TextPrimary = Color(0xFF17233D)
private val TextSecondary = Color(0xFF78828F)
private val DividerColor = Color(0xFFD9E1E8)
private val CheckboxBorder = Color(0xFFAAAAAA)
private val DefaultLabelColor = Color(0xFFFF8FAD)

@Composable
fun InboxScreen(
    viewModel: InboxViewModel,
    onTaskClick: (String) -> Unit = {},
    onSearch: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        InboxHeader(taskCount = uiState.tasks.size, onSearch = onSearch)

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
                    TaskList(
                        tasks = uiState.tasks,
                        labels = uiState.labels,
                        onTaskComplete = viewModel::closeTask,
                        onTaskClick = onTaskClick
                    )
                }
            }
        }
    }
}

@Composable
private fun InboxHeader(taskCount: Int, onSearch: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        fontFamily = com.taska.android.ui.theme.Archivo,
                        fontStyle = FontStyle.Italic,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextPrimary
                    )
                ) { append("Inbox") }
                append("  ")
                withStyle(
                    SpanStyle(
                        fontFamily = com.taska.android.ui.theme.Archivo,
                        fontSize = 14.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Normal,
                        fontStyle = FontStyle.Normal
                    )
                ) { append("$taskCount à trier") }
            },
            modifier = Modifier.weight(1f)
        )
        com.taska.android.ui.shared.SearchAction(onSearch)
        IconButton(onClick = {}) {
            Icon(
                imageVector = Icons.Outlined.FilterList,
                contentDescription = "Filtrer",
                tint = TextPrimary
            )
        }
    }
}

@Composable
private fun TaskList(
    tasks: List<TaskDto>,
    labels: Map<String, LabelDto>,
    onTaskComplete: (String) -> Unit,
    onTaskClick: (String) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(tasks, key = { it.id }) { task ->
            TaskItem(
                task = task,
                labels = labels,
                onComplete = { onTaskComplete(task.id) },
                onClick = { onTaskClick(task.id) }
            )
            HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
        }
        item {
            AddTaskHint()
        }
    }
}

@Composable
private fun TaskItem(
    task: TaskDto,
    labels: Map<String, LabelDto>,
    onComplete: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .border(1.5.dp, CheckboxBorder, CircleShape)
                .clickable { onComplete() }
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = task.content,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    lineHeight = 22.sp
                )
            )
            val taskLabels = task.labels?.filter { it.isNotBlank() }.orEmpty()
            if (taskLabels.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    taskLabels.forEach { labelName ->
                        LabelChip(name = labelName, labelDto = labels[labelName])
                    }
                }
            }
        }
    }
}

@Composable
private fun LabelChip(name: String, labelDto: LabelDto?) {
    val dotColor = labelDto?.color?.let { parseHexColor(it) } ?: DefaultLabelColor
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(8.dp)) {
            drawCircle(color = dotColor)
        }
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = name,
            style = TextStyle(
                fontSize = 13.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Normal
            )
        )
    }
}

@Composable
private fun AddTaskHint() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "+ appui long pour glisser vers un projet",
            style = TextStyle(
                fontFamily = com.taska.android.ui.theme.Archivo,
                fontSize = 13.sp,
                color = TextSecondary
            )
        )
    }
}

private fun parseHexColor(hex: String): Color? = try {
    Color(android.graphics.Color.parseColor(if (hex.startsWith("#")) hex else "#$hex"))
} catch (e: Exception) {
    null
}
