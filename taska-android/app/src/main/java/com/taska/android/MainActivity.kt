package com.taska.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.taska.android.data.api.RetrofitClient
import com.taska.android.ui.addtask.AddTaskBottomSheet
import com.taska.android.ui.addtask.AddTaskViewModel
import com.taska.android.ui.drawer.WithDrawer
import com.taska.android.ui.inbox.InboxScreen
import com.taska.android.ui.inbox.InboxViewModel
import com.taska.android.ui.shared.BottomNavBar
import com.taska.android.ui.shared.NavDestination
import com.taska.android.ui.theme.TaskaTheme

class MainActivity : ComponentActivity() {

    private val inboxViewModel: InboxViewModel by viewModels()

    override fun onResume() {
        super.onResume()
        inboxViewModel.load()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RetrofitClient.init(this)

        enableEdgeToEdge()
        setContent {
            TaskaTheme {
                WithDrawer(
                    onInboxSelected = { /* already on inbox, do nothing */ },
                    onProjectSelected = { projectId ->
                        startActivity(
                            Intent(this, ProjectActivity::class.java)
                                .putExtra("project_id", projectId)
                                .putExtra("nav_current", NavDestination.INBOX.name)
                        )
                    }
                ) {
                    InboxRoot(
                        inboxViewModel = inboxViewModel,
                        onNavigate = { dest ->
                            when (dest) {
                                NavDestination.TODAY -> startActivity(
                                    Intent(this, TodayActivity::class.java)
                                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                )
                                NavDestination.DAY -> startActivity(
                                    Intent(this, DayActivity::class.java)
                                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                )
                                NavDestination.WEEK -> startActivity(
                                    Intent(this, WeekActivity::class.java)
                                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                )
                                NavDestination.TRACKER -> startActivity(
                                    Intent(this, TrackerActivity::class.java)
                                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                )
                                NavDestination.INBOX -> Unit
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun InboxRoot(inboxViewModel: InboxViewModel, onNavigate: (NavDestination) -> Unit) {
    val context = LocalContext.current
    val addTaskViewModel: AddTaskViewModel = viewModel()
    var showAddTask by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        InboxScreen(
            viewModel = inboxViewModel,
            onTaskClick = { taskId ->
                context.startActivity(
                    Intent(context, TaskDetailActivity::class.java).apply {
                        putExtra("task_id", taskId)
                    }
                )
            },
            onSearch = { startActivity(Intent(context, SearchActivity::class.java)) },
            modifier = Modifier.weight(1f)
        )
        BottomNavBar(
            current = NavDestination.INBOX,
            onNavigate = onNavigate,
            onAddClick = { showAddTask = true }
        )
    }

    if (showAddTask) {
        AddTaskBottomSheet(
            viewModel = addTaskViewModel,
            onDismiss = { showAddTask = false },
            onTaskCreated = {
                showAddTask = false
                inboxViewModel.load()
            }
        )
    }
}
