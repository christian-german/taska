package com.taska.android.ui.taskdetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class TaskMutationErrorFeedbackTest {
  @get:Rule val composeRule = createComposeRule()

  @Test
  fun mutationErrorIsDisplayedAndConsumedOnce() {
    var error by mutableStateOf<String?>("Use an explicit occurrence")
    var unrelatedRecompositions by mutableIntStateOf(0)
    var consumptions = 0

    composeRule.setContent {
      unrelatedRecompositions
      TaskMutationErrorFeedback(
        error = error,
        onConsumed = {
          consumptions++
          error = null
        },
      )
    }

    composeRule.onNodeWithText("Use an explicit occurrence").assertIsDisplayed()
    composeRule.runOnIdle {
      assertNull(error)
      assertEquals(1, consumptions)
      unrelatedRecompositions++
    }
    composeRule.runOnIdle { assertEquals(1, consumptions) }
  }
}
