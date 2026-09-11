// SPDX-FileCopyrightText: 2026 Abraham Menéndez
// SPDX-License-Identifier: AGPL-3.0-or-later

package com.abrahammenendez.espeyu.ui.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
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

private const val PRIVACY_POLICY_URL =
    "https://github.com/abrahammenendez/espeyu/blob/main/PRIVACY.md"

private const val PERSONAL_WEBSITE_URL = "https://abrahammenendez.com"

/** Low enough to leave the reflection alone, high enough to spot in a screenshot. */
private const val MARK_ALPHA = 0.26f

/** Carries the pale glyph on a bright frame, where it would otherwise wash out. */
private const val MARK_SHADOW_ALPHA = 0.16f

private val MarkShadow = 1.dp

private val MarkPadding = 12.dp

private val BlockSpacing = 16.dp

/** The app's own glyph, and the way into the about dialog. */
@Composable
fun AboutMark(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val mark = painterResource(R.drawable.ic_mark)
    IconButton(onClick = onClick, modifier = modifier.padding(MarkPadding)) {
        Box {
            Icon(
                painter = mark,
                contentDescription = null,
                modifier = Modifier.offset(MarkShadow, MarkShadow),
                tint = Color.Black.copy(alpha = MARK_SHADOW_ALPHA),
            )
            Icon(
                painter = mark,
                contentDescription = stringResource(R.string.action_about),
                tint = Color.White.copy(alpha = MARK_ALPHA),
            )
        }
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
                Text(stringResource(R.string.about_summary))
                Text(stringResource(R.string.about_version, BuildConfig.VERSION_NAME))
                Text(authorLine())
                Text(sourceLine())
                Text(privacyPolicyLine())
                Text(stringResource(R.string.about_license))
            }
        },
    )
}

/** Label and link render as one paragraph so the URL follows the name and wraps only if it must. */
@Composable
private fun authorLine() = buildAnnotatedString {
    append(stringResource(R.string.about_author))
    append(" ")
    withLink(LinkAnnotation.Url(PERSONAL_WEBSITE_URL, linkStyles())) {
        append(PERSONAL_WEBSITE_URL.removePrefix("https://"))
    }
}

@Composable
private fun sourceLine() = buildAnnotatedString {
    append(stringResource(R.string.about_source))
    append(" ")
    withLink(LinkAnnotation.Url(SOURCE_URL, linkStyles())) {
        append(SOURCE_URL.removePrefix("https://"))
    }
}

@Composable
private fun privacyPolicyLine() = buildAnnotatedString {
    append(stringResource(R.string.about_privacy_policy))
    append(" ")
    withLink(LinkAnnotation.Url(PRIVACY_POLICY_URL, linkStyles())) {
        append(PRIVACY_POLICY_URL.removePrefix("https://"))
    }
}

@Composable
private fun linkStyles() =
    TextLinkStyles(
        SpanStyle(
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline,
        )
    )
