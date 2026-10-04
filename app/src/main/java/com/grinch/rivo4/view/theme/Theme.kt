package com.grinch.rivo4.view.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import com.grinch.rivo4.controller.util.PreferenceManager
import com.grinch.rivo4.view.components.LocalRivoSurfaceStyle
import com.grinch.rivo4.view.components.rememberRivoSurfaceStyle
import org.koin.compose.koinInject

const val KEY_CUSTOM_PRIMARY_COLOR = "custom_primary_color"
const val CUSTOM_PRIMARY_COLOR_UNSET: Int = -1

val LocalNavBarStyle: ProvidableCompositionLocal<Int> =
    staticCompositionLocalOf { PreferenceManager.NAV_BAR_STYLE_STANDARD }
 
private val DYNAMIC_VARIANT_SEEDS = mapOf(
    0 to 0xFF6750A4.toInt(), // Tonal Spot
    1 to 0xFF7E5265.toInt(), // Expressive
    2 to 0xFF5F5E5E.toInt(), // Neutral
    3 to 0xFF0061A4.toInt(), // Vibrant
    4 to 0xFF006A60.toInt(), // Fruit Salad
    5 to 0xFF984061.toInt(), // Rainbow
    6 to 0xFF436916.toInt(), // Content
    7 to 0xFF006874.toInt(), // Fidelity
    8 to 0xFF303030.toInt(), // Monochrome
    9 to 0xFF9C4146.toInt(), // Big Clock
    10 to 0xFFB52750.toInt(), // Candy
    11 to 0xFF00658E.toInt(), // Deep Ocean
    12 to 0xFF8B5000.toInt()  // Sunset Glow
)

@Composable
fun Rivo4Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    prefs: PreferenceManager = koinInject(),
    content: @Composable () -> Unit
) {
    val settingsVersion by prefs.settingsChanged.collectAsState()
    val context = LocalContext.current

    val themeMode = remember(settingsVersion) {
        prefs.getThemeMode()
    }
    val effectiveDarkTheme = remember(themeMode, darkTheme) {
        when (themeMode) {
            PreferenceManager.THEME_MODE_LIGHT -> false
            PreferenceManager.THEME_MODE_DARK -> true
            else -> darkTheme
        }
    }

    val dynamicColor = remember(settingsVersion) {
        prefs.getBoolean(PreferenceManager.KEY_DYNAMIC_COLORS, true)
    }
    val amoledMode = remember(settingsVersion) {
        prefs.getBoolean(PreferenceManager.KEY_AMOLED_MODE, false)
    }
    val customPrimaryInt = remember(settingsVersion) {
        prefs.getInt(KEY_CUSTOM_PRIMARY_COLOR, CUSTOM_PRIMARY_COLOR_UNSET)
    }
    val cardRoundness = remember(settingsVersion) {
        prefs.getInt(PreferenceManager.KEY_CARD_ROUNDNESS, RivoShapeDefaults.DefaultRoundness).coerceAtLeast(5)
    }

    val dynamicVariant = remember(settingsVersion) {
        prefs.getDynamicColorVariant()
    }

    val colorScheme = remember(dynamicColor, dynamicVariant, amoledMode, customPrimaryInt, effectiveDarkTheme) {
        val base = when {
            dynamicColor && dynamicVariant == 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
                if (effectiveDarkTheme) androidx.compose.material3.dynamicDarkColorScheme(context) else androidx.compose.material3.dynamicLightColorScheme(context)

            dynamicColor && dynamicVariant > 0 ->
                rivoColorSchemeFromSeed(DYNAMIC_VARIANT_SEEDS[dynamicVariant] ?: 0xFF6750A4.toInt(), effectiveDarkTheme)

            customPrimaryInt != CUSTOM_PRIMARY_COLOR_UNSET ->
                rivoColorSchemeFromSeed(customPrimaryInt, effectiveDarkTheme)

            effectiveDarkTheme -> RivoDarkColorScheme
            else -> RivoLightColorScheme
        }
        if (effectiveDarkTheme && amoledMode) base.toAmoledColorScheme() else base
    }

    val shapes = remember(cardRoundness) { rivoShapes(cardRoundness) }
    val callColors = remember(colorScheme, effectiveDarkTheme) { rivoCallColors(colorScheme, effectiveDarkTheme) }

    val navBarStyle = remember(settingsVersion) {
        prefs.getInt(PreferenceManager.KEY_NAV_BAR_STYLE, PreferenceManager.NAV_BAR_STYLE_STANDARD)
    }

    val surfaceStyle = rememberRivoSurfaceStyle(prefs)

    val useGoogleSans = remember(settingsVersion) {
        prefs.isGoogleSansFlexEnabled()
    }
    val fontWeight = remember(settingsVersion) {
        prefs.getFontWeight()
    }
    val fontWidth = remember(settingsVersion) {
        prefs.getFontWidth()
    }
    val fontGrade = remember(settingsVersion) {
        prefs.getFontGrade()
    }
    val fontRoundness = remember(settingsVersion) {
        prefs.getFontRoundness()
    }
    val fontOpticalSize = remember(settingsVersion) {
        prefs.getFontOpticalSize()
    }
    val fontSlant = remember(settingsVersion) {
        prefs.getFontSlant()
    }

    val appFontFamily = remember(useGoogleSans, fontWeight, fontWidth, fontGrade, fontRoundness, fontOpticalSize, fontSlant) {
        if (useGoogleSans) {
            createGoogleSansFlexFamily(
                weight = fontWeight,
                width = fontWidth,
                grade = fontGrade,
                roundness = fontRoundness,
                opticalSize = fontOpticalSize,
                slant = fontSlant
            )
        } else {
            androidx.compose.ui.text.font.FontFamily.Default
        }
    }

    val dynamicTypography = remember(appFontFamily) {
        createRivoTypography(appFontFamily)
    }

    CompositionLocalProvider(
        LocalCallColors provides callColors,
        LocalCardRoundness provides cardRoundness,
        LocalNavBarStyle provides navBarStyle,
        LocalRivoSurfaceStyle provides surfaceStyle
    ) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            motionScheme = MotionScheme.expressive(),
            shapes = shapes,
            typography = dynamicTypography,
            content = content
        )
    }
}
