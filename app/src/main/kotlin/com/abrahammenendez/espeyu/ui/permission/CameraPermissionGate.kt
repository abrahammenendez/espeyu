// SPDX-FileCopyrightText: 2026 Abraham Menéndez
// SPDX-License-Identifier: AGPL-3.0-or-later

package com.abrahammenendez.espeyu.ui.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.abrahammenendez.espeyu.R

/** Keeps the message to a readable measure once the screen is wider than a portrait phone. */
private val MessageMaxWidth = 360.dp

/**
 * One quiet screen, then the mirror forever. A denial leads to the system settings rather than to a
 * second prompt.
 */
@Composable
fun CameraPermissionGate(content: @Composable () -> Unit) {
    val context = LocalContext.current
    var isGranted by remember { mutableStateOf(context.hasCameraPermission()) }
    var wasDenied by rememberSaveable { mutableStateOf(false) }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            isGranted = granted
            wasDenied = !granted
        }

    // Granting from the system settings does not restart the process the way revoking does, so
    // coming back is the only moment the app learns about it.
    LifecycleResumeEffect(Unit) {
        isGranted = context.hasCameraPermission()
        onPauseOrDispose {}
    }

    when {
        isGranted -> content()
        wasDenied ->
            PermissionPrompt(
                message = stringResource(R.string.permission_denied),
                actionLabel = stringResource(R.string.permission_open_settings),
                onAction = { context.startActivity(appSettingsIntent(context)) },
            )
        else ->
            PermissionPrompt(
                message = stringResource(R.string.permission_rationale),
                actionLabel = stringResource(R.string.permission_grant),
                onAction = { launcher.launch(Manifest.permission.CAMERA) },
            )
    }
}

@Composable
private fun PermissionPrompt(
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displaySmall,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = MessageMaxWidth),
            )
            Button(onClick = onAction) { Text(actionLabel) }
        }
    }
}

private fun Context.hasCameraPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED

private fun appSettingsIntent(context: Context): Intent =
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:${context.packageName}".toUri())
