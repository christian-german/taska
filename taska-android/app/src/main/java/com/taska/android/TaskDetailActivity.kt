package com.taska.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.taska.android.ui.taskdetail.TaskDetailScreen
import com.taska.android.ui.taskdetail.TaskDetailViewModel
import com.taska.android.ui.theme.TaskaTheme

class TaskDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaskaTheme {
                val viewModel: TaskDetailViewModel = viewModel()
                TaskDetailScreen(
                    viewModel = viewModel,
                    onClose = { finish() }
                )
            }
        }
    }
}
