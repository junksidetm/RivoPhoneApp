package com.grinch.rivo4.view.components

import com.grinch.rivo4.controller.util.ContactUtils

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Voicemail
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.graphics.shapes.RoundedPolygon
import coil.compose.AsyncImage
import com.grinch.rivo4.controller.util.PreferenceManager
import com.grinch.rivo4.view.theme.RIVO_AVATAR_SHAPE_SQUIRCLE
import com.grinch.rivo4.view.theme.RivoMaterialShapes
import com.grinch.rivo4.view.theme.RivoMotion
import com.grinch.rivo4.view.theme.rememberRivoMorphShape
import com.grinch.rivo4.view.theme.rivoAvatarShape
import org.koin.compose.koinInject
import kotlin.math.abs

@Immutable
data class RivoAvatarStyle(
    val showPicture: Boolean,
    val showFirstLetter: Boolean,
    val colorful: Boolean,
    val gradient: Boolean,
    val shapeIndex: Int,
    val shape: Shape
)

@Immutable
data class RivoAvatarColors(
    val container: Color,
    val content: Color
)

object RivoAvatarDefaults {
    const val HueCount: Int = 12

    val IconSize: Dp = 24.dp
    val BadgeSize: Dp = 18.dp
    val BadgeIconSize: Dp = 12.dp

    const val LightContainerSaturation: Float = 0.62f
    const val LightContainerLightness: Float = 0.82f
    const val LightContentSaturation: Float = 0.88f
    const val LightContentLightness: Float = 0.22f

    const val DarkContainerSaturation: Float = 0.34f
    const val DarkContainerLightness: Float = 0.26f
    const val DarkContentSaturation: Float = 0.80f
    const val DarkContentLightness: Float = 0.88f
}

val LocalRivoAvatarStyle: ProvidableCompositionLocal<RivoAvatarStyle?> =
    staticCompositionLocalOf { null }

@Composable
fun rememberRivoAvatarStyle(prefs: PreferenceManager = koinInject()): RivoAvatarStyle {
    val settingsVersion by prefs.settingsChanged.collectAsState()
    val showPicture = remember(settingsVersion) {
        prefs.getBoolean(PreferenceManager.KEY_SHOW_PICTURE, true)
    }
    val showFirstLetter = remember(settingsVersion) {
        prefs.getBoolean(PreferenceManager.KEY_SHOW_FIRST_LETTER, true)
    }
    val colorful = remember(settingsVersion) {
        prefs.getBoolean(PreferenceManager.KEY_COLORFUL_AVATARS, true)
    }
    val gradient = remember(settingsVersion) {
        prefs.getBoolean(PreferenceManager.KEY_GRADIENT_AVATARS, false)
    }
    val shapeIndex = remember(settingsVersion) {
        prefs.getInt(PreferenceManager.KEY_AVATAR_SHAPE, RIVO_AVATAR_SHAPE_SQUIRCLE)
    }
    val shape = rivoAvatarShape(shapeIndex)
    return remember(showPicture, showFirstLetter, colorful, gradient, shapeIndex, shape) {
        RivoAvatarStyle(showPicture, showFirstLetter, colorful, gradient, shapeIndex, shape)
    }
}

@Composable
fun rivoAvatarStyle(): RivoAvatarStyle {
    val provided = LocalRivoAvatarStyle.current
    if (provided != null) return provided
    return rememberRivoAvatarStyle()
}

fun rivoAvatarHueIndex(name: String): Int =
    (abs(name.hashCode().toLong()) % RivoAvatarDefaults.HueCount).toInt()

private fun hslColor(hue: Float, saturation: Float, lightness: Float): Color =
    Color(ColorUtils.HSLToColor(floatArrayOf(hue, saturation, lightness)))

/**
 * Computes an adaptive, high-contrast text/icon overlay color (crisp white or dark on-surface)
 * based on the luminance of the avatar background container color.
 *
 * Example:
 * - If container color is dark blue (low luminance), overlay text is white.
 * - If container color is yellow or light tone (high luminance), overlay text is dark.
 */
fun adaptiveAvatarContentColor(containerColor: Color): Color {
    return if (containerColor.luminance() > 0.45f) {
        Color(0xFF1C1B1F) // High-contrast dark on bright/yellow backgrounds
    } else {
        Color.White       // High-contrast white on dark blue/deep backgrounds
    }
}

