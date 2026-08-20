package com.taska.android.ui.taskdetail

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TaskDetailCompletionControlTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun activeControlIsUncheckedAccessibleAndInteractive() {
        var clicks = 0
        composeRule.setContent {
            CompletionControl(false, true, 1, { clicks++ })
        }

        composeRule.onNodeWithTag("task-detail-completion")
            .assertIsOff()
            .assertIsEnabled()
            .assertContentDescriptionEquals("Complete task")
            .performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun completedControlIsCheckedAndOffersReopenAction() {
        composeRule.setContent {
            CompletionControl(true, true, null, {})
        }

        composeRule.onNodeWithTag("task-detail-completion")
            .assertIsOn()
            .assertContentDescriptionEquals("Reopen task")
    }

    @Test
    fun pendingControlIsDisabled() {
        composeRule.setContent {
            CompletionControl(false, false, null, {})
        }

        composeRule.onNodeWithTag("task-detail-completion").assertIsNotEnabled()
    }
}
