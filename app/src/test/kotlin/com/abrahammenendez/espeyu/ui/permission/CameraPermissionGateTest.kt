// SPDX-FileCopyrightText: 2026 Abraham Menéndez
// SPDX-License-Identifier: AGPL-3.0-or-later

package com.abrahammenendez.espeyu.ui.permission

import android.Manifest
import android.app.Application
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

private const val MIRROR = "mirror"

@RunWith(RobolectricTestRunner::class)
class CameraPermissionGateTest {

    @get:Rule val compose = createComposeRule()

    @Test
    fun `without the permission the gate explains why it is needed`() {
        showGate()

        compose.onNodeWithText("Allow camera").assertIsDisplayed()
        compose.onNodeWithText(MIRROR).assertDoesNotExist()
    }

    @Test
    fun `with the permission the mirror is all there is`() {
        grantCamera()

        showGate()

        compose.onNodeWithText(MIRROR).assertIsDisplayed()
        compose.onNodeWithText("Allow camera").assertDoesNotExist()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `a permission granted in the system settings is picked up on the way back`() {
        val lifecycle = TestLifecycleOwner(Lifecycle.State.RESUMED, UnconfinedTestDispatcher())
        compose.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycle) {
                CameraPermissionGate { Text(MIRROR) }
            }
        }
        compose.onNodeWithText(MIRROR).assertDoesNotExist()

        lifecycle.currentState = Lifecycle.State.CREATED
        grantCamera()
        lifecycle.currentState = Lifecycle.State.RESUMED

        compose.onNodeWithText(MIRROR).assertIsDisplayed()
    }

    private fun showGate() {
        compose.setContent { CameraPermissionGate { Text(MIRROR) } }
    }

    private fun grantCamera() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        shadowOf(application).grantPermissions(Manifest.permission.CAMERA)
    }
}
