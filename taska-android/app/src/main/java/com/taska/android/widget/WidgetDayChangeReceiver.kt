package com.taska.android.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class WidgetDayChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_REFRESH_FOR_NEW_DAY) TaskWidgetRefresh.request(context)
    }

    companion object {
        const val ACTION_REFRESH_FOR_NEW_DAY = "com.taska.android.widget.REFRESH_FOR_NEW_DAY"
    }
}
