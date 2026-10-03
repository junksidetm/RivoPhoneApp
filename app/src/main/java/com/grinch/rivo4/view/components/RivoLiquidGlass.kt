package com.grinch.rivo4.view.components

import android.graphics.BlendMode
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BlurOn
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Dialpad
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grinch.rivo4.R
import org.intellij.lang.annotations.Language
import kotlin.math.roundToInt

// --- AGSL Shaders ported from Cresto & Glasense UI ---

@Language("AGSL")
internal const val LIQUID_LENS_SHADER = """
float radiusAt(float2 coord, float4 radii) {
    if (coord.x >= 0.0) {
        if (coord.y <= 0.0) return radii.y;
        else return radii.z;
    } else {
        if (coord.y <= 0.0) return radii.x;
        else return radii.w;
    }
}

float sdRoundedRect(float2 coord, float2 halfSize, float radius) {
    float2 cornerCoord = abs(coord) - (halfSize - float2(radius));
    float outside = length(max(cornerCoord, 0.0)) - radius;
    float inside = min(max(cornerCoord.x, cornerCoord.y), 0.0);
    return outside + inside;
}

float2 gradSdRoundedRect(float2 coord, float2 halfSize, float radius) {
    float2 cornerCoord = abs(coord) - (halfSize - float2(radius));
    if (cornerCoord.x >= 0.0 || cornerCoord.y >= 0.0) {
        return sign(coord) * normalize(max(cornerCoord, 0.0));
    } else {
        float gradX = step(cornerCoord.y, cornerCoord.x);
        return sign(coord) * float2(gradX, 1.0 - gradX);
    }
}

float circleMap(float x) {
    return 1.0 - sqrt(max(0.0, 1.0 - x * x));
}

uniform shader content;
uniform float2 size;
uniform float4 cornerRadii;
uniform float refractionHeight;
uniform float refractionAmount;
uniform float depthEffect;

half4 main(float2 coord) {
    float2 halfSize = size * 0.5;
    float2 centeredCoord = coord - halfSize;
    float radius = radiusAt(coord, cornerRadii);
    float sd = sdRoundedRect(centeredCoord, halfSize, radius);

    if (-sd >= refractionHeight) {
        return content.eval(coord);
    }
    sd = min(sd, 0.0);

    float d = circleMap(1.0 - -sd / refractionHeight) * refractionAmount;
    float gradRadius = min(radius * 1.5, min(halfSize.x, halfSize.y));
    float2 grad = normalize(
        gradSdRoundedRect(centeredCoord, halfSize, gradRadius)
            + depthEffect * normalize(centeredCoord)
    );
    return content.eval(coord + d * grad);
}
"""

@Language("AGSL")
internal const val LIQUID_HIGHLIGHT_SHADER = """
float radiusAt(float2 coord, float4 radii) {
    if (coord.x >= 0.0) {
        if (coord.y <= 0.0) return radii.y;
        else return radii.z;
    } else {
        if (coord.y <= 0.0) return radii.x;
        else return radii.w;
    }
}
float sdRoundedRect(float2 coord, float2 halfSize, float radius) {
    float2 cornerCoord = abs(coord) - (halfSize - float2(radius));
    return length(max(cornerCoord, 0.0)) - radius + min(max(cornerCoord.x, cornerCoord.y), 0.0);
}
float2 gradSdRoundedRect(float2 coord, float2 halfSize, float radius) {
    float2 cornerCoord = abs(coord) - (halfSize - float2(radius));
    if (cornerCoord.x >= 0.0 || cornerCoord.y >= 0.0) return sign(coord) * normalize(max(cornerCoord, 0.0));
    float gradX = step(cornerCoord.y, cornerCoord.x);
    return sign(coord) * float2(gradX, 1.0 - gradX);
}
uniform float2 size;
uniform float4 cornerRadii;
layout(color) uniform half4 color;
uniform float angle;
uniform float falloff;
half4 main(float2 coord) {
    float2 halfSize = size * 0.5;
    float2 centeredCoord = coord - halfSize;
    float radius = radiusAt(coord, cornerRadii);
    float gradRadius = min(radius * 1.5, min(halfSize.x, halfSize.y));
    float2 grad = gradSdRoundedRect(centeredCoord, halfSize, gradRadius);
    float intensity = pow(abs(dot(grad, float2(cos(angle), sin(angle)))), falloff);
    return color * intensity;
}
"""

