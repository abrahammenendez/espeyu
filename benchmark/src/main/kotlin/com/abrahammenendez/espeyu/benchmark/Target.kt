// SPDX-FileCopyrightText: 2026 Abraham Menéndez
// SPDX-License-Identifier: AGPL-3.0-or-later

package com.abrahammenendez.espeyu.benchmark

import androidx.benchmark.macro.MacrobenchmarkScope

const val TARGET_PACKAGE = "com.abrahammenendez.espeyu"

/** Without this the permission gate is what gets measured. */
fun MacrobenchmarkScope.grantCameraPermission() {
    device.executeShellCommand("pm grant $TARGET_PACKAGE android.permission.CAMERA")
}
