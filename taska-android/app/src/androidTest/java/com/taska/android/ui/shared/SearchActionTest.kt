package com.taska.android.ui.shared

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import org.junit.Rule
import org.junit.Test

class SearchActionTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun searchActionIsAccessibleAndClickable() {
        composeRule.setContent { SearchAction(onClick = {}) }
        composeRule.onNodeWithContentDescription("Rechercher des tâches")
            .assertIsDisplayed()
            .assertHasClickAction()
    }
}