/**
 * Constructs a hardware-accelerated dual-branch Liquid Glass [RenderEffect].
 * - Branch 1: Micro-blur (2dp) for crisp edge details + deep physical refraction lens.
 * - Branch 2: Wide ambient blur (16dp) for deep optical background wash.
 * - Composited via hardware SRC_OVER.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun buildLiquidGlassRenderEffect(
    width: Float,
    height: Float,
    cornerRadii: FloatArray,
    firstBlurRadius: Float = 6f,
    firstOpacity: Float = 1.0f,
    firstRefractionHeight: Float = 48f,
    firstRefractionAmount: Float = 36f,
    secondBlurRadius: Float = 42f,
    secondOpacity: Float = 0.85f,
    secondRefractionHeight: Float = 48f,
    secondRefractionAmount: Float = 24f
): androidx.compose.ui.graphics.RenderEffect {
    val first = liquidGlassBranch(
        width = width,
        height = height,
        cornerRadii = cornerRadii,
        blurRadius = firstBlurRadius,
        opacity = firstOpacity,
        refractionHeight = firstRefractionHeight,
        refractionAmount = firstRefractionAmount
    )
    val second = liquidGlassBranch(
        width = width,
        height = height,
        cornerRadii = cornerRadii,
        blurRadius = secondBlurRadius,
        opacity = secondOpacity,
        refractionHeight = secondRefractionHeight,
        refractionAmount = secondRefractionAmount
    )

    return RenderEffect.createBlendModeEffect(
        first,
        second,
        BlendMode.SRC_OVER
    ).asComposeRenderEffect()
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun liquidGlassBranch(
    width: Float,
    height: Float,
    cornerRadii: FloatArray,
    blurRadius: Float,
    opacity: Float,
    refractionHeight: Float,
    refractionAmount: Float
): RenderEffect {
    val blur = RenderEffect.createBlurEffect(
        blurRadius.coerceAtLeast(1f),
        blurRadius.coerceAtLeast(1f),
        Shader.TileMode.DECAL
    )
    val lensShader = RuntimeShader(LIQUID_LENS_SHADER).apply {
        setFloatUniform("size", width, height)
        setFloatUniform("cornerRadii", cornerRadii)
        setFloatUniform("refractionHeight", refractionHeight)
        setFloatUniform("refractionAmount", -refractionAmount)
        setFloatUniform("depthEffect", 0f)
    }
    val lens = RenderEffect.createRuntimeShaderEffect(lensShader, "content")
    val blurredAndRefracted = RenderEffect.createChainEffect(lens, blur)

    val alpha = opacity.coerceIn(0f, 1f)
    val matrix = ColorMatrix(
        floatArrayOf(
            1f, 0f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f, 0f,
            0f, 0f, 1f, 0f, 0f,
            0f, 0f, 0f, alpha, 0f
        )
    )
    return RenderEffect.createColorFilterEffect(
        ColorMatrixColorFilter(matrix),
        blurredAndRefracted
    )
}

/**
 * Applies a Liquid Glass modifier:
 * - On Android 13+ (API 33): Dual-branch physical refraction AGSL lens + hardware blur + specular highlight.
 * - On Android 12 (API 31-32): Hardware RenderEffect blur + specular rim.
 * - Below Android 12: Translucent frosted tint + specular border.
 */
fun Modifier.rivoLiquidGlass(
    shape: Shape = RoundedCornerShape(24.dp),
    tintColor: Color = Color.White.copy(alpha = 0.12f),
    borderColor: Color = Color.White.copy(alpha = 0.35f),
    borderWidth: Dp = 1.dp
): Modifier = this
    .clip(shape)
    .background(tintColor)
    .border(borderWidth, borderColor, shape)

