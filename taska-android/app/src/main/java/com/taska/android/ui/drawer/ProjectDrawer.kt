package com.taska.android.ui.drawer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.MoveToInbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.taska.android.ui.theme.frostedChrome
import com.taska.android.data.model.ProjectDto
import kotlinx.coroutines.launch
import kotlin.math.abs

private val DrawerBg = Color(0xFFF6F8FA)
private val TextPrimary = Color(0xFF17233D)
private val TextSecondary = Color(0xFF78828F)
private val OrangeAccent = Color(0xFF14B37D)
private val DividerColor = Color(0xFFD9E1E8)

@Composable
fun WithDrawer(
    onInboxSelected: () -> Unit,
    onProjectSelected: (String) -> Unit,
    content: @Composable () -> Unit
) {
    val drawerViewModel: DrawerViewModel = viewModel()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = false,
        drawerContent = {
            ProjectDrawer(
                viewModel = drawerViewModel,
                onClose = { scope.launch { drawerState.close() } },
                onInboxClick = {
                    scope.launch { drawerState.close() }
                    onInboxSelected()
                },
                onProjectClick = { projectId ->
                    scope.launch { drawerState.close() }
                    onProjectSelected(projectId)
                }
            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(drawerState) {
                    val edgePx = 40.dp.toPx()
                    val minSwipePx = 50.dp.toPx()
                    awaitEachGesture {
                        var startX = 0f
                        var gotDown = false
                        while (!gotDown) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val down = event.changes.firstOrNull { it.pressed && !it.previousPressed }
                            if (down != null) { startX = down.position.x; gotDown = true }
                        }
                        var totalDx = 0f
                        var totalDy = 0f
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val change = event.changes.firstOrNull() ?: break
                            totalDx += change.position.x - change.previousPosition.x
                            totalDy += change.position.y - change.previousPosition.y
                            if (!change.pressed) break
                        }
                        if (startX < edgePx && totalDx > minSwipePx && totalDx > abs(totalDy)) {
                            scope.launch { drawerState.open() }
                        }
                    }
                }
        ) {
            content()
        }
    }
}

@Composable
private fun ProjectDrawer(
    viewModel: DrawerViewModel,
    onClose: () -> Unit,
    onInboxClick: () -> Unit,
    onProjectClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.85f)
            .frostedChrome()
            .statusBarsPadding()
            .pointerInput(Unit) {
                val minSwipePx = 80.dp.toPx()
                awaitEachGesture {
                    var totalDx = 0f
                    var totalDy = 0f
                    var gotDown = false
                    while (!gotDown) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val down = event.changes.firstOrNull { it.pressed && !it.previousPressed }
                        if (down != null) gotDown = true
                    }
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val change = event.changes.firstOrNull() ?: break
                        totalDx += change.position.x - change.previousPosition.x
                        totalDy += change.position.y - change.previousPosition.y
                        if (!change.pressed) break
                    }
                    if (totalDx < -minSwipePx && abs(totalDx) > abs(totalDy) * 1.5f) {
                        onClose()
                    }
                }
            }
    ) {
        // Header: logo + close button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "taska",
                    style = TextStyle(
                        fontFamily = com.taska.android.ui.theme.Archivo,
                        fontStyle = FontStyle.Italic,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextPrimary
                    )
                )
                Text(
                    text = "_",
                    style = TextStyle(
                        fontFamily = com.taska.android.ui.theme.Archivo,
                        fontStyle = FontStyle.Italic,
                        fontSize = 26.sp,
                        color = OrangeAccent
                    )
                )
            }
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Fermer",
                    tint = TextSecondary
                )
            }
        }

        // Scrollable project tree
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Inbox row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onInboxClick() }
                    .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.MoveToInbox,
                    contentDescription = "Inbox",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Inbox",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal, color = TextPrimary, lineHeight = 22.sp)
                )
            }
            HorizontalDivider(color = DividerColor, thickness = 0.5.dp)

            // PROJETS section label
            Text(
                text = "PROJETS",
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
                style = TextStyle(
                    fontFamily = com.taska.android.ui.theme.Archivo,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Medium
                )
            )

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(24.dp),
                    color = TextPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                uiState.childrenMap[null].orEmpty().forEach { project ->
                    ProjectTreeItem(
                        project = project,
                        depth = 0,
                        childrenMap = uiState.childrenMap,
                        expandedIds = uiState.expandedIds,
                        onToggleExpand = viewModel::toggleExpand,
                        onProjectClick = onProjectClick
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectTreeItem(
    project: ProjectDto,
    depth: Int,
    childrenMap: Map<String?, List<ProjectDto>>,
    expandedIds: Set<String>,
    onToggleExpand: (String) -> Unit,
    onProjectClick: (String) -> Unit
) {
    val children = childrenMap[project.id].orEmpty()
    val hasChildren = children.isNotEmpty()
    val isExpanded = project.id in expandedIds
    val dotColor = project.color?.let { parseHexColor(it) } ?: TextSecondary

    val startPad = (16 + depth * 16).dp
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProjectClick(project.id) }
            .padding(start = startPad, end = 16.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (hasChildren) {
            Icon(
                imageVector = if (isExpanded) Icons.Outlined.KeyboardArrowDown else Icons.Outlined.ChevronRight,
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
                    .clickable(onClick = { onToggleExpand(project.id) }),
                tint = TextSecondary
            )
        } else {
            Spacer(modifier = Modifier.width(20.dp))
        }

        Spacer(modifier = Modifier.width(8.dp))

        Canvas(modifier = Modifier.size(10.dp)) {
            drawCircle(color = dotColor)
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = project.name,
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = TextPrimary,
                lineHeight = 22.sp
            ),
            modifier = Modifier.weight(1f)
        )
    }

    if (isExpanded) {
        children.forEach { child ->
            ProjectTreeItem(
                project = child,
                depth = depth + 1,
                childrenMap = childrenMap,
                expandedIds = expandedIds,
                onToggleExpand = onToggleExpand,
                onProjectClick = onProjectClick
            )
        }
    }
}

private fun parseHexColor(hex: String): Color = try {
    Color(android.graphics.Color.parseColor(if (hex.startsWith("#")) hex else "#$hex"))
} catch (e: Exception) {
    Color(0xFF9A9A9A)
}
