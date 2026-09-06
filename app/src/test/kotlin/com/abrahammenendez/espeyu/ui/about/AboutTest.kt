// SPDX-FileCopyrightText: 2026 Abraham Menéndez
// SPDX-License-Identifier: AGPL-3.0-or-later

package com.abrahammenendez.espeyu.ui.about

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.abrahammenendez.espeyu.ui.theme.EspeyuTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AboutTest {

    @get:Rule val compose = createComposeRule()

    @Test
    fun `the mark announces what it opens rather than the app name`() {
        var opened = false
        compose.setContent { EspeyuTheme { AboutMark(onClick = { opened = true }) } }

        compose.onNodeWithContentDescription("About Espeyu").performClick()

        assertTrue(opened)
    }

    @Test
    fun `the dialog credits the licence and the source`() {
        compose.setContent { EspeyuTheme { AboutDialog(onDismiss = {}) } }

        compose.onNodeWithText("AGPL-3.0-or-later", substring = true).assertIsDisplayed()
        compose.onNodeWithText("github.com/abrahammenendez/espeyu").assertIsDisplayed()
    }

    @Test
    fun `closing the dialog reports it`() {
        var dismissed = false
        compose.setContent { EspeyuTheme { AboutDialog(onDismiss = { dismissed = true }) } }

        compose.onNodeWithText("Close").performClick()

        assertTrue(dismissed)
    }
}
