@file:OptIn(ExperimentalMaterial3Api::class)

package com.taska.android.ui.taskdetail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CalendarViewWeek
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.taska.android.data.model.LabelDto
import com.taska.android.data.model.ProjectDto
import com.taska.android.data.model.TaskDto
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Locale

private val AppBackground = Color(0xFFEAE5DC)
private val TextPrimary = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF9A9A9A)
private val OverdueColor = Color(0xFFDD4433)
private val DividerColor = Color(0xFFD5D0C8)
private val Orange = Color(0xFFE8763A)
private val GreenDone = Color(0xFF4CAF50)

private enum class ActivePicker { DATE, CALENDAR, TIME, PROJECT, DURATION, LABELS, PRIORITY }

@Composable
fun TaskDetailScreen(
    viewModel: TaskDetailViewModel,
    onClose: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val task = state.task

    var titleEdit by remember(task?.content) { mutableStateOf(task?.content ?: "") }
    var descEdit by remember(task?.description) { mutableStateOf(task?.description ?: "") }
    var activePicker by remember { mutableStateOf<ActivePicker?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
    ) {
        TopBar(onClose = onClose, onDeleteClick = { showDeleteConfirm = true })

        Box(modifier = Modifier.weight(1f)) {
            when {
                state.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = TextPrimary
                )
                state.error != null -> Text(
                    text = "Erreur : ${state.error}",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    color = TextSecondary
                )
                task != null -> TaskContent(
                    task = task,
                    project = state.project,
                    subtasks = state.subtasks,
                    titleEdit = titleEdit,
                    onTitleChange = { titleEdit = it },
                    onTitleSave = {
                        if (titleEdit.isNotBlank() && titleEdit != task.content)
                            viewModel.updateContent(titleEdit)
                    },
                    descEdit = descEdit,
                    onDescChange = { descEdit = it },
                    onDescSave = {
                        if (descEdit != (task.description ?: ""))
                            viewModel.updateDescription(descEdit)
                    },
                    onPropertyClick = { picker ->
                        focusManager.clearFocus()
                        activePicker = picker
                    },
                    onToggleSubtask = viewModel::toggleSubtask,
                    onAddSubtask = viewModel::addSubtask
                )
            }
        }

        if (task != null) {
            BottomBar(
                timerStarted = state.timerStarted,
                onReporter = {
                    focusManager.clearFocus()
                    activePicker = ActivePicker.DATE
                },
                onStartTimer = { viewModel.startTimer() }
            )
        }
    }

    when (activePicker) {
        ActivePicker.DATE -> DateShortcutsDialog(
            hasDue = task?.dueAt != null,
            onSelect = { millis ->
                viewModel.rescheduleAllDay(millis)
                activePicker = ActivePicker.TIME
            },
            onOpenCalendar = { activePicker = ActivePicker.CALENDAR },
            onClear = { viewModel.clearDue(); activePicker = null },
            onDismiss = { activePicker = null }
        )
        ActivePicker.CALENDAR -> {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = viewModel.dueAtToMillis()
            )
            DatePickerDialog(
                onDismissRequest = { activePicker = null },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            viewModel.rescheduleAllDay(it)
                        }
                        activePicker = ActivePicker.TIME
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { activePicker = null }) { Text("Annuler") }
                }
            ) { DatePicker(state = datePickerState) }
        }
        ActivePicker.TIME -> {
            val dueAt = task?.dueAt
            val (initialHour, initialMinute) = if (dueAt != null && task.allDay == false) {
                try {
                    val zoned = java.time.Instant.parse(dueAt).atZone(java.time.ZoneId.systemDefault())
                    Pair(zoned.hour, zoned.minute)
                } catch (_: Exception) { Pair(9, 0) }
            } else Pair(9, 0)
            val timeState = rememberTimePickerState(
                initialHour = initialHour,
                initialMinute = initialMinute,
                is24Hour = true
            )
            TimePickerDialog(
                onConfirm = {
                    viewModel.rescheduleWithTime(
                        viewModel.dueAtToMillis(),
                        timeState.hour,
                        timeState.minute
                    )
                    activePicker = null
                },
                onAllDay = { activePicker = null },
                onDismiss = { activePicker = null }
            ) {
                TimePicker(state = timeState)
            }
        }
        ActivePicker.PROJECT -> ProjectPickerDialog(
            projects = state.projects,
            currentProjectId = task?.projectId,
            onSelect = { projectId ->
                viewModel.updateProject(projectId)
                activePicker = null
            },
            onDismiss = { activePicker = null }
        )
        ActivePicker.DURATION -> DurationPickerDialog(
            currentMinutes = task?.estimateMinutes,
            onSelect = { minutes ->
                viewModel.updateEstimate(minutes)
                activePicker = null
            },
            onDismiss = { activePicker = null }
        )
        ActivePicker.LABELS -> LabelsPickerDialog(
            allLabels = state.allLabels,
            selected = task?.labels.orEmpty(),
            onConfirm = { labels ->
                viewModel.updateLabels(labels)
                activePicker = null
            },
            onDismiss = { activePicker = null }
        )
        ActivePicker.PRIORITY -> PriorityPickerDialog(
            currentPriority = task?.priority,
            onSelect = { priority ->
                viewModel.updatePriority(priority)
                activePicker = null
            },
            onDismiss = { activePicker = null }
        )
        null -> {}
    }

    if (showDeleteConfirm) {
        DeleteConfirmDialog(
            onConfirm = { viewModel.deleteTask(onClose) },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}

@Composable
private fun TopBar(onClose: () -> Unit, onDeleteClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClose) {
            Icon(Icons.Filled.Close, contentDescription = "Fermer", tint = TextPrimary)
        }
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onDeleteClick) {
            Icon(Icons.Outlined.Delete, contentDescription = "Supprimer", tint = TextPrimary)
        }
        IconButton(onClick = {}) {
            Icon(Icons.Outlined.MoreVert, contentDescription = "Plus", tint = TextPrimary)
        }
    }
}

