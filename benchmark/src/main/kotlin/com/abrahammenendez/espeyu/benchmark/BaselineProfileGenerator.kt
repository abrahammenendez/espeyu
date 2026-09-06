// SPDX-FileCopyrightText: 2026 Abraham Menéndez
// SPDX-License-Identifier: AGPL-3.0-or-later

package com.abrahammenendez.espeyu.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule val baselineProfile = BaselineProfileRule()

    @Test
    fun generate() =
        baselineProfile.collect(packageName = TARGET_PACKAGE) {
            grantCameraPermission()
            pressHome()
            startActivityAndWait()
            device.waitForIdle()
        }
}