private fun rivoTintedAvatarColors(name: String, dark: Boolean): RivoAvatarColors {
    val hue = rivoAvatarHueIndex(name) * (360f / RivoAvatarDefaults.HueCount)
    val isInherentlyBrightHue = hue in 40f..170f // Yellow, Lime, Amber, Green
    val container = if (dark) {
        val lightness = if (isInherentlyBrightHue) 0.68f else 0.35f
        val saturation = if (isInherentlyBrightHue) 0.75f else RivoAvatarDefaults.DarkContainerSaturation
        hslColor(
            hue,
            saturation,
            lightness
        )
    } else {
        val lightness = if (isInherentlyBrightHue) 0.76f else 0.48f
        val saturation = if (isInherentlyBrightHue) 0.85f else 0.65f
        hslColor(
            hue,
            saturation,
            lightness
        )
    }
    val content = adaptiveAvatarContentColor(container)
    return RivoAvatarColors(
        container = container,
        content = content
    )
}

@Composable
fun rivoAvatarColors(name: String, colorful: Boolean = true): RivoAvatarColors {
    val scheme = MaterialTheme.colorScheme
    val tinted = colorful && name.any { it.isLetter() }
    val dark = scheme.surface.luminance() < 0.5f
    val neutralContainer = scheme.secondaryContainer
    val neutralContent = scheme.onSecondaryContainer
    return remember(name, tinted, dark, neutralContainer, neutralContent) {
        if (tinted) {
            rivoTintedAvatarColors(name, dark)
        } else {
            RivoAvatarColors(neutralContainer, neutralContent)
        }
    }
}

private fun gradientAvatarBrush(name: String, dark: Boolean): Brush {
    val baseHue = rivoAvatarHueIndex(name) * (360f / RivoAvatarDefaults.HueCount)
    val accentHue = (baseHue + 45f) % 360f

    return if (dark) {
        Brush.linearGradient(
            colors = listOf(
                hslColor(baseHue, 0.75f, 0.42f),
                hslColor(accentHue, 0.85f, 0.26f)
            ),
            start = Offset.Zero,
            end = Offset.Infinite
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                hslColor(baseHue, 0.85f, 0.78f),
                hslColor(accentHue, 0.90f, 0.58f)
            ),
            start = Offset.Zero,
            end = Offset.Infinite
        )
    }
}

private fun gradientAvatarContentColor(name: String, dark: Boolean): Color {
    val baseHue = rivoAvatarHueIndex(name) * (360f / RivoAvatarDefaults.HueCount)
    val accentHue = (baseHue + 45f) % 360f
    val (startColor, endColor) = if (dark) {
        hslColor(baseHue, 0.75f, 0.42f) to hslColor(accentHue, 0.85f, 0.26f)
    } else {
        hslColor(baseHue, 0.85f, 0.78f) to hslColor(accentHue, 0.90f, 0.58f)
    }
    val avgLum = (startColor.luminance() + endColor.luminance()) / 2f
    return if (avgLum > 0.45f) Color(0xFF1C1B1F) else Color.White
}

private val WHITESPACE_REGEX = Regex("\\s+")

private fun contactInitials(name: String, useTwo: Boolean): String {
    val cleanName = ContactUtils.stripTitlePrefix(name)
    val letters = cleanName.filter { it.isLetter() }
    if (letters.isEmpty()) return ""
    if (!useTwo) return letters.first().uppercase()
    val words = cleanName.trim().split(WHITESPACE_REGEX).filter { it.any { c -> c.isLetter() } }
    return if (words.size >= 2) {
        words.take(2).joinToString("") { word ->
            word.first { it.isLetter() }.uppercase()
        }
    } else {
        letters.take(2).uppercase()
    }
}

@Composable
private fun rememberMorphShapeIfEnabled(
    morphEnabled: Boolean,
    morphSelected: Boolean,
    morphOnPress: Boolean,
    pressed: Boolean,
    morphStart: RoundedPolygon,
    morphEnd: RoundedPolygon
): Shape? {
    if (!morphEnabled) return null
    val morphTarget = if (morphSelected || (morphOnPress && pressed)) 1f else 0f
    val morphProgress by animateFloatAsState(
        targetValue = morphTarget,
        animationSpec = RivoMotion.shapeMorph(),
        label = "RivoAvatarMorph"
    )
    return rememberRivoMorphShape(morphStart, morphEnd) { morphProgress }
}