@Composable
private fun TaskContent(
    task: TaskDto,
    project: ProjectDto?,
    subtasks: List<TaskDto>,
    titleEdit: String,
    onTitleChange: (String) -> Unit,
    onTitleSave: () -> Unit,
    descEdit: String,
    onDescChange: (String) -> Unit,
    onDescSave: () -> Unit,
    onPropertyClick: (ActivePicker) -> Unit,
    onToggleSubtask: (TaskDto) -> Unit,
    onAddSubtask: (String) -> Unit
) {
    var showAddSubtask by remember { mutableStateOf(false) }
    var newSubtaskContent by remember { mutableStateOf("") }
    val doneCount = subtasks.count { it.isCompleted == true }
    val taskLabels = task.labels?.filter { it.isNotBlank() }.orEmpty()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Title
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .border(2.dp, priorityColor(task.priority), CircleShape)
                )
                Spacer(Modifier.width(14.dp))
                BasicTextField(
                    value = titleEdit,
                    onValueChange = onTitleChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (!it.hasFocus) onTitleSave() },
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextPrimary,
                        lineHeight = 36.sp
                    ),
                    cursorBrush = SolidColor(TextPrimary)
                )
            }
        }

        // Description
        item {
            BasicTextField(
                value = descEdit,
                onValueChange = onDescChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 62.dp, end = 20.dp, bottom = 12.dp)
                    .onFocusChanged { if (!it.hasFocus) onDescSave() },
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    color = TextSecondary,
                    lineHeight = 20.sp
                ),
                cursorBrush = SolidColor(TextPrimary),
                decorationBox = { inner ->
                    Box {
                        if (descEdit.isEmpty()) {
                            Text(
                                "Ajouter une description…",
                                style = TextStyle(fontSize = 14.sp, color = Color(0xFFBBBBBB))
                            )
                        }
                        inner()
                    }
                }
            )
        }

        item { HorizontalDivider(color = DividerColor) }

        // ÉCHÉANCE
        item {
            PropertyRow(
                icon = Icons.Outlined.Schedule,
                label = "ÉCHÉANCE",
                value = task.dueAt?.let { formatDueDate(it, task.allDay) },
                valueColor = if (isOverdue(task.dueAt, task.allDay)) OverdueColor else TextPrimary,
                onClick = { onPropertyClick(ActivePicker.DATE) }
            )
            HorizontalDivider(color = DividerColor)
        }

        // PROJET
        item {
            PropertyRow(
                icon = Icons.Outlined.FolderOpen,
                label = "PROJET",
                onClick = { onPropertyClick(ActivePicker.PROJECT) },
                customValue = {
                    if (project != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val dotColor = project.color?.let { parseHexColor(it) } ?: Color.Gray
                            Canvas(modifier = Modifier.size(8.dp)) { drawCircle(dotColor) }
                            Spacer(Modifier.width(6.dp))
                            Text(project.name, style = TextStyle(fontSize = 14.sp, color = TextPrimary))
                        }
                    } else {
                        Text("—", style = TextStyle(fontSize = 14.sp, color = TextSecondary))
                    }
                }
            )
            HorizontalDivider(color = DividerColor)
        }

        // DURÉE
        item {
            PropertyRow(
                icon = Icons.Outlined.Timer,
                label = "DURÉE",
                value = task.estimateMinutes?.let { "${formatDuration(it)} estimées" },
                onClick = { onPropertyClick(ActivePicker.DURATION) }
            )
            HorizontalDivider(color = DividerColor)
        }

        // RAPPEL (grayed out)
        item {
            PropertyRow(
                icon = Icons.Outlined.Notifications,
                label = "RAPPEL",
                value = null,
                enabled = false,
                onClick = {}
            )
            HorizontalDivider(color = DividerColor)
        }

        // TAGS
        item {
            PropertyRow(
                icon = Icons.Outlined.Label,
                label = "TAGS",
                value = if (taskLabels.isNotEmpty()) taskLabels.joinToString(" · ") else null,
                onClick = { onPropertyClick(ActivePicker.LABELS) }
            )
            HorizontalDivider(color = DividerColor)
        }

        // PRIORITÉ
        item {
            PropertyRow(
                icon = Icons.Outlined.Flag,
                label = "PRIORITÉ",
                value = priorityLabel(task.priority),
                valueColor = priorityColor(task.priority),
                onClick = { onPropertyClick(ActivePicker.PRIORITY) }
            )
            HorizontalDivider(color = DividerColor)
        }

        // Subtasks header
        item {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "SOUS-TÂCHES ($doneCount/${subtasks.size})",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp,
                    color = TextSecondary
                ),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }

        items(subtasks, key = { it.id }) { subtask ->
            SubtaskItem(subtask = subtask, onToggle = { onToggleSubtask(subtask) })
        }

        // Add subtask row
        item {
            if (showAddSubtask) {
                val subtaskFocus = remember { FocusRequester() }
                var hadFocus by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    delay(100)
                    subtaskFocus.requestFocus()
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, Color(0xFFAAAAAA), CircleShape)
                    )
                    Spacer(Modifier.width(12.dp))
                    BasicTextField(
                        value = newSubtaskContent,
                        onValueChange = { newSubtaskContent = it },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(subtaskFocus)
                            .onFocusChanged { fs ->
                                if (fs.hasFocus) {
                                    hadFocus = true
                                } else if (hadFocus) {
                                    if (newSubtaskContent.isNotBlank()) {
                                        onAddSubtask(newSubtaskContent)
                                        newSubtaskContent = ""
                                    }
                                    showAddSubtask = false
                                }
                            },
                        textStyle = TextStyle(fontSize = 15.sp, color = TextPrimary),
                        cursorBrush = SolidColor(TextPrimary),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (newSubtaskContent.isNotBlank()) {
                                onAddSubtask(newSubtaskContent)
                                newSubtaskContent = ""
                            }
                            showAddSubtask = false
                        }),
                        decorationBox = { inner ->
                            Box {
                                if (newSubtaskContent.isEmpty()) {
                                    Text(
                                        "Nom de la sous-tâche…",
                                        style = TextStyle(fontSize = 15.sp, color = Color(0xFFBBBBBB))
                                    )
                                }
                                inner()
                            }
                        }
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAddSubtask = true }
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Ajouter une sous-tâche",
                        style = TextStyle(fontSize = 15.sp, color = TextSecondary)
                    )
                }
            }
        }
    }
}

