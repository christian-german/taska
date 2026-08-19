package com.taska.android.widget

import android.content.res.Configuration
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.ListView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.taska.android.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskWidgetDesignTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test fun `widget layout uses a rounded Taska card and circular completion controls`() {
        val root = LayoutInflater.from(context).inflate(R.layout.task_widget, null)

        val card = root.background as GradientDrawable
        assertNotNull(card)
        assertEquals(10f * context.resources.displayMetrics.density, card.cornerRadius, 0.01f)
        assertTrue(root.findViewById<View>(R.id.widget_check_0) is ImageView)
        assertTrue(root.findViewById<View>(R.id.widget_check_7) is ImageView)
    }

    @Test fun `widget supports long task titles with bounded rows`() {
        val root = LayoutInflater.from(context).inflate(R.layout.task_widget, null)
        val title = root.findViewById<TextView>(R.id.widget_task_0)

        title.text = "A deliberately long Taska task title that must remain readable without escaping the rounded widget card"

        assertEquals(2, title.maxLines)
        assertNotNull(title.ellipsize)
        assertEquals(
            context.resources.getDimensionPixelSize(R.dimen.widget_row_min_height),
            (root.findViewById<View>(R.id.widget_row_0) as ViewGroup).getChildAt(0).layoutParams.height
        )
    }

    @Test fun `today widget reuses the widget card and completed control assets`() {
        val root = LayoutInflater.from(context).inflate(R.layout.task_widget, null)

        assertNotNull(root.background as GradientDrawable)
        assertNotNull(context.getDrawable(R.drawable.widget_completion_checked))
        assertNotNull(context.getDrawable(R.drawable.widget_completion_empty))
        assertTrue(root.findViewById<View>(R.id.widget_check_0) is ImageView)
    }

    @Test fun `week widget has a scrollable list and centered date headers`() {
        val root = LayoutInflater.from(context).inflate(R.layout.week_task_widget, null)
        val header = LayoutInflater.from(context).inflate(R.layout.week_widget_date_header, null) as TextView
        val taskRow = LayoutInflater.from(context).inflate(R.layout.week_widget_task_row, null)

        assertTrue(root.findViewById<View>(R.id.widget_week_list) is ListView)
        assertEquals(android.view.Gravity.CENTER, header.gravity)
        assertTrue(taskRow.findViewById<View>(R.id.widget_task_check) is ImageView)
        assertEquals(2, taskRow.findViewById<TextView>(R.id.widget_task_text).maxLines)
    }

    @Test fun `widget color resources adapt between light and dark system themes`() {
        val light = themedContext(Configuration.UI_MODE_NIGHT_NO)
        val dark = themedContext(Configuration.UI_MODE_NIGHT_YES)

        assertEquals(0xFFFFFFFF.toInt(), light.getColor(R.color.widget_surface))
        assertEquals(0xFF17233D.toInt(), dark.getColor(R.color.widget_surface))
        assertEquals(0xFF17233D.toInt(), light.getColor(R.color.widget_primary_ink))
        assertEquals(0xFFF6F8FA.toInt(), dark.getColor(R.color.widget_primary_ink))
        assertEquals(0xFF14B37D.toInt(), light.getColor(R.color.widget_accent))
        assertEquals(0xFF14B37D.toInt(), dark.getColor(R.color.widget_accent))
    }

    private fun themedContext(nightMode: Int): android.content.Context {
        val configuration = Configuration(context.resources.configuration).apply {
            uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or nightMode
        }
        return context.createConfigurationContext(configuration)
    }
}
