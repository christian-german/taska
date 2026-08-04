package com.taska.android.widget

import android.content.ComponentName
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.taska.android.R
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskWidgetManifestTest {
    @Test fun `widget provider is declared with its metadata`() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val info = context.packageManager.getReceiverInfo(
            ComponentName(context, TaskWidgetProvider::class.java),
            android.content.pm.PackageManager.GET_META_DATA
        )
        assertEquals(R.xml.task_widget_info, info.metaData.getInt("android.appwidget.provider"))
    }

    @Test fun `today widget provider is declared with its metadata`() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val info = context.packageManager.getReceiverInfo(
            ComponentName(context, TodayTaskWidgetProvider::class.java),
            android.content.pm.PackageManager.GET_META_DATA
        )
        assertEquals(R.xml.today_task_widget_info, info.metaData.getInt("android.appwidget.provider"))
    }
}