@Composable
private fun PropertyRow(
    icon: ImageVector,
    label: String,
    value: String? = null,
    valueColor: Color = TextPrimary,
    enabled: Boolean = true,
    customValue: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    val tint = if (enabled) TextSecondary else Color(0xFFCCCCCC)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (enabled) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = TextStyle(
                fontSize = 11.sp,
                color = tint,
                letterSpacing = 0.5.sp,
                fontWeight = FontWeight.Medium
            )
        )
        Spacer(Modifier.weight(1f))
        when {
            customValue != null -> customValue()
            value != null -> Text(
                text = value,
                style = TextStyle(
                    fontSize = 14.sp,
                    color = if (enabled) valueColor else Color(0xFFCCCCCC)
                )
            )
            else -> Text(
                "—",
                style = TextStyle(fontSize = 14.sp, color = tint)
            )
        }
    }
}

@Composable
private fun SubtaskItem(subtask: TaskDto, onToggle: () -> Unit) {
    val done = subtask.isCompleted == true
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (done) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(GreenDone),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(13.dp))
            }
        } else {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, Color(0xFFAAAAAA), CircleShape)
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = subtask.content,
            style = TextStyle(
                fontSize = 15.sp,
                color = if (done) TextSecondary else TextPrimary,
                textDecoration = if (done) TextDecoration.LineThrough else TextDecoration.None
            )
        )
    }
}

