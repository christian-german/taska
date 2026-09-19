package com.taska.android.ui.shared

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class DetachedOccurrenceBadgeTest {
  @get:Rule val composeRule = createComposeRule()

  @Test
  fun detachedOccurrenceBadgeIsVisible() {
    composeRule.setContent { DetachedOccurrenceBadge() }

    composeRule.onNodeWithText("Hors série").assertIsDisplayed()
  }
}
