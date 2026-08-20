package com.taska.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.taska.android.data.api.RetrofitClient
import com.taska.android.ui.search.SearchScreen
import com.taska.android.ui.search.SearchViewModel
import com.taska.android.ui.theme.TaskaTheme

class SearchActivity : ComponentActivity() {
    private val viewModel: SearchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitClient.init(this)
        enableEdgeToEdge()
        setContent {
            TaskaTheme {
                SearchScreen(
                    viewModel = viewModel,
                    onBack = onBackPressedDispatcher::onBackPressed,
                    onTaskClick = { taskId ->
                        startActivity(Intent(this, TaskDetailActivity::class.java).putExtra("task_id", taskId))
                    },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isFinishing) viewModel.load()
    }
}