@Composable
private fun BottomBar(
    timerStarted: Boolean,
    onReporter: () -> Unit,
    onStartTimer: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onReporter,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Outlined.CalendarMonth, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Reporter")
        }
        Button(
            onClick = onStartTimer,
            modifier = Modifier.weight(1f),
            enabled = !timerStarted,
            colors = ButtonDefaults.buttonColors(
                containerColor = Orange,
                disabledContainerColor = Color(0xFFF5C4A8)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Outlined.PlayArrow, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(if (timerStarted) "Timer démarré" else "Démarrer timer")
        }
    }
}

@Composable
private fun DateShortcutsDialog(
    hasDue: Boolean,
    onSelect: (Long) -> Unit,
    onOpenCalendar: () -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    val shortcuts = listOf(
        Triple("Aujourd'hui", Icons.Outlined.WbSunny, todayMillis()),
        Triple("Demain", Icons.Outlined.Event, tomorrowMillis()),
        Triple("Ce week-end", Icons.Outlined.CalendarViewWeek, nextSaturdayMillis()),
        Triple("Semaine prochaine", Icons.Outlined.CalendarMonth, nextMondayMillis())
    )
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(vertical = 8.dp)
        ) {
            Text(
                "Reporter à",
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
            shortcuts.forEach { (label, icon, millis) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(millis) }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon, null, tint = Color(0xFF555555), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(14.dp))
                    Text(label, modifier = Modifier.weight(1f))
                }
                HorizontalDivider(color = Color(0xFFF0F0F0))
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenCalendar() }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.CalendarToday, null, tint = Color(0xFF555555), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(14.dp))
                Text("Choisir une date…")
            }
            if (hasDue) {
                HorizontalDivider(color = Color(0xFFF0F0F0))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onClear() }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Close, null, tint = OverdueColor, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(14.dp))
                    Text("Supprimer l'échéance", color = OverdueColor)
                }
            }
        }
    }
}

@Composable
private fun TimePickerDialog(
    onConfirm: () -> Unit,
    onAllDay: () -> Unit,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(vertical = 16.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Choisir l'heure",
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            content()
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onAllDay) { Text("Journée entière") }
                Row {
                    TextButton(onClick = onDismiss) { Text("Annuler") }
                    TextButton(onClick = onConfirm) { Text("OK") }
                }
            }
        }
    }
}

