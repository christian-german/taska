package com.taska.android.widget

import android.content.res.Configuration
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.ListView
import android.graphics.Typeface
import android.graphics.Color
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
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
        assertTrue(taskRow.findViewById<View>(R.id.widget_task_appointment) is ImageView)
        assertEquals(View.GONE, taskRow.findViewById<View>(R.id.widget_task_appointment).visibility)
        assertEquals(2, taskRow.findViewById<TextView>(R.id.widget_task_text).maxLines)
        assertEquals(View.GONE, root.findViewById<View>(R.id.widget_status).visibility)
    }


    @Test fun `today widget hides count status and task separators by default`() {
        val root = LayoutInflater.from(context).inflate(R.layout.task_widget, null)

        assertEquals(View.GONE, root.findViewById<View>(R.id.widget_status).visibility)
        assertEquals(View.GONE, root.findViewById<View>(R.id.widget_overdue_header).visibility)
        listOf(
            R.id.widget_divider_0, R.id.widget_divider_1, R.id.widget_divider_2,
            R.id.widget_divider_3, R.id.widget_divider_4, R.id.widget_divider_5,
            R.id.widget_divider_6,
        ).forEach { assertEquals(View.GONE, root.findViewById<View>(it).visibility) }
    }

    @Test fun `today rows provide hidden outlined appointment indicators`() {
        val root = LayoutInflater.from(context).inflate(R.layout.task_widget, null)
        val indicator = root.findViewById<ImageView>(R.id.widget_appointment_0)

        assertEquals(View.GONE, indicator.visibility)
        assertNotNull(indicator.drawable)
        assertEquals(14f * context.resources.displayMetrics.density, indicator.layoutParams.width.toFloat(), 0.01f)
        assertEquals("Appointment", context.getString(R.string.widget_appointment))
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
        assertEquals(0xFFC62828.toInt(), light.getColor(R.color.widget_overdue_ink))
        assertEquals(0xFFFF8A80.toInt(), dark.getColor(R.color.widget_overdue_ink))
        assertTrue(contrast(light.getColor(R.color.widget_overdue_ink), light.getColor(R.color.widget_surface)) >= 4.5)
        assertTrue(contrast(dark.getColor(R.color.widget_overdue_ink), dark.getColor(R.color.widget_surface)) >= 4.5)
    }

    @Test fun `overdue widget text is red and bold without changing its content`() {
        val styled = overdueWidgetText(context, "08:30  Follow up") as Spanned

        assertEquals("08:30  Follow up", styled.toString())
        assertEquals(context.getColor(R.color.widget_overdue_ink), styled.getSpans(0, styled.length, ForegroundColorSpan::class.java).single().foregroundColor)
        assertEquals(Typeface.BOLD, styled.getSpans(0, styled.length, StyleSpan::class.java).single().style)
    }

    private fun themedContext(nightMode: Int): android.content.Context {
        val configuration = Configuration(context.resources.configuration).apply {
            uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or nightMode
        }
        return context.createConfigurationContext(configuration)
    }

    private fun contrast(foreground: Int, background: Int): Double {
        fun luminance(color: Int): Double {
            fun channel(value: Int): Double {
                val normalized = value / 255.0
                return if (normalized <= 0.04045) normalized / 12.92 else Math.pow((normalized + 0.055) / 1.055, 2.4)
            }
            return 0.2126 * channel(Color.red(color)) + 0.7152 * channel(Color.green(color)) + 0.0722 * channel(Color.blue(color))
        }
        val lighter = maxOf(luminance(foreground), luminance(background))
        val darker = minOf(luminance(foreground), luminance(background))
        return (lighter + 0.05) / (darker + 0.05)
    }
}