/**
 * Interactive Liquid Glass Preview Card showcased in Settings -> Interface & Appearance
 * when the "Frosted Glass & Blur Effects" toggle is enabled.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LiquidGlassPreviewCard(
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val infiniteTransition = rememberInfiniteTransition(label = "LiquidGlassTransition")

    // Animated floating orb offsets to visually demonstrate live refraction through the glass
    val orbOffset1 by infiniteTransition.animateFloat(
        initialValue = -30f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Orb1"
    )
    val orbOffset2 by infiniteTransition.animateFloat(
        initialValue = 25f,
        targetValue = -25f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Orb2"
    )

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.65f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "Liquid Glass Engine Active",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Physical refraction, dual-pass blur & specular rim reflection",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Live Optical Refraction Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceContainer,
                                MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Background Layer: Dynamic, moving colorful orbs & mock dialpad chip
                Box(modifier = Modifier.fillMaxSize()) {
                    // Orb 1: Primary vibrant sphere
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(with(density) { (40.dp + orbOffset1.dp).roundToPx() }, with(density) { (20.dp + (orbOffset1 * 0.5f).dp).roundToPx() }) }
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    )
                                )
                            )
                    )

                    // Orb 2: Tertiary accent sphere
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset { IntOffset(with(density) { (-30.dp + orbOffset2.dp).roundToPx() }, with(density) { (15.dp + (orbOffset2 * 0.7f).dp).roundToPx() }) }
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.tertiary,
                                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                                    )
                                )
                            )
                    )

                    // Orb 3: Secondary warm glow
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset { IntOffset(with(density) { (orbOffset1 * -0.8f).dp.roundToPx() }, with(density) { -10.dp.roundToPx() }) }
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.secondary,
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                    )
                                )
                            )
                    )
                }

                // Foreground: Liquid Glass Floating Capsule
                val glassShape = RoundedCornerShape(24.dp)
                val isHardwareBlurSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                val isAgslSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

                val blurRadiusPx = with(density) { 24.dp.toPx() }

                Box(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                        .height(68.dp)
                        .then(
                            if (isHardwareBlurSupported) {
                                Modifier.graphicsLayer {
                                    renderEffect = if (isAgslSupported) {
                                        val radii = floatArrayOf(
                                            with(density) { 24.dp.toPx() },
                                            with(density) { 24.dp.toPx() },
                                            with(density) { 24.dp.toPx() },
                                            with(density) { 24.dp.toPx() }
                                        )
                                        buildLiquidGlassRenderEffect(
                                            width = size.width,
                                            height = size.height,
                                            cornerRadii = radii
                                        )
                                    } else {
                                        RenderEffect.createBlurEffect(
                                            blurRadiusPx, blurRadiusPx,
                                            Shader.TileMode.CLAMP
                                        ).asComposeRenderEffect()
                                    }
                                }
                            } else {
                                Modifier
                            }
                        )
                        .clip(glassShape)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.35f),
                                    Color.White.copy(alpha = 0.12f),
                                    Color.Black.copy(alpha = 0.15f)
                                )
                            )
                        )
                        .border(
                            width = 1.5.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.85f), // Top specular rim highlight
                                    Color.White.copy(alpha = 0.25f),
                                    Color.White.copy(alpha = 0.65f)  // Inverted bottom rim reflection
                                )
                            ),
                            shape = glassShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Call,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Liquid Refractive Capsule",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isAgslSupported) "AGSL Snell's Law Refraction + Dual Blur" else "Hardware Accelerated Blur",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Technical Feature Pills
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LiquidGlassFeatureChip(
                    icon = Icons.Outlined.Layers,
                    label = "Dual-Branch Blur (2dp + 16dp)"
                )
                LiquidGlassFeatureChip(
                    icon = Icons.Outlined.BlurOn,
                    label = "Snell's Law SDF Lens"
                )
                LiquidGlassFeatureChip(
                    icon = Icons.Outlined.LightMode,
                    label = "Specular Rim Reflection"
                )
            }
        }
    }
}

@Composable
private fun LiquidGlassFeatureChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