@Composable
private fun ProjectPickerDialog(
    projects: List<ProjectDto>,
    currentProjectId: String?,
    onSelect: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(vertical = 8.dp)
        ) {
            Text(
                "Projet",
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(null) }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.FolderOpen, null, tint = Color(0xFF555555), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(14.dp))
                Text("Aucun projet", modifier = Modifier.weight(1f))
                if (currentProjectId == null) {
                    Icon(Icons.Filled.Check, null, tint = Orange, modifier = Modifier.size(18.dp))
                }
            }
            HorizontalDivider(color = Color(0xFFF0F0F0))
            projects.forEach { project ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(project.id) }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val dotColor = project.color?.let { parseHexColor(it) } ?: Color.Gray
                    Canvas(modifier = Modifier.size(10.dp)) { drawCircle(dotColor) }
                    Spacer(Modifier.width(14.dp))
                    Text(project.name, modifier = Modifier.weight(1f))
                    if (project.id == currentProjectId) {
                        Icon(Icons.Filled.Check, null, tint = Orange, modifier = Modifier.size(18.dp))
                    }
                }
                HorizontalDivider(color = Color(0xFFF0F0F0))
            }
        }
    }
}

@Composable
private fun DurationPickerDialog(
    currentMinutes: Int?,
    onSelect: (Int?) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(15, 30, 60, 90, 120, 180)
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(vertical = 8.dp)
        ) {
            Text(
                "Durée estimée",
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(null) }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.width(32.dp))
                Text("Aucune", modifier = Modifier.weight(1f), style = TextStyle(fontSize = 14.sp, color = TextSecondary))
                if (currentMinutes == null) {
                    Icon(Icons.Filled.Check, null, tint = Orange, modifier = Modifier.size(18.dp))
                }
            }
            HorizontalDivider(color = Color(0xFFF0F0F0))
            options.forEach { minutes ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(minutes) }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Timer, null, tint = Color(0xFF555555), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(14.dp))
                    Text(formatDuration(minutes), modifier = Modifier.weight(1f))
                    if (currentMinutes == minutes) {
                        Icon(Icons.Filled.Check, null, tint = Orange, modifier = Modifier.size(18.dp))
                    }
                }
                HorizontalDivider(color = Color(0xFFF0F0F0))
            }
        }
    }
}

@Composable
private fun LabelsPickerDialog(
    allLabels: List<LabelDto>,
    selected: List<String>,
    onConfirm: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var current by remember { mutableStateOf(selected.toSet()) }
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(vertical = 8.dp)
        ) {
            Text(
                "Tags",
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
            allLabels.forEach { label ->
                val isSelected = label.name in current
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            current = if (isSelected) current - label.name else current + label.name
                        }
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val dotColor = label.color?.let { parseHexColor(it) } ?: Color(0xFFFF8FAD)
                    Canvas(modifier = Modifier.size(10.dp)) { drawCircle(dotColor) }
                    Spacer(Modifier.width(14.dp))
                    Text(label.name, modifier = Modifier.weight(1f))
                    if (isSelected) {
                        Icon(Icons.Filled.Check, null, tint = Orange, modifier = Modifier.size(18.dp))
                    }
                }
                HorizontalDivider(color = Color(0xFFF0F0F0))
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) { Text("Annuler") }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = { onConfirm(current.toList()) }) { Text("Confirmer") }
            }
        }
    }
}

@Composable
private fun PriorityPickerDialog(
    currentPriority: Int?,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(
        Triple(1, "Urgente", Color(0xFFE83030)),
        Triple(2, "Haute", Color(0xFFFF8C00)),
        Triple(3, "Moyenne", Color(0xFF4287F5)),
        Triple(4, "Normale", Color(0xFFAAAAAA))
    )
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(vertical = 8.dp)
        ) {
            Text(
                "Priorité",
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
            options.forEach { (priority, label, color) ->
                val isSelected = priority == (currentPriority ?: 4)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(priority) }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Flag, null, tint = color, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(14.dp))
                    Text(label, modifier = Modifier.weight(1f))
                    if (isSelected) {
                        Icon(Icons.Filled.Check, null, tint = Orange, modifier = Modifier.size(18.dp))
                    }
                }
                HorizontalDivider(color = Color(0xFFF0F0F0))
            }
        }
    }
}

