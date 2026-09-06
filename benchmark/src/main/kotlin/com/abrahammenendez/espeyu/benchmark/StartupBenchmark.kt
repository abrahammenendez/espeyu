// SPDX-FileCopyrightText: 2026 Abraham Menéndez
// SPDX-License-Identifier: AGPL-3.0-or-later

package com.abrahammenendez.espeyu.benchmark

import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The number that makes "fast" true: time to full display, which the app reports the moment the
 * first camera frame reaches the screen.
 */
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {

    @get:Rule val benchmark = MacrobenchmarkRule()

    @Test
    fun coldStartToFirstFrame() =
        benchmark.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = listOf(StartupTimingMetric()),
            iterations = 10,
            startupMode = StartupMode.COLD,
            setupBlock = {
                grantCameraPermission()
                pressHome()
            },
        ) {
            startActivityAndWait()
        }
}
