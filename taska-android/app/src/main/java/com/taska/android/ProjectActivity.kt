package com.taska.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.taska.android.ui.addtask.AddTaskBottomSheet
import com.taska.android.ui.addtask.AddTaskViewModel
import com.taska.android.ui.project.ProjectScreen
import com.taska.android.ui.project.ProjectViewModel
import com.taska.android.ui.shared.BottomNavBar
import com.taska.android.ui.shared.NavDestination
import com.taska.android.ui.theme.TaskaTheme

class ProjectActivity : ComponentActivity() {

    private val projectViewModel: ProjectViewModel by viewModels()

    override fun onResume() {
        super.onResume()
        projectViewModel.reload()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val projectId = intent.getStringExtra("project_id") ?: run { finish(); return }
        projectViewModel.load(projectId)

        enableEdgeToEdge()
        setContent {
            TaskaTheme {
                val context = LocalContext.current
                val addTaskViewModel: AddTaskViewModel = viewModel()
                var showAddTask by remember { mutableStateOf(false) }
                val navCurrent = intent.getStringExtra("nav_current")
                    ?.let { runCatching { NavDestination.valueOf(it) }.getOrNull() }
                    ?: NavDestination.INBOX

                Column(modifier = Modifier.fillMaxSize()) {
                    ProjectScreen(
                        viewModel = projectViewModel,
                        onBack = { finish() },
                        onTaskClick = { taskId ->
                            context.startActivity(
                                Intent(context, TaskDetailActivity::class.java)
                                    .putExtra("task_id", taskId)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                    BottomNavBar(
                        current = navCurrent,
                        onNavigate = { dest ->
                            when (dest) {
                                NavDestination.INBOX -> startActivity(
                                    Intent(this@ProjectActivity, MainActivity::class.java)
                                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                )
                                NavDestination.TODAY -> startActivity(
                                    Intent(this@ProjectActivity, TodayActivity::class.java)
                                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                )
                                NavDestination.WEEK -> startActivity(
                                    Intent(this@ProjectActivity, WeekActivity::class.java)
                                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                )
                                NavDestination.TRACKER -> startActivity(
                                    Intent(this@ProjectActivity, TrackerActivity::class.java)
                                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                )
                            }
                        },
                        onAddClick = { showAddTask = true }
                    )
                }

                if (showAddTask) {
                    AddTaskBottomSheet(
                        viewModel = addTaskViewModel,
                        onDismiss = { showAddTask = false },
                        onTaskCreated = {
                            showAddTask = false
                            projectViewModel.reload()
                        }
                    )
                }
            }
        }
    }
}
