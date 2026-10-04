package com.grinch.rivo4.view.screen.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FontDownload
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grinch.rivo4.controller.util.PreferenceManager
import com.grinch.rivo4.view.components.MenuTopAppBar
import com.grinch.rivo4.view.components.RivoExpressiveCard
import com.grinch.rivo4.view.components.RivoSwitchListItem
import com.grinch.rivo4.view.theme.createGoogleSansFlexFamily
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun TypographySettingsScreen(
    navigator: DestinationsNavigator
) {
    val prefs = koinInject<PreferenceManager>()
    val settingsState by prefs.settingsChanged.collectAsState()

    var useGoogleSans by remember(settingsState) {
        mutableStateOf(prefs.isGoogleSansFlexEnabled())
    }
    var fontWeight by remember(settingsState) {
        mutableIntStateOf(prefs.getFontWeight())
    }
    var fontWidth by remember(settingsState) {
        mutableFloatStateOf(prefs.getFontWidth())
    }
    var fontGrade by remember(settingsState) {
        mutableFloatStateOf(prefs.getFontGrade())
    }
    var fontRoundness by remember(settingsState) {
        mutableFloatStateOf(prefs.getFontRoundness())
    }
    var fontOpticalSize by remember(settingsState) {
        mutableFloatStateOf(prefs.getFontOpticalSize())
    }
    var fontSlant by remember(settingsState) {
        mutableFloatStateOf(prefs.getFontSlant())
    }

    Scaffold(
        topBar = {
            MenuTopAppBar(
                text = "Typography",
                navigator = navigator
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Preview at top
            item {
                RivoTypeTester(
                    useGoogleSans = useGoogleSans,
                    weight = fontWeight,
                    width = fontWidth,
                    grade = fontGrade,
                    roundness = fontRoundness,
                    opticalSize = fontOpticalSize,
                    slant = fontSlant
                )
            }

            // 2. Switch to turn Google Sans Flex on/off just below it
            item {
                RivoExpressiveCard {
                    RivoSwitchListItem(
                        headline = "Google Sans Flex",
                        supporting = "Enable variable weight, width, and optical axes optimizations",
                        leadingIcon = Icons.Outlined.FontDownload,
                        checked = useGoogleSans,
                        onCheckedChange = { enabled ->
                            useGoogleSans = enabled
                            prefs.setGoogleSansFlexEnabled(enabled)
                        }
                    )
                }
            }

            // 3. All sliders
            item {
                AnimatedVisibility(
                    visible = useGoogleSans,
                    enter = expandVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                    exit = shrinkVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy)) + fadeOut()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        RivoTypographySliderTile(
                            label = "Grade",
                            code = "GRAD",
                            value = fontGrade,
                            min = -200f,
                            max = 150f,
                            displayValue = fontGrade.toInt().toString(),
                            onValueChange = {
                                fontGrade = it
                                prefs.setFontGrade(it)
                            }
                        )
                        RivoTypographySliderTile(
                            label = "Weight",
                            code = "wght",
                            value = fontWeight.toFloat(),
                            min = 100f,
                            max = 1000f,
                            displayValue = fontWeight.toString(),
                            onValueChange = {
                                fontWeight = it.toInt()
                                prefs.setFontWeight(it.toInt())
                            }
                        )
                        RivoTypographySliderTile(
                            label = "Width",
                            code = "wdth",
                            value = fontWidth,
                            min = 50f,
                            max = 150f,
                            displayValue = "${fontWidth.toInt()}%",
                            onValueChange = {
                                fontWidth = it
                                prefs.setFontWidth(it)
                            }
                        )
                        RivoTypographySliderTile(
                            label = "Roundness",
                            code = "ROND",
                            value = fontRoundness,
                            min = 0f,
                            max = 100f,
                            displayValue = "${fontRoundness.toInt()}%",
                            onValueChange = {
                                fontRoundness = it
                                prefs.setFontRoundness(it)
                            }
                        )
                        RivoTypographySliderTile(
                            label = "Optical Size",
                            code = "opsz",
                            value = fontOpticalSize,
                            min = 8f,
                            max = 144f,
                            displayValue = "${fontOpticalSize.toInt()}pt",
                            onValueChange = {
                                fontOpticalSize = it
                                prefs.setFontOpticalSize(it)
                            }
                        )
                        RivoTypographySliderTile(
                            label = "Slant",
                            code = "slnt",
                            value = fontSlant,
                            min = -10f,
                            max = 0f,
                            displayValue = "${fontSlant.toInt()}°",
                            onValueChange = {
                                fontSlant = it
                                prefs.setFontSlant(it)
                            }
                        )
                    }
                }
            }

            // 4. Reset button at the very bottom
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    FilledTonalButton(
                        onClick = {
                            prefs.resetTypography()
                            useGoogleSans = true
                            fontWeight = PreferenceManager.DEFAULT_FONT_WEIGHT
                            fontWidth = PreferenceManager.DEFAULT_FONT_WIDTH
                            fontGrade = PreferenceManager.DEFAULT_FONT_GRADE
                            fontRoundness = PreferenceManager.DEFAULT_FONT_ROUNDNESS
                            fontOpticalSize = PreferenceManager.DEFAULT_FONT_OPTICAL_SIZE
                            fontSlant = PreferenceManager.DEFAULT_FONT_SLANT
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(
                            Icons.Outlined.RestartAlt,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Reset Typography",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RivoTypeTester(
    useGoogleSans: Boolean,
    weight: Int,
    width: Float,
    grade: Float,
    roundness: Float,
    opticalSize: Float,
    slant: Float
) {
    val colorScheme = MaterialTheme.colorScheme
    val previewFontFamily = remember(useGoogleSans, weight, width, grade, roundness, opticalSize, slant) {
        if (useGoogleSans) {
            createGoogleSansFlexFamily(weight, width, grade, roundness, opticalSize, slant)
        } else {
            androidx.compose.ui.text.font.FontFamily.Default
        }
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = colorScheme.primaryContainer.copy(alpha = 0.25f),
        border = BorderStroke(1.dp, colorScheme.primaryContainer.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = colorScheme.primary,
                    contentColor = colorScheme.onPrimary,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.FontDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Google Sans Flex Preview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = previewFontFamily
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "The quick brown fox jumps over the lazy dog 1234567890",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                fontFamily = previewFontFamily,
                color = colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Variable axes: GRAD ${grade.toInt()}, wght $weight, wdth ${width.toInt()}%, ROND ${roundness.toInt()}%, opsz ${opticalSize.toInt()}pt, slnt ${slant.toInt()}°",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = previewFontFamily,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RivoTypographySliderTile(
    label: String,
    code: String,
    value: Float,
    min: Float,
    max: Float,
    displayValue: String,
    onValueChange: (Float) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = colorScheme.surfaceContainerHighest.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = code,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colorScheme.primaryContainer
                ) {
                    Text(
                        text = displayValue,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        fontWeight = FontWeight.ExtraBold,
                        color = colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Slider(
                value = value.coerceIn(min, max),
                onValueChange = onValueChange,
                valueRange = min..max,
                colors = SliderDefaults.colors(
                    thumbColor = colorScheme.primary,
                    activeTrackColor = colorScheme.primary,
                    inactiveTrackColor = colorScheme.surfaceContainerHighest
                )
            )
        }
    }
}
