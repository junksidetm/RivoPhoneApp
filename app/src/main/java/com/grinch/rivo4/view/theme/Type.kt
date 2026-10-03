package com.grinch.rivo4.view.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.grinch.rivo4.R

/**
 * Creates a dynamic [FontFamily] utilizing the local Google Sans Flex variable font
 * with configurable axes matching the Material 3 Expressive and Wallet-Flutter specifications.
 */
@OptIn(ExperimentalTextApi::class)
fun createGoogleSansFlexFamily(
    weight: Int = 400,
    width: Float = 100f,
    grade: Float = 50f,
    roundness: Float = 100f,
    opticalSize: Float = 12f,
    slant: Float = 0f
): FontFamily {
    val clampedWeight = weight.coerceIn(100, 1000)
    val clampedWidth = width.coerceIn(50f, 150f)
    val clampedGrade = grade.coerceIn(-200f, 150f)
    val clampedRoundness = roundness.coerceIn(0f, 100f)
    val clampedOpticalSize = opticalSize.coerceIn(8f, 144f)
    val clampedSlant = slant.coerceIn(-10f, 0f)

    return FontFamily(
        Font(
            resId = R.font.google_sans_flex,
            weight = FontWeight(clampedWeight),
            variationSettings = FontVariation.Settings(
                FontVariation.weight(clampedWeight),
                FontVariation.width(clampedWidth),
                FontVariation.slant(clampedSlant),
                FontVariation.Setting("GRAD", clampedGrade),
                FontVariation.Setting("ROND", clampedRoundness),
                FontVariation.Setting("opsz", clampedOpticalSize)
            )
        )
    )
}

val DefaultGoogleSansFlexFamily: FontFamily = createGoogleSansFlexFamily()

private fun rivoTextStyle(
    fontSize: Float,
    lineHeight: Float,
    letterSpacing: Float,
    fontWeight: FontWeight,
    fontFamily: FontFamily
): TextStyle = TextStyle(
    fontFamily = fontFamily,
    fontWeight = fontWeight,
    fontSize = fontSize.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp
)

fun createRivoTypography(
    fontFamily: FontFamily = DefaultGoogleSansFlexFamily
): Typography = Typography(
    displayLarge = rivoTextStyle(57f, 64f, -0.2f, FontWeight.Normal, fontFamily),
    displayMedium = rivoTextStyle(45f, 52f, 0f, FontWeight.Normal, fontFamily),
    displaySmall = rivoTextStyle(36f, 44f, 0f, FontWeight.Normal, fontFamily),
    headlineLarge = rivoTextStyle(32f, 40f, 0f, FontWeight.Normal, fontFamily),
    headlineMedium = rivoTextStyle(28f, 36f, 0f, FontWeight.Normal, fontFamily),
    headlineSmall = rivoTextStyle(24f, 32f, 0f, FontWeight.Normal, fontFamily),
    titleLarge = rivoTextStyle(22f, 28f, 0f, FontWeight.Normal, fontFamily),
    titleMedium = rivoTextStyle(16f, 24f, 0.2f, FontWeight.Medium, fontFamily),
    titleSmall = rivoTextStyle(14f, 20f, 0.1f, FontWeight.Medium, fontFamily),
    bodyLarge = rivoTextStyle(16f, 24f, 0.5f, FontWeight.Normal, fontFamily),
    bodyMedium = rivoTextStyle(14f, 20f, 0.2f, FontWeight.Normal, fontFamily),
    bodySmall = rivoTextStyle(12f, 16f, 0.4f, FontWeight.Normal, fontFamily),
    labelLarge = rivoTextStyle(14f, 20f, 0.1f, FontWeight.Medium, fontFamily),
    labelMedium = rivoTextStyle(12f, 16f, 0.5f, FontWeight.Medium, fontFamily),
    labelSmall = rivoTextStyle(11f, 16f, 0.5f, FontWeight.Medium, fontFamily),
    displayLargeEmphasized = rivoTextStyle(57f, 64f, 0f, FontWeight.Medium, fontFamily),
    displayMediumEmphasized = rivoTextStyle(45f, 52f, 0f, FontWeight.Medium, fontFamily),
    displaySmallEmphasized = rivoTextStyle(36f, 44f, 0f, FontWeight.Medium, fontFamily),
    headlineLargeEmphasized = rivoTextStyle(32f, 40f, 0f, FontWeight.Medium, fontFamily),
    headlineMediumEmphasized = rivoTextStyle(28f, 36f, 0f, FontWeight.Medium, fontFamily),
    headlineSmallEmphasized = rivoTextStyle(24f, 32f, 0f, FontWeight.Medium, fontFamily),
    titleLargeEmphasized = rivoTextStyle(22f, 28f, 0f, FontWeight.Medium, fontFamily),
    titleMediumEmphasized = rivoTextStyle(16f, 24f, 0.15f, FontWeight.Bold, fontFamily),
    titleSmallEmphasized = rivoTextStyle(14f, 20f, 0.1f, FontWeight.Bold, fontFamily),
    bodyLargeEmphasized = rivoTextStyle(16f, 24f, 0.15f, FontWeight.Medium, fontFamily),
    bodyMediumEmphasized = rivoTextStyle(14f, 20f, 0.25f, FontWeight.Medium, fontFamily),
    bodySmallEmphasized = rivoTextStyle(12f, 16f, 0.4f, FontWeight.Medium, fontFamily),
    labelLargeEmphasized = rivoTextStyle(14f, 20f, 0.1f, FontWeight.Bold, fontFamily),
    labelMediumEmphasized = rivoTextStyle(12f, 16f, 0.5f, FontWeight.Bold, fontFamily),
    labelSmallEmphasized = rivoTextStyle(11f, 16f, 0.5f, FontWeight.Bold, fontFamily)
)

val RivoTypography: Typography = createRivoTypography(DefaultGoogleSansFlexFamily)
