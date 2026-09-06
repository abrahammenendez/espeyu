// SPDX-FileCopyrightText: 2026 Abraham Menéndez
// SPDX-License-Identifier: AGPL-3.0-or-later

package com.abrahammenendez.espeyu

import androidx.compose.runtime.Composable
import com.abrahammenendez.espeyu.ui.mirror.MirrorScreen
import com.abrahammenendez.espeyu.ui.permission.CameraPermissionGate
import com.abrahammenendez.espeyu.ui.theme.EspeyuTheme

@Composable
fun EspeyuApp() {
    EspeyuTheme { CameraPermissionGate { MirrorScreen() } }
}