@Composable
fun RivoAvatar(
    name: String,
    photoUri: String? = null,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    shape: Shape? = null,
    badgeIcon: ImageVector? = null,
    badgeColor: Color? = null,
    textStyle: TextStyle = MaterialTheme.typography.titleLarge,
    style: RivoAvatarStyle = rivoAvatarStyle(),
    contentDescription: String? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    morphOnPress: Boolean = false,
    morphSelected: Boolean = false,
    morphStart: RoundedPolygon = RivoMaterialShapes.AvatarMorphStart,
    morphEnd: RoundedPolygon = RivoMaterialShapes.AvatarMorphEnd
) {
    val morphEnabled = morphOnPress || morphSelected
    val resolvedInteractionSource = if (morphEnabled || onClick != null) {
        interactionSource ?: remember { MutableInteractionSource() }
    } else {
        null
    }
    val pressed = if (morphEnabled && resolvedInteractionSource != null) {
        val pressedState by resolvedInteractionSource.collectIsPressedAsState()
        pressedState
    } else {
        false
    }

    val morphShape = rememberMorphShapeIfEnabled(
        morphEnabled = morphEnabled,
        morphSelected = morphSelected,
        morphOnPress = morphOnPress,
        pressed = pressed,
        morphStart = morphStart,
        morphEnd = morphEnd
    )

    val avatarShape = when {
        morphEnabled && morphShape != null -> morphShape
        shape != null -> shape
        else -> style.shape
    }

    val colors = rivoAvatarColors(name, style.colorful)
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val gradientBrush = if (style.gradient) {
        remember(name, dark) { gradientAvatarBrush(name, dark) }
    } else null
    val hasLetters = name.any { it.isLetter() }
    val description = contentDescription

    val rootModifier = modifier
        .then(
            if (description != null) {
                Modifier.semantics { this.contentDescription = description }
            } else {
                Modifier
            }
        )
        .then(
            if (onClick != null) {
                Modifier
                    .clip(avatarShape)
                    .clickable(
                        interactionSource = resolvedInteractionSource,
                        indication = LocalIndication.current,
                        enabled = enabled,
                        onClick = onClick
                    )
            } else {
                Modifier
            }
        )

    val backgroundModifier = if (gradientBrush != null) {
        Modifier.background(gradientBrush, avatarShape)
    } else {
        Modifier.background(colors.container, avatarShape)
    }

    val effectiveContentColor = remember(colors, style.gradient, dark, name) {
        if (style.gradient) {
            gradientAvatarContentColor(name, dark)
        } else {
            colors.content
        }
    }

    Box(modifier = rootModifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(backgroundModifier)
                .clip(avatarShape),
            contentAlignment = Alignment.Center
        ) {
            if (style.showPicture && !photoUri.isNullOrEmpty() && photoUri != "voicemail://icon") {
                AsyncImage(
                    model = photoUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = effectiveContentColor,
                    modifier = Modifier.size(RivoAvatarDefaults.IconSize)
                )
            } else if (photoUri == "voicemail://icon" || name.equals("Voicemail", ignoreCase = true) || name.equals("Messagerie vocale", ignoreCase = true) || name.equals("Poczta głosowa", ignoreCase = true)) {
                Icon(
                    imageVector = Icons.Outlined.Voicemail,
                    contentDescription = null,
                    tint = effectiveContentColor,
                    modifier = Modifier.size(RivoAvatarDefaults.IconSize)
                )
            } else if (style.showFirstLetter && hasLetters) {
                Text(
                    text = contactInitials(name, style.gradient),
                    style = textStyle,
                    color = effectiveContentColor
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = effectiveContentColor,
                    modifier = Modifier.size(RivoAvatarDefaults.IconSize)
                )
            }
        }

        if (badgeIcon != null) {
            Surface(
                modifier = Modifier
                    .size(RivoAvatarDefaults.BadgeSize)
                    .align(Alignment.BottomEnd),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = RivoElevation.Raised
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = badgeIcon,
                        contentDescription = null,
                        tint = badgeColor ?: MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(RivoAvatarDefaults.BadgeIconSize)
                    )
                }
            }
        }
    }
}