@Composable
private fun DeleteConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Supprimer la tâche ?") },
        text = { Text("Cette action est irréversible.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Supprimer", color = OverdueColor)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

// ---- Helpers ----

private fun priorityColor(priority: Int?): Color = when (priority) {
    1 -> Color(0xFFE83030)
    2 -> Color(0xFFFF8C00)
    3 -> Color(0xFF4287F5)
    else -> Color(0xFFAAAAAA)
}

private fun priorityLabel(priority: Int?): String = when (priority) {
    1 -> "Urgente"
    2 -> "Haute"
    3 -> "Moyenne"
    else -> "Normale"
}

private fun formatDuration(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return when {
        h == 0 -> "${m}min"
        m == 0 -> "${h}h"
        else -> "${h}h${m}"
    }
}

private fun formatDueDate(dueAt: String, allDay: Boolean): String {
    return try {
        val zoned = java.time.Instant.parse(dueAt).atZone(java.time.ZoneId.systemDefault())
        val cal = Calendar.getInstance().apply {
            set(zoned.year, zoned.monthValue - 1, zoned.dayOfMonth, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        fun zeroed(c: Calendar) = (c.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        fun sameDay(a: Calendar, b: Calendar) =
            a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
                    a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)

        val today = zeroed(Calendar.getInstance())
        val yesterday = zeroed(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) })
        val tomorrow = zeroed(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) })

        val dayPart = when {
            sameDay(cal, today) -> "aujourd'hui"
            sameDay(cal, yesterday) -> "hier"
            sameDay(cal, tomorrow) -> "demain"
            else -> {
                val month = cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.FRENCH) ?: ""
                "${zoned.dayOfMonth} $month"
            }
        }
        if (!allDay) {
            val h = zoned.hour.toString().padStart(2, '0')
            val m = zoned.minute.toString().padStart(2, '0')
            "$dayPart · $h:$m"
        } else {
            dayPart
        }
    } catch (_: Exception) {
        dueAt.substringBefore('T')
    }
}

private fun isOverdue(dueAt: String?, allDay: Boolean): Boolean {
    dueAt ?: return false
    return try {
        val zoned = java.time.Instant.parse(dueAt).atZone(java.time.ZoneId.systemDefault())
        val cal = Calendar.getInstance().apply {
            set(zoned.year, zoned.monthValue - 1, zoned.dayOfMonth,
                if (allDay) 23 else zoned.hour,
                if (allDay) 59 else zoned.minute, 0)
            set(Calendar.MILLISECOND, 0)
        }
        cal.before(Calendar.getInstance())
    } catch (_: Exception) { false }
}

private fun todayMillis() = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
}.timeInMillis

private fun tomorrowMillis() = Calendar.getInstance().apply {
    add(Calendar.DAY_OF_YEAR, 1)
    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
}.timeInMillis

private fun nextSaturdayMillis(): Long {
    val cal = Calendar.getInstance()
    var days = (Calendar.SATURDAY - cal.get(Calendar.DAY_OF_WEEK) + 7) % 7
    if (days == 0) days = 7
    return cal.apply {
        add(Calendar.DAY_OF_YEAR, days)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun nextMondayMillis(): Long {
    val cal = Calendar.getInstance()
    var days = (Calendar.MONDAY - cal.get(Calendar.DAY_OF_WEEK) + 7) % 7
    if (days == 0) days = 7
    return cal.apply {
        add(Calendar.DAY_OF_YEAR, days)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun parseHexColor(hex: String): Color? = try {
    Color(android.graphics.Color.parseColor(if (hex.startsWith("#")) hex else "#$hex"))
} catch (_: Exception) { null }
