// SPDX-FileCopyrightText: 2026 Abraham Menéndez
// SPDX-License-Identifier: AGPL-3.0-or-later

package com.abrahammenendez.espeyu.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.abrahammenendez.espeyu.R

// Variable fonts: each FontWeight drives the wght axis. minSdk 26 is Android O, where variable
// fonts are supported, so there is no pre-O static fallback to declare.
private val Inter =
    FontFamily(
        Font(R.font.inter_variable, FontWeight.Normal),
        Font(R.font.inter_variable, FontWeight.Medium),
        Font(R.font.inter_variable, FontWeight.SemiBold),
    )

private val Buenard = FontFamily(Font(R.font.buenard_variable, FontWeight.Normal))

private val baseline = Typography()

/**
 * Inter for headings and interface labels, Buenard for running text. Metrics stay as Material tuned
 * them; the display and headline styles move to SemiBold, which carries at Inter's proportions
 * where Regular reads thin next to the Buenard body.
 */
internal val EspeyuTypography =
    Typography(
        displayLarge =
            baseline.displayLarge.copy(fontFamily = Inter, fontWeight = FontWeight.SemiBold),
        displayMedium =
            baseline.displayMedium.copy(fontFamily = Inter, fontWeight = FontWeight.SemiBold),
        displaySmall =
            baseline.displaySmall.copy(fontFamily = Inter, fontWeight = FontWeight.SemiBold),
        headlineLarge =
            baseline.headlineLarge.copy(fontFamily = Inter, fontWeight = FontWeight.SemiBold),
        headlineMedium =
            baseline.headlineMedium.copy(fontFamily = Inter, fontWeight = FontWeight.SemiBold),
        headlineSmall =
            baseline.headlineSmall.copy(fontFamily = Inter, fontWeight = FontWeight.SemiBold),
        titleLarge = baseline.titleLarge.copy(fontFamily = Inter),
        titleMedium = baseline.titleMedium.copy(fontFamily = Inter),
        titleSmall = baseline.titleSmall.copy(fontFamily = Inter),
        bodyLarge = baseline.bodyLarge.copy(fontFamily = Buenard),
        bodyMedium = baseline.bodyMedium.copy(fontFamily = Buenard),
        bodySmall = baseline.bodySmall.copy(fontFamily = Buenard),
        labelLarge = baseline.labelLarge.copy(fontFamily = Inter),
        labelMedium = baseline.labelMedium.copy(fontFamily = Inter),
        labelSmall = baseline.labelSmall.copy(fontFamily = Inter),
    )
