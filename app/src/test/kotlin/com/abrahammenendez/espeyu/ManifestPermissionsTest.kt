// SPDX-FileCopyrightText: 2026 Abraham Menéndez
// SPDX-License-Identifier: AGPL-3.0-or-later

package com.abrahammenendez.espeyu

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * The absence of INTERNET is the strongest privacy claim Espeyu makes, and dependencies can
 * introduce permissions the source never mentions. This asserts against the merged manifest.
 */
@RunWith(RobolectricTestRunner::class)
class ManifestPermissionsTest {

    @Test
    fun `the camera is the only permission the app asks the platform for`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val declared =
            context.packageManager
                .getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
                .requestedPermissions
                .orEmpty()
                // androidx.core defines this signature permission on the app itself to keep its
                // own broadcast receivers unexported. It grants nothing to anyone else.
                .filterNot { it.startsWith(context.packageName) }

        assertEquals(listOf(Manifest.permission.CAMERA), declared)
    }
}
