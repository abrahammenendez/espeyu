// SPDX-FileCopyrightText: 2026 Abraham Menéndez
// SPDX-License-Identifier: AGPL-3.0-or-later

package com.abrahammenendez.espeyu.ui.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import com.abrahammenendez.espeyu.BuildConfig
import com.abrahammenendez.espeyu.R

private const val SOURCE_URL = "https://github.com/abrahammenendez/espeyu"

/** Low enough to leave the reflection alone, high enough to survive a screenshot. */
private const val MARK_ALPHA = 0.55f

private val MarkPadding = 12.dp

/** Attribution and its link are one thought, so they sit closer than the blocks around them. */
private val BlockSpacing = 16.dp

private val LineSpacing = 8.dp

/**
 * The app's own glyph and the way into the about dialog. `ic_mark` carries the dark casing that
 * keeps it legible over a live preview of any colour; this only places it and fades it back.
 */
@Composable
fun AboutMark(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier.padding(MarkPadding)) {
        Icon(
            painter = painterResource(R.drawable.ic_mark),
            contentDescription = stringResource(R.string.action_about),
            modifier = Modifier.alpha(MARK_ALPHA),
            tint = Color.Unspecified,
        )
    }
}

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.about_close)) }
        },
        title = { Text(stringResource(R.string.app_name)) },
        text = {
            // The dialog's own text slot does not scroll, so a large font scale would clip
            // this rather than let the reader reach the end of it.
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(BlockSpacing),
            ) {
                Text(stringResource(R.string.about_version, BuildConfig.VERSION_NAME))
                Text(stringResource(R.string.about_summary))
                Column(verticalArrangement = Arrangement.spacedBy(LineSpacing)) {
                    Text(stringResource(R.string.about_credits))
                    Text(sourceLink())
                }
            }
        },
    )
}

@Composable
private fun sourceLink() = buildAnnotatedString {
    val styles =
        TextLinkStyles(
            SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
            )
        )
    withLink(LinkAnnotation.Url(SOURCE_URL, styles)) {
        append(SOURCE_URL.removePrefix("https://"))
    }
}
