package com.taska.android.ui.shared

import android.content.Context
import android.widget.Toast
import com.taska.android.R

object TaskCreationFeedback {
    fun show(context: Context) {
        Toast.makeText(context.applicationContext, R.string.task_created, Toast.LENGTH_SHORT).show()
    }
}
